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
package org.miaixz.bus.core.center.date.culture;

import org.miaixz.bus.core.xyz.DateKit;

import java.util.Calendar;
import java.util.Date;

/**
 * 星座
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Constellation {

    /**
     * 白羊
     */
    ARIES(0, "白羊"),

    /**
     * 金牛
     */
    TAURUS(1, "金牛"),

    /**
     * 双子
     */
    GEMINI(2, "双子"),

    /**
     * 巨蟹
     */
    CANCER(3, "巨蟹"),
    /**
     * 狮子
     */
    LEO(4, "狮子"),
    /**
     * 处女
     */
    VIRGO(5, "处女"),
    /**
     * 天秤
     */
    LIBRA(6, "天秤"),
    /**
     * 天蝎
     */
    SCORPIO(7, "天蝎"),
    /**
     * 射手
     */
    SAGITTARIUS(8, "射手"),
    /**
     * 摩羯
     */
    CAPRICORN(9, "摩羯"),
    /**
     * 水瓶
     */
    AQUARIUS(10, "水瓶"),
    /**
     * 双鱼
     */
    PISCES(11, "双鱼");

    private static final Constellation[] ENUMS = Constellation.values();

    /**
     * 编码
     */
    private final long code;
    /**
     * 名称
     */
    private final String name;

    Constellation(long code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月，从0开始计数，见{@link Month#getValue()}
     * @param day   天
     * @return 星座名
     */
    public static int getCode(int month, int day) {
        int code = 0;
        switch (month) {
            case 1:
                // Capricorn 摩羯座（12月22日～1月20日）
                code = day <= 20 ? 11 : 0;
                break;
            case 2:
                // Aquarius 水瓶座（1月21日～2月19日）
                code = day <= 19 ? 0 : 1;
                break;
            case 3:
                // Pisces 双鱼座（2月20日～3月20日）
                code = day <= 20 ? 1 : 2;
                break;
            case 4:
                // Aries 白羊座 3月21日～4月20日
                code = day <= 20 ? 2 : 3;
                break;
            case 5:
                // Taurus 金牛座 4月21～5月21日
                code = day <= 21 ? 3 : 4;
                break;
            case 6:
                // Gemini 双子座 5月22日～6月21日
                code = day <= 21 ? 4 : 5;
                break;
            case 7:
                // Cancer 巨蟹座（6月22日～7月22日）
                code = day <= 22 ? 5 : 6;
                break;
            case 8:
                // Leo 狮子座（7月23日～8月23日）
                code = day <= 23 ? 6 : 7;
                break;
            case 9:
                // Virgo 处女座（8月24日～9月23日）
                code = day <= 23 ? 7 : 8;
                break;
            case 10:
                // Libra 天秤座（9月24日～10月23日）
                code = day <= 23 ? 8 : 9;
                break;
            case 11:
                // Scorpio 天蝎座（10月24日～11月22日）
                code = day <= 22 ? 9 : 10;
                break;
            case 12:
                // Sagittarius 射手座（11月23日～12月21日）
                code = day <= 21 ? 10 : 11;
                break;
        }
        return code;
    }

    /**
     * 通过生日计算星座
     *
     * @param date 出生日期
     * @return 星座名
     */
    public static String getDesc(final Date date) {
        return getDesc(DateKit.calendar(date));
    }

    /**
     * 通过生日计算星座
     *
     * @param calendar 出生日期
     * @return 星座名
     */
    public static String getDesc(final Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getDesc(calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月，从0开始计数
     * @param day   天
     * @return 星座名
     */
    public static String getDesc(final Month month, final int day) {
        return getDesc(month.getValue(), day);
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月，从0开始计数，见{@link Month#getValue()}
     * @param day   天
     * @return 星座名
     */
    public static String getDesc(int month, int day) {
        int code = getCode(month, day);
        return ENUMS[code].name;
    }

    /**
     * @return 单位对应的编码
     */
    public long getCode() {
        return this.code;
    }

    /**
     * @return 对应的名称
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return 对应的名称
     */
    public String getDesc(int code) {
        return this.name;
    }

}
