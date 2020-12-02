package ygor

import de.hbznrw.ygor.processing.UploadThreadGokb

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJobFrame {

  Enrichment.FileType fileType
  String uuid
  UploadThreadGokb.Status status


  UploadJobFrame(Enrichment.FileType fileType){
    this(fileType, UUID.randomUUID().toString())
  }


  UploadJobFrame(Enrichment.FileType fileType, String uuid){
    this.fileType = fileType
    this.uuid = uuid
    status = UploadThreadGokb.Status.PREPARATION
  }


  UploadJob toUploadJob(UploadThreadGokb uploadThread){
    return new UploadJob(this.fileType, uploadThread, this.uuid)
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  UploadThreadGokb.Status getStatus(){
    return status
  }

}
