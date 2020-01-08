package ygor.field

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.commons.lang.StringUtils
import ygor.Enrichment
import ygor.Record
import ygor.identifier.AbstractIdentifier
import ygor.identifier.DoiIdentifier
import ygor.identifier.EissnIdentifier
import ygor.identifier.EzbIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier


class GokbTitleReference{
  ZdbIdentifier zdbId
  EzbIdentifier ezbId
  DoiIdentifier doiIdentifier
  EissnIdentifier onlineIdentifier
  PissnIdentifier printIdentifier
  String publicationTitle

  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

  static constraints = {}


  GokbTitleReference(String identifier, Enrichment enrichment){
    Record referencedRecord = enrichment.dataContainer.getRecord(new ZdbIdentifier(identifier,
        enrichment.mappingsContainer.getMapping("zdbId", MappingsContainer.YGOR)))
    zdbId = referencedRecord.zdbId
    ezbId = referencedRecord.ezbId
    doiIdentifier = referencedRecord.doiId
    onlineIdentifier = referencedRecord.onlineIdentifier
    printIdentifier = referencedRecord.printIdentifier
    publicationTitle = referencedRecord.multiFields.get("publicationTitle").getFirstPrioValue()
  }


  boolean isValid(){
    // publicationTitle must be set
    if (StringUtils.isEmpty(publicationTitle)){
      return false
    }
    // one identifier must be set
    for (AbstractIdentifier i in [zdbId, onlineIdentifier, printIdentifier]){
      if (!StringUtils.isEmpty(i?.identifier)){
        return true
      }
    }
    // else
    return false
  }


  ObjectNode toJson() {
    if (isValid()){
      ObjectNode result = NODE_FACTORY.objectNode()
      result.put("title", publicationTitle)
      ArrayNode identifiers = NODE_FACTORY.arrayNode()
      newIdentifierIntoArrayNode(identifiers, "zdb", zdbId?.identifier)
      newIdentifierIntoArrayNode(identifiers, "eissn", onlineIdentifier?.identifier)
      newIdentifierIntoArrayNode(identifiers, "issn", printIdentifier?.identifier)
      result.set("identifiers", identifiers)
    }
  }


  void newIdentifierIntoArrayNode(ArrayNode identifiers, String type, String value){
    if (value != null){
      ObjectNode identifier = NODE_FACTORY.objectNode()
      identifier.put("type", type)
      identifier.put("value", value)
      identifiers.add(identifier)
    }
    return
  }


  static HistoryEvent fromJson(JsonNode json, FieldKeyMapping mapping) {
    // TODO ? (Alternatively always re-calculate from Record)
  }
}
