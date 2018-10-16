package ygor.field

import ygor.source.AbstractSource


class FieldKeyMapping {

    final static String YGOR    = "ygor"
    final static String KBART   = "kbart"
    final static String ZDB     = "zdb"
    final static String EZB     = "ezb"
    final static OBLIGATORY_KEYS = [YGOR, KBART, ZDB, EZB]

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

    FieldKeyMapping(Map<AbstractSource, String> mappings){
        if (!mappings || !(mappings instanceof Map<?, ?>) || mappings.size() != OBLIGATORY_KEYS.size()) {
            throw IllegalArgumentException("Illegal mapping argument given for FieldKeyMapping configuration: "
                    .concat(mappings))
        }
        for (obligatoryKey in OBLIGATORY_KEYS){
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
            if (mapping.key == YGOR){
                ygorKey = mapping.key
            }
            else if (mapping.key == KBART){
                kbartKey = mapping.key
            }
            else if (mapping.key == ZDB){
                zdbKey = mapping.key
            }
            else if (mapping.key == EZB){
                ezbKey = mapping.key
            }
        }
    }


    /**
     * @param type One of {@value #YGOR}, {@value #KBART}, {@value #ZDB} or {@value #EZB}
     * @return The value of the given type.
     */
    def get(String type){
        if (type == YGOR){
            ygorKey
        }
        else if (type == KBART){
            kbartKey
        }
        else if (type == ZDB){
            zdbKey
        }
        else if (type == EZB){
            ezbKey
        }
    }
}
