/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.center.date;

import java.text.ParsePosition;
import java.time.*;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.xml.datatype.XMLGregorianCalendar;

import org.miaixz.bus.core.center.date.culture.en.Modify;
import org.miaixz.bus.core.center.date.culture.en.Month;
import org.miaixz.bus.core.center.date.culture.en.Various;
import org.miaixz.bus.core.center.date.format.FormatManager;
import org.miaixz.bus.core.center.date.format.parser.DateParser;
import org.miaixz.bus.core.center.date.format.parser.FastDateParser;
import org.miaixz.bus.core.center.date.format.parser.PositionDateParser;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.DateException;
import org.miaixz.bus.core.math.ChineseNumberFormatter;
import org.miaixz.bus.core.xyz.CompareKit;
import org.miaixz.bus.core.xyz.ObjectKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * 针对{@link java.util.Calendar} 对象封装工具类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Calendar extends Calculate {

    /**
     * 是否为上午
     *
     * @param calendar {@link java.util.Calendar}
     * @return 是否为上午
     */
    public static boolean isAM(final java.util.Calendar calendar) {
        return java.util.Calendar.AM == calendar.get(java.util.Calendar.AM_PM);
    }

    /**
     * 是否为下午
     *
     * @param calendar {@link java.util.Calendar}
     * @return 是否为下午
     */
    public static boolean isPM(final java.util.Calendar calendar) {
        return java.util.Calendar.PM == calendar.get(java.util.Calendar.AM_PM);
    }

    /**
     * 比较两个日期是否为同一天
     *
     * @param cal1 日期1
     * @param cal2 日期2
     * @return 是否为同一天
     */
    public static boolean isSameDay(final java.util.Calendar cal1, java.util.Calendar cal2) {
        if (ObjectKit.notEquals(cal1.getTimeZone(), cal2.getTimeZone())) {
            // 统一时区
            cal2 = calendar(cal2, cal1.getTimeZone());
        }
        return isSameYear(cal1, cal2)
                && cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);
    }

    /**
     * 比较两个日期是否为同一周 同一个周的意思是：ERA（公元）、year（年）、month（月）、week（周）都一致。
     *
     * @param cal1  日期1
     * @param cal2  日期2
     * @param isMon 一周的第一天是否为周一。国内第一天为星期一，国外第一天为星期日
     * @return 是否为同一周
     */
    public static boolean isSameWeek(java.util.Calendar cal1, java.util.Calendar cal2, final boolean isMon) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }

        // 防止比较前修改原始Calendar对象
        cal1 = (java.util.Calendar) cal1.clone();

        if (ObjectKit.notEquals(cal1.getTimeZone(), cal2.getTimeZone())) {
            // 统一时区
            cal2 = calendar(cal2, cal1.getTimeZone());
        } else {
            cal2 = (java.util.Calendar) cal2.clone();
        }

        // 把所传日期设置为其当前周的第一天
        // 比较设置后的两个日期是否是同一天：true 代表同一周
        if (isMon) {
            cal1.setFirstDayOfWeek(java.util.Calendar.MONDAY);
            cal1.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
            cal2.setFirstDayOfWeek(java.util.Calendar.MONDAY);
            cal2.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.MONDAY);
        } else {
            cal1.setFirstDayOfWeek(java.util.Calendar.SUNDAY);
            cal1.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY);
            cal2.setFirstDayOfWeek(java.util.Calendar.SUNDAY);
            cal2.set(java.util.Calendar.DAY_OF_WEEK, java.util.Calendar.SUNDAY);
        }
        return isSameDay(cal1, cal2);
    }

    /**
     * 比较两个日期是否为同一月 同一个月的意思是：ERA（公元）、year（年）、month（月）都一致。
     *
     * @param cal1 日期1
     * @param cal2 日期2
     * @return 是否为同一月
     */
    public static boolean isSameMonth(final java.util.Calendar cal1, java.util.Calendar cal2) {
        if (ObjectKit.notEquals(cal1.getTimeZone(), cal2.getTimeZone())) {
            // 统一时区
            cal2 = calendar(cal2, cal1.getTimeZone());
        }

        return isSameYear(cal1, cal2) && cal1.get(java.util.Calendar.MONTH) == cal2.get(java.util.Calendar.MONTH);
    }

    /**
     * 比较两个日期是否为同一年 同一个年的意思是：ERA（公元）、year（年）都一致。
     *
     * @param cal1 日期1
     * @param cal2 日期2
     * @return 是否为同一年
     */
    public static boolean isSameYear(final java.util.Calendar cal1, java.util.Calendar cal2) {
        if (cal1 == null || cal2 == null) {
            throw new IllegalArgumentException("The date must not be null");
        }

        if (ObjectKit.notEquals(cal1.getTimeZone(), cal2.getTimeZone())) {
            // 统一时区
            cal2 = calendar(cal2, cal1.getTimeZone());
        }

        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
                && cal1.get(java.util.Calendar.ERA) == cal2.get(java.util.Calendar.ERA);
    }

    /**
     * 检查两个Calendar时间戳是否相同 此方法检查两个Calendar的毫秒数时间戳是否相同
     *
     * @param date1 时间1
     * @param date2 时间2
     * @return 两个Calendar时间戳是否相同。如果两个时间都为{@code null}返回true，否则有{@code null}返回false
     */
    public static boolean isSameInstant(final java.util.Calendar date1, final java.util.Calendar date2) {
        if (null == date1) {
            return null == date2;
        }
        if (null == date2) {
            return false;
        }

        return date1.getTimeInMillis() == date2.getTimeInMillis();
    }

    /**
     * 是否为本月第一天
     *
     * @param calendar {@link java.util.Calendar}
     * @return 是否为本月最后一天
     */
    public static boolean isFirstDayOfMonth(final java.util.Calendar calendar) {
        return 1 == calendar.get(java.util.Calendar.DAY_OF_MONTH);
    }

    /**
     * 是否为本月最后一天
     *
     * @param calendar {@link java.util.Calendar}
     * @return 是否为本月最后一天
     */
    public static boolean isLastDayOfMonth(final java.util.Calendar calendar) {
        return calendar.get(java.util.Calendar.DAY_OF_MONTH) == calendar
                .getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
    }

    /**
     * 创建Calendar对象，时间为默认时区的当前时间
     *
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar calendar() {
        return java.util.Calendar.getInstance();
    }

    /**
     * 转换为Calendar对象
     *
     * @param date 日期对象
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar calendar(final Date date) {
        if (date instanceof DateTime) {
            return ((DateTime) date).toCalendar();
        } else {
            return calendar(date.getTime());
        }
    }

    /**
     * 转换为Calendar对象
     *
     * @param calendar 日期对象
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar calendar(final XMLGregorianCalendar calendar) {
        return calendar.toGregorianCalendar();
    }

    /**
     * 转换为Calendar对象，使用当前默认时区
     *
     * @param millis 时间戳
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar calendar(final long millis) {
        return calendar(millis, TimeZone.getDefault());
    }

    /**
     * 转换为Calendar对象
     *
     * @param millis   时间戳
     * @param timeZone 时区
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar calendar(final long millis, final TimeZone timeZone) {
        final java.util.Calendar cal = java.util.Calendar.getInstance(timeZone);
        cal.setTimeInMillis(millis);
        return cal;
    }

    /**
     * 转换为指定时区的Calendar，返回新的Calendar
     *
     * @param calendar 时间
     * @param timeZone 新时区
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar calendar(java.util.Calendar calendar, final TimeZone timeZone) {
        // 转换到统一时区，例如UTC
        calendar = (java.util.Calendar) calendar.clone();
        calendar.setTimeZone(timeZone);
        return calendar;
    }

    /**
     * 修改日期为某个时间字段起始时间
     *
     * @param calendar {@link java.util.Calendar}
     * @param various  保留到的时间字段，如定义为 {@link Various#SECOND}，表示这个字段不变，这个字段以下字段全部归0
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar truncate(final java.util.Calendar calendar, final Various various) {
        return Modifier.modify(calendar, various.getValue(), Modify.TRUNCATE);
    }

    /**
     * 修改日期为某个时间字段四舍五入时间
     *
     * @param calendar {@link java.util.Calendar}
     * @param various  时间字段，即保留到哪个日期字段
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar round(final java.util.Calendar calendar, final Various various) {
        return Modifier.modify(calendar, various.getValue(), Modify.ROUND);
    }

    /**
     * 修改日期为某个时间字段结束时间 可选是否归零毫秒。
     *
     * <p>
     * 有时候由于毫秒部分必须为0（如MySQL数据库中），因此在此加上选项。
     * </p>
     *
     * @param calendar {@link java.util.Calendar}
     * @param various  时间字段
     * @param truncate 是否毫秒归零
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar ceiling(final java.util.Calendar calendar, final Various various,
            final boolean truncate) {
        return Modifier.modify(calendar, various.getValue(), Modify.CEILING, truncate);
    }

    /**
     * 修改秒级别的开始时间，即忽略毫秒部分
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar beginOfSecond(final java.util.Calendar calendar) {
        return truncate(calendar, Various.SECOND);
    }

    /**
     * 修改秒级别的结束时间，即毫秒设置为999
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @param truncate 是否毫秒归零
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar endOfSecond(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.SECOND, truncate);
    }

    /**
     * 修改某小时的开始时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar beginOfHour(final java.util.Calendar calendar) {
        return truncate(calendar, Various.HOUR_OF_DAY);
    }

    /**
     * 修改某小时的结束时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @param truncate 是否毫秒归零
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar endOfHour(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.HOUR_OF_DAY, truncate);
    }

    /**
     * 修改某分钟的开始时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar beginOfMinute(final java.util.Calendar calendar) {
        return truncate(calendar, Various.MINUTE);
    }

    /**
     * 修改某分钟的结束时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @param truncate 是否毫秒归零
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar endOfMinute(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.MINUTE, truncate);
    }

    /**
     * 修改某天的开始时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar beginOfDay(final java.util.Calendar calendar) {
        return truncate(calendar, Various.DAY_OF_MONTH);
    }

    /**
     * 修改某天的结束时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @param truncate 是否毫秒归零
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar endOfDay(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.DAY_OF_MONTH, truncate);
    }

    /**
     * 修改给定日期当前周的开始时间，周一定为一周的开始时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar beginOfWeek(final java.util.Calendar calendar) {
        return beginOfWeek(calendar, true);
    }

    /**
     * 修改给定日期当前周的开始时间
     *
     * @param calendar           日期 {@link java.util.Calendar}
     * @param isMondayAsFirstDay 是否周一做为一周的第一天（false表示周日做为第一天）
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar beginOfWeek(final java.util.Calendar calendar, final boolean isMondayAsFirstDay) {
        calendar.setFirstDayOfWeek(isMondayAsFirstDay ? java.util.Calendar.MONDAY : java.util.Calendar.SUNDAY);
        // WEEK_OF_MONTH为上限的字段（不包括），实际调整的为DAY_OF_MONTH
        return truncate(calendar, Various.WEEK_OF_MONTH);
    }

    /**
     * 修改某周的结束时间
     *
     * @param calendar          日期 {@link java.util.Calendar}
     * @param isSundayAsLastDay 是否周日做为一周的最后一天（false表示周六做为最后一天）
     * @param truncate          是否毫秒归零
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar endOfWeek(final java.util.Calendar calendar, final boolean isSundayAsLastDay,
            final boolean truncate) {
        calendar.setFirstDayOfWeek(isSundayAsLastDay ? java.util.Calendar.MONDAY : java.util.Calendar.SUNDAY);
        // WEEK_OF_MONTH为上限的字段（不包括），实际调整的为DAY_OF_MONTH
        return ceiling(calendar, Various.WEEK_OF_MONTH, truncate);
    }

    /**
     * 修改某月的开始时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar beginOfMonth(final java.util.Calendar calendar) {
        return truncate(calendar, Various.MONTH);
    }

    /**
     * 修改某月的结束时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @param truncate 是否毫秒归零
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar endOfMonth(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.MONTH, truncate);
    }

    /**
     * 修改某季度的开始时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar beginOfQuarter(final java.util.Calendar calendar) {
        calendar.set(java.util.Calendar.MONTH, calendar.get(Various.MONTH.getValue()) / 3 * 3);
        calendar.set(java.util.Calendar.DAY_OF_MONTH, 1);
        return beginOfDay(calendar);
    }

    /**
     * 获取某季度的结束时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @param truncate 是否毫秒归零
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar endOfQuarter(final java.util.Calendar calendar, final boolean truncate) {
        final int year = calendar.get(java.util.Calendar.YEAR);
        final int month = calendar.get(Various.MONTH.getValue()) / 3 * 3 + 2;

        final java.util.Calendar resultCal = java.util.Calendar.getInstance(calendar.getTimeZone());
        resultCal.set(year, month, Month.of(month).getLastDay(isLeapYear(year)));

        return endOfDay(resultCal, truncate);
    }

    /**
     * 修改某年的开始时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar beginOfYear(final java.util.Calendar calendar) {
        return truncate(calendar, Various.YEAR);
    }

    /**
     * 修改某年的结束时间
     *
     * @param calendar 日期 {@link java.util.Calendar}
     * @param truncate 是否毫秒归零
     * @return {@link java.util.Calendar}
     */
    public static java.util.Calendar endOfYear(final java.util.Calendar calendar, final boolean truncate) {
        return ceiling(calendar, Various.YEAR, truncate);
    }

    /**
     * 获得指定日期年份和季度 格式：[20131]表示2013年第一季度
     *
     * @param cal 日期
     * @return 年和季度，格式类似于20131
     */
    public static String yearAndQuarter(final java.util.Calendar cal) {
        return StringKit.builder().append(cal.get(java.util.Calendar.YEAR))
                .append(cal.get(java.util.Calendar.MONTH) / 3 + 1).toString();
    }

    /**
     * 获取指定日期字段的最小值，例如分钟的最小值是0
     *
     * @param calendar {@link java.util.Calendar}
     * @param various  {@link Various}
     * @return 字段最小值
     * @see java.util.Calendar#getActualMinimum(int)
     */
    public static int getBeginValue(final java.util.Calendar calendar, final Various various) {
        return getBeginValue(calendar, various.getValue());
    }

    /**
     * 获取指定日期字段的最小值，例如分钟的最小值是0
     *
     * @param calendar  {@link java.util.Calendar}
     * @param dateField {@link Various}
     * @return 字段最小值
     * @see java.util.Calendar#getActualMinimum(int)
     */
    public static int getBeginValue(final java.util.Calendar calendar, final int dateField) {
        if (java.util.Calendar.DAY_OF_WEEK == dateField) {
            return calendar.getFirstDayOfWeek();
        }
        return calendar.getActualMinimum(dateField);
    }

    /**
     * 获取指定日期字段的最大值，例如分钟的最大值是59
     *
     * @param calendar {@link java.util.Calendar}
     * @param various  {@link Various}
     * @return 字段最大值
     * @see java.util.Calendar#getActualMaximum(int)
     */
    public static int getEndValue(final java.util.Calendar calendar, final Various various) {
        return getEndValue(calendar, various.getValue());
    }

    /**
     * 获取指定日期字段的最大值，例如分钟的最大值是59
     *
     * @param calendar  {@link java.util.Calendar}
     * @param dateField {@link Various}
     * @return 字段最大值
     * @see java.util.Calendar#getActualMaximum(int)
     */
    public static int getEndValue(final java.util.Calendar calendar, final int dateField) {
        if (java.util.Calendar.DAY_OF_WEEK == dateField) {
            return (calendar.getFirstDayOfWeek() + 6) % 7;
        }
        return calendar.getActualMaximum(dateField);
    }

    /**
     * 获得日期的某个部分 例如获得年的部分，则使用 getField(DatePart.YEAR)
     *
     * @param calendar {@link java.util.Calendar}
     * @param field    表示日期的哪个部分的枚举 {@link Various}
     * @return 某个部分的值
     */
    public static int getField(final java.util.Calendar calendar, final Various field) {
        return Assert.notNull(calendar).get(Assert.notNull(field).getValue());
    }

    /**
     * Calendar{@link Instant}对象
     *
     * @param calendar Date对象
     * @return {@link Instant}对象
     */
    public static Instant toInstant(final java.util.Calendar calendar) {
        return null == calendar ? null : calendar.toInstant();
    }

    /**
     * {@link java.util.Calendar} 转换为 {@link LocalDateTime}，使用系统默认时区
     *
     * @param calendar {@link java.util.Calendar}
     * @return {@link LocalDateTime}
     */
    public static LocalDateTime toLocalDateTime(final java.util.Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return LocalDateTime.ofInstant(calendar.toInstant(), calendar.getTimeZone().toZoneId());
    }

    /**
     * {@code null}安全的{@link java.util.Calendar}比较，{@code null}小于任何日期
     *
     * @param calendar1 日期1
     * @param calendar2 日期2
     * @return 比较结果，如果calendar1 &lt; calendar2，返回数小于0，calendar1==calendar2返回0，calendar1 &gt; calendar2 大于0
     */
    public static int compare(final java.util.Calendar calendar1, final java.util.Calendar calendar2) {
        return CompareKit.compare(calendar1, calendar2);
    }

    /**
     * 将指定Calendar时间格式化为纯中文形式，比如：
     *
     * <pre>
     *     2018-02-24 12:13:14 转换为 二〇一八年二月二十四日（withTime为false）
     *     2018-02-24 12:13:14 转换为 二〇一八年二月二十四日十二时十三分十四秒（withTime为true）
     * </pre>
     *
     * @param calendar {@link java.util.Calendar}
     * @param withTime 是否包含时间部分
     * @return 格式化后的字符串
     */
    public static String formatChineseDate(final java.util.Calendar calendar, final boolean withTime) {
        final StringBuilder result = StringKit.builder();

        // 年
        final String year = String.valueOf(calendar.get(java.util.Calendar.YEAR));
        final int length = year.length();
        for (int i = 0; i < length; i++) {
            result.append(ChineseNumberFormatter.formatChar(year.charAt(i), false));
        }
        result.append('年');

        // 月
        final int month = calendar.get(java.util.Calendar.MONTH) + 1;
        result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(month));
        result.append('月');

        // 日
        final int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(day));
        result.append('日');

        // 只替换年月日，时分秒中零不需要替换
        final String temp = result.toString().replace(Symbol.C_UL_ZERO, '〇');
        result.delete(0, result.length());
        result.append(temp);

        if (withTime) {
            // 时
            final int hour = calendar.get(java.util.Calendar.HOUR_OF_DAY);
            result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(hour));
            result.append('时');
            // 分
            final int minute = calendar.get(java.util.Calendar.MINUTE);
            result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(minute));
            result.append('分');
            // 秒
            final int second = calendar.get(java.util.Calendar.SECOND);
            result.append(ChineseNumberFormatter.of().setColloquialMode(true).format(second));
            result.append('秒');
        }

        return result.toString();
    }

    /**
     * 通过给定的日期格式解析日期时间字符串。 传入的日期格式会逐个尝试，直到解析成功，返回{@link java.util.Calendar}对象，否则抛出{@link DateException}异常。 方法来自：Apache
     * Commons-Lang3
     *
     * @param text          日期时间字符串，非空
     * @param parsePatterns 需要尝试的日期时间格式数组，非空, 见SimpleDateFormat
     * @return 解析后的Calendar
     * @throws IllegalArgumentException 如果日期字符串或模式数组为空
     * @throws DateException            如果没有合适的日期模式
     */
    public static java.util.Calendar parseByPatterns(final CharSequence text, final String... parsePatterns)
            throws DateException {
        return parseByPatterns(text, null, parsePatterns);
    }

    /**
     * 通过给定的日期格式解析日期时间字符串。 传入的日期格式会逐个尝试，直到解析成功，返回{@link java.util.Calendar}对象，否则抛出{@link DateException}异常。 方法来自：Apache
     * Commons-Lang3
     *
     * @param text          日期时间字符串，非空
     * @param locale        地区，当为{@code null}时使用{@link Locale#getDefault()}
     * @param parsePatterns 需要尝试的日期时间格式数组，非空, 见SimpleDateFormat
     * @return 解析后的Calendar
     * @throws IllegalArgumentException 如果日期字符串或模式数组为空
     * @throws DateException            如果没有合适的日期模式
     */
    public static java.util.Calendar parseByPatterns(final CharSequence text, final Locale locale,
            final String... parsePatterns) throws DateException {
        return parseByPatterns(text, locale, true, parsePatterns);
    }

    /**
     * 通过给定的日期格式解析日期时间字符串。 传入的日期格式会逐个尝试，直到解析成功，返回{@link java.util.Calendar}对象，否则抛出{@link DateException}异常。 方法来自：Apache
     * Commons-Lang3
     *
     * @param text          日期时间字符串，非空
     * @param locale        地区，当为{@code null}时使用{@link Locale#getDefault()}
     * @param lenient       日期时间解析是否使用严格模式
     * @param parsePatterns 需要尝试的日期时间格式数组，非空, 见SimpleDateFormat
     * @return 解析后的Calendar
     * @throws IllegalArgumentException 如果日期字符串或模式数组为空
     * @throws DateException            如果没有合适的日期模式
     * @see java.util.Calendar#isLenient()
     */
    public static java.util.Calendar parseByPatterns(final CharSequence text, final Locale locale,
            final boolean lenient, final String... parsePatterns) throws DateException {
        if (text == null || parsePatterns == null) {
            throw new IllegalArgumentException("Date and Patterns must not be null");
        }

        final TimeZone tz = TimeZone.getDefault();
        final Locale lcl = ObjectKit.defaultIfNull(locale, Locale.getDefault());
        final ParsePosition pos = new ParsePosition(0);
        final java.util.Calendar calendar = java.util.Calendar.getInstance(tz, lcl);
        calendar.setLenient(lenient);

        final FormatManager formatManager = FormatManager.getInstance();
        for (final String parsePattern : parsePatterns) {
            if (formatManager.isCustomParse(parsePattern)) {
                final Date parse = formatManager.parse(text, parsePattern);
                if (null == parse) {
                    continue;
                }
                calendar.setTime(parse);
                return calendar;
            }

            final FastDateParser fdp = new FastDateParser(parsePattern, tz, lcl);
            calendar.clear();
            try {
                if (fdp.parse(text, pos, calendar) && pos.getIndex() == text.length()) {
                    return calendar;
                }
            } catch (final IllegalArgumentException ignore) {
                // 宽大处理是防止日历被设定
            }
            pos.setIndex(0);
        }

        throw new DateException("Unable to parse the date: {}", text);
    }

    /**
     * 使用指定{@link DateParser}解析字符串为{@link java.util.Calendar}
     *
     * @param text    日期字符串
     * @param lenient 是否宽容模式
     * @param parser  {@link DateParser}
     * @return 解析后的 {@link java.util.Calendar}，解析失败返回{@code null}
     * @throws DateException 解析失败抛出此异常
     */
    public static java.util.Calendar parse(final CharSequence text, final boolean lenient,
            final PositionDateParser parser) {
        Assert.notNull(parser, "Parser must be not null!");
        return parser.parseCalendar(text, null, lenient);
    }

    /**
     * 计算年龄
     *
     * @param birthDay 生日
     * @return int 年龄
     */
    public static int age(LocalDate birthDay) {
        Period period = Period.between(birthDay, LocalDate.now());
        if (period.getYears() < 0) {
            throw new DateTimeException("birthDay is after now!");
        } else {
            return period.getYears();
        }
    }

    /**
     * 计算年龄
     *
     * @param birthDay 生日
     * @return int 年龄
     */
    public static int age(LocalDateTime birthDay) {
        return age(birthDay.toLocalDate());
    }

    /**
     * 计算相对于dateToCompare的年龄，常用于计算指定生日在某年的年龄
     * 按照《最高人民法院关于审理未成年人刑事案件具体应用法律若干问题的解释》第二条规定刑法第十七条规定的“周岁”，按照公历的年、月、日计算，从周岁生日的第二天起算。
     * <ul>
     * <li>2022-03-01出生，则相对2023-03-01，周岁为0，相对于2023-03-02才是1岁。</li>
     * <li>1999-02-28出生，则相对2000-02-29，周岁为1</li>
     * </ul>
     *
     * @param birthday      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    public static int age(final java.util.Calendar birthday, final java.util.Calendar dateToCompare) {
        return age(birthday.getTimeInMillis(), dateToCompare.getTimeInMillis());
    }

    /**
     * 计算相对于dateToCompare的年龄（周岁），常用于计算指定生日在某年的年龄
     * 按照《最高人民法院关于审理未成年人刑事案件具体应用法律若干问题的解释》第二条规定刑法第十七条规定的“周岁”，按照公历的年、月、日计算，从周岁生日的第二天起算。
     * <ul>
     * <li>2022-03-01出生，则相对2023-03-01，周岁为0，相对于2023-03-02才是1岁。</li>
     * <li>1999-02-28出生，则相对2000-02-29，周岁为1</li>
     * </ul>
     *
     * @param birthday      生日
     * @param dateToCompare 需要对比的日期
     * @return 年龄
     */
    protected static int age(final long birthday, final long dateToCompare) {
        if (birthday > dateToCompare) {
            throw new IllegalArgumentException("Birthday is after dateToCompare!");
        }

        final java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTimeInMillis(dateToCompare);

        final int year = cal.get(java.util.Calendar.YEAR);
        final int month = cal.get(java.util.Calendar.MONTH);
        final int dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH);

        // 复用cal
        cal.setTimeInMillis(birthday);
        int age = year - cal.get(java.util.Calendar.YEAR);
        // 当前日期，则为0岁
        if (age == 0) {
            return 0;
        }

        final int monthBirth = cal.get(java.util.Calendar.MONTH);
        if (month == monthBirth) {
            final int dayOfMonthBirth = cal.get(java.util.Calendar.DAY_OF_MONTH);
            // 法定生日当天不算年龄，从第二天开始计算
            if (dayOfMonth <= dayOfMonthBirth) {
                // 如果生日在当月，但是未达到生日当天的日期，年龄减一
                age--;
            }
        } else if (month < monthBirth) {
            // 如果当前月份未达到生日的月份，年龄计算减一
            age--;
        }

        return age;
    }

}
