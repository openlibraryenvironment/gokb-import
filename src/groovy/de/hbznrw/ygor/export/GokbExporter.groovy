package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.hbznrw.ygor.format.GokbFormatter
import de.hbznrw.ygor.normalizers.DoiNormalizer
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.tools.StopwordToolkit
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import ygor.Enrichment
import ygor.Enrichment.FileType
import ygor.Record
import ygor.field.MappingsContainer

@Log4j
class GokbExporter {

  static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper()
  static GokbFormatter FORMATTER = new GokbFormatter()
  static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance

  static File getFile(Enrichment enrichment, FileType type, boolean validate) {
    if (validate){
      enrichment.validateContainer()
    }
    switch (type) {
      case FileType.ORIGIN:
        return new File(enrichment.originPathName)
      case FileType.JSON_PACKAGE_ONLY:
        ObjectNode result = GokbExporter.extractPackage(enrichment)
        def file = new File(enrichment.resultPathName + ".package.json")
        file.write(JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result), "UTF-8")
        return file
      case FileType.JSON_TITLES_ONLY:
        ArrayNode result = GokbExporter.extractTitles(enrichment)
        def file = new File(enrichment.resultPathName + ".titles.json")
        file.write(JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result), "UTF-8")
        return file
      case FileType.JSON_OO_RAW:
        enrichment.saveResult()
        return new File(enrichment.resultPathName)
    }
    return null
  }


  static ObjectNode extractPackage(Enrichment enrichment) {
    ObjectNode pkg = new ObjectNode(NODE_FACTORY)
    log.debug("extracting package ...")
    pkg.set("packageHeader", extractPackageHeader(enrichment))
    pkg.set("tipps", extractTipps(enrichment))
    log.debug("extracting package finished")
    pkg
  }


  static ArrayNode extractTitles(Enrichment enrichment) {
    log.debug("extracting titles ...")
    ArrayNode titles = new ArrayNode(NODE_FACTORY)
    for (Record record in enrichment.dataContainer.records.values()) {
      if (record.isValid()){
        record.deriveHistoryEventObjects(enrichment)
        ObjectNode title = JsonToolkit.getTitleJsonFromRecord("gokb", record, FORMATTER)
        title = postProcessPublicationTitle(title, record)
        titles.add(title)
      }
    }
    titles = removeEmptyFields(titles)
    titles = removeEmptyIdentifiers(titles, FileType.JSON_TITLES_ONLY)
    titles = postProcessTitleIdentifiers(titles, FileType.JSON_TITLES_ONLY,
        enrichment.dataContainer.info.namespace_title_id)
    enrichment.dataContainer.titles = titles
    log.debug("extracting titles finished")
    titles
  }


  static ArrayNode extractTipps(Enrichment enrichment) {
    log.debug("extracting tipps ...")
    ArrayNode tipps = new ArrayNode(NODE_FACTORY)
    for (Record record in enrichment.dataContainer.records.values()) {
      if (record.isValid()){
        tipps.add(JsonToolkit.getTippJsonFromRecord("gokb", record, FORMATTER))
      }
    }
    tipps = removeEmptyFields(tipps)
    tipps = removeEmptyIdentifiers(tipps, FileType.JSON_PACKAGE_ONLY)
    tipps = postProcessTitleIdentifiers(tipps, FileType.JSON_PACKAGE_ONLY,
        enrichment.dataContainer.info.namespace_title_id)
    enrichment.dataContainer.tipps = tipps
    log.debug("extracting tipps finished")
    tipps
  }


  static ArrayNode postProcessTitleIdentifiers(ArrayNode arrayNode, FileType type, String namespace) {
    log.debug("postprocessing title ids ...")
    if (type.equals(FileType.JSON_TITLES_ONLY)) {
      for (def title in arrayNode.elements()) {
        postProcessTitleId(title.identifiers, namespace)
      }
    } else if (type.equals(FileType.JSON_PACKAGE_ONLY)) {
      for (def tipp in arrayNode.elements()) {
        postProcessTitleId(tipp.title.identifiers, namespace)
      }
    }
    log.debug("postprocessing title ids finished")
    arrayNode
  }


  static ArrayNode removeEmptyIdentifiers(ArrayNode arrayNode, FileType type) {
    log.debug("removing invalid fields ...")
    if (type.equals(FileType.JSON_TITLES_ONLY)) {
      for (def title in arrayNode.elements()) {
        removeEmptyIds(title.identifiers)
      }
    } else if (type.equals(FileType.JSON_PACKAGE_ONLY)) {
      for (def tipp in arrayNode.elements()) {
        removeEmptyIds(tipp.title.identifiers)
      }
    }
    log.debug("removing invalid fields finished")
    arrayNode
  }


  static ObjectNode extractPackageHeader(Enrichment enrichment) {
    // this currently parses the old package header
    // TODO: refactor
    log.debug("parsing package header ...")
    def packageHeader = enrichment.dataContainer.pkg.packageHeader
    def result = new ObjectNode(NODE_FACTORY)

    for (String field in ["breakable", "consistent", "fixed", "global",
                          "listStatus", "nominalProvider", "paymentType", "scope", "userListVerifier"]) {
      result.put("${field}", (String) packageHeader."${field}")
    }
    setIsil(packageHeader, result)
    if (enrichment.packageName){
      result.put("name", enrichment.packageName)
    }
    else {
      result.put("name", packageHeader.name)
    }

    def nominalPlatform = new ObjectNode(NODE_FACTORY)
    nominalPlatform.put("name", (String) packageHeader.nominalPlatform.name)
    nominalPlatform.put("primaryUrl", (String) packageHeader.nominalPlatform.url)
    result.set("nominalPlatform", nominalPlatform)

    if (null != enrichment.dataContainer.curatoryGroup){
      ArrayNode curatoryGroups = NODE_FACTORY.arrayNode()
      curatoryGroups.add(enrichment.dataContainer.curatoryGroup)
      result.set("curatoryGroups", (curatoryGroups))
    }
    if (!StringUtils.isEmpty(enrichment.dataContainer.pkgId) && !StringUtils.isEmpty(enrichment.dataContainer.pkgIdNamespace)){
      ArrayNode identifiers = NODE_FACTORY.arrayNode()
      ObjectNode identifier = NODE_FACTORY.objectNode()
      identifier.put("type", enrichment.dataContainer.pkgIdNamespace)
      identifier.put("value", enrichment.dataContainer.pkgId)
      identifiers.add(identifier)
      result.set("identifiers", identifiers)
    }
    result.set("additionalProperties", getArrayNode(packageHeader, "additionalProperties"))

    def source = new ObjectNode(NODE_FACTORY)
    if (packageHeader.source?.name != null && !StringUtils.isEmpty(packageHeader.source.name))
      source.put("name", packageHeader.source.name)
    if (packageHeader.source?.normname != null && !StringUtils.isEmpty(packageHeader.source.normname))
      source.put("normname", packageHeader.source.normname)
    if (packageHeader.source?.url != null && !StringUtils.isEmpty(packageHeader.source.url))
      source.put("url", packageHeader.source.url)
    result.set("source", source)

    enrichment.dataContainer.packageHeader = result
    log.debug("parsing package header finished")
    result
  }


  private static ObjectNode postProcessPublicationTitle(ObjectNode titleNode, Record record){
    String title = titleNode.get("name").asText()
    List<String> ramifications = record.multiFields.get("publicationTitleRamification").getFieldValuesBySource(MappingsContainer.ZDB)
    if (ramifications != null && !ramifications.isEmpty()){
      String extendedTitle = title
      for (String ramification in ramifications){
        if (!StringUtils.isEmpty(ramification)){
          extendedTitle = extendedTitle.concat(" / ").concat(ramification)
        }
      }
      titleNode.set("name", new TextNode(extendedTitle))
    }
    else{
      for (String extendedTitleFieldName in ["publicationSubTitle", "publicationTitleVariation"]){
        String extendedTitle = record.multiFields.get(extendedTitleFieldName).getFirstPrioValue()
        if (!StringUtils.isEmpty(extendedTitle)){
          if (isRoughlySubString(title, extendedTitle)){
            titleNode.set("name", new TextNode(extendedTitle))
          }
          else{
            titleNode.set("name", new TextNode(title.concat(": ").concat(extendedTitle)))
          }
          return titleNode
        }
      }
    }
    return titleNode
  }


  private static boolean isRoughlySubString(String subStringCandidate, String longerString){
    List<String> subStringStems = getStems(subStringCandidate)
    List<String> longerStringStems = getStems(longerString)
    if (subStringStems.size() >= longerStringStems.size()){
      return false
    }
    for (String subStem in subStringStems){
      if (!(subStem in longerStringStems)){
        return false
      }
    }
    return true
  }


  /**
   * This helper method is called get"Stems" as syntactical stemming intended (but not implemented for now).
   */
  static private List<String> getStems(String title){
    List<String> result = []
    String[] splitTitle = title.split(" ")
    for (String split in splitTitle){
      split = split.toLowerCase()
      if (!StopwordToolkit.isStopword(split) && isWord(split)){
        result.add(split.replaceAll("[^a-zà]", ""))
      }
    }
    return result
  }


  private static boolean isWord(String string){
    return string.matches(".*[a-zà].*")
  }


  private static void setIsil(packageHeader, ObjectNode result) {
    String isil = packageHeader.isil
    if (!StringUtils.isEmpty(isil)) {
      def isilNode = new ObjectNode(NODE_FACTORY)
      isilNode.put("type", "isil")
      isilNode.put("value", isil)
      def idsArray = new ArrayNode(NODE_FACTORY)
      idsArray.add(isilNode)
      result.set("identifiers", idsArray)
    }
  }


  static private ArrayNode getArrayNode(def source, def sourceField) {
    ArrayNode result = new ArrayNode(NODE_FACTORY)
    for (def item in source."${sourceField}") {
      result.add(item.v)
    }
    result
  }


  static private void removeEmptyIds(ArrayNode identifiers) {
    def count = 0
    def idsToBeRemoved = []
    for (ObjectNode idNode in identifiers.elements()) {
      if (idNode.elements().size() == 1 && idNode.get("type") != null) {
        // identifier has "type" only ==> remove
        idsToBeRemoved << count
      } else if (idNode.elements().size() > 1 && idNode.get("value").asText().trim() == "\"\"") {
        idsToBeRemoved << count
      }
      count++
    }
    for (int i = idsToBeRemoved.size() - 1; i > -1; i--) {
      identifiers.remove(idsToBeRemoved[i])
    }
  }


  // adapted from: https://technicaldifficulties.io/2018/04/26/using-jackson-to-remove-empty-json-fields/ -thx Stacie!
  static ObjectNode removeEmptyFields(final ObjectNode jsonNode) {
    ObjectNode result = new ObjectMapper().createObjectNode()
    for (def entry in jsonNode.fields()) {
      String key = entry.getKey()
      JsonNode value = entry.getValue()
      if (value instanceof ObjectNode) {
        JsonNode subNode = removeEmptyFields((ObjectNode) value)
        if (subNode.size() > 0) {
          Map<String, ObjectNode> map = new HashMap<String, ObjectNode>()
          map.put(key, subNode)
          result.setAll(map)
        }
      } else if (value instanceof ArrayNode) {
        JsonNode subNode = removeEmptyFields((ArrayNode) value)
        if (subNode.size() > 0) {
          result.set(key, subNode)
        }
      } else if (value.asText() != null) {
        if (value.asText().equals(" ")) {
          result.put(key, "")
        } else if (!value.asText().isEmpty()) {
          result.set(key, value)
        }
      }
    }
    return result
  }


  static private void postProcessTitleId(ArrayNode identifiers, String namespace) {
    int count = 0
    ObjectNode titleIdNode = null
    for (ObjectNode idNode in identifiers) {
      if (idNode.get("type").asText().equals("titleId")) {
        titleIdNode = idNode
        break
      }
      else {
        count++
      }
    }
    if (titleIdNode == null) {
      // There is no titleId node --> nothing to do
      return
    }
    // remove title id node if value is a copy another identifier node value
    for (int i = 0; i < identifiers.size(); i++) {
      if (i != count &&
          identifiers.get(i).get("value").asText().equals(titleIdNode.get("value").asText())) {
        identifiers.remove(count)
        return
      }
    }
    if (StringUtils.isEmpty(namespace)) {
      // namespace has not been selected -->
      // just remove titleId node from identifiers
      identifiers.remove(count)
      return
    }
    // set identifier type
    titleIdNode.remove("type")
    titleIdNode.set("type", new TextNode(namespace))
    // normalize value
    if (titleIdNode.get("type").asText().equals("doi")){
      TextNode oldValue = titleIdNode.remove("value")
      titleIdNode.set("value", new TextNode(DoiNormalizer.normalizeDoi(oldValue.asText())))
    }
  }


  static ArrayNode removeEmptyFields(ArrayNode array) {
    ArrayNode result = new ObjectMapper().createArrayNode()
    for (JsonNode value in array.elements()) {
      if (value instanceof ArrayNode) {
        JsonNode subNode = removeEmptyFields((ArrayNode) (value))
        if (subNode.size() > 0) {
          result.add(subNode)
        }
      } else if (value instanceof ObjectNode) {
        JsonNode subNode = removeEmptyFields((ObjectNode) (value))
        if (subNode.size() > 0) {
          result.add(removeEmptyFields((ObjectNode) (value)))
        }
      } else if (value.asText() != null) {
        if (value.asText().equals(" ")) {
          result.add("")
        } else if (!value.asText().isEmpty()) {
          result.add(value)
        }
      }
    }
    return result
  }

}
