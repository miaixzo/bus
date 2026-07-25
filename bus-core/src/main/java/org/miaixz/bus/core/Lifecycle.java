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
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core;

/**
 * Common lifecycle contract for managed work, services, and resources.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Lifecycle {

    /**
     * Returns the current externally observable lifecycle state.
     *
     * @return lifecycle state
     */
    State state();

    /**
     * Returns whether the component is currently executing or available.
     *
     * @return {@code true} when active
     */
    default boolean active() {
        return state().active();
    }

    /**
     * Returns whether the component completed or closed normally.
     *
     * @return {@code true} after successful termination
     */
    default boolean successful() {
        return state().successful();
    }

    /**
     * Returns whether no further lifecycle progress is expected.
     *
     * @return {@code true} in a terminal state
     */
    default boolean terminal() {
        return state().terminal();
    }

    /**
     * Returns whether the lifecycle ended through cancellation.
     *
     * @return {@code true} when cancelled
     */
    default boolean cancelled() {
        return state().cancelled();
    }

    /**
     * Returns whether the lifecycle ended through failure.
     *
     * @return {@code true} when failed
     */
    default boolean failed() {
        return state().failed();
    }

    /**
     * Shared externally observable lifecycle state.
     */
    enum State {

        /**
         * The component has been created but has not started.
         */
        NEW,

        /**
         * Work is waiting to be executed.
         */
        QUEUED,

        /**
         * The component is initializing and is not yet available.
         */
        STARTING,

        /**
         * The component is executing or available for use.
         */
        RUNNING,

        /**
         * The component is stopping or releasing resources.
         */
        CLOSING,

        /**
         * A finite operation completed successfully.
         */
        COMPLETED,

        /**
         * A service or resource closed normally.
         */
        CLOSED,

        /**
         * Work was cancelled before normal completion.
         */
        CANCELLED,

        /**
         * Work, a service, or a resource terminated because of failure.
         */
        FAILED,

        /**
         * The lifecycle state cannot be determined.
         */
        UNKNOWN;

        /**
         * Returns whether the component is currently executing or available.
         *
         * @return {@code true} for {@link #RUNNING}
         */
        public boolean active() {
            return this == RUNNING;
        }

        /**
         * Returns whether the component completed or closed normally.
         *
         * @return {@code true} for {@link #COMPLETED} or {@link #CLOSED}
         */
        public boolean successful() {
            return this == COMPLETED || this == CLOSED;
        }

        /**
         * Returns whether no further lifecycle progress is expected.
         *
         * @return {@code true} for successful, cancelled, or failed termination
         */
        public boolean terminal() {
            return switch (this) {
                case COMPLETED, CLOSED, CANCELLED, FAILED -> true;
                default -> false;
            };
        }

        /**
         * Returns whether the lifecycle ended through cancellation.
         *
         * @return {@code true} for {@link #CANCELLED}
         */
        public boolean cancelled() {
            return this == CANCELLED;
        }

        /**
         * Returns whether the lifecycle ended through failure.
         *
         * @return {@code true} for {@link #FAILED}
         */
        public boolean failed() {
            return this == FAILED;
        }

    }

}
