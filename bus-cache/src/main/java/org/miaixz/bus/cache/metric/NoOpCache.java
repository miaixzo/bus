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
package org.miaixz.bus.cache.metric;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import org.miaixz.bus.cache.CacheX;

/**
 * 无缓存实现，用于快速关闭缓存
 * <p>
 * 空操作缓存实现，所有方法都是空操作，不执行任何实际缓存功能。 主要用于需要快速关闭缓存功能的场景，避免修改大量代码。
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class NoOpCache<K, V> implements CacheX<K, V> {

    /**
     * 读取缓存值
     * <p>
     * 空操作，始终返回null
     * </p>
     *
     * @param key 缓存键
     * @return 始终返回null
     */
    @Override
    public V read(K key) {
        return null;
    }

    /**
     * 写入缓存值
     * <p>
     * 空操作，不执行任何操作
     * </p>
     *
     * @param key    缓存键
     * @param value  缓存值
     * @param expire 过期时间（毫秒）
     */
    @Override
    public void write(K key, V value, long expire) {
        // 空操作
    }

    /**
     * 批量读取缓存值
     * <p>
     * 空操作，始终返回空Map
     * </p>
     *
     * @param keys 缓存键集合
     * @return 始终返回空Map
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        return Collections.emptyMap();
    }

    /**
     * 批量写入缓存值
     * <p>
     * 空操作，不执行任何操作
     * </p>
     *
     * @param keyValueMap 键值映射
     * @param expire      过期时间（毫秒）
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        // 空操作
    }

    /**
     * 移除缓存
     * <p>
     * 空操作，不执行任何操作
     * </p>
     *
     * @param keys 要移除的缓存键
     */
    @Override
    public void remove(K... keys) {
        // 空操作
    }

    /**
     * 清空缓存
     * <p>
     * 空操作，不执行任何操作
     * </p>
     */
    @Override
    public void clear() {
        // 空操作
    }

}