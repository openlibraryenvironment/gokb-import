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
class ValidatorSpec extends Specification {

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
    
    
    void "isValidString(String str)"() {
        given:
            def test = [
                "This is a title",
                "Multiple|Strings",
                "ab",
                "",
                null
                ]
            def result = [
                Status.VALIDATOR_STRING_IS_VALID,
                Status.VALIDATOR_STRING_IS_INVALID,
                Status.VALIDATOR_STRING_IS_INVALID,
                Status.VALIDATOR_STRING_IS_INVALID,
                Status.VALIDATOR_STRING_IS_INVALID
                ]
            
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> ${result[i]}"
            }
            Validator.isValidString(test[0]) == result[0]
            Validator.isValidString(test[1]) == result[1]
            Validator.isValidString(test[2]) == result[2]
            Validator.isValidString(test[3]) == result[3]
            Validator.isValidString(test[4]) == result[4]
    }
    
    void "isValidNumber(String str)"() {
        given:
            def test = [
                "124",
                "124,5",
                "333.6",
                "ab",
                "",
                null
                ]
            def result = [
                Status.VALIDATOR_NUMBER_IS_VALID,
                Status.VALIDATOR_NUMBER_IS_INVALID,
                Status.VALIDATOR_NUMBER_IS_INVALID,
                Status.VALIDATOR_NUMBER_IS_INVALID,
                Status.VALIDATOR_NUMBER_IS_INVALID,
                Status.VALIDATOR_NUMBER_IS_INVALID
                ]
            
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> ${result[i]}"
            }
            Validator.isValidNumber(test[0]) == result[0]
            Validator.isValidNumber(test[1]) == result[1]
            Validator.isValidNumber(test[2]) == result[2]
            Validator.isValidNumber(test[3]) == result[3]
            Validator.isValidNumber(test[4]) == result[4]
            Validator.isValidNumber(test[5]) == result[5]
    }
    
    void "isValidIdentifier(String str, Object identifierType)"() {
        given:
            def test = [
                ["1234-5678",    TitleStruct.EISSN],
                ["12345-678",    TitleStruct.EISSN],
                ["12345678",     TitleStruct.EISSN],
                ["1234-56789",   TitleStruct.EISSN],
                ["1234-56X",     TitleStruct.PISSN],
                ["1234-567X",    TitleStruct.PISSN],
                ["1234-X",       ZdbBridge.IDENTIFIER],
                ["1234-5X",      ZdbBridge.IDENTIFIER],
                ["1234678910-X", ZdbBridge.IDENTIFIER],
                ["1234678910-X", "unkown identifier"]
                ]
            def result = [
                Status.VALIDATOR_IDENTIFIER_IS_VALID,
                Status.VALIDATOR_IDENTIFIER_IS_INVALID,
                Status.VALIDATOR_IDENTIFIER_IS_INVALID,
                Status.VALIDATOR_IDENTIFIER_IS_INVALID,
                Status.VALIDATOR_IDENTIFIER_IS_INVALID,
                Status.VALIDATOR_IDENTIFIER_IS_VALID,
                Status.VALIDATOR_IDENTIFIER_IS_VALID,
                Status.VALIDATOR_IDENTIFIER_IS_INVALID,
                Status.VALIDATOR_IDENTIFIER_IS_VALID,
                Status.VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE
                ]

        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> ${result[i]}"
            }
            Validator.isValidIdentifier(test[0][0], test[0][1]) == result[0]
            Validator.isValidIdentifier(test[1][0], test[1][1]) == result[1]
            Validator.isValidIdentifier(test[2][0], test[2][1]) == result[2]
            Validator.isValidIdentifier(test[3][0], test[3][1]) == result[3]
            Validator.isValidIdentifier(test[4][0], test[4][1]) == result[4]
            Validator.isValidIdentifier(test[5][0], test[5][1]) == result[5]
            Validator.isValidIdentifier(test[6][0], test[6][1]) == result[6]
            Validator.isValidIdentifier(test[7][0], test[7][1]) == result[7]
            Validator.isValidIdentifier(test[8][0], test[8][1]) == result[8]
            Validator.isValidIdentifier(test[9][0], test[9][1]) == result[9]
    }
    
    void "isValidDate(String str)"() {
        given:
            def test = [
                "1999-01-01 00:00:00.000",
                "2000-12-31 23:59:59.000",
                "-01-01 00:00:00.000",
                "-12-31 23:59:59.000",
                null,
                ""
                ]
            def result = [
                Status.VALIDATOR_DATE_IS_VALID,
                Status.VALIDATOR_DATE_IS_VALID,
                Status.VALIDATOR_DATE_IS_INVALID,
                Status.VALIDATOR_DATE_IS_INVALID,
                Status.VALIDATOR_DATE_IS_MISSING,
                Status.VALIDATOR_DATE_IS_MISSING
                ]
            
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> ${result[i]}"
            }
            Validator.isValidDate(test[0]) == result[0]
            Validator.isValidDate(test[1]) == result[1]
            Validator.isValidDate(test[2]) == result[2]
            Validator.isValidDate(test[3]) == result[3]
            Validator.isValidDate(test[4]) == result[4]
            Validator.isValidDate(test[5]) == result[5]
    }
    
    void "isValidURL(String str)"() {
        given:
            def test = [
                "https://google.de/",
                "http://google.de/?123",
                "google.de",
                null,
                ""
                ]
            def result = [
                Status.VALIDATOR_URL_IS_VALID,
                Status.VALIDATOR_URL_IS_VALID,
                Status.VALIDATOR_URL_IS_INVALID,
                Status.VALIDATOR_URL_IS_INVALID,
                Status.VALIDATOR_URL_IS_INVALID
                ]
            
        expect:
            test.eachWithIndex{e, i ->
                println "${test[i]} -> ${result[i]}"
            }
            Validator.isValidURL(test[0]) == result[0]
            Validator.isValidURL(test[1]) == result[1]
            Validator.isValidURL(test[2]) == result[2]
            Validator.isValidURL(test[3]) == result[3]
            Validator.isValidURL(test[4]) == result[4]
    }
}
