package de.hbznrw.ygor.processing;

import de.hbznrw.ygor.bridges.*
import de.hbznrw.ygor.interfaces.BridgeInterface
import de.hbznrw.ygor.interfaces.ProcessorInterface
import groovy.util.logging.Log4j
import ygor.Enrichment
import com.google.common.base.Throwables

@Log4j
class MultipleProcessingThread extends Thread {

    public  ProcessorInterface processor
    private BridgeInterface bridge
    
    public isRunning = true
    
	private enrichment
	private apiCalls
    private typeOfKey
    
    private refactorThis // TODO refactor

    private int progressTotal   = 0
    private int progressCurrent = 0
    
	MultipleProcessingThread(Enrichment en, HashMap options) {
		this.enrichment = en
        this.processor = new KbartProcessor()
		this.apiCalls  = options.get('options')
        this.typeOfKey = options.get('typeOfKey')
        
        this.refactorThis = options // TODO refactor
	}
	
	public void run() {
		if(null == enrichment.originPathName)
			System.exit(0)
		
		enrichment.setStatus(Enrichment.ProcessingState.WORKING)
		
		log.info('Starting ..')
        
		try {  
            apiCalls.each{
                call ->
                    switch(call) {
                        case KbartBridge.IDENTIFIER:
                            // writes stash->kbart
                            // writes stash->zdb or stash->issn 
                            bridge = new KbartBridge(this, new HashMap(
                                inputFile:  enrichment.originPathName,
                                typeOfKey:  typeOfKey,
                                delimiter:  refactorThis.get('delimiter'),
                                quote:      refactorThis.get('quote'),
                                quoteMode:  refactorThis.get('quoteMode')
                                )
                            )
                            break

                        case GbvBridge.IDENTIFIER:
                            bridge = new GbvBridge(this, new HashMap(
                                inputFile:  enrichment.originPathName,
                                typeOfKey:  typeOfKey
                                )
                            )
                            break 
                        case EzbBridge.IDENTIFIER:
                            bridge = new EzbBridge(this, new HashMap(
                                inputFile:  enrichment.originPathName, 
                                typeOfKey:  typeOfKey
                                )
                            )
                            break
                            /*
                        case ZdbBridge.IDENTIFIER:
                            bridge = new ZdbBridge(this, new HashMap(
                                inputFile:  enrichment.originPathName,
                                typeOfKey:  typeOfKey
                                )
                            )
                            break
                            */
                    }
                  
                    if(bridge) {
                        bridge.go()
                        bridge = null
                    }
            }
           								
		} catch(Exception e) {
			enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)

			log.error(e.getMessage())
			log.info('Aborted.')

			def stacktrace = Throwables.getStackTraceAsString(e).substring(0, 600).replaceAll("\\p{C}", " ");
            enrichment.setMessage(stacktrace)
			return
		}
		log.info('Done.')
		
        enrichment.dataContainer.info.stash = processor.stash.values
        enrichment.dataContainer.info.stash.processedKbartEntries = processor.getCount()

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
