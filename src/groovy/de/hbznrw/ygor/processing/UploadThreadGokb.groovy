package de.hbznrw.ygor.processing

import groovy.util.logging.Log4j
import ygor.Enrichment


@Log4j
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
  YgorFeedback ygorFeedback

  abstract Map getResultsTable()

  abstract String getGokbResponseValue(String responseKey, boolean updateResponse)

  void refreshStatus(){
    if (status == Status.STARTED){
      String responseStatus = getGokbResponseValue("responseStatus", true)
      if (responseStatus == null && status == Status.STARTED){
        if (result.size() > 0 && result[0].get("error") != null){
          status = Status.ERROR
          log.info("Turned UploadThread status to $Status.ERROR .")
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
      if (count >= total){
        status = Status.SUCCESS
        return
      }
      if ("ok" == responseStatus && "true" == getGokbResponseValue("finished", false)){
        status = Status.SUCCESS
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
