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
package org.miaixz.bus.auth;

import java.util.Set;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable identity authenticated for one invocation.
 *
 * @param subjectId authenticated human or workload subject identifier
 * @param clientId  authenticated client identifier
 * @param scopes    immutable granted scope names
 * @param claims    immutable authenticated claim snapshot
 * @author Kimi Liu
 */
public record Principal(String subjectId, String clientId, Set<String> scopes, Claims claims) {

    /**
     * Normalizes identifiers and snapshots mutable inputs.
     *
     * @throws ValidateException if both identity identifiers are absent
     */
    public Principal {
        subjectId = normalize(subjectId);
        clientId = normalize(clientId);
        if (subjectId == null && clientId == null) {
            throw new ValidateException("Principal must identify a subject or client");
        }
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
        claims = claims == null ? Claims.empty() : claims;
    }

    /**
     * Trims an optional identifier and converts blank text to null.
     *
     * @param value optional identifier
     * @return normalized identifier or null
     */
    private static String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

}
