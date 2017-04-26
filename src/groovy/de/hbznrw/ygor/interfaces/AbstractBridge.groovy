package de.hbznrw.ygor.interfaces

import groovy.util.logging.Log4j
import de.hbznrw.ygor.iet.enums.Query

@Log4j
abstract class AbstractBridge implements BridgeInterface {

    protected Thread master 
	protected ConnectorInterface connector
	protected ProcessorInterface processor
    
    public Query[] tasks
    
    protected HashMap options
    protected stashIndex
	
	void go() throws Exception {
	}
	
	void go(String outputFile) throws Exception {
	}
    
    void processStash() throws Exception {
        log.info(" -- processStash() not implemented -- ")
    }
    
    void finish() throws Exception {
        log.info(" -- finish() not implemented -- ")
    }
    
    void increaseProgress() {
        if(processor)
            processor.countUp()
        if(master)
            master.increaseProgress()
    }
    
    Thread getMaster() {
        master
    }
    ConnectorInterface getConnector() {
        connector
    }
    ProcessorInterface getProcessor() {
        processor
    }
}
