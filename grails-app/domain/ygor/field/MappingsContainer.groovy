package ygor.field

import groovy.json.JsonSlurper
import org.apache.commons.lang.StringUtils

import java.nio.file.Paths

class MappingsContainer {

    final private static JsonSlurper SLURPY = new JsonSlurper()
    private static String MAPPINGS_FILE = Paths.get("src/java/resources/YgorFieldKeyMapping.json").toAbsolutePath().toString()

    Map ygorMappings
    Map kbartMappings
    Map zdbMappings
    Map ezbMappings
    static hasMany = [ygorMappings : FieldKeyMapping, kbartMappings : FieldKeyMapping,
                      zdbMappings : FieldKeyMapping, ezbMappings : FieldKeyMapping]


    MappingsContainer(){
        initialize(MAPPINGS_FILE)
    }


    MappingsContainer(String mappingsFile){
        initialize(mappingsFile)
    }


    def initialize(String mappingsFile){
        ygorMappings = [:]
        kbartMappings = [:]
        zdbMappings = [:]
        ezbMappings = [:]
        readMappingsFile(new File(mappingsFile))
    }


    def readMappingsFile(File mappingsFile){
        if (mappingsFile) {
            def json = SLURPY.parse(mappingsFile)
            json.each { map ->
                FieldKeyMapping mapping = jsonNodeToMapping(map)
                putMapping(mapping)
            }
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
        ygorMappings.put(mapping.ygorKey, mapping)
        kbartMappings.put(mapping.kbartKey, mapping)
        zdbMappings.put(mapping.zdbKey, mapping)
        ezbMappings.put(mapping.ezbKey, mapping)
    }


    /**
     * @param key The key to identify the mapping.
     * @param type One of {@value FieldKeyMapping#YGOR}, {@value FieldKeyMapping#KBART}, {@value FieldKeyMapping#ZDB} or
     *      {@value FieldKeyMapping#EZB}
     * @return A mapping with keys for each FieldKeyMapping type.
     */
    def getMapping(String key, String type){
        if (type == FieldKeyMapping.YGOR){
            return ygorMappings.get(key)
        }
        if (type == FieldKeyMapping.KBART){
            return kbartMappings.get(key)
        }
        if (type == FieldKeyMapping.ZDB){
            return zdbMappings.get(key)
        }
        if (type == FieldKeyMapping.EZB){
            return ezbMappings.get(key)
        }
    }


    def getAllIdFieldKeyMappings(){
        // TODO
    }


    def getIdentifier(FieldKeyMapping mapping){
        // TODO
    }
}
