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

import org.miaixz.bus.core.date.Converter;
import org.miaixz.bus.core.date.formatter.FormatBuilder;

import java.time.LocalTime;
import java.time.MonthDay;
import java.util.Locale;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 日期场景属性
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Fields {

    /**
     * 格式化通配符: yyyy
     */
    public static final String NORM_YEAR_PATTERN = "yyyy";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyy
     */
    public static final FormatBuilder NORM_YEAR_FORMAT = FormatBuilder.getInstance(NORM_YEAR_PATTERN);

    /**
     * 格式化通配符: yyyy-MM
     */
    public static final String NORM_YEAR_MTOTH_PATTERN = "yyyy-MM";
    /**
     * 格式化通配符: {@link FormatBuilder}yyyy-MM
     */
    public static final FormatBuilder NORM_YEAR_MTOTH_FORMAT = FormatBuilder.getInstance(NORM_YEAR_MTOTH_PATTERN);

    /**
     * 格式化通配符: yyyy-MM-dd
     */
    public static final String NORM_DATE_PATTERN = "yyyy-MM-dd";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyy-MM-dd
     */
    public static final FormatBuilder NORM_DATE_FORMAT = FormatBuilder.getInstance(NORM_DATE_PATTERN);

    /**
     * 格式化通配符: yyyyMMdd
     */
    public static final String PURE_DATE_PATTERN = "yyyyMMdd";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyyMMdd
     */
    public static final FormatBuilder PURE_DATE_FORMAT = FormatBuilder.getInstance(PURE_DATE_PATTERN);

    /**
     * 格式化通配符: yyyy-MM-dd HH:mm
     */
    public static final String NORM_DATETIME_MINUTE_PATTERN = "yyyy-MM-dd HH:mm";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyy-MM-dd HH:mm
     */
    public static final FormatBuilder NORM_DATETIME_MINUTE_FORMAT = FormatBuilder.getInstance(NORM_DATETIME_MINUTE_PATTERN);

    /**
     * 格式化通配符: yyyy-MM-dd HH:mm:ss
     */
    public static final String NORM_DATETIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyy-MM-dd HH:mm:ss
     */
    public static final FormatBuilder NORM_DATETIME_FORMAT = FormatBuilder.getInstance(NORM_DATETIME_PATTERN);

    /**
     * 格式化通配符: yyyy-MM-dd hh:mm:ss
     */
    public static final String NORM_PART_DATETIME_PATTERN = "yyyy-MM-dd hh:mm:ss";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyy-MM-dd hh:mm:ss
     */
    public static final FormatBuilder NORM_PART_DATETIME_FORMAT = FormatBuilder.getInstance(NORM_PART_DATETIME_PATTERN);

    /**
     * 格式化通配符: yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final String NORM_DATETIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyy-MM-dd HH:mm:ss.SSS
     */
    public static final FormatBuilder NORM_DATETIME_MS_FORMAT = FormatBuilder.getInstance(NORM_DATETIME_MS_PATTERN);

    /**
     * 格式化通配符: yyyy-MM-dd HH:mm:ss,SSS
     */
    public static final String NORM_DATETIME_ISO8601_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";
    /**
     * 格式化通配符: {@link FormatBuilder}：yyyy-MM-dd HH:mm:ss,SSS
     */
    public static final FormatBuilder NORM_DATETIME_ISO8601_FORMAT = FormatBuilder.getInstance(NORM_DATETIME_ISO8601_PATTERN);

    /**
     * 格式化通配符: yyyyMMddHHmmss
     */
    public static final String PURE_DATETIME_PATTERN = "yyyyMMddHHmmss";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyyMMddHHmmss
     */
    public static final FormatBuilder PURE_DATETIME_FORMAT = FormatBuilder.getInstance(PURE_DATETIME_PATTERN);

    /**
     * 格式化通配符: yyyyMMddHHmmssSSS
     */
    public static final String PURE_DATETIME_MS_PATTERN = "yyyyMMddHHmmssSSS";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyyMMddHHmmssSSS
     */
    public static final FormatBuilder PURE_DATETIME_MS_FORMAT = FormatBuilder.getInstance(PURE_DATETIME_MS_PATTERN);

    /**
     * 格式化通配符: yyyyMMddHHmmss.SSS
     */
    public static final String PURE_DATETIME_TIP_PATTERN = "yyyyMMddHHmmss.SSS";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyyMMddHHmmss.SSS
     */
    public static final FormatBuilder PURE_DATETIME_TIP_FORMAT = FormatBuilder.getInstance(PURE_DATETIME_TIP_PATTERN);

    /**
     * 格式化通配符: yyyyMMddHHmmss.SSSSSSZZZZZ
     */
    public static final String PURE_DATETIME_ICE_PATTERN = "yyyyMMddHHmmss.SSSSSSZZZZZ";
    /**
     * 格式化通配符: {@link FormatBuilder} yyyyMMddHHmmss.SSSSSSZZZZZ
     */
    public static final FormatBuilder PURE_DATETIME_ICE_FORMAT = FormatBuilder.getInstance(PURE_DATETIME_ICE_PATTERN);

    /**
     * 标准时间格式: HH:mm:ss
     */
    public static final String NORM_TIME_PATTERN = "HH:mm:ss";
    /**
     * 标准时间格式:{@link FormatBuilder}HH:mm:ss
     */
    public static final FormatBuilder NORM_TIME_FORMAT = FormatBuilder.getInstance(NORM_TIME_PATTERN);

    /**
     * 格式化通配符: yyMMddHHmm
     */
    public static final String PURE_DATE_MINUTE_PATTERN = "yyMMddHHmm";
    /**
     * 格式化通配符: {@link FormatBuilder} yyMMddHHmm
     */
    public static final FormatBuilder PURE_DATE_MINUTE_FORMAT = FormatBuilder.getInstance(PURE_DATE_MINUTE_PATTERN);

    /**
     * 格式化通配符: yyMMddHHmm
     */
    public static final String PURE_DATE_DAY_PATTERN = "MMdd";
    /**
     * 格式化通配符: {@link FormatBuilder} yyMMddHHmm
     */
    public static final FormatBuilder PURE_DATE_DAY_FORMAT = FormatBuilder.getInstance(PURE_DATE_DAY_PATTERN);

    /**
     * 格式化通配符: HHmmss
     */
    public static final String PURE_TIME_PATTERN = "HHmmss";
    /**
     * 格式化通配符: {@link FormatBuilder} HHmmss
     */
    public static final FormatBuilder PURE_TIME_FORMAT = FormatBuilder.getInstance(PURE_TIME_PATTERN);

    /**
     * 格式化通配符: HHmmssSSS
     */
    public static final String PURE_TIME_MS_PATTERN = "HHmmssSSS";
    /**
     * 格式化通配符: {@link FormatBuilder} HHmmssSSS
     */
    public static final FormatBuilder PURE_TIME_MS_FORMAT = FormatBuilder.getInstance(PURE_TIME_MS_PATTERN);

    /**
     * 格式化通配符: HH:mm
     */
    public static final String HOUR_MINUTE_PATTERN = "HH:mm";
    /**
     * 格式化通配符: {@link FormatBuilder}  HH:mm
     */
    public static final FormatBuilder HOUR_MINUTE_FORMAT = FormatBuilder.getInstance(HOUR_MINUTE_PATTERN);

    /**
     * 格式化通配符: HHmm
     */
    public static final String SHORT_HOUR_MINUTE_PATTERN = "HHmm";
    /**
     * 格式化通配符: {@link FormatBuilder} HHmm
     */
    public static final FormatBuilder SHORT_HOUR_MINUTE_FORMAT = FormatBuilder.getInstance(SHORT_HOUR_MINUTE_PATTERN);

    /**
     * 格式化通配符: mm:ss
     */
    public static final String MINUTE_SECOND_PATTERN = "mm:ss";
    /**
     * 格式化通配符: {@link FormatBuilder} mm:ss
     */
    public static final FormatBuilder MINUTE_SECOND_FORMAT = FormatBuilder.getInstance(MINUTE_SECOND_PATTERN);

    /**
     * 格式化通配符: mmss
     */
    public static final String SHORT_MINUTE_SECOND_PATTERN = "mmss";
    /**
     * 格式化通配符: {@link FormatBuilder} mmss
     */
    public static final FormatBuilder SHORT_MINUTE_SECOND_FORMAT = FormatBuilder.getInstance(SHORT_MINUTE_SECOND_PATTERN);

    /**
     * HTTP头日期时间格式: EEE, dd MMM yyyy HH:mm:ss z
     */
    public static final String HTTP_DATETIME_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";
    /**
     * HTTP头日期时间格式: {@link FormatBuilder} EEE, dd MMM yyyy HH:mm:ss z
     */
    public static final FormatBuilder HTTP_DATETIME_FORMAT = FormatBuilder.getInstance(HTTP_DATETIME_PATTERN, TimeZone.getTimeZone("GMT"), Locale.US);

    /**
     * JDK日期时间格式: EEE MMM dd HH:mm:ss zzz yyyy
     */
    public static final String JDK_DATETIME_PATTERN = "EEE MMM dd HH:mm:ss zzz yyyy";
    /**
     * JDK日期时间格式: {@link FormatBuilder} EEE MMM dd HH:mm:ss zzz yyyy
     */
    public static final FormatBuilder JDK_DATETIME_FORMAT = FormatBuilder.getInstance(JDK_DATETIME_PATTERN, Locale.US);

    /**
     * 中文日期格式: M月d日
     */
    public static final String ORM_MONTH_CN_PATTERN = "M月d日";
    /**
     * 中文日期格式: {@link FormatBuilder} M月d日
     */
    public static final FormatBuilder NORM_MONTH_CN_FORMAT = FormatBuilder.getInstance(ORM_MONTH_CN_PATTERN);

    /**
     * 中文日期格式: yyyy年M月d日
     */
    public static final String NORM_DATE_CN_PATTERN = "yyyy年M月d日";
    /**
     * 中文日期格式: {@link FormatBuilder} yyyy年M月d日
     */
    public static final FormatBuilder NORM_DATE_CN_FORMAT = FormatBuilder.getInstance(NORM_DATE_CN_PATTERN);

    /**
     * 标准日期格式：yyyy年MM月dd日 HH时mm分ss秒
     */
    public static final String NORM_CN_DATE_TIME_PATTERN = "yyyy年MM月dd日 HH时mm分ss秒";
    /**
     * 标准日期格式 {@link FormatBuilder}：yyyy年MM月dd日 HH时mm分ss秒
     */
    public static final FormatBuilder NORM_CN_DATE_TIME_FORMAT = FormatBuilder.getInstance(NORM_CN_DATE_TIME_PATTERN);

    /**
     * UTC时间: yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public static final String UTC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    /**
     * UTC时间: {@link FormatBuilder} yyyy-MM-dd'T'HH:mm:ss'Z'
     */
    public static final FormatBuilder UTC_FORMAT = FormatBuilder.getInstance(UTC_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间: yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static final String MSEC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    /**
     * UTC时间: {@link FormatBuilder} yyyy-MM-dd'T'HH:mm:ss.SSSZ
     */
    public static final FormatBuilder MSEC_FORMAT = FormatBuilder.getInstance(MSEC_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间：yyyy-MM-dd'T'HH:mm:ss
     */
    public static final String SIMPLE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    /**
     * UTC时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss
     */
    public static final FormatBuilder SIMPLE_FORMAT = FormatBuilder.getInstance(SIMPLE_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间：yyyy-MM-dd'T'HH:mm:ss.SSS
     */
    public static final String SIMPLE_MS_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    /**
     * UTC时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss.SSS
     */
    public static final FormatBuilder SIMPLE_MS_FORMAT = FormatBuilder.getInstance(SIMPLE_MS_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间: yyyy-MM-dd HH:mm:ss Z
     */
    public static final String SPACEY_PATTERN = "yyyy-MM-dd HH:mm:ss Z";
    /**
     * UTC时间: {@link FormatBuilder} yyyy-MM-dd HH:mm:ss Z
     */
    public static final FormatBuilder SPACEY_FORMAT = FormatBuilder.getInstance(SPACEY_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间: yyyy-MM-dd HH:mm:ss.SSS Z
     */
    public static final String SPACEY_MSEC_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS Z";
    /**
     * UTC时间: {@link FormatBuilder} yyyy-MM-dd HH:mm:ss.SSS Z
     */
    public static final FormatBuilder SPACEY_MSEC_FORMAT = FormatBuilder.getInstance(SPACEY_MSEC_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static final String OUTPUT_MSEC_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    /**
     * UTC时间: {@link FormatBuilder} yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
     */
    public static final FormatBuilder OUTPUT_MSEC_FORMAT = FormatBuilder.getInstance(OUTPUT_MSEC_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * UTC时间：yyyy-MM-dd'T'HH:mm:ssZ
     */
    public final static String WITH_ZONE_OFFSET_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";

    /**
     * UTC时间：yyyy-MM-dd'T'HH:mm:ssXXX
     */
    public static final String WITH_XXX_OFFSET_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";
    /**
     * UTC时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ssXXX
     */
    public static final FormatBuilder WITH_XXX_OFFSET_FORMAT = FormatBuilder.getInstance(WITH_XXX_OFFSET_PATTERN);

    /**
     * UTC时间：yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public static final String MS_WITH_XXX_OFFSET_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
    /**
     * UTC时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss.SSSXXX
     */
    public static final FormatBuilder MS_WITH_XXX_OFFSET_FORMAT = FormatBuilder.getInstance(MS_WITH_XXX_OFFSET_PATTERN);

    /**
     * UTC时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ssZ
     */
    public final static FormatBuilder WITH_ZONE_OFFSET_FORMAT = FormatBuilder.getInstance(WITH_ZONE_OFFSET_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * 格式：秒时间戳（Unix时间戳）
     */
    public static final String NORM_FORMAT_SECONDS = "#sss";
    /**
     * 格式：毫秒时间戳
     */
    public static final String NORM_FORMAT_MILLISECONDS = "#SSS";

    /**
     * 标准日期时间正则，每个字段支持单个数字或2个数字
     * <pre>
     *     yyyy-MM-dd HH:mm:ss.SSS
     *     yyyy-MM-dd HH:mm:ss
     *     yyyy-MM-dd HH:mm
     *     yyyy-MM-dd
     * </pre>
     */
    public static final Pattern REGEX_NORM = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}(\\s\\d{1,2}:\\d{1,2}(:\\d{1,2})?)?(.\\d{1,6})?");

    /**
     * java.util.Date EEE MMM zzz 缩写数组
     */
    public final static String[] WTB = {
            /**
             * 星期
            */
            "sun", "mon", "tue", "wed", "thu", "fri", "sat",
            /**
             * 月份
            */
            "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec",
            /**
             * 时区
            */
            "gmt", "ut", "utc", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt"
    };

    /**
     * 星座
     */
    public static final String[] ZODIAC = {
            "白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座", "天秤座", "天蝎座", "射手座", "摩羯座",
            "水瓶座", "双鱼座"
    };

    /**
     * 星座分隔时间日
     */
    public static final int[] SLICED = new int[]{20, 19, 21, 20, 21, 22, 23, 23, 23, 24, 23, 22};

    /**
     * 12生肖,属相
     */
    public static final String[] CN_ANIMAL = {
            "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };

    /**
     * 十天干信息
     */
    public static final String[] CN_GAN = {
            "甲", "乙", "丙", "丁", "戊", "己", "庚", "辛", "壬", "癸"
    };

    /**
     * 十二地支信息
     */
    public static final String[] CN_ZHI = {
            "子", "丑", "寅", "卯", "辰", "巳", "午", "未", "申", "酉", "戌", "亥"
    };

    /**
     * 中文数字
     */
    public static final String[] CN_NUMBER = {
            "〇", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二"
    };

    /**
     * 中文星期
     */
    public static final String[] CN_WEEK = {
            "日", "一", "二", "三", "四", "五", "六"
    };

    /**
     * 农历月份名
     */
    public static final String[] CN_MONTH = {
            "正", "二", "三", "四", "五", "六", "七", "八", "九", "十", "冬", "腊"
    };

    /**
     * 农历日期名
     */
    public static final String[] CN_DAY = {
            "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十", "十一", "十二",
            "十三", "十四", "十五", "十六", "十七", "十八", "十九", "廿十", "廿一", "廿二", "廿三", "廿四",
            "廿五", "廿六", "廿七", "廿八", "廿九", "卅十"
    };

    /**
     * 六十甲子
     */
    public static final String[] CN_JIA_ZI = {
            "甲子", "乙丑", "丙寅", "丁卯", "戊辰", "己巳", "庚午", "辛未", "壬申", "癸酉", "甲戌", "乙亥",
            "丙子", "丁丑", "戊寅", "己卯", "庚辰", "辛巳", "壬午", "癸未", "甲申", "乙酉", "丙戌", "丁亥",
            "戊子", "己丑", "庚寅", "辛卯", "壬辰", "癸巳", "甲午", "乙未", "丙申", "丁酉", "戊戌", "己亥",
            "庚子", "辛丑", "壬寅", "癸卯", "甲辰", "乙巳", "丙午", "丁未", "戊申", "己酉", "庚戌", "辛亥",
            "壬子", "癸丑", "甲寅", "乙卯", "丙辰", "丁巳", "戊午", "己未", "庚申", "辛酉", "壬戌", "癸亥"
    };

    /**
     * 季节
     */
    public static final String[] CN_SEASON = {
            "孟春", "仲春", "季春", "孟夏", "仲夏", "季夏", "孟秋", "仲秋", "季秋", "孟冬", "仲冬", "季冬"
    };

    /**
     * 节气表，国标以冬至为首个节气
     */
    public static final String[] CN_SOLARTERM = {
            "冬至", "小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种",
            "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪"
    };
    /**
     * 每月天数
     */
    public static final int[] DAYS_OF_MONTH = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    /**
     * 闰年表（存在闰月的年份偏移值）
     */
    public static final int[] LEAP_YEAR = {
            6, 14, 19, 25, 33, 36, 38, 41, 44, 52, 55, 79, 117, 136, 147, 150, 155, 158, 185, 193
    };
    /**
     * 月份表（2个字节表示1年，共16个二进制位，前4位表示闰月月份，后12位表示每月大小）
     */
    public static final int[] LEAP_MONTH = {
            0x00, 0x04, 0xad, 0x08, 0x5a, 0x01, 0xd5, 0x54, 0xb4, 0x09, 0x64, 0x05, 0x59, 0x45, 0x95,
            0x0a, 0xa6, 0x04, 0x55, 0x24, 0xad, 0x08, 0x5a, 0x62, 0xda, 0x04, 0xb4, 0x05, 0xb4, 0x55,
            0x52, 0x0d, 0x94, 0x0a, 0x4a, 0x2a, 0x56, 0x02, 0x6d, 0x71, 0x6d, 0x01, 0xda, 0x02, 0xd2,
            0x52, 0xa9, 0x05, 0x49, 0x0d, 0x2a, 0x45, 0x2b, 0x09, 0x56, 0x01, 0xb5, 0x20, 0x6d, 0x01,
            0x59, 0x69, 0xd4, 0x0a, 0xa8, 0x05, 0xa9, 0x56, 0xa5, 0x04, 0x2b, 0x09, 0x9e, 0x38, 0xb6,
            0x08, 0xec, 0x74, 0x6c, 0x05, 0xd4, 0x0a, 0xe4, 0x6a, 0x52, 0x05, 0x95, 0x0a, 0x5a, 0x42,
            0x5b, 0x04, 0xb6, 0x04, 0xb4, 0x22, 0x6a, 0x05, 0x52, 0x75, 0xc9, 0x0a, 0x52, 0x05, 0x35,
            0x55, 0x4d, 0x0a, 0x5a, 0x02, 0x5d, 0x31, 0xb5, 0x02, 0x6a, 0x8a, 0x68, 0x05, 0xa9, 0x0a,
            0x8a, 0x6a, 0x2a, 0x05, 0x2d, 0x09, 0xaa, 0x48, 0x5a, 0x01, 0xb5, 0x09, 0xb0, 0x39, 0x64,
            0x05, 0x25, 0x75, 0x95, 0x0a, 0x96, 0x04, 0x4d, 0x54, 0xad, 0x04, 0xda, 0x04, 0xd4, 0x44,
            0xb4, 0x05, 0x54, 0x85, 0x52, 0x0d, 0x92, 0x0a, 0x56, 0x6a, 0x56, 0x02, 0x6d, 0x02, 0x6a,
            0x41, 0xda, 0x02, 0xb2, 0xa1, 0xa9, 0x05, 0x49, 0x0d, 0x0a, 0x6d, 0x2a, 0x09, 0x56, 0x01,
            0xad, 0x50, 0x6d, 0x01, 0xd9, 0x02, 0xd1, 0x3a, 0xa8, 0x05, 0x29, 0x85, 0xa5, 0x0c, 0x2a,
            0x09, 0x96, 0x54, 0xb6, 0x08, 0x6c, 0x09, 0x64, 0x45, 0xd4, 0x0a, 0xa4, 0x05, 0x51, 0x25,
            0x95, 0x0a, 0x2a, 0x72, 0x5b, 0x04, 0xb6, 0x04, 0xac, 0x52, 0x6a, 0x05, 0xd2, 0x0a, 0xa2,
            0x4a, 0x4a, 0x05, 0x55, 0x94, 0x2d, 0x0a, 0x5a, 0x02, 0x75, 0x61, 0xb5, 0x02, 0x6a, 0x03,
            0x61, 0x45, 0xa9, 0x0a, 0x4a, 0x05, 0x25, 0x25, 0x2d, 0x09, 0x9a, 0x68, 0xda, 0x08, 0xb4,
            0x09, 0xa8, 0x59, 0x54, 0x03, 0xa5, 0x0a, 0x91, 0x3a, 0x96, 0x04, 0xad, 0xb0, 0xad, 0x04,
            0xda, 0x04, 0xf4, 0x62, 0xb4, 0x05, 0x54, 0x0b, 0x44, 0x5d, 0x52, 0x0a, 0x95, 0x04, 0x55,
            0x22, 0x6d, 0x02, 0x5a, 0x71, 0xda, 0x02, 0xaa, 0x05, 0xb2, 0x55, 0x49, 0x0b, 0x4a, 0x0a,
            0x2d, 0x39, 0x36, 0x01, 0x6d, 0x80, 0x6d, 0x01, 0xd9, 0x02, 0xe9, 0x6a, 0xa8, 0x05, 0x29,
            0x0b, 0x9a, 0x4c, 0xaa, 0x08, 0xb6, 0x08, 0xb4, 0x38, 0x6c, 0x09, 0x54, 0x75, 0xd4, 0x0a,
            0xa4, 0x05, 0x45, 0x55, 0x95, 0x0a, 0x9a, 0x04, 0x55, 0x44, 0xb5, 0x04, 0x6a, 0x82, 0x6a,
            0x05, 0xd2, 0x0a, 0x92, 0x6a, 0x4a, 0x05, 0x55, 0x0a, 0x2a, 0x4a, 0x5a, 0x02, 0xb5, 0x02,
            0xb2, 0x31, 0x69, 0x03, 0x31, 0x73, 0xa9, 0x0a, 0x4a, 0x05, 0x2d, 0x55, 0x2d, 0x09, 0x5a,
            0x01, 0xd5, 0x48, 0xb4, 0x09, 0x68, 0x89, 0x54, 0x0b, 0xa4, 0x0a, 0xa5, 0x6a, 0x95, 0x04,
            0xad, 0x08, 0x6a, 0x44, 0xda, 0x04, 0x74, 0x05, 0xb0, 0x25, 0x54, 0x03
    };
    /**
     * 节假日名称
     */
    public static String[] CN_HOLIDAY = {
            "元旦节", "春节", "清明节", "劳动节", "端午节", "中秋节", "国庆节", "国庆中秋", "抗战胜利日"
    };

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

        private final int value;

        Type(int value) {
            this.value = value;
        }

        /**
         * 将 {@link Calendar}相关值转换为DatePart枚举对象
         *
         * @param calendarPartIntValue Calendar中关于Week的int值
         * @return this
         */
        public static Type of(int calendarPartIntValue) {
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

        public int getValue() {
            return this.value;
        }

    }

    /**
     * 时间单位,每个单位都是以毫秒为基数
     */
    public enum Units {

        /**
         * 一毫秒
         */
        MILLISECOND(1, "毫秒"),
        /**
         * 一秒的毫秒数
         */
        SECOND(1000, "秒"),
        /**
         * 一分钟的毫秒数
         */
        MINUTE(SECOND.getUnit() * 60, "分"),
        /**
         * 一小时的毫秒数
         */
        HOUR(MINUTE.getUnit() * 60, "小时"),
        /**
         * 一天的毫秒数
         */
        DAY(HOUR.getUnit() * 24, "天"),
        /**
         * 一周的毫秒数
         */
        WEEK(DAY.getUnit() * 7, "周");

        /**
         * 计算单位
         */
        private final long unit;
        /**
         * 名称
         */
        private final String name;

        Units(long unit, String name) {
            this.unit = unit;
            this.name = name;
        }

        /**
         * @return 单位对应的毫秒数
         */
        public long getUnit() {
            return this.unit;
        }

        /**
         * @return 单位对应的名称
         */
        public String getName() {
            return this.name;
        }

    }

    /**
     * 日期信息
     */
    public enum Date {

        /**
         * 今天
         */
        TODAY("today", "今天"),
        /**
         * 明天
         */
        TOMORROW("tomorrow", "明天"),
        /**
         * 下周
         */
        NEXTWEEK("nextWeek", "下周"),
        /**
         * 下月
         */
        NEXTMONTH("nextMonth", "下月"),
        /**
         * 明年
         */
        NEXTYEAR("nextYear", "明年"),
        /**
         * 昨天
         */
        YESTERDAY("yesterday", "昨天"),
        /**
         * 上周
         */
        LASTWEEK("lastWeek", "上周"),
        /**
         * 上月
         */
        LASTMONTH("lastMonth", "上月"),
        /**
         * 去年
         */
        LASTYEAR("lastYear", "去年");

        /**
         * 编码
         */
        private final String key;
        /**
         * 中文名称
         */
        private final String name;

        Date(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public static Map<String, String> convertToMap() {
            Map<String, String> map = new HashMap<>();
            for (Date date : Date.values()) {
                map.put(date.key, date.key);
                map.put(date.name, date.key);
            }
            return map;
        }

        public static Date getByCode(String code) {
            for (Date date : Date.values()) {
                if (date.key.equals(code)) {
                    return date;
                }
            }
            return null;
        }

        public String getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

    }

    /**
     * 十二时辰枚举，包含英文全称，中文全称，时间范围
     */
    public enum Chrono {

        ZISHI(1, "子时", "23:00:00", "01:00:00"),
        CHOUSHI(2, "丑时", "01:00:00", "03:00:00"),
        YINSHI(3, "寅时", "03:00:00", "05:00:00"),
        MAOSHI(4, "卯辰", "05:00:00", "07:00:00"),
        CHENSHI(5, "辰时", "07:00:00", "09:00:00"),
        SISHI(6, "巳时", "09:00:00", "11:00:00"),
        WUSHI(7, "午时", "11:00:00", "13:00:00"),
        WEISHI(8, "未时", "13:00:00", "15:00:00"),
        SHENSHI(9, "申时", "15:00:00", "17:00:00"),
        YOUSHI(10, "酉时", "17:00:00", "19:00:00"),
        XUSHI(11, "戌时", "19:00:00", "21:00:00"),
        HAISHI(12, "亥时", "21:00:00", "23:00:00");

        /**
         * 序号
         */
        private final int key;

        /**
         * 中文名称
         */
        private final String name;

        /**
         * 开始时间
         */
        private final String startTime;

        /**
         * 结束时间
         */
        private final String endTime;

        Chrono(int key, String name, String startTime, String endTime) {
            this.key = key;
            this.name = name;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        /**
         * 根据时间查询时辰名称枚举
         *
         * @param localTime LocalTime
         * @return this
         */
        public static Chrono getChrono(LocalTime localTime) {
            for (Chrono chrono : Chrono.values()) {
                // 子时，特殊计算
                if (isZiShi(localTime)) {
                    return Chrono.ZISHI;
                }
                if (isBetween(localTime, LocalTime.parse(chrono.startTime), LocalTime.parse(chrono.endTime))) {
                    return chrono;
                }
            }
            return null;
        }

        /**
         * 时间区间判断
         *
         * @param localTime LocalTime
         * @param timeStart 开始时间
         * @param timeEnd   结束时间
         * @return 在区间 true，不在 false
         */
        private static boolean isBetween(LocalTime localTime, LocalTime timeStart, LocalTime timeEnd) {
            return localTime.equals(timeStart) || (localTime.isAfter(timeStart) && localTime.isBefore(timeEnd));
        }

        /**
         * 是否子时
         *
         * @param localTime LocalTime
         * @return 是 true， 否 false
         */
        private static boolean isZiShi(LocalTime localTime) {
            // 23点，0点
            if (LocalTime.of(23, 0, 0).equals(localTime) || LocalTime.MIDNIGHT.equals(localTime)) {
                return true;
            }
            // 23-0点
            if (localTime.isAfter(LocalTime.of(23, 0, 0)) && localTime.isBefore(LocalTime.MIDNIGHT)) {
                return true;
            }
            // 0-1点
            return localTime.isAfter(LocalTime.MIDNIGHT) && localTime.isBefore(LocalTime.of(1, 0, 0));
        }

        /**
         * 根据时间查询时辰名称
         *
         * @param localTime LocalTime
         * @return String
         */
        public static String getName(LocalTime localTime) {
            Chrono chrono = getChrono(localTime);
            return null != chrono ? chrono.name : null;
        }

        /**
         * 根据时间查询时辰名称
         *
         * @param date Date
         * @return String
         */
        public static String getName(java.util.Date date) {
            return getName(Converter.toLocalTime(date));
        }

        public int getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
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
        Jan(Calendar.JANUARY, "January", "一月", "一"),
        /**
         * 二月
         */
        Feb(Calendar.FEBRUARY, "February", "二月", "二"),
        /**
         * 三月
         */
        Mar(Calendar.MARCH, "March", "三月", "三"),
        /**
         * 四月
         */
        Apr(Calendar.APRIL, "April", "四月", "四"),
        /**
         * 五月
         */
        May(Calendar.MAY, "May", "五月", "五"),
        /**
         * 六月
         */
        Jun(Calendar.JUNE, "June", "六月", "六"),
        /**
         * 七月
         */
        Jul(Calendar.JULY, "July", "七月", "七"),
        /**
         * 八月
         */
        Aug(Calendar.AUGUST, "August", "八月", "八"),
        /**
         * 九月
         */
        Sep(Calendar.SEPTEMBER, "September", "九月", "九"),
        /**
         * 十月
         */
        Oct(Calendar.OCTOBER, "October", "十月", "十"),
        /**
         * 十一月
         */
        Nov(Calendar.NOVEMBER, "November", "十一月", "十一"),
        /**
         * 十二月
         */
        Dec(Calendar.DECEMBER, "December", "十二月", "十二"),
        /**
         * 十三月,仅用于农历
         */
        Und(Calendar.UNDECIMBER, "Undecimber", "十三月", "十三");

        /**
         * 每月最后一天
         */
        private static final int[] MOHTH_OF_LASTDAY = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, -1};

        /**
         * 序号
         */
        private final int key;
        /**
         * 英文全称
         */
        private final String enName;
        /**
         * 中文全称
         */
        private final String cnName;
        /**
         * 中文简称
         */
        private final String shortName;

        Month(int key, String enName, String cnName, String shortName) {
            this.key = key;
            this.enName = enName;
            this.cnName = cnName;
            this.shortName = shortName;
        }

        /**
         * 根据code查询月份名称枚举
         *
         * @param code code
         * @return this
         */
        public static Month getByCode(int code) {
            if (code >= 1 && code <= 12) {
                for (Month month : Month.values()) {
                    if (month.key == code) {
                        return month;
                    }
                }
            }
            return null;
        }

        /**
         * 根据code查询月份英文简称
         *
         * @param code code
         * @return String
         */
        public static String getShortNameEnByCode(int code) {
            Month month = getByCode(code);
            return null != month ? month.name() : null;
        }

        /**
         * 根据code查询月份英文全称
         *
         * @param code code
         * @return String
         */
        public static String getFullNameEnByCode(int code) {
            Month month = getByCode(code);
            return null != month ? month.enName : null;
        }

        /**
         * 根据code查询月份中文全称
         *
         * @param code code
         * @return String
         */
        public static String getFullNameCnByCode(int code) {
            Month month = getByCode(code);
            return null != month ? month.cnName : null;
        }

        /**
         * 根据code查询月份中文
         *
         * @param code code
         * @return String
         */
        public static String getShortNameCnByCode(int code) {
            Month month = getByCode(code);
            return null != month ? month.shortName : null;
        }

        /**
         * 获得指定月的最后一天
         *
         * @param month      月份
         * @param isLeapYear 是否为闰年，闰年只对二月有影响
         * @return 最后一天，可能为28,29,30,31
         */
        public static int getLastDay(int month, boolean isLeapYear) {
            int lastDay = MOHTH_OF_LASTDAY[month];
            if (isLeapYear && Calendar.FEBRUARY == month) {
                // 闰年二月
                lastDay += 1;
            }
            return lastDay;
        }

        /**
         * 获取此月份最后一天的值，不支持的月份（例如UNDECIMBER）返回-1
         *
         * @param isLeapYear 是否闰年
         * @return 此月份最后一天的值
         */
        public int getLastDay(boolean isLeapYear) {
            return getLastDay(this.key, isLeapYear);
        }

        public int getKey() {
            return key;
        }

        public String getEnName() {
            return enName;
        }

        public String getCnName() {
            return cnName;
        }

        public String getShortName() {
            return shortName;
        }

    }

    /**
     * 季度枚举
     */
    public enum Quarter {

        /**
         * 第一季度
         */
        Q1(1, "一季度"),
        /**
         * 第二季度
         */
        Q2(2, "二季度"),
        /**
         * 第三季度
         */
        Q3(3, "三季度"),
        /**
         * 第四季度
         */
        Q4(4, "四季度");

        private final int key;
        private final String name;

        Quarter(int key, String name) {
            this.key = key;
            this.name = name;
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

        public int getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

    }

    /**
     * 星期枚举
     * 与Calendar中的星期int值对应
     *
     * @see Calendar#SUNDAY
     * @see Calendar#MONDAY
     * @see Calendar#TUESDAY
     * @see Calendar#WEDNESDAY
     * @see Calendar#THURSDAY
     * @see Calendar#FRIDAY
     * @see Calendar#SATURDAY
     */
    public enum Week {

        /**
         * 周日
         */
        Sun(Calendar.SUNDAY, "Sunday", "星期日"),
        /**
         * 周一
         */
        Mon(Calendar.MONDAY, "Monday", "星期一"),
        /**
         * 周二
         */
        Tue(Calendar.TUESDAY, "Tuesday", "星期二"),
        /**
         * 周三
         */
        Wed(Calendar.WEDNESDAY, "Wednesday", "星期三"),
        /**
         * 周四
         */
        Thu(Calendar.THURSDAY, "Thursday", "星期四"),
        /**
         * 周五
         */
        Fri(Calendar.FRIDAY, "Friday", "星期五"),
        /**
         * 周六
         */
        Sat(Calendar.SATURDAY, "Saturday", "星期六");

        /**
         * 序号
         */
        private final int key;

        /**
         * 英文
         */
        private final String enName;

        /**
         * 中文
         */
        private final String cnName;


        Week(int key, String enName, String cnName) {
            this.key = key;
            this.enName = enName;
            this.cnName = cnName;
        }

        /**
         * 根据code查询星期名称枚举
         *
         * @param code code
         * @return this
         */
        public static Week getByCode(int code) {
            if (code >= 1 && code <= 12) {
                for (Week week : Week.values()) {
                    if (week.key == code) {
                        return week;
                    }
                }
            }
            return null;
        }

        /**
         * 根据code查询星期英文简称
         *
         * @param code code
         * @return String
         */
        public static String getShortNameByCode(int code) {
            Week week = getByCode(code);
            return null != week ? week.name() : null;
        }

        /**
         * 根据code查询星期英文全称
         *
         * @param code code
         * @return String
         */
        public static String getEnNameByCode(int code) {
            Week week = getByCode(code);
            return null != week ? week.enName : null;
        }

        /**
         * 根据code查询星期中文名称
         *
         * @param code code
         * @return String
         */
        public static String getCnNameByCode(int code) {
            Week week = getByCode(code);
            return null != week ? week.cnName : null;
        }

        public int getKey() {
            return key;
        }

        public String getEnName() {
            return enName;
        }

        public String getCnName() {
            return cnName;
        }

    }

    /**
     * 星座名称枚举，包含英文全称，中文全称，时间范围
     */
    public enum Zodiac {

        Aries(1, "白羊座", "03-21", "04-19"),
        Taurus(2, "金牛座", "04-20", "05-20"),
        Gemini(3, "双子座", "05-21", "06-21"),
        Cancer(4, "巨蟹座", "06-22", "07-22"),
        Leo(5, "狮子座", "07-23", "08-22"),
        Virgo(6, "处女座", "08-23", "09-22"),
        Libra(7, "天秤座", "09-23", "10-23"),
        Scorpio(8, "天蝎座", "10-24", "11-22"),
        Sagittarius(9, "射手座", "11-23", "12-21"),
        Capricorn(10, "摩羯座", "12-22", "01-19"),
        Aquarius(11, "水瓶座", "01-20", "02-18"),
        Pisces(12, "双鱼座", "02-19", "03-20");

        /**
         * 序号
         */
        private final int key;
        /**
         * 中文名称
         */
        private final String name;
        /**
         * 开始时间
         */
        private final String startDate;
        /**
         * 结束时间
         */
        private final String endDate;

        Zodiac(int key, String name, String startDate, String endDate) {
            this.key = key;
            this.name = name;
            this.startDate = startDate;
            this.endDate = endDate;
        }

        /**
         * 根据日期查询星座名称枚举
         *
         * @param monthDayStr MM-dd格式
         * @return this
         */
        public static Zodiac getZodiacByMonthDay(String monthDayStr) {
            MonthDay monthDay = MonthDay.parse(Symbol.MINUS + Symbol.MINUS + monthDayStr);
            for (Zodiac zodiac : Zodiac.values()) {
                if (zodiac.startDate.equals(monthDayStr) || zodiac.endDate.equals(monthDayStr)) {
                    return zodiac;
                }
                if (isCapricorn(monthDay)) {
                    return Zodiac.Capricorn;
                }
                MonthDay monthDayStart = MonthDay.parse(Symbol.MINUS + Symbol.MINUS + zodiac.startDate);
                MonthDay monthDayEnd = MonthDay.parse(Symbol.MINUS + Symbol.MINUS + zodiac.endDate);
                if (monthDay.isAfter(monthDayStart) && monthDay.isBefore(monthDayEnd)) {
                    return zodiac;
                }
            }
            return null;
        }

        /**
         * 是否是摩羯座
         *
         * @param monthDay 日期
         * @return true/false
         */
        private static boolean isCapricorn(MonthDay monthDay) {
            MonthDay capricorn_start = MonthDay.parse(Symbol.MINUS + Symbol.MINUS + "12-22");
            MonthDay capricorn_end = MonthDay.parse(Symbol.MINUS + Symbol.MINUS + "01-19");
            if (capricorn_start.equals(monthDay) || capricorn_end.equals(monthDay)) {
                return true;
            }
            if (monthDay.isAfter(capricorn_start) && monthDay.isBefore(capricorn_end)) {
                return true;
            }
            return monthDay.isAfter(capricorn_start) && monthDay.isBefore(capricorn_end);
        }

        /**
         * 根据日期查询星座中文名称
         *
         * @param monthDayStr MM-dd格式
         * @return String
         */
        public static String getCnNameByMonthDay(String monthDayStr) {
            Zodiac zodiac = getZodiacByMonthDay(monthDayStr);
            return null != zodiac ? zodiac.name : null;
        }

        /**
         * 根据日期查询星座英文名称
         *
         * @param monthDayStr MM-dd格式
         * @return String
         */
        public static String getEnNameByMonthDay(String monthDayStr) {
            Zodiac zodiac = getZodiacByMonthDay(monthDayStr);
            return null != zodiac ? zodiac.name() : null;
        }

        public int getKey() {
            return key;
        }

        public String getName() {
            return name;
        }

        public String getStartDate() {
            return startDate;
        }

        public String getEndDate() {
            return endDate;
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

}
