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
package org.miaixz.bus.auth.protocol.oauth2.grant;

import java.time.Duration;
import java.util.Set;

import org.miaixz.bus.auth.protocol.oauth2.GrantType;

/**
 * Exposes only the immutable OAuth 2.x policy required by grant processing.
 * <p>
 * Provider options implement this contract so grant mechanisms do not depend on endpoint or server orchestration
 * packages. It contains no endpoint addresses, client-authentication policy, persistence, or project configuration.
 * </p>
 *
 * @author Kimi Liu
 */
public interface GrantPolicy {

    /**
     * Returns the canonical authorization-server issuer.
     *
     * @return issuer identifier
     */
    String issuer();

    /**
     * Returns scopes supported by the authorization server.
     *
     * @return immutable supported scopes
     */
    Set<String> scopesSupported();

    /**
     * Returns supported standard grant types.
     *
     * @return immutable supported grant types
     */
    Set<GrantType> grantTypesSupported();

    /**
     * Returns the maximum one-time authorization-code lifetime.
     *
     * @return authorization-code lifetime
     */
    Duration authorizationCodeLifetime();

    /**
     * Returns the maximum issued access-token lifetime.
     *
     * @return access-token lifetime
     */
    Duration accessTokenLifetime();

    /**
     * Returns the maximum refresh-token authorization lifetime.
     *
     * @return refresh-token lifetime
     */
    Duration refreshTokenLifetime();

    /**
     * Tests whether every authorization-code request requires PKCE.
     *
     * @return whether PKCE is required
     */
    boolean pkceRequired();

    /**
     * Tests whether each refresh operation must rotate its token.
     *
     * @return whether rotation is required
     */
    boolean refreshTokenRotationRequired();

}
