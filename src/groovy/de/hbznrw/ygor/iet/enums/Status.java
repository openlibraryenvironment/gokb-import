package de.hbznrw.ygor.iet.enums;

public enum Status {
	
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
	// e.g. not implemented yet
	VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE  ("VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE"),
	
	// given title seems to be valid
    VALIDATOR_STRING_IS_VALID    ("VALIDATOR_STRING_IS_VALID"),
    // given title is not valid
    VALIDATOR_STRING_IS_INVALID  ("VALIDATOR_STRING_IS_INVALID"),
    
    // given title seems to be valid
    VALIDATOR_NUMBER_IS_VALID    ("VALIDATOR_NUMBER_IS_VALID"),
    // given title is not valid
    VALIDATOR_NUMBER_IS_INVALID  ("VALIDATOR_NUMBER_IS_INVALID"),
    
    // given url seems to be valid
    VALIDATOR_URL_IS_VALID    ("VALIDATOR_URL_IS_VALID"),
    // given url is invalid
    VALIDATOR_URL_IS_INVALID  ("VALIDATOR_URL_IS_INVALID"),
    
    
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
