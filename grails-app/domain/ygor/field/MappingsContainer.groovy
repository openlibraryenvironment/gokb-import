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

    final public static String YGOR    = "ygor"
    final public static String KBART   = "kbart"
    final public static String ZDB     = "zdb"
    final public static String EZB     = "ezb"
    final public static OBLIGATORY_KEYS = [YGOR, KBART, ZDB, EZB]
    final public static DEFAULT_SOURCE_PRIO = [ZDB, KBART, EZB]

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
        if (StringUtils.isEmpty(json.ygor)){
            throw new MissingFieldException("Missing YgorFieldKey entry in ".concat(json))
        }
        // other keys are allowed to be empty Strings
        if (json.kbart == null){
            throw new MissingFieldException("Missing Kbart key entry in ".concat(json))
        }
        if (json.zdb == null){
            throw new MissingFieldException("Missing ZDB key entry in ".concat(json))
        }
        if (json.ezb == null){
            throw new MissingFieldException("Missing EZB key entry in ".concat(json))
        }
        new FieldKeyMapping(false, json)
    }


    private def putMapping(FieldKeyMapping mapping){
        if (!StringUtils.isEmpty(mapping.ygorKey)){
            ygorMappings.put(mapping.ygorKey, mapping)
        }
        if (!StringUtils.isEmpty(mapping.kbartKey)){
            kbartMappings.put(mapping.kbartKey, mapping)
        }
        if (!StringUtils.isEmpty(mapping.zdbKey)){
            zdbMappings.put(mapping.zdbKey, mapping)
        }
        if (!StringUtils.isEmpty(mapping.ezbKey)){
            ezbMappings.put(mapping.ezbKey, mapping)
        }
    }


    /**
     * @param key The key to identify the mapping.
     * @param type One of {@value #YGOR}, {@value #KBART}, {@value #ZDB} or {@value #EZB}
     * @return A mapping with keys for each FieldKeyMapping type.
     */
    def getMapping(String key, String type){
        if (type == YGOR){
            return ygorMappings.get(key)
        }
        if (type == KBART){
            return kbartMappings.get(key)
        }
        if (type == ZDB){
            return zdbMappings.get(key)
        }
        if (type == EZB){
            return ezbMappings.get(key)
        }
    }


    def getAllIdFieldKeyMappings(){
        [ZdbIdentifier.fieldKeyMapping, PissnIdentifier.fieldKeyMapping, EissnIdentifier.fieldKeyMapping]
    }
}
