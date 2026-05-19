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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.vortex.strategy.LimiterStrategy;

/**
 * A rate limiter that provides a non-blocking, fail-fast token bucket mechanism suitable for a reactive environment.
 * <p>
 * This class implements the token bucket algorithm. It is designed to be used within the gateway's strategy chain to
 * enforce rate limits on APIs. Instead of blocking the calling thread when no tokens are available, its
 * {@link #acquire()} method throws an exception, allowing the request to be rejected quickly with a "Too Many Requests"
 * error.
 *
 * @see LimiterStrategy
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public class Limiter {

    /**
     * Creates an empty limiter definition.
     */
    public Limiter() {
        // No initialization required.
    }

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
     * The underlying token bucket instance. It is volatile and lazily initialized to ensure thread safety.
     */
    private volatile RateLimiter rateLimiter;

    /**
     * Lazily initializes and retrieves the underlying {@link RateLimiter} instance using thread-safe double-checked locking.
     *
     * @return the singleton {@link RateLimiter} instance for this limiter configuration
     */
    public RateLimiter fetchRateLimiter() {
        if (null == rateLimiter) {
            synchronized (this) {
                if (null == rateLimiter) {
                    rateLimiter = RateLimiter.create(throttle);
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

    /**
     * Lightweight token bucket rate limiter for per-minute request quotas.
     * <p>
     * The limiter stores up to one second of permits, matching the previous smooth-burst behavior closely while avoiding a
     * direct Guava dependency. The implementation is synchronized because each limiter instance is shared by concurrent
     * requests and the critical section contains only a few arithmetic operations.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public static final class RateLimiter {

        /**
         * Number of nanoseconds in one second.
         */
        private static final double NANOS_PER_SECOND = 1_000_000_000D;

        /**
         * Refill rate expressed as permits per second.
         */
        private final double permitsPerSecond;

        /**
         * Maximum stored permits retained after idle time.
         */
        private final double maxPermits;

        /**
         * Currently stored permits.
         */
        private double storedPermits;

        /**
         * Last refill timestamp from {@link System#nanoTime()}.
         */
        private long lastRefillNanos;

        /**
         * Creates a rate limiter from a per-minute quota.
         *
         * @param permitsPerMinute maximum number of permits per minute
         */
        private RateLimiter(int permitsPerMinute) {
            if (permitsPerMinute <= 0) {
                throw new IllegalArgumentException("permitsPerMinute must be greater than zero");
            }
            this.permitsPerSecond = permitsPerMinute / 60D;
            this.maxPermits = Math.max(1D, this.permitsPerSecond);
            this.storedPermits = this.maxPermits;
            this.lastRefillNanos = System.nanoTime();
        }

        /**
         * Creates a rate limiter from a per-minute quota.
         *
         * @param permitsPerMinute maximum number of permits per minute
         * @return a new rate limiter
         */
        public static RateLimiter create(int permitsPerMinute) {
            return new RateLimiter(permitsPerMinute);
        }

        /**
         * Attempts to acquire one permit without blocking.
         *
         * @return {@code true} when one permit is available
         */
        public synchronized boolean tryAcquire() {
            refill(System.nanoTime());
            if (storedPermits < 1D) {
                return false;
            }
            storedPermits -= 1D;
            return true;
        }

        /**
         * Refills stored permits according to elapsed time.
         *
         * @param nowNanos current monotonic timestamp
         */
        private void refill(long nowNanos) {
            if (nowNanos <= lastRefillNanos) {
                return;
            }
            double elapsedSeconds = (nowNanos - lastRefillNanos) / NANOS_PER_SECOND;
            storedPermits = Math.min(maxPermits, storedPermits + elapsedSeconds * permitsPerSecond);
            lastRefillNanos = nowNanos;
        }

    }

}
