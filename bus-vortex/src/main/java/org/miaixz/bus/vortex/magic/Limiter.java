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
package org.miaixz.bus.vortex.magic;

import com.google.common.util.concurrent.RateLimiter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.vortex.strategy.LimiterStrategy;

/**
 * A rate limiter that encapsulates Google Guava's {@link RateLimiter} to provide a non-blocking, fail-fast mechanism
 * suitable for a reactive environment.
 * <p>
 * This class implements the token bucket algorithm. It is designed to be used within the gateway's strategy chain to
 * enforce rate limits on APIs. Instead of blocking the calling thread when no tokens are available, its
 * {@link #acquire()} method throws an exception, allowing the request to be rejected quickly with a "Too Many Requests"
 * error.
 *
 * @author Kimi Liu
 * @see com.google.common.util.concurrent.RateLimiter
 * @see LimiterStrategy
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Limiter {

    /**
     * The IP address of the request source. Used for creating per-IP limiters.
     */
    private String ip;

    /**
     * The name of the API method being limited.
     */
    private String method;

    /**
     * The version of the API method being limited.
     */
    private String version;

    /**
     * The maximum number of complete requests allowed per minute.
     */
    private Integer throttle;

    /**
     * The underlying Guava RateLimiter instance. It is volatile and lazily initialized to ensure thread safety.
     */
    private volatile RateLimiter rateLimiter;

    /**
     * Lazily initializes and retrieves the underlying {@link RateLimiter} instance using thread-safe double-checked
     * locking.
     *
     * @return The singleton {@link RateLimiter} instance for this limiter configuration.
     */
    public RateLimiter fetchRateLimiter() {
        if (null == rateLimiter) {
            synchronized (this) {
                if (null == rateLimiter) {
                    rateLimiter = RateLimiter.create(throttle / 60D);
                }
            }
        }
        return rateLimiter;
    }

    /**
     * Attempts to acquire a permit from the rate limiter in a non-blocking manner.
     * <p>
     * This method uses {@code tryAcquire()}, which returns immediately. If no permit is available, it does not wait.
     * Instead, it throws a {@link ValidateException}, signaling that the request has been rate-limited.
     *
     * @throws ValidateException with error code {@link ErrorCode#_LIMITER} if no permit is available.
     */
    public void acquire() {
        if (!fetchRateLimiter().tryAcquire()) {
            throw new ValidateException(ErrorCode._LIMITER);
        }
    }

}
