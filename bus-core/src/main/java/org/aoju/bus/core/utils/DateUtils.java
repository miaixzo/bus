/*
 * The MIT License
 *
 * Copyright (c) 2017, aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.utils;

import org.aoju.bus.core.consts.Fields;
import org.aoju.bus.core.date.Between;
import org.aoju.bus.core.date.DateTime;
import org.aoju.bus.core.date.TimeInterval;
import org.aoju.bus.core.date.format.BetweenFormat;
import org.aoju.bus.core.date.format.DateParser;
import org.aoju.bus.core.date.format.DatePrinter;
import org.aoju.bus.core.date.format.FastDateFormat;
import org.aoju.bus.core.lang.Validator;
import org.aoju.bus.core.lang.exception.CommonException;
import org.aoju.bus.core.lang.exception.InstrumentException;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 时间工具类
 *
 * @author Kimi Liu
 * @version 3.0.5
 * @since JDK 1.8
 */
public class DateUtils extends Fields {

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
     * @param date Long类型Date（Unix时间戳）
     * @return 时间对象
     * @since 3.0.7
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
     * @param date Long类型Date（Unix时间戳）
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
     * 转换为Calendar对象
     *
     * @param date 日期对象
     * @return Calendar对象
     */
    public static Calendar calendar(Date date) {
        return calendar(date.getTime());
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
     * 当前时间，格式 yyyy-MM-dd HH:mm:ss
     *
     * @return 当前时间的标准形式字符串
     */
    public static String now() {
        return formatDateTime(new DateTime());
    }

    /**
     * 当前时间long
     *
     * @param isNano 是否为高精度时间
     * @return 时间
     */
    public static long current(boolean isNano) {
        return isNano ? System.nanoTime() : System.currentTimeMillis();
    }

    /**
     * 当前时间秒数
     *
     * @return 当前时间秒数
     * @since 4.0.0
     */
    public static long currentSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 当前日期，格式 yyyy-MM-dd
     *
     * @return 当前日期的标准形式字符串
     */
    public static String today() {
        return formatDate(new DateTime());
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
     * 获得指定日期所属季度，从1开始计数
     *
     * @param date 日期
     * @return 第几个季度
     * @since 4.1.0
     */
    public static int quarter(Date date) {
        return DateTime.of(date).quarter();
    }

    /**
     * 获得指定日期所属季度
     *
     * @param date 日期
     * @return 第几个季度枚举
     * @since 4.1.0
     */
    public static Quarter quarterEnum(Date date) {
        return DateTime.of(date).quarterEnum();
    }

    /**
     * 获得月份，从0开始计数
     *
     * @param date 日期
     * @return 月份，从0开始计数
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
    public static Fields.Month monthEnum(Date date) {
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
     * 获得指定日期是星期几，1表示周日，2表示周一
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
    public static Fields.Week dayOfWeekEnum(Date date) {
        return DateTime.of(date).dayOfWeekEnum();
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
     * 是否为下午
     *
     * @param date 日期
     * @return 是否为下午
     */
    public static boolean isPM(Date date) {
        return DateTime.of(date).isPM();
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
        return monthEnum(date());
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
        return dayOfWeekEnum(date());
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
     * @return Quarter ，类似于 20132
     */
    public static String yearAndQuarter(Date date) {
        return yearAndQuarter(calendar(date));
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format 日期格式，常用格式见： {@link Fields}
     * @return 格式化后的字符串
     */
    public static String format(Date date, String format) {
        if (null == date || StringUtils.isBlank(format)) {
            return null;
        }
        return format(date, FastDateFormat.getInstance(format));
    }

    /**
     * 根据特定格式格式化日期
     *
     * @param date   被格式化的日期
     * @param format {@link DatePrinter} 或 {@link FastDateFormat}
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
     * 格式化日期部分（不包括时间）
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
     * 格式化时间
     * 格式 HH:mm:ss
     *
     * @param date 被格式化的日期
     * @return 格式化后的字符串
     * @since 3.0.1
     */
    public static String formatTime(Date date) {
        if (null == date) {
            return null;
        }
        return Fields.NORM_TIME_FORMAT.format(date);
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
     * @param parser  格式化器,{@link FastDateFormat}
     * @return DateTime对象
     */
    public static DateTime parse(String dateStr, DateParser parser) {
        return new DateTime(dateStr, parser);
    }

    /**
     * 将特定格式的日期转换为Date对象
     *
     * @param dateStr 特定格式的日期
     * @param format  格式，例如yyyy-MM-dd
     * @return 日期对象
     */
    public static DateTime parse(String dateStr, String format) {
        return new DateTime(dateStr, format);
    }

    /**
     * 格式yyyy-MM-dd HH:mm:ss
     *
     * @param dateString 标准形式的时间字符串
     * @return 日期对象
     */
    public static DateTime parseDateTime(String dateString) {
        dateString = FileUtils.normalize(dateString);
        return parse(dateString, Fields.NORM_DATETIME_FORMAT);
    }

    /**
     * 格式yyyy-MM-dd
     *
     * @param dateString 标准形式的日期字符串
     * @return 日期对象
     */
    public static DateTime parseDate(String dateString) {
        dateString = FileUtils.normalize(dateString);
        return parse(dateString, Fields.NORM_DATE_FORMAT);
    }

    /**
     * 解析时间，格式HH:mm:ss，默认为1970-01-01
     *
     * @param timeString 标准形式的日期字符串
     * @return 日期对象
     */
    public static DateTime parseTime(String timeString) {
        timeString = FileUtils.normalize(timeString);
        return parse(timeString, Fields.NORM_TIME_FORMAT);
    }

    /**
     * 解析时间，格式HH:mm:ss，日期默认为今天
     *
     * @param timeString 标准形式的日期字符串
     * @return 日期对象
     * @since 3.1.1
     */
    public static DateTime parseTimeToday(String timeString) {
        timeString = StringUtils.format("{} {}", today(), timeString);
        return parse(timeString, Fields.NORM_DATETIME_FORMAT);
    }

    /**
     * 解析UTC时间，格式为：yyyy-MM-dd'T'HH:mm:ss'Z
     *
     * @param utcString UTC时间
     * @return 日期对象
     * @since 4.1.14
     */
    public static DateTime parseUTC(String utcString) {
        return parse(utcString, Fields.UTC_FORMAT);
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
     * </ol>
     *
     * @param dateStr 日期字符串
     * @return 日期
     */
    public static DateTime parse(String dateStr) {
        if (null == dateStr) {
            return null;
        }
        // 去掉两边空格并去掉中文日期中的“日”，以规范长度
        dateStr = dateStr.trim().replace("日", "");
        int length = dateStr.length();

        if (Validator.isNumber(dateStr)) {
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
        }

        if (length == Fields.NORM_DATETIME_PATTERN.length() || length == Fields.NORM_DATETIME_PATTERN.length() + 1) {
            if (dateStr.contains("T")) {
                //UTC时间格式：类似2018-09-13T05:34:31
                return parseUTC(dateStr);
            }
            return parseDateTime(dateStr);
        } else if (length == Fields.NORM_DATE_PATTERN.length()) {
            return parseDate(dateStr);
        } else if (length == Fields.NORM_TIME_PATTERN.length() || length == Fields.NORM_TIME_PATTERN.length() + 1) {
            return parseTimeToday(dateStr);
        } else if (length == Fields.NORM_DATETIME_MINUTE_PATTERN.length() || length == Fields.NORM_DATETIME_MINUTE_PATTERN.length() + 1) {
            return parse(FileUtils.normalize(dateStr), Fields.NORM_DATETIME_MINUTE_FORMAT);
        } else if (length >= Fields.NORM_DATETIME_MS_PATTERN.length() - 2) {
            return parse(FileUtils.normalize(dateStr), Fields.NORM_DATETIME_MS_FORMAT);
        }

        // 没有更多匹配的时间格式
        throw new CommonException("No format fit for date String [{}] !", dateStr);
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
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * 获取某天的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar;
    }

    /**
     * 获取某周的开始时间
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
     * 获取某周的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfWeek(Calendar calendar) {
        return beginOfWeek(calendar, true);
    }

    /**
     * 获取某周的开始时间，周一定为一周的开始时间
     *
     * @param calendar           日期 {@link Calendar}
     * @param isMondayAsFirstDay 是否周一做为一周的第一天（false表示周日做为第一天）
     * @return {@link Calendar}
     * @since 3.1.2
     */
    public static Calendar beginOfWeek(Calendar calendar, boolean isMondayAsFirstDay) {
        if (isMondayAsFirstDay) {
            // 设置周一为一周开始
            calendar.setFirstDayOfWeek(Week.MONDAY.getValue());
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        } else {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        }
        return beginOfDay(calendar);
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
     * @param isSundayAsLastDay 是否周日做为一周的最后一天（false表示周六做为最后一天）
     * @return {@link Calendar}
     * @since 3.1.2
     */
    public static Calendar endOfWeek(Calendar calendar, boolean isSundayAsLastDay) {
        if (isSundayAsLastDay) {
            // 设置周一为一周开始
            calendar.setFirstDayOfWeek(Week.MONDAY.getValue());
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        } else {
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
        }
        return endOfDay(calendar);
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
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return beginOfDay(calendar);
    }

    /**
     * 获取某月的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfMonth(Calendar calendar) {
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        return endOfDay(calendar);
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
     * @since 4.1.0
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
     * @since 4.1.0
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
     * 获取某年的开始时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar beginOfYear(Calendar calendar) {
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        return beginOfMonth(calendar);
    }

    /**
     * 获取某年的结束时间
     *
     * @param calendar 日期 {@link Calendar}
     * @return {@link Calendar}
     */
    public static Calendar endOfYear(Calendar calendar) {
        calendar.set(Calendar.MONTH, Calendar.DECEMBER);
        return endOfMonth(calendar);
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
     * @since 3.0.1
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
     * @since 3.0.1
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
     * @since 3.0.1
     */
    public static DateTime nextMonth() {
        return offsetMonth(new DateTime(), 1);
    }

    /**
     * 偏移毫秒数
     *
     * @param date   日期
     * @param offset 偏移毫秒数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMillisecond(Date date, int offset) {
        return offset(date, Fields.DateField.MILLISECOND, offset);
    }

    /**
     * 偏移秒数
     *
     * @param date   日期
     * @param offset 偏移秒数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetSecond(Date date, int offset) {
        return offset(date, Fields.DateField.SECOND, offset);
    }

    /**
     * 偏移分钟
     *
     * @param date   日期
     * @param offset 偏移分钟数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMinute(Date date, int offset) {
        return offset(date, Fields.DateField.MINUTE, offset);
    }

    /**
     * 偏移小时
     *
     * @param date   日期
     * @param offset 偏移小时数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetHour(Date date, int offset) {
        return offset(date, Fields.DateField.HOUR_OF_DAY, offset);
    }

    /**
     * 偏移天
     *
     * @param date   日期
     * @param offset 偏移天数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetDay(Date date, int offset) {
        return offset(date, Fields.DateField.DAY_OF_YEAR, offset);
    }

    /**
     * 偏移周
     *
     * @param date   日期
     * @param offset 偏移周数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetWeek(Date date, int offset) {
        return offset(date, Fields.DateField.WEEK_OF_YEAR, offset);
    }

    /**
     * 偏移月
     *
     * @param date   日期
     * @param offset 偏移月数，正数向未来偏移，负数向历史偏移
     * @return 偏移后的日期
     */
    public static DateTime offsetMonth(Date date, int offset) {
        return offset(date, Fields.DateField.MONTH, offset);
    }

    /**
     * 获取指定日期偏移指定时间后的时间
     *
     * @param date      基准日期
     * @param dateField 偏移的粒度大小（小时、天、月等）
     * @param offset    偏移量，正数为向后偏移，负数为向前偏移
     * @return 偏移后的日期
     */
    public static DateTime offset(Date date, Fields.DateField dateField, int offset) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(dateField.getValue(), offset);
        return new DateTime(cal.getTime());
    }

    /**
     * 判断两个日期相差的时长，只保留绝对值
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param unit      相差的单位
     * @return 日期差
     */
    public static long between(Date beginDate, Date endDate, Fields.Unit unit) {
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
     * @since 3.3.1
     */
    public static long between(Date beginDate, Date endDate, Fields.Unit unit, boolean isAbs) {
        return new Between(beginDate, endDate, isAbs).between(unit);
    }

    /**
     * 判断两个日期相差的毫秒数
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return 日期差
     * @since 3.0.1
     */
    public static long betweenMs(Date beginDate, Date endDate) {
        return new Between(beginDate, endDate).between(Unit.MS);
    }

    /**
     * 判断两个日期相差的天数
     *
     * <pre>
     * 有时候我们计算相差天数的时候需要忽略时分秒。
     * 比如：2016-02-01 23:59:59和2016-02-02 00:00:00相差一秒
     * 如果isReset为false相差天数为0。
     * 如果isReset为true相差天数将被计算为1
     * </pre>
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间
     * @return 日期差
     * @since 3.0.1
     */
    public static long betweenDay(Date beginDate, Date endDate, boolean isReset) {
        if (isReset) {
            beginDate = beginOfDay(beginDate);
            endDate = beginOfDay(endDate);
        }
        return between(beginDate, endDate, Unit.DAY);
    }

    /**
     * 计算两个日期相差月数
     * 在非重置情况下，如果起始日期的天小于结束日期的天，月数要少算1（不足1个月）
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间（重置天时分秒）
     * @return 相差月数
     * @since 3.0.8
     */
    public static long betweenMonth(Date beginDate, Date endDate, boolean isReset) {
        return new Between(beginDate, endDate).betweenMonth(isReset);
    }

    /**
     * 计算两个日期相差年数
     * 在非重置情况下，如果起始日期的月小于结束日期的月，年数要少算1（不足1年）
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param isReset   是否重置时间为起始时间（重置月天时分秒）
     * @return 相差年数
     * @since 3.0.8
     */
    public static long betweenYear(Date beginDate, Date endDate, boolean isReset) {
        return new Between(beginDate, endDate).betweenYear(isReset);
    }

    /**
     * 格式化日期间隔输出
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @param level     级别，按照天、小时、分、秒、毫秒分为5个等级
     * @return XX天XX小时XX分XX秒
     */
    public static String formatBetween(Date beginDate, Date endDate, Fields.Level level) {
        return formatBetween(between(beginDate, endDate, Unit.MS), level);
    }

    /**
     * 格式化日期间隔输出，精确到毫秒
     *
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return XX天XX小时XX分XX秒
     * @since 3.0.1
     */
    public static String formatBetween(Date beginDate, Date endDate) {
        return formatBetween(between(beginDate, endDate, Unit.MS));
    }

    /**
     * 格式化日期间隔输出
     *
     * @param betweenMs 日期间隔
     * @param level     级别，按照天、小时、分、秒、毫秒分为5个等级
     * @return XX天XX小时XX分XX秒XX毫秒
     */
    public static String formatBetween(long betweenMs, Fields.Level level) {
        return new BetweenFormat(betweenMs, level).format();
    }

    /**
     * 格式化日期间隔输出，精确到毫秒
     *
     * @param betweenMs 日期间隔
     * @return XX天XX小时XX分XX秒XX毫秒
     * @since 3.0.1
     */
    public static String formatBetween(long betweenMs) {
        return new BetweenFormat(betweenMs, Fields.Level.MILLSECOND).format();
    }

    /**
     * 当前日期是否在日期指定范围内
     * 起始日期和结束日期可以互换
     *
     * @param date      被检查的日期
     * @param beginDate 起始日期
     * @param endDate   结束日期
     * @return 是否在范围内
     * @since 3.0.8
     */
    public static boolean isIn(Date date, Date beginDate, Date endDate) {
        if (date instanceof DateTime) {
            return ((DateTime) date).isIn(beginDate, endDate);
        } else {
            return new DateTime(date).isIn(beginDate, endDate);
        }
    }

    /**
     * 计时，常用于记录某段代码的执行时间，单位：纳秒
     *
     * @param preTime 之前记录的时间
     * @return 时间差，纳秒
     */
    public static long spendNt(long preTime) {
        return System.nanoTime() - preTime;
    }

    /**
     * 计时，常用于记录某段代码的执行时间，单位：毫秒
     *
     * @param preTime 之前记录的时间
     * @return 时间差，毫秒
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
        return Integer.parseInt(DateUtils.format(date, "yyMMddHHmm"));
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
     * 计算某个过程花费的时间，精确到毫秒
     *
     * @return Timer
     */
    public static TimeInterval timer() {
        return new TimeInterval();

    }

    /**
     * 生日转为年龄，计算法定年龄
     *
     * @param birthDay 生日
     * @return 年龄
     */
    public static int ageOfNow(Date birthDay) {
        return age(birthDay, date());
    }

    /**
     * 计算相对于dateToCompare的年龄，长用于计算指定生日在某年的年龄
     *
     * @param birthDay      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int age(Date birthDay, Date dateToCompare) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dateToCompare);

        if (cal.before(birthDay)) {
            throw new IllegalArgumentException("Birthday is after date {}!");
        }

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

        cal.setTime(birthDay);
        int age = year - cal.get(Calendar.YEAR);

        int monthBirth = cal.get(Calendar.MONTH);
        if (month == monthBirth) {
            int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);
            if (dayOfMonth < dayOfMonthBirth) {
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
     * 是否闰年
     *
     * @param year 年
     * @return 是否闰年
     */
    public static boolean isLeapYear(int year) {
        return new GregorianCalendar().isLeapYear(year);
    }

    /**
     * 判定给定开始时间经过某段时间后是否过期
     *
     * @param startDate   开始时间
     * @param dateField   时间单位
     * @param timeLength  时长
     * @param checkedDate 被比较的时间。如果经过时长后的时间晚于被检查的时间，就表示过期
     * @return 是否过期
     * @since 3.1.1
     */
    public static boolean isExpired(Date startDate, DateField dateField, int timeLength, Date checkedDate) {
        final Date endDate = offset(startDate, dateField, timeLength);
        return endDate.after(checkedDate);
    }

    /**
     * 秒数转为时间格式(HH:mm:ss)
     * 参考：https://github.com/iceroot
     *
     * @param seconds 需要转换的秒数
     * @return 转换后的字符串
     * @since 3.1.2
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
            sb.append("0");
        }
        sb.append(hour);
        sb.append(":");
        if (minute < 10) {
            sb.append("0");
        }
        sb.append(minute);
        sb.append(":");
        if (second < 10) {
            sb.append("0");
        }
        sb.append(second);
        return sb.toString();
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
     * 获取当前时间-24小时制
     *
     * @return string date 当前日期
     */
    public static String getTime24() {
        return NORM_DATETIME_FORMAT.format(new Date());
    }

    /**
     * 获取当前时间-12小时制
     *
     * @return string date 当前日期
     */
    public static String getTime12() {
        return NORM_DATETIME_FORMAT.format(new Date());

    }

    /**
     * 获取UNIX时间
     *
     * @return string date 当前日期
     */
    public static String getTimestamp() {
        return Long.toString(System.currentTimeMillis());
    }

    /**
     * 转换时间
     *
     * @param object date
     * @return Date 日期
     */
    public static Date toDate(String object) {
        try {
            return NORM_DATETIME_FORMAT.parse(object);
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 转换时间
     *
     * @param object date
     * @return Date 日期
     */
    public static Date objectToDate(String object) {
        try {
            return PURE_DATETIME_FORMAT.parse(object);
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
    }

    /**
     * 转换时间
     *
     * @param date 日期
     * @return String 日期
     */
    public static String toString(Date date) {
        return NORM_DATETIME_FORMAT.format(date);
    }

    /**
     * REST API调用方法
     *
     * @return 日期
     */
    public static String getMillis() {
        Calendar calendar = Calendar.getInstance();
        return PURE_TIME_MS_FORMAT.format(calendar.getTime());
    }

    /**
     * 给指定的日期加上(减去)月份
     *
     * @param date    日期
     * @param pattern 格式
     * @param num     数量
     * @return 日期
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
     * @param date    日期
     * @param pattern 格式
     * @param num     数量
     * @return 日期
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
     * @param date 日期
     * @return 时间差
     */
    public static int compareDateWithNow(Date date) {
        Date now = new Date();
        int rnum = date.compareTo(now);
        return rnum;
    }

    /**
     * 两个时间比较(时间戳比较)
     *
     * @param date 日期
     * @return 时间差
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
     * @param object 字符串日期
     * @return the boolean
     */
    public static boolean compareWithNow(String object) {
        long expired = dateToUnixTimestamp() - (Long.parseLong(object) * 1000);
        return expired <= 900000 && expired >= -900000;
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date 需要转换的日期 yyyy-MM-dd HH:mm:ss
     * @return long 时间戳
     */
    public static long dateToUnixTimestamp(String date) {
        long timestamp = 0;
        try {
            timestamp = NORM_DATETIME_FORMAT.parse(date).getTime();
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }

        return timestamp;
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date   需要转换的日期
     * @param format 格式
     * @return long 时间戳
     */
    public static long dateToUnixTimestamp(String date, String format) {
        long timestamp = 0;
        try {
            timestamp = new SimpleDateFormat(format).parse(date).getTime();
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return timestamp;
    }

    /**
     * 将当前日期转换成Unix时间戳
     *
     * @return long 时间戳
     */
    public static long dateToUnixTimestamp() {
        long timestamp = System.currentTimeMillis();
        return timestamp;
    }

    /**
     * 将指定的日期转换成Unix时间戳
     *
     * @param date   需要转换的日期
     * @param format 格式
     * @return long 时间戳
     */
    public static String dateFromUnixTimestamp(String date, String format) {
        return format(new Date(Long.parseLong(date)), format);
    }

    public static String getTodayYMD() {
        return PURE_DATE_FORMAT.format(new Date());

    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp 时间戳
     * @return String 日期字符串
     */
    public static String unixTimestampToDate(long timestamp) {
        return NORM_DATETIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp 时间戳
     * @param format    格式
     * @return String 日期字符串
     */
    public static String TimeStamp2Date(long timestamp, String format) {
        String date = new SimpleDateFormat(format).format(new Date(
                timestamp));
        return date;
    }

    /**
     * 将Unix时间戳转换成日期
     *
     * @param timestamp 时间戳
     * @return String 日期字符串
     */
    public static String TimeStamp2Date(long timestamp) {
        return NORM_DATETIME_FORMAT.format(new Date(timestamp));
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
    public static Map<String, String> getDayDate(int type, String beginkey,
                                                 String endkey, String begin, String end) {
        Map<String, String> map = new HashMap<String, String>();
        Date dBegin = null; // 开始日期
        Date dEnd = null; // 结束日期
        try {
            dBegin = PURE_DATETIME_FORMAT.parse(begin);
            dEnd = PURE_DATETIME_FORMAT.parse(end);
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
            map.put(beginkey, PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            map.put(endkey, PURE_DATETIME_FORMAT.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return map;

    }

    /**
     * （日）返回时间段内的所有的天 type 0本期 1上期 2去年同期(日)
     *
     * @param begin 起始日期
     * @param end   截止日期
     * @return the list
     */
    public static List<String> getDaysList(String begin, String end) {
        List<String> lDate = new ArrayList<String>();
        Date date1; // 开始日期
        Date dEnd; // 结束日期
        try {
            date1 = PURE_DATETIME_FORMAT.parse(begin);
            dEnd = PURE_DATETIME_FORMAT.parse(end);
            Calendar calBegin = Calendar.getInstance();
            // 使用给定的 Date 设置此 Calendar 的时间
            calBegin.setTime(date1);
            Calendar calEnd = Calendar.getInstance();
            // 使用给定的 Date 设置此 Calendar 的时间
            calEnd.setTime(dEnd);
            // 添加第一个 既开始时间
            lDate.add(PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            while (calBegin.compareTo(calEnd) < 0) {
                // 根据日历的规则，为给定的日历字段添加或减去指定的时间量
                calBegin.add(Calendar.DAY_OF_MONTH, 1);
                Date ss = calBegin.getTime();
                String str = PURE_DATETIME_FORMAT.format(ss);
                lDate.add(str);
            }
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return lDate;
    }

    /**
     * （周）计算（周） 上期和去年同期的起止日期和起止周 计算上期的起止时间 和去年同期 type 0本期 1上期 2去年同期 起始日期key
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
    public static Map<String, String> getWeekDate(int type, String beginkey,
                                                  String endkey, String beginWkey, String endWkey, String begin,
                                                  String end, String beginW, String endW) {
        Map<String, String> map = new HashMap<String, String>();
        Date date1 = null; // 开始日期
        Date dEnd = null; // 结束日期
        try {
            date1 = PURE_DATETIME_FORMAT.parse(begin);
            dEnd = PURE_DATETIME_FORMAT.parse(end);
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
            map.put(beginkey, PURE_DATETIME_FORMAT.format(calBegin.getTime()));
            map.put(endkey, PURE_DATETIME_FORMAT.format(calEnd.getTime()));
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return map;

    }

    /**
     * （周）返回起止时间内的所有自然周
     *
     * @param begin  时间起
     * @param end    时间止
     * @param startw 周起
     * @param endW   周止
     * @return the list
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
            throw new InstrumentException(e);
        }
        return lDate;
    }

    /**
     * （月）得当时间段内的所有月份
     *
     * @param StartDate 开始日期
     * @param endDate   结束日期
     * @return the list
     */
    public static List<String> getYearMouthBy(String StartDate, String endDate) {
        DateFormat df = new SimpleDateFormat("yyyy-MM");
        Date date1 = null; // 开始日期
        Date date2 = null; // 结束日期
        try {
            date1 = df.parse(StartDate);
            date2 = df.parse(endDate);
        } catch (ParseException e) {
            throw new InstrumentException(e);
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
     *
     * @param type     计算上期
     * @param beginkey 开始时间key
     * @param endkey   截止时间key
     * @param begin    开始时间
     * @param end      截止时间
     * @return the map
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
            throw new InstrumentException(e);
        }
        return map;

    }

    /**
     * （年）计算本期（年）的上期
     *
     * @param beginkey 开始时间key
     * @param endkey   截止时间key
     * @param begin    开始时间
     * @param end      截止时间
     * @return the map
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
            throw new InstrumentException(e);
        }
        return map;

    }

    /**
     * 获取年份时间段内的所有年
     *
     * @param StartDate 开始时间
     * @param endDate   截止时间
     * @return the list
     */
    public static List<String> getYearBy(String StartDate, String endDate) {
        DateFormat df = new SimpleDateFormat("yyyy");
        Date date1 = null; // 开始日期
        Date date2 = null; // 结束日期
        try {
            date1 = df.parse(StartDate);
            date2 = df.parse(endDate);
        } catch (ParseException e) {
            throw new InstrumentException(e);
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
     *
     * @param start 日期
     * @param end   日期
     * @return the int
     */
    public static int getWeekCount(Date start, Date end) {
        Calendar c_begin = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        c_begin.setTime(start);
        Calendar c_end = Calendar.getInstance();
        // 使用给定的 Date 设置此 Calendar 的时间
        c_end.setTime(end);
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
     * @param year 最多53 一般52 如果12月月末今天在本年53周（属于第二年第一周） 那么按照当年52周算
     * @return the int
     */
    public static int getAllWeeks(String year) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(PURE_DATETIME_FORMAT.parse(year + "-12-31"));
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
                    calBegin.get((Calendar.YEAR))
                            + "-"
                            + setMonthByQuarterToString(0,
                            Integer.parseInt(map.get(beginWkey))));
            map.put(endkey,
                    calEnd.get((Calendar.YEAR))
                            + "-"
                            + setMonthByQuarterToString(1,
                            Integer.parseInt(map.get(endWkey))));
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return map;

    }

    /**
     * （季度）获取季度份时间段内的所有季度
     *
     * @param StartDate 开始日期
     * @param beginQ    开始季度
     * @param endDate   截止日期
     * @param endQ      结束季度
     * @return the list
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
            throw new InstrumentException(e);
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
        long r = 0;
        if (diff > Unit.DAY.getMillis()) {
            r = (diff / Unit.DAY.getMillis());
            return r + "天前";
        }
        if (diff > Unit.HOUR.getMillis()) {
            r = (diff / Unit.HOUR.getMillis());
            return r + "个小时前";
        }
        if (diff > Unit.MINUTE.getMillis()) {
            r = (diff / Unit.MINUTE.getMillis());
            return r + "分钟前";
        }
        return "刚刚";
    }

    /**
     * 转换日期
     *
     * @param date 日期
     * @return 日期
     */
    public static String getTimeCN(String date) {
        return NORM_DATE_CN_FORMAT.format(toDate(date));
    }

    /**
     * 转换日期
     *
     * @param date 日期
     * @return 日期
     */
    public static String getDayCN(String date) {
        return NORM_MONTH_CN_FORMAT.format(toDate(date));
    }

    /**
     * 转换星期
     *
     * @param date 日期
     * @return 日期
     */
    public static String getWeekCN(String date) {
        int w = 0;
        try {
            Calendar cal = Calendar.getInstance();
            Date d = PURE_DATETIME_FORMAT.parse(date);
            cal.setTime(d);
            w = cal.get(Calendar.DAY_OF_WEEK) - 1;
            if (w < 0) {
                w = 0;
            }
        } catch (ParseException e) {
            throw new InstrumentException(e);
        }
        return Week.of(w).toChinese("星期");
    }

    /**
     * 比较时间(string类型)大小
     *
     * @param date1 日期
     * @param date2 date1 大于date2 return 1
     *              date1 小于date2 return -1
     *              date1 等于date2 return 0
     * @return the int
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
            throw new InstrumentException(e);
        }
    }

    /**
     * 返回添加指定年后日期
     *
     * @param date   日期
     * @param amount 年份
     * @return the date
     */
    public static Date addYears(final Date date, final int amount) {
        return add(date, Calendar.YEAR, amount);
    }

    /**
     * 返回添加指定月后的日期
     *
     * @param date   日期
     * @param amount 月份
     * @return the date
     */
    public static Date addMonths(final Date date, final int amount) {
        return add(date, Calendar.MONTH, amount);
    }

    /**
     * 返回添加指定周后的日期
     *
     * @param date   日期
     * @param amount 周
     * @return the date
     */
    public static Date addWeeks(final Date date, final int amount) {
        return add(date, Calendar.WEEK_OF_YEAR, amount);
    }

    /**
     * 返回添加指定天后的日期
     *
     * @param date   日期
     * @param amount 天
     * @return the date
     */
    public static Date addDays(final Date date, final int amount) {
        return add(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * 返回添加指定小时后的日期
     *
     * @param date   日期
     * @param amount 小时
     * @return the date
     */
    public static Date addHours(final Date date, final int amount) {
        return add(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * 返回添加指定时间后的时间
     *
     * @param date   日期
     * @param amount 分钟
     * @return the date
     */
    public static Date addMinutes(final Date date, final int amount) {
        return add(date, Calendar.MINUTE, amount);
    }

    /**
     * 返回添加指定秒后的日期
     *
     * @param date   日期
     * @param amount 秒
     * @return the date
     */
    public static Date addSeconds(final Date date, final int amount) {
        return add(date, Calendar.SECOND, amount);
    }

    /**
     * 返回添加指定毫秒后的日期
     *
     * @param date   日期
     * @param amount 毫秒
     * @return the date
     */
    public static Date addMilliseconds(final Date date, final int amount) {
        return add(date, Calendar.MILLISECOND, amount);
    }

    /**
     * 返回添加指定规则后的日期
     *
     * @param date          日期
     * @param calendarField 规则
     * @param amount        数量
     * @return the date
     */
    private static Date add(final Date date, final int calendarField, final int amount) {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(calendarField, amount);
        return c.getTime();
    }

    /**
     * Sets the years field to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to set
     * @return a new {@code Date} set with the specified value
     */
    public static Date setYears(final Date date, final int amount) {
        return set(date, Calendar.YEAR, amount);
    }

    /**
     * Sets the months field to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to set
     * @return a new {@code Date} set with the specified value
     */
    public static Date setMonths(final Date date, final int amount) {
        return set(date, Calendar.MONTH, amount);
    }

    /**
     * Sets the day of month field to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to set
     * @return a new {@code Date} set with the specified value
     */
    public static Date setDays(final Date date, final int amount) {
        return set(date, Calendar.DAY_OF_MONTH, amount);
    }

    /**
     * Sets the hours field to a date returning a new object.  Hours range
     * from  0-23.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to set
     * @return a new {@code Date} set with the specified value
     */
    public static Date setHours(final Date date, final int amount) {
        return set(date, Calendar.HOUR_OF_DAY, amount);
    }

    /**
     * Sets the minute field to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to set
     * @return a new {@code Date} set with the specified value
     */
    public static Date setMinutes(final Date date, final int amount) {
        return set(date, Calendar.MINUTE, amount);
    }

    /**
     * Sets the seconds field to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to set
     * @return a new {@code Date} set with the specified value
     */
    public static Date setSeconds(final Date date, final int amount) {
        return set(date, Calendar.SECOND, amount);
    }

    /**
     * Sets the milliseconds field to a date returning a new object.
     * The original {@code Date} is unchanged.
     *
     * @param date   the date, not null
     * @param amount the amount to set
     * @return a new {@code Date} set with the specified value
     */
    public static Date setMilliseconds(final Date date, final int amount) {
        return set(date, Calendar.MILLISECOND, amount);
    }

    /**
     * Sets the specified field to a date returning a new object.
     * This does not use a lenient calendar.
     * The original {@code Date} is unchanged.
     *
     * @param date          the date, not null
     * @param calendarField the {@code Calendar} field to set the amount to
     * @param amount        the amount to set
     * @return a new {@code Date} set with the specified value
     */
    private static Date set(final Date date, final int calendarField, final int amount) {
        // getInstance() returns a new object, so this method is thread safe.
        final Calendar c = Calendar.getInstance();
        c.setLenient(false);
        c.setTime(date);
        c.set(calendarField, amount);
        return c.getTime();
    }

    /**
     * Converts a {@code Date} into a {@code Calendar}.
     *
     * @param date the date to convert to a Calendar
     * @return the created Calendar
     * @since 3.0
     */
    public static Calendar toCalendar(final Date date) {
        final Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    /**
     * Converts a {@code Date} of a given {@code TimeZone} into a {@code Calendar}
     *
     * @param date the date to convert to a Calendar
     * @param tz   the time zone of the {@code date}
     * @return the created Calendar
     */
    public static Calendar toCalendar(final Date date, final TimeZone tz) {
        final Calendar c = Calendar.getInstance(tz);
        c.setTime(date);
        return c;
    }

    /**
     * 出生日期转年龄
     *
     * @param birthday 时间戳字符串
     * @return int 年龄
     */
    public static Integer getAge(String birthday) {
        Integer age = 0;
        try {
            Date birthDay = new Date(Long.parseLong(birthday));
            Calendar cal = Calendar.getInstance();

            if (cal.before(birthDay)) {
                throw new IllegalArgumentException("The birthDay is before Now.It's unbelievable!");
            }
            int yearNow = cal.get(Calendar.YEAR);
            int monthNow = cal.get(Calendar.MONTH);
            int dayOfMonthNow = cal.get(Calendar.DAY_OF_MONTH);
            cal.setTime(birthDay);

            int yearBirth = cal.get(Calendar.YEAR);
            int monthBirth = cal.get(Calendar.MONTH);
            int dayOfMonthBirth = cal.get(Calendar.DAY_OF_MONTH);

            age = yearNow - yearBirth;

            if (monthNow <= monthBirth) {
                if (monthNow == monthBirth) {
                    if (dayOfMonthNow < dayOfMonthBirth) {
                        age--;
                    }
                } else {
                    age--;
                }
            }

        } catch (Exception e) {
            return null;
        }
        return age;
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

    /**
     * 获取中文星期
     *
     * @param date 日期
     * @return 星期
     */
    public String getChineseWeek(Calendar date) {
        String[] dayNames = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
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
     * 获取公共节假日天数
     *
     * @param d1 日期
     * @param d2 日期
     * @return the int
     */
    public int getHolidays(Calendar d1, Calendar d2) {
        return getDaysBetween(d1, d2) - getWorkingDay(d1, d2);
    }

}
