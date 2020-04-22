package ygor

import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.export.structure.PackageHeader
import de.hbznrw.ygor.export.structure.PackageHeaderNominalPlatform
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.processing.YgorProcessingException
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.tools.FileToolkit
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.tools.RecordFileFilter
import de.hbznrw.ygor.tools.SessionToolkit
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class Enrichment{

  static mapWith = "none" // disable persisting into database

  static enum FileType {
    ORIGIN,
    RESULT,
    JSON,
    JSON_PACKAGE_ONLY,
    JSON_TITLES_ONLY,
    JSON_OO_RAW
  }

  static enum ProcessingState {
    PREPARE_1, PREPARE_2, WORKING, FINISHED, ERROR
  }

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

  File sessionFolder
  String ygorVersion
  String date

  def thread
  MappingsContainer mappingsContainer
  def dataContainer
  def stats

  Map<String, List<String>> greenRecords = new HashMap<>()
  Map<String, List<String>> yellowRecords = new HashMap<>()
  Map<String, List<String>> redRecords = new HashMap<>()

  static constraints = {
  }


  Enrichment(File sessionFolder, String originalFilename){
    this.sessionFolder = sessionFolder
    originName = originalFilename.replaceAll(/\s+/, '_')
    originHash = FileToolkit.getMD5Hash(originName + Math.random())
    originPathName = this.sessionFolder.getPath() + File.separator + originHash
    resultHash = FileToolkit.getMD5Hash(originName + Math.random())
    resultPathName = sessionFolder.getPath() + File.separator + resultHash
    dataContainer = new DataContainer()
  }


  def process(HashMap options, KbartReader kbartReader) throws YgorProcessingException{
    resultName = FileToolkit.getDateTimePrefixedFileName(originName)
    ygorVersion = options.get('ygorVersion')

    dataContainer.info.file = originName
    dataContainer.info.type = options.get('ygorType')

    mappingsContainer = new MappingsContainer()
    thread = new MultipleProcessingThread(this, options, kbartReader)
    date = LocalDateTime.now().toString()
    thread.start()
  }


  def stop(){
    thread.stopEnrichment()
  }

  def setProgress(double progress){
    this.apiProgress = progress
  }

  double getProgress(){
    apiProgress
  }

  def setMessage(String message){
    this.apiMessage = message
  }

  String getMessage(){
    apiMessage
  }

  def setStatusByCallback(ProcessingState status){
    setStatus(status)
  }

  def setStatus(ProcessingState status){
    this.status = status
  }

  ProcessingState getStatus(){
    status
  }

  void validateContainer(){
    dataContainer.validateRecords()
  }


  File getAsFile(FileType type, boolean validate){
    // by now, the only export file type is for GOKb, so call GOKbExporter
    return GokbExporter.getFile(this, type, validate)
  }


  void saveResult(){
    log.info("Saving enrichment...")
    StringWriter result = new StringWriter()
    result.append("{\"sessionFolder\":\"").append(sessionFolder.absolutePath).append("\",")
    result.append("\"originalFileName\":\"").append(originName).append("\",")
    result.append("\"ygorVersion\":\"").append(ygorVersion).append("\",")
    result.append("\"date\":\"").append(date).append("\",")
    result.append("\"originHash\":\"").append(originHash).append("\",")
    result.append("\"resultHash\":\"").append(resultHash).append("\",")
    result.append("\"originPathName\":\"").append(originPathName).append("\",")
    result.append("\"resultPathName\":\"").append(resultPathName).append("\",")
    String pn = packageName ? packageName : dataContainer.packageHeader?.name?.asText()
    if (pn){
      result.append("\"packageName\":\"").append(pn).append("\",")
    }
    if (greenRecords != null && !greenRecords.isEmpty()){
      result.append("\"greenRecords\":").append(JsonToolkit.mapToJson(greenRecords)).append(",")
    }
    if (yellowRecords != null && !yellowRecords.isEmpty()){
      result.append("\"yellowRecords\":").append(JsonToolkit.mapToJson(yellowRecords)).append(",")
    }
    if (redRecords != null && !redRecords.isEmpty()){
      result.append("\"redRecords\":").append(JsonToolkit.mapToJson(redRecords)).append(",")
    }
    result.append("\"configuration\":{")
    result.append("\"namespaceTitleId\":\"").append(dataContainer.info.namespace_title_id).append("\",")
    if (dataContainer.curatoryGroup != null){
      result.append("\"curatoryGroup\":\"").append(dataContainer.curatoryGroup).append("\",")
    }
    if (dataContainer.pkgId != null){
      result.append("\"pkgId\":\"").append(dataContainer.pkgId).append("\",")
    }
    if (dataContainer.pkgIdNamespace != null){
      result.append("\"pkgIdNamespace\":\"").append(dataContainer.pkgIdNamespace).append("\",")
    }
    if (dataContainer.isil != null){
      result.append("\"isil\":\"").append(dataContainer.isil).append("\",")
    }
    if (dataContainer.pkg?.packageHeader?.nominalProvider != null){
      result.append("\"nominalProvider\":\"").append(dataContainer.pkg.packageHeader.nominalProvider).append("\",")
    }
    if (dataContainer.pkg?.packageHeader?.nominalPlatform != null){
      result.append("\"nominalPlatform\":{")
      result.append("\"name\":\"").append(dataContainer.pkg?.packageHeader?.nominalPlatform.name).append("\",")
      result.append("\"url\":\"").append(dataContainer.pkg?.packageHeader?.nominalPlatform.url).append("\"")
      result.append("},")
    }
    result.append("\"mappingsContainer\":")
    result.append(JsonToolkit.toJson(mappingsContainer))
    result.append("}}")
    File file = new File(resultPathName)
    file.getParentFile().mkdirs()
    file.write(JsonOutput.prettyPrint(result.toString()), "UTF-8")

    // write records into separate files named <resultHash>_<recordUid>
    for (def record in dataContainer.records){
      new File(resultPathName.concat("_").concat(record.key))
              .write(JsonOutput.prettyPrint(JsonToolkit.toJson(record.value)), "UTF-8")
    }
    log.info("Saving enrichment finished.")
  }


  static Enrichment fromRawJson(JsonNode rootNode, boolean loadRecordData){
    String sessionFolder = JsonToolkit.fromJson(rootNode, "sessionFolder")
    String originalFileName = JsonToolkit.fromJson(rootNode, "originalFileName")
    def en = new Enrichment(new File(sessionFolder), originalFileName)
    en.ygorVersion = JsonToolkit.fromJson(rootNode, "ygorVersion") // TODO compare with current version and abort?
    en.date = JsonToolkit.fromJson(rootNode, "date")
    en.originHash = JsonToolkit.fromJson(rootNode, "originHash")
    en.resultHash = JsonToolkit.fromJson(rootNode, "resultHash")
    en.originPathName = JsonToolkit.fromJson(rootNode, "originPathName")
    en.resultPathName = JsonToolkit.fromJson(rootNode, "resultPathName")
    en.mappingsContainer = JsonToolkit.fromJson(rootNode, "configuration.mappingsContainer")
    en.resultName = FileToolkit.getDateTimePrefixedFileName(originalFileName)
    if (loadRecordData){
      en.dataContainer = DataContainer.fromJson(en.sessionFolder, en.resultHash, en.mappingsContainer)
    }
    en.dataContainer.info.namespace_title_id = JsonToolkit.fromJson(rootNode, "configuration.namespaceTitleId")

    if (null != JsonToolkit.fromJson(rootNode, "configuration.curatoryGroup")){
      en.dataContainer.curatoryGroup = JsonToolkit.fromJson(rootNode, "configuration.curatoryGroup")
    }
    if (null != JsonToolkit.fromJson(rootNode, "configuration.pkgId")){
      en.dataContainer.pkgId = JsonToolkit.fromJson(rootNode, "configuration.pkgId")
    }
    if (null != JsonToolkit.fromJson(rootNode, "configuration.pkgIdNamespace")){
      en.dataContainer.pkgIdNamespace = JsonToolkit.fromJson(rootNode, "configuration.pkgIdNamespace")
    }
    if (null != JsonToolkit.fromJson(rootNode, "configuration.isil")){
      en.dataContainer.isil = JsonToolkit.fromJson(rootNode, "configuration.isil")
    }
    en.dataContainer.pkg.packageHeader = new PackageHeader()
    en.dataContainer.pkg.packageHeader.nominalProvider = JsonToolkit.fromJson(rootNode, "configuration.nominalProvider")
    en.dataContainer.pkg.packageHeader.nominalPlatform = PackageHeaderNominalPlatform.fromJson(rootNode, "configuration.nominalPlatform")
    en.packageName = JsonToolkit.fromJson(rootNode, "packageName")
    en.greenRecords = JsonToolkit.fromJsonNode(rootNode.get("greenRecords"))
    en.yellowRecords = JsonToolkit.fromJsonNode(rootNode.get("yellowRecords"))
    en.redRecords = JsonToolkit.fromJsonNode(rootNode.get("redRecords"))
    return en
  }


  static Enrichment fromJsonFile(def file, boolean loadRecordData){
    JsonNode rootNode = JsonToolkit.jsonNodeFromFile(file)
    Enrichment enrichment = Enrichment.fromRawJson(rootNode, loadRecordData)
    enrichment.setTippPlatformNameMapping()
    enrichment.setTippPlatformUrlMapping()
    enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
    enrichment
  }


  static Enrichment fromZipFile(def zipFile, String sessionFoldersRoot) throws IOException{
    JsonSlurper slurpy = new JsonSlurper()
    byte[] buffer = new byte[1024]
    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.fileItem.tempFile))
    ZipEntry zipEntry = zis.getNextEntry()
    Map<?,?> configMap = getConfigMap(zipEntry, zis, slurpy, sessionFoldersRoot)
    File sessionFolder = new File(configMap.get("sessionFolder"))
    File configFile = new File(configMap.get("resultPathName"))
    zipEntry = zis.getNextEntry()
    while (zipEntry != null) {
      File nextFile = getNextFileFromZip(sessionFolder, zipEntry)
      writeIntoFileOutputStream(nextFile, zis, buffer)
      zipEntry = zis.getNextEntry()
    }
    zis.closeEntry()
    zis.close()

    List<File> recordFiles = sessionFolder.listFiles(new RecordFileFilter(configMap.get("resultHash")))
    Enrichment enrichment = fromJsonFile(configFile, true)
    for (File RecordFile in recordFiles){
      Record record = Record.fromJson(JsonToolkit.jsonNodeFromFile(RecordFile), enrichment.mappingsContainer)
      enrichment.dataContainer.records.put(record.uid, record)
    }
    enrichment
  }


  private static Map getConfigMap(ZipEntry configZipEntry, ZipInputStream zis,
                                  JsonSlurper slurpy, String sessionFoldersRoot) throws IOException{
    File tmpFile = new File(sessionFoldersRoot.concat(File.separator).concat(UUID.randomUUID().toString()))
    File configFile = getNextFileFromZip(tmpFile, configZipEntry)
    Files.createDirectories(configFile.parentFile.toPath())
    configFile.createNewFile()
    writeIntoFileOutputStream(configFile, zis, new byte[1024])
    Map<?,?> configMap = slurpy.parseText(configFile.text)
    File sessionFolder = new File(configMap.get("sessionFolder"))
    if (sessionFolder.exists()){
      Paths.get(configMap.get("sessionFolder")).deleteDir()
    }
    sessionFolder.mkdirs()
    Path fullResultPath = Paths.get(configMap.get("sessionFolder").concat(File.separator).concat(configMap.get("resultHash")))
    Files.move(configFile.toPath(), fullResultPath, StandardCopyOption.REPLACE_EXISTING)
    return configMap
  }


  private static void writeIntoFileOutputStream(File nextFile, ZipInputStream zis, byte[] buffer){
    FileOutputStream fos = new FileOutputStream(nextFile)
    int len
    while ((len = zis.read(buffer)) > 0){
      fos.write(buffer, 0, len)
    }
    fos.close()
  }


  private static File getNextFileFromZip(File destinationDir, ZipEntry zipEntry) throws IOException {
    File destFile = new File(destinationDir, zipEntry.getName())
    String destDirPath = destinationDir.getCanonicalPath()
    String destFilePath = destFile.getCanonicalPath()
    if (!destFilePath.startsWith(destDirPath + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName())
    }
    return destFile
  }


  FieldKeyMapping setTippPlatformNameMapping(){
    FieldKeyMapping platformNameMapping = mappingsContainer.getMapping("platformName", MappingsContainer.YGOR)
    if (StringUtils.isEmpty(platformNameMapping.val)){
      platformNameMapping.val = dataContainer.pkg.packageHeader.nominalPlatform.name
    }
    platformNameMapping
  }


  FieldKeyMapping setTippPlatformUrlMapping(){
    FieldKeyMapping platformUrlMapping = mappingsContainer.getMapping("platformUrl", MappingsContainer.YGOR)
    if (StringUtils.isEmpty(platformUrlMapping.val)){
      platformUrlMapping.val = dataContainer.pkg.packageHeader.nominalPlatform.url
    }
    platformUrlMapping
  }


  void enrollMappingToRecords(FieldKeyMapping mapping){
    MultiField multiField = new MultiField(mapping)
    for (Record record in dataContainer.records.values()){
      multiField.validate(dataContainer.info.namespace_title_id)
      record.addMultiField(multiField)
    }
    return
  }


  void setCurrentSession(){
    sessionFolder = new File(Holders.config.ygor.uploadLocation + File.separator + SessionToolkit.getSession().id)
    originPathName = sessionFolder.absolutePath + File.separator + originHash
    resultPathName = sessionFolder.absolutePath + File.separator + resultHash
  }


  synchronized void classifyAllRecords(){
    log.info("Classifying all records...")
    greenRecords = new TreeMap<>()
    yellowRecords = new TreeMap<>()
    redRecords = new TreeMap<>()
    String namespace = dataContainer.info.namespace_title_id
    for (Record record in dataContainer.records.values()){
      record.normalize(namespace)
      record.validate(namespace)
      classifyRecord(record)
    }
    log.info("Classifying all records finished.")
  }


  synchronized void classifyRecord(Record record){
    String key = record.displayTitle.concat(record.uid)
    List<String> values = [
        valOrEmpty(record.displayTitle.size() > 100 ? record.displayTitle.substring(0,100).concat("...") : record.displayTitle),
        valOrEmpty(record.zdbIntegrationUrl),
        valOrEmpty(record.zdbId),
        valOrEmpty(record.onlineIdentifier),
        valOrEmpty(record.uid)
    ]
    if (record.isValid()){
      if (record.multiFields.get("titleUrl").isCorrect(record.publicationType) &&
          record.duplicates.isEmpty() &&
          (!record.publicationType.equals("serial") || record.zdbIntegrationUrl != null) &&
          !record.hasFlagOfColour(RecordFlag.Colour.YELLOW)){
        greenRecords.put(key, values)
        yellowRecords.remove(key)
        redRecords.remove(key)
      }
      else{
        yellowRecords.put(key, values)
        greenRecords.remove(key)
        redRecords.remove(key)
      }
    }
    else{
      redRecords.put(key,values)
      yellowRecords.remove(key)
      greenRecords.remove(key)
    }
  }


  private String valOrEmpty(def val){
    if (val == null || val.equals("null")){
      return ""
    }
    return val.toString()
  }

}
