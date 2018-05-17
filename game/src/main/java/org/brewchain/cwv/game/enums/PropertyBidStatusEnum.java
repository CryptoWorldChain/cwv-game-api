package org.brewchain.cwv.game.enums;

public enum PropertyBidStatusEnum {
	PREVIEW("0"),// 预竞拍
	BIDDING("1"), //竞拍中
	NOTICE("2"), //公示中
	NOBID("3"); //流拍
	private String value;
	PropertyBidStatusEnum(String value){
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
