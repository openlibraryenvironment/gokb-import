package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*

class Title {
        
    Pod imprint          = new Pod("")
    Pod issuer           = new Pod("")
    Pod medium           = new Pod("Journal", Status.HARDCODED)
    Pod type             = new Pod("Serial", Status.HARDCODED)
    Pod name             = new Pod("")
    Pod publishedFrom    = new Pod("")
    Pod publishedTo      = new Pod("")
    
    Pod identifiers       = new Pod([]) // list
    Pod publisher_history = new Pod([]) // list
    
    // ???
    Pod shortcode        = new Pod("")
    Pod status           = new Pod("Current", Status.HARDCODED)
    
    // ???
    Pod OAStatus         = new Pod("")
    Pod continuingSeries = new Pod("")
    Pod defaultAccessURL = new Pod("")
    Pod editStatus       = new Pod("In Progress", Status.HARDCODED)
}


class TitleStruct {
    
    static final ISSN  = 'issn'
    static final EISSN = 'eissn'
    static final PISSN = 'pissn'
    
    static TitleIdentifier getNewIdentifier() {
        return new TitleIdentifier()
    }
    
    static TitlePublisherHistory getNewPublisherHistory() {
        return new TitlePublisherHistory()
    }
}

class TitleIdentifier {
      
    Pod type  = new Pod("")
    Pod value = new Pod("")
}

class TitlePublisherHistory {
    
    Pod endDate   = new Pod()
    Pod name      = new Pod("")
    Pod startDate = new Pod()
    Pod status    = new Pod("Active", Status.HARDCODED)
}
