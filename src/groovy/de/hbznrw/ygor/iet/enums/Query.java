package de.hbznrw.ygor.iet.enums;

public enum Query {
	
	ZDBID ("ZDBID"),
	EZBID ("EZBID"),
	HBZID ("HBZID"),
	
	GBVTITLE     ("GBVTITLE"),
    GBVPUBLISHER ("GBVPUBLISHER"),
    GBVEISSN     ("GBVEISSN"),
    GBVPISSN     ("GBVPISSN"),

	ZDBTITLE     ("ZDBTITLE"),     // old
	ZDBPUBLISHER ("ZDBPUBLISHER")  // old
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
