package org.brewchain.cwv.game.enums;

public enum PropertyIncomeTypeEnum {
	DIVIDED("1"),// 分红
	MINING("2"),// 挖矿
	TAX("3"); // 税收
	private String value;
	PropertyIncomeTypeEnum(String value){
		this.value = value;
	}
	public String getValue() {
		return value;
	}
}
