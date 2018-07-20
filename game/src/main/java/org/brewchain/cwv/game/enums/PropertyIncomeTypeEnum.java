package org.brewchain.cwv.game.enums;

public enum PropertyIncomeTypeEnum {
	DIVIDED((byte)1),// 分红
	MINING((byte)2),// 挖矿
	TAX((byte)3); // 税收
	private byte value;
	PropertyIncomeTypeEnum(byte value){
		this.value = value;
	}
	public byte getValue() {
		return value;
	}
}
