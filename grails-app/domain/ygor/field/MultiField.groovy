package ygor.field

import de.hbznrw.ygor.export.Validator
import de.hbznrw.ygor.export.Normalizer

class MultiField {

    String ygorFieldKey
    FieldKeyMapping keyMapping
    Map fields = [:]
    List sourcePrio = []
    String type
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
                fields.put(keyMapping.get(source), new Field(source, mappedKey, value))
            }
        }
    }

    String getPrioValue(){
        for (source in sourcePrio){
            def field = fields.get(source)
            if (field != null){
                return Normalizer.normalize(type, field.value)
            }
        }
        return ""
    }

    void validate(){
        Validator.validate(type, getPrioValue())
    }

}
