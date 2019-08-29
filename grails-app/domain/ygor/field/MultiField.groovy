package ygor.field

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import de.hbznrw.ygor.normalizers.CommonNormalizer
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.validators.Validator

import java.util.regex.Matcher
import java.util.regex.Pattern

class MultiField {

    String ygorFieldKey
    FieldKeyMapping keyMapping          // TODO: keep in MappingsContainer only and access by ygorFieldKey (?)
    Map fields = [:]
    String type                         // TODO: move to FieldKeyMapping (?)
    String status
    String normalized = null

    static hasMany = [fields : Field]

    final private static Pattern FIXED_PATTERN = Pattern.compile("\\{fixed=(.*)}")

    static constraints = {
    }


    MultiField(FieldKeyMapping fieldKeyMapping){
        if (fieldKeyMapping != null){
            this.ygorFieldKey = fieldKeyMapping.ygorKey
            this.type = fieldKeyMapping.type
            keyMapping = fieldKeyMapping
        }
    }


    def addField(String source, String key, String value){
        if (keyMapping == null){
            fields.put(source, new Field(source, key, value))
        }
        else{
            for (mappedKey in keyMapping.get(source)){
                if (key == mappedKey){
                    fields.put(source, new Field(source, mappedKey, value))
                    break
                }
                // else: is there already a value with a higher prio?
                if (fields.get(source) != null){
                    break
                }
            }
        }
    }


    String getPrioValue(){
        if (normalized != null){
            return normalized
        }
        if (keyMapping == null){
            return fields.values().toArray()[0]
        }
        if (keyMapping.valIsFix){
            return extractFixedValue(keyMapping.val)
        }
        // no fixed value --> search for collected values
        for (source in keyMapping.sourcePrio){
            def field = fields.get(source)
            if (field != null){
                return field.value
            }
        }
        // no collected value --> return default value (if any)
        return keyMapping.val
    }


    String getPrioSource(){
        if (keyMapping == null){
            return "kbart"
        }
        if (keyMapping.valIsFix){
            return "default"
        }
        // no fixed value --> search for collected values
        for (source in keyMapping.sourcePrio){
            def field = fields.get(source)
            if (field != null){
                return source
            }
        }
        // no collected value --> return default value (if any)
        return "default"
    }


    void normalize(String namespace){
        normalized = CommonNormalizer.normalize(this, type, namespace)
    }


    void validate(String namespace){
        status = Validator.validate(type, getPrioValue(), ygorFieldKey, namespace)
    }


    String toString(){
        this.getClass().getName().concat(": ").concat(ygorFieldKey).concat(", fields: ").concat(fields.toString())
    }


    String asJson(JsonGenerator jsonGenerator){
        jsonGenerator.writeStartObject()
        jsonGenerator.writeStringField("ygorKey", ygorFieldKey)
        jsonGenerator.writeStringField("status", status)
        jsonGenerator.writeStringField("normalized", normalized)

        jsonGenerator.writeFieldName("fields")
        jsonGenerator.writeStartArray()
        for (Field f in fields.values()){
            f.asJson(jsonGenerator)
        }
        jsonGenerator.writeEndArray()
        jsonGenerator.writeEndObject()
    }


    static MultiField fromJson(JsonNode json, FieldKeyMapping mapping){
        MultiField result = new MultiField(mapping)
        if (mapping == null){
            result.ygorFieldKey = JsonToolkit.fromJson(json, "ygorKey")
        }
        result.status = JsonToolkit.fromJson(json, "status")
        result.normalized = JsonToolkit.fromJson(json, "normalized")
        Iterator it = json.path("fields").iterator()
        while (it.hasNext()){
            JsonNode fieldNode = it.next()
            String source = JsonToolkit.fromJson(fieldNode, "source")
            String key = JsonToolkit.fromJson(fieldNode, "key")
            String value = JsonToolkit.fromJson(fieldNode, "value")
            result.addField(source, key, value)
        }
        result
    }


    static String extractFixedValue(String value){
        Matcher fixedMatcher = FIXED_PATTERN.matcher(value)
        if (fixedMatcher.matches()){
            value = fixedMatcher.group(1)
        }
        value
    }
}
