package de.hbznrw.ygor.iet.interfaces

import de.hbznrw.ygor.iet.enums.Query

abstract class BridgeAbstract implements BridgeInterface {

    public Thread master
    public Query query
	public ConnectorInterface connector
	public ProcessorInterface processor
	
	void go() throws Exception {
	}
	
	void go(String outputFile) throws Exception {
	}
    
    void increaseProgress() {
        if(master)
            master.increaseProgress()
    }
}
