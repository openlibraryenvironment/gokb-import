package de.hbznrw.ygor.export

import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class StructValidatorSpec extends Specification {

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

  void "isValidCoverage(TippCoverage coverage)"() {

    given:
    println "UNIT TEST NOT IMPLEMENTED"
    expect:
    1 == 1

    /*
    def coverage1 = PackageStruct.getNewTippCoverage()
    coverage1.startDate   = new Pod("some_string", Status.VALIDATOR_DATE_IS_INVALID)
    coverage1.endDate     = new Pod("some_string", Status.VALIDATOR_DATE_IS_INVALID)
    coverage1.startVolume = new Pod("11",  Status.VALIDATOR_NUMBER_IS_VALID)
    coverage1.endVolume   = new Pod("11",  Status.VALIDATOR_NUMBER_IS_VALID)

    def coverage2 = PackageStruct.getNewTippCoverage()
    coverage2.startDate   = new Pod("some_string", Status.VALIDATOR_DATE_IS_INVALID)
    coverage2.endDate     = new Pod("some_string", Status.VALIDATOR_DATE_IS_INVALID)
    coverage2.startVolume = new Pod("5",   Status.VALIDATOR_NUMBER_IS_VALID)
    coverage2.endVolume   = new Pod("22",  Status.VALIDATOR_NUMBER_IS_VALID)

    def coverage3 = PackageStruct.getNewTippCoverage()
    coverage3.startDate   = new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID)
    coverage3.endDate     = new Pod("2012-02-05 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID)
    coverage3.startVolume = new Pod("",  Status.VALIDATOR_NUMBER_IS_INVALID)
    coverage3.endVolume   = new Pod("",  Status.VALIDATOR_NUMBER_IS_INVALID)

    def coverage4 = PackageStruct.getNewTippCoverage()
    coverage4.startDate   = new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID)
    coverage4.endDate     = new Pod("2012-02-05 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID)
    coverage4.startVolume = new Pod("3",  Status.VALIDATOR_NUMBER_IS_VALID)
    coverage4.endVolume   = new Pod("6",  Status.VALIDATOR_NUMBER_IS_VALID)

    def coverage5 = PackageStruct.getNewTippCoverage()
    coverage5.startDate   = new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID)
    coverage5.endDate     = new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID)
    coverage5.startVolume = new Pod("1",  Status.VALIDATOR_NUMBER_IS_VALID)
    coverage5.endVolume   = new Pod("1",  Status.VALIDATOR_NUMBER_IS_VALID)

    def coverage6 = PackageStruct.getNewTippCoverage()
    coverage6.startDate   = new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID)
    coverage6.endDate     = new Pod("2012-01-01 00:00:00.000", Status.VALIDATOR_DATE_IS_VALID)
    coverage6.startVolume = new Pod("5",    Status.VALIDATOR_NUMBER_IS_VALID)
    coverage6.endVolume   = new Pod("101",  Status.VALIDATOR_NUMBER_IS_VALID)

    given:
        List<TippCoverage> test = [
            coverage1,
            coverage2,
            coverage3,
            coverage4,
            coverage5,
            coverage6
        ]
        def result = [
            Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID,
            Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID,
            Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID,
            Status.STRUCTVALIDATOR_COVERAGE_IS_VALID,
            Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID,
            Status.STRUCTVALIDATOR_COVERAGE_IS_INVALID
        ]

    expect:
        test.eachWithIndex{e, i ->
            println "${test[i].startDate.v} - ${test[i].endDate.v}, Vol: ${test[i].startVolume.v} - ${test[i].endVolume.v} -> ${result[i]}"
        }
        StructValidator.isValidCoverage(test.get(0)) == result[0]
        StructValidator.isValidCoverage(test.get(1)) == result[1]
        StructValidator.isValidCoverage(test.get(2)) == result[2]
        StructValidator.isValidCoverage(test.get(3)) == result[3]
        StructValidator.isValidCoverage(test.get(4)) == result[4]
        StructValidator.isValidCoverage(test.get(5)) == result[5]
    */
  }

  void "isValidHistoryEvent(TitleHistoryEvent historyEvent)"() {

    given:
    println "UNIT TEST NOT IMPLEMENTED"
    expect:
    1 == 1
  }

  void "isValidPublisherHistory(TitlePublisherHistory publisherHistory)"() {

    given:
    println "UNIT TEST NOT IMPLEMENTED"
    expect:
    1 == 1
  }
}
