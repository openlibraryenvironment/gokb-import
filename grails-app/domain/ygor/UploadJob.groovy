package ygor

import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.UploadThreadGokb
import de.hbznrw.ygor.processing.YgorFeedback

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJob extends UploadJobFrame{

  static mapWith = "none" // disable persisting into database

  def uploadThread
  Integer total

  UploadJob(Enrichment.FileType fileType, UploadThreadGokb uploadThread, YgorFeedback ygorFeedback){
    super(fileType, ygorFeedback)
    fillFrame(uploadThread)
  }


  UploadJob(Enrichment.FileType fileType, UploadThreadGokb uploadThread, String uuid, YgorFeedback ygorFeedback){
    super(fileType, uuid, ygorFeedback)
    fillFrame(uploadThread)
  }


  UploadJob fillFrame(UploadThreadGokb uploadThread){
    this.uploadThread = uploadThread
    total = uploadThread.total
    ygorFeedback.statusDescription += " Filled UploadJob with UploadThread."
    return this
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
  Integer getCount(){
    return uploadThread.count
  }


  @Override
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
