/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org 6tail and other contributors.              *
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
package org.miaixz.bus.core.center.date.culture.cn;

import org.miaixz.bus.core.center.date.culture.Samsara;
import org.miaixz.bus.core.center.date.culture.cn.sixty.EarthBranch;
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
public class Zodiac extends Samsara {

    public static final String[] NAMES = {
            "鼠", "牛", "虎", "兔", "龙", "蛇", "马", "羊", "猴", "鸡", "狗", "猪"
    };

    public Zodiac(int index) {
        super(NAMES, index);
    }

    public Zodiac(String name) {
        super(NAMES, name);
    }

    public static Zodiac fromIndex(int index) {
        return new Zodiac(index);
    }

    public static Zodiac fromName(String name) {
        return new Zodiac(name);
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
        return NAMES[year % Normal._12];
    }

    public Zodiac next(int n) {
        return fromIndex(nextIndex(n));
    }

    /**
     * 地支
     *
     * @return 地支
     */
    public EarthBranch getEarthBranch() {
        return EarthBranch.fromIndex(index);
    }

}
