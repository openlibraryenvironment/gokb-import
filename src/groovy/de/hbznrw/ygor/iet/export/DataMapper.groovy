package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.iet.Envelope
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.*
import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.tools.DateToolkit
import groovy.util.logging.Log4j
import de.hbznrw.ygor.tools.*


@Log4j
class DataMapper {
    
    /**
     * Creating:
     * 
     * - identifier (simple struct)
     * - title.name
     * - title.publishedFrom
     * - title.publishedTo
     * - publisher_history (complex struct)
     * - history_event (complex struct)
     * 
     * @param title
     * @param query
     * @param env
     */
    
    static void mapEnvelopeToTitle(Title title, Query query, Envelope env) {

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
                
            ident.type.m    = Status.IGNORE
            
            DataSetter.setIdentifier(ident.value, ident.type.v, env.message.join("|"))

            title.identifiers << ident // no pod
        }
        
        else if(query == Query.GBV_TITLE) {
            DataSetter.setString(title.name, env.message)
        }

        else if(query == Query.GBV_PUBLISHED_FROM) {
            DataSetter.setDate(title.publishedFrom, Normalizer.IS_START_DATE, env.message)
        }
        
        else if(query == Query.GBV_PUBLISHED_TO) {
            DataSetter.setDate(title.publishedTo, Normalizer.IS_END_DATE, env.message)
        }
        
        else if(query == Query.GBV_PUBLISHER) {
            def virtPubHistory = null
            def virtEndDate    = null
            
            env.message.each{ e ->
               e.messages['name'].eachWithIndex{ elem, i ->
                   def pubHistory = TitleStruct.getNewPublisherHistory()
                   
                   DataSetter.setString(pubHistory.name, e.messages['name'][i])            
                   DataSetter.setDate  (pubHistory.startDate, Normalizer.IS_START_DATE, e.messages['startDate'][i])
                   DataSetter.setDate  (pubHistory.endDate,   Normalizer.IS_END_DATE,   e.messages['endDate'][i])
                                   
                   if([e.messages['startDate'][i], e.messages['endDate'][i]].contains("anfangs")){
                       virtPubHistory = pubHistory
                   } else {
                       // store lowest start date for dummy calculation
                       if(virtEndDate == null || (pubHistory.startDate.m == Status.VALIDATOR_DATE_IS_VALID && virtEndDate > pubHistory.startDate.v)){
                           virtEndDate = pubHistory.startDate.v
                       }

                       def valid = StructValidator.isValidPublisherHistory(pubHistory)
                       def pod = new Pod(pubHistory, valid)
                       
                       if(Status.STRUCTVALIDATOR_REMOVE_FLAG != valid){    
                           title.publisher_history << pod
                       }
                       else {
                           log.debug("! ignore crappy title publisher history")
                       }
                   }
                }
            }
            
            if(virtPubHistory){
                if(virtEndDate){
                    log.info("adding virtual end date to title.publisher_history: ${virtPubHistory.endDate.v}")
                    
                    DataSetter.setDate(virtPubHistory.startDate, Normalizer.IS_START_DATE, '')
                    DataSetter.setDate(virtPubHistory.endDate,   Normalizer.IS_END_DATE,   DateToolkit.getDateMinusOneMinute(virtEndDate))

                    def valid = StructValidator.isValidPublisherHistory(virtPubHistory)
                    def pod = new Pod(virtPubHistory, valid)
                    
                    if(Status.STRUCTVALIDATOR_REMOVE_FLAG != valid){
                        title.publisher_history << pod
                    }
                    else {
                        log.debug("! ignore crappy title publisher history")
                    }  
                }
            }
        }

        else if(query == Query.GBV_HISTORY_EVENTS) {
            def histEvent =  TitleStruct.getNewHistoryEvent()

            env.message.each{ e ->
                e.messages['title'].eachWithIndex{ elem, i ->
                    
                    def hEvent = TitleStruct.getNewHistoryEventGeneric()
                    DataSetter.setString(hEvent.title, e.messages['title'][i])
                    
                    if("Vorg.".equals(e.messages['type'][i])){
                        histEvent.from << hEvent
                    }
                    else if("Forts.".equals(e.messages['type'][i])){
                        histEvent.to << hEvent
                    }

                    def ident = TitleStruct.getNewIdentifier()
                    
                    ident.type.m  = Status.IGNORE
                    ident.type.v  = e.messages['identifierType'][i].toLowerCase()
                    
                    DataSetter.setIdentifier(ident.value, ident.type.v, e.messages['identifierValue'][i])                 
                    
                    hEvent.identifiers << ident
                }
            }
            
            title.history_events << new Pod(histEvent)
        }
    }
    
    /**
     * Creating:
     * 
     * - identifier (simple struct)
     * - tipp.title.name
     * - tipp.utl
     * - coverage (complex struct)
     * 
     * @param tipp
     * @param query
     * @param env
     * @param dc
     */
    static void mapEnvelopeToTipp(Tipp tipp, Query query, Envelope env, DataContainer dc) {

        if(query in [Query.ZDBID, Query.GBV_EISSN]) {
            def ident = TitleStruct.getNewIdentifier()
            
            if(Query.ZDBID == query)
                ident.type.v = ZdbBridge.IDENTIFIER
            else if(Query.GBV_EISSN == query)
                ident.type.v = TitleStruct.EISSN

            ident.type.m    = Status.IGNORE
            
            DataSetter.setIdentifier(ident.value, ident.type.v, env.message)

            tipp.title.v.identifiers << ident // no pod
        }
        
        else if(query == Query.GBV_TITLE) {
            DataSetter.setString(tipp.title.v.name, env.message)
        }
        
        else if(query == Query.GBV_TIPP_URL) {
            // TODO check if valid url
            DataSetter.setTippURL(tipp.url, dc.pkg.packageHeader.v.nominalPlatform.v, env.message)
        }

        else if(query == Query.GBV_TIPP_COVERAGE) {     
            
            env.message.each{ e ->
                e.messages['coverageNote'].eachWithIndex{ elem, i ->
                    
                    def coverage = PackageStruct.getNewTippCoverage()
                    
                    if(e.messages['startDate'][i]){
                        DataSetter.setDate(coverage.startDate, Normalizer.IS_START_DATE, e.messages['startDate'][i])  
                    }
                    if(e.messages['endDate'][i]){
                        DataSetter.setDate(coverage.endDate,   Normalizer.IS_END_DATE,   e.messages['endDate'][i])
                    }
                    if(e.messages['startVolume'][i]){
                        DataSetter.setCoverageVolume(coverage.startVolume, Normalizer.IS_START_DATE, e.messages['startVolume'][i])
                    }
                    if(e.messages['endVolume'][i]){
                        DataSetter.setCoverageVolume(coverage.endVolume,   Normalizer.IS_END_DATE,   e.messages['endVolume'][i], )
                    } 
                    
                    // TODO
                    DataSetter.setString(coverage.coverageNote, e.messages['coverageNote'][i])
                    //coverage.coverageNote.org = e.messages['coverageNote'][i]
                    //coverage.coverageNote.v   = Normalizer.normString(coverage.coverageNote.org)
                    //coverage.coverageNote.m   = Normalizer.normString(
                    //    (e.states.find{it.toString().startsWith('coverageNote_')}).toString().replaceFirst('coverageNote_', '')
                    //)
                    
                    def valid = StructValidator.isValidCoverage(coverage)
                    def pod = new Pod(coverage, valid)
                    
                    if(Status.STRUCTVALIDATOR_REMOVE_FLAG != valid){
                        tipp.coverage << pod
                    }
                    else {
                        log.debug("! ignore crappy tipp coverage")
                    }
                }
            }
        }
    }
    
    /**
     * Creating:
     * 
     * - identifier (simple struct)
     * - history_event (complex struct)
     * - he.date
     * - he.from
     * - he.to
     * 
     * @param dc
     * @param title
     * @param stash
     */
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
           
            def valid = StructValidator.isValidHistoryEvent(he.v)
            he.m = valid

            if(Status.STRUCTVALIDATOR_REMOVE_FLAG != valid){
                historyEvents << he
            } 
            else {
                log.debug("! ignore crappy title history event")
            }
        }
        
        title.history_events = historyEvents
    }
    
    /**
     * Creating:
     * 
     * - platform.primaryUrl
     * - platform.name
     * 
     * platform.primaryUrl is checked against tipp.url
     * platform.name is checked against platform.primaryUrl
     * 
     * @param tipp
     */
    static void mapPlatform(Tipp tipp) { 
        
        log.info("mapping platform for tipp: " + tipp.title.v.name.v)
        
        def platform = PackageStruct.getNewTippPlatform()
        
        def url = null
        if(tipp.url.m == Status.VALIDATOR_URL_IS_VALID){
            url = 'http://' + Normalizer.getURLAuthority(tipp.url.v)
        }
        platform.primaryUrl.org = tipp.url.v
        platform.primaryUrl.v   = url
        platform.primaryUrl.m   = Validator.isValidURL(url)
        
        DataSetter.setString(platform.name, platform.primaryUrl.v)
                    
        tipp.platform = new Pod(platform)
    }
    
    /**
     * Adding:
     * 
     * - ph.name
     * 
     * @param orgMap
     * @param title
     */
    static void mapOrganisations(HashMap orgMap, Title title) {

        log.info("mapping publisher history organisations for title: " + title.name.v)
        
        // TODO: store state for statistics
        
        title.publisher_history.each { ph ->
            log.debug("checking: " + ph.v.name.v)
            def prefLabelMatch = false
            
            // find prefLabel
            orgMap.any { prefLabel ->
                if(ph.v.name.v?.equalsIgnoreCase(prefLabel.key)) {
                    log.debug("matched prefLabel: " + prefLabel.key)
                    DataSetter.setString(ph.v.name, prefLabel.key)
                    prefLabelMatch = true
                    return true
                }
            }
            // find all altLabels
            if(!prefLabelMatch){
                def prefLabels = []
                orgMap.each { prefLabel, altLabels ->
                    altLabels.each { altLabel ->
                        if(ph.v.name.v?.equalsIgnoreCase(altLabel)) {
                            log.debug("matched altLabel: " + altLabel + " -> set prefLabel: " + prefLabel)
                            prefLabels << prefLabel
                        }
                    }
                }
                DataSetter.setString(ph.v.name, prefLabels)
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
