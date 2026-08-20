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
import java.net.URISyntaxException;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an RFC 8693 successful token exchange response with its mandatory issued token type.
 *
 * @param accessToken     issued token value carried in {@code access_token}
 * @param issuedTokenType absolute URI identifying the semantic type of the issued token
 * @param tokenType       access-token usage type, including {@code N_A} for a non-access token
 * @param expiresIn       optional lifetime in seconds
 * @param scope           optional effective scope
 * @param refreshToken    optional refresh token
 * @param extensions      additional token exchange response members
 * @author Kimi Liu
 */
public record TokenExchangeResponse(String accessToken, String issuedTokenType, TokenType tokenType,
        Optional<Long> expiresIn, Optional<Scope> scope, Optional<String> refreshToken,
        JsonValue.ObjectValue extensions) implements TokenEndpointResponse {

    /**
     * Validates RFC 8693 response components and isolates extensions.
     *
     * @throws IllegalArgumentException if a component or container is {@code null}
     * @throws ValidateException        if token, URI, lifetime, or extension semantics are invalid
     */
    public TokenExchangeResponse {
        Assert.notBlank(accessToken, "OAuth token exchange access token must not be blank");
        requireVisibleAscii(accessToken, "OAuth token exchange access token");
        requireAbsoluteUri(issuedTokenType);
        Assert.notNull(tokenType, "OAuth token exchange token type must not be null");
        Assert.notNull(expiresIn, "OAuth token exchange lifetime container must not be null");
        Assert.notNull(scope, "OAuth token exchange scope container must not be null");
        Assert.notNull(refreshToken, "OAuth token exchange refresh-token container must not be null");
        Assert.notNull(extensions, "OAuth token exchange extensions must not be null");
        expiresIn.filter(value -> value < 0L).ifPresent(value -> {
            throw new ValidateException("OAuth token exchange lifetime must not be negative");
        });
        refreshToken.ifPresent(value -> {
            Assert.notBlank(value, "OAuth token exchange refresh token must not be blank");
            requireVisibleAscii(value, "OAuth token exchange refresh token");
        });
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "OAuth token exchange extension member name must not be blank");
            if (standard(name)) {
                throw new ValidateException("OAuth token exchange extension replaces a standard component");
            }
        }
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies response members represented by explicit components.
     *
     * @param name exact response member name
     * @return {@code true} for an RFC 8693 or inherited OAuth token component
     */
    private static boolean standard(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ACCESS_TOKEN, OAuth2.Parameters.ISSUED_TOKEN_TYPE, OAuth2.Parameters.TOKEN_TYPE, OAuth2.Parameters.EXPIRES_IN, OAuth2.Parameters.SCOPE, OAuth2.Parameters.REFRESH_TOKEN -> true;
            default -> false;
        };
    }

    /**
     * Validates the required absolute token type URI.
     *
     * @param value issued token type
     */
    private static void requireAbsoluteUri(final String value) {
        Assert.notBlank(value, "OAuth issued token type must not be blank");
        try {
            if (!new URI(value).isAbsolute()) {
                throw new ValidateException("OAuth issued token type must be an absolute URI");
            }
        } catch (URISyntaxException cause) {
            throw new ValidateException("OAuth issued token type must be a valid absolute URI", cause);
        }
    }

    /**
     * Validates RFC 6749 VSCHAR token syntax.
     *
     * @param value token value
     * @param label safe diagnostic label
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
     * Returns a redacted token exchange summary.
     *
     * @return non-sensitive response summary
     */
    @Override
    public String toString() {
        return "TokenExchangeResponse[accessToken=[REDACTED],issuedTokenType=" + issuedTokenType + ",tokenType="
                + tokenType + ",expiresIn=" + expiresIn + ",scope=" + scope
                + ",refreshToken=[REDACTED],extensions=[REDACTED]]";
    }

}
