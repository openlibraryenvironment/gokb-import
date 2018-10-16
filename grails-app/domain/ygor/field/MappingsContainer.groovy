package ygor.field

import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import org.apache.commons.lang.StringUtils
import ygor.identifier.EissnIdentifier
import ygor.identifier.PissnIdentifier
import ygor.identifier.ZdbIdentifier

import java.nio.file.Paths

@Log4j
class MappingsContainer {

    final private static JsonSlurper SLURPY = new JsonSlurper()
    private static String MAPPINGS_FILE =
            Paths.get("src/java/resources/YgorFieldKeyMapping.json").toAbsolutePath().toString()

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
        try{
            initialize(mappingsFile)
        }
        catch(MissingFieldException mfe){
            log("Incomplete mapping.\n" + mfe)
        }
    }


    def initialize(String mappingsFile) throws MissingFieldException{
        ygorMappings = [:]
        kbartMappings = [:]
        zdbMappings = [:]
        ezbMappings = [:]
        readMappingsFile(new File(mappingsFile))
    }


    def readMappingsFile(File mappingsFile) throws MissingFieldException{
        if (mappingsFile) {
            def json = SLURPY.parse(mappingsFile)
            json.each { map ->
                FieldKeyMapping mapping = jsonNodeToMapping(map)
                putMapping(mapping)
            }
        }
    }


    static FieldKeyMapping jsonNodeToMapping(def json) throws MissingFieldException{
        // ygor key must exist and is not allowed to have an empty value
        if (json.ygor == null || StringUtils.isEmpty(json.ygor)){
            throw new MissingFieldException("Missing YgorFieldKey entry in ".concat(json))
        }
        // other keys are allowed to be empty
        if (json.kbart == null){
            throw new MissingFieldException("Missing Kbart key entry in ".concat(json))
        }
        if (json.zdb == null){
            throw new MissingFieldException("Missing ZDB key entry in ".concat(json))
        }
        if (json.ezb == null){
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
        return [ZdbIdentifier.FIELD_KEY_MAPPING, PissnIdentifier.FIELD_KEY_MAPPING, EissnIdentifier.FIELD_KEY_MAPPING]
    }
}
