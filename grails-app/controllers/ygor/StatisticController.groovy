package ygor

import com.google.gson.Gson
import de.hbznrw.ygor.tools.FileToolkit
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import ygor.field.MultiField
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

  def grailsApplication
  EnrichmentService enrichmentService
  Map<String, Map<String, List<String>>> greenRecords = new HashMap<>()
  Map<String, Map<String, List<String>>> yellowRecords = new HashMap<>()
  Map<String, Map<String, List<String>>> redRecords = new HashMap<>()

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
    Enrichment enrichment = getEnrichment(resultHash)
    try{
      if (enrichment){
        enrichment.dataContainer.markDuplicateIds()
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
            ygorVersion   : enrichment.ygorVersion,
            date          : enrichment.date,
            filename      : enrichment.originName,
            greenRecords  : greenRecords[resultHash],
            yellowRecords : yellowRecords[resultHash],
            redRecords    : redRecords[resultHash],
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
    switch (colour){
      case RecordFlag.Colour.RED.toString():
        records = redRecords[resultHash]
        break
      case RecordFlag.Colour.YELLOW.toString():
        records = yellowRecords[resultHash]
        break
      case RecordFlag.Colour.GREEN.toString():
        records = greenRecords[resultHash]
        break
      default:
        records = null
    }
    List resultData = []
    if (records != null){
      int from = pageIndex
      int to = pageIndex + size
      records.eachWithIndex{TreeMap.Entry<String, Map<String, String>> entry, int i ->
        if (i >= from && i < to){
          def value = entry.value
          if (value.size() > 4){
            String title = value.getAt(0)
            String uid = value.getAt(4)
            if (!(StringUtils.isEmpty(title)) && !(StringUtils.isEmpty(uid))){
              value[0] = "<a href=\"/ygor/statistic/edit/".concat(uid).concat("?resultHash=").concat(resultHash).concat("\"/>")
            }
          }
          if (value.size() > 1){
            String linkValue = value.getAt(1)
            if (!(StringUtils.isEmpty(linkValue))){
              value[1] = "<a class=\"link-icon\" href=\"".concat(linkValue).concat("\"/>")
            }
          }
          resultData.add(value)
        }
      }
    }
    render "{\"recordsTotal\":${records.size()},\"recordsFiltered\":${records.size()},\"draw\":${pageIndex},\"data\":".concat(new Gson().toJson(resultData)).concat("}")
  }


  def cancel(){
    // restore record from dataContainer
    String resultHash = params['resultHash']
    Enrichment enrichment = getEnrichment(resultHash)

    Record record = enrichment.dataContainer.getRecord(params['record.uid'])
    classifyRecord(record)
    render(
        view: 'show',
        model: [
            resultHash    : resultHash,
            currentView   : 'statistic',
            ygorVersion   : enrichment.ygorVersion,
            date          : enrichment.date,
            filename      : enrichment.originName,
            packageName   : enrichment.packageName
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
    classifyRecord(record)
    // TODO: sort records in case of having changed the record's title
    render(
        view: 'show',
        model: [
            resultHash    : resultHash,
            currentView   : 'statistic',
            ygorVersion   : enrichment.ygorVersion,
            date          : enrichment.date,
            filename      : enrichment.originName,
            packageName   : enrichment.packageName
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


  synchronized private void classifyRecord(Record record){
    String key = record.displayTitle.concat(record.uid)
    List<String> values = [
        valOrEmpty(record.displayTitle),
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
        greenRecords[params['resultHash']].put(key, values)
        yellowRecords[params['resultHash']].remove(key)
        redRecords[params['resultHash']].remove(key)
      }
      else{
        yellowRecords[params['resultHash']].put(key, values)
        greenRecords[params['resultHash']].remove(key)
        redRecords[params['resultHash']].remove(key)
      }
    }
    else{
      redRecords[params['resultHash']].put(key,values)
      yellowRecords[params['resultHash']].remove(key)
      greenRecords[params['resultHash']].remove(key)
    }
  }


  private String valOrEmpty(def val){
    if (val == null || val.equals("null")){
      return ""
    }
    return val.toString()
  }


  private Enrichment getEnrichment(String resultHash){
    // get enrichment if existing
    def enrichments = enrichmentService.getSessionEnrichments()
    if (null != enrichments.get(resultHash)){
      return enrichments.get(resultHash)
    }
    // else get new Enrichment
    redRecords[resultHash] = new TreeMap<>()
    yellowRecords[resultHash] = new TreeMap<>()
    greenRecords[resultHash] = new TreeMap<>()
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


  synchronized private void classifyAllRecords(String resultHash){
    greenRecords[resultHash] = new TreeMap<>()
    yellowRecords[resultHash] = new TreeMap<>()
    redRecords[resultHash] = new TreeMap<>()
    Enrichment enrichment = getEnrichment(resultHash)
    if (enrichment == null){
      return
    }
    String namespace = enrichment.dataContainer.info.namespace_title_id
    for (Record record in enrichment.dataContainer.records.values()){
      record.normalize(namespace)
      record.validate(namespace)
      classifyRecord(record)
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
    sendFile(Enrichment.FileType.JSON_PACKAGE_ONLY)
  }



  def sendTitlesFile = {
    sendFile(Enrichment.FileType.JSON_TITLES_ONLY)
  }


  private void sendFile(Enrichment.FileType fileType){
    def en = getCurrentEnrichment()
    if (en){
      def response = enrichmentService.sendFile(en, fileType, params.gokbUsername, params.gokbPassword)
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
      Map model = [
          originHash   : en.originHash,
          resultHash   : en.resultHash,
          currentView  : 'statistic',
          ygorVersion  : en.ygorVersion,
          date         : en.date,
          filename     : en.originName,
          greenRecords : greenRecords[en.resultHash],
          yellowRecords: yellowRecords[en.resultHash],
          redRecords   : redRecords[en.resultHash],
          status       : en.status,
          packageName  : en.packageName
      ]
      model.putAll(getResponseMapped(response, fileType))
      render(
          view: 'show',
          model: model
      )
    }
  }


  private Map getResponseMapped(List response, Enrichment.FileType fileType){
    if (fileType.equals(Enrichment.FileType.JSON_TITLES_ONLY)){
      return getTitlesResponseMapped(response)
    }
    if (fileType.equals(Enrichment.FileType.JSON_PACKAGE_ONLY)){
      return getPackageResponseMapped(response)
    }
    // else
    return [:]
  }


  private Map getTitlesResponseMapped(List response){
    Map result = [:]
    List errorDetails = []
    result.put("response_exists", "true")
    int ok = 0, error = 0
    for (Map outerMap in response){
      for (Map innerMap in outerMap.values()){
        for (Map record in innerMap.get("results")){
          if (record.get("result").equals("OK")){
            ok++
          }
          else if (record.get("result").equals("ERROR")){
            error++
            errorDetails.add(getRecordError(record))
          }
        }
      }
    }
    result.put("response_ok", ok.toString())
    result.put("response_error", error.toString())
    result.put("error_details", errorDetails)
    return result
  }


  private String getRecordError(Map record){
    StringBuilder result = new StringBuilder()
    if (record.get("message") != null){
      result.append(record.get("message"))
    }
    result.toString()
  }


  private Map<String, String> getPackageResponseMapped(List response){
    Map<String, String> result = new HashMap<>()
    List<String> errorDetails = []
    result.put("response_exists", "true")
    int ok = 0, error = 0
    for (Map outerMap in response){
      for (Map innerMap in outerMap.values()){
        if (!StringUtils.isEmpty(innerMap.get("message"))){
          result.put("response_message", innerMap.get("message"))
        }
        if (innerMap.get("result").equals("OK") && innerMap.get("errors").isEmpty()){
          ok = extractNumberFromResponse(innerMap.get("message"), "with", "TIPPs")
        }
        else {
          for (def entry in innerMap){
            if (entry.key.equals("errors")){
              for (resultMap in entry.value){
                error++
                errorDetails.add(resultMap.'message')
              }
            }
          }
        }
        if (ok > 0){
          result.put("response_ok", ok.toString())
        }
        result.put("response_error", error.toString())
        result.put("error_details", errorDetails)
      }
    }
    return result
  }


  private Integer extractNumberFromResponse(String response, String prefix, String suffix){
    final Pattern p = Pattern.compile(prefix.concat("[\\s]*([0-9]+)[\\s]*").concat(suffix))
    Matcher m = p.matcher(response)
    m.find()
    try {
      return Integer.valueOf(m.group(1))
    }
    catch (Exception e){
      log.error("Could not extract number from GOKb response message. ".concat(e.getMessage()))
      return null
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
        classifyRecord(record)
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
