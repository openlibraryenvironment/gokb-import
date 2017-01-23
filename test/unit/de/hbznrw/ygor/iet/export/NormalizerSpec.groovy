package de.hbznrw.ygor.iet.export

import java.util.ArrayList

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
    def setup() {}

    // run after every feature method
    def cleanup() {}
    
    // run before the first feature method
    def setupSpec() {}     
    
    // run after the last feature method
    def cleanupSpec() {}   
    
    
    
    void "normString(String str)"() {
        given:
            def test = [
                null,
                "",
                "  Trim  ",
                "Remo     ving Multi  ple  Spa    ces",
                "Reformatting - a : x  ,y :z"
                ]
            def result = [
                null,
                "",
                "Trim",
                "Remo ving Multi ple Spa ces",
                "Reformatting - a: x, y: z"
                ]
                  
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> ${result[i]}"
            }
            Normalizer.normString(test[0]) == result[0]
            Normalizer.normString(test[1]) == result[1]
            Normalizer.normString(test[2]) == result[2]
            Normalizer.normString(test[3]) == result[3]
            Normalizer.normString(test[4]) == result[4]
    }
    
    void "normString(ArrayList list)"() {
        given:
            def test = [
                [null, null, null],
                [null, "value1", "value2"],
                ["a", "b", "c", "d"]
                ]  
            def result = [
                "null|null|null",
                "null|value1|value2",
                "a|b|c|d"
                ]
            
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> ${result[i]}"
            }
            Normalizer.normString(test[0]) == result[0]
            Normalizer.normString(test[1]) == result[1]
            Normalizer.normString(test[2]) == result[2]
            Normalizer.normString(test[3]) == result[3]
    }
       
    void "normIdentifier(String str, Object type)"() {
        given:
            def test = [
                ["12345678", TitleStruct.EISSN],
                ["1234/5678", TitleStruct.EISSN],
                ["1234567", TitleStruct.EISSN],
                ["123456789", TitleStruct.EISSN],
                ["33445XXX", TitleStruct.PISSN],
                ["3344--YYYY", TitleStruct.PISSN],
                ["1234-567X", ZdbBridge.IDENTIFIER],
                ["12345", ZdbBridge.IDENTIFIER],
                [null, null],
                ["", ""]
            ]
            def result = [
                "1234-5678",
                "1234-5678",
                "1234567",
                "123456789",
                "3344-5XXX",
                "3344-YYYY",
                "1234567-X",
                "1234-5",
                null,
                ""
                ]
        
        expect: 
            test.eachWithIndex{e, i ->
                println "${test[i]} -> ${result[i]}"
            }
            Normalizer.normIdentifier(test[0][0], test[0][1]) == result[0]
            Normalizer.normIdentifier(test[1][0], test[1][1]) == result[1]
            Normalizer.normIdentifier(test[2][0], test[2][1]) == result[2]
            Normalizer.normIdentifier(test[3][0], test[3][1]) == result[3]
            Normalizer.normIdentifier(test[4][0], test[4][1]) == result[4]
            Normalizer.normIdentifier(test[5][0], test[5][1]) == result[5]
            Normalizer.normIdentifier(test[6][0], test[6][1]) == result[6]
            Normalizer.normIdentifier(test[7][0], test[7][1]) == result[7]
            Normalizer.normIdentifier(test[8][0], test[8][1]) == result[8]
            Normalizer.normIdentifier(test[9][0], test[9][1]) == result[9]
            
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
        given:
            def test = [
                "2008",
                "2005/06",
                "2002/2003",
                "2005-",
                "-2006",
                "2005-06",
                "2002-2003",
                "10.2005-11.2006",
                "10.2005 - 11.2006",
                null,
                ""
                ]
            def result = [
                ["2008-01-01 00:00:00.000", "2008-12-31 23:59:59.000"],
                ["2005-01-01 00:00:00.000", "2006-12-31 23:59:59.000"],
                ["2002-01-01 00:00:00.000", "2003-12-31 23:59:59.000"],
                ["2005-01-01 00:00:00.000", ""],
                ["", "2006-12-31 23:59:59.000"],
                ["2005-01-01 00:00:00.000", "2006-12-31 23:59:59.000"],
                ["2002-01-01 00:00:00.000", "2003-12-31 23:59:59.000"],
                ["2005-01-01 00:00:00.000", "2006-12-31 23:59:59.000"],
                ["2005-01-01 00:00:00.000", "2006-12-31 23:59:59.000"],
                [null, null],
                ["", ""]
                ]
        
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> START_DATE: ${result[i][0]}"
            }
            Normalizer.normDate(test[0], Normalizer.IS_START_DATE) == result[0][0]
            Normalizer.normDate(test[1], Normalizer.IS_START_DATE) == result[1][0]
            Normalizer.normDate(test[2], Normalizer.IS_START_DATE) == result[2][0]
            Normalizer.normDate(test[3], Normalizer.IS_START_DATE) == result[3][0]
            Normalizer.normDate(test[4], Normalizer.IS_START_DATE) == result[4][0]
            Normalizer.normDate(test[5], Normalizer.IS_START_DATE) == result[5][0]
            Normalizer.normDate(test[6], Normalizer.IS_START_DATE) == result[6][0]
            Normalizer.normDate(test[7], Normalizer.IS_START_DATE) == result[7][0]
            Normalizer.normDate(test[8], Normalizer.IS_START_DATE) == result[8][0]
            Normalizer.normDate(test[9], Normalizer.IS_START_DATE) == result[9][0]
            Normalizer.normDate(test[10], Normalizer.IS_START_DATE) == result[10][0]
            
            
            test.eachWithIndex{e, i ->
                println "${test[i]} -> END_DATE: ${result[i][1]}"
            }
            Normalizer.normDate(test[0], Normalizer.IS_END_DATE) == result[0][1]
            Normalizer.normDate(test[1], Normalizer.IS_END_DATE) == result[1][1]
            Normalizer.normDate(test[2], Normalizer.IS_END_DATE) == result[2][1]
            Normalizer.normDate(test[3], Normalizer.IS_END_DATE) == result[3][1]
            Normalizer.normDate(test[4], Normalizer.IS_END_DATE) == result[4][1]
            Normalizer.normDate(test[5], Normalizer.IS_END_DATE) == result[5][1]
            Normalizer.normDate(test[6], Normalizer.IS_END_DATE) == result[6][1]
            Normalizer.normDate(test[7], Normalizer.IS_END_DATE) == result[7][1]
            Normalizer.normDate(test[8], Normalizer.IS_END_DATE) == result[8][1]
            Normalizer.normDate(test[9], Normalizer.IS_END_DATE) == result[9][1]
            Normalizer.normDate(test[10], Normalizer.IS_END_DATE) == result[10][1]
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
        given:
            def test = [
                "18.2005 - 27.2014",
                "Verlag; 18.2005 - 27.2014",
                "22.2022",
                "05.1995 - "
                ]
            def result = [
                ["18", "27"],
                ["18", "27"],
                ["22", "22"],
                ["05", ""]
                ]
            
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> START_DATE: ${result[i][0]}"
            }
            Normalizer.normCoverageVolume(test[0], Normalizer.IS_START_DATE) == result[0][0]
            Normalizer.normCoverageVolume(test[1], Normalizer.IS_START_DATE) == result[1][0]
            Normalizer.normCoverageVolume(test[2], Normalizer.IS_START_DATE) == result[2][0]
            Normalizer.normCoverageVolume(test[3], Normalizer.IS_START_DATE) == result[3][0]
            
            test.eachWithIndex{e, i ->
                println "${test[i]} -> END_DATE: ${result[i][1]}"
            }
            Normalizer.normCoverageVolume(test[0], Normalizer.IS_END_DATE) == result[0][1]
            Normalizer.normCoverageVolume(test[1], Normalizer.IS_END_DATE) == result[1][1]
            Normalizer.normCoverageVolume(test[2], Normalizer.IS_END_DATE) == result[2][1]
            Normalizer.normCoverageVolume(test[3], Normalizer.IS_END_DATE) == result[3][1]
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
}
