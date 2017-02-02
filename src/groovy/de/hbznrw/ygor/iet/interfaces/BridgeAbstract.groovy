package de.hbznrw.ygor.iet.interfaces

import groovy.util.logging.Log4j
import de.hbznrw.ygor.iet.enums.Query

@Log4j
abstract class BridgeAbstract implements BridgeInterface {

    protected Thread master
    public Query[] tasks
	protected ConnectorInterface connector
	protected ProcessorInterface processor
	
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
