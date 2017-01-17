package de.hbznrw.ygor.iet.enums;

public enum Query {
	
	ZDBID ("ZDBID"),
	EZBID ("EZBID"),
	HBZID ("HBZID"),
	
    GBV_EISSN     ("GBV_EISSN"),
    GBV_PISSN     ("GBV_PISSN"),
    
	GBV_GVKPPN          ("GBV_GVKPPN"),
	GBV_TITLE           ("GBV_TITLE"),
    GBV_PUBLISHER       ("GBV_PUBLISHER"),
    GBV_PUBLISHED_FROM  ("GBV_PUBLISHED_FROM"),
    GBV_PUBLISHED_TO    ("GBV_PUBLISHED_TO"),
    GBV_TIPP_URL        ("GBV_TIPP_URL"),
    GBV_PLATFORM_URL    ("GBV_PLATFORM_URL"),
        
    GBV_TIPP_COVERAGE   ("GBV_TIPP_COVERAGE"),
    
	ZDB_TITLE     ("ZDB_TITLE"),     // old
	ZDB_PUBLISHER ("ZDB_PUBLISHER")  // old
	;
	
	private String value;
	
	Query(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
}
