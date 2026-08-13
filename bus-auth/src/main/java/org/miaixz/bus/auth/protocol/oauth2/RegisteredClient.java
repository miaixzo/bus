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

import java.net.URI;
import java.util.Set;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Options;

/**
 * Immutable trusted OAuth 2.0 client registration.
 * <p>
 * The canonical constructor trims the identifier, snapshots redirect URIs, and replaces absent options with the shared
 * immutable empty value. Secret material is never retained by this model; options contain only registration metadata
 * and typed references.
 * </p>
 *
 * @param id           non-blank exact OAuth client identifier
 * @param redirectUris registered exact redirect URI allowlist, defensively copied
 * @param options      immutable typed client registration metadata
 * @author Kimi Liu
 */
public record RegisteredClient(String id, Set<URI> redirectUris, Options options) {

    /**
     * Typed option selecting the registered token endpoint authentication method.
     */
    public static final Options.Key<String> TOKEN_ENDPOINT_AUTH_METHOD = Options
            .key("oauth2.client.token_endpoint_auth_method", String.class);

    /**
     * Validates required identity and snapshots caller-owned collections.
     *
     * @throws ValidateException if {@code id} is {@code null} or blank
     */
    public RegisteredClient {
        if (id == null || id.isBlank()) {
            throw new ValidateException("Registered client identifier must not be blank");
        }
        id = id.trim();
        redirectUris = redirectUris == null ? Set.of() : Set.copyOf(redirectUris);
        options = options == null ? Options.empty() : options;
    }

    /**
     * Returns the exact registered token endpoint authentication method.
     *
     * @return configured method, or {@code null} when no method was registered
     */
    public String tokenEndpointAuthMethod() {
        return options.get(TOKEN_ENDPOINT_AUTH_METHOD);
    }

}
