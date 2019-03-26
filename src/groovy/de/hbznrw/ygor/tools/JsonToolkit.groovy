package de.hbznrw.ygor.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVFormat
import de.hbznrw.ygor.export.DataContainer
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

class JsonToolkit {

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

        // dc.titles = removeMetaClass(dc.titles)

        ObjectMapper mapper = new ObjectMapper()
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        String jsonString = mapper.writeValueAsString(dc.titles)
        JsonOutput.prettyPrint(jsonString)

        jsonBuilder {
            // 'meta'    dc.info
            // 'package' dc.pkg
            'titles'  dc.titles
        }
        
        JsonOutput.prettyPrint(writer.toString())
    }

    private static def removeMetaClass(def dataStructure){
        System.out.println(dataStructure?.class)
        if (dataStructure instanceof Map){
            dataStructure.each { key, value ->
                removeMetaClass(value)
            }
        }
        else if (dataStructure instanceof Collection) {
            dataStructure.each { item ->
                removeMetaClass(item)
            }
        }
        else if (dataStructure.getClass().isArray()) {
            dataStructure.each { item ->
                removeMetaClass(item)
            }
        }
        else {
            def iter = dataStructure.properties.iterator()
            while (iter.hasNext()) {
                def entry = iter.next()
                if (entry.key in ["metaClass", "class"]) {
                    dataStructure.properties.remove(entry.key)
                } else {
                    if (entry.value) {
                        System.out.println(entry.value)
                        removeMetaClass(entry.value)
                    }
                }
            }
        }
    }
}
