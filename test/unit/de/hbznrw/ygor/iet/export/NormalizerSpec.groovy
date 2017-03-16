package de.hbznrw.ygor.iet.export

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import de.hbznrw.ygor.iet.enums.*
import de.hbznrw.ygor.iet.bridge.*
import de.hbznrw.ygor.iet.export.structure.*

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class NormalizerSpec extends Specification {

    // fields
    // fixture methods
    // feature methods
    // helper methods
    
    // run before every feature method
    def setup() {
        true
    }

    // run after every feature method
    def cleanup() {
        true
    }
    
    // run before the first feature method
    def setupSpec() {
        true
    }     
    
    // run after the last feature method
    def cleanupSpec() {
        true
    }
    
    void "normString(String str)"() { 
         
        when:
            println "${raw} -> ${result}"
        
        then:
            Normalizer.normString(raw) == result
        
        where:
            raw                                     | result
            null                                    | null
            ""                                      | ""
            "  Trim  "                              | "Trim"
            "Remo     ving Multi  ple  Spa    ces"  | "Remo ving Multi ple Spa ces"
            "Reformatting - a : x  ,y :z"           | "Reformatting - a: x, y: z"               
    }
    
    void "normString(ArrayList list)"() {
        
        when:
            println "${raw} -> ${result}"
        
        then:
            Normalizer.normString(raw) == result
        
        where:
            raw                                     | result
            [null, null, null]                      | "null|null|null"
            [null, "value1", "value2"]              | "null|value1|value2"
            ["a", "b", "c", "d"]                    | "a|b|c|d"
            []                                      | ""
    }
       
    void "normIdentifier(String str, Object type)"() {
        
        when:
            println "${raw[0]}, ${raw[1]} -> ${result}"
        
        then:
            Normalizer.normIdentifier(raw[0], raw[1]) == result
        
        where:
            raw                                     | result
            ["12345678", TitleStruct.EISSN]         | "1234-5678"
            ["1234/5678", TitleStruct.EISSN]        | "1234-5678"
            ["1234567", TitleStruct.EISSN]          | "1234567"
            ["123456789", TitleStruct.EISSN]        | "123456789"
            ["33445XXX", TitleStruct.PISSN]         | "3344-5XXX"
            ["3344--YYYY", TitleStruct.PISSN]       | "3344-YYYY"
            ["1234-567X", ZdbBridge.IDENTIFIER]     | "1234567-X"
            ["12345", ZdbBridge.IDENTIFIER]         | "1234-5"
            [null, null]                            | null
            ["", ""]                                | ""
            
            // TODO: not implemented DataNormalizer.normIdentifier(", EzbBridge.IDENTIFIER) 
    }
    
    void "normIdentifier(ArrayList list, Object type)"() {
        
        given:
            def list1 = ["12345678","1234-88XX","999966"]
            def list2 = [null,"1234-88XX","999966"]
            def result1 = "1234-5678|1234-88XX|999966"
            def result2 = "null|1234-88XX|999966"
        
        expect:
            println "${list1} -> ${result1}"
            println "${list2} -> ${result2}"
            
            Normalizer.normIdentifier(list1, TitleStruct.EISSN) == result1
            Normalizer.normIdentifier(list2, TitleStruct.PISSN) == result2
    }   
    
    void "normDate(String str, Object dateType)"() {
        
        when:
            println "${raw}, ${Normalizer.IS_START_DATE} -> ${resultStartDate}"
            println "${raw}, ${Normalizer.IS_END_DATE} -> ${resultEndDate}"
            
        then:
            Normalizer.normDate(raw, Normalizer.IS_START_DATE) == resultStartDate
            Normalizer.normDate(raw, Normalizer.IS_END_DATE) == resultEndDate
        
        where:
            raw                                 | resultStartDate               | resultEndDate
            "2008"                              | "2008-01-01 00:00:00.000"     | "2008-12-31 23:59:59.000"
            "2010,2"                            | "2010-01-01 00:00:00.000"     | "2010-12-31 23:59:59.000"
            "4.2010"                            | "2010-01-01 00:00:00.000"     | "2010-12-31 23:59:59.000"
            "8.2010,6"                          | "2010-01-01 00:00:00.000"     | "2010-12-31 23:59:59.000"
            "2005/06"                           | "2005-01-01 00:00:00.000"     | "2006-12-31 23:59:59.000"
            "2002/2003"                         | "2002-01-01 00:00:00.000"     | "2003-12-31 23:59:59.000"
            "2005-"                             | "2005-01-01 00:00:00.000"     | ""
            "-2006"                             | ""                            | "2006-12-31 23:59:59.000"
            "2005-06"                           | "2005-01-01 00:00:00.000"     | "2006-12-31 23:59:59.000"
            "2002-2003"                         | "2002-01-01 00:00:00.000"     | "2003-12-31 23:59:59.000"
            "10.2005-11.2006"                   | "2005-01-01 00:00:00.000"     | "2006-12-31 23:59:59.000"
            "10.2005 - 11.2006"                 | "2005-01-01 00:00:00.000"     | "2006-12-31 23:59:59.000"
            "2002 - 2003"                       | "2002-01-01 00:00:00.000"     | "2003-12-31 23:59:59.000"
            "Verlag; 17.2012 -"                 | "2012-01-01 00:00:00.000"     | ""
            null                                | null                          | null
            ""                                  | ""                            | ""
            "1977, 1"                           | "1977-01-01 00:00:00.000"     | "1977-12-31 23:59:59.000"
            "Verlag; 1981 - 1995"               | "1981-01-01 00:00:00.000"     | "1995-12-31 23:59:59.000"
            "Verlag; 1.1981/82 -"               | "1981-01-01 00:00:00.000"     | ""
            "Verlag; 4.2010,2 - 10.2016"        | "2010-01-01 00:00:00.000"     | "2016-12-31 23:59:59.000"
            "Verlag; 4.2010 - 10.2016,2"        | "2010-01-01 00:00:00.000"     | "2016-12-31 23:59:59.000"
            "Verlag; 4.2011,2 - 10.2017,2"      | "2011-01-01 00:00:00.000"     | "2017-12-31 23:59:59.000"
            "[2022-2023]"                       | "2022-01-01 00:00:00.000"     | "2023-12-31 23:59:59.000"
    }
    
    void "normDate(ArrayList list, Object dateType)"() {
        
        given:
            def startDatePostfix = "-01-01 00:00:00.000"
            def endDatePostfix   = "-12-31 23:59:59.000"
        
            def list1 = ["2008","2005-","2005-06", null, null]
            def list2 = [null,"2005-06","-2006", "2005/06", "2010"]
            
            def resultStartDate1 = "2008${startDatePostfix}|2005${startDatePostfix}|2005${startDatePostfix}|null|null"
            def resultStartDate2 = "null|2005${startDatePostfix}||2005${startDatePostfix}|2010${startDatePostfix}"
            def resultEndDate1   = "2008${endDatePostfix}||2006${endDatePostfix}|null|null"
            def resultEndDate2   = "null|2006${endDatePostfix}|2006${endDatePostfix}|2006${endDatePostfix}|2010${endDatePostfix}"

        expect:
            println "${list1} -> START_DATE: ${resultStartDate1}"
            println "${list2} -> START_DATE: ${resultStartDate2}"
            Normalizer.normDate(list1, Normalizer.IS_START_DATE) == resultStartDate1
            Normalizer.normDate(list2, Normalizer.IS_START_DATE) == resultStartDate2
            
            println "${list1} -> END_DATE: ${resultEndDate1}"
            println "${list2} -> END_DATE: ${resultEndDate2}"
            Normalizer.normDate(list1, Normalizer.IS_END_DATE)   == resultEndDate1
            Normalizer.normDate(list2, Normalizer.IS_END_DATE)   == resultEndDate2
    }  
    
    void "normCoverageVolume(String str, Object dateType)"() {
        
        when:
            println "${raw}, ${Normalizer.IS_START_DATE} -> ${resultStartVol}"
            println "${raw}, ${Normalizer.IS_END_DATE} -> ${resultEndVol}"
            
        then:
            Normalizer.normCoverageVolume(raw, Normalizer.IS_START_DATE) == resultStartVol
            Normalizer.normCoverageVolume(raw, Normalizer.IS_END_DATE) == resultEndVol
        
        where:
            raw                                     | resultStartVol    | resultEndVol
            "18.2005 - 27.2014"                     | "18"              | "27"
            "Verlag; 18.2005 - 27.2014"             | "18"              | "27"
            "22.2022"                               | "22"              | "22"
            "05.1995 - "                            | "05"              | ""
            "Verlag; 1.1981/82 -"                   | "1"               | ""
            "Verlag; 1.1971 - 38.2001/02"           | "1"               | "38"
            "1997, 1"                               | "1"               | "1"
    }
    
    void "normURL(String str) "() {
        
        given:
            def test = [
                "https://google.de/",
                "http://yahoo.de?q=laser",
                "https://google.de/this/and/that",
                "http://laser.hbz-nrw.de:8080/and/some/more",
                "golem.de"
                ]
            def result = [
                "google.de",
                "yahoo.de",
                "google.de",
                "laser.hbz-nrw.de:8080",
                "golem.de"
                ]
            
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> ${result[i]}"
            }
            Normalizer.normURL(test[0]) == result[0]
            Normalizer.normURL(test[1]) == result[1]
            Normalizer.normURL(test[2]) == result[2]
            Normalizer.normURL(test[3]) == result[3]
            Normalizer.normURL(test[4]) == result[4]
    }
    
    void "normURL(ArrayList list)"() {
        
        given:
            def test = [
                ["http://google.de/", null, "http://yahoo.de?q=laser"]
            ]
            def result = [
                "google.de|null|yahoo.de"
                ]
         expect:
             println "${test[0]} -> ${result[0]}"
             Normalizer.normURL(test[0]) == result[0]
    }
    
    void "normTippURL(String str, String nominalPlatform) "() {
        
        given:
            def test = [
                    ["https://google.de/?123"        , "https://google.de"],
                    ["http://yahoo.de?q=laser"       , "https://yahoo.de/etc"],
                    ["https://google.de/sub/?123"    , "https://google.de"],
                    ["http://yahoo.de/a/b/c?q=laser" , "http://yahoo.de/v/w"],
                    ["https://google.de"             , "https://yahoo.de"],
                    ["https://google.de/?123"        , ""],
                    [""                              , "https://google.de"]
                ]
            def result = [
                "https://google.de/?123",
                "http://yahoo.de?q=laser" ,
                "https://google.de/sub/?123",
                "http://yahoo.de/a/b/c?q=laser",
                "",
                "https://google.de/?123",
                ""
                ]
            
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i][0]} matches ${test[i][1]} -> ${result[i]}"
            }
            Normalizer.normTippURL(test[0][0], test[0][1]) == result[0]
            Normalizer.normTippURL(test[1][0], test[1][1]) == result[1]
            Normalizer.normTippURL(test[2][0], test[2][1]) == result[2]
            Normalizer.normTippURL(test[3][0], test[3][1]) == result[3]
            Normalizer.normTippURL(test[4][0], test[4][1]) == result[4]
            Normalizer.normTippURL(test[5][0], test[5][1]) == result[5]
            Normalizer.normTippURL(test[6][0], test[6][1]) == result[6]
    }
    
    void "normTippURL(ArrayList list, String nominalPlatform)"() {
        
        given:
            def test = [
                    [["https://google.de/?123", "http://yahoo.de?q=laser", ""]                      , "https://google.de"],
                    [["http://yahoo.de/a/b/c?q=laser", "http://yahoo.de/v/w", "https://google.de"]  , "https://google.de"],
                    [["http://yahoo.de/a/b/c?q=laser", "", null]                                    , "https://yahoo.de"],
                    [["http://yahoo.de/a/b/c?q=laser", "", null]                                    , "https://google.de"],
                    [["http://yahoo.de/a/b/c?q=laser", "http://yahoo.de/v/w", "https://google.de"]  , "https://yahoo.de"]
                ]
            def result = [
                "https://google.de/?123",
                "https://google.de" ,
                "http://yahoo.de/a/b/c?q=laser",
                "",
                "http://yahoo.de/a/b/c?q=laser|http://yahoo.de/v/w"
                ]
                
         expect:
             test.eachWithIndex{e, i ->
                println "${test[i][0]} matches ${test[i][1]} -> ${result[i]}"
             }
             Normalizer.normTippURL(test[0][0], test[0][1]) == result[0]
             Normalizer.normTippURL(test[1][0], test[1][1]) == result[1]
             Normalizer.normTippURL(test[2][0], test[2][1]) == result[2]
             Normalizer.normTippURL(test[3][0], test[3][1]) == result[3]
             Normalizer.normTippURL(test[4][0], test[4][1]) == result[4]
           
    }
    
    void "parseDate(String str, Object dateType)"() {
        
        when:
            println "${raw}, ${Normalizer.IS_START_DATE} -> ${resultStartDate}"
            println "${raw}, ${Normalizer.IS_END_DATE} -> ${resultEndDate}"
        

        then:
            Normalizer.parseDate(raw, Normalizer.IS_START_DATE) == resultStartDate
            Normalizer.parseDate(raw, Normalizer.IS_END_DATE) == resultEndDate
    
        where:
            raw                                     | resultStartDate | resultEndDate
            "05"                                    | "2005"          | "2005"
            "2022"                                  | "2022"          | "2022"
            "2010/11"                               | "2010"          | "2011"
            "10/11"                                 | "2010"          | "2011"
            "15-16"                                 | "2015"          | "2016"
            "1997-1998"                             | "1997"          | "1998"
            "1991/1992"                             | "1991"          | "1992"
            "1.1981/82"                             | "1981"          | "1982"
    }
    
    void "parseCoverageVolume(String str)"() {
        
        when:
            println "${raw} -> ${result}"
        

        then:
            Normalizer.parseCoverageVolume(raw) == result
    
        where:
            raw                                     | result
            "22.2022"                               | "22"
            "23. 2022"                              | "23"
            "1997,1"                                | "1"
            "1997, 2"                               | "2"
    }
    
}
