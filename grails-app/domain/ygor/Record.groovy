package ygor

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import de.hbznrw.ygor.tools.JsonToolkit
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.AbstractIdentifier
import ygor.identifier.DoiIdentifier
import ygor.identifier.EissnIdentifier
import ygor.identifier.EzbIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier

class Record {

    static ObjectMapper MAPPER = new ObjectMapper()
    static{
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    }

    String          uid
    ZdbIdentifier   zdbId
    EzbIdentifier   ezbId
    DoiIdentifier   doiId
    EissnIdentifier eissn
    PissnIdentifier pissn
    Map multiFields
    Map validation


    static hasMany = [multiFields : MultiField,
                      validation : String]

    static constraints = {
    }

    Record(List<AbstractIdentifier> ids, MappingsContainer container){
        this(ids, container, null)
    }

    Record(List<AbstractIdentifier> ids, MappingsContainer container, String uid){
        if (null == uid) {
            this.uid = UUID.randomUUID().toString()
        }
        else{
            this.uid = uid
        }
        for (id in ids){
            addIdentifier(id)
        }
        multiFields = [:]
        for (def ygorMapping in container.ygorMappings){
            multiFields.put(ygorMapping.key, new MultiField(ygorMapping.value))
        }
    }

    void addIdentifier(AbstractIdentifier identifier){
        if (identifier instanceof ZdbIdentifier){
            if (zdbId){
                throw new IllegalArgumentException("ZDB id ".concat(zdbId).concat(" already given for record"))
            }
            zdbId = identifier
        }
        else if (identifier instanceof EzbIdentifier){
            if (ezbId){
                throw new IllegalArgumentException("EZB id ".concat(ezbId).concat(" already given for record"))
            }
            ezbId = identifier
        }
        else if (identifier instanceof DoiIdentifier){
            if (doiId){
                throw new IllegalArgumentException("EISSN ".concat(doiId).concat(" already given for record"))
            }
            doiId = identifier
        }
        else if (identifier instanceof EissnIdentifier){
            if (eissn){
                throw new IllegalArgumentException("EISSN ".concat(eissn).concat(" already given for record"))
            }
            eissn = identifier
        }
        else if (identifier instanceof PissnIdentifier){
            if (pissn){
                throw new IllegalArgumentException("PISSN ".concat(pissn).concat(" already given for record"))
            }
            pissn = identifier
        }
    }


    void addValidation(String property, String status){
        validation.put(property, status)
    }

    String getValidation(String property){
        return validation.get(property)
    }


    void addMultiField(MultiField multiField){
        multiFields.put(multiField.ygorFieldKey, multiField)
    }

    MultiField getMultiField(def ygorFieldKey){
        multiFields.get(ygorFieldKey)
    }

    void validateMultifields(String namespace){
        multiFields.each{k,v -> v.validate(namespace)}
    }


    String asJson(JsonGenerator jsonGenerator){
        jsonGenerator.writeStartObject()
        jsonGenerator.writeStringField("uid", uid)
        jsonGenerator.writeStringField("zdbId", zdbId?.identifier)
        jsonGenerator.writeStringField("ezbId", ezbId?.identifier)
        jsonGenerator.writeStringField("doiId", doiId?.identifier)
        jsonGenerator.writeStringField("eissn", eissn?.identifier)
        jsonGenerator.writeStringField("pissn", pissn?.identifier)

        jsonGenerator.writeFieldName("multiFields")
        jsonGenerator.writeStartArray()
        for (MultiField mf in multiFields.values()){
            mf.asJson(jsonGenerator)
        }
        jsonGenerator.writeEndArray()
        jsonGenerator.writeEndObject()
    }


    static Record fromJson(JsonNode json, MappingsContainer mappings){
        List<AbstractIdentifier> ids = new ArrayList<>()
        ids.add(new ZdbIdentifier(JsonToolkit.fromJson(json, "zdbId"), mappings.getMapping("zdbId", MappingsContainer.YGOR)))
        ids.add(new EzbIdentifier(JsonToolkit.fromJson(json, "ezbId"), mappings.getMapping("ezbId", MappingsContainer.YGOR)))
        ids.add(new DoiIdentifier(JsonToolkit.fromJson(json, "doiId"), mappings.getMapping("doiId", MappingsContainer.YGOR)))
        ids.add(new EissnIdentifier(JsonToolkit.fromJson(json, "eissn"), mappings.getMapping("eissn", MappingsContainer.YGOR)))
        ids.add(new PissnIdentifier(JsonToolkit.fromJson(json, "pissn"), mappings.getMapping("pissn", MappingsContainer.YGOR)))
        String uid = JsonToolkit.fromJson(json, "uid")
        Record result = new Record(ids, mappings, uid)
        Iterator it = ((ArrayNode)(json.path("multiFields"))).iterator()
        while (it.hasNext()){
            ObjectNode nextNode = it.next()
            String ygorKey = JsonToolkit.fromJson(nextNode, "ygorKey")
            result.addMultiField(MultiField.fromJson(nextNode, mappings.getMapping(ygorKey, MappingsContainer.YGOR)))
        }
        result
    }

}
