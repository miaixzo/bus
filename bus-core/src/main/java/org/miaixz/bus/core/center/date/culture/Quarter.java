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
    public int getCode() {
        return this.code;
    }

    /**
     * 获取季度值
     *
     * @return 季度值
     */
    public int getName() {
        return this.code;
    }

    /**
     * 获取季度值
     *
     * @return 季度值
     */
    public String getDesc(int code) {
        return ENUMS[code].name;
    }

}
