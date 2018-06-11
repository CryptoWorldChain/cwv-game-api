package org.brewchain.cwv.game.enums;

public enum PropertyBidStatusEnum {
	
	PREVIEW((byte) 0),// 预竞拍
	BIDDING((byte) 1), //竞拍中
	NOTICE((byte) 2), //公示中
	NOBID((byte) 3), //流拍
	CANCEL((byte) 4); //取消竞拍
	private byte value;
	PropertyBidStatusEnum(byte value){
		this.value = value;
	}
	public byte getValue() {
		return value;
	}
}
