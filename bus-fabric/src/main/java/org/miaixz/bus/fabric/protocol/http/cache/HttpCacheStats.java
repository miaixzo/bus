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
package org.miaixz.bus.fabric.protocol.http.cache;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable HTTP cache statistics snapshot.
 *
 * @param requestCount      cache strategy request count
 * @param networkCount      network response count
 * @param hitCount          cache hit count
 * @param missCount         cache miss count
 * @param corruptionCount   corrupt candidate count
 * @param writeSuccessCount cache body write success count
 * @param writeAbortCount   cache body write abort count
 * @param writeFailureCount cache write failure count
 * @author Kimi Liu
 * @since Java 21+
 */
public record HttpCacheStats(long requestCount, long networkCount, long hitCount, long missCount, long corruptionCount,
        long writeSuccessCount, long writeAbortCount, long writeFailureCount) {

    /**
     * Creates a validated stats snapshot.
     *
     * @param requestCount      cache strategy requests
     * @param networkCount      network responses
     * @param hitCount          cache hits
     * @param missCount         cache misses
     * @param corruptionCount   corrupt candidates
     * @param writeSuccessCount completed cache writes
     * @param writeAbortCount   intentionally aborted cache writes
     * @param writeFailureCount failed cache writes
     */
    public HttpCacheStats {
        requestCount = nonNegative(requestCount, "Request count");
        networkCount = nonNegative(networkCount, "Network count");
        hitCount = nonNegative(hitCount, "Hit count");
        missCount = nonNegative(missCount, "Miss count");
        corruptionCount = nonNegative(corruptionCount, "Corruption count");
        writeSuccessCount = nonNegative(writeSuccessCount, "Write success count");
        writeAbortCount = nonNegative(writeAbortCount, "Write abort count");
        writeFailureCount = nonNegative(writeFailureCount, "Write failure count");
    }

    /**
     * Creates a stats snapshot.
     *
     * @param requestCount      request count
     * @param networkCount      network count
     * @param hitCount          hit count
     * @param missCount         miss count
     * @param corruptionCount   corruption count
     * @param writeSuccessCount write success count
     * @param writeAbortCount   write abort count
     * @param writeFailureCount write failure count
     * @return stats
     */
    public static HttpCacheStats of(
            final long requestCount,
            final long networkCount,
            final long hitCount,
            final long missCount,
            final long corruptionCount,
            final long writeSuccessCount,
            final long writeAbortCount,
            final long writeFailureCount) {
        return new HttpCacheStats(requestCount, networkCount, hitCount, missCount, corruptionCount, writeSuccessCount,
                writeAbortCount, writeFailureCount);
    }

    /**
     * Validates cache counters before publishing an immutable stats snapshot.
     *
     * @param value counter value
     * @param name  counter name used in validation messages
     * @return validated counter value
     */
    private static long nonNegative(final long value, final String name) {
        Assert.isTrue(value >= 0, () -> new ValidateException(name + " must be non-negative"));
        return value;
    }

}
