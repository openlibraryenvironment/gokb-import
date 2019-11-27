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
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.*

class Record{

  static ObjectMapper MAPPER = new ObjectMapper()
  static {
    MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
  }

  String uid
  ZdbIdentifier zdbId
  EzbIdentifier ezbId
  DoiIdentifier doiId
  EissnIdentifier onlineIdentifier
  PissnIdentifier printIdentifier
  Map multiFields
  Map validation
  String zdbIntegrationDate
  String ezbIntegrationDate
  String zdbIntegrationUrl
  String ezbIntegrationUrl


  static hasMany = [multiFields       : MultiField,
                    validation        : String,
                    validationMessages: String]

  static constraints = {
  }

  Record(List<AbstractIdentifier> ids, MappingsContainer container) {
    this(ids, container, null)
  }

  Record(List<AbstractIdentifier> ids, MappingsContainer container, String uid) {
    if (null == uid) {
      this.uid = UUID.randomUUID().toString()
    }
    else {
      this.uid = uid
    }
    for (id in ids) {
      addIdentifier(id)
    }
    multiFields = [:]
    validation = [:]
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
      if (zdbId && identifier.identifier != zdbId.identifier) {
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
    else if (identifier instanceof EissnIdentifier) {
      if (onlineIdentifier && identifier.identifier != onlineIdentifier.identifier) {
        throw new IllegalArgumentException("EISSN ${dentifier} already set to ${onlineIdentifier} for record")
      }
      onlineIdentifier = identifier
    }
    else if (identifier instanceof PissnIdentifier) {
      if (printIdentifier && identifier.identifier != printIdentifier.identifier) {
        throw new IllegalArgumentException("PISSN ${identifier} already set to ${printIdentifier} for record")
      }
      printIdentifier = identifier
    }
  }


  List<MultiField> getIdentifierFields() {
    List<MultiField> result = []
    if (multiFields.get("ezbId")) result.add(multiFields.get("ezbId"))
    if (multiFields.get("onlineIdentifier")) result.add(multiFields.get("onlineIdentifier"))
    if (multiFields.get("parentPublicationTitleId")) result.add(multiFields.get("parentPublicationTitleId"))
    if (multiFields.get("precedingPublicationTitleId")) result.add(multiFields.get("precedingPublicationTitleId"))
    if (multiFields.get("printIdentifier")) result.add(multiFields.get("printIdentifier"))
    if (multiFields.get("titleId")) result.add(multiFields.get("titleId"))
    if (multiFields.get("zdbId")) result.add(multiFields.get("zdbId"))
    result
  }


  void normalize(String namespace) {
    EditionNormalizer.normalizeEditionNumber(this)
    for (MultiField multiField in multiFields.values()) {
      multiField.normalize(namespace)
    }
  }


  boolean isValid(String type) {
    // check ZDB api match for non-ebooks
    if (type.toLowerCase() != "ebooks") {
      if (!zdbIntegrationDate) {
        // has not been matched in ZDB API
        return false
      }
    }
    // validate tipp.titleUrl
    MultiField urlMultiField = multiFields.get("titleUrl")
    if (urlMultiField == null) {
      return false
    }
    if (urlMultiField.status != Status.VALIDATOR_URL_IS_VALID.toString()) {
      return false
    }
    // check multifields for critical errors
    for (MultiField multiField in multiFields.values()){
      if (multiField.isCriticallyInvalid()){
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

  Status getValidation(String property) {
    return validation.get(property)
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


  private void validateMultifields(String namespace) {
    multiFields.each { k, v -> v.validate(namespace) }
  }

  def getCoverage() {
    false // TODO
  }

  String asJson(JsonGenerator jsonGenerator) {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("uid", uid)
    jsonGenerator.writeStringField("zdbId", zdbId?.identifier)
    jsonGenerator.writeStringField("ezbId", ezbId?.identifier)
    jsonGenerator.writeStringField("doiId", doiId?.identifier)
    jsonGenerator.writeStringField("eissn", onlineIdentifier?.identifier)
    jsonGenerator.writeStringField("pissn", printIdentifier?.identifier)
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
    result
  }


  static Record fromJson(JsonNode json, MappingsContainer mappings) {
    List<AbstractIdentifier> ids = new ArrayList<>()
    ids.add(new ZdbIdentifier(JsonToolkit.fromJson(json, "zdbId"), mappings.getMapping("zdbId", MappingsContainer.YGOR)))
    ids.add(new EzbIdentifier(JsonToolkit.fromJson(json, "ezbId"), mappings.getMapping("ezbId", MappingsContainer.YGOR)))
    ids.add(new DoiIdentifier(JsonToolkit.fromJson(json, "doiId"), mappings.getMapping("doiId", MappingsContainer.YGOR)))
    ids.add(new EissnIdentifier(JsonToolkit.fromJson(json, "eissn"), mappings.getMapping("onlineIdentifier", MappingsContainer.YGOR)))
    ids.add(new PissnIdentifier(JsonToolkit.fromJson(json, "pissn"), mappings.getMapping("printIdentifier", MappingsContainer.YGOR)))
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
    result
  }

}
