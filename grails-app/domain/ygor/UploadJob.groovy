package ygor

import de.hbznrw.ygor.processing.SendPackageThreadGokb
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
    if (fileType in [Enrichment.FileType.TITLES, Enrichment.FileType.PACKAGE, Enrichment.FileType.PACKAGE_WITH_TITLEDATA]){
      uploadThread.start()
    }
    // else there is nothing to do
  }


  void updateCount(){
    if (uploadThread instanceof SendPackageThreadGokb){
      SendPackageThreadGokb sendPackageThreadGokb = (SendPackageThreadGokb) uploadThread
      sendPackageThreadGokb.updateCount()
      if (sendPackageThreadGokb.isInterrupted()){
        status = Status.ERROR
      }
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
      String responseStatus = uploadThread.getGokbResponseValue("responseStatus", true)
      if (responseStatus == null && status == Status.STARTED){
        if (uploadThread.result.size() > 0 && uploadThread.result[0].get("error") != null){
          status = Status.ERROR
          return
        }
      }
      if ("error" == responseStatus){
        status = Status.ERROR
        return
      }
      String innerResponseStatus = uploadThread.getGokbResponseValue("job_result.result", false)
      if (innerResponseStatus != null && "error" == innerResponseStatus.toLowerCase()){
        status = Status.ERROR
        return
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
