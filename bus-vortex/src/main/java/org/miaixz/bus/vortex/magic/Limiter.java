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
package org.miaixz.bus.vortex.magic;

import com.google.common.util.concurrent.RateLimiter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Rate limiter class, implementing request rate limiting based on the token bucket algorithm.
 *
 * @author Justubborn
 * @since Java 17+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@RequiredArgsConstructor
public class Limiter {

    /**
     * The IP address of the request source.
     */
    private String ip;

    /**
     * The name of the request method.
     */
    private String method;

    /**
     * The version number of the request.
     */
    private String version;

    /**
     * The token generation rate of the token bucket (tokens generated per second).
     */
    private int tokenCount;

    /**
     * The {@link RateLimiter} instance, implementing rate limiting using Google Guava's RateLimiter.
     */
    private volatile RateLimiter rateLimiter;

    /**
     * Initializes the token bucket, setting the token generation rate. Uses {@code synchronized} to ensure thread
     * safety during initialization.
     */
    public synchronized void initRateLimiter() {
        rateLimiter = RateLimiter.create(tokenCount);
    }

    /**
     * Retrieves the {@link RateLimiter} instance, using double-checked locking to ensure thread safety.
     *
     * @return The {@link RateLimiter} instance.
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
     * Attempts to acquire a token, blocking until successful.
     *
     * @return The waiting time (in seconds) to acquire the token.
     */
    public double acquire() {
        return fetchRateLimiter().acquire();
    }

}
