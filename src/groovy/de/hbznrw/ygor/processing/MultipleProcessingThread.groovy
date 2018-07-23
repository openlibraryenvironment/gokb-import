package de.hbznrw.ygor.processing;

import de.hbznrw.ygor.bridges.*
import de.hbznrw.ygor.connectors.KbartConnector
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.interfaces.BridgeInterface
import de.hbznrw.ygor.interfaces.ProcessorInterface
import groovy.util.logging.Log4j
import ygor.Enrichment
import com.google.common.base.Throwables
import ygor.source.EzbSource
import ygor.source.KbartSource
import ygor.source.ZdbSource

@Log4j
class MultipleProcessingThread extends Thread {

    static final KEY_ORDER = [KbartConnector.KBART_HEADER_ZDB_ID,
                              KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER,
                              KbartConnector.KBART_HEADER_PRINT_IDENTIFIER]

    static final SOURCE_ORDER = [KbartSource, ZdbSource, EzbSource]

    DataContainer container

    // old member variables following; TODO use or delete

    public ProcessorInterface processor
    public isRunning = true

    private BridgeInterface bridge
    private enrichment
	private apiCalls
    private refactorThis // TODO refactor

    private int progressTotal   = 0
    private int progressCurrent = 0
    
	MultipleProcessingThread(Enrichment en, HashMap options) {
        this.container = new DataContainer()

		this.enrichment = en
        this.processor = new KbartProcessor()
		this.apiCalls  = options.get('options')
        this.refactorThis = options // TODO refactor
	}
	
	void run() {
		if(null == enrichment.originPathName)
			System.exit(0)
		
		enrichment.setStatus(Enrichment.ProcessingState.WORKING)
		log.info('Starting ..')
        
		try {  
            apiCalls.each{ call ->
                switch(call) {
                    case KbartBridge.IDENTIFIER:
                        // writes stash->kbart
                        // writes stash->zdb or stash->issn
                        bridge = new KbartBridge(this, new HashMap(
                                inputFile:  enrichment.originPathName,
                                delimiter:  refactorThis.get('delimiter'),
                                quote:      refactorThis.get('quote'),
                                quoteMode:  refactorThis.get('quoteMode')
                            )
                        )
                        break
                    case EzbBridge.IDENTIFIER:
                        bridge = new EzbBridge(this, new HashMap(inputFile:  enrichment.originPathName))
                        break
                    case ZdbBridge.IDENTIFIER:
                        bridge = new ZdbBridge(this, new HashMap(inputFile:  enrichment.originPathName))
                        break
                }
                if(bridge) {
                    bridge.go()
                    bridge = null
                }
            }
		}
        catch(YgorProcessingException e) {
			enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)
            enrichment.setMessage(e.toString().substring(YgorProcessingException.class.getName().length() + 2))
			log.error(e.getMessage())
            log.error(e.printStackTrace())
			log.info('Aborted.')
			return
		}
        catch(Exception e) {
            enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)
            log.error(e.getMessage())
            log.info('Aborted.')
            def stacktrace = Throwables.getStackTraceAsString(e).substring(0, 800).replaceAll("\\p{C}", " ")
            enrichment.setMessage(stacktrace + " ..")
            return
        }
		log.info('Done.')
		
        enrichment.dataContainer.info.stash = processor.stash.values
        enrichment.dataContainer.info.stash.processedKbartEntries = processor.getCount()

        // TODO: re-get duplicate keys

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
