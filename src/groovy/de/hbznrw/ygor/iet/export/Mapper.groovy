package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.*
import de.hbznrw.ygor.iet.bridge.*

import de.hbznrw.ygor.tools.DateToolkit


class Mapper {

    static void mapToTitle(DataContainer dc, Title title, Query query, Envelope env) {

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
            tmp.value.v = Normalizer.normIdentifier(env.message, tmp.type.v)
            tmp.value.m = Validator.isValidIdentifier(tmp.value.v, tmp.type.v)
            
            // TODO: handle multiple ezbid matches
            
            title.identifiers << tmp // no pod
        }
        
        else if(query == Query.GBV_TITLE) {
            title.name.v = Normalizer.normString(env.message)
            title.name.m = Validator.isValidString(title.name.v)
        }
        
        else if(query == Query.GBV_PUBLISHER) {
            def dummy     = null
            def dummyDate = null
            
            env.message.each{ e ->
               e.messages['name'].eachWithIndex{ elem, i ->
                   def tmp = TitleStruct.getNewPublisherHistory()
                   
                   tmp.name.v = Normalizer.normString(e.messages['name'][i])
               
                   tmp.startDate.v = Normalizer.normDate(e.messages['startDate'][i], Normalizer.IS_START_DATE)
                   tmp.startDate.m = Validator.isValidDate(tmp.startDate.v)
                          
                   tmp.endDate.v = Normalizer.normDate(e.messages['endDate'][i], Normalizer.IS_END_DATE)
                   tmp.endDate.m = Validator.isValidDate(tmp.endDate.v)
                   
                   if([e.messages['startDate'][i], e.messages['endDate'][i]].contains("anfangs")){
                       dummy = tmp
                   } else {
                       // store lowest start date for dummy calculation
                       if(dummyDate == null || (tmp.startDate.m == Status.VALIDATOR_DATE_IS_VALID && dummyDate > tmp.startDate.v))
                           dummyDate = tmp.startDate.v
                           
                       title.publisher_history << tmp // no pod
                   }
                }
            }
            
            if(dummy){
                if(dummyDate){
                    dummy.endDate.v   = DateToolkit.getDateMinusOneMinute(dummyDate)
                    dummy.endDate.m   = Validator.isValidDate(dummy.endDate.v)
                    dummy.startDate.v = ''
                    dummy.startDate.m = Validator.isValidDate(dummy.startDate.v)
                    
                    println "    .. title.publisher_history << (${dummy.endDate.v})"
                    title.publisher_history << dummy // no pod
                }
            }

        }
        
        else if(query == Query.GBV_PUBLISHED_FROM) {
            title.publishedFrom.v = Normalizer.normDate(env.message, Normalizer.IS_START_DATE)
            title.publishedFrom.m = Validator.isValidDate(title.publishedFrom.v)
        }
        
        else if(query == Query.GBV_PUBLISHED_TO) {
            title.publishedTo.v = Normalizer.normDate(env.message, Normalizer.IS_END_DATE)
            title.publishedTo.m = Validator.isValidDate(title.publishedTo.v)
        }
    }
    
    static void mapToTipp(DataContainer dc, Tipp tipp, Query query, Envelope env) {

        if(query in [Query.ZDBID, Query.GBV_EISSN]) {
            def tmp = TitleStruct.getNewIdentifier()
            
            if(Query.ZDBID == query)
                tmp.type.v = ZdbBridge.IDENTIFIER
            else if(Query.GBV_EISSN == query)
                tmp.type.v = TitleStruct.EISSN

            tmp.type.m  = Status.IGNORE
            tmp.value.v = Normalizer.normIdentifier(env.message, tmp.type.v)
            tmp.value.m = Validator.isValidIdentifier(tmp.value.v, tmp.type.v)

            tipp.title.v.identifiers << tmp // no pod
        }
        
        else if(query == Query.GBV_TITLE) {
            tipp.title.v.name.v = Normalizer.normString(env.message)
            tipp.title.v.name.m = Validator.isValidString(tipp.title.v.name.v)
        }
        
        else if(query == Query.GBV_TIPP_URL) {
            tipp.url.v = Normalizer.normTippURL(env.message, dc.pkg.packageHeader.v.nominalPlatform.v)
            tipp.url.m = Validator.isValidURL(tipp.url.v)
        }
        
        else if(query == Query.GBV_PLATFORM_URL) {
            def tmp = PackageStruct.getNewTippPlatform()
            
            tmp.name.v = Normalizer.normURL(env.message)
            tmp.name.m = env.state
            
            tmp.primaryUrl.v = Normalizer.normURL(env.message)
            tmp.primaryUrl.m = env.state

            tipp.platform = new Pod(tmp)
        }
        
        else if(query == Query.GBV_TIPP_COVERAGE) {     
            
            env.message.each{ e ->
                e.messages['coverageNote'].eachWithIndex{ elem, i ->
                    
                    def tmp = PackageStruct.getNewTippCoverage()
                    // TODO
                    tmp.coverageNote.v = Normalizer.normString(e.messages['coverageNote'][i])
                    tmp.coverageNote.m = Normalizer.normString(
                        (e.states.find{it.toString().startsWith('coverageNote_')}).toString().replaceFirst('coverageNote_', '')
                        )
                    
                    if(e.messages['startDate'][i]){
                        tmp.startDate.v = Normalizer.normDate(e.messages['startDate'][i], Normalizer.IS_START_DATE)
                        tmp.startDate.m = Validator.isValidDate(tmp.startDate.v)   
                    }
                    if(e.messages['endDate'][i]){
                        tmp.endDate.v = Normalizer.normDate(e.messages['endDate'][i], Normalizer.IS_END_DATE)
                        tmp.endDate.m = Validator.isValidDate(tmp.endDate.v)
                    }
                    if(e.messages['startVolume'][i]){
                        tmp.startVolume.v = Normalizer.normCoverageVolume(e.messages['startVolume'][i], Normalizer.IS_START_DATE)
                        tmp.startVolume.m = Validator.isValidNumber(tmp.startVolume.v)
                    }
                    if(e.messages['endVolume'][i]){
                        tmp.endVolume.v = Normalizer.normCoverageVolume(e.messages['endVolume'][i], Normalizer.IS_END_DATE)
                        tmp.endVolume.m = Validator.isValidNumber(tmp.endVolume.v)
                    } 
                    
                    def valid = Validator.isValidCoverage(tmp.startDate, tmp.endDate, tmp.startVolume, tmp.endVolume) ? Status.VALIDATOR_COVERAGE_IS_VALID : Status.VALIDATOR_COVERAGE_IS_INVALID
                    tipp.coverage << new Pod(tmp, valid)
                }
            }
        }
    }
  
    static Title getExistingTitleByPrimaryIdentifier(DataContainer dc, String key) {
        if(dc.titles.containsKey("${key}"))
            return dc.titles.get("${key}").v

        null
    }
    
    static Tipp getExistingTippByPrimaryIdentifier(DataContainer dc, String key) {
        if(dc.pkg.tipps.containsKey("${key}"))
            return dc.pkg.tipps.get("${key}").v

        null
    }
}
