package org.brewchain.cwv.game.enums;

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
	WAS_ERROR_ACCOUNT("02","账户查询失败"),
	
	/**
	 * 竞拍详情
	 */
	
	PBD_SUCCESS("01","查询成功"),
	PBD_ERROR_ID("02","查询竞拍ID错误"),
	PBD_ERROR_STATUS("03","房产竞拍状态错误"),
	
	/**
	 * 公示详情
	 */
	
	PBN_SUCCESS("01","查询成功"),
	PBN_ERROR_ID("02","查询竞拍ID错误"),
	PBN_ERROR_STATUS("03","房产竞拍状态错误"),
	
	/**
	 * 发起竞价
	 */
	APS_SUCCESS("01","竞价成功"),
	APS_VALIDATE_ID("02","竞拍房产ID错误"),
	APS_VALIDATE_PRICE("03","价格错误"),
	APS_VALIDATE_PRICE_LADDER("04","竞拍价格必须是增加量的倍数"),
	APS_ERROR_PRICE("05","竞价必须高于当前竞拍价"),
	APS_ERROR_ACCOUNT("06","账户余额不足"),
	APS_ERROR_STATUS_0("07","竞拍未开始"),
	APS_ERROR_STATUS_2("08","竞拍已结束"),
	APS_FALURE("09","竞价失败"),
	APS_ERROR_TIME("10","竞拍已结束"),
	

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
	BPS_ERROR_ID("02","交易ID错误"),
	BPS_ERROR_BALANCE("03","CWB不足"),
	BPS_ERROR_TRADE_PWD("04","交易密码错误"),
	BPS_VALIDATE_PWD_SET("05","未设置交易密码"),
	BPS_ERROR_STATUS("06","该交易被成交或撤销"),
	
	
	/**
	 * 卖出房产（创建房产交易）
	 */
	SPS_SUCCESS("01","卖出成功"),
	SPS_ERROR_ID("02","房产ID错误"),
	SPS_ERROR_STATUS("03","房产状态错误"),
	SPS_VALIDATE_PRICE("04","房产价格式错误"),
	SPS_ERROR_TRADEPWD("05","交易密码错误"),
	SPS_VALIDATE_PWD_SET("06","未设置交易密码"),
	
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
