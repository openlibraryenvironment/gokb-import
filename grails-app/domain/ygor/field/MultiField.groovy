package ygor.field

import org.apache.commons.lang.StringUtils
import ygor.source.EzbSource
import ygor.source.KbartSource
import ygor.source.SourceInterface
import ygor.source.ZdbSource

class MultiField {

    static List<String> DEFAULT_SOURCE_PRIO = [FieldKeyMapping.ZDB, FieldKeyMapping.KBART, FieldKeyMapping.EZB]
    List<String> sourcePrio
    String ygorFieldKey
    FieldKeyMapping keys
    Map<String, Field> fields = [:]

    static constraints = {
    }


    MultiField(String ygorFieldKey){
        this.ygorFieldKey = ygorFieldKey
        keys = MappingsContainer.getMapping(ygorFieldKey, FieldKeyMapping.YGOR)
        this.sourcePrio = DEFAULT_SOURCE_PRIO
    }


    static void setSourcePrio(List<String> sourcePrio) {
        if (!sourcePrio || !(sourcePrio instanceof List) || sourcePrio.size() != DEFAULT_SOURCE_PRIO.size()){
            throw IllegalArgumentException("Illegal static list of sources given for MultiField configuration: "
                    .concat(sourcePrio))
        }
        for (necessaryKey in [FieldKeyMapping.ZDB, FieldKeyMapping.KBART, FieldKeyMapping.EZB]){
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


    def addValue(SourceInterface source, String value){
        if (source instanceof KbartSource){
            fields.put(keys.get(FieldKeyMapping.KBART), new Field(source, keys.get(FieldKeyMapping.KBART), value))
        }
        else if (source instanceof ZdbSource){
            fields.put(keys.get(FieldKeyMapping.ZDB), new Field(source, keys.get(FieldKeyMapping.ZDB), value))
        }
        else if (source instanceof EzbSource){
            fields.put(keys.get(FieldKeyMapping.EZB), new Field(source, keys.get(FieldKeyMapping.EZB), value))
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
