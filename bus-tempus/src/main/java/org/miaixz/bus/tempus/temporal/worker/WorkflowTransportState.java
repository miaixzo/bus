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
package org.miaixz.bus.tempus.temporal.worker;

/**
 * Known Temporal workflow transport states.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum WorkflowTransportState {

    /**
     * Transport has no active connection.
     */
    IDLE("IDLE"),

    /**
     * Transport is connecting.
     */
    CONNECTING("CONNECTING"),

    /**
     * Transport is ready.
     */
    READY("READY"),

    /**
     * Transport is shut down.
     */
    SHUTDOWN("SHUTDOWN"),

    /**
     * Transport is in transient failure.
     */
    TRANSIENT_FAILURE("TRANSIENT_FAILURE"),

    /**
     * Transport state is unknown.
     */
    UNKNOWN("UNKNOWN");

    /**
     * Raw transport state value.
     */
    private final String value;

    /**
     * Creates a workflow transport state.
     *
     * @param value raw transport state value
     */
    WorkflowTransportState(String value) {
        this.value = value;
    }

    /**
     * Gets the raw transport state value.
     *
     * @return raw transport state value
     */
    public String value() {
        return value;
    }

    /**
     * Checks whether the state matches the given value.
     *
     * @param value transport state value
     * @return {@code true} when values match
     */
    public boolean matches(String value) {
        return this.value.equals(value);
    }

    /**
     * Resolves a raw transport state value.
     *
     * @param value raw transport state value
     * @return resolved transport state
     */
    public static WorkflowTransportState of(String value) {
        for (WorkflowTransportState state : values()) {
            if (state.matches(value)) {
                return state;
            }
        }
        return UNKNOWN;
    }

}
