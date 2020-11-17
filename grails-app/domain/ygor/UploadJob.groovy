package ygor

import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.UploadThreadGokb

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJob{

  Enrichment.FileType fileType
  String uuid
  def uploadThread
  int total

  UploadJob(Enrichment.FileType fileType, UploadThreadGokb uploadThread){
    this.fileType = fileType
    uuid = UUID.randomUUID().toString()
    this.uploadThread = uploadThread
    total = uploadThread.total
  }


  void start(){
    if (fileType in [Enrichment.FileType.TITLES, Enrichment.FileType.PACKAGE, Enrichment.FileType.PACKAGE_WITH_TITLEDATA]){
      uploadThread.start()
    }
    // else there is nothing to do
  }


  void updateCount(){
    if (uploadThread instanceof SendPackageThreadGokb){
      SendPackageThreadGokb sendPackageThreadGokb = (SendPackageThreadGokb) uploadThread
      sendPackageThreadGokb.updateCount()
    }
    // else if (uploadThread instanceof SendTitlesThreadGokb)
    //   --> there is no need for explicit updating as it happens automatically
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  int getCount(){
    return uploadThread.count
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  UploadThreadGokb.Status getStatus(){
    return uploadThread.status
  }


  @SuppressWarnings(["JpaAttributeMemberSignatureInspection", "JpaAttributeTypeInspection"])
  Map getResultsTable(){
    Map results = [:]
    results.putAll(uploadThread.getResultsTable())
    return results
  }


  void refreshStatus(){
    uploadThread.refreshStatus()
  }

}
