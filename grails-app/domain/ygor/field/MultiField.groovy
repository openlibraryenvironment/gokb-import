package ygor.field

import ygor.source.EzbSource

class MultiField {

    String ygorFieldKey
    FieldKeyMapping keyMapping
    Map fields = [:]
    List sourcePrio = []
    static hasMany = [sourcePrio : String, fields : Field]

    static constraints = {
    }


    MultiField(String ygorFieldKey){
        this(MappingsContainer.getMapping(ygorFieldKey, MappingsContainer.YGOR))
    }


    MultiField(FieldKeyMapping fieldKeyMapping){
        this.ygorFieldKey = fieldKeyMapping.ygorKey
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


    def addValue(String source, String value){
        fields.put(keyMapping.get(source), new Field(source, keyMapping.get(source), value))
    }


    String getPrioValue(){
        for (source in sourcePrio){
            def field = fields.get(source)
            if (field != null){
                return field.value
            }
        }
        return null
    }

}
