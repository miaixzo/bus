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
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Session-local SSE retry state updated by server retry directives.
 */
final class SseRetryState {

    /**
     * Immutable configured retry policy.
     */
    private final SseRetryPolicy policy;

    /**
     * Current server-adjustable base delay.
     */
    private final AtomicReference<Duration> current;

    /**
     * Creates session-local retry state.
     *
     * @param policy complete immutable retry policy
     */
    SseRetryState(final SseRetryPolicy policy) {
        this.policy = Assert.notNull(policy, () -> new ValidateException("SSE retry policy must not be null"));
        this.current = new AtomicReference<>(policy.initialDelay());
    }

    /**
     * Returns the immutable policy.
     *
     * @return retry policy
     */
    SseRetryPolicy policy() {
        return policy;
    }

    /**
     * Returns the current base delay.
     *
     * @return current base delay
     */
    Duration current() {
        return current.get();
    }

    /**
     * Updates the server-provided base delay.
     *
     * @param retry new base delay
     */
    void update(final Duration retry) {
        current.set(SseRetryPolicy.duration(retry, "SSE retry"));
    }

    /**
     * Calculates the next reconnect delay.
     *
     * @param attempt reconnect attempt
     * @return capped reconnect delay
     */
    Duration nextDelay(final int attempt) {
        return policy.nextDelay(current(), attempt);
    }

}
