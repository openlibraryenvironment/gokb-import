package de.hbznrw.ygor.iet.export

class Data {

    Meta    meta
    def     content
    
    Data() {

        meta = new Meta(
            type:   "alpha",
            ygor:   "0.3",
            date:   new Date() // TODO timezone
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
    Date   date
}

class Title {
    
    Title(){}
    
    Title(String issn){
        def tmp     = Data.getNewIdentifier()
        tmp.type    = 'issn'
        tmp.value   = issn
        identifiers << tmp
    }
    
    String imprint = ""
    String issuer = ""
    String medium = ""
    String type = ""
    String name = ""
    String publishedFrom = ""
    String publishedTo = ""
    
    def identifiers = []
    def publisher_history = []
    
    // ???
    String shortcode = ""
    String status = ""
    
    // ???
    String OAStatus = ""
    String continuingSeries = ""
    String defaultAccessURL = ""
    String editStatus = ""
}

class Identifier {
    
    String type = ""
    String value = ""
}

class PublisherHistory {
    
    Date   endDate
    String name = ""
    Date   startDate
    String status = ""
}
