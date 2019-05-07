package de.hbznrw.ygor.processing

import com.google.common.base.Throwables
import de.hbznrw.ygor.bridges.EzbBridge
import de.hbznrw.ygor.bridges.KbartBridge
import de.hbznrw.ygor.bridges.ZdbBridge
import de.hbznrw.ygor.connectors.KbartConnector
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.interfaces.ProcessorInterface
import de.hbznrw.ygor.readers.EzbReader
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import de.hbznrw.ygor.readers.ZdbReader
import groovy.util.logging.Log4j
import ygor.Enrichment
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier
import ygor.integrators.EzbIntegrationService
import ygor.integrators.KbartIntegrationService
import ygor.integrators.ZdbIntegrationService

@Log4j
class MultipleProcessingThread extends Thread {

    static final KEY_ORDER = [KbartConnector.KBART_HEADER_ZDB_ID,
                              KbartConnector.KBART_HEADER_DOI_IDENTIFIER,
                              KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER,
                              KbartConnector.KBART_HEADER_PRINT_IDENTIFIER]
    static final SOURCE_ORDER = [MappingsContainer.KBART,
                                 MappingsContainer.ZDB,
                                 MappingsContainer.EZB] // TODO
    public identifierByKey = [:]

    MappingsContainer mappingsContainer
    FieldKeyMapping zdbKeyMapping
    FieldKeyMapping pissnKeyMapping
    FieldKeyMapping eissnKeyMapping

    // old member variables following; TODO use or delete

    public ProcessorInterface processor
    public isRunning

    private enrichment
	private apiCalls
    private delimiter
    private quote
    private quoteMode
    private recordSeparator
    private dataType
    private platform
    private kbartFile

    private int progressTotal   = 0
    private int progressCurrent

    private KbartReader kbartReader
    private ZdbReader zdbReader
    private EzbReader ezbReader
    
	MultipleProcessingThread(Enrichment en, HashMap options) {
		enrichment = en
        processor = new KbartProcessor()
		apiCalls  = options.get('options')
        delimiter = options.get('delimiter')
        quote = options.get('quote')
        quoteMode = options.get('quoteMode')
        recordSeparator = options.get('recordSeparator')
        dataType = options.get('dataTyp')
        platform = options.get('platform')
        kbartFile = en.originPathName
        kbartReader = new KbartReader(this, delimiter)
        zdbReader = new ZdbReader()
        ezbReader = new EzbReader()
        mappingsContainer = new MappingsContainer()
        zdbKeyMapping = mappingsContainer.getMapping("zdbId", MappingsContainer.YGOR)
        pissnKeyMapping = mappingsContainer.getMapping("printIdentifier", MappingsContainer.YGOR)
        eissnKeyMapping = mappingsContainer.getMapping("onlineIdentifier", MappingsContainer.YGOR)
        identifierByKey = [(zdbKeyMapping) : ZdbIdentifier.class,
                           (pissnKeyMapping) : PissnIdentifier.class,
                           (eissnKeyMapping) : EissnIdentifier.class]
	}

    @Override
	void run() {
        isRunning = true
        progressCurrent = 0
		if(null == enrichment.originPathName)
			System.exit(0)
		enrichment.setStatus(Enrichment.ProcessingState.WORKING)
		log.info('Starting MultipleProcessingThread run...')
		try {
            apiCalls.each{ call ->
                switch(call) {
                    case KbartBridge.IDENTIFIER:
                        KbartReaderConfiguration conf =
                            new KbartReaderConfiguration(delimiter, quote, quoteMode, recordSeparator)
                        new KbartIntegrationService().integrate(this, enrichment.dataContainer, mappingsContainer, conf)
                        break
                    case EzbBridge.IDENTIFIER:
                        new EzbIntegrationService().integrate(this, enrichment.dataContainer, mappingsContainer)
                        break
                    case ZdbBridge.IDENTIFIER:
                        new ZdbIntegrationService().integrate(this, enrichment.dataContainer, mappingsContainer)
                        break
                }
            }
            log.info('Done MultipleProcessingThread run.')
		}
        catch(YgorProcessingException e) { // TODO Throw it in ...IntegrationService and / or ...Reader
			enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)
            enrichment.setMessage(e.toString().substring(YgorProcessingException.class.getName().length() + 2))
			log.error(e.getMessage())
            log.error(e.printStackTrace())
			log.info('Aborted MultipleProcessingThread run.')
			return
		}
        catch(Exception e) {
            enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)
            log.error(e.getMessage())
            log.info('Aborted MultipleProcessingThread run.')
            def stacktrace = Throwables.getStackTraceAsString(e).substring(0, 800).replaceAll("\\p{C}", " ")
            enrichment.setMessage(stacktrace + " ..")
            return
        }

        validate(enrichment.dataContainer.info.namespace_title_id)

        processUiSettings()                              // set "medium"
        GokbExporter.extractTitles(enrichment)           // to enrichment.dataContainer.titles
        GokbExporter.extractTipps(enrichment)            // to enrichment.dataContainer.tipps
        GokbExporter.removeEmptyIdentifiers(enrichment)  // e. g. empty identifiers, incomplete publisher_history, ...
        GokbExporter.extractPackageHeader(enrichment)    // to enrichment.dataContainer.packageHeader

        enrichment.dataContainer.info.stash = processor.stash.values // TODO adapt the following
        enrichment.dataContainer.info.stash.processedKbartEntries = processor.getCount()

        def duplicateKeys = []
        for (key in KEY_ORDER){
            enrichment.dataContainer.info.stash."${key}".each { k, v ->
                if (! processor.stash.getKeyByValue("${key}", v)) {
                    duplicateKeys << v
                }
            }
        }
        enrichment.dataContainer.info.stash.duplicateKeyEntries = duplicateKeys.unique()

        enrichment.saveResult()
		enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
	}


    private void validate(){
        for (Record record : enrichment.dataContainer.records) {
            record.validate()
        }
    }


    private void processUiSettings(){
        setTitleMedium()
        setTitleType()
        setTippPlatform()
    }


    private void setTitleMedium() {
        FieldKeyMapping mediumMapping = mappingsContainer.getMapping("medium", MappingsContainer.YGOR)

        if (dataType == 'ebooks') {
            mediumMapping.val = "Book"
        }
        else if (dataType == 'database') {
            mediumMapping.val = "Database"
        }
        else {
            mediumMapping.val = "Journal"
        }

        MultiField titleMedium = new MultiField(mediumMapping)
        for (Record record in enrichment.dataContainer.records) {
            record.addMultiField(titleMedium)
        }
    }


    private void setTitleType() {
        FieldKeyMapping typeMapping = mappingsContainer.getMapping("publicationType", MappingsContainer.YGOR)

        if (dataType == 'ebooks') {
            typeMapping.val = "Book"
        }
        else if (dataType == 'database') {
            typeMapping.val = "Database"
        }
        else {
            typeMapping.val = "Serial"
        }

        MultiField titleType = new MultiField(typeMapping)
        for (Record record in enrichment.dataContainer.records) {
            record.addMultiField(titleType)
        }
    }


    private void setTippPlatform() {
        FieldKeyMapping platformNameMapping = mappingsContainer.getMapping("platformName", MappingsContainer.YGOR)
        FieldKeyMapping platformUrlMapping = mappingsContainer.getMapping("platformUrl", MappingsContainer.YGOR)

        platformNameMapping.val = enrichment.dataContainer.pkg.packageHeader.v.nominalPlatform.name
        platformUrlMapping.val = enrichment.dataContainer.pkg.packageHeader.v.nominalPlatform.url
        MultiField nameField = new MultiField(platformNameMapping)
        MultiField urlField = new MultiField(platformUrlMapping)

        for (Record record in enrichment.dataContainer.records) {
            record.addMultiField(nameField)
            record.addMultiField(urlField)
        }
    }


    void setProgressTotal(int i) {
        progressTotal = i
    }


    void increaseProgress() {
        progressCurrent++
        enrichment.setProgress((progressCurrent / progressTotal) * 100)
    }


    int getApiCallsSize() {
        return apiCalls.size()    
    }


    Enrichment getEnrichment() {
        enrichment
    }
}  
