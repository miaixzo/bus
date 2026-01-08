/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
     * The rate at which tokens are generated per second (requests per second).
     */
    private int tokenCount;

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
                    rateLimiter = RateLimiter.create(tokenCount);
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
     * @throws ValidateException with error code {@link ErrorCode#_100505} if no permit is available.
     */
    public void acquire() {
        if (!fetchRateLimiter().tryAcquire()) {
            throw new ValidateException(ErrorCode._100505);
        }
    }

}
