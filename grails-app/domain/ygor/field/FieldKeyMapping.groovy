package ygor.field

import org.slf4j.Logger

class FieldKeyMapping {

    String ygorKey
    String kbartKey
    String zdbKey
    String ezbKey

    static constraints = {
        ygorKey  nullable : false
        kbartKey nullable : true
        zdbKey   nullable : true
        ezbKey   nullable : true
    }

    FieldKeyMapping(){
        // add explicit default constructor
    }

    FieldKeyMapping(Map<String, String> mappings){
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
                ygorKey = mapping.key
            }
            else if (mapping.key == MappingsContainer.KBART){
                kbartKey = mapping.key
            }
            else if (mapping.key == MappingsContainer.ZDB){
                zdbKey = mapping.key
            }
            else if (mapping.key == MappingsContainer.EZB){
                ezbKey = mapping.key
            }
        }
    }


    /**
     * @param type One of {@value #MappingsContainer.YGOR}, {@value #KMappingsContainer.BART},
     * {@value #MappingsContainer.ZDB} or {@value #MappingsContainer.EZB}
     * @return The value of the given type.
     */
    def get(String type){
        if (type == MappingsContainer.YGOR){
            ygorKey
        }
        else if (type == MappingsContainer.KBART){
            kbartKey
        }
        else if (type == MappingsContainer.ZDB){
            zdbKey
        }
        else if (type == MappingsContainer.EZB){
            ezbKey
        }
    }
}
