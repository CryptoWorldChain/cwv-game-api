package org.brewchain.cwv.game.enums;

public enum MarketTypeEnum {
	EXCHANGE((byte) 0),// 买卖
	BID((byte) 1), //竞拍
	DRAW((byte) 2), //抽奖
	INCOME((byte) 3), //抽奖
	RECHARGE((byte) 4); //充值
	private byte value;
	MarketTypeEnum(byte value){
		this.value = value;
	}
	public byte getValue() {
		return value;
	}
}
