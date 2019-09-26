package ygor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.tools.FileToolkit
import de.hbznrw.ygor.tools.JsonToolkit
import groovy.json.JsonOutput
import org.apache.commons.lang.StringUtils
import org.codehaus.groovy.runtime.InvokerInvocationException
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField

import java.time.LocalDateTime

class Enrichment {

  static enum FileType {
    ORIGIN,
    RESULT,
    JSON,
    JSON_PACKAGE_ONLY,
    JSON_TITLES_ONLY,
    JSON_OO_RAW
  }

  static enum ProcessingState {
    UNTOUCHED, PREPARE, WORKING, FINISHED, ERROR
  }

  static ObjectMapper JSON_OBJECT_MAPPER = new ObjectMapper()

  ProcessingState status

  // frontend api stuff
  String apiMessage
  double apiProgress = 0.0

  String originName
  String originHash
  String originPathName
  String packageName

  String resultName
  String resultHash
  String resultPathName
  String dataType

  File sessionFolder
  String ygorVersion
  String date

  def thread
  MappingsContainer mappingsContainer
  def dataContainer
  def stats

  static constraints = {
  }

  Enrichment(File sessionFolder, String originalFilename) {
    this.sessionFolder = sessionFolder
    originName = originalFilename.replaceAll(/\s+/, '_')
    originHash = FileToolkit.getMD5Hash(originName + Math.random())
    originPathName = this.sessionFolder.getPath() + File.separator + originHash

    dataContainer = new DataContainer()
    setStatus(ProcessingState.UNTOUCHED)
  }

  def process(HashMap options) {
    resultName = FileToolkit.getDateTimePrefixedFileName(originName)
    resultHash = FileToolkit.getMD5Hash(originName + Math.random())
    resultPathName = sessionFolder.getPath() + File.separator + resultHash
    dataType = options.get('dataTyp')
    ygorVersion = options.get('ygorVersion')

    dataContainer.info.file = originName
    dataContainer.info.type = options.get('ygorType')

    mappingsContainer = new MappingsContainer()
    thread = new MultipleProcessingThread(this, options)
    date = LocalDateTime.now().toString()
    thread.start()
  }

  def setProgress(double progress) {
    this.apiProgress = progress
  }

  double getProgress() {
    apiProgress
  }

  def setMessage(String message) {
    this.apiMessage = message
  }

  String getMessage() {
    apiMessage
  }

  def setStatusByCallback(ProcessingState status) {
    setStatus(status)
  }

  def setStatus(ProcessingState status) {
    this.status = status
  }

  ProcessingState getStatus() {
    status
  }

  void validateContainer() {
    dataContainer.validateRecords()
  }


  File getAsFile(FileType type) {
    // by now, the only export file type is for GOKb, so call GOKbExporter

    if (type.equals(FileType.JSON_OO_RAW)) {
      return GokbExporter.getFile(this, type, resultPathName)
    }
    // else
    return GokbExporter.getFile(this, type, originPathName)
  }


  void saveResult() {
    StringWriter result = new StringWriter()
    result.append("{\"sessionFolder\":\"").append(sessionFolder.absolutePath).append("\",")
    result.append("\"originalFileName\":\"").append(originName).append("\",")
    result.append("\"ygorVersion\":\"").append(ygorVersion).append("\",")
    result.append("\"date\":\"").append(date).append("\",")
    result.append("\"resultHash\":\"").append(resultHash).append("\",")
    String pn = packageName ? packageName : dataContainer.packageHeader.name.asText()
    result.append("\"packageName\":\"").append(pn).append("\",")
    result.append("\"configuration\":{")
    result.append("\"dataType\":\"").append(dataType).append("\",")
    result.append("\"namespaceTitleId\":\"").append(dataContainer.info.namespace_title_id).append("\",")
    result.append("\"mappingsContainer\":")
    result.append(JsonToolkit.toJson(mappingsContainer))
    result.append("},\"data\":")
    result.append(JsonToolkit.toJson(dataContainer.records.values()))
    result.append("}")
    File file = new File(resultPathName)
    file.getParentFile().mkdirs()
    file.write(JsonOutput.prettyPrint(result.toString()), "UTF-8")
  }


  static Enrichment fromRawJson(JsonNode rootNode) {
    String sessionFolder = JsonToolkit.fromJson(rootNode, "sessionFolder")
    String originalFileName = JsonToolkit.fromJson(rootNode, "originalFileName")
    def en = new Enrichment(new File(sessionFolder), originalFileName)
    en.ygorVersion = JsonToolkit.fromJson(rootNode, "ygorVersion") // TODO compare with current version and abort?
    en.date = JsonToolkit.fromJson(rootNode, "date")
    en.resultHash = JsonToolkit.fromJson(rootNode, "resultHash")
    en.mappingsContainer = JsonToolkit.fromJson(rootNode, "configuration.mappingsContainer")
    en.resultName = FileToolkit.getDateTimePrefixedFileName(originalFileName)
    en.resultPathName = sessionFolder.concat(File.separator).concat(FileToolkit.getMD5Hash(originalFileName + Math.random()))
    en.dataContainer = DataContainer.fromJson(rootNode.path("data"), en.mappingsContainer)
    en.dataContainer.info.namespace_title_id = JsonToolkit.fromJson(rootNode, "configuration.namespaceTitleId")
    en.dataType = JsonToolkit.fromJson(rootNode, "configuration.dataType")
    en.packageName = JsonToolkit.fromJson(rootNode, "packageName")
    return en
  }


  static Enrichment fromFile(def file) {
    String json
    try {
      json = file.getInputStream()?.text
    }
    catch (MissingMethodException | InvokerInvocationException e) {
      json = file.newInputStream()?.text
    }
    JsonNode rootNode = JSON_OBJECT_MAPPER.readTree(json)
    Enrichment enrichment = Enrichment.fromRawJson(rootNode)
    enrichment.setTitleMediumMapping()
    enrichment.setTitleTypeMapping()
    enrichment.setTippPlatformNameMapping()
    enrichment.setTippPlatformUrlMapping()
    enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
    enrichment
  }


  FieldKeyMapping setTitleMediumMapping() {
    FieldKeyMapping mediumMapping = mappingsContainer.getMapping("medium", MappingsContainer.YGOR)
    if (dataType == 'ebooks') {
      mediumMapping.val = "Book"
    } else if (dataType == 'database') {
      mediumMapping.val = "Database"
    } else {
      mediumMapping.val = "Journal"
    }
    return mediumMapping
  }


  FieldKeyMapping setTitleTypeMapping() {
    FieldKeyMapping typeMapping = mappingsContainer.getMapping("publicationType", MappingsContainer.YGOR)
    if (dataType == 'ebooks') {
      typeMapping.val = "Book"
    } else if (dataType == 'database') {
      typeMapping.val = "Database"
    } else {
      typeMapping.val = "Serial"
    }
    return typeMapping
  }


  FieldKeyMapping setTippPlatformNameMapping() {
    FieldKeyMapping platformNameMapping = mappingsContainer.getMapping("platformName", MappingsContainer.YGOR)
    if (StringUtils.isEmpty(platformNameMapping.val)) {
      platformNameMapping.val = dataContainer.pkg.packageHeader.v.nominalPlatform.name
    }
    platformNameMapping
  }


  FieldKeyMapping setTippPlatformUrlMapping() {
    FieldKeyMapping platformUrlMapping = mappingsContainer.getMapping("platformUrl", MappingsContainer.YGOR)
    if (StringUtils.isEmpty(platformUrlMapping.val)) {
      platformUrlMapping.val = dataContainer.pkg.packageHeader.v.nominalPlatform.url
    }
    platformUrlMapping
  }


  void enrollMappingToRecords(FieldKeyMapping mapping) {
    MultiField titleMedium = new MultiField(mapping)
    for (Record record in dataContainer.records.values()) {
      record.addMultiField(titleMedium)
    }
    return
  }
}
