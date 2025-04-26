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
package org.miaixz.bus.core.center.date.culture.en;

import java.time.temporal.ChronoField;

import org.miaixz.bus.core.lang.Assert;

/**
 * 季度枚举
 *
 * @author Kimi Liu
 * @see #Q1
 * @see #Q2
 * @see #Q3
 * @see #Q4
 * @since Java 17+
 */
public enum Quarter {

    /**
     * 一季度
     */
    Q1(1, "一季度"),
    /**
     * 二季度
     */
    Q2(2, "二季度"),
    /**
     * 三季度
     */
    Q3(3, "三季度"),
    /**
     * 四季度
     */
    Q4(4, "四季度");

    private static final Quarter[] ENUMS = Quarter.values();

    /**
     * 编码
     */
    private final int code;
    /**
     * 名称
     */
    private final String name;

    Quarter(final int code, final String name) {
        this.code = code;
        this.name = name;
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
    public static String getName(int code) {
        return ENUMS[code].name;
    }

    /**
     * 获取季度值
     *
     * @return 季度值
     */
    public int getCode() {
        return this.code;
    }

    /**
     * 获取季度值
     *
     * @return 季度值
     */
    public String getName() {
        return this.name;
    }

    /**
     * 根据给定的月份值返回对应的季度
     *
     * @param monthValue 月份值，取值范围为1到12
     * @return 对应的季度
     * @throws IllegalArgumentException 如果月份值不在有效范围内（1到12），将抛出异常
     */
    public static Quarter fromMonth(final int monthValue) {
        ChronoField.MONTH_OF_YEAR.checkValidValue(monthValue);
        return of(computeQuarterValueInternal(monthValue));
    }

    /**
     * 根据给定的月份返回对应的季度
     *
     * @param month 月份
     * @return 对应的季度
     */
    public static Quarter fromMonth(final Month month) {
        Assert.notNull(month);
        final int monthValue = month.getValue();
        return of(computeQuarterValueInternal(monthValue));
    }

    /**
     * 该季度的第一个月
     *
     * @return 结果
     */
    public Month firstMonth() {
        return Month.of(this.code * 3 - 3);
    }

    /**
     * 该季度最后一个月
     *
     * @return 结果
     */
    public Month lastMonth() {
        return Month.of(this.code * 3 - 1);
    }

    /**
     * 计算给定月份对应的季度值
     *
     * @param monthValue 月份值，取值范围为1到12
     * @return 对应的季度值
     */
    private static int computeQuarterValueInternal(final int monthValue) {
        return (monthValue - 1) / 3 + 1;
    }

}
