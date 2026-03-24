/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.cortex.guard;

import org.miaixz.bus.cache.CacheX;

/**
 * Token-bucket rate limiter backed by CacheX.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class RateLimiter {

    /**
     * Shared cache used to track request counters per time window.
     */
    private final CacheX<String, Object> cacheX;
    /**
     * Maximum number of requests allowed per second for a key.
     */
    private final int ratePerSecond;

    /**
     * Creates a RateLimiter backed by the given CacheX.
     *
     * @param cacheX        shared cache used to track request counts
     * @param ratePerSecond maximum number of requests allowed per second
     */
    public RateLimiter(CacheX<String, Object> cacheX, int ratePerSecond) {
        this.cacheX = cacheX;
        this.ratePerSecond = ratePerSecond;
    }

    /**
     * Attempts to acquire a permit for the given key within the current second.
     *
     * @param key rate limit key (e.g. client IP or service method)
     * @return true if the request is allowed, false if rate exceeded
     */
    public boolean tryAcquire(String key) {
        long second = System.currentTimeMillis() / 1000L;
        String rlKey = "rl:" + key + ":" + second;
        long count = cacheX.increment(rlKey);
        if (count == 1) {
            cacheX.write(rlKey, String.valueOf(count), 2000L);
        }
        return count <= ratePerSecond;
    }

}
