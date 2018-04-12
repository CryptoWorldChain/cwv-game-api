package org.brewchain.cwv.game.enums;

import lombok.Data;

/**
 * 返回代码及提示信息 枚举
 * @author Moon
 * @date 2018-03-30
 */
public enum ReturnCodeMsgEnum {
	

	/**
	 * 查询钱包账户列表
	 */
	WAS_SUCCESS("01","查询成功"),
	
	
	/**
	 * 发起竞价
	 */
	APS_SUCCESS("01","竞价成功"),
	APS_VALIDATE_ID("02","竞拍房产ID错误"),
	APS_VALIDATE_PRICE("03","价格错误"),
	APS_ERROR_PRICE("04","竞价必须高于当前竞拍价"),
	APS_ERROR_ACCOUNT("05","账户余额不足"),
	APS_ERROR_STATUS("06","竞拍已结束"),

	/**
	 * 查询竞拍房产
	 */
	PBS_SUCCESS("01","查询成功"),
	
	/**
	 * 查询房产竞价记录
	 */
	PBA_SUCCESS("01","查询成功"),
	
	/**
	 * 查询房产交易
	 */
	PES_SUCCESS("01","查询成功"),
	
	/**
	 * 购买房产交易
	 */
	BPS_SUCCESS("01","购买成功"),
	BPS_ERROR_ID("02","交易ID不能为空"),
	BPS_ERROR_AMOUNT("03","买入金额错误"),
	BPS_ERROR_STATUS("04","该交易被成交或撤销"),
	
	
	/**
	 * 卖出房产（创建房产交易）
	 */
	SPS_SUCCESS("01","购买成功"),
	SPS_VALIDATE_ID("02","房产ID格式错误"),
	SPS_VALIDATE_PRICE("03","房产价格式错误"),
	SPS_VALIDATE_TAX("04","手续费格式错误"),
	
	/**
	 * 取消房产交易
	 */
	CPT_SUCCESS("01","取消成功"),
	CPT_VALIDATE_ID("02","交易ID格式错误"),
	
	/**
	 * 公共信息
	 */
	SUCCESS("01","成功"),
	SUCCESS_OPERATE("01","设置成功"),
	ERROR_TIMES_LIMIT("10","次数超限"),
	ERROR_VALIDATION("80","格式错误"),
	EXCEPTION("99","未知异常")
	;
	
	private String retCode;
	private String retMsg;
	private ReturnCodeMsgEnum(String retCode, String retMsg) {
		this.retCode = retCode;
		this.retMsg = retMsg;
	}
	public String getRetCode() {
		return retCode;
	}
	public void setRetCode(String retCode) {
		this.retCode = retCode;
	}
	public String getRetMsg() {
		return retMsg;
	}
	public void setRetMsg(String retMsg) {
		this.retMsg = retMsg;
	}
	

}
