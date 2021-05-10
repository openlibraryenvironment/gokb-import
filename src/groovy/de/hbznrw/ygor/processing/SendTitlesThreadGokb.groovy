package de.hbznrw.ygor.processing

import de.hbznrw.ygor.export.GokbExporter
import groovy.util.logging.Log4j
import ygor.Enrichment
import ygor.Record

import javax.annotation.Nonnull

@Log4j
class SendTitlesThreadGokb extends UploadThreadGokb{

  SendTitlesThreadGokb(@Nonnull Enrichment enrichment, @Nonnull String uri,
                       @Nonnull String user, @Nonnull String password, @Nonnull String locale, YgorFeedback ygorFeedback){
    this.enrichment = enrichment
    this.uri = uri
    this.user = user
    this.password = password
    this.total += enrichment.yellowRecords?.size()
    this.total += enrichment.greenRecords?.size()
    result = []
    this.locale = locale
    status = UploadThreadGokb.Status.PREPARATION
    this.ygorFeedback = ygorFeedback
    ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.PREPARATION
    ygorFeedback.statusDescription += " Created SendTitlesThreadGokb."
    log.info("Set up send titles upload thread with ${this.total} records.")
  }


  @Override
  void run(){
    log.info("Starting titles upload thread ...")
    status = UploadThreadGokb.Status.STARTED
    ygorFeedback.statusDescription += " Started SendTitlesThreadGokb."
    ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.RUNNING
    ygorFeedback.reportingComponent = this.getClass()
    for (def recId in enrichment.dataContainer.records){
      ygorFeedback.dataComponent = Record.class
      String titleText = GokbExporter.extractTitle(enrichment, recId, false)
      log.info("... export Record " + recId + " to " + uri)
      result << GokbExporter.sendText(uri, titleText, user, password, locale, ygorFeedback)
      count ++
    }
    log.info("Finished titles upload thread.")
  }


  @Override
  Map getResultsTable(){
    Map results = [:]
    results.put("listDocuments.gokb.response.type", "listDocuments.gokb.response.titles")
    results.put("listDocuments.gokb.response.ok", String.valueOf(getCountForStatus("OK")))
    results.put("listDocuments.gokb.response.error", String.valueOf(getCountForStatus("ERROR")))
    results.putAll(getErrorMap())
    return results
  }


  private int getCountForStatus(String status){
    int statusCount = 0
    for (def resultItem in result){
      def innerResults = resultItem.get("info")
      if (innerResults.get("result").equals(status)){
        statusCount++
      }
    }
    return statusCount
  }


  private Map getErrorMap(){
    int i=1
    Map errorMap = [:]
    for (def resultItem in result){
      def innerResults = resultItem.get("info")
      if (innerResults.get("result").equals("ERROR")){
        errorMap.put(String.valueOf(i), innerResults.get("message"))
      }
    }
    errorMap
  }


  @Override
  String getGokbResponseValue(String responseKey, boolean updateResponse){
    // TODO
    return "TODO!"
  }
}