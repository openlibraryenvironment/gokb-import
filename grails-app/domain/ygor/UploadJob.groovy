package ygor


import de.hbznrw.ygor.processing.UploadThread

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJob{

  Enrichment.FileType fileType
  String uuid
  Status status
  UploadThread uploadThread
  int total

  UploadJob(Enrichment.FileType fileType, UploadThread uploadThread){
    this.fileType = fileType
    uuid = UUID.randomUUID().toString()
    status = Status.PREPARATION
    this.uploadThread = uploadThread
    total = uploadThread.total
  }


  void start(){
    if (fileType.equals(Enrichment.FileType.TITLES)){
      uploadThread.start()
    }
    // else ... TODO: sendPackage
  }


  enum Status{
    PREPARATION,
    STARTED,
    SUCCESS,
    ERROR
  }
}
