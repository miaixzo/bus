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
package org.miaixz.bus.cache;

import java.util.Map;

/**
 * An interface for collecting and tracking cache hit rate statistics.
 * <p>
 * This defines the core operations for the cache statistics collector, including recording request counts, hit counts,
 * retrieving statistics, and resetting counters. It provides hit rate statistics for cache patterns or groups, suitable
 * for monitoring cache performance.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Collector {

    /**
     * Increments the total number of requests for a specific cache pattern.
     * <p>
     * This is used to track how frequently a cached method or group is accessed. Example code:
     * </p>
     *
     * <pre>{@code
     * Collector collector = new SomeCollectorImpl();
     * collector.reqIncr("userCache", 1);
     * }</pre>
     *
     * @param pattern The name of the cache pattern or group.
     * @param count   The number of requests to add.
     */
    void reqIncr(String pattern, int count);

    /**
     * Increments the number of cache hits for a specific cache pattern.
     * <p>
     * This is used to track how often a request results in a cache hit. Example code:
     * </p>
     *
     * <pre>{@code
     * Collector collector = new SomeCollectorImpl();
     * collector.hitIncr("userCache", 1);
     * }</pre>
     *
     * @param pattern The name of the cache pattern or group.
     * @param count   The number of hits to add.
     */
    void hitIncr(String pattern, int count);

    /**
     * Retrieves the current cache hit rate statistics.
     * <p>
     * Returns a map where the keys are the cache pattern names and the values are {@link Snapshot} objects containing
     * the statistics for that pattern. Example code:
     * </p>
     *
     * <pre>{@code
     * Collector collector = new SomeCollectorImpl();
     * Map<String, Snapshot> stats = collector.getHitting();
     * stats.forEach((pattern, snapshot) -> System.out.println(pattern + ": Hit Rate " + snapshot.getRate()));
     * }</pre>
     *
     * @return A map containing the cache patterns and their corresponding hit rate data.
     */
    Map<String, Snapshot> getHitting();

    /**
     * Resets the statistics for a specific cache pattern.
     * <p>
     * This clears the hit and request counts for the given pattern, effectively restarting the statistics collection.
     * Example code:
     * </p>
     *
     * <pre>{@code
     * Collector collector = new SomeCollectorImpl();
     * collector.reset("userCache");
     * }</pre>
     *
     * @param pattern The name of the cache pattern or group to reset.
     */
    void reset(String pattern);

    /**
     * Resets the statistics for all cache patterns.
     * <p>
     * This clears all hit and request counts tracked by this collector instance. Example code:
     * </p>
     *
     * <pre>{@code
     * Collector collector = new SomeCollectorImpl();
     * collector.resetAll();
     * }</pre>
     */
    void resetAll();

    /**
     * Gets the name used for the summary or global statistics.
     * <p>
     * Returns "全局" (Global in Chinese) if the system language is Chinese, otherwise returns "summary". Example code:
     * </p>
     *
     * <pre>{@code
     * Collector collector = new SomeCollectorImpl();
     * String summary = collector.summaryName();
     * System.out.println("Summary Name: " + summary);
     * }</pre>
     *
     * @return The name for the summary statistics. Returns "全局" for Chinese locale, "summary" otherwise.
     */
    default String summaryName() {
        return "zh".equalsIgnoreCase(System.getProperty("user.language")) ? "全局" : "summary";
    }

    /**
     * A data object representing a snapshot of cache statistics at a point in time.
     * <p>
     * It stores the number of hits, total requests, and the calculated hit rate percentage.
     * </p>
     */
    class Snapshot {

        /**
         * The number of cache hits.
         */
        private final long hit;

        /**
         * The total number of cache requests.
         */
        private final long required;

        /**
         * The hit rate, formatted as a string (e.g., "xx.x%").
         */
        private final String rate;

        /**
         * Constructs a new Snapshot instance.
         *
         * @param hit      The number of cache hits.
         * @param required The total number of cache requests.
         * @param rate     The pre-formatted hit rate string.
         */
        private Snapshot(long hit, long required, String rate) {
            this.hit = hit;
            this.required = required;
            this.rate = rate;
        }

        /**
         * Creates a new {@link Snapshot} instance from hit and request counts.
         * <p>
         * This factory method calculates the hit rate and formats it as a string. Example code:
         * </p>
         *
         * <pre>{@code
         * Snapshot snapshot = Snapshot.newInstance(50, 100);
         * System.out.println("Hit Rate: " + snapshot.getRate());
         * }</pre>
         *
         * @param hit      The number of cache hits.
         * @param required The total number of cache requests.
         * @return A new {@link Snapshot} instance.
         */
        public static Snapshot newInstance(long hit, long required) {
            double rate = (required == 0 ? 0.0 : hit * 100.0 / required);
            String rateStr = String.format("%.1f%s", rate, "%");
            return new Snapshot(hit, required, rateStr);
        }

        /**
         * Merges two {@link Snapshot} instances into a new one.
         * <p>
         * The hit and request counts from both snapshots are summed up to create a combined snapshot. Example code:
         * </p>
         *
         * <pre>{@code
         * Snapshot snapshot1 = Snapshot.newInstance(50, 100);
         * Snapshot snapshot2 = Snapshot.newInstance(30, 50);
         * Snapshot merged = Snapshot.merge(snapshot1, snapshot2);
         * System.out.println("Merged Hit Rate: " + merged.getRate());
         * }</pre>
         *
         * @param s1 The first snapshot to merge.
         * @param s2 The second snapshot to merge.
         * @return A new {@link Snapshot} representing the merged statistics.
         */
        public static Snapshot merge(Snapshot s1, Snapshot s2) {
            return newInstance(s1.getHit() + s2.getHit(), s1.getRequired() + s2.getRequired());
        }

        /**
         * @deprecated Use {@link #merge(Snapshot, Snapshot)} instead.
         */
        @Deprecated
        public static Snapshot mergeShootingDO(Snapshot s1, Snapshot s2) {
            return merge(s1, s2);
        }

        /**
         * Gets the number of cache hits.
         *
         * @return The hit count.
         */
        public long getHit() {
            return hit;
        }

        /**
         * Gets the total number of cache requests.
         *
         * @return The request count.
         */
        public long getRequired() {
            return required;
        }

        /**
         * Gets the hit rate as a formatted string.
         * <p>
         * The format is typically "xx.x%".
         * </p>
         *
         * @return The formatted hit rate string.
         */
        public String getRate() {
            return rate;
        }
    }

}
