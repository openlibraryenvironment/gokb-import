package de.hbznrw.ygor.tools

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.google.gson.JsonObject
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVFormat
import de.hbznrw.ygor.export.DataContainer
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import ygor.Record
import ygor.field.MultiField

class JsonToolkit {

    private static ObjectMapper MAPPER = new ObjectMapper()
    private static JsonNodeFactory FACTORY = JsonNodeFactory.instance
    final private static String ARRAY = "\$COUNT"
    final private static String TYPE = "\$TYPE"
    final private static String VALUE = "\$VALUE"


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
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
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


    static JsonObject getTippJsonFromRecord(String target, Record record){
        getJsonFromRecord("\$TITLE", target, record)
    }


    static JsonObject getTitleJsonFromRecord(String target, Record record){
        getJsonFromRecord("\$TIPP", target, record)
    }


    private static JsonObject getJsonFromRecord (String typeFilter, String target, Record record){
        ObjectNode result = MAPPER.createObjectNode()
        for (MultiField multiField in record.multiFields.values()){
            Set qualifiedKeys = multiField.keyMapping."${target}"
            qualifiedKeys.each {qualifiedKey ->
                ArrayList splittedKey = qualifiedKey.split("\\.") as ArrayList
                if (splittedKey.size() > 1 && splittedKey[0].equals(typeFilter)){
                    // JsonNode node = getJsonNodeFromSplitString(ARRAY, splittedKey[1..splittedKey.size()-1], multiField.getPrioValue())
                    ArrayList subarray = splittedKey[1..splittedKey.size()-1]
                    def value = multiField.getPrioValue()
                    upsertIntoJsonNode(result, subarray, value)
                }
            }
        }
    }


    /*
    private static JsonNode getJsonNodeFromSplitString(String[] keyPath, String value){

    }
    */

    private static void upsertIntoJsonNode(JsonNode root, ArrayList<String> keyPath, String value){
        if (keyPath.size() < 1){
            return
        }
        if (keyPath[0].startsWith("(")){
            // TODO special case with additional text node
        }
        else if (keyPath.size() == 1){
            // root is final leaf / text node
            root.put(keyPath[0], value)
        }
        else {
            JsonNode subNode = getSubNode(keyPath)
            if (keyPath[0].equals(ARRAY)){
                root.add(subNode)
            }
            else {
                root.put(keyPath[0], subNode)
            }
            upsertIntoJsonNode(subNode, keyPath[1..keyPath.size() - 1], value)
        }

        /* else if (keyPath[0].equals(ARRAY)){
            // add element to array
            JsonNode subNode = getSubNode(keyPath)
            root.add(subNode)
            upsertIntoJsonNode(subNode, keyPath[1..keyPath.size()-1], value)
        }

        else if (root.get(keyPath[0])){
            // child already exists
            upsertIntoJsonNode(root.get(keyPath[0]), keyPath[1..keyPath.size()-1], value)
        }
        else{
            // specified child node does not exist, yet
            JsonNode subNode = getSubNode(keyPath)
            root.put(keyPath[0], subNode)
            ArrayList<String> subPath = keyPath[1..keyPath.size()-1]
            upsertIntoJsonNode(subNode, subPath, value)
        } */
    }


    private static JsonNode getSubNode(ArrayList<String> keyPath){
        if (keyPath.size() < 2){
            return null
        }
        if (keyPath.size() == 2){
            if (keyPath[1].startsWith("(")){
                return new ObjectNode(FACTORY)
            }
            else{
                return new TextNode(FACTORY)
            }
        }
        if (keyPath[1].equals(ARRAY)){
            return new ArrayNode(FACTORY)
        }
        // else
        return new ObjectNode(FACTORY)
    }
}
