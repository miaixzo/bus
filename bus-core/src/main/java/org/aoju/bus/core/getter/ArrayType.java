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
 * 数组类型的Get接口
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
 */
public interface ArrayType {

    /**
     * 获取Object型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    String[] getObjs(String key);

    /**
     * 获取String型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    String[] getStrs(String key);

    /**
     * 获取Integer型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    Integer[] getInts(String key);

    /**
     * 获取Short型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    Short[] getShorts(String key);

    /**
     * 获取Boolean型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    Boolean[] getBools(String key);

    /**
     * 获取Long型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    Long[] getLongs(String key);

    /**
     * 获取Character型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    Character[] getChars(String key);

    /**
     * 获取Double型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    Double[] getDoubles(String key);

    /**
     * 获取Byte型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    Byte[] getBytes(String key);

    /**
     * 获取BigInteger型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    BigInteger[] getBigIntegers(String key);

    /**
     * 获取BigDecimal型属性值数组
     *
     * @param key 属性名
     * @return 属性值列表
     */
    BigDecimal[] getBigDecimals(String key);

}
