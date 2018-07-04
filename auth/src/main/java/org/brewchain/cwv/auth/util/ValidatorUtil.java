package org.brewchain.cwv.auth.util;

import java.util.regex.Pattern;

public class ValidatorUtil {
    /**
     * 校验用户名
     * 
     * @param username
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isUsername(String username) {
        return Pattern.matches(ValidateEnum.USERNAME.getRegStr(), username);
    }
 
    /**
     * 校验密码
     * 
     * @param password
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isPassword(String password) {
        return Pattern.matches(ValidateEnum.PASSWORD.getRegStr(), password);
    }
 
    /**
     * 校验手机号
     * 
     * @param mobile
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isMobile(String mobile) {
        return Pattern.matches(ValidateEnum.MOBILE.getRegStr(), mobile);
    }
 
    /**
     * 校验邮箱
     * 
     * @param email
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isEmail(String email) {
        return Pattern.matches(ValidateEnum.EMAIL.getRegStr(), email);
    }
 
    /**
     * 校验汉字
     * 
     * @param chinese
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isChinese(String chinese) {
        return Pattern.matches(ValidateEnum.CHINESE.getRegStr(), chinese);
    }
    
    /**
     * 校验汉字
     * 
     * @param chinese
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isChineseName(String chineseName) {
        return Pattern.matches(ValidateEnum.CHINESENAME.getRegStr(), chineseName);
    }
 
    /**
     * 校验身份证
     * 
     * @param idCard
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isIDCard(String idCard) {
        return Pattern.matches(ValidateEnum.ID_CARD.getRegStr(), idCard);
    }
 
    /**
     * 校验URL
     * 
     * @param url
     * @return 校验通过返回true，否则返回false
     */
    public static boolean isUrl(String url) {
        return Pattern.matches(ValidateEnum.URL.getRegStr(), url);
    }
 
    /**
     * 校验IP地址
     * 
     * @param ipAddr
     * @return
     */
    public static boolean isIPAddr(String ipAddr) {
        return Pattern.matches(ValidateEnum.IP_ADDR.getRegStr(), ipAddr);
    }
    
    
    public enum ValidateEnum{
    	USERNAME("^[a-zA-Z]\\w{5,20}$","手机格式错误"),
    	PASSWORD("^[a-zA-Z0-9]{6,20}$","密码格式错误"),
//    	MOBILE("^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$","手机格式错误"),
    	MOBILE("^[0-9]{6,13}$","手机格式错误"),
    	EMAIL("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*","邮箱格式错误"),
    	CHINESE("^[\u4E00-\u9FA5]{0,}$","汉字格式错误"),
    	CHINESENAME("^[\u4E00-\u9FA5]{2,10}$","中文名字格式错误"),
    	ID_CARD("(^\\d{18}$)|(^\\d{15}$)","身份证格式错误"),
    	URL("http(s)?://([\\w-]+\\.)+[\\w-]+(/[\\w- ./?%&=]*)?","链接格式错误"),
    	IP_ADDR("(25[0-5]|2[0-4]\\d|[0-1]\\d{2}|[1-9]?\\d)","IP格式错误");
		private String regStr;
		private String verifyMsg;
		private ValidateEnum(String regStr, String verifyMsg) {
			this.regStr = regStr;
			this.verifyMsg = verifyMsg;
		}
		public String getRegStr() {
			return regStr;
		}
		public void setRegStr(String regStr) {
			this.regStr = regStr;
		}
		public String getVerifyMsg() {
			return verifyMsg;
		}
		public void setVerifyMsg(String verifyMsg) {
			this.verifyMsg = verifyMsg;
		}
	}
}
