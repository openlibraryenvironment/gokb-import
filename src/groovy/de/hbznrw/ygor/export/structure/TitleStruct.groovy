package de.hbznrw.ygor.export.structure

class TitleStruct {
    
    static final ISSN  = 'issn'
    static final EISSN = 'eissn'
    static final PISSN = 'issn'
    static final DOI = 'doi'
    
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
