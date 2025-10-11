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
package org.miaixz.bus.cache.support.metrics;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.miaixz.bus.cache.Metrics;

/**
 * An in-memory implementation of {@link Metrics} for cache hit rate statistics.
 * <p>
 * This class uses {@link ConcurrentHashMap} to store hit and request counts, making it suitable for concurrent updates
 * in a single-node or testing environment.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MemoryMetrics implements Metrics {

    /**
     * A thread-safe map to store the hit counts for each cache pattern.
     */
    private final ConcurrentMap<String, AtomicLong> hitMap = new ConcurrentHashMap<>();

    /**
     * A thread-safe map to store the total request counts for each cache pattern.
     */
    private final ConcurrentMap<String, AtomicLong> requireMap = new ConcurrentHashMap<>();

    /**
     * Increments the hit count for a specific cache pattern.
     *
     * @param pattern The name of the cache pattern or group.
     * @param count   The number of hits to add.
     */
    @Override
    public void hitIncr(String pattern, int count) {
        hitMap.computeIfAbsent(pattern, (k) -> new AtomicLong()).addAndGet(count);
    }

    /**
     * Increments the request count for a specific cache pattern.
     *
     * @param pattern The name of the cache pattern or group.
     * @param count   The number of requests to add.
     */
    @Override
    public void reqIncr(String pattern, int count) {
        requireMap.computeIfAbsent(pattern, (k) -> new AtomicLong()).addAndGet(count);
    }

    /**
     * Retrieves a snapshot of the current cache hit rate statistics for all patterns.
     * <p>
     * It calculates the hit rate for each pattern and also provides a summary of the global hit rate.
     * </p>
     *
     * @return A map where keys are pattern names and values are {@link Snapshot} objects containing the statistics.
     */
    @Override
    public Map<String, Snapshot> getHitting() {
        Map<String, Snapshot> result = new LinkedHashMap<>();
        AtomicLong statisticsHit = new AtomicLong(0);
        AtomicLong statisticsRequired = new AtomicLong(0);

        // Iterate over the request map to calculate the hit rate for each pattern.
        requireMap.forEach((pattern, count) -> {
            long hit = hitMap.computeIfAbsent(pattern, (key) -> new AtomicLong(0)).get();
            long require = count.get();
            statisticsHit.addAndGet(hit);
            statisticsRequired.addAndGet(require);
            result.put(pattern, Snapshot.newInstance(hit, require));
        });

        // Add the global hit rate statistics.
        result.put(summaryName(), Snapshot.newInstance(statisticsHit.get(), statisticsRequired.get()));
        return result;
    }

    /**
     * Resets the hit and request counts for a specific cache pattern.
     *
     * @param pattern The name of the cache pattern or group to reset.
     */
    @Override
    public void reset(String pattern) {
        hitMap.remove(pattern);
        requireMap.remove(pattern);
    }

    /**
     * Resets all statistics, clearing all hit and request counts.
     */
    @Override
    public void resetAll() {
        hitMap.clear();
        requireMap.clear();
    }

}
