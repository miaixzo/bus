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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.DateKit;

import java.util.Calendar;
import java.util.Date;

/**
 * 生肖
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum Zodiac {

    /**
     * 猴
     */
    MONKEY(8, "猴"),
    /**
     * 鸡
     */
    HICKEN(9, "鸡"),
    /**
     * 狗
     */
    DOG(10, "狗"),
    /**
     * 猪
     */
    PIG(11, "猪"),
    /**
     * 鼠
     */
    MOUSE(0, "鼠"),

    /**
     * 牛
     */
    OX(1, "牛"),

    /**
     * 虎
     */
    TIGER(2, "虎"),

    /**
     * 兔
     */
    RABBIT(3, "兔"),
    /**
     * 龙
     */
    LOONG(4, "龙"),
    /**
     * 蛇
     */
    SNAKE(5, "蛇"),
    /**
     * 马
     */
    HORSE(6, "马"),
    /**
     * 羊
     */
    SHEEP(7, "羊");

    private static final Zodiac[] ENUMS = Zodiac.values();

    /**
     * 编码
     */
    private final long code;
    /**
     * 名称
     */
    private final String name;

    Zodiac(long code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 通过生日计算生肖，只计算1900年后出生的人
     *
     * @param date 出生日期（年需农历）
     * @return 星座名
     */
    public static String getName(final Date date) {
        return getName(DateKit.calendar(date));
    }

    /**
     * 通过生日计算生肖，只计算1900年后出生的人
     *
     * @param calendar 出生日期（年需农历）
     * @return 星座名
     */
    public static String getName(final Calendar calendar) {
        if (null == calendar) {
            return null;
        }
        return getName(calendar.get(Calendar.YEAR));
    }

    /**
     * 计算生肖
     *
     * @param year 农历年
     * @return 生肖名
     */
    public static String getName(final int year) {
        return ENUMS[year % Normal._12].name;
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
