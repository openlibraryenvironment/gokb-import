package ygor

import com.google.gson.Gson
import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.SendTitlesThreadGokb
import de.hbznrw.ygor.tools.FileToolkit
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
  Map<String, UploadJob> uploadJobs = [:]

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
            originHash    : originHash,
            resultHash    : resultHash,
            currentView   : 'statistic',
            ygorVersion   : enrichment.ygorVersion,
            date          : enrichment.date,
            filename      : enrichment.originName,
            greenRecords  : enrichment.greenRecords,
            yellowRecords : enrichment.yellowRecords,
            redRecords    : enrichment.redRecords,
            status        : enrichment.status,
            packageName   : enrichment.packageName,
            jobIds        : uploadJobs.keySet()
        ]
    )
  }


  def records(){
    String resultHash = request.parameterMap.resultHash[0]
    String colour = request.parameterMap.colour[0]
    int pageIndex = Integer.valueOf(request.parameterMap.start[0])
    int size = Integer.valueOf(request.parameterMap.length[0])
    int draw = Integer.valueOf(request.parameterMap.draw[0])
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
    List resultData = []
    if (records != null){
      int from = pageIndex
      int to = pageIndex + size
      int i = 0
      records.forEach(){key, value ->
        if (i >= from && i < to){
          if (value.size() > 4){
            String title = value.getAt(0)
            String uid = value.getAt(4)
            if (!(StringUtils.isEmpty(title)) && !(StringUtils.isEmpty(uid))){
              value[0] = "<a href=\"/ygor/statistic/edit/".concat(uid).concat("?resultHash=").concat(resultHash)
                  .concat("\">").concat(title).concat("</a>")
            }
          }
          if (value.size() > 1){
            String linkValue = value.getAt(1)
            if (!(StringUtils.isEmpty(linkValue))){
              value[1] = "<a class=\"link-icon\" href=\"".concat(linkValue).concat("\"/>")
            }
          }
          resultData.add(value)
          i++
        }
      }
    }
    render "{\"recordsTotal\":${records.size()},\"recordsFiltered\":${records.size()},\"draw\":${pageIndex},\"data\":".concat(new Gson().toJson(resultData)).concat("}")
  }


  def cancel(){
    // restore record from dataContainer
    String resultHash = params['resultHash']
    Enrichment enrichment = getEnrichment(resultHash)
    render(
        view: 'show',
        model: [
            resultHash    : resultHash,
            currentView   : 'statistic',
            greenRecords  : enrichment.greenRecords,
            yellowRecords : enrichment.yellowRecords,
            redRecords    : enrichment.redRecords,
            ygorVersion   : enrichment.ygorVersion,
            date          : enrichment.date,
            filename      : enrichment.originName,
            packageName   : enrichment.packageName
        ]
    )
  }


  def save(){
    // write record into dataContainer
    String resultHash = request.parameterMap['resultHash'][0]
    Enrichment enrichment = getEnrichment(resultHash)
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
            resultHash    : resultHash,
            currentView   : 'statistic',
            greenRecords  : enrichment.greenRecords,
            yellowRecords : enrichment.yellowRecords,
            redRecords    : enrichment.redRecords,
            ygorVersion   : enrichment.ygorVersion,
            date          : enrichment.date,
            filename      : enrichment.originName,
            packageName   : enrichment.packageName
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
    if (enrichment){
      def response = []
      String uri = getDestinationUri(fileType)
      UploadJob uploadJob
      if (fileType.equals(Enrichment.FileType.TITLES)){
        SendTitlesThreadGokb sendTitlesThread = new SendTitlesThreadGokb(enrichment, uri, gokbUsername, gokbPassword)
        uploadJob = new UploadJob(Enrichment.FileType.TITLES, sendTitlesThread)
      }
      else if (fileType.equals(Enrichment.FileType.PACKAGE)){
        SendPackageThreadGokb sendPackageThread = new SendPackageThreadGokb(grailsApplication, enrichment, uri, gokbUsername, gokbPassword)
        uploadJob = new UploadJob(Enrichment.FileType.PACKAGE, sendPackageThread)
        // sendPackageThread.getGokbJobId()
      }
      if (uploadJob != null){
        uploadJobs.put(uploadJob.uuid, uploadJob)
        uploadJob.start()
        /*flash.info = []
        flash.warning = []
        List errorList = []
        def total = 0
        def errors = 0
        log.debug("sendFile response: ${response}")
        if (response.info){
          log.debug("json class: ${response.info.class}")
          def info_objects = response.info.results
          info_objects[0].each{ robj ->
            log.debug("robj: ${robj}")
            if (robj.result == 'ERROR'){
              errorList.add(robj.message)
              errors++
            }
            total++
          }
          flash.info = "Total: ${total}, Errors: ${errors}"
          flash.error = errorList
        }*/
        render(
            view         : 'show',
            model: [
                originHash   : enrichment.originHash,
                resultHash   : enrichment.resultHash,
                currentView  : 'statistic',
                ygorVersion  : enrichment.ygorVersion,
                date         : enrichment.date,
                filename     : enrichment.originName,
                greenRecords : enrichment.greenRecords,
                yellowRecords: enrichment.yellowRecords,
                redRecords   : enrichment.redRecords,
                status       : enrichment.status.toString(),
                packageName  : enrichment.packageName,
                dataType     : fileType,
                jobIds       : uploadJobs.keySet()
            ]
        )
      }
    }
  }


  def removeJobId = {
    uploadJobs.remove(params.uid)
    render '{}'
  }


  def getJobStatus = {
    UploadJob uploadJob = uploadJobs.get(params.uid)
    if (uploadJob == null){
      render '{}'
    }
    else{
      uploadJob.refreshStatus()
      render '{"status":"' + uploadJob.status + '"}'
    }
  }


  def getJobProgress = {
    UploadJob uploadJob = uploadJobs.get(params.uid)
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
    UploadJob uploadJob = uploadJobs.get(params.uid)
    if (uploadJob != null){
      results.putAll(uploadJob.getResultsTable())
    }
    StringJoiner stringJoiner = new StringJoiner(",", "[", "]")
    for (def entry in results){
      stringJoiner.add('{"'.concat(entry.key).concat('":"').concat(entry.value.toString()).concat('"}'))
    }
    render stringJoiner.toString()
  }


  private String getDestinationUri(fileType){
    def uri = fileType.equals(Enrichment.FileType.PACKAGE) ?
        grailsApplication.config.gokbApi.xrPackageUri :
        (fileType.equals(Enrichment.FileType.TITLES) ?
            grailsApplication.config.gokbApi.xrTitleUri :
            null
        )
    return uri.concat("?async=true")
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
