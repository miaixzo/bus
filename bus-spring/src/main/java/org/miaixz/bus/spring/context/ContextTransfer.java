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
package org.miaixz.bus.spring.context;

import java.util.Objects;
import java.util.concurrent.Callable;

/**
 * Explicit capture, installation, and cross-boundary context transfer operations.
 *
 * @author Kimi Liu
 */
public final class ContextTransfer {

    /**
     * Prevents instantiation of the static context transfer utility.
     */
    private ContextTransfer() {
        // No initialization required.
    }

    /**
     * Selects which parts of a captured context may cross an execution boundary.
     *
     * @author Kimi Liu
     */
    public enum Mode {

        /**
         * Transfers request correlation, authenticated identity, and raw request credentials.
         */
        ALL,

        /**
         * Transfers request correlation and authenticated identity without raw credentials.
         */
        IDENTITY_ONLY,

        /**
         * Transfers request correlation only.
         */
        REQUEST_ONLY,

        /**
         * Transfers no context values.
         */
        NONE;

        /**
         * Applies this mode to a captured state.
         *
         * @param state captured state
         * @return state safe for the selected transfer boundary
         */
        private ContextState apply(ContextState state) {
            ContextState source = state == null ? ContextState.empty() : state;
            return switch (this) {
                case ALL -> source;
                case IDENTITY_ONLY -> source.withoutCredentials();
                case REQUEST_ONLY -> source.requestOnly();
                case NONE -> ContextState.empty();
            };
        }

    }

    /**
     * Captures the complete state installed for the current execution.
     *
     * @return immutable current state
     */
    public static ContextState capture() {
        return ContextBuilder.capture();
    }

    /**
     * Captures the current state and filters it through a transfer mode.
     *
     * @param mode mode defining which values may cross the execution boundary
     * @return immutable state permitted by the mode
     * @throws NullPointerException when {@code mode} is {@code null}
     */
    public static ContextState capture(Mode mode) {
        return Objects.requireNonNull(mode, "mode").apply(capture());
    }

    /**
     * Installs an immutable state on the current thread.
     *
     * @param state state to install; {@code null} installs the shared empty state
     * @return lexical scope that restores the previous state when closed
     */
    public static ContextScope install(ContextState state) {
        return ContextCarrier.install(state);
    }

    /**
     * Captures the complete current state and wraps a task with its lifecycle.
     *
     * @param task task to wrap
     * @return context-aware task
     * @throws NullPointerException when {@code task} is {@code null}
     */
    public static Runnable wrap(Runnable task) {
        return wrap(task, Mode.ALL);
    }

    /**
     * Captures the mode-filtered current state and wraps a task with its lifecycle.
     *
     * @param task task to wrap
     * @param mode mode defining which values may cross the execution boundary
     * @return context-aware task
     * @throws NullPointerException when {@code task} or {@code mode} is {@code null}
     */
    public static Runnable wrap(Runnable task, Mode mode) {
        Objects.requireNonNull(task, "task");
        ContextState captured = capture(mode);
        return () -> {
            try (ContextScope ignored = install(captured)) {
                task.run();
            }
        };
    }

    /**
     * Captures the complete current state and wraps a value-returning task with its lifecycle.
     *
     * @param task task to wrap
     * @param <V>  result type produced by the task
     * @return context-aware task
     * @throws NullPointerException when {@code task} is {@code null}
     */
    public static <V> Callable<V> wrap(Callable<V> task) {
        return wrap(task, Mode.ALL);
    }

    /**
     * Captures the mode-filtered current state and wraps a value-returning task with its lifecycle.
     *
     * @param task task to wrap
     * @param mode mode defining which values may cross the execution boundary
     * @param <V>  result type produced by the task
     * @return context-aware task
     * @throws NullPointerException when {@code task} or {@code mode} is {@code null}
     */
    public static <V> Callable<V> wrap(Callable<V> task, Mode mode) {
        Objects.requireNonNull(task, "task");
        ContextState captured = capture(mode);
        return () -> {
            try (ContextScope ignored = install(captured)) {
                return task.call();
            }
        };
    }

}
