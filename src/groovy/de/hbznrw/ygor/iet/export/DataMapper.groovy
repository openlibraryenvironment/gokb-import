package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*

class DataMapper {

    static maptoTitle(Title title, Query query, Envelope envelope) {
       
        switch(query) {
            case Query.ZDBID:
                def tmp     = TitleStruct.getNewIdentifier()
                tmp.type    = 'zdb'
                tmp.value   = DataMapper.normString(envelope.message)
                tmp._meta   = envelope.state
                title.identifiers << tmp
                break;
                
            case Query.ZDBTITLE:
                title.name = DataMapper.normString(envelope.message)
                break;
                
            case Query.ZDBPUBLISHER:
                // TODO testing [pub][date][state_pub, state_date]
                def tmp           = TitleStruct.getNewPublisherHistory()
                if(envelope.messages){
                    tmp.name      = DataMapper.normString(envelope.messages['name'])
                    tmp.startDate = DataMapper.normString(envelope.messages['startDate'])
                    tmp._meta     = envelope.states.toString()
                }
                title.publisher_history << tmp
                break;
                 
            case Query.EZBID:
                def tmp     = TitleStruct.getNewIdentifier()
                tmp.type    = 'ezb'
                tmp.value   = DataMapper.normString(envelope.message)
                tmp._meta   = envelope.state
                title.identifiers << tmp
                break;
        }
    }
    
    static String normString(ArrayList l) {
        if(!l) l = []
        DataMapper.normString(l.join(", "))
    }
    
    static String normString(String s) {
        s = s.replaceAll("  "," ")
        s.trim()
    }
    
    static Title getExistingTitleByPrimaryIdentifier(DataContainer data, String value) {
        def result = null

        data.titles.each { title ->
            title.identifiers.each { i ->
                if("issn".equals(i.type) && value.equals(i.value)) {
                    result = title
                }
            }
        }
        result
    }
}
