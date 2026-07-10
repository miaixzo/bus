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
package org.miaixz.bus.fabric;

import java.time.Duration;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Single executable fabric call.
 * <p>
 * A call represents one concrete request, connection open, or protocol write. Implementations may run immediately,
 * dispatch work in the background, or wrap an already-started transport operation, but the public contract stays
 * focused on execution and lifecycle state rather than exposing internal futures.
 *
 * @param <T> result type
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Call<T> {

    /**
     * Executes this call synchronously.
     *
     * @return completed result
     */
    T execute();

    /**
     * Enqueues this call for asynchronous execution.
     *
     * @return this call
     */
    Call<T> enqueue();

    /**
     * Cancels this call and propagates cancellation to owned runtime resources.
     *
     * @return true when this invocation changed the call to cancelled
     */
    boolean cancel();

    /**
     * Returns whether this call is cancelled.
     *
     * @return true when cancelled
     */
    boolean cancelled();

    /**
     * Returns whether this call is complete.
     *
     * @return true when complete
     */
    boolean done();

    /**
     * Waits for this call to complete.
     *
     * @return completed value
     */
    default T await() {
        return execute();
    }

    /**
     * Waits for this call to complete within a timeout.
     *
     * @param timeout timeout
     * @return completed value
     */
    default T await(final Duration timeout) {
        if (timeout == null || timeout.isNegative()) {
            throw new ValidateException("Timeout must be non-null and non-negative");
        }
        return await();
    }

}
