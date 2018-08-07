package org.brewchain.cwv.game.chain;
/**
 * 执行合约竞价返回结果
 * @author Moon
 * @date 2018-08-06
 */
public enum BidAuctionRetEnum {
	AUCTION_BID_SUCCESS(0,"成功"),
	AUCTION_BID_ERROR_CREATER(1,"合约创建者不能参与竞拍"),
	AUCTION_BID_ERROR_TIME_START(2,"未到竞拍开始时间"),
	AUCTION_BID_IS_END(3,"竞拍已结束"),
	AUCTION_BID_ERROR_PRICE(4,"竞拍价小于当前最高价"),
	AUCTION_BID_ERROR_BALANCE(5,"竞拍用户的余额不足"),
	AUCTION_BID_ERROR_PRICE_INCREASE(6,"低于竞价幅度值"),
	AUCTION_END_CREATER_ONLY(7,"合约创建者调用结束方法"),
	AUCTION_END_BEFORE_END(8,"竞拍未结束"),
	AUCTION_END_INVOKED(9,"已经调用过竞拍结束方法");
	private long retCode;
	private String retMsg;
	private BidAuctionRetEnum(long retCode, String retMsg) {
		this.retCode = retCode;
		this.retMsg = retMsg;
	}
	public long getRetCode() {
		return retCode;
	}
	public void setRetCode(long retCode) {
		this.retCode = retCode;
	}
	public String getRetMsg() {
		return retMsg;
	}
	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}

	public static String getValue(long key){
		for(BidAuctionRetEnum e : BidAuctionRetEnum.values()) {
			if(e.retCode == key)
				return e.retMsg;
		}
		return "";
	}
}
