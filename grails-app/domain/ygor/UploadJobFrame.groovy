package ygor

import de.hbznrw.ygor.processing.UploadThreadGokb

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJobFrame {

  Enrichment.FileType fileType
  String uuid


  UploadJobFrame(Enrichment.FileType fileType){
    this.fileType = fileType
    uuid = UUID.randomUUID().toString()
  }


  UploadJob toUploadJob(UploadThreadGokb uploadThread){
    UploadJob result = (UploadJob) this
    return result.fillFrame(uploadThread)
  }

}
