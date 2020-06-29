package ygor

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.tools.JsonToolkit
import ygor.field.FieldKeyMapping

@SuppressWarnings('JpaObjectClassSignatureInspection')
class RecordFlag{

  static mapWith = "none" // disable persisting into database

  Status status
  Colour colour
  String text
  String messageCode
  String ygorFieldKey
  String uid
  ErrorCode errorCode


  RecordFlag(Status status, String text, String messageCode, FieldKeyMapping fieldKeyMapping, ErrorCode errorCode){
    this.status = status
    this.colour = null
    this.text = text
    this.messageCode = messageCode
    this.ygorFieldKey = fieldKeyMapping.ygorKey
    this.errorCode = errorCode
    uid = UUID.randomUUID().toString()
  }


  void setColour(Colour colour){
    this.colour = colour
  }


  enum Colour{
    RED ("RED"),
    YELLOW ("YELLOW"),
    GREEN ("GREEN")

    private colour

    Colour(String colour){
      this.colour = colour
    }
  }


  void asJson(JsonGenerator jsonGenerator) {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("status", status.toString().toUpperCase())
    jsonGenerator.writeStringField("colour", colour.toString())
    jsonGenerator.writeStringField("text", text)
    jsonGenerator.writeStringField("messageCode", messageCode)
    jsonGenerator.writeStringField("ygorFieldKey", ygorFieldKey)
    jsonGenerator.writeStringField("uid", uid)
    jsonGenerator.writeStringField("errorCode", errorCode.toString())
    jsonGenerator.writeEndObject()
  }


  static RecordFlag fromJson(JsonNode json){
    RecordFlag result = new RecordFlag()
    result.status = JsonToolkit.fromJson(json, "status", Status.class)
    result.colour = JsonToolkit.fromJson(json, "colour", Colour.class)
    result.text = JsonToolkit.fromJson(json, "text")
    result.messageCode = JsonToolkit.fromJson(json, "messageCode")
    result.ygorFieldKey = JsonToolkit.fromJson(json, "ygorFieldKey")
    result.uid = JsonToolkit.fromJson(json, "uid")
    result.errorCode = JsonToolkit.fromJson(json, "errorCode", ErrorCode.class)
    result
  }


  enum ErrorCode{
    ISSUE_ONLINE_DATES_ORDER,
    ONLINE_ID_REPLACED,
    PRINT_ID_REPLACED
  }

}
