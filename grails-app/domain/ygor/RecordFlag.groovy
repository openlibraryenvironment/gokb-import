package ygor

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.tools.JsonToolkit
import ygor.field.FieldKeyMapping

class RecordFlag{

  Status status
  Colour colour
  String text
  String messageCode
  String ygorFieldKey


  RecordFlag(Status status, String text, String messageCode, FieldKeyMapping fieldKeyMapping){
    this.status = status
    this.colour = null
    this.text = text
    this.messageCode = messageCode
    this.ygorFieldKey = fieldKeyMapping.ygorKey
  }


  boolean isRed(){
    if (colour != null){
      return colour.equals(Colour.RED)
    }
    return status in [Status.INVALID]
  }


  boolean isYellow(){
    if (colour != null){
      return colour.equals(Colour.YELLOW)
    }
    return status in [Status.MISMATCH, Status.WARNING]
  }


  boolean isGreen(){
    if (colour != null){
      return colour.equals(Colour.GREEN)
    }
    return status in [Status.VALID]
  }


  void setRed(){
    colour = Colour.RED
  }


  void setYellow(){
    colour = Colour.YELLOW
  }


  void setGreen(){
    colour = Colour.GREEN
  }


  enum Colour{
    RED ("red"),
    YELLOW ("yellow"),
    GREEN ("green")

    private colour

    Colour(String colour){
      this.colour = colour
    }
  }


  void asJson(JsonGenerator jsonGenerator) {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("status", status.toString())
    jsonGenerator.writeStringField("colour", colour.toString())
    jsonGenerator.writeStringField("text", text)
    jsonGenerator.writeStringField("messageCode", messageCode)
    jsonGenerator.writeStringField("ygorFieldKey", ygorFieldKey)
    jsonGenerator.writeEndObject()
  }


  static RecordFlag fromJson(JsonNode json){
    RecordFlag result = new RecordFlag()
    result.status = JsonToolkit.fromJson(json, "status", Status.getClass())
    result.colour = JsonToolkit.fromJson(json, "colour", Colour.getClass())
    result.text = JsonToolkit.fromJson(json, "text")
    result.messageCode = JsonToolkit.fromJson(json, "messageCode")
    result.ygorFieldKey = JsonToolkit.fromJson(json, "ygorFieldKey")
    result
  }

}
