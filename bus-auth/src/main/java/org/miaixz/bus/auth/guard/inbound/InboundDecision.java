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
package org.miaixz.bus.auth.guard.inbound;

import org.miaixz.bus.auth.guard.GuardEvaluator.Verified;

/**
 * Result of inbound service authentication.
 *
 * @param status   request disposition
 * @param verified authenticated identity when applicable
 *
 * @author Kimi Liu
 */
public record InboundDecision(Status status, Verified verified) {

    /**
     * Request attribute for the inbound decision, including a decision with no verified identity.
     */
    public static final String ATTRIBUTE = "org.miaixz.bus.auth.guard.inbound.InboundDecision";

    /**
     * Transport-neutral inbound result.
     */
    public enum Status {
        /**
         * The call may continue.
         */
        ACCEPTED,
        /**
         * The caller did not provide a valid required identity.
         */
        REJECTED,
        /**
         * The protected identity policy is unavailable.
         */
        UNAVAILABLE
    }

}
