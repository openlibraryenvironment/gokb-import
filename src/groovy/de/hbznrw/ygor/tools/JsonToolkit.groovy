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
    final private static String ARRAY = "\$ARRAY"
    final private static String COUNT = "\$COUNT"


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


    static ObjectNode getTippJsonFromRecord(String target, Record record){
        getJsonFromRecord("\$TIPP", target, record)
    }


    static ObjectNode getTitleJsonFromRecord(String target, Record record){
        getJsonFromRecord("\$TITLE", target, record)
    }


    private static ObjectNode getJsonFromRecord (String typeFilter, String target, Record record){
        ObjectNode result = MAPPER.createObjectNode()
        for (MultiField multiField in record.multiFields.values()){
            Set qualifiedKeys = multiField.keyMapping."${target}"
            qualifiedKeys.each {qualifiedKey ->
                ArrayList splitKey = qualifiedKey.split("\\.") as ArrayList
                if (splitKey.size() > 1 && splitKey[0].equals(typeFilter)){
                    // JsonNode node = getJsonNodeFromSplitString(ARRAY, splittedKey[1..splittedKey.size()-1], multiField.getPrioValue())
                    ArrayList subarray = splitKey // [1..splittedKey.size()-1]
                    def value = multiField.getPrioValue()
                    upsertIntoJsonNode(result, subarray, value)
                }
            }
        }
        result
    }


    private static void upsertIntoJsonNode(JsonNode root, ArrayList<String> keyPath, String value){
        assert keyPath.size() > 1
        if (keyPath.size() == 2 && keyPath[1].startsWith("(")){
            ObjectNode multiLeaf = buildMultiLeaf(keyPath, value)
            putAddNode(keyPath, root, multiLeaf)
        }
        else{
            if (keyPath.get(1).equals(COUNT)){
                upsertIntoJsonNode(root, keyPath[1..keyPath.size() - 1], value)
            }
            else {
                JsonNode subNode = getSubNode(keyPath, value)
                subNode = putAddNode(keyPath, root, subNode)
                if (keyPath.size() > 2) {
                    // root is not final leaf --> iterate
                    upsertIntoJsonNode(subNode, keyPath[1..keyPath.size() - 1], value)
                }
            }
        }
    }


    private static JsonNode getSubNode(ArrayList<String> keyPath, String value){
        assert keyPath.size() > 1
        if (keyPath.size() == 2){
            return new TextNode(value)
        }
        if (keyPath[2].equals(COUNT) || keyPath[2].equals(ARRAY)){
            return new ArrayNode(FACTORY)
        }
        // else
        return new ObjectNode(FACTORY)
    }


    private static JsonNode putAddNode(ArrayList<String> keyPath, JsonNode root, JsonNode subNode){
        if (root instanceof ArrayNode){
            root.add(subNode)
        }
        else {
            if (root.get(keyPath[1]) == null) {
                root.put(keyPath[1], subNode)
            }
            else{
                subNode = root.get(keyPath[1])
            }
        }
        return subNode
    }


    private static ObjectNode buildMultiLeaf(ArrayList<String> keyPath, String value){
        ObjectNode result = new ObjectNode(FACTORY)
        String[] singleNodes = keyPath[1].replaceAll("^\\(|\\)\$", "").split(",")
        for (int i=1; i<singleNodes.length; i++){
            String[] entry = singleNodes[i-1].split(":")
            assert entry.length == 2
            result.put(entry[0], entry[1])
        }
        result.put(singleNodes[singleNodes.length-1], value)
        result
    }
}
