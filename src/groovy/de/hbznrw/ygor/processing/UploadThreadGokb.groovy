package de.hbznrw.ygor.processing

import ygor.Enrichment
import ygor.UploadJob


abstract class UploadThreadGokb extends Thread{

  Enrichment enrichment
  String uri
  String user
  String password
  int total = 0
  List result

  abstract def getThreadInfo(Map<String, Object> infoMap);

  Map getResponseSorted(Map response){
    Map result = [:]
    if (response.get("listDocuments.gokb.response.status") == UploadJob.Status.SUCCESS){
      response.remove("progress")
      result.putAll(getResponseSortedDetails())
    }
    else{
      result.put("progress", response.get("progress"))
    }
    return result
  }


  abstract protected Map getResponseSortedDetails();


  abstract int getCount();
}
