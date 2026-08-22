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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents the OpenID Connect claims carried by a verified or locally issued ID Token.
 * <p>
 * The model preserves extension claims but performs only syntax and type validation. Signature, issuer, audience,
 * authorized-party, time, nonce, and token-hash bindings belong to {@code IdTokenVerifier}.
 * </p>
 *
 * @param issuer                     case-sensitive OpenID Provider issuer identifier mapped from {@code iss}
 * @param subject                    locally unique subject identifier mapped from {@code sub}
 * @param audience                   ordered non-empty audience values mapped from {@code aud}
 * @param expiration                 expiration instant mapped from the {@code exp} NumericDate
 * @param issuedAt                   issuance instant mapped from the {@code iat} NumericDate
 * @param authenticatedAt            optional authentication instant mapped from {@code auth_time}
 * @param nonce                      optional replay-binding value copied from the Authentication Request
 * @param authenticationContextClass optional Authentication Context Class Reference mapped from {@code acr}
 * @param authenticationMethods      ordered Authentication Methods References mapped from {@code amr}
 * @param authorizedParty            optional authorized party mapped from {@code azp}
 * @param accessTokenHash            optional access-token hash mapped from {@code at_hash}
 * @param codeHash                   optional authorization-code hash mapped from {@code c_hash}
 * @param stateHash                  optional state hash mapped from {@code s_hash}
 * @param sessionId                  optional OpenID Provider session identifier mapped from {@code sid}
 * @param extensions                 additional ID Token claims that do not duplicate registered components
 * @author Kimi Liu
 */
public record IdTokenClaims(String issuer, String subject, List<String> audience, Instant expiration, Instant issuedAt,
        Optional<Instant> authenticatedAt, Optional<String> nonce, Optional<String> authenticationContextClass,
        List<String> authenticationMethods, Optional<String> authorizedParty, Optional<String> accessTokenHash,
        Optional<String> codeHash, Optional<String> stateHash, Optional<String> sessionId,
        JsonValue.ObjectValue extensions) {

    /**
     * Creates and validates an immutable OpenID Connect ID Token Claims Set.
     *
     * @throws IllegalArgumentException if a required value or container is {@code null}
     * @throws ValidateException        if an issuer, StringOrURI, list, hash, or extension claim violates OIDC syntax
     */
    public IdTokenClaims {
        issuer = validateIssuer(issuer);
        subject = stringOrUri(subject, "OpenID Connect ID Token subject");
        audience = uniqueStrings(audience, "OpenID Connect ID Token audience", true, true);
        Assert.notNull(expiration, "OpenID Connect ID Token expiration must not be null");
        Assert.notNull(issuedAt, "OpenID Connect ID Token issued-at time must not be null");
        Assert.notNull(authenticatedAt, "OpenID Connect authentication-time container must not be null");
        authenticatedAt = Optional.ofNullable(authenticatedAt.getOrNull());
        nonce = optionalText(nonce, "OpenID Connect ID Token nonce");
        authenticationContextClass = optionalStringOrUri(
                authenticationContextClass,
                "OpenID Connect authentication context class");
        authenticationMethods = uniqueStrings(
                authenticationMethods,
                "OpenID Connect authentication method",
                false,
                false);
        authorizedParty = optionalText(authorizedParty, "OpenID Connect authorized party");
        accessTokenHash = optionalHash(accessTokenHash, "OpenID Connect access-token hash");
        codeHash = optionalHash(codeHash, "OpenID Connect authorization-code hash");
        stateHash = optionalHash(stateHash, "OpenID Connect state hash");
        sessionId = optionalText(sessionId, "OpenID Connect Session identifier");
        Assert.notNull(extensions, "OpenID Connect ID Token extension claims must not be null");
        for (String name : extensions.values().keySet()) {
            if (registered(name)) {
                throw new ValidateException("OpenID Connect ID Token extensions duplicate claim: " + name);
            }
        }
        extensions = new JsonValue.ObjectValue(extensions.values());
    }

    /**
     * Identifies ID Token claims represented by explicit model components.
     *
     * @param name exact claim name
     * @return {@code true} for a registered JWT or OpenID Connect claim component
     */
    private static boolean registered(final String name) {
        return switch (name) {
            case JwtClaims.ISSUER, JwtClaims.SUBJECT, JwtClaims.AUDIENCE, JwtClaims.EXPIRATION, JwtClaims.ISSUED_AT, OpenIdConnect.Claims.AUTH_TIME, OpenIdConnect.Claims.NONCE, OpenIdConnect.Claims.ACR, OpenIdConnect.Claims.AMR, OpenIdConnect.Claims.AUTHORIZED_PARTY, OpenIdConnect.Claims.ACCESS_TOKEN_HASH, OpenIdConnect.Claims.CODE_HASH, OpenIdConnect.Claims.STATE_HASH, OpenIdConnect.Claims.SESSION_ID -> true;
            default -> false;
        };
    }

    /**
     * Validates the OpenID Provider issuer URL.
     *
     * @param value issuer wire value
     * @return unchanged issuer
     * @throws ValidateException if the value is not an HTTPS URL without userinfo, query, or fragment
     */
    private static String validateIssuer(final String value) {
        Assert.notBlank(value, "OpenID Connect ID Token issuer must not be blank");
        final Url url;
        try {
            url = Url.parse(value);
        } catch (RuntimeException cause) {
            throw new ValidateException("OpenID Connect ID Token issuer must be a valid URL", cause);
        }
        if (!Protocol.HTTPS.name.equals(url.scheme()) || url.host().isEmpty() || !url.username().isEmpty()
                || !url.password().isEmpty() || !url.query().isEmpty() || url.fragment() != null
                || !value.equals(value.trim())) {
            throw new ValidateException(
                    "OpenID Connect ID Token issuer must be an HTTPS URL without userinfo, query, or fragment");
        }
        return value;
    }

    /**
     * Freezes one ordered list and rejects blank or duplicate values.
     *
     * @param values            source list
     * @param label             safe diagnostic label for each entry
     * @param required          whether the list must be non-empty
     * @param validateUriSyntax whether each entry uses JWT StringOrURI syntax
     * @return immutable ordered values
     */
    private static List<String> uniqueStrings(
            final List<String> values,
            final String label,
            final boolean required,
            final boolean validateUriSyntax) {
        Assert.notNull(values, label + " list must not be null");
        if (required && values.isEmpty()) {
            throw new ValidateException(label + " list must not be empty");
        }
        final Set<String> unique = new HashSet<>(values.size());
        for (String value : values) {
            if (validateUriSyntax) {
                stringOrUri(value, label);
            } else {
                Assert.notBlank(value, label + " must not be blank");
            }
            if (!unique.add(value)) {
                throw new ValidateException(label + " list must not contain duplicates");
            }
        }
        return List.copyOf(values);
    }

    /**
     * Normalizes an optional non-blank OIDC string.
     *
     * @param value optional source container
     * @param label safe diagnostic label
     * @return normalized Bus optional
     */
    private static Optional<String> optionalText(final Optional<String> value, final String label) {
        Assert.notNull(value, label + " container must not be null");
        final String present = value.getOrNull();
        if (present != null) {
            Assert.notBlank(present, label + " must not be blank");
        }
        return Optional.ofNullable(present);
    }

    /**
     * Normalizes an optional JWT StringOrURI value.
     *
     * @param value optional source container
     * @param label safe diagnostic label
     * @return normalized Bus optional
     */
    private static Optional<String> optionalStringOrUri(final Optional<String> value, final String label) {
        final Optional<String> normalized = optionalText(value, label);
        final String present = normalized.getOrNull();
        if (present != null) {
            stringOrUri(present, label);
        }
        return normalized;
    }

    /**
     * Normalizes and validates an optional OIDC hash claim.
     *
     * @param value optional source container
     * @param label safe diagnostic label
     * @return normalized Bus optional
     */
    private static Optional<String> optionalHash(final Optional<String> value, final String label) {
        final Optional<String> normalized = optionalText(value, label);
        final String present = normalized.getOrNull();
        if (present != null) {
            for (int index = 0; index < present.length(); index++) {
                final char character = present.charAt(index);
                if (!((character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z)
                        || (character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z)
                        || (character >= Symbol.C_ZERO && character <= Symbol.C_NINE) || character == Symbol.C_MINUS
                        || character == Symbol.C_UNDERLINE)) {
                    throw new ValidateException(label + " must use unpadded base64url encoding");
                }
            }
        }
        return normalized;
    }

    /**
     * Validates a JWT StringOrURI value.
     *
     * @param value candidate string
     * @param label safe diagnostic label
     * @return unchanged value
     * @throws ValidateException if a value containing a colon is not a valid URI
     */
    private static String stringOrUri(final String value, final String label) {
        Assert.notBlank(value, label + " must not be blank");
        if (value.indexOf(Symbol.C_COLON) >= 0) {
            try {
                new URI(value);
            } catch (URISyntaxException cause) {
                throw new ValidateException(label + " must satisfy JWT StringOrURI syntax", cause);
            }
        }
        return value;
    }

    /**
     * Returns a diagnostic summary without nonce, hashes, or extension claim values.
     *
     * @return redacted ID Token claim summary
     */
    @Override
    public String toString() {
        return "IdTokenClaims[issuer=" + issuer + ", subject=" + subject + ", audience=" + audience + ", expiration="
                + expiration + ", issuedAt=" + issuedAt + ", authenticatedAt=" + authenticatedAt
                + ", nonce=[REDACTED], authenticationContextClass=" + authenticationContextClass
                + ", authenticationMethods=" + authenticationMethods + ", authorizedParty=" + authorizedParty
                + ", accessTokenHash=[REDACTED], codeHash=[REDACTED], stateHash=[REDACTED]"
                + ", sessionId=[REDACTED], extensions=[REDACTED]]";
    }

}
