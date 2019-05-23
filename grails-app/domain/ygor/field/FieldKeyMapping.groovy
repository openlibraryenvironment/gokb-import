package ygor.field

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang.StringUtils
import org.apache.commons.lang.reflect.FieldUtils

class FieldKeyMapping {

    final static ObjectMapper MAPPER = new ObjectMapper()
    String ygorKey
    Set kbartKeys = new HashSet()
    Set zdbKeys = new HashSet()
    Set ezbKeys = new HashSet()
    String type
    Set gokb
    String val = ""
    boolean valIsFix
    List sourcePrio = MappingsContainer.DEFAULT_SOURCE_PRIO

    static constraints = {
        ygorKey   nullable : false
        type      nullable : false
        gokb      nullable : false
    }

    static hasMany = [kbartKeys : String,
                      zdbKeys : String,
                      ezbKeys : String,
                      gokb : String,
                      sourcePrio : String]

    FieldKeyMapping(){
        // add explicit default constructor
    }

    FieldKeyMapping(boolean dontUseDefaultConstructor, def mappings){
        if (mappings == null || !(mappings instanceof Map<?, ?>)) {
            throw IllegalArgumentException("Illegal mapping argument given for FieldKeyMapping configuration: "
                    .concat(mappings))
        }
        parseMapping(mappings)
    }

    private void parseMapping(Map<?, ?> mappings) {
        for (mapping in mappings) {
            switch (mapping.key){
                case MappingsContainer.YGOR:
                    ygorKey = mapping.value
                    break
                case MappingsContainer.KBART:
                    if (mapping.value instanceof Collection<?>) {
                        kbartKeys.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value.toString())) {
                        kbartKeys.add(mapping.value)
                    }
                    break
                case MappingsContainer.ZDB:
                    if (mapping.value instanceof Collection<?>) {
                        zdbKeys.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value.toString())) {
                        zdbKeys.add(mapping.value)
                    }
                    break
                case MappingsContainer.EZB:
                    if (mapping.value instanceof Collection<?>) {
                        ezbKeys.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value.toString())) {
                        ezbKeys.add(mapping.value)
                    }
                    break
                case MappingsContainer.TYPE:
                    type = mapping.value
                    break
                case MappingsContainer.GOKB:
                    gokb = new HashSet<>()
                    if (mapping.value instanceof Collection<?>) {
                        gokb.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value.toString())) {
                        gokb.add(mapping.value)
                    }
                    break
                case "default":
                    val = mapping.value
                    valIsFix = false
                    break
                case "fixed":
                    val = mapping.value
                    valIsFix = true
                    break
                case "in":
                    parseMapping(mapping.value)
                    break
                case "out":
                    parseMapping(mapping.value)
                    break
                case "value":
                    if (!mapping.value instanceof String){
                        parseMapping(mapping.value)
                    }
                    break
                default:
                    continue
            }
        }
    }


    /**
     * @param source One of {MappingsContainer.@value YGOR}, { MappingsContainer.@value KBART},
     * {MappingsContainer.@value ZDB}, {MappingsContainer.@value EZB} or
     * {MappingsContainer.@value TYPE}
     * @return The values of the given source.
     */
    Collection get(String source){
        if (source == MappingsContainer.YGOR){
            [ygorKey]
        }
        else if (source == MappingsContainer.KBART){
            kbartKeys
        }
        else if (source == MappingsContainer.ZDB){
            zdbKeys
        }
        else if (source == MappingsContainer.EZB){
            ezbKeys
        }
        else if (source == MappingsContainer.TYPE){
            [type]
        }
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


    void asJson(JsonGenerator jsonGenerator){
        jsonGenerator.writeStartObject()
        jsonGenerator.writeStringField("ygorKey", ygorKey)
        jsonGenerator.writeStringField("type", type)
        jsonGenerator.writeStringField("value", val)
        jsonGenerator.writeStringField("valueIsFix", String.valueOf(valIsFix))

        jsonGenerator.writeFieldName("kbartKeys")
        jsonGenerator.writeStartArray()
        for (String kk in kbartKeys){
            jsonGenerator.writeString(kk)
        }
        jsonGenerator.writeEndArray()

        jsonGenerator.writeFieldName("zdbKeys")
        jsonGenerator.writeStartArray()
        for (String zk in zdbKeys){
            jsonGenerator.writeString(zk)
        }
        jsonGenerator.writeEndArray()

        jsonGenerator.writeFieldName("ezbKeys")
        jsonGenerator.writeStartArray()
        for (String ek in ezbKeys){
            jsonGenerator.writeString(ek)
        }
        jsonGenerator.writeEndArray()

        jsonGenerator.writeFieldName("gokbFields")
        jsonGenerator.writeStartArray()
        for (String gf in gokb){
            jsonGenerator.writeString(gf)
        }
        jsonGenerator.writeEndArray()

        jsonGenerator.writeFieldName("sourcePrio")
        jsonGenerator.writeStartArray()
        for (String sp in sourcePrio){
            jsonGenerator.writeString(sp)
        }
        jsonGenerator.writeEndArray()

        jsonGenerator.writeEndObject()
    }


    static FieldKeyMapping fromJson(JsonNode jsonNode){
        Map<String, Object> map = MAPPER.convertValue(jsonNode, Map.class)
        return new FieldKeyMapping(false, map)
    }
}
