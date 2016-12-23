package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*

class Title {
        
    String imprint          = ""
    String issuer           = ""
    String medium           = "Journal"
    String type             = "Serial"
    String name             = ""
    String publishedFrom    = ""
    String publishedTo      = ""
    
    def identifiers         = []
    def publisher_history   = []
    
    // ???
    String shortcode        = ""
    String status           = "Current"
    
    // ???
    String OAStatus         = ""
    String continuingSeries = ""
    String defaultAccessURL = ""
    String editStatus       = "In Progress"
    
    Title(){
    }
    
    Title(String issn){
        def tmp     = TitleStruct.getNewIdentifier()
        tmp.type    = TitleStruct.ISSN
        tmp.value   = issn
        tmp._meta   = Status.UNDEFINED
        identifiers << tmp
    }
}


class TitleStruct {
    static final ISSN  = 'issn'
    static final EISSN = 'eissn'
    static final PISSN = 'pissn'
    
    static getNewIdentifier() {
        return new TitleIdentifier()
    }
    
    static getNewPublisherHistory() {
        return new TitlePublisherHistory()
    }
}

class TitleIdentifier {
    String _meta    = Status.UNDEFINED
    
    String type     = ""
    String value    = ""
}

class TitlePublisherHistory {
    String _meta    = Status.UNDEFINED
    
    String endDate
    String name     = ""
    String startDate
    String status   = "Active"
}
