package de.hbznrw.ygor.iet.export

import de.hbznrw.ygor.tools.*
import de.hbznrw.ygor.iet.enums.*
import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import ygor.Enrichment.FileType

class Transformer {
    
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
        json = Transformer.parseCuratoryGroups(json)
        json = Transformer.parseSource(json)
        json = Transformer.parseVariantNames(json)
        json = Transformer.parseTipps(json)
        json = Transformer.parseCoverage(json)
        json = Transformer.parseTitles(json)
        json = Transformer.parsePublisherHistory(json)
        json = Transformer.parseIdentifiers(json)
        
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
    
    static Object cleanUpJSON(Object json){
        
        // remove empty identifiers
        json.package.tipps.each{ tipp ->
            def identifiers = []
            tipp.title.identifiers.each { ident ->
                if(ident.value) {
                    identifiers << ident
                }
            }
            tipp.title.identifiers = identifiers
        }
        
        //remove tipps without name and identifier
        def tipps = []
        json.package.tipps.each{ tipp ->
            if(tipp.title.name?.trim() != "" && tipp.title.identifiers?.size() > 0) {
                tipps << tipp
            }
        }
        json.package.tipps = tipps
        
        // remove empty identifiers
        json.titles.each{ title ->
            def identifiers = []
            title.identifiers.each { ident ->
                if(ident.value) {
                    identifiers << ident
                }
            }
            title.identifiers = identifiers
        }
        
        //remove titles without name and identifier
        def titles = []
        json.titles.each{ title ->
            if(title.name?.trim() != "" && title.identifiers?.size() > 0) {
                titles << title
            }
        }
        json.titles = titles
        
        json
    }
    
    static Object parsePackageHeader(Object json) {
        
        println ". DataTransformer.parsePackageHeader()"
        
        json.package = json.package.v
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
    
    static Object parseCuratoryGroups(Object json) {
        
        println ". DataTransformer.parseCuratoryGroups()"
        
        json.package.packageHeader.curatoryGroups = json.package.packageHeader.curatoryGroups.v
        
        def curatoryGroups = []
        json.package.packageHeader.curatoryGroups.each{ cg -> curatoryGroups << cg.v }
        json.package.packageHeader.curatoryGroups = curatoryGroups
        
        
        json.package.packageHeader.curatoryGroups.eachWithIndex{ cg, i ->
            def curatoryGroup = [:]
            cg.each { g ->
                curatoryGroup << ["${g.key}" : g.value.v.v] 
            }
            json.package.packageHeader.curatoryGroups[i] = curatoryGroup
        }

        json
    }
    
    
    static Object parseSource(Object json) {
        
        println ". DataTransformer.parseSource()"
        
        json.package.packageHeader.source = json.package.packageHeader.source.v
        
        def source = [:]
        json.package.packageHeader.source.eachWithIndex{ se, i ->
            if(se.value.v instanceof java.lang.String) {
                source << ["${se.key}":se.value.v]
            }
        }
        json.package.packageHeader.source = source
        
        json
    }
    
    static parseVariantNames(Object json) {
        
        println "DataTransformer.parseVariantNames() -  TODO"
        
        json.package.packageHeader.variantNames = json.package.packageHeader.variantNames.v
        
        json
    }
    
    static Object parseTipps(Object json) {
        
        println ". DataTransformer.parseTipps()"
        
        def tipps = []
        json.package.tipps.v.each{ t -> tipps << t.value.v }
        json.package.tipps = tipps
        
        json.package.tipps.each{ t ->
            t.each{ ta ->
                if(ta.value.v instanceof java.lang.String || ta.value.v == null) {
                    t."${ta.key}" = (ta.value.v ? ta.value.v : "")
                }
            }
        }
        
        json.package.tipps.each{ t ->
            t.platform          = t.platform.v
            t.title             = t.title.v
            t.title.identifiers = t.title.identifiers.v
            t.title.name        = t.title.name.v
            t.title.type        = t.title.type.v
            t.coverage          = t.coverage.v
        }
        
        json.package.tipps.eachWithIndex{ t, i ->
            def platform = [:]
            t.platform.each{ pf ->
                if(pf.value.v instanceof java.lang.String) {
                    platform << ["${pf.key}":pf.value.v]
                }
            }
            json.package.tipps[i].platform = platform
        }      
        
        json
    }
   
    static Object parseCoverage(Object json) {
        
        println ". DataTransformer.parseCoverage()"
    
        json.package.tipps.each{ t ->
            t.coverage.eachWithIndex{ c, i ->
                def coverage = [:]
                c.each{ cfields ->
                    if(cfields.value.v instanceof java.lang.String) {
                        coverage << ["${cfields.key}":cfields.value.v]
                    }
                }
                t.coverage[i] = coverage
            }
        }
        
        json
    }
        
    static parseTitles(Object json) {
        
        println ". DataTransformer.parseTitles()"
        
        def titles = []
        json.titles = json.titles.v.each{ t -> titles << t.value.v }
        json.titles = titles
        
        json.titles.each{ t ->
            t.each{ tfield ->
                if(tfield.value.v instanceof java.lang.String) {
                    t."${tfield.key}" = (tfield.value.v ? tfield.value.v : "")
                }
            }
        }
        
        json.titles.each{ t -> t.identifiers = t.identifiers.v }
        json.titles.each{ t -> t.publisher_history = t.publisher_history.v }
        
        json
    }
  
    static Object parsePublisherHistory(Object json) {
        
        println ". DataTransformer.parsePublisherHistory()"
        
        json.titles.each{ t ->
            t.publisher_history.eachWithIndex { ph, i ->
                def publisher_history = [:] 
                ph.v.each{ phfield ->
                    publisher_history << ["${phfield.key}":phfield.value.v]
                }
                t.publisher_history[i] = publisher_history
            }
        }
        
        json
    }
    
    static Object parseIdentifiers(Object json) {
   
        println ". DataTransformer.parseIdentifiers()"
        
        json.package.tipps.each{ t ->
            def identifiers = []
            t.title.identifiers.each { i -> identifiers << i.v }
            t.title.identifiers = identifiers
        }
        
        json.package.tipps.each{ t ->
            t.title.identifiers.each{ i ->
                i.each{ e ->
                    i."${e.key}" = e.value.v
                }
            }
        }

        json.titles.each{ t ->
            def identifiers = []
            t.identifiers.each { i -> identifiers << i.v }
            t.identifiers = identifiers
        }
        
        json.titles.each{ t ->
            t.identifiers.each{ i ->
                i.each{ e ->
                    i."${e.key}" = e.value.v
                }
            }
        }
        
        json
    }
}