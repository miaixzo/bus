/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.core.getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * 列表类型的Get接口
 *
 * @author Kimi Liu
 * @version 3.6.6
 * @since JDK 1.8+
 */
public interface ListTypeGetter {

    /**
     * 获取Object型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<Object> getObjList(String key);

    /**
     * 获取String型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<String> getStrList(String key);

    /**
     * 获取Integer型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<Integer> getIntList(String key);

    /**
     * 获取Short型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<Short> getShortList(String key);

    /**
     * 获取Boolean型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<Boolean> getBoolList(String key);

    /**
     * 获取BigDecimal型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<Long> getLongList(String key);

    /**
     * 获取Character型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<Character> getCharList(String key);

    /**
     * 获取Double型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<Double> getDoubleList(String key);

    /**
     * 获取Byte型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<Byte> getByteList(String key);

    /**
     * 获取BigDecimal型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<BigDecimal> getBigDecimalList(String key);

    /**
     * 获取BigInteger型属性值列表
     *
     * @param key 属性名
     * @return 属性值列表
     */
    List<BigInteger> getBigIntegerList(String key);

}
