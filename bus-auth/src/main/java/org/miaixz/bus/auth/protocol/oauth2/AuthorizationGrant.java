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
package org.miaixz.bus.auth.protocol.oauth2;

import java.time.Instant;
import java.util.Set;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;

/**
 * Immutable OAuth 2.0 authorization grant persisted by the product-owned {@link GrantStore}. The record contains only
 * normalized identifiers, immutable scopes, an absolute expiration instant, and typed non-secret metadata.
 *
 * @param id        stable non-blank grant identifier
 * @param subjectId authorized non-blank subject identifier
 * @param clientId  registered non-blank client identifier
 * @param scopes    immutable granted scope set
 * @param expiresAt absolute expiration instant
 * @param options   immutable typed grant metadata
 * @author Kimi Liu
 */
public record AuthorizationGrant(String id, String subjectId, String clientId, Set<String> scopes, Instant expiresAt,
        Options options) {

    /**
     * Typed metadata key identifying the persisted token class.
     */
    public static final Options.Key<String> TOKEN_TYPE = Options.key("oauth2.grant.token_type", String.class);

    /**
     * Typed metadata key identifying a rotating refresh-token family.
     */
    public static final Options.Key<String> FAMILY_ID = Options.key("oauth2.grant.family_id", String.class);

    /**
     * Validates required identifiers and expiration while snapshotting scopes and normalizing absent metadata.
     *
     * @throws ValidateException when an identifier is blank or the expiration instant is absent
     */
    public AuthorizationGrant {
        if (id == null || id.isBlank() || subjectId == null || subjectId.isBlank() || clientId == null
                || clientId.isBlank() || expiresAt == null) {
            throw new ValidateException("Authorization grant is incomplete");
        }
        scopes = scopes == null ? Set.of() : Set.copyOf(scopes);
        options = options == null ? Options.empty() : options;
    }

}
