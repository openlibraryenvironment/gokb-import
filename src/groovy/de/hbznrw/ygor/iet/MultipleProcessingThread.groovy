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
            options.each{
                option ->
                    switch(option) {
                        case 'zdbid':
                            bridge = new ZdbIdBridge(document.originPathName, indexOfKey)
                            break
                        case 'ezbid':
                            bridge = new EzbIdBridge(document.originPathName, indexOfKey)
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
			document.processCallback(Enrichment.StateOfProcess.ERROR)
			
			println(e.getMessage())
			println(e.getStackTrace())
			
			println('Aborted.')
			return
		}
		println('Done.')
		
		document.processCallback(Enrichment.StateOfProcess.FINISHED)
	}
}
