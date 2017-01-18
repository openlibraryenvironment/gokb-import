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
	VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE  ("VALIDATOR_IDENTIFIER_IN_UNKNOWN_STATE")
	
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
