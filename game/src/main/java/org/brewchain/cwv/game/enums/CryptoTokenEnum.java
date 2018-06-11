package org.brewchain.cwv.game.enums;

public enum CryptoTokenEnum{
	CYT_HOUSE("house");
	private String value;
	CryptoTokenEnum(String value){
		this.value = value;		
	}
	public String getValue() {
		return value;
	}
	
}
