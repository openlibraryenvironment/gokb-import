package de.hbznrw.ygor.processing

import de.hbznrw.ygor.export.GokbExporter
import groovy.util.logging.Log4j
import ygor.Enrichment

import javax.annotation.Nonnull

@Log4j
class SendTitlesThread extends UploadThread{

  int count = 0

  SendTitlesThread(@Nonnull Enrichment enrichment, @Nonnull String uri,
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
      log.info("export Record: " + uri)
      result << GokbExporter.sendText(uri, titleText, user, password)
      count ++
    }
  }

}