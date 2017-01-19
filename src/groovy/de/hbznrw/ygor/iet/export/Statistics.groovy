package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.tools.*
import de.hbznrw.ygor.iet.enums.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import de.hbznrw.ygor.iet.export.structure.TitleStruct
import de.hbznrw.ygor.iet.bridge.*

class Statistics {
    
    final static RESULT_OK = 0
    final static RESULT_MULTIPLE_MATCHES = 1
    final static RESULT_NO_MATCH = 2
    
    final static VALID_DATE   = 0
    final static INVALID_DATE = 1
    final static MISSING_DATE = 2
        
    static Object getStatsBeforeParsing(Object json){
        
        // general
        
        json.meta.stats << ["tipps before cleanUp" :  json.package.tipps.size()]
        json.meta.stats << ["titles before cleanUp" : json.titles.size()]
        
        // titles
        
        List<Integer> titleName = [0,0,0]
        
        json.titles.each{ title ->
            def name = title.value.v.name
            
            if(name?.m.equals(Status.RESULT_OK.toString())) {
                titleName[Statistics.RESULT_OK]++
            }
            else if(name?.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                 titleName[Statistics.RESULT_MULTIPLE_MATCHES]++
            }
            else if(name?.m.equals(Status.RESULT_NO_MATCH.toString())) {
                titleName[Statistics.RESULT_NO_MATCH]++
            }
        }
        json.meta.stats << ["titles.name AS SINGLE MATCH":       titleName[Statistics.RESULT_OK]]
        json.meta.stats << ["titles.name with multiple matches": titleName[Statistics.RESULT_MULTIPLE_MATCHES]]
        json.meta.stats << ["titles.name with no match":         titleName[Statistics.RESULT_NO_MATCH]]
        
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
                        tmp[Statistics.RESULT_OK]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_INVALID.toString())) {
                         tmp[Statistics.RESULT_MULTIPLE_MATCHES]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE.toString())) {
                        tmp[Statistics.RESULT_NO_MATCH]++
                    }
                }
            }
        }
        
        identifiers.each{ i ->
            json.meta.stats["title.identifier ${i.key.toUpperCase()} AS VALID RESULTS"]    = i.value[Statistics.RESULT_OK]
            json.meta.stats["title.identifier ${i.key.toUpperCase()} are invalid"]         = i.value[Statistics.RESULT_MULTIPLE_MATCHES]
            json.meta.stats["title.identifier ${i.key.toUpperCase()} are in unkown state"] = i.value[Statistics.RESULT_NO_MATCH]
        }
        
        // tipps
        
        List<Integer> tippUrls = [0,0,0]
        
        json.package.tipps.each{ tipp ->
            def url = tipp.value.v.url

            if(url?.m.equals(Status.RESULT_OK.toString())) {
                tippUrls[Statistics.RESULT_OK]++
            }
            else if(url?.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                tippUrls[Statistics.RESULT_MULTIPLE_MATCHES]++
            }
            else if(url?.m.equals(Status.RESULT_NO_MATCH.toString())) {
                tippUrls[Statistics.RESULT_NO_MATCH]++
            }
        }
        json.meta.stats << ["tipp.title.url FOUND AS SINGLE RESULT":    tippUrls[Statistics.RESULT_OK]]
        json.meta.stats << ["tipp.title.url found with multiple results": tippUrls[Statistics.RESULT_MULTIPLE_MATCHES]]
        json.meta.stats << ["tipp.title.url found with no match":     tippUrls[Statistics.RESULT_NO_MATCH]]
        
        
        // dates
        
        List<Integer> phDates = [0,0,0]
        
        json.titles.each{ title ->
            title.value.v.publisher_history.each { ph ->              
                def sd = ph.startDate
                def ed = ph.endDate

                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                    phDates[Statistics.VALID_DATE]++
                }
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                    phDates[Statistics.INVALID_DATE]++
                }
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                    phDates[Statistics.MISSING_DATE]++
                }
            }
        }
        json.meta.stats << ["titles.publisher_history FOUND WITH VALID DATES":   phDates[Statistics.VALID_DATE]]
        json.meta.stats << ["titles.publisher_history found with invalid dates": phDates[Statistics.INVALID_DATE]]
        json.meta.stats << ["titles.publisher_history found with missing dates": phDates[Statistics.MISSING_DATE]]
        
        List<Integer> covDates = [0,0,0]
        
        json.package.tipps.each{ tipp ->
            tipp.value.v.each{ tippField ->
                if(tippField.key.equals("coverage")) {
                    tippField.value.each{ covField ->       
                        def sd = covField.startDate
                        def ed = covField.endDate
                       
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                            covDates[Statistics.VALID_DATE]++
                        }
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                            covDates[Statistics.INVALID_DATE]++
                        }
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                            covDates[Statistics.MISSING_DATE]++
                        }
                    }
                }
            }
        }

        json.meta.stats << ["tipp.coverage FOUND WITH VALID DATES":   covDates[Statistics.VALID_DATE]]
        json.meta.stats << ["tipp.coverage found with invalid dates": covDates[Statistics.INVALID_DATE]]
        json.meta.stats << ["tipp.coverage found with missing dates": covDates[Statistics.MISSING_DATE]]
        
        json
    }
    
    static Object getStatsAfterCleanUp(Object json){
        
        // general
        
        json.meta.stats << ["tipps AFTER CLEANUP" :  json.package.tipps.size()]
        json.meta.stats << ["titles AFTER CLEANUP" : json.titles.size()]
        
        json
    }
   
}