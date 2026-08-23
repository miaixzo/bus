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
package org.miaixz.bus.auth.shared.dpop;

import java.time.Instant;
import java.util.Set;

import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.Jwk;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Retains a structurally valid RFC 9449 DPoP proof JWT without exposing its replayable compact form in diagnostics.
 * <p>
 * Construction validates the protected JOSE header and DPoP claim grammar but does not establish signature validity.
 * Only {@link DpopIssuer} and {@link DpopVerifier} establish cryptographic provenance for normal framework use.
 * </p>
 *
 * @author Kimi Liu
 */
public class DpopProof {

    /**
     * Required DPoP proof media type value.
     */
    public static final String TYPE = "dpop+jwt";
    /**
     * HTTP method claim name.
     */
    public static final String HTTP_METHOD = "htm";
    /**
     * HTTP target URI claim name.
     */
    public static final String HTTP_URI = "htu";
    /**
     * Access-token hash claim name.
     */
    public static final String ACCESS_TOKEN_HASH = "ath";
    /**
     * Authorization server nonce claim name.
     */
    public static final String NONCE = "nonce";

    /**
     * Replayable compact JWS retained for protocol transmission only.
     */
    private final String compact;
    /**
     * Integrity-protected DPoP JOSE header.
     */
    private final JoseHeader header;
    /**
     * Complete DPoP Claims Set including extension claims.
     */
    private final JwtClaims claims;
    /**
     * Validated public asymmetric JWK embedded in the protected header.
     */
    private final Jwk publicKey;

    /**
     * Creates a structurally validated proof model from its exact compact, header, and Claims Set representations.
     *
     * @param compact exact three-segment compact JWS
     * @param header  parsed protected JOSE header
     * @param claims  complete implementation-neutral DPoP Claims Set
     * @throws IllegalArgumentException if an argument is {@code null} or a required string is blank
     * @throws ValidateException        if the header, key, URI, method, or claims violate RFC 9449 grammar
     */
    public DpopProof(final String compact, final JoseHeader header, final JwtClaims claims) {
        this.compact = requireCompact(compact);
        this.header = Assert.notNull(header, "DPoP JOSE header must not be null");
        this.claims = Assert.notNull(claims, "DPoP Claims Set must not be null");
        validateHeader(header);
        this.publicKey = embeddedPublicKey(header);
        validateClaims(claims);
    }

    /**
     * Produces the RFC 9449 comparison form of an HTTP target URI with query and fragment omitted.
     *
     * @param uri parsed request target
     * @return normalized absolute URI used in the {@code htu} claim
     */
    static String normalize(final Url uri) {
        Assert.notNull(uri, "DPoP HTTP target URI must not be null");
        validateHttpTarget(uri);
        final int port = uri.port() == Url.defaultPort(uri.scheme()) ? -1 : uri.port();
        return Url.builder().scheme(uri.scheme()).host(uri.host()).port(port).path(uri.path()).build().encoded();
    }

    /**
     * Extracts and validates the public JWK needed before signature verification.
     *
     * @param header parsed DPoP JOSE header
     * @return public asymmetric JWK
     */
    static Jwk embeddedPublicKey(final JoseHeader header) {
        final JsonValue value = header.parameter(JoseHeader.JSON_WEB_KEY)
                .orElseThrow(() -> new ValidateException("DPoP protected header requires jwk"));
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("DPoP jwk header parameter must be a JSON object");
        }
        final Jwk key = new Jwk(object);
        if (key.hasPrivateMaterial()) {
            throw new ValidateException("DPoP protected header must not contain private key material");
        }
        return key.publicOnly();
    }

    /**
     * Validates protected-header type, algorithm, and embedded-key placement.
     *
     * @param candidate DPoP JOSE header
     */
    private static void validateHeader(final JoseHeader candidate) {
        if (!candidate.unprotectedParameters().values().isEmpty()) {
            throw new ValidateException("DPoP compact proof must not contain an unprotected header");
        }
        final Set<String> required = Set.of(JoseHeader.TYPE, JoseHeader.ALGORITHM, JoseHeader.JSON_WEB_KEY);
        if (!candidate.protectedParameters().values().keySet().containsAll(required)) {
            throw new ValidateException("DPoP proof requires protected typ, alg, and jwk parameters");
        }
        if (candidate.type().filter(TYPE::equalsIgnoreCase).isEmpty()) {
            throw new ValidateException("DPoP proof typ must be dpop+jwt");
        }
        final JwaAlgorithm algorithm = JwaAlgorithm.of(candidate.algorithm());
        final JwaAlgorithm.Registration registration = algorithm.require(JwaAlgorithm.Kind.SIGNATURE);
        if (registration.keyTypes().contains("oct")) {
            throw new ValidateException("DPoP proof requires an asymmetric signature algorithm");
        }
    }

    /**
     * Validates required proof claim types and lexical restrictions.
     *
     * @param candidate parsed DPoP Claims Set
     */
    private static void validateClaims(final JwtClaims candidate) {
        final String method = requiredString(candidate, HTTP_METHOD);
        final Http.Method resolved;
        try {
            resolved = Http.Method.of(method);
        } catch (IllegalArgumentException cause) {
            throw new ValidateException("DPoP htm is not a supported HTTP method", cause);
        }
        if (!resolved.value().equals(method) || resolved == Http.Method.ALL || resolved == Http.Method.NONE
                || resolved == Http.Method.BEFORE || resolved == Http.Method.AFTER) {
            throw new ValidateException("DPoP htm must be an exact HTTP wire method token");
        }
        final String target = requiredString(candidate, HTTP_URI);
        if (!target.equals(target.trim())) {
            throw new ValidateException("DPoP htu must not contain surrounding whitespace");
        }
        validateHttpTarget(Url.parse(target));
        candidate.issuedAt().orElseThrow(() -> new ValidateException("DPoP proof requires NumericDate iat"));
        candidate.jwtId().orElseThrow(() -> new ValidateException("DPoP proof requires string jti"));
        validateOptionalString(candidate, ACCESS_TOKEN_HASH);
        validateOptionalString(candidate, NONCE);
    }

    /**
     * Validates an absolute HTTP(S) target without credentials, query, or fragment.
     *
     * @param target parsed candidate URI
     */
    private static void validateHttpTarget(final Url target) {
        if (!Protocol.HTTP.name.equals(target.scheme()) && !Protocol.HTTPS.name.equals(target.scheme())) {
            throw new ValidateException("DPoP htu must use HTTP or HTTPS");
        }
        if (!target.username().isEmpty() || !target.password().isEmpty() || target.querySize() != 0
                || target.fragment() != null) {
            throw new ValidateException("DPoP htu must omit user-info, query, and fragment");
        }
    }

    /**
     * Requires one non-empty string claim.
     *
     * @param source Claims Set containing the member
     * @param name   exact claim name
     * @return non-empty string claim value
     */
    private static String requiredString(final JwtClaims source, final String name) {
        return optionalString(source, name)
                .orElseThrow(() -> new ValidateException("DPoP proof requires string claim " + name));
    }

    /**
     * Reads one optional non-empty string claim without coercion.
     *
     * @param source Claims Set containing the member
     * @param name   exact claim name
     * @return exact claim value when present
     */
    private static Optional<String> optionalString(final JwtClaims source, final String name) {
        final Optional<JsonValue> value = source.claim(name);
        if (value.isEmpty()) {
            return Optional.empty();
        }
        if (!(value.getOrThrow() instanceof JsonValue.StringValue string) || string.value().isEmpty()) {
            throw new ValidateException("DPoP claim " + name + " must be a non-empty JSON string");
        }
        return Optional.of(string.value());
    }

    /**
     * Validates an optional DPoP string claim when present.
     *
     * @param source Claims Set containing the member
     * @param name   exact claim name
     */
    private static void validateOptionalString(final JwtClaims source, final String name) {
        optionalString(source, name);
    }

    /**
     * Validates an exact three-segment compact representation without parsing its contents twice.
     *
     * @param value compact candidate
     * @return validated compact value
     */
    private static String requireCompact(final String value) {
        Assert.notBlank(value, "DPoP compact proof must not be blank");
        final String[] segments = value.split("\\.", -1);
        if (segments.length != 3 || segments[0].isEmpty() || segments[1].isEmpty() || segments[2].isEmpty()) {
            throw new ValidateException("DPoP proof must use three-segment JWS Compact Serialization");
        }
        return value;
    }

    /**
     * Returns the exact replayable compact proof for wire transmission.
     *
     * @return compact DPoP proof JWT
     */
    public String compact() {
        return compact;
    }

    /**
     * Returns the complete integrity-protected JOSE header.
     *
     * @return immutable JOSE header
     */
    public JoseHeader header() {
        return header;
    }

    /**
     * Returns the complete Claims Set including unknown extension claims.
     *
     * @return immutable JWT Claims Set
     */
    public JwtClaims claims() {
        return claims;
    }

    /**
     * Returns the public asymmetric JWK carried by the protected header.
     *
     * @return validated public JWK
     */
    public Jwk publicKey() {
        return publicKey;
    }

    /**
     * Returns the exact case-sensitive HTTP method claim.
     *
     * @return RFC 9110 method token
     */
    public String httpMethod() {
        return requiredString(claims, HTTP_METHOD);
    }

    /**
     * Returns the absolute target URI claim without query or fragment.
     *
     * @return HTTP target URI lexical value
     */
    public String httpUri() {
        return requiredString(claims, HTTP_URI);
    }

    /**
     * Returns the proof issuance instant.
     *
     * @return required JWT issued-at value
     */
    public Instant issuedAt() {
        return claims.issuedAt().orElseThrow(() -> new ValidateException("DPoP proof requires iat"));
    }

    /**
     * Returns the unique proof identifier.
     *
     * @return required JWT identifier
     */
    public String jwtId() {
        return claims.jwtId().orElseThrow(() -> new ValidateException("DPoP proof requires jti"));
    }

    /**
     * Returns the optional access-token hash claim.
     *
     * @return unpadded Base64URL SHA-256 hash when present
     */
    public Optional<String> accessTokenHash() {
        return optionalString(claims, ACCESS_TOKEN_HASH);
    }

    /**
     * Returns the optional authorization server nonce claim.
     *
     * @return nonce value when present
     */
    public Optional<String> nonce() {
        return optionalString(claims, NONCE);
    }

    /**
     * Computes the RFC 7638 SHA-256 JWK thumbprint used by the OAuth {@code cnf.jkt} member.
     *
     * @return unpadded Base64URL thumbprint
     * @throws ValidateException if the embedded key type has no DPoP thumbprint profile in this version
     */
    public String confirmationThumbprint() {
        final String canonical = switch (publicKey.keyType()) {
            case "RSA" -> "{\"e\":\"" + jwkString("e") + "\",\"kty\":\"RSA\",\"n\":\"" + jwkString("n") + "\"}";
            case "EC" -> "{\"crv\":\"" + jwkString("crv") + "\",\"kty\":\"EC\",\"x\":\"" + jwkString("x")
                    + "\",\"y\":\"" + jwkString("y") + "\"}";
            case "OKP" -> "{\"crv\":\"" + jwkString("crv") + "\",\"kty\":\"OKP\",\"x\":\"" + jwkString("x") + "\"}";
            default -> throw new ValidateException("DPoP public key type cannot produce an RFC 7638 thumbprint");
        };
        return Base64.encodeUrlSafe(Builder.sha256(canonical.getBytes(Charset.UTF_8)));
    }

    /**
     * Returns a fixed diagnostic label that does not disclose proof or claim material.
     *
     * @return redacted proof representation
     */
    @Override
    public String toString() {
        return "DpopProof[compact=[REDACTED]]";
    }

    /**
     * Returns one required public JWK string member used by RFC 7638 canonicalization.
     *
     * @param name exact JWK member name
     * @return decoded string value
     */
    private String jwkString(final String name) {
        final JsonValue value = publicKey.parameter(name)
                .orElseThrow(() -> new ValidateException("DPoP public JWK lacks thumbprint member " + name));
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("DPoP public JWK thumbprint member must be a string");
        }
        return string.value();
    }

}
