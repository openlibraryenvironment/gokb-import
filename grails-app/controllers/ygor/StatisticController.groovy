package ygor

import groovy.util.logging.Log4j
import ygor.field.MultiField

@Log4j
class StatisticController {

  static scope = "session"
  static FileFilter DIRECTORY_FILTER = new FileFilter() {
    @Override
    boolean accept(File file) {
      return file.isDirectory()
    }
  }

  def grailsApplication
  Map<String, Enrichment> enrichments = new HashMap<>()
  Map<String, Map<String, Map<String, String>>> validRecords = new HashMap<>()
  Map<String, Map<String, Map<String, String>>> invalidRecords = new HashMap<>()

  def index() {
    render(
        view: 'index',
        model: [currentView: 'statistic']
    )
  }

  def show() {
    String sthash = (String) request.parameterMap['sthash'][0]
    String ygorVersion
    String date
    String filename
    log.info('reading file: ' + sthash)
    Enrichment enrichment = getEnrichment(sthash)
    try {
      if (enrichment) {
        ygorVersion = enrichment.ygorVersion
        date = enrichment.date
        filename = enrichment.originName
        classifyAllRecords(sthash)
      }
      else {
        throw new EmptyStackException()
      }
    }
    catch (Exception e) {
      log.error(e.getMessage())
      log.error(e.getStackTrace())
    }

    render(
        view: 'show',
        model: [
            sthash        : sthash,
            currentView   : 'statistic',
            ygorVersion   : ygorVersion,
            date          : date,
            filename      : filename,
            dataType      : enrichment?.dataType,
            validRecords  : validRecords[sthash],
            invalidRecords: invalidRecords[sthash]
        ]
    )
  }


  def cancel() {
    // restore record from dataContainer
    String sthash = params['sthash']
    Enrichment enrichment = getEnrichment(sthash)
    Record record = enrichment.dataContainer.getRecord(params['record.uid'])
    classifyRecord(record, enrichment)
    render(
        view: 'show',
        model: [
            sthash        : sthash,
            currentView   : 'statistic',
            dataType      : enrichment?.dataType,
            validRecords  : validRecords[sthash],
            invalidRecords: invalidRecords[sthash]
        ]
    )
  }


  def save() {
    // write record into dataContainer
    String sthash = params['sthash']
    Enrichment enrichment = getEnrichment(sthash)
    Record record = enrichment.dataContainer.records[params['record.uid']]
    for (def field in params['fieldschanged']){
      record.multiFields.get(field.key).revised = field.value
    }
    classifyRecord(record, enrichment)
    render(
        view: 'show',
        model: [
            sthash        : sthash,
            currentView   : 'statistic',
            dataType      : enrichment?.dataType,
            invalidRecords: invalidRecords[sthash],
            validRecords  : validRecords[sthash]
        ]
    )
  }


  def edit() {
    String sthash = request.parameterMap['sthash'][0]
    Enrichment enrichment = getEnrichment(sthash)
    Record record = enrichment.dataContainer.getRecord(params.id)
    [
        sthash: sthash,
        record: record
    ]
  }


  def update() {
    def sthash = params.sthash
    def value = params.value
    def key = params.key
    def uid = params.uid
    Record record

    try {
      Enrichment enrichment = getEnrichment(sthash)
      String namespace = enrichment.dataContainer.info.namespace_title_id
      if (enrichment) {
        record = enrichment.dataContainer.records.get(uid)
        MultiField multiField = record.multiFields.get(key)
        multiField.revised = value.trim()
        record.validate(namespace)
      }
      else {
        throw new EmptyStackException()
      }
    }
    catch (Exception e) {
      log.error(e.getMessage())
      log.error(e.getStackTrace())
    }
    render (groovy.json.JsonOutput.toJson ([
        record        : record.asStatisticsJson(),
        sthash        : sthash
    ]))
  }


  private void classifyRecord(Record record, Enrichment enrichment) {
    def multiFieldMap = record.asMultiFieldMap()
    if (record.isValid(enrichment.dataType)) {
      validRecords[params['sthash']].put(multiFieldMap.get("uid"), multiFieldMap)
      invalidRecords[params['sthash']].remove(multiFieldMap.get("uid"))
    }
    else {
      invalidRecords[params['sthash']].put(multiFieldMap.get("uid"), multiFieldMap)
      validRecords[params['sthash']].remove(multiFieldMap.get("uid"))
    }
  }


  private Enrichment getEnrichment(String sthash) {
    // get enrichment if existing
    if (null != enrichments.get(sthash)){
      return enrichments.get(sthash)
    }
    // get new Enrichment
    invalidRecords[sthash] = new HashMap<>()
    validRecords[sthash] = new HashMap<>()
    File uploadLocation = new File(grailsApplication.config.ygor.uploadLocation)
    for (def dir in uploadLocation.listFiles(DIRECTORY_FILTER)) {
      for (def file in dir.listFiles()) {
        if (file.getName() == sthash) {
          Enrichment enrichment = Enrichment.fromFile(file)
          enrichments.put(sthash, enrichment)
          return enrichment
        }
      }
    }
    return null
  }


  private void classifyAllRecords(String sthash){
    Enrichment enrichment = getEnrichment(sthash)
    if (enrichment == null){
      return
    }
    String namespace = enrichment.dataContainer.info.namespace_title_id
    for (Record record in enrichment.dataContainer.records.values()) {
      record.validate(namespace)
      def multiFieldMap = record.asMultiFieldMap()
      if (record.isValid(enrichment.dataType)) {
        validRecords[sthash].put(multiFieldMap.get("uid"), multiFieldMap)
      } else {
        invalidRecords[sthash].put(multiFieldMap.get("uid"), multiFieldMap)
      }
    }
  }


  static final PROCESSED_KBART_ENTRIES = "processed kbart entries"
  static final IGNORED_KBART_ENTRIES = "ignored kbart entries"
  static final DUPLICATE_KEY_ENTRIES = "duplicate key entries"
}
