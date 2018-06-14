package de.hbznrw.ygor.enums;

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
    GBV_HISTORY_EVENTS  ("GBV_HISTORY_EVENTS"),
    
    KBART_TIPP_URL      ("KBART_TIPP_URL"), 
    KBART_TIPP_COVERAGE ("KBART_TIPP_COVERAGE"),
    KBART_TIPP_ACCESS   ("KBART_TIPP_ACCESS"),
    
	ZDB_TITLE     ("ZDB_TITLE"),
	ZDB_PUBLISHER ("ZDB_PUBLISHER"),

	ZDB_EISSN     ("ZDB_EISSN"),
	ZDB_PISSN     ("ZDB_PISSN"),

	ZDB_GVKPPN          ("ZDB_GVKPPN"),
	ZDB_PUBLISHED_FROM  ("ZDB_PUBLISHED_FROM"),
	ZDB_PUBLISHED_TO    ("ZDB_PUBLISHED_TO"),
	ZDB_HISTORY_EVENTS  ("ZDB_HISTORY_EVENTS"),


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
