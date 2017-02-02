package de.hbznrw.ygor.iet.export

import org.springframework.util.StringUtils

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.Pod
import de.hbznrw.ygor.iet.export.structure.Tipp
import de.hbznrw.ygor.iet.export.structure.Title
import de.hbznrw.ygor.iet.export.structure.TitleStruct
import de.hbznrw.ygor.iet.bridge.*

import java.sql.Timestamp

class Validator {

    final static IS_START_DATE  = 1000
    final static IS_END_DATE    = 1001
    
    
    /**
     *
     * @param str
     * @return
     */
    static isValidString(String str) {
        if(!str || str.trim().equals("") || str.length() < 2){
            return Status.VALIDATOR_STRING_IS_INVALID
        }
        if(str.contains("|")){
            return Status.VALIDATOR_STRING_IS_INVALID
        }
        return Status.VALIDATOR_STRING_IS_VALID
    }
    
    /**
     *
     * @param str
     * @return
     */
    static isValidNumber(String str) {
        if(str && !str.trim().equals("")){
            if(str.isInteger())
                return Status.VALIDATOR_NUMBER_IS_VALID
        }
        return Status.VALIDATOR_NUMBER_IS_INVALID
    }
    
    /**
     * 
     * @param str
     * @param identifierType
     * @return
     */
    static isValidIdentifier(String str, Object identifierType) {
        if(identifierType.equals(TitleStruct.EISSN) || identifierType.equals(TitleStruct.PISSN)){
            if(9 == str.length() && 4 == str.indexOf("-"))
                return Status.VALIDATOR_IDENTIFIER_IS_VALID
            else 
                return Status.VALIDATOR_IDENTIFIER_IS_INVALID
        }
        else if(identifierType.equals(ZdbBridge.IDENTIFIER)){
            if(2 < str.length() && str.indexOf("-") == str.length()-2)
                return Status.VALIDATOR_IDENTIFIER_IS_VALID
            else
                return Status.VALIDATOR_IDENTIFIER_IS_INVALID
        }
        else if(identifierType.equals(EzbBridge.IDENTIFIER)){
            // TODO .. no valid definition 
            if(str.length() > 2)
                return Status.VALIDATOR_IDENTIFIER_IS_VALID
            else
                return Status.VALIDATOR_IDENTIFIER_IS_INVALID
        }
        return Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE
    }
    
    
    /**
     * 
     * @param str
     * @return
     */
    static isValidURL(String str) {
      
        if(!str || str.trim().equals("") || str.contains("|")){
            return Status.VALIDATOR_URL_IS_INVALID
        }
        
        try {
            def url = new URL(str)
        } catch(Exception e) {
            return Status.VALIDATOR_URL_IS_INVALID
        }
        
        return Status.VALIDATOR_URL_IS_VALID
    }
    
    /**
     * 
     * @param str
     * @return
     */
    static isValidDate(String str) {
        if(!str || str.trim().equals("")){
            return Status.VALIDATOR_DATE_IS_MISSING
        }
        try {
            def check = Timestamp.valueOf(str);
            return Status.VALIDATOR_DATE_IS_VALID
        }
        catch(Exception e) {
            return Status.VALIDATOR_DATE_IS_INVALID
        }
    }
    
    /**
     * 
     * @param startDate
     * @param endDate
     * @param startVolume
     * @param endVolume
     * @return
     */
    static isValidCoverage(Pod startDate, Pod endDate, Pod startVolume, Pod endVolume){
        if((startDate.m == Status.VALIDATOR_DATE_IS_INVALID && endDate.m == Status.VALIDATOR_DATE_IS_INVALID) 
            && (startVolume.m == Status.VALIDATOR_NUMBER_IS_INVALID && endVolume.m == Status.VALIDATOR_NUMBER_IS_INVALID))
                return false
        
        if((startDate.m == Status.VALIDATOR_DATE_IS_INVALID && endDate.m == Status.VALIDATOR_DATE_IS_INVALID) && (startVolume.v == endVolume.v))
            return false
        
        if((startDate.v == endDate.v) && (startVolume.v == endVolume.v))
            return false
        
        if((startDate.v == endDate.v) && (startDate.m == Status.VALIDATOR_DATE_IS_VALID && endDate.m == Status.VALIDATOR_DATE_IS_VALID))
            return false
            
        true
    }
    
    static isValidHistoryEvent(Pod historyEvent){
        if(historyEvent.v.date && historyEvent.v.from.size() > 0 && historyEvent.v.to.size() > 0){
 
            // TODO implement
            // TODO implement unit test
            return Status.VALIDATOR_HISTORYEVENT_IS_VALID
        }
        
        return Status.VALIDATOR_HISTORYEVENT_IS_INVALID
    }
}
