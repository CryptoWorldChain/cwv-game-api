package org.brewchain.cwv.game.enums;

public enum PropertyExchangeStatusEnum {
	ONSALE("0"),// 发起
	SOLD("1"), //已售出
	CANCEL("2"); //撤销
	private String value;
	PropertyExchangeStatusEnum(String value){
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
