package de.hbznrw.ygor.tools

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.*
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.format.YgorFormatter
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils
import ygor.Record
import ygor.field.MultiField

import java.lang.reflect.Method

class JsonToolkit {

    private static ObjectMapper MAPPER = new ObjectMapper()
    private static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance
    private static JsonFactory FACTORY = new JsonFactory()
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
            'titles'  dc.titles
            'tipps'   dc.tipps
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


    static ObjectNode getTippJsonFromRecord(String target, Record record, YgorFormatter formatter){
        getJsonFromRecord("\$TIPP", target, record, formatter)
    }


    static ObjectNode getTitleJsonFromRecord(String target, Record record, YgorFormatter formatter){
        getJsonFromRecord("\$TITLE", target, record, formatter)
    }


    private static ObjectNode getJsonFromRecord(String typeFilter, String target, Record record,
                                                YgorFormatter formatter){
        ObjectNode result = MAPPER.createObjectNode()
        for (MultiField multiField in record.multiFields.values()){
            if (multiField.keyMapping == null){
                def value = multiField.getPrioValue()
                ArrayList concatKey = Arrays.asList(typeFilter)
                concatKey.addAll(multiField.fields.values().iterator().next().key)
                upsertIntoJsonNode(result, concatKey, value, multiField.type, formatter)
            }
            else{
                Set qualifiedKeys = multiField.keyMapping."${target}"
                qualifiedKeys.each {qualifiedKey ->
                    ArrayList splitKey = qualifiedKey.split("\\.") as ArrayList
                    if (splitKey.size() > 1 && splitKey[0].equals(typeFilter)){
                        // JsonNode node = getJsonNodeFromSplitString(ARRAY, splittedKey[1..splittedKey.size()-1], multiField.getPrioValue())
                        def value = multiField.getPrioValue()
                        upsertIntoJsonNode(result, splitKey, value, multiField.type, formatter)
                    }
                }
            }
        }
        result
    }


    private static void upsertIntoJsonNode(JsonNode root, ArrayList<String> keyPath, String value, String type,
                                           YgorFormatter formatter){
        assert keyPath.size() > 1
        if (keyPath.size() == 2 && keyPath[1].startsWith("(")){
            ObjectNode multiLeaf = buildMultiLeaf(keyPath, value)
            putAddNode(keyPath, root, multiLeaf)
        }
        else{
            if (keyPath.get(1).equals(ARRAY)){
                upsertIntoJsonNode(root, keyPath[1..keyPath.size() - 1], value, type, formatter)
            }
            else if (keyPath.get(1).equals(COUNT)){
                // TODO until now, only 1 element in array is supported ==> implement count
                if (root.size() == 0){
                    root.add(new ObjectNode(NODE_FACTORY))
                }
                upsertIntoJsonNode(root.get(0), keyPath[1..keyPath.size() - 1], value, type, formatter)
            }
            else {
                JsonNode subNode = getSubNode(keyPath, value, type, formatter)
                subNode = putAddNode(keyPath, root, subNode)
                if (keyPath.size() > 2) {
                    // root is not final leaf --> iterate
                    upsertIntoJsonNode(subNode, keyPath[1..keyPath.size() - 1], value, type, formatter)
                }
            }
        }
    }


    private static JsonNode getSubNode(ArrayList<String> keyPath, String value, String type, YgorFormatter formatter){
        assert keyPath.size() > 1
        if (keyPath.size() == 2){
            return new TextNode(GokbExporter.no)
        }
        if (keyPath[2].equals(ARRAY)){
            return new ArrayNode(NODE_FACTORY)
        }
        // else
        return new ObjectNode(NODE_FACTORY)
    }


    private static JsonNode putAddNode(ArrayList<String> keyPath, JsonNode root, JsonNode subNode){
        if (root instanceof ArrayNode){
            root.add(subNode)
        }
        else {
            if (isEmptyNode(root.get(keyPath[1]))) {
                root.put(keyPath[1], subNode)
            }
            else{
                subNode = root.get(keyPath[1])
            }
        }
        return subNode
    }


    private static ObjectNode buildMultiLeaf(ArrayList<String> keyPath, String value){
        ObjectNode result = new ObjectNode(NODE_FACTORY)
        String[] singleNodes = keyPath[1].replaceAll("^\\(|\\)\$", "").split(",")
        for (int i=1; i<singleNodes.length; i++){
            String[] entry = singleNodes[i-1].split(":")
            assert entry.length == 2
            result.put(entry[0], entry[1])
        }
        result.put(singleNodes[singleNodes.length-1], value)
        result
    }


    private static boolean isEmptyNode (def obj){
        if (obj == null)
            return true
        if (obj instanceof String && StringUtils.isEmpty(obj))
            return true
        if (obj instanceof TextNode && obj.toString() == "\"\"")
            return true
        return false
    }


    /**
     * Requires an asJson(JsonGenerator) method for the given object class or Collection of given object(s) class
     */
    static String toJson(Object obj){
        Writer writer = new StringWriter()
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(writer)
        if (obj instanceof Collection){
            if (obj instanceof Map){
                jsonGenerator.writeStartObject()
                for (Map.Entry item in (obj as Map)){
                    jsonGenerator.writeObjectField(item.key, item.value.asJson(jsonGenerator))
                }
                jsonGenerator.writeEndObject()
            }
            else{
                jsonGenerator.writeStartArray()
                for (Object item in (obj as List)){
                    item.asJson(jsonGenerator)
                }
                jsonGenerator.writeEndArray()
            }
        }
        else{
            obj.asJson(jsonGenerator)
        }
        jsonGenerator.close()
        writer.toString()
    }


    /**
     * Requires a fromJson(JsonNode) method for the desired object(s) class
     */
    static Object fromJson(JsonNode root, String subField){

        String[] pathSplit = subField.split("\\.", 2)
        JsonNode subNode = root.path(pathSplit[0])

        if (pathSplit.length > 1){
            return fromJson(subNode, pathSplit[1])
        }
        if (subNode instanceof TextNode){
            return subNode.asText()
        }
        else if (subNode instanceof ArrayNode){
            int i=0 // TODO
        }
        else if (subNode instanceof ObjectNode) {
            Class clazz = Class.forName("ygor.field.".concat(
                    subField.substring(0, 1).toUpperCase() + subField.substring(1)))
            Method method = clazz.getMethod("fromJson", JsonNode.class)
            return method.invoke(null, subNode)
        }
        else{
            assert (subNode instanceof MissingNode || subNode instanceof NullNode)
            return null
        }
    }

}
