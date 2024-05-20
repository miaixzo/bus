/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.core.lang;

import java.time.DayOfWeek;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Locale;

/**
 * 日期场景属性
 *
 * <p>
 * 工具类，提供格式化字符串很多，但是对于具体什么含义，不够清晰，这里进行说明：
 * </p>
 * 常见日期格式模式字符串：
 * <ul>
 *    <li>yyyy-MM-dd                   示例：2022-08-05</li>
 *    <li>yyyy年MM月dd日                示例：2022年08月05日</li>
 *    <li>yyyy-MM-dd HH:mm:ss          示例：2022-08-05 12:59:59</li>
 *    <li>yyyy-MM-dd HH:mm:ss.SSS      示例：2022-08-05 12:59:59.559</li>
 *    <li>yyyy-MM-dd HH:mm:ss.SSSZ     示例：2022-08-05 12:59:59.559+0800【东八区中国时区】、2022-08-05 04:59:59.559+0000【冰岛0时区】, 年月日 时分秒 毫秒 时区</li>
 *    <li>yyyy-MM-dd HH:mm:ss.SSSz     示例：2022-08-05 12:59:59.559UTC【世界标准时间=0时区】、2022-08-05T12:59:59.599GMT【冰岛0时区】、2022-08-05T12:59:59.599CST【东八区中国时区】、2022-08-23T03:45:00.599EDT【美国东北纽约时间，-0400】 ,年月日 时分秒 毫秒 时区</li>
 *    <li>yyyy-MM-dd'T'HH:mm:ss.SSS'Z' 示例：2022-08-05T12:59:59.559Z, 其中：''单引号表示转义字符，T:分隔符，Z:一般指UTC,0时区的时间含义</li>
 *    <li>yyyy-MM-dd'T'HH:mm:ss.SSSZ   示例：2022-08-05T11:59:59.559+0800, 其中：Z,表示时区</li>
 *    <li>yyyy-MM-dd'T'HH:mm:ss.SSSX   示例：2022-08-05T12:59:59.559+08, 其中：X:两位时区，+08表示：东8区，中国时区</li>
 *    <li>yyyy-MM-dd'T'HH:mm:ss.SSSXX  示例：2022-08-05T12:59:59.559+0800, 其中：XX:四位时区</li>
 *    <li>yyyy-MM-dd'T'HH:mm:ss.SSSXXX 示例：2022-08-05T12:59:59.559+08:00, 其中：XX:五位时区</li>
 *    <li>yyyy-MM-dd'T'HH:mm:ss        示例：2022-08-05T12:59:59+08</li>
 *    <li>yyyy-MM-dd'T'HH:mm:ssXXX     示例：2022-08-05T12:59:59+08:00</li>
 *    <li>yyyy-MM-dd'T'HH:mm:ssZ       示例：2022-08-05T12:59:59+0800</li>
 *    <li>yyyy-MM-dd'T'HH:mm:ss'Z'     示例：2022-08-05T12:59:59Z</li>
 *    <li>EEE MMM dd HH:mm:ss z yyyy   示例：周五 8月 05 12:59:00 UTC+08:00 2022</li>
 *    <li>EEE MMM dd HH:mm:ss zzz yyyy 示例：周五 8月 05 12:59:00 UTC+08:00 2022,其中z表示UTC时区，但：1~3个z没有任何区别</li>
 *    <li>EEE, dd MMM yyyy HH:mm:ss z  示例：周五, 05 8月 2022 12:59:59 UTC+08:00</li>
 * </ul>
 * <p>
 * 系统提供的，请查看，有大量定义好的格式化对象，可以直接使用，如：
 * {@link DateTimeFormatter#ISO_DATE}
 * {@link DateTimeFormatter#ISO_DATE_TIME}
 * 查看更多，请参阅上述官方文档
 * </p>
 *
 * <p>
 * 特殊说明：UTC时间，世界标准时间，0时区的时间，伦敦时间，可以直接加Z表示不加空格，
 * 如：“09:30 UTC”表示为“09:30Z”或“T0930Z”，其中：Z 是 +00:00 的缩写，意思是 UTC(零时分秒的偏移量).
 * </p>
 * <ul>
 *     <li>yyyy-MM-dd'T'HH:mm:ss'Z'</li>
 *     <li>2022-08-23T15:20:46UTC</li>
 *     <li>2022-08-23T15:20:46 UTC</li>
 *     <li>2022-08-23T15:20:46+0000</li>
 *     <li>2022-08-23T15:20:46 +0000</li>
 *     <li>2022-08-23T15:20:46Z</li>
 * </ul>
 * <p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Fields {

    /**
     * 年格式：yyyy
     */
    public static final String NORM_YEAR = "yyyy";
    /**
     * 年月格式：yyyy-MM
     */
    public static final String NORM_MONTH = "yyyy-MM";

    /**
     * 简单年月格式：yyyyMM
     */
    public static final String SIMPLE_MONTH = "yyyyMM";

    /**
     * 标准日期格式：yyyy-MM-dd
     */
    public static final String NORM_DATE = "yyyy-MM-dd";

    /**
     * 标准时间格式：HH:mm:ss
     */
    public static final String NORM_TIME = "HH:mm:ss";

    /**
     * 标准日期时间格式，精确到分：yyyy-MM-dd HH:mm
     */
    public static final String NORM_DATETIME_MINUTE = "yyyy-MM-dd HH:mm";

    /**
     * 标准日期时间格式，精确到秒：yyyy-MM-dd HH:mm:ss
     */
    public static final String NORM_DATETIME = "yyyy-MM-dd HH:mm:ss";

    /**
     * 标准日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final String NORM_DATETIME_MS = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * ISO8601日期时间格式，精确到毫秒：yyyy-MM-dd HH:mm:ss,SSS
     */
    public static final String NORM_DATETIME_COMMA_MS = "yyyy-MM-dd HH:mm:ss,SSS";

    /**
     * 标准日期格式：yyyy年MM月dd日
     */
    public static final String CN_DATE = "yyyy年MM月dd日";

    /**
     * 标准日期格式：yyyy年MM月dd日HH时mm分ss秒
     */
    public static final String CN_DATE_TIME = "yyyy年MM月dd日HH时mm分ss秒";

    /**
     * 标准日期格式：yyyyMMdd
     */
    public static final String PURE_DATE = "yyyyMMdd";

    /**
     * 标准日期格式：HHmmss
     */
    public static final String PURE_TIME = "HHmmss";

    /**
     * 标准日期格式：yyyyMMddHHmmss
     */
    public static final String PURE_DATETIME = "yyyyMMddHHmmss";

    /**
     * 标准日期格式：yyyyMMddHHmmssSSS
     */
    public static final String PURE_DATETIME_MS = "yyyyMMddHHmmssSSS";

    /**
     * 格式化通配符: yyyyMMddHHmmss.SSS
     */
    public static final String PURE_DATETIME_TIP_PATTERN = "yyyyMMddHHmmss.SSS";

    /**
     * HTTP头中日期时间格式：EEE, dd MMM yyyy HH:mm:ss z
     */
    public static final String HTTP_DATETIME = "EEE, dd MMM yyyy HH:mm:ss z";

    /**
     * JDK中日期时间格式：EEE MMM dd HH:mm:ss zzz yyyy
     */
    public static final String JDK_DATETIME = "EEE MMM dd HH:mm:ss zzz yyyy";

    /**
     * ISO8601日期时间：yyyy-MM-dd'T'HH:mm:ss
     * 按照ISO8601规范，默认使用T分隔日期和时间，末尾不加Z表示当地时区
     */
    public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss";

    /**
     * UTC时间：yyyy-MM-dd'T'HH:mm:ss.SSS
     */
    public static final String ISO8601_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    /**
     * UTC时间：yyyy-MM-dd'T'HH:mm:ss'Z'
     * 按照ISO8601规范，后缀加Z表示UTC时间
     */
    public static final String UTC = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    /**
     * ISO8601时间：yyyy-MM-dd'T'HH:mm:ssZ，Z表示一个时间偏移，如+0800
     */
    public static final String ISO8601_WITH_ZONE_OFFSET = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     * ISO8601时间：yyyy-MM-dd'T'HH:mm:ssXXX
     */
    public static final String ISO8601_WITH_XXX_OFFSET = "yyyy-MM-dd'T'HH:mm:ssXXX";

    /**
     * ISO8601时间：yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static final String UTC_MS = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    /**
     * ISO8601时间：yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static final String ISO8601_MS_WITH_ZONE_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /**
     * ISO8601时间：yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public static final String ISO8601_MS_WITH_XXX_OFFSET = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    /**
     * 星期枚举
     * 与Calendar中的星期int值对应
     *
     * @see #SUNDAY
     * @see #MONDAY
     * @see #TUESDAY
     * @see #WEDNESDAY
     * @see #THURSDAY
     * @see #FRIDAY
     * @see #SATURDAY
     */
    public enum Week {

        /**
         * 周日
         */
        SUNDAY(Calendar.SUNDAY),
        /**
         * 周一
         */
        MONDAY(Calendar.MONDAY),
        /**
         * 周二
         */
        TUESDAY(Calendar.TUESDAY),
        /**
         * 周三
         */
        WEDNESDAY(Calendar.WEDNESDAY),
        /**
         * 周四
         */
        THURSDAY(Calendar.THURSDAY),
        /**
         * 周五
         */
        FRIDAY(Calendar.FRIDAY),
        /**
         * 周六
         */
        SATURDAY(Calendar.SATURDAY);

        private static final Week[] ENUMS = Week.values();

        /**
         * 星期对应{@link Calendar} 中的Week值
         */
        private final int value;

        /**
         * 构造
         *
         * @param value 星期对应{@link Calendar} 中的Week值
         */
        Week(final int value) {
            this.value = value;
        }

        /**
         * 将 {@link Calendar}星期相关值转换为Week枚举对象
         *
         * @param calendarWeekIntValue Calendar中关于Week的int值，1表示Sunday
         * @return Week
         * @see #SUNDAY
         * @see #MONDAY
         * @see #TUESDAY
         * @see #WEDNESDAY
         * @see #THURSDAY
         * @see #FRIDAY
         * @see #SATURDAY
         */
        public static Week of(final int calendarWeekIntValue) {
            if (calendarWeekIntValue > ENUMS.length || calendarWeekIntValue < 1) {
                return null;
            }
            return ENUMS[calendarWeekIntValue - 1];
        }

        /**
         * 解析别名为Week对象，别名如：sun或者SUNDAY，不区分大小写
         * 参考：https://github.com/sisyphsu/dateparser/blob/master/src/main/java/com/github/sisyphsu/dateparser/DateParser.java#L319
         *
         * @param name 别名值
         * @return 周枚举Week，非空
         * @throws IllegalArgumentException 如果别名无对应的枚举，抛出此异常
         */
        public static Week of(final String name) throws IllegalArgumentException {
            if (null != name && name.length() > 1) {
                switch (Character.toLowerCase(name.charAt(0))) {
                    case 'm':
                        return MONDAY; // monday
                    case 'w':
                        return WEDNESDAY; // wednesday
                    case 'f':
                        return FRIDAY; // friday
                    case 't':
                        switch (Character.toLowerCase(name.charAt(1))) {
                            case 'u':
                                return TUESDAY; // tuesday
                            case 'h':
                                return THURSDAY; // thursday
                        }
                        break;
                    case 's':
                        switch (Character.toLowerCase(name.charAt(1))) {
                            case 'a':
                                return SATURDAY; // saturday
                            case 'u':
                                return SUNDAY; // sunday
                        }
                        break;
                }
            }

            throw new IllegalArgumentException("Invalid Week name: " + name);
        }

        /**
         * 将 {@link DayOfWeek}星期相关值转换为Week枚举对象
         *
         * @param dayOfWeek DayOfWeek星期值
         * @return Week
         * @see #SUNDAY
         * @see #MONDAY
         * @see #TUESDAY
         * @see #WEDNESDAY
         * @see #THURSDAY
         * @see #FRIDAY
         * @see #SATURDAY
         */
        public static Week of(final DayOfWeek dayOfWeek) {
            Assert.notNull(dayOfWeek);
            int week = dayOfWeek.getValue() + 1;
            if (8 == week) {
                // 周日
                week = 1;
            }
            return of(week);
        }

        /**
         * 获得星期对应{@link Calendar} 中的Week值
         *
         * @return 星期对应 {@link Calendar} 中的Week值
         */
        public int getValue() {
            return this.value;
        }

        /**
         * 获取ISO8601规范的int值，from 1 (Monday) to 7 (Sunday).
         *
         * @return ISO8601规范的int值
         */
        public int getIso8601Value() {
            int iso8601IntValue = getValue() - 1;
            if (0 == iso8601IntValue) {
                iso8601IntValue = 7;
            }
            return iso8601IntValue;
        }

        /**
         * 转换为中文名
         *
         * @return 星期的中文名
         */
        public String toChinese() {
            return toChinese("星期");
        }

        /**
         * 转换为中文名
         *
         * @param weekNamePre 表示星期的前缀，例如前缀为“星期”，则返回结果为“星期一”；前缀为”周“，结果为“周一”
         * @return 星期的中文名
         */
        public String toChinese(final String weekNamePre) {
            switch (this) {
                case SUNDAY:
                    return weekNamePre + "日";
                case MONDAY:
                    return weekNamePre + "一";
                case TUESDAY:
                    return weekNamePre + "二";
                case WEDNESDAY:
                    return weekNamePre + "三";
                case THURSDAY:
                    return weekNamePre + "四";
                case FRIDAY:
                    return weekNamePre + "五";
                case SATURDAY:
                    return weekNamePre + "六";
                default:
                    return null;
            }
        }

        /**
         * 转换为{@link DayOfWeek}
         *
         * @return {@link DayOfWeek}
         */
        public DayOfWeek toJdkDayOfWeek() {
            return DayOfWeek.of(getIso8601Value());
        }
    }

    /**
     * 季度枚举
     *
     * @see #Q1
     * @see #Q2
     * @see #Q3
     * @see #Q4
     */
    public enum Quarter {

        /**
         * 第一季度
         */
        Q1(1),
        /**
         * 第二季度
         */
        Q2(2),
        /**
         * 第三季度
         */
        Q3(3),
        /**
         * 第四季度
         */
        Q4(4);

        private final int value;

        Quarter(final int value) {
            this.value = value;
        }

        /**
         * 将 季度int转换为Season枚举对象
         *
         * @param intValue 季度int表示
         * @return {@code Quarter}
         * @see #Q1
         * @see #Q2
         * @see #Q3
         * @see #Q4
         */
        public static Quarter of(final int intValue) {
            switch (intValue) {
                case 1:
                    return Q1;
                case 2:
                    return Q2;
                case 3:
                    return Q3;
                case 4:
                    return Q4;
                default:
                    return null;
            }
        }

        /**
         * 获取季度值
         *
         * @return 季度值
         */
        public int getValue() {
            return this.value;
        }
    }

    /**
     * 月份枚举
     * 与Calendar中的月份int值对应
     *
     * @see Calendar#JANUARY
     * @see Calendar#FEBRUARY
     * @see Calendar#MARCH
     * @see Calendar#APRIL
     * @see Calendar#MAY
     * @see Calendar#JUNE
     * @see Calendar#JULY
     * @see Calendar#AUGUST
     * @see Calendar#SEPTEMBER
     * @see Calendar#OCTOBER
     * @see Calendar#NOVEMBER
     * @see Calendar#DECEMBER
     * @see Calendar#UNDECIMBER
     */
    public enum Month {
        /**
         * 一月
         */
        JANUARY(Calendar.JANUARY),
        /**
         * 二月
         */
        FEBRUARY(Calendar.FEBRUARY),
        /**
         * 三月
         */
        MARCH(Calendar.MARCH),
        /**
         * 四月
         */
        APRIL(Calendar.APRIL),
        /**
         * 五月
         */
        MAY(Calendar.MAY),
        /**
         * 六月
         */
        JUNE(Calendar.JUNE),
        /**
         * 七月
         */
        JULY(Calendar.JULY),
        /**
         * 八月
         */
        AUGUST(Calendar.AUGUST),
        /**
         * 九月
         */
        SEPTEMBER(Calendar.SEPTEMBER),
        /**
         * 十月
         */
        OCTOBER(Calendar.OCTOBER),
        /**
         * 十一月
         */
        NOVEMBER(Calendar.NOVEMBER),
        /**
         * 十二月
         */
        DECEMBER(Calendar.DECEMBER),
        /**
         * 十三月，仅用于农历
         */
        UNDECIMBER(Calendar.UNDECIMBER);

        private static final Month[] ENUMS = Month.values();

        /**
         * 对应值，见{@link Calendar}
         */
        private final int value;

        /**
         * 构造
         *
         * @param value 对应值，见{@link Calendar}
         */
        Month(final int value) {
            this.value = value;
        }

        /**
         * 将 {@link Calendar}月份相关值转换为Month枚举对象
         * 未找到返回{@code null}
         *
         * @param calendarMonthIntValue Calendar中关于Month的int值，从0开始
         * @return Month
         * @see Calendar#JANUARY
         * @see Calendar#FEBRUARY
         * @see Calendar#MARCH
         * @see Calendar#APRIL
         * @see Calendar#MAY
         * @see Calendar#JUNE
         * @see Calendar#JULY
         * @see Calendar#AUGUST
         * @see Calendar#SEPTEMBER
         * @see Calendar#OCTOBER
         * @see Calendar#NOVEMBER
         * @see Calendar#DECEMBER
         * @see Calendar#UNDECIMBER
         */
        public static Month of(final int calendarMonthIntValue) {
            if (calendarMonthIntValue >= ENUMS.length || calendarMonthIntValue < 0) {
                return null;
            }
            return ENUMS[calendarMonthIntValue];
        }

        /**
         * 解析别名为Month对象，别名如：jan或者JANUARY，不区分大小写
         *
         * @param name 别名值
         * @return 月份枚举Month，非空
         * @throws IllegalArgumentException 如果别名无对应的枚举，抛出此异常
         */
        public static Month of(final String name) throws IllegalArgumentException {
            if (null != name && name.length() > 2) {
                switch (Character.toLowerCase(name.charAt(0))) {
                    case 'a':
                        switch (Character.toLowerCase(name.charAt(1))) {
                            case 'p':
                                return APRIL; // april
                            case 'u':
                                return AUGUST; // august
                        }
                        break;
                    case 'j':
                        if (Character.toLowerCase(name.charAt(1)) == 'a') {
                            return JANUARY; // january
                        }
                        switch (Character.toLowerCase(name.charAt(2))) {
                            case 'n':
                                return JUNE; // june
                            case 'l':
                                return JULY; // july
                        }
                        break;
                    case 'f':
                        return FEBRUARY; // february
                    case 'm':
                        switch (Character.toLowerCase(name.charAt(2))) {
                            case 'r':
                                return MARCH; // march
                            case 'y':
                                return MAY; // may
                        }
                        break;
                    case 's':
                        return SEPTEMBER; // september
                    case 'o':
                        return OCTOBER; // october
                    case 'n':
                        return NOVEMBER; // november
                    case 'd':
                        return DECEMBER; // december
                }
            }

            throw new IllegalArgumentException("Invalid Month name: " + name);
        }

        /**
         * {@link java.time.Month}转换为Month对象
         *
         * @param month {@link java.time.Month}
         * @return Month
         */
        public static Month of(final java.time.Month month) {
            return of(month.ordinal());
        }

        /**
         * 获得指定月的最后一天
         *
         * @param month      月份，从0开始
         * @param isLeapYear 是否为闰年，闰年只对二月有影响
         * @return 最后一天，可能为28,29,30,31
         */
        public static int getLastDay(final int month, final boolean isLeapYear) {
            final Month of = of(month);
            Assert.notNull(of, "Invalid Month base 0: " + month);
            return of.getLastDay(isLeapYear);
        }

        /**
         * 获取{@link Calendar}中的对应值
         * 此值从0开始，即0表示一月
         *
         * @return {@link Calendar}中的对应月份值，从0开始计数
         */
        public int getValue() {
            return this.value;
        }

        /**
         * 获取月份值，此值与{@link java.time.Month}对应
         * 此值从1开始，即1表示一月
         *
         * @return 月份值，对应{@link java.time.Month}，从1开始计数
         */
        public int getValueBaseOne() {
            Assert.isFalse(this == UNDECIMBER, "Unsupported UNDECIMBER Field");
            return getValue() + 1;
        }

        /**
         * 获取此月份最后一天的值
         * 不支持 {@link #UNDECIMBER}
         *
         * @param isLeapYear 是否闰年
         * @return 此月份最后一天的值
         */
        public int getLastDay(final boolean isLeapYear) {
            switch (this) {
                case FEBRUARY:
                    return isLeapYear ? 29 : 28;
                case APRIL:
                case JUNE:
                case SEPTEMBER:
                case NOVEMBER:
                    return 30;
                default:
                    return 31;
            }
        }

        /**
         * 转换为{@link java.time.Month}
         *
         * @return {@link java.time.Month}
         */
        public java.time.Month toJdkMonth() {
            return java.time.Month.of(getValueBaseOne());
        }

        /**
         * 获取显示名称
         *
         * @param style 名称风格
         * @return 显示名称
         */
        public String getDisplayName(final TextStyle style) {
            return getDisplayName(style, Locale.getDefault());
        }

        /**
         * 获取显示名称
         *
         * @param style  名称风格
         * @param locale {@link Locale}
         * @return 显示名称
         */
        public String getDisplayName(final TextStyle style, final Locale locale) {
            return toJdkMonth().getDisplayName(style, locale);
        }
    }

    /**
     * 日期时间单位，每个单位都是以毫秒为基数
     */
    public enum Units {
        /**
         * 一毫秒
         */
        MS(1),
        /**
         * 一秒的毫秒数
         */
        SECOND(1000),
        /**
         * 一分钟的毫秒数
         */
        MINUTE(SECOND.getMillis() * 60),
        /**
         * 一小时的毫秒数
         */
        HOUR(MINUTE.getMillis() * 60),
        /**
         * 一天的毫秒数
         */
        DAY(HOUR.getMillis() * 24),
        /**
         * 一周的毫秒数
         */
        WEEK(DAY.getMillis() * 7);

        private final long millis;

        Units(final long millis) {
            this.millis = millis;
        }

        /**
         * 单位兼容转换，将{@link ChronoUnit}转换为对应的DateUnit
         *
         * @param unit {@link ChronoUnit}
         * @return Units，null表示不支持此单位
         */
        public static Units of(final ChronoUnit unit) {
            switch (unit) {
                case MICROS:
                    return Units.MS;
                case SECONDS:
                    return Units.SECOND;
                case MINUTES:
                    return Units.MINUTE;
                case HOURS:
                    return Units.HOUR;
                case DAYS:
                    return Units.DAY;
                case WEEKS:
                    return Units.WEEK;
            }
            return null;
        }

        /**
         * 单位兼容转换，将DateUnit转换为对应的{@link ChronoUnit}
         *
         * @param unit Units
         * @return {@link ChronoUnit}
         */
        public static ChronoUnit toChronoUnit(final Units unit) {
            switch (unit) {
                case MS:
                    return ChronoUnit.MICROS;
                case SECOND:
                    return ChronoUnit.SECONDS;
                case MINUTE:
                    return ChronoUnit.MINUTES;
                case HOUR:
                    return ChronoUnit.HOURS;
                case DAY:
                    return ChronoUnit.DAYS;
                case WEEK:
                    return ChronoUnit.WEEKS;
            }
            return null;
        }

        /**
         * @return 单位对应的毫秒数
         */
        public long getMillis() {
            return this.millis;
        }

        /**
         * 单位兼容转换，将DateUnit转换为对应的{@link ChronoUnit}
         *
         * @return {@link ChronoUnit}
         */
        public ChronoUnit toChronoUnit() {
            return Units.toChronoUnit(this);
        }
    }

    /**
     * 修改类型
     */
    public enum Modify {
        /**
         * 取指定日期短的起始值.
         */
        TRUNCATE,

        /**
         * 指定日期属性按照四舍五入处理
         */
        ROUND,

        /**
         * 指定日期属性按照进一法处理
         */
        CEILING
    }

    /**
     * 日期各个部分的枚举
     * 与Calendar相应值对应
     */
    public enum Type {

        /**
         * 世纪
         *
         * @see Calendar#ERA
         */
        ERA(Calendar.ERA),
        /**
         * 年
         *
         * @see Calendar#YEAR
         */
        YEAR(Calendar.YEAR),
        /**
         * 月
         *
         * @see Calendar#MONTH
         */
        MONTH(Calendar.MONTH),
        /**
         * 一年中第几周
         *
         * @see Calendar#WEEK_OF_YEAR
         */
        WEEK_OF_YEAR(Calendar.WEEK_OF_YEAR),
        /**
         * 一月中第几周
         *
         * @see Calendar#WEEK_OF_MONTH
         */
        WEEK_OF_MONTH(Calendar.WEEK_OF_MONTH),
        /**
         * 一月中的第几天
         *
         * @see Calendar#DAY_OF_MONTH
         */
        DAY_OF_MONTH(Calendar.DAY_OF_MONTH),
        /**
         * 一年中的第几天
         *
         * @see Calendar#DAY_OF_YEAR
         */
        DAY_OF_YEAR(Calendar.DAY_OF_YEAR),
        /**
         * 周几，1表示周日，2表示周一
         *
         * @see Calendar#DAY_OF_WEEK
         */
        DAY_OF_WEEK(Calendar.DAY_OF_WEEK),
        /**
         * 天所在的周是这个月的第几周
         *
         * @see Calendar#DAY_OF_WEEK_IN_MONTH
         */
        DAY_OF_WEEK_IN_MONTH(Calendar.DAY_OF_WEEK_IN_MONTH),
        /**
         * 上午或者下午
         *
         * @see Calendar#AM_PM
         */
        AM_PM(Calendar.AM_PM),
        /**
         * 小时，用于12小时制
         *
         * @see Calendar#HOUR
         */
        HOUR(Calendar.HOUR),
        /**
         * 小时，用于24小时制
         *
         * @see Calendar#HOUR
         */
        HOUR_OF_DAY(Calendar.HOUR_OF_DAY),
        /**
         * 分钟
         *
         * @see Calendar#MINUTE
         */
        MINUTE(Calendar.MINUTE),
        /**
         * 秒
         *
         * @see Calendar#SECOND
         */
        SECOND(Calendar.SECOND),
        /**
         * 毫秒
         *
         * @see Calendar#MILLISECOND
         */
        MILLISECOND(Calendar.MILLISECOND);

        private final int value;

        Type(final int value) {
            this.value = value;
        }

        /**
         * 将 {@link Calendar}相关值转换为DatePart枚举对象
         *
         * @param calendarPartIntValue Calendar中关于Week的int值
         * @return Type
         */
        public static Type of(final int calendarPartIntValue) {
            switch (calendarPartIntValue) {
                case Calendar.ERA:
                    return ERA;
                case Calendar.YEAR:
                    return YEAR;
                case Calendar.MONTH:
                    return MONTH;
                case Calendar.WEEK_OF_YEAR:
                    return WEEK_OF_YEAR;
                case Calendar.WEEK_OF_MONTH:
                    return WEEK_OF_MONTH;
                case Calendar.DAY_OF_MONTH:
                    return DAY_OF_MONTH;
                case Calendar.DAY_OF_YEAR:
                    return DAY_OF_YEAR;
                case Calendar.DAY_OF_WEEK:
                    return DAY_OF_WEEK;
                case Calendar.DAY_OF_WEEK_IN_MONTH:
                    return DAY_OF_WEEK_IN_MONTH;
                case Calendar.AM_PM:
                    return AM_PM;
                case Calendar.HOUR:
                    return HOUR;
                case Calendar.HOUR_OF_DAY:
                    return HOUR_OF_DAY;
                case Calendar.MINUTE:
                    return MINUTE;
                case Calendar.SECOND:
                    return SECOND;
                case Calendar.MILLISECOND:
                    return MILLISECOND;
                default:
                    return null;
            }
        }

        /**
         * 获取{@link Calendar}中对应的值
         *
         * @return {@link Calendar}中对应的值
         */
        public int getValue() {
            return this.value;
        }

    }

}
