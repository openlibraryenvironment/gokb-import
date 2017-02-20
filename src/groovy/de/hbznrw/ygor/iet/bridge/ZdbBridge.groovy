package de.hbznrw.ygor.iet.bridge

import groovy.util.logging.Log4j
import de.hbznrw.ygor.iet.connector.*
import de.hbznrw.ygor.iet.enums.Query
import de.hbznrw.ygor.iet.formatadapter.*
import de.hbznrw.ygor.iet.interfaces.*

@Log4j
class ZdbBridge extends BridgeAbstract implements BridgeInterface {
	
    static final IDENTIFIER = 'zdb'

	Query[] tasks = [
        Query.ZDBID,
        Query.ZDB_TITLE,
        Query.ZDB_PUBLISHER
        ]
    
    private HashMap options
	
	ZdbBridge(Thread master, HashMap options) {
        this.master    = master
        this.options   = options
		this.connector = new DnbSruOiaDcConnector(this)
		this.processor = master.processor
	}
	
	@Override
	void go() throws Exception {
		log.info("Input:  " + options.get('inputFile'))
        
        master.enrichment.dataContainer.info.api << connector.getAPIQuery('<issn>')
        
        processor.setBridge(this)
        processor.setConfiguration(",", null, null)
        processor.processFile(options)
	}
	
	@Override
	void go(String outputFile) throws Exception {
		log.warn("deprecated function call go(outputFile)")
	}
}
