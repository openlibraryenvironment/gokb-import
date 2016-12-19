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
	
	def query = [
        Query.ZDBID,
        Query.ZDBTITLE
        ]
    
	String inputFile
	int    indexOfKey
	
	ZdbBridge(Thread master, String inputFile, int indexOfKey) {
        this.master     = master
		this.inputFile  = inputFile
		this.indexOfKey = indexOfKey
		
		this.connector     = new SruConnector(this)
		this.processor     = new CsvProcessor(this)
	}
	
	@Override
	void go() throws Exception {
		def outputFile = FileToolkit.getDateTimePrefixedFileName(inputFile)
		go(outputFile)
	}
	
	@Override
	void go(String outputFile) throws Exception {
		println("Input:  " + inputFile)
		println("Output: " + outputFile + "\n")
		
		processor.setConfiguration(",", null, null)
		processor.processFile(inputFile, indexOfKey, outputFile)
	}
}
