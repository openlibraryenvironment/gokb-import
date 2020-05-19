package ygor

import com.google.gson.Gson
import de.hbznrw.ygor.export.GokbExporter
import de.hbznrw.ygor.processing.SendPackageThread
import de.hbznrw.ygor.processing.SendTitlesThread
import de.hbznrw.ygor.tools.FileToolkit
import grails.converters.JSON
import groovy.util.logging.Log4j
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
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

import java.util.regex.Matcher
import java.util.regex.Pattern

@Log4j
class StatisticController{

  static scope = "session"
  static FileFilter DIRECTORY_FILTER = new FileFilter(){
    @Override
    boolean accept(File file){
      return file.isDirectory()
    }
  }
  final static Pattern INT_FROM_MESSAGE_REGEX = Pattern.compile("with (\\d+) TIPPs")
  def grailsApplication
  EnrichmentService enrichmentService
  Set<String> enrichmentsUploading = []
  String gokbUsername
  String gokbPassword

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
            packageName   : enrichment.packageName
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
        SendTitlesThread sendTitlesThread = new SendTitlesThread(enrichment, uri, gokbUsername, gokbPassword)
        uploadJob = new UploadJob(Enrichment.FileType.TITLES, sendTitlesThread)
      }
      else if (fileType.equals(Enrichment.FileType.PACKAGE)){
        SendPackageThread sendPackageThread = new SendPackageThread(enrichment, uri, gokbUsername, gokbPassword)
        uploadJob = new UploadJob(Enrichment.FileType.PACKAGE, sendPackageThread)
      }
      if (uploadJob != null){
        uploadJob.start()
        flash.info = []
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
        }
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
                jobId        : getJobId(response)
            ]
        )
      }
    }
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


  private String getJobId(ArrayList response){
    for (def responseItem in response){
      for (def value in responseItem.values()){
        return String.valueOf(value.get("job_id"))
      }
    }
  }


  def getJobInfo = {
    if (gokbUsername == null || gokbPassword == null){
      return null
    }
    if (params.type == Enrichment.FileType.PACKAGE.toString()){
      getPackageJobInfo()
    }
    else{
      getTitlesJobInfo()
    }
  }


  private void getTitlesJobInfo(){

  }

  private void getPackageJobInfo(){
    def uri = grailsApplication.config.gokbApi.xrJobInfo.concat(params.jobId)
    def http = new HTTPBuilder(uri)
    Map<String, Object> result = new HashMap<>()
    result["jobId"] = params.jobId
    http.auth.basic gokbUsername, gokbPassword

    http.request(Method.GET, ContentType.JSON){ req ->
      response.success = { response, resultMap ->
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          if (response.status < 400){
            if (resultMap.result.equals("ERROR")){
              result.put('error', resultMap.message)
            }
            else{
              result.putAll(getResponseSorted(resultMap))
            }
          }
          else{
            result.put('warning': resultMap)
          }
        }
        else{
          result.putAll(handleAuthenticationError(response))
        }
      }
      response.failure = { response, resultMap ->
        log.error("GOKb server response: ${response.statusLine}")
        if (response.headers.'Content-Type' == 'application/json;charset=UTF-8'){
          result.put('error': resultMap)
        }
        else{
          result.putAll(handleAuthenticationError(response))
        }
      }
      response.'401' = { resp ->
        result.putAll(handleAuthenticationError(resp))
      }
    }
    render result as JSON
  }


  private Map handleAuthenticationError(response){
    log.error("GOKb server response: ${response.statusLine}")
    return ['error': ['message': "Authentication error!", 'result': "ERROR"]]
  }


  private Map getResponseSorted(Map response){
    Map result = [:]
    result.put("response_exists", "true")
    if (response.get("finished") == true){
      response.remove("progress")
      result.put("response_finished", "true")
      if (response.get("job_result")?.get("pkgId") != null){
        getResponseSortedPackage(response, result)
      }
      else{
        getResponseSortedTitles(response, result)
      }
    }
    else{
      result.put("response_finished", "false")
      result.put("progress", response.get("progress"))
    }
    return result
  }


  private List getResponseSortedPackage(Map response, Map result){
    def jobResult = response.get("job_result")
    String message = jobResult?.get("message")
    if (message != null){
      result.put("response_message", message)
    }
    int error = jobResult?.get("errors") != null ? jobResult?.get("errors")?.size() : 0
    int ok = jobResult?.get("results") != null ? jobResult?.get("results")?.size() : 0
    if (ok == 0){
      // package update --> get "OK" information from message string
      Matcher matcher = INT_FROM_MESSAGE_REGEX.matcher(message)
      if (matcher.find()){
        ok = Integer.valueOf(matcher.group(1))
      }
    }
    result.put("response_ok", ok.toString())
    result.put("response_error", error.toString())
  }


  private List getResponseSortedTitles(Map response, Map result){
    int ok, error
    List errorDetails = []
    String message = response.get("job_result")?.get("message")
    for (Map resultItem in response.get("job_result")?.get("results")){
      if (resultItem.get("result").equals("OK")){
        ok++
      }
      else if (resultItem.get("result").equals("ERROR")){
        error++
        errorDetails.add(getRecordError(resultItem))
      }
    }
    result.put("response_ok", ok.toString())
    result.put("response_error", error.toString())
    if (errorDetails.size() > 0){
      result.put("error_details", errorDetails)
    }
    if (message != null){
      result.put("response_message", message)
    }
  }


  private String getRecordError(Map record){
    StringBuilder result = new StringBuilder()
    if (record.get("message") != null){
      result.append(record.get("message"))
    }
    result.toString()
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
