package de.hbznrw.ygor.processing

import ygor.Enrichment


abstract class UploadThreadGokb extends Thread{

  Enrichment enrichment
  String uri
  String user
  String password
  int total = 0
  List result
  int count = 0
  String locale
  Status status

  abstract Map getResultsTable()

  abstract String getGokbResponseValue(String responseKey, boolean updateResponse)

  void refreshStatus(){
    if (status == Status.STARTED){
      if (count >= total){
        status = Status.FINISHED_UNDEFINED
      }
      String responseStatus = getGokbResponseValue("responseStatus", true)
      if (responseStatus == null && status == Status.STARTED){
        if (result.size() > 0 && result[0].get("error") != null){
          status = Status.ERROR
          return
        }
      }
      if ("error" == responseStatus){
        status = Status.ERROR
        return
      }
      String innerResponseStatus = getGokbResponseValue("job_result.result", false)
      if (innerResponseStatus != null && "error" == innerResponseStatus.toLowerCase()){
        status = Status.ERROR
        return
      }
    }
  }


  enum Status{
    PREPARATION,
    STARTED,
    FINISHED_UNDEFINED,
    SUCCESS,
    ERROR
  }

}
