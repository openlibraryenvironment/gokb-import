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

    void validate(){
        Validator.validate(type, getPrioValue(), ygorFieldKey, "doi")
        // TODO: fix hard-coded namespace
    }


    String toString(){
        this.getClass().getName().concat(": ").concat(ygorFieldKey).concat(", fields: ").concat(fields.toString())
    }

}
