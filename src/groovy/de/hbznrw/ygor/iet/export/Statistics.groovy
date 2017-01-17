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
        
    static Object statisticBeforeParsing(Object json){
        
        // general
        
        json.meta.stats << ["tipps before cleanUp" :  json.package.v.tipps.v.size()]
        json.meta.stats << ["titles before cleanUp" : json.titles.v.size()]
        
        // titles
        
        List<Integer> titleName = [0,0,0]
        
        json.titles.v.each{ key, value ->
            value.v.each{ titleField ->
                if(titleField.key.equals("name")) {
                    
                    if(titleField.value.m.equals(Status.RESULT_OK.toString())) {
                        titleName[Statistics.RESULT_OK]++
                    }
                    else if(titleField.value.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                         titleName[Statistics.RESULT_MULTIPLE_MATCHES]++
                    }
                    else if(titleField.value.m.equals(Status.RESULT_NO_MATCH.toString())) {
                        titleName[Statistics.RESULT_NO_MATCH]++
                    }
                }
            } 
        }
        json.meta.stats << ["titles.name WITH SINGLE MATCH":     titleName[Statistics.RESULT_OK]]
        json.meta.stats << ["titles.name with multiple matches": titleName[Statistics.RESULT_MULTIPLE_MATCHES]]
        json.meta.stats << ["titles.name with no match":         titleName[Statistics.RESULT_NO_MATCH]]
        
        // titles.publisher_history
        
        List<Integer> publisherHistory = [0,0,0]
        
        json.titles.v.each{ key, value ->
            value.v.each { titleField ->
                if(titleField.key.equals("publisher_history")) {
                    
                    if(titleField.value.m.equals(Status.RESULT_OK.toString())) {
                        publisherHistory[Statistics.RESULT_OK]++
                    }
                    else if(titleField.value.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                         publisherHistory[Statistics.RESULT_MULTIPLE_MATCHES]++
                    }
                    else if(titleField.value.m.equals(Status.RESULT_NO_MATCH.toString())) {
                        publisherHistory[Statistics.RESULT_NO_MATCH]++
                    }
                }
            }
        }
        json.meta.stats << ["titles.publisher_history with single result":    publisherHistory[Statistics.RESULT_OK]]
        json.meta.stats << ["titles.publisher_history with multiple results": publisherHistory[Statistics.RESULT_MULTIPLE_MATCHES]]
        json.meta.stats << ["titles.publisher_history with no result":        publisherHistory[Statistics.RESULT_NO_MATCH]]
        
        
        // titles.identifiers
        
        HashMap<String, List<Integer>> identifiers = [:]
        
        identifiers[TitleStruct.PISSN]      = [0,0,0]
        identifiers[TitleStruct.EISSN]      = [0,0,0]   
        identifiers[ZdbBridge.IDENTIFIER]   = [0,0,0]       
        identifiers[EzbBridge.IDENTIFIER]   = [0,0,0]
        
        json.titles.v.each{ key, value ->
            value.v.identifiers.v.each { ident ->
                
                def tmp = identifiers["${ident.v.type.v}"]
                if(tmp) {
                    if(ident.v.value.m.equals(Status.RESULT_OK.toString())) {
                        tmp[Statistics.RESULT_OK]++
                    }
                    else if(ident.v.value.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                         tmp[Statistics.RESULT_MULTIPLE_MATCHES]++
                    }
                    else if(ident.v.value.m.equals(Status.RESULT_NO_MATCH.toString())) {
                        tmp[Statistics.RESULT_NO_MATCH]++
                    }
                }
            }
        }
        
        identifiers.each{ i ->
            json.meta.stats["title.identifier ${i.key.toUpperCase()} GOT SINGLE RESULT"]     = i.value[Statistics.RESULT_OK]
            json.meta.stats["title.identifier ${i.key.toUpperCase()} got multiple results"] = i.value[Statistics.RESULT_MULTIPLE_MATCHES]
            json.meta.stats["title.identifier ${i.key.toUpperCase()} got no result"]         = i.value[Statistics.RESULT_NO_MATCH]
        }
        
        // tipps
        
        List<Integer> tippUrls = [0,0,0]
        
        json.package.v.tipps.v.each{ key, value ->
            value.v.each{ tippField ->

                if(tippField.key.equals("url")) {
                    if(tippField.value.m.equals(Status.RESULT_OK.toString())) {
                        tippUrls[Statistics.RESULT_OK]++
                    }
                    else if(tippField.value.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                        tippUrls[Statistics.RESULT_MULTIPLE_MATCHES]++
                    }
                    else if(tippField.value.m.equals(Status.RESULT_NO_MATCH.toString())) {
                        tippUrls[Statistics.RESULT_NO_MATCH]++
                    }
                }
            }
        }
        json.meta.stats << ["tipp.title.url GOT SINGLE RESULT":    tippUrls[Statistics.RESULT_OK]]
        json.meta.stats << ["tipp.title.url got multiple results": tippUrls[Statistics.RESULT_MULTIPLE_MATCHES]]
        json.meta.stats << ["tipp.title.url got not matching":     tippUrls[Statistics.RESULT_NO_MATCH]]
        
        
        // dates
        
        List<Integer> phDates = [0,0,0]
        
        json.titles.v.each{ title ->
            title.value.v.publisher_history.v.each { ph ->
                ph.v.each { phe ->
                    
                    if(phe.value.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                        phDates[Statistics.VALID_DATE]++
                    }
                    else if(phe.value.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                        phDates[Statistics.INVALID_DATE]++
                    }
                    else if(phe.value.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                        phDates[Statistics.MISSING_DATE]++
                    }
                }
            }
        }
        json.meta.stats << ["titles.publisher_history WITH VALID DATES":   phDates[Statistics.VALID_DATE]]
        json.meta.stats << ["titles.publisher_history with invalid dates": phDates[Statistics.INVALID_DATE]]
        json.meta.stats << ["titles.publisher_history with missing dates": phDates[Statistics.MISSING_DATE]]
        
        json
    }
    
    static Object statisticAfterCleanUp(Object json){
        
        // general
        
        json.meta.stats << ["tipps AFTER CLEANUP" :  json.package.tipps.size()]
        json.meta.stats << ["titles AFTER CLEANUP" : json.titles.size()]
        
        json
    }
   
}