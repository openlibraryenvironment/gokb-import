package de.hbznrw.ygor.iet;

import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.iet.interfaces.BridgeInterface
import ygor.Enrichment
import java.nio.file.Files
import java.nio.file.Paths

class MultipleProcessingThread extends Thread {

	private enrichment
	private indexOfKey
    private typeOfKey
	private options
    
    public isRunning = true
	
    private int progressTotal   = 0
    private int progressCurrent = 0
    
	private BridgeInterface bridge
	
	MultipleProcessingThread(Enrichment en, HashMap options) {
		this.enrichment = en
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
		
		println('Starting ..')
        
		try {  
            LineNumberReader lnr = new LineNumberReader(
                new FileReader(new File(enrichment.originPathName))
                );
            lnr.skip(Long.MAX_VALUE);
            progressTotal = lnr.getLineNumber() * options.size()
            lnr.close();

            options.each{
                option ->
                    switch(option) {
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
                        case GbvBridge.IDENTIFIER:
                            bridge = new GbvBridge(this, new HashMap(
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
			
			println(e.getMessage())
			println(e.getStackTrace())
			
			println('Aborted.')
			return
		}
		println('Done.')
		
		enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
	}
    
    void increaseProgress() {
        progressCurrent++
        enrichment.setProgress((progressCurrent / progressTotal) * 100)
    }
}
