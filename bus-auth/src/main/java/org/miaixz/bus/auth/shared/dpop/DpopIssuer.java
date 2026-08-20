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

import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.Jwk;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.data.id.UUID;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;
import org.miaixz.bus.fabric.Clock;
import org.miaixz.bus.fabric.UnoUrl;

/**
 * Issues one RFC 9449 DPoP proof JWT for an exact outbound HTTP request and optional access token or server nonce.
 * <p>
 * The issuer owns standard proof members and accepts extensions only through isolated immutable JSON objects. It does
 * not store proofs, select signing keys, or infer OAuth requests from application state.
 * </p>
 *
 * @author Kimi Liu
 */
public final class DpopIssuer {

    /**
     * Provider-neutral JSON codec used for protected header and Claims Set octets.
     */
    private final JsonProvider jsonProvider;
    /**
     * Profile-scoped shared JWS execution service.
     */
    private final JwsService jwsService;
    /**
     * Fabric clock used for the proof NumericDate.
     */
    private final Clock clock;

    /**
     * Creates a DPoP issuer with explicit JSON, signing, and time dependencies.
     *
     * @param jsonProvider provider-neutral JSON codec
     * @param jwsService   profile-scoped JWS service
     * @param clock        shared Fabric clock
     */
    public DpopIssuer(final JsonProvider jsonProvider, final JwsService jwsService, final Clock clock) {
        this.jsonProvider = Assert.notNull(jsonProvider, "DPoP issuer JSON provider must not be null");
        this.jwsService = Assert.notNull(jwsService, "DPoP issuer JWS service must not be null");
        this.clock = Assert.notNull(clock, "DPoP issuer clock must not be null");
    }

    /**
     * Creates the protected-only DPoP JOSE header after inserting extension members without replacement.
     *
     * @param request validated issue request
     * @return protected-only JOSE header
     */
    private static JoseHeader header(final Request request) {
        return JoseHeader.jws(
                request.algorithm(),
                Optional.empty(),
                Optional.of(request.publicKey()),
                Optional.of(DpopProof.TYPE),
                request.protectedExtensions());
    }

    /**
     * Creates standard DPoP claims and appends non-conflicting extension claims.
     *
     * @param request  validated issue request
     * @param issuedAt single clock reading used by the proof
     * @return complete immutable Claims Set
     */
    private static JwtClaims claims(final Request request, final Instant issuedAt) {
        final Optional<String> accessTokenHash = request.accessToken().map(DpopIssuer::accessTokenHash);
        final ProofClaims proof = new ProofClaims(request.method(), DpopProof.normalize(request.uri()), accessTokenHash,
                request.nonce(), request.claimExtensions());
        final JwtClaims.Registered registered = new JwtClaims.Registered(Optional.empty(), Optional.empty(), List.of(),
                false, Optional.empty(), Optional.empty(), Optional.of(issuedAt),
                Optional.of(UUID.randomUUID().toString()));
        return new JwtClaims(registered, proof.extensions());
    }

    /**
     * Computes the RFC 9449 access-token hash over its exact ASCII octets.
     *
     * @param accessToken exact access-token wire value
     * @return unpadded Base64URL SHA-256 hash
     */
    private static String accessTokenHash(final String accessToken) {
        final byte[] ascii = accessToken.getBytes(Charset.US_ASCII);
        try {
            return Base64.encodeUrlSafe(Builder.sha256(ascii));
        } finally {
            Arrays.fill(ascii, (byte) 0);
        }
    }

    /**
     * Rejects a DPoP claim extension that attempts to replace a proof-owned member.
     *
     * @param extensions extension object under validation
     */
    private static void rejectProofClaims(final JsonValue.ObjectValue extensions) {
        for (String name : extensions.values().keySet()) {
            Assert.notBlank(name, "DPoP claim extension name must not be blank");
            if (name.equals(DpopProof.HTTP_METHOD) || name.equals(DpopProof.HTTP_URI)
                    || name.equals(DpopProof.ACCESS_TOKEN_HASH) || name.equals(DpopProof.NONCE)) {
                throw new ValidateException("DPoP claim extension must not replace a proof-owned member");
            }
        }
    }

    /**
     * Validates a non-empty ASCII protocol value before byte-level hashing.
     *
     * @param value candidate wire value
     * @param label safe semantic label
     */
    private static void validateAscii(final String value, final String label) {
        Assert.notBlank(value, label + " must not be blank");
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character < 0x21 || character > 0x7e) {
                throw new ValidateException(label + " must contain visible ASCII characters only");
            }
        }
    }

    /**
     * Generates and signs one request-bound proof using the explicitly supplied private signing key.
     *
     * @param request    exact request target, algorithm, public key, optional bindings, and extensions
     * @param signingKey asymmetric private signing key or key pair accepted by the shared JWS service
     * @return immutable signed DPoP proof
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if the request, extensions, key, or algorithm violates the DPoP profile
     */
    public DpopProof issue(final Request request, final Key signingKey) {
        Assert.notNull(request, "DPoP issue request must not be null");
        Assert.notNull(signingKey, "DPoP signing key must not be null");
        final Instant issuedAt = clock.now();
        final JoseHeader header = header(request);
        final JwtClaims claims = claims(request, issuedAt);
        final byte[] payload = jsonProvider.writeValue(claims.values());
        final JwsService.Signature signature = jwsService.sign(header, payload, signingKey);
        final String compact = jwsService.compact(new JwsService.Jws(payload, List.of(signature)));
        return new DpopProof(compact, header, claims);
    }

    /**
     * Defines one exact outbound HTTP request and the key material advertised by its DPoP proof.
     *
     * @param method              standard HTTP wire method
     * @param uri                 absolute request target; query and fragment are omitted from {@code htu}
     * @param algorithm           asymmetric JWS algorithm
     * @param publicKey           public JWK corresponding to the signing key
     * @param accessToken         optional access token whose exact ASCII serialization is bound through {@code ath}
     * @param nonce               optional authorization server nonce
     * @param protectedExtensions non-conflicting protected JOSE extension members
     * @param claimExtensions     non-conflicting DPoP claim extensions
     * @author Kimi Liu
     */
    public record Request(Http.Method method, UnoUrl uri, JwaAlgorithm algorithm, Jwk publicKey,
            Optional<String> accessToken, Optional<String> nonce, JsonValue.ObjectValue protectedExtensions,
            JsonValue.ObjectValue claimExtensions) {

        /**
         * Validates, detaches, and freezes all issue parameters before signing can occur.
         *
         * @throws IllegalArgumentException if a component is {@code null}
         * @throws ValidateException        if the method, algorithm, key, secret text, URI, or extensions are invalid
         */
        public Request {
            Assert.notNull(method, "DPoP HTTP method must not be null");
            Assert.notNull(uri, "DPoP HTTP target URI must not be null");
            Assert.notNull(algorithm, "DPoP JWS algorithm must not be null");
            Assert.notNull(publicKey, "DPoP public JWK must not be null");
            Assert.notNull(accessToken, "DPoP access-token container must not be null");
            Assert.notNull(nonce, "DPoP nonce container must not be null");
            Assert.notNull(protectedExtensions, "DPoP protected extensions must not be null");
            Assert.notNull(claimExtensions, "DPoP claim extensions must not be null");
            if (method == Http.Method.ALL || method == Http.Method.NONE || method == Http.Method.BEFORE
                    || method == Http.Method.AFTER) {
                throw new ValidateException("DPoP requires an HTTP wire method rather than a Bus routing control");
            }
            DpopProof.normalize(uri);
            final JwaAlgorithm.Registration registration = algorithm.require(JwaAlgorithm.Kind.SIGNATURE);
            if (registration.keyTypes().contains("oct")) {
                throw new ValidateException("DPoP requires an asymmetric signature algorithm");
            }
            if (publicKey.hasPrivateMaterial()) {
                throw new ValidateException("DPoP advertised JWK must not contain private key material");
            }
            publicKey = publicKey.publicOnly();
            if (!registration.keyTypes().contains(publicKey.keyType())) {
                throw new ValidateException("DPoP advertised JWK type does not match the selected algorithm");
            }
            publicKey.algorithm().filter(value -> !value.equals(algorithm.name())).ifPresent(value -> {
                throw new ValidateException("DPoP advertised JWK algorithm does not match the selected algorithm");
            });
            accessToken.ifPresent(value -> validateAscii(value, "DPoP access token"));
            nonce.ifPresent(value -> Assert.notBlank(value, "DPoP nonce must not be blank"));
            protectedExtensions = new JsonValue.ObjectValue(protectedExtensions.values());
            claimExtensions = new JsonValue.ObjectValue(claimExtensions.values());
            rejectProofClaims(claimExtensions);
        }

    }

    /**
     * Holds the DPoP-specific claim components separately from JWT registered claims.
     *
     * @param method           exact outbound HTTP method
     * @param uri              normalized HTTP target URI
     * @param accessTokenHash  optional access-token hash
     * @param nonce            optional authorization server nonce
     * @param additionalClaims non-conflicting DPoP extension claims
     * @author Kimi Liu
     */
    private record ProofClaims(Http.Method method, String uri, Optional<String> accessTokenHash, Optional<String> nonce,
            JsonValue.ObjectValue additionalClaims) {

        /**
         * Validates and freezes the typed proof-specific claim values.
         *
         * @param method           outbound HTTP method
         * @param uri              normalized HTTP target URI
         * @param accessTokenHash  optional access-token hash
         * @param nonce            optional authorization server nonce
         * @param additionalClaims DPoP extension claims
         */
        private ProofClaims {
            Assert.notNull(method, "DPoP proof HTTP method must not be null");
            Assert.notBlank(uri, "DPoP proof HTTP target must not be blank");
            Assert.notNull(accessTokenHash, "DPoP access-token hash container must not be null");
            Assert.notNull(nonce, "DPoP proof nonce container must not be null");
            Assert.notNull(additionalClaims, "DPoP proof extensions must not be null");
            additionalClaims = new JsonValue.ObjectValue(additionalClaims.values());
        }

        /**
         * Encodes the typed DPoP claim components at the JWT extension wire boundary.
         *
         * @return immutable DPoP extension claim object
         */
        private JsonValue.ObjectValue extensions() {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            values.put(DpopProof.HTTP_METHOD, new JsonValue.StringValue(method.value()));
            values.put(DpopProof.HTTP_URI, new JsonValue.StringValue(uri));
            accessTokenHash
                    .ifPresent(value -> values.put(DpopProof.ACCESS_TOKEN_HASH, new JsonValue.StringValue(value)));
            nonce.ifPresent(value -> values.put(DpopProof.NONCE, new JsonValue.StringValue(value)));
            values.putAll(additionalClaims.values());
            return new JsonValue.ObjectValue(values);
        }

    }

}
