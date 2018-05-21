package org.brewchain.cwv.game.enums;

public enum PropertyIncomeStatusEnum {
	NEW("0"),// 未领取
	CLAIMED("1"); //已领取
	private String value;
	PropertyIncomeStatusEnum(String value){
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
