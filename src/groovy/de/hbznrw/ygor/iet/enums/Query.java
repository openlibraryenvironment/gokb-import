package de.hbznrw.ygor.iet.enums;

public enum Query {
	
	ZDBID ("ZDBID"),
	EZBID ("EZBID"),
	HBZID ("HBZID");
	
	private String value;
	
	Query(String value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value;
	}
}
