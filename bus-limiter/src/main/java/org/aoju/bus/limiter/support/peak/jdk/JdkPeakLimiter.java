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
package org.aoju.bus.limiter.support.peak.jdk;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.limiter.support.peak.PeakLimiter;

import java.util.Objects;
import java.util.concurrent.Semaphore;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class JdkPeakLimiter extends PeakLimiter {

    private String limiterName;

    private LoadingCache<CacheKey, Semaphore> cache;

    public JdkPeakLimiter(String limiterName) {
        this.limiterName = limiterName;
        this.cache = CacheBuilder.newBuilder()
                .concurrencyLevel(Normal._16)
                .initialCapacity(2048)
                .build(new CacheLoader<>() {
                    @Override
                    public Semaphore load(CacheKey key) {
                        return new Semaphore(key.max);
                    }
                });
    }

    @Override
    public boolean acquire(Object key, int max) {
        CacheKey cacheKey = new CacheKey(key, max);
        return cache.getIfPresent(cacheKey).tryAcquire();
    }

    @Override
    public void release(Object key, int max) {
        CacheKey cacheKey = new CacheKey(key, max);
        Semaphore semaphore = cache.getIfPresent(cacheKey);
        if (null != semaphore) {
            semaphore.release();
        }
    }

    @Override
    public String getLimiterName() {
        return limiterName;
    }

    public static class CacheKey {

        private Object key;

        private int max;

        public CacheKey(Object key, int max) {
            this.key = key;
            this.max = max;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (null == o || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return max == cacheKey.max &&
                    Objects.equals(key, cacheKey.key);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, max);
        }
    }

}
