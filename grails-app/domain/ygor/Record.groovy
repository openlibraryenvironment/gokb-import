package ygor

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.enums.Status
import de.hbznrw.ygor.normalizers.EditionNormalizer
import de.hbznrw.ygor.tools.JsonToolkit
import de.hbznrw.ygor.validators.RecordValidator
import ygor.field.MappingsContainer
import ygor.field.MultiField
import ygor.identifier.*

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
    Map             multiFields
    Map             validation
    String          zdbIntegrationDate
    String          ezbIntegrationDate


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
        validation = [:]
        for (def ygorMapping in container.ygorMappings){
            multiFields.put(ygorMapping.key, new MultiField(ygorMapping.value))
        }
        zdbIntegrationDate = null
        ezbIntegrationDate = null
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


    List<MultiField> getIdentifierFields(){
        List<MultiField> result = []
        if (multiFields.get("ezbId")) result.add(multiFields.get("ezbId"))
        if (multiFields.get("onlineIdentifier")) result.add(multiFields.get("onlineIdentifier"))
        if (multiFields.get("parentPublicationTitleId")) result.add(multiFields.get("parentPublicationTitleId"))
        if (multiFields.get("precedingPublicationTitleId")) result.add(multiFields.get("precedingPublicationTitleId"))
        if (multiFields.get("printIdentifier")) result.add(multiFields.get("printIdentifier"))
        if (multiFields.get("titleId")) result.add(multiFields.get("titleId"))
        if (multiFields.get("zdbId")) result.add(multiFields.get("zdbId"))
        result
    }


    void normalize(String namespace){
        EditionNormalizer.normalizeEditionNumber(this)
        for (MultiField multiField in multiFields.values()){
            multiField.normalize(namespace)
        }
    }


    boolean isValid(){
        // validate tipp.titleUrl
        MultiField urlMultiField = multiFields.get("titleUrl")
        if (urlMultiField == null || urlMultiField.status != Status.VALIDATOR_URL_IS_VALID.toString()){
            return false
        }
        return true
    }


    void validate(String namespace){
        this.validateMultifields(namespace)
        RecordValidator.validateCoverage(this)
        RecordValidator.validateHistoryEvent(this)
        RecordValidator.validatePublisherHistory(this)
    }


    void addValidation(String property, Status status){
        validation.put(property, status)
    }

    Status getValidation(String property){
        return validation.get(property)
    }


    void addMultiField(MultiField multiField){
        multiFields.put(multiField.ygorFieldKey, multiField)
    }

    MultiField getMultiField(def ygorFieldKey){
        multiFields.get(ygorFieldKey)
    }

    private void validateMultifields(String namespace){
        multiFields.each{k,v -> v.validate(namespace)}
    }


    def getCoverage(){
        false // TODO
    }


    String asJson(JsonGenerator jsonGenerator){
        jsonGenerator.writeStartObject()
        jsonGenerator.writeStringField("uid", uid)
        jsonGenerator.writeStringField("zdbId", zdbId?.identifier)
        jsonGenerator.writeStringField("ezbId", ezbId?.identifier)
        jsonGenerator.writeStringField("doiId", doiId?.identifier)
        jsonGenerator.writeStringField("eissn", eissn?.identifier)
        jsonGenerator.writeStringField("pissn", pissn?.identifier)
        if (ezbIntegrationDate){
            jsonGenerator.writeStringField("ezbIntegrationDate", ezbIntegrationDate)
        }
        if (zdbIntegrationDate){
            jsonGenerator.writeStringField("zdbIntegrationDate", zdbIntegrationDate)
        }

        jsonGenerator.writeFieldName("multiFields")
        jsonGenerator.writeStartArray()
        for (MultiField mf in multiFields.values()){
            mf.asJson(jsonGenerator)
        }
        jsonGenerator.writeEndArray()
        jsonGenerator.writeEndObject()
    }


    Map<String, String> asMultiFieldMap(){
        Map<String, String> result = [:]
        result.put("uid", uid)
        if (ezbIntegrationDate){
            result.put("ezbIntegrationDate", ezbIntegrationDate)
        }
        if (zdbIntegrationDate){
            result.put("zdbIntegrationDate", zdbIntegrationDate)
        }
        for (def multiField in multiFields){
            result.put(multiField.key, multiField.value.getPrioValue())
        }
        result
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
        String ezbIntegrationDate = JsonToolkit.fromJson(json, "ezbIntegrationDate")
        if (ezbIntegrationDate){
            result.ezbIntegrationDate = ezbIntegrationDate
        }
        String zdbIntegrationDate = JsonToolkit.fromJson(json, "zdbIntegrationDate")
        if (zdbIntegrationDate){
            result.zdbIntegrationDate = zdbIntegrationDate
        }
        result
    }

}
