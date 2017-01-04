package de.hbznrw.ygor.iet.bridge

import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;

import de.hbznrw.ygor.iet.connector.*
import de.hbznrw.ygor.iet.enums.Query;
import de.hbznrw.ygor.iet.formatadapter.*
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.iet.processor.CsvProcessor
import de.hbznrw.ygor.tools.FileToolkit

class ZdbBridge extends BridgeAbstract implements BridgeInterface {
	
    static final IDENTIFIER = 'zdb'
    
    // api requests to do
	def query = [
        Query.ZDBID,
        Query.ZDBTITLE,
        Query.ZDBPUBLISHER
        ]
    
    HashMap options
	
	ZdbBridge(Thread master, HashMap options) {
        this.master  = master
        this.options = options

		this.connector     = new SruConnector(this)
		this.processor     = new CsvProcessor(this)
	}
	
	@Override
	void go() throws Exception {
		println("Input:  " + options.get('inputFile'))
        
        processor.setConfiguration(",", null, null)
        processor.processFile(options)
	}
	
	@Override
	void go(String outputFile) throws Exception {
		println("deprecated function call go(outputFile)")
	}
}
