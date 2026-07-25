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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

import org.miaixz.bus.core.Lifecycle;
import org.miaixz.bus.core.lang.exception.StatefulException;

/**
 * Single source of truth for the public lifecycle of one HTTP/2 connection.
 * <p>
 * Frame-reader, GOAWAY and draining phases remain protocol-internal; callers observe only the common lifecycle
 * contract.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class Http2ConnectionLifecycle implements Lifecycle {

    /**
     * Atomic lifecycle state.
     */
    private final AtomicReference<State> state = new AtomicReference<>(State.STARTING);

    /**
     * Creates and opens a connection lifecycle.
     *
     * @return running lifecycle
     */
    static Http2ConnectionLifecycle running() {
        final Http2ConnectionLifecycle lifecycle = new Http2ConnectionLifecycle();
        lifecycle.state.set(State.RUNNING);
        return lifecycle;
    }

    /**
     * Returns the current lifecycle state.
     *
     * @return lifecycle state
     */
    @Override
    public State state() {
        return state.get();
    }

    /**
     * Compatibility-shaped atomic getter used by the connection coordinator.
     *
     * @return lifecycle state
     */
    State get() {
        return state();
    }

    /**
     * Performs one legal atomic transition.
     *
     * @param expected expected state
     * @param update   target state
     * @return true when transitioned
     */
    boolean compareAndSet(final State expected, final State update) {
        validate(expected, update);
        return state.compareAndSet(expected, update);
    }

    /**
     * Updates lifecycle state through a transition function.
     *
     * @param update transition function
     * @return previous state
     */
    State getAndUpdate(final UnaryOperator<State> update) {
        return state.getAndUpdate(current -> {
            final State next = update.apply(current);
            validate(current, next);
            return next;
        });
    }

    /**
     * Completes the lifecycle at a terminal state.
     *
     * @param update terminal state
     */
    void set(final State update) {
        for (;;) {
            final State current = state.get();
            if (current == update) {
                return;
            }
            validate(current, update);
            if (state.compareAndSet(current, update)) {
                return;
            }
        }
    }

    /**
     * Validates the deliberately small HTTP/2 public transition graph.
     *
     * @param current current state
     * @param next    target state
     */
    private static void validate(final State current, final State next) {
        final boolean valid = current == next
                || current == State.STARTING && (next == State.RUNNING || next == State.CLOSING || next == State.FAILED)
                || current == State.RUNNING && (next == State.CLOSING || next == State.CLOSED || next == State.FAILED)
                || current == State.CLOSING && (next == State.CLOSED || next == State.FAILED)
                || current.terminal() && current == next;
        if (!valid) {
            throw new StatefulException("Invalid HTTP/2 lifecycle transition: " + current + " -> " + next);
        }
    }

}
