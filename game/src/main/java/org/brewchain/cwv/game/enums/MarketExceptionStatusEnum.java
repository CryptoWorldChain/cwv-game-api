package org.brewchain.cwv.game.enums;

public enum MarketExceptionStatusEnum {
	HANDLE_START(0),// 买卖
	HANDLE_SUCCESS(1), //竞拍
	HANDLE(2); //抽奖
	private int value;
	MarketExceptionStatusEnum(int value){
		this.value = value;
	}
	public int getValue() {
		return value;
	}
}
