package org.brewchain.cwv.game.enums;

public enum UpAndDownEnum {
	DOWN(-1),
	NOCHANGE(0),
	UP(1);
	private int value;
	UpAndDownEnum(int value){
		this.value = value;		
	}
	public int getValue() {
		return value;
	}
}
