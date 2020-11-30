package ygor

import de.hbznrw.ygor.processing.UploadThreadGokb

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJobFrame {

  Enrichment.FileType fileType
  String uuid
  UploadThreadGokb.Status status


  UploadJobFrame(Enrichment.FileType fileType){
    this.fileType = fileType
    uuid = UUID.randomUUID().toString()
    status = UploadThreadGokb.Status.PREPARATION
  }


  UploadJob toUploadJob(UploadThreadGokb uploadThread){
    return new UploadJob(this.fileType, uploadThread)
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  UploadThreadGokb.Status getStatus(){
    return status
  }

}
