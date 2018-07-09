package org.brewchain.cwv.game.enums;

public enum TransHashTypeEnum {
	EXCHANGE_BUY( "exchange_buy"),// 
	EXCHANGE_BUY_GROUP( "exchange_buy_group"),// 
	EXCHANGE_BUY_ROLLBACK( "exchange_buy_rollback"),//
	EXCHANGE_BUY_ROLLBACK_GROUP( "exchange_buy_rollback_group"),//
	
	EXCHANGE_SELL( "exchange_sell"),//
	EXCHANGE_SELL_ROLLBACK( "exchange_sell_rollback"),//
	
	BID_ACUTION( "bid_acution"),//
	BID_ACUTION_ROLLBACK( "bid_acution_rollback"),//

	DRAW("draw"),//
	DRAW_ROLLBACK("draw_rollback");
	
	private String value;
	TransHashTypeEnum(String value){
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
}
