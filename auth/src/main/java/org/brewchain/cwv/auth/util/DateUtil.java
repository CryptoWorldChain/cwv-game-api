package org.brewchain.cwv.auth.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DateUtil {

	public static final String DAY = "yyyy-MM-dd";
	public static final String DAYTIME = "yyyy-MM-dd HH:mm:ss";
	public static final String DAY_HHMM = "yyyy-MM-dd HH:mm";
	private static final int NUM = 1000;
	private static final int NUMTEN = 10;

	private DateUtil() {
	}

	public static String getNow() {
		Date date = new Date(System.currentTimeMillis());
		return format(date, DAYTIME);
	}

	public static long getNowToLong(String pattern) {
		return parseDateToTimeMillis(format(new Date(), pattern), pattern);
	}

	/**
	 * 格式化输出日期
	 *
	 * @param date
	 *            日期
	 * @param pattern
	 *            格式
	 * @return 返回字符型日期
	 */
	public static String format(Date date, String pattern) {
		String result = "";
		try {
			if (date != null) {
				DateFormat dateFormat = new SimpleDateFormat(pattern);
				result = dateFormat.format(date);
			}
		} catch (Exception e) {
			log.error("DateUtil format method error", e);
		}
		return result;
	}

	public static String getTimeStamp() {
		Date currentDate = new Date();
		long timeStampLong = currentDate.getTime() / NUM;
		return timeStampLong + "";
	}

	/**
	 * 获取指定月份包含的天数
	 *
	 * @param schedulingmonth
	 *            格式 yyyy-MM
	 */
	public static int getMaxdays(String schedulingmonth) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, Integer.parseInt(schedulingmonth.split("-")[1]) - 1);
		int alldays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		return alldays;
	}

	public static String getCurrentMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		int currentmonth = cal.get(Calendar.MONTH) + 1;
		if (currentmonth < NUMTEN) {
			return cal.get(Calendar.YEAR) + "-0" + currentmonth;
		}
		return cal.get(Calendar.YEAR) + "-" + currentmonth;
	}

	public static long parseDateToTimeMillis(String dateStr, String format) {
		long datetime = 0;
		Date date = null;
		try {
			DateFormat df = new SimpleDateFormat(format);
			if ((!"".equals(dateStr)) && dateStr != null) {
				date = df.parse(dateStr);
				datetime = date.getTime();
			}
		} catch (Exception e) {
			log.error("DateUtil parseDateToTimeMillis method !", e);
		}
		return datetime;
	}

	/**
	 * 把毫秒转化成日期
	 *
	 * @param dateFormat(日期格式，例如：MM/
	 *            dd/yyyy HH:mm:ss)
	 */

	public static String transferLongToDate(String dateFormat, Long millSec) {

		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

		Date date = new Date(millSec);

		return sdf.format(date);

	}

	/**
	 * 将时间转换为时间戳
	 * 
	 * @param s(时间)
	 * @return
	 */
	public static Long dateToStamp(String s) throws ParseException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date date = simpleDateFormat.parse(s);
		long ts = date.getTime();
		return ts;
	}

	/**
	 * 比较日期  
	 * -1表示ss时间在ee时间之前
	 * 1表示ss时间在ee时间之后
	 * 0表示时间相等
	 * -100表示ss和ee有一个为空
	 * @param ss
	 * @param ee
	 * @return
	 */
	public static int compare(Date ss, Date ee) {
		if (ss != null && ee != null) {
			if (ss.getTime() < ee.getTime()) {
				return -1;
			} else if (ss.getTime() > ee.getTime()) {
				return 1;
			}else if (ss.getTime() == ee.getTime()){
				return 0;
			}
		}
		return -100;
	}

	/**
	 * 比较日期
	 */
	public static int compare(String s, String e) {
		if ("".equals(s) || "".equals(e)) {
			return -1;
		}
		Date ss = parseDate(s);
		Date ee = parseDate(e);
		if (ss != null && ee != null) {
			if (ss.getTime() < ee.getTime()) {
				return -1;
			} else if (ss.getTime() > ee.getTime()) {
				return 1;
			}
		}
		return 0;
	}

	/**
	 * 日期相减
	 *
	 * @param date
	 *            日期
	 * @param date1
	 *            日期
	 * @return 返回相减后的日期
	 */
	public static int diffDate(java.util.Date date, java.util.Date date1) {
		return (int) ((getMillis(date) - getMillis(date1)) / (24 * 3600 * 1000));
	}

	/**
	 * 返回毫秒
	 *
	 * @param date
	 *            日期
	 * @return 返回毫秒
	 */
	public static long getMillis(java.util.Date date) {
		java.util.Calendar c = java.util.Calendar.getInstance();
		c.setTime(date);
		return c.getTimeInMillis();
	}

	/**
	 * 格式化日期
	 *
	 * @param dateStr
	 *            字符型日期
	 * @param format
	 *            格式
	 * @return 返回日期
	 */
	public static java.util.Date parseDate(String dateStr, String format) {
		java.util.Date date = null;
		try {
			java.text.DateFormat df = new java.text.SimpleDateFormat(format);
			if ((!"".equals(dateStr)) && dateStr != null) {
				date = df.parse(dateStr);
			}
		} catch (Exception e) {
			log.error("DateUtil parseDate method !", e);
		}
		return date;
	}

	public static java.util.Date parseDate(String dateStr) {
		return parseDate(dateStr, DAYTIME);
	}

	public static String getWtmTaskIntervalTime(Date target) {
		Date now = new Date();
		long nowTime = now.getTime();
		long targetTime = target.getTime();
		StringBuilder result = new StringBuilder();
		String flag = "+";
		Long value = targetTime - nowTime;
		if (targetTime < nowTime) {
			flag = "-";
			value = nowTime - targetTime;
		}
		result.append(flag);
		long day = value / (1000 * 60 * 60 * 24);
		long hour = (value % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
		long minute = ((value % (1000 * 60 * 60 * 24)) % (1000 * 60 * 60)) / (1000 * 60);
		if (day == 0) {
			result.append("-天");
		} else {
			result.append(day);
			result.append("天");
		}
		result.append(hour);
		result.append("时");
		result.append(minute);
		result.append("分");
		return result.toString();
	}

	public static Date addMinute(Date time, int addMinute) {
		Calendar ca = Calendar.getInstance();
		ca.setTime(time);
		ca.add(Calendar.MINUTE, addMinute);
		return ca.getTime();
	}

	public static String getNowDate() {
		Date date = new Date(System.currentTimeMillis());
		return format(date, DAY);
	}

}
