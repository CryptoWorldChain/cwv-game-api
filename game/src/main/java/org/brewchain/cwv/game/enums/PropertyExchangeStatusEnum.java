package org.brewchain.cwv.game.enums;

public enum PropertyExchangeStatusEnum {
	ONSALE((byte) 0),// 发起
	SOLD((byte) 1), //已售出
	CANCEL((byte) 2); //撤销
	private byte value;
	PropertyExchangeStatusEnum(byte value){
		this.value = value;
	}
	public byte getValue() {
		return value;
	}
}
