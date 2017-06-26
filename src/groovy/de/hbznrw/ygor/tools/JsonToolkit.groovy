package de.hbznrw.ygor.tools;

import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVFormat
import de.hbznrw.ygor.export.DataContainer
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

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
    
    static Object parseFileToJson(String filename) {
        
        def file        = new File(filename)
        def jsonSlurper = new JsonSlurper()
        def json        = jsonSlurper.parseText(file.getText())
        
        json
    }
    
    static String parseDataToJson(DataContainer dc) {
        
        def writer      = new StringWriter()
        def jsonBuilder = new groovy.json.StreamingJsonBuilder(writer)
     
        jsonBuilder {
            'meta'    dc.info
            'package' dc.pkg
            'titles'  dc.titles
        }
        
        JsonOutput.prettyPrint(writer.toString())
    }
}
