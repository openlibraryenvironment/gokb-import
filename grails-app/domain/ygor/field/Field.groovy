package ygor.field

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator


class Field {

    String source
    String key
    String value

    static constraints = {
        source nullable : false
        key    nullable : false
        value  nullable : false
    }

    Field(String source, String key, String value){
        this.source = source
        this.key = key
        this.value = value
    }

    String toString(){
        "Field: ".concat(source).concat("-").concat(key).concat(": ").concat(value)
    }

    String asJson(JsonGenerator jsonGenerator){
        jsonGenerator.writeStartObject()
        jsonGenerator.writeStringField("source", source)
        jsonGenerator.writeStringField("key", key)
        jsonGenerator.writeStringField("value", value)
        jsonGenerator.writeEndObject()
    }

}
