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
package org.miaixz.bus.core.cache.provider;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.core.xyz.DateKit;

/**
 * 缓存对象
 *
 * @param <K> Key类型
 * @param <V> Value类型
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheObject<K, V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852238899708L;

    /**
     * 键
     */
    protected final K key;
    /**
     * 值对象
     */
    protected final V object;
    /**
     * 对象存活时长，0表示永久存活
     */
    protected final long ttl;
    /**
     * 上次访问时间
     */
    protected volatile long lastAccess;
    /**
     * 访问次数
     */
    protected AtomicLong accessCount = new AtomicLong();

    /**
     * 构造
     *
     * @param key    键
     * @param object 值
     * @param ttl    超时时长
     */
    protected CacheObject(final K key, final V object, final long ttl) {
        this.key = key;
        this.object = object;
        this.ttl = ttl;
        this.lastAccess = System.currentTimeMillis();
    }

    /**
     * 获取键
     *
     * @return 键
     */
    public K getKey() {
        return this.key;
    }

    /**
     * 获取值
     *
     * @return 值
     */
    public V getValue() {
        return this.object;
    }

    /**
     * 获取对象存活时长，即超时总时长，0表示无限
     *
     * @return 对象存活时长
     */
    public long getTtl() {
        return this.ttl;
    }

    /**
     * 获取过期时间，返回{@code null}表示永不过期
     *
     * @return 此对象的过期时间，返回{@code null}表示永不过期
     */
    public Date getExpiredTime() {
        if (this.ttl > 0) {
            return DateKit.date(this.lastAccess + this.ttl);
        }
        return null;
    }

    /**
     * 获取上次访问时间
     *
     * @return 上次访问时间
     */
    public long getLastAccess() {
        return this.lastAccess;
    }

    @Override
    public String toString() {
        return "CacheObject [data=" + key + ", object=" + object + ", lastAccess=" + lastAccess + ", accessCount="
                + accessCount + ", ttl=" + ttl + "]";
    }

    /**
     * 判断是否过期
     *
     * @return 是否过期
     */
    protected boolean isExpired() {
        if (this.ttl > 0) {
            // 此处不考虑时间回拨
            return (System.currentTimeMillis() - this.lastAccess) > this.ttl;
        }
        return false;
    }

    /**
     * 获取值
     *
     * @param isUpdateLastAccess 是否更新最后访问时间
     * @return 获得对象
     */
    protected V get(final boolean isUpdateLastAccess) {
        if (isUpdateLastAccess) {
            lastAccess = System.currentTimeMillis();
        }
        accessCount.getAndIncrement();
        return this.object;
    }

}
