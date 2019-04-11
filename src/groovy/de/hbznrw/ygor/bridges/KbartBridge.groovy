package de.hbznrw.ygor.bridges

import de.hbznrw.ygor.export.DataMapper
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
        Query.KBART_TIPP_ACCESS,
        Query.KBART_MONOGRAPH_VOLUME,
        Query.KBART_MONOGRAPH_EDITION,
        Query.KBART_FIRST_AUTHOR,
        Query.KBART_FIRST_EDITOR,
        Query.KBART_DATE_MONOGRAPH_PUBLISHED_PRINT,
        Query.KBART_DATE_MONOGRAPH_PUBLISHED_ONLINE,
        Query.KBART_TITLE_ID
    ]

	KbartBridge(Thread master, HashMap options) {
        this.master     = master
        this.options    = options
        this.connector  = new KbartConnector(this)

        if(this.options.dataTyp == 'ebooks') {
            this.connector.kbartKeys.add('publication_title')
            this.connector.kbartKeys.add('publisher_name')
            this.connector.kbartKeys.add('online_identifier')
            this.connector.kbartKeys.add('print_identifier')
            this.connector.kbartKeys.add('date_monograph_published_print')
            this.connector.kbartKeys.add('date_monograph_published_online')
            this.connector.kbartKeys.add('monograph_edition')
            this.connector.kbartKeys.add('monograph_volume')
            this.connector.kbartKeys.add('first_editor')
            this.connector.kbartKeys.add('first_author')

            this.tasks = this.tasks.plus(Query.KBART_TITLE)
            this.tasks = this.tasks.plus(Query.KBART_PUBLISHER)
            this.tasks = this.tasks.plus(Query.KBART_EISBN)
            this.tasks = this.tasks.plus(Query.KBART_PISBN)
            this.tasks = this.tasks.plus(Query.KBART_DATE_MONOGRAPH_PUBLISHED_PRINT)
            this.tasks = this.tasks.plus(Query.KBART_DATE_MONOGRAPH_PUBLISHED_ONLINE)
            this.tasks = this.tasks.plus(Query.KBART_MONOGRAPH_EDITION)
            this.tasks = this.tasks.plus(Query.KBART_MONOGRAPH_VOLUME)
            this.tasks = this.tasks.plus(Query.KBART_FIRST_EDITOR)
            this.tasks = this.tasks.plus(Query.KBART_FIRST_AUTHOR)
        }

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
            connector.poll(uid, stash.getKeyType(uid), null)
            
            // TODO refactor
            processor.processEntry(master.enrichment.dataContainer, uid, 'TODO_REFACTOR_TO_ZdbBridge.IDENTIFIER')
        }
    }
    @Override
    void finish() throws Exception {
        log.info("finish()")

        def stash  = processor.getStash()
        def orgMap = DataMapper.getOrganisationMap()

//        master.enrichment.dataContainer.titles.each { key, value ->
//            DataMapper.mapHistoryEvents(master.enrichment.dataContainer, value.v, stash)
//            DataMapper.mapOrganisations(orgMap, value.v)
//        }
        master.enrichment.dataContainer.pkg.tipps.each { key, value ->
            DataMapper.mapPlatform(value.v, master.enrichment.dataContainer)
        }
    }

}
