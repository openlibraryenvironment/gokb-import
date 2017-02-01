package de.hbznrw.ygor.iet.export.structure

import de.hbznrw.ygor.iet.enums.*

class TitleStruct {
    
    static final ISSN  = 'issn'
    static final EISSN = 'eissn'
    static final PISSN = 'pissn'
    
    static Identifier getNewIdentifier() {
        return new Identifier()
    }
    
    static TitlePublisherHistory getNewPublisherHistory() {
        return new TitlePublisherHistory()
    }
    
    static TitleHistoryEvent getNewHistoryEvent() {
        return new TitleHistoryEvent()
    }
    
    static TitleHistoryEventGeneric getNewHistoryEventGeneric() {
        return new TitleHistoryEventGeneric()
    }
}
