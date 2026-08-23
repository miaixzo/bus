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
package org.miaixz.bus.auth.source.protocol.oauth2;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents a successful OAuth 2.x token endpoint response.
 * <p>
 * Access and refresh tokens are sensitive bearer material. The record retains them for protocol processing but its
 * diagnostic representation never reveals either token or extension content.
 * </p>
 *
 * @param accessToken  issued access token
 * @param tokenType    registered access-token type
 * @param expiresIn    optional access-token lifetime in seconds
 * @param refreshToken optional refresh token
 * @param scope        optional effective scope when it differs from or was absent in the request
 * @param extensions   unknown response members that do not duplicate registered components
 * @author Kimi Liu
 */
public record TokenResponse(String accessToken, TokenType tokenType, Optional<Long> expiresIn,
        Optional<String> refreshToken, Optional<Scope> scope, JsonValue.ObjectValue extensions)
        implements TokenEndpointResponse {

    /**
     * Creates and validates an immutable successful token response.
     *
     * @throws IllegalArgumentException if a required component or optional container is {@code null}
     * @throws ValidateException        if token syntax, lifetime, token type URI, or extensions violate the active RFCs
     */
    public TokenResponse {
        Assert.notEmpty(accessToken, "OAuth 2.x access token must not be empty");
        requireVisibleAscii(accessToken, "OAuth 2.x access token");
        Assert.notNull(tokenType, "OAuth 2.x access token type must not be null");
        Assert.notNull(expiresIn, "OAuth 2.x token expiration container must not be null");
        Assert.notNull(refreshToken, "OAuth 2.x refresh token container must not be null");
        Assert.notNull(scope, "OAuth 2.x token response scope container must not be null");
        Assert.notNull(extensions, "OAuth 2.x token response extensions must not be null");

        final Long lifetime = expiresIn.getOrNull();
        if (lifetime != null && lifetime < 0L) {
            throw new ValidateException("OAuth 2.x access token lifetime must not be negative");
        }
        final String refresh = refreshToken.getOrNull();
        if (refresh != null) {
            Assert.notEmpty(refresh, "OAuth 2.x refresh token must not be empty when present");
            requireVisibleAscii(refresh, "OAuth 2.x refresh token");
        }
        final Scope effectiveScope = scope.getOrNull();
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "OAuth token response extension member name must not be blank");
            if (standard(name)) {
                throw new ValidateException("OAuth 2.x token response extensions duplicate member: " + name);
            }
        }

        expiresIn = Optional.ofNullable(lifetime);
        refreshToken = Optional.ofNullable(refresh);
        scope = Optional.ofNullable(effectiveScope);
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies ordinary OAuth token response members represented by explicit components.
     *
     * @param name exact response member name
     * @return {@code true} for an RFC 6749 success component
     */
    private static boolean standard(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.REFRESH_TOKEN, OAuth2.Parameters.SCOPE -> true;
            default -> false;
        };
    }

    /**
     * Validates a non-empty RFC 6749 {@code VSCHAR} token value.
     *
     * @param value token value to validate
     * @param label safe component label used in diagnostics
     * @throws ValidateException if a character lies outside visible ASCII
     */
    private static void requireVisibleAscii(final String value, final String label) {
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x20 || character > 0x7e) {
                throw new ValidateException(label + " contains a character outside RFC 6749 VSCHAR");
            }
        }
    }

    /**
     * Returns a diagnostic representation that omits all token and extension values.
     *
     * @return redacted token response summary
     */
    @Override
    public String toString() {
        return "TokenResponse[accessToken=[REDACTED], tokenType=" + tokenType.value() + ", expiresIn=" + expiresIn
                + ", refreshToken=[REDACTED], scope=" + scope + ", extensions=[REDACTED]]";
    }

}
