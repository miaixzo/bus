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
 */
public interface CardinalityPolicy {

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
     * Creates a {@link TopN} compatibility policy that admits at most {@code n} direct values and maps the rest to a
     * shared sentinel.
     *
     * @param n number of direct values to retain
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

    /**
     * Evaluate a tag value. Returns the value that should be used (original, "__overflow__", "__other__", or empty for
     * denied).
     *
     * @param value the incoming tag value
     * @return allowed value, or sentinel if overflow/deny
     */
    String evaluate(String value);

    /**
     * Accept the first N distinct values; replace subsequent novel values with "__overflow__".
     *
     * @author Kimi Liu
     */
    class FirstN implements CardinalityPolicy {

        /**
         * Maximum number of distinct values to allow before overflowing.
         */
        private final int max;

        /**
         * Set of distinct values seen so far; thread-safe.
         */
        private final Set<String> seen = Collections.synchronizedSet(new HashSet<>());

        /**
         * Creates a FirstN policy with the given maximum distinct value count.
         *
         * @param max maximum number of distinct values to allow before overflowing
         * @throws IllegalArgumentException if {@code max} is not positive
         */
        public FirstN(int max) {
            if (max <= 0) {
                throw new IllegalArgumentException("FirstN maximum must be positive");
            }
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
            synchronized (seen) {
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

    }

    /**
     * Retains a bounded set of directly exported values and maps every value outside that admitted set to
     * {@code "__other__"}. Frequencies are tracked only for admitted values, so the policy never creates more than
     * {@code N + 1} exported values over its lifetime.
     *
     * @author Kimi Liu
     */
    class TopN implements CardinalityPolicy {

        /**
         * Maximum number of top-frequency values to retain.
         */
        private final int max;

        /**
         * Frequency counters for the bounded admitted value set.
         */
        private final ConcurrentHashMap<String, AtomicLong> freq = new ConcurrentHashMap<>();

        /**
         * Creates a TopN policy with the given maximum retained value count.
         *
         * @param max number of top-frequency values to retain
         * @throws IllegalArgumentException if {@code max} is not positive
         */
        public TopN(int max) {
            if (max <= 0) {
                throw new IllegalArgumentException("TopN maximum must be positive");
            }
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
            synchronized (freq) {
                AtomicLong existing = freq.get(value);
                if (existing != null) {
                    existing.incrementAndGet();
                    return value;
                }
                if (freq.size() < max) {
                    freq.put(value, new AtomicLong(1));
                    return value;
                }
                return "__other__";
            }
        }

    }

    /**
     * Deny this tag key entirely; it is stripped from all metrics.
     *
     * @author Kimi Liu
     */
    class Deny implements CardinalityPolicy {

        /**
         * Singleton instance; this policy has no state.
         */
        static final Deny INSTANCE = new Deny();

        /**
         * Creates a deny policy; {@link #INSTANCE} remains available as the shared form.
         */
        public Deny() {
            // No initialization required.
        }

        /**
         * Always returns {@code null} to signal that this tag should be stripped.
         *
         * @param value the incoming tag value
         * @return {@code null}
         */
        @Override
        public String evaluate(String value) {
            return null;
        }

    }

}
