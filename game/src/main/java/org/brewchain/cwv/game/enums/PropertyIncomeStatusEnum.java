package org.brewchain.cwv.game.enums;

public enum PropertyIncomeStatusEnum {
	NEW((byte)0),// 未领取
	CLAIMED((byte)1),//已领取
	EXCEPTION((byte)2);// 异常 
	private byte value;
	PropertyIncomeStatusEnum(byte value){
		this.value = value;
	}
	public byte getValue() {
		return value;
	}
}
