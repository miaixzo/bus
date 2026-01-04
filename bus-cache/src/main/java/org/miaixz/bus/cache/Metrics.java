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
package org.miaixz.bus.cache;

import java.util.Map;

/**
 * An interface for tracking cache hit rate statistics.
 * <p>
 * This defines the core operations for cache metrics, including recording request counts, hit counts, retrieving
 * statistics, and resetting counters. It provides hit rate statistics for cache patterns or groups, suitable for
 * monitoring cache performance.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Metrics {

    /**
     * Increments the total number of requests for a specific cache pattern.
     * <p>
     * This is used to track how frequently a cached method or group is accessed. Example code:
     * </p>
     * 
     * <pre>{@code
     * Metrics metrics = new SomeMetricsImpl();
     * metrics.reqIncr("userCache", 1);
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
     * Metrics metrics = new SomeMetricsImpl();
     * metrics.hitIncr("userCache", 1);
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
     * Metrics metrics = new SomeMetricsImpl();
     * Map<String, Snapshot> stats = metrics.getHitting();
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
     * Metrics metrics = new SomeMetricsImpl();
     * metrics.reset("userCache");
     * }</pre>
     *
     * @param pattern The name of the cache pattern or group to reset.
     */
    void reset(String pattern);

    /**
     * Resets the statistics for all cache patterns.
     * <p>
     * This clears all hit and request counts tracked by this metrics instance. Example code:
     * </p>
     * 
     * <pre>{@code
     * Metrics metrics = new SomeMetricsImpl();
     * metrics.resetAll();
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
     * Metrics metrics = new SomeMetricsImpl();
     * String summary = metrics.summaryName();
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
         * Snapshot merged = Snapshot.mergeShootingDO(snapshot1, snapshot2);
         * System.out.println("Merged Hit Rate: " + merged.getRate());
         * }</pre>
         *
         * @param do1 The first snapshot to merge.
         * @param do2 The second snapshot to merge.
         * @return A new {@link Snapshot} representing the merged statistics.
         */
        public static Snapshot mergeShootingDO(Snapshot do1, Snapshot do2) {
            long hit = do1.getHit() + do2.getHit();
            long required = do1.getRequired() + do2.getRequired();
            return newInstance(hit, required);
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
