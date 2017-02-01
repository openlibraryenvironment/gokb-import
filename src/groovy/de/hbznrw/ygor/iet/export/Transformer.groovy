package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.tools.*
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.export.structure.Title
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import ygor.Enrichment.FileType

class Transformer {
    
    static final USE_VALIDATOR = true
    static final NO_VALIDATOR  = false

    static String getSimpleJSON(DataContainer dc, FileType type) {
     
        def validator   = Transformer.USE_VALIDATOR
        def jsonSlurper = new JsonSlurper()
        def json        = jsonSlurper.parseText(JsonToolkit.parseDataToJson(dc))
        
        if(type.equals(FileType.JSON_DEBUG)){
            validator = Transformer.NO_VALIDATOR
            json = Statistics.getStatsBeforeParsing(json)
        }
        
        json = Transformer.parsePackageHeader(json)
        json = Transformer.parseHashMaps(json)

        //json = Transformer.parseAdditionalProperties(json)
        json = Transformer.parseCuratoryGroups(json)
        json = Transformer.parseSource(json)
        json = Transformer.parseVariantNames(json)
        
        json = Transformer.parseTipps(json, validator)
        json = Transformer.parseTippTitleIdentifiers(json, validator)
        json = Transformer.parseTippCoverage(json, validator)

        json = Transformer.parseTitles(json, validator)
        json = Transformer.parseTitleIdentifiers(json, validator)
        json = Transformer.parsePublisherHistory(json, validator)
        json = Transformer.parseHistoryEvents(json, validator)
        
        json = Transformer.cleanUpJSON(json, validator)
        
        if(type.equals(FileType.JSON_DEBUG)){
            json = Statistics.getStatsAfterCleanUp(json)
        }
        
        if(type.equals(FileType.JSON_PACKAGE)){
            json = json.package
        }
        else if(type.equals(FileType.JSON_TITLES)){
            json = json.titles
        }
        
        new JsonBuilder(json).toPrettyString() 
    }
    
    static Object parsePackageHeader(Object json) {
        
        println ". DataTransformer.parsePackageHeader()"
        
        json.package.packageHeader = json.package.packageHeader.v
        json.package.packageHeader.additionalProperties = json.package.packageHeader.additionalProperties.v

        json.package.packageHeader.each{ ph ->
            if(ph.value.v instanceof java.lang.String) {
                json.package.packageHeader."${ph.key}" = (ph.value.v ? ph.value.v : "")
            }
        }

        if(json.package.packageHeader.name) {
            json.package.packageHeader.name =  json.package.packageHeader.name.v.v
        }
        
        json  
    }
    
    static Object parseHashMaps(Object json) {
        
         println ". DataTransformer.parseHashMaps()"

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
        
        println ". DataTransformer.parseCuratoryGroups()"
        
        json.package.packageHeader.curatoryGroups.eachWithIndex{ cg, i ->
            
            def curatoryGroup = [:]
            cg.each{ attr ->
                curatoryGroup << ["${attr.key}" : attr.value.v]
            }
            json.package.packageHeader.curatoryGroups[i] = curatoryGroup
        }
        
        json
    }
    
    static Object parseSource(Object json) {
        
        println ". DataTransformer.parseSource()"
        
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
        
        println ". DataTransformer.parseVariantNames()"
        
        json.package.packageHeader.variantNames.eachWithIndex{ vn, i ->
            
            def variantName = [:]
            vn.each{ attr ->
                variantName << ["${attr.key}" : attr.value.v]
            }
            json.package.packageHeader.variantNames[i] = variantName
        }
        json
    }
    
    static Object parseTipps(Object json, boolean useValidator) {
        
        println ". DataTransformer.parseTipps()"
        
        json.package.tipps.each{ tipp ->
            tipp.each{ attr ->
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
        
        println ". DataTransformer.parseTippCoverage()"
    
        json.package.tipps.each{ tipp ->
            tipp.coverage.eachWithIndex{ cover, i ->
                def coverage = [:]
                
                // use validator
                if(!useValidator || (useValidator && cover.m == Status.VALIDATOR_COVERAGE_IS_VALID.toString())){
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
        
         println ". DataTransformer.parseTippTitleIdentifiers()"

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
        
    static parseTitles(Object json, boolean useValidator) {
        
        println ". DataTransformer.parseTitles()"

        json.titles.each{ title ->
            title.each{ attr ->
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
        
        json
    }
  
    static Object parseTitleIdentifiers(Object json, boolean useValidator) {
        
         println ". DataTransformer.parseTitleIdentifiers()"

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
        
        println ". DataTransformer.parsePublisherHistory()"

        json.titles.each{ title ->
            title.publisher_history.eachWithIndex { ph, i ->
                def publisher_history = [:]
                ph.each{ attr -> 
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
        
        // only valid entries
        if(useValidator){

            json.titles.each{ title ->
                def publisher_history = []
                title.publisher_history.each{ ph ->
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
        
        println ". DataTransformer.parseHistoryEvents()"
        json.titles.each{ title ->
            title.history_events.eachWithIndex { he, i ->

                he.v.date = he.v.date.v
                
                he.v.from.each{ from ->
                    def validIdentifiers = []
                    from.identifiers.eachWithIndex{ ident, fi ->
                        
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
                    
                    // only valid entries
                    if(useValidator){
                        if(from.title.m == Status.VALIDATOR_STRING_IS_VALID.toString())
                            from.title = from.title.v
                        else
                            from.title = ""
                    }
                    else {
                        from.title = from.title.v
                    }
                }
                he.v.to.each{ to ->
                    def validIdentifiers = []
                    to.identifiers.eachWithIndex{ ident, ti ->
                        
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
                    
                    // only valid entries
                    if(useValidator){
                        if(to.title.m == Status.VALIDATOR_STRING_IS_VALID.toString())
                            to.title = to.title.v
                        else
                            to.title = ""
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
                    if(he.m == Status.VALIDATOR_HISTORYEVENT_IS_VALID.toString())
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
    
    static Object cleanUpJSON(Object json, boolean useValidator){
        
        println ". DataTransformer.cleanUpJSON()"
        
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