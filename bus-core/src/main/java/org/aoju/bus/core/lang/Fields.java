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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.lang;

import org.aoju.bus.core.date.format.FormatBuilder;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

/**
 * 日期场景属性
 *
 * @author Kimi Liu
 * @version 6.1.6
 * @since JDK 1.8+
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
    public static final String NORM_CN_DATE_TIME_PATTERN = "yyyy年MM月dd日HH时mm分ss秒";
    /**
     * 标准日期格式 {@link FormatBuilder}：yyyy年MM月dd日HH时mm分ss秒
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
    public static final String UTC_SIMPLE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
    /**
     * UTC时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ss
     */
    public static final FormatBuilder UTC_SIMPLE_FORMAT = FormatBuilder.getInstance(UTC_SIMPLE_PATTERN, TimeZone.getTimeZone("UTC"));

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
     * UTC时间{@link FormatBuilder}：yyyy-MM-dd'T'HH:mm:ssZ
     */
    public final static FormatBuilder WITH_ZONE_OFFSET_FORMAT = FormatBuilder.getInstance(WITH_ZONE_OFFSET_PATTERN, TimeZone.getTimeZone("UTC"));

    /**
     * 标准日期时间正则，每个字段支持单个数字或2个数字
     * <pre>
     *     yyyy-MM-dd HH:mm:ss.SSS
     *     yyyy-MM-dd HH:mm:ss
     *     yyyy-MM-dd HH:mm
     *     yyyy-MM-dd
     * </pre>
     */
    public static final Pattern REGEX_NORM = Pattern.compile("\\d{4}-\\d{1,2}-\\d{1,2}(\\s\\d{1,2}:\\d{1,2}(:\\d{1,2})?)?(.\\d{1,3})?");

    /**
     * java.util.Date EEE MMM zzz 缩写数组
     */
    public final static String[] WTB = {
            "sun", "mon", "tue", "wed", "thu", "fri", "sat",                                     // 星期
            "jan", "feb", "mar", "apr", "may", "jun", "jul", "aug", "sep", "oct", "nov", "dec",  // 月份
            "gmt", "ut", "utc", "est", "edt", "cst", "cdt", "mst", "mdt", "pst", "pdt"           // 时区
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
     * 星期
     */
    public static final String[] CN_WEEK = {
            "日", "一", "二", "三", "四", "五", "六"
    };

    /**
     * 农历年份名
     */
    public static final String[] CN_YEAR = {
            "〇", "一", "二", "三", "四", "五", "六", "七", "八", "九"
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
     * 节
     */
    public static final String[] CN_JIE = {
            "小寒", "立春", "惊蛰", "清明", "立夏", "芒种", "小暑", "立秋", "白露", "寒露", "立冬", "大雪"
    };

    /**
     * 气
     */
    public static final String[] CN_QI = {
            "大寒", "雨水", "春分", "谷雨", "小满", "夏至", "大暑", "处暑", "秋分", "霜降", "小雪", "冬至"
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
     * 闰年表
     */
    public static final int[] LEAP_YEAR = {
            6, 14, 19, 25, 33, 36, 38, 41, 44, 52, 55, 79, 117, 136, 147, 150, 155, 158, 185, 193
    };
    /**
     * 闰月表
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

        private int value;

        Type(int value) {
            this.value = value;
        }

        /**
         * 将 {@link Calendar}相关值转换为DatePart枚举对象
         *
         * @param calendarPartIntValue Calendar中关于Week的int值
         * @return {@link Type}
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
    public enum Time {

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

        Time(long millis) {
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
         * 十三月,仅用于农历
         */
        UNDECIMBER(Calendar.UNDECIMBER);

        /**
         * 每月最后一天
         */
        private static final int[] MOHTH_OF_LASTDAY = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, -1};

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

        public int getValue() {
            return this.value;
        }

        /**
         * 获取此月份最后一天的值，不支持的月份（例如UNDECIMBER）返回-1
         *
         * @param isLeapYear 是否闰年
         * @return 此月份最后一天的值
         */
        public int getLastDay(boolean isLeapYear) {
            return getLastDay(this.value, isLeapYear);
        }

    }

    /**
     * 季度枚举
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
        MILLISECOND("毫秒");

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
