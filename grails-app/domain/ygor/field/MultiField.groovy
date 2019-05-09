package ygor.field

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonGenerator
import de.hbznrw.ygor.export.Validator

class MultiField {

    String ygorFieldKey
    FieldKeyMapping keyMapping          // TODO: keep in MappingsContainer only and access by ygorFieldKey
    Map fields = [:]
    List sourcePrio = []                // TODO: move to FieldKeyMapping
    String type                         // TODO: move to FieldKeyMapping (?)
    String status

    static hasMany = [sourcePrio : String, fields : Field]

    static constraints = {
    }


    MultiField(FieldKeyMapping fieldKeyMapping){
        this.ygorFieldKey = fieldKeyMapping.ygorKey
        this.type = fieldKeyMapping.type
        keyMapping = fieldKeyMapping
        this.sourcePrio = MappingsContainer.DEFAULT_SOURCE_PRIO
    }


    void setSourcePrio(List<String> sourcePrio) {
        if (!sourcePrio || sourcePrio.size() != MappingsContainer.DEFAULT_SOURCE_PRIO.size()){
            throw IllegalArgumentException("Illegal static list of sources given for MultiField configuration: "
                    .concat(sourcePrio))
        }
        for (necessaryKey in [MappingsContainer.ZDB, MappingsContainer.KBART, MappingsContainer.EZB]){
            boolean found = false
            for (givenSource in sourcePrio){
                if (givenSource == necessaryKey){
                    found = true
                }
            }
            if (!found){
                throw NoSuchElementException("Missing ".concat(necessaryKey)
                        .concat(" in given MultiField configuration: ".concat(sourcePrio)))
            }
        }
        this.sourcePrio = sourcePrio
    }


    def addField(String source, String key, String value){
        for (mappedKey in keyMapping.get(source)) {
            if (key == mappedKey) {
                fields.put(source, new Field(source, mappedKey, value))
            }
        }
    }

    String getPrioValue(){
        if (keyMapping.valIsFix){
            return keyMapping.val
        }
        // no fixed value --> search for collected values
        for (source in sourcePrio){
            def field = fields.get(source)
            if (field != null){
                return field.value
            }
        }
        // no collected value --> return default value (if any)
        return keyMapping.val
    }

    void validate(String namespace){
        status = Validator.validate(type, getPrioValue(), ygorFieldKey, namespace)
    }


    String toString(){
        this.getClass().getName().concat(": ").concat(ygorFieldKey).concat(", fields: ").concat(fields.toString())
    }

    String asJson(){
        Writer writer=new StringWriter()
        JsonGenerator jsonGenerator = new JsonFactory().createGenerator(writer)
        jsonGenerator.writeStartObject()
        jsonGenerator.writeStringField("ygorKey", ygorFieldKey)
        jsonGenerator.writeStringField("status", status)

        jsonGenerator.writeFieldName("fields")
        jsonGenerator.writeStartArray()
        for (Field f in fields){
            jsonGenerator.writeString(f.asJson())
        }
        jsonGenerator.writeEndArray()
        jsonGenerator.writeEndObject()
        jsonGenerator.close()
        return writer.toString()
    }

}
