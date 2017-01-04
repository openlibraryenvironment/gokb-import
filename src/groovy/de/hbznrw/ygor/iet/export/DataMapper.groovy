package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.bridge.*

class DataMapper {

    static Title mapToTitle(Title title, Query query, Envelope env) {
       
        if(query in [Query.ZDBID, Query.EZBID, Query.GBVEISSN, Query.GBVPISSN, Query.GBVGVKPPN]) {
            def tmp = TitleStruct.getNewIdentifier()
            
            if(Query.ZDBID == query)
                tmp.type = ZdbBridge.IDENTIFIER
            else if(Query.EZBID == query)
                tmp.type = EzbBridge.IDENTIFIER
            else if(Query.GBVEISSN == query)
                tmp.type = TitleStruct.EISSN
            else if(Query.GBVPISSN == query)
                tmp.type = TitleStruct.PISSN
            else if(Query.GBVGVKPPN == query)
                tmp.type = "gvk_ppn"
                
            tmp.value   = DataMapper.normString(env.message)
            tmp._meta   = env.state
            title.identifiers << tmp
        }
        
        else if(query == Query.GBVTITLE) {
            title.name = DataMapper.normString(env.message)
        }
        
        else if(query == Query.GBVPUBLISHER) {
            // TODO testing [pub][date][state_pub, state_date]
            def tmp           = TitleStruct.getNewPublisherHistory()
            if(env.messages){
                tmp.name      = DataMapper.normString(env.messages['name'])
                tmp.startDate = DataMapper.normString(env.messages['startDate'])
                tmp._meta     = env.states.toString()
            }
            title.publisher_history << tmp
        }
        
        title
    }
    
    static Tipp mapToTipp(Tipp tipp, Query query, Envelope env) {
        
        if(query in [Query.ZDBID, Query.GBVEISSN]) {
            def tmp = TitleStruct.getNewIdentifier()
            
            if(Query.ZDBID == query)
                tmp.type = ZdbBridge.IDENTIFIER
            else if(Query.GBVEISSN == query)
                tmp.type = TitleStruct.EISSN
                
            tmp.value   = DataMapper.normString(env.message)
            tmp._meta   = env.state
            tipp.title.identifiers << tmp
        }
        
        else if(query == Query.GBVTITLE) {
            tipp.title.name = DataMapper.normString(env.message)
        }
        
        tipp
    }
    
    static String normString(ArrayList list) {
        list ? DataMapper.normString(list.join(", ")) : ""
    }
    
    static String normString(String s) {
        s = s.replaceAll("  "," ")
        s = s.replaceAll(" : ",": ")
        s.trim()
    }
    
    static Title getExistingTitleByPrimaryIdentifier(DataContainer dc, String value) {
        def result = null

        dc.titles.each { title ->
            title.identifiers.each { i ->
                if(TitleStruct.ISSN.equals(i.type) && value.equals(i.value)) {
                    result = title
                }
            }
        }
        result
    }
    
    static Tipp getExistingTippByPrimaryIdentifier(DataContainer dc, String value) {
        def result = null

        dc.pkg.tipps.each { tipp ->
            tipp.title.identifiers.each { i ->
                if(TitleStruct.ISSN.equals(i.type) && value.equals(i.value)) {
                    result = tipp
                }
            }
        }
        result
    }
    
    static clearUp(DataContainer dc) {
        
        dc.titles.each{ title ->
            title.identifiers.removeIf {it.type == TitleStruct.ISSN} 
        }
        // TODO
        /*
        dc.pkg.tipps.each{ tipp ->
            tipp.title.identifiers.removeIf {it.type == TitleStruct.ISSN}
        }
        */
    }
}
