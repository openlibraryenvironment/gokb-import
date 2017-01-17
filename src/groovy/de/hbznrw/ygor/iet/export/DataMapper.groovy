package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.*
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
            tmp.value.v = DataNormalizer.normIdentifier(env.message, tmp.type.v)
            tmp.value.m = env.state
            title.identifiers.v << new Pod(tmp)
        }
        
        else if(query == Query.GBV_TITLE) {
            title.name.v = DataNormalizer.normString(env.message)
            title.name.m = env.state
        }
        
        else if(query == Query.GBV_PUBLISHER) {
 
            env.messages['name'].eachWithIndex{ elem, i ->
            
                def tmp    = TitleStruct.getNewPublisherHistory()
                
                tmp.name.v = DataNormalizer.normString(env.messages['name'][i])
            
                tmp.startDate.v = DataNormalizer.normDate(env.messages['startDate'][i], DataNormalizer.IS_START_DATE)
                tmp.startDate.m = DataNormalizer.isValidDate(tmp.startDate.v)
                       
                tmp.endDate.v = DataNormalizer.normDate(env.messages['endDate'][i], DataNormalizer.IS_END_DATE)
                tmp.endDate.m = DataNormalizer.isValidDate(tmp.endDate.v)
                
                title.publisher_history.v << new Pod(tmp)
                //  store children status here
                title.publisher_history.m = DataNormalizer.normString(
                    (env.states.find{it.toString().startsWith('name_')}).toString().replaceFirst('name_', '')
                )
            }
        }
        
        else if(query == Query.GBV_PUBLISHED_FROM) {
            title.publishedFrom.v = DataNormalizer.normDate(env.message, DataNormalizer.IS_START_DATE)
            title.publishedFrom.m = env.state
        }
        
        else if(query == Query.GBV_PUBLISHED_TO) {
            title.publishedTo.v = DataNormalizer.normDate(env.message, DataNormalizer.IS_END_DATE)
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
            tmp.value.v = DataNormalizer.normIdentifier(env.message, tmp.type.v)
            tmp.value.m = env.state
            
            tipp.title.v.identifiers.v << new Pod(tmp)
        }
        
        else if(query == Query.GBV_TITLE) {
            tipp.title.v.name.v = DataNormalizer.normString(env.message)
            tipp.title.v.name.m = env.state
        }
        
        else if(query == Query.GBV_TIPP_URL) {
            tipp.url.v = DataNormalizer.normString(env.message)
            tipp.url.m = env.state
        }
        
        else if(query == Query.GBV_PLATFORM_URL) {
            def tmp = PackageStruct.getNewTippPlatform()
            
            tmp.name.v = DataNormalizer.normURL(env.message)
            tmp.name.m = env.state
            
            tmp.primaryUrl.v = DataNormalizer.normURL(env.message)
            tmp.primaryUrl.m = env.state

            tipp.platform = new Pod(tmp)
        }
        
        else if(query == Query.GBV_TIPP_COVERAGE) {
            
            env.messages['coverageNote'].eachWithIndex{ elem, i ->
                def tmp = PackageStruct.getNewTippCoverage()
                // TODO
                tmp.coverageNote.v = DataNormalizer.normString(env.messages['coverageNote'][i])
                tmp.coverageNote.m = DataNormalizer.normString(env.states[i])
                
                if(env.messages['startDate'][i]){
                    tmp.startDate.v = DataNormalizer.normDate(env.messages['startDate'][i], DataNormalizer.IS_START_DATE)
                    tmp.startDate.m = DataNormalizer.isValidDate(tmp.startDate.v)   
                }
                if(env.messages['endDate'][i]){
                    tmp.endDate.v = DataNormalizer.normDate(env.messages['endDate'][i], DataNormalizer.IS_END_DATE)
                    tmp.endDate.m = DataNormalizer.isValidDate(tmp.startDate.v)
                }
                if(env.messages['startVolume'][i]){
                    tmp.startVolume.v = DataNormalizer.normCoverageVolume(env.messages['startVolume'][i], DataNormalizer.IS_START_DATE)
                    tmp.startVolume.m = env.states[i]
                }
                if(env.messages['endVolume'][i]){
                    tmp.endVolume.v = DataNormalizer.normCoverageVolume(env.messages['endVolume'][i], DataNormalizer.IS_END_DATE)
                    tmp.endVolume.m = env.states[i]                 
                }  
                tipp.coverage.v << tmp
            }
        }
       
        tipp
    }
  
    static Title getExistingTitleByPrimaryIdentifier(DataContainer dc, String key) {
        def result = null

        if(dc.titles.v.containsKey(key))
            result = dc.titles.v[key]

        result
    }
    
    static Tipp getExistingTippByPrimaryIdentifier(DataContainer dc, String key) {
        def result = null

        if(dc.pkg.v.tipps.v.containsKey(key))
            result = dc.pkg.v.tipps.v[key]

        result
    }
}
