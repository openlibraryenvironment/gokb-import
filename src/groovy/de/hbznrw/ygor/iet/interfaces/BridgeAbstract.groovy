package de.hbznrw.ygor.iet.interfaces

import de.hbznrw.ygor.iet.enums.Query

abstract class BridgeAbstract implements BridgeInterface {

    public Thread master
    public Query[] tasks
	public ConnectorInterface connector
	public ProcessorInterface processor
	
	void go() throws Exception {
	}
	
	void go(String outputFile) throws Exception {
	}
    
    void workOffStash(stash) throws Exception {
        println " -- BridgeAbstract.workOffStash(Object stash) not implemented -- "
    }
    
    void increaseProgress() {
        if(processor)
            processor.count++
        if(master)
            master.increaseProgress()
    }
}
