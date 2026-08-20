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
package org.miaixz.bus.auth.protocol.oidc.codec;

import org.miaixz.bus.auth.protocol.oauth2.AuthorizationResponse;
import org.miaixz.bus.auth.protocol.oauth2.codec.AuthorizationResponseEncoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.fabric.protocol.http.HttpRequest;
import org.miaixz.bus.fabric.protocol.http.HttpResponse;

/**
 * Encodes an OIDC Authorization Code Flow response by delegating to the standard OAuth response encoder.
 * <p>
 * Delegation preserves the standard success or error response branch, state, HTTP redirect, and cache-prevention
 * contract without adding an OIDC-specific authorization response wrapper.
 * </p>
 *
 * @author Kimi Liu
 */
public final class AuthenticationResponseEncoder {

    /**
     * Shared successful OAuth authorization response encoder.
     */
    private final AuthorizationResponseEncoder oauthEncoder;

    /**
     * Creates an OIDC Authentication Response encoder over the shared OAuth codec.
     *
     * @param oauthEncoder strict OAuth authorization response encoder
     * @throws IllegalArgumentException if {@code oauthEncoder} is {@code null}
     */
    public AuthenticationResponseEncoder(final AuthorizationResponseEncoder oauthEncoder) {
        this.oauthEncoder = Assert
                .notNull(oauthEncoder, "OpenID Connect OAuth authorization response encoder must not be null");
    }

    /**
     * Encodes one OIDC code-flow success or error redirect.
     *
     * @param request     originating Fabric authorization request
     * @param redirectUri exact redirect URI already validated against client registration
     * @param response    standard OAuth authorization response
     * @return complete empty redirect response
     */
    public HttpResponse encode(
            final HttpRequest request,
            final String redirectUri,
            final AuthorizationResponse response) {
        Assert.notNull(response, "OpenID Connect Authentication Response must not be null");
        return oauthEncoder.encode(request, redirectUri, response);
    }

}
