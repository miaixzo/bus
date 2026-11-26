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
package org.miaixz.bus.errors;

import java.util.Objects;

/**
 * Identifies the execution boundary and transmission phase for an error decision.
 *
 * @param listener      stable listener identifier, such as {@code mvc} or {@code vortex}
 * @param protocol      stable protocol identifier
 * @param phase         current transmission phase
 * @param policyVersion selected listener policy version, or zero without a policy
 *
 * @author Kimi Liu
 */
public record ErrorContext(String listener, String protocol, Phase phase, long policyVersion) {

    /**
     * Ensures the execution boundary is completely identified.
     *
     * @param listener      stable listener identifier, such as {@code mvc} or {@code vortex}
     * @param protocol      stable protocol identifier
     * @param phase         current transmission phase
     * @param policyVersion selected listener policy version, or zero without a policy
     */
    public ErrorContext {
        Objects.requireNonNull(listener, "listener");
        Objects.requireNonNull(protocol, "protocol");
        Objects.requireNonNull(phase, "phase");
    }

    /**
     * Creates a context without a listener policy.
     *
     * @param listener stable listener identifier
     * @param protocol stable protocol identifier
     * @param phase    current transmission phase
     */
    public ErrorContext(String listener, String protocol, Phase phase) {
        this(listener, protocol, phase, 0);
    }

    /**
     * Transmission state observed by the component that owns the response.
     *
     * @author Kimi Liu
     */
    public enum Phase {
        /**
         * A library operation without a transport response to write.
         */
        LOCAL,
        /**
         * The component may still select a response.
         */
        OPEN,
        /**
         * Response headers or the protocol upgrade have already been sent.
         */
        COMMITTED
    }

}
