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
package org.miaixz.bus.core.center.date.chinese;

/**
 * 农历标准化输出格式枚举
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum ChineseDateFormat {

    /**
     * 干支纪年 数序纪月 数序纪日
     */
    GSS("干支纪年 数序纪月 数序纪日"),
    /**
     * 生肖纪年 数序纪月 数序纪日
     */
    XSS("生肖纪年 数序纪月 数序纪日"),
    /**
     * 干支生肖纪年 数序纪月（传统表示） 数序纪日
     */
    GXSS("干支生肖纪年 数序纪月（传统表示） 数序纪日"),
    /**
     * 干支纪年 数序纪月 干支纪日
     */
    GSG("干支纪年 数序纪月 干支纪日"),
    /**
     * 干支纪年 干支纪月 干支纪日
     */
    GGG("干支纪年 干支纪月 干支纪日"),
    /**
     * 农历年年首所在的公历年份 干支纪年 数序纪月 数序纪日
     */
    MIX("农历年年首所在的公历年份 干支纪年 数序纪月 数序纪日");

    /**
     * 农历标准化输出格式信息
     */
    private final String info;

    /**
     * 构造
     *
     * @param info 输出格式信息
     */
    ChineseDateFormat(final String info) {
        this.info = info;
    }

    /**
     * 获取农历日期输出格式相关描述
     *
     * @return 输出格式信息
     */
    public String getName() {
        return this.info;
    }

}
