package de.hbznrw.ygor.tools;

import java.io.File;

import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord

import de.hbznrw.ygor.iet.export.DataContainer
import ygor.Enrichment
import groovy.json.JsonOutput

public class JsonToolkit {

    static String parseCsvToJson(File file) {
        
        def writer      = new StringWriter()
        def jsonBuilder = new groovy.json.StreamingJsonBuilder(writer)
        def records     = []
        
        file.withReader { reader ->
            CSVParser csv = new CSVParser(reader, CSVFormat.EXCEL)
            for (record in csv.iterator()) {
                records << record
            }
        }
                
        jsonBuilder {
            // TODO
        }
        
        JsonOutput.prettyPrint(writer.toString())
    }
    
    static String parseDataToJson(DataContainer data) {
        
        def writer      = new StringWriter()
        def jsonBuilder = new groovy.json.StreamingJsonBuilder(writer)
     
        jsonBuilder {
            'meta'    data.meta
            'package' data.pkg
            'titles'  data.titles
        }
        
        JsonOutput.prettyPrint(writer.toString())
    }
}
