package ygor.field

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.normalizers.CommonNormalizer
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.validators.Validator
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.plugins.web.taglib.ValidationTagLib

import java.util.regex.Matcher
import java.util.regex.Pattern

class MultiField {

  String ygorFieldKey
  String displayName
  FieldKeyMapping keyMapping          // TODO: keep in MappingsContainer only and access by ygorFieldKey (?)
  List fields = []
  String type                         // TODO: move to FieldKeyMapping (?)
  String status
  String normalized = null
  String revised = null

  static hasMany = [fields: Field]

  final private static Pattern FIXED_PATTERN = Pattern.compile("\\{fixed=(.*)}")
  final private static def g = new ValidationTagLib()

  static constraints = {
  }


  MultiField(FieldKeyMapping fieldKeyMapping) {
    if (fieldKeyMapping != null) {
      this.ygorFieldKey = fieldKeyMapping.ygorKey
      if (fieldKeyMapping.displayName != null){
        this.displayName = fieldKeyMapping.displayName
      }
      else{
        this.displayName = fieldKeyMapping.ygorKey
      }
      this.type = fieldKeyMapping.type
      keyMapping = fieldKeyMapping
    }
  }


  def addField(Field field) {
    if (keyMapping == null) {
      fields.add(field)
    }
    else {
      for (mappedKey in keyMapping.get(field.source)) {
        if (field.key == mappedKey) {
          fields.add(field)
          break
        }
      }
    }
  }


  String getFirstPrioValue() {
    return getPrioValues()[0]
  }


  List<String> getPrioValues() {
    if (revised != null) {
      return [revised]
    }
    if (normalized != null) {
      return [normalized]
    }
    if (keyMapping == null) {
      return fields
    }
    if (keyMapping.valIsFix) {
      return [extractFixedValue(keyMapping.val)]
    }
    // no fixed value --> search for collected values
    for (source in keyMapping.sourcePrio) {
      List<String> prioFields = getFieldValuesBySource(source)
      if (!prioFields.isEmpty()) {
        return prioFields
      }
    }
    // no collected value --> return default value (if any)
    return [keyMapping.val]
  }


  String getFieldValue(String source, int index){
    List<String> allValuesOfThisSource = getFieldValuesBySource(source)
    if (allValuesOfThisSource.size() <= index){
      return null
    }
    return allValuesOfThisSource.get(index)
  }


  List<String> getFieldValuesBySource(String source){
    List<String> result = []
    for (Field field in fields){
      if (field.source == source){
        result.add(field.value)
      }
    }
    return result
  }


  List<Field> getFields(String source){
    List<Field> result = []
    for (Field field in fields){
      if (field.source == source){
        result.add(field)
      }
    }
    result
  }


  String getPrioSource() {
    if (revised != null){
      return g.message(code: 'record.source.revised')
    }
    if (keyMapping == null) {
      return "kbart"
    }
    if (keyMapping.valIsFix) {
      return g.message(code:'record.source.default')
    }
    // no fixed value --> search for collected values
    for (source in keyMapping.sourcePrio) {
      def values = getFieldValuesBySource(source)
      if (!values.isEmpty()) {
        return source
      }
    }
    // no collected value --> return default value (if any)
    return g.message(code:'record.source.default')
  }


  void normalize(String namespace) {
    normalized = CommonNormalizer.normalize(this, type, namespace)
  }


  void validate(String namespace) {
    String value = getFirstPrioValue()
    if (keyMapping != null && keyMapping.allowedValues != null && !keyMapping.allowedValues.isEmpty()){
      if (!(value in keyMapping.allowedValues)){
        // this value is not allowed by the config "allowedValues" in YgorFieldKeyMapping.json
        if (StringUtils.isEmpty(value)){
          status = Status.MISSING
        }
        else{
          status = Status.INVALID
        }
        return
      }
    }
    status = Validator.validate(type, value, ygorFieldKey, namespace)
  }


  boolean isCriticallyIncorrect(String publicationType){
    if (keyMapping == null){
      return false
    }
    return "error".equals(keyMapping.getFlag(status, publicationType))
  }


  boolean isNonCriticallyIncorrect(String publicationType){
    if (keyMapping == null){
      return false
    }
    return "warning".equals(keyMapping.getFlag(status, publicationType))
  }


  boolean isCorrect(String publicationType){
    if (keyMapping == null){
      // fields without a key mapping cannot be evaluated
      return true
    }
    return "ok".equals(keyMapping.getFlag(status, publicationType))
  }


  String toString() {
    this.getClass().getName().concat(": ").concat(ygorFieldKey).concat(", fields: ").concat(fields.toString())
  }


  String asJson(JsonGenerator jsonGenerator) {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("ygorKey", ygorFieldKey)
    jsonGenerator.writeStringField("displayName", displayName)
    jsonGenerator.writeStringField("status", status)
    jsonGenerator.writeStringField("normalized", normalized)
    jsonGenerator.writeStringField("revised", revised)

    jsonGenerator.writeFieldName("fields")
    jsonGenerator.writeStartArray()
    for (Field f in fields) {
      f.asJson(jsonGenerator)
    }
    jsonGenerator.writeEndArray()
    jsonGenerator.writeEndObject()
  }


  static MultiField fromJson(JsonNode json, FieldKeyMapping mapping) {
    MultiField result = new MultiField(mapping)
    if (mapping == null) {
      result.ygorFieldKey = JsonToolkit.fromJson(json, "ygorKey")
    }
    result.displayName = JsonToolkit.fromJson(json, "displayName")
    result.status = JsonToolkit.fromJson(json, "status")
    result.normalized = JsonToolkit.fromJson(json, "normalized")
    result.revised = JsonToolkit.fromJson(json, "revised")
    Iterator it = json.path("fields").iterator()
    while (it.hasNext()) {
      JsonNode fieldNode = it.next()
      String source = JsonToolkit.fromJson(fieldNode, "source")
      String key = JsonToolkit.fromJson(fieldNode, "key")
      int index = JsonToolkit.fromJson(fieldNode, "index")
      String value = JsonToolkit.fromJson(fieldNode, "value")
      result.addField(new Field(source, key, index, value))
    }
    result
  }


  static String extractFixedValue(String value) {
    Matcher fixedMatcher = FIXED_PATTERN.matcher(value)
    if (fixedMatcher.matches()) {
      value = fixedMatcher.group(1)
    }
    value
  }
}
