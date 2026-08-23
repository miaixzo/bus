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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents an RFC 7662 token introspection response with token metadata isolated from the mandatory active flag.
 *
 * @param active     whether the token is currently active at the authorization server
 * @param metadata   optional token metadata, permitted only for an active token
 * @param extensions additional introspection response members
 * @author Kimi Liu
 */
public record IntrospectionResponse(boolean active, Optional<TokenMetadata> metadata,
        JsonValue.ObjectValue extensions) {

    /**
     * Validates active/inactive response structure and isolates extension members.
     *
     * @throws IllegalArgumentException if a component container is {@code null}
     * @throws ValidateException        if an inactive response contains metadata or extensions
     */
    public IntrospectionResponse {
        Assert.notNull(metadata, "OAuth introspection metadata container must not be null");
        Assert.notNull(extensions, "OAuth introspection extensions must not be null");
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "OAuth introspection extension member name must not be blank");
            if (standard(name)) {
                throw new ValidateException("OAuth introspection extension replaces a standard member");
            }
        }
        if (!active && (metadata.isPresent() || !extensions.values().isEmpty())) {
            throw new ValidateException("Inactive OAuth introspection response must contain only active=false");
        }
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies RFC 7662 response members represented by explicit components.
     *
     * @param name exact case-sensitive member name
     * @return {@code true} for active or a token metadata component
     */
    private static boolean standard(final String name) {
        return switch (name) {
            case OAuth2.Parameters.ACTIVE, OAuth2.Parameters.SCOPE, OAuth2.Parameters.CLIENT_ID, OAuth2.Parameters.USERNAME, OAuth2.Parameters.TOKEN_TYPE, JwtClaims.EXPIRATION, JwtClaims.ISSUED_AT, JwtClaims.NOT_BEFORE, JwtClaims.SUBJECT, JwtClaims.AUDIENCE, JwtClaims.ISSUER, JwtClaims.JWT_ID -> true;
            default -> false;
        };
    }

    /**
     * Returns a privacy-preserving diagnostic representation.
     *
     * @return summary exposing only whether the token is active
     */
    @Override
    public String toString() {
        return "IntrospectionResponse[active=" + active + ",tokenInformation=[REDACTED]]";
    }

    /**
     * Holds the optional token metadata members registered by RFC 7662.
     *
     * @param scope     optional token scope
     * @param clientId  optional client identifier associated with the token
     * @param username  optional resource owner username
     * @param tokenType optional registered access-token type
     * @param exp       optional expiration NumericDate in seconds
     * @param iat       optional issuance NumericDate in seconds
     * @param nbf       optional not-before NumericDate in seconds
     * @param subject   optional token subject StringOrURI
     * @param audience  ordered token audience StringOrURI values
     * @param issuer    optional token issuer StringOrURI
     * @param jwtId     optional token JWT identifier
     * @author Kimi Liu
     */
    public record TokenMetadata(Optional<Scope> scope, Optional<String> clientId, Optional<String> username,
            Optional<TokenType> tokenType, Optional<Long> exp, Optional<Long> iat, Optional<Long> nbf,
            Optional<String> subject, List<String> audience, Optional<String> issuer, Optional<String> jwtId) {

        /**
         * Validates and freezes all RFC 7662 token metadata components.
         *
         * @throws IllegalArgumentException if a container, list, or list entry is {@code null}
         * @throws ValidateException        if a StringOrURI value is malformed
         */
        public TokenMetadata {
            Assert.notNull(scope, "OAuth introspection scope container must not be null");
            Assert.notNull(clientId, "OAuth introspection client identifier container must not be null");
            Assert.notNull(username, "OAuth introspection username container must not be null");
            Assert.notNull(tokenType, "OAuth introspection token type container must not be null");
            Assert.notNull(exp, "OAuth introspection expiration container must not be null");
            Assert.notNull(iat, "OAuth introspection issued-at container must not be null");
            Assert.notNull(nbf, "OAuth introspection not-before container must not be null");
            Assert.notNull(subject, "OAuth introspection subject container must not be null");
            Assert.notNull(audience, "OAuth introspection audience list must not be null");
            Assert.notNull(issuer, "OAuth introspection issuer container must not be null");
            Assert.notNull(jwtId, "OAuth introspection JWT identifier container must not be null");
            clientId.ifPresent(
                    value -> Assert.notBlank(value, "OAuth introspection client identifier must not be blank"));
            username.ifPresent(value -> Assert.notBlank(value, "OAuth introspection username must not be blank"));
            subject.ifPresent(value -> validateStringOrUri(value, "OAuth introspection subject"));
            issuer.ifPresent(value -> validateStringOrUri(value, "OAuth introspection issuer"));
            jwtId.ifPresent(value -> Assert.notBlank(value, "OAuth introspection JWT identifier must not be blank"));
            final List<String> audiences = new ArrayList<>(audience.size());
            for (String value : audience) {
                validateStringOrUri(value, "OAuth introspection audience");
                audiences.add(value);
            }
            audience = List.copyOf(audiences);
        }

        /**
         * Validates the JWT StringOrURI syntax used by subject, audience, and issuer metadata.
         *
         * @param value candidate StringOrURI value
         * @param label safe diagnostic label
         */
        private static void validateStringOrUri(final String value, final String label) {
            Assert.notBlank(value, label + " must not be blank");
            if (value.indexOf(Symbol.C_COLON) >= 0) {
                try {
                    new URI(value);
                } catch (URISyntaxException cause) {
                    throw new ValidateException(label + " containing a colon must be a valid URI", cause);
                }
            }
        }

    }

}
