package de.hbznrw.ygor.iet.bridge

import groovy.util.logging.Log4j
import de.hbznrw.ygor.iet.connector.*
import de.hbznrw.ygor.iet.enums.Query
import de.hbznrw.ygor.iet.formatadapter.*
import de.hbznrw.ygor.iet.interfaces.*

@Log4j
class KbartBridge extends BridgeAbstract implements BridgeInterface {
	
    static final IDENTIFIER = 'kbart'
    
	Query[] tasks = [
        Query.KBART_TIPP_URL,
        Query.KBART_TIPP_COVERAGE
    ]

	KbartBridge(Thread master, HashMap options) {
        this.master     = master
		this.options    = options
		this.connector  = new KbartConnector(this)
		this.processor  = master.processor
        this.stashIndex = KbartBridge.IDENTIFIER
	}
	
	@Override
	void go() throws Exception {
		log.info("Input:  " + options.get('inputFile'))
        
        master.enrichment.dataContainer.info.api << 'KBART-FILE'
        
        processor.setBridge(this)
        processor.setConfiguration(",", '"', null)
        processor.processFile(options)
	}
	
	@Override
	void go(String outputFile) throws Exception {
		log.warn("deprecated function call go(outputFile)")
	}
    
    @Override
    void processStash() throws Exception {
        log.info("processStash()")
        
        def stash = processor.getStash()
        
        stash.get(this.stashIndex).each{ key, uid ->
            
            if(!master.isRunning) {
                log.info('Aborted by user action.')
                return
            }
            increaseProgress()

            // for kbart file no really need
            connector.poll(key)
            
            // TODO refactor
            processor.processEntry(master.enrichment.dataContainer, key, 'TODO_REFACTOR_TO_ZdbBridge.IDENTIFIER')
        }
    }
}
