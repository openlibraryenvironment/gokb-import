package ygor

import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.SendTitlesThreadGokb
import de.hbznrw.ygor.processing.YgorFeedback
import de.hbznrw.ygor.tools.FileToolkit
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import org.springframework.web.servlet.support.RequestContextUtils
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
class StatisticController implements ControllersHelper{

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
    SessionService.setSessionDuration(request, 3600)
    render(
        view: 'index',
        model: [currentView: 'statistic']
    )
  }

  def show(){
    SessionService.setSessionDuration(request, 28800)
    String resultHash = request.parameterMap.resultHash[0]
    if (enrichmentsUploading.contains(resultHash)){
      return null
    }
    enrichmentsUploading.add(resultHash)
    String originHash = request.parameterMap.originHash[0]
    Enrichment enrichment = getEnrichment(resultHash)
    enrichmentsUploading.remove(resultHash)
    render(
        view: 'show',
        model: [
            originHash      : originHash,
            resultHash      : resultHash,
            currentView     : 'statistic',
            ygorVersion     : enrichment.ygorVersion,
            date            : enrichment.lastProcessingDate,
            filename        : enrichment.originName,
            greenRecords    : recordsPrivate(resultHash, RecordFlag.Colour.GREEN.toString(), 0, 10, 1),
            yellowRecords   : recordsPrivate(resultHash, RecordFlag.Colour.YELLOW.toString(), 0, 10, 1),
            redRecords      : recordsPrivate(resultHash, RecordFlag.Colour.RED.toString(), 0, 10, 1),
            status          : enrichment.status,
            packageName     : enrichment.packageName,
            runningJobIds   : runningUploadJobs.keySet(),
            finishedJobIds  : finishedUploadJobs.keySet(),
            titlesUploaded  : true == enrichment.hasBeenUploaded.get(Enrichment.FileType.TITLES),
            packageUploaded : true == enrichment.hasBeenUploaded.get(Enrichment.FileType.PACKAGE)
        ]
    )
  }

  def records(){
    render recordsPrivate(params.resultHash, params.colour, params.int('start'), params.int('length'), params.int('draw')) as JSON
  }


  synchronized private def recordsPrivate(String resultHash, String colour, int start, int size, int draw){
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
            String title = StringUtils.isEmpty(value.get(0)) ? message(code: 'missing') : value.get(0)
            String uid = value.get(4)
            if (!(StringUtils.isEmpty(uid)) && !value[0].contains('<')){
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
    result
  }


  def cancel(){
    // restore record from dataContainer
    String resultHash = params['resultHash']
    Enrichment enrichment = getEnrichment(resultHash)
    render(
        view: 'show',
        model: [
            resultHash      : resultHash,
            currentView     : 'statistic',
            greenRecords    : recordsPrivate(resultHash, RecordFlag.Colour.GREEN.toString(), 0, 10, 1),
            yellowRecords   : recordsPrivate(resultHash, RecordFlag.Colour.YELLOW.toString(), 0, 10, 1),
            redRecords      : recordsPrivate(resultHash, RecordFlag.Colour.RED.toString(), 0, 10, 1),
            ygorVersion     : enrichment.ygorVersion,
            date            : enrichment.lastProcessingDate,
            filename        : enrichment.originName,
            packageName     : enrichment.packageName,
            runningJobIds   : runningUploadJobs.keySet(),
            finishedJobIds  : finishedUploadJobs.keySet(),
            titlesUploaded  : true == enrichment.hasBeenUploaded.get(Enrichment.FileType.TITLES),
            packageUploaded : true == enrichment.hasBeenUploaded.get(Enrichment.FileType.PACKAGE)
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
            resultHash      : resultHash,
            currentView     : 'statistic',
            greenRecords    : recordsPrivate(resultHash, RecordFlag.Colour.GREEN.toString(), 0, 10, 1),
            yellowRecords   : recordsPrivate(resultHash, RecordFlag.Colour.YELLOW.toString(), 0, 10, 1),
            redRecords      : recordsPrivate(resultHash, RecordFlag.Colour.RED.toString(), 0, 10, 1),
            ygorVersion     : enrichment.ygorVersion,
            date            : enrichment.lastProcessingDate,
            filename        : enrichment.originName,
            packageName     : enrichment.packageName,
            runningJobIds   : runningUploadJobs.keySet(),
            finishedJobIds  : finishedUploadJobs.keySet(),
            titlesUploaded  : true == enrichment.hasBeenUploaded.get(Enrichment.FileType.TITLES),
            packageUploaded : true == enrichment.hasBeenUploaded.get(Enrichment.FileType.PACKAGE)
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
        resultHash          : resultHash,
        record              : record,
        zdbEnrichmentActive : enrichment.isZdbIntegrated
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
        if (key.equals("publicationType")){
          record.publicationType = multiField.revised.toLowerCase()
        }
        record.validateContent(namespace)
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
              enrichment.setStatusByCallback(Enrichment.ProcessingState.FINISHED)
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
    SessionService.setSessionDuration(request, 3600)
    request.session.lastUpdate = [:]
    enrichmentService.deleteFileAndFormat(getCurrentEnrichment())
    redirect(
        controller: 'Enrichment',
        view: 'process'
    )
  }


  def correctFile = {
    SessionService.setSessionDuration(request, 3600)
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
    SessionService.setSessionDuration(request, 7200)
    def en = getCurrentEnrichment()
    if (en){
      def result = enrichmentService.getFile(en, Enrichment.FileType.PACKAGE)
      render(file: result, fileName: "${en.resultName}.package.json")
    }
    else{
      noValidEnrichment()
    }
  }


  def downloadIntegratedPackageFile = {
    SessionService.setSessionDuration(request, 72000)
    def en = getCurrentEnrichment()
    if (en){
      def result = enrichmentService.getFile(en, Enrichment.FileType.PACKAGE_WITH_TITLEDATA)
      render(file: result, fileName: "${en.resultName}.packageWithTitleData.json")
    }
    else{
      noValidEnrichment()
    }
  }


  def downloadTitlesFile = {
    SessionService.setSessionDuration(request, 72000)
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
    SessionService.setSessionDuration(request, 72000)
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
    YgorFeedback ygorFeedback = new YgorFeedback(YgorFeedback.YgorProcessingStatus.RUNNING, "Send package file.",
        this.getClass(), null,        null, null, null)
    SessionService.setSessionDuration(request, 72000)
    sendFile(Enrichment.FileType.PACKAGE, ygorFeedback)
  }


  def sendIntegratedPackageFile = {
    YgorFeedback ygorFeedback = new YgorFeedback(YgorFeedback.YgorProcessingStatus.RUNNING, "Send integrated package file.",
        this.getClass(), null,        null, null, null)
    SessionService.setSessionDuration(request, 72000)
    sendFile(Enrichment.FileType.PACKAGE_WITH_TITLEDATA, ygorFeedback)
  }


  def sendTitlesFile = {
    YgorFeedback ygorFeedback = new YgorFeedback(YgorFeedback.YgorProcessingStatus.RUNNING, "Send titles file.",
        this.getClass(), null,        null, null, null)
    SessionService.setSessionDuration(request, 72000)
    sendFile(Enrichment.FileType.TITLES, ygorFeedback)
  }


  private void sendFile(Enrichment.FileType fileType, YgorFeedback ygorFeedback){
    gokbUsername = params.gokbUsername
    gokbPassword = params.gokbPassword
    def enrichment = getCurrentEnrichment()
    if (enrichment == null){
      flash.error = message(code: 'error.enrichment.missing').toString().concat("<br>")
    }
    if (!enrichment.hasBeenUploaded.get(fileType)){
      String uri = getDestinationUri(grailsApplication, fileType, enrichment.addOnly)
      UploadJob uploadJob
      if (fileType.equals(Enrichment.FileType.TITLES)){
        SendTitlesThreadGokb sendTitlesThread = new SendTitlesThreadGokb(enrichment, uri, gokbUsername, gokbPassword,
            RequestContextUtils.getLocale(request).toString(), ygorFeedback)
        uploadJob = new UploadJob(Enrichment.FileType.TITLES, sendTitlesThread, ygorFeedback)
      }
      else if (fileType.equals(Enrichment.FileType.PACKAGE)){
        SendPackageThreadGokb sendPackageThread = new SendPackageThreadGokb(enrichment, uri, gokbUsername, gokbPassword,
            false, ygorFeedback)
        uploadJob = new UploadJob(Enrichment.FileType.PACKAGE, sendPackageThread, ygorFeedback)
      }
      else if (fileType.equals(Enrichment.FileType.PACKAGE_WITH_TITLEDATA)){
        SendPackageThreadGokb sendPackageThread = new SendPackageThreadGokb(enrichment, uri, gokbUsername, gokbPassword,
            true, ygorFeedback)
        uploadJob = new UploadJob(Enrichment.FileType.PACKAGE_WITH_TITLEDATA, sendPackageThread, ygorFeedback)
      }
      if (uploadJob != null){
        runningUploadJobs.put(uploadJob.uuid, uploadJob)
        uploadJob.start()
        enrichment.hasBeenUploaded.put(fileType, true)
      }
      Thread.sleep(750)
    }
    render(
        view         : 'show',
        model: [
            originHash      : enrichment.originHash,
            resultHash      : enrichment.resultHash,
            currentView     : 'statistic',
            ygorVersion     : enrichment.ygorVersion,
            date            : enrichment.lastProcessingDate,
            filename        : enrichment.originName,
            greenRecords    : recordsPrivate(enrichment.resultHash, RecordFlag.Colour.GREEN.toString(), 0, 10, 1),
            yellowRecords   : recordsPrivate(enrichment.resultHash, RecordFlag.Colour.YELLOW.toString(), 0, 10, 1),
            redRecords      : recordsPrivate(enrichment.resultHash, RecordFlag.Colour.RED.toString(), 0, 10, 1),
            status          : enrichment.status.toString(),
            packageName     : enrichment.packageName,
            dataType        : fileType,
            runningJobIds   : runningUploadJobs.keySet(),
            finishedJobIds  : finishedUploadJobs.keySet(),
            titlesUploaded  : true == enrichment.hasBeenUploaded.get(Enrichment.FileType.TITLES),
            packageUploaded : true == enrichment.hasBeenUploaded.get(Enrichment.FileType.PACKAGE),
            ygorFeedback    : ygorFeedback
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
      render '{"status":"' + uploadJob.getStatus() + '"}'
    }
  }


  def refreshJobStatus = {
    UploadJob uploadJob = runningUploadJobs.get(params.uid)
    if (uploadJob != null){
      uploadJob.refreshStatus()
      if (uploadJob.getStatus().toString() in ['FINISHED_UNDEFINED', 'SUCCESS', 'ERROR']){
        runningUploadJobs.remove(params.uid)
        finishedUploadJobs.put(params.uid, uploadJob)
      }
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


  def ajaxGetStatus = {
    def en = getCurrentEnrichment()
    if (en){
      render '{"status":"' + en.getStatus() + '", "message":"' + en.getMessage() + '", "progress":' + en.getProgress().round() + '}'
    }
  }


  Enrichment getCurrentEnrichment(){
    return EnrichmentController.getCurrentEnrichmentStatic(enrichmentService, request)
  }


  void noValidEnrichment(){
    flash.info = null
    flash.warning = message(code: 'warning.fileNotFound')
    flash.error = null
    redirect(controller: 'Enrichment', action: 'process')
  }


  def setFlag() {
    Record record
    try{
      Enrichment enrichment = getEnrichment(params.resultHash)
      String namespace = enrichment.dataContainer.info.namespace_title_id
      if (enrichment){
        record = enrichment.dataContainer.records.get(params.record.uid)
        for (def flagId in params.flags){
          RecordFlag flag = record.getFlag(flagId.key)
          flag.setColour(RecordFlag.Colour.valueOf(flagId.value))
        }
        record.validateContent(namespace)
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
