package ygor

import de.hbznrw.ygor.processing.UploadThreadGokb
import de.hbznrw.ygor.processing.YgorFeedback

import javax.annotation.Nonnull

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJobFrame {

  static mapWith = "none" // disable persisting into database

  Enrichment.FileType fileType
  String uuid
  UploadThreadGokb.Status status
  YgorFeedback ygorFeedback


  UploadJobFrame(Enrichment.FileType fileType, @Nonnull YgorFeedback ygorFeedback){
    this(fileType, UUID.randomUUID().toString(), ygorFeedback)
  }


  UploadJobFrame(Enrichment.FileType fileType, String uuid, @Nonnull YgorFeedback ygorFeedback){
    this.fileType = fileType
    this.uuid = uuid
    this.ygorFeedback = ygorFeedback
    ygorFeedback.ygorProcessingStatus = YgorFeedback.YgorProcessingStatus.PREPARATION
    ygorFeedback.statusDescription += " Created UploadJobFrame."
    status = UploadThreadGokb.Status.PREPARATION
    log.info("Created UploadJobFrame $uuid .")
  }


  UploadJob toUploadJob(UploadThreadGokb uploadThread){
    ygorFeedback.statusDescription += " Transformed to UploadJob."
    log.info("Transforming UploadJobFrame $uuid to UploadJob.")
    return new UploadJob(this.fileType, uploadThread, this.uuid, ygorFeedback)
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  UploadThreadGokb.Status getStatus(){
    return status
  }

}
