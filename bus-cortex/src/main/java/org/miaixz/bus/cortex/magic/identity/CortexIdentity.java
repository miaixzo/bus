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
package org.miaixz.bus.cortex.magic.identity;

/**
 * Common Cortex identity normalization shared by registry, setting, watch, and guard code.
 *
 * @author Kimi Liu
 */
public class CortexIdentity {

    /**
     * Keeps Cortex identity handling on the static API.
     */
    public CortexIdentity() {
        // No initialization required.
    }

    /**
     * Requires an explicit tenant identifier. There is no default tenant.
     *
     * @param tenantId raw tenantId value
     * @return canonical tenantId value
     */
    public static String tenantId(String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new IllegalArgumentException("tenant_id is required");
        }
        return tenantId.trim();
    }

    /**
     * Encodes a tenant identifier as an unambiguous cache and route key segment.
     *
     * @param tenantId explicit tenant identifier
     * @return length-prefixed tenant segment
     */
    public static String tenantKey(String tenantId) {
        String value = tenantId(tenantId);
        return value.length() + ":" + value;
    }

    /**
     * Rejects a payload outside the explicitly selected tenant.
     */
    public static void requireTenant(String expected, String actual) {
        if (!tenantId(expected).equals(tenantId(actual))) {
            throw new IllegalArgumentException("Tenant identity mismatch");
        }
    }

    /**
     * Returns the canonical application identifier.
     *
     * @param app_id raw application identifier
     * @return canonical application identifier or {@code null} when blank
     */
    public static String applicationId(String app_id) {
        return app_id == null || app_id.isBlank() ? null : app_id;
    }

}
