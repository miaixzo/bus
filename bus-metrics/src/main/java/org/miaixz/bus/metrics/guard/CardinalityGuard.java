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

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.observe.tag.AttributeDescriptor;
import org.miaixz.bus.metrics.observe.tag.AttributeType;
import org.miaixz.bus.metrics.observe.tag.Attributes;
import org.miaixz.bus.metrics.observe.tag.Tag;

/**
 * Cardinality guard that prevents metric tag explosion (and resulting OOM).
 * <p>
 * Inspired by Netflix Spectator's {@code CardinalityLimiters}. Apply policies by tag key, either programmatically or
 * via Spring configuration ({@code bus.metrics.cardinality}).
 * <p>
 * The original static methods remain a compatibility facade over a global {@link Scope}. New provider instances can
 * receive an independent scope so policy state does not leak between application contexts.
 *
 * @author Kimi Liu
 */
public class CardinalityGuard {

    /**
     * Process-wide scope retained for the static compatibility facade.
     */
    private static final Scope GLOBAL = new Scope();

    /**
     * Keeps metric-cardinality policy enforcement on the static API.
     */
    public CardinalityGuard() {
        // No initialization required.
    }

    /**
     * Register a policy for the given tag key.
     *
     * @param tagKey tag key
     * @param policy cardinality policy
     */
    public static void policy(String tagKey, CardinalityPolicy policy) {
        GLOBAL.policy(tagKey, policy);
    }

    /**
     * Set the default maximum number of distinct tag values allowed when no explicit policy is registered.
     *
     * @param max maximum distinct values; defaults to {@link Builder#CARDINALITY_DEFAULT_MAX}
     */
    public static void setDefaultMax(int max) {
        GLOBAL.setDefaultMax(max);
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
        return GLOBAL.enforce(metricName, tags);
    }

    /**
     * Returns the total number of cardinality violations recorded for the given tag key.
     *
     * @param tagKey the tag key to query
     * @return cumulative violation count, or 0 if no violations have occurred
     */
    public static long violationCount(String tagKey) {
        return GLOBAL.violationCount(tagKey);
    }

    /**
     * Returns the scope used by the legacy static API.
     *
     * @return global compatibility scope
     */
    public static Scope globalScope() {
        return GLOBAL;
    }

    /**
     * Isolated, thread-safe cardinality policy state for one provider or application context.
     *
     * @author Kimi Liu
     */
    public static final class Scope {

        /**
         * Explicit and lazily created policies keyed by attribute name.
         */
        private final ConcurrentHashMap<String, CardinalityPolicy> policies = new ConcurrentHashMap<>();
        /**
         * Cumulative policy violations keyed by attribute name.
         */
        private final ConcurrentHashMap<String, AtomicLong> violationCounts = new ConcurrentHashMap<>();
        /**
         * Last warning timestamp keyed by attribute name.
         */
        private final ConcurrentHashMap<String, Long> lastLogTimestamps = new ConcurrentHashMap<>();
        /**
         * Listeners receiving immutable cardinality violation events.
         */
        private final CopyOnWriteArrayList<Consumer<CardinalityViolation>> violationListeners = new CopyOnWriteArrayList<>();

        /**
         * Default first-N limit for attributes without explicit policy.
         */
        private volatile int defaultMax;

        /**
         * Creates a scope using {@link Builder#CARDINALITY_DEFAULT_MAX}.
         */
        public Scope() {
            this(Builder.CARDINALITY_DEFAULT_MAX);
        }

        /**
         * Creates a scope using an explicit default limit.
         *
         * @param defaultMax default maximum distinct values per tag key
         */
        public Scope(int defaultMax) {
            setDefaultMax(defaultMax);
        }

        /**
         * Writes a validated string replacement through a wildcard descriptor.
         *
         * @param builder    destination builder
         * @param descriptor string descriptor
         * @param value      replacement value
         */
        @SuppressWarnings("unchecked")
        private static void putString(Attributes.Builder builder, AttributeDescriptor<?> descriptor, String value) {
            builder.put((AttributeDescriptor<String>) descriptor, value);
        }

        /**
         * Copies one typed value into an attribute builder.
         *
         * @param builder destination builder
         * @param value   typed value
         * @param <T>     attribute value type
         */
        private static <T> void putValue(Attributes.Builder builder, Attributes.Value<T> value) {
            builder.put(value.descriptor(), value.value());
        }

        /**
         * Registers a policy for one tag key.
         *
         * @param tagKey tag key
         * @param policy policy instance owned by this scope
         * @throws IllegalArgumentException if the tag key is blank
         */
        public void policy(String tagKey, CardinalityPolicy policy) {
            if (tagKey == null || tagKey.isBlank()) {
                throw new IllegalArgumentException("Tag key must not be blank");
            }
            policies.put(tagKey, Objects.requireNonNull(policy, "Cardinality policy must not be null"));
        }

        /**
         * Sets the implicit first-N limit.
         *
         * @param max maximum distinct values; must be positive
         * @throws IllegalArgumentException if {@code max} is not positive
         */
        public void setDefaultMax(int max) {
            if (max <= 0) {
                throw new IllegalArgumentException("Default cardinality maximum must be positive");
            }
            defaultMax = max;
        }

        /**
         * Applies this scope's policies to legacy tags.
         *
         * @param metricName metric name used for diagnostics
         * @param tags       input tags
         * @return filtered and normalized tags
         * @throws IllegalArgumentException if a non-empty tag array is supplied with a blank metric name
         */
        public Tag[] enforce(String metricName, Tag[] tags) {
            if (tags == null || tags.length == 0) {
                return tags;
            }
            if (metricName == null || metricName.isBlank()) {
                throw new IllegalArgumentException("Metric name must not be blank");
            }
            List<Tag> result = new ArrayList<>(tags.length);
            for (Tag tag : tags) {
                Objects.requireNonNull(tag, "Metric tag must not be null");
                CardinalityPolicy policy = policies
                        .computeIfAbsent(tag.key(), key -> CardinalityPolicy.firstN(defaultMax));
                String allowed = policy.evaluate(tag.value());
                if (allowed == null) {
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
         * Applies this scope's policies while preserving typed attribute values. A strict identity is rejected rather
         * than merged, and only string attributes may be replaced with overflow sentinels.
         *
         * @param metricName metric name used for diagnostics
         * @param attributes typed attributes
         * @return filtered and normalized attributes
         * @throws IllegalArgumentException if a non-empty collection has a blank metric name, a strict identity is
         *                                  rejected, or a non-string attribute requires a replacement sentinel
         */
        public Attributes enforce(String metricName, Attributes attributes) {
            Objects.requireNonNull(attributes, "Metric attributes must not be null");
            if (attributes.isEmpty()) {
                return attributes;
            }
            if (metricName == null || metricName.isBlank()) {
                throw new IllegalArgumentException("Metric name must not be blank");
            }
            Attributes.Builder result = Attributes.builder();
            for (Attributes.Value<?> entry : attributes.values()) {
                AttributeDescriptor<?> descriptor = entry.descriptor();
                String original = String.valueOf(entry.value());
                CardinalityPolicy policy = policies
                        .computeIfAbsent(descriptor.key(), key -> CardinalityPolicy.firstN(defaultMax));
                String allowed = policy.evaluate(original);
                if (allowed == null || !allowed.equals(original)) {
                    String replacement = allowed == null ? "denied" : allowed;
                    recordViolation(metricName, descriptor.key(), original, replacement);
                    if (descriptor.strictIdentity()) {
                        throw new IllegalArgumentException(
                                "Strict metric identity exceeded cardinality policy: " + descriptor.key());
                    }
                    if (allowed == null) {
                        continue;
                    }
                    if (descriptor.type() != AttributeType.STRING) {
                        throw new IllegalArgumentException(
                                "Non-string metric attribute cannot use cardinality sentinel: " + descriptor.key());
                    }
                    putString(result, descriptor, allowed);
                } else {
                    putValue(result, entry);
                }
            }
            return result.build();
        }

        /**
         * Returns the cumulative violations for one tag key in this scope.
         *
         * @param tagKey tag key
         * @return violation count
         */
        public long violationCount(String tagKey) {
            AtomicLong count = violationCounts.get(tagKey);
            return count == null ? 0 : count.get();
        }

        /**
         * Adds a cardinality violation listener if it is not already registered.
         *
         * @param listener listener to add
         */
        public void addViolationListener(Consumer<CardinalityViolation> listener) {
            violationListeners.addIfAbsent(Objects.requireNonNull(listener, "Violation listener must not be null"));
        }

        /**
         * Removes a previously registered cardinality violation listener.
         *
         * @param listener listener to remove
         */
        public void removeViolationListener(Consumer<CardinalityViolation> listener) {
            violationListeners.remove(Objects.requireNonNull(listener, "Violation listener must not be null"));
        }

        /**
         * Counts a policy violation and emits a throttled warning.
         *
         * @param metricName  metric family name
         * @param tagKey      attribute key
         * @param original    rejected value
         * @param replacement replacement value or denial reason
         */
        private void recordViolation(String metricName, String tagKey, String original, String replacement) {
            violationCounts.computeIfAbsent(tagKey, key -> new AtomicLong()).incrementAndGet();
            Instant detectedAt = Instant.now();
            CardinalityViolation violation = new CardinalityViolation(metricName, tagKey, original, replacement,
                    detectedAt);
            for (Consumer<CardinalityViolation> listener : violationListeners) {
                try {
                    listener.accept(violation);
                } catch (RuntimeException exception) {
                    Logger.warn(
                            false,
                            "Metrics",
                            "Cardinality violation listener failed: listenerClass={}, reason={}",
                            listener.getClass().getName(),
                            exception.getMessage());
                }
            }
            long now = detectedAt.toEpochMilli();
            Long last = lastLogTimestamps.get(tagKey);
            if (last == null || now - last > Builder.CARDINALITY_LOG_THROTTLE_MS) {
                lastLogTimestamps.put(tagKey, now);
                Logger.warn(
                        false,
                        "Metrics",
                        "Cardinality violation on metric={} tagKey={} replacedWith={} originalLength={}",
                        metricName,
                        tagKey,
                        replacement,
                        original == null ? 0 : original.length());
            }
        }
    }

}
