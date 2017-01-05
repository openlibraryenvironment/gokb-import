package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.bridge.*

class DataMapper {

    static Title mapToTitle(Title title, Query query, Envelope env) {
       
        if(query in [Query.ZDBID, Query.EZBID, Query.GBV_EISSN, Query.GBV_PISSN, Query.GBV_GVKPPN]) {
            def tmp = TitleStruct.getNewIdentifier()
            
            if(Query.ZDBID == query)
                tmp.type.v = ZdbBridge.IDENTIFIER
            else if(Query.EZBID == query)
                tmp.type.v = EzbBridge.IDENTIFIER
            else if(Query.GBV_EISSN == query)
                tmp.type.v = TitleStruct.EISSN
            else if(Query.GBV_PISSN == query)
                tmp.type.v = TitleStruct.PISSN
            else if(Query.GBV_GVKPPN == query)
                tmp.type.v = "gvk_ppn"
                
            tmp.type.m  = Status.IGNORE
            tmp.value.v = DataMapper.normString(env.message)
            tmp.value.m = env.state
            title.identifiers.v << new Pod(tmp)
        }
        
        else if(query == Query.GBV_TITLE) {
            title.name.v = DataMapper.normString(env.message)
            title.name.m = env.state
        }
        
        else if(query == Query.GBV_PUBLISHER) {
 
                // TODO refactoring
                def tmp         = TitleStruct.getNewPublisherHistory()
                
                tmp.name.v      = DataMapper.normString(env.messages['name'])
                tmp.name.m      = DataMapper.normString(env.states.find{
                        it.toString().startsWith('name_')
                    }
                )
                tmp.startDate.v = DataMapper.normString(env.messages['startDate'])
                tmp.startDate.m = DataMapper.normString(env.states.find{
                        it.toString().startsWith('startDate_')
                    }
                )         
                tmp.endDate.v   = DataMapper.normString(env.messages['endDate'])
                tmp.endDate.m   = DataMapper.normString(env.states.find{
                        it.toString().startsWith('endDate_')
                    }
                )  
                tmp.status.v    = DataMapper.normString(env.messages['status'])
                tmp.status.m    = DataMapper.normString(env.states.find{
                        it.toString().startsWith('status_')
                    }
                )
                title.publisher_history.v << new Pod(tmp)
        }
        
        else if(query == Query.GBV_PUBLISHED_FROM) {
            title.publishedFrom.v = DataMapper.normString(env.message)
            title.publishedFrom.m = env.state
        }
        
        else if(query == Query.GBV_PUBLISHED_TO) {
            title.publishedTo.v   = DataMapper.normString(env.message)
            title.publishedTo.m = env.state
        }
        
        title
    }
    
    static Tipp mapToTipp(Tipp tipp, Query query, Envelope env) {
        
        if(query in [Query.ZDBID, Query.GBV_EISSN]) {
            def tmp = TitleStruct.getNewIdentifier()
            
            if(Query.ZDBID == query)
                tmp.type.v = ZdbBridge.IDENTIFIER
            else if(Query.GBV_EISSN == query)
                tmp.type.v = TitleStruct.EISSN

            tmp.type.m  = Status.IGNORE
            tmp.value.v = DataMapper.normString(env.message)
            tmp.value.m = env.state
            
            tipp.title.v.identifiers.v << new Pod(tmp)
        }
        
        else if(query == Query.GBV_TITLE) {
            tipp.title.v.name.v = DataMapper.normString(env.message)
            tipp.title.v.name.m = env.state
        }
        
        else if(query == Query.GBV_TIPP_URL) {
            tipp.url.v = DataMapper.normString(env.message)
            tipp.url.m = env.state
        }
        
        tipp
    }
    
    static String normString(ArrayList list) {
        list ? DataMapper.normString(list.join("|")) : ""
    }
    
    static String normString(String s) {
        if(!s)
            return s
        
        s = s.replaceAll("  "," ")
        s = s.replaceAll(" : ",": ")
        s.trim()
    }
    
    static Title getExistingTitleByPrimaryIdentifier(DataContainer dc, String ident) {
        def result = null

        if(dc.titles.v.containsKey(ident))
            result dc.titles.v[ident]

        result
    }
    
    static Tipp getExistingTippByPrimaryIdentifier(DataContainer dc, String ident) {
        def result = null

        if(dc.pkg.v.tipps.v.containsKey(ident))
            result dc.pkg.v.tipps.v[ident]

        result
    }
    
    static clearUp(DataContainer dc) {
        /*
        dc.titles.v.each{ title ->
            title.v.identifiers.v.removeIf {it.v.type == TitleStruct.ISSN} 
        }*/
        // TODO
        /*
        dc.pkg.tipps.each{ tipp ->
            tipp.title.identifiers.removeIf {it.type == TitleStruct.ISSN}
        }
        */
    }
}
