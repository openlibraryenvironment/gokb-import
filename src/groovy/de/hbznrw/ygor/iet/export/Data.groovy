package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.enums.*

class Data {

    Meta    meta
    def     content
    
    Data() {

        meta = new Meta(
            type:   "alpha",
            ygor:   "0.3",
            date:   new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('GMT+1'))
        )
        
        content = []
    }
    
    static getNewIdentifier() {
        return new Identifier()
    }
    
    static getNewPublisherHistory() {
        return new PublisherHistory()
    }
}


class Meta {
    
    String type = ""
    String ygor = ""
    String date = ""
}

class Title {
    
    Title(){}
    
    Title(String issn){
        def tmp     = Data.getNewIdentifier()
        tmp.type    = 'issn'
        tmp.value   = issn
        tmp._meta   = Status.STATUS_OK
        identifiers << tmp
    }
    
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
}

class Identifier {
    String _meta    = ""
    
    String type     = ""
    String value    = ""
}

class PublisherHistory {
    String _meta    = ""
    
    String endDate
    String name     = ""
    String startDate
    String status   = "Active"
}
