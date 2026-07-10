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

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Shared lifecycle status for calls, sessions, channels, dispatch tasks, and runtime resources.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Status {

    /**
     * Work is queued and has not started.
     */
    QUEUED,

    /**
     * Work is running but not necessarily opened as a long-lived resource.
     */
    RUNNING,

    /**
     * A long-lived resource is opened.
     */
    OPENED,

    /**
     * A resource is closing.
     */
    CLOSING,

    /**
     * Work completed successfully without a long-lived open resource.
     */
    DONE,

    /**
     * A resource closed normally.
     */
    CLOSED,

    /**
     * Work or a resource was cancelled.
     */
    CANCELLED,

    /**
     * Work or a resource failed.
     */
    FAILED;

    /**
     * Returns whether this status is open.
     *
     * @return true when opened
     */
    public boolean opened() {
        return this == OPENED;
    }

    /**
     * Returns whether this status is closed.
     *
     * @return true when closed
     */
    public boolean closed() {
        return this == CLOSED || this == DONE;
    }

    /**
     * Returns whether this status failed.
     *
     * @return true when failed
     */
    public boolean failed() {
        return this == FAILED;
    }

    /**
     * Returns whether this status is terminal.
     *
     * @return true when terminal
     */
    public boolean terminal() {
        return switch (this) {
            case DONE, CLOSED, CANCELLED, FAILED -> true;
            default -> false;
        };
    }

    /**
     * Returns whether this status may transition to another status.
     *
     * @param next next status
     * @return true when the transition is allowed
     */
    public boolean canTransit(final Status next) {
        if (next == null) {
            throw new ValidateException("Next status must not be null");
        }
        if (terminal()) {
            return this == next;
        }
        return switch (this) {
            case QUEUED -> next == RUNNING || next == OPENED || next == CANCELLED || next == DONE || next == FAILED;
            case RUNNING -> next == OPENED || next == CLOSING || next == CANCELLED || next == DONE || next == FAILED;
            case OPENED -> next == CLOSING || next == CLOSED || next == CANCELLED || next == FAILED;
            case CLOSING -> next == CLOSED || next == CANCELLED || next == FAILED;
            default -> false;
        };
    }

}
