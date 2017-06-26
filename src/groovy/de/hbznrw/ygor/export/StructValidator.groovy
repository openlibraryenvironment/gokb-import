package de.hbznrw.ygor.export

import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.export.structure.TippCoverage
import de.hbznrw.ygor.export.structure.TitleHistoryEvent
import de.hbznrw.ygor.export.structure.TitlePublisherHistory

class StructValidator {
    
    /**
     * 
     * @param startDate
     * @param endDate
     * @param startVolume
     * @param endVolume
     * @return
     */
    static isValidCoverage(TippCoverage coverage){
        // startDate
        // endDate
        // startIssue
        // endIssue
        // startVolume
        // endVolume
        // coverageNote
        // coverageDepth
        // embargo
        
        // coverage due parsing error
        if( coverage.startDate.v == coverage.endDate.v && 
            coverage.startVolume.v == coverage.endVolume.v && 
            coverage.startDate.v == coverage.startVolume.v
        ){
            return Status.STRUCTVALIDATOR_REMOVE_FLAG
        }
        
        /*
        if((coverage.startDate.m == Status.VALIDATOR_DATE_IS_INVALID && coverage.endDate.m == Status.VALIDATOR_DATE_IS_INVALID)){ 
            return Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID
        }
        else {
            if((coverage.startDate.v == coverage.endDate.v)){
                return Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID
            }
            else {
                if((coverage.startVolume.m == Status.VALIDATOR_NUMBER_IS_INVALID && coverage.endVolume.m == Status.VALIDATOR_NUMBER_IS_INVALID)){
                    return Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID
                }
                else {
                    return Status.STRUCTVALIDATOR_COVERAGE_IS_VALID
                }
            }
        }    
        */
        return Status.STRUCTVALIDATOR_COVERAGE_IS_UNDEF
    }
    
    static isValidHistoryEvent(TitleHistoryEvent historyEvent){
        // date
        // from
        // to
        
        // history event due parsing error
        if(historyEvent.from.size() == 0 && historyEvent.to.size() == 0 && historyEvent.date.m == Status.UNDEFINED){
            return Status.STRUCTVALIDATOR_REMOVE_FLAG
        }
        if(historyEvent.from.size() == 0 && historyEvent.to.size() == 0 && historyEvent.date.m == Status.VALIDATOR_DATE_IS_MISSING){
            return Status.STRUCTVALIDATOR_REMOVE_FLAG
        }
        
        /*
        if(historyEvent.date.m == Status.VALIDATOR_DATE_IS_VALID && historyEvent.from.size() > 0 && historyEvent.to.size() > 0){
            return Status.STRUCTVALIDATOR_HISTORYEVENT_IS_VALID
        }
        */
        return Status.STRUCTVALIDATOR_HISTORYEVENT_IS_UNDEF
    }
    
    static isValidPublisherHistory(TitlePublisherHistory publisherHistory){
        // startDate
        // endDate
        // status
        // name
        
        if(false){
            return Status.STRUCTVALIDATOR_REMOVE_FLAG
        }
        /*
        if( publisherHistory.startDate.m == Status.VALIDATOR_DATE_IS_VALID &&
            publisherHistory.endDate.m == Status.VALIDATOR_DATE_IS_VALID &&
            publisherHistory.name.m == Status.VALIDATOR_STRING_IS_VALID){
 
            return Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_VALID
   
        }
        */
        return Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_UNDEF
    }
}
