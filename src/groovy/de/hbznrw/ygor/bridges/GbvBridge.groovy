package de.hbznrw.ygor.bridges

import de.hbznrw.ygor.export.DataMapper
import groovy.util.logging.Log4j
import de.hbznrw.ygor.connectors.*
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.interfaces.*
import de.hbznrw.ygor.export.structure.TitleStruct

@Log4j
@Deprecated // replaced by ZdbBridge
class GbvBridge extends AbstractBridge implements BridgeInterface {

    static final IDENTIFIER = 'gbv'
    
	Query[] tasks = [
        Query.ZDBID,
        Query.GBV_GVKPPN,
        Query.GBV_PISSN,
        Query.GBV_EISSN,
        Query.GBV_TITLE,
        Query.GBV_PUBLISHER,
        Query.GBV_PUBLISHED_FROM,
        Query.GBV_PUBLISHED_TO,
        Query.GBV_HISTORY_EVENTS
        ]

	GbvBridge(Thread master, HashMap options) {
        this.master    = master
        this.options   = options
        this.processor = master.processor
        
        if(options.get('typeOfKey') == KbartConnector.KBART_HEADER_ZDB_ID){
            this.connector  = new ZdbdbSruPicaConnector(this, ZdbdbSruPicaConnector.QUERY_PICA_ZDB)
            this.stashIndex = KbartConnector.KBART_HEADER_ZDB_ID
        }
        else if(options.get('typeOfKey') == KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER){
            this.connector  = new ZdbdbSruPicaConnector(this, ZdbdbSruPicaConnector.QUERY_PICA_ISS)
            this.stashIndex = KbartConnector.KBART_HEADER_ONLINE_IDENTIFIER
        }
        else if(options.get('typeOfKey') == KbartConnector.KBART_HEADER_PRINT_IDENTIFIER){
            this.connector  = new ZdbdbSruPicaConnector(this, ZdbdbSruPicaConnector.QUERY_PICA_ISS)
            this.stashIndex = KbartConnector.KBART_HEADER_PRINT_IDENTIFIER
        }
	}
	
	@Override
	void go() throws Exception {
        log.info("Input:  " + options.get('inputFile'))
        
        master.enrichment.dataContainer.info.api << connector.getAPIQuery('<identifier>', null)
        
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
        
        stash.get(stashIndex).each{ uid, key ->

            if(!master.isRunning) {
                log.info('Aborted by user action.')
                return
            }

            increaseProgress()
            def pollStatus = connector.poll(key, stash.getKeyType(uid))

            // fallback for empty api response
            if (pollStatus == AbstractEnvelope.STATUS_NO_RESPONSE) {
                log.info("AbstractEnvelope.STATUS_NO_RESPONSE @ " + key)
                processor.processEntry(master.enrichment.dataContainer, uid, key, null)
            }

            connector.getPicaRecords().eachWithIndex { pr, i ->
                processor.processEntry(master.enrichment.dataContainer, uid, key, pr)
            }
        }
    }
    
    @Override
    void finish() throws Exception {
        log.info("finish()")
        
        def stash  = processor.getStash()
        def orgMap = DataMapper.getOrganisationMap()
        
        master.enrichment.dataContainer.titles.each { key, value ->
            DataMapper.mapHistoryEvents(master.enrichment.dataContainer, value.v, stash)
            DataMapper.mapOrganisations(orgMap, value.v)
        }
        master.enrichment.dataContainer.pkg.tipps.each { key, value ->
            DataMapper.mapPlatform(value.v, master.enrichment.dataContainer)
        }
    }
}
