/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
 ********************************************************************************/
package org.aoju.bus.cron.pattern.parser;

/**
 * 值处理接口
 * 值处理用于限定表达式中相应位置的值范围,并转换表达式值为int值
 *
 * @author Kimi Liu
 * @version 5.6.9
 * @since JDK 1.8+
 */
public interface ValueParser {

    /**
     * 处理String值并转为int
     * 转换包括：
     * <ol>
     * <li>数字字符串转为数字</li>
     * <li>别名转为对应的数字（如月份和星期）</li>
     * </ol>
     *
     * @param value String值
     * @return int
     */
    int parse(String value);

    /**
     * 返回最小值
     *
     * @return 最小值
     */
    int getMin();

    /**
     * 返回最大值
     *
     * @return 最大值
     */
    int getMax();

}
