package ygor.field

import org.apache.commons.lang.StringUtils

class FieldKeyMapping {

    String ygorKey
    Set kbartKeys = new HashSet()
    Set zdbKeys = new HashSet()
    Set ezbKeys = new HashSet()
    String type
    Set gokb
    String val = ""
    boolean valIsFix

    static constraints = {
        ygorKey   nullable : false
        type      nullable : false
        gokb      nullable : false
    }

    static hasMany = [kbartKeys : String,
                      zdbKeys : String,
                      ezbKeys : String,
                      gokb : String]

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
                    if (mapping.value instanceof Collection<?>) {
                        kbartKeys.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value.toString())) {
                        kbartKeys.add(mapping.value)
                    }
                    break
                case MappingsContainer.ZDB:
                    if (mapping.value instanceof Collection<?>) {
                        zdbKeys.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value.toString())) {
                        zdbKeys.add(mapping.value)
                    }
                    break
                case MappingsContainer.EZB:
                    if (mapping.value instanceof Collection<?>) {
                        ezbKeys.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value.toString())) {
                        ezbKeys.add(mapping.value)
                    }
                    break
                case MappingsContainer.TYPE:
                    type = mapping.value
                    break
                case MappingsContainer.GOKB:
                    gokb = new HashSet<>()
                    if (mapping.value instanceof Collection<?>) {
                        gokb.addAll(mapping.value)
                    }
                    else if (!StringUtils.isEmpty(mapping.value.toString())) {
                        gokb.add(mapping.value)
                    }
                    break
                case "default":
                    val = mapping.value
                    valIsFix = false
                    break
                case "fixed":
                    val = mapping.value
                    valIsFix = true
                    break
                case "in":
                    parseMapping(mapping.value)
                    break
                case "out":
                    parseMapping(mapping.value)
                    break
                case "val":
                    parseMapping(mapping.value)
                    break
            }
        }
    }


    /**
     * @param source One of {MappingsContainer.@value YGOR}, { MappingsContainer.@value KBART},
     * {MappingsContainer.@value ZDB}, {MappingsContainer.@value EZB} or
     * {MappingsContainer.@value TYPE}
     * @return The value of the given source.
     */
    def get(String source){
        if (source == MappingsContainer.YGOR){
            ygorKey
        }
        else if (source == MappingsContainer.KBART){
            kbartKeys
        }
        else if (source == MappingsContainer.ZDB){
            zdbKeys
        }
        else if (source == MappingsContainer.EZB){
            ezbKeys
        }
        else if (source == MappingsContainer.TYPE){
            type
        }
    }
}
