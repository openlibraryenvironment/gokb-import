package ygor.field

import org.apache.commons.lang.StringUtils

class FieldKeyMapping {

    String ygorKey
    Set kbartKeys
    Set zdbKeys
    Set ezbKeys

    static constraints = {
        ygorKey   nullable : false
        kbartKeys nullable : false
        zdbKeys   nullable : false
        ezbKeys   nullable : false
    }

    static hasMany = [kbartKeys : String,
                      zdbKeys : String,
                      ezbKeys : String]

    FieldKeyMapping(){
        // add explicit default constructor
    }

    FieldKeyMapping(boolean dontUseDefaultConstructor, def mappings){
        if (mappings == null || !(mappings instanceof Map<?, ?>) ||
                mappings.size() != MappingsContainer.OBLIGATORY_KEYS.size()) {
            throw IllegalArgumentException("Illegal mapping argument given for FieldKeyMapping configuration: "
                    .concat(mappings))
        }
        for (obligatoryKey in MappingsContainer.OBLIGATORY_KEYS){
            boolean found = false
            for (mapping in mappings){
                if (mapping.getKey() == obligatoryKey){
                    found = true
                }
            }
            if (!found){
                throw NoSuchElementException("Missing ".concat(obligatoryKey)
                        .concat(" in given FieldKeyMapping configuration: ".concat(mappings)))
            }
        }
        for (mapping in mappings){
            if (mapping.key == MappingsContainer.YGOR){
                ygorKey = mapping.value
            }
            else if (mapping.key == MappingsContainer.KBART){
                kbartKeys = new HashSet()
                if (mapping.value instanceof Collection<?>){
                    kbartKeys.addAll(mapping.value)
                }
                else if (!StringUtils.isEmpty(mapping.value)){
                    kbartKeys.add(mapping.value)
                }
            }
            else if (mapping.key == MappingsContainer.ZDB){
                zdbKeys = new HashSet()
                if (mapping.value instanceof Collection<?>){
                    zdbKeys.addAll(mapping.value)
                }
                else if (!StringUtils.isEmpty(mapping.value)){
                    zdbKeys.add(mapping.value)
                }
            }
            else if (mapping.key == MappingsContainer.EZB){
                ezbKeys = new HashSet()
                if (mapping.value instanceof Collection<?>){
                    ezbKeys.addAll(mapping.value)
                }
                else if (!StringUtils.isEmpty(mapping.value)){
                    ezbKeys.add(mapping.value)
                }
            }
        }
    }


    /**
     * @param type One of {MappingsContainer.@value YGOR}, { MappingsContainer.@value KBART},
     * {MappingsContainer.@value ZDB} or {MappingsContainer.@value EZB}
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
    }
}
