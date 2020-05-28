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

  abstract Map getResultsTable();

  abstract String getGokbResponseValue(String jobId, String responseKey);

}
