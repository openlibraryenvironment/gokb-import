package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.*
import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.tools.DateToolkit
import groovy.util.logging.Log4j
import de.hbznrw.ygor.tools.*

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
            obj.v = DataSetter.flattenList(orgValue)
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
            obj.v = DataSetter.flattenList(orgValue)
            obj.m = normMeta
        }
    }
    
    static def setIdentifier(Object obj, Object identType, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normIdentifier  (orgValue, identType)
        def normMeta  = Validator.isValidIdentifier(normValue, identType)
        
        if(Status.VALIDATOR_IDENTIFIER_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = DataSetter.flattenList(orgValue)
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
            obj.v = DataSetter.flattenList(orgValue)
            obj.m = normMeta
        }
    }
        
    static def setTippURL(Object obj, Object nominalPlatform, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normTippURL(orgValue, nominalPlatform)
        def normMeta  = Validator.isValidURL  (normValue)
        
        if(Status.VALIDATOR_URL_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = DataSetter.flattenList(orgValue)
            obj.m = normMeta
        }
    }
    
    static def setURL(Object obj, Object orgValue){
        obj.org       = orgValue
        def normValue = Normalizer.normURL  (orgValue)
        def normMeta  = Validator.isValidURL(normValue)
        
        if(Status.VALIDATOR_URL_IS_VALID == normMeta){
            obj.v = normValue
            obj.m = normMeta
        }
        else {
            obj.v = DataSetter.flattenList(orgValue)
            obj.m = normMeta
        }
    }
    
    static def flattenList(Object obj){
       if(obj instanceof List){
           if(obj.size() == 0){
               return ""
           }
           else if(obj.size() == 1){
               return obj.getAt(0)
           }
           else {
               return obj.join("|")
           }
       }
    } 
}
