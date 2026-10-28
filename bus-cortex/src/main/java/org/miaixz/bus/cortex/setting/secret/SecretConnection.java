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
package org.miaixz.bus.cortex.setting.secret;

import java.util.List;

/**
 * Immutable value-free backend routing restrictions. Prefix checks are literal startsWith checks, in addition to policy
 * authorization. Authentication and backend endpoint details belong to the bound provider.
 *
 * @param tenantId        authenticated tenant identifier
 * @param id              connection identifier scoped to the tenant
 * @param provider        non-blank provider type identifier, matched against the registered provider
 * @param generation      non-negative connection configuration generation attached to returned materials
 * @param allowedPrefixes non-empty immutable copy of permitted resource prefixes; matching is literal startsWith
 * @param enabled         whether this connection may be resolved
 * @author Kimi Liu
 */
public record SecretConnection(String tenantId, String id, String provider, long generation,
        List<String> allowedPrefixes, boolean enabled) {

    /**
     * Validates the record components before publishing this immutable value. Supplied collections are defensively
     * copied.
     *
     * @param tenantId        authenticated tenant identifier
     * @param id              connection identifier scoped to the tenant
     * @param provider        non-blank provider type identifier, matched against the registered provider
     * @param generation      non-negative connection configuration generation attached to returned materials
     * @param allowedPrefixes non-empty immutable copy of permitted resource prefixes; matching is literal startsWith
     * @param enabled         whether this connection may be resolved
     * @throws IllegalArgumentException if component values violate this record's constraints
     * @throws NullPointerException     if the prefix collection or one of its elements is null
     */
    public SecretConnection {
        if (tenantId == null || tenantId.isBlank() || id == null || id.isBlank() || provider == null
                || provider.isBlank() || generation < 0) {
            throw new IllegalArgumentException("Invalid secret connection");
        }
        allowedPrefixes = List.copyOf(allowedPrefixes);
        if (allowedPrefixes.isEmpty() || allowedPrefixes.stream().anyMatch(String::isBlank)) {
            throw new IllegalArgumentException("Explicit resource prefixes are required");
        }
    }

    /**
     * Tests a resource against the explicitly configured literal prefixes. This is an additional routing restriction,
     * not a substitute for the access policy.
     *
     * @param resource logical backend resource name before URL encoding
     * @return true if at least one allowed prefix matches
     */
    public boolean allows(String resource) {
        return allowedPrefixes.stream().anyMatch(resource::startsWith);
    }

}
