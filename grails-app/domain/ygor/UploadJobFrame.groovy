package ygor

import de.hbznrw.ygor.processing.UploadThreadGokb
import de.hbznrw.ygor.processing.YgorFeedback

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJobFrame {

  static mapWith = "none" // disable persisting into database

  Enrichment.FileType fileType
  String uuid
  UploadThreadGokb.Status status
  YgorFeedback ygorFeedback


  UploadJobFrame(Enrichment.FileType fileType, YgorFeedback ygorFeedback){
    this(fileType, UUID.randomUUID().toString(), ygorFeedback)
  }


  UploadJobFrame(Enrichment.FileType fileType, String uuid, YgorFeedback ygorFeedback){
    this.fileType = fileType
    this.uuid = uuid
    this.ygorFeedback = ygorFeedback
    ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.PREPARATION
    ygorFeedback.statusDescription += " Created UploadJobFrame."
    status = UploadThreadGokb.Status.PREPARATION
  }


  UploadJob toUploadJob(UploadThreadGokb uploadThread){
    ygorFeedback.statusDescription += " Transformed to UploadJob."
    return new UploadJob(this.fileType, uploadThread, this.uuid, ygorFeedback)
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  UploadThreadGokb.Status getStatus(){
    return status
  }

}
