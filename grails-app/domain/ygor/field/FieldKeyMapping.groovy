package ygor.field

import org.apache.commons.lang.StringUtils

class FieldKeyMapping {

    String ygorKey
    Set kbartKeys
    Set zdbKeys
    Set ezbKeys
    String type

    static constraints = {
        ygorKey   nullable : false
        kbartKeys nullable : false
        zdbKeys   nullable : false
        ezbKeys   nullable : false
        type      nullable : false
    }

    static hasMany = [kbartKeys : String,
                      zdbKeys : String,
                      ezbKeys : String]

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
                    kbartKeys = new HashSet()
                    if (mapping.value instanceof Collection<?>) {
                        kbartKeys.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value)) {
                        kbartKeys.add(mapping.value)
                    }
                    break
                case MappingsContainer.ZDB:
                    zdbKeys = new HashSet()
                    if (mapping.value instanceof Collection<?>) {
                        zdbKeys.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value)) {
                        zdbKeys.add(mapping.value)
                    }
                    break
                case MappingsContainer.EZB:
                    ezbKeys = new HashSet()
                    if (mapping.value instanceof Collection<?>) {
                        ezbKeys.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value)) {
                        ezbKeys.add(mapping.value)
                    }
                    break
                case MappingsContainer.TYPE:
                    type = mapping.value
                case "in":
                    parseMapping(mapping.value)
                    break
                case "in":
                    parseMapping(mapping.value)
                    break
            }
        }
    }


    /**
     * @param type One of {MappingsContainer.@value YGOR}, { MappingsContainer.@value KBART},
     * {MappingsContainer.@value ZDB}, {MappingsContainer.@value EZB} or
     * {MappingsContainer.@value TYPE}
     * @return The value of the given type.
     */
    def get(String type){
        if (type == MappingsContainer.YGOR){
            ygorKey
        }
        else if (type == MappingsContainer.KBART){
            kbartKeys
        }
        else if (type == MappingsContainer.ZDB){
            zdbKeys
        }
        else if (type == MappingsContainer.EZB){
            ezbKeys
        }
        else if (type == MappingsContainer.TYPE){
            this.type
        }
    }
}
