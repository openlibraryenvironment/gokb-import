package de.hbznrw.ygor.iet;

import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.iet.interfaces.BridgeInterface
import ygor.Enrichment
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

class MultipleProcessingThread extends Thread {

	private document
	private indexOfKey
	private options
	
    private int progressTotal   = 0
    private int progressCurrent = 0
    
	private BridgeInterface bridge
	
	MultipleProcessingThread(Enrichment document, HashMap options) {
		this.document   = document
		this.indexOfKey = options['indexOfKey']
		this.options    = options['options']
	}
	
	public void run() {
		if(null == document.originPathName)
			System.exit(0)
	
		if(null == indexOfKey)
			System.exit(0)
		
		document.setStatus(Enrichment.StateOfProcess.WORKING)
		
		println('Starting ..')
        
		//BridgeInterface bridge
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
                        case 'zdbid':
                            bridge = new ZdbIdBridge(this, document.originPathName, indexOfKey)
                            break
                        case 'ezbid':
                            bridge = new EzbIdBridge(this, document.originPathName, indexOfKey)
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
			document.setStatusByCallback(Enrichment.StateOfProcess.ERROR)
			
			println(e.getMessage())
			println(e.getStackTrace())
			
			println('Aborted.')
			return
		}
		println('Done.')
		
		document.setStatusByCallback(Enrichment.StateOfProcess.FINISHED)
	}
    
    void increaseProgress() {
        progressCurrent++
        document.setProgress((progressCurrent / progressTotal) * 100)
    }
}
