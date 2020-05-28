package de.hbznrw.ygor.processing

import de.hbznrw.ygor.export.GokbExporter
import groovy.util.logging.Log4j
import ygor.Enrichment
import ygor.UploadJob

import javax.annotation.Nonnull

@Log4j
class SendTitlesThreadGokb extends UploadThreadGokb{

  SendTitlesThreadGokb(@Nonnull Enrichment enrichment, @Nonnull String uri,
                       @Nonnull String user, @Nonnull String password){
    this.enrichment = enrichment
    this.uri = uri
    this.user = user
    this.password = password
    total += enrichment.yellowRecords?.size()
    total += enrichment.greenRecords?.size()
    result = []
  }


  @Override
  void run(){
    for (def recId in enrichment.dataContainer.records){
      String titleText = GokbExporter.extractTitle(enrichment, recId, false)
      log.info("export Record " + recId + " to " + uri)
      result << GokbExporter.sendText(uri, titleText, user, password)
      count ++
    }
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
    int count = 0
    for (def resultItem in result){
      def innerResults = resultItem.get("info")
      if (innerResults.get("result").equals(status)){
        count++
      }
    }
    return count
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
  String getGokbResponseValue(String responseKey){
    // TODO
    return "TODO!"
  }
}