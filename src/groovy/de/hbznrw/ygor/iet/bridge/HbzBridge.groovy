package de.hbznrw.ygor.iet.bridge

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

class HbzBridge extends BridgeAbstract implements BridgeInterface {
	
    def query = [
        Query.HBZID
    ]
    
	HashMap options
	
	HbzBridge(Thread master, HashMap options) {
        this.master  = master
        this.options = options
		
		this.connector     = new LobidConnector(this)
		this.processor     = new CsvProcessor(this)
	}
	
	@Override
	void go() throws Exception {
		def outputFile = FileToolkit.getDateTimePrefixedFileName(inputFile)
		go(outputFile)
	}
	
	@Override
	void go(String outputFile) throws Exception {
		options << [outputFile : outputFile]
        
        println("Input:  " + options.get('inputFile'))
        println("Output: " + options.get('outputFile') + "\n")
        
        processor.setConfiguration(",", null, null)
        processor.processFile(options)
	}
}
