package de.hbznrw.ygor.iet.enums;

public enum Status {
	
    /*
     * USE: 
     * 
     * title.m.equals(Status.VALIDATOR_STRING_IS_VALID)
     * tipp.url.m == Status.VALIDATOR_URL_IS_VALID
     *  
     * NOT:
     * 
     * title.m.equals(Status.VALIDATOR_STRING_IS_VALID.toString()
     * tipp.url.m == Status.VALIDATOR_URL_IS_VALID.toString()
     *  
     */
    
	UNKNOWN_REQUEST    ("UNKNOWN_REQUEST"),
	
	STATUS_OK          ("STATUS_OK"),
	STATUS_ERROR       ("STATUS_ERROR"),
	STATUS_NO_RESPONSE ("STATUS_NO_RESPONSE"),   
	
   // api responses
    
    RESULT_OK               ("RESULT_OK"),
    RESULT_NO_MATCH         ("RESULT_NO_MATCH"),
    RESULT_MULTIPLE_MATCHES ("RESULT_MULTIPLE_MATCHES"),
	
    
    // use for the non processed
    
	// default value
	UNDEFINED          ("UNDEFINED"),
	// hardcoded values
	HARDCODED          ("HARDCODED"),
	// constants, tmp vars, etc
	IGNORE             ("IGNORE"),
	
	// after validator check
	
	// use for valid date values
	VALIDATOR_DATE_IS_VALID    ("VALIDATOR_DATE_IS_VALID"),
	// use for non conform date values
	VALIDATOR_DATE_IS_INVALID  ("VALIDATOR_DATE_IS_INVALID"),
	// no value given
	VALIDATOR_DATE_IS_MISSING  ("VALIDATOR_DATE_IS_MISSING"),
	
	// given identifier seems to be valid
	VALIDATOR_IDENTIFIER_IS_VALID          ("VALIDATOR_IDENTIFIER_IS_VALID"),
	// given identifier is not valid
	VALIDATOR_IDENTIFIER_IS_INVALID        ("VALIDATOR_IDENTIFIER_IS_INVALID"),
	// given identifier seems to contain multiple matches
	VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC     ("VALIDATOR_IDENTIFIER_IS_NOT_ATOMIC"),
	// no given identifier
	VALIDATOR_IDENTIFIER_IS_MISSING        ("VALIDATOR_IDENTIFIER_IS_MISSING"),
	// e.g. not implemented yet
    VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE  ("VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE"),
	
	// given title seems to be valid
    VALIDATOR_STRING_IS_VALID       ("VALIDATOR_STRING_IS_VALID"),
    // given title is given but not valid
    VALIDATOR_STRING_IS_INVALID     ("VALIDATOR_STRING_IS_INVALID"),
    // given title seems to contain multiple matches
    VALIDATOR_STRING_IS_NOT_ATOMIC  ("VALIDATOR_STRING_IS_NOT_ATOMIC"),
    // no given title 
    VALIDATOR_STRING_IS_MISSING     ("VALIDATOR_STRING_IS_MISSING"),    
    
    // given url seems to be valid
    VALIDATOR_URL_IS_VALID      ("VALIDATOR_URL_IS_VALID"),
    // given url is invalid
    VALIDATOR_URL_IS_INVALID    ("VALIDATOR_URL_IS_INVALID"),
    // given url seems to contain multiple matches
    VALIDATOR_URL_IS_NOT_ATOMIC ("VALIDATOR_URL_IS_NOT_ATOMIC"),
    // no given url
    VALIDATOR_URL_IS_MISSING    ("VALIDATOR_URL_IS_MISSING"),
    
    // given number seems to be valid
    VALIDATOR_NUMBER_IS_VALID       ("VALIDATOR_NUMBER_IS_VALID"),
    // given number is not valid
    VALIDATOR_NUMBER_IS_INVALID     ("VALIDATOR_NUMBER_IS_INVALID"),
    // given number seems to contain multiple matches
    VALIDATOR_NUMBER_IS_NOT_ATOMIC  ("VALIDATOR_NUMBER_IS_NOT_ATOMIC"),
    // no given number
    VALIDATOR_NUMBER_IS_MISSING     ("VALIDATOR_NUMBER_IS_MISSING"),
    
    
    // given coverage seems to be valid
    VALIDATOR_COVERAGE_IS_VALID    ("VALIDATOR_COVERAGE_IS_VALID"),
    // given coverage is invalid
    VALIDATOR_COVERAGE_IS_INVALID  ("VALIDATOR_COVERAGE_IS_INVALID"),
    
    // given history event seems to be valid
    VALIDATOR_HISTORYEVENT_IS_VALID    ("VALIDATOR_HISTORYEVENT_IS_VALID"),
    // given history event is invalid
    VALIDATOR_HISTORYEVENT_IS_INVALID  ("VALIDATOR_HISTORYEVENT_IS_INVALID")
    
	;

	private String value;
	
	Status(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
}
