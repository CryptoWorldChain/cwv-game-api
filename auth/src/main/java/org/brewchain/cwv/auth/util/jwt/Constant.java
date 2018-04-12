package org.brewchain.cwv.auth.util.jwt;

/**
 * 全局配置常量
 * @author Moon
 *
 */
public class Constant {
	
	/**
	 * 数据请求返回码
	 */
	/**
	 * 成功
	 */
	public static final int RESCODE_SUCCESS = 1000;				//成功
	public static final int RESCODE_SUCCESS_DATA = 1001;		//成功(有返回数据)
	public static final int RESCODE_NOEXIST = 1004;				//查询结果为空
	/**
	 * 失败
	 */
	public static final int RESCODE_EXCEPTION = 1002;			//请求抛出异常
	public static final int RESCODE_EXCEPTION_DATA = 1008;		//异常带数据
	public static final int RESCODE_NOLOGIN = 1003;				//未登陆状态
	public static final int RESCODE_NOAUTH = 1005;				//无操作权限
	public static final int RESCODE_LOGINEXPIRE = 1009;			//登录过期
	/**
	 * token（暂时没有刷新自动token机制，通过refresh_token重新获取）
	 */
	public static final int RESCODE_REFTOKEN_MSG = 1006;		//刷新TOKEN(有返回数据)
	public static final int RESCODE_REFTOKEN = 1007;			//刷新TOKEN
	
	public static final int JWT_ERRCODE_EXPIRE = 4001;			//Token过期
	public static final int JWT_ERRCODE_FAIL = 4002;			//验证不通过

	/**
	 * jwt
	 */
	public static final String TOKEN_TYPE = "JWT";	
	public static final String JWT_ID = "5236A";										//jwtid
	public static final String JWT_SECERT = "7786df7fc3a34e26a61c034d5ec8245d";			//密匙
	public static final long JWT_TTL = 10*60*1000;	//access_token有效时间
	public static final long JWT_TTL_REFRESH = 10*24*60*60*1000;	//refresh_token有效时间
	
	/**
	 * 验证码URL
	 */
	public static final String URL_GET_CODE_ = "";	
	public static final String URL_GET_PHONE_CODE = "http://localhost:18000/cwv/sms/pbaut.do";		
	public static final String URL_VERIFY_CODE = "http://localhost:18000/cwv/sms/pbmsv.do";	
	public static final String URL_VERIFY_PHONE_CODE  = "http://localhost:18000/cwv/sms/pbver.do";
	
}

