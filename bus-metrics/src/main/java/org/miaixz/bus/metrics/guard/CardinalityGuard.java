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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Global cardinality guard that prevents metric tag explosion (and resulting OOM).
 * <p>
 * Inspired by Netflix Spectator's {@code CardinalityLimiters}. Apply policies by tag key, either programmatically or
 * via Spring configuration ({@code bus.metrics.cardinality}).
 * <p>
 * All policy lookups are thread-safe and zero-allocation in the hot path when no violation occurs.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CardinalityGuard {

    /**
     * Per-tag-key cardinality policies; auto-populated with firstN(defaultMax) on first access.
     */
    private static final ConcurrentHashMap<String, CardinalityPolicy> POLICIES = new ConcurrentHashMap<>();
    /**
     * Default maximum distinct tag values when no explicit policy is registered.
     */
    private static volatile int defaultMax = Builder.CARDINALITY_DEFAULT_MAX;
    /**
     * Per-tag-key cumulative violation counters for self-monitoring.
     */
    private static final ConcurrentHashMap<String, AtomicLong> VIOLATION_COUNTS = new ConcurrentHashMap<>();
    /**
     * Throttle log output to at most once per 60 seconds per tag key.
     */
    private static final ConcurrentHashMap<String, Long> LAST_LOG_TS = new ConcurrentHashMap<>();

    /** Private constructor; this is a static utility class. */
    private CardinalityGuard() {
    }

    /**
     * Register a policy for the given tag key.
     */
    public static void policy(String tagKey, CardinalityPolicy policy) {
        POLICIES.put(tagKey, policy);
    }

    /**
     * Set the default maximum number of distinct tag values allowed when no explicit policy is registered.
     *
     * @param max maximum distinct values; defaults to {@link Builder#CARDINALITY_DEFAULT_MAX}
     */
    public static void setDefaultMax(int max) {
        defaultMax = max;
    }

    /**
     * Enforce all registered policies on the given tag array. Tags that are denied (policy = deny) are dropped. Tags
     * with no registered policy are accepted as-is (up to {@code defaultMax} distinct values via an implicit firstN
     * policy).
     *
     * @param metricName metric name (used for logging)
     * @param tags       input tags
     * @return filtered tags (may be shorter than input if tags are denied)
     */
    public static Tag[] enforce(String metricName, Tag[] tags) {
        if (tags == null || tags.length == 0) {
            return tags;
        }
        List<Tag> result = new ArrayList<>(tags.length);
        for (Tag tag : tags) {
            CardinalityPolicy pol = POLICIES.computeIfAbsent(tag.key(), k -> CardinalityPolicy.firstN(defaultMax));
            String allowed = pol.evaluate(tag.value());
            if (allowed == null) {
                // deny policy — strip tag entirely
                recordViolation(metricName, tag.key(), tag.value(), "denied");
            } else if (!allowed.equals(tag.value())) {
                recordViolation(metricName, tag.key(), tag.value(), allowed);
                result.add(Tag.of(tag.key(), allowed));
            } else {
                result.add(tag);
            }
        }
        return result.toArray(new Tag[0]);
    }

    /**
     * Returns the total number of cardinality violations recorded for the given tag key.
     *
     * @param tagKey the tag key to query
     * @return cumulative violation count, or 0 if no violations have occurred
     */
    public static long violationCount(String tagKey) {
        AtomicLong c = VIOLATION_COUNTS.get(tagKey);
        return c == null ? 0 : c.get();
    }

    /**
     * Records a cardinality violation, increments the counter, and logs a throttled warning.
     *
     * @param metricName metric name for log context
     * @param tagKey     the tag key that triggered the violation
     * @param original   the original tag value
     * @param replaced   the replacement sentinel value
     */
    private static void recordViolation(String metricName, String tagKey, String original, String replaced) {
        VIOLATION_COUNTS.computeIfAbsent(tagKey, k -> new AtomicLong()).incrementAndGet();
        long now = System.currentTimeMillis();
        Long last = LAST_LOG_TS.get(tagKey);
        if (last == null || now - last > Builder.CARDINALITY_LOG_THROTTLE_MS) {
            LAST_LOG_TS.put(tagKey, now);
            Logger.warn(
                    "[bus-metrics] Cardinality violation on metric={} tagKey={} value={} -> {}",
                    metricName,
                    tagKey,
                    original,
                    replaced);
        }
    }

}
