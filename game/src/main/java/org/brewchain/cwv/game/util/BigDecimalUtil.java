package org.brewchain.cwv.game.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BigDecimalUtil {
	/**
	 * 获取long值
	 * @param d
	 * @return
	 */
	public static long longValue(double d){
		return longValue(new BigDecimal(d));
	}
	
	public static long longValue(BigDecimal b){
		
		return b.setScale(0, BigDecimal.ROUND_HALF_UP).longValue();
	}
	
	public static long multiply(double d,double f) {
		return multiply(new BigDecimal(d),new BigDecimal(f));
	}
	
	public static long multiply(BigDecimal d,BigDecimal f) {
		
		return longValue(d.multiply(f));
	}
	

}
