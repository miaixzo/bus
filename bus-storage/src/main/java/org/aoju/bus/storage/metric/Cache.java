/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.storage.metric;

/**
 * 缓存,用来缓存State
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public interface Cache {

    /**
     * 设置缓存
     *
     * @param key   缓存KEY
     * @param value 缓存内容
     */
    void set(String key, Object value);

    /**
     * 设置缓存,指定过期时间
     *
     * @param key     缓存KEY
     * @param value   缓存内容
     * @param timeout 指定缓存过期时间（毫秒）
     */
    void set(String key, Object value, long timeout);

    /**
     * 获取缓存
     *
     * @param key 缓存KEY
     * @return 缓存内容
     */
    Object get(String key);

    /**
     * 是否存在key,如果对应key的value值已过期,也返回false
     *
     * @param key 缓存KEY
     * @return true：存在key,并且value没过期；false：key不存在或者已过期
     */
    boolean containsKey(String key);

    /**
     * 清理过期的缓存
     */
    default void pruneCache() {

    }

}
