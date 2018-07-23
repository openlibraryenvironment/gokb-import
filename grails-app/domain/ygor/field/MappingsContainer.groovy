package ygor.field

import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils

class MappingsContainer {

    final private static JsonSlurper SLURPY = new JsonSlurper()
    private static URL MAPPINGS_URL =
            getClass().getResource("../../../../../java/resources/YgorFieldKeyMapping.json")

    private static Map YGOR_MAPPINGS
    private static Map KBART_MAPPINGS
    private static Map ZDB_MAPPINGS
    private static Map EZB_MAPPINGS

    static hasMany = [YGOR_MAPPINGS : FieldKeyMapping, KBART_MAPPINGS : FieldKeyMapping,
                      ZDB_MAPPINGS : FieldKeyMapping, EZB_MAPPINGS : FieldKeyMapping]


    MappingsContainer(){
        initialize(MAPPINGS_URL)
    }

    MappingsContainer(URL mappingsFile){
        initialize(mappingsFile)
    }


    def initialize(File mappingsFile){
        YGOR_MAPPINGS = [:]
        KBART_MAPPINGS = [:]
        ZDB_MAPPINGS = [:]
        EZB_MAPPINGS = [:]
        readMappingsUrl(mappingsFile)
    }

    def readMappingsUrl(File mappingsFile){
        def json = SLURPY.parse(mappingsFile)
        json.each {map ->
            FieldKeyMapping mapping = jsonNodeToMapping(map)
            putMapping(mapping)
        }
    }


    static FieldKeyMapping jsonNodeToMapping(def json){
        if (!json.ygor | StringUtils.isEmpty(json.ygor)){
            throw new MissingFieldException("Missing YgorFieldKey entry in ".concat(json))
        }
        if (!json.kbart | StringUtils.isEmpty(json.kbart)){
            throw new MissingFieldException("Missing Kbart key entry in ".concat(json))
        }
        if (!json.zdb | StringUtils.isEmpty(json.zdb)){
            throw new MissingFieldException("Missing ZDB key entry in ".concat(json))
        }
        if (!json.ezb | StringUtils.isEmpty(json.ezb)){
            throw new MissingFieldException("Missing EZB key entry in ".concat(json))
        }
        new FieldKeyMapping(json)
    }


    private def putMapping(FieldKeyMapping mapping){
        YGOR_MAPPINGS.put(mapping.ygorKey, mapping)
        KBART_MAPPINGS.put(mapping.kbartKey, mapping)
        ZDB_MAPPINGS.put(mapping.zdbKey, mapping)
        EZB_MAPPINGS.put(mapping.ezbKey, mapping)
    }


    /**
     * @param key The key to identify the mapping.
     * @param type One of {@value FieldKeyMapping#YGOR}, {@value FieldKeyMapping#KBART}, {@value FieldKeyMapping#ZDB} or
     *      {@value FieldKeyMapping#EZB}
     * @return A mapping with keys for each FieldKeyMapping type.
     */
    static def getMapping(String key, String type){
        if (type == FieldKeyMapping.YGOR){
            return YGOR_MAPPINGS.get(key)
        }
        if (type == FieldKeyMapping.KBART){
            return KBART_MAPPINGS.get(key)
        }
        if (type == FieldKeyMapping.ZDB){
            return ZDB_MAPPINGS.get(key)
        }
        if (type == FieldKeyMapping.EZB){
            return EZB_MAPPINGS.get(key)
        }
    }

}
