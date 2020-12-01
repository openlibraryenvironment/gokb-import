package ygor

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.processing.SendPackageThreadGokb
import de.hbznrw.ygor.processing.UploadThreadGokb
import de.hbznrw.ygor.tools.JsonToolkit

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJob extends UploadJobFrame{

  def uploadThread
  Integer total

  UploadJob(Enrichment.FileType fileType, UploadThreadGokb uploadThread){
    super(fileType)
    fillFrame(uploadThread)
  }


  UploadJob fillFrame(UploadThreadGokb uploadThread){
    this.uploadThread = uploadThread
    total = uploadThread.total
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


  @Override
  void asJson(JsonGenerator jsonGenerator){
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("fileType", fileType.toString())
    jsonGenerator.writeStringField("uuid", uuid)
    jsonGenerator.writeStringField("status", status)
    jsonGenerator.writeNumberField("total", total)
    jsonGenerator.writeObjectField("uploadThread", uploadThread)
    jsonGenerator.writeEndObject()
  }


  @Override
  static UploadJob fromJson(JsonNode json) {
    Enrichment.FileType fileType = new Enrichment.FileType(JsonToolkit.fromJson(json, "fileType"))
    String uuid = JsonToolkit.fromJson(json, "uuid")
    UploadThreadGokb.Status status = new UploadThreadGokb.Status(JsonToolkit.fromJson(json, "status"))
    Integer total = JsonToolkit.fromJson(json, "total")
    UploadThreadGokb uploadThread = JsonToolkit.fromJson(json, "uploadThread")
    UploadJob result = new UploadJob(fileType, uploadThread)
    result.uuid = uuid
    result.total = total
    result.status = status
    result
  }

}
