package org.brewchain.cwv.game.enums;

public enum CoinEnum{
	CWB(0),
	CWV(1),
	ETH(2);
	private int value;
	CoinEnum(int value){
		this.value = value;		
	}
	public int getValue() {
		return value;
	}
	
}
