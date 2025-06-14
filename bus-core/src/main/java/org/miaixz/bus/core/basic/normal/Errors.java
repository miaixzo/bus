/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.basic.normal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.exception.InternalException;

/**
 * 错误码接口，定义错误码和错误信息的获取与注册方法，用于统一管理系统中的错误码。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Errors {

    /**
     * 全局错误码缓存，使用并发安全的ConcurrentHashMap存储所有注册的错误码条目。 键为错误码（String），值为错误码条目（Entry）。
     */
    Map<String, Entry> ERRORS_CACHE = new ConcurrentHashMap<>();

    /**
     * 获取错误码。
     *
     * @return 错误码字符串，用于唯一标识一个错误
     */
    String getKey();

    /**
     * 获取错误信息。
     *
     * @return 错误信息字符串，描述错误的详细信息
     */
    String getValue();

    /**
     * 将错误码注册到全局缓存中。 如果错误码已存在于缓存中，将抛出InternalException异常。
     *
     * @throws InternalException 如果尝试注册重复的错误码
     */
    default void register() {
        if (ERRORS_CACHE.containsKey(getKey())) {
            throw new InternalException("重复注册错误码：" + getKey());
        }
        ERRORS_CACHE.putIfAbsent(getKey(), new Entry(getKey(), getValue()));
    }

    /**
     * 检查全局缓存中是否包含指定的错误码。
     *
     * @param code 待检查的错误码
     * @return true 表示缓存中包含该错误码，false 表示不包含
     */
    static boolean contains(String code) {
        return ERRORS_CACHE.containsKey(code);
    }

    /**
     * 从全局缓存中获取指定错误码的条目。
     *
     * @param key 错误码
     * @return 对应的错误码条目（Entry），若不存在则返回 null
     */
    static Entry require(String key) {
        return ERRORS_CACHE.get(key);
    }

    /**
     * 错误码条目内部类，存储错误码和错误信息，实现ErrorCode接口。
     */
    class Entry implements Errors {

        /** 错误码 */
        private final String key;
        /** 错误信息 */
        private final String value;

        /**
         * 构造错误码条目。
         *
         * @param key   错误码
         * @param value 错误信息
         */
        public Entry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * 获取错误码。
         *
         * @return 错误码字符串
         */
        @Override
        public String getKey() {
            return key;
        }

        /**
         * 获取错误信息。
         *
         * @return 错误信息字符串
         */
        @Override
        public String getValue() {
            return value;
        }
    }

}