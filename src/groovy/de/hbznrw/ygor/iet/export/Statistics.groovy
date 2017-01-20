package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.tools.*
import de.hbznrw.ygor.iet.enums.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import de.hbznrw.ygor.iet.export.structure.TitleStruct
import de.hbznrw.ygor.iet.bridge.*

class Statistics {
    
    final static OPTION_1 = 0
    final static OPTION_2 = 1
    final static OPTION_3 = 2
        
    static Object getStatsBeforeParsing(Object json){
        
        // general
        
        json.meta.stats << ["tipps before cleanUp" :  json.package.tipps.size()]
        json.meta.stats << ["titles before cleanUp" : json.titles.size()]
        
        // titles
        
        List<Integer> titleName = [0,0]
        
        json.titles.each{ title ->
            def name = title.value.v.name
            
            if(name?.m.equals(Status.VALIDATOR_STRING_IS_VALID.toString())) {
                titleName[Statistics.OPTION_1]++
            }
            else if(name?.m.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
                 titleName[Statistics.OPTION_2]++
            }
        }
        json.meta.stats << ["titles.name ARE VALID":     titleName[Statistics.OPTION_1]]
        json.meta.stats << ["titles.name are not valid": titleName[Statistics.OPTION_2]]
        
        // titles.identifiers
        
        HashMap<String, List<Integer>> identifiers = [:]
        
        identifiers[TitleStruct.PISSN]    = [0,0,0]
        identifiers[TitleStruct.EISSN]    = [0,0,0]   
        identifiers[ZdbBridge.IDENTIFIER] = [0,0,0]       
        identifiers[EzbBridge.IDENTIFIER] = [0,0,0]
        
        json.titles.each{ title ->
            title.value.v.identifiers.each { ident ->
                
                println ident
                
                def tmp = identifiers["${ident.type.v}"]
                
                if(tmp) {
                    if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_VALID.toString())) {
                        tmp[Statistics.OPTION_1]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_INVALID.toString())) {
                        tmp[Statistics.OPTION_2]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE.toString())) {
                        tmp[Statistics.OPTION_3]++
                    }
                }
            }
        }
        
        identifiers.each{ i ->
            json.meta.stats["title.identifier ${i.key.toUpperCase()} AS VALID RESULTS"]    = i.value[Statistics.OPTION_1]
            json.meta.stats["title.identifier ${i.key.toUpperCase()} are invalid"]         = i.value[Statistics.OPTION_2]
            json.meta.stats["title.identifier ${i.key.toUpperCase()} are in unkown state"] = i.value[Statistics.OPTION_3]
        }
        
        // tipps
        
        List<Integer> tippUrls = [0,0,0]
        
        json.package.tipps.each{ tipp ->
            def url = tipp.value.v.url

            if(url?.m.equals(Status.RESULT_OK.toString())) {
                tippUrls[Statistics.OPTION_1]++
            }
            else if(url?.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                tippUrls[Statistics.OPTION_2]++
            }
            else if(url?.m.equals(Status.RESULT_NO_MATCH.toString())) {
                tippUrls[Statistics.OPTION_3]++
            }
        }
        json.meta.stats << ["tipp.title.url FOUND AS SINGLE RESULT":      tippUrls[Statistics.OPTION_1]]
        json.meta.stats << ["tipp.title.url found with multiple results": tippUrls[Statistics.OPTION_2]]
        json.meta.stats << ["tipp.title.url found with no match":         tippUrls[Statistics.OPTION_3]]
        
        
        // dates
        
        List<Integer> phDates = [0,0,0]
        
        json.titles.each{ title ->
            title.value.v.publisher_history.each { ph ->              
                def sd = ph.startDate
                def ed = ph.endDate

                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                    phDates[Statistics.OPTION_1]++
                }
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                    phDates[Statistics.OPTION_2]++
                }
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                    phDates[Statistics.OPTION_3]++
                }
            }
        }
        json.meta.stats << ["titles.publisher_history FOUND WITH VALID DATES":   phDates[Statistics.OPTION_1]]
        json.meta.stats << ["titles.publisher_history found with invalid dates": phDates[Statistics.OPTION_2]]
        json.meta.stats << ["titles.publisher_history found with missing dates": phDates[Statistics.OPTION_3]]
        
        List<Integer> covDates = [0,0,0]
        
        json.package.tipps.each{ tipp ->
            tipp.value.v.each{ tippField ->
                if(tippField.key.equals("coverage")) {
                    tippField.value.each{ covField ->       
                        def sd = covField.startDate
                        def ed = covField.endDate
                       
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                            covDates[Statistics.OPTION_1]++
                        }
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                            covDates[Statistics.OPTION_2]++
                        }
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                            covDates[Statistics.OPTION_3]++
                        }
                    }
                }
            }
        }

        json.meta.stats << ["tipp.coverage FOUND WITH VALID DATES":   covDates[Statistics.OPTION_1]]
        json.meta.stats << ["tipp.coverage found with invalid dates": covDates[Statistics.OPTION_2]]
        json.meta.stats << ["tipp.coverage found with missing dates": covDates[Statistics.OPTION_3]]
        
        json
    }
    
    static Object getStatsAfterCleanUp(Object json){
        
        // general
        
        json.meta.stats << ["tipps AFTER CLEANUP" :  json.package.tipps.size()]
        json.meta.stats << ["titles AFTER CLEANUP" : json.titles.size()]
        
        json
    }
   
}