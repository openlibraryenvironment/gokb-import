package ygor.field

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.readers.ZdbReader
import org.apache.commons.lang.StringUtils
import ygor.identifier.AbstractIdentifier
import ygor.identifier.DoiIdentifier
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier


class GokbTitleReference{
  ZdbIdentifier zdbId
  DoiIdentifier doiIdentifier
  EissnIdentifier onlineIdentifier
  PissnIdentifier printIdentifier
  String publicationTitle

  static ZDB_READER = new ZdbReader()
  static MAPPINGS = new MappingsContainer()
  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

  static constraints = {}


  GokbTitleReference(String identifier, String type){
    String queryString = ZdbReader.getAPIQuery(identifier, type)
    List<Map<String, List<String>>> referencedTitles = ZDB_READER.readItemData(queryString)
    // for sake of simplicity, use only unambiguous references for now
    if (referencedTitles.size() == 1){
      Map<String, List<String>> referencedTitle = referencedTitles.getAt(0)
      // ZDB ID from mappings
      FieldKeyMapping zdbMapping = MAPPINGS.ygorMappings.get("zdbId")
      zdbId = new ZdbIdentifier(referencedTitle.get(zdbMapping.zdbKeys?.get(0))?.get(0), zdbMapping)
      // DOI from mappings
      FieldKeyMapping doiMapping = MAPPINGS.ygorMappings.get("doi")
      doiIdentifier = new DoiIdentifier(referencedTitle.get(doiMapping.zdbKeys?.get(0))?.get(0), doiMapping)
      // eISSN from mappings
      FieldKeyMapping eissnMapping = MAPPINGS.ygorMappings.get("onlineIdentifier")
      onlineIdentifier = new EissnIdentifier(referencedTitle.get(eissnMapping.zdbKeys?.get(0))?.get(0), eissnMapping)
      // pISSN from mappings
      FieldKeyMapping pissnMapping = MAPPINGS.ygorMappings.get("printIdentifier")
      printIdentifier = new PissnIdentifier(referencedTitle.get(pissnMapping.zdbKeys?.get(0))?.get(0), pissnMapping)
      // publicationTitle from mappings
      publicationTitle = referencedTitle.get(MAPPINGS.ygorMappings.get("publicationTitle").zdbKeys.get(0))?.get(0)
    }
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
      newIdentifierIntoArrayNode(identifiers, "pissn", printIdentifier?.identifier)
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