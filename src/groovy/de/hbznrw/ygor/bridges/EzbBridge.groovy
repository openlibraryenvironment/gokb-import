package de.hbznrw.ygor.bridges

import groovy.util.logging.Log4j
import de.hbznrw.ygor.connectors.*
import de.hbznrw.ygor.enums.Query
import de.hbznrw.ygor.iet.formatadapter.*
import de.hbznrw.ygor.interfaces.*
import de.hbznrw.ygor.export.structure.TitleStruct

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

        if(options.get('typeOfKey') == ZdbBridge.IDENTIFIER){
            this.connector  = new EzbXmlConnector(this, EzbXmlConnector.QUERY_XML_ZDB)
            this.stashIndex = ZdbBridge.IDENTIFIER
        }
        else {
            this.connector  = new EzbXmlConnector(this, EzbXmlConnector.QUERY_XML_IS)
            this.stashIndex = TitleStruct.ISSN
        }
	}
	
	@Override
	void go() throws Exception {
		log.info("Input:  " + options.get('inputFile'))
        
        master.enrichment.dataContainer.info.api << connector.getAPIQuery('<zdbid>')
        
        processor.setBridge(this)
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
            connector.poll(key)
            
            processor.processEntry(master.enrichment.dataContainer, uid, key)
        }
    }
}
