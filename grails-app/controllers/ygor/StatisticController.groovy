package ygor

import de.hbznrw.ygor.tools.FileToolkit
import groovy.util.logging.Log4j
import ygor.field.MultiField

@Log4j
class StatisticController{

  static scope = "session"
  static FileFilter DIRECTORY_FILTER = new FileFilter(){
    @Override
    boolean accept(File file){
      return file.isDirectory()
    }
  }

  def grailsApplication
  EnrichmentService enrichmentService
  Map<String, Map<String, Map<String, String>>> greenRecords = new HashMap<>()
  Map<String, Map<String, Map<String, String>>> yellowRecords = new HashMap<>()
  Map<String, Map<String, Map<String, String>>> redRecords = new HashMap<>()

  def index(){
    render(
        view: 'index',
        model: [currentView: 'statistic']
    )
  }

  def show(){
    String resultHash = request.parameterMap.resultHash[0]
    String originHash = request.parameterMap.originHash[0]
    log.info('show enrichment ' + resultHash)
    String ygorVersion
    String date
    String filename
    Enrichment enrichment = getEnrichment(resultHash)
    try{
      if (enrichment){
        ygorVersion = enrichment.ygorVersion
        date = enrichment.date
        filename = enrichment.originName
        classifyAllRecords(resultHash)
      }
      else{
        throw new EmptyStackException()
      }
    }
    catch (Exception e){
      log.error(e.getMessage())
      log.error(e.getStackTrace())
    }

    render(
        view: 'show',
        model: [
            originHash    : originHash,
            resultHash    : resultHash,
            currentView   : 'statistic',
            ygorVersion   : ygorVersion,
            date          : date,
            filename      : filename,
            dataType      : enrichment?.dataType,
            greenRecords  : greenRecords[resultHash],
            yellowRecords : yellowRecords[resultHash],
            redRecords    : redRecords[resultHash],
            status        : enrichment.status
        ]
    )
  }


  def cancel(){
    // restore record from dataContainer
    String resultHash = params['resultHash']
    Enrichment enrichment = getEnrichment(resultHash)
    Record record = enrichment.dataContainer.getRecord(params['record.uid'])
    classifyRecord(record, enrichment)
    render(
        view: 'show',
        model: [
            resultHash    : resultHash,
            currentView   : 'statistic',
            dataType      : enrichment?.dataType,
            greenRecords  : greenRecords[resultHash],
            yellowRecords : yellowRecords[resultHash],
            redRecords    : redRecords[resultHash]
        ]
    )
  }


  def save(){
    // write record into dataContainer
    String resultHash = params['resultHash']
    Enrichment enrichment = getEnrichment(resultHash)
    Record record = enrichment.dataContainer.records[params['record.uid']]
    for (def field in params['fieldschanged']){
      record.multiFields.get(field.key).revised = field.value
    }
    classifyRecord(record, enrichment)
    render(
        view: 'show',
        model: [
            resultHash    : resultHash,
            currentView   : 'statistic',
            dataType      : enrichment?.dataType,
            redRecords    : redRecords[resultHash],
            yellowRecords : yellowRecords[resultHash],
            greenRecords  : greenRecords[resultHash]
        ]
    )
  }


  def edit(){
    String resultHash = request.parameterMap['resultHash'][0]
    Enrichment enrichment = getEnrichment(resultHash)
    Record record = enrichment.dataContainer.getRecord(params.id)
    [
        resultHash: resultHash,
        record    : record
    ]
  }


  def update(){
    def resultHash = params.resultHash
    def value = params.value
    def key = params.key
    def uid = params.uid
    Record record

    try{
      Enrichment enrichment = getEnrichment(resultHash)
      String namespace = enrichment.dataContainer.info.namespace_title_id
      if (enrichment){
        record = enrichment.dataContainer.records.get(uid)
        MultiField multiField = record.multiFields.get(key)
        multiField.revised = value.trim()
        record.validate(namespace)
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


  private void classifyRecord(Record record, Enrichment enrichment){
    def multiFieldMap = record.asMultiFieldMap()
    if (record.isValid(enrichment.dataType)){
      if (record.multiFields.get("titleUrl").isCorrect() &&
          (!record.multiFields.get("publicationType").getFirstPrioValue().equals("Serial") || record.zdbIntegrationUrl != null)){
        greenRecords[params['resultHash']].put(multiFieldMap.get("uid"), multiFieldMap)
        yellowRecords[params['resultHash']].remove(multiFieldMap.get("uid"))
        redRecords[params['resultHash']].remove(multiFieldMap.get("uid"))
      }
      else{
        yellowRecords[params['resultHash']].put(multiFieldMap.get("uid"), multiFieldMap)
        greenRecords[params['resultHash']].remove(multiFieldMap.get("uid"))
        redRecords[params['resultHash']].remove(multiFieldMap.get("uid"))
      }
    }
    else{
      redRecords[params['resultHash']].put(multiFieldMap.get("uid"), multiFieldMap)
      yellowRecords[params['resultHash']].remove(multiFieldMap.get("uid"))
      greenRecords[params['resultHash']].remove(multiFieldMap.get("uid"))
    }
  }


  private Enrichment getEnrichment(String resultHash){
    // get enrichment if existing
    def enrichments = enrichmentService.getSessionEnrichments()
    if (null != enrichments.get(resultHash)){
      return enrichments.get(resultHash)
    }
    // else get new Enrichment
    redRecords[resultHash] = new HashMap<>()
    yellowRecords[resultHash] = new HashMap<>()
    greenRecords[resultHash] = new HashMap<>()
    File uploadLocation = new File(grailsApplication.config.ygor.uploadLocation)
    for (def dir in uploadLocation.listFiles(DIRECTORY_FILTER)){
      for (def file in dir.listFiles()){
        if (file.getName() == resultHash){
          Enrichment enrichment = Enrichment.fromJsonFile(file)
          enrichmentService.addSessionEnrichment(enrichment)
          return enrichment
        }
      }
    }
    return null
  }


  private void classifyAllRecords(String resultHash){
    greenRecords[resultHash] = new HashMap<>()
    yellowRecords[resultHash] = new HashMap<>()
    redRecords[resultHash] = new HashMap<>()
    Enrichment enrichment = getEnrichment(resultHash)
    if (enrichment == null){
      return
    }
    String namespace = enrichment.dataContainer.info.namespace_title_id
    for (Record record in enrichment.dataContainer.records.values()){
      record.validate(namespace)
      classifyRecord(record, enrichment)
    }
  }


  static final PROCESSED_KBART_ENTRIES = "processed kbart entries"
  static final IGNORED_KBART_ENTRIES = "ignored kbart entries"
  static final DUPLICATE_KEY_ENTRIES = "duplicate key entries"


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
      def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_PACKAGE_ONLY)
      render(file: result, fileName: "${en.resultName}.package.json")
    }
    else{
      noValidEnrichment()
    }
  }


  def downloadTitlesFile = {
    def en = getCurrentEnrichment()
    if (en){
      def result = enrichmentService.getFile(en, Enrichment.FileType.JSON_TITLES_ONLY)
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
    def en = getCurrentEnrichment()
    if (en){
      def response = enrichmentService.sendFile(en, Enrichment.FileType.JSON_PACKAGE_ONLY,
          params.gokbUsername, params.gokbPassword)
      flash.info = []
      flash.warning = []
      flash.error = []

      if (response.JSON){
        if (response.info[0] != null){
          if (response.info[0].result == 'ERROR'){
            flash.warning = [response.info[0].message]
          }
          else{
            flash.info = response.info[0].message
          }
          flash.error = []

          response.info[0].errors?.each{ e ->
            flash.error.add(e.message)
          }
        }
        if (response.warning[0] != null){
          log.debug("${response.warning[0]}")
          flash.warning = [response.warning[0].message]

          response.warning[0].errors.each{ e ->
            flash.warning.add(e.message)
          }
        }
        if (response.error[0] != null){
          log.warn("ERROR: ${response.error}")
          flash.error = [response.error[0].message]

          response.error.errors?.each{ e ->
            flash.error.add(e.message)
          }
        }
      }
    }
    else{
      flash.error = "There was an error authenticating with GOKb!"
    }
    render(
        view: 'show',
        model: [
            originHash    : en.originHash,
            resultHash    : en.resultHash,
            currentView   : 'statistic',
            ygorVersion   : en.ygorVersion,
            date          : en.date,
            filename      : en.originName,
            dataType      : en.dataType,
            greenRecords  : greenRecords[en.resultHash],
            yellowRecords : yellowRecords[en.resultHash],
            redRecords    : redRecords[en.resultHash],
            status        : en.status
        ]
    )
  }


  def sendTitlesFile = {
    def en = getCurrentEnrichment()
    if (en){
      def response = enrichmentService.sendFile(en, Enrichment.FileType.JSON_TITLES_ONLY,
          params.gokbUsername, params.gokbPassword)
      flash.info = []
      flash.warning = []
      List errorList = []
      def total = 0
      def errors = 0
      log.debug("sendTitlesFile response: ${response}")
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
          view: 'show',
          model: [
              originHash    : en.originHash,
              resultHash    : en.resultHash,
              currentView   : 'statistic',
              ygorVersion   : en.ygorVersion,
              date          : en.date,
              filename      : en.originName,
              dataType      : en.dataType,
              greenRecords  : greenRecords[en.resultHash],
              yellowRecords : yellowRecords[en.resultHash],
              redRecords    : redRecords[en.resultHash],
              status        : en.status
          ]
      )
    }
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
}
