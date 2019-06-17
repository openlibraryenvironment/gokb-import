package de.hbznrw.ygor.processing

import com.google.common.base.Throwables
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.export.Statistics
import de.hbznrw.ygor.readers.EzbReader
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import de.hbznrw.ygor.readers.ZdbReader
import de.hbznrw.ygor.validators.RecordValidator
import groovy.util.logging.Log4j
import ygor.Enrichment
import ygor.Record
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier
import ygor.integrators.EzbIntegrationService
import ygor.integrators.KbartIntegrationService
import ygor.integrators.ZdbIntegrationService

@Log4j
class MultipleProcessingThread extends Thread {

    static final KEY_ORDER = ["zdbId", "doiId", "eissn", "pissn"]

    static final SOURCE_ORDER = [MappingsContainer.KBART,
                                 MappingsContainer.ZDB,
                                 MappingsContainer.EZB] // TODO
    public identifierByKey = [:]

    FieldKeyMapping zdbKeyMapping
    FieldKeyMapping pissnKeyMapping
    FieldKeyMapping eissnKeyMapping

    // old member variables following; TODO use or delete

    public isRunning

    private Enrichment enrichment
    private apiCalls
    private delimiter
    private quote
    private quoteMode
    private recordSeparator
    private platform
    private kbartFile

    private int progressTotal   = 0
    private int progressCurrent

    private KbartReader kbartReader
    private ZdbReader zdbReader
    private EzbReader ezbReader

    MultipleProcessingThread(Enrichment en, HashMap options) {
        enrichment = en
        apiCalls  = options.get('options')
        delimiter = options.get('delimiter')
        quote = options.get('quote')
        quoteMode = options.get('quoteMode')
        recordSeparator = options.get('recordSeparator')
        platform = options.get('platform')
        kbartFile = en.originPathName
        kbartReader = new KbartReader(this, delimiter)
        zdbReader = new ZdbReader()
        ezbReader = new EzbReader()
        zdbKeyMapping = en.mappingsContainer.getMapping("zdbId", MappingsContainer.YGOR)
        pissnKeyMapping = en.mappingsContainer.getMapping("printIdentifier", MappingsContainer.YGOR)
        eissnKeyMapping = en.mappingsContainer.getMapping("onlineIdentifier", MappingsContainer.YGOR)
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
                    case KbartReader.IDENTIFIER:
                        KbartReaderConfiguration conf =
                                new KbartReaderConfiguration(delimiter, quote, quoteMode, recordSeparator)
                        new KbartIntegrationService(enrichment.mappingsContainer).integrate(this, enrichment.dataContainer, conf)
                        break
                    case EzbReader.IDENTIFIER:
                        new EzbIntegrationService(enrichment.mappingsContainer).integrate(this, enrichment.dataContainer)
                        break
                    case ZdbReader.IDENTIFIER:
                        new ZdbIntegrationService(enrichment.mappingsContainer).integrate(this, enrichment.dataContainer)
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

        validate()
        checkIDsUniqueness()

        processUiSettings()                              // set "medium"

        GokbExporter.extractTitles(enrichment)           // to enrichment.dataContainer.titles
        GokbExporter.extractTipps(enrichment)            // to enrichment.dataContainer.tipps

        Statistics.getRecordsStatisticsBeforeParsing(enrichment)
                                                         // to enrichment.stats

        GokbExporter.removeEmptyIdentifiers(enrichment)  // e. g. empty identifiers, incomplete publisher_history, ...
        GokbExporter.extractPackageHeader(enrichment)    // to enrichment.dataContainer.packageHeader

        enrichment.saveResult()
        enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
    }


    private void validate(){
        for (Record record : enrichment.dataContainer.records) {
            record.validateMultifields(enrichment.dataContainer.info.namespace_title_id)
            RecordValidator.validateCoverage(record)
            RecordValidator.validateHistoryEvent(record)
            RecordValidator.validatePublisherHistory(record)
        }
    }


    private void checkIDsUniqueness(){
        // TODO
    }


    private void processUiSettings(){
        FieldKeyMapping mediumMapping = enrichment.setTitleMediumMapping()
        enrichment.enrollMappingToRecords(mediumMapping)
        FieldKeyMapping typeMapping = enrichment.setTitleTypeMapping()
        enrichment.enrollMappingToRecords(typeMapping)
        FieldKeyMapping tippNameMapping = enrichment.setTippPlatformNameMapping()
        enrichment.enrollMappingToRecords(tippNameMapping)
        FieldKeyMapping tippUrlMapping = enrichment.setTippPlatformUrlMapping()
        enrichment.enrollMappingToRecords(tippUrlMapping)
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
