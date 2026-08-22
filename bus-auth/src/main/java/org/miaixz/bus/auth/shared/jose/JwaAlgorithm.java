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
package org.miaixz.bus.auth.shared.jose;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Preserves a case-sensitive IANA JOSE algorithm identifier and exposes controlled metadata for algorithms understood
 * by this framework version.
 * <p>
 * The value object remains open so registered extensions and collision-resistant names survive decoding. Presence in
 * the local registry does not enable an algorithm: every operation must additionally pass its profile allow-list and
 * key validation policy.
 * </p>
 *
 * @author Kimi Liu
 */
public class JwaAlgorithm {

    /**
     * Unsecured JWS algorithm identifier, recognized solely so execution can reject it explicitly.
     */
    public static final JwaAlgorithm NONE = known(Normal.NONE, Kind.UNSECURED, Set.of(), null, 0);
    /**
     * HMAC using SHA-256 signature algorithm identifier.
     */
    public static final JwaAlgorithm HS256 = known("HS256", Kind.SIGNATURE, Set.of("oct"), Algorithm.HMACSHA256, 256);
    /**
     * HMAC using SHA-384 signature algorithm identifier.
     */
    public static final JwaAlgorithm HS384 = known("HS384", Kind.SIGNATURE, Set.of("oct"), Algorithm.HMACSHA384, 384);
    /**
     * HMAC using SHA-512 signature algorithm identifier.
     */
    public static final JwaAlgorithm HS512 = known("HS512", Kind.SIGNATURE, Set.of("oct"), Algorithm.HMACSHA512, 512);
    /**
     * RSASSA-PKCS1-v1_5 using SHA-256 signature algorithm identifier.
     */
    public static final JwaAlgorithm RS256 = known(
            "RS256",
            Kind.SIGNATURE,
            Set.of("RSA"),
            Algorithm.SHA256WITHRSA,
            2048);
    /**
     * RSASSA-PKCS1-v1_5 using SHA-384 signature algorithm identifier.
     */
    public static final JwaAlgorithm RS384 = known(
            "RS384",
            Kind.SIGNATURE,
            Set.of("RSA"),
            Algorithm.SHA384WITHRSA,
            2048);
    /**
     * RSASSA-PKCS1-v1_5 using SHA-512 signature algorithm identifier.
     */
    public static final JwaAlgorithm RS512 = known(
            "RS512",
            Kind.SIGNATURE,
            Set.of("RSA"),
            Algorithm.SHA512WITHRSA,
            2048);
    /**
     * RSASSA-PSS using SHA-256 and MGF1 SHA-256 signature algorithm identifier.
     */
    public static final JwaAlgorithm PS256 = known(
            "PS256",
            Kind.SIGNATURE,
            Set.of("RSA"),
            Algorithm.SHA256WITHRSA_PSS,
            2048);
    /**
     * RSASSA-PSS using SHA-384 and MGF1 SHA-384 signature algorithm identifier.
     */
    public static final JwaAlgorithm PS384 = known(
            "PS384",
            Kind.SIGNATURE,
            Set.of("RSA"),
            Algorithm.SHA384WITHRSA_PSS,
            2048);
    /**
     * RSASSA-PSS using SHA-512 and MGF1 SHA-512 signature algorithm identifier.
     */
    public static final JwaAlgorithm PS512 = known(
            "PS512",
            Kind.SIGNATURE,
            Set.of("RSA"),
            Algorithm.SHA512WITHRSA_PSS,
            2048);
    /**
     * ECDSA using P-256 and SHA-256 signature algorithm identifier.
     */
    public static final JwaAlgorithm ES256 = known(
            "ES256",
            Kind.SIGNATURE,
            Set.of("EC"),
            Algorithm.SHA256WITHECDSA,
            256);
    /**
     * ECDSA using P-384 and SHA-384 signature algorithm identifier.
     */
    public static final JwaAlgorithm ES384 = known(
            "ES384",
            Kind.SIGNATURE,
            Set.of("EC"),
            Algorithm.SHA384WITHECDSA,
            384);
    /**
     * ECDSA using P-521 and SHA-512 signature algorithm identifier.
     */
    public static final JwaAlgorithm ES512 = known(
            "ES512",
            Kind.SIGNATURE,
            Set.of("EC"),
            Algorithm.SHA512WITHECDSA,
            521);
    /**
     * Edwards-curve digital signature algorithm identifier.
     */
    public static final JwaAlgorithm EDDSA = known("EdDSA", Kind.SIGNATURE, Set.of("OKP"), Algorithm.ED25519, 256);

    /**
     * RSAES-PKCS1-v1_5 key-management identifier retained for interoperability but not mapped for execution.
     */
    public static final JwaAlgorithm RSA1_5 = known("RSA1_5", Kind.KEY_MANAGEMENT, Set.of("RSA"), null, 2048);
    /**
     * RSAES OAEP using SHA-1 and MGF1 SHA-1 key-management identifier.
     */
    public static final JwaAlgorithm RSA_OAEP = known("RSA-OAEP", Kind.KEY_MANAGEMENT, Set.of("RSA"), null, 2048);
    /**
     * RSAES OAEP using SHA-256 and MGF1 SHA-256 key-management identifier.
     */
    public static final JwaAlgorithm RSA_OAEP_256 = known(
            "RSA-OAEP-256",
            Kind.KEY_MANAGEMENT,
            Set.of("RSA"),
            null,
            2048);
    /**
     * AES Key Wrap with a 128-bit key-management identifier.
     */
    public static final JwaAlgorithm A128KW = known("A128KW", Kind.KEY_MANAGEMENT, Set.of("oct"), null, 128);
    /**
     * AES Key Wrap with a 192-bit key-management identifier.
     */
    public static final JwaAlgorithm A192KW = known("A192KW", Kind.KEY_MANAGEMENT, Set.of("oct"), null, 192);
    /**
     * AES Key Wrap with a 256-bit key-management identifier.
     */
    public static final JwaAlgorithm A256KW = known("A256KW", Kind.KEY_MANAGEMENT, Set.of("oct"), null, 256);
    /**
     * Direct use of a shared symmetric key as the content-encryption key.
     */
    public static final JwaAlgorithm DIRECT = known("dir", Kind.KEY_MANAGEMENT, Set.of("oct"), null, 128);
    /**
     * ECDH-ES direct key-agreement identifier.
     */
    public static final JwaAlgorithm ECDH_ES = known("ECDH-ES", Kind.KEY_MANAGEMENT, Set.of("EC", "OKP"), null, 256);
    /**
     * ECDH-ES with AES-128 Key Wrap identifier.
     */
    public static final JwaAlgorithm ECDH_ES_A128KW = known(
            "ECDH-ES+A128KW",
            Kind.KEY_MANAGEMENT,
            Set.of("EC", "OKP"),
            null,
            256);
    /**
     * ECDH-ES with AES-192 Key Wrap identifier.
     */
    public static final JwaAlgorithm ECDH_ES_A192KW = known(
            "ECDH-ES+A192KW",
            Kind.KEY_MANAGEMENT,
            Set.of("EC", "OKP"),
            null,
            256);
    /**
     * ECDH-ES with AES-256 Key Wrap identifier.
     */
    public static final JwaAlgorithm ECDH_ES_A256KW = known(
            "ECDH-ES+A256KW",
            Kind.KEY_MANAGEMENT,
            Set.of("EC", "OKP"),
            null,
            256);
    /**
     * AES-GCM key wrapping with a 128-bit key identifier.
     */
    public static final JwaAlgorithm A128GCMKW = known("A128GCMKW", Kind.KEY_MANAGEMENT, Set.of("oct"), null, 128);
    /**
     * AES-GCM key wrapping with a 192-bit key identifier.
     */
    public static final JwaAlgorithm A192GCMKW = known("A192GCMKW", Kind.KEY_MANAGEMENT, Set.of("oct"), null, 192);
    /**
     * AES-GCM key wrapping with a 256-bit key identifier.
     */
    public static final JwaAlgorithm A256GCMKW = known("A256GCMKW", Kind.KEY_MANAGEMENT, Set.of("oct"), null, 256);
    /**
     * PBES2 SHA-256 with AES-128 Key Wrap identifier.
     */
    public static final JwaAlgorithm PBES2_HS256_A128KW = known(
            "PBES2-HS256+A128KW",
            Kind.KEY_MANAGEMENT,
            Set.of("oct"),
            null,
            128);
    /**
     * PBES2 SHA-384 with AES-192 Key Wrap identifier.
     */
    public static final JwaAlgorithm PBES2_HS384_A192KW = known(
            "PBES2-HS384+A192KW",
            Kind.KEY_MANAGEMENT,
            Set.of("oct"),
            null,
            192);
    /**
     * PBES2 SHA-512 with AES-256 Key Wrap identifier.
     */
    public static final JwaAlgorithm PBES2_HS512_A256KW = known(
            "PBES2-HS512+A256KW",
            Kind.KEY_MANAGEMENT,
            Set.of("oct"),
            null,
            256);

    /**
     * AES-CBC with HMAC SHA-256 authenticated encryption identifier.
     */
    public static final JwaAlgorithm A128CBC_HS256 = known(
            "A128CBC-HS256",
            Kind.CONTENT_ENCRYPTION,
            Set.of("oct"),
            null,
            256);
    /**
     * AES-CBC with HMAC SHA-384 authenticated encryption identifier.
     */
    public static final JwaAlgorithm A192CBC_HS384 = known(
            "A192CBC-HS384",
            Kind.CONTENT_ENCRYPTION,
            Set.of("oct"),
            null,
            384);
    /**
     * AES-CBC with HMAC SHA-512 authenticated encryption identifier.
     */
    public static final JwaAlgorithm A256CBC_HS512 = known(
            "A256CBC-HS512",
            Kind.CONTENT_ENCRYPTION,
            Set.of("oct"),
            null,
            512);
    /**
     * AES-GCM authenticated encryption with a 128-bit key identifier.
     */
    public static final JwaAlgorithm A128GCM = known("A128GCM", Kind.CONTENT_ENCRYPTION, Set.of("oct"), null, 128);
    /**
     * AES-GCM authenticated encryption with a 192-bit key identifier.
     */
    public static final JwaAlgorithm A192GCM = known("A192GCM", Kind.CONTENT_ENCRYPTION, Set.of("oct"), null, 192);
    /**
     * AES-GCM authenticated encryption with a 256-bit key identifier.
     */
    public static final JwaAlgorithm A256GCM = known("A256GCM", Kind.CONTENT_ENCRYPTION, Set.of("oct"), null, 256);

    /**
     * Canonical locally understood algorithm values, each of which owns its registration metadata exactly once.
     */
    private static final List<JwaAlgorithm> KNOWN = List.of(
            NONE,
            HS256,
            HS384,
            HS512,
            RS256,
            RS384,
            RS512,
            PS256,
            PS384,
            PS512,
            ES256,
            ES384,
            ES512,
            EDDSA,
            RSA1_5,
            RSA_OAEP,
            RSA_OAEP_256,
            A128KW,
            A192KW,
            A256KW,
            DIRECT,
            ECDH_ES,
            ECDH_ES_A128KW,
            ECDH_ES_A192KW,
            ECDH_ES_A256KW,
            A128GCMKW,
            A192GCMKW,
            A256GCMKW,
            PBES2_HS256_A128KW,
            PBES2_HS384_A192KW,
            PBES2_HS512_A256KW,
            A128CBC_HS256,
            A192CBC_HS384,
            A256CBC_HS512,
            A128GCM,
            A192GCM,
            A256GCM);

    /**
     * Case-sensitive JOSE registration or extension identifier.
     */
    private final String name;
    /**
     * Registration metadata for a locally understood value, or {@code null} for an open extension value.
     */
    private final Registration registration;

    /**
     * Creates an open JOSE algorithm value after validating its StringOrURI grammar.
     *
     * @param name case-sensitive registration or collision-resistant identifier
     */
    public JwaAlgorithm(final String name) {
        this(name, null);
    }

    /**
     * Creates a JOSE algorithm value with optional immutable local registration metadata.
     *
     * @param name         case-sensitive registration or collision-resistant identifier
     * @param registration local registration metadata, or {@code null} for an extension value
     */
    private JwaAlgorithm(final String name, final Registration registration) {
        this.name = validateName(name);
        this.registration = registration;
    }

    /**
     * Returns a canonical known value or preserves an unknown legal algorithm identifier.
     *
     * @param name case-sensitive IANA registration or collision-resistant StringOrURI
     * @return immutable JOSE algorithm value
     */
    public static JwaAlgorithm of(final String name) {
        final String validated = validateName(name);
        for (JwaAlgorithm known : KNOWN) {
            if (known.name.equals(validated)) {
                return known;
            }
        }
        return new JwaAlgorithm(validated);
    }

    /**
     * Creates a known value before the immutable registry is assembled.
     *
     * @param name           case-sensitive wire name
     * @param kind           semantic algorithm kind
     * @param keyTypes       compatible JWK key types
     * @param coreAlgorithm  exact bus-core execution mapping, or {@code null} when none exists
     * @param minimumKeyBits minimum key strength in bits
     * @return known algorithm value
     */
    private static JwaAlgorithm known(
            final String name,
            final Kind kind,
            final Set<String> keyTypes,
            final Algorithm coreAlgorithm,
            final int minimumKeyBits) {
        return new JwaAlgorithm(name,
                new Registration(name, kind, keyTypes, Optional.ofNullable(coreAlgorithm), minimumKeyBits));
    }

    /**
     * Validates the non-blank ASCII StringOrURI grammar used by JOSE algorithm identifiers.
     *
     * @param name candidate identifier
     * @return validated unchanged identifier
     */
    private static String validateName(final String name) {
        Assert.notBlank(name, "JWA algorithm name must not be blank");
        for (int index = 0; index < name.length(); index++) {
            if (name.charAt(index) > 0x7f) {
                throw new ValidateException("JWA algorithm name must contain ASCII characters only");
            }
        }
        if (name.indexOf(Symbol.C_COLON) >= 0) {
            try {
                new URI(name);
            } catch (URISyntaxException cause) {
                throw new ValidateException("JWA algorithm StringOrURI syntax is invalid", cause);
            }
        }
        return name;
    }

    /**
     * Returns the exact case-sensitive wire identifier.
     *
     * @return JOSE algorithm identifier
     */
    public String name() {
        return name;
    }

    /**
     * Looks up immutable framework metadata without enabling the algorithm.
     *
     * @return local RFC 7518 registration metadata when understood
     */
    public Optional<Registration> registration() {
        return Optional.ofNullable(registration);
    }

    /**
     * Requires a known secured algorithm of the requested semantic kind.
     *
     * @param kind operation kind expected by the caller
     * @return matching immutable local registration metadata
     * @throws ValidateException if the identifier is unknown, unsecured, or registered for another kind
     */
    public Registration require(final Kind kind) {
        Assert.notNull(kind, "Required JWA algorithm kind must not be null");
        final Registration registration = registration()
                .orElseThrow(() -> new ValidateException("JOSE algorithm is not understood by this framework"));
        if (registration.kind() == Kind.UNSECURED) {
            throw new ValidateException("Unsecured JOSE algorithm none must not execute");
        }
        if (registration.kind() != kind) {
            throw new ValidateException("JOSE algorithm is not registered for the requested operation kind");
        }
        return registration;
    }

    /**
     * Compares algorithm identifiers exactly as required by JOSE.
     *
     * @param other comparison value
     * @return {@code true} only for another value with the same case-sensitive name
     */
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof JwaAlgorithm algorithm && name.equals(algorithm.name);
    }

    /**
     * Returns a hash derived from the case-sensitive identifier.
     *
     * @return stable value hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    /**
     * Returns the wire identifier for diagnostics without revealing key material.
     *
     * @return case-sensitive algorithm name
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Identifies the semantic operation family assigned by the JOSE algorithm registry.
     *
     * @author Kimi Liu
     */
    public enum Kind {
        /**
         * Algorithm deliberately provides no integrity protection.
         */
        UNSECURED,
        /**
         * Algorithm computes or verifies a JWS signature or MAC.
         */
        SIGNATURE,
        /**
         * Algorithm encrypts, wraps, derives, or directly supplies a JWE content-encryption key.
         */
        KEY_MANAGEMENT,
        /**
         * Algorithm performs authenticated encryption of JWE plaintext.
         */
        CONTENT_ENCRYPTION

    }

    /**
     * Describes one locally understood RFC 7518 registration without activating it for any profile.
     *
     * @param name           exact case-sensitive wire identifier
     * @param kind           registered semantic operation kind
     * @param keyTypes       compatible case-sensitive JWK {@code kty} values
     * @param coreAlgorithm  exact bus-core Algorithm mapping when available
     * @param minimumKeyBits minimum permitted key strength in bits
     * @author Kimi Liu
     */
    public record Registration(String name, Kind kind, Set<String> keyTypes, Optional<Algorithm> coreAlgorithm,
            int minimumKeyBits) {

        /**
         * Validates and freezes local registration metadata.
         *
         * @throws IllegalArgumentException if a required component is {@code null} or blank
         * @throws ValidateException        if key strength is invalid for the registration kind
         */
        public Registration {
            Assert.notBlank(name, "JWA registration name must not be blank");
            Assert.notNull(kind, "JWA registration kind must not be null");
            Assert.notNull(keyTypes, "JWA registration key types must not be null");
            Assert.notNull(coreAlgorithm, "JWA registration core algorithm must not be null");
            keyTypes = Set.copyOf(keyTypes);
            if (kind == Kind.UNSECURED ? minimumKeyBits != 0 || !keyTypes.isEmpty()
                    : minimumKeyBits <= 0 || keyTypes.isEmpty()) {
                throw new ValidateException("JWA registration key requirements do not match its operation kind");
            }
        }

    }

}
