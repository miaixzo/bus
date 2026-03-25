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
package org.miaixz.bus.cache.collect;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.miaixz.bus.cache.Collector;

/**
 * An in-memory implementation of {@link Collector} for cache hit rate statistics.
 * <p>
 * This class uses {@link ConcurrentHashMap} to store hit and request counts, making it suitable for concurrent updates
 * in a single-node or testing environment.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class MemoryCollector implements Collector {

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
        long totalHit = 0;
        long totalRequired = 0;

        // Snapshot requireMap first, then read the corresponding hit counter.
        // New entries added concurrently after this point are simply not included in this snapshot.
        for (Map.Entry<String, AtomicLong> entry : requireMap.entrySet()) {
            String pattern = entry.getKey();
            long require = entry.getValue().get();
            AtomicLong hitCounter = hitMap.get(pattern);
            long hit = hitCounter != null ? hitCounter.get() : 0L;
            totalHit += hit;
            totalRequired += require;
            result.put(pattern, Snapshot.newInstance(hit, require));
        }

        // Add the global hit rate statistics.
        result.put(summaryName(), Snapshot.newInstance(totalHit, totalRequired));
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
