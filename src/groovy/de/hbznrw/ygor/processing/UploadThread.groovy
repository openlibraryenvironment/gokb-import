package de.hbznrw.ygor.processing

import ygor.Enrichment

abstract class UploadThread extends Thread{

  Enrichment enrichment
  String uri
  String user
  String password
  int total = 0
  List result

}
