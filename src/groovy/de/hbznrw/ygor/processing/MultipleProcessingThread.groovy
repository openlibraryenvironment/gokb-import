package de.hbznrw.ygor.processing

import com.google.common.base.Throwables
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.export.Statistics
import de.hbznrw.ygor.readers.EzbReader
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import de.hbznrw.ygor.readers.ZdbReader
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

    static final KEY_ORDER = ["zdbId", "onlineIdentifier" /*, "printIdentifier" */]

    public identifierByKey = [:]

    FieldKeyMapping zdbKeyMapping
    FieldKeyMapping pissnKeyMapping
    FieldKeyMapping eissnKeyMapping

    public isRunning

    private Enrichment enrichment
    private apiCalls
    private delimiter
    private quote
    private quoteMode
    private recordSeparator
    private platform
    private kbartFile

    private double progressCurrent = 0.0
    private double progressIncrement

    private KbartReader kbartReader
    private ZdbReader zdbReader
    private EzbReader ezbReader

    MultipleProcessingThread(Enrichment en, HashMap options){
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
    void run(){
        isRunning = true
        progressCurrent = 0.0
        if(null == enrichment.originPathName)
            System.exit(0)
        enrichment.setStatus(Enrichment.ProcessingState.WORKING)
        log.info('Starting MultipleProcessingThread run...')
        try {
            // TODO: make sure, Kbart file is processed first
            apiCalls.each{ call ->
                switch(call){
                    case KbartReader.IDENTIFIER:
                        KbartReaderConfiguration conf =
                                new KbartReaderConfiguration(delimiter, quote, quoteMode, recordSeparator)
                        KbartIntegrationService kbartIntegrationService = new KbartIntegrationService(enrichment.mappingsContainer)
                        calculateProgressIncrement()
                        kbartIntegrationService.integrate(this, enrichment.dataContainer, conf)
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
        catch(YgorProcessingException e){ // TODO Throw it in ...IntegrationService and / or ...Reader
            enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)
            enrichment.setMessage(e.toString().substring(YgorProcessingException.class.getName().length() + 2))
            log.error(e.getMessage())
            log.error(e.printStackTrace())
            log.info('Aborted MultipleProcessingThread run.')
            return
        }
        catch(Exception e){
            enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)
            log.error(e.getMessage())
            log.info('Aborted MultipleProcessingThread run.')
            def stacktrace = Throwables.getStackTraceAsString(e).substring(0, 800).replaceAll("\\p{C}", " ")
            enrichment.setMessage(stacktrace + " ..")
            return
        }

        normalize()
        validate()
        checkIDsUniqueness()

        processUiSettings()                              // set "medium"

        Statistics.getRecordsStatisticsBeforeParsing(enrichment)
                                                         // to enrichment.stats

        GokbExporter.extractPackageHeader(enrichment)    // to enrichment.dataContainer.packageHeader
        enrichment.saveResult()
        enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
    }


    private void normalize(){
        for (Record record : enrichment.dataContainer.records.values()){
            record.normalize(enrichment.dataContainer.info.namespace_title_id)
        }
    }


    private void validate(){
        for (Record record : enrichment.dataContainer.records.values()){
            record.validate(enrichment.dataContainer.info.namespace_title_id)
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


    private void calculateProgressIncrement(){
        double lines = (double) (countLines(kbartFile)-1)
        if (lines > 0){
            progressIncrement = 100.0 / lines / (double) apiCalls.size()
            // division by 3 for number of tasks (Kbart, ZDB, EZB)
        }
        else {
            progressIncrement = 1 // dummy assignment
        }
    }


    private static int countLines(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename))
        try {
            byte[] c = new byte[1024]
            int readChars = is.read(c)
            if (readChars == -1) return 0
            int count = 0
            while (readChars == 1024){
                for (int i=0; i<1024;){
                    if (c[i++] == '\n') ++count
                }
                readChars = is.read(c)
            }
            while (readChars != -1){
                System.out.println(readChars)
                for (int i=0; i<readChars; ++i){
                    if (c[i] == '\n') ++count
                }
                readChars = is.read(c)
            }
            return count == 0 ? 1 : count
        }
        finally {
            is.close()
        }
    }


    void increaseProgress(){
        progressCurrent += progressIncrement
        enrichment.setProgress(progressCurrent)
    }


    Enrichment getEnrichment(){
        enrichment
    }
}
