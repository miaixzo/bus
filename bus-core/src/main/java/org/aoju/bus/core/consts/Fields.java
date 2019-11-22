/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.core.consts;

import org.aoju.bus.core.date.format.FastDateFormat;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * 日期场景属性
 *
 * @author Kimi Liu
 * @version 5.2.3
 * @since JDK 1.8+
 */
public class Fields {

    /**
     * 格式化通配符: yyyy
     */
    public final static String NORM_YEAR_PATTERN = "yyyy";
    /**
     * 格式化通配符: {@link FastDateFormat} yyyy
     */
    public final static FastDateFormat NORM_YEAR_FORMAT = FastDateFormat.getInstance(NORM_YEAR_PATTERN);

    /**
     * 格式化通配符: yyyy-MM
     */
    public final static String NORM_YEAR_MTOTH_PATTERN = "yyyy-MM";
    /**
     * 格式化通配符: {@link FastDateFormat}yyyy-MM
     */
    public final static FastDateFormat NORM_YEAR_MTOTH_FORMAT = FastDateFormat.getInstance(NORM_YEAR_MTOTH_PATTERN);

    /**
     * 格式化通配符: yyyy-MM-dd
     */
    public final static String NORM_DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 格式化通配符: {@link FastDateFormat} yyyy-MM-dd
     */
    public final static FastDateFormat NORM_DATE_FORMAT = FastDateFormat.getInstance(NORM_DATE_PATTERN);

    /**
     * 格式化通配符: yyyyMMdd
     */
    public final static String PURE_DATE_PATTERN = "yyyyMMdd";
    /**
     * 格式化通配符: {@link FastDateFormat} yyyyMMdd
     */
    public final static FastDateFormat PURE_DATE_FORMAT = FastDateFormat.getInstance(PURE_DATE_PATTERN);

    /**
     * 格式化通配符: yyyy-MM-dd HH:mm
     */
    public final static String NORM_DATETIME_MINUTE_PATTERN = "yyyy-MM-dd HH:mm";
    /**
     * 格式化通配符: {@link FastDateFormat} yyyy-MM-dd HH:mm
     */
    public final static FastDateFormat NORM_DATETIME_MINUTE_FORMAT = FastDateFormat.getInstance(NORM_DATETIME_MINUTE_PATTERN);

    /**
     * 格式化通配符: yyyy-MM-dd HH:mm:ss
     */
    public final static String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 格式化通配符: {@link FastDateFormat} yyyy-MM-dd HH:mm:ss
     */
    public final static FastDateFormat NORM_DATETIME_FORMAT = FastDateFormat.getInstance(NORM_DATETIME_PATTERN);

    /**
     * 格式化通配符: :yyyy-MM-dd HH:mm:ss.SSS
     */
    public final static String NORM_DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    /**
     * 格式化通配符: {@link FastDateFormat} yyyy-MM-dd HH:mm:ss.SSS
     */
    public final static FastDateFormat NORM_DATETIME_MS_FORMAT = FastDateFormat.getInstance(NORM_DATETIME_MS_PATTERN);

    /**
     * 格式化通配符: yyyyMMddHHmmss
     */
    public final static String PURE_DATETIME_PATTERN = "yyyyMMddHHmmss";
    /**
     * 格式化通配符: {@link FastDateFormat} yyyyMMddHHmmss
     */
    public final static FastDateFormat PURE_DATETIME_FORMAT = FastDateFormat.getInstance(PURE_DATETIME_PATTERN);

    /**
     * 格式化通配符: yyyyMMddHHmmssSSS
     */
    public final static String PURE_DATETIME_MS_PATTERN = "yyyyMMddHHmmssSSS";
    /**
     * 格式化通配符: {@link FastDateFormat} yyyyMMddHHmmssSSS
     */
    public final static FastDateFormat PURE_DATETIME_MS_FORMAT = FastDateFormat.getInstance(PURE_DATETIME_MS_PATTERN);

    /**
     * 标准时间格式: HH:mm:ss
     */
    public final static String NORM_TIME_PATTERN = "HH:mm:ss";
    /**
     * 标准时间格式:{@link FastDateFormat}HH:mm:ss
     */
    public final static FastDateFormat NORM_TIME_FORMAT = FastDateFormat.getInstance(NORM_TIME_PATTERN);

    /**
     * 格式化通配符: HHmmss
     */
    public final static String PURE_TIME_PATTERN = "HHmmss";
    /**
     * 格式化通配符: {@link FastDateFormat} HHmmss
     */
    public final static FastDateFormat PURE_TIME_FORMAT = FastDateFormat.getInstance(PURE_TIME_PATTERN);

    /**
     * 格式化通配符: HHmmssSSS
     */
    public final static String PURE_TIME_MS_PATTERN = "HHmmssSSS";
    /**
     * 格式化通配符: {@link FastDateFormat} HHmmssSSS
     */
    public final static FastDateFormat PURE_TIME_MS_FORMAT = FastDateFormat.getInstance(PURE_TIME_MS_PATTERN);

    /**
     * 格式化通配符: HH:mm
     */
    public static final String HOUR_MINUTE_PATTERN = "HH:mm";
    /**
     * 格式化通配符: {@link FastDateFormat}  HH:mm
     */
    public final static FastDateFormat HOUR_MINUTE_FORMAT = FastDateFormat.getInstance(HOUR_MINUTE_PATTERN);

    /**
     * 格式化通配符: HHmm
     */
    public static final String SHORT_HOUR_MINUTE_PATTERN = "HHmm";
    /**
     * 格式化通配符: {@link FastDateFormat} HHmm
     */
    public final static FastDateFormat SHORT_HOUR_MINUTE_FORMAT = FastDateFormat.getInstance(SHORT_HOUR_MINUTE_PATTERN);

    /**
     * 格式化通配符: mm:ss
     */
    public static final String MINUTE_SECOND_PATTERN = "mm:ss";
    /**
     * 格式化通配符: {@link FastDateFormat} mm:ss
     */
    public final static FastDateFormat MINUTE_SECOND_FORMAT = FastDateFormat.getInstance(MINUTE_SECOND_PATTERN);

    /**
     * 格式化通配符: mmss
     */
    public static final String SHORT_MINUTE_SECOND_PATTERN = "mmss";
    /**
     * 格式化通配符: {@link FastDateFormat} mmss
     */
    public final static FastDateFormat SHORT_MINUTE_SECOND_FORMAT = FastDateFormat.getInstance(SHORT_MINUTE_SECOND_PATTERN);

    /**
     * HTTP头日期时间格式: EEE, dd MMM yyyy HH:mm:ss z
     */
    public final static String HTTP_DATETIME_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    /**
     * HTTP头日期时间格式: {@link FastDateFormat} EEE, dd MMM yyyy HH:mm:ss z
     */
    public final static FastDateFormat HTTP_DATETIME_FORMAT = FastDateFormat.getInstance(HTTP_DATETIME_PATTERN);

    /**
     * JDK日期时间格式: EEE MMM dd HH:mm:ss zzz yyyy
     */
    public final static String JDK_DATETIME_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";
    /**
     * JDK日期时间格式: {@link FastDateFormat} EEE MMM dd HH:mm:ss zzz yyyy
     */
    public final static FastDateFormat JDK_DATETIME_FORMAT = FastDateFormat.getInstance(JDK_DATETIME_PATTERN);

    /**
     * 中文日期格式: yyyy年M月d日
     */
    public final static String NORM_DATE_CN_PATTERN = "yyyy年M月d日";
    /**
     * 中文日期格式: {@link FastDateFormat} yyyy年M月d日
     */
    public final static FastDateFormat NORM_DATE_CN_FORMAT = FastDateFormat.getInstance(NORM_DATE_CN_PATTERN);

    /**
     * 中文日期格式: M月d日
     */
    public final static String ORM_MONTH_CN_PATTERN = "M月d日";
    /**
     * 中文日期格式: {@link FastDateFormat} M月d日
     */
    public final static FastDateFormat NORM_MONTH_CN_FORMAT = FastDateFormat.getInstance(ORM_MONTH_CN_PATTERN);

    /**
     * UTC时间: yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public final static String UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    /**
     * UTC时间: {@link FastDateFormat} yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public final static FastDateFormat UTC_FORMAT = FastDateFormat.getInstance(UTC_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间: yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static final String MSEC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    /**
     * UTC时间: {@link FastDateFormat} yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public final static FastDateFormat MSEC_FORMAT = FastDateFormat.getInstance(MSEC_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间: yyyy-MM-dd HH:mm:ss Z
     */
    public static final String SPACEY_PATTERN = "yyyy-MM-dd HH:mm:ss Z";
    /**
     * UTC时间: {@link FastDateFormat} yyyy-MM-dd HH:mm:ss Z
     */
    public final static FastDateFormat SPACEY_FORMAT = FastDateFormat.getInstance(SPACEY_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间: yyyy-MM-dd HH:mm:ss.SSS Z
     */
    public static final String SPACEY_MSEC_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS Z";
    /**
     * UTC时间: {@link FastDateFormat} yyyy-MM-dd HH:mm:ss.SSS Z
     */
    public final static FastDateFormat SPACEY_MSEC_FORMAT = FastDateFormat.getInstance(SPACEY_MSEC_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static final String OUTPUT_MSEC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    /**
     * UTC时间: {@link FastDateFormat} yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public final static FastDateFormat OUTPUT_MSEC_FORMAT = FastDateFormat.getInstance(OUTPUT_MSEC_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * 星座
     */
    public static final String[] ZODIAC = new String[]{"摩羯座", "水瓶座", "双鱼座", "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座"};
    /**
     * 属相
     */
    public static final String[] CHINESE_ZODIAC = new String[]{"鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"};
    /**
     * 星座分隔时间日
     */
    public static final int[] ZODIAC_SLICED = new int[]{20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};

    /**
     * 日期各个部分的枚举
     * 与Calendar相应值对应
     *
     * @author Kimi Liu
     * @version 5.2.3
     * @since JDK 1.8+
     */
    public enum DateField {

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
         * 周几,1表示周日,2表示周一
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
         * 小时,用于12小时制
         *
         * @see Calendar#HOUR
         */
        HOUR(Calendar.HOUR),
        /**
         * 小时,用于24小时制
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

        private int value;

        DateField(int value) {
            this.value = value;
        }

        /**
         * 将 {@link Calendar}相关值转换为DatePart枚举对象
         *
         * @param calendarPartIntValue Calendar中关于Week的int值
         * @return {@link DateField}
         */
        public static DateField of(int calendarPartIntValue) {
            switch (calendarPartIntValue) {
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

        public int getValue() {
            return this.value;
        }

    }

    /**
     * 日期时间单位,每个单位都是以毫秒为基数
     *
     * @author Kimi Liu
     * @version 5.2.3
     * @since JDK 1.8+
     */
    public enum Unit {

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

        private long millis;

        Unit(long millis) {
            this.millis = millis;
        }

        /**
         * @return 单位对应的毫秒数
         */
        public long getMillis() {
            return this.millis;
        }
    }

    /**
     * 月份枚举
     * 与Calendar中的月份int值对应
     *
     * @author Kimi Liu
     * @version 5.2.3
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
     * @since JDK 1.8+
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
         * 十三月,仅用于农历
         */
        UNDECIMBER(Calendar.UNDECIMBER);

        private int value;

        Month(int value) {
            this.value = value;
        }

        /**
         * 将 {@link Calendar}月份相关值转换为Month枚举对象
         *
         * @param calendarMonthIntValue Calendar中关于Month的int值
         * @return {@link Month}
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
        public static Month of(int calendarMonthIntValue) {
            switch (calendarMonthIntValue) {
                case Calendar.JANUARY:
                    return JANUARY;
                case Calendar.FEBRUARY:
                    return FEBRUARY;
                case Calendar.MARCH:
                    return MARCH;
                case Calendar.APRIL:
                    return APRIL;
                case Calendar.MAY:
                    return MAY;
                case Calendar.JUNE:
                    return JUNE;
                case Calendar.JULY:
                    return JULY;
                case Calendar.AUGUST:
                    return AUGUST;
                case Calendar.SEPTEMBER:
                    return SEPTEMBER;
                case Calendar.OCTOBER:
                    return OCTOBER;
                case Calendar.NOVEMBER:
                    return NOVEMBER;
                case Calendar.DECEMBER:
                    return DECEMBER;
                case Calendar.UNDECIMBER:
                    return UNDECIMBER;
                default:
                    return null;
            }
        }

        public int getValue() {
            return this.value;
        }
    }

    /**
     * 季度枚举
     *
     * @author Kimi Liu
     * @version 5.2.3
     * @since JDK 1.8+
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

        private int value;

        Quarter(int value) {
            this.value = value;
        }

        /**
         * 将 季度int转换为Season枚举对象
         *
         * @param intValue 季度int表示
         * @return {@link Quarter}
         * @see #Q1
         * @see #Q2
         * @see #Q3
         * @see #Q4
         */
        public static Quarter of(int intValue) {
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

        public int getValue() {
            return this.value;
        }
    }

    /**
     * 星期枚举
     * 与Calendar中的星期int值对应
     *
     * @author Kimi Liu
     * @version 5.2.3
     * @see #SUNDAY
     * @see #MONDAY
     * @see #TUESDAY
     * @see #WEDNESDAY
     * @see #THURSDAY
     * @see #FRIDAY
     * @see #SATURDAY
     * @since JDK 1.8+
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

        /**
         * 星期对应{@link Calendar} 中的Week值
         */
        private int value;

        /**
         * 构造
         *
         * @param value 星期对应{@link Calendar} 中的Week值
         */
        Week(int value) {
            this.value = value;
        }

        /**
         * 将 {@link Calendar}星期相关值转换为Week枚举对象
         *
         * @param calendarWeekIntValue Calendar中关于Week的int值
         * @return {@link Week}
         * @see #SUNDAY
         * @see #MONDAY
         * @see #TUESDAY
         * @see #WEDNESDAY
         * @see #THURSDAY
         * @see #FRIDAY
         * @see #SATURDAY
         */
        public static Week of(int calendarWeekIntValue) {
            switch (calendarWeekIntValue) {
                case Calendar.SUNDAY:
                    return SUNDAY;
                case Calendar.MONDAY:
                    return MONDAY;
                case Calendar.TUESDAY:
                    return TUESDAY;
                case Calendar.WEDNESDAY:
                    return WEDNESDAY;
                case Calendar.THURSDAY:
                    return THURSDAY;
                case Calendar.FRIDAY:
                    return FRIDAY;
                case Calendar.SATURDAY:
                    return SATURDAY;
                default:
                    return null;
            }
        }

        /**
         * 获得星期对应{@link Calendar} 中的Week值
         *
         * @return 星期对应{@link Calendar} 中的Week值
         */
        public int getValue() {
            return this.value;
        }

        /**
         * 转换为中文名
         *
         * @return 星期的中文名
         * @since 3.3.0
         */
        public String toChinese() {
            return toChinese("星期");
        }

        /**
         * 转换为中文名
         *
         * @param weekNamePre 表示星期的前缀,例如前缀为“星期”,则返回结果为“星期一”；前缀为”周“,结果为“周一”
         * @return 星期的中文名
         */
        public String toChinese(String weekNamePre) {
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

    }

    /**
     * 格式化等级枚举
     */
    public enum Level {

        /**
         * 天
         */
        DAY("天"),
        /**
         * 小时
         */
        HOUR("小时"),
        /**
         * 分钟
         */
        MINUTE("分"),
        /**
         * 秒
         */
        SECOND("秒"),
        /**
         * 毫秒
         */
        MILLSECOND("毫秒");

        /**
         * 级别名称
         */
        public String name;

        /**
         * 构造
         *
         * @param name 级别名称
         */
        Level(String name) {
            this.name = name;
        }

        /**
         * 获取级别名称
         *
         * @return 级别名称
         */
        public String getName() {
            return this.name;
        }
    }

}
