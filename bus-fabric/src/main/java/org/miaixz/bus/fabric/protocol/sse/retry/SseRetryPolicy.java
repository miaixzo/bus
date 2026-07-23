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
package org.miaixz.bus.fabric.protocol.sse.retry;

import java.time.Duration;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Policy;

/**
 * Immutable SSE reconnect policy. Server {@code retry:} fields update session state without mutating this policy.
 *
 * @param initialDelay initial reconnect delay
 * @param maxDelay     maximum reconnect delay
 * @author Kimi Liu
 * @since Java 21+
 */
public record SseRetryPolicy(Duration initialDelay, Duration maxDelay) implements Policy {

    /**
     * Typed option for the complete SSE reconnect policy.
     */
    public static final Options.Key<SseRetryPolicy> OPTION = Options.key("sse.retry", SseRetryPolicy.class);

    /**
     * Shared default retry policy.
     */
    private static final SseRetryPolicy DEFAULTS = new SseRetryPolicy(
            org.miaixz.bus.fabric.Builder.SSE_RETRY_DEFAULT_CURRENT,
            org.miaixz.bus.fabric.Builder.SSE_RETRY_DEFAULT_MAX_DELAY);

    /**
     * Creates and validates a retry policy.
     *
     * @param initialDelay initial reconnect delay
     * @param maxDelay     maximum reconnect delay
     */
    public SseRetryPolicy {
        initialDelay = duration(initialDelay, "SSE initial retry");
        maxDelay = duration(maxDelay, "SSE maximum retry");
    }

    /**
     * Returns the shared default retry policy.
     *
     * @return default retry policy
     */
    public static SseRetryPolicy defaults() {
        return DEFAULTS;
    }

    /**
     * Resolves the complete policy from options.
     *
     * @param options option source
     * @return configured policy or shared defaults
     */
    public static SseRetryPolicy resolve(final Options options) {
        final Options current = Assert.notNull(options, () -> new ValidateException("Options must not be null"));
        final SseRetryPolicy configured = current.get(OPTION);
        return configured == null ? defaults() : configured;
    }

    /**
     * Adds this complete policy to an option snapshot.
     *
     * @param options option source
     * @return updated option snapshot
     */
    @Override
    public Options from(final Options options) {
        return Assert.notNull(options, () -> new ValidateException("Options must not be null")).with(OPTION, this);
    }

    /**
     * Calculates capped exponential backoff from a current server-adjustable base delay.
     *
     * @param base    current base delay
     * @param attempt non-negative reconnect attempt
     * @return delay capped by {@link #maxDelay()}
     */
    Duration nextDelay(final Duration base, final int attempt) {
        final Duration current = duration(base, "SSE retry");
        Assert.isTrue(attempt >= Normal._0, () -> new ValidateException("SSE retry attempt must be non-negative"));
        if (current.compareTo(maxDelay) >= Normal._0) {
            return maxDelay;
        }
        if (attempt >= Long.SIZE - Normal._1) {
            return maxDelay;
        }
        try {
            final Duration calculated = current.multipliedBy(1L << attempt);
            return calculated.compareTo(maxDelay) > Normal._0 ? maxDelay : calculated;
        } catch (final ArithmeticException ignored) {
            return maxDelay;
        }
    }

    /**
     * Validates a non-negative duration.
     *
     * @param value duration candidate
     * @param name  component name
     * @return validated duration
     */
    static Duration duration(final Duration value, final String name) {
        final Duration checked = Assert
                .notNull(value, () -> new ValidateException(name + " must be non-null and non-negative"));
        Assert.isTrue(!checked.isNegative(), () -> new ValidateException(name + " must be non-null and non-negative"));
        return checked;
    }

}
