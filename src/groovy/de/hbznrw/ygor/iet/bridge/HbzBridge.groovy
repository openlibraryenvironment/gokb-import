package de.hbznrw.ygor.iet.bridge

import groovy.util.logging.Log4j

import java.util.ArrayList;
import java.util.HashMap

import org.apache.commons.csv.CSVRecord;

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.connector.*
import de.hbznrw.ygor.iet.enums.Query;
import de.hbznrw.ygor.iet.formatadapter.*
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.iet.processor.CsvProcessor
import de.hbznrw.ygor.tools.FileToolkit 

@Log4j
class HbzBridge extends BridgeAbstract implements BridgeInterface {
	
    static final IDENTIFIER = 'hbz'
    
    Query[] tasks = [
        Query.HBZID
    ]
    
	private HashMap options
	
	HbzBridge(Thread master, HashMap options) {
        this.master    = master
        this.options   = options
		this.connector = new LobidConnector(this)
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
