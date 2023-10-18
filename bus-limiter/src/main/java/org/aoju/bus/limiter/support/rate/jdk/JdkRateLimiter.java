/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.limiter.support.rate.jdk;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.limiter.support.rate.RateLimiter;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 一个RateLimiter组件
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkRateLimiter extends RateLimiter {

    private String rateLimiterName;

    private LoadingCache<CacheKey, RateLimiterObject>
            cache;

    /**
     * @param rateLimiterName       名称
     * @param expireAfterAccess     过期时间
     * @param expireAfterAccessUnit 过期数量
     */
    public JdkRateLimiter(String rateLimiterName, long expireAfterAccess, TimeUnit expireAfterAccessUnit) {
        this.rateLimiterName = rateLimiterName;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(expireAfterAccess, expireAfterAccessUnit)
                .concurrencyLevel(Normal._16)
                .build(new CacheLoader<CacheKey, RateLimiterObject>() {
                    @Override
                    public RateLimiterObject load(CacheKey key) {
                        return new RateLimiterObject();
                    }
                });
    }

    @Override
    public boolean acquire(Object key, double rate, long capacity) {
        CacheKey cacheKey = new CacheKey(key, rate, capacity);
        RateLimiterObject rateLimiterObject = cache.getUnchecked(cacheKey);
        return rateLimiterObject.tryAcquire(1, rate, capacity);
    }

    @Override
    public String getLimiterName() {
        return rateLimiterName;
    }

    private static class CacheKey {

        private Object key;
        private double rate;
        private long capacity;

        public CacheKey(Object key, double rate, long capacity) {
            this.key = key;
            this.rate = rate;
            this.capacity = capacity;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (null == o || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Double.compare(cacheKey.rate, rate) == 0 &&
                    capacity == cacheKey.capacity &&
                    Objects.equals(key, cacheKey.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, rate, capacity);
        }
    }

}
