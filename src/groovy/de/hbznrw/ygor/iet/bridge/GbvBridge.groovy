package de.hbznrw.ygor.iet.bridge

import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;

import de.hbznrw.ygor.iet.connector.*
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.*
import de.hbznrw.ygor.iet.formatadapter.*
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.iet.processor.CsvProcessor
import de.hbznrw.ygor.tools.FileToolkit

class GbvBridge extends BridgeAbstract implements BridgeInterface {
	
    static final IDENTIFIER = 'gbv'
    
	def tasks = [
        Query.ZDBID,
        Query.GBV_GVKPPN,
        Query.GBV_PISSN,
        Query.GBV_EISSN,
        Query.GBV_TITLE,
        Query.GBV_PUBLISHER,
        Query.GBV_PUBLISHED_FROM,
        Query.GBV_PUBLISHED_TO,
        Query.GBV_TIPP_URL,
        Query.GBV_PLATFORM_URL,
        Query.GBV_TIPP_COVERAGE,
        Query.GBV_HISTORY_EVENTS
        ]
    
    HashMap options
	
	GbvBridge(Thread master, HashMap options) {
        this.master  = master
        this.options = options

		this.connector     = new SruPicaConnector(this)
		this.processor     = master.processor
        processor.setBridge(this)
	}
	
	@Override
	void go() throws Exception {
        println("Input:  " + options.get('inputFile'))
        
        master.enrichment.dataContainer.info.api << connector.getAPIQuery('<issn>')
        
        processor.setConfiguration(",", null, null)
        processor.processFile(options)
	}
	
	@Override
	void go(String outputFile) throws Exception {
		println("deprecated function call go(outputFile)")
	}
    
    @Override
    void workOffStash(Object stash) throws Exception {
        println "GbvBridge.processStash()"

        stash['issn'].each{ key, value ->

            if(!master.isRunning) {
                println('Aborted by user action.')
                return
            }

            increaseProgress()
            connector.poll(key)
            
            connector.picaRecords.eachWithIndex { pr, i ->
                
                def uid   = UUID.randomUUID().toString()
                def title = processor.processEntry(master.enrichment.dataContainer, uid, pr)
                def zdbid
                // TODO: fix empty zdbid
                
                title.identifiers.each{ ident ->
                    if(ident.value.m == Status.VALIDATOR_IDENTIFIER_IS_VALID && ident.type.v == ZdbBridge.IDENTIFIER){
                        zdbid = ident.value.v
                    }
                }
                stash[ZdbBridge.IDENTIFIER] << ["${zdbid}":"${uid}"]
            }
        }
    }
    
    @Override
    void finish(Object stash) throws Exception {
        println "GbvBridge.finish()"
        
        master.enrichment.dataContainer.titles.each { key, value ->
            Mapper.mapHistoryEvents(master.enrichment.dataContainer, value.v, stash)
        }
    }
}
