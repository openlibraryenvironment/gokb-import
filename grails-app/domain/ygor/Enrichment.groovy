package ygor

import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.export.DataContainer
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.export.structure.PackageHeader
import de.hbznrw.ygor.export.structure.PackageHeaderNominalPlatform
import de.hbznrw.ygor.export.structure.PackageHeaderNominalProvider
import de.hbznrw.ygor.normalizers.DateNormalizer
import de.hbznrw.ygor.processing.MultipleProcessingThread
import de.hbznrw.ygor.readers.KbartReader
import de.hbznrw.ygor.tools.FileToolkit
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.tools.RecordFileFilter
import de.hbznrw.ygor.tools.SessionToolkit
import grails.util.Holders
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils
import org.springframework.web.multipart.commons.CommonsMultipartFile
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@SuppressWarnings('JpaObjectClassSignatureInspection')
class Enrichment{

  static mapWith = "none" // disable persisting into database
  static enum FileType {
    ORIGIN,
    RESULT,
    JSON,
    PACKAGE,
    TITLES,
    PACKAGE_WITH_TITLEDATA,
    RAW
  }

  static enum ProcessingState {
    PREPARE_1, PREPARE_2, WORKING, FINISHED, ERROR
  }

  ProcessingState status

  // frontend api stuff
  String apiMessage
  double apiProgress = 0.0

  String originUrl
  String updateUrl
  String originName
  String originHash
  String originPathName
  String fileNameDate
  File transferredFile
  String packageName
  long packageId

  String resultName
  String resultHash
  String enrichmentFolder
  File sessionFolder

  String ygorVersion
  List<String> processingOptions
  String lastProcessingDate
  String locale
  boolean addOnly
  boolean ignoreLastChanged
  boolean isZdbIntegrated
  boolean isEzbIntegrated
  boolean autoUpdate                // shall be auto-updated in future times
  boolean isUpdate                  // this enrichment is part of an update
  boolean needsPreciseClassification// this enrichment is part of an Ygor or gokb-ui triggered process
  boolean markDuplicates            // is only necessary when the process is triggered by Ygor UI
  // possible TODO: gokb-ui might need this field in future times

  def thread
  KbartReader kbartReader
  MappingsContainer mappingsContainer
  def dataContainer

  Map<String, List<String>> greenRecords = new HashMap<>()
  Map<String, List<String>> yellowRecords = new HashMap<>()
  Map<String, List<String>> redRecords = new HashMap<>()

  Map<FileType, Boolean> hasBeenUploaded = new HashMap<>()

  static constraints = {
  }


  Enrichment(File sessionFolder, String originalFilename){
    this.sessionFolder = sessionFolder
    originName = originalFilename.replaceAll(/\s+/, '_')
    originHash = FileToolkit.getMD5Hash(originName + Math.random())
    originPathName = this.sessionFolder.getPath() + File.separator + originHash
    resultHash = FileToolkit.getMD5Hash(originName + Math.random())
    enrichmentFolder = sessionFolder.getPath() + File.separator + resultHash + File.separator
    new File(enrichmentFolder).mkdirs()
    mappingsContainer = new MappingsContainer()
    dataContainer = new DataContainer(sessionFolder, enrichmentFolder, resultHash, mappingsContainer)
    isZdbIntegrated = false
    isEzbIntegrated = false
    autoUpdate = false
    isUpdate = false
    needsPreciseClassification = true
    transferredFile = null
    markDuplicates = false
    ignoreLastChanged = false
  }


  static Enrichment fromCommonsMultipartFile(CommonsMultipartFile file){
    Enrichment en = fromFilename(file.originalFilename)
    en.markDuplicates = true
    en.transferredFile = new File(en.originPathName)
    file.transferTo(en.transferredFile)
    return en
  }


  static Enrichment fromFilename(String filename){
    return new Enrichment(EnrichmentService.getSessionFolder(), filename)
  }


  static Enrichment fromFilename(String sessionFolder, String filename) throws Exception{
    return new Enrichment(new File(sessionFolder), filename)
  }


  void addFileAndFormat(){
    setStatus(Enrichment.ProcessingState.PREPARE_1)
    def tmp = [:]
    def formats = EnrichmentService.getSessionFormats()
    formats.put(originHash, tmp)
    EnrichmentService.getSessionEnrichments().put(resultHash, this)
  }


  /**
   * Ygor's central processing method.
   */
  def process(HashMap options, KbartReader kbartReader) throws Exception{
    log.debug("Start processing enrichment ${originName}.")
    this.kbartReader = kbartReader
    if (kbartReader.fileNameDate){
      this.fileNameDate = DateNormalizer.YYYY_MM_DD.format(kbartReader.fileNameDate)
    }
    resultName = FileToolkit.getDateTimePrefixedFileName(originName)
    ygorVersion = options.get('ygorVersion')
    dataContainer.info.file = originName
    dataContainer.info.type = options.get('ygorType')
    thread = new MultipleProcessingThread(this, options, kbartReader)
    thread.start()
  }


  def stop(){
    log.debug("Stop processing enrichment ${this.originName}.")
    thread.stopEnrichment()
  }

  def setProgress(double progress){
    this.apiProgress = progress
  }

  @SuppressWarnings('JpaAttributeMemberSignatureInspection')
  double getProgress(){
    apiProgress
  }

  def setMessage(String message){
    apiMessage = message
  }

  @SuppressWarnings('JpaAttributeMemberSignatureInspection')
  String getMessage(){
    apiMessage
  }

  def setStatusByCallback(ProcessingState status){
    setStatus(status)
  }

  def setStatus(ProcessingState status){
    this.status = status
  }

  @SuppressWarnings('JpaAttributeMemberSignatureInspection')
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


  void save(){
    log.info("Saving enrichment with ${dataContainer?.records?.size()} records...")
    String result = asJson(true)
    File file = new File(enrichmentFolder.concat(File.separator).concat(resultHash))
    file.getParentFile().mkdirs()
    file.write(JsonOutput.prettyPrint(result), "UTF-8")
    log.info("Saving enrichment finished.")
  }


  String asJson(boolean includeRecords){
    StringWriter result = new StringWriter()
    result.append("{\"sessionFolder\":\"").append(sessionFolder.absolutePath).append("\",")
    result.append("\"originalFileName\":\"").append(originName).append("\",")
    result.append("\"ygorVersion\":\"").append(ygorVersion).append("\",")
    result.append("\"lastProcessingDate\":\"").append(lastProcessingDate).append("\",")
    result.append("\"originHash\":\"").append(originHash).append("\",")
    result.append("\"resultHash\":\"").append(resultHash).append("\",")
    result.append("\"originPathName\":\"").append(originPathName).append("\",")
    if (fileNameDate){
      result.append("\"fileNameDate\":\"").append(fileNameDate).append("\",")
    }
    result.append("\"autoUpdate\":\"").append(String.valueOf(autoUpdate)).append("\",")
    result.append("\"isUpdate\":\"").append(String.valueOf(isUpdate)).append("\",")
    result.append("\"needsPreciseClassification\":\"").append(String.valueOf(needsPreciseClassification)).append("\",")
    result.append("\"markDuplicates\":\"").append(String.valueOf(markDuplicates)).append("\",")
    result.append("\"enrichmentFolder\":\"").append(enrichmentFolder).append("\",")
    String pn = packageName ? packageName : dataContainer.packageHeader?.name?.asText()
    if (pn){
      result.append("\"packageName\":\"").append(pn).append("\",")
    }
    if (includeRecords){
      result.append("\"records\":").append(JsonToolkit.setToJson(dataContainer.records)).append(",")
      result.append("\"greenRecords\":").append(JsonToolkit.mapToJson(greenRecords)).append(",")
      result.append("\"yellowRecords\":").append(JsonToolkit.mapToJson(yellowRecords)).append(",")
      result.append("\"redRecords\":").append(JsonToolkit.mapToJson(redRecords)).append(",")
    }
    String token = dataContainer.pkgHeader?.token
    if (token){
      result.append("\"token\":\"").append(token).append("\",")
    }
    String uuid = dataContainer.pkgHeader?.uuid
    if (uuid){
      result.append("\"uuid\":\"").append(uuid).append("\",")
    }
    result.append("\"configuration\":")
    result.append(this.getConfiguration())
    result.append("}")
    result.toString()
  }


  @SuppressWarnings('JpaAttributeMemberSignatureInspection')
  String getConfiguration(){
    StringWriter result = new StringWriter()
    result.append("{")
    if (originUrl != null){
      result.append("\"originUrl\":\"").append(originUrl).append("\",")
    }
    if (updateUrl != null){
      result.append("\"updateUrl\":\"").append(updateUrl).append("\",")
    }
    result.append("\"namespaceTitleId\":\"").append(dataContainer.info.namespace_title_id).append("\",")
    result.append("\"addOnly\":\"").append(String.valueOf(addOnly)).append("\",")
    result.append("\"ignoreLastChanged\":\"").append(String.valueOf(ignoreLastChanged)).append("\",")
    result.append("\"isZdbIntegrated\":\"").append(String.valueOf(isZdbIntegrated)).append("\",")
    result.append("\"isEzbIntegrated\":\"").append(String.valueOf(isEzbIntegrated)).append("\",")
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
    if (dataContainer.pkgHeader?.nominalProvider != null){
      result.append("\"nominalProvider\":{")
        result.append("\"name\":\"").append(dataContainer.pkgHeader?.nominalProvider.name).append("\",")
        result.append("\"oid\":\"").append(dataContainer.pkgHeader?.nominalProvider.oid).append("\"")
      result.append("},")
    }
    if (dataContainer.pkgHeader?.nominalPlatform != null){
      result.append("\"nominalPlatform\":{")
        result.append("\"name\":\"").append(dataContainer.pkgHeader?.nominalPlatform.name).append("\",")
        result.append("\"url\":\"").append(dataContainer.pkgHeader?.nominalPlatform.url).append("\"")
        result.append("\"oid\":\"").append(dataContainer.pkgHeader?.nominalPlatform.oid).append("\"")
      result.append("},")
    }
    result.append("\"locale\":\"").append(locale).append("\",")
    result.append("\"processingOptions\":").append(JsonToolkit.listToJson(processingOptions)).append(",")
    result.append("\"mappingsContainer\":")
    result.append(JsonToolkit.toJson(mappingsContainer))
    result.append("}")
    return result.toString()
  }


  static Enrichment fromRawJson(JsonNode rootNode, boolean loadRecordData){
    String sessionFolder = JsonToolkit.fromJson(rootNode, "sessionFolder")
    String originalFileName = JsonToolkit.fromJson(rootNode, "originalFileName")
    def en = new Enrichment(new File(sessionFolder), originalFileName)
    en.ygorVersion = JsonToolkit.fromJson(rootNode, "ygorVersion") // TODO compare with current version and abort?
    en.lastProcessingDate = JsonToolkit.fromJson(rootNode, "lastProcessingDate")
    en.originHash = JsonToolkit.fromJson(rootNode, "originHash")
    en.resultHash = JsonToolkit.fromJson(rootNode, "resultHash")
    en.originPathName = JsonToolkit.fromJson(rootNode, "originPathName")
    en.fileNameDate = JsonToolkit.fromJson(rootNode, "fileNameDate")
    en.autoUpdate = Boolean.valueOf(JsonToolkit.fromJson(rootNode, "autoUpdate"))
    en.isUpdate = Boolean.valueOf(JsonToolkit.fromJson(rootNode, "isUpdate"))
    en.needsPreciseClassification = Boolean.valueOf(JsonToolkit.fromJson(rootNode, "needsPreciseClassification"))
    en.markDuplicates = Boolean.valueOf(JsonToolkit.fromJson(rootNode, "markDuplicates"))
    en.enrichmentFolder = JsonToolkit.fromJson(rootNode, "enrichmentFolder")
    en.mappingsContainer = JsonToolkit.fromJson(rootNode, "configuration.mappingsContainer")
    en.resultName = FileToolkit.getDateTimePrefixedFileName(originalFileName)
    en.dataContainer =
        DataContainer.fromJson(en.sessionFolder, en.enrichmentFolder, en.resultHash, en.mappingsContainer, loadRecordData)
    en.dataContainer.records = JsonToolkit.fromJson(rootNode, "records")
    en.dataContainer.markDuplicateIds()
    en.originUrl = JsonToolkit.fromJson(rootNode, "configuration.originUrl")
    en.updateUrl = JsonToolkit.fromJson(rootNode, "configuration.updateUrl")
    en.dataContainer.info.namespace_title_id = JsonToolkit.fromJson(rootNode, "configuration.namespaceTitleId")
    en.addOnly = Boolean.valueOf(JsonToolkit.fromJson(rootNode, "configuration.addOnly"))
    en.ignoreLastChanged = Boolean.valueOf(JsonToolkit.fromJson(rootNode, "configuration.ignoreLastChanged"))
    en.isZdbIntegrated = Boolean.valueOf(JsonToolkit.fromJson(rootNode, "configuration.isZdbIntegrated"))
    en.isEzbIntegrated = Boolean.valueOf(JsonToolkit.fromJson(rootNode, "configuration.isEzbIntegrated"))
    en.processingOptions = JsonToolkit.fromJson(rootNode, "configuration.processingOptions")
    en.locale = JsonToolkit.fromJson(rootNode, "configuration.locale")
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
    en.dataContainer.pkgHeader = new PackageHeader()
    en.dataContainer?.pkgHeader?.nominalProvider = PackageHeaderNominalProvider.fromJson(rootNode, "configuration.nominalProvider")
    en.dataContainer?.pkgHeader?.nominalPlatform = PackageHeaderNominalPlatform.fromJson(rootNode, "configuration.nominalPlatform")
    en.packageName = JsonToolkit.fromJson(rootNode, "packageName")
    if (null != JsonToolkit.fromJson(rootNode, "token")){
      en.dataContainer?.pkgHeader?.token = JsonToolkit.fromJson(rootNode, "token")
    }
    if (null != JsonToolkit.fromJson(rootNode, "uuid")){
      en.dataContainer?.pkgHeader?.uuid = JsonToolkit.fromJson(rootNode, "uuid")
    }
    en.greenRecords = JsonToolkit.fromJsonNode(rootNode.get("greenRecords"))
    if (en.greenRecords == null){
      en.greenRecords = new HashMap<>()
    }
    en.yellowRecords = JsonToolkit.fromJsonNode(rootNode.get("yellowRecords"))
    if (en.yellowRecords == null){
      en.yellowRecords = new HashMap<>()
    }
    en.redRecords = JsonToolkit.fromJsonNode(rootNode.get("redRecords"))
    if (en.redRecords == null){
      en.redRecords = new HashMap<>()
    }
    en.hasBeenUploaded.put(FileType.TITLES, false)
    en.hasBeenUploaded.put(FileType.PACKAGE, false)
    return en
  }


  static Enrichment fromJsonFile(File file, boolean loadRecordData){
    JsonNode rootNode = JsonToolkit.jsonNodeFromFile(file)
    Enrichment enrichment = Enrichment.fromRawJson(rootNode, loadRecordData)
    enrichment.enrollPlatformToRecords()
    enrichment
  }


  static Enrichment fromZipFile(def zipFile, String sessionFoldersRoot) throws IOException{
    JsonSlurper slurpy = new JsonSlurper()
    ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())
    ZipEntry zipEntry = zis.getNextEntry()
    if (zipEntry != null){
      Map<?,?> configMap = getConfigMap(zipEntry, zis, slurpy, sessionFoldersRoot)
      def (File enrichmentFolder, File configFile) = getRecordFiles(configMap, zis)
      zis.closeEntry()
      zis.close()
      List<File> recordFiles = enrichmentFolder.listFiles(new RecordFileFilter(configMap.get("resultHash")))
      Enrichment enrichment = fromJsonFile(configFile, true)
      for (File recordFile in recordFiles){
        enrichment.dataContainer.records.add(JsonToolkit.fromJson(JsonToolkit.jsonNodeFromFile(recordFile), "uid"))
      }
      enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
      return enrichment
    }
    return null
  }

  private static List getRecordFiles(Map configMap, ZipInputStream zis){
    File enrichmentFolder = new File(configMap.get("enrichmentFolder"))
    File configFile = new File(enrichmentFolder.absolutePath.concat(File.separator).concat(configMap.get("resultHash")))
    ZipEntry zipEntry = zis.getNextEntry()
    byte[] buffer = new byte[1024]
    while (zipEntry != null){
      File nextFile = getNextFileFromZip(enrichmentFolder.toPath(), zipEntry)
      nextFile.createNewFile()
      writeIntoFileOutputStream(nextFile, zis, buffer)
      zipEntry = zis.getNextEntry()
    }
    [enrichmentFolder, configFile]
  }


  private static Map getConfigMap(ZipEntry configZipEntry, ZipInputStream zis,
                                  JsonSlurper slurpy, String sessionFoldersRoot) throws IOException{
    String sessionId = SessionToolkit.getSession().id
    Path destinationDir = new File(sessionFoldersRoot.concat(File.separator).concat(sessionId)
                                                     .concat(File.separator).concat(configZipEntry.getName())).toPath()
    Files.createDirectories(destinationDir)
    File configFile = getNextFileFromZip(destinationDir, configZipEntry)
    configFile.createNewFile()
    writeIntoFileOutputStream(configFile, zis, new byte[1024])
    Map configMap = slurpy.parseText(configFile.text)
    configMap.put("sessionFolderOriginal", configMap.get("sessionFolder"))
    configMap.put("sessionFolder", sessionFoldersRoot.concat(File.separator).concat(sessionId))
    configMap.put("enrichmentFolderOriginal", configMap.get("enrichmentFolder"))
    configMap.put("enrichmentFolder", destinationDir.toString())
    configMap
  }


  private static void writeIntoFileOutputStream(File nextFile, ZipInputStream zis, byte[] buffer){
    FileOutputStream fos = new FileOutputStream(nextFile)
    int len
    while ((len = zis.read(buffer)) > 0){
      fos.write(buffer, 0, len)
    }
    fos.close()
  }


  private static File getNextFileFromZip(Path destinationDir, ZipEntry zipEntry) throws IOException {
    String dest = destinationDir.toString()
    File destFile = new File(dest, zipEntry.getName())
    if (!destFile.getCanonicalPath().startsWith(dest + File.separator)) {
      throw new IOException("Entry is outside of the target dir: " + zipEntry.getName())
    }
    return destFile
  }


  void enrollPlatformToRecords() {
    // TODO: do this in one iteration to half loading and saving times
    enrollMappingToRecords("platformName", dataContainer?.pkgHeader?.nominalPlatform.name)
    enrollMappingToRecords("platformUrl", dataContainer?.pkgHeader?.nominalPlatform.url)
    enrollMappingToRecords("platformOid", dataContainer?.pkgHeader?.nominalPlatform.oid)
  }


  void enrollMappingToRecords(String ygorField, String value){
    FieldKeyMapping tippNameMapping = createMappingWithValue(ygorField, value)
    MultiField multiField = new MultiField(tippNameMapping)
    for (String recId in dataContainer.records){
      Record record = Record.load(enrichmentFolder, resultHash, recId, mappingsContainer)
      multiField.validate(dataContainer.info.namespace_title_id)
      record.addMultiField(multiField)
      record.save(enrichmentFolder, resultHash)
    }
    return
  }


  FieldKeyMapping createMappingWithValue(String ygorField, String value){
    FieldKeyMapping platformNameMapping = mappingsContainer.getMapping(ygorField, MappingsContainer.YGOR)
    if (platformNameMapping != null && StringUtils.isEmpty(platformNameMapping.val)){
      platformNameMapping.val = value
    }
    platformNameMapping
  }


  void setCurrentSession(){
    sessionFolder = new File(Holders.config.ygor.uploadLocation.toString() + File.separator + SessionToolkit.getSession().id)
    enrichmentFolder = sessionFolder.absolutePath + File.separator + resultHash
    dataContainer.enrichmentFolder = enrichmentFolder + File.separator
  }


  synchronized void classifyAllRecords(){
    log.debug("Classifying all records ...")
    greenRecords = new TreeMap<>()
    yellowRecords = new TreeMap<>()
    redRecords = new TreeMap<>()
    String namespace = dataContainer.info.namespace_title_id
    for (String recId in dataContainer.records){
      Record record = Record.load(enrichmentFolder, resultHash, recId, mappingsContainer)
      record.normalize(namespace)
      record.validate(namespace)
      classifyRecord(record)
      record.save(enrichmentFolder, resultHash)
    }
    log.debug("Classifying all records finished with ${redRecords.size()} red, ${yellowRecords.size()} yellow and ${greenRecords.size()} green records.")
  }


  synchronized void classifyRecord(Record record){
    record.setDisplayTitle()
    String key = record.displayTitle.concat(record.uid)
    List<String> values = [
        StringUtils.isEmpty(record.displayTitle) ? "" : (
            record.displayTitle.size() > 100 ? record.displayTitle.substring(0,100).concat("...") : record.displayTitle),
        valOrEmpty(record.zdbIntegrationUrl),
        valOrEmpty(record.zdbId),
        valOrEmpty(record.onlineIdentifier),
        valOrEmpty(record.uid)
    ]
    if (record.isValid()){
      if (needsPreciseClassification && record.multiFields.get("titleUrl").isCorrect(record.publicationType) &&
          record.duplicates.isEmpty() && (!record.publicationType.equals("serial") || record.zdbIntegrationUrl != null)
          && !record.hasFlagOfColour(RecordFlag.Colour.YELLOW)){
        greenRecords.put(key, values)
        yellowRecords.remove(key)
        redRecords.remove(key)
      }
      else{
        // Note: if this process is not triggered by an UI with a view of processed results, there is no need and no use
        // in showing differences between green and yellow records. Hence, they are all classified as yellow.
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
    if (val instanceof AbstractIdentifier){
      return val.toReducedString()
    }
    return val.toString()
  }

}
