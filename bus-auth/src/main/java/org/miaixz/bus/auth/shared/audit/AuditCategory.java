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
 * Classifies internal authentication audit events without becoming a protocol or wire registration value.
 *
 * @author Kimi Liu
 */
public enum AuditCategory implements Enumers<AuditCategory> {

    /**
     * Registry loading, validation, compilation, and commit activity.
     */
    REGISTRY(1),

    /**
     * Principal authentication and Source sign-in activity.
     */
    AUTHENTICATION(2),

    /**
     * OAuth or equivalent authorization-decision activity.
     */
    AUTHORIZATION(3),

    /**
     * Token issuance, validation, rotation, introspection, or revocation activity.
     */
    TOKEN(4),

    /**
     * Framework Session lifecycle activity.
     */
    SESSION(5),

    /**
     * SCIM or other identity provisioning activity.
     */
    PROVISIONING(6),

    /**
     * LDAP directory operation activity.
     */
    DIRECTORY(7),

    /**
     * RADIUS authentication and accounting-related framework activity.
     */
    RADIUS(8);

    /**
     * Stable persistence code independent of declaration order.
     */
    private final int code;

    /**
     * Creates an audit category with its stable persistence code.
     *
     * @param code stable persistence code
     */
    AuditCategory(final int code) {
        this.code = code;
    }

    /**
     * Returns the stable internal persistence code.
     *
     * @return stable audit category code
     */
    @Override
    public int code() {
        return code;
    }

}
