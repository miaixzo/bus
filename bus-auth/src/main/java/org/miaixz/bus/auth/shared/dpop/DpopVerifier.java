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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

import org.miaixz.bus.auth.FabricX.Url;
import org.miaixz.bus.auth.Timeout;
import org.miaixz.bus.auth.guard.SecretGuard;
import org.miaixz.bus.auth.guard.TimeGuard;
import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JwaAlgorithm;
import org.miaixz.bus.auth.shared.jose.Jwk;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.auth.shared.jwt.JwtClaims;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Verifies an RFC 9449 compact proof against its embedded public JWK and the exact receiving HTTP request.
 * <p>
 * Public-key conversion is delegated to a narrow typed factory because the JWK is carried by the proof rather than
 * discovered through Roster or network access. Replay registration and access-token {@code cnf.jkt} binding remain the
 * responsibility of {@link DpopValidator} after this cryptographic and request-binding verification succeeds.
 * </p>
 *
 * @author Kimi Liu
 */
public class DpopVerifier {

    /**
     * Profile-scoped shared JWS parser and verifier.
     */
    private final JwsService jwsService;
    /**
     * Explicit converter for the proof's validated public JWK.
     */
    private final PublicKeyFactory publicKeyFactory;
    /**
     * Shared clock and skew guard.
     */
    private final TimeGuard timeGuard;
    /**
     * Shared constant-time comparison primitive for proof-bound values.
     */
    private final SecretGuard secretGuard;

    /**
     * Creates a verifier with explicit JSON, JWS, key conversion, time, and secret comparison dependencies.
     *
     * @param jwsService       profile-scoped JWS service
     * @param publicKeyFactory typed public JWK converter
     * @param timeGuard        shared issued-at and operation-timeout guard
     * @param secretGuard      shared constant-time comparison primitive
     */
    public DpopVerifier(final JwsService jwsService, final PublicKeyFactory publicKeyFactory, final TimeGuard timeGuard,
            final SecretGuard secretGuard) {
        this.jwsService = Assert.notNull(jwsService, "DPoP verifier JWS service must not be null");
        this.publicKeyFactory = Assert.notNull(publicKeyFactory, "DPoP public-key factory must not be null");
        this.timeGuard = Assert.notNull(timeGuard, "DPoP time guard must not be null");
        this.secretGuard = Assert.notNull(secretGuard, "DPoP secret guard must not be null");
    }

    /**
     * Validates header fields needed before using the embedded JWK for signature verification.
     *
     * @param header parsed protected JOSE header
     */
    private static void validateProtectedHeader(final JoseHeader header) {
        final Set<String> required = Set.of(JoseHeader.TYPE, JoseHeader.ALGORITHM, JoseHeader.JSON_WEB_KEY);
        if (!header.unprotectedParameters().values().isEmpty()
                || !header.protectedParameters().values().keySet().containsAll(required)) {
            throw new ValidateException("DPoP proof requires protected-only typ, alg, and jwk parameters");
        }
        if (header.type().filter(DpopProof.TYPE::equalsIgnoreCase).isEmpty()) {
            throw new ValidateException("DPoP proof typ must be dpop+jwt");
        }
        final JwaAlgorithm.Registration registration = JwaAlgorithm.of(header.algorithm())
                .require(JwaAlgorithm.Kind.SIGNATURE);
        if (registration.keyTypes().contains("oct")) {
            throw new ValidateException("DPoP proof requires an asymmetric signature algorithm");
        }
    }

    /**
     * Computes the RFC 9449 access-token hash over exact visible ASCII octets.
     *
     * @param accessToken presented access-token wire value
     * @return unpadded Base64URL SHA-256 hash
     */
    private static String accessTokenHash(final String accessToken) {
        validateAscii(accessToken, "DPoP access token");
        final byte[] bytes = accessToken.getBytes(Charset.US_ASCII);
        try {
            return Base64.encodeUrlSafe(Builder.sha256(bytes));
        } finally {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    /**
     * Validates a non-empty visible ASCII protocol value before byte-level hashing.
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
     * Verifies the compact signature before parsing and validating the request-bound Claims Set.
     *
     * @param compact received three-segment DPoP proof
     * @param request exact receiving request and validation policy
     * @param timeout shared end-to-end operation timeout
     * @return cryptographically and semantically verified proof
     * @throws IllegalArgumentException if an argument is {@code null}
     * @throws ValidateException        if parsing, key conversion, signature, request binding, or temporal validation
     *                                  fails
     */
    public DpopProof verify(final String compact, final Request request, final Timeout timeout) {
        Assert.notBlank(compact, "DPoP compact proof must not be blank");
        Assert.notNull(request, "DPoP verification request must not be null");
        Assert.notNull(timeout, "DPoP verification timeout must not be null");
        final JwsService.Jws parsed = jwsService.parseCompact(compact, request.critical());
        final JwsService.Signature signature = parsed.signatures().get(0);
        validateProtectedHeader(signature.header());
        final Jwk publicJwk = DpopProof.embeddedPublicKey(signature.header());
        final Key publicKey = Assert
                .notNull(publicKeyFactory.create(publicJwk), "DPoP public-key factory result must not be null");
        jwsService.verify(signature, parsed.payload(), publicKey, request.critical());
        final JsonValue value = JsonKit.readValue(parsed.payload());
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("DPoP proof payload must be a JSON object Claims Set");
        }
        final DpopProof proof = new DpopProof(compact, signature.header(), new JwtClaims(object));
        validateRequestBinding(proof, request, timeout);
        return proof;
    }

    /**
     * Validates HTTP method, target URI, issuance window, access-token hash, and optional nonce.
     *
     * @param proof   signature-verified proof
     * @param request exact receiving request
     * @param timeout shared operation timeout and clock
     */
    private void validateRequestBinding(final DpopProof proof, final Request request, final Timeout timeout) {
        if (!request.method().value().equals(proof.httpMethod())) {
            throw new ValidateException("DPoP htm does not match the receiving HTTP method");
        }
        final String expectedUri = DpopProof.normalize(request.uri());
        final String presentedUri = DpopProof.normalize(Url.parse(proof.httpUri()));
        if (!expectedUri.equals(presentedUri)) {
            throw new ValidateException("DPoP htu does not match the receiving HTTP target URI");
        }
        timeGuard.validateIssuedAt(proof.issuedAt(), timeout);
        final Instant latest;
        try {
            latest = proof.issuedAt().plus(request.maximumAge());
        } catch (ArithmeticException cause) {
            throw new ValidateException("DPoP proof age exceeds the supported time range", cause);
        }
        if (!latest.isAfter(timeout.clock().now())) {
            throw new ValidateException("DPoP proof exceeds its permitted age");
        }
        if (request.accessToken().isPresent() != proof.accessTokenHash().isPresent()) {
            throw new ValidateException("DPoP ath presence does not match access-token presentation");
        }
        request.accessToken().ifPresent(
                token -> compare(
                        accessTokenHash(token),
                        proof.accessTokenHash().orElseThrow(),
                        "DPoP ath does not match the presented access token"));
        request.nonce().ifPresent(
                expected -> compare(
                        expected,
                        proof.nonce().orElseThrow(() -> new ValidateException("DPoP proof requires the server nonce")),
                        "DPoP nonce does not match the server nonce"));
    }

    /**
     * Compares two protocol values in constant time and clears temporary character arrays.
     *
     * @param expected  trusted expected value
     * @param presented proof-supplied value
     * @param message   safe refusal message
     */
    private void compare(final String expected, final String presented, final String message) {
        final char[] expectedCharacters = expected.toCharArray();
        final char[] presentedCharacters = presented.toCharArray();
        try {
            if (!secretGuard.matches(expectedCharacters, presentedCharacters)) {
                throw new ValidateException(message);
            }
        } finally {
            Arrays.fill(expectedCharacters, Symbol.C_NUL);
            Arrays.fill(presentedCharacters, Symbol.C_NUL);
        }
    }

    /**
     * Converts a structurally validated public JWK into the exact asymmetric verification key used by JWS.
     * <p>
     * Implementations must be deterministic and local. They must not perform Roster access, remote discovery, or trust
     * decisions; the proof itself supplies the public key.
     * </p>
     *
     * @author Kimi Liu
     */
    @FunctionalInterface
    public interface PublicKeyFactory {

        /**
         * Converts one public asymmetric JWK into a JCA verification key.
         *
         * @param publicJwk validated RSA, EC, or OKP public JWK
         * @return corresponding verification key
         * @throws ValidateException if the key type, curve, or parameters cannot be converted safely
         */
        Key create(Jwk publicJwk);

    }

    /**
     * Defines the exact receiving HTTP request and freshness policy for one proof.
     *
     * @param method      receiving standard HTTP method
     * @param uri         receiving absolute target URI
     * @param accessToken optional presented access token requiring {@code ath}
     * @param nonce       optional server-issued nonce required in the proof
     * @param maximumAge  positive maximum proof age from {@code iat}
     * @param critical    exact JOSE critical extensions processed by the caller
     * @author Kimi Liu
     */
    public record Request(Http.Method method, Url uri, Optional<String> accessToken, Optional<String> nonce,
            Duration maximumAge, Set<String> critical) {

        /**
         * Validates and freezes all receiving-request values.
         *
         * @throws IllegalArgumentException if a component or collection member is {@code null}
         * @throws ValidateException        if method, URI, token, nonce, or maximum age violates the verification
         *                                  profile
         */
        public Request {
            Assert.notNull(method, "DPoP receiving HTTP method must not be null");
            Assert.notNull(uri, "DPoP receiving HTTP target URI must not be null");
            Assert.notNull(accessToken, "DPoP access-token container must not be null");
            Assert.notNull(nonce, "DPoP nonce container must not be null");
            Assert.notNull(maximumAge, "DPoP maximum age must not be null");
            Assert.notNull(critical, "DPoP understood critical set must not be null");
            if (method == Http.Method.ALL || method == Http.Method.NONE || method == Http.Method.BEFORE
                    || method == Http.Method.AFTER) {
                throw new ValidateException("DPoP requires an HTTP wire method rather than a Bus routing control");
            }
            DpopProof.normalize(uri);
            accessToken.ifPresent(value -> validateAscii(value, "DPoP access token"));
            nonce.ifPresent(value -> Assert.notBlank(value, "DPoP server nonce must not be blank"));
            if (maximumAge.isZero() || maximumAge.isNegative()) {
                throw new ValidateException("DPoP maximum proof age must be positive");
            }
            critical = Set.copyOf(critical);
            for (String name : critical) {
                Assert.notBlank(name, "DPoP understood critical parameter must not be blank");
            }
        }

    }

}
