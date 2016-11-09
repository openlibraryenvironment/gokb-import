package de.hbznrw.ygor.iet;

import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.iet.interfaces.BridgeInterface
import ygor.Enrichment


class ProcessingThread extends Thread {

	private document
	private indexOfKey
	private options
	
	private BridgeInterface bridge
	
	ProcessingThread(Enrichment document, int indexOfKey, String options) {
		this.document   = document
		this.indexOfKey = indexOfKey
		this.options    = options
	}
	
	/*
	int getProgress(){
		if(bridge)
			return bridge.progress
		else
			return -1
	}
*/
	public void run() {
		if(null == document.originPathName)
			System.exit(0)
	
		if(null == indexOfKey)
			System.exit(0)
		
		document.setStatus(Enrichment.StateOfProcess.WORKING)
		
		println('Starting ..')

		//BridgeInterface bridge
		try {
			switch(options) {
				case 'zdbid':
					bridge = new ZdbIdBridge(document.originPathName, indexOfKey)
				break
				case 'ezbid':
					bridge = new EzbIdBridge(document.originPathName, indexOfKey)
				break
				case 'hbzid':
					bridge = new HbzIdBridge(document.originPathName, indexOfKey)
				case 'gokbid':
				default:
					println("not implemented yet")
				break
				
			}
			if(bridge)
				bridge.go(document.resultPathName)
				
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
