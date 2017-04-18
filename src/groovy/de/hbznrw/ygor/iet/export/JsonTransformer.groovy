package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.tools.*
import de.hbznrw.ygor.iet.enums.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j

//import org.apache.commons.logging.Log

import ygor.Enrichment.FileType

@Log4j
class JsonTransformer {
    
    static final USE_VALIDATOR = true
    static final NO_VALIDATOR  = false
    
    static final USE_PRETTY_PRINT = true
    static final NO_PRETTY_PRINT  = false

    static String getSimpleJSON(DataContainer dc, FileType type, boolean prettyPrint) {
        
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parseText(JsonToolkit.parseDataToJson(dc))
        
        JsonTransformer.getSimpleJSON(json, type, prettyPrint)
    }
    
    static String getSimpleJSON(Object json, FileType type, boolean prettyPrint) {
     
        log.info("getSimpleJSON()")
        
        def validator = JsonTransformer.USE_VALIDATOR
        
        if(type.equals(FileType.JSON_DEBUG)){
            
            validator = JsonTransformer.NO_VALIDATOR
            Statistics.getStatsBeforeParsing(json)
        }

        JsonTransformer.parsePackageHeader(json)
        JsonTransformer.parseHashMaps(json)

        //Transformer.parseAdditionalProperties(json)
        
        JsonTransformer.parseCuratoryGroups(json)
        JsonTransformer.parseSource(json)
        JsonTransformer.parseVariantNames(json)
        
        JsonTransformer.parseTipps(json, validator)
        JsonTransformer.parseTippTitleIdentifiers(json, validator)
        JsonTransformer.parseTippCoverage(json, validator)

        JsonTransformer.parseTitles(json, validator)
        JsonTransformer.parseTitleIdentifiers(json, validator)
        JsonTransformer.parsePublisherHistory(json, validator)
        JsonTransformer.parseHistoryEvents(json, validator)

        JsonTransformer.removeEmpty(json, validator)
        
        if(type.equals(FileType.JSON_DEBUG)){
            Statistics.getStatsAfterCleanUp(json)
        }
        else {
            json.meta.stats = []
        }
        
        if(type.equals(FileType.JSON_PACKAGE_ONLY)){
            
            json.package.tipps.eachWithIndex{ tipp, i ->
                json.package.tipps[i].remove("_meta")
            }
            json = json.package
        }
        else if(type.equals(FileType.JSON_TITLES_ONLY)){
            
            json.titles.eachWithIndex{ title, i ->
                json.titles[i].remove("_meta")
            }
            json = json.titles
        }
        
        if(prettyPrint){
            return new JsonBuilder(json).toPrettyString()
        }
        else {
            return new JsonBuilder(json).toString()
        }
    }
    
    static Object parsePackageHeader(Object json) {
        log.debug("parsePackageHeader()")
        
        json.package.packageHeader = json.package.packageHeader.v
        json.package.packageHeader.additionalProperties = json.package.packageHeader.additionalProperties.v

        json.package.packageHeader.each{ ph ->
            if(ph.value.v instanceof java.lang.String) {
                json.package.packageHeader."${ph.key}" = (ph.value.v ? ph.value.v : "")
            }
        }

        if(json.package.packageHeader.name) {
            json.package.packageHeader.name = json.package.packageHeader.name.v.v
        }
        
        json  
    }
    
    static Object parseHashMaps(Object json) {
         log.debug("parseHashMaps()")
        
         def tipps = []
         json.package.tipps.each{ tipp ->
             tipps << tipp.value.v
         }
         json.package.tipps = tipps
         
         def titles = []
         json.titles = json.titles.each{ title ->
             titles << title.value.v
         }
         json.titles = titles
         
         json
    }
     
    static Object parseCuratoryGroups(Object json) {
        log.debug("parseCuratoryGroups()")
        
        json.package.packageHeader.curatoryGroups.eachWithIndex{ cg, i ->
            json.package.packageHeader.curatoryGroups[i] = cg.v
        }
        
        json
    }
    
    static Object parseSource(Object json) {
        log.debug("parseSource()")
        
        json.package.packageHeader.source = json.package.packageHeader.source.v
        
        def source = [:]
        json.package.packageHeader.source.eachWithIndex{ src, i ->
            if(src.value.v instanceof java.lang.String) {
                source << ["${src.key}" : src.value.v]
            }
        }
        json.package.packageHeader.source = source
        
        json
    }
    
    static Object parseVariantNames(Object json) {
        log.debug("parseVariantNames()")
        
        json.package.packageHeader.variantNames.eachWithIndex{ vn, i ->
            json.package.packageHeader.variantNames[i] = vn.v
        }
        
        json
    }
    
    static Object parseTipps(Object json, boolean useValidator) {
        log.debug("parseTipps()")
        
        json.package.tipps.each{ tipp ->
            tipp.each{ attr ->
                // ignore _meta
                if(attr.key != '_meta'){ 
                    if(attr.value.v instanceof java.lang.String || attr.value.v == null) {
                        def value = (attr.value.v == null) ? "" : attr.value.v
    
                        if(attr.key == 'url'){
                            // use validator
                            if(useValidator){
                                if(attr.value.m != Status.VALIDATOR_URL_IS_VALID.toString())
                                    value = ""
                            }
                        }
                        tipp."${attr.key}" = value
                    }
                }
            }
        }
        
        json.package.tipps.each{ tipp ->
            tipp.platform   = tipp.platform.v
            tipp.title      = tipp.title.v           
            tipp.title.type = tipp.title.type.v
            
            def value       = tipp.title.name.v
            // use validator
            if(useValidator){
                if(tipp.title.name.m != Status.VALIDATOR_STRING_IS_VALID.toString())
                    value = ""
            }
            tipp.title.name = value
        }
        
        json.package.tipps.eachWithIndex{ tipp, i ->
            def platform = [:]
            tipp.platform.each{ pf ->
                if(pf.value.v instanceof java.lang.String) {
                    platform << ["${pf.key}" : pf.value.v]
                }
            }
            json.package.tipps[i].platform = platform
        }      
        
        json
    }
   
    static Object parseTippCoverage(Object json, boolean useValidator) {
        log.debug("parseTippCoverage()")
        
        json.package.tipps.each{ tipp ->
            tipp.coverage.eachWithIndex{ cover, i ->
                def coverage = [:]
                
                // use validator
                //if(!useValidator || (useValidator && cover.m == Status.STRUCTVALIDATOR_COVERAGE_IS_VALID.toString())){
                // TODO remove if struct validator is implemented
                // workaround
                if(!useValidator || (useValidator && cover.m != Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID.toString())){
                    cover.v.each{ attr ->
                        if(attr.value.v instanceof java.lang.String) {
                            def value = attr.value.v
                            
                            if(['startDate', 'endDate'].contains(attr.key)){
                                // use validator
                                if(useValidator){
                                    if(attr.value.m != Status.VALIDATOR_DATE_IS_VALID.toString())
                                        value = ""
                                }
                            }
                            else if(['startVolume', 'endVolume'].contains(attr.key)){
                                // use validator
                                if(useValidator){
                                    if(attr.value.m != Status.VALIDATOR_NUMBER_IS_VALID.toString())
                                        value = ""
                                }
                            }
                            coverage << ["${attr.key}" : value]
                        }
                    }
                    tipp.coverage[i] = coverage
                }
                else {
                    tipp.coverage[i] = null
                }
            }
            tipp.coverage = tipp.coverage.minus(null)
        }
        
        json
    }
    
    static Object parseTippTitleIdentifiers(Object json, boolean useValidator) {
         log.debug("parseTippTitleIdentifiers()")
        
         json.package.tipps.each{ tipp ->
             def validIdentifiers = []
             tipp.title.identifiers.each{ ident ->
                 
                 // use validator
                 if(useValidator){
                     if(ident.value.m == Status.VALIDATOR_IDENTIFIER_IS_VALID.toString()){
                         ident.type  = ident.type.v
                         ident.value = ident.value.v
                         validIdentifiers << ident
                     }
                 }
                 else {
                     ident.type  = ident.type.v
                     ident.value = ident.value.v
                     validIdentifiers << ident
                 }
             }
             tipp.title.identifiers = validIdentifiers
         }

         json
     }
        
    static Object parseTitles(Object json, boolean useValidator) {
        log.debug("parseTitles()")
        
        json.titles.each{ title ->
            title.each{ attr ->
                // ignore _meta
                if(attr.key != '_meta'){ 
                    if(attr.value.v instanceof java.lang.String) {
                    
                        if(attr.key == "name") {
                            def value = attr.value.v
                            // use validator
                            if(useValidator)
                                if(attr.value.m != Status.VALIDATOR_STRING_IS_VALID.toString())
                                    value = ""
                                    
                            title."${attr.key}" = value
                        }
                        else {
                            title."${attr.key}" = (attr.value.v ? attr.value.v : "")
                        }
                    }
                }
            }
        }
        
        json
    }
  
    static Object parseTitleIdentifiers(Object json, boolean useValidator) {
         log.debug("parseTitleIdentifiers()")
        
         json.titles.each{ title ->
             def validIdentifiers = []
             title.identifiers.each{ ident ->

                 // use validator
                 if(useValidator){
                     if(ident.value.m == Status.VALIDATOR_IDENTIFIER_IS_VALID.toString()){
                         ident.type  = ident.type.v
                         ident.value = ident.value.v
                         validIdentifiers << ident
                     }
                 }
                 else {
                     ident.type  = ident.type.v
                     ident.value = ident.value.v
                     validIdentifiers << ident
                 }
             }
             title.identifiers = validIdentifiers
         }
         
         json
    }
         
    static Object parsePublisherHistory(Object json, boolean useValidator) {
        log.debug("parsePublisherHistory()")
        
        json.titles.each{ title ->
            title.publisher_history.eachWithIndex { ph, i ->
                def publisher_history = [:]
                ph.v.each{ attr -> 
                    def value = attr.value.v

                    if(['startDate', 'endDate'].contains(attr.key)){
                        // use validator
                        if(useValidator){
                            if(attr.value.m != Status.VALIDATOR_DATE_IS_VALID.toString())
                                value = ""
                        }
                    }
                    publisher_history << ["${attr.key}" : value]
                }
                title.publisher_history[i] = publisher_history
            }
        }
        
        json.titles.each{ title ->
            def publisher_history = []
            title.publisher_history.each{ ph ->
                
                // only valid entries
                // TODO remove if struct validator is implemented
                // workaround
                if(useValidator && ph.m != Status.STRUCTVALIDATOR_PUBLISHERHISTORY_IS_INVALID.toString()){
                    ph.any { attr ->
                        if(["startDate", "endDate"].contains(attr.key.toString()) && attr.value.toString() != ""){
                            publisher_history << ph
                            return true
                        } 
                    }
                    title.publisher_history = publisher_history
                }
            }
        }

        json
    }
    
    static Object parseHistoryEvents(Object json, boolean useValidator) {
        log.debug("parseHistoryEvents()")
        
        json.titles.each{ title ->
            title.history_events.eachWithIndex { he, i ->

                he.v.date = he.v.date.v
                
                he.v.from.each{ from ->
                    def validIdentifiers = []
                    from.identifiers.each{ ident ->
                        
                        // use validator
                        if(useValidator){
                            if(ident.value.m == Status.VALIDATOR_IDENTIFIER_IS_VALID.toString()){
                                ident.type  = ident.type.v
                                ident.value = ident.value.v
                                validIdentifiers << ident
                            }
                        }
                        else {
                            ident.type  = ident.type.v
                            ident.value = ident.value.v
                            validIdentifiers << ident
                        }
                    }
                    from.identifiers = validIdentifiers
                    
                    // only valid entries
                    if(useValidator){
                        if(from.title.m == Status.VALIDATOR_STRING_IS_VALID.toString()){
                            from.title = from.title.v
                        }
                        else {
                            from.title = ""
                        }
                    }
                    else {
                        from.title = from.title.v
                    }
                }
                he.v.to.each{ to ->
                    def validIdentifiers = []
                    to.identifiers.each{ ident ->
                        
                        // use validator
                        if(useValidator){
                            if(ident.value.m == Status.VALIDATOR_IDENTIFIER_IS_VALID.toString()){
                                ident.type  = ident.type.v
                                ident.value = ident.value.v
                                validIdentifiers << ident
                            }
                        }
                        else {
                            ident.type  = ident.type.v
                            ident.value = ident.value.v
                            validIdentifiers << ident
                        }
                    }
                    to.identifiers = validIdentifiers
                    
                    // only valid entries
                    if(useValidator){
                        if(to.title.m == Status.VALIDATOR_STRING_IS_VALID.toString()){
                            to.title = to.title.v
                        }
                        else {
                            to.title = ""
                        }
                    }
                    else {
                        to.title = to.title.v
                    }
                }
            }
            
            // TODO refactoring: use Status.VALIDATOR_HISTORYEVENT_IS_VALID
            
            def historyEvents = []
            title.history_events.each{ he ->

                // only valid entries
                if(useValidator){
                    //if(he.m == Status.STRUCTVALIDATOR_HISTORYEVENT_IS_VALID.toString())
                    // TODO remove if struct validator is implemented
                    // workaround
                    if(he.m != Status.STRUCTVALIDATOR_HISTORYEVENT_IS_INVALID.toString())
                        historyEvents << he.v
                }
                else {
                    historyEvents << he.v
                }
            }
            title.history_events = historyEvents
        }
        json
    }
    
    static Object removeEmpty(Object json, boolean useValidator){
        log.debug("removeEmpty()")
   
        // remove tipps without name and identifier
        if(useValidator){
            
            def tipps = []
            json.package.tipps.each{ tipp ->
                if(tipp.title.identifiers.size() > 0) {
                    tipps << tipp
                }
            }
            json.package.tipps = tipps
        }
            
        // remove titles without name and identifier
        if(useValidator){
            
            def titles = []
            json.titles.each{ title ->
                if(title.identifiers.size() > 0) {
                    titles << title
                }
            }
            json.titles = titles
        }
        
        json
    }
}