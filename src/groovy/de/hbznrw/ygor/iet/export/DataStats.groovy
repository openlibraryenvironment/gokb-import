package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.tools.*
import de.hbznrw.ygor.iet.enums.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import de.hbznrw.ygor.iet.export.structure.TitleStruct
import de.hbznrw.ygor.iet.bridge.*

class DataStats {
    
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
                        titleName[DataStats.RESULT_OK]++
                    }
                    else if(titleField.value.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                         titleName[DataStats.RESULT_MULTIPLE_MATCHES]++
                    }
                    else if(titleField.value.m.equals(Status.RESULT_NO_MATCH.toString())) {
                        titleName[DataStats.RESULT_NO_MATCH]++
                    }
                }
            } 
        }
        json.meta.stats << ["titles.name WITH SINGLE MATCH":     titleName[DataStats.RESULT_OK]]
        json.meta.stats << ["titles.name with multiple matches": titleName[DataStats.RESULT_MULTIPLE_MATCHES]]
        json.meta.stats << ["titles.name with no match":         titleName[DataStats.RESULT_NO_MATCH]]
        
        // titles.publisher_history
        
        List<Integer> publisherHistory = [0,0,0]
        
        json.titles.v.each{ key, value ->
            value.v.each { titleField ->
                if(titleField.key.equals("publisher_history")) {
                    if(titleField.value.m.equals(Status.RESULT_OK.toString())) {
                        publisherHistory[DataStats.RESULT_OK]++
                    }
                    else if(titleField.value.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                         publisherHistory[DataStats.RESULT_MULTIPLE_MATCHES]++
                    }
                    else if(titleField.value.m.equals(Status.RESULT_NO_MATCH.toString())) {
                        publisherHistory[DataStats.RESULT_NO_MATCH]++
                    }
                }
            }
        }
        json.meta.stats << ["titles.publisher_history with single result":    publisherHistory[DataStats.RESULT_OK]]
        json.meta.stats << ["titles.publisher_history with multiple results": publisherHistory[DataStats.RESULT_MULTIPLE_MATCHES]]
        json.meta.stats << ["titles.publisher_history with no result":        publisherHistory[DataStats.RESULT_NO_MATCH]]
        
        
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
                        tmp[DataStats.RESULT_OK]++
                    }
                    else if(ident.v.value.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                         tmp[DataStats.RESULT_MULTIPLE_MATCHES]++
                    }
                    else if(ident.v.value.m.equals(Status.RESULT_NO_MATCH.toString())) {
                        tmp[DataStats.RESULT_NO_MATCH]++
                    }
                }
            }
        }
        
        identifiers.each{ i ->
            json.meta.stats["title.identifier ${i.key.toUpperCase()} GOT SINGLE RESULT"]     = i.value[DataStats.RESULT_OK]
            json.meta.stats["title.identifier ${i.key.toUpperCase()} got multiple results"] = i.value[DataStats.RESULT_MULTIPLE_MATCHES]
            json.meta.stats["title.identifier ${i.key.toUpperCase()} got no result"]         = i.value[DataStats.RESULT_NO_MATCH]
        }
        
        // tipps
        
        List<Integer> tippUrls = [0,0,0]
        
        json.package.v.tipps.v.each{ key, value ->
            value.v.each{ tippField ->

                if(tippField.key.equals("url")) {
                    
                    if(tippField.value.m.equals(Status.RESULT_OK.toString())) {
                        tippUrls[DataStats.RESULT_OK]++
                    }
                    else if(tippField.value.m.equals(Status.RESULT_MULTIPLE_MATCHES.toString())) {
                        tippUrls[DataStats.RESULT_MULTIPLE_MATCHES]++
                    }
                    else if(tippField.value.m.equals(Status.RESULT_NO_MATCH.toString())) {
                        tippUrls[DataStats.RESULT_NO_MATCH]++
                    }
                }
            }
        }
        json.meta.stats << ["tipp.title.url GOT SINGLE RESULT":    tippUrls[DataStats.RESULT_OK]]
        json.meta.stats << ["tipp.title.url got multiple results": tippUrls[DataStats.RESULT_MULTIPLE_MATCHES]]
        json.meta.stats << ["tipp.title.url got not matching":     tippUrls[DataStats.RESULT_NO_MATCH]]
        
        
        // dates
        
        List<Integer> phDates = [0,0,0]
        
        json.titles.v.each{ title ->
            title.value.v.publisher_history.v.each { ph ->
                ph.v.each { phe ->
                    
                    println phe.value
                    
                    if(phe.value.m.equals(Status.VALID_DATE.toString())){
                        phDates[DataStats.VALID_DATE]++
                    }
                    else if(phe.value.m.equals(Status.INVALID_DATE.toString())){
                        phDates[DataStats.INVALID_DATE]++
                    }
                    else if(phe.value.m.equals(Status.MISSING_DATE.toString())){
                        phDates[DataStats.MISSING_DATE]++
                    }
                }
            }
        }
        json.meta.stats << ["titles.publisher_history WITH VALID DATES":   phDates[DataStats.VALID_DATE]]
        json.meta.stats << ["titles.publisher_history with invalid dates": phDates[DataStats.INVALID_DATE]]
        json.meta.stats << ["titles.publisher_history with missing dates": phDates[DataStats.MISSING_DATE]]
        
        json
    }
    
    static Object statisticAfterCleanUp(Object json){
        
        // general
        
        json.meta.stats << ["tipps AFTER CLEANUP" :  json.package.tipps.size()]
        json.meta.stats << ["titles AFTER CLEANUP" : json.titles.size()]
        
        json
    }
   
}