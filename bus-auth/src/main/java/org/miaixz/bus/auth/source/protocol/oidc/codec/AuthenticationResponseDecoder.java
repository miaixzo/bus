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
package org.miaixz.bus.auth.source.protocol.oidc.codec;

import java.net.URI;
import java.net.URISyntaxException;

import org.miaixz.bus.auth.Callback;
import org.miaixz.bus.auth.source.protocol.oauth2.AuthorizationResponse;
import org.miaixz.bus.auth.source.protocol.oauth2.OAuth2;
import org.miaixz.bus.auth.source.protocol.oauth2.codec.AuthorizationResponseDecoder;
import org.miaixz.bus.auth.source.protocol.oidc.OpenIdConnect;
import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Decodes an OIDC Authorization Code Flow callback through the shared OAuth authorization response decoder.
 * <p>
 * The decoder accepts only a query-mode code or standard OAuth error response. It explicitly rejects implicit, hybrid,
 * token, and session-management response parameters before delegating, and requires the successful OAuth branch to
 * remain represented by the shared OAuth authorization response union.
 * </p>
 *
 * @author Kimi Liu
 */
public class AuthenticationResponseDecoder implements Decoder<Callback.Inbound, AuthorizationResponse> {

    /**
     * Shared strict OAuth authorization callback decoder.
     */
    private final AuthorizationResponseDecoder oauthDecoder;

    /**
     * Creates an OIDC Authentication Response decoder over the shared OAuth codec.
     *
     * @param oauthDecoder strict OAuth authorization response decoder
     * @throws IllegalArgumentException if {@code oauthDecoder} is {@code null}
     */
    public AuthenticationResponseDecoder(final AuthorizationResponseDecoder oauthDecoder) {
        this.oauthDecoder = Assert
                .notNull(oauthDecoder, "OpenID Connect OAuth authorization response decoder must not be null");
    }

    /**
     * Rejects a callback request URI that contains a fragment component.
     *
     * @param requestUri original callback request URI
     * @throws ValidateException if URI syntax is invalid or a fragment is present
     */
    private static void rejectFragment(final String requestUri) {
        try {
            if (new URI(requestUri).getRawFragment() != null) {
                throw new ValidateException("OpenID Connect callback decoder does not accept fragment response mode");
            }
        } catch (URISyntaxException exception) {
            throw new ValidateException("OpenID Connect callback request URI is invalid", exception);
        }
    }

    /**
     * Identifies response parameters that belong to implicit, hybrid, or session-management responses.
     *
     * @param name exact callback parameter name
     * @return {@code true} when the parameter is forbidden in authorization-code flow
     */
    private static boolean forbidden(final String name) {
        return switch (name) {
            case OpenIdConnect.Parameters.ID_TOKEN, OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.EXPIRES_IN, OpenIdConnect.Parameters.SESSION_STATE -> true;
            default -> false;
        };
    }

    /**
     * Decodes exactly one standard query-mode code-flow success or OAuth error response.
     *
     * @param encoded raw protocol-neutral inbound callback
     * @return discriminated OIDC success or standard OAuth error
     * @throws IllegalArgumentException if {@code encoded} is {@code null}
     * @throws ValidateException        if the callback uses fragment, token, hybrid, or session response fields
     */
    @Override
    public AuthorizationResponse decode(final Callback.Inbound encoded) {
        Assert.notNull(encoded, "OpenID Connect Authentication callback must not be null");
        rejectFragment(encoded.requestUri());
        for (Callback.Parameter parameter : encoded.parameters()) {
            if (forbidden(parameter.name())) {
                throw new ValidateException(
                        "OpenID Connect callback decoder accepts only authorization-code flow query parameters");
            }
        }
        return switch (oauthDecoder.decode(encoded)) {
            case AuthorizationResponseDecoder.Success success -> success.response();
            case AuthorizationResponseDecoder.Error error -> error.response();
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

}
