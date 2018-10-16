package de.hbznrw.ygor.processing;

import de.hbznrw.ygor.bridges.*
import de.hbznrw.ygor.connectors.KbartConnector
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.interfaces.ProcessorInterface
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.readers.KbartReaderConfiguration
import groovy.util.logging.Log4j
import ygor.Enrichment
import com.google.common.base.Throwables
import ygor.field.MappingsContainer
import ygor.integrators.EzbIntegrationService
import ygor.integrators.KbartIntegrationService
import ygor.integrators.ZdbIntegrationService
import ygor.source.EzbSource
import ygor.source.KbartSource
import ygor.source.ZdbSource

@Log4j
class MultipleProcessingThread extends Thread {

    static final KEY_ORDER = [KbartConnector.KBART_HEADER_ZDB_ID,
                              KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER,
                              KbartConnector.KBART_HEADER_PRINT_IDENTIFIER]

    static final SOURCE_ORDER = [KbartSource, ZdbSource, EzbSource]

    DataContainer dataContainer
    MappingsContainer mappingsContainer

    // old member variables following; TODO use or delete

    public ProcessorInterface processor
    public isRunning

    private enrichment
	private apiCalls
    private delimiter
    private quote
    private quoteMode
    private recordSeparator
    private kbartFile

    private int progressTotal   = 0
    private int progressCurrent

    private KbartReader kbartReader
    
	MultipleProcessingThread(Enrichment en, HashMap options) {
        this.dataContainer = new DataContainer()
		this.enrichment = en
        this.processor = new KbartProcessor()
		this.apiCalls  = options.get('options')
        this.delimiter = options.get('delimiter')
        this.quote = options.get('quote')
        this.quoteMode = options.get('quoteMode')
        this.recordSeparator = options.get('recordSeparator')
        this.kbartFile = en.originPathName
        this.kbartReader = new KbartReader(this)
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
                        KbartIntegrationService.integrate(this, dataContainer, mappingsContainer, conf)
                        break
                    case EzbBridge.IDENTIFIER:
                        EzbIntegrationService.integrate() // TODO
                        break
                    case ZdbBridge.IDENTIFIER:
                        ZdbIntegrationService.integrate() // TODO
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
