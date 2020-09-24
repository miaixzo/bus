/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.core.getter;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * 可选默认值的数组类型的Get接口
 * 提供一个统一的接口定义返回不同类型的值(基本类型)
 * 如果值不存在或获取错误,返回默认值
 *
 * @author Kimi Liu
 * @version 6.1.0
 * @since JDK 1.8+
 */
public interface OptArrayType {

    /**
     * 获取Object型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    Object[] getObjs(String key, Object[] defaultValue);

    /**
     * 获取String型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    String[] getStrs(String key, String[] defaultValue);

    /**
     * 获取Integer型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    Integer[] getInts(String key, Integer[] defaultValue);

    /**
     * 获取Short型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    Short[] getShorts(String key, Short[] defaultValue);

    /**
     * 获取Boolean型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    Boolean[] getBools(String key, Boolean[] defaultValue);

    /**
     * 获取Long型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    Long[] getLongs(String key, Long[] defaultValue);

    /**
     * 获取Character型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    Character[] getChars(String key, Character[] defaultValue);

    /**
     * 获取Double型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    Double[] getDoubles(String key, Double[] defaultValue);

    /**
     * 获取Byte型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    Byte[] getBytes(String key, Byte[] defaultValue);

    /**
     * 获取BigInteger型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    BigInteger[] getBigIntegers(String key, BigInteger[] defaultValue);

    /**
     * 获取BigDecimal型属性值数组
     *
     * @param key          属性名
     * @param defaultValue 默认数组值
     * @return 属性值列表
     */
    BigDecimal[] getBigDecimals(String key, BigDecimal[] defaultValue);

}
