package de.hbznrw.ygor.export

import de.hbznrw.ygor.bridges.ZdbBridge
import de.hbznrw.ygor.export.structure.TitleStruct
import de.hbznrw.ygor.normalizers.*
import grails.test.mixin.TestMixin
import grails.test.mixin.support.GrailsUnitTestMixin
import spock.lang.Specification

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
            StringNormalizer.normalizeString(raw, false) == result
        
        where:
            raw                                     | result
            null                                    | null
            ""                                      | ""
            "  Trim  "                              | "Trim"
            "Remo     ving Multi  ple  Spa    ces"  | "Remo ving Multi ple Spa ces"
            "Reformatting - a : x  ,y :z"           | "Reformatting - a: x, y: z"               
    }

    void "normStringTitle(String str)"() {

        when:
        println "${raw} -> ${result}"

        then:
        StringNormalizer.normalizeStringTitle(raw) == result

        where:
        raw                                     | result
        null                                    | null
        "Remain@remain"                         | "Remain@remain"
        "Remain @ remain"                       | "Remain @ remain"
        "Remain@ remain"                        | "Remain@ remain"
        "Trim @trim"                            | "Trim trim"

    }

    void "normString(ArrayList list)"() {
        
        when:
            println "${raw} -> ${result}"
        
        then:
            StringNormalizer.normalizeString(raw, false) == result
        
        where:
            raw                                     | result
            [null, null, null]                      | "null|null|null"
            [null, "value1", "value2"]              | "null|value1|value2"
            ["a", "b", "c", "d"]                    | "a|b|c|d"
            ["x"]                                   | "x"
            []                                      | ""
            ((String)null)                          | null
            ""                                      | ""
    }
       
    void "normIdentifier(String str, Object type)"() {
        
        when:
            println "${raw[0]}, ${raw[1]} -> ${result}"
        
        then:
            IdentifierNormalizer.normIdentifier(raw[0], raw[1], DataMapper.IDENTIFIER_NAMESPACES[0]) == result
        
        where:
            raw                                 | result
            ["12345678", TitleStruct.EISSN]     | "1234-5678"
            ["1234/5678", TitleStruct.EISSN]    | "1234-5678"
            ["1234567", TitleStruct.EISSN]      | "1234567"
            ["123456789", TitleStruct.EISSN]    | "123456789"
            ["33445XXX", TitleStruct.PISSN]     | "3344-5XXX"
            ["3344--YYYY", TitleStruct.PISSN]   | "3344-YYYY"
            ["1234-567X", ZdbBridge.IDENTIFIER] | "1234567-X"
            ["12345", ZdbBridge.IDENTIFIER]     | "1234-5"
            [null, null]                        | null
            ["", ""]                            | ""
            
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

            IdentifierNormalizer.normIdentifier(list1, TitleStruct.EISSN, DataMapper.IDENTIFIER_NAMESPACES[0]) == result1
            IdentifierNormalizer.normIdentifier(list2, TitleStruct.PISSN, DataMapper.IDENTIFIER_NAMESPACES[0]) == result2
    }   
    
    void "normDate(String str, Object dateType)"() {
        
        when:
            println "${raw}, ${DateNormalizer.START_DATE} -> ${resultStartDate}"
            println "${raw}, ${DateNormalizer.END_DATE} -> ${resultEndDate}"
            
        then:
            DateNormalizer.normalizeDate(raw, DateNormalizer.START_DATE) == resultStartDate
            DateNormalizer.normalizeDate(raw, DateNormalizer.END_DATE) == resultEndDate
        
        where:
            raw                                 | resultStartDate               | resultEndDate
            "2008"                              | "2008-01-01 00:00:00.000"     | "2008-12-31 23:59:59.000"
            "2010,2"                            | "2010-02-01 00:00:00.000"     | "2010-02-28 23:59:59.000"
            "4.2010"                            | "2010-01-01 00:00:00.000"     | "2010-12-31 23:59:59.000"
            "8.2010,6"                          | "2010-06-01 00:00:00.000"     | "2010-06-30 23:59:59.000"
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
            "Verlag; 17.2012,6 -"               | "2012-06-01 00:00:00.000"     | ""
            "Verlag; -17.2012,6"                | ""                            | "2012-06-30 23:59:59.000" 
            null                                | null                          | null
            ""                                  | ""                            | ""
            "1977, 5"                           | "1977-05-01 00:00:00.000"     | "1977-05-31 23:59:59.000"
            "Verlag; 1981 - 1995"               | "1981-01-01 00:00:00.000"     | "1995-12-31 23:59:59.000"
            "Verlag; 1.1981/82 -"               | "1981-01-01 00:00:00.000"     | ""
            "Verlag; 4.2010,2 - 10.2016"        | "2010-02-01 00:00:00.000"     | "2016-12-31 23:59:59.000"
            "Verlag; 4.2010 - 10.2016,2"        | "2010-01-01 00:00:00.000"     | "2016-02-29 23:59:59.000"
            "Verlag; 4.2011,6 - 10.2017,2"      | "2011-06-01 00:00:00.000"     | "2017-02-28 23:59:59.000"
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
            DateNormalizer.normalizeDate(list1, DateNormalizer.START_DATE) == resultStartDate1
            DateNormalizer.normalizeDate(list2, DateNormalizer.START_DATE) == resultStartDate2
            
            println "${list1} -> END_DATE: ${resultEndDate1}"
            println "${list2} -> END_DATE: ${resultEndDate2}"
            DateNormalizer.normalizeDate(list1, DateNormalizer.END_DATE)   == resultEndDate1
            DateNormalizer.normalizeDate(list2, DateNormalizer.END_DATE)   == resultEndDate2
    }  
    
    void "normCoverageVolume(String str, Object dateType)"() {
        
        when:
            println "${raw}, ${DateNormalizer.START_DATE} -> ${resultStartVol}"
            println "${raw}, ${DateNormalizer.END_DATE} -> ${resultEndVol}"
            
        then:
            CoverageNormalizer.normalizeCoverageVolume(raw, DateNormalizer.START_DATE) == resultStartVol
            CoverageNormalizer.normalizeCoverageVolume(raw, DateNormalizer.END_DATE) == resultEndVol
        
        where:
            raw                                     | resultStartVol    | resultEndVol
            "18.2005 - 27.2014"                     | "18"              | "27"
            "Verlag; 18.2005 - 27.2014"             | "18"              | "27"
            "22.2022"                               | "22"              | "22"
            "05.1995 - "                            | "05"              | ""
            "Verlag; 1.1981/82 -"                   | "1"               | ""
            "Verlag; 1.1971 - 38.2001/02"           | "1"               | "38"
            "1997, 1"                               | ""                | ""
            null                                    | null              | null
            
    }
    
    void "normURL(String str, boolean onlyAuthority) "() {
        
        when:
            println "${raw} & ${onlyAuthority} -> ${result}"
            
        then:
            UrlNormalizer.normURL(raw, onlyAuthority) == result
            
        where:
            raw                                             | onlyAuthority     | result
            "https://google.de/"                            | true              | "https://google.de"
            "https://google.de/"                            | false             | "https://google.de/"
            "http://yahoo.de?q=laser"                       | true              | "http://yahoo.de"
            "http://yahoo.de?q=laser"                       | false             | "http://yahoo.de?q=laser"
            "https://google.de/this/and/that"               | true              | "https://google.de"
            "https://google.de/this/and/that"               | false             | "https://google.de/this/and/that"
            "http://laser.hbz-nrw.de:8080/and/some/more/"   | true              | "http://laser.hbz-nrw.de:8080"
            "http://laser.hbz-nrw.de:8080/and/some/more/"   | false             | "http://laser.hbz-nrw.de:8080/and/some/more/"
            "golem.de"                                      | true              | "http://golem.de"
            "golem.de"                                      | false             | "http://golem.de"
            ""                                              | true              | ""
            ""                                              | false             | ""
            null                                            | true              | null         
            null                                            | false             | null
    }
    
    void "normURL(ArrayList list, boolean onlyAuthority)"() {
        
        when:
            println "${raw} & ${onlyAuthority} -> ${result}"
            
        then:
            UrlNormalizer.normURL(raw, onlyAuthority) == result
            
        where:
        raw                                                                     | onlyAuthority | result
      
            ["http://google.de/", null, "yahoo.de?q=laser"]                     | true          | "http://google.de|null|http://yahoo.de"
            ["http://google.de/", null, "yahoo.de?q=laser"]                     | false         | "http://google.de/|null|http://yahoo.de?q=laser"
            ["https://google.de/", "http://google.com/", "ftp://yahoo.de?q=1"]  | true          | "https://google.de|http://google.com|ftp://yahoo.de"
            ["https://google.de/", "http://google.com/", "ftp://yahoo.de?q=1"]  | false         | "https://google.de/|http://google.com/|ftp://yahoo.de?q=1"
    }

    void "parseDate(String str, Object dateType)"() {
        
        when:
            println "${raw}, ${DateNormalizer.START_DATE} -> ${resultStartDate}"
            println "${raw}, ${DateNormalizer.END_DATE} -> ${resultEndDate}"
        

        then:
            DateNormalizer.parseDate(raw, DateNormalizer.START_DATE) == resultStartDate
            DateNormalizer.parseDate(raw, DateNormalizer.END_DATE) == resultEndDate
    
        where:
            raw                                     | resultStartDate | resultEndDate
            "05"                                    | ["2005", null]  | ["2005", null]
            "2022"                                  | ["2022", null]  | ["2022", null]
            "2022,5"                                | ["2022", "5"]   | ["2022", "5"]
            "2010/11"                               | ["2010", null]  | ["2011", null]
            "10/11"                                 | ["2010", null]  | ["2011", null]
            "15-16"                                 | ["2015", null]  | ["2016", null]
            "1997-1998"                             | ["1997", null]  | ["1998", null]
            "1991/1992"                             | ["1991", null]  | ["1992", null]
            "1991,11/1992"                          | ["1991", "11"]  | ["1992", null]
            "1997-1998, 2"                          | ["1997", null]  | ["1998", "2"]
            "1997,5-1998,2"                         | ["1997", "5"]   | ["1998", "2"]
            //"11.2005,5-7.2008,2"                    | ["2005", "5"]   | ["2008", "2"]
            "1991,5/1992"                           | ["1991", "5"]   | ["1992", null]
            "1.1981/82"                             | ["1981", null]  | ["1982", null]
    }
    
    void "parseCoverageVolume(String str)"() {
        
        when:
            println "${raw} -> ${result}"
        

        then:
            CoverageNormalizer.parseCoverageVolume(raw) == result
    
        where:
            raw                                     | result
            "22.2022"                               | "22"
            "23. 2022"                              | "23"
            "19. 1977,8"                            | "19"
            "1997,1"                                | ""
            "1997, 2"                               | ""
    }
    
}
