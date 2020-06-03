package ygor

import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.SendTitlesThreadGokb
import de.hbznrw.ygor.tools.FileToolkit
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import ygor.field.FieldKeyMapping
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier
import ygor.identifier.DoiIdentifier
import ygor.identifier.EzbIdentifier
import ygor.identifier.OnlineIdentifier
import ygor.identifier.PrintIdentifier
import ygor.identifier.ZdbIdentifier

@Log4j
class StatisticController{

  def grailsApplication
  static scope = "session"
  static FileFilter DIRECTORY_FILTER = new FileFilter(){
    @Override
    boolean accept(File file){
      return file.isDirectory()
    }
  }
  EnrichmentService enrichmentService
  Set<String> enrichmentsUploading = []
  String gokbUsername
  String gokbPassword
  Map<String, UploadJob> runningUploadJobs = [:]
  Map<String, UploadJob> finishedUploadJobs = [:]

  def index(){
    render(
        view: 'index',
        model: [currentView: 'statistic']
    )
  }

  def show(){
    String resultHash = request.parameterMap.resultHash[0]
    if (enrichmentsUploading.contains(resultHash)){
      return null
    }
    enrichmentsUploading.add(resultHash.toString())
    String originHash = request.parameterMap.originHash[0]
    log.info('show enrichment ' + resultHash)
    Enrichment enrichment = getEnrichment(resultHash)
    enrichmentsUploading.remove(resultHash.toString())
    render(
        view: 'show',
        model: [
            originHash     : originHash,
            resultHash     : resultHash,
            currentView    : 'statistic',
            ygorVersion    : enrichment.ygorVersion,
            date           : enrichment.date,
            filename       : enrichment.originName,
            greenRecords   : enrichment.greenRecords,
            yellowRecords  : enrichment.yellowRecords,
            redRecords     : enrichment.redRecords,
            status         : enrichment.status,
            packageName    : enrichment.packageName,
            runningJobIds  : runningUploadJobs.keySet(),
            finishedJobIds : finishedUploadJobs.keySet()
        ]
    )
  }


  def records(){
    log.debug("${params}")
    String resultHash = params.resultHash
    String colour = params.colour
    int start = params.int('start')
    int size = params.int('length')
    int draw = params.int('draw')
    Map records
    Enrichment enrichment = getCurrentEnrichment()
    switch (colour){
      case RecordFlag.Colour.RED.toString():
        records = enrichment.redRecords
        break
      case RecordFlag.Colour.YELLOW.toString():
        records = enrichment.yellowRecords
        break
      case RecordFlag.Colour.GREEN.toString():
        records = enrichment.greenRecords
        break
      default:
        records = null
    }
    def result = [recordsTotal: records.size(), recordsFiltered: records.size(), draw: draw, displayStart: start, data: []]

    if (records != null){
      int from = start
      int to = from + size
      int i = 0
      records.each { key, value ->
        // log.debug("Processing value ${value}")
        if (i >= from && i < to){
          def cols = value
          if (value.size() > 4){
            String title = value.get(0)
            String uid = value.get(4)
            if (!(StringUtils.isEmpty(title)) && !(StringUtils.isEmpty(uid)) && !value[0].contains('<')){
              StringWriter sw = new StringWriter()
              sw.write('<a href="/ygor/statistic/edit/')
              sw.write(uid)
              sw.write('?resultHash=')
              sw.write(resultHash)
              sw.write('">')
              sw.write(title)
              sw.write('</a>')
              cols[0] = sw.toString()
            }
          }
          if (value.size() > 1 && !value[1].contains('<')){
            String linkValue = value.get(1)
            if (!(StringUtils.isEmpty(linkValue))){
              StringWriter sw = new StringWriter()
              sw.write('<a class="link-icon" href="')
              sw.write(linkValue)
              sw.write('"></a>')
              cols[1] = sw.toString()
            }
          }
          result.data.add(cols)
        }
        i++
      }
    }
    log.debug("New data: ${result}")
    render result as JSON
  }


  def cancel(){
    // restore record from dataContainer
    String resultHash = params['resultHash']
    Enrichment enrichment = getEnrichment(resultHash)
    render(
        view: 'show',
        model: [
            resultHash     : resultHash,
            currentView    : 'statistic',
            greenRecords   : enrichment.greenRecords,
            yellowRecords  : enrichment.yellowRecords,
            redRecords     : enrichment.redRecords,
            ygorVersion    : enrichment.ygorVersion,
            date           : enrichment.date,
            filename       : enrichment.originName,
            packageName    : enrichment.packageName,
            runningJobIds  : runningUploadJobs.keySet(),
            finishedJobIds : finishedUploadJobs.keySet()
        ]
    )
  }


  def save(){
    // write record into dataContainer
    String resultHash = request.parameterMap['resultHash'][0]
    Enrichment enrichment = getEnrichment(resultHash)
    enrichment.hasBeenUploaded.put(Enrichment.FileType.TITLES, false)
    enrichment.hasBeenUploaded.put(Enrichment.FileType.PACKAGE, false)
    String enrichmentFolder = enrichment.sessionFolder.absolutePath.concat(File.separator).concat(resultHash).concat(File.separator)
    Record record = Record.load(enrichmentFolder, resultHash, params['record.uid'], enrichment.mappingsContainer)
    for (def field in params['fieldschanged']){
      MultiField multiField = record.multiFields.get(field.key)
      FieldKeyMapping fkm = enrichment.mappingsContainer.getMapping(field.key, MappingsContainer.YGOR)
      switch (field.key){
        // TODO : replace hard coded YGOR keys
        case "zdbId":
          upsertRecordIdentifier(enrichment, record, record.zdbId, ZdbIdentifier.class, fkm, field.value)
          break
        case "ezbId":
          upsertRecordIdentifier(enrichment, record, record.ezbId, EzbIdentifier.class, fkm, field.value)
          break
        case "doiId":
          upsertRecordIdentifier(enrichment, record, record.doiId, DoiIdentifier.class, fkm, field.value)
          break
        case "onlineIdentifier":
          upsertRecordIdentifier(enrichment, record, record.onlineIdentifier, OnlineIdentifier.class, fkm, field.value)
          break
        case "printIdentifier":
          upsertRecordIdentifier(enrichment, record, record.printIdentifier, PrintIdentifier.class, fkm, field.value)
          break
      }
      multiField.revised = field.value
    }
    record.save(enrichmentFolder, resultHash)
    enrichment.classifyRecord(record)
    // TODO: sort records in case of having changed the record's title
    render(
        view: 'show',
        model: [
            resultHash     : resultHash,
            currentView    : 'statistic',
            greenRecords   : enrichment.greenRecords,
            yellowRecords  : enrichment.yellowRecords,
            redRecords     : enrichment.redRecords,
            ygorVersion    : enrichment.ygorVersion,
            date           : enrichment.date,
            filename       : enrichment.originName,
            packageName    : enrichment.packageName,
            runningJobIds  : runningUploadJobs.keySet(),
            finishedJobIds : finishedUploadJobs.keySet()
        ]
    )
  }


  private void upsertRecordIdentifier(Enrichment enrichment, Record record, AbstractIdentifier identifier, Class clazz,
                                      FieldKeyMapping fkm, String id){
    if (identifier != null){
      enrichment.dataContainer.removeRecordFromIdSortation(identifier, record)
      identifier.identifier = id
    }
    else{
      identifier = clazz.newInstance(id, fkm)
    }
    enrichment.dataContainer.addRecordToIdSortation(identifier, record)
  }


  def edit(){
    String resultHash = request.parameterMap['resultHash'][0]
    Enrichment enrichment = getEnrichment(resultHash)
    String enrichmentFolder = enrichment.sessionFolder.absolutePath.concat(File.separator).concat(resultHash).concat(File.separator)
    Record record = Record.load(enrichmentFolder, resultHash, params.id, enrichment.mappingsContainer)
    [
        resultHash: resultHash,
        record    : record
    ]
  }


  def update(){
    def resultHash = params.resultHash
    def value = params.value
    def key = params.key
    Record record

    try{
      Enrichment enrichment = getEnrichment(resultHash)
      String namespace = enrichment.dataContainer.info.namespace_title_id
      if (enrichment){
        String enrichmentFolder = enrichment.sessionFolder.absolutePath.concat(File.separator).concat(resultHash).concat(File.separator)
        record = Record.load(enrichmentFolder, resultHash, params['uid'], enrichment.mappingsContainer)
        MultiField multiField = record.multiFields.get(key)
        multiField.revised = value.trim()
        record.validate(namespace)
        record.save(enrichmentFolder, resultHash)
      }
      else{
        throw new EmptyStackException()
      }
    }
    catch (Exception e){
      log.error(e.getMessage())
      log.error(e.getStackTrace())
    }
    render(groovy.json.JsonOutput.toJson([
        record    : record.asStatisticsJson(),
        resultHash: resultHash
    ]))
  }


  private Enrichment getEnrichment(String resultHash){
    // get enrichment if existing
    def enrichments = enrichmentService.getSessionEnrichments()
    if (null != enrichments.get(resultHash)){
      return enrichments.get(resultHash)
    }
    // else get new Enrichment
    File uploadLocation = new File(grailsApplication.config.ygor.uploadLocation)
    for (def dir in uploadLocation.listFiles(DIRECTORY_FILTER)){
      for (def file in dir.listFiles()){
        if (file.isDirectory() && file.getName() == resultHash){
          for (def subFile in file.listFiles()){
            if (subFile.getName() == resultHash){
              log.info("getting enrichment from file... ".concat(resultHash))
              Enrichment enrichment = Enrichment.fromJsonFile(subFile, false)
              enrichmentService.addSessionEnrichment(enrichment)
              log.info("getting enrichment from file... ".concat(resultHash).concat(" finished."))
              return enrichment
            }
          }
        }
      }
    }
    return null
  }


  def deleteFile = {
    request.session.lastUpdate = [:]
    enrichmentService.deleteFileAndFormat(getCurrentEnrichment())
    redirect(
        controller: 'Enrichment',
        view: 'process'
    )
  }


  def correctFile = {
    Enrichment enrichment = getCurrentEnrichment()
    enrichmentService.deleteFileAndFormat(enrichment)
    redirect(
        controller: 'Enrichment',
        view: 'process',
        model: [
            enrichment : enrichment,
            currentView: 'process'
        ]
    )
  }


  def downloadPackageFile = {
    def en = getCurrentEnrichment()
    if (en){
      def result = enrichmentService.getFile(en, Enrichment.FileType.PACKAGE)
      render(file: result, fileName: "${en.resultName}.package.json")
    }
    else{
      noValidEnrichment()
    }
  }


  def downloadTitlesFile = {
    def en = getCurrentEnrichment()
    if (en){
      def result = enrichmentService.getFile(en, Enrichment.FileType.TITLES)
      render(file: result, fileName: "${en.resultName}.titles.json")
    }
    else{
      noValidEnrichment()
    }
  }


  def downloadRawFile = {
    def en = getCurrentEnrichment()
    if (en){
      File zip = FileToolkit.zipFiles(en.sessionFolder, en.resultHash);
      render(file: zip, fileName: "${en.resultName}.raw.zip", contentType: "application/zip")
    }
    else{
      noValidEnrichment()
    }
  }


  def sendPackageFile = {
    sendFile(Enrichment.FileType.PACKAGE)
  }



  def sendTitlesFile = {
    sendFile(Enrichment.FileType.TITLES)
  }


  private void sendFile(Enrichment.FileType fileType){
    gokbUsername = params.gokbUsername
    gokbPassword = params.gokbPassword
    def enrichment = getCurrentEnrichment()
    if (enrichment && !enrichment.hasBeenUploaded.get(fileType)){
      def response = []
      String uri = getDestinationUri(fileType, enrichment.addOnly)
      UploadJob uploadJob
      if (fileType.equals(Enrichment.FileType.TITLES)){
        SendTitlesThreadGokb sendTitlesThread = new SendTitlesThreadGokb(enrichment, uri, gokbUsername, gokbPassword)
        uploadJob = new UploadJob(Enrichment.FileType.TITLES, sendTitlesThread)
      }
      else if (fileType.equals(Enrichment.FileType.PACKAGE)){
        SendPackageThreadGokb sendPackageThread = new SendPackageThreadGokb(grailsApplication, enrichment, uri, gokbUsername, gokbPassword)
        uploadJob = new UploadJob(Enrichment.FileType.PACKAGE, sendPackageThread)
      }
      if (uploadJob != null){
        runningUploadJobs.put(uploadJob.uuid, uploadJob)
        uploadJob.start()
        enrichment.hasBeenUploaded.put(fileType, true)
      }
    }
    render(
        view         : 'show',
        model: [
            originHash     : enrichment.originHash,
            resultHash     : enrichment.resultHash,
            currentView    : 'statistic',
            ygorVersion    : enrichment.ygorVersion,
            date           : enrichment.date,
            filename       : enrichment.originName,
            greenRecords   : enrichment.greenRecords,
            yellowRecords  : enrichment.yellowRecords,
            redRecords     : enrichment.redRecords,
            status         : enrichment.status.toString(),
            packageName    : enrichment.packageName,
            dataType       : fileType,
            runningJobIds  : runningUploadJobs.keySet(),
            finishedJobIds : finishedUploadJobs.keySet()
        ]
    )
  }


  def removeJobId = {
    runningUploadJobs.remove(params.uid)
    finishedUploadJobs.remove(params.uid)
    render '{}'
  }


  def getJobStatus = {
    UploadJob uploadJob = runningUploadJobs.get(params.uid)
    if (uploadJob == null){
      uploadJob = finishedUploadJobs.get(params.uid)
    }
    if (uploadJob == null){
      render '{}'
    }
    else{
      render '{"status":"' + uploadJob.status + '"}'
    }
  }


  def refreshJobStatus = {
    UploadJob uploadJob = runningUploadJobs.get(params.uid)
    uploadJob.refreshStatus()
    if (uploadJob.status.toString() in ['FINISHED_UNDEFINED', 'SUCCESS', 'ERROR']){
      runningUploadJobs.remove(params.uid)
      finishedUploadJobs.put(params.uid, uploadJob)
    }
    render '{}'
  }


  def getJobProgress = {
    UploadJob uploadJob = runningUploadJobs.get(params.uid)
    if (uploadJob == null){
      render '{}'
    }
    else{
      uploadJob.updateCount()
      render '{"count":"' + uploadJob.getCount() + '"}'
    }
  }


  def getResultsTable = {
    def results = [:]
    UploadJob uploadJob = finishedUploadJobs.get(params.uid)
    if (uploadJob != null){
      results.putAll(uploadJob.getResultsTable())
    }
    StringJoiner stringJoiner = new StringJoiner(",", "[", "]")
    for (def entry in results){
      stringJoiner.add('{"'.concat(entry.key).concat('":"').concat(entry.value.toString()).concat('"}'))
    }
    render stringJoiner.toString()
  }


  private String getDestinationUri(fileType, boolean addOnly){
    def uri = fileType.equals(Enrichment.FileType.PACKAGE) ?
        grailsApplication.config.gokbApi.xrPackageUri :
        (fileType.equals(Enrichment.FileType.TITLES) ?
            grailsApplication.config.gokbApi.xrTitleUri :
            null
        )
    uri = uri.concat("?async=true")
    if (addOnly){
      uri = uri.concat("&addOnly=true")
    }
    return uri
  }


  def ajaxGetStatus = {
    def en = getCurrentEnrichment()
    if (en){
      render '{"status":"' + en.getStatus() + '", "message":"' + en.getMessage() + '", "progress":' + en.getProgress().round() + '}'
    }
  }


  Enrichment getCurrentEnrichment(){
    def hash = (String) request.parameterMap['resultHash'][0]
    def enrichments = enrichmentService.getSessionEnrichments()
    Enrichment result = enrichments[hash]
    if (null == result){
      result = enrichments.get("${hash}")
    }
    result
  }


  HashMap getCurrentFormat(){
    def hash = (String) request.parameterMap['originHash'][0]
    enrichmentService.getSessionFormats().get("${hash}")
  }


  void noValidEnrichment(){
    flash.info = null
    flash.warning = message(code: 'warning.fileNotFound')
    flash.error = null
    redirect(controller: 'Enrichment', action: 'process')
  }


  def setFlag() {
    def record
    try{
      Enrichment enrichment = getEnrichment(params.resultHash)
      String namespace = enrichment.dataContainer.info.namespace_title_id
      if (enrichment){
        record = enrichment.dataContainer.records.get(params.record.uid)
        for (def flagId in params.flags){
          RecordFlag flag = record.getFlag(flagId.key)
          flag.setColour(RecordFlag.Colour.valueOf(flagId.value))
        }
        record.validate(namespace)
        enrichment.classifyRecord(record)
      }
    }
    catch (Exception e){
      log.error(e.getMessage())
      log.error(e.getStackTrace())
    }
    render(
        view: 'edit',
        model: [resultHash: params.resultHash,
                record    : record
        ]
    )
  }
}
