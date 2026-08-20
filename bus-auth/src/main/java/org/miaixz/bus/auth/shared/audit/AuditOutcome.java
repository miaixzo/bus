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
package org.miaixz.bus.auth.shared.audit;

import org.miaixz.bus.core.lang.Enumers;

/**
 * Classifies the internal result recorded by a sanitized audit event.
 * <p>
 * This management value does not replace Outcome or a formal protocol error response. A safe shared Bus error, when
 * applicable, is carried separately by AuditEvent.
 * </p>
 *
 * @author Kimi Liu
 */
public enum AuditOutcome implements Enumers<AuditOutcome> {

    /**
     * The audited operation completed successfully.
     */
    SUCCEEDED(1),

    /**
     * The audited operation was expectedly refused by validation, authentication, or policy.
     */
    REJECTED(2),

    /**
     * The audited operation could not complete because of an operational failure.
     */
    FAILED(3);

    /**
     * Stable persistence code independent of declaration order.
     */
    private final int code;

    /**
     * Creates an audit outcome with its stable persistence code.
     *
     * @param code stable persistence code
     */
    AuditOutcome(final int code) {
        this.code = code;
    }

    /**
     * Returns the stable internal persistence code.
     *
     * @return stable audit outcome code
     */
    @Override
    public int code() {
        return code;
    }

}
