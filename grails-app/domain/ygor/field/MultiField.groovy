package ygor.field

import ygor.source.AbstractSource
import ygor.source.EzbSource
import ygor.source.KbartSource
import ygor.source.ZdbSource

class MultiField {

    String ygorFieldKey
    FieldKeyMapping keys
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
        keys = fieldKeyMapping
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
                if (givenSource.getClass() == necessaryKey){
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


    def addValue(AbstractSource source, String value){
        if (source instanceof KbartSource){
            fields.put(keys.get(MappingsContainer.KBART), new Field(source, keys.get(MappingsContainer.KBART), value))
        }
        else if (source instanceof ZdbSource){
            fields.put(keys.get(MappingsContainer.ZDB), new Field(source, keys.get(MappingsContainer.ZDB), value))
        }
        else if (source instanceof EzbSource){
            fields.put(keys.get(MappingsContainer.EZB), new Field(source, keys.get(MappingsContainer.EZB), value))
        }
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
