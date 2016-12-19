package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*

class DataMapper {

    static mapping(Title title, Query query, Envelope envelope) {
       
        switch(query) {
            case Query.ZDBID:
                def tmp     = Data.getNewIdentifier()
                tmp.type    = 'zdb'
                tmp.value   = envelope.message.join(", ").trim()
                title.identifiers << tmp
                break;
                
            case Query.ZDBTITLE:
                title.name = envelope.message.join(", ").trim()
                break;
                
            case Query.EZBID:
                def tmp     = Data.getNewIdentifier()
                tmp.type    = 'ezb'
                tmp.value   = envelope.message.join(", ").trim()
                title.identifiers << tmp
                break;
        }
    }
    
    static Title getExistingTitleByISSN(Data data, String value) {
        def result = null

        data.content.each { title ->
            title.identifiers.each { i ->
                if("issn".equals(i.type) && value.equals(i.value)) {
                    result = title
                }
            }
        }
        result
    }
}
