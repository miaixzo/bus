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
 * Represents an object stored in the cache, containing the key, value, and metadata.
 *
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 * @author Kimi Liu
 * @since Java 17+
 */
public class CacheObject<K, V> implements Serializable {

    @Serial
    private static final long serialVersionUID = 2852231019717L;

    /**
     * The key of the cached object.
     */
    protected final K key;
    /**
     * The value of the cached object.
     */
    protected final V object;
    /**
     * The Time-To-Live (TTL) for this object in milliseconds. A value of 0 means it never expires.
     */
    protected final long ttl;
    /**
     * The timestamp of the last access to this object.
     */
    protected volatile long lastAccess;
    /**
     * The number of times this object has been accessed.
     */
    protected AtomicLong accessCount = new AtomicLong();

    /**
     * Constructs a new cache object.
     *
     * @param key    The key.
     * @param object The value.
     * @param ttl    The Time-To-Live (TTL) in milliseconds.
     */
    protected CacheObject(final K key, final V object, final long ttl) {
        this.key = key;
        this.object = object;
        this.ttl = ttl;
        this.lastAccess = System.currentTimeMillis();
    }

    /**
     * Gets the key of the cached object.
     *
     * @return The key.
     */
    public K getKey() {
        return this.key;
    }

    /**
     * Gets the value of the cached object.
     *
     * @return The value.
     */
    public V getValue() {
        return this.object;
    }

    /**
     * Gets the Time-To-Live (TTL) for this object. A value of 0 indicates it is permanent.
     *
     * @return The TTL in milliseconds.
     */
    public long getTtl() {
        return this.ttl;
    }

    /**
     * Gets the expiration time for this object.
     *
     * @return The expiration time as a {@link Date}, or {@code null} if the object never expires.
     */
    public Date getExpiredTime() {
        if (this.ttl > 0) {
            return DateKit.date(this.lastAccess + this.ttl);
        }
        return null;
    }

    /**
     * Gets the timestamp of the last access.
     *
     * @return The last access time in milliseconds.
     */
    public long getLastAccess() {
        return this.lastAccess;
    }

    @Override
    public String toString() {
        return "CacheObject [key=" + key + ", value=" + object + ", lastAccess=" + lastAccess + ", accessCount="
                + accessCount + ", ttl=" + ttl + "]";
    }

    /**
     * Checks if the cached object has expired.
     *
     * @return {@code true} if the object has expired, otherwise {@code false}.
     */
    protected boolean isExpired() {
        if (this.ttl > 0) {
            // This check does not account for system time being moved backward.
            return (System.currentTimeMillis() - this.lastAccess) > this.ttl;
        }
        return false;
    }

    /**
     * Retrieves the value and optionally updates the last access time.
     *
     * @param isUpdateLastAccess Whether to update the last access time.
     * @return The cached value.
     */
    protected V get(final boolean isUpdateLastAccess) {
        if (isUpdateLastAccess) {
            lastAccess = System.currentTimeMillis();
        }
        accessCount.getAndIncrement();
        return this.object;
    }

}
