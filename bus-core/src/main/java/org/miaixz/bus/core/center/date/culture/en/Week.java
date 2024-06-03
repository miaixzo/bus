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
package org.miaixz.bus.core.center.date.culture.en;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.EnumKit;

import java.time.DayOfWeek;
import java.util.Calendar;

/**
 * 星期枚举
 * 与Calendar中的星期int值对应
 *
 * @author Kimi Liu
 * @see #SUNDAY
 * @see #MONDAY
 * @see #TUESDAY
 * @see #WEDNESDAY
 * @see #THURSDAY
 * @see #FRIDAY
 * @see #SATURDAY
 * @since Java 17+
 */
public enum Week {

    /**
     * 周日
     */
    SUNDAY(Calendar.SUNDAY, "日"),
    /**
     * 周一
     */
    MONDAY(Calendar.MONDAY, "一"),
    /**
     * 周二
     */
    TUESDAY(Calendar.TUESDAY, "二"),
    /**
     * 周三
     */
    WEDNESDAY(Calendar.WEDNESDAY, "三"),
    /**
     * 周四
     */
    THURSDAY(Calendar.THURSDAY, "四"),
    /**
     * 周五
     */
    FRIDAY(Calendar.FRIDAY, "五"),
    /**
     * 周六
     */
    SATURDAY(Calendar.SATURDAY, "六");

    private static final Week[] ENUMS = Week.values();

    /**
     * 星期对应{@link Calendar} 中的Week值
     */
    private final int code;
    /**
     * 星期对应名称
     */
    private final String name;

    /**
     * 构造
     *
     * @param code 星期对应{@link Calendar} 中的Week值
     */
    Week(final int code, final String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 将 {@link Calendar}星期相关值转换为Week枚举对象
     *
     * @param code Calendar中关于Week的int值，1表示Sunday
     * @return Week
     * @see #SUNDAY
     * @see #MONDAY
     * @see #TUESDAY
     * @see #WEDNESDAY
     * @see #THURSDAY
     * @see #FRIDAY
     * @see #SATURDAY
     */
    public static Week of(final int code) {
        if (code > ENUMS.length || code < 1) {
            return null;
        }
        return ENUMS[code - 1];
    }

    /**
     * 解析别名为Week对象，别名如：sun或者SUNDAY，不区分大小写
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
     * 转换为中文名
     *
     * @param code   Calendar中关于Week的int值，1表示Sunday
     * @param prefix 表示星期的前缀，例如前缀为“星期”，则返回结果为“星期一”；前缀为”周“，结果为“周一”
     * @return 星期的中文名
     */
    public static String getName(final int code, String prefix) {
        return prefix + ENUMS[code].name;
    }

    /**
     * 获取枚举属性信息
     *
     * @param fieldName 属性名称
     * @return the string[]
     */
    public static String[] get(String fieldName) {
        return EnumKit.getFieldValues(Week.class, fieldName).toArray(String[]::new);
    }

    /**
     * 获得星期对应{@link Calendar} 中的Week值
     *
     * @return 星期对应 {@link Calendar} 中的Week值
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 获取ISO8601规范的int值，from 1 (Monday) to 7 (Sunday).
     *
     * @return ISO8601规范的int值
     */
    public int getIsoValue() {
        int iso8601IntValue = getCode() - 1;
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
    public String getName() {
        return this.name;
    }

    /**
     * 转换为中文名
     *
     * @param prefix 表示星期的前缀，例如前缀为“星期”，则返回结果为“星期一”；前缀为”周“，结果为“周一”
     * @return 星期的中文名
     */
    public String getName(final String prefix) {
        switch (this) {
            case SUNDAY:
                return prefix + "日";
            case MONDAY:
                return prefix + "一";
            case TUESDAY:
                return prefix + "二";
            case WEDNESDAY:
                return prefix + "三";
            case THURSDAY:
                return prefix + "四";
            case FRIDAY:
                return prefix + "五";
            case SATURDAY:
                return prefix + "六";
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
        return DayOfWeek.of(getIsoValue());
    }

}
