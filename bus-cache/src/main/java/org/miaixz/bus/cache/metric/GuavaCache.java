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

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.miaixz.bus.cache.CacheX;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Guava 缓存支持
 * <p>
 * 基于Google Guava Cache实现的缓存接口，提供本地缓存功能。 支持设置最大容量、过期时间等配置参数，并提供批量读写操作。
 * </p>
 *
 * @param <K> 键类型
 * @param <V> 值类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class GuavaCache<K, V> implements CacheX<K, V> {

    /**
     * Guava缓存实例
     */
    private LoadingCache<K, V> cache;

    /**
     * 构造方法
     * <p>
     * 使用指定的大小和过期时间创建缓存实例
     * </p>
     *
     * @param size   缓存最大容量
     * @param expire 过期时间（毫秒）
     */
    public GuavaCache(long size, long expire) {
        this.cache = CacheBuilder.newBuilder().maximumSize(size).expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                .build(new CacheLoader<>() {
                    @Override
                    public Object load(Object key) {
                        return null;
                    }
                });
    }

    /**
     * 构造方法
     * <p>
     * 使用Properties配置创建缓存实例，支持配置最大容量、过期时间等参数
     * </p>
     *
     * @param properties 配置属性
     */
    public GuavaCache(Properties properties) {
        // 获取参数前缀
        String prefix = StringKit.isNotEmpty(properties.getProperty("prefix")) ? properties.getProperty("prefix")
                : Normal.EMPTY;
        // 获取所有配置值
        String maximumSize = properties.getProperty(prefix + "maximumSize");
        String expireAfterAccess = properties.getProperty(prefix + "expireAfterAccess");
        String expireAfterWrite = properties.getProperty(prefix + "expireAfterWrite");
        String initialCapacity = properties.getProperty(prefix + "initialCapacity");
        // 使用链式调用构建CacheBuilder
        this.cache = CacheBuilder.newBuilder()
                .maximumSize(StringKit.isNotEmpty(maximumSize) ? Long.parseLong(maximumSize) : 1000)
                .expireAfterAccess(Long.parseLong(expireAfterAccess), TimeUnit.MILLISECONDS)
                .expireAfterWrite(Long.parseLong(expireAfterWrite), TimeUnit.MILLISECONDS)
                .initialCapacity(Integer.parseInt(initialCapacity)).build(new CacheLoader<>() {
                    @Override
                    public Object load(Object key) {
                        return null;
                    }
                });
    }

    /**
     * 构造函数（支持异步加载）
     * <p>
     * 使用指定的大小、过期时间和缓存加载器创建缓存实例
     * </p>
     *
     * @param size        最大缓存条目数
     * @param expire      过期时间（毫秒）
     * @param cacheLoader 缓存加载器
     */
    public GuavaCache(long size, long expire, CacheLoader<K, V> cacheLoader) {
        this.cache = CacheBuilder.newBuilder()
                // 设置最大缓存条目数
                .maximumSize(size)
                // 设置写入后过期时间
                .expireAfterWrite(expire, TimeUnit.MILLISECONDS)
                // 设置缓存加载器
                .build(cacheLoader);
    }

    /**
     * 从缓存中读取单个值
     *
     * @param key 键
     * @return 值，如果不存在则返回null
     */
    @Override
    public V read(K key) {
        return this.cache.getIfPresent(key);
    }

    /**
     * 从缓存中批量读取值
     *
     * @param keys 键集合
     * @return 键值映射
     */
    @Override
    public Map<K, V> read(Collection<K> keys) {
        return this.cache.getAllPresent(keys);
    }

    /**
     * 向缓存中写入单个键值对
     *
     * @param key    键
     * @param value  值
     * @param expire 过期时间（毫秒）
     */
    @Override
    public void write(K key, V value, long expire) {
        this.cache.put(key, value);
    }

    /**
     * 向缓存中批量写入键值对
     *
     * @param keyValueMap 键值映射
     * @param expire      过期时间（毫秒）
     */
    @Override
    public void write(Map<K, V> keyValueMap, long expire) {
        this.cache.putAll(keyValueMap);
    }

    /**
     * 从缓存中移除指定的键
     *
     * @param keys 要移除的键
     */
    @Override
    public void remove(K... keys) {
        this.cache.invalidateAll(Arrays.asList(keys));
    }

    /**
     * 清空缓存
     */
    @Override
    public void clear() {
        this.cache.cleanUp();
    }

}