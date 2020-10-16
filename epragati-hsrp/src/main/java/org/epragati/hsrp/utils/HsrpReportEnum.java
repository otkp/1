package org.epragati.hsrp.utils;

public enum HsrpReportEnum {
	TRFAILLIST(1,"TRFAILEDLIST"),PRFAILEDLIST(2,"PRFAILEDLIST"),INITIALFAILED(3,"INITIALFAILED");
	
	private Integer code;
	private String value;
	
	HsrpReportEnum(Integer code,String value) {
		this.value=value;
		this.code=code;
	}
	public Integer getCode() {
		return code;
	}
	public void setCode(Integer code) {
		this.code = code;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
	
}
