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
package org.miaixz.bus.auth.protocol.oidc;

import java.util.LinkedHashMap;
import java.util.Map;

import org.miaixz.bus.auth.protocol.oauth2.Scope;
import org.miaixz.bus.auth.protocol.oauth2.TokenEndpointResponse;
import org.miaixz.bus.auth.protocol.oauth2.TokenResponse;
import org.miaixz.bus.auth.protocol.oauth2.TokenType;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an OpenID Connect token endpoint success by composing the OAuth token response with its ID Token.
 *
 * @param tokenResponse standard OAuth token response components
 * @param idToken       issued OpenID Connect ID Token
 * @author Kimi Liu
 */
public record OpenIdTokenResponse(TokenResponse tokenResponse, IdToken idToken) implements TokenEndpointResponse {

    /**
     * Validates the composed OAuth and OpenID Connect response components.
     *
     * @throws IllegalArgumentException if a component is {@code null}
     */
    public OpenIdTokenResponse {
        Assert.notNull(tokenResponse, "OpenID Connect OAuth token response must not be null");
        Assert.notNull(idToken, "OpenID Connect ID Token must not be null");
    }

    /**
     * Extracts the registered OpenID Connect {@code id_token} member from an OAuth-decoded response.
     * <p>
     * OAuth codecs preserve protocol extensions while remaining independent of OpenID Connect. This boundary removes
     * the OIDC member from the OAuth extension object and represents it through the typed {@link IdToken} component.
     * </p>
     *
     * @param response OAuth token response containing the OIDC token extension
     * @return composed OpenID Connect token response
     * @throws ValidateException if the response omits a non-empty string {@code id_token}
     */
    public static OpenIdTokenResponse from(final TokenResponse response) {
        Assert.notNull(response, "OAuth token response must not be null");
        final Map<String, JsonValue> extensions = new LinkedHashMap<>(response.extensions().values());
        final JsonValue encoded = extensions.remove(OpenIdConnect.Parameters.ID_TOKEN);
        if (!(encoded instanceof JsonValue.StringValue compact) || compact.value().isBlank()) {
            throw new ValidateException("OpenID Connect token response requires a non-empty id_token");
        }
        final TokenResponse oauth = new TokenResponse(response.accessToken(), response.tokenType(),
                response.expiresIn(), response.refreshToken(), response.scope(), new JsonValue.ObjectValue(extensions));
        return new OpenIdTokenResponse(oauth, new IdToken(compact.value()));
    }

    /**
     * Returns the composed OAuth access token.
     *
     * @return sensitive OAuth access token
     */
    public String accessToken() {
        return tokenResponse.accessToken();
    }

    /**
     * Returns the composed OAuth token type.
     *
     * @return registered access-token type
     */
    public TokenType tokenType() {
        return tokenResponse.tokenType();
    }

    /**
     * Returns the composed OAuth access-token lifetime.
     *
     * @return optional lifetime in seconds
     */
    public Optional<Long> expiresIn() {
        return tokenResponse.expiresIn();
    }

    /**
     * Returns the composed OAuth refresh token.
     *
     * @return optional sensitive refresh token
     */
    public Optional<String> refreshToken() {
        return tokenResponse.refreshToken();
    }

    /**
     * Returns the composed OAuth effective scope.
     *
     * @return optional effective scope
     */
    public Optional<Scope> scope() {
        return tokenResponse.scope();
    }

    /**
     * Returns unregistered OAuth response extensions after removal of the OIDC ID Token.
     *
     * @return immutable extension object
     */
    public JsonValue.ObjectValue extensions() {
        return tokenResponse.extensions();
    }

    /**
     * Returns a diagnostic representation without token material.
     *
     * @return redacted OpenID Connect token response summary
     */
    @Override
    public String toString() {
        return "OpenIdTokenResponse[tokenResponse=[REDACTED],idToken=[REDACTED]]";
    }

}
