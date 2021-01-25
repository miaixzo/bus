/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
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
package org.aoju.bus.core.getter;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 基于分组的Get接口
 *
 * @author Kimi Liu
 * @version 6.1.9
 * @since JDK 1.8+
 */
public interface GroupedType {

    /**
     * 获取字符串型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    String getStrByGroup(String key, String group);

    /**
     * 获取int型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    Integer getIntByGroup(String key, String group);

    /**
     * 获取short型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    Short getShortByGroup(String key, String group);

    /**
     * 获取boolean型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    Boolean getBoolByGroup(String key, String group);

    /**
     * 获取Long型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    Long getLongByGroup(String key, String group);

    /**
     * 获取char型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    Character getCharByGroup(String key, String group);

    /**
     * 获取double型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    Double getDoubleByGroup(String key, String group);

    /**
     * 获取byte型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    Byte getByteByGroup(String key, String group);

    /**
     * 获取BigDecimal型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    BigDecimal getBigDecimalByGroup(String key, String group);

    /**
     * 获取BigInteger型属性值
     *
     * @param key   属性名
     * @param group 分组
     * @return 属性值
     */
    BigInteger getBigIntegerByGroup(String key, String group);

}
