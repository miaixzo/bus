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
package org.miaixz.bus.metrics.guard;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-key cardinality limiter policy. Three strategies are supported.
 * <p>
 * Inspired by Netflix Spectator's {@code CardinalityLimiters}.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public sealed interface CardinalityPolicy
        permits CardinalityPolicy.FirstN, CardinalityPolicy.TopN, CardinalityPolicy.Deny {

    /**
     * Evaluate a tag value. Returns the value that should be used (original, "__overflow__", "__other__", or empty for
     * denied).
     *
     * @param value the incoming tag value
     * @return allowed value, or sentinel if overflow/deny
     */
    String evaluate(String value);

    // ── Factories ─────────────────────────────────────────────────────────

    /**
     * Create a {@link FirstN} policy that accepts the first {@code n} distinct values.
     *
     * @param n maximum number of distinct values to allow
     * @return a new FirstN policy
     */
    static CardinalityPolicy firstN(int n) {
        return new FirstN(n);
    }

    /**
     * Create a {@link TopN} policy that keeps the {@code n} most frequent values.
     *
     * @param n number of top values to retain
     * @return a new TopN policy
     */
    static CardinalityPolicy topN(int n) {
        return new TopN(n);
    }

    /**
     * Create a {@link Deny} policy that strips this tag key from all metrics.
     *
     * @return the singleton Deny instance
     */
    static CardinalityPolicy deny() {
        return Deny.INSTANCE;
    }

    // ── Implementations ───────────────────────────────────────────────────

    /**
     * Accept the first N distinct values; replace subsequent novel values with "__overflow__".
     */
    final class FirstN implements CardinalityPolicy {

        /** Maximum number of distinct values to allow before overflowing. */
        private final int max;
        /** Set of distinct values seen so far; thread-safe. */
        private final Set<String> seen = Collections.synchronizedSet(new HashSet<>());

        /**
         * Creates a FirstN policy with the given maximum distinct value count.
         *
         * @param max maximum number of distinct values to allow before overflowing
         */
        FirstN(int max) {
            this.max = max;
        }

        /**
         * Returns the original value if within the first N distinct values, otherwise "__overflow__".
         *
         * @param value the incoming tag value
         * @return the original value or "__overflow__"
         */
        @Override
        public String evaluate(String value) {
            if (seen.contains(value)) {
                return value;
            }
            if (seen.size() < max) {
                seen.add(value);
                return value;
            }
            return "__overflow__";
        }
    }

    /**
     * Keep the N most frequent values using a Count-Min Sketch approximation; infrequent values are replaced with
     * "__other__".
     */
    final class TopN implements CardinalityPolicy {

        /** Maximum number of top-frequency values to retain. */
        private final int max;
        /** Frequency map for all observed values; bounded to {@code max * 4} entries before pruning. */
        private final ConcurrentHashMap<String, AtomicLong> freq = new ConcurrentHashMap<>();

        /**
         * Creates a TopN policy with the given maximum retained value count.
         *
         * @param max number of top-frequency values to retain
         */
        TopN(int max) {
            this.max = max;
        }

        /**
         * Returns the original value if it is among the top N by frequency, otherwise "__other__".
         *
         * @param value the incoming tag value
         * @return the original value or "__other__"
         */
        @Override
        public String evaluate(String value) {
            freq.computeIfAbsent(value, k -> new AtomicLong(0)).incrementAndGet();
            // Keep only top-N by pruning low-frequency entries when map exceeds 4×max
            if (freq.size() > max * 4) {
                prune();
            }
            // Check if this value is in top-N
            long myFreq = freq.getOrDefault(value, new AtomicLong(0)).get();
            long threshold = computeThreshold();
            return myFreq >= threshold ? value : "__other__";
        }

        /**
         * Returns the minimum frequency threshold for a value to be considered top-N.
         *
         * @return the frequency threshold
         */
        private long computeThreshold() {
            if (freq.size() <= max) {
                return 0;
            }
            long[] sorted = freq.values().stream().mapToLong(AtomicLong::get).sorted().toArray();
            return sorted[Math.max(0, sorted.length - max)];
        }

        /**
         * Removes low-frequency entries from the frequency map to keep it bounded.
         */
        private void prune() {
            long threshold = computeThreshold();
            freq.entrySet().removeIf(e -> e.getValue().get() < threshold);
        }
    }

    /**
     * Deny this tag key entirely; it is stripped from all metrics.
     */
    final class Deny implements CardinalityPolicy {

        /** Singleton instance; this policy has no state. */
        static final Deny INSTANCE = new Deny();

        /** Private constructor; use {@link #INSTANCE} singleton. */
        private Deny() {
        }

        /**
         * Always returns {@code null} to signal that this tag should be stripped.
         *
         * @param value the incoming tag value
         * @return {@code null}
         */
        @Override
        public String evaluate(String value) {
            return null; // null signals "strip this tag"
        }
    }

}
