package ygor

import de.hbznrw.ygor.processing.SendTitlesThread

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJob{

  Enrichment.FileType fileType
  String uuid
  Status status
  SendTitlesThread sendTitlesThread
  int total

  UploadJob(Enrichment.FileType fileType, SendTitlesThread sendTitlesThread){
    this.fileType = fileType
    uuid = UUID.randomUUID().toString()
    status = Status.PREPARATION
    this.sendTitlesThread = sendTitlesThread
    total = sendTitlesThread.total
  }


  void start(){
    if (fileType.equals(Enrichment.FileType.TITLES)){
      sendTitlesThread.start()
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
