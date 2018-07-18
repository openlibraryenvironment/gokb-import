package de.hbznrw.ygor.bridges

import groovy.util.logging.Log4j
import de.hbznrw.ygor.connectors.*
import de.hbznrw.ygor.enums.Query
import de.hbznrw.ygor.interfaces.*

@Log4j
class KbartBridge extends AbstractBridge implements BridgeInterface {

    static final IDENTIFIER = 'kbart'
    
	Query[] tasks = [
        Query.KBART_TIPP_URL,
        Query.KBART_TIPP_COVERAGE,
        Query.KBART_TIPP_ACCESS
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
		log.info("Input:  " + options.get('inputFile') + " [" 
            + options.get('delimiter') + ", " + options.get('quote') + ", " + options.get('quoteMode') + "]")
        
        master.enrichment.dataContainer.info.api << 'KBART-FILE'
        
        processor.setBridge(this)
        processor.setConfiguration(options.get('delimiter'), options.get('quote'), options.get('quoteMode'), null)
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
        
        stash.get(this.stashIndex).each{ uid, fields ->
            
            if(!master.isRunning) {
                log.info('Aborted by user action.')
                return
            }
            increaseProgress()

            // for kbart file no really need
            connector.poll(uid, stash.getKeyType(uid))
            
            // TODO refactor
            processor.processEntry(master.enrichment.dataContainer, uid, 'TODO_REFACTOR_TO_ZdbBridge.IDENTIFIER')
        }
    }
}
