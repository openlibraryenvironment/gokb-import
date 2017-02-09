package de.hbznrw.ygor.iet.export

import java.util.HashMap
import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.*
import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.tools.DateToolkit
import groovy.util.logging.Log4j
import de.hbznrw.ygor.tools.*


@Log4j
class Mapper {
    
    static void mapToTitle(DataContainer dc, Title title, Query query, Envelope env) {

        if(query in [Query.ZDBID, Query.EZBID, Query.GBV_EISSN, Query.GBV_PISSN, Query.GBV_GVKPPN]) {
            def ident = TitleStruct.getNewIdentifier()
            
            if(Query.ZDBID == query)
                ident.type.v = ZdbBridge.IDENTIFIER
            else if(Query.EZBID == query)
                ident.type.v = EzbBridge.IDENTIFIER
            else if(Query.GBV_EISSN == query)
                ident.type.v = TitleStruct.EISSN
            else if(Query.GBV_PISSN == query)
                ident.type.v = TitleStruct.PISSN
            else if(Query.GBV_GVKPPN == query)
                ident.type.v = "gvk_ppn"
                
            ident.type.m  = Status.IGNORE
            
            //ident.value.org = env.message
            ident.value.v   = Normalizer.normIdentifier  (env.message, ident.type.v)
            ident.value.m   = Validator.isValidIdentifier(ident.value.v, ident.type.v)
            
            // TODO: handle multiple ezbid matches
            
            title.identifiers << ident // no pod
        }
        
        else if(query == Query.GBV_TITLE) {
            title.name.org = env.message
            title.name.v   = Normalizer.normString  (title.name.org)
            title.name.m   = Validator.isValidString(title.name.v)
        }
        
        else if(query == Query.GBV_PUBLISHER) {
            def dummy     = null
            def dummyDate = null
            
            env.message.each{ e ->
               e.messages['name'].eachWithIndex{ elem, i ->
                   def pubHist = TitleStruct.getNewPublisherHistory()
                   
                   pubHist.name.org = e.messages['name'][i]
                   pubHist.name.v   = Normalizer.normString  (pubHist.name.org)
                   pubHist.name.m   = Validator.isValidString(pubHist.name.v)
               
                   pubHist.startDate.org = e.messages['startDate'][i]
                   pubHist.startDate.v   = Normalizer.normDate  (pubHist.startDate.org, Normalizer.IS_START_DATE)
                   pubHist.startDate.m   = Validator.isValidDate(pubHist.startDate.v)
                   
                   pubHist.endDate.org = e.messages['endDate'][i]
                   pubHist.endDate.v   = Normalizer.normDate  (pubHist.endDate.org, Normalizer.IS_END_DATE)
                   pubHist.endDate.m   = Validator.isValidDate(pubHist.endDate.v)
                                   
                   if([e.messages['startDate'][i], e.messages['endDate'][i]].contains("anfangs")){
                       dummy = pubHist
                   } else {
                       // store lowest start date for dummy calculation
                       if(dummyDate == null || (pubHist.startDate.m == Status.VALIDATOR_DATE_IS_VALID && dummyDate > pubHist.startDate.v))
                           dummyDate = pubHist.startDate.v
                           
                       title.publisher_history << pubHist // no pod
                   }
                }
            }
            
            if(dummy){
                if(dummyDate){
                    dummy.endDate.v   = DateToolkit.getDateMinusOneMinute(dummyDate)
                    dummy.endDate.m   = Validator.isValidDate(dummy.endDate.v)
                    dummy.startDate.v = ''
                    dummy.startDate.m = Validator.isValidDate(dummy.startDate.v)
                    
                    log.info("adding virtual end date to title.publisher_history: ${dummy.endDate.v}")
                    
                    title.publisher_history << dummy // no pod
                }
            }
        }
        
        else if(query == Query.GBV_PUBLISHED_FROM) {
            title.publishedFrom.org = env.message
            title.publishedFrom.v   = Normalizer.normDate  (title.publishedFrom.org, Normalizer.IS_START_DATE)
            title.publishedFrom.m   = Validator.isValidDate(title.publishedFrom.v)
        }
        
        else if(query == Query.GBV_PUBLISHED_TO) {
            title.publishedTo.org = env.message
            title.publishedTo.v   = Normalizer.normDate  (title.publishedTo.org, Normalizer.IS_END_DATE)
            title.publishedTo.m   = Validator.isValidDate(title.publishedTo.v)
        }
        
        else if(query == Query.GBV_HISTORY_EVENTS) {
            def histEvent =  TitleStruct.getNewHistoryEvent()

            env.message.each{ e ->
                e.messages['title'].eachWithIndex{ elem, i ->
                    
                    def hex = TitleStruct.getNewHistoryEventGeneric()
                    hex.title.org = e.messages['title'][i]
                    hex.title.v   = Normalizer.normString  (hex.title.org)
                    hex.title.m   = Validator.isValidString(hex.title.v)
                    
                    if("Vorg.".equals(e.messages['type'][i])){
                        histEvent.from << hex
                    }
                    else if("Forts.".equals(e.messages['type'][i])){
                        histEvent.to << hex
                    }

                    def ident = TitleStruct.getNewIdentifier()
                    
                    ident.type.m  = Status.IGNORE
                    ident.type.v  = e.messages['identifierType'][i].toLowerCase()
                    ident.value.v = Normalizer.normIdentifier (e.messages['identifierValue'][i], ident.type.v)
                    ident.value.m = Validator.isValidIdentifier(ident.value.v, ident.type.v)                   
                    
                    hex.identifiers << ident
                }
            }
            
            title.history_events << new Pod(histEvent)
        }
    }
    
    static void mapToTipp(DataContainer dc, Tipp tipp, Query query, Envelope env) {

        if(query in [Query.ZDBID, Query.GBV_EISSN]) {
            def ident = TitleStruct.getNewIdentifier()
            
            if(Query.ZDBID == query)
                ident.type.v = ZdbBridge.IDENTIFIER
            else if(Query.GBV_EISSN == query)
                ident.type.v = TitleStruct.EISSN

            ident.type.m  = Status.IGNORE
            ident.value.v = Normalizer.normIdentifier  (env.message, ident.type.v)
            ident.value.m = Validator.isValidIdentifier(ident.value.v, ident.type.v)

            tipp.title.v.identifiers << ident // no pod
        }
        
        else if(query == Query.GBV_TITLE) {
            tipp.title.v.name.org = env.message
            tipp.title.v.name.v   = Normalizer.normString  (tipp.title.v.name.org)
            tipp.title.v.name.m   = Validator.isValidString(tipp.title.v.name.v)
        }
        
        else if(query == Query.GBV_TIPP_URL) {
            tipp.url.org = env.message
            tipp.url.v   = Normalizer.normTippURL(tipp.url.org, dc.pkg.packageHeader.v.nominalPlatform.v)
            tipp.url.m   = Validator.isValidURL  (tipp.url.v)
        }

        else if(query == Query.GBV_TIPP_COVERAGE) {     
            
            env.message.each{ e ->
                e.messages['coverageNote'].eachWithIndex{ elem, i ->
                    
                    def coverage = PackageStruct.getNewTippCoverage()
                    // TODO
                    coverage.coverageNote.org = e.messages['coverageNote'][i]
                    coverage.coverageNote.v   = Normalizer.normString(coverage.coverageNote.org)
                    coverage.coverageNote.m   = Normalizer.normString(
                        (e.states.find{it.toString().startsWith('coverageNote_')}).toString().replaceFirst('coverageNote_', '')
                    )
                    
                    if(e.messages['startDate'][i]){
                        coverage.startDate.org = e.messages['startDate'][i]
                        coverage.startDate.v   = Normalizer.normDate  (coverage.startDate.org, Normalizer.IS_START_DATE)
                        coverage.startDate.m   = Validator.isValidDate(coverage.startDate.v)   
                    }
                    if(e.messages['endDate'][i]){
                        coverage.endDate.org = e.messages['endDate'][i]
                        coverage.endDate.v   = Normalizer.normDate  (coverage.endDate.org, Normalizer.IS_END_DATE)
                        coverage.endDate.m   = Validator.isValidDate(coverage.endDate.v)
                    }
                    if(e.messages['startVolume'][i]){
                        coverage.startVolume.org = e.messages['startVolume'][i]
                        coverage.startVolume.v   = Normalizer.normCoverageVolume(coverage.startVolume.org, Normalizer.IS_START_DATE)
                        coverage.startVolume.m   = Validator.isValidNumber      (coverage.startVolume.v)
                    }
                    if(e.messages['endVolume'][i]){
                        coverage.endVolume.org = e.messages['endVolume'][i]
                        coverage.endVolume.v   = Normalizer.normCoverageVolume(coverage.endVolume.org, Normalizer.IS_END_DATE)
                        coverage.endVolume.m   = Validator.isValidNumber      (coverage.endVolume.v)
                    } 
                    
                    def valid = Validator.isValidCoverage(coverage.startDate, coverage.endDate, coverage.startVolume, coverage.endVolume) ? Status.VALIDATOR_COVERAGE_IS_VALID : Status.VALIDATOR_COVERAGE_IS_INVALID
                    
                    if(Status.VALIDATOR_COVERAGE_IS_INVALID == valid && coverage.startDate.v == coverage.endDate.v && coverage.startVolume.v == coverage.endVolume.v) {
                        // prefilter to reduce crappy results
                        log.debug("! ignore crappy tipp coverage")
                    }
                    else {
                        tipp.coverage << new Pod(coverage, valid)
                    }
                }
            }
        }
    }
      
    static void mapHistoryEvents(DataContainer dc, Title title, Object stash) {
        
        log.info("mapping history events for title: " + title.name.v)

        // todo: handle multiple history events
        def historyEvents = []
        
        title.history_events.each{ he ->
            
            def hex = TitleStruct.getNewHistoryEventGeneric()
            hex.title.v = title.name.v
            hex.title.m = title.name.m

            title.identifiers.each{ ident ->
                if([ZdbBridge.IDENTIFIER, TitleStruct.EISSN].contains(ident.type.v))
                    hex.identifiers << ident
            }
            
            // set identifiers
            // set missing eissn
            // set missing title
            // set date
            if(he.v.from.size() > 0){
                he.v.to << hex
                he.v.from.each { from ->
                    def identifiers = []
                    from.identifiers.each{ ident ->
                        identifiers << ident
                        if(ident.type.v == ZdbBridge.IDENTIFIER){
                            def target = stash[ZdbBridge.IDENTIFIER].get("${ident.value.v}")
                            target = dc.titles.get("${target}")
    
                            if(target){
                                target.v.identifiers.each{ targetIdent ->
                                    if(targetIdent.type.v == TitleStruct.EISSN){
                                        identifiers << targetIdent
                                    }
                                }
                                from.title.v = target.v.name.v
                                from.title.m = target.v.name.m 
                            }
                        }
                    }
                    from.identifiers = identifiers
                }
                he.v.date.v = title.publishedFrom.v
                he.v.date.m = title.publishedFrom.m
            }
            
            // set identifiers
            // set missing eissn
            // set date
            else if(he.v.to.size() > 0){
                he.v.from << hex
                he.v.to.each { to ->
                    def identifiers = []
                    to.identifiers.each{ ident ->
                        identifiers << ident
                        if(ident.type.v == ZdbBridge.IDENTIFIER){
                            def target = stash[ZdbBridge.IDENTIFIER].get("${ident.value.v}")
                            target = dc.titles.get("${target}")
    
                            if(target){
                                target.v.identifiers.each{ targetIdent ->
                                    if(targetIdent.type.v == TitleStruct.EISSN){
                                        identifiers << targetIdent
                                    }
                                }
                                he.v.date.v = target.v.publishedFrom.v
                                he.v.date.m = target.v.publishedFrom.m
                            }
                        }
                    }
                    to.identifiers = identifiers
                }
            }
           
            def valid = Validator.isValidHistoryEvent(he)
            
            if(Status.VALIDATOR_HISTORYEVENT_IS_INVALID == valid && he.v.date.m == Status.UNDEFINED && he.v.from.size() == 0 && he.v.to.size() == 0) {
                // prefilter to reduce crappy results
                log.debug("! ignore crappy title history event")
            } 
            else {
                he.m = valid
                historyEvents << he
            }
        }
        
        title.history_events = historyEvents
    }
    
    static void mapPlatform(Tipp tipp) { 
        
        log.info("mapping platform for tipp: " + tipp.title.v.name.v)
        
        if(tipp.url.m == Status.VALIDATOR_URL_IS_VALID){
            def platform = PackageStruct.getNewTippPlatform()

            platform.primaryUrl.org = tipp.url.v
            platform.primaryUrl.v   = Normalizer.normURL  (tipp.url.v)
            platform.primaryUrl.m   = Validator.isValidURL(platform.primaryUrl.v)
            
            platform.name.org = platform.primaryUrl.v
            platform.name.v   = Normalizer.normString  (platform.primaryUrl.v)
            platform.name.m   = Validator.isValidString(platform.name.v)
            
            tipp.platform = new Pod(platform)
        }
    }
    
    static void mapOrganisations(HashMap orgMap, Title title) {

        log.info("mapping publisher history organisations for title: " + title.name.v)
        
        // TODO: store state for statistics
        
        title.publisher_history.each { ph ->
            log.debug("checking: " + ph.name.v)
            def prefLabelMatch = false
            
            // find prefLabel
            orgMap.any { prefLabel ->
                if(ph.name.v.equalsIgnoreCase(prefLabel.key)) {
                    log.debug("matched prefLabel: " + prefLabel.key)
                    ph.name.v = Normalizer.normString(prefLabel.key)
                    ph.name.m = Validator.isValidString(ph.name.v)
                    prefLabelMatch = true
                    return true
                }
            }
            // find all altLabels
            if(!prefLabelMatch){
                def prefLabels = []
                orgMap.each { prefLabel, altLabels ->
                    altLabels.each { altLabel ->
                        if(ph.name.v.equalsIgnoreCase(altLabel)) {
                            log.debug("matched altLabel: " + altLabel + " -> set prefLabel: " + prefLabel)
                            prefLabels << prefLabel
                        }
                    }
                }
                ph.name.v = Normalizer.normString(prefLabels)
                ph.name.m = Validator.isValidString(ph.name.v)
            }
        }
    }
  
    static HashMap getOrganisationMap() {
        
        def resource = FileToolkit.getResourceByClassPath('/de/hbznrw/ygor/resources/ONLD.jsonld')
        def orgJson  = JsonToolkit.parseFileToJson(resource.file.path)
        def orgMap   = [:]

        orgJson.'@graph'.each { e ->
            orgMap.put(e.'skos:prefLabel', e.'skos:altLabel')
        }
        orgMap
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
