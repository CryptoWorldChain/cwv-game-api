package org.brewchain.cwv.game.enums;

public enum TransactionTypeEnum {
	/**
	 * 
	 * 01 market
	 * 0101 exchange
	 * 010101 sell
	 * 010102 sell_cancel
	 * 010103 buy_amount
	 * 01010301 buy_amount_rollback
	 * 010104 buy_group
	 * 0102 bid
	 * 010201 bid_create
	 * 010202 bid_create_cancel
	 * 010203 bid_auction
	 * 0103 draw
	 * 010301 draw_random
	 * 010302 draw_group
	 * 02 property
	 * 0201 income
	 * 020101 income_address
	 * 020102 wallet_address
	 * @return 
	 */
	
	MARKET("01","market"),
	EXCHANGE("0101","exchange"),
	EXCHANGE_SELL("010101","exchange_sell"),//√
	EXCHANGE_SELL_CANCEL("010102","exchange_sell_cancel"),//√
	EXCHANGE_BUY_AMOUNT("010103","exchange_buy_amount"),//√
	EXCHANGE_BUY_AMOUNT_ROLLBACK("01010301","exchange_buy_amount_rollback"),
	EXCHANGE_BUY_GROUP("010104","exchange_buy_group"),//√
	BID("0102","bid"),
	BID_CREATE("010201","bid_create"),//√
	BID_CREATE_CANCEL("010202","bid_create_cancel"),//√ 没有status
	BID_AUCTION("010203","bid_auction"),//√ 没有status
	DRAW("0103","draw"),
	DRAW_RANDOM("010301","draw_random"),//√ 没有status
	DRAW_GROUP("010302","draw_group"),//√
	PROPERTY("02","property"),
	INCOME("0201","income"),
	INCOME_CREATE("020101","income_create"),//√
	INCOME_CLAIM("020102","income_claim"),//√
	CONTRACT_CREATE("0301","contract_create");
	private final String key;
	private final String value;
	
	private TransactionTypeEnum(String key, String value) {
		this.key = key;
		this.value = value;
	}
	public String getKey() {
		return key;
	}
	public String getValue() {
		return value;
	}
	

}
