package ygor

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.processing.UploadThreadGokb
import de.hbznrw.ygor.tools.JsonToolkit

@SuppressWarnings("JpaObjectClassSignatureInspection")
class UploadJobFrame {

  static String uploadJobsPath = grails.util.Holders.grailsApplication.config.ygor.autoUpdateJobsLocation.toString()
  static JsonFactory JSON_FACTORY = new JsonFactory()
  static{
    new File(uploadJobsPath).mkdirs()
    JSON_FACTORY.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
  }


  Enrichment.FileType fileType
  String uuid
  UploadThreadGokb.Status status

  @JsonIgnoreProperties(["errors", "messageCodesResolver"])
  UploadJobFrame(Enrichment.FileType fileType){
    this.fileType = fileType
    uuid = UUID.randomUUID().toString()
    status = UploadThreadGokb.Status.PREPARATION
  }


  UploadJob toUploadJob(UploadThreadGokb uploadThread){
    return new UploadJob(this.fileType, uploadThread)
  }


  @SuppressWarnings("JpaAttributeMemberSignatureInspection")
  UploadThreadGokb.Status getStatus(){
    return status
  }


  void save(){
    if (uuid == null){
      return
    }
    StringWriter stringWriter = new StringWriter()
    JsonGenerator jsonGenerator = JSON_FACTORY.createGenerator(stringWriter)
    File targetFile = new File(uploadJobsPath.concat(uuid))
    this.asJson(jsonGenerator)
    jsonGenerator.close()
    PrintWriter printWriter = new PrintWriter(targetFile, "UTF-8")
    printWriter.println(stringWriter.toString())
    printWriter.close()
    stringWriter.close()
  }


  void asJson(JsonGenerator jsonGenerator){
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("fileType", fileType.toString())
    jsonGenerator.writeStringField("uuid", uuid)
    jsonGenerator.writeStringField("status", status)
    jsonGenerator.writeEndObject()
  }


  static UploadJob fromJson(JsonNode json) {
    Enrichment.FileType fileType = new Enrichment.FileType(JsonToolkit.fromJson(json, "fileType"))
    String uuid = JsonToolkit.fromJson(json, "uuid")
    UploadThreadGokb.Status status = new UploadThreadGokb.Status(JsonToolkit.fromJson(json, "status"))
    UploadJobFrame result = new UploadJobFrame(fileType)
    result.uuid = uuid
    result.status = status
    result
  }

}
