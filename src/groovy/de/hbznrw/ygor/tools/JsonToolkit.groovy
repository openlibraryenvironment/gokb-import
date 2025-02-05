package de.hbznrw.ygor.tools


import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.*
import com.google.gson.Gson
import de.hbznrw.ygor.format.YgorFormatter
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.runtime.InvokerInvocationException
import ygor.Record
import ygor.field.HistoryEvent
import ygor.field.MultiField
import groovy.util.logging.Log4j

import java.lang.reflect.Method
import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
class JsonToolkit {

  private static ObjectMapper MAPPER = new ObjectMapper()
  private static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance
  private static Gson GSON = new Gson()
  final private static String ARRAY = "\$ARRAY"
  final private static String COUNT = "\$COUNT"

  final private static Pattern QUOTES_PATTERN = Pattern.compile("[\"'](.*)[\"']")

  private static def removeMetaClass(def dataStructure) {
    System.out.println(dataStructure?.class)
    if (dataStructure instanceof Map) {
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


  static ObjectNode getTippJsonFromRecord(String target, Record record, YgorFormatter formatter,
                                          char multiValueSeparator) {
    getJsonFromRecord(new ArrayList(Arrays.asList("\$TIPP")), target, record, formatter, multiValueSeparator)
  }


  static ObjectNode getTitleJsonFromRecord(String target, Record record, YgorFormatter formatter,
                                           char multiValueSeparator) {
    getJsonFromRecord(new ArrayList(Arrays.asList("\$TITLE")), target, record, formatter, multiValueSeparator)
  }


  static ObjectNode getCombinedTitleTippJsonFromRecord(String target, Record record, YgorFormatter formatter,
                                                       char multiValueSeparator) {
    getJsonFromRecord(new ArrayList(Arrays.asList("\$TITLE", "\$TIPP")), target, record, formatter, multiValueSeparator)
  }


  private static ObjectNode getJsonFromRecord(List<String> typeFilter, String target, Record record,
                                              YgorFormatter formatter, char multiValueSeparator) {
    ArrayList concatKeyStub = new ArrayList<>(typeFilter)
    if (concatKeyStub.size() == 2 && concatKeyStub.contains("\$TITLE") && concatKeyStub.contains("\$TIPP")){
      concatKeyStub.remove("\$TITLE")
    }
    ObjectNode result = MAPPER.createObjectNode()
    for (MultiField multiField in record.multiFields.values()) {
      if (multiField.keyMapping == null) {
        def value = multiField.getFirstPrioValue()
        ArrayList concatKey = new ArrayList<>(concatKeyStub)
        Iterator it = multiField.fields.iterator()
        if (it.hasNext()){
          concatKey.addAll(it.next().key)
        }
        upsertIntoJsonNode(result, concatKey, value, multiField.type, multiField.isMultiValueCapable,
            multiValueSeparator, formatter, false)
      }
      else {
        Set qualifiedKeys = multiField.keyMapping."${target}"
        qualifiedKeys.each { qualifiedKey ->
          ArrayList splitKey = qualifiedKey.split("\\.") as ArrayList
          if (splitKey.size() > 1 && splitKey[0] in typeFilter) {
            def value = multiField.getFirstPrioValue()
            upsertIntoJsonNode(result, splitKey, value, multiField.type, multiField.isMultiValueCapable,
                multiValueSeparator, formatter, multiField.keyMapping.keepIfEmpty)
          }
        }
      }
    }
    if ("\$TITLE" in typeFilter && record.historyEvents.size() > 0){
      ArrayNode historyEvents = MAPPER.createArrayNode()
      for (HistoryEvent he in record.historyEvents){
        if (he.isValid()){
          historyEvents.add(he.toJson())
        }
      }
      if (historyEvents.size() > 0){
        result.set("historyEvents", historyEvents)
      }
    }
    result
  }


  private static void upsertIntoJsonNode(JsonNode root, ArrayList<String> keyPath, String value, String type,
                                         boolean isMultiValueCapable, char multiValueSeparator, YgorFormatter formatter,
                                         boolean keepIfEmpty) {
    if (keyPath.size() <= 1){
      return
    }
    if (keyPath.size() == 2 && keyPath[1].startsWith("(")) {
      ObjectNode multiLeaf = buildMultiLeaf(keyPath, value)
      putAddNode(keyPath, root, multiLeaf)
    }
    else {
      if (keyPath.get(1).equals(ARRAY)) {
        upsertIntoJsonNode(root, keyPath[1..keyPath.size() - 1], value, type, isMultiValueCapable, multiValueSeparator,
            formatter, keepIfEmpty)
      }
      else if (keyPath.get(1).equals(COUNT)) {
        // TODO until now, only 1 element in array is supported ==> implement count
        if (root.size() == 0) {
          root.add(new ObjectNode(NODE_FACTORY))
        }
        upsertIntoJsonNode(root.get(0), keyPath[1..keyPath.size() - 1], value, type, isMultiValueCapable,
            multiValueSeparator, formatter, keepIfEmpty)
      }
      else {
        JsonNode subNode = getSubNode(keyPath, value, keepIfEmpty, isMultiValueCapable, multiValueSeparator)
        subNode = putAddNode(keyPath, root, subNode)
        if (keyPath.size() > 2) {
          // root is not final leaf --> iterate
          upsertIntoJsonNode(subNode, keyPath[1..keyPath.size() - 1], value, type, isMultiValueCapable,
              multiValueSeparator, formatter, keepIfEmpty)
        }
      }
    }
  }


  private static JsonNode getSubNode(ArrayList<String> keyPath, String value, boolean keepIfEmpty,
                                     boolean isFieldMultiValueCapable, char multiValueSeparator) {
    assert keyPath.size() > 1
    if (keyPath.size() == 2) {
      value = MultiField.extractFixedValue(value)
      if (isFieldMultiValueCapable){
        String [] values = value.split(String.valueOf(multiValueSeparator))
        ArrayNode result = new ArrayNode(NODE_FACTORY)
        for (String v in values){
          TextNode singleValueNode = new TextNode(v.trim())
          result.add(singleValueNode)
        }
        return result
      }
      else{
        if (value.equals("") && keepIfEmpty) {
          value = " " // this is obviously a hack without any harm. Correct implementation seems expensive.
        }
        return new TextNode(value)
      }
    }
    if (keyPath[2].equals(ARRAY)) {
      return new ArrayNode(NODE_FACTORY)
    }
    // else
    return new ObjectNode(NODE_FACTORY)
  }


  private static JsonNode putAddNode(ArrayList<String> keyPath, JsonNode root, JsonNode subNode) {
    if (root instanceof ArrayNode) {
      root.add(subNode)
    }
    else {
      if (isEmptyNode(root.get(keyPath[1]))) {
        root.put(keyPath[1], subNode)
      }
      else {
        subNode = root.get(keyPath[1])
      }
    }
    return subNode
  }


  private static ObjectNode buildMultiLeaf(ArrayList<String> keyPath, String value) {
    ObjectNode result = new ObjectNode(NODE_FACTORY)
    String[] singleNodes = keyPath[1].replaceAll("^\\(|\\)\$", "").split(",")
    for (int i = 1; i < singleNodes.length; i++) {
      String[] entry = singleNodes[i - 1].split(":")
      assert entry.length == 2
      result.put(entry[0], entry[1])
    }
    result.put(singleNodes[singleNodes.length - 1], value)
    result
  }


  private static boolean isEmptyNode(def obj) {
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
  static String toJson(Object obj) {
    Writer writer = new StringWriter()
    JsonGenerator jsonGenerator = new JsonFactory().createGenerator(writer)
    if (obj instanceof Collection) {
      if (obj instanceof Map) {
        jsonGenerator.writeStartObject()
        for (Map.Entry item in (obj as Map)) {
          jsonGenerator.writeObjectField(item.key, item.value.asJson(jsonGenerator))
        }
        jsonGenerator.writeEndObject()
      }
      else {
        jsonGenerator.writeStartArray()
        for (Object item in (obj as List)) {
          item.asJson(jsonGenerator)
        }
        jsonGenerator.writeEndArray()
      }
    }
    else {
      obj.asJson(jsonGenerator)
    }
    jsonGenerator.close()
    writer.toString()
  }


  synchronized static String mapToJson(Map<?, ?> map) {
    try{
      return GSON.toJson(map)
    }
    catch (Exception e){
      return null
    }
  }


  synchronized static String setToJson(Set<?> set) {
    try{
      return GSON.toJson(new ArrayList(set))
    }
    catch (Exception e){
      log.warn("Could not transform set to Json: ".concat(set.toString()))
      return null
    }
  }


  synchronized static String listToJson(List<?> list) {
    try{
      return GSON.toJson(list)
    }
    catch (Exception e){
      log.warn("Could not transform list to Json: ".concat(list.toString()))
      return null
    }
  }


  static List listFromJson(JsonNode root, String subField){
    String[] pathSplit = subField.split("\\.", 2)
    JsonNode subNode = root.path(pathSplit[0])
    if (pathSplit.length > 1) {
      return fromJson(subNode, pathSplit[1])
    }

  }


  static List listFromJson(String jsonString, Class targetType){
    return MAPPER.readValue(jsonString, MAPPER.getTypeFactory().constructCollectionType(List.class, targetType))
  }



  /**
   * Requires a fromJson(JsonNode) method for the desired object(s) class
   */
  static def fromJson(JsonNode root, String subField) {

    String[] pathSplit = subField.split("\\.", 2)
    JsonNode subNode = root.path(pathSplit[0])

    if (pathSplit.length > 1) {
      return fromJson(subNode, pathSplit[1])
    }
    if (subNode instanceof TextNode) {
      return subNode.asText()
    }
    if (subNode instanceof IntNode) {
      return subNode.asInt()
    }
    if (subNode instanceof ArrayNode) {
      List<String> result = new ArrayList<>()
      for (JsonNode arrayItemNode in subNode.asCollection()){
        String arrayItemText = arrayItemNode.toString()
        Matcher matcher = QUOTES_PATTERN.matcher(arrayItemText)
        if (matcher.matches()){
          arrayItemText = matcher.group(1)
        }
        result.add(arrayItemText)
      }
      return result
    }
    else if (subNode instanceof ObjectNode) {
      Class clazz = Class.forName("ygor.field.".concat(
          subField.substring(0, 1).toUpperCase() + subField.substring(1)))
      Method method = clazz.getMethod("fromJson", JsonNode.class)
      return method.invoke(null, subNode)
    }
    else {
      assert (subNode instanceof MissingNode || subNode instanceof NullNode)
      return null
    }
  }


  static JsonNode jsonNodeFromFile(File file){
    String json
    try{
      json = file.getInputStream()?.text
    }
    catch (MissingMethodException | InvokerInvocationException e){
      json = file.newInputStream()?.text
    }
    MAPPER.readTree(json)
  }


  static <T extends Enum<T>> T fromJson(JsonNode json, String subField, Class<T> enumClass){
    String value = JsonToolkit.fromJson(json, subField)
    if (value == null || value.equals("null")){
      return null
    }
    try{
      return Enum.valueOf(enumClass, value)
    }
    catch (IllegalArgumentException iae){
      log.debug(String.format("Could not create Enum instance of class %s with value %s", enumClass.getName(), value))
      return null
    }
  }


  synchronized static Map<?, ?> fromJsonNode(JsonNode mapNode){
    try{
      MAPPER.convertValue(mapNode, new TypeReference<Map<String, Object>>(){})
    }
    catch (Exception e){
      return null
    }
  }

}
