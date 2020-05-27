package ygor

import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.SendTitlesThreadGokb
import de.hbznrw.ygor.processing.UploadThreadGokb

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJob{

  Enrichment.FileType fileType
  String uuid
  Status status
  def uploadThread
  int total

  UploadJob(Enrichment.FileType fileType, UploadThreadGokb uploadThread){
    this.fileType = fileType
    uuid = UUID.randomUUID().toString()
    status = Status.PREPARATION
    this.uploadThread = uploadThread
    total = uploadThread.total
  }


  void start(){
    status = Status.STARTED
    if (fileType in [Enrichment.FileType.TITLES, Enrichment.FileType.PACKAGE]){
      uploadThread.start()
    }
    // else there is nothing to do
  }


  void updateCount(){
    if (uploadThread instanceof SendPackageThreadGokb){
      ((SendPackageThreadGokb) uploadThread).updateCount()
    }
    // else if (uploadThread instanceof SendTitlesThreadGokb)
    //   --> there is no need for explicit updating as it happens automatically
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  int getCount(){
    return uploadThread.count
  }


  @SuppressWarnings(["JpaAttributeMemberSignatureInspection", "JpaAttributeTypeInspection"])
  Map getResultsTable(){
    Map results = [:]
    results.putAll(uploadThread.getResultsTable())
    return results
  }


  void refreshStatus(){
    if (status == Status.STARTED){
      if (uploadThread.count >= uploadThread.total){
        status = Status.FINISHED_UNDEFINED
      }
    }
  }


  enum Status{
    PREPARATION,
    STARTED,
    FINISHED_UNDEFINED,
    SUCCESS,
    ERROR
  }
}
