package de.hbznrw.ygor.iet.bridge

import java.util.ArrayList;

import org.apache.commons.csv.CSVRecord;

import de.hbznrw.ygor.iet.connector.*
import de.hbznrw.ygor.iet.enums.Query;
import de.hbznrw.ygor.iet.formatadapter.*
import de.hbznrw.ygor.iet.interfaces.*
import de.hbznrw.ygor.iet.processor.CsvProcessor
import de.hbznrw.ygor.tools.FileToolkit

class GbvBridge extends BridgeAbstract implements BridgeInterface {
	
    static final IDENTIFIER = 'gbv'
    
    // api requests to do 
	def query = [
        Query.ZDBID,
        Query.GBV_GVKPPN,
        Query.GBV_PISSN,
        Query.GBV_EISSN,
        Query.GBV_TITLE,
        Query.GBV_PUBLISHER,
        Query.GBV_PUBLISHED_FROM,
        Query.GBV_PUBLISHED_TO,
        Query.GBV_TIPP_URL
        ]
    
    HashMap options
	
	GbvBridge(Thread master, HashMap options) {
        this.master  = master
        this.options = options

		this.connector     = new SruPicaConnector(this)
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
