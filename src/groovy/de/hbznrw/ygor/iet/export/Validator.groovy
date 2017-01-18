package de.hbznrw.ygor.iet.export

import org.springframework.util.StringUtils

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
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
        return Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE
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
}
