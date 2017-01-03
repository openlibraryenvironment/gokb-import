package de.hbznrw.ygor.iet;

import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.iet.interfaces.BridgeInterface
//import ygor.Data
import ygor.Enrichment
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class MultipleProcessingThread extends Thread {

	private document
	private indexOfKey
    private typeOfKey
	private options
    
    public isRunning = true
	
    private int progressTotal   = 0
    private int progressCurrent = 0
    
	private BridgeInterface bridge
	
	MultipleProcessingThread(Enrichment document, HashMap options) {
		this.document   = document
		this.indexOfKey = options.get('indexOfKey')
        this.typeOfKey  = options.get('typeOfKey')
		this.options    = options.get('options')
	}
	
	public void run() {
		if(null == document.originPathName)
			System.exit(0)
	
		if(null == indexOfKey)
			System.exit(0)
		
		document.setStatus(Enrichment.ProcessingState.WORKING)
		
		println('Starting ..')
        
		try {  
            LineNumberReader lnr = new LineNumberReader(
                new FileReader(new File(document.originPathName))
                );
            lnr.skip(Long.MAX_VALUE);
            progressTotal = lnr.getLineNumber() * options.size()
            lnr.close();
            
            options.each{
                option ->
                    switch(option) {
                        case GbvBridge.IDENTIFIER:
                            bridge = new GbvBridge(this, new HashMap(
                                inputFile:  document.originPathName, 
                                indexOfKey: indexOfKey, 
                                typeOfKey:  typeOfKey
                                )
                            )
                            break
                        case ZdbBridge.IDENTIFIER:
                            bridge = new ZdbBridge(this, new HashMap(
                                inputFile:  document.originPathName,
                                indexOfKey: indexOfKey,
                                typeOfKey:  typeOfKey
                                )
                            )
                            break
                        case EzbBridge.IDENTIFIER:
                            bridge = new EzbBridge(this, new HashMap(
                                inputFile:  document.originPathName, 
                                indexOfKey: indexOfKey, 
                                typeOfKey:  typeOfKey
                                )
                            )
                            break
                    }
                    
                    if(bridge) {
                        bridge.go(document.resultPathName)
                        Files.copy(
                            Paths.get(document.resultPathName),
                            Paths.get(document.originPathName),
                            StandardCopyOption.REPLACE_EXISTING
                            )
                    }
            }
           								
		} catch(Exception e) {
			document.setStatusByCallback(Enrichment.ProcessingState.ERROR)
			
			println(e.getMessage())
			println(e.getStackTrace())
			
			println('Aborted.')
			return
		}
		println('Done.')
		
		document.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
	}
    
    void increaseProgress() {
        progressCurrent++
        document.setProgress((progressCurrent / progressTotal) * 100)
    }
}
