package de.hbznrw.ygor.export

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.node.ObjectNode
import de.hbznrw.ygor.enums.*
import grails.converters.JSON
import groovy.util.logging.Log4j

//import org.apache.commons.logging.Log

import ygor.Enrichment.FileType
import ygor.Record

@Log4j
class JsonTransformer {

    static final USE_VALIDATOR = true
    static final NO_VALIDATOR  = false
    
    static final USE_PRETTY_PRINT = true
    static final NO_PRETTY_PRINT  = false

    static ObjectMapper MAPPER = new ObjectMapper()
    static{
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
    }


    static String getSimpleJSON(Object json, FileType type, boolean prettyPrint) {
     
        log.info("getSimpleJSON()")
        
        def validator = JsonTransformer.USE_VALIDATOR
        
        JsonTransformer.parsePackageHeader(json)
        JsonTransformer.parseHashMaps(json)

        //Transformer.parseAdditionalProperties(json)
        
        JsonTransformer.parseNominalPlatform(json)
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
        json.meta.stats = []

        if(type.equals(FileType.JSON_PACKAGE_ONLY)){
            
            json.pkg.tipps.eachWithIndex{ tipp, i ->
                json.pkg.tipps[i].remove("_meta")
            }
            json = json.pkg
        }
        else if(type.equals(FileType.JSON_TITLES_ONLY)){
            
            json.titles.eachWithIndex{ title, i ->
                json.titles[i].remove("_meta")
            }
            json = json.titles
        }
        
        if(prettyPrint){
            return new JSON(json).toString(true)
        }
        else {
            return new JSON(json).toString()
        }
    }
    
    static Object parsePackageHeader(Object json) {
        log.debug("parsePackageHeader()")
        
        json.pkg.packageHeader = json.pkg.packageHeader.v
        json.pkg.packageHeader.additionalProperties = json.pkg.packageHeader.additionalProperties.v

        json.pkg.packageHeader.each{ ph ->
            if(ph.value.v instanceof java.lang.String) {
                json.pkg.packageHeader."${ph.key}" = (ph.value.v ? ph.value.v : "")
            }
        }

        if(json.pkg.packageHeader.name) {
            json.pkg.packageHeader.name = json.pkg.packageHeader.name.v.v
        }
        
        json  
    }
    
    static Object parseHashMaps(Object json) {
         log.debug("parseHashMaps()")
        
         def tipps = []
         json.pkg.tipps.each{ tipp ->
             tipps << tipp.value.v
         }
         json.pkg.tipps = tipps
         
         def titles = []
         json.titles = json.titles.each{ title ->
             titles << title.value.v
         }
         json.titles = titles
         
         json
    }
    static Object parseNominalPlatform(Object json) {
        log.debug("parseNominalPlatform()")

        def plt = [
          'name': json.pkg.packageHeader.nominalPlatform.name,
          'primaryUrl': json.pkg.packageHeader.nominalPlatform.primaryUrl
        ]

        json.pkg.packageHeader.nominalPlatform = plt
    }
     
    static Object parseCuratoryGroups(Object json) {
        log.debug("parseCuratoryGroups()")
        
        json.pkg.packageHeader.curatoryGroups.eachWithIndex{ cg, i ->
            json.pkg.packageHeader.curatoryGroups[i] = cg.v
        }
        
        json
    }
    
    static Object parseSource(Object json) {
        log.debug("parseSource()")
        
        json.pkg.packageHeader.source = json.pkg.packageHeader.source.v
        
        def source = [:]
        json.pkg.packageHeader.source.eachWithIndex{ src, i ->
            if(src.value.v instanceof java.lang.String && src.value.v.trim().size() > 0) {
                source << ["${src.key}" : src.value.v]
            }
        }
        json.pkg.packageHeader.source = source
        
        json
    }
    
    static Object parseVariantNames(Object json) {
        log.debug("parseVariantNames()")
        
        json.pkg.packageHeader.variantNames.eachWithIndex{ vn, i ->
            json.pkg.packageHeader.variantNames[i] = vn.v
        }
        
        json
    }
    
    static Object parseTipps(Object json, boolean useValidator) {
        log.debug("parseTipps()")
        
        json.pkg.tipps.each{ tipp ->
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
        
        json.pkg.tipps.each{ tipp ->
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
        
        json.pkg.tipps.eachWithIndex{ tipp, i ->
            def platform = [:]
            tipp.platform.each{ pf ->
                if(pf.value.v instanceof java.lang.String) {
                    platform << ["${pf.key}" : pf.value.v]
                }
            }
            json.pkg.tipps[i].platform = platform
        }      
        
        json
    }
   
    static Object parseTippCoverage(Object json, boolean useValidator) {
        log.debug("parseTippCoverage()")
        
        json.pkg.tipps.each{ tipp ->
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
                                    if(attr.value.m != Status.VALIDATOR_STRING_IS_VALID.toString())
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
        
         json.pkg.tipps.each{ tipp ->
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
//                         if(["startDate", "endDate"].contains(attr.key.toString()) && attr.value.toString() != ""){
//                             publisher_history << ph
//                             return true
//                         }

                        if(attr.key.toString() == "name" && attr.value.toString() != "") {
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
            title.historyEvents.eachWithIndex { he, i ->

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
            
            def theHistoryEvents = []
            title.historyEvents.each{ he ->

                // only valid entries
                if(useValidator){
                    //if(he.m == Status.STRUCTVALIDATOR_HISTORYEVENT_IS_VALID.toString())
                    // TODO remove if struct validator is implemented
                    // workaround
                    if(he.m != Status.STRUCTVALIDATOR_HISTORYEVENT_IS_INVALID.toString())
                        theHistoryEvents << he.v
                }
                else {
                    theHistoryEvents << he.v
                }
            }
            title.historyEvents = theHistoryEvents
        }
        json
    }
    
    static Object removeEmpty(Object json, boolean useValidator){
        log.debug("removeEmpty()")
   
        // remove tipps without name and identifier
        if(useValidator){
            
            def tipps = []
            json.pkg.tipps.each{ tipp ->
                if(tipp.title.identifiers.size() == 0 || tipp.title.name == "") {
                    log.info("removeEmpty(): tipp removed from json")
                }
                else {
                    tipps << tipp
                }
            }
            json.pkg.tipps = tipps
        }
            
        // remove titles without name and identifier
        if(useValidator){
            
            def titles = []
            json.titles.each{ title ->
                if(title.identifiers.size() == 0 || title.name == "") {
                    log.info("removeEmpty(): title removed from json")
                }
                else {
                    titles << title
                }
            }
            json.titles = titles
        }
        
        json
    }
}
