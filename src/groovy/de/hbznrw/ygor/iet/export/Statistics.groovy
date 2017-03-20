package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.tools.*
import de.hbznrw.ygor.iet.enums.*
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
    final static OPTION_8 = 7
    
    static Object getStatsBeforeParsing(Object json){
         
        json.meta.stats << ['general':[:]]
        json.meta.stats << ['tipps':[:]]
        json.meta.stats << ['titles':[:]]
        
        json.meta.stats.tipps        << ['coverage':[:]]
        json.meta.stats.tipps        << ['title':[:]]
        json.meta.stats.tipps.title  << ['identifiers':[:]]
        
        json.meta.stats.titles << ['identifiers':[:]]
        json.meta.stats.titles << ['publisher_history':[:]]
        json.meta.stats.titles << ['history_events':[:]]

        // general
        
        json.meta.stats.general << ["tipps before cleanUp":  json.package.tipps.size()]
        json.meta.stats.general << ["titles before cleanUp": json.titles.size()] 
        
        
        
        // titles
        // titles
        
        List<Integer> tName = [0,[],0,[],0,[]]
        
        json.titles.each{ title ->
            def name = title.value.v.name
            
            if(name?.m.equals(Status.VALIDATOR_STRING_IS_NOT_ATOMIC.toString())) {
                tName[Statistics.OPTION_1]++
                tName[Statistics.OPTION_2] << "${name.v}"
                
                    Statistics.addMeta(title, 'title.name', name)
            }
            else if(name?.m.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
                tName[Statistics.OPTION_3]++
                tName[Statistics.OPTION_4] << "${name.v}"
                 
                    Statistics.addMeta(title, 'title.name', name)
            }
            else if(name?.m.equals(Status.VALIDATOR_STRING_IS_MISSING.toString())) {
                tName[Statistics.OPTION_5]++
                //tName[Statistics.OPTION_6] << "${title.key}"
                
                    Statistics.addMeta(title, 'title.name', name)
            }
        }
        
        Statistics.format(tName, "names are not atomic", Statistics.OPTION_1, Statistics.OPTION_2, json.meta.stats.titles)
        Statistics.format(tName, "names are not valid",  Statistics.OPTION_3, Statistics.OPTION_4, json.meta.stats.titles)
        Statistics.format(tName, "names are missing",    Statistics.OPTION_5, Statistics.OPTION_6, json.meta.stats.titles)
          
        
        
        // titles.identifiers
        // titles.identifiers
        
        HashMap<String, List<Integer>> titleIdentifiers = [:]
        
        titleIdentifiers[TitleStruct.PISSN]    = [0, 0,[],0,[],0,[],0]
        titleIdentifiers[TitleStruct.EISSN]    = [0, 0,[],0,[],0,[],0]   
        titleIdentifiers[ZdbBridge.IDENTIFIER] = [0, 0,[],0,[],0,[],0]       
        titleIdentifiers[EzbBridge.IDENTIFIER] = [0, 0,[],0,[],0,[],0]
        
        json.titles.each{ title ->
            title.value.v.identifiers.each { ident ->
                def tmp = titleIdentifiers["${ident.type.v}"]
                
                if(tmp) {
                    if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_VALID.toString())) {
                        tmp[Statistics.OPTION_1]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_INVALID.toString())) {
                        tmp[Statistics.OPTION_2]++
                        tmp[Statistics.OPTION_3] << "${ident.value.v}"
                        
                            Statistics.addMeta(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC.toString())) {
                        tmp[Statistics.OPTION_4]++
                        tmp[Statistics.OPTION_5] << "${ident.value.v}"
                            
                            Statistics.addMeta(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_MISSING.toString())) {
                        tmp[Statistics.OPTION_6]++
                        //tmp[Statistics.OPTION_7] << "${title.key}"
                        
                            Statistics.addMeta(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE.toString())) {
                        tmp[Statistics.OPTION_8]++
                        
                            Statistics.addMeta(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                }
            }
        }
        titleIdentifiers.each{ i ->
            
            json.meta.stats.titles.identifiers["${i.key.toUpperCase()} ARE VALID"]           = i.value[Statistics.OPTION_1]
            json.meta.stats.titles.identifiers["${i.key.toUpperCase()} are in unkown state"] = i.value[Statistics.OPTION_8]  
            
            Statistics.format(i.value, "${i.key.toUpperCase()} are invalid",    Statistics.OPTION_2, Statistics.OPTION_3, json.meta.stats.titles.identifiers)
            Statistics.format(i.value, "${i.key.toUpperCase()} are not atomic", Statistics.OPTION_4, Statistics.OPTION_5, json.meta.stats.titles.identifiers)
            Statistics.format(i.value, "${i.key.toUpperCase()} are missing",    Statistics.OPTION_6, Statistics.OPTION_7, json.meta.stats.titles.identifiers)
            
        }
        
        
        
        // titles.publisher_history
        // titles.publisher_history
        
        List<Integer> publisher_history = [0,0,0]
        List<Integer> phName = [0,[],0,[],0,[]]
        
        json.titles.each{ title ->
            title.value.v.publisher_history?.each { ph ->
                
                if(ph.m.equals(Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_VALID.toString())){
                    publisher_history[Statistics.OPTION_1]++
                }
                else if(ph.m.equals(Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_INVALID.toString())){
                    publisher_history[Statistics.OPTION_2]++
                }
                else if(ph.m.equals(Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_UNDEF.toString())){
                    publisher_history[Statistics.OPTION_3]++
                }
                
                def name = ph.name

                if(name?.m.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
                    phName[Statistics.OPTION_1]++
                    phName[Statistics.OPTION_2] << "${name.v}"
                     
                        Statistics.addMeta(title, 'title.publisher_history.name', name)
                }
                else if(name?.m.equals(Status.VALIDATOR_STRING_IS_NOT_ATOMIC.toString())) {
                    phName[Statistics.OPTION_3]++
                    phName[Statistics.OPTION_4] << "${name.v}"
                    
                        Statistics.addMeta(title, 'title.publisher_history.name', name)
                }
                else if(name?.m.equals(Status.VALIDATOR_STRING_IS_MISSING.toString())) {
                    phName[Statistics.OPTION_5]++
                    //phName[Statistics.OPTION_6] << "${title.key}"
                     
                        Statistics.addMeta(title, 'title.publisher_history.name', name)
                }
            }
        }

        json.meta.stats.titles << ["publisher_history ARE VALID":     publisher_history[Statistics.OPTION_1]]
        json.meta.stats.titles << ["publisher_history are invalid":   publisher_history[Statistics.OPTION_2]]
        json.meta.stats.titles << ["publisher_history are undefined": publisher_history[Statistics.OPTION_3]]
        
        Statistics.format(phName, "names are not valid",  Statistics.OPTION_1, Statistics.OPTION_2, json.meta.stats.titles.publisher_history)
        Statistics.format(phName, "names are not atomic", Statistics.OPTION_3, Statistics.OPTION_4, json.meta.stats.titles.publisher_history)
        Statistics.format(phName, "names are missing",    Statistics.OPTION_5, Statistics.OPTION_6, json.meta.stats.titles.publisher_history)
 
        
        
        // tipps 
        // tipps
        
        List<Integer> tippUrls = [0, 0,[],0,[],0,[]]
        
        json.package.tipps.each{ tipp ->
            def url = tipp.value.v.url

            if(url?.m.equals(Status.VALIDATOR_URL_IS_VALID.toString())) {
                tippUrls[Statistics.OPTION_1]++
            }
            else if(url?.m.equals(Status.VALIDATOR_URL_IS_INVALID.toString())) {
                tippUrls[Statistics.OPTION_2]++
                tippUrls[Statistics.OPTION_3] << "${url.v}"
                
                    Statistics.addMeta(tipp, 'tipp.url', url)
            }
            else if(url?.m.equals(Status.VALIDATOR_URL_IS_NOT_ATOMIC.toString())) {
                tippUrls[Statistics.OPTION_4]++
                tippUrls[Statistics.OPTION_5] << "${url.v}"
                
                    Statistics.addMeta(tipp, 'tipp.url', url)
            }
            else if(url?.m.equals(Status.VALIDATOR_URL_IS_MISSING.toString())) {
                tippUrls[Statistics.OPTION_6]++
                //tippUrls[Statistics.OPTION_7] << "${tipp.key}"
                
                    Statistics.addMeta(tipp, 'tipp.url', url)
            }
        }
        
        json.meta.stats.tipps << ["url ARE VALID":  tippUrls[Statistics.OPTION_1]]
        
        Statistics.format(tippUrls, "url are not valid",  Statistics.OPTION_2, Statistics.OPTION_3, json.meta.stats.tipps)
        Statistics.format(tippUrls, "url are not atomic", Statistics.OPTION_4, Statistics.OPTION_5, json.meta.stats.tipps)
        Statistics.format(tippUrls, "url are missing",    Statistics.OPTION_6, Statistics.OPTION_7, json.meta.stats.tipps)
        
        
        
        // title.publisher_history.dates
        // title.publisher_history.dates
        
        List<Integer> phDates = [0, 0,[],0,[]]
        
        json.titles.each{ title ->
            title.value.v.publisher_history.each { ph -> 
                                    
                def sd = ph.startDate
                def ed = ph.endDate

                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                    phDates[Statistics.OPTION_1]++
                }
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                    phDates[Statistics.OPTION_2]++
                    phDates[Statistics.OPTION_3] << "${sd.v}"
                    
                        Statistics.addMeta(title, 'title.publisher_history.startDate', sd)
                }
                if(ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                    phDates[Statistics.OPTION_2]++
                    phDates[Statistics.OPTION_3] << "${ed.v}"
                    
                        Statistics.addMeta(title, 'title.publisher_history.endDate', ed)
                }
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                    phDates[Statistics.OPTION_4]++
                    //phDates[Statistics.OPTION_5] << "${title.key}"
                    
                        Statistics.addMeta(title, 'title.publisher_history.startDate', sd)
                }
                if(ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                    phDates[Statistics.OPTION_4]++
                    //phDates[Statistics.OPTION_5] << "${title.key}"
                    
                        Statistics.addMeta(title, 'title.publisher_history.endDate', ed)
                }
            }
        }     
        
        json.meta.stats.titles.publisher_history << ["VALID DATES FOUND": phDates[Statistics.OPTION_1]]
        
        Statistics.format(phDates, "invalid dates", Statistics.OPTION_2, Statistics.OPTION_3, json.meta.stats.titles.publisher_history)
        Statistics.format(phDates, "missing dates", Statistics.OPTION_4, Statistics.OPTION_5, json.meta.stats.titles.publisher_history)
        
        
        
        // TODO invalid coverages
        
        // tipp.coverage.dates
        // tipp.coverage.dates
        
        List<Integer> coverages = [0,0,0]
        List<Integer> covDates  = [0, 0,[],0,[]]
        
        json.package.tipps.each{ tipp ->
            tipp.value.v.each{ tippField ->
                if(tippField.key.equals("coverage")) {
                    tippField.value.m.each{ m ->
                        if(m.equals(Status.STRUCTVALIDATOR_COVERAGE_IS_VALID.toString())){
                            coverages[Statistics.OPTION_1]++
                        }
                        else if(m.equals(Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID.toString())){
                            coverages[Statistics.OPTION_2]++
                        }
                        else if(m.equals(Status.STRUCTVALIDATOR_COVERAGE_IS_UNDEF.toString())){
                            coverages[Statistics.OPTION_3]++
                        }
                    }
                    tippField.value.v.each{ covField ->       
                        def sd = covField.startDate
                        def ed = covField.endDate
                       
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                            covDates[Statistics.OPTION_1]++
                        }
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                            covDates[Statistics.OPTION_2]++
                            covDates[Statistics.OPTION_3] << "${sd.v}"
                            
                                Statistics.addMeta(tipp, 'tipp.coverage.startDate', sd)
                        }
                        if(ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                            covDates[Statistics.OPTION_2]++
                            covDates[Statistics.OPTION_3] << "${ed.v}"
                            
                                Statistics.addMeta(tipp, 'tipp.coverage.endDate', ed)
                        }
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                            covDates[Statistics.OPTION_4]++
                            //covDates[Statistics.OPTION_5] << "${tipp.key}"
                            
                                Statistics.addMeta(tipp, 'tipp.coverage.startDate', sd)
                        }
                        if(ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                            covDates[Statistics.OPTION_4]++
                            //covDates[Statistics.OPTION_5] << "${tipp.key}"
                            
                                Statistics.addMeta(tipp, 'tipp.coverage.endDate', ed)
                        }
                    }
                }
            }
        }
        
        json.meta.stats.tipps << ["coverage ARE VALID":     coverages[Statistics.OPTION_1]]
        json.meta.stats.tipps << ["coverage are invalid":   coverages[Statistics.OPTION_2]]
        json.meta.stats.tipps << ["coverage are undefined": coverages[Statistics.OPTION_3]]
        
        json.meta.stats.tipps.coverage << ["VALID DATES FOUND": covDates[Statistics.OPTION_1]]
        
        Statistics.format(covDates, "invalid dates", Statistics.OPTION_2, Statistics.OPTION_3, json.meta.stats.tipps.coverage)
        Statistics.format(covDates, "missing dates", Statistics.OPTION_4, Statistics.OPTION_5, json.meta.stats.tipps.coverage)

        
        
        // tipp.title.identifiers
        // tipp.title.identifiers
        
        HashMap<String, List<Integer>> tippIdentifiers = [:]
        
        tippIdentifiers[TitleStruct.EISSN]    = [0, 0,[],0,[],0,[],0]
        tippIdentifiers[ZdbBridge.IDENTIFIER] = [0, 0,[],0,[],0,[],0]
        
        json.package.tipps.each{ tipp ->
            tipp.value.v.title.identifiers.each { ident ->
                def tmp = tippIdentifiers["${ident.type.v}"]
                
                if(tmp) {
                    if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_VALID.toString())) {
                        tmp[Statistics.OPTION_1]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_INVALID.toString())) {
                        tmp[Statistics.OPTION_2]++
                        tmp[Statistics.OPTION_3] << "${ident.value.v}"
                        
                            Statistics.addMeta(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC.toString())) {
                        tmp[Statistics.OPTION_4]++
                        tmp[Statistics.OPTION_5] << "${ident.value.v}"
                            
                            Statistics.addMeta(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_MISSING.toString())) {
                        tmp[Statistics.OPTION_6]++
                        //tmp[Statistics.OPTION_7] << "${title.key}"
                        
                            Statistics.addMeta(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE.toString())) {
                        tmp[Statistics.OPTION_8]++
                        
                            Statistics.addMeta(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                }
            }
        }
        tippIdentifiers.each{ i ->
            
            json.meta.stats.tipps.title.identifiers["${i.key.toUpperCase()} ARE VALID"]           = i.value[Statistics.OPTION_1]
            json.meta.stats.tipps.title.identifiers["${i.key.toUpperCase()} are in unkown state"] = i.value[Statistics.OPTION_8]
            
            Statistics.format(i.value, "${i.key.toUpperCase()} are invalid",    Statistics.OPTION_2, Statistics.OPTION_3, json.meta.stats.tipps.title.identifiers)
            Statistics.format(i.value, "${i.key.toUpperCase()} are not atomic", Statistics.OPTION_4, Statistics.OPTION_5, json.meta.stats.tipps.title.identifiers)
            Statistics.format(i.value, "${i.key.toUpperCase()} are missing",    Statistics.OPTION_6, Statistics.OPTION_7, json.meta.stats.tipps.title.identifiers)
            
        } 
        
        
        
        // TODO invalid history events
        
        // title.history_events
        // title.history_events
        
        List<Integer> historyEvents = [0,0,0]
        
        json.titles.each{ title ->
            title.value.v.history_events.each { he ->              
        
                if(he?.m.equals(Status.STRUCTVALIDATOR_HISTORYEVENT_IS_VALID.toString())){
                    historyEvents[Statistics.OPTION_1]++
                }
                else if(he?.m.equals(Status.STRUCTVALIDATOR_HISTORYEVENT_IS_INVALID.toString())){
                    historyEvents[Statistics.OPTION_2]++
                }
                else if(he?.m.equals(Status.STRUCTVALIDATOR_HISTORYEVENT_IS_UNDEF.toString())){
                    historyEvents[Statistics.OPTION_3]++
                }
                
            }
        }
        json.meta.stats.titles.history_events << ["history_events ARE VALID":     historyEvents[Statistics.OPTION_1]]
        json.meta.stats.titles.history_events << ["history_events are invalid":   historyEvents[Statistics.OPTION_2]]
        json.meta.stats.titles.history_events << ["history_events are undefined": historyEvents[Statistics.OPTION_3]]
        
        json
    }
    
    static Object getStatsAfterCleanUp(Object json){
        
        // general
        
        json.meta.stats.general << ["tipps after cleanup":  json.package.tipps.size()]
        json.meta.stats.general << ["titles after cleanup": json.titles.size()]
        
        json
    }
    
    static format(List data, String text, int count, int result, Object target) {
        
        if(data[count] > 0 && data[result].minus("").size() > 0) {
            target.put("${text}", [data[count], data[result].minus("")])
        }
        else {
            target.put("${text}", data[count])
        }
    }
    
    static addMeta(Object target, String dom, Object obj) {
        
        obj.dom = dom
        target.value.v._meta.add(obj)
    }
}