/** 
 * Copyright: Copyright (c)2014
 * Company: 支付通(ICardPay) 
 */
package com.torry.data.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 日期工具类
 * @author ZQ
 * @date 2017年8月7日
 */
public class DateUtil {

	/** 年-月-日 时:分:秒 显示格式 */
	public static String DATE_TO_STRING_DETAIAL_PATTERN = "yyyy-MM-dd HH:mm:ss";

	/** 年-月-日 显示格式 */
	public static String DATE_TO_STRING_SHORT_PATTERN = "yyyy-MM-dd";

	private static SimpleDateFormat simpleDateFormat;



	public static Date currentDate() {
		return  new Date();
	}


	/**
	 * 获取当前时间
	 * @return
     */
	public static String currentFormatDate() {
		simpleDateFormat = new SimpleDateFormat(DATE_TO_STRING_DETAIAL_PATTERN);
		return simpleDateFormat.format(new Date());
	}


	/**
	 * date转化为String
	 * @param date
	 * @return
     */
	public static String dateToString(Date date) {
		simpleDateFormat = new SimpleDateFormat(DATE_TO_STRING_DETAIAL_PATTERN);
		return simpleDateFormat.format(date);
	}

	public static Date addMinuteToDate(int minute){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.MINUTE, minute);
		Date date = calendar.getTime();
		return date;
	}

	/**
	 * 给指定的日期增加分钟，为空时默认当前时间
	 * @param minute 增加分钟  正数相加、负数相减
	 * @return String
	 */
	public static String addMinuteToDateStr(int minute){
		return dateToString(addMinuteToDate(minute));
	}


	public static Date addDayToDate(int day){
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, day);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MINUTE,0);
		Date date = calendar.getTime();
		return date;
	}

	public static String addDayToDateStr(int day){
		return dateToString(addDayToDate(day));
	}


	public static Date getWeekMonday(){
		Calendar calendar = Calendar.getInstance();
		calendar.set(GregorianCalendar.DAY_OF_WEEK,GregorianCalendar.MONDAY);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MINUTE,0);
		Date date = calendar.getTime();
		return date;
	}

	public static String getWeekMondayStr(){
		return dateToString(getWeekMonday());
	}

	/**
	 * String转化为Date
	 * @param date
	 * @return
	 */
	public static Date stringToShortDate(String date) {
		simpleDateFormat = new SimpleDateFormat(DATE_TO_STRING_SHORT_PATTERN);
		Date dateFormat = null;
		try {
			dateFormat = simpleDateFormat.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return dateFormat;
	}

	/**
	 * 时间往后推n天的时间
	 * @param date
	 * @param day
	 * @return
	 */
	public static Date getNextDay(Date date, int day){
		try {
			Calendar calendar = new GregorianCalendar();
			calendar.setTime(date);
			calendar.add(Calendar.DATE,day);//把日期往后增加一天.整数往后推,负数往前移动
			date = calendar.getTime(); //这个时间就是日期往后推一天的结果
		} catch (Exception e) {
			e.printStackTrace();
		}
		return date;
	}

}
