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
package org.miaixz.bus.core.lang.thread;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

import org.miaixz.bus.core.lang.Assert;

/**
 * Exponential backoff strategy with optional full jitter.
 * <p>
 * The delay is calculated as {@code initialDelay * multiplier^(attempt - 1)}, capped by {@code maxDelay}. When jitter
 * is enabled, a random delay between zero and the capped delay is returned.
 * </p>
 *
 * @author Kimi Liu
 * @see Backoff
 * @see FixedBackoff
 * @since Java 21+
 */
public class ExponentialBackoff implements Backoff {

    /**
     * The positive delay used for the first retry attempt.
     */
    private final Duration initialDelay;

    /**
     * The multiplier applied after each retry attempt.
     */
    private final double multiplier;

    /**
     * The maximum delay allowed after exponential growth is applied.
     */
    private final Duration maxDelay;

    /**
     * Whether to apply full jitter to the calculated capped delay.
     */
    private final boolean jitter;

    /**
     * Creates an exponential backoff strategy.
     *
     * @param initialDelay The delay used for the first retry attempt, must be positive.
     * @param multiplier   The growth multiplier, must be at least 1.0.
     * @param maxDelay     The maximum delay, must be greater than or equal to the initial delay.
     * @param jitter       Whether to use full jitter.
     */
    public ExponentialBackoff(final Duration initialDelay, final double multiplier, final Duration maxDelay,
            final boolean jitter) {
        Assert.notNull(initialDelay, "initialDelay must not be null");
        Assert.isTrue(!initialDelay.isNegative() && !initialDelay.isZero(), "initialDelay must be positive");
        Assert.isTrue(multiplier >= 1.0, "multiplier must be >= 1.0");
        Assert.notNull(maxDelay, "maxDelay must not be null");
        Assert.isTrue(maxDelay.compareTo(initialDelay) >= 0, "maxDelay must be >= initialDelay");

        this.initialDelay = initialDelay;
        this.multiplier = multiplier;
        this.maxDelay = maxDelay;
        this.jitter = jitter;
    }

    /**
     * Calculates the delay for the given retry attempt.
     *
     * @param attempt The retry attempt number, starting at 1.
     * @return The calculated delay, capped by {@code maxDelay} and optionally jittered.
     */
    @Override
    public Duration next(final int attempt) {
        final long initialMillis = this.initialDelay.toMillis();
        final long maxMillis = this.maxDelay.toMillis();
        final long computedMillis = (long) (initialMillis * Math.pow(this.multiplier, attempt - 1));
        final long delayMillis = (computedMillis <= 0 || computedMillis > maxMillis) ? maxMillis : computedMillis;

        if (this.jitter && delayMillis > 0) {
            return Duration.ofMillis(ThreadLocalRandom.current().nextLong(delayMillis + 1));
        }
        return Duration.ofMillis(delayMillis);
    }

}
