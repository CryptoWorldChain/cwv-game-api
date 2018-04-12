package org.brewchain.cwv.game.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
	public static String DAY = "yyyy-MM-dd";
	public static String DAY_TIME = "yyyy-MM-dd HH:mm:ss";
	
	
	public static String getToday(){
		return new SimpleDateFormat(DAY).format(new Date());
	}
	
	public static String getTodayTime(){
		return new SimpleDateFormat(DAY_TIME).format(new Date());
	}
	
	public static String getDay(Date day){
		return new SimpleDateFormat(DAY).format(day);
	}
	
	public static String getDayTime(Date day){
		return new SimpleDateFormat(DAY_TIME).format(day);
	}
	
	
}
