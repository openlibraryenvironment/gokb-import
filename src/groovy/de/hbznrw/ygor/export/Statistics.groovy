package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.node.JsonNodeFactory
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.node.ArrayNode
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.export.structure.TitleStruct
import de.hbznrw.ygor.bridges.*
import ygor.Enrichment
import ygor.Record
import ygor.field.MappingsContainer
import ygor.field.MultiField

class Statistics {

    static JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance
    
    final static COUNT_1 = 0
    final static LIST_1  = 1
    final static COUNT_2 = 2
    final static LIST_2  = 3
    final static COUNT_3 = 4
    final static LIST_3  = 5
    final static COUNT_4 = 6
    final static LIST_4  = 7
    final static COUNT_5 = 8
    final static LIST_5  = 9


    static ObjectNode getRecordsStatisticsBeforeParsing(Enrichment enrichment){

        ObjectNode stats = new ObjectNode(NODE_FACTORY)
        stats.set("general", new ObjectNode(NODE_FACTORY))
        stats.set("tipps", new ObjectNode(NODE_FACTORY))
        stats.set("titles", new ObjectNode(NODE_FACTORY))

        stats.set("tipps", new ObjectNode(NODE_FACTORY))
        stats.get("tipps").set("coverage", new ObjectNode(NODE_FACTORY))
        stats.get("tipps").set("title", new ObjectNode(NODE_FACTORY))
        stats.get("tipps").set("title", new ObjectNode(NODE_FACTORY))
        stats.get("tipps").get("title").set("identifiers", new ObjectNode(NODE_FACTORY))

        stats.set("titles", new ObjectNode(NODE_FACTORY))
        stats.get("titles").set("identifiers", new ObjectNode(NODE_FACTORY))
        stats.get("titles").set("publisher_history", new ObjectNode(NODE_FACTORY))
        stats.get("titles").set("historyEvents", new ObjectNode(NODE_FACTORY))

        Statistics.processTipps(enrichment.dataContainer.records, stats)
        // Statistics.processTitles(enrichment.dataContainer.records, stats)

        stats.set("general", new ObjectNode(NODE_FACTORY))
        stats.get("general").set("tipps before cleanUp", new TextNode(String.valueOf(enrichment.dataContainer.tipps.size())))
        stats.get("general").set("titles before cleanUp", new TextNode(String.valueOf(enrichment.dataContainer.titles.size())))

        /* TODO
        json.meta.stats.general << ["processed kbart entries": json.meta.stash.processedKbartEntries]
        json.meta.stats.general << ["ignored kbart entries":   json.meta.stash.ignoredKbartEntries]
        json.meta.stats.general << ["duplicate key entries":   json.meta.stash.duplicateKeyEntries] */

        enrichment.stats = stats
    }
    
    static Object getStatsBeforeParsing(Object json){
         
        json.meta.stats             << ['general':[:]]
        json.meta.stats             << ['tipps':[:]]
        json.meta.stats             << ['titles':[:]]
        
        json.meta.stats.tipps       << ['coverage':[:]]
        json.meta.stats.tipps       << ['title':[:]]
        json.meta.stats.tipps.title << ['identifiers':[:]]
        
        json.meta.stats.titles      << ['identifiers':[:]]
        json.meta.stats.titles      << ['publisher_history':[:]]
        json.meta.stats.titles      << ['historyEvents':[:]]

        // Statistics.processTipps(json)
        // Statistics.processTitles(json)
        
        json.meta.stats.general << ["tipps before cleanUp":    json.package.tipps.size()]
        json.meta.stats.general << ["titles before cleanUp":   json.titles.size()]

        json.meta.stats.general << ["processed kbart entries": json.meta.stash.processedKbartEntries]
        json.meta.stats.general << ["ignored kbart entries":   json.meta.stash.ignoredKbartEntries]
        json.meta.stats.general << ["duplicate key entries":   json.meta.stash.duplicateKeyEntries]

        json
    }
    
    static Object processTipps(Set<Record> records, ObjectNode stats){

        List<Integer> tippTitleName = Statistics.getStorage()
        records.each{record ->

            MultiField nameField = record.getMultiField("publicationTitle")
            String nameStatus = nameField.status

            if(nameStatus?.equals(Status.VALIDATOR_STRING_IS_VALID.toString())) {
                tippTitleName[Statistics.COUNT_1]++
            }
            else if(nameStatus?.equals(Status.VALIDATOR_STRING_IS_NOT_ATOMIC.toString())) {
                tippTitleName[Statistics.COUNT_2]++
                tippTitleName[Statistics.LIST_2] << "${nameField.getPrioValue()}"
                // Statistics.addMetaData(record, 'tipp.title.name', nameStatus) TODO ?
            }
            else if(nameStatus?.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
                tippTitleName[Statistics.COUNT_3]++
                tippTitleName[Statistics.LIST_3] << "${nameField.getPrioValue()}"
                // Statistics.addMetaData(record, 'tipp.title.name', nameStatus) TODO ?
            }
            else if(nameStatus?.m.equals(Status.VALIDATOR_STRING_IS_MISSING.toString())) {
                tippTitleName[Statistics.COUNT_4]++
                // Statistics.addMetaData(record, 'tipp.title.name', nameStatus) TODO ?
            }
        }
        Statistics.format("NAME IS VALID",      tippTitleName, Statistics.COUNT_1, Statistics.LIST_1, stats)
        Statistics.format("name is not atomic", tippTitleName, Statistics.COUNT_2, Statistics.LIST_2, stats)
        Statistics.format("name is not valid",  tippTitleName, Statistics.COUNT_3, Statistics.LIST_3, stats)
        Statistics.format("name is missing",    tippTitleName, Statistics.COUNT_4, Statistics.LIST_4, stats)


        List<Integer> tippUrls = Statistics.getStorage()
        records.each{record ->
            def urlField = record.getMultiField("titleUrl")
            String urlStatus = urlField.status

            if(urlStatus.equals(Status.VALIDATOR_URL_IS_VALID.toString())) {
                tippUrls[Statistics.COUNT_1]++
            }
            else if(urlStatus.equals(Status.VALIDATOR_URL_IS_INVALID.toString())) {
                tippUrls[Statistics.COUNT_2]++
                tippUrls[Statistics.LIST_2] << "${urlField.getPrioValue()}"
                // Statistics.addMetaData(record, 'tipp.url', url) TODO ?
            }
            else if(urlStatus.equals(Status.VALIDATOR_URL_IS_NOT_ATOMIC.toString())) {
                tippUrls[Statistics.COUNT_3]++
                tippUrls[Statistics.LIST_3] << "${url.org}"
                // Statistics.addMetaData(record, 'tipp.url', url) TODO ?
            }
            else if(urlStatus.equals(Status.VALIDATOR_URL_IS_MISSING.toString())) {
                tippUrls[Statistics.COUNT_4]++
                // Statistics.addMetaData(record, 'tipp.url', url) TODO ?
            }
            else if(urlStatus.equals(Status.VALIDATOR_TIPPURL_NOT_MATCHING.toString())) {
                tippUrls[Statistics.COUNT_5]++
                tippUrls[Statistics.LIST_5] << "${Normalizer.normString(urlField.getPrioValue())}"
                // Statistics.addMetaData(record, 'tipp.url', url) TODO ?
            }
        }
        Statistics.format("URL IS VALID",      tippUrls, Statistics.COUNT_1, Statistics.LIST_1, stats)
        Statistics.format("url is not valid",  tippUrls, Statistics.COUNT_2, Statistics.LIST_2, stats)
        Statistics.format("url is not atomic", tippUrls, Statistics.COUNT_3, Statistics.LIST_3, stats)
        Statistics.format("url is missing",    tippUrls, Statistics.COUNT_4, Statistics.LIST_4, stats)
        Statistics.format("url is not matching against packageHeader.nominalPlattform",
            tippUrls, Statistics.COUNT_5, Statistics.LIST_5, stats)


        HashMap<String, List<Integer>> tippIdentifiers = [:]
        tippIdentifiers[TitleStruct.EISSN]    = Statistics.getStorage()
        tippIdentifiers[ZdbBridge.IDENTIFIER] = Statistics.getStorage()
        tippIdentifiers[TitleStruct.DOI]      = Statistics.getStorage()
        
        /*json.package.tipps.each{ tipp ->
            
            tipp.value.v.title.v.identifiers.each { ident ->
                
                def tmp   = tippIdentifiers["${ident.type.v}"]
                
                if(tmp) {
                    if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_VALID.toString())) {
                        tmp[Statistics.COUNT_1]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_INVALID.toString())) {
                        tmp[Statistics.COUNT_2]++
                        tmp[Statistics.LIST_2] << "${ident.value.org}"
                        
                            Statistics.addMetaData(tipp, "tipp.title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC.toString())) {
                        tmp[Statistics.COUNT_3]++
                        tmp[Statistics.LIST_3] << "${ident.value.org}"
                            
                            Statistics.addMetaData(tipp, "tipp.title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_MISSING.toString())) {
                        tmp[Statistics.COUNT_4]++
                        
                            Statistics.addMetaData(tipp, "tipp.title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE.toString())) {
                        tmp[Statistics.COUNT_5]++
                        
                            Statistics.addMetaData(tipp, "tipp.title.identifier(${ident.type.v})", ident.value)
                    }
                }
            }
        }
        tippIdentifiers.each{ i ->
            json.meta.stats.tipps.title.identifiers["${i.key.toUpperCase()} IS VALID"]        = i.value[Statistics.COUNT_1]
            json.meta.stats.tipps.title.identifiers["${i.key.toUpperCase()} IS unkown state"] = i.value[Statistics.COUNT_5]
            
            Statistics.format("${i.key.toUpperCase()} is invalid",    i.value, Statistics.COUNT_2, Statistics.LIST_2, json.meta.stats.tipps.title.identifiers)
            Statistics.format("${i.key.toUpperCase()} is not atomic", i.value, Statistics.COUNT_3, Statistics.LIST_3, json.meta.stats.tipps.title.identifiers)
            Statistics.format("${i.key.toUpperCase()} is missing",    i.value, Statistics.COUNT_4, Statistics.LIST_4, json.meta.stats.tipps.title.identifiers)
            
        }
  
        // TODO invalid coverages

        // tipp.coverage.dates
        
        List<Integer> coverages = Statistics.getStorage()
        List<Integer> covDates  = Statistics.getStorage()
        
        json.package.tipps.each{ tipp ->
            tipp.value.v.each{ tippField ->
                if(tippField.key.equals("coverage")) {
                    tippField.value.m.each{ m ->
                        if(m.equals(Status.STRUCTVALIDATOR_COVERAGE_IS_VALID.toString())){
                            coverages[Statistics.COUNT_1]++
                        }
                        else if(m.equals(Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID.toString())){
                            coverages[Statistics.COUNT_2]++
                        }
                        else if(m.equals(Status.STRUCTVALIDATOR_COVERAGE_IS_UNDEF.toString())){
                            coverages[Statistics.COUNT_3]++
                        }
                    }
                    tippField.value.v.each{ covField ->
                        def sd = covField.startDate
                        def ed = covField.endDate
                       
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                            covDates[Statistics.COUNT_1]++
                        }
                        
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                            covDates[Statistics.COUNT_2]++
                            covDates[Statistics.LIST_2] << "${sd.org}"
                            
                                Statistics.addMetaData(tipp, 'tipp.coverage.startDate', sd)
                        }
                        if(ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                            covDates[Statistics.COUNT_2]++
                            covDates[Statistics.LIST_2] << "${ed.org}"
                            
                                Statistics.addMetaData(tipp, 'tipp.coverage.endDate', ed)
                        }
                        if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                            covDates[Statistics.COUNT_3]++
                            
                                Statistics.addMetaData(tipp, 'tipp.coverage.startDate', sd)
                        }
                        if(ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                            covDates[Statistics.COUNT_3]++
                            
                                Statistics.addMetaData(tipp, 'tipp.coverage.endDate', ed)
                        }
                    }
                }
            }
        }
        
        json.meta.stats.tipps.coverage << ["~ ARE VALID":              coverages[Statistics.COUNT_1]]
        json.meta.stats.tipps.coverage << ["~ are invalid":            coverages[Statistics.COUNT_2]]
        json.meta.stats.tipps.coverage << ["~ are in undefined state": coverages[Statistics.COUNT_3]]
        
        Statistics.format("VALID DATES FOUND",   covDates, Statistics.COUNT_1, Statistics.LIST_1, json.meta.stats.tipps.coverage)
        Statistics.format("invalid dates found", covDates, Statistics.COUNT_2, Statistics.LIST_2, json.meta.stats.tipps.coverage)
        Statistics.format("missing dates found", covDates, Statistics.COUNT_3, Statistics.LIST_3, json.meta.stats.tipps.coverage)*/
        
        stats
    }
    
    static Object processTitles(Object json){

        List<Integer> titleName = Statistics.getStorage()
        
        json.titles.each{ title ->
            def name = title.value.v.name
            
            if(name?.m.equals(Status.VALIDATOR_STRING_IS_VALID.toString())) {
                titleName[Statistics.COUNT_1]++
            }
            else if(name?.m.equals(Status.VALIDATOR_STRING_IS_NOT_ATOMIC.toString())) {
                titleName[Statistics.COUNT_2]++
                titleName[Statistics.LIST_2] << "${name.org}"
                
                    Statistics.addMetaData(title, 'title.name', name)
            }
            else if(name?.m.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
                titleName[Statistics.COUNT_3]++
                titleName[Statistics.LIST_3] << "${name.org}"
                 
                    Statistics.addMetaData(title, 'title.name', name)
            }
            else if(name?.m.equals(Status.VALIDATOR_STRING_IS_MISSING.toString())) {
                titleName[Statistics.COUNT_4]++
                
                    Statistics.addMetaData(title, 'title.name', name)
            }
        }
        
        Statistics.format("NAME IS VALID",      titleName, Statistics.COUNT_1, Statistics.LIST_1, json.meta.stats.titles)
        Statistics.format("name is not atomic", titleName, Statistics.COUNT_2, Statistics.LIST_2, json.meta.stats.titles)
        Statistics.format("name is not valid",  titleName, Statistics.COUNT_3, Statistics.LIST_3, json.meta.stats.titles)
        Statistics.format("name is missing",    titleName, Statistics.COUNT_4, Statistics.LIST_4, json.meta.stats.titles)
        
        // titles.identifiers
        // titles.identifiers
        
        HashMap<String, List<Integer>> titleIdentifiers = [:]
        
        titleIdentifiers[TitleStruct.PISSN]    = Statistics.getStorage()
        titleIdentifiers[TitleStruct.EISSN]    = Statistics.getStorage()
        titleIdentifiers[ZdbBridge.IDENTIFIER] = Statistics.getStorage()
        titleIdentifiers[EzbBridge.IDENTIFIER] = Statistics.getStorage()
        titleIdentifiers[TitleStruct.DOI]      = Statistics.getStorage()
        
        json.titles.each{ title ->
            title.value.v.identifiers.each { ident ->
                def tmp = titleIdentifiers["${ident.type.v}"]
                
                if(tmp) {
                    if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_VALID.toString())) {
                        tmp[Statistics.COUNT_1]++
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_INVALID.toString())) {
                        tmp[Statistics.COUNT_2]++
                        tmp[Statistics.LIST_2] << "${ident.value.org}"
                        
                            Statistics.addMetaData(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC.toString())) {
                        tmp[Statistics.COUNT_3]++
                        tmp[Statistics.LIST_3] << "${ident.value.org}"
                            
                            Statistics.addMetaData(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IS_MISSING.toString())) {
                        tmp[Statistics.COUNT_4]++
                        
                            Statistics.addMetaData(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                    else if(ident.value.m.equals(Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE.toString())) {
                        tmp[Statistics.COUNT_5]++
                        
                            Statistics.addMetaData(title, "title.identifier(${ident.type.v})", ident.value)
                    }
                }
            }
        }
        titleIdentifiers.each{ i ->
            json.meta.stats.titles.identifiers["${i.key.toUpperCase()} IS VALID"]        = i.value[Statistics.COUNT_1]
            json.meta.stats.titles.identifiers["${i.key.toUpperCase()} is unkown state"] = i.value[Statistics.COUNT_5]
            
            Statistics.format("${i.key.toUpperCase()} is invalid",    i.value, Statistics.COUNT_2, Statistics.LIST_2, json.meta.stats.titles.identifiers)
            Statistics.format("${i.key.toUpperCase()} is not atomic", i.value, Statistics.COUNT_3, Statistics.LIST_3, json.meta.stats.titles.identifiers)
            Statistics.format("${i.key.toUpperCase()} is missing",    i.value, Statistics.COUNT_4, Statistics.LIST_4, json.meta.stats.titles.identifiers)
        }
        
        // titles.publisher_history
        // titles.publisher_history
        
        List<Integer> pubStruct   = Statistics.getStorage()
        List<Integer> pubHistName = Statistics.getStorage()
        
        json.titles.each{ title ->
            title.value.v.publisher_history?.each { ph ->
                
                if(ph.m.equals(Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_VALID.toString())){
                    pubStruct[Statistics.COUNT_1]++
                }
                else if(ph.m.equals(Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_INVALID.toString())){
                    pubStruct[Statistics.COUNT_2]++
                }
                else if(ph.m.equals(Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_UNDEF.toString())){
                    pubStruct[Statistics.COUNT_3]++
                }
                
                def phName = ph.v.name

                if(phName?.m.equals(Status.VALIDATOR_STRING_IS_VALID.toString())) {
                    pubHistName[Statistics.COUNT_1]++
                }
                if(phName?.m.equals(Status.VALIDATOR_STRING_IS_INVALID.toString())) {
                    pubHistName[Statistics.COUNT_2]++
                    pubHistName[Statistics.LIST_2] << "${phName.org}"
                     
                        Statistics.addMetaData(title, 'title.publisher_history.name', phName)
                }
                else if(phName?.m.equals(Status.VALIDATOR_STRING_IS_NOT_ATOMIC.toString())) {
                    pubHistName[Statistics.COUNT_3]++
                    pubHistName[Statistics.LIST_3] << "${phName.org}"
                    
                        Statistics.addMetaData(title, 'title.publisher_history.name', phName)
                }
                else if(phName?.m.equals(Status.VALIDATOR_STRING_IS_MISSING.toString())) {
                    pubHistName[Statistics.COUNT_4]++
                     
                        Statistics.addMetaData(title, 'title.publisher_history.name', phName)
                }
                else if(phName?.m.equals(Status.VALIDATOR_PUBLISHER_NOT_MATCHING.toString())) {
                    pubHistName[Statistics.COUNT_5]++
                    pubHistName[Statistics.LIST_5] << "${phName.org}"
                    
                        Statistics.addMetaData(title, 'title.publisher_history.name', phName)
                }
            }
        }

        json.meta.stats.titles.publisher_history << ["~ ARE VALID":              pubStruct[Statistics.COUNT_1]]
        json.meta.stats.titles.publisher_history << ["~ are invalid":            pubStruct[Statistics.COUNT_2]]
        json.meta.stats.titles.publisher_history << ["~ are in undefined state": pubStruct[Statistics.COUNT_3]]
        
        Statistics.format("NAME IS VALID",      pubHistName, Statistics.COUNT_1, Statistics.LIST_1, json.meta.stats.titles.publisher_history)
        Statistics.format("name is not valid",  pubHistName, Statistics.COUNT_2, Statistics.LIST_2, json.meta.stats.titles.publisher_history)
        Statistics.format("name is not atomic", pubHistName, Statistics.COUNT_3, Statistics.LIST_3, json.meta.stats.titles.publisher_history)
        Statistics.format("name is missing",    pubHistName, Statistics.COUNT_4, Statistics.LIST_4, json.meta.stats.titles.publisher_history)
        
        Statistics.format("name is not matching against ONLD.jsonld",
            pubHistName, Statistics.COUNT_5, Statistics.LIST_5, json.meta.stats.titles.publisher_history)

        // TODO invalid history events
        
        // title.historyEvents
        // title.historyEvents
        
        List<Integer> theHistoryEvents = Statistics.getStorage()
        
        json.titles.each{ title ->
            title.value.v.historyEvents.each { he ->
        
                if(he?.m.equals(Status.STRUCTVALIDATOR_HISTORYEVENT_IS_VALID.toString())){
                    theHistoryEvents[Statistics.COUNT_1]++
                }
                else if(he?.m.equals(Status.STRUCTVALIDATOR_HISTORYEVENT_IS_INVALID.toString())){
                    theHistoryEvents[Statistics.COUNT_2]++
                }
                else if(he?.m.equals(Status.STRUCTVALIDATOR_HISTORYEVENT_IS_UNDEF.toString())){
                    theHistoryEvents[Statistics.COUNT_3]++
                }
                
            }
        }
        json.meta.stats.titles.historyEvents << ["~ ARE VALID":              theHistoryEvents[Statistics.COUNT_1]]
        json.meta.stats.titles.historyEvents << ["~ are invalid":            theHistoryEvents[Statistics.COUNT_2]]
        json.meta.stats.titles.historyEvents << ["~ are in undefined state": theHistoryEvents[Statistics.COUNT_3]]

        // title.publisher_history.dates
        // title.publisher_history.dates
        
        List<Integer> pubHistDates = Statistics.getStorage()
        
        json.titles.each{ title ->
            title.value.v.publisher_history.each { ph ->
                                    
                def sd = ph.v.startDate
                def ed = ph.v.endDate

                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString()) || ed?.m.equals(Status.VALIDATOR_DATE_IS_VALID.toString())){
                    pubHistDates[Statistics.COUNT_1]++
                }
                
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                    pubHistDates[Statistics.COUNT_2]++
                    pubHistDates[Statistics.LIST_2] << "${sd.org}"
                    
                        Statistics.addMetaData(title, 'title.publisher_history.startDate', sd)
                }
                if(ed?.m.equals(Status.VALIDATOR_DATE_IS_INVALID.toString())){
                    pubHistDates[Statistics.COUNT_2]++
                    pubHistDates[Statistics.LIST_2] << "${ed.org}"
                    
                        Statistics.addMetaData(title, 'title.publisher_history.endDate', ed)
                }
                if(sd?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                    pubHistDates[Statistics.COUNT_3]++
                    
                        Statistics.addMetaData(title, 'title.publisher_history.startDate', sd)
                }
                if(ed?.m.equals(Status.VALIDATOR_DATE_IS_MISSING.toString())){
                    pubHistDates[Statistics.COUNT_3]++
                    
                        Statistics.addMetaData(title, 'title.publisher_history.endDate', ed)
                }
            }
        }
        
        Statistics.format("VALID DATES FOUND",   pubHistDates, Statistics.COUNT_1, Statistics.LIST_1, json.meta.stats.titles.publisher_history)
        Statistics.format("invalid dates found", pubHistDates, Statistics.COUNT_2, Statistics.LIST_2, json.meta.stats.titles.publisher_history)
        Statistics.format("missing dates found", pubHistDates, Statistics.COUNT_3, Statistics.LIST_3, json.meta.stats.titles.publisher_history)
    
        json
    }
    
    static Object getStatsAfterCleanUp(Object json){
        
        // general
        // see JsonTransformer.removeEmpty(Object json, boolean useValidator)

        def nonEmptyTipps = 0
        def nonEmptyTitles = 0

        json.package.tipps.each{ tipp ->
            if(! (tipp.title.identifiers.size() == 0 || tipp.title.name == "")) {
                nonEmptyTipps++
            }
        }
        json.titles.each{ title ->
            if(! (title.identifiers.size() == 0 || title.name == "")) {
                nonEmptyTitles++
            }
        }

        json.meta.stats.general << ["tipps after cleanUp":  nonEmptyTipps]
        json.meta.stats.general << ["titles after cleanUp": nonEmptyTitles]
        
        json
    }

    static format(String key, List data, int indexCount, int indexResult, ObjectNode target){
        
        if(data[indexCount] > 0 && data[indexResult].minus("").size() > 0) {
            ArrayNode pair = new ArrayNode(NODE_FACTORY).add(data[indexCount]).add(data[indexResult])
            target.set(key, pair)
        }
        else {
            target.put(key, data[indexCount])
        }
    }
        
    /**
     * Adding statistic data into json
     * 
     * @param target
     * @param dom
     * @param obj
     * @return
     */
    static addMetaData(Object target, String dom, Object obj){
        
        obj.dom = dom
        if(!target.value.v._meta.data){
            target.value.v._meta << ['data':[]]
        }
        target.value.v._meta.data.add(obj)
    }
    
    /**
     * List for storing data
     * 
     * @return
     */
    static getStorage(){
        
        List<Integer> storage = [0,[], 0,[], 0,[], 0,[], 0,[], 0,[], 0,[], 0,[], 0,[], 0,[], 0,[], 0,[], 0,[], 0,[]]
        storage
    }
}