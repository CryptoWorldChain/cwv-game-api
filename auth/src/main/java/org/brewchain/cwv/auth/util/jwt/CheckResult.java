package org.brewchain.cwv.auth.util.jwt;

import io.jsonwebtoken.Claims;

/**
 * 验证结果模型
 * @author Moon
 *
 */
public class CheckResult {

	private int errCode;
	
	private String msg;
	
	private boolean success;
	
	private Claims claims;

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Claims getClaims() {
		return claims;
	}

	public void setClaims(Claims claims) {
		this.claims = claims;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}

