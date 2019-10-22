package ygor

import de.hbznrw.ygor.tools.JsonToolkit
import groovy.util.logging.Log4j
import net.sf.json.JSON
import ygor.field.MultiField

@Log4j
class StatisticController {

  def grailsApplication
  Map<String, Map<String, String>> invalidRecords
  Map<String, Map<String, String>> validRecords

  static scope = "session"
  static FileFilter DIRECTORY_FILTER = new FileFilter() {
    @Override
    boolean accept(File file) {
      return file.isDirectory()
    }
  }

  def index() {
    render(
        view: 'index',
        model: [currentView: 'statistic']
    )
  }

  def show() {
    String sthash = (String) request.parameterMap['sthash'][0]
    invalidRecords = new HashMap<>()
    validRecords = new HashMap<>()
    String ygorVersion
    String date
    String filename
    Enrichment enrichment
    log.info('reading file: ' + sthash)

    try {
      enrichment = getEnrichmentFromFile(sthash)
      String namespace = enrichment.dataContainer.info.namespace_title_id
      if (enrichment) {
        for (Record record in enrichment.dataContainer.records.values()) {
          record.validate(namespace)
          def multiFieldMap = record.asMultiFieldMap()
          if (record.isValid(enrichment.dataType)) {
            validRecords.put(multiFieldMap.get("uid"), multiFieldMap)
          } else {
            invalidRecords.put(multiFieldMap.get("uid"), multiFieldMap)
          }
        }
        ygorVersion = enrichment.ygorVersion
        date = enrichment.date
        filename = enrichment.originName
      } else {
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
            invalidRecords: invalidRecords,
            validRecords  : validRecords
        ]
    )
  }


  def edit() {
    String sthash = request.parameterMap['sthash'][0]
    Enrichment enrichment = getEnrichmentFromFile(sthash)
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
      Enrichment enrichment = getEnrichmentFromFile(sthash)
      String namespace = enrichment.dataContainer.info.namespace_title_id
      if (enrichment) {
        record = enrichment.dataContainer.records.get(uid)
        MultiField multiField = record.multiFields.get(key)
        multiField.revised = value.trim()
        record.validate(namespace)
        classifyRecord(record, enrichment)
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
      validRecords.put(multiFieldMap.get("uid"), multiFieldMap)
      invalidRecords.remove(multiFieldMap.get("uid"))
    }
    else {
      invalidRecords.put(multiFieldMap.get("uid"), multiFieldMap)
      validRecords.remove(multiFieldMap.get("uid"))
    }
  }


  private Enrichment getEnrichmentFromFile(String sthash) {
    File uploadLocation = new File(grailsApplication.config.ygor.uploadLocation)
    for (def dir in uploadLocation.listFiles(DIRECTORY_FILTER)) {
      for (def file in dir.listFiles()) {
        if (file.getName() == sthash) {
          return Enrichment.fromFile(file)
        }
      }
    }
    return null
  }


  static final PROCESSED_KBART_ENTRIES = "processed kbart entries"
  static final IGNORED_KBART_ENTRIES = "ignored kbart entries"
  static final DUPLICATE_KEY_ENTRIES = "duplicate key entries"
}
