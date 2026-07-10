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
package org.miaixz.bus.fabric.protocol;

/**
 * Stable protocol pipeline checkpoint codes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public enum Checkpoint {

    /**
     * Prepare exchange metadata.
     */
    PREPARE("prepare", false, false),

    /**
     * Run guard checks.
     */
    GUARD("guard", false, false),

    /**
     * Resolve route metadata.
     */
    ROUTE("route", false, false),

    /**
     * Connect network resources.
     */
    CONNECT("connect", true, false),

    /**
     * Exchange with a server.
     */
    SERVER("server", true, false),

    /**
     * Completed successfully.
     */
    COMPLETE("complete", false, true),

    /**
     * Completed with failure.
     */
    FAILURE("failure", false, true);

    /**
     * Stable code.
     */
    private final String code;

    /**
     * Network checkpoint flag.
     */
    private final boolean network;

    /**
     * Terminal checkpoint flag.
     */
    private final boolean terminal;

    /**
     * Creates a protocol checkpoint.
     *
     * @param code     code
     * @param network  network flag
     * @param terminal terminal flag
     */
    Checkpoint(final String code, final boolean network, final boolean terminal) {
        this.code = code;
        this.network = network;
        this.terminal = terminal;
    }

    /**
     * Returns stable checkpoint code.
     *
     * @return code
     */
    public String code() {
        return code;
    }

    /**
     * Returns whether this checkpoint touches network flow.
     *
     * @return true when network checkpoint
     */
    public boolean network() {
        return network;
    }

    /**
     * Returns whether this checkpoint is terminal.
     *
     * @return true when terminal
     */
    public boolean terminal() {
        return terminal;
    }

}
