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

/**
 * Read-only lifecycle view shared by calls, sessions, connections, and runtime resources.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public interface Lifecycle {

    /**
     * Returns the current lifecycle state.
     *
     * @return lifecycle state
     */
    Status state();

    /**
     * Returns whether this lifecycle is queued.
     *
     * @return true when queued
     */
    default boolean queued() {
        return state() == Status.QUEUED;
    }

    /**
     * Returns whether this lifecycle is running.
     *
     * @return true when running
     */
    default boolean running() {
        return state() == Status.RUNNING;
    }

    /**
     * Returns whether this lifecycle is opened or actively running.
     *
     * @return true when opened or running
     */
    default boolean opened() {
        final Status status = state();
        return status == Status.OPENED || status == Status.RUNNING;
    }

    /**
     * Returns whether this lifecycle is closing.
     *
     * @return true when closing
     */
    default boolean closing() {
        return state() == Status.CLOSING;
    }

    /**
     * Returns whether this lifecycle is closed.
     *
     * @return true when closed
     */
    default boolean closed() {
        return state() == Status.CLOSED;
    }

    /**
     * Returns whether this lifecycle is cancelled.
     *
     * @return true when cancelled
     */
    default boolean cancelled() {
        return state() == Status.CANCELLED;
    }

    /**
     * Returns whether this lifecycle has failed.
     *
     * @return true when failed
     */
    default boolean failed() {
        return state() == Status.FAILED;
    }

    /**
     * Returns whether this lifecycle completed successfully.
     *
     * @return true when done or closed
     */
    default boolean successful() {
        final Status status = state();
        return status == Status.DONE || status == Status.CLOSED;
    }

    /**
     * Returns whether this lifecycle reached a terminal state.
     *
     * @return true when terminal
     */
    default boolean terminal() {
        return state().terminal();
    }

}
