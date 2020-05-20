package de.hbznrw.ygor.processing

import ygor.Enrichment


abstract class UploadThreadGokb extends Thread{

  Enrichment enrichment
  String uri
  String user
  String password
  int total = 0
  List result

  abstract def getJobInfo(Map<String, Object> infoMap);


  Map getResponseSorted(Map response){
    Map result = [:]
    if (response.get("finished") == true){
      response.remove("progress")
      result.put("response_finished", "true")
      result.putAll(getResponseSortedDetails(response))
    }
    else{
      result.put("response_finished", "false")
      result.put("progress", response.get("progress"))
    }
    return result
  }


  abstract protected Map getResponseSortedDetails();


  abstract int getCount();
}
