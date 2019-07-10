package de.hbznrw.ygor.bridges

import groovy.util.logging.Log4j
import de.hbznrw.ygor.connectors.*
import de.hbznrw.ygor.enums.Query
import de.hbznrw.ygor.interfaces.*

@Log4j
class EzbBridge extends AbstractBridge implements BridgeInterface {
	
    static final IDENTIFIER = 'ezb'
    
	Query[] tasks = [
        Query.EZBID
    ]

	EzbBridge(Thread master, HashMap options) {
        this.master     = master
		this.options    = options
        this.processor  = master.processor
        this.connector  = new EzbXmlConnector(this)
	}

	@Override
	void go() throws Exception {
		log.info("Input:  " + options.get('inputFile'))
        master.enrichment.dataContainer.info.api << connector.getAPIQuery('<zdbid>', null)
        processor.setBridge(this)
        processor.processFile(options)
	}

	@Override
	void go(String outputFile) throws Exception {
		log.warn("deprecated function call go(outputFile)")
	}
}
