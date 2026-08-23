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
package org.miaixz.bus.auth.shared.jwt;

import java.security.Key;
import java.util.Map;
import java.util.Set;

import org.miaixz.bus.auth.shared.jose.JoseHeader;
import org.miaixz.bus.auth.shared.jose.JweService;
import org.miaixz.bus.auth.shared.jose.JwsService;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.MediaType;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Verifies compact JWT structure and JOSE protection before parsing a implementation-neutral Claims Set.
 * <p>
 * Key resolution is deliberately outside this service. Successful cryptographic verification does not perform issuer,
 * audience, temporal, replay, or protocol-specific claim validation.
 * </p>
 *
 * @author Kimi Liu
 */
public class JwtVerifier {

    /**
     * Profile-scoped JWS service.
     */
    private final JwsService jwsService;
    /**
     * Profile-scoped JWE service.
     */
    private final JweService jweService;

    /**
     * Creates a verifier with explicit JOSE services and no key-discovery fallback.
     *
     * @param jwsService profile-scoped JWS service
     * @param jweService profile-scoped JWE service
     */
    public JwtVerifier(final JwsService jwsService, final JweService jweService) {
        this.jwsService = Assert.notNull(jwsService, "JWT verifier JWS service must not be null");
        this.jweService = Assert.notNull(jweService, "JWT verifier JWE service must not be null");
    }

    /**
     * Counts compact segments without accepting empty or ambiguous separators as validation success.
     *
     * @param compact compact JOSE text
     * @return segment count
     */
    private static int segments(final String compact) {
        return compact.split("\\.", -1).length;
    }

    /**
     * Applies media type case rules to the abbreviated or full JWT content type.
     *
     * @param value JOSE cty value
     * @return whether it identifies application/JWT
     */
    private static boolean jwtMediaType(final String value) {
        return "JWT".equalsIgnoreCase(value) || MediaType.APPLICATION_JWT.equalsIgnoreCase(value);
    }

    /**
     * Verifies a signed or encrypted compact JWT according to the explicitly selected operation.
     *
     * @param compact      sensitive compact JWT representation
     * @param verification explicit key and critical-extension inputs
     * @return immutable cryptographically verified JWT
     */
    public Jwt verify(final String compact, final Verification verification) {
        Assert.notBlank(compact, "JWT compact value must not be blank");
        Assert.notNull(verification, "JWT verification input must not be null");
        return switch (verification) {
            case Signed signed -> verifySigned(compact, signed);
            case Encrypted encrypted -> verifyEncrypted(compact, encrypted);
            default -> throw new IllegalStateException("Unsupported protocol model implementation");
        };
    }

    /**
     * Verifies a compact JWS and parses its payload as a JWT Claims Set.
     *
     * @param compact      three-segment compact JWS
     * @param verification signing key and critical extensions
     * @return verified JWT
     */
    private Jwt verifySigned(final String compact, final Signed verification) {
        if (segments(compact) != 3) {
            throw new ValidateException("Signed JWT must contain three compact segments");
        }
        final JwsService.Jws jws = jwsService.parseCompact(compact, verification.critical());
        final JwsService.Signature signature = jws.signatures().get(0);
        jwsService.verify(signature, jws.payload(), verification.key(), verification.critical());
        return new Jwt(compact, signature.header(), claims(jws.payload()));
    }

    /**
     * Decrypts a compact JWE and optionally verifies a nested signed JWT.
     *
     * @param compact      five-segment compact JWE
     * @param verification decryption and optional nested-signature inputs
     * @return verified outer JWT carrying final claims
     */
    private Jwt verifyEncrypted(final String compact, final Encrypted verification) {
        if (segments(compact) != 5) {
            throw new ValidateException("Encrypted JWT must contain five compact segments");
        }
        final JweService.Jwe jwe = jweService.parseCompact(compact, verification.outerCritical());
        final byte[] plaintext = jweService.decrypt(jwe, 0, verification.decryptionKey(), verification.outerCritical());
        final JoseHeader outerHeader = new JoseHeader(jwe.protectedHeader(), new JsonValue.ObjectValue(Map.of()));
        final boolean nested = outerHeader.contentType().filter(JwtVerifier::jwtMediaType).isPresent();
        final JwtClaims claims;
        if (nested) {
            final Key signatureKey = verification.nestedSignatureKey().orElseThrow(
                    () -> new ValidateException("Nested JWT requires an explicit signature verification key"));
            final String inner = new String(plaintext, Charset.US_ASCII);
            if (segments(inner) != 3) {
                throw new ValidateException("Nested JWT plaintext must be a three-segment compact JWS");
            }
            claims = verifySigned(inner, new Signed(signatureKey, verification.innerCritical())).claims();
        } else {
            claims = claims(plaintext);
        }
        return new Jwt(compact, outerHeader, claims);
    }

    /**
     * Parses exact UTF-8 JSON payload bytes as an object-backed JWT Claims Set.
     *
     * @param payload verified or decrypted payload bytes
     * @return validated claims
     */
    private JwtClaims claims(final byte[] payload) {
        final JsonValue value = JsonKit.readValue(payload);
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("JWT Claims Set must be a JSON object");
        }
        return new JwtClaims(object);
    }

    /**
     * Seals the two cryptographic verification shapes accepted by this service.
     *
     * @author Kimi Liu
     */
    public interface Verification {

    }

    /**
     * Supplies one explicit JWS verification key and processed critical extensions.
     *
     * @param key      public or symmetric JWS verification key
     * @param critical critical extension names processed by the caller
     * @author Kimi Liu
     */
    public record Signed(Key key, Set<String> critical) implements Verification {

        /**
         * Validates and freezes signed JWT verification inputs.
         */
        public Signed {
            Assert.notNull(key, "Signed JWT verification key must not be null");
            Assert.notNull(critical, "Signed JWT critical parameters must not be null");
            critical = Set.copyOf(critical);
        }

    }

    /**
     * Supplies an explicit JWE key and optional nested JWS key with independently processed critical extensions.
     *
     * @param decryptionKey      private or symmetric JWE recipient key
     * @param nestedSignatureKey explicit nested JWS key when cty identifies JWT
     * @param outerCritical      processed outer JWE critical extensions
     * @param innerCritical      processed nested JWS critical extensions
     * @author Kimi Liu
     */
    public record Encrypted(Key decryptionKey, Optional<Key> nestedSignatureKey, Set<String> outerCritical,
            Set<String> innerCritical) implements Verification {

        /**
         * Validates and freezes encrypted JWT verification inputs.
         */
        public Encrypted {
            Assert.notNull(decryptionKey, "Encrypted JWT decryption key must not be null");
            Assert.notNull(nestedSignatureKey, "Nested JWT signature key container must not be null");
            Assert.notNull(outerCritical, "Encrypted JWT outer critical parameters must not be null");
            Assert.notNull(innerCritical, "Encrypted JWT inner critical parameters must not be null");
            outerCritical = Set.copyOf(outerCritical);
            innerCritical = Set.copyOf(innerCritical);
        }

    }

}
