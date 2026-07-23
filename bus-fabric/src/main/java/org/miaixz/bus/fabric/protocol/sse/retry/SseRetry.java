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
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Session-local SSE reconnect controller updated by server retry directives.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SseRetry {

    /**
     * Session-local retry state.
     */
    private final SseRetryState state;

    /**
     * Creates a session-local reconnect controller.
     *
     * @param policy complete immutable retry policy
     */
    private SseRetry(final SseRetryPolicy policy) {
        this.state = new SseRetryState(
                Assert.notNull(policy, () -> new ValidateException("SSE retry policy must not be null")));
    }

    /**
     * Creates a session-local reconnect controller using the default retry policy.
     *
     * @return reconnect controller initialized with the default policy
     */
    public static SseRetry defaults() {
        return of(SseRetryPolicy.defaults());
    }

    /**
     * Creates session-local retry state from a complete policy.
     *
     * @param policy complete retry policy
     * @return new mutable session-local retry state
     */
    public static SseRetry of(final SseRetryPolicy policy) {
        return new SseRetry(policy);
    }

    /**
     * Returns the complete immutable retry policy.
     *
     * @return retry policy
     */
    public SseRetryPolicy policy() {
        return state.policy();
    }

    /**
     * Returns current base retry delay.
     *
     * @return current retry delay
     */
    public Duration current() {
        return state.current();
    }

    /**
     * Updates current base retry delay.
     *
     * @param retry retry delay
     */
    public void update(final Duration retry) {
        state.update(retry);
    }

    /**
     * Returns the delay for a retry attempt using capped exponential backoff.
     *
     * @param attempt attempt index
     * @return next delay
     */
    public Duration nextDelay(final int attempt) {
        return state.nextDelay(attempt);
    }

}
