package org.ukettle.www.toolkit;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class DateUtils {

	/**
	 * 获取当前时间-24小时制
	 * 
	 * @return string date 当前日期
	 */
	public static String getTime24() {
		return getTimeFormat24().format(new Date());
	}

	/**
	 * 获取当前时间-12小时制
	 * 
	 * @return string date 当前日期
	 */
	public static String getTime12() {
		return getTimeFormat24().format(new Date());

	}

	/**
	 * 获取UNIX时间
	 * 
	 * @return string date 当前日期
	 */
	public static String getTimestamp() {
		return Long.toString(System.currentTimeMillis() / 1000);
	}

	/**
	 * 转换时间
	 * 
	 * @param String
	 *            date
	 * @return Date 日期
	 */
	public static Date format(String object) {
		try {
			return getTimeFormat24().parse(object);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 转换时间
	 * 
	 * @param String
	 *            date
	 * @return Date 日期
	 */
	public static String format(Long object) {
		return getTimeFormat24().format(object);
	}

	/**
	 * 转换时间
	 * 
	 * @param String
	 *            date
	 * @return Date 日期
	 */
	public static Date objectToDate(String object) {
		try {
			return getTimeFormatYMDHMS().parse(object);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 转换时间
	 * 
	 * @param Date
	 *            date
	 * @return String 日期
	 */
	public static String toString(Long date) {
		return getTimeFormat24().format(date);
	}

	/**
	 * 转换时间
	 * 
	 * @param Date
	 *            date
	 * @return String 日期
	 */
	public static String toString(Date date) {
		return getTimeFormat24().format(date);
	}

	public int getDaysBetween(Calendar d1, Calendar d2) {
		if (d1.after(d2)) {
			Calendar swap = d1;
			d1 = d2;
			d2 = swap;
		}
		int days = d2.get(6) - d1.get(6);
		int y2 = d2.get(1);
		if (d1.get(1) != y2) {
			d1 = (Calendar) d1.clone();
			do {
				days += d1.getActualMaximum(6);
				d1.add(1, 1);
			} while (d1.get(1) != y2);
		}
		return days;
	}

	public int getWorkingDay(Calendar d1, Calendar d2) {
		int result = -1;
		if (d1.after(d2)) {
			Calendar swap = d1;
			d1 = d2;
			d2 = swap;
		}
		int charge_start_date = 0;
		int charge_end_date = 0;

		int stmp = 7 - d1.get(7);
		int etmp = 7 - d2.get(7);
		if ((stmp != 0) && (stmp != 6)) {
			charge_start_date = stmp - 1;
		}
		if ((etmp != 0) && (etmp != 6)) {
			charge_end_date = etmp - 1;
		}

		result = getDaysBetween(getNextMonday(d1), getNextMonday(d2)) / 7 * 5
				+ charge_start_date - charge_end_date;
		return result;
	}

	public String getChineseWeek(Calendar date) {
		String[] dayNames = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" };
		int dayOfWeek = date.get(7);
		return dayNames[(dayOfWeek - 1)];
	}

	public Calendar getNextMonday(Calendar date) {
		Calendar result = null;
		result = date;
		do {
			result = (Calendar) result.clone();
			result.add(5, 1);
		} while (result.get(7) != 2);
		return result;
	}

	/**
	 * 获取公共节假日
	 * 
	 * @param date
	 * @param pattern
	 * @param num
	 * @return
	 */
	public int getHolidays(Calendar d1, Calendar d2) {
		return getDaysBetween(d1, d2) - getWorkingDay(d1, d2);
	}

	/**
	 * REST API调用方法
	 * 
	 * @param date
	 * @param pattern
	 * @param num
	 * @return
	 */
	public static String getMillis() {
		Calendar calendar = Calendar.getInstance();
		return getTimeHMS24().format(calendar.getTime());
	}

	/**
	 * 给指定的日期加上(减去)月份
	 * 
	 * @param date
	 * @param pattern
	 * @param num
	 * @return
	 */
	public static String addMoth(Date date, String pattern, int num) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Calendar calender = Calendar.getInstance();
		calender.setTime(date);
		calender.add(Calendar.MONTH, num);
		return simpleDateFormat.format(calender.getTime());
	}

	/**
	 * 给制定的时间加上(减去)天
	 * 
	 * @param date
	 * @param pattern
	 * @param num
	 * @return
	 */
	public static String addDay(Date date, String pattern, int num) {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
		Calendar calender = Calendar.getInstance();
		calender.setTime(date);
		calender.add(Calendar.DATE, num);
		return simpleDateFormat.format(calender.getTime());
	}

	/**
	 * 两个时间比较
	 * 
	 * @param
	 * @return
	 */
	public static int compareDateWithNow(Date date) {
		Date now = new Date();
		int rnum = date.compareTo(now);
		return rnum;
	}

	/**
	 * 两个时间比较(时间戳比较)
	 * 
	 * @param
	 * @return
	 */
	public static int compareDateWithNow(long date) {
		long now = dateToUnixTimestamp();
		if (date > now) {
			return 1;
		} else if (date < now) {
			return -1;
		} else {
			return 0;
		}
	}

	/**
	 * 两个时间比较(时间戳比较)
	 * 
	 * @param
	 * @return
	 * @throws ParseException
	 */
	public static boolean compareWithNow(String object) {
		long expired = dateToUnixTimestamp() - (Long.parseLong(object) * 1000);
		if (expired > 900000 || expired < -900000) {
			return false;
		}
		return true;
	}

	/**
	 * 将指定的日期转换成Unix时间戳
	 * 
	 * @param date
	 *            需要转换的日期 yyyy-MM-dd HH:mm:ss
	 * @return long 时间戳
	 */
	public static long dateToUnixTimestamp(String date) {
		long timestamp = 0;
		try {
			getTimeFormat24().parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return timestamp;
	}

	/**
	 * 将指定的日期转换成Unix时间戳
	 * 
	 * @param date
	 *            需要转换的日期 yyyy-MM-dd
	 * @return long 时间戳
	 */
	public static long dateToUnixTimestamp(String date, String dateFormat) {
		long timestamp = 0;
		try {
			timestamp = new SimpleDateFormat(dateFormat).parse(date).getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return timestamp;
	}

	/**
	 * 将当前日期转换成Unix时间戳
	 * 
	 * @return long 时间戳
	 */
	public static long dateToUnixTimestamp() {
		long timestamp = new Date().getTime();
		return timestamp;
	}

	/**
	 * 将Unix时间戳转换成日期
	 * 
	 * @param timestamp
	 *            时间戳
	 * @return String 日期字符串
	 */
	public static String unixTimestampToDate(long timestamp) {
		SimpleDateFormat sd = getTimeFormat24();
		sd.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		return sd.format(new Date(timestamp));
	}

	/**
	 * 将Unix时间戳转换成日期
	 * 
	 * @param timestamp
	 *            时间戳
	 * @return String 日期字符串
	 */
	public static String TimeStamp2Date(long timestamp, String dateFormat) {
		String date = new SimpleDateFormat(dateFormat).format(new Date(
				timestamp));
		return date;
	}

	/**
	 * 将Unix时间戳转换成日期
	 * 
	 * @param timestamp
	 *            时间戳
	 * @return String 日期字符串
	 */
	public static String TimeStamp2Date(long timestamp) {
		return getTimeFormat24().format(new Date(timestamp));
	}

	/**
	 * （日）计算去年同期和上期的起止时间 （日） beginkey、endkey 返回的map key
	 * 
	 * begin \ end本期起止日期
	 */
	public static Map<String, String> getDayDate(int type, String beginkey,
			String endkey, String begin, String end) {
		Map<String, String> map = new HashMap<String, String>();
		Date dBegin = null; // 开始日期
		Date dEnd = null; // 结束日期
		try {
			SimpleDateFormat sdf = getTimeFormatYMD();
			dBegin = sdf.parse(begin);
			dEnd = sdf.parse(end);
			Calendar calBegin = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calBegin.setTime(dBegin);
			Calendar calEnd = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calEnd.setTime(dEnd);
			if (type == 1) {// 计算上期
				// 计算查询时间段相隔多少天
				long beginTime = dBegin.getTime();
				long endTime = dEnd.getTime();
				long inter = endTime - beginTime;
				if (inter < 0) {
					inter = inter * (-1);
				}
				long dateMillSec = 24 * 60 * 60 * 1000;
				long dateCnt = inter / dateMillSec;
				long remainder = inter % dateMillSec;
				if (remainder != 0) {
					dateCnt++;
				}
				int day = Integer.parseInt(String.valueOf(dateCnt)) + 1;
				calBegin.add(Calendar.DATE, -day);// 像前推day天
				calEnd.add(Calendar.DATE, -day);// 像前推day天
			} else if (type == 2) {
				calBegin.add(Calendar.YEAR, -1);// 去年同期
				calEnd.add(Calendar.YEAR, -1);// 去年同期
			}
			map.put(beginkey, sdf.format(calBegin.getTime()));
			map.put(endkey, sdf.format(calEnd.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return map;

	}

	/**
	 * （日）返回时间段内的所有的天 type 0本期 1上期 2去年同期(日)
	 * 
	 * @param begin
	 *            起始日期
	 * @param end
	 *            截止日期
	 * @return
	 */
	public static List<String> getDaysList(String begin, String end) {
		SimpleDateFormat sdf = getTimeFormatYMD();
		List<String> lDate = new ArrayList<String>();
		Date date1 = null; // 开始日期
		Date dEnd = null; // 结束日期
		try {
			date1 = sdf.parse(begin);
			dEnd = sdf.parse(end);
			Calendar calBegin = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calBegin.setTime(date1);
			Calendar calEnd = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calEnd.setTime(dEnd);
			// 添加第一个 既开始时间
			lDate.add(sdf.format(calBegin.getTime()));
			while (calBegin.compareTo(calEnd) < 0) {
				// 根据日历的规则，为给定的日历字段添加或减去指定的时间量
				calBegin.add(Calendar.DAY_OF_MONTH, 1);
				Date ss = calBegin.getTime();
				String str = sdf.format(ss);
				lDate.add(str);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return lDate;
	}

	/**
	 * （周）计算（周） 上期和去年同期的起止日期和起止周 计算上期的起止时间 和去年同期 type 0本期 1上期 2去年同期 起始日期key
	 * beginkey endkey 起始日期的起止周key beginWkey endWkey 本期：begin end 本期起止周
	 * beginW、endW
	 */
	public static Map<String, String> getWeekDate(int type, String beginkey,
			String endkey, String beginWkey, String endWkey, String begin,
			String end, String beginW, String endW) {
		Map<String, String> map = new HashMap<String, String>();
		SimpleDateFormat sdf = getTimeFormatYMD();
		Date date1 = null; // 开始日期
		Date dEnd = null; // 结束日期
		try {
			date1 = sdf.parse(begin);
			dEnd = sdf.parse(end);
			Calendar calBegin = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calBegin.setTime(date1);
			Calendar calEnd = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calEnd.setTime(dEnd);
			calBegin.setFirstDayOfWeek(Calendar.MONDAY);
			calEnd.setFirstDayOfWeek(Calendar.MONDAY);
			if (type == 1) {// 计算上期
				// 计算查询时间段相隔多少周
				int week = getWeekCount(date1, dEnd);
				// 往前推week周
				calBegin.add(Calendar.WEEK_OF_YEAR, -week);// 像前推week周
				calEnd.add(Calendar.WEEK_OF_YEAR, -week);// 像前推week周
				map.put(beginWkey,
						String.valueOf(calBegin.get(Calendar.WEEK_OF_YEAR)));// 得到其实日期的周);
				map.put(endWkey,
						String.valueOf(calEnd.get(Calendar.WEEK_OF_YEAR)));// 得到其实日期的周);
				// 得到起始周的周一 和结束周的周末
				int day_of_week = calBegin.get(Calendar.DAY_OF_WEEK) - 1;
				if (day_of_week == 0)
					day_of_week = 7;
				calBegin.add(Calendar.DATE, -day_of_week + 1);
				// 本周周末
				int day_of_week_end = calEnd.get(Calendar.DAY_OF_WEEK) - 1;
				if (day_of_week_end == 0)
					day_of_week_end = 7;
				calEnd.add(Calendar.DATE, -day_of_week_end + 7);
			} else if (type == 2) {
				calBegin.add(Calendar.YEAR, -1);// 去年同期
				calEnd.add(Calendar.YEAR, -1);// 去年同期

				// 去年的开始的本周
				calBegin.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(beginW));
				calEnd.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(endW));
				// 年-1 周不变
				map.put(beginWkey, beginW);
				map.put(endWkey, endW);

				// 得到起始周的周一 和结束周的周末
				int day_of_week = calBegin.get(Calendar.DAY_OF_WEEK) - 1;
				if (day_of_week == 0)
					day_of_week = 7;
				calBegin.add(Calendar.DATE, -day_of_week + 1);
				// 本周周末
				int day_of_week_end = calEnd.get(Calendar.DAY_OF_WEEK) - 1;
				if (day_of_week_end == 0)
					day_of_week_end = 7;
				calEnd.add(Calendar.DATE, -day_of_week_end + 7);
			}
			map.put(beginkey, sdf.format(calBegin.getTime()));
			map.put(endkey, sdf.format(calEnd.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return map;

	}

	/**
	 * （周）返回起止时间内的所有自然周
	 * 
	 * @param begin
	 *            时间起
	 * @param end
	 *            时间止
	 * @param startw
	 *            周起
	 * @param endW
	 *            周止
	 * @return
	 */
	public static List<String> getWeeksList(String begin, String end,
			String startw, String endW) {
		DateFormat sdf = new SimpleDateFormat("yyyy");
		List<String> lDate = new ArrayList<String>();
		Date date1 = null; // 开始日期
		Date dEnd = null; // 结束日期
		try {
			date1 = sdf.parse(begin);
			dEnd = sdf.parse(end);
			Calendar calBegin = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calBegin.setTime(date1);
			Calendar calEnd = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calEnd.setTime(dEnd);
			// 开始时间是今年的第几周
			calBegin.setFirstDayOfWeek(Calendar.MONDAY);
			// 添加第一个周
			int beginww = Integer.parseInt(startw);
			int endww = Integer.parseInt(endW);

			int beginY = calBegin.get(Calendar.YEAR);
			int endY = calEnd.get(Calendar.YEAR);

			int weekall = getAllWeeks(beginY + "");
			// 如果是同一年
			do {
				lDate.add(beginY + "年第" + beginww + "周");
				if (beginww == weekall) {
					beginww = 0;
					beginY++;
					weekall = getAllWeeks(beginY + "");
				}
				if (beginY == endY && beginww == endww) {
					break;
				}
				beginww++;
			} while (beginY <= endY);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return lDate;
	}

	/**
	 * （月）得当时间段内的所有月份
	 * 
	 * @param StartDate
	 * @param endDate
	 * @return
	 */
	public static List<String> getYearMouthBy(String StartDate, String endDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM");
		Date date1 = null; // 开始日期
		Date date2 = null; // 结束日期
		try {
			date1 = df.parse(StartDate);
			date2 = df.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		// 定义集合存放月份
		List<String> list = new ArrayList<String>();
		// 添加第一个月，即开始时间
		list.add(df.format(date1));
		c1.setTime(date1);
		c2.setTime(date2);
		while (c1.compareTo(c2) < 0) {
			c1.add(Calendar.MONTH, 1);// 开始日期加一个月直到等于结束日期为止
			Date ss = c1.getTime();
			String str = df.format(ss);
			list.add(str);
		}
		return list;
	}

	/**
	 * （月）计算本期的上期和去年同期 1 上期 2同期 返回的mapkay beginkey endkey 本期起止：begin end
	 * 计算上期的起止时间 和去年同期 type 0本期 1上期 2去年同期
	 */
	public static Map<String, String> getMonthDate(int type, String beginkey,
			String endkey, String begin, String end) {
		Map<String, String> map = new HashMap<String, String>();
		DateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date date1 = null; // 开始日期
		Date dEnd = null; // 结束日期
		try {
			date1 = sdf.parse(begin);
			dEnd = sdf.parse(end);
			Calendar calBegin = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calBegin.setTime(date1);
			Calendar calEnd = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calEnd.setTime(dEnd);
			if (type == 1) {// 计算上期
				int year = calBegin.get(Calendar.YEAR);
				int month = calBegin.get(Calendar.MONTH);

				int year1 = calEnd.get(Calendar.YEAR);
				int month1 = calEnd.get(Calendar.MONTH);
				int result;
				if (year == year1) {
					result = month1 - month;// 两个日期相差几个月，即月份差
				} else {
					result = 12 * (year1 - year) + month1 - month;// 两个日期相差几个月，即月份差
				}
				result++;
				calBegin.add(Calendar.MONTH, -result);// 像前推day天
				calEnd.add(Calendar.MONTH, -result);// 像前推day天
			} else if (type == 2) {
				calBegin.add(Calendar.YEAR, -1);// 去年同期
				calEnd.add(Calendar.YEAR, -1);// 去年同期
			}
			map.put(beginkey, sdf.format(calBegin.getTime()));
			map.put(endkey, sdf.format(calEnd.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return map;

	}

	/**
	 * （年）计算本期（年）的上期
	 */
	public static Map<String, String> getYearDate(String beginkey,
			String endkey, String begin, String end) {
		Map<String, String> map = new HashMap<String, String>();
		DateFormat sdf = new SimpleDateFormat("yyyy");
		Date date1 = null; // 开始日期
		Date dEnd = null; // 结束日期
		try {
			date1 = sdf.parse(begin);
			dEnd = sdf.parse(end);
			Calendar calBegin = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calBegin.setTime(date1);
			Calendar calEnd = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calEnd.setTime(dEnd);
			int year = calBegin.get(Calendar.YEAR);
			int year1 = calEnd.get(Calendar.YEAR);
			int result;
			result = year1 - year + 1;// 两个日期的年份差
			calBegin.add(Calendar.YEAR, -result);// 像前推N年
			calEnd.add(Calendar.YEAR, -result);// 像前推N年
			map.put(beginkey, sdf.format(calBegin.getTime()));
			map.put(endkey, sdf.format(calEnd.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return map;

	}

	/**
	 * 获取年份时间段内的所有年
	 * 
	 * @param StartDate
	 * @param endDate
	 * @return
	 */
	public static List<String> getYearBy(String StartDate, String endDate) {
		DateFormat df = new SimpleDateFormat("yyyy");
		Date date1 = null; // 开始日期
		Date date2 = null; // 结束日期
		try {
			date1 = df.parse(StartDate);
			date2 = df.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		Calendar c1 = Calendar.getInstance();
		Calendar c2 = Calendar.getInstance();
		// 定义集合存放月份
		List<String> list = new ArrayList<String>();
		// 添加第一个月，即开始时间
		list.add(df.format(date1));
		c1.setTime(date1);
		c2.setTime(date2);
		while (c1.compareTo(c2) < 0) {
			c1.add(Calendar.YEAR, 1);// 开始日期加一个月直到等于结束日期为止
			Date ss = c1.getTime();
			String str = df.format(ss);
			list.add(str);
		}
		return list;
	}

	/**
	 * 获取两个日期段相差的周数
	 */
	public static int getWeekCount(Date date1, Date dEnd) {
		Calendar c_begin = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		c_begin.setTime(date1);
		Calendar c_end = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		c_end.setTime(dEnd);
		int count = 0;
		// c_end.add(Calendar.DAY_OF_YEAR, 1);
		// 结束日期下滚一天是为了包含最后一天
		c_begin.setFirstDayOfWeek(Calendar.MONDAY);
		c_end.setFirstDayOfWeek(Calendar.MONDAY);
		while (c_begin.before(c_end)) {
			if (c_begin.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
				count++;
			}
			c_begin.add(Calendar.DAY_OF_YEAR, 1);
		}
		return count;
	}

	/**
	 * 返回该年有多少个自然周
	 * 
	 * @param year
	 *            最多53 一般52 如果12月月末今天在本年53周（属于第二年第一周） 那么按照当年52周算
	 * @return
	 */
	public static int getAllWeeks(String year) {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat sdf = getTimeFormatYMD();
		try {
			calendar.setTime(sdf.parse(year + "-12-31"));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		calendar.setFirstDayOfWeek(Calendar.MONDAY);
		int week = calendar.get(Calendar.WEEK_OF_YEAR);
		if (week != 53) {
			week = 52;
		}
		return week;
	}

	/**
	 * 
	 * (季度) 计算本期的上期起止时间和同期的起止时间 返回的mao key 时间起止：beginkey endkey 季度起止： beginWkey
	 * endWkey 本期的时间起止：begin end 季度：beginW endW type 0本期 1上期 2去年同期 季度
	 */
	public static Map<String, String> getQuarterDate(int type, String beginkey,
			String endkey, String beginWkey, String endWkey, String begin,
			String end, String beginW, String endW) {
		// 计算本期的起始日期 和截止日期
		Map<String, String> map = new HashMap<String, String>();
		DateFormat sdf = new SimpleDateFormat("yyyy");
		Date date1 = null; // 开始日期
		Date dEnd = null; // 结束日期
		try {
			date1 = sdf.parse(begin);
			dEnd = sdf.parse(end);
			Calendar calBegin = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calBegin.setTime(date1);
			calBegin.set(Calendar.MONTH,
					setMonthByQuarter(Integer.parseInt(beginW)));
			Calendar calEnd = Calendar.getInstance();
			// 使用给定的 Date 设置此 Calendar 的时间
			calEnd.setTime(dEnd);
			calEnd.set(Calendar.MONTH,
					setMonthByQuarter(Integer.parseInt(endW)));

			if (type == 1) {// 计算上期
				// 计算查询时间段相隔多少季度(多少个月)
				int quarter = ((Integer.parseInt(end) - Integer.parseInt(begin))
						* 4
						+ (Integer.parseInt(endW) - Integer.parseInt(beginW)) + 1) * 3;
				// 往前推week周
				calBegin.add(Calendar.MONTH, -quarter);// 像前推week月份
				calEnd.add(Calendar.MONTH, -quarter);// 像前推week月份
				map.put(beginWkey, String.valueOf(getQuarterByMonth(calBegin
						.get(Calendar.MONTH))));// 得到其实日期的月);
				map.put(endWkey, String.valueOf(getQuarterByMonth(calEnd
						.get(Calendar.MONTH))));// 得到其实日期的月);
			} else if (type == 2) {
				calBegin.add(Calendar.YEAR, -1);// 去年同期
				calEnd.add(Calendar.YEAR, -1);// 去年同期
				// 年-1 周不变
				map.put(beginWkey, beginW);
				map.put(endWkey, endW);
			}
			map.put(beginkey,
					String.valueOf(calBegin.get((Calendar.YEAR)))
							+ "-"
							+ setMonthByQuarterToString(0,
									Integer.parseInt(map.get(beginWkey))));
			map.put(endkey,
					String.valueOf(calEnd.get((Calendar.YEAR))
							+ "-"
							+ setMonthByQuarterToString(1,
									Integer.parseInt(map.get(endWkey)))));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return map;

	}

	/**
	 * （季度）获取季度份时间段内的所有季度
	 * 
	 * @param StartDate
	 * @param endDate
	 * @return
	 */
	public static List<String> getQuarterBy(String StartDate, String beginQ,
			String endDate, String endQ) {
		DateFormat sdf = new SimpleDateFormat("yyyy-MM");
		Date date1 = null; // 开始日期
		Date dEnd = null; // 结束日期

		try {
			date1 = sdf.parse(StartDate);
			dEnd = sdf.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Calendar calBegin = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calBegin.setTime(date1);
		Calendar calEnd = Calendar.getInstance();
		// 使用给定的 Date 设置此 Calendar 的时间
		calEnd.setTime(dEnd);
		List<String> list = new ArrayList<String>();
		int beginY = calBegin.get(Calendar.YEAR);
		int beginYQ = Integer.parseInt(beginQ);
		int endY = calEnd.get(Calendar.YEAR);
		int endYQ = Integer.parseInt(endQ);
		do {
			list.add(beginY + "年第" + beginYQ + "季度");
			if (beginY == endY && beginYQ == endYQ) {
				return list;
			}
			beginYQ++;
			if (beginYQ > 4) {
				beginYQ = 1;
				beginY++;
			}
		} while (true);
	}

	/**
	 * 根据季度返回季度第一月
	 */
	public static int setMonthByQuarter(int quarter) {
		if (quarter == 1) {
			return 1;
		}
		if (quarter == 2) {
			return 4;
		}
		if (quarter == 3) {
			return 7;
		}
		if (quarter == 4) {
			return 10;
		}

		return 1;

	}

	/**
	 * 根据季度返回季度第一月或最后一月 0 起始月 1截止月
	 * 
	 */
	public static String setMonthByQuarterToString(int type, int quarter) {
		if (quarter == 1) {
			if (type == 1) {
				return "03";
			}
			return "01";
		}
		if (quarter == 2) {
			if (type == 1) {
				return "06";
			}
			return "04";
		}
		if (quarter == 3) {
			if (type == 1) {
				return "09";
			}
			return "07";
		}
		if (quarter == 4) {
			if (type == 1) {
				return "12";
			}
			return "10";
		}
		return "01";

	}

	/**
	 * 根据月份获取所在季度
	 * 
	 * @param month
	 * @return
	 */
	private static int getQuarterByMonth(int month) {
		int quarter = 1;
		if (month >= 1 && month <= 3) {
			return 1;
		}
		if (month >= 4 && month <= 6) {
			return 2;
		}
		if (month >= 7 && month <= 9) {
			return 3;
		}
		if (month >= 10 && month <= 12) {
			return 4;
		}

		return quarter;

	}

	public static String isAssign(String now, int assign) {
		Calendar fromCal = Calendar.getInstance();
		Date date = null;
		try {
			if (null == now) {
				now = getTime24();
			}
			date = getTimeFormat24().parse(now);
			fromCal.setTime(date);
			fromCal.add(Calendar.MINUTE, assign);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return getTimeFormat24().format(fromCal.getTime());
	}

	/**
	 * 返回文字描述的日期
	 * 
	 * @param date
	 * @return
	 */
	public static String getTimeFormatText(Date date) {
		if (date == null) {
			return null;
		}
		long diff = new Date().getTime() - date.getTime();
		long r = 0;
		if (diff > year) {
			r = (diff / year);
			return r + "年前";
		}
		if (diff > month) {
			r = (diff / month);
			return r + "个月前";
		}
		if (diff > day) {
			r = (diff / day);
			return r + "天前";
		}
		if (diff > hour) {
			r = (diff / hour);
			return r + "个小时前";
		}
		if (diff > minute) {
			r = (diff / minute);
			return r + "分钟前";
		}
		return "刚刚";
	}

	/**
	 * 
	 * 转换日期
	 * 
	 * @param date
	 * @return
	 */
	public static String getTimeCN(String date) {
		return getTimeFormatCN().format(format(date));
	}

	/**
	 * 
	 * 转换日期
	 * 
	 * @param date
	 * @return
	 */
	public static String getDayCN(String date) {
		return getDayFormatCN().format(format(date));
	}

	public static int getInterval(String beginVal, String endVal) {
		int day = 0;
		try {
			Date beginDate = getTimeFormat24().parse(beginVal);
			Date endDate = getTimeFormat24().parse(endVal);
			day = (int) ((endDate.getTime() - beginDate.getTime()) / 1000L);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return day;
	}

	/**
	 * 
	 * 转换星期
	 * 
	 * @param date
	 * @return
	 */
	public static String getWeekCN(String date) {
		int w = 0;
		try {
			Calendar cal = Calendar.getInstance();
			Date d = getTimeFormatYMD().parse(date);
			cal.setTime(d);
			w = cal.get(Calendar.DAY_OF_WEEK) - 1;
			if (w < 0)
				w = 0;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return WEEK[w];
	}

	/**
	 * 
	 * 比较时间(string类型)大小
	 * 
	 * @param date1
	 * @param date2
	 *            date1 大于date2 return 1 <br>
	 *            date1 小于date2 return -1 <br>
	 *            date1 等于date2 return 0 <br>
	 * @return
	 */
	public static int compareDate(String date1, String date2) {
		SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date dt1 = f.parse(date1);
			Date dt2 = f.parse(date2);
			if (dt1.getTime() > dt2.getTime()) {
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
				return -1;
			} else {
				return 0;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static String format(Date date) {
		long delta = new Date().getTime() - date.getTime();
		if (delta < 1L * ONE_MINUTE) {
			long seconds = toSeconds(delta);
			return (seconds <= 0 ? 1 : seconds) + ONE_SECOND_AGO;
		}
		if (delta < 45L * ONE_MINUTE) {
			long minutes = toMinutes(delta);
			return (minutes <= 0 ? 1 : minutes) + ONE_MINUTE_AGO;
		}
		if (delta < 24L * ONE_HOUR) {
			long hours = toHours(delta);
			return (hours <= 0 ? 1 : hours) + ONE_HOUR_AGO;
		}
		if (delta < 48L * ONE_HOUR) {
			return "昨天";
		}
		if (delta < 30L * ONE_DAY) {
			long days = toDays(delta);
			return (days <= 0 ? 1 : days) + ONE_DAY_AGO;
		}
		if (delta < 12L * 4L * ONE_WEEK) {
			long months = toMonths(delta);
			return (months <= 0 ? 1 : months) + ONE_MONTH_AGO;
		} else {
			long years = toYears(delta);
			return (years <= 0 ? 1 : years) + ONE_YEAR_AGO;
		}
	}

	private static long toSeconds(long date) {
		return date / 1000L;
	}

	private static long toMinutes(long date) {
		return toSeconds(date) / 60L;
	}

	private static long toHours(long date) {
		return toMinutes(date) / 60L;
	}

	private static long toDays(long date) {
		return toHours(date) / 24L;
	}

	private static long toMonths(long date) {
		return toDays(date) / 30L;
	}

	private static long toYears(long date) {
		return toMonths(date) / 365L;
	}

	public static SimpleDateFormat getTimeFormat24() {
		return new SimpleDateFormat(FORMAT_YYYY_MM_DD_HH_MM_SS_24);
	}

	public static SimpleDateFormat getTimeFormat12() {
		return new SimpleDateFormat(FORMAT_YYYY_MM_DD_HH_MM_SS_12);
	}

	public static SimpleDateFormat getTimeFormatHM24() {
		return new SimpleDateFormat(FORMAT_YYYY_MM_DD_HH_MM_24);
	}

	public static SimpleDateFormat getTimeFormatYMDHMS() {
		return new SimpleDateFormat(FORMAT_YYYMMDDHHMMSS_24);
	}

	public static SimpleDateFormat getTimeFormatHM12() {
		return new SimpleDateFormat(FORMAT_YYYY_MM_DD_HH_MM_12);
	}

	public static SimpleDateFormat getTimeHMS24() {
		return new SimpleDateFormat(FORMAT_HHMMSSSSS_24);
	}

	public static SimpleDateFormat getTimeHMS12() {
		return new SimpleDateFormat(FORMAT_HHMMSSSSS_12);
	}

	public static SimpleDateFormat getTimeFormatY() {
		return new SimpleDateFormat(FORMAT_YYYY);
	}

	public static SimpleDateFormat getTimeFormatYM() {
		return new SimpleDateFormat(FORMAT_YYYY_MM);
	}

	public static SimpleDateFormat getTimeFormatYMD() {
		return new SimpleDateFormat(FORMAT_YYYY_MM_DD);
	}

	public static SimpleDateFormat getTimeFormatMD() {
		return new SimpleDateFormat(FORMAT_MM_DD);
	}

	public static SimpleDateFormat getTimeFormatCN() {
		return new SimpleDateFormat(FORMAT_YYYY_MM_DD_CN);
	}

	public static SimpleDateFormat getDayFormatCN() {
		return new SimpleDateFormat(FORMAT_MM_DD_CN);
	}

	private final static String[] WEEK = { "星期日", "星期一", "星期二", "星期三", "星期四",
			"星期五", "星期六" };
	/** 24小时-年:月:日 时:分:秒 */
	private final static String FORMAT_YYYY_MM_DD_HH_MM_SS_24 = "yyyy-MM-dd HH:mm:ss";
	/** 12小时-年:月:日 时:分:秒 */
	private final static String FORMAT_YYYY_MM_DD_HH_MM_SS_12 = "yyyy-MM-dd hh:mm:ss";
	/** 24小时-年:月:日 时:分 */
	private final static String FORMAT_YYYY_MM_DD_HH_MM_24 = "yyyy-MM-dd HH:mm";
	/** 12小时-年:月:日 时:分 */
	private final static String FORMAT_YYYY_MM_DD_HH_MM_12 = "yyyy-MM-dd hh:mm";
	/** 24小时-时:分:秒:毫秒 */
	private final static String FORMAT_YYYMMDDHHMMSS_24 = "yyyyMMddhhmmss";
	/** 24小时-时:分:秒:毫秒 */
	private final static String FORMAT_HHMMSSSSS_24 = "HHmmssSSS";
	/** 12小时-时:分:秒:毫秒 */
	private final static String FORMAT_HHMMSSSSS_12 = "hhmmssSSS";
	/** 年:月:日 时:分 */
	private final static String FORMAT_YYYY = "yyyy";
	/** 年:月:日 时:分 */
	private final static String FORMAT_YYYY_MM = "yyyy-MM";
	/** 年:月:日 时:分 */
	private final static String FORMAT_YYYY_MM_DD = "yyyy-MM-dd";
	/** 年:月:日 时:分 */
	private final static String FORMAT_MM_DD = "MM-dd";
	/** 年:月:日 时:分 */
	private final static String FORMAT_YYYY_MM_DD_CN = "yyyy年M月d日";
	/** 年:月:日 时:分 */
	private final static String FORMAT_MM_DD_CN = "M月d日";
	/** 1分钟 */
	private final static long minute = 60 * 1000;
	/** 1小时 */
	private final static long hour = 60 * minute;
	/** 1天 */
	private final static long day = 24 * hour;
	/** 月 */
	private final static long month = 31 * day;
	/** 年 */
	private final static long year = 12 * month;

	private static final long ONE_MINUTE = 60000L;
	private static final long ONE_HOUR = 3600000L;
	private static final long ONE_DAY = 86400000L;
	private static final long ONE_WEEK = 604800000L;

	private static final String ONE_SECOND_AGO = "秒前";
	private static final String ONE_MINUTE_AGO = "分钟前";
	private static final String ONE_HOUR_AGO = "小时前";
	private static final String ONE_DAY_AGO = "天前";
	private static final String ONE_MONTH_AGO = "月前";
	private static final String ONE_YEAR_AGO = "年前";

}