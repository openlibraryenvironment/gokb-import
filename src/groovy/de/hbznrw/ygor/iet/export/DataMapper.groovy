package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.bridge.*

class DataMapper {

    static maptoTitle(Title title, Query query, Envelope envelope) {
       
        if(query in [Query.ZDBID, Query.EZBID, Query.GBVEISSN, Query.GBVPISSN]) {
            def tmp     = TitleStruct.getNewIdentifier()
            
            if(Query.ZDBID == query)
                tmp.type    = ZdbBridge.IDENTIFIER
            else if(Query.EZBID == query)
                tmp.type    = EzbBridge.IDENTIFIER
            else if(Query.GBVEISSN == query)
                tmp.type    = TitleStruct.EISSN
            else if(Query.GBVPISSN == query)
                tmp.type    = TitleStruct.PISSN
                
            tmp.value   = DataMapper.normString(envelope.message)
            tmp._meta   = envelope.state
            title.identifiers << tmp
        }
        
        else if(query == Query.GBVTITLE) {
            title.name = DataMapper.normString(envelope.message)
        }
        
        else if(query == Query.GBVPUBLISHER) {
            // TODO testing [pub][date][state_pub, state_date]
            def tmp           = TitleStruct.getNewPublisherHistory()
            if(envelope.messages){
                tmp.name      = DataMapper.normString(envelope.messages['name'])
                tmp.startDate = DataMapper.normString(envelope.messages['startDate'])
                tmp._meta     = envelope.states.toString()
            }
            title.publisher_history << tmp
        }
    }
    
    static String normString(ArrayList l) {
        if(!l) l = []
        DataMapper.normString(l.join(", "))
    }
    
    static String normString(String s) {
        s = s.replaceAll("  "," ")
        s = s.replaceAll(" : ",": ")
        s.trim()
    }
    
    static Title getExistingTitleByPrimaryIdentifier(DataContainer data, String value) {
        def result = null

        data.titles.each { title ->
            title.identifiers.each { i ->
                if(TitleStruct.ISSN.equals(i.type) && value.equals(i.value)) {
                    result = title
                }
            }
        }
        result
    }
}
