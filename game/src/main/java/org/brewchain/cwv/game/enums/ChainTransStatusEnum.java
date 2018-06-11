package org.brewchain.cwv.game.enums;

public enum ChainTransStatusEnum {
	START( (byte) 0,"start"),// 
	DONE( (byte) 1,"done"),// 预竞拍
	ERROR( (byte) 2,"error");
	
	private byte key;
	private String value;
	ChainTransStatusEnum(byte key,String value){
		this.value = value;
		this.key = key;
	}
	public byte getKey() {
		return key;
	}
	public String getValue() {
		return value;
	}
}
