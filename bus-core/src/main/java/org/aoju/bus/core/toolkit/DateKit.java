/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.core.toolkit;

import org.aoju.bus.core.convert.NumberChinese;
import org.aoju.bus.core.date.Between;
import org.aoju.bus.core.date.Boundary;
import org.aoju.bus.core.date.DateTime;
import org.aoju.bus.core.date.TimeInterval;
import org.aoju.bus.core.date.format.DateParser;
import org.aoju.bus.core.date.format.DatePeriod;
import org.aoju.bus.core.date.format.DatePrinter;
import org.aoju.bus.core.date.format.FormatBuilder;
import org.aoju.bus.core.lang.*;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.lang.System;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 时间工具类
 *
 * @author Kimi Liu
 * @version 6.0.3
 * @since JDK 1.8+
 */
public class DateKit {

    /**
     * 支持的最小年份
     */
    public final static int MIN_YEAR = 1850;
    /**
     * 支持的最大年份
     */
    public final static int MAX_YEAR = 2150;

    /**
     * 农历年，和公历是一样的
     */
    private int lyear;
    /**
     * 农历月，范围1-12
     */
    private int lmonth;
    /**
     * 农历日期
     */
    private int ldate;
    /**
     * 是否为闰月日期
     */
    private boolean isLeapMonth = false;
    /**
     * 农历这年闰月，如果不闰月，默认为0
     */
    private int leapMonth = 0;
    /**
     * 公历日期，公历月份范围：0-11
     */
    private GregorianCalendar solar = new GregorianCalendar();

    /**
     * 默认构造
     */
    public DateKit() {
        lunar(solar.get(Calendar.YEAR), solar.get(Calendar.MONTH), solar.get(Calendar.DATE));
    }

    /**
     * 通过农历年、月、日构造
     *
     * @param lyear       农历年
     * @param lmonth      农历月份,范围1-12
     * @param ldate       农历日
     * @param isleapMonth 是否闰月
     */
    public DateKit(int lyear, int lmonth, int ldate, boolean isleapMonth) {
        lunar(lyear, lmonth, ldate, isleapMonth);
    }

    /**
     * 通过公历构造
     *
     * @param calendar 　公历日期
     */
    public DateKit(Calendar calendar) {
        lunar(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
    }

    /**
     * 转换日期
     *
     * @param date 日期
     * @return 日期
     */
    public static String dateCN(String date) {
        return Fields.NORM_DATE_CN_FORMAT.format(date);
    }

    /**
     * 转换星期
     *
     * @param date 日期
     * @return 日期
     */
    public static String weekCN(String date) {
        try {
            Calendar cal = Calendar.getInstance();
            Date d = Fields.PURE_DATETIME_FORMAT.parse(date);
            cal.setTime(d);
            int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (w < 0) {
                w = 0;
            }
            return Fields.Week.of(w).toChinese("星期");
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 转换为{@link DateTime}对象
     *
     * @return 当前时间
     */
    public static DateTime date() {
        return new DateTime();
    }

    /**
     * {@link Date}类型时间转为{@link DateTime}
     *
     * @param date Long类型Date(Unix时间戳)
     * @return 时间对象
     */
    public static DateTime date(Date date) {
        if (date instanceof DateTime) {
            return (DateTime) date;
        }
        return new DateTime(date);
    }

    /**
     * Long类型时间转为{@link DateTime}
     * 同时支持10位秒级别时间戳和13位毫秒级别时间戳
     *
     * @param date Long类型Date(Unix时间戳)
     * @return 时间对象
     */
    public static DateTime date(long date) {
        return new DateTime(date);
    }

    /**
     * {@link Calendar}类型时间转为{@link DateTime}
     *
     * @param calendar {@link Calendar}
     * @return 时间对象
     */
    public static DateTime date(Calendar calendar) {
        return new DateTime(calendar);
    }

    /**
     * {@link TemporalAccessor}类型时间转为{@link DateTime}
     * 始终根据已有{@link TemporalAccessor} 产生新的{@link DateTime}对象
     *
     * @param temporalAccessor {@link TemporalAccessor}
     * @return 时间对象
     */
    public static DateTime date(TemporalAccessor temporalAccessor) {
        return new DateTime(temporalAccessor);
    }

    /**
     * 转换为Calendar对象
     *
     * @param date 日期对象
     * @return Calendar对象
     */
    public static Calendar calendar(Date date) {
        if (date instanceof DateTime) {
            return ((DateTime) date).toCalendar();
        } else {
            return calendar(date.getTime());
        }
    }

    /**
     * 转换为Calendar对象
     *
     * @param millis 时间戳
     * @return Calendar对象
     */
    public static Calendar calendar(long millis) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(millis);
        return cal;
    }

    /**
     * 当前时间,格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 当前时间的标准形式字符串
     */
    public static String now() {
        return formatDateTime(new DateTime());
    }

    /**
     * 当前时间毫秒数
     *
     * @return 当前时间毫秒数
     */
    public static long timestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 当前时间long
     *
     * @param isNano 是否为高精度时间
     * @return 时间
     */
    public static long timestamp(boolean isNano) {
        return isNano ? System.nanoTime() : System.currentTimeMillis();
    }

    /**
     * 获得年的部分
     *
     * @param date 日期
     * @return 年的部分
     */
    public static int year(Date date) {
        return DateTime.of(date).year();
    }

    /**
     * 获得指定日期所属季度,从1开始计数
     *
     * @param date 日期
     * @return 第几个季度
     */
    public static int quarter(Date date) {
        return DateTime.of(date).quarter();
    }

    /**
     * 获得指定日期所属季度
     *
     * @param date 日期
     * @return 第几个季度枚举
     */
    public static Fields.Quarter quarters(Date date) {
        return DateTime.of(date).quarterEnum();
    }

    /**
     * 获得月份,从0开始计数
     *
     * @param date 日期
     * @return 月份, 从0开始计数
     */
    public static int month(Date date) {
        return DateTime.of(date).month();
    }

    /**
     * 获得月份
     *
     * @param date 日期
     * @return month
     */
    public static Fields.Month months(Date date) {
        return DateTime.of(date).monthEnum();
    }

    /**
     * 获得指定日期是所在年份的第几周
     *
     * @param date 日期
     * @return 周
     */
    public static int weekOfYear(Date date) {
        return DateTime.of(date).weekOfYear();
    }

    /**
     * 获得指定日期是所在月份的第几周
     *
     * @param date 日期
     * @return 周
     */
    public static int weekOfMonth(Date date) {
        return DateTime.of(date).weekOfMonth();
    }

    /**
     * 获得指定日期是这个日期所在月份的第几天
     *
     * @param date 日期
     * @return 天
     */
    public static int dayOfMonth(Date date) {
        return DateTime.of(date).dayOfMonth();
    }

    /**
     * 获得指定日期是这个日期所在年的第几天
     *
     * @param date 日期
     * @return 天
     */
    public static int dayOfYear(Date date) {
        return DateTime.of(date).dayOfYear();
    }

    /**
     * 获得指定日期是星期几,1表示周日,2表示周一
     *
     * @param date 日期
     * @return 天
     */
    public static int dayOfWeek(Date date) {
        return DateTime.of(date).dayOfWeek();
    }

    /**
     * 获得指定日期是星期几
     *
     * @param date 日期
     * @return month
     */
    public static Fields.Week dayOfWeeks(Date date) {
        return DateTime.of(date).dayOfWeekEnum();
    }

    /**
     * 获得指定年份的总天数
     *
     * @param year 年份
     * @return 天
     */
    public static int lengthOfYear(int year) {
        return Year.of(year).length();
    }

    /**
     * 获得指定日期的小时数部分
     *
     * @param date          日期
     * @param is24HourClock 是否24小时制
     * @return 小时数
     */
    public static int hour(Date date, boolean is24HourClock) {
        return DateTime.of(date).hour(is24HourClock);
    }

    /**
     * 获得指定日期的分钟数部分
     * 例如：10:04:15.250 =》 4
     *
     * @param date 日期
     * @return 分钟数
     */
    public static int minute(Date date) {
        return DateTime.of(date).minute();
    }

    /**
     * 获得指定日期的秒数部分
     *
     * @param date 日期
     * @return 秒数
     */
    public static int second(Date date) {
        return DateTime.of(date).second();
    }

    /**
     * 获得指定日期的毫秒数部分
     *
     * @param date 日期
     * @return 毫秒数
     */
    public static int millsecond(Date date) {
        return DateTime.of(date).millsecond();
    }

    /**
     * 是否为上午
     *
     * @param date 日期
     * @return 是否为上午
     */
    public static boolean isAM(Date date) {
        return DateTime.of(date).isAM();
    }

    /**
     * 是否为上午
     *
     * @param calendar {@link Calendar}
     * @return 是否为上午
     */
    public static boolean isAM(Calendar calendar) {
        return Calendar.AM == calendar.get(Calendar.AM_PM);
    }

    /**
     * 是否为下午
     *
     * @param date 日期
     * @return 是否为下午
     */
    public static boolean isPM(Date date) {
        return DateTime.of(date).isPM();
    }

    /**
     * 是否为下午
     *
     * @param calendar {@link Calendar}
     * @return 是否为下午
     */
    public static boolean isPM(Calendar calendar) {
        return Calendar.PM == calendar.get(Calendar.AM_PM);
    }

    /**
     * @return 今年
     */
    public static int thisYear() {
        return year(date());
    }

    /**
     * @return 当前月份
     */
    public static int thisMonth() {
        return month(date());
    }

    /**
     * @return 当前月份
     */
    public static Fields.Month thisMonthEnum() {
        return months(date());
    }

    /**
     * @return 当前日期所在年份的第几周
     */
    public static int thisWeekOfYear() {
        return weekOfYear(date());
    }

    /**
     * @return 当前日期所在年份的第几周
     */
    public static int thisWeekOfMonth() {
        return weekOfMonth(date());
    }

    /**
     * @return 当前日期是这个日期所在月份的第几天
     */
    public static int thisDayOfMonth() {
        return dayOfMonth(date());
    }

    /**
     * @return 当前日期是星期几
     */
    public static int thisDayOfWeek() {
        return dayOfWeek(date());
    }

    /**
     * @return 当前日期是星期几
     */
    public static Fields.Week thisDayOfWeekEnum() {
        return dayOfWeeks(date());
    }

    /**
     * @param is24HourClock 是否24小时制
     * @return 当前日期的小时数部分
     */
    public static int thisHour(boolean is24HourClock) {
        return hour(date(), is24HourClock);
    }

    /**
     * @return 当前日期的分钟数部分
     */
    public static int thisMinute() {
        return minute(date());
    }

    /**
     * @return 当前日期的秒数部分
     */
    public static int thisSecond() {
        return second(date());
    }

    /**
     * @return 当前日期的毫秒数部分
     */
    public static int thisMillsecond() {
        return millsecond(date());
    }

    /**
     * 获得指定日期年份和季节
     * 格式：[20131]表示2013年第一季度
     *
     * @param date 日期
     * @return Quarter ,类似于 20132
     */
    public static String yearAndQuarter(Date date) {
        return yearAndQuarter(calendar(date));
    }

    /**
     * 获得指定日期年份和季节
     * 格式：[20131]表示2013年第一季度
     *
     * @param cal 日期
     */
    private static String yearAndQuarter(Calendar cal) {
        return new StringBuilder().append(cal.get(Calendar.YEAR)).append(cal.get(Calendar.MONTH) / 3 + 1).toString();
    }

    /**
     * 获得指定日期区间内的年份和季节
     *
     * @param startDate 起始日期(包含)
     * @param endDate   结束日期(包含)
     * @return 季度列表 ，元素类似于 20132
     */
    public static LinkedHashSet<String> yearAndQuarter(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return new LinkedHashSet<>(0);
        }
        return yearAndQuarter(startDate.getTime(), endDate.getTime());
    }

    /**
     * 获得指定日期区间内的年份和季节
     *
     * @param startDate 起始日期(包含)
     * @param endDate   结束日期(包含)
     * @return 季度列表 ，元素类似于 20132
     */
    public static LinkedHashSet<String> yearAndQuarter(long startDate, long endDate) {
        LinkedHashSet<String> quarters = new LinkedHashSet<>();
        final Calendar cal = calendar(startDate);
        while (startDate <= endDate) {
            // 如果开始时间超出结束时间，让结束时间为开始时间，处理完后结束循环
            quarters.add(yearAndQuarter(cal));

            cal.add(Calendar.MONTH, 3);
            startDate = cal.getTimeInMillis();
        }

        return quarters;
    }

    /**
     * 按照给定的通配模式 YYYY-MM-DD HH:MM:SS ,将时间格式化成相应的字符串
     *
     * @param date 待格式化的时间
     * @return 格式化成功返回成功后的字符串, 失败返回<b>null</b>
     */
    public static String format(Date date) {
        if (date != null) {
            SimpleDateFormat dstSdf = new SimpleDateFormat(Fields.NORM_DATETIME_PATTERN);
            return dstSdf.format(date);
        }
        return Normal.EMPTY;
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format 日期格式,常用格式见： {@link Fields}
     * @return 格式化后的字符串
     */
    public static String format(Date date, String format) {
        if (null == date || StringKit.isBlank(format)) {
            return null;
        }

        final SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (date instanceof DateTime) {
            final TimeZone timeZone = ((DateTime) date).getTimeZone();
            if (null != timeZone) {
                sdf.setTimeZone(timeZone);
            }
        }
        return format(date, sdf);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format {@link DatePrinter} 或 {@link FormatBuilder}
     * @return 格式化后的字符串
     */
    public static String format(Date date, DatePrinter format) {
        if (null == format || null == date) {
            return null;
        }
        return format.format(date);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format {@link SimpleDateFormat}
     * @return 格式化后的字符串
     */
    public static String format(Date date, DateFormat format) {
        if (null == format || null == date) {
            return null;
        }
        return format.format(date);
    }

    /**
     * 按照给定的通配模式,格式化成相应的时间字符串
     *
     * @param srcDate     原始时间字符串
     * @param srcPattern  原始时间通配符
     * @param destPattern 格式化成的时间通配符
     * @return 格式化成功返回成功后的字符串, 失败返回<b>""</b>
     */
    public static String format(String srcDate, String srcPattern, String destPattern) {
        try {
            SimpleDateFormat srcSdf = new SimpleDateFormat(srcPattern);
            SimpleDateFormat dstSdf = new SimpleDateFormat(destPattern);
            return dstSdf.format(srcSdf.parse(srcDate));
        } catch (ParseException e) {
            return Normal.EMPTY;
        }
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date 需要转换的日期 yyyy-MM-dd HH:mm:ss
     * @return long 时间戳
     */
    public static long format(String date) {
        try {
            return Fields.NORM_DATETIME_FORMAT.parse(date).getTime();
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }

    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp 时间戳
     * @return String 日期字符串
     */
    public static String format(long timestamp) {
        return Fields.NORM_DATETIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp 时间戳
     * @param format    格式
     * @return String 日期字符串
     */
    public static String format(long timestamp, String format) {
        return new SimpleDateFormat(format).format(new Date(timestamp));
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date   需要转换的日期
     * @param format 格式
     * @return long 时间戳
     */
    public static long format(String date, String format) {
        try {
            return new SimpleDateFormat(format).parse(date).getTime();
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 格式化日期时间
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param localDateTime 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime localDateTime) {
        return format(localDateTime, Fields.NORM_DATETIME_PATTERN);
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param localDateTime 被格式化的日期
     * @param format        日期格式，常用格式见： {@link Fields}
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime localDateTime, String format) {
        if (null == localDateTime || StringKit.isBlank(format)) {
            return null;
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(df);
    }

    /**
     * 格式化日期部分(不包括时间)
     * 格式 yyyy-MM-dd
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatDate(Date date) {
        if (null == date) {
            return null;
        }
        return Fields.NORM_DATE_FORMAT.format(date);
    }

    /**
     * 格式化为Http的标准日期格式
     *
     * @param date 被格式化的日期
     * @return HTTP标准形式日期字符串
     */
    public static String formatHttpDate(Date date) {
        if (null == date) {
            return null;
        }
        return Fields.HTTP_DATETIME_FORMAT.format(date);
    }

    /**
     * 格式化时间
     * 格式 HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     */
    public static String formatTime(Date date) {
        if (null == date) {
            return null;
        }
        return Fields.NORM_TIME_FORMAT.format(date);
    }

    /**
     * 格式化日期时间
     * 格式 yyyy-MM-dd HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的日期
     */
    public static String formatDateTime(Date date) {
        if (null == date) {
            return null;
        }
        return Fields.NORM_DATETIME_FORMAT.format(date);
    }

    /**
     * 格式化为中文日期格式，如果isUppercase为false
     * 则返回：2018年10月24日，否则,返回二〇一八年十月二十四日
     *
     * @param date        被格式化的日期
     * @param isUppercase 是否采用大写形式
     * @param withTime    是否包含时间部分
     * @return 中文日期字符串
     */
    public static String formatCNDate(Date date, boolean isUppercase, boolean withTime) {
        if (null == date) {
            return null;
        }

        if (false == isUppercase) {
            return (withTime ? Fields.NORM_CN_DATE_TIME_FORMAT : Fields.NORM_DATE_CN_FORMAT).format(date);
        }

        return formatCNDate(calendar(date), withTime);
    }

    /**
     * 将指定Calendar时间格式化为纯中文形式
     *
     * <pre>
     *     2018-02-24 12:13:14 转换为 二〇一八年二月二十四日（withTime为false）
     *     2018-02-24 12:13:14 转换为 二〇一八年二月二十四日一十二时一十三分一十四秒（withTime为true）
     * </pre>
     *
     * @param calendar {@link Calendar}
     * @param withTime 是否包含时间部分
     * @return 格式化后的字符串
     */
    public static String formatCNDate(Calendar calendar, boolean withTime) {
        final StringBuilder result = StringKit.builder();

        // 年
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        final int length = year.length();
        for (int i = 0; i < length; i++) {
            result.append(NumberChinese.toChinese(year.charAt(i), false));
        }
        result.append('年');

        // 月
        int month = calendar.get(Calendar.MONTH) + 1;
        result.append(NumberChinese.format(month, false));
        result.append('月');

        // 日
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        result.append(NumberChinese.format(day, false));
        result.append('日');

        if (withTime) {
            // 时
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            result.append(NumberChinese.format(hour, false));
            result.append('时');
            // 分
            int minute = calendar.get(Calendar.MINUTE);
            result.append(NumberChinese.format(minute, false));
            result.append('分');
            // 秒
            int second = calendar.get(Calendar.SECOND);
            result.append(NumberChinese.format(second, false));
            result.append('秒');
        }

        return result.toString().replace('零', '〇');
    }

    /**
     * 将日期字符串转换为{@link DateTime}对象，格式：
     * <ol>
     * <li>yyyy-MM-dd HH:mm:ss</li>
     * <li>yyyy/MM/dd HH:mm:ss</li>
     * <li>yyyy.MM.dd HH:mm:ss</li>
     * <li>yyyy年MM月dd日 HH时mm分ss秒</li>
     * <li>yyyy-MM-dd</li>
     * <li>yyyy/MM/dd</li>
     * <li>yyyy.MM.dd</li>
     * <li>HH:mm:ss</li>
     * <li>HH时mm分ss秒</li>
     * <li>yyyy-MM-dd HH:mm</li>
     * <li>yyyy-MM-dd HH:mm:ss.SSS</li>
     * <li>yyyyMMddHHmmss</li>
     * <li>yyyyMMddHHmmssSSS</li>
     * <li>yyyyMMdd</li>
     * <li>EEE, dd MMM yyyy HH:mm:ss z</li>
     * <li>EEE MMM dd HH:mm:ss zzz yyyy</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
     * </ol>
     *
     * @param dateCharSequence 日期字符串
     * @return 日期
     */
    public static DateTime parse(CharSequence dateCharSequence) {
        if (StringKit.isBlank(dateCharSequence)) {
            return null;
        }
        String dateStr = dateCharSequence.toString();
        // 去掉两边空格并去掉中文日期中的“日”和“秒”，以规范长度
        dateStr = StringKit.removeAll(dateStr.trim(), '日', '秒');
        int length = dateStr.length();

        if (MathKit.isNumber(dateStr)) {
            // 纯数字形式
            if (length == Fields.PURE_DATETIME_PATTERN.length()) {
                return parse(dateStr, Fields.PURE_DATETIME_FORMAT);
            } else if (length == Fields.PURE_DATETIME_MS_PATTERN.length()) {
                return parse(dateStr, Fields.PURE_DATETIME_MS_FORMAT);
            } else if (length == Fields.PURE_DATE_PATTERN.length()) {
                return parse(dateStr, Fields.PURE_DATE_FORMAT);
            } else if (length == Fields.PURE_TIME_PATTERN.length()) {
                return parse(dateStr, Fields.PURE_TIME_FORMAT);
            }
        } else if (PatternKit.isMatch(RegEx.TIME, dateStr)) {
            // HH:mm:ss 或者 HH:mm 时间格式匹配单独解析
            return parseTimeToday(dateStr);
        } else if (StringKit.containsAnyIgnoreCase(dateStr, Fields.WTB)) {
            // JDK的Date对象toString默认格式，类似于：
            // Tue Jan 07 15:22:15 +0800 2020
            // Wed Jan 08 00:00:00 CST 2020
            // Thu Jan 09 17:51:10 GMT+08:00 2020
            return parseCST(dateStr);
        } else if (StringKit.contains(dateStr, 'T')) {
            // UTC时间
            return parseUTC(dateStr);
        }

        // 含有单个位数数字的日期时间格式
        dateStr = normalize(dateStr);
        if (PatternKit.isMatch(Fields.REGEX_NORM, dateStr)) {
            final int colonCount = StringKit.count(dateStr, Symbol.COLON);
            switch (colonCount) {
                case 0:
                    // yyyy-MM-dd
                    return parse(dateStr, Fields.NORM_DATE_FORMAT);
                case 1:
                    // yyyy-MM-dd HH:mm
                    return parse(dateStr, Fields.NORM_DATETIME_MINUTE_FORMAT);
                case 2:
                    if (StringKit.contains(dateStr, Symbol.DOT)) {
                        // yyyy-MM-dd HH:mm:ss.SSS
                        return parse(dateStr, Fields.NORM_DATETIME_MS_FORMAT);
                    }
                    // yyyy-MM-dd HH:mm:ss
                    return parse(dateStr, Fields.NORM_DATETIME_FORMAT);
            }
        }

        // 没有更多匹配的时间格式
        throw new InstrumentException("No format fit for date String [{}] !", dateStr);
    }

    /**
     * 构建LocalDateTime对象
     *
     * @param dateStr 时间字符串(带格式)
     * @param format  使用{@link Fields}定义的格式
     * @return LocalDateTime对象
     */
    public static LocalDateTime parse(CharSequence dateStr, String format) {
        dateStr = normalize(dateStr);
        DateTimeFormatter df = DateTimeFormatter.ofPattern(format);
        try {
            return LocalDateTime.parse(dateStr, df);
        } catch (DateTimeParseException e) {
            // 在给定日期字符串没有时间部分时，LocalDateTime会报错，此时使用LocalDate中转转换
            return LocalDate.parse(dateStr, df).atStartOfDay();
        }
    }

    /**
     * 构建DateTime对象
     *
     * @param dateStr    Date字符串
     * @param dateFormat 格式化器 {@link SimpleDateFormat}
     * @return DateTime对象
     */
    public static DateTime parse(String dateStr, DateFormat dateFormat) {
        return new DateTime(dateStr, dateFormat);
    }

    /**
     * 构建DateTime对象
     *
     * @param dateStr Date字符串
     * @param parser  格式化器,{@link FormatBuilder}
     * @return DateTime对象
     */
    public static DateTime parse(String dateStr, DateParser parser) {
        return new DateTime(dateStr, parser);
    }

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param dateStr 特定格式的日期
     * @param format  格式,例如yyyy-MM-dd
     * @return 日期对象
     */
    public static DateTime parse(String dateStr, String format) {
        return new DateTime(dateStr, format);
    }

    /**
     * 解析日期字符串，忽略时分秒，支持的格式包括：
     * <pre>
     * yyyy-MM-dd
     * yyyy/MM/dd
     * yyyy.MM.dd
     * yyyy年MM月dd日
     * </pre>
     *
     * @param dateString 标准形式的日期字符串
     * @return 日期对象
     */
    public static DateTime parseDate(String dateString) {
        return parse(normalize(dateString), Fields.NORM_DATE_FORMAT);
    }

    /**
     * 解析时间,格式HH:mm:ss,默认为1970-01-01
     *
     * @param timeString 标准形式的日期字符串
     * @return 日期对象
     */
    public static DateTime parseTime(String timeString) {
        return parse(normalize(timeString), Fields.NORM_TIME_FORMAT);
    }

    /**
     * 解析日期时间字符串，格式支持：
     *
     * <pre>
     * yyyy-MM-dd HH:mm:ss
     * yyyy/MM/dd HH:mm:ss
     * yyyy.MM.dd HH:mm:ss
     * yyyy年MM月dd日 HH:mm:ss
     * </pre>
     *
     * @param dateString 标准形式的时间字符串
     * @return 日期对象
     */
    public static DateTime parseDateTime(String dateString) {
        return parse(normalize(dateString), Fields.NORM_DATETIME_FORMAT);
    }

    /**
     * 解析时间,格式HH:mm:ss,日期默认为今天
     *
     * @param timeString 标准形式的日期字符串
     * @return 日期对象
     */
    public static DateTime parseTimeToday(String timeString) {
        timeString = StringKit.format("{} {}", formatDate(new DateTime()), timeString);
        if (1 == StringKit.count(timeString, Symbol.C_COLON)) {
            // 时间格式为 HH:mm
            return parse(timeString, Fields.NORM_DATETIME_MINUTE_PATTERN);
        } else {
            // 时间格式为 HH:mm:ss
            return parse(timeString, Fields.NORM_DATETIME_FORMAT);
        }
    }

    /**
     * 解析CST时间，格式：
     * <ol>
     * <li>EEE MMM dd HH:mm:ss z yyyy(例如：Wed Aug 01 00:00:00 CST 2020)</li>
     * </ol>
     *
     * @param cstString UTC时间
     * @return 日期对象
     */
    public static DateTime parseCST(CharSequence cstString) {
        if (cstString == null) {
            return null;
        }

        return parse((String) cstString, Fields.JDK_DATETIME_FORMAT);
    }

    /**
     * 解析UTC时间，格式：
     * <ol>
     * <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z'</li>
     * <li>yyyy-MM-dd'T'HH:mm:ssZ</li>
     * <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ</li>
     * </ol>
     *
     * @param utcString UTC时间
     * @return 日期对象
     */
    public static DateTime parseUTC(String utcString) {
        if (utcString == null) {
            return null;
        }
        int length = utcString.length();
        if (StringKit.contains(utcString, 'Z')) {
            if (length == Fields.UTC_PATTERN.length() - 4) {
                // 格式类似：2020-01-15T05:32:30Z
                return parse(utcString, Fields.UTC_FORMAT);
            } else if (length == Fields.OUTPUT_MSEC_PATTERN.length() - 4) {
                // 格式类似：2020-01-15T05:32:30.999Z
                return parse(utcString, Fields.OUTPUT_MSEC_FORMAT);
            }
        } else {
            if (length == Fields.WITH_ZONE_OFFSET_PATTERN.length() + 2 || length == Fields.WITH_ZONE_OFFSET_PATTERN.length() + 3) {
                // 格式类似：2020-01-15T05:32:30+0800 或 2020-01-15T05:32:30+08:00
                return parse(utcString, FormatBuilder.getInstance("yyyy-MM-dd'T'HH:mm:ssZ", TimeZone.getTimeZone("UTC")));
            } else if (length == Fields.MSEC_PATTERN.length() + 2 || length == Fields.MSEC_PATTERN.length() + 3) {
                // 格式类似：2020-01-15T05:32:30.999+0800 或 2020-01-15T05:32:30.999+08:00
                return parse(utcString, Fields.MSEC_FORMAT);
            } else if (length == Fields.UTC_SIMPLE_PATTERN.length() - 2) {
                // 格式类似：2020-07-07T15:31:20
                return parse(utcString, Fields.UTC_SIMPLE_FORMAT);
            }
        }
        // 没有更多匹配的时间格式
        throw new InstrumentException("No format fit for date String [{}] !", utcString);
    }

    /**
     * 修改日期为某个时间字段起始时间
     *
     * @param date      {@link Date}
     * @param dateField 时间字段
     * @return {@link DateTime}
     */
    public static DateTime truncate(Date date, Fields.DateField dateField) {
        return new DateTime(truncate(calendar(date), dateField));
    }

    /**
     * 修改日期为某个时间字段起始时间
     *
     * @param calendar  {@link Calendar}
     * @param dateField 时间字段
     * @return 原{@link Calendar}
     */
    public static Calendar truncate(Calendar calendar, Fields.DateField dateField) {
        return modify(calendar, dateField.getValue(), Fields.ModifyType.TRUNCATE);
    }


    /**
     * 修改日期为某个时间字段四舍五入时间
     *
     * @param date      {@link Date}
     * @param dateField 时间字段
     * @return {@link DateTime}
     */
    public static DateTime round(Date date, Fields.DateField dateField) {
        return new DateTime(round(calendar(date), dateField));
    }

    /**
     * 修改日期为某个时间字段四舍五入时间
     *
     * @param calendar  {@link Calendar}
     * @param dateField 时间字段
     * @return 原{@link Calendar}
     */
    public static Calendar round(Calendar calendar, Fields.DateField dateField) {
        return modify(calendar, dateField.getValue(), Fields.ModifyType.ROUND);
    }

    /**
     * 修改日期为某个时间字段结束时间
     *
     * @param date      {@link Date}
     * @param dateField 时间字段
     * @return {@link DateTime}
     */
    public static DateTime ceiling(Date date, Fields.DateField dateField) {
        return new DateTime(ceiling(calendar(date), dateField));
    }

    /**
     * 修改日期为某个时间字段结束时间
     *
     * @param calendar  {@link Calendar}
     * @param dateField 时间字段
     * @return 原{@link Calendar}
     */
    public static Calendar ceiling(Calendar calendar, Fields.DateField dateField) {
        return modify(calendar, dateField.getValue(), Fields.ModifyType.CEILING);
    }

    /**
     * 获取秒级别的开始时间，即忽略毫秒部分
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfSecond(Date date) {
        return new DateTime(beginOfSecond(calendar(date)));
    }

    /**
     * 获取秒级别的结束时间，即毫秒设置为999
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfSecond(Date date) {
        return new DateTime(endOfSecond(calendar(date)));
    }

    /**
     * 获取秒级别的开始时间，即忽略毫秒部分
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfSecond(Calendar calendar) {
        return truncate(calendar, Fields.DateField.SECOND);
    }

    /**
     * 获取秒级别的结束时间，即毫秒设置为999
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfSecond(Calendar calendar) {
        return ceiling(calendar, Fields.DateField.SECOND);
    }

    /**
     * 获取某天的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfDay(Date date) {
        return new DateTime(beginOfDay(calendar(date)));
    }

    /**
     * 获取某天的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfDay(Date date) {
        return new DateTime(endOfDay(calendar(date)));
    }

    /**
     * 获取某天的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfDay(Calendar calendar) {
        return truncate(calendar, Fields.DateField.DAY_OF_MONTH);
    }

    /**
     * 获取某天的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfDay(Calendar calendar) {
        return ceiling(calendar, Fields.DateField.DAY_OF_MONTH);
    }

    /**
     * 获取某周的开始时间，周一定为一周的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfWeek(Date date) {
        return new DateTime(beginOfWeek(calendar(date)));
    }

    /**
     * 获取某周的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfWeek(Date date) {
        return new DateTime(endOfWeek(calendar(date)));
    }

    /**
     * 获取给定日期当前周的开始时间，周一定为一周的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfWeek(Calendar calendar) {
        return beginOfWeek(calendar, true);
    }

    /**
     * 获取给定日期当前周的开始时间
     *
     * @param calendar           日期 {@link Calendar}
     * @param isMondayAsFirstDay 是否周一做为一周的第一天(false表示周日做为第一天)
     * @return {@link Calendar}
     */
    public static Calendar beginOfWeek(Calendar calendar, boolean isMondayAsFirstDay) {
        calendar.setFirstDayOfWeek(isMondayAsFirstDay ? Calendar.MONDAY : Calendar.SUNDAY);
        // WEEK_OF_MONTH为上限的字段(不包括)，实际调整的为DAY_OF_MONTH
        return truncate(calendar, Fields.DateField.WEEK_OF_MONTH);
    }

    /**
     * 获取某周的结束时间，周日定为一周的结束
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfWeek(Calendar calendar) {
        return endOfWeek(calendar, true);
    }

    /**
     * 获取某周的结束时间
     *
     * @param calendar          日期 {@link Calendar}
     * @param isSundayAsLastDay 是否周日做为一周的最后一天(false表示周六做为最后一天)
     * @return {@link Calendar}
     */
    public static Calendar endOfWeek(Calendar calendar, boolean isSundayAsLastDay) {
        calendar.setFirstDayOfWeek(isSundayAsLastDay ? Calendar.MONDAY : Calendar.SUNDAY);
        // WEEK_OF_MONTH为上限的字段(不包括)，实际调整的为DAY_OF_MONTH
        return ceiling(calendar, Fields.DateField.WEEK_OF_MONTH);
    }

    /**
     * 获取某月的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfMonth(Date date) {
        return new DateTime(beginOfMonth(calendar(date)));
    }

    /**
     * 获取某月的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfMonth(Date date) {
        return new DateTime(endOfMonth(calendar(date)));
    }

    /**
     * 获取某月的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfMonth(Calendar calendar) {
        return truncate(calendar, Fields.DateField.MONTH);
    }

    /**
     * 获取某月的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfMonth(Calendar calendar) {
        return ceiling(calendar, Fields.DateField.MONTH);
    }

    /**
     * 获取某季度的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfQuarter(Date date) {
        return new DateTime(beginOfQuarter(calendar(date)));
    }

    /**
     * 获取某季度的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfQuarter(Date date) {
        return new DateTime(endOfQuarter(calendar(date)));
    }

    /**
     * 获取某季度的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfQuarter(Calendar calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Fields.DateField.MONTH.getValue()) / 3 * 3);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return beginOfDay(calendar);
    }

    /**
     * 获取某季度的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfQuarter(Calendar calendar) {
        calendar.set(Calendar.MONTH, calendar.get(Fields.DateField.MONTH.getValue()) / 3 * 3 + 2);
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return endOfDay(calendar);
    }

    /**
     * 获取某年的开始时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime beginOfYear(Date date) {
        return new DateTime(beginOfYear(calendar(date)));
    }

    /**
     * 获取某年的结束时间
     *
     * @param date 日期
     * @return {@link DateTime}
     */
    public static DateTime endOfYear(Date date) {
        return new DateTime(endOfYear(calendar(date)));
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param cal1 日期1
     * @param cal2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) &&
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA);
    }

    /**
     * 获取某年的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfYear(Calendar calendar) {
        return truncate(calendar, Fields.DateField.YEAR);
    }

    /**
     * 获取某年的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfYear(Calendar calendar) {
        return ceiling(calendar, Fields.DateField.YEAR);
    }

    /**
     * 昨天
     *
     * @return 昨天
     */
    public static DateTime yesterday() {
        return offsetDay(new DateTime(), -1);
    }

    /**
     * 明天
     *
     * @return 明天
     */
    public static DateTime tomorrow() {
        return offsetDay(new DateTime(), 1);
    }

    /**
     * 上周
     *
     * @return 上周
     */
    public static DateTime lastWeek() {
        return offsetWeek(new DateTime(), -1);
    }

    /**
     * 下周
     *
     * @return 下周
     */
    public static DateTime nextWeek() {
        return offsetWeek(new DateTime(), 1);
    }

    /**
     * 上个月
     *
     * @return 上个月
     */
    public static DateTime lastMonth() {
        return offsetMonth(new DateTime(), -1);
    }

    /**
     * 下个月
     *
     * @return 下个月
     */
    public static DateTime nextMonth() {
        return offsetMonth(new DateTime(), 1);
    }

    /**
     * 偏移毫秒数
     *
     * @param date   日期
     * @param offset 偏移毫秒数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMillisecond(Date date, int offset) {
        return offset(date, Fields.DateField.MILLISECOND, offset);
    }

    /**
     * 偏移秒数
     *
     * @param date   日期
     * @param offset 偏移秒数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetSecond(Date date, int offset) {
        return offset(date, Fields.DateField.SECOND, offset);
    }

    /**
     * 偏移分钟
     *
     * @param date   日期
     * @param offset 偏移分钟数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMinute(Date date, int offset) {
        return offset(date, Fields.DateField.MINUTE, offset);
    }

    /**
     * 偏移小时
     *
     * @param date   日期
     * @param offset 偏移小时数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetHour(Date date, int offset) {
        return offset(date, Fields.DateField.HOUR_OF_DAY, offset);
    }

    /**
     * 偏移天
     *
     * @param date   日期
     * @param offset 偏移天数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetDay(Date date, int offset) {
        return offset(date, Fields.DateField.DAY_OF_YEAR, offset);
    }

    /**
     * 偏移周
     *
     * @param date   日期
     * @param offset 偏移周数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetWeek(Date date, int offset) {
        return offset(date, Fields.DateField.WEEK_OF_YEAR, offset);
    }

    /**
     * 偏移月
     *
     * @param date   日期
     * @param offset 偏移月数,正数向未来偏移,负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMonth(Date date, int offset) {
        return offset(date, Fields.DateField.MONTH, offset);
    }

    /**
     * 获取指定日期偏移指定时间后的时间
     *
     * @param date      基准日期
     * @param dateField 偏移的粒度大小(小时、天、月等)
     * @param offset    偏移量,正数为向后偏移,负数为向前偏移
     * @return 偏移后的日期
     */
    public static DateTime offset(Date date, Fields.DateField dateField, int offset) {
        return new DateTime(date).offset(dateField, offset);
    }

    /**
     * 判断两个日期相差的时长,只保留绝对值
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param unit      相差的单位
     * @return 日期差
     */
    public static long between(Date beginDate, Date endDate, Fields.Time unit) {
        return between(beginDate, endDate, unit, true);
    }

    /**
     * 判断两个日期相差的时长
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param unit      相差的单位
     * @param isAbs     日期间隔是否只保留绝对值正数
     * @return 日期差
     */
    public static long between(Date beginDate, Date endDate, Fields.Time unit, boolean isAbs) {
        return new Between(beginDate, endDate, isAbs).between(unit);
    }

    /**
     * 判断两个日期相差的毫秒数
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return 日期差
     */
    public static long betweenMs(Date beginDate, Date endDate) {
        return new Between(beginDate, endDate).between(Fields.Time.MS);
    }

    /**
     * 判断两个日期相差的天数
     *
     * <pre>
     * 有时候我们计算相差天数的时候需要忽略时分秒
     * 比如：2016-02-01 23:59:59和2016-02-02 00:00:00相差一秒
     * 如果isReset为false相差天数为0
     * 如果isReset为true相差天数将被计算为1
     * </pre>
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间
     * @return 日期差
     */
    public static long betweenDay(Date beginDate, Date endDate, boolean isReset) {
        if (isReset) {
            beginDate = beginOfDay(beginDate);
            endDate = beginOfDay(endDate);
        }
        return between(beginDate, endDate, Fields.Time.DAY);
    }

    /**
     * 计算两个日期相差月数
     * 在非重置情况下,如果起始日期的天小于结束日期的天,月数要少算1(不足1个月)
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间(重置天时分秒)
     * @return 相差月数
     */
    public static long betweenMonth(Date beginDate, Date endDate, boolean isReset) {
        return new Between(beginDate, endDate).betweenMonth(isReset);
    }

    /**
     * 计算两个日期相差年数
     * 在非重置情况下,如果起始日期的月小于结束日期的月,年数要少算1(不足1年)
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间(重置月天时分秒)
     * @return 相差年数
     */
    public static long betweenYear(Date beginDate, Date endDate, boolean isReset) {
        return new Between(beginDate, endDate).betweenYear(isReset);
    }

    /**
     * 格式化日期间隔输出
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param level     级别,按照天、小时、分、秒、毫秒分为5个等级
     * @return XX天XX小时XX分XX秒
     */
    public static String formatBetween(Date beginDate, Date endDate, Fields.Level level) {
        return formatBetween(between(beginDate, endDate, Fields.Time.MS), level);
    }

    /**
     * 格式化日期间隔输出,精确到毫秒
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return XX天XX小时XX分XX秒
     */
    public static String formatBetween(Date beginDate, Date endDate) {
        return formatBetween(between(beginDate, endDate, Fields.Time.MS));
    }

    /**
     * 格式化日期间隔输出
     *
     * @param betweenMs 日期间隔
     * @param level     级别,按照天、小时、分、秒、毫秒分为5个等级
     * @return XX天XX小时XX分XX秒XX毫秒
     */
    public static String formatBetween(long betweenMs, Fields.Level level) {
        return new DatePeriod(betweenMs, level).format();
    }

    /**
     * 格式化日期间隔输出,精确到毫秒
     *
     * @param betweenMs 日期间隔
     * @return XX天XX小时XX分XX秒XX毫秒
     */
    public static String formatBetween(long betweenMs) {
        return new DatePeriod(betweenMs, Fields.Level.MILLISECOND).format();
    }

    /**
     * 计时,常用于记录某段代码的执行时间,单位：纳秒
     *
     * @param preTime 之前记录的时间
     * @return 时间差, 纳秒
     */
    public static long spendNt(long preTime) {
        return System.nanoTime() - preTime;
    }

    /**
     * 计时,常用于记录某段代码的执行时间,单位：毫秒
     *
     * @param preTime 之前记录的时间
     * @return 时间差, 毫秒
     */
    public static long spendMs(long preTime) {
        return System.currentTimeMillis() - preTime;
    }

    /**
     * 格式化成yyMMddHHmm后转换为int型
     *
     * @param date 日期
     * @return int
     */
    public static int toIntSecond(Date date) {
        return Integer.parseInt(DateKit.format(date, "yyMMddHHmm"));
    }

    /**
     * 计算指定指定时间区间内的周数
     *
     * @param start 开始时间
     * @param end   结束时间
     * @return 周数
     */
    public static int weekCount(Date start, Date end) {
        final Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTime(start);
        final Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTime(end);

        final int startWeekofYear = startCalendar.get(Calendar.WEEK_OF_YEAR);
        final int endWeekofYear = endCalendar.get(Calendar.WEEK_OF_YEAR);

        int count = endWeekofYear - startWeekofYear + 1;

        if (Calendar.SUNDAY != startCalendar.get(Calendar.DAY_OF_WEEK)) {
            count--;
        }

        return count;
    }

    /**
     * 计时器
     * 计算某个过程花费的时间,精确到毫秒
     *
     * @return Timer
     */
    public static TimeInterval timer() {
        return new TimeInterval();

    }

    /**
     * 是否闰年
     *
     * @param year 年
     * @return 是否闰年
     */
    public static boolean isLeapYear(int year) {
        return new GregorianCalendar().isLeapYear(year);
    }

    /**
     * 判定在指定检查时间是否过期
     * 当前日期是否在日期指定范围内
     * 起始日期和结束日期可以互换
     *
     * @param date      被检查的日期
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return 是否在范围内
     */
    public static boolean isIn(Date date, Date beginDate, Date endDate) {
        if (date instanceof DateTime) {
            return ((DateTime) date).isIn(beginDate, endDate);
        } else {
            return new DateTime(date).isIn(beginDate, endDate);
        }
    }

    /**
     * 生日转为年龄，计算法定年龄
     *
     * @param birthDay 生日，标准日期字符串
     * @return 年龄
     */
    public static int ageOfNow(String birthDay) {
        return ageOfNow(parse(birthDay));
    }

    /**
     * 生日转为年龄，计算法定年龄
     *
     * @param birthDay 生日
     * @return 年龄
     */
    public static int ageOfNow(Date birthDay) {
        return getAge(birthDay, date());
    }

    /**
     * 出生日期转年龄
     *
     * @param birthday 时间戳字符串
     * @return int 年龄
     */
    public static int getAge(String birthday) {
        return getAge(Long.parseLong(birthday), Calendar.getInstance().getTimeInMillis());
    }

    /**
     * 计算相对于dateToCompare的年龄，长用于计算指定生日在某年的年龄
     *
     * @param birthday      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int getAge(Date birthday, Date dateToCompare) {
        Assert.notNull(birthday, "Birthday can not be null !");
        if (null == dateToCompare) {
            dateToCompare = date();
        }
        return getAge(birthday.getTime(), dateToCompare.getTime());
    }

    /**
     * 计算相对于dateToCompare的年龄，长用于计算指定生日在某年的年龄
     *
     * @param birthday      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int getAge(Calendar birthday, Calendar dateToCompare) {
        return getAge(birthday.getTimeInMillis(), dateToCompare.getTimeInMillis());
    }

    /**
     * 计算相对于dateToCompare的年龄,长用于计算指定生日在某年的年龄
     *
     * @param birthDay      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int getAge(long birthDay, long dateToCompare) {
        if (birthDay > dateToCompare) {
            throw new IllegalArgumentException("Birthday is after dateToCompare!");
        }

        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(dateToCompare);

        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
        final boolean isLastDayOfMonth = dayOfMonth == cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.setTimeInMillis(birthDay);
        int age = year - cal.get(Calendar.YEAR);

        final int monthBirth = cal.get(Calendar.MONTH);
        if (month == monthBirth) {

            final int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
            final boolean isLastDayOfMonthBirth = dayOfMonthBirth == cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            if ((false == isLastDayOfMonth || false == isLastDayOfMonthBirth) && dayOfMonth < dayOfMonthBirth) {
                // 如果生日在当月，但是未达到生日当天的日期，年龄减一
                age--;
            }
        } else if (month < monthBirth) {
            // 如果当前月份未达到生日的月份，年龄计算减一
            age--;
        }

        return age;
    }

    /**
     * 两个时间比较
     *
     * @param date 日期
     * @return 时间差
     */
    public static int compareWithNow(Date date) {
        return date.compareTo(new Date());
    }

    /**
     * 两个时间比较(时间戳比较)
     *
     * @param date 日期
     * @return 时间差
     */
    public static int compareWithNow(long date) {
        long now = timestamp();
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
     * @param object 字符串日期
     * @return the boolean
     */
    public static boolean compareWithNow(String object) {
        long expired = timestamp() - (Long.parseLong(object) * 1000);
        return expired <= 900000 && expired >= -900000;
    }

    /**
     * 获取年份时间段内的所有年
     *
     * @param StartDate 开始时间
     * @param endDate   截止时间
     * @return the list
     */
    public static List<String> getYears(String StartDate, String endDate) {
        List<String> list = new ArrayList<>();
        try {
            DateFormat df = new SimpleDateFormat(Fields.NORM_YEAR_PATTERN);
            Date date1 = df.parse(StartDate);
            Date date2 = df.parse(endDate);
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();

            list.add(df.format(date1));
            c1.setTime(date1);
            c2.setTime(date2);
            while (c1.compareTo(c2) < 0) {
                c1.add(Calendar.YEAR, 1);
                Date ss = c1.getTime();
                String str = df.format(ss);
                list.add(str);
            }
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return list;
    }


    /**
     * (季度) 计算本期的上期起止时间和同期的起止时间 返回的mao key 时间起止：beginkey endkey 季度起止： beginWkey
     * endWkey 本期的时间起止：begin end 季度：beginW endW type 0本期 1上期 2去年同期 季度
     *
     * @param type      计算上期
     * @param beginkey  开始时间key
     * @param endkey    截止时间key
     * @param beginWkey 开始周key
     * @param endWkey   截止周key
     * @param begin     开始时间
     * @param end       截止时间
     * @param beginW    开始周
     * @param endW      截止周
     * @return the map
     */
    public static Map<String, String> getQuarters(int type,
                                                  String beginkey,
                                                  String endkey,
                                                  String beginWkey,
                                                  String endWkey,
                                                  String begin,
                                                  String end,
                                                  String beginW,
                                                  String endW) {
        Map<String, String> map = new HashMap<>();
        try {
            DateFormat sdf = new SimpleDateFormat(Fields.NORM_YEAR_PATTERN);
            Date date1 = sdf.parse(begin);
            Date dEnd = sdf.parse(end);
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            calBegin.set(Calendar.MONTH,
                    setMonthByQuarter(Integer.parseInt(beginW)));
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            calEnd.set(Calendar.MONTH,
                    setMonthByQuarter(Integer.parseInt(endW)));

            if (type == 1) {
                int quarter = ((Integer.parseInt(end) - Integer.parseInt(begin))
                        * 4
                        + (Integer.parseInt(endW) - Integer.parseInt(beginW)) + 1) * 3;

                calBegin.add(Calendar.MONTH, -quarter);
                calEnd.add(Calendar.MONTH, -quarter);
                map.put(beginWkey, String.valueOf(getQuarterByMonth(calBegin
                        .get(Calendar.MONTH))));
                map.put(endWkey, String.valueOf(getQuarterByMonth(calEnd
                        .get(Calendar.MONTH))));
            } else if (type == 2) {
                calBegin.add(Calendar.YEAR, -1);
                calEnd.add(Calendar.YEAR, -1);

                map.put(beginWkey, beginW);
                map.put(endWkey, endW);
            }
            map.put(beginkey,
                    calBegin.get((Calendar.YEAR))
                            + Symbol.HYPHEN
                            + setMonthByQuarterToString(0,
                            Integer.parseInt(map.get(beginWkey))));
            map.put(endkey,
                    calEnd.get((Calendar.YEAR))
                            + Symbol.HYPHEN
                            + setMonthByQuarterToString(1,
                            Integer.parseInt(map.get(endWkey))));
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return map;
    }

    /**
     * (季度)获取季度份时间段内的所有季度
     *
     * @param StartDate 开始日期
     * @param beginQ    开始季度
     * @param endDate   截止日期
     * @param endQ      结束季度
     * @return the list
     */
    public static List<String> getQuarters(String StartDate,
                                           String beginQ,
                                           String endDate,
                                           String endQ) {
        try {
            DateFormat sdf = new SimpleDateFormat(Fields.NORM_YEAR_MTOTH_PATTERN);
            Date date1 = sdf.parse(StartDate);
            Date dEnd = sdf.parse(endDate);

            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            List<String> list = new ArrayList<>();
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

        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 计算去年同期和上期的起止时间
     *
     * @param type     计算上期
     * @param beginkey 开始时间key
     * @param endkey   截止时间key
     * @param begin    开始时间
     * @param end      截止时间
     * @return the map
     */
    public static Map<String, String> getLast(int type,
                                              String beginkey,
                                              String endkey,
                                              String begin,
                                              String end) {
        Map<String, String> map = new HashMap<>();
        try {
            Date dBegin = Fields.PURE_DATETIME_FORMAT.parse(begin);
            Date dEnd = Fields.PURE_DATETIME_FORMAT.parse(end);
            Calendar calBegin = Calendar.getInstance();

            calBegin.setTime(dBegin);
            Calendar calEnd = Calendar.getInstance();

            calEnd.setTime(dEnd);
            if (type == 1) {

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
                calBegin.add(Calendar.DATE, -day);
                calEnd.add(Calendar.DATE, -day);
            } else if (type == 2) {
                calBegin.add(Calendar.YEAR, -1);
                calEnd.add(Calendar.YEAR, -1);
            }
            map.put(beginkey, Fields.PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            map.put(endkey, Fields.PURE_DATETIME_FORMAT.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return map;
    }

    /**
     * 计算时间段内的所有的天
     * type:0本期1上期2去年同期
     *
     * @param begin 起始日期
     * @param end   截止日期
     * @return the list
     */
    public static List<String> getLast(String begin, String end) {
        List<String> lDate = new ArrayList<>();
        Date date1;
        Date dEnd;
        try {
            date1 = Fields.PURE_DATETIME_FORMAT.parse(begin);
            dEnd = Fields.PURE_DATETIME_FORMAT.parse(end);
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            lDate.add(Fields.PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            while (calBegin.compareTo(calEnd) < 0) {
                calBegin.add(Calendar.DAY_OF_MONTH, 1);
                Date ss = calBegin.getTime();
                String str = Fields.PURE_DATETIME_FORMAT.format(ss);
                lDate.add(str);
            }
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return lDate;
    }

    /**
     * (周)计算(周) 上期和去年同期的起止日期和起止周 计算上期的起止时间 和去年同期 type 0本期 1上期 2去年同期 起始日期key
     * beginkey endkey 起始日期的起止周key beginWkey endWkey 本期：begin end 本期起止周
     * beginW、endW
     *
     * @param type      计算上期
     * @param beginkey  开始时间key
     * @param endkey    截止时间key
     * @param beginWkey 开始周key
     * @param endWkey   截止周key
     * @param begin     开始时间
     * @param end       截止时间
     * @param beginW    开始周
     * @param endW      截止周
     * @return the map
     */
    public static Map<String, String> getLast(int type, String beginkey,
                                              String endkey, String beginWkey, String endWkey, String begin,
                                              String end, String beginW, String endW) {
        Map<String, String> map = new HashMap<>();
        try {
            Date date1 = Fields.PURE_DATETIME_FORMAT.parse(begin);
            Date dEnd = Fields.PURE_DATETIME_FORMAT.parse(end);
            Calendar calBegin = Calendar.getInstance();

            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            calBegin.setFirstDayOfWeek(Calendar.MONDAY);
            calEnd.setFirstDayOfWeek(Calendar.MONDAY);
            if (type == 1) {
                int week = getWeeksCount(date1, dEnd);
                calBegin.add(Calendar.WEEK_OF_YEAR, -week);
                calEnd.add(Calendar.WEEK_OF_YEAR, -week);
                map.put(beginWkey,
                        String.valueOf(calBegin.get(Calendar.WEEK_OF_YEAR)));
                map.put(endWkey,
                        String.valueOf(calEnd.get(Calendar.WEEK_OF_YEAR)));
                int day_of_week = calBegin.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week == 0)
                    day_of_week = 7;
                calBegin.add(Calendar.DATE, -day_of_week + 1);
                int day_of_week_end = calEnd.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week_end == 0)
                    day_of_week_end = 7;
                calEnd.add(Calendar.DATE, -day_of_week_end + 7);
            } else if (type == 2) {
                calBegin.add(Calendar.YEAR, -1);
                calEnd.add(Calendar.YEAR, -1);

                calBegin.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(beginW));
                calEnd.set(Calendar.WEEK_OF_YEAR, Integer.parseInt(endW));
                map.put(beginWkey, beginW);
                map.put(endWkey, endW);

                int day_of_week = calBegin.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week == 0)
                    day_of_week = 7;
                calBegin.add(Calendar.DATE, -day_of_week + 1);

                int day_of_week_end = calEnd.get(Calendar.DAY_OF_WEEK) - 1;
                if (day_of_week_end == 0)
                    day_of_week_end = 7;
                calEnd.add(Calendar.DATE, -day_of_week_end + 7);
            }
            map.put(beginkey, Fields.PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            map.put(endkey, Fields.PURE_DATETIME_FORMAT.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return map;
    }

    /**
     * (年)计算本期(年)的上期
     *
     * @param beginkey 开始时间key
     * @param endkey   截止时间key
     * @param begin    开始时间
     * @param end      截止时间
     * @return the map
     */
    public static Map<String, String> getLast(String beginkey,
                                              String endkey,
                                              String begin,
                                              String end) {
        Map<String, String> map = new HashMap<>();
        try {
            DateFormat sdf = new SimpleDateFormat(Fields.NORM_YEAR_PATTERN);
            Date date1 = sdf.parse(begin);
            Date dEnd = sdf.parse(end);
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            int year = calBegin.get(Calendar.YEAR);
            int year1 = calEnd.get(Calendar.YEAR);
            int result;
            result = year1 - year + 1;
            calBegin.add(Calendar.YEAR, -result);
            calEnd.add(Calendar.YEAR, -result);
            map.put(beginkey, sdf.format(calBegin.getTime()));
            map.put(endkey, sdf.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return map;
    }

    /**
     * 当时间段内的所有月份
     *
     * @param StartDate 开始日期
     * @param endDate   结束日期
     * @return the list
     */
    public static List<String> getMonths(String StartDate, String endDate) {
        List<String> list = new ArrayList<>();
        try {
            DateFormat df = new SimpleDateFormat(Fields.NORM_YEAR_MTOTH_PATTERN);

            Date date1 = df.parse(StartDate);
            Date date2 = df.parse(endDate);
            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();

            list.add(df.format(date1));
            c1.setTime(date1);
            c2.setTime(date2);
            while (c1.compareTo(c2) < 0) {
                c1.add(Calendar.MONTH, 1);
                Date ss = c1.getTime();
                String str = df.format(ss);
                list.add(str);
            }
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return list;
    }

    /**
     * (月)计算本期的上期和去年同期 1 上期 2同期 返回的mapkay beginkey endkey 本期起止：begin end
     * 计算上期的起止时间 和去年同期 type 0本期 1上期 2去年同期
     *
     * @param type     计算上期
     * @param beginkey 开始时间key
     * @param endkey   截止时间key
     * @param begin    开始时间
     * @param end      截止时间
     * @return the map
     */
    public static Map<String, String> getMonths(int type,
                                                String beginkey,
                                                String endkey,
                                                String begin,
                                                String end) {
        Map<String, String> map = new HashMap<>();
        try {
            DateFormat sdf = new SimpleDateFormat(Fields.NORM_YEAR_MTOTH_PATTERN);
            Date date1 = sdf.parse(begin);
            Date dEnd = sdf.parse(end);
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            if (type == 1) {
                int year = calBegin.get(Calendar.YEAR);
                int month = calBegin.get(Calendar.MONTH);

                int year1 = calEnd.get(Calendar.YEAR);
                int month1 = calEnd.get(Calendar.MONTH);
                int result;
                if (year == year1) {
                    result = month1 - month;
                } else {
                    result = 12 * (year1 - year) + month1 - month;
                }
                result++;
                calBegin.add(Calendar.MONTH, -result);
                calEnd.add(Calendar.MONTH, -result);
            } else if (type == 2) {
                calBegin.add(Calendar.YEAR, -1);
                calEnd.add(Calendar.YEAR, -1);
            }
            map.put(beginkey, sdf.format(calBegin.getTime()));
            map.put(endkey, sdf.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return map;
    }

    /**
     * (周)返回起止时间内的所有自然周
     *
     * @param begin  时间起
     * @param end    时间止
     * @param startw 周起
     * @param endW   周止
     * @return the list
     */
    public static List<String> getWeeks(String begin,
                                        String end,
                                        String startw,
                                        String endW) {
        List<String> lDate = new ArrayList<>();
        try {
            DateFormat sdf = new SimpleDateFormat(Fields.NORM_YEAR_PATTERN);
            Date date1 = sdf.parse(begin);
            Date dEnd = sdf.parse(end);
            Calendar calBegin = Calendar.getInstance();
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTime(dEnd);
            calBegin.setFirstDayOfWeek(Calendar.MONDAY);
            int beginww = Integer.parseInt(startw);
            int endww = Integer.parseInt(endW);

            int beginY = calBegin.get(Calendar.YEAR);
            int endY = calEnd.get(Calendar.YEAR);

            int weekall = getAllWeeks(beginY + Normal.EMPTY);
            do {
                lDate.add(beginY + "年第" + beginww + "周");
                if (beginww == weekall) {
                    beginww = 0;
                    beginY++;
                    weekall = getAllWeeks(beginY + Normal.EMPTY);
                }
                if (beginY == endY && beginww == endww) {
                    break;
                }
                beginww++;
            } while (beginY <= endY);
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return lDate;
    }

    /**
     * 返回该年有多少个自然周
     *
     * @param year 最多53 一般52 如果12月月末今天在本年53周(属于第二年第一周) 那么按照当年52周算
     * @return the int
     */
    public static int getAllWeeks(String year) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(Fields.PURE_DATETIME_FORMAT.parse(year + "-12-31"));
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int week = calendar.get(Calendar.WEEK_OF_YEAR);
        if (week != 53) {
            week = 52;
        }
        return week;
    }

    /**
     * 获取两个日期段相差的周数
     *
     * @param start 日期
     * @param end   日期
     * @return the int
     */
    public static int getWeeksCount(Date start, Date end) {
        Calendar c_begin = Calendar.getInstance();
        c_begin.setTime(start);
        Calendar c_end = Calendar.getInstance();
        c_end.setTime(end);
        int count = 0;
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
     * 根据季度返回季度第一月
     *
     * @param quarter 季度
     * @return 月份
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
     * @param type    第一个月份
     * @param quarter 季度
     * @return 月份
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
     * @param month 月份
     * @return 季度
     */
    public static int getQuarterByMonth(int month) {
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

    /**
     * 返回文字描述的日期
     *
     * @param date 日期
     * @return 日期
     */
    public static String getTimeFormatText(Date date) {
        if (date == null) {
            return null;
        }
        long diff = System.currentTimeMillis() - date.getTime();
        long r;
        if (diff > Fields.Time.DAY.getMillis()) {
            r = (diff / Fields.Time.DAY.getMillis());
            return r + "天前";
        }
        if (diff > Fields.Time.HOUR.getMillis()) {
            r = (diff / Fields.Time.HOUR.getMillis());
            return r + "个小时前";
        }
        if (diff > Fields.Time.MINUTE.getMillis()) {
            r = (diff / Fields.Time.MINUTE.getMillis());
            return r + "分钟前";
        }
        return "刚刚";
    }

    /**
     * 原有时间基础上,加上/减去(负数)N年
     *
     * @param date   日期
     * @param amount 年份
     * @return 操作后的时间
     */
    public static Date addYears(final Date date, final int amount) {
        return add(date, Calendar.YEAR, amount);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N月
     *
     * @param date   日期
     * @param amount 月份
     * @return 操作后的时间
     */
    public static Date addMonths(final Date date, final int amount) {
        return add(date, Calendar.MONTH, amount);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N周
     *
     * @param date   日期
     * @param amount 周
     * @return 操作后的时间
     */
    public static Date addWeeks(final Date date, final int amount) {
        return add(date, Calendar.WEEK_OF_YEAR, amount);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N天
     *
     * @param date   日期
     * @param amount 天
     * @return 操作后的时间
     */
    public static Date addDays(final Date date, final int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N小时
     *
     * @param date   日期
     * @param amount 小时
     * @return 操作后的时间
     */
    public static Date addHours(final Date date, final int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N分钟
     *
     * @param date   日期
     * @param amount 分钟
     * @return 操作后的时间
     */
    public static Date addMinutes(final Date date, final int amount) {
        return add(date, Calendar.MINUTE, amount);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N秒
     *
     * @param date   日期
     * @param amount 秒
     * @return 操作后的时间
     */
    public static Date addSeconds(final Date date, final int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    /**
     * 原有时间基础上,加上/减去(负数)N毫秒
     *
     * @param date   日期
     * @param amount 毫秒
     * @return 操作后的时间
     */
    public static Date addMilliseconds(final Date date, final int amount) {
        return add(date, Calendar.MILLISECOND, amount);
    }

    /**
     * 返回添加指定规则后的日期
     *
     * @param date   日期
     * @param field  规则
     * @param amount 数量
     * @return 操作后的日期 {@code Date}
     */
    private static Date add(final Date date, final int field, final int amount) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, amount);
        return calendar.getTime();
    }

    /**
     * 原有时间基础上,设置加上/减去(负数)N年
     *
     * @param date   时间
     * @param amount 数量
     * @return 操作后的时间
     */
    public static Date setYears(final Date date, final int amount) {
        return set(date, Calendar.YEAR, amount);
    }

    /**
     * 原有时间基础上,设置加上/减去(负数)N月
     *
     * @param date   时间
     * @param amount 数量
     * @return 操作后的时间
     */
    public static Date setMonths(final Date date, final int amount) {
        return set(date, Calendar.MONTH, amount);
    }


    /**
     * 原有时间基础上,设置N天
     *
     * @param date   日期
     * @param amount 天
     * @return 操作后的时间
     */
    public static Date setDays(final Date date, final int amount) {
        return set(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * 原有时间基础上,设置N小时
     *
     * @param date   日期
     * @param amount 小时
     * @return 操作后的时间
     */
    public static Date setHours(final Date date, final int amount) {
        return set(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 原有时间基础上,设置N分钟
     *
     * @param date   日期
     * @param amount 小时
     * @return 操作后的时间
     */
    public static Date setMinutes(final Date date, final int amount) {
        return set(date, Calendar.MINUTE, amount);
    }

    /**
     * 原有时间基础上,设置N秒
     *
     * @param date   日期
     * @param amount 小时
     * @return 操作后的时间
     */
    public static Date setSeconds(final Date date, final int amount) {
        return set(date, Calendar.SECOND, amount);
    }

    /**
     * 原有时间基础上,设置N毫秒
     *
     * @param date   日期
     * @param amount 小时
     * @return 操作后的时间
     */
    public static Date setMilliseconds(final Date date, final int amount) {
        return set(date, Calendar.MILLISECOND, amount);
    }

    /**
     * 返回设置指定规则后的日期
     *
     * @param date   日期
     * @param field  规则
     * @param amount 数量
     * @return 操作后的日期 {@code Date}
     */
    private static Date set(final Date date, final int field, final int amount) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        calendar.setTime(date);
        calendar.set(field, amount);
        return calendar.getTime();
    }

    /**
     * 将{@code Date}转换为{@code Calendar}
     *
     * @param date 日期转换为日历的日期
     * @return 创建的日历
     */
    public static Calendar toCalendar(final Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * 将{@code Date}转换为{@code Calendar}.
     *
     * @param date     日期转换为日历的日期
     * @param timeZone 时区
     * @return 创建的日历
     */
    public static Calendar toCalendar(final Date date, final TimeZone timeZone) {
        final Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(date);
        return calendar;
    }

    /**
     * 校验日期格式,日期不能早于当前天
     *
     * @param dptDate 日期,仅需包含年月日
     * @param pattern 日期转移格式
     * @return true/false
     */
    public static boolean isDate(String dptDate, String pattern) {
        if (dptDate == null || dptDate.isEmpty())
            return false;
        String formatDate = format(dptDate, pattern, pattern);
        if (formatDate.equals(dptDate)) {
            return true;
        }
        return false;
    }

    /**
     * 校验日期格式,日期不能早于当前天, 默认日期转义格式：yyyy-MM-dd
     *
     * @param dptDate 日期,仅需包含年月日
     * @return true/false
     */
    public static boolean isDate(String dptDate) {
        return isDate(dptDate, Fields.NORM_DATE_PATTERN);
    }

    /**
     * 校验前面的日期go,是否早于或者等于后面的日期back
     *
     * @param go      日期1
     * @param back    日期2
     * @param pattern 日期正则表达式
     * @return true/false
     */
    public static boolean isBefore(String go, String back, String pattern) {
        if (go == null || back == null || go.isEmpty() || back.isEmpty())
            return false;

        Date goDate = addDays(parse(go, pattern), -1);
        Date backDate = parse(back, pattern);
        if (goDate != null && backDate != null) {
            return goDate.before(backDate);
        }
        return false;
    }

    /**
     * 校验前面的日期go,是否早于或者等于后面的日期back
     *
     * @param go   日期1
     * @param back 日期2
     * @return true/false
     */
    public static boolean isBefore(String go, String back) {
        return isBefore(go, back, Fields.NORM_DATE_PATTERN);
    }

    /**
     * 验证长日期格式yyyy-MM-dd HH:mm:ss
     *
     * @param datetime 日期
     * @return true/false
     */
    public static boolean isDatetime(String datetime) {
        return isDate(datetime, Fields.NORM_DATETIME_PATTERN);
    }

    /**
     * 校验短日期格式[yyyyMMdd]
     *
     * @param date 短日期
     * @return true/false
     */
    public static boolean isShortDate(String date) {
        if (date == null || Normal.EMPTY.equals(date))
            return false;
        String regex = "^([\\d]{4}(((0[13578]|1[02])((0[1-9])|([12][0-9])|(3[01])))|(((0[469])|11)((0[1-9])|([12][1-9])|30))|(02((0[1-9])|(1[0-9])|(2[1-8])))))|((((([02468][048])|([13579][26]))00)|([0-9]{2}(([02468][048])|([13579][26]))))(((0[13578]|1[02])((0[1-9])|([12][0-9])|(3[01])))|(((0[469])|11)((0[1-9])|([12][1-9])|30))|(02((0[1-9])|(1[0-9])|(2[1-9])))))$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }

    /**
     * 判断传入的日期是否 &gt;=今天
     *
     * @param date 待判断的日期
     * @return true/false
     */
    public static boolean isNotLessThanToday(String date) {
        return isNotLessThanToday(date, Fields.NORM_DATE_PATTERN);
    }

    /**
     * 判断传入的日期是否&gt;=今天
     *
     * @param date   待判断的日期
     * @param format 格式
     * @return true/false
     */
    public static boolean isNotLessThanToday(String date, String format) {
        if (date == null || date.isEmpty())
            return false;
        Date cmpDate = addDays(new Date(), -1);
        Date srcDate = parse(date, format);
        return srcDate.after(cmpDate);
    }

    /**
     * 通过生日计算星座
     *
     * @param date 出生日期
     * @return 星座名
     */
    public static String getZodiac(Date date) {
        return getZodiac(DateKit.calendar(date));
    }

    /**
     * 通过生日计算星座
     *
     * @param calendar 出生日期
     * @return 星座名
     */
    public static String getZodiac(Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getZodiac(calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月,从0开始计数
     * @param day   天
     * @return 星座名
     */
    public static String getZodiac(Fields.Month month, int day) {
        return getZodiac(month.getValue(), day);
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月,从0开始计数,见{@link Fields.Month#getValue()}
     * @param day   天
     * @return 星座名
     */
    public static String getZodiac(int month, int day) {
        return day < Fields.ZODIAC_SLICED[month] ? Fields.ZODIAC[month] : Fields.ZODIAC[month + 1];
    }

    /**
     * 通过生日计算生肖,只计算1900年后出生的人
     *
     * @param date 出生日期(年需农历)
     * @return 星座名
     */
    public static String getChineseZodiac(Date date) {
        return getChineseZodiac(DateKit.calendar(date));
    }

    /**
     * 通过生日计算生肖,只计算1900年后出生的人
     *
     * @param calendar 出生日期(年需农历)
     * @return 星座名
     */
    public static String getChineseZodiac(Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getChineseZodiac(calendar.get(Calendar.YEAR));
    }

    /**
     * 计算生肖,只计算1900年后出生的人
     *
     * @param year 农历年
     * @return 生肖名
     */
    public static String getChineseZodiac(int year) {
        if (year < 1900) {
            return null;
        }
        return StringKit.toString(Fields.CN_ZODIAC[(year - 1900) % Fields.CN_ZODIAC.length]);
    }

    /**
     * 获取指定日期字段的最小值，例如分钟的最小值是0
     *
     * @param calendar  {@link Calendar}
     * @param dateField {@link Fields.DateField}
     * @return 字段最小值
     * @see Calendar#getActualMinimum(int)
     */
    public static int getBeginValue(Calendar calendar, int dateField) {
        if (Calendar.DAY_OF_WEEK == dateField) {
            return calendar.getFirstDayOfWeek();
        }
        return calendar.getActualMinimum(dateField);
    }

    /**
     * 获取指定日期字段的最大值，例如分钟的最大值是59
     *
     * @param calendar  {@link Calendar}
     * @param dateField {@link Fields.DateField}
     * @return 字段最大值
     * @see Calendar#getActualMaximum(int)
     */
    public static int getEndValue(Calendar calendar, int dateField) {
        if (Calendar.DAY_OF_WEEK == dateField) {
            return (calendar.getFirstDayOfWeek() + 6) % 7;
        }
        return calendar.getActualMaximum(dateField);
    }

    /**
     * 标准化日期，默认处理以空格区分的日期时间格式，空格前为日期
     * 将以下字符替换为"-"
     * <pre>
     * "."
     * "/"
     * "年"
     * "月"
     * </pre>
     *
     * <p>
     * 将以下字符替换为":"
     * <pre>
     * "时"
     * "分"
     * "秒"
     * </pre>
     *
     * <p>
     * 将以下字符去除
     * <pre>
     * "日"
     * </pre>
     *
     * <p>
     * 当末位是":"时去除之(不存在毫秒时)
     *
     * @param dateStr 日期时间字符串
     * @return 格式化后的日期字符串
     */
    private static String normalize(CharSequence dateStr) {
        if (StringKit.isBlank(dateStr)) {
            return StringKit.toString(dateStr);
        }

        // 日期时间分开处理
        final List<String> dateAndTime = StringKit.splitTrim(dateStr, Symbol.C_SPACE);
        final int size = dateAndTime.size();
        if (size < 1 || size > 2) {
            // 非可被标准处理的格式
            return StringKit.toString(dateStr);
        }

        final StringBuilder builder = StringKit.builder();

        // 日期部分("\"、"/"、"."、"年"、"月"都替换为"-")
        String datePart = dateAndTime.get(0).replaceAll("[/.年月]", Symbol.HYPHEN);
        datePart = StringKit.removeSuffix(datePart, "日");
        builder.append(datePart);

        // 时间部分
        if (size == 2) {
            builder.append(Symbol.C_SPACE);
            String timePart = dateAndTime.get(1).replaceAll("[时分秒]", Symbol.COLON);
            timePart = StringKit.removeSuffix(timePart, Symbol.COLON);
            //将ISO8601中的逗号替换为.
            timePart = timePart.replace(Symbol.C_COMMA, Symbol.C_DOT);
            builder.append(timePart);
        }


        return builder.toString();
    }

    /**
     * 修改日期
     *
     * @param calendar   {@link Calendar}
     * @param dateField  日期字段，即保留到哪个日期字段
     * @param modifyType 修改类型，包括舍去、四舍五入、进一等
     * @return 修改后的{@link Calendar}
     */
    public static Calendar modify(Calendar calendar, int dateField, Fields.ModifyType modifyType) {
        // AM_PM上下午特殊处理
        if (Calendar.AM_PM == dateField) {
            boolean isAM = DateKit.isAM(calendar);
            switch (modifyType) {
                case TRUNCATE:
                    calendar.set(Calendar.HOUR_OF_DAY, isAM ? 0 : 12);
                    break;
                case CEILING:
                    calendar.set(Calendar.HOUR_OF_DAY, isAM ? 11 : 23);
                    break;
                case ROUND:
                    int min = isAM ? 0 : 12;
                    int max = isAM ? 11 : 23;
                    int href = (max - min) / 2 + 1;
                    int value = calendar.get(Calendar.HOUR_OF_DAY);
                    calendar.set(Calendar.HOUR_OF_DAY, (value < href) ? min : max);
                    break;
            }
            // 处理下一级别字段
            return modify(calendar, dateField + 1, modifyType);
        }

        int[] ignoreFields = new int[]{
                Calendar.HOUR_OF_DAY, // 与HOUR同名
                Calendar.AM_PM, // 此字段单独处理，不参与计算起始和结束
                Calendar.DAY_OF_WEEK_IN_MONTH, // 不参与计算
                Calendar.DAY_OF_YEAR, // DAY_OF_MONTH体现
                Calendar.WEEK_OF_MONTH, // 特殊处理
                Calendar.WEEK_OF_YEAR // WEEK_OF_MONTH体现
        };
        // 循环处理各级字段，精确到毫秒字段
        for (int i = dateField + 1; i <= Calendar.MILLISECOND; i++) {
            if (ArrayKit.contains(ignoreFields, i)) {
                // 忽略无关字段(WEEK_OF_MONTH)始终不做修改
                continue;
            }

            // 在计算本周的起始和结束日时，月相关的字段忽略
            if (Calendar.WEEK_OF_MONTH == dateField || Calendar.WEEK_OF_YEAR == dateField) {
                if (Calendar.DAY_OF_MONTH == i) {
                    continue;
                }
            } else {
                // 其它情况忽略周相关字段计算
                if (Calendar.DAY_OF_WEEK == i) {
                    continue;
                }
            }

            modifyField(calendar, i, modifyType);
        }
        return calendar;
    }

    /**
     * 修改日期字段值
     *
     * @param calendar   {@link Calendar}
     * @param field      字段，见{@link Calendar}
     * @param modifyType {@link Fields.ModifyType}
     */
    private static void modifyField(Calendar calendar, int field, Fields.ModifyType modifyType) {
        if (Calendar.HOUR == field) {
            // 修正小时。HOUR为12小时制，上午的结束时间为12:00，此处改为HOUR_OF_DAY: 23:59
            field = Calendar.HOUR_OF_DAY;
        }

        switch (modifyType) {
            case TRUNCATE:
                calendar.set(field, DateKit.getBeginValue(calendar, field));
                break;
            case CEILING:
                calendar.set(field, DateKit.getEndValue(calendar, field));
                break;
            case ROUND:
                int min = DateKit.getBeginValue(calendar, field);
                int max = DateKit.getEndValue(calendar, field);
                int href;
                if (Calendar.DAY_OF_WEEK == field) {
                    // 星期特殊处理，假设周一是第一天，中间的为周四
                    href = (min + 3) % 7;
                } else {
                    href = (max - min) / 2 + 1;
                }
                int value = calendar.get(field);
                calendar.set(field, (value < href) ? min : max);
                break;
        }
    }

    /**
     * 纳秒转毫秒
     *
     * @param duration 时长
     * @return 时长毫秒
     */
    public static long nanosToMillis(long duration) {
        return TimeUnit.NANOSECONDS.toMillis(duration);
    }

    /**
     * 纳秒转秒,保留小数
     *
     * @param duration 时长
     * @return 秒
     */
    public static double nanosToSeconds(long duration) {
        return duration / 1_000_000_000.0;
    }

    /**
     * Date对象转换为{@link Instant}对象
     *
     * @param date Date对象
     * @return {@link Instant}对象
     */
    public static Instant toInstant(Date date) {
        return null == date ? null : date.toInstant();
    }

    /**
     * Calendar{@link Instant}对象
     *
     * @param calendar Date对象
     * @return {@link Instant}对象
     */
    public static Instant toInstant(Calendar calendar) {
        return null == calendar ? null : calendar.toInstant();
    }

    /**
     * Date对象转换为{@link Instant}对象
     *
     * @param temporalAccessor Date对象
     * @return {@link Instant}对象
     */
    public static Instant toInstant(TemporalAccessor temporalAccessor) {
        if (null == temporalAccessor) {
            return null;
        }

        Instant result;
        if (temporalAccessor instanceof Instant) {
            result = (Instant) temporalAccessor;
        } else if (temporalAccessor instanceof LocalDateTime) {
            result = ((LocalDateTime) temporalAccessor).atZone(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof ZonedDateTime) {
            result = ((ZonedDateTime) temporalAccessor).toInstant();
        } else if (temporalAccessor instanceof OffsetDateTime) {
            result = ((OffsetDateTime) temporalAccessor).toInstant();
        } else if (temporalAccessor instanceof LocalDate) {
            result = ((LocalDate) temporalAccessor).atStartOfDay(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof LocalTime) {
            // 指定本地时间转换 为Instant，取当天日期
            result = ((LocalTime) temporalAccessor).atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();
        } else if (temporalAccessor instanceof OffsetTime) {
            // 指定本地时间转换 为Instant，取当天日期
            result = ((OffsetTime) temporalAccessor).atDate(LocalDate.now()).toInstant();
        } else {
            result = Instant.from(temporalAccessor);
        }

        return result;
    }

    /**
     * {@link Instant} 转换为 {@link LocalDateTime}，使用系统默认时区
     *
     * @param instant {@link Instant}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    /**
     * {@link Calendar} 转换为 {@link LocalDateTime}，使用系统默认时区
     *
     * @param calendar {@link Calendar}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime toLocalDateTime(Calendar calendar) {
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    /**
     * {@link Date} 转换为 {@link LocalDateTime}，使用系统默认时区
     *
     * @param date {@link Calendar}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        final DateTime dateTime = date(date);
        return LocalDateTime.ofInstant(dateTime.toInstant(), dateTime.getZoneId());
    }

    /**
     * HH:mm:ss 时间格式字符串转为秒数
     *
     * @param timeStr 字符串时分秒(HH:mm:ss)格式
     * @return 时分秒转换后的秒数
     */
    public static int timeToSecond(String timeStr) {
        if (StringKit.isEmpty(timeStr)) {
            return 0;
        }

        final List<String> hms = StringKit.splitTrim(timeStr, Symbol.COLON, 3, true);
        int lastIndex = hms.size() - 1;

        int result = 0;
        for (int i = lastIndex; i >= 0; i--) {
            result += Integer.parseInt(hms.get(i)) * Math.pow(60, (lastIndex - i));
        }
        return result;
    }

    /**
     * 秒数转为时间格式(HH:mm:ss)
     *
     * @param seconds 需要转换的秒数
     * @return 转换后的字符串
     */
    public static String secondToTime(int seconds) {
        if (seconds < 0) {
            throw new IllegalArgumentException("Seconds must be a positive number!");
        }

        int hour = seconds / 3600;
        int other = seconds % 3600;
        int minute = other / 60;
        int second = other % 60;
        final StringBuilder sb = new StringBuilder();
        if (hour < 10) {
            sb.append(Symbol.ZERO);
        }
        sb.append(hour);
        sb.append(Symbol.COLON);
        if (minute < 10) {
            sb.append(Symbol.ZERO);
        }
        sb.append(minute);
        sb.append(Symbol.COLON);
        if (second < 10) {
            sb.append(Symbol.ZERO);
        }
        sb.append(second);
        return sb.toString();
    }

    /**
     * 创建日期范围生成器
     *
     * @param start 起始日期时间
     * @param end   结束日期时间
     * @param unit  步进单位
     * @return {@link Boundary}
     */
    public static Boundary range(Date start, Date end, final Fields.DateField unit) {
        return new Boundary(start, end, unit);
    }

    /**
     * 创建日期范围生成器
     *
     * @param start 起始日期时间
     * @param end   结束日期时间
     * @param unit  步进单位
     * @return {@link Boundary}
     */
    public static List<DateTime> rangeToList(Date start, Date end, final Fields.DateField unit) {
        return CollKit.newArrayList((Iterable<DateTime>) range(start, end, unit));
    }

    /**
     * 获取农历日的表示
     *
     * @param lunarDay 　农历日数值表示
     * @return 农历日传统字符表示
     */
    public static String getDayName(int lunarDay) {
        return Fields.CN_DAY[lunarDay - 1];
    }

    /**
     * 获取农历月份
     *
     * @param lunarMonth 　农历月数值表示
     * @return 农历月传统字符表示
     */
    public static String getMonthName(int lunarMonth) {
        return Fields.CN_MONTH[lunarMonth - 1];
    }

    /**
     * 获取农历年份
     *
     * @param lunarYear 　农历年数值表示
     * @return 农历年传统字符表示
     */
    public static String getYearName(int lunarYear) {
        StringBuilder sb = new StringBuilder();
        sb.append(Fields.CN_YEAR[lunarYear / 1000]);
        sb.append(Fields.CN_YEAR[lunarYear % 1000 / 100]);
        sb.append(Fields.CN_YEAR[lunarYear % 100 / 10]);
        sb.append(Fields.CN_YEAR[lunarYear % 10]);
        return sb.toString();
    }

    /**
     * 农历转公历
     *
     * @param lunarYear   　农历年
     * @param lunarMonth  　农历月，从１开始
     * @param LunarDate   　农历日
     * @param isLeapMonth 　是否润月
     * @return 公历日期
     */
    public static Calendar lunar2Solar(int lunarYear, int lunarMonth, int LunarDate, boolean isLeapMonth) {
        DateKit ret = new DateKit();
        ret.lunar(lunarYear, lunarMonth, LunarDate, isLeapMonth);
        return ret.solar;
    }

    /**
     * 计算两个农历日期之差
     *
     * @param lc1   　农历１
     * @param lc2   　农历２
     * @param field 　计算的维度，比如按月,天等
     * @return 具体的差值
     */
    public static long luanrDiff(DateKit lc1, DateKit lc2, int field) {
        return solarDiff(lc1.solar, lc2.solar, field);
    }

    /**
     * 公历转农历
     *
     * @param solar 　公历日期
     * @return 农历日期
     */
    public static DateKit solar2Lunar(Calendar solar) {
        DateKit ret = new DateKit();
        ret.lunar(solar.get(Calendar.YEAR), solar.get(Calendar.MONTH), solar.get(Calendar.DATE));
        return ret;
    }

    /**
     * 判断两个整数所代表公历日期的差值
     * 一年按365天计算，一个月按30天计算
     *
     * @param solarCode1 　农历日期代码
     * @param solarCode2 　农历日期代码
     * @param field      　差值单位
     * @return 差值
     */
    public static long solarDiff(int solarCode1, int solarCode2, int field) {
        GregorianCalendar c1 = new GregorianCalendar(solarCode1 / 10000, solarCode1 % 10000 / 100 - 1,
                solarCode1 % 10000 % 100);
        GregorianCalendar c2 = new GregorianCalendar(solarCode2 / 10000, solarCode2 % 10000 / 100 - 1,
                solarCode2 % 10000 % 100);
        return solarDiff(c1, c2, field);
    }

    /**
     * 求两个公历日期之差，field可以为年月日，时分秒
     * 一年按365天计算，一个月按30天计算
     *
     * @param solar1 　历日期
     * @param solar2 　历日期
     * @param field  差值单位
     * @return 差值
     */
    public static long solarDiff(Calendar solar1, Calendar solar2, int field) {
        long t1 = solar1.getTimeInMillis();
        long t2 = solar2.getTimeInMillis();
        switch (field) {
            case Calendar.SECOND:
                return (long) Math.rint(Double.valueOf(t1 - t2) / Double.valueOf(1000));
            case Calendar.MINUTE:
                return (long) Math.rint(Double.valueOf(t1 - t2) / Double.valueOf(60 * 1000));
            case Calendar.HOUR:
                return (long) Math.rint(Double.valueOf(t1 - t2) / Double.valueOf(3600 * 1000));
            case Calendar.DATE:
                return (long) Math.rint(Double.valueOf(t1 - t2) / Double.valueOf(24 * 3600 * 1000));
            case Calendar.MONTH:
                return (long) Math.rint(Double.valueOf(t1 - t2) / Double.valueOf(30 * 24 * 3600 * 1000));
            case Calendar.YEAR:
                return (long) Math.rint(Double.valueOf(t1 - t2) / Double.valueOf(365 * 24 * 3600 * 1000));
            default:
                return -1;
        }
    }

    /**
     * 返回传统天干地支年名称
     *
     * @param y 农历年
     * @return 传统农历年份的表示
     */
    public static String getTraditionalYearName(int y) {
        y = y - 1804;
        return (Normal.EMPTY + Fields.CN_GAN[y % 10] + Fields.CN_ZHI[y % 12] + "年");
    }

    /**
     * 获取生肖名
     *
     * @param y 　农历年
     * @return 生肖名
     */
    public static String getAnimalYearName(int y) {
        return Fields.CN_ZODIAC[(y - 4) % 12 - 1];
    }

    /**
     * 一个简单的二分查找，返回查找到的元素坐标，用于查找农历二维数组信息
     *
     * @param array 　数组
     * @param n     　待查询数字
     * @return 查到的坐标
     */
    private static int binSearch(int[] array, int n) {
        if (null == array || array.length == 0) {
            return -1;
        }
        int min = 0, max = array.length - 1;
        if (n <= array[min]) {
            return min;
        } else if (n >= array[max]) {
            return max;
        }
        while (max - min > 1) {
            int newIndex = (max + min) / 2; // 二分
            if (array[newIndex] > n) { // 取小区间
                max = newIndex;
            } else if (array[newIndex] < n) {// 取大区间
                min = newIndex;
            } else { // 相等，直接返回下标
                return newIndex;
            }
        }
        if (array[max] == n) {
            return max;
        } else if (array[min] == n) {
            return min;
        } else {
            return min; // 返回 较小的一个
        }
    }

    /**
     * 返回中国农历的全名
     *
     * @return String
     */
    public String getFullLunarName() {
        return this.toString() + Symbol.SPACE + getTraditionalYearName(this.lyear) + Symbol.SPACE + getAnimalYearName(this.lyear);
    }

    /**
     * 日期增加,和<code>GregorianCalendar.add</code>类似
     *
     * @param field  　单位
     * @param amount 数值
     * @see GregorianCalendar
     */
    public void add(int field, int amount) {
        this.solar.add(field, amount);
        this.lunar(this.solar.get(Calendar.YEAR), this.solar.get(Calendar.MONTH),
                this.solar.get(Calendar.DATE));
    }

    /**
     * 返回农历日期，不包含年份
     *
     * @param showLeap 　是否显示闰月的闰字
     * @return 农历日期
     */
    public String getLunar(boolean showLeap) {
        if (this.lmonth < 1 || this.lmonth > 12 || this.ldate < 1
                || this.ldate > 30) {
            throw new InstrumentException("Wrong lunar ldate: " + lmonth + Symbol.SPACE + ldate);
        }
        if (showLeap) {
            return (this.isLeapMonth ? "闰" : Normal.EMPTY) + getMonthName(this.lmonth) + "月"
                    + getDayName(this.ldate);
        } else {
            return getMonthName(this.lmonth) + "月" + getDayName(this.ldate);
        }
    }

    /**
     * 创建LunarInfo中某一年的一列公历日历编码
     * 公历日历编码：年份+月份+天，用于查询某个公历日期在某个LunarInfo列的哪一个区间
     *
     * @param solarYear 年份
     * @return 公历日历编码
     */
    private int[] builder(int solarYear) {
        if (solarYear < MIN_YEAR && solarYear > MAX_YEAR) {
            throw new InstrumentException("Illegal solar year: " + solarYear);
        }
        int lunarIndex = solarYear - MIN_YEAR;
        int[] solarCodes = new int[Fields.CN_LUNAR[lunarIndex].length];
        for (int i = 0; i < solarCodes.length; i++) {
            if (0 == i) { // 第一个数表示闰月，不用更改
                solarCodes[i] = Fields.CN_LUNAR[lunarIndex][i];
            } else if (1 == i) {
                if (Fields.CN_LUNAR[lunarIndex][1] > 999) {
                    // 这年农历一月一日对应的公历实际是上一年的
                    solarCodes[i] = (solarYear - 1) * 10000 + Fields.CN_LUNAR[lunarIndex][i];
                } else {
                    solarCodes[i] = solarYear * 10000 + Fields.CN_LUNAR[lunarIndex][i];
                }
            } else {
                solarCodes[i] = solarYear * 10000 + Fields.CN_LUNAR[lunarIndex][i];
            }
        }
        return solarCodes;
    }

    /**
     * 通过给定的农历日期，计算公历日期
     *
     * @param lunarYear   　农历年
     * @param lunarMonth  　农历月，从１开始
     * @param lunarDate   　农历日期
     * @param isleapMonth 　是否为闰月
     */
    private void lunar(final int lunarYear, final int lunarMonth, final int lunarDate, final boolean isleapMonth) {
        if (lunarYear < MIN_YEAR && lunarYear > MAX_YEAR) {
            throw new InstrumentException("LunarYear must in (" + MIN_YEAR + "," + MAX_YEAR + ")");
        }
        this.lyear = lunarYear;
        this.lmonth = lunarMonth;
        this.ldate = lunarDate;
        int solarMontDate = Fields.CN_LUNAR[lunarYear - MIN_YEAR][lunarMonth];
        this.leapMonth = Fields.CN_LUNAR[lunarYear - MIN_YEAR][0];
        if (this.leapMonth != 0 && (lunarMonth > this.leapMonth || (lunarMonth == this.leapMonth && isleapMonth))) {
            // 闰月，且当前农历月大于闰月月份，取下一个月的LunarInfo码
            // 闰月，且当前农历月等于闰月月份，并且此农历月为闰月，取下一个月的LunarInfo码
            solarMontDate = Fields.CN_LUNAR[lunarYear - MIN_YEAR][lunarMonth + 1];
        }
        this.solar.set(Calendar.YEAR, lunarYear);
        this.solar.set(Calendar.MONTH, (solarMontDate / 100) - 1);
        this.solar.set(Calendar.DATE, solarMontDate % 100);
        this.add(Calendar.DATE, lunarDate - 1);
    }

    /**
     * 通过给定公历日期，计算农历日期
     *
     * @param solarYear  公历年
     * @param solarMonth 公历月，0-11
     * @param solarDate  公历日
     */
    private void lunar(final int solarYear, final int solarMonth, final int solarDate) {
        if (solarYear < MIN_YEAR && solarYear > MAX_YEAR) {
            throw new InstrumentException("Illegal solar year: " + solarYear);
        }
        int solarCode = solarYear * 10000 + 100 * (1 + solarMonth) + solarDate; // 公历码
        this.leapMonth = Fields.CN_LUNAR[solarYear - MIN_YEAR][0];
        int[] solarCodes = builder(solarYear);
        int newMonth = binSearch(solarCodes, solarCode);
        if (-1 == newMonth) {
            throw new InstrumentException("No lunarInfo found by solarCode: " + solarCode);
        }
        int xdate = Long.valueOf(solarDiff(solarCode, solarCodes[newMonth], Calendar.DATE)).intValue();
        if (0 == newMonth) {// 在上一年
            int preYear = solarYear - 1;
            short[] preSolarCodes = Fields.CN_LUNAR[preYear - MIN_YEAR];
            // 取上年农历12月1号公历日期码
            int nearSolarCode = preSolarCodes[preSolarCodes.length - 1]; // 上一年12月1号
            // 下一年公历1月表示为了13月，这里做翻译，并计算出日期码
            nearSolarCode = (nearSolarCode / 100 == 13 ? preYear + 1 : preYear) * 10000
                    + (nearSolarCode / 100 == 13 ? nearSolarCode - 1200 : nearSolarCode);
            if (nearSolarCode > solarCode) {// 此公历日期在上一年农历12月1号，之前，即在上年农历11月内
                newMonth = 11;
                // 取农历11月的公历码
                nearSolarCode = preYear * 10000 + preSolarCodes[preSolarCodes.length - 2];
            } else {// 此公历日期在上一年农历12月内
                newMonth = 12;
            }
            xdate = Long.valueOf(solarDiff(solarCode, nearSolarCode, Calendar.DATE)).intValue();
            if (xdate < 0) {
                throw new InstrumentException("Wrong solarCode: " + solarCode);
            }
            this.ldate = 1 + xdate;
            this.lyear = preYear;
            this.lmonth = newMonth;
            this.isLeapMonth = false; // 农历12月不可能为闰月
        } else if (solarCodes.length == newMonth + 1 && xdate >= 30) {// 在下一年(公历12月只有30天)
            newMonth = 1; // 农历肯定是1月
            // 取下一年的公历日期码
            short[] nextSolarCodes = Fields.CN_LUNAR[solarYear + 1 - MIN_YEAR];
            // 取下一年农历1月1号公历日期码
            int nearSolarCode = solarYear * 10000 + nextSolarCodes[1]; // 下一年农历1月1号公历日期码
            xdate = Long.valueOf(solarDiff(solarCode, nearSolarCode, Calendar.DATE)).intValue();
            if (xdate < 0) {
                throw new InstrumentException("Wrong solarCode: " + solarCode);
            }
            this.ldate = 1 + xdate;
            this.lyear = solarYear + 1; // 农历年到了下一年
            this.lmonth = newMonth;
            this.isLeapMonth = false; // 农历1月不可能为闰月
        } else {
            if (xdate < 0) {
                throw new InstrumentException("Wrong solarCode: " + solarCode);
            }
            this.ldate = 1 + xdate;
            this.lyear = solarYear;
            this.isLeapMonth = 0 != leapMonth && (leapMonth + 1 == newMonth);
            if (0 != leapMonth && leapMonth < newMonth) {
                this.lmonth = newMonth - 1;
            } else {
                this.lmonth = newMonth;
            }
        }
        this.solar.set(Calendar.YEAR, solarYear);
        this.solar.set(Calendar.MONTH, solarMonth);
        this.solar.set(Calendar.DATE, solarDate);
    }

    @Override
    public String toString() {
        if (this.lyear < MIN_YEAR || this.lyear > MAX_YEAR || this.lmonth < 1 || this.lmonth > 12 || this.ldate < 1
                || this.ldate > 30) {
            return "Wrong lunar date: " + lyear + Symbol.SPACE + lmonth + Symbol.SPACE + ldate;
        }
        return getYearName(this.lyear) + "年" + (this.isLeapMonth ? "闰" : Normal.EMPTY) + getMonthName(this.lmonth) + "月"
                + getDayName(this.ldate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DateKit that = (DateKit) o;

        if (lyear != that.lyear) {
            return false;
        }
        if (lmonth != that.lmonth) {
            return false;
        }
        if (ldate != that.ldate) {
            return false;
        }
        return isLeapMonth == that.isLeapMonth;
    }

    @Override
    public int hashCode() {
        int result = lyear;
        result = 31 * result + lmonth;
        result = 31 * result + ldate;
        result = 31 * result + (isLeapMonth ? 1 : 0);
        return result;
    }

}
