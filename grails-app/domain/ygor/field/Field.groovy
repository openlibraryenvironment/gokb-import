package ygor.field

import com.fasterxml.jackson.core.JsonGenerator


@SuppressWarnings('JpaObjectClassSignatureInspection')
class Field {

  static mapWith = "none" // disable persisting into database

  String source
  String key
  int index
  String value

  static constraints = {
    source nullable: false
    key nullable: false
    value nullable: false
  }


  Field(String source, String key, String value) {
    this.source = source
    this.key = key
    this.index = 0 // default index is 0 because for most fields we expect only 1 entry
    this.value = value
  }


  Field(String source, String key, int index, String value) {
    this(source, key, value)
    this.index = index
  }


  String toString() {
    "Field: ".concat(source).concat("-").concat(key).concat(": ").concat(value)
  }


  void asJson(JsonGenerator jsonGenerator) {
    jsonGenerator.writeStartObject()
    jsonGenerator.writeStringField("source", source)
    jsonGenerator.writeStringField("key", key)
    jsonGenerator.writeNumberField("index", index)
    jsonGenerator.writeStringField("value", value)
    jsonGenerator.writeEndObject()
  }

}
