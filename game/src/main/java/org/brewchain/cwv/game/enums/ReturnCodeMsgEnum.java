package org.brewchain.cwv.game.enums;

/**
 * 返回代码及提示信息 枚举
 * @author Moon
 * @date 2018-03-30
 */
public enum ReturnCodeMsgEnum {
	
	
	/**
	 * 查询房产游戏详细
	 */
	PGD_ERROR_ID("02","游戏ID错误"),
	
	/**
	 * 取消房产交易
	 */
	CPE_SUCCESS("01","撤销成功"),
	CPE_ERROR_ID("02","交易ID错误"),
	CPE_ERROR_STATUS("03","交易状态不支持撤销"),
	

	/**
	 * 取消房产竞拍
	 */
	CPC_SUCCESS("01","撤销成功"),
	CPC_ERROR_ID("02","竞拍ID错误"),
	CPC_ERROR_STATUS("03","交易状态不支持撤销"),
	CPC_ERROR_PROPERTY("04","房产暂不支持竞拍"),
	CPC_ERROR_USER("05","用户暂无权限"),
	
	/**
	 * 领取房产收益
	 */
	PIC_SUCCESS("01","领取成功"),
	PIC_ERROR_ID("02","收益ID错误"),
	PIC_ERROR_STATUS("03","收益已领取，无法重复领取"),
	

	/**
	 * 查询钱包账户列表
	 */
	PDS_SUCCESS("01","抽奖成功"),
	PDS_ERROR_DRAW_COUNT("02","抽奖次数不足"),
	
	/**
	 * 抽奖房产
	 */
	WAS_SUCCESS("01","查询成功"),
	WAS_ERROR_ACCOUNT("02","账户查询失败"),
	
	/**
	 * 创建房产竞拍
	 */
	CPB_SUCCESS("01","创建成功"),
	CPB_ERROR_ID("02","房产ID错误"),
	CPB_ERROR_PROPERTY("03","房产暂不支持竞拍"),
	CPB_ERROR_USER("04","用户暂无权限"),
	CPB_ERROR_STATUS("05","房产状态错误"),
	CPB_VALIDATE_PWD_SET("06","未设置交易密码"),
	CPB_ERROR_TRADE_PWD("07","交易密码错误"),
	
	/**
	 * 查询账户余额
	 */
	WAB_SUCCESS("01","查询成功"),
	WAB_ERROR_TYPE("02","账户类型错误"),
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
	APS_VALIDATE_PRICE_LADDER("04","竞拍价格必须是单位增加量的倍数"),
	APS_ERROR_PRICE("05","竞价必须高于当前竞拍价"),
	APS_ERROR_ACCOUNT("06","账户余额不足"),
	APS_ERROR_STATUS_0("07","竞拍未开始"),
	APS_ERROR_STATUS_2("08","竞拍已结束"),
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
	BPS_ERROR_USER("07","不支持购买本人出售房产"),
	
	
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
