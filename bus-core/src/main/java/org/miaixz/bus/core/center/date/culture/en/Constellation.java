/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.center.date.culture.en;

import org.miaixz.bus.core.center.date.Calendar;
import org.miaixz.bus.core.xyz.EnumKit;

import java.time.LocalDate;
import java.time.MonthDay;
import java.util.Date;

/**
 * 星座
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Constellation {

    /**
     * 白羊座
     */
    ARIES(0, "白羊", MonthDay.of(3, 21), MonthDay.of(4, 19)),
    /**
     * 金牛座
     */
    TAURUS(1, "金牛", MonthDay.of(4, 20), MonthDay.of(5, 20)),
    /**
     * 双子座
     */
    GEMINI(2, "双子", MonthDay.of(5, 21), MonthDay.of(6, 21)),
    /**
     * 巨蟹座
     */
    CANCER(3, "巨蟹", MonthDay.of(6, 22), MonthDay.of(7, 22)),
    /**
     * 狮子座
     */
    LEO(4, "狮子", MonthDay.of(7, 23), MonthDay.of(8, 22)),
    /**
     * 处女座
     */
    VIRGO(5, "处女", MonthDay.of(8, 23), MonthDay.of(9, 22)),
    /**
     * 天秤座
     */
    LIBRA(6, "天秤", MonthDay.of(9, 23), MonthDay.of(10, 23)),
    /**
     * 天蝎座
     */
    SCORPIO(7, "天蝎", MonthDay.of(10, 24), MonthDay.of(11, 22)),
    /**
     * 射手座
     */
    SAGITTARIUS(8, "射手", MonthDay.of(11, 23), MonthDay.of(12, 21)),
    /**
     * 摩羯座
     */
    CAPRICORN(9, "摩羯", MonthDay.of(12, 22), MonthDay.of(1, 19)),
    /**
     * 水瓶座
     */
    AQUARIUS(10, "水瓶", MonthDay.of(1, 20), MonthDay.of(2, 18)),
    /**
     * 双鱼座
     */
    PISCES(11, "双鱼", MonthDay.of(2, 19), MonthDay.of(3, 20));

    private static final Constellation[] ENUMS = Constellation.values();

    /**
     * 编码
     */
    private final long code;
    /**
     * 名称
     */
    private final String name;

    /**
     * 开始月日(包含)
     */
    private final MonthDay begin;

    /**
     * 结束月日(包含)
     */
    private final MonthDay end;

    Constellation(long code, String name, MonthDay begin, MonthDay end) {
        this.code = code;
        this.name = name;
        this.begin = begin;
        this.end = end;
    }

    /**
     * 按宫位获取星座
     *
     * @param code 编码
     * @return this
     */
    public static Constellation get(int code) {
        if (code < 1 || code > 12) {
            throw new IllegalArgumentException();
        }
        return ENUMS[(code + 2) % 12];
    }

    /**
     * 按日期获取星座
     *
     * @param date 日期
     * @return this
     */
    public static Constellation get(LocalDate date) {
        return get(date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * 按月日获取星座
     *
     * @param month 月份
     * @param day   日期
     * @return this
     */
    public static Constellation get(int month, int day) {
        return get(MonthDay.of(month, day));
    }

    /**
     * 按月日获取星座
     *
     * @param monthDay 月份
     * @return this
     */
    public static Constellation get(MonthDay monthDay) {
        int month = monthDay.getMonthValue();
        int day = monthDay.getDayOfMonth();
        Constellation zodiac = ENUMS[month - 1];
        return day <= zodiac.end.getDayOfMonth() ? zodiac : ENUMS[month % 12];
    }

    /**
     * 通过生日计算星座
     *
     * @param date 出生日期
     * @return 星座名
     */
    public static String getName(final Date date) {
        return getName(Calendar.calendar(date));
    }

    /**
     * 通过生日计算星座
     *
     * @param calendar 出生日期
     * @return 星座名
     */
    public static String getName(final java.util.Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getName(calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH));
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月，从0开始计数
     * @param day   天
     * @return 星座名
     */
    public static String getName(final Month month, final int day) {
        return getName(month.getValue(), day);
    }

    /**
     * 通过生日计算星座
     *
     * @param month 月，从0开始计数，见{@link Month#getValue()}
     * @param day   天
     * @return 星座名
     */
    public static String getName(final int month, final int day) {
        return get(month, day).name;
    }

    /**
     * 获取枚举属性信息
     *
     * @param fieldName 属性名称
     * @return the string[]
     */
    public static String[] get(String fieldName) {
        return EnumKit.getFieldValues(Constellation.class, fieldName).toArray(String[]::new);
    }

    /**
     * @return 对应的名称
     */
    public String getName(final int code) {
        return this.name;
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

}
