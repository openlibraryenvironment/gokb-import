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
        
        json.package.tipps.each{ t ->
            t.platform          = t.platform.v
            t.title             = t.title.v
            t.title.name        = t.title.name.v
            t.title.type        = t.title.type.v
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
                        coverage << ["${attr.key}":attr.value.v]
                    }
                }
                tipp.coverage[i] = coverage
            }
        }
        
        json
    }
    
    static Object parseTippTitleIdentifiers(Object json) {
        
         println ". DataTransformer.parseTippTitleIdentifiers()"

         json.package.tipps.each{ tipp ->
             tipp.title.identifiers.each{ ident ->
                 ident.each{ attr ->
                     // e.key: type  | e.value.v: eissn     | e.value.m: VALIDATOR_IDENTIFIER_IS_VALID
                     // e.key: value | e.value.v: 1234-5678 | e.value.m: VALIDATOR_IDENTIFIER_IS_VALID
                     ident."${attr.key}" = attr.value.v
                 }
             }
         }

         json
     }
        
    static parseTitles(Object json) {
        
        println ". DataTransformer.parseTitles()"

        json.titles.each{ title ->
            title.each{ attr ->
                if(attr.value.v instanceof java.lang.String) {
                    title."${attr.key}" = (attr.value.v ? attr.value.v : "")
                }
            }
        }
        
        json
    }
  
    static Object parseTitleIdentifiers(Object json) {
        
         println ". DataTransformer.parseTitleIdentifiers()"

         json.titles.each{ title ->
             title.identifiers.each{ ident ->
                 ident.each{ attr ->
                     // e.key: type  | e.value.v: eissn     | e.value.m: VALIDATOR_IDENTIFIER_IS_VALID
                     // e.key: value | e.value.v: 1234-5678 | e.value.m: VALIDATOR_IDENTIFIER_IS_VALID
                     ident."${attr.key}" = attr.value.v
                 }
             }
         }
         
         json
    }
         
    static Object parsePublisherHistory(Object json) {
        
        println ". DataTransformer.parsePublisherHistory()"
        
        json.titles.each{ title ->
            title.publisher_history.eachWithIndex { ph, i ->
                def publisher_history = [:] 
                ph.each{ attr ->
                    publisher_history << ["${attr.key}" : attr.value.v]
                }
                title.publisher_history[i] = publisher_history
            }
        }
        
        json
    }
         
    static Object parseIdentifiers(Object json, Object removeNonValid) {
   
        println ". DataTransformer.parseIdentifiers()"
  
        // TODO
        
        json.package.tipps.each{ t ->
            t.title.identifiers.each{ i ->
                i.each{ e ->
                    // e.key: type  | e.value.v: eissn     | e.value.m: VALIDATOR_IDENTIFIER_IS_VALID
                    // e.key: value | e.value.v: 1234-5678 | e.value.m: VALIDATOR_IDENTIFIER_IS_VALID
                    i."${e.key}" = e.value.v
                }
            }
        }
        
        json.titles.each{ t ->
            t.identifiers.each{ i ->
                i.each{ e ->
                    // e.key: type  | e.value.v: eissn     | e.value.m: VALIDATOR_IDENTIFIER_IS_VALID
                    // e.key: value | e.value.v: 1234-5678 | e.value.m: VALIDATOR_IDENTIFIER_IS_VALID
                    i."${e.key}" = e.value.v
                }
            }
        }
        
        json
    }
    
    static Object cleanUpJSON(Object json){
        
        println ". DataTransformer.cleanUpJSON()"
        
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
}