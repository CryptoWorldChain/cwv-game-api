package org.brewchain.cwv.game.enums;

public enum GameStatusEnum{
	NEW(0),
	HOT(1);
	private int value;
	GameStatusEnum(int value){
		this.value = value;		
	}
	public int getValue() {
		return value;
	}
	
}
