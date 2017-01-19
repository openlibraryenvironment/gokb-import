package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.tools.*
import de.hbznrw.ygor.iet.enums.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import ygor.Enrichment.FileType

class Transformer {
    
    static final YES = 42
    static final NO  = 48
    
    /*
     * the ugly ...  
     * TODO refactoring  
     */ 

    static String getSimpleJSON(DataContainer dc, FileType type) {
     
        def jsonSlurper = new JsonSlurper()
        def json = jsonSlurper.parseText(JsonToolkit.parseDataToJson(dc))

        if(type.equals(FileType.JSON)){
            json = Statistics.getStatsBeforeParsing(json)
        }
        
        json = Transformer.parsePackageHeader(json)
        json = Transformer.parseHashMaps(json)

        //json = Transformer.parseAdditionalProperties(json)
        json = Transformer.parseCuratoryGroups(json)
        json = Transformer.parseSource(json)
        //json = Transformer.parseVariantNames(json)
        
        json = Transformer.parseTipps(json)
        json = Transformer.parseTippTitleIdentifiers(json)
        json = Transformer.parseTippCoverage(json)

        json = Transformer.parseTitles(json)
        json = Transformer.parseTitleIdentifiers(json)
        json = Transformer.parsePublisherHistory(json)

        json = Transformer.cleanUpJSON(json)
        
        if(type.equals(FileType.JSON)){
            json = Statistics.getStatsAfterCleanUp(json)
        }
        else if(type.equals(FileType.JSON_PACKAGE)){
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
    
    static Object parseTipps(Object json) {
        
        println ". DataTransformer.parseTipps()"
        
        json.package.tipps.each{ tipp ->
            tipp.each{ attr ->
                if(attr.value.v instanceof java.lang.String || attr.value.v == null) {
                    tipp."${attr.key}" = (attr.value.v ? attr.value.v : "")
                }
            }
        }
        
        json.package.tipps.each{ tipp ->
            tipp.platform = tipp.platform.v
            tipp.title    = tipp.title.v
            
            // use validator
            if(tipp.title.name.m == Status.VALIDATOR_STRING_IS_VALID.toString()){
                tipp.title.name = tipp.title.name.v
            }
            else {
                tipp.title.name = ""
            }
            tipp.title.type = tipp.title.type.v
        }
        
        json.package.tipps.eachWithIndex{ tipp, i ->
            def platform = [:]
            tipp.platform.each{ pf ->
                if(pf.value.v instanceof java.lang.String) {
                    platform << ["${pf.key}":pf.value.v]
                }
            }
            json.package.tipps[i].platform = platform
        }      
        
        json
    }
   
    static Object parseTippCoverage(Object json) {
        
        println ". DataTransformer.parseTippCoverage()"
    
        json.package.tipps.each{ tipp ->
            tipp.coverage.eachWithIndex{ cover, i ->
                def coverage = [:]
                cover.each{ attr ->
                    if(attr.value.v instanceof java.lang.String) {
                        
                        // use validator
                        if(['startDate', 'endDate'].contains(attr.key)){
                            if(attr.value.m == Status.VALIDATOR_DATE_IS_VALID.toString())
                                coverage << ["${attr.key}":attr.value.v]
                            else
                                coverage << ["${attr.key}":""]
                        }
                        // use validator
                        else if(['startVolume', 'endVolume'].contains(attr.key)){
                            if(attr.value.m == Status.VALIDATOR_NUMBER_IS_VALID.toString())
                                coverage << ["${attr.key}":attr.value.v]
                            else
                                coverage << ["${attr.key}":""]
                        } 
                        else {
                            coverage << ["${attr.key}":attr.value.v]
                        }
                        
                    }
                }
                tipp.coverage[i] = coverage
            }
        }
        
        // only valid entries
        json.package.tipps.each{ tipp ->
            def coverage = []
            tipp.coverage.each{ cover ->
                cover.any { attr ->
                    if(["startDate", "startVolume", "endDate", "endVolume"].contains(attr.key.toString()) && attr.value.toString() != ""){
                        coverage << cover
                        return true
                    } 
                }
            }
            tipp.coverage = coverage
        }
        
        json
    }
    
    static Object parseTippTitleIdentifiers(Object json) {
        
         println ". DataTransformer.parseTippTitleIdentifiers()"

         json.package.tipps.each{ tipp ->
             def validIdentifiers = []
             tipp.title.identifiers.each{ ident ->
                 
                 // use validator
                 if(ident.value.m == Status.VALIDATOR_IDENTIFIER_IS_VALID.toString()){
                     ident.type  = ident.type.v
                     ident.value = ident.value.v
                     validIdentifiers << ident
                 }
             }
             tipp.title.identifiers = validIdentifiers
         }

         json
     }
        
    static parseTitles(Object json) {
        
        println ". DataTransformer.parseTitles()"

        json.titles.each{ title ->
            title.each{ attr ->
                if(attr.value.v instanceof java.lang.String) {
                    
                    // use validator
                    if(attr.key == "name") {
                        if(attr.value.m == Status.VALIDATOR_STRING_IS_VALID.toString()){
                            title."${attr.key}" = attr.value.v
                        }
                        else {
                            title."${attr.key}" = ""
                        }
                    }
                    else {
                        title."${attr.key}" = (attr.value.v ? attr.value.v : "")
                    }
                }
            }
        }
        
        json
    }
  
    static Object parseTitleIdentifiers(Object json) {
        
         println ". DataTransformer.parseTitleIdentifiers()"

         json.titles.each{ title ->
             def validIdentifiers = []
             title.identifiers.each{ ident ->

                 // use validator
                 if(ident.value.m == Status.VALIDATOR_IDENTIFIER_IS_VALID.toString()){
                     ident.type  = ident.type.v
                     ident.value = ident.value.v
                     validIdentifiers << ident
                 }
             }
             title.identifiers = validIdentifiers
         }
         
         json
    }
         
    static Object parsePublisherHistory(Object json) {
        
        println ". DataTransformer.parsePublisherHistory()"

        json.titles.each{ title ->
            title.publisher_history.eachWithIndex { ph, i ->
                def publisher_history = [:]
                ph.each{ attr ->

                    // use validator
                    if(['startDate', 'endDate'].contains(attr.key)){
                        if(attr.value.m == Status.VALIDATOR_DATE_IS_VALID.toString())
                            publisher_history << ["${attr.key}" : attr.value.v]
                        else
                            publisher_history << ["${attr.key}":""]
                    }
                    else {
                        publisher_history << ["${attr.key}" : attr.value.v]
                    }
                }
                title.publisher_history[i] = publisher_history
            }
        }
        
        // only valid entries
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
        
        json
    }
    
    static Object cleanUpJSON(Object json){
        
        println ". DataTransformer.cleanUpJSON()"
        
        //remove tipps without name and identifier
        def tipps = []
        json.package.tipps.each{ tipp ->
            if(tipp.title.identifiers.size() > 0) {
                tipps << tipp
            }
        }
        json.package.tipps = tipps
               
        //remove titles without name and identifier
        def titles = []
        json.titles.each{ title ->
            if(title.identifiers.size() > 0) {
                titles << title
            }
        }
        json.titles = titles
        
        json
    }
}