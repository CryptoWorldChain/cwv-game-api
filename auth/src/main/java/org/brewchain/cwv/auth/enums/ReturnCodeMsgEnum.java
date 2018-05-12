package org.brewchain.cwv.auth.enums;

import lombok.Data;

/**
 * 返回代码及提示信息 枚举
 * @author Moon
 * @date 2018-03-30
 */
public enum ReturnCodeMsgEnum {
	/**
	 * 注册
	 */
	REG_SUCCESS("01","注册成功"),
	REG_DUPLICATE_PHONE("02","⼿机号已注册"),
	REG_ERROR_CODE("03","验证码错误"),
	REG_ERROR_PHONE_CODE("04","短信验证码错误"),
	REG_ERROR_PHONE_CODE_EXPIRED("05","验证码已过期，请重新发起短信验证！"),
	REG_FREQUENTLY_CALLED("90","注册调⽤过于频繁"),
	REG_EXCEPTION("99","未知异常"),
	
	/**
	 * 登陆
	 */
	LIN_SUCCESS("01","登录成功"),
	LIN_ERROR_PHONE_PWD("02","⼿机号或密码错误"),
	LIN_ERROR_CODE("03","验证码错误"),
	LIN_EXCEPTION("99","未知异常"),
	
	/**
	 * 重置密码
	 */
	RSP_SUCCESS("01","重置密码成功"),
	RSP_ERROR_CODE("02","验证码无效"),
	RSP_ERROR_PHONE("03","手机号无效"),
	RSP_ERROR_PHONE_CODE("04","短信验证码无效"),
	RSP_ERROR_TIMES("05","次数超限"),
	RSP_EXCEPTION("99","未知异常"),
	
	/**
	 * 修改密码
	 */
	SPS_SUCCESS("01","修改密码成功"),
	SPS_ERROR_PWD_OLD("02","旧密码错误"),
	SPS_DUPLICATE_PWD("03","新密码与原密码相同"),
	SPS_ERROR_PHONE_CODE("04","短信验证码无效"),
	SPS_ERROR_TIMES("05","次数超限"),
	SPS_EXCEPTION("99","未知异常"),
	
	/**
	 * 设置交易密码
	 */
	STP_SUCCESS("01","成功"),
	STP_ERROR_PWD_OLD("02","旧交易密码错误"),
	STP_DUPLICATE_PWD("03","新密码与原密码相同"),
	STP_ERROR_PHONE_CODE("04","短信验证码无效"),
	STP_ERROR_TIMES("05","次数超限"),
	STP_EXCEPTION("99","未知异常"),
	
	/**
	 * 设置昵称
	 */
	SNN_SUCCESS("01","设置昵称成功"),
	SNN_EXCEPTION("99","未知异常"),
	
	/**
	 * 设置头像
	 */
	SHI_SUCCESS("01","重置密码成功"),
	SHI_EXCEPTION("99","未知异常"),
	
	/**
	 * 注销
	 */
	
	LOU_SUCCESS("01","注销成功"),
	LOU_EXCEPTION("99","未知异常"),
	
	/**
	 * 校验token
	 */
	ATS_SUCCESS("01","校验通过"),
	ATS_ERROR_TOKEN("02","access_token无效"),
	ATS_EXCEPTION("99","未知异常"),
	
	/**
	 * 刷新token
	 */
	RTS_SUCCESS("01","刷新成功"),
	RTS_ERROR_TOKEN("02","refresh_token无效"),
	RTS_EXCEPTION("99","未知异常"),
	
	/**
	 * 设置头像
	 */
	SIS_SUCCESS("01","设置头像成功"),
	SIS_EXCEPTION("99","未知异常"),
	
	/**
	 * 校验短信验证码
	 */
	MSV_SUCCESS("01","有效"),
	MSV_ERROR("02","无效"),
	MSV_EXCEPTION("99","未知异常"),
	
	/**
	 * 发送短信验证码
	 */
	SMC_SUCCESS("01","发送成功！"),
	SMC_ERROR("02","短信发送失败"),
	SMC_ERROR_PHONE("03","手机号错误"),
	SMC_EXCEPTION("99","未知异常"),
	
	
	
	/**
	 * 短信验证码
	 */
	VER_SUCCESS("01","验证码验证成功！"),
	VER_ERROR_EMPTY("02","没有短信验证记录，请重新发起短信验证！"),
	VER_ERROR_DUPLICATE("03","有多条短信验证记录,请重新发起短信验证！"),
	VER_ERROR_EXPIRED("04","验证码已过期，请重新发起短信验证！"),
	VER_ERROR("05","无效"),
	VER_EXCEPTION("99","未知异常"),
	
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
