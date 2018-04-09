package org.brewchain.cwv.game.enums;

import lombok.Data;

/**
 * 返回代码及提示信息 枚举
 * @author Moon
 * @date 2018-03-30
 */
public enum ReturnCodeMsgEnum {
	
	
	
	/**
	 * 查询房产交易
	 */
	GPT_SUCCESS("01","查询成功"),
	
	/**
	 * 购买房产交易
	 */
	BPT_SUCCESS("01","购买成功"),
	BPT_ERROR_ID("02","交易ID不能为空"),
	
	/**
	 * 卖出房产（创建房产交易）
	 */
	SPT_SUCCESS("01","购买成功"),
	SPT_VALIDATE_ID("02","房产ID格式错误"),
	SPT_VALIDATE_PRICE("03","房产价格式错误"),
	SPT_VALIDATE_TAX("04","手续费格式错误"),
	
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
