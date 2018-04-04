package org.brewchain.cwv.common.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;

/**
 * 随机数获取
 * @author leo
 *
 */
public class RandomUtill {
	
	public static HashMap<String, Long> codeMap = new HashMap<String,Long>();
	
	/**
	 * 随机生成六位数，每一位都在0-9之间的数
	 * @return
	 */
	public static String auto6Number() {
		StringBuilder strNumber = new StringBuilder();
		for(int i=0;i<6;i++) {
			strNumber.append(RandomUtils.nextInt(0, 10));
		}
		return strNumber.toString();
	}
	
	/**
	 * 随机生成n位数，每一位都在0-9之间的数
	 * @param intNum    设定的位数
	 * @return
	 */
	public static String autoNumber(int intNum) {
		StringBuilder strNumber = new StringBuilder();
		for(int i=0;i<intNum;i++) {
			strNumber.append(RandomUtils.nextInt(0, 10));
		}
		return strNumber.toString();
	}
//	public static void main(String[] args) {
//		System.out.println(RandomUtill.auto6Number());
//		String str = RandomUtill.auto6Number();
//		String msg = "您的验证码是：%s";
//		msg = String.format(msg, str);
//		System.out.println(msg);
//	}

	/**
	 * 调取删除过期code
	 */
	public static void updateCodeMap() {
		Iterator iter = codeMap.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry<String, Long> entry = (Map.Entry) iter.next();
			String key = entry.getKey();
			Long val = entry.getValue();
			long now = new Date().getTime();
			val = val+(60*1000);
			if(now>val){
				iter.remove();
			}
		}
			
	}

}
