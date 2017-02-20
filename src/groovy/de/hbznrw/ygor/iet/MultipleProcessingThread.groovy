package de.hbznrw.ygor.iet;

import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.iet.interfaces.BridgeInterface
import de.hbznrw.ygor.iet.interfaces.ProcessorInterface
import de.hbznrw.ygor.iet.processor.CsvProcessor
import groovy.util.logging.Log4j
import ygor.Enrichment

@Log4j
class MultipleProcessingThread extends Thread {

    public  ProcessorInterface processor
    private BridgeInterface bridge
    
    public isRunning = true
    
	private enrichment
	private indexOfKey
    private typeOfKey
	private options

    private int progressTotal   = 0
    private int progressCurrent = 0
    
	MultipleProcessingThread(Enrichment en, HashMap options) {
		this.enrichment = en
        
        this.processor = new CsvProcessor()
		this.indexOfKey = options.get('indexOfKey')
        this.typeOfKey  = options.get('typeOfKey')
		this.options    = options.get('options')
	}
	
	public void run() {
		if(null == enrichment.originPathName)
			System.exit(0)
	
		if(null == indexOfKey)
			System.exit(0)
		
		enrichment.setStatus(Enrichment.ProcessingState.WORKING)
		
		log.info('Starting ..')
        
		try {  
            options.each{
                option ->
                    switch(option) {
                        case GbvBridge.IDENTIFIER:
                            bridge = new GbvBridge(this, new HashMap(
                                inputFile:  enrichment.originPathName,
                                indexOfKey: indexOfKey,
                                typeOfKey:  typeOfKey
                                )
                            )
                            break
                        case EzbBridge.IDENTIFIER:
                            bridge = new EzbBridge(this, new HashMap(
                                inputFile:  enrichment.originPathName, 
                                indexOfKey: indexOfKey, 
                                typeOfKey:  typeOfKey
                                )
                            )
                            break
                        case ZdbBridge.IDENTIFIER:
                            bridge = new ZdbBridge(this, new HashMap(
                                inputFile:  enrichment.originPathName,
                                indexOfKey: indexOfKey,
                                typeOfKey:  typeOfKey
                                )
                            )
                            break
                    }
                  
                    if(bridge)
                        bridge.go()
            }
           								
		} catch(Exception e) {
			enrichment.setStatusByCallback(Enrichment.ProcessingState.ERROR)
			
			log.error(e.getMessage())
			log.error(e.getStackTrace())
			
			log.info('Aborted.')
			return
		}
		log.info('Done.')
		
		enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
	}
    
    void setProgressTotal(int i) {
        progressTotal = i
    }
    
    void increaseProgress() {
        progressCurrent++
        enrichment.setProgress((progressCurrent / progressTotal) * 100)
    }
    
    Enrichment getEnrichment() {
        enrichment
    }
}  
