/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.builtin.software;

import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * Operating system services are responsible for the management of platform resources, including the processor, memory,
 * files, and input and output. They generally shield applications from the implementation details of the machine. This
 * class is provided for information purposes only. Interpretation of the meaning of services is platform-dependent.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
public class OSService {

    private final String name;
    private final int processID;
    private final State state;

    /**
     * Instantiate a new {@link OSService}.
     *
     * @param name      The service name.
     * @param processID The process ID if running, or 0 if stopped.
     * @param state     The service {@link State}.
     */
    public OSService(String name, int processID, State state) {
        this.name = name;
        this.processID = processID;
        this.state = state;
    }

    /**
     * <p>
     * Getter for the field <code>name</code>.
     * </p>
     *
     * @return Returns the name of the service.
     */
    public String getName() {
        return this.name;
    }

    /**
     * <p>
     * Getter for the field <code>processID</code>.
     * </p>
     *
     * @return Returns the processID.
     */
    public int getProcessID() {
        return this.processID;
    }

    /**
     * <p>
     * Getter for the field <code>state</code>.
     * </p>
     *
     * @return Returns the state of the service.
     */
    public State getState() {
        return this.state;
    }

    /**
     * Service Execution States
     */
    public enum State {
        RUNNING, STOPPED, OTHER
    }

}
