package org.brewchain.cwv.game.enums;

public enum PropertyStatusEnum {
	NOSALE("0"), //未出售
	ONSALE("1"), //出售中
	BIDDING("2"), //竞拍中
	NOOWNER("9"); //未开放
	private String value;
	PropertyStatusEnum(String value){
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
