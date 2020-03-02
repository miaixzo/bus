/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
import java.util.Date;

/**
 * 基本类型的getter接口
 * 提供一个统一的接口定义返回不同类型的值（基本类型）
 *
 * @author Kimi Liu
 * @version 5.6.5
 * @since JDK 1.8+
 */
public interface BasicType<K> {

    /**
     * 获取Object属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    Object getObj(K key);

    /**
     * 获取字符串型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    String getStr(K key);

    /**
     * 获取int型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    Integer getInt(K key);

    /**
     * 获取short型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    Short getShort(K key);

    /**
     * 获取boolean型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    Boolean getBool(K key);

    /**
     * 获取long型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    Long getLong(K key);

    /**
     * 获取char型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    Character getChar(K key);

    /**
     * 获取float型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    Float getFloat(K key);

    /**
     * 获取double型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    Double getDouble(K key);

    /**
     * 获取byte型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    Byte getByte(K key);

    /**
     * 获取BigDecimal型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    BigDecimal getBigDecimal(K key);

    /**
     * 获取BigInteger型属性值
     *
     * @param key 属性名
     * @return 属性值
     */
    BigInteger getBigInteger(K key);

    /**
     * 获得Enum类型的值
     *
     * @param <E>   枚举类型
     * @param clazz Enum的Class
     * @param key   KEY
     * @return Enum类型的值, 无则返回Null
     */
    <E extends Enum<E>> E getEnum(Class<E> clazz, K key);

    /**
     * 获取Date类型值
     *
     * @param key 属性名
     * @return Date类型属性值
     */
    Date getDate(K key);

}
