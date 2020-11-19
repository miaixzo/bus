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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.core.getter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

/**
 * 基本类型的getter接口抽象实现
 * 提供一个统一的接口定义返回不同类型的值(基本类型)
 * 在不提供默认值的情况下, 如果值不存在或获取错误,返回null
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
public interface OptNullType<K> extends BasicType<K>, OptBasicType<K> {

    @Override
    default Object getObj(K key) {
        return getObj(key, null);
    }

    /**
     * 获取字符串型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default String getStr(K key) {
        return this.getStr(key, null);
    }

    /**
     * 获取int型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default Integer getInt(K key) {
        return this.getInt(key, null);
    }

    /**
     * 获取short型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default Short getShort(K key) {
        return this.getShort(key, null);
    }

    /**
     * 获取boolean型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default Boolean getBool(K key) {
        return this.getBool(key, null);
    }

    /**
     * 获取long型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default Long getLong(K key) {
        return this.getLong(key, null);
    }

    /**
     * 获取char型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default Character getChar(K key) {
        return this.getChar(key, null);
    }

    /**
     * 获取float型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default Float getFloat(K key) {
        return this.getFloat(key, null);
    }

    /**
     * 获取double型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default Double getDouble(K key) {
        return this.getDouble(key, null);
    }

    /**
     * 获取byte型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default Byte getByte(K key) {
        return this.getByte(key, null);
    }

    /**
     * 获取BigDecimal型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default BigDecimal getBigDecimal(K key) {
        return this.getBigDecimal(key, null);
    }

    /**
     * 获取BigInteger型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default BigInteger getBigInteger(K key) {
        return this.getBigInteger(key, null);
    }

    /**
     * 获取Enum型属性值
     * 无值或获取错误返回null
     *
     * @param clazz Enum 的 Class
     * @param key   属性名
     * @return 属性值
     */
    @Override
    default <E extends Enum<E>> E getEnum(Class<E> clazz, K key) {
        return this.getEnum(clazz, key, null);
    }

    /**
     * 获取Date型属性值
     * 无值或获取错误返回null
     *
     * @param key 属性名
     * @return 属性值
     */
    @Override
    default Date getDate(K key) {
        return this.getDate(key, null);
    }

}
