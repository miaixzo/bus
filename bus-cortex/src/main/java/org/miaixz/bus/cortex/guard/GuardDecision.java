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
package org.miaixz.bus.cortex.guard;

import lombok.Getter;

/**
 * Standard decision result returned by guard strategies.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Getter
public class GuardDecision {

    /**
     * Whether the request is allowed to continue.
     */
    private final boolean allowed;

    /**
     * Machine-readable decision code.
     */
    private final String code;

    /**
     * Human-readable decision message.
     */
    private final String message;
    /**
     * Optional structured details.
     */
    private final java.util.Map<String, Object> details;

    /**
     * Creates one decision result.
     *
     * @param allowed whether the request is allowed
     * @param code    machine-readable code
     * @param message human-readable message
     */
    public GuardDecision(boolean allowed, String code, String message) {
        this(allowed, code, message, java.util.Map.of());
    }

    /**
     * Creates one decision result with structured details.
     *
     * @param allowed whether the request is allowed
     * @param code    machine-readable code
     * @param message human-readable message
     * @param details structured details
     */
    public GuardDecision(boolean allowed, String code, String message, java.util.Map<String, Object> details) {
        this.allowed = allowed;
        this.code = code;
        this.message = message;
        this.details = details == null ? java.util.Map.of() : java.util.Map.copyOf(details);
    }

    /**
     * Returns whether the request is allowed.
     *
     * @return {@code true} when the request is allowed
     */
    public boolean isAllowed() {
        return allowed;
    }

    /**
     * Creates a successful decision.
     *
     * @return allow decision
     */
    public static GuardDecision allow() {
        return new GuardDecision(true, "ALLOW", "Guard check passed");
    }

    /**
     * Creates a denied decision.
     *
     * @param code    machine-readable code
     * @param message human-readable message
     * @return deny decision
     */
    public static GuardDecision deny(String code, String message) {
        return new GuardDecision(false, code, message);
    }

}
