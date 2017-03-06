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
class ValidatorSpec extends Specification {

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
    
    void "isValidString(String str)"() {
        
        when:
            println "${raw} -> ${result}"
        
        then:
            Validator.isValidString(raw) == result
        
        where:
            raw                 | result
            "a"                 | Status.VALIDATOR_STRING_IS_INVALID
            "This is a title"   | Status.VALIDATOR_STRING_IS_VALID
            "Multiple|Strings"  | Status.VALIDATOR_STRING_IS_NOT_ATOMIC
            "ab"                | Status.VALIDATOR_STRING_IS_VALID
            ""                  | Status.VALIDATOR_STRING_IS_MISSING
            null                | Status.VALIDATOR_STRING_IS_MISSING
    }
    
    void "isValidNumber(String str)"() {
        
        when:
            println "${raw} -> ${result}"
        
        then:
            Validator.isValidNumber(raw) == result
        
        where:
            raw                 | result
            "124"               | Status.VALIDATOR_NUMBER_IS_VALID
            "124,5"             | Status.VALIDATOR_NUMBER_IS_INVALID
            "333.6"             | Status.VALIDATOR_NUMBER_IS_INVALID
            "ab"                | Status.VALIDATOR_NUMBER_IS_INVALID
            "123|456"           | Status.VALIDATOR_NUMBER_IS_NOT_ATOMIC
            ""                  | Status.VALIDATOR_NUMBER_IS_MISSING
            null                | Status.VALIDATOR_NUMBER_IS_MISSING
    }
    
    void "isValidIdentifier(String str, Object identifierType)"() {
        
        when:
            println "${raw1}, ${raw2} -> ${result}"
        
        then:
            Validator.isValidIdentifier(raw1, raw2) == result
        
        where:
            raw1            | raw2                  | result
            "1234-5678"     | TitleStruct.EISSN     | Status.VALIDATOR_IDENTIFIER_IS_VALID
            "12345-678"     | TitleStruct.EISSN     | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "12345678"      | TitleStruct.EISSN     | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "1234-56789"    | TitleStruct.EISSN     | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "1234-56X"      | TitleStruct.PISSN     | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "1234-567X"     | TitleStruct.PISSN     | Status.VALIDATOR_IDENTIFIER_IS_VALID
            "1234-X"        | ZdbBridge.IDENTIFIER  | Status.VALIDATOR_IDENTIFIER_IS_VALID
            "1234-5X"       | ZdbBridge.IDENTIFIER  | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "1234678910-X"  | ZdbBridge.IDENTIFIER  | Status.VALIDATOR_IDENTIFIER_IS_VALID
            "23"            | EzbBridge.IDENTIFIER  | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "1234254"       | EzbBridge.IDENTIFIER  | Status.VALIDATOR_IDENTIFIER_IS_VALID
            "1234678910-X"  | "unkown identifier"   | Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE
            ""              | TitleStruct.EISSN     | Status.VALIDATOR_IDENTIFIER_IS_MISSING
            null            | EzbBridge.IDENTIFIER  | Status.VALIDATOR_IDENTIFIER_IS_MISSING
    }
    
    void "isValidDate(String str)"() {
        
        when:
            println "${raw} -> ${result}"
        
        then:
            Validator.isValidDate(raw) == result
        
        where:
            raw                         | result
            "1999-01-01 00:00:00.000"   | Status.VALIDATOR_DATE_IS_VALID
            "1989-12-31 23:59:59.000"   | Status.VALIDATOR_DATE_IS_VALID
            "-01-01 00:00:00.000"       | Status.VALIDATOR_DATE_IS_INVALID
            "-12-31 23:59:59.000"       | Status.VALIDATOR_DATE_IS_INVALID
            ""                          | Status.VALIDATOR_DATE_IS_MISSING
            null                        | Status.VALIDATOR_DATE_IS_MISSING
    }
    
    void "isValidURL(String str)"() {
        
        when:
            println "${raw} -> ${result}"
        
        then:
            Validator.isValidURL(raw) == result
        
        where:
            raw                     | result
            "https://google.de/"    | Status.VALIDATOR_URL_IS_VALID
            "http://google.de/?123" | Status.VALIDATOR_URL_IS_VALID
            "google.de"             | Status.VALIDATOR_URL_IS_INVALID
            "http://bib.uni-regensburg.de/ezeit/?2007988|http://www.emeraldinsight.com/loi/bij" | Status.VALIDATOR_URL_IS_NOT_ATOMIC
            ""                      | Status.VALIDATOR_URL_IS_MISSING
            null                    | Status.VALIDATOR_URL_IS_MISSING
    }
    
    void "isValidCoverage(Pod startDate, Pod endDate, Pod startVolume, Pod endVolume)"() {
        given:
            def test = [
                [
                    new Pod("ads", Status.VALIDATOR_DATE_IS_INVALID),
                    new Pod("ads", Status.VALIDATOR_DATE_IS_INVALID),
                    new Pod("11",  Status.VALIDATOR_NUMBER_IS_VALID),
                    new Pod("11",  Status.VALIDATOR_NUMBER_IS_VALID)
                ],
                [
                    new Pod("ads", Status.VALIDATOR_DATE_IS_INVALID),
                    new Pod("ads", Status.VALIDATOR_DATE_IS_INVALID),
                    new Pod("5",   Status.VALIDATOR_NUMBER_IS_VALID),
                    new Pod("22",  Status.VALIDATOR_NUMBER_IS_VALID)
                ],
                [
                    new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID),
                    new Pod("2012-02-05 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID),
                    new Pod("",  Status.VALIDATOR_NUMBER_IS_INVALID),
                    new Pod("",  Status.VALIDATOR_NUMBER_IS_INVALID)
                ],
                [
                    new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID),
                    new Pod("2012-02-05 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID),
                    new Pod("3",  Status.VALIDATOR_NUMBER_IS_VALID),
                    new Pod("6",  Status.VALIDATOR_NUMBER_IS_VALID)
                ],
                [
                    new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID),
                    new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID),
                    new Pod("1",  Status.VALIDATOR_NUMBER_IS_VALID),
                    new Pod("1",  Status.VALIDATOR_NUMBER_IS_VALID)
                ],
                [
                    new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID),
                    new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID),
                    new Pod("5",  Status.VALIDATOR_NUMBER_IS_VALID),
                    new Pod("10", Status.VALIDATOR_NUMBER_IS_VALID)
                ],
            ]
            def result = [
                false,
                true,
                true,
                true,
                false,
                false
            ]
        
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i][0].v} - ${test[i][1].v}, Vol: ${test[i][2].v} - ${test[i][3].v} -> ${result[i]}"
            }
            Validator.isValidCoverage(test[0][0],test[0][1],test[0][2],test[0][3]) == result[0]
            Validator.isValidCoverage(test[1][0],test[1][1],test[1][2],test[1][3]) == result[1]
            Validator.isValidCoverage(test[2][0],test[2][1],test[2][2],test[2][3]) == result[2]
            Validator.isValidCoverage(test[3][0],test[3][1],test[3][2],test[3][3]) == result[3]
            Validator.isValidCoverage(test[4][0],test[4][1],test[4][2],test[4][3]) == result[4]
            Validator.isValidCoverage(test[5][0],test[5][1],test[5][2],test[5][3]) == result[5]
    }
    
    void "isValidHistoryEvent(Pod historyEvent)"() {
        given:
            println "UNIT TEST NOT IMPLEMENTED"
        expect:
            1 == 1
    }
}
