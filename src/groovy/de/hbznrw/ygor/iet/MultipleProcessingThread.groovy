package de.hbznrw.ygor.iet;

import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.iet.interfaces.BridgeInterface
import de.hbznrw.ygor.iet.interfaces.ProcessorInterface
import de.hbznrw.ygor.iet.processor.*
import groovy.util.logging.Log4j
import ygor.Enrichment

@Log4j
class MultipleProcessingThread extends Thread {

    public  ProcessorInterface processor
    private BridgeInterface bridge
    
    public isRunning = true
    
	private enrichment
    private typeOfKey
	private options

    private int progressTotal   = 0
    private int progressCurrent = 0
    
	MultipleProcessingThread(Enrichment en, HashMap options) {
		this.enrichment = en
        this.processor = new KbartProcessor()
        this.typeOfKey  = options.get('typeOfKey')
		this.options    = options.get('options')
	}
	
	public void run() {
		if(null == enrichment.originPathName)
			System.exit(0)
		
		enrichment.setStatus(Enrichment.ProcessingState.WORKING)
		
		log.info('Starting ..')
        
		try {  
            options.each{
                option ->
                    switch(option) {
                        case KbartBridge.IDENTIFIER:
                            // writes stash->kbart
                            // writes stash->zdb or stash->issn 
                            bridge = new KbartBridge(this, new HashMap(
                                inputFile:  enrichment.originPathName,
                                typeOfKey:  typeOfKey
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
			log.error(e.getStackTrace())
			
			log.info('Aborted.')
			return
		}
		log.info('Done.')
		
        enrichment.dataContainer.info.stash = processor.stash.values
        
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
    
    int getOptionsSize() {
        return options.size()    
    }
    
    Enrichment getEnrichment() {
        enrichment
    }
}  
