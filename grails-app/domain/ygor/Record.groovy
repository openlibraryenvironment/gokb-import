package ygor

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.normalizers.EditionNormalizer
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.validators.RecordValidator
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.grails.io.support.ClassPathResource
import ygor.field.HistoryEvent
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.*

class Record{

  static ObjectMapper MAPPER = new ObjectMapper()
  static List<String> GOKB_FIELD_ORDER = []
  static {
    MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    GOKB_FIELD_ORDER.addAll(new JsonSlurper().parseText(new ClassPathResource("/resources/GokbOutputFieldOrder.json").file.text))
  }

  String uid
  ZdbIdentifier zdbId
  EzbIdentifier ezbId
  DoiIdentifier doiId
  OnlineIdentifier onlineIdentifier
  PrintIdentifier printIdentifier
  String publicationType
  Map multiFields
  Map validation
  String zdbIntegrationDate
  String ezbIntegrationDate
  String zdbIntegrationUrl
  String ezbIntegrationUrl
  List historyEvents
  Map<AbstractIdentifier, String> duplicates


  static hasMany = [multiFields       : MultiField,
                    validation        : Status,
                    historyEvents     : HistoryEvent,
                    duplicates        : String]

  static constraints = {
  }


  Record(List<AbstractIdentifier> ids, MappingsContainer container) {
    this(ids, container, UUID.randomUUID().toString())
  }


  Record(List<AbstractIdentifier> ids, MappingsContainer container, String uid) {
    this.uid = uid
    for (id in ids) {
      addIdentifier(id)
    }
    multiFields = [:]
    validation = [:]
    duplicates = [:]
    historyEvents = []
    for (def ygorMapping in container.ygorMappings) {
      multiFields.put(ygorMapping.key, new MultiField(ygorMapping.value))
    }
    zdbIntegrationDate = null
    ezbIntegrationDate = null
    zdbIntegrationUrl = null
    ezbIntegrationUrl = null
  }


  void addIdentifier(AbstractIdentifier identifier) {
    if (identifier instanceof ZdbIdentifier) {
      if (zdbId && identifier.identifier.replaceAll("x", "X") != zdbId.identifier.replaceAll("x", "X")) {
        throw new IllegalArgumentException("ZDB id ${identifier} already set to ${zdbId} for record")
      }
      zdbId = identifier
    }
    else if (identifier instanceof EzbIdentifier) {
      if (ezbId && identifier.identifier != ezbId.identifier) {
        throw new IllegalArgumentException("EZB id ${identifier} already set to ${ezbId} for record")
      }
      ezbId = identifier
    }
    else if (identifier instanceof DoiIdentifier) {
      if (doiId && identifier.identifier != doiId.identifier) {
        throw new IllegalArgumentException("DOI ${identifier} already set to ${doiId} for record")
      }
      doiId = identifier
    }
    else if (identifier instanceof OnlineIdentifier) {
      if (onlineIdentifier && identifier.identifier != onlineIdentifier.identifier) {
        throw new IllegalArgumentException("EISSN ${identifier} already set to ${onlineIdentifier} for record")
      }
      onlineIdentifier = identifier
    }
    else if (identifier instanceof PrintIdentifier) {
      if (printIdentifier && identifier.identifier != printIdentifier.identifier) {
        throw new IllegalArgumentException("ISSN ${identifier} already set to ${printIdentifier} for record")
      }
      printIdentifier = identifier
    }
  }


  void normalize(String namespace) {
    EditionNormalizer.normalizeEditionNumber(this)
    for (MultiField multiField in multiFields.values()) {
      multiField.normalize(namespace)
    }
  }


  void deriveHistoryEventObjects(Enrichment enrichment) {
    // first, re-set history - there might be old events of previous calculations
    historyEvents = []
    for (int index = 0; index < multiFields.get("historyEventDate").getFields(MappingsContainer.ZDB).size(); index++){
      historyEvents << new HistoryEvent(this, index, enrichment)
    }
  }


  boolean isValid() {
    // validate tipp.titleUrl
    MultiField urlMultiField = multiFields.get("titleUrl")
    if (urlMultiField == null) {
      return false
    }
    // check multifields for critical errors
    for (MultiField multiField in multiFields.values()){
      if (multiField.isCriticallyIncorrect(publicationType)){
        return false
      }
    }
    return true
  }


  void validate(String namespace) {
    this.validateMultifields(namespace)
    RecordValidator.validateCoverage(this)
    RecordValidator.validateHistoryEvent(this)
    RecordValidator.validatePublisherHistory(this)
  }


  void addValidation(String property, Status status) {
    validation.put(property, status)
  }


  void addDuplicates(AbstractIdentifier id, Set<Record> recordUids){
    for (Record rec in recordUids){
      if (rec.uid != this.uid && !haveDistinctiveId(this, rec)){
        duplicates.put(id, rec.uid)
      }
    }
  }


  static haveDistinctiveId(Record rec1, Record rec2){
    if (rec1.zdbId?.identifier != null && rec2.zdbId?.identifier != null &&
        rec1.zdbId.identifier != rec2.zdbId.identifier){
      return true
    }
    if (rec1.onlineIdentifier?.identifier != null && rec2.onlineIdentifier?.identifier != null &&
        rec1.onlineIdentifier.identifier != rec2.onlineIdentifier.identifier){
      return true
    }
    if (rec1.printIdentifier?.identifier != null && rec2.printIdentifier?.identifier != null &&
        rec1.printIdentifier.identifier != rec2.printIdentifier.identifier){
      return true
    }
    // else
    return false
  }


  void addMultiField(MultiField multiField) {
    multiFields.put(multiField.ygorFieldKey, multiField)
  }


  MultiField getMultiField(def ygorFieldKey) {
    multiFields.get(ygorFieldKey)
  }


  List<MultiField> getFieldsByPath(String path){
    List<MultiField> result = []
    multiFields.each{ key, value ->
      if (key.contains(path)){
        // TODO: this criterion works for now, but is not very precise
        //       possibly, it should be replaced with a check on MultiField.keyMapping.gokb.
        //       Optionally, use reflection for output sink ("gokb").
        result.add(value)
      }
    }
    return result
  }


  List<MultiField> multiFieldsInGokbOrder(){
    multiFields.values().sort{
      multiField -> (GOKB_FIELD_ORDER.indexOf(multiField.ygorFieldKey) > -1 ?
          GOKB_FIELD_ORDER.indexOf(multiField.ygorFieldKey) :
          GOKB_FIELD_ORDER.size())
    }
  }


  private void validateMultifields(String namespace) {
    multiFields.each { k, v -> v.validate(namespace) }
  }


  def getCoverage() {
    false // TODO
  }


  private void processHistoryEvents(){
    // for () // TODO
  }


  String asJson(JsonGenerator jsonGenerator) {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("uid", uid)
    jsonGenerator.writeStringField("zdbId", zdbId?.identifier)
    jsonGenerator.writeStringField("ezbId", ezbId?.identifier)
    jsonGenerator.writeStringField("doiId", doiId?.identifier)
    jsonGenerator.writeStringField("eissn", onlineIdentifier?.identifier)
    jsonGenerator.writeStringField("issn", printIdentifier?.identifier)
    jsonGenerator.writeStringField("publicationType", publicationType)
    if (ezbIntegrationDate) {
      jsonGenerator.writeStringField("ezbIntegrationDate", ezbIntegrationDate)
    }
    if (ezbIntegrationUrl) {
      jsonGenerator.writeStringField("ezbIntegrationUrl", ezbIntegrationUrl)
    }
    if (zdbIntegrationDate) {
      jsonGenerator.writeStringField("zdbIntegrationDate", zdbIntegrationDate)
    }
    if (zdbIntegrationUrl) {
      jsonGenerator.writeStringField("zdbIntegrationUrl", zdbIntegrationUrl)
    }
    jsonGenerator.writeFieldName("multiFields")
    jsonGenerator.writeStartArray()
    for (MultiField mf in multiFields.values()) {
      mf.asJson(jsonGenerator)
    }
    if (!duplicates.isEmpty()){
      jsonGenerator.writeFieldName("duplicates")
      jsonGenerator.writeStartArray()
      for (def dup in duplicates){
        jsonGenerator.writeStringField(dup.key.toString(), dup.value)
      }
      jsonGenerator.writeEndArray()
    }
    jsonGenerator.writeEndArray()
    jsonGenerator.writeEndObject()
  }


  String asStatisticsJson() {
    Writer writer = new StringWriter()
    JsonGenerator jsonGenerator = new JsonFactory().createGenerator(writer)
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("uid", uid)
    for (MultiField mf in multiFields.values()) {
      jsonGenerator.writeFieldName(mf.ygorFieldKey)
      jsonGenerator.writeStartObject()
      jsonGenerator.writeStringField("value", mf.getFirstPrioValue())
      jsonGenerator.writeStringField("source", mf.getPrioSource())
      jsonGenerator.writeStringField("status", mf.status)
      jsonGenerator.writeEndObject()
    }
    jsonGenerator.writeEndObject()
    jsonGenerator.close()
    writer.toString()
  }


  Map<String, String> asMultiFieldMap() {
    Map<String, String> result = [:]
    result.put("uid", uid)
    if (ezbIntegrationDate) {
      result.put("ezbIntegrationDate", ezbIntegrationDate)
    }
    if (ezbIntegrationUrl) {
      result.put("ezbIntegrationUrl", ezbIntegrationUrl)
    }
    if (zdbIntegrationDate) {
      result.put("zdbIntegrationDate", zdbIntegrationDate)
    }
    if (zdbIntegrationUrl) {
      result.put("zdbIntegrationUrl", zdbIntegrationUrl)
    }
    for (def multiField in multiFields) {
      result.put(multiField.key, multiField.value.getFirstPrioValue())
    }
    result.put("displayTitle", this.getDisplayTitle())
    result
  }


  String getDisplayTitle(){
    List<String> titleFieldNames = ["publicationTitleVariation", "publicationSubTitle", "publicationTitle"]
    for (String displayTitleCandidateFieldNames in titleFieldNames){
      String value = multiFields.get(displayTitleCandidateFieldNames).getFirstPrioValue()
      if (!StringUtils.isEmpty(value)){
        return value
      }
    }
  }


  static Record fromJson(JsonNode json, MappingsContainer mappings) {
    List<AbstractIdentifier> ids = new ArrayList<>()
    ids.add(new ZdbIdentifier(JsonToolkit.fromJson(json, "zdbId"), mappings.getMapping("zdbId", MappingsContainer.YGOR)))
    ids.add(new EzbIdentifier(JsonToolkit.fromJson(json, "ezbId"), mappings.getMapping("ezbId", MappingsContainer.YGOR)))
    ids.add(new DoiIdentifier(JsonToolkit.fromJson(json, "doiId"), mappings.getMapping("doiId", MappingsContainer.YGOR)))
    ids.add(new OnlineIdentifier(JsonToolkit.fromJson(json, "eissn"), mappings.getMapping("onlineIdentifier", MappingsContainer.YGOR)))
    ids.add(new PrintIdentifier(JsonToolkit.fromJson(json, "issn"), mappings.getMapping("printIdentifier", MappingsContainer.YGOR)))
    String uid = JsonToolkit.fromJson(json, "uid")
    Record result = new Record(ids, mappings, uid)
    Iterator it = ((ArrayNode) (json.path("multiFields"))).iterator()
    while (it.hasNext()) {
      ObjectNode nextNode = it.next()
      String ygorKey = JsonToolkit.fromJson(nextNode, "ygorKey")
      result.addMultiField(MultiField.fromJson(nextNode, mappings.getMapping(ygorKey, MappingsContainer.YGOR)))
    }
    String ezbIntegrationDate = JsonToolkit.fromJson(json, "ezbIntegrationDate")
    if (ezbIntegrationDate) {
      result.ezbIntegrationDate = ezbIntegrationDate
    }
    String ezbIntegrationUrl = JsonToolkit.fromJson(json, "ezbIntegrationUrl")
    if (ezbIntegrationUrl) {
      result.ezbIntegrationUrl = ezbIntegrationUrl
    }
    String zdbIntegrationDate = JsonToolkit.fromJson(json, "zdbIntegrationDate")
    if (zdbIntegrationDate) {
      result.zdbIntegrationDate = zdbIntegrationDate
    }
    String zdbIntegrationUrl = JsonToolkit.fromJson(json, "zdbIntegrationUrl")
    if (zdbIntegrationUrl) {
      result.zdbIntegrationUrl = zdbIntegrationUrl
    }
    String publicationType = JsonToolkit.fromJson(json, "publicationType")
    if (publicationType) {
      result.publicationType = publicationType
    }
    result.duplicates = [:]
    Collection duplicates = JsonToolkit.fromJson(json, "duplicates")
    if (duplicates != null){
      for (def dup in duplicates){
        result.duplicates.put(AbstractIdentifier.fromString(dup.key), dup.value)
      }
    }
    result
  }

}
