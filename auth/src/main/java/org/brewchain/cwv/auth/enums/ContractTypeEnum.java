package org.brewchain.cwv.auth.enums;

public enum ContractTypeEnum {
	RANDOM_CONTRACT("1","RandomApple.sol"),
	EXCHANGE_CONTRACT("2","test.sol"),
	AUCTION_CONTRACT("3","RandomApple.sol");
	private String name;
	private String value;
	
	
	private ContractTypeEnum(String name, String value) {
		this.name = name;
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
