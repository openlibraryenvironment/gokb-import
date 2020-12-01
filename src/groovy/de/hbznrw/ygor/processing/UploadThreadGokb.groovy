package de.hbznrw.ygor.processing

import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.tools.JsonToolkit
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
      if (count >= total){
        status = Status.SUCCESS
      }
    }
  }


  static UploadThreadGokb fromJson(JsonNode json) {
    Enrichment.FileType fileType = new Enrichment.FileType(JsonToolkit.fromJson(json, "fileType"))
    String uuid = JsonToolkit.fromJson(json, "uuid")
    UploadThreadGokb.Status status = new UploadThreadGokb.Status(JsonToolkit.fromJson(json, "status"))
    Integer total = JsonToolkit.fromJson(json, "total")
    UploadThreadGokb uploadThread = JsonToolkit.fromJson(json, "uploadThread")
    UploadThreadGokb result = new UploadThreadGokb(fileType, uploadThread)
    result.uuid = uuid
    result.total = total
    result.status = status
    result
  }


  enum Status{
    PREPARATION,
    STARTED,
    FINISHED_UNDEFINED,
    SUCCESS,
    ERROR
  }

}
