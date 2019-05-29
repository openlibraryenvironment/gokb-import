package de.hbznrw.ygor.export

import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.export.structure.TitleStruct
import de.hbznrw.ygor.validators.Validator
import groovy.util.logging.Log4j

// if validator result is NOT valid, use org value

@Log4j
class DataSetter {

    static def setCoverageVolume(Object obj, Object dateType, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normCoverageVolume(orgValue, dateType)
        def normMeta  = Validator.isValidNumber      (normValue)
        
        if(Status.VALIDATOR_NUMBER_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = ''
            obj.m = normMeta
        }
    }
    
    static def setDate(Object obj, Object dateType, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normDate  (orgValue, dateType)
        def normMeta  = Validator.isValidDate(normValue)
        
        if(Status.VALIDATOR_DATE_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = ''
            obj.m = normMeta
        }
    }
    
    static def setIdentifier(Object obj, Object identType, Object orgValue, String namespace){

        obj.org       = orgValue
        def normValue = Normalizer.normIdentifier  (orgValue, identType, namespace)
        def normMeta  = Validator.isValidIdentifier(normValue, identType, namespace)

        normValue = fixIdentifier(normValue, identType)

        if(Status.VALIDATOR_IDENTIFIER_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = ''
            obj.m = normMeta
        }
    }


    static def fixIdentifier(String orgValue, Object identType){
        if (identType.equals(TitleStruct.EISSN)){
            orgValue = fixEISSN(orgValue)
        }
        orgValue
    }


    static def fixEISSN(String orgValue){
        // set the last character to upper case if it is "x" and if the rest of the eissn is valid
        if (orgValue.matches("[\\d]{4}-[\\d]{3}x")){
            orgValue = orgValue.replace("x", "X")
            log.info("Set \"x\" to upper case in " + orgValue)
        }
        orgValue
    }


    static def setInteger(Object obj, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normInteger (orgValue)
        def normMeta  = Validator.isValidNumber(normValue)

        if(Status.VALIDATOR_NUMBER_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = ''
            obj.m = normMeta
        }
    }

    static def setString(Object obj, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normString  (orgValue)
        def normMeta  = Validator.isValidString(normValue)
        
        if(Status.VALIDATOR_STRING_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = ''
            obj.m = normMeta
        }
    }

    static def setStringAsTitle(Object obj, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normString  (orgValue)
        normValue = Normalizer.normStringTitle  (normValue)
        def normMeta  = Validator.isValidString(normValue)

        if(Status.VALIDATOR_STRING_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = ''
            obj.m = normMeta
        }
    }

    static def setURL(Object obj, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normURL  (orgValue, false)
        def normMeta  = Validator.isValidURL(normValue)
        
        if(Status.VALIDATOR_URL_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = ''
            obj.m = normMeta
        }
    }
    
    static def setURLAuthorityOnly(Object obj, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normURL  (orgValue, true)
        def normMeta  = Validator.isValidURL(normValue)
        
        if(Status.VALIDATOR_URL_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = ''
            obj.m = normMeta
        }
    }

    static def extractNumbers(Object obj, List<String> orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.extractNumbers(orgValue)
        obj.m         = Validator.isValidNumber(normValue)

        if(Status.VALIDATOR_NUMBER_IS_VALID == obj.m){
            obj.v = normValue
        }
        else {
            obj.v = ''
        }
    }
}
