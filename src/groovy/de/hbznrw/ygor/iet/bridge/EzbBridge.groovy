package de.hbznrw.ygor.iet.bridge

import java.util.ArrayList;
import java.util.HashMap
import java.util.LinkedHashMap

import org.apache.commons.csv.CSVRecord;

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.connector.*
import de.hbznrw.ygor.iet.enums.Query;
import de.hbznrw.ygor.iet.formatadapter.*
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.iet.processor.CsvProcessor
import de.hbznrw.ygor.tools.FileToolkit

class EzbBridge extends BridgeAbstract implements BridgeInterface {
	
    static final IDENTIFIER = 'ezb'
    
	def tasks = [
        Query.EZBID
    ]
    
	HashMap options
	
	EzbBridge(Thread master, HashMap options) {
        this.master  = master
		this.options = options
		
		this.connector     = new EzbConnector(this)
		this.processor     = master.processor
        processor.setBridge(this)
	}
	
	@Override
	void go() throws Exception {
		println("Input:  " + options.get('inputFile'))
        
        master.enrichment.dataContainer.info.api << connector.getAPIQuery('<zdbid>')
        
        processor.setConfiguration(",", null, null)
        processor.processFile(options)
	}
	
	@Override
	void go(String outputFile) throws Exception {
		println("deprecated function call go(outputFile)")
	}
    
    @Override
    void workOffStash(Object stash) throws Exception {
        println "EzbBridge.processStash()"
        
        stash['zdb'].each{ key, value ->
            
            if(!master.isRunning) {
                println('Aborted by user action.')
                return
            }
            
            increaseProgress()
            connector.poll(key)
            
            processor.processEntry(master.enrichment.dataContainer, value)
        }
    }
}
