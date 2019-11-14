package ygor

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
  Map<String, Map<String, Map<String, String>>> validRecords = new HashMap<>()
  Map<String, Map<String, Map<String, String>>> invalidRecords = new HashMap<>()

  def index(){
    render(
        view: 'index',
        model: [currentView: 'statistic']
    )
  }

  def show(){
    String resultHash = (String) request.parameterMap['resultHash'][0]
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
            resultHash    : resultHash,
            currentView   : 'statistic',
            ygorVersion   : ygorVersion,
            date          : date,
            filename      : filename,
            dataType      : enrichment?.dataType,
            validRecords  : validRecords[resultHash],
            invalidRecords: invalidRecords[resultHash]
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
            validRecords  : validRecords[resultHash],
            invalidRecords: invalidRecords[resultHash]
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
            invalidRecords: invalidRecords[resultHash],
            validRecords  : validRecords[resultHash]
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
      validRecords[params['resultHash']].put(multiFieldMap.get("uid"), multiFieldMap)
      invalidRecords[params['resultHash']].remove(multiFieldMap.get("uid"))
    }
    else{
      invalidRecords[params['resultHash']].put(multiFieldMap.get("uid"), multiFieldMap)
      validRecords[params['resultHash']].remove(multiFieldMap.get("uid"))
    }
  }


  private Enrichment getEnrichment(String resultHash){
    // get enrichment if existing
    def enrichments = enrichmentService.getSessionEnrichments()
    if (null != enrichments.get(resultHash)){
      return enrichments.get(resultHash)
    }
    // else get new Enrichment
    invalidRecords[resultHash] = new HashMap<>()
    validRecords[resultHash] = new HashMap<>()
    File uploadLocation = new File(grailsApplication.config.ygor.uploadLocation)
    for (def dir in uploadLocation.listFiles(DIRECTORY_FILTER)){
      for (def file in dir.listFiles()){
        if (file.getName() == resultHash){
          Enrichment enrichment = Enrichment.fromFile(file)
          enrichmentService.addSessionEnrichment(enrichment)
          return enrichment
        }
      }
    }
    return null
  }


  private void classifyAllRecords(String resultHash){
    validRecords[resultHash] = new HashMap<>()
    invalidRecords[resultHash] = new HashMap<>()
    Enrichment enrichment = getEnrichment(resultHash)
    if (enrichment == null){
      return
    }
    String namespace = enrichment.dataContainer.info.namespace_title_id
    for (Record record in enrichment.dataContainer.records.values()){
      record.validate(namespace)
      def multiFieldMap = record.asMultiFieldMap()
      if (record.isValid(enrichment.dataType)){
        validRecords[resultHash].put(multiFieldMap.get("uid"), multiFieldMap)
      }
      else{
        invalidRecords[resultHash].put(multiFieldMap.get("uid"), multiFieldMap)
      }
    }
  }


  static final PROCESSED_KBART_ENTRIES = "processed kbart entries"
  static final IGNORED_KBART_ENTRIES = "ignored kbart entries"
  static final DUPLICATE_KEY_ENTRIES = "duplicate key entries"
}
