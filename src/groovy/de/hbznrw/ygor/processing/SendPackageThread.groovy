package de.hbznrw.ygor.processing

import de.hbznrw.ygor.export.GokbExporter
import groovy.util.logging.Log4j
import ygor.Enrichment

import javax.annotation.Nonnull

@Log4j
class SendPackageThread extends UploadThread{

  SendPackageThread(@Nonnull Enrichment enrichment, @Nonnull String uri,
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
    def json = enrichment.getAsFile(Enrichment.FileType.PACKAGE, true)
    log.info("exportFile: " + enrichment.resultHash + " -> " + uri)
    String body = json.getText()
    result << GokbExporter.sendText(uri, body, user, password)
  }

}