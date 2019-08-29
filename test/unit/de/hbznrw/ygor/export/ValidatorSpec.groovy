package de.hbznrw.ygor.export

import de.hbznrw.ygor.export.structure.TitleStruct
import de.hbznrw.ygor.validators.Validator
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification
import de.hbznrw.ygor.enums.*
import de.hbznrw.ygor.bridges.*

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
            Validator.isValidIdentifier(raw1, raw2, DataMapper.IDENTIFIER_NAMESPACES[0]) == result
        
        where:
            raw1            | raw2              | result
            "1234-5678"     | TitleStruct.EISSN | Status.VALIDATOR_IDENTIFIER_IS_VALID
            "12345-678"     | TitleStruct.EISSN | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "12345678"      | TitleStruct.EISSN | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "1234-56789"    | TitleStruct.EISSN | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "1234-56X"      | TitleStruct.PISSN | Status.VALIDATOR_IDENTIFIER_IS_INVALID
            "1234-567X"     | TitleStruct.PISSN | Status.VALIDATOR_IDENTIFIER_IS_VALID
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
}
