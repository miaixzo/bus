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

import org.miaixz.bus.core.lang.Assert;

/**
 * Fixed delay backoff strategy.
 * <p>
 * This strategy returns the same positive delay for every retry attempt and is the strategy used by
 * {@link RetryableTask#delay(Duration)}.
 * </p>
 *
 * @author Kimi Liu
 * @see Backoff
 * @see ExponentialBackoff
 */
public class FixedBackoff implements Backoff {

    /**
     * The fixed positive delay returned for each retry attempt.
     */
    private final Duration delay;

    /**
     * Creates a fixed delay backoff strategy.
     *
     * @param delay The fixed delay, must be positive.
     */
    public FixedBackoff(final Duration delay) {
        Assert.notNull(delay, "delay must not be null");
        Assert.isTrue(!delay.isNegative() && !delay.isZero(), "delay must be positive");
        this.delay = delay;
    }

    /**
     * Returns the configured fixed delay.
     *
     * @param attempt The retry attempt number. This strategy ignores the value.
     * @return The configured fixed delay.
     */
    @Override
    public Duration next(final int attempt) {
        return this.delay;
    }

}
