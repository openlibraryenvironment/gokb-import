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
    final static OPTION_4 = 3
    final static OPTION_5 = 4
    final static OPTION_6 = 5
    final static OPTION_7 = 6
    
    static Object getStatsBeforeParsing(Object json){
         
        json.meta.stats << ['general':[:]]
        json.meta.stats << ['tipps':[:]]
        json.meta.stats << ['titles':[:]]
        json.meta.stats << ['identifiers':[:]]
        
        json.meta.stats.tipps << ['coverage':[:]]
        json.meta.stats.tipps << ['title':[:]]
        json.meta.stats.titles << ['publisher_history':[:]]

        // general
        
        json.meta.stats.general << ["tipps before cleanUp":  json.package.tipps.size()]
        json.meta.stats.general << ["titles before cleanUp": json.titles.size()]
        
        // titles
        
        List<Integer> tName = [0,[],0,[],0]
        
        json.titles.each{ title ->
            def name = title.value.v.name
            
            if(name?.m.equals(Status.VALIDATOR_STRING_IS_NOT_ATOMIC.toString())) {
                tName[Statistics.OPTION_1]++
                tName[Statistics.OPTION_2] << name.v
            }
            else if(name?.m.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
                 tName[Statistics.OPTION_3]++
                 tName[Statistics.OPTION_4] << name.v
            }
            else if(name?.m.equals(Status.VALIDATOR_STRING_IS_MISSING.toString())) {
                tName[Statistics.OPTION_5]++
            }
        }
        
        if(tName[Statistics.OPTION_1] > 0) {
            json.meta.stats.titles << ["names are not atomic": [tName[Statistics.OPTION_1], tName[Statistics.OPTION_2].minus("")]]  
        }
        else {
            json.meta.stats.titles << ["names are not atomic": 0]
        }
        if(tName[Statistics.OPTION_3] > 0) {
            json.meta.stats.titles << ["names are not valid": [tName[Statistics.OPTION_3], tName[Statistics.OPTION_4].minus("")]]
        }
        else {
            json.meta.stats.titles << ["names are not valid": 0]
        }
        json.meta.stats.titles << ["names are missing": tName[Statistics.OPTION_5]]
            
        // titles.identifiers
        
        HashMap<String, List<Integer>> identifiers = [:]
        
        identifiers[TitleStruct.PISSN]    = [0,0,[],0,[],0,0]
        identifiers[TitleStruct.EISSN]    = [0,0,[],0,[],0,0]   
        identifiers[ZdbBridge.IDENTIFIER] = [0,0,[],0,[],0,0]       
        identifiers[EzbBridge.IDENTIFIER] = [0,0,[],0,[],0,0]
        
        json.titles.each{ title ->
            title.value.v.identifiers.each { ident ->
                def tmp = identifiers["${ident.type.v}"]
                
                if(tmp) {
                    if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_VALID.toString())) {
                        tmp[Statistics.OPTION_1]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_INVALID.toString())) {
                        tmp[Statistics.OPTION_2]++
                        tmp[Statistics.OPTION_3] << ident.value.v
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC.toString())) {
                        tmp[Statistics.OPTION_4]++
                        tmp[Statistics.OPTION_5] << ident.value.v
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_MISSING.toString())) {
                        tmp[Statistics.OPTION_6]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE.toString())) {
                        tmp[Statistics.OPTION_7]++
                    }
                }
            }
        }
        identifiers.each{ i ->
            
            json.meta.stats.identifiers["${i.key.toUpperCase()} ARE VALID"]           =  i.value[Statistics.OPTION_1]
            json.meta.stats.identifiers["${i.key.toUpperCase()} are missing"]         =  i.value[Statistics.OPTION_6]
            json.meta.stats.identifiers["${i.key.toUpperCase()} are in unkown state"] =  i.value[Statistics.OPTION_7]
            
            if(i.value[Statistics.OPTION_2] > 0) {
                json.meta.stats.identifiers["${i.key.toUpperCase()} are invalid"] = [i.value[Statistics.OPTION_2], i.value[Statistics.OPTION_3].minus("")]
            }
            else {
                json.meta.stats.identifiers["${i.key.toUpperCase()} are invalid"] = 0
            }
            if(i.value[Statistics.OPTION_4] > 0) {
                json.meta.stats.identifiers["${i.key.toUpperCase()} are not atomic"] = [i.value[Statistics.OPTION_4], i.value[Statistics.OPTION_5].minus("")]
            }
            else {
                json.meta.stats.identifiers["${i.key.toUpperCase()} are not atomic"] = 0
            }
            
        }
        
        // titles.publisher_history
        
        List<Integer> phName = [0,[],0,[],0]
        
        json.titles.each{ title ->
            title.value.v.publisher_history?.each { ph ->               
                def name = ph.name

                if(name?.m.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
                     phName[Statistics.OPTION_1]++
                     phName[Statistics.OPTION_2] << name.v
                }
                else if(name?.m.equals(Status.VALIDATOR_STRING_IS_NOT_ATOMIC.toString())) {
                    phName[Statistics.OPTION_3]++
                    phName[Statistics.OPTION_4] << name.v
                }
                else if(name?.m.equals(Status.VALIDATOR_STRING_IS_MISSING.toString())) {
                     phName[Statistics.OPTION_5]++
                }
            }
        }
        
        if(phName[Statistics.OPTION_1] > 0) {
            json.meta.stats.titles.publisher_history << ["names are not valid": [phName[Statistics.OPTION_1], phName[Statistics.OPTION_2].minus("")]]
        }
        else {
            json.meta.stats.titles.publisher_history << ["names are not valid": 0]
        }
        if(phName[Statistics.OPTION_3] > 0) {
            json.meta.stats.titles.publisher_history << ["names are not atomic": [phName[Statistics.OPTION_3], phName[Statistics.OPTION_4].minus("")]]
        } 
        else {
            json.meta.stats.titles.publisher_history << ["names are not atomic": 0]
        }
        json.meta.stats.titles.publisher_history << ["names are missing": phName[Statistics.OPTION_5]]
        
        // tipps  
        
        List<Integer> tippUrls = [0,0,[],0,[],0]
        
        json.package.tipps.each{ tipp ->
            def url = tipp.value.v.url

            if(url?.m.equals(Status.VALIDATOR_URL_IS_VALID.toString())) {
                tippUrls[Statistics.OPTION_1]++
            }
            else if(url?.m.equals(Status.VALIDATOR_URL_IS_INVALID.toString())) {
                tippUrls[Statistics.OPTION_2]++
                tippUrls[Statistics.OPTION_3] << url.v
            }
            else if(url?.m.equals(Status.VALIDATOR_URL_IS_NOT_ATOMIC.toString())) {
                tippUrls[Statistics.OPTION_4]++
                tippUrls[Statistics.OPTION_5] << url.v
            }
            else if(url?.m.equals(Status.VALIDATOR_URL_IS_MISSING.toString())) {
                tippUrls[Statistics.OPTION_6]++
            }
        }
        
        if(tippUrls[Statistics.OPTION_2] > 0) {
            json.meta.stats.tipps.title << ["url are not valid": [tippUrls[Statistics.OPTION_2], tippUrls[Statistics.OPTION_3].minus("")]] 
        }
        else {
            json.meta.stats.tipps.title << ["url are not valid": 0]
        }   
        if(tippUrls[Statistics.OPTION_4] > 0) {
            json.meta.stats.tipps.title << ["url are not atomic": [tippUrls[Statistics.OPTION_4], tippUrls[Statistics.OPTION_5].minus("")]]
        }
        else {
            json.meta.stats.tipps.title << ["url are not atomic": 0]
        }
        json.meta.stats.tipps.title << ["url ARE VALID":   tippUrls[Statistics.OPTION_1]]
        json.meta.stats.tipps.title << ["url are missing": tippUrls[Statistics.OPTION_6]]
        
        // dates
        
        List<Integer> phDates = [0,0,[],0]
        
        json.titles.each{ title ->
            title.value.v.publisher_history.each { ph ->              
                def sd = ph.startDate
                def ed = ph.endDate

                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                    phDates[Statistics.OPTION_1]++
                }
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                    phDates[Statistics.OPTION_2]++
                    phDates[Statistics.OPTION_3] << sd.v
                }
                if(ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                    phDates[Statistics.OPTION_2]++
                    phDates[Statistics.OPTION_3] << ed.v
                }
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                    phDates[Statistics.OPTION_4]++
                }
            }
        }               
        
        if(phDates[Statistics.OPTION_2] > 0) {
            json.meta.stats.titles.publisher_history << ["invalid dates": [phDates[Statistics.OPTION_2], phDates[Statistics.OPTION_3].minus("")]]
        }
        else {
            json.meta.stats.titles.publisher_history << ["invalid dates": 0]
        }
        json.meta.stats.titles.publisher_history << ["VALID DATES FOUND": phDates[Statistics.OPTION_1]]
        json.meta.stats.titles.publisher_history << ["missing dates":     phDates[Statistics.OPTION_4]]
        
        List<Integer> coverages = [0,0]
        List<Integer> covDates  = [0,0,[],0]
        
        // TODO invalid coverages
        
        json.package.tipps.each{ tipp ->
            tipp.value.v.each{ tippField ->
                if(tippField.key.equals("coverage")) {
                    tippField.value.m.each{ m ->
                        if(m.equals(Status.VALIDATOR_COVERAGE_IS_VALID.toString()))
                            coverages[Statistics.OPTION_1]++
                        else if(m.equals(Status.VALIDATOR_COVERAGE_IS_INVALID.toString()))
                            coverages[Statistics.OPTION_2]++
                    }
                    tippField.value.v.each{ covField ->       
                        def sd = covField.startDate
                        def ed = covField.endDate
                       
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                            covDates[Statistics.OPTION_1]++
                        }
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                            covDates[Statistics.OPTION_2]++
                            covDates[Statistics.OPTION_3] << sd.v
                        }
                        if(ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                            covDates[Statistics.OPTION_2]++
                            covDates[Statistics.OPTION_3] << ed.v
                        }
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                            covDates[Statistics.OPTION_4]++
                        }
                    }
                }
            }
        }
        
        json.meta.stats.tipps << ["coverage ARE VALID":   coverages[Statistics.OPTION_1]]
        json.meta.stats.tipps << ["coverage are invalid": coverages[Statistics.OPTION_2]]
        
        if(covDates[Statistics.OPTION_2] > 0)  {
            json.meta.stats.tipps.coverage << ["invalid dates": [covDates[Statistics.OPTION_2], covDates[Statistics.OPTION_3].minus("")]]
        }
        else {
            json.meta.stats.tipps.coverage << ["invalid dates": 0]
        }
        json.meta.stats.tipps.coverage << ["VALID DATES FOUND": covDates[Statistics.OPTION_1]]
        json.meta.stats.tipps.coverage << ["missing dates":     covDates[Statistics.OPTION_4]]
        
        // TODO invalid history events
        
         List<Integer> historyEvents = [0,0]
        
        json.titles.each{ title ->
            title.value.v.history_events.each { he ->              
        
                if(he?.m.equals(Status.VALIDATOR_HISTORYEVENT_IS_VALID.toString())){
                    historyEvents[Statistics.OPTION_1]++
                }
                if(he?.m.equals(Status.VALIDATOR_HISTORYEVENT_IS_INVALID.toString())){
                    historyEvents[Statistics.OPTION_2]++
                }
                
            }
        }
        json.meta.stats.titles << ["history_events ARE VALID":   historyEvents[Statistics.OPTION_1]]
        json.meta.stats.titles << ["history_events are invalid": historyEvents[Statistics.OPTION_2]]

        json
    }
    
    static Object getStatsAfterCleanUp(Object json){
        
        // general
        
        json.meta.stats.general << ["tipps after cleanup":  json.package.tipps.size()]
        json.meta.stats.general << ["titles after cleanup": json.titles.size()]
        
        json
    }
   
}