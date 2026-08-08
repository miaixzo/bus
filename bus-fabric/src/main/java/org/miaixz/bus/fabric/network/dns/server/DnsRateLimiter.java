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
package org.miaixz.bus.fabric.network.dns.server;

import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

import org.miaixz.bus.core.lang.Normal;

/**
 * Per-client fixed-window DNS query rate limiter used before resolution.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class DnsRateLimiter {

    /**
     * Disabled rate limiter instance.
     */
    private static final DnsRateLimiter DISABLED = new DnsRateLimiter(0);

    /**
     * Maximum queries allowed per client per second, or zero when disabled.
     */
    private final int maxPerSecond;

    /**
     * Per-client counters.
     */
    private final ConcurrentHashMap<String, Counter> counters;

    /**
     * Creates a rate limiter.
     *
     * @param maxPerSecond maximum queries per client per second, or zero to disable
     */
    DnsRateLimiter(final int maxPerSecond) {
        this.maxPerSecond = maxPerSecond;
        this.counters = new ConcurrentHashMap<>();
    }

    /**
     * Returns a disabled rate limiter.
     *
     * @return disabled rate limiter
     */
    static DnsRateLimiter disabled() {
        return DISABLED;
    }

    /**
     * Returns whether a query is allowed for a client.
     *
     * @param clientAddress client address, or {@code null} when unavailable
     * @return true when the query is allowed
     */
    boolean allow(final InetAddress clientAddress) {
        if (maxPerSecond <= 0) {
            return true;
        }
        final String key = clientAddress == null ? Normal.EMPTY : clientAddress.getHostAddress();
        return counters.computeIfAbsent(key, ignored -> new Counter()).allow(maxPerSecond, System.currentTimeMillis());
    }

    /**
     * Clears all client counters.
     */
    void clear() {
        counters.clear();
    }

    /**
     * Per-client fixed-window counter.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class Counter {

        /**
         * Current fixed-window epoch second.
         */
        private long second;

        /**
         * Count observed inside the current fixed window.
         */
        private int count;

        /**
         * Creates an empty counter.
         */
        private Counter() {
            // No initialization required.
        }

        /**
         * Returns whether another query is allowed in this fixed window.
         *
         * @param limit         maximum allowed count per second
         * @param currentMillis current epoch millis
         * @return true when the query is allowed
         */
        private synchronized boolean allow(final int limit, final long currentMillis) {
            final long currentSecond = currentMillis / 1000L;
            if (currentSecond != second) {
                second = currentSecond;
                count = 0;
            }
            if (count >= limit) {
                return false;
            }
            count++;
            return true;
        }

    }

}
