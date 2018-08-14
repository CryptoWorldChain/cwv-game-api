package org.brewchain.cwv.auth.enums;

public enum PropertyTypeEnum {
	TYPICAL("1"), // 标志性房产
	ORDINARY("2"), //价值性房产
	FUNCTIONAL("3"); //功能性房产
	private String value;
	PropertyTypeEnum(String value){
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
