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
package org.miaixz.bus.core.map;

import org.miaixz.bus.core.text.TextJoiner;
import org.miaixz.bus.core.toolkit.ArrayKit;
import org.miaixz.bus.core.toolkit.StringKit;

import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Map拼接器，可以拼接包括Map、Entry列表等
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MapJoiner {

    private final TextJoiner joiner;
    private final String keyValueSeparator;

    /**
     * 构造
     *
     * @param joiner            entry之间的Joiner
     * @param keyValueSeparator kv之间的连接符
     */
    public MapJoiner(final TextJoiner joiner, final String keyValueSeparator) {
        this.joiner = joiner;
        this.keyValueSeparator = keyValueSeparator;
    }

    /**
     * 构建一个MapJoiner
     *
     * @param separator         entry之间的连接符
     * @param keyValueSeparator kv之间的连接符
     * @return this
     */
    public static MapJoiner of(final String separator, final String keyValueSeparator) {
        return of(TextJoiner.of(separator), keyValueSeparator);
    }

    /**
     * 构建一个MapJoiner
     *
     * @param joiner            entry之间的Joiner
     * @param keyValueSeparator kv之间的连接符
     * @return this
     */
    public static MapJoiner of(final TextJoiner joiner, final String keyValueSeparator) {
        return new MapJoiner(joiner, keyValueSeparator);
    }

    /**
     * 追加Map
     *
     * @param <K>       键类型
     * @param <V>       值类型
     * @param map       Map
     * @param predicate Map过滤器
     * @return this
     */
    public <K, V> MapJoiner append(final Map<K, V> map, final Predicate<Map.Entry<K, V>> predicate) {
        return append(map.entrySet().iterator(), predicate);
    }

    /**
     * 追加Entry列表
     *
     * @param <K>       键类型
     * @param <V>       值类型
     * @param parts     Entry列表
     * @param predicate Map过滤器
     * @return this
     */
    public <K, V> MapJoiner append(final Iterator<? extends Map.Entry<K, V>> parts, final Predicate<Map.Entry<K, V>> predicate) {
        if (null == parts) {
            return this;
        }

        Map.Entry<K, V> entry;
        while (parts.hasNext()) {
            entry = parts.next();
            if (null == predicate || predicate.test(entry)) {
                joiner.append(TextJoiner.of(this.keyValueSeparator).append(entry.getKey()).append(entry.getValue()));
            }
        }
        return this;
    }

    /**
     * 追加其他字符串，其他字符串简单拼接
     *
     * @param params 字符串列表
     * @return this
     */
    public MapJoiner append(final String... params) {
        if (ArrayKit.isNotEmpty(params)) {
            joiner.append(StringKit.concat(false, params));
        }
        return this;
    }

    @Override
    public String toString() {
        return joiner.toString();
    }

}
