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


  /*@Override
  Map getResponseSorted(Map response){
    Map result = [:]
    if (response.get("listDocuments.gokb.response.status") == UploadJob.Status.SUCCESS){
      response.remove("progress")
      result.putAll(getResponseSortedDetails(response))
    }
    else{
      result.put("progress", response.get("progress"))
    }
    return result
  }*/


  /*protected Map getResponseSortedDetails(Map response){
    Map<String, Object> sortedDetails = [:]
    int ok, error
    List errorDetails = []
    for (def resultItem in response){
      def innerResults = resultItem.get("info")
      if (innerResults.get("result").equals("OK")){
        ok++
      }
      else if (innerResults.get("result").equals("ERROR")){
        error++
        errorDetails.add(getRecordError(innerResults))
      }
    }
    sortedDetails.put("listDocuments.gokb.response.ok", ok.toString())
    sortedDetails.put("listDocuments.gokb.response.error", error.toString())
    if (errorDetails.size() > 0){
      sortedDetails.put("error_details", errorDetails)
      sortedDetails.put("listDocuments.gokb.response.status", UploadJob.Status.ERROR)
    }
    else{
      sortedDetails.put("listDocuments.gokb.response.status", UploadJob.Status.SUCCESS)
    }
    sortedDetails
  }*/


  /*private String getRecordError(Map record){
    StringBuilder result = new StringBuilder()
    if (record.get("message") != null){
      result.append(record.get("message"))
    }
    result.toString()
  }*/


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

}