package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import de.hbznrw.ygor.format.GokbFormatter
import de.hbznrw.ygor.normalizers.DoiNormalizer
import de.hbznrw.ygor.processing.YgorFeedback
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.tools.StopwordToolkit
import groovy.util.logging.Log4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.commons.lang.StringUtils
import ygor.Enrichment
import ygor.Enrichment.FileType
import ygor.Record
import ygor.field.MappingsContainer

import javax.annotation.Nonnull
import java.nio.ByteBuffer
import java.nio.channels.FileLock
import java.nio.channels.OverlappingFileLockException
import java.nio.charset.Charset

// TODO: Refactor. Postprocessing methods should only be used once within an export, not twice as some are used for
//       Tipps and for Titles by now.

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
      case FileType.PACKAGE:
        ObjectNode result = GokbExporter.extractPackage(enrichment, type)
        def file = new File(enrichment.enrichmentFolder + ".package.json")
        if (enrichment.dataContainer.records.size() > 1000){
          file.write(JSON_OBJECT_MAPPER.writeValueAsString(result), "UTF-8")
        }
        else{
          file.write(JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result), "UTF-8")
        }
        return file
      case FileType.PACKAGE_WITH_TITLEDATA:
        ObjectNode result = GokbExporter.extractPackage(enrichment, type)
        def file = new File(enrichment.enrichmentFolder + ".packageWithTitleData.json")
        if (enrichment.dataContainer.records.size() > 1000){
          file.write(JSON_OBJECT_MAPPER.writeValueAsString(result), "UTF-8")
        }
        else{
          file.write(JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result), "UTF-8")
        }
        return file
      case FileType.TITLES:
        return extractTitles(enrichment)
      case FileType.RAW:
        enrichment.save()
        return new File(enrichment.enrichmentFolder)
    }
    return null
  }


  static ObjectNode extractPackage(Enrichment enrichment, FileType type) {
    ObjectNode pkg = new ObjectNode(NODE_FACTORY)
    log.debug("extracting package ...")
    pkg.set("packageHeader", extractPackageHeader(enrichment))
    pkg.set("tipps", extractTipps(enrichment, type))
    if (enrichment.dataContainer?.pkgHeader?.token != null){
      pkg.put("updateToken", enrichment.dataContainer?.pkgHeader?.token)
    }

    if (enrichment.dataContainer?.pkgHeader?.updateURL != null){
      pkg.put("updateURL", enrichment.dataContainer?.pkgHeader?.updateURL)
    }
    log.debug("extracting package finished")
    pkg
  }


  static ArrayNode extractTipps(Enrichment enrichment, FileType type) {
    log.debug("extracting tipps from ${enrichment.dataContainer.records.size()} records ...")
    ArrayNode tipps = new ArrayNode(NODE_FACTORY)
    Character multiValueSeparator = getMultiValueSeparator(enrichment)
    for (String recId in enrichment.dataContainer.records) {
      Record record = Record.load(enrichment.dataContainer.enrichmentFolder, enrichment.resultHash, recId,
          enrichment.dataContainer.mappingsContainer)
      if (record.isValid()){
        ObjectNode tipp
        if (type.equals(FileType.PACKAGE_WITH_TITLEDATA)){
          tipp = JsonToolkit.getCombinedTitleTippJsonFromRecord(MappingsContainer.KB, record, FORMATTER, multiValueSeparator)
        }
        else{
          tipp = JsonToolkit.getTippJsonFromRecord(MappingsContainer.KB, record, FORMATTER, multiValueSeparator)
        }
        tipp = postProcessIssnIsbn(tipp, record, FileType.PACKAGE)
        if (type.equals(FileType.PACKAGE_WITH_TITLEDATA)){
          // additionally rename title identifiers
          postProcessIssnIsbn(tipp, record, FileType.TITLES)
        }
        tipp = removeEmptyFields(tipp)
        tipp = removeEmptyIdentifiers(tipp, type)
        tipp.set("title", postProcessPublicationTitle(tipp.get("title"), record))
        tipp = postProcessTitleIdentifiers(tipp, type, enrichment.dataContainer.info.namespace_title_id)
        tipps.add(tipp)
      }
    }
    enrichment.dataContainer.tipps = tipps
    log.debug("extracting tipps finished")
    tipps
  }


  private static Character getMultiValueSeparator(Enrichment enrichment){
    if (enrichment.kbartReader.delimiterChar == ','){
      return ';'
    }
    else{
      return ','
    }
  }


  static File extractTitles(Enrichment enrichment) {
    String fileName = enrichment.enrichmentFolder + ".titles.json"
    log.debug("extracting titles ... to ".concat(fileName))
    RandomAccessFile titlesFile = new RandomAccessFile(fileName, "rw")
    def fileChannel = titlesFile.getChannel()
    Character multiValueSeparator = getMultiValueSeparator(enrichment)
    try {
      FileLock fileLock = fileChannel.tryLock()
      if (null != fileLock){
        for (int i=0; i<enrichment.dataContainer.records.size(); i++){
          String recId = enrichment.dataContainer.records[i]
          def titleRecord = extractTitle(enrichment, recId, true, multiValueSeparator)
          byte[] title
          StringWriter strw = new StringWriter()
          if (i == 0) {
            strw.write("[")
          }
          if (titleRecord) {
            if (i != 0){
              strw.write(",")
            }
            strw.write(titleRecord)
          }
          if (i == enrichment.dataContainer.records.size()-1) {
            strw.write("]")
          }
          title = strw.toString().getBytes(Charset.forName("UTF-8"))
          ByteBuffer buffer = ByteBuffer.wrap(title)
          buffer.put(title)
          buffer.flip()
          while (buffer.hasRemaining()){
            fileChannel.write(buffer)
          }
        }
      }
      fileLock.close()
    }
    catch (OverlappingFileLockException | IOException e) {
      log.error("Exception occurred while trying lock titles file... " + e.getMessage())
    }
    return new File(fileName)
  }


  static String extractTitle(Enrichment enrichment, String recordId, boolean printPretty, char multiValueSeparator) {
    Record record = Record.load(enrichment.dataContainer.enrichmentFolder, enrichment.resultHash, recordId,
        enrichment.dataContainer.mappingsContainer)
    if (record != null && record.isValid()){
      record.deriveHistoryEventObjects(enrichment)
      ObjectNode title = JsonToolkit.getTitleJsonFromRecord(MappingsContainer.KB, record, FORMATTER, multiValueSeparator)
      title = postProcessPublicationTitle(title, record)
      title = postProcessIssnIsbn(title, record, FileType.TITLES)
      title = removeEmptyFields(title)
      title = removeEmptyIdentifiers(title, FileType.TITLES)
      title = removeEmptyPrices(title)
      title = postProcessTitleIdentifiers(title, FileType.TITLES, enrichment.dataContainer.info.namespace_title_id)
      if (printPretty){
        return JSON_OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(title)
      }
      // else
      return JSON_OBJECT_MAPPER.writeValueAsString(title)
    }
    // else
    return null
  }


  static ObjectNode postProcessTitleIdentifiers(ObjectNode item, FileType type, String namespace) {
    if (type.equals(FileType.TITLES)) {
      postProcessTitleId(item.identifiers, namespace)
    }
    else if (type.equals(FileType.PACKAGE)) {
      postProcessTitleId(item.title.identifiers, namespace)
    }
    else if (type.equals(FileType.PACKAGE_WITH_TITLEDATA)) {
      postProcessTitleId(item.identifiers, namespace)
      postProcessTitleId(item.title.identifiers, namespace)
    }
    item
  }


  static ObjectNode removeEmptyIdentifiers(ObjectNode item, FileType type) {
    if (type.equals(FileType.TITLES)) {
      removeEmptyIds(item.identifiers)
    }
    else if (type.equals(FileType.PACKAGE)) {
      removeEmptyIds(item.title.identifiers)
    }
    else if (type.equals(FileType.PACKAGE_WITH_TITLEDATA)) {
      removeEmptyIds(item.identifiers)
      removeEmptyIds(item.title.identifiers)
    }
    item
  }


  static ObjectNode extractPackageHeader(Enrichment enrichment) {
    // this currently parses the old package header
    // TODO: refactor
    log.debug("parsing package header ...")
    def packageHeader = enrichment.dataContainer.pkgHeader
    def result = new ObjectNode(NODE_FACTORY)
    def identifiers = new ArrayNode(NODE_FACTORY)

    setIsil(enrichment.dataContainer, identifiers)
    if (enrichment.packageName){
      result.put("name", enrichment.packageName)
    }
    else {
      result.put("name", packageHeader.name)
    }
    if (enrichment.packageId){
      result.put("gokbId", enrichment.packageId)
    }
    if (enrichment.packageUuid){
      result.put("uuid", enrichment.packageUuid)
    }
    if (enrichment.fileNameDate){
      result.put("fileNameDate", enrichment.fileNameDate)
    }
    def nominalPlatform = new ObjectNode(NODE_FACTORY)
    nominalPlatform.put("name", (String) packageHeader.nominalPlatform.name)
    nominalPlatform.put("primaryUrl", (String) packageHeader.nominalPlatform.url)
    nominalPlatform.put("oid", (String) packageHeader.nominalPlatform.oid)
    result.set("nominalPlatform", nominalPlatform)

    def nominalProvider = new ObjectNode(NODE_FACTORY)
    nominalProvider.put("name", (String) packageHeader.nominalProvider.name)
    nominalProvider.put("oid", (String) packageHeader.nominalProvider.oid)
    result.set("nominalProvider", nominalProvider)

    if (null != enrichment.dataContainer.curatoryGroup){
      ArrayNode curatoryGroups = NODE_FACTORY.arrayNode()
      curatoryGroups.add(enrichment.dataContainer.curatoryGroup)
      result.set("curatoryGroups", (curatoryGroups))
    }
    setPkgId(enrichment.dataContainer, identifiers)
    if (enrichment.autoUpdate == true){
      result.set("generateToken", new TextNode("true"))
    }
    result.set("identifiers", identifiers)

    enrichment.dataContainer.packageHeader = result
    log.debug("parsing package header finished")
    result
  }


  private static ObjectNode postProcessPublicationTitle(ObjectNode titleNode, Record record){
    String title = titleNode.get("name")?.asText()
    if (title == null){
      title = ""
    }
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
    if (StringUtils.isEmpty(title)){
      title = record.multiFields.get("publicationTitleKbart").getFirstPrioValue()
      titleNode.set("name", new TextNode(title))
    }
    return titleNode
  }

  private static ObjectNode postProcessIssnIsbn(ObjectNode node, Record record, FileType type){
    ObjectNode onlineIdentifier
    ObjectNode printIdentifier
    if (type.equals(FileType.TITLES)){
      onlineIdentifier = getIdentifierNodeByType(node.get("identifiers"), "onlineIdentifier")
      printIdentifier = getIdentifierNodeByType(node.get("identifiers"), "printIdentifier")
    }
    else if (type.equals(FileType.PACKAGE)){
      onlineIdentifier = getIdentifierNodeByType(node.get("title").get("identifiers"), "onlineIdentifier")
      printIdentifier = getIdentifierNodeByType(node.get("title").get("identifiers"), "printIdentifier")
    }
    else{
      return node
    }
    if (record.publicationType.equals("serial")){
      if (onlineIdentifier != null){
        onlineIdentifier.remove("type")
        onlineIdentifier.set("type", new TextNode("eissn"))
      }
      if (printIdentifier != null){
        printIdentifier.remove("type")
        printIdentifier.set("type", new TextNode("issn"))
      }
    }
    else if (record.publicationType.equals("monograph")){
      if (onlineIdentifier != null){
        onlineIdentifier.remove("type")
        onlineIdentifier.set("type", new TextNode("isbn"))
      }
      if (printIdentifier != null){
        printIdentifier.remove("type")
        printIdentifier.set("type", new TextNode("pisbn"))
      }
    }
    return node
  }


  private static ObjectNode getIdentifierNodeByType(ArrayNode identifiers, String type){
    for (ObjectNode identifier in identifiers){
      if (identifier.get("type").asText().equals(type)){
        return identifier
      }
    }
    return null
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


  private static void setPkgId(DataContainer dataContainer, ArrayNode identifiers){
    if (!StringUtils.isEmpty(dataContainer.pkgId) && !StringUtils.isEmpty(dataContainer.pkgIdNamespace)){
      ObjectNode identifier = NODE_FACTORY.objectNode()
      identifier.put("type", dataContainer.pkgIdNamespace)
      identifier.put("value", dataContainer.pkgId)
      identifiers.add(identifier)
    }
  }


  private static void setIsil(DataContainer dc, ArrayNode identifiers) {
    if (!StringUtils.isEmpty(dc.isil)) {
      def isilNode = new ObjectNode(NODE_FACTORY)
      isilNode.put("type", "isil")
      isilNode.put("value", dc.isil)
      identifiers.add(isilNode)
    }
  }


  static private void removeEmptyIds(ArrayNode identifiers) {
    def count = 0
    def idsToBeRemoved = []
    for (ObjectNode idNode in identifiers.elements()) {
      if (idNode.elements().size() == 1 && idNode.get("type") != null) {
        // identifier has "type" only ==> remove
        idsToBeRemoved << count
      }
      else if (idNode.elements().size() > 1 && idNode.get("value").asText().trim() == "\"\"") {
        idsToBeRemoved << count
      }
      count++
    }
    for (int i = idsToBeRemoved.size() - 1; i > -1; i--) {
      identifiers.remove(idsToBeRemoved[i])
    }
  }


  static ObjectNode removeEmptyPrices(ObjectNode item) {
    def count = 0
    def pricesToBeRemoved = []
    if (item.get("prices") != null){
      for (ObjectNode priceNode in item.get("prices").elements()) {
        if (priceNode.get("amount") == null || priceNode.get("amount").asText().trim() == "\"\"") {
          pricesToBeRemoved << count
        }
        else if (priceNode.get("currency") == null || priceNode.get("currency").asText().trim() == "\"\"") {
          pricesToBeRemoved << count
        }
        else if (priceNode.get("type") == null || priceNode.get("type").asText().trim() == "\"\"") {
          pricesToBeRemoved << count
        }
        count++
      }
    }
    for (int i = pricesToBeRemoved.size() - 1; i > -1; i--) {
      item.get("prices")?.remove(pricesToBeRemoved[i])
    }
    item
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
      count++
    }
    if (titleIdNode == null) {
      // There is no titleId node --> nothing to do
      return
    }
    if (StringUtils.isEmpty(namespace)) {
      // namespace has not been selected --> remove titleId node from identifiers
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


  static Map sendText(@Nonnull String url, @Nonnull String text, @Nonnull String user, @Nonnull String password,
                      @Nonnull String locale, YgorFeedback ygorFeedback){
    def http = new HTTPBuilder(url)
    ygorFeedback.statusDescription += " Sending text to ${url} with user ${user}."
    if (user != null && password != null){
      http.auth.basic user, password
    }
    log.info("... sending text ${text.substring(0, text.length() > 50 ? 50 : text.length())}")
    http.request(Method.POST, ContentType.JSON){ request ->
      headers.'User-Agent' = 'ygor'
      headers.'Accept-Language' = locale
      body = text
      response.success = { response, html ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.OK
          ygorFeedback.statusDescription += " Text was successfully sent."
          if (response.status < 400){
            return ['info': html]
          }
          else{
            return ['warning': html]
          }
        }
        else{
          ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.ERROR
          ygorFeedback.statusDescription += " Authentication error!"
          return ['error': ['message': "Authentication error!", 'result': "ERROR"]]
        }
      }
      response.failure = { response, html ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.ERROR
          ygorFeedback.statusDescription += " Unspecified error!"
          return ['error': html]
        }
        else{
          return ['error': ['message': "Authentication error!", 'result': "ERROR"]]
        }
      }
      response.'401' = { response ->
        ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.ERROR
        ygorFeedback.statusDescription += " Ygor received 401 response!"
        return ['error': ['message': "Authentication error!", 'result': "ERROR"]]
      }
    }
  }


  static Map sendUpdate(@Nonnull String url, @Nonnull String text, @Nonnull String locale, YgorFeedback ygorFeedback){
    def http = new HTTPBuilder(url)
    ygorFeedback.statusDescription += " Sending update to ${url}."
    log.info("...sending update ${text.substring(0, text.length() > 50 ? 50 : text.length())}")
    http.request(Method.POST, ContentType.JSON){ request ->
      headers.'User-Agent' = 'ygor'
      headers.'Accept-Language' = locale
      body = text
      response.success = { response, html ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.OK
          ygorFeedback.statusDescription += " Update was successfully sent."
          if (response.status < 400){
            return ['info': html]
          }
          else{
            ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.ERROR
            ygorFeedback.statusDescription += " Unspecified error! Missing UTF-8 content type."
            return ['warning': html]
          }
        }
        else{
          ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.ERROR
          ygorFeedback.statusDescription += " Authentication error!"
          return ['error': ['message': "Authentication error!", 'result': "ERROR"]]
        }
      }
      response.failure = { response, html ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.ERROR
          ygorFeedback.statusDescription += " Unspecified error!"
          return ['error': html]
        }
        else{
          ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.ERROR
          ygorFeedback.statusDescription += " Unspecified error! Missing UTF-8 content type."
          return ['error': ['message': "Authentication error!", 'result': "ERROR"]]
        }
      }
      response.'401' = { response ->
        ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.ERROR
        ygorFeedback.statusDescription += " Ygor received 401 response!"
        return ['error': ['message': "Authentication error!", 'result': "ERROR"]]
      }
    }
  }

}
