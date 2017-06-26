package de.hbznrw.ygor.bridges

import groovy.util.logging.Log4j
import de.hbznrw.ygor.connectors.*
import de.hbznrw.ygor.enums.Query
import de.hbznrw.ygor.iet.formatadapter.*
import de.hbznrw.ygor.interfaces.*

@Deprecated
@Log4j
class ZdbBridge extends AbstractBridge implements BridgeInterface {
	
    static final IDENTIFIER = 'zdb'

	Query[] tasks = [
        Query.ZDBID,
        Query.ZDB_TITLE,
        Query.ZDB_PUBLISHER
        ]
	
	ZdbBridge(Thread master, HashMap options) {
        this.master     = master
        this.options    = options
		this.connector  = new DnbSruOiaDcConnector(this)
		this.processor  = master.processor
        this.stashIndex = null
	}
	
	@Override
	void go() throws Exception {
		log.info("Input:  " + options.get('inputFile'))
        
        master.enrichment.dataContainer.info.api << connector.getAPIQuery('<issn>')
        
        processor.setBridge(this)
        processor.processFile(options)
	}
	
	@Override
	void go(String outputFile) throws Exception {
		log.warn("deprecated function call go(outputFile)")
	}
}
