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
import java.util.*;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Optional;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Represents one RFC 7517 JSON Web Key through explicit common parameters and key-type-specific material.
 * <p>
 * The object preserves unknown extension values but never treats a field-name collection as its schema. Conversion to
 * and from a JSON object occurs only at the JWK wire boundary. Execution-key construction and key ownership remain the
 * responsibility of the project key loader, framework key parser, and protocol security layers.
 * </p>
 *
 * @author Kimi Liu
 */
public class Jwk {

    /**
     * Required key type member name.
     */
    public static final String KEY_TYPE = "kty";
    /**
     * Optional public key use member name.
     */
    public static final String PUBLIC_KEY_USE = "use";
    /**
     * Optional key operations member name.
     */
    public static final String KEY_OPERATIONS = "key_ops";
    /**
     * Optional intended algorithm member name.
     */
    public static final String ALGORITHM = "alg";
    /**
     * Optional key identifier member name.
     */
    public static final String KEY_ID = "kid";
    /**
     * Optional X.509 certificate URL member name.
     */
    public static final String CERTIFICATE_URL = "x5u";
    /**
     * Optional X.509 certificate chain member name.
     */
    public static final String CERTIFICATE_CHAIN = "x5c";
    /**
     * Optional SHA-1 certificate thumbprint member name.
     */
    public static final String CERTIFICATE_THUMBPRINT = "x5t";
    /**
     * Optional SHA-256 certificate thumbprint member name.
     */
    public static final String CERTIFICATE_THUMBPRINT_SHA256 = "x5t#S256";

    /**
     * Common registered parameters shared by every JWK key type.
     */
    private final Common common;
    /**
     * Parameters governed by the selected JWK key type.
     */
    private final Material material;

    /**
     * Decodes one complete JWK JSON object into explicit common and key-material components.
     *
     * @param parameters complete provider-neutral JWK object
     * @throws IllegalArgumentException if {@code parameters} is {@code null}
     * @throws ValidateException        if a registered or understood key-type parameter violates RFC 7517
     */
    public Jwk(final JsonValue.ObjectValue parameters) {
        this(decode(parameters));
    }

    /**
     * Creates a JWK from explicit common parameters and key-type material.
     *
     * @param common   common registered parameters and extensions
     * @param material key-type-specific parameters
     * @throws IllegalArgumentException if either component is {@code null}
     */
    public Jwk(final Common common, final Material material) {
        this.common = Assert.notNull(common, "JWK common parameters must not be null");
        this.material = Assert.notNull(material, "JWK key material must not be null");
        for (String name : common.extensions().values().keySet()) {
            if (materialName(material.keyType(), name)) {
                throw new ValidateException("JWK extensions must not replace a key-type parameter");
            }
        }
    }

    /**
     * Creates a JWK from one completed decode result.
     *
     * @param decoded completed typed decode result
     */
    private Jwk(final Decoded decoded) {
        this(decoded.common(), decoded.material());
    }

    /**
     * Decodes all standard members and routes remaining values to the appropriate extension object.
     *
     * @param parameters source JSON object
     * @return typed decode result
     */
    private static Decoded decode(final JsonValue.ObjectValue parameters) {
        Assert.notNull(parameters, "JWK parameters must not be null");
        for (String name : parameters.values().keySet()) {
            Assert.notBlank(name, "JWK parameter name must not be blank");
        }
        final String keyType = requiredString(parameters, KEY_TYPE);
        final Map<String, JsonValue> extensions = new LinkedHashMap<>();
        for (Map.Entry<String, JsonValue> entry : parameters.values().entrySet()) {
            if (!commonName(entry.getKey()) && !materialName(keyType, entry.getKey())) {
                extensions.put(entry.getKey(), entry.getValue());
            }
        }
        final Common common = new Common(optionalString(parameters, PUBLIC_KEY_USE),
                strings(parameters, KEY_OPERATIONS, false), optionalString(parameters, ALGORITHM).map(JwaAlgorithm::of),
                optionalString(parameters, KEY_ID), optionalString(parameters, CERTIFICATE_URL).map(Jwk::absoluteUri),
                strings(parameters, CERTIFICATE_CHAIN, true), optionalString(parameters, CERTIFICATE_THUMBPRINT),
                optionalString(parameters, CERTIFICATE_THUMBPRINT_SHA256), new JsonValue.ObjectValue(extensions));
        return new Decoded(common, switch (keyType) {
            case "RSA" -> rsa(parameters);
            case "EC" -> new Ec(requiredString(parameters, "crv"), requiredBase64Url(parameters, "x"),
                    requiredBase64Url(parameters, "y"), optionalBase64Url(parameters, "d"));
            case "oct" -> new Oct(requiredBase64Url(parameters, "k"));
            case "OKP" -> new Okp(requiredString(parameters, "crv"), requiredBase64Url(parameters, "x"),
                    optionalBase64Url(parameters, "d"));
            default -> new Extension(keyType, new JsonValue.ObjectValue(extensions));
        });
    }

    /**
     * Decodes RSA public, private, CRT, and multi-prime parameters.
     *
     * @param parameters complete RSA JWK object
     * @return typed RSA material
     */
    private static Rsa rsa(final JsonValue.ObjectValue parameters) {
        final Optional<String> privateExponent = optionalBase64Url(parameters, "d");
        final Optional<String> firstPrime = optionalBase64Url(parameters, "p");
        final Optional<String> secondPrime = optionalBase64Url(parameters, "q");
        final Optional<String> firstExponent = optionalBase64Url(parameters, "dp");
        final Optional<String> secondExponent = optionalBase64Url(parameters, "dq");
        final Optional<String> coefficient = optionalBase64Url(parameters, "qi");
        final List<OtherPrime> otherPrimes = otherPrimes(parameters);
        final int crtCount = (firstPrime.isPresent() ? 1 : 0) + (secondPrime.isPresent() ? 1 : 0)
                + (firstExponent.isPresent() ? 1 : 0) + (secondExponent.isPresent() ? 1 : 0)
                + (coefficient.isPresent() ? 1 : 0);
        if ((!otherPrimes.isEmpty() || crtCount != 0) && privateExponent.isEmpty()) {
            throw new ValidateException("RSA JWK CRT parameters require d");
        }
        if (crtCount != 0 && crtCount != 5) {
            throw new ValidateException("RSA JWK CRT parameters must be supplied as one complete set");
        }
        return new Rsa(requiredBase64Url(parameters, "n"), requiredBase64Url(parameters, "e"), privateExponent,
                firstPrime, secondPrime, firstExponent, secondExponent, coefficient, otherPrimes);
    }

    /**
     * Decodes the optional RSA multi-prime information array.
     *
     * @param parameters complete RSA JWK object
     * @return immutable additional-prime list
     */
    private static List<OtherPrime> otherPrimes(final JsonValue.ObjectValue parameters) {
        final JsonValue value = parameters.values().get("oth");
        if (value == null) {
            return List.of();
        }
        if (!(value instanceof JsonValue.ArrayValue array) || array.values().isEmpty()) {
            throw new ValidateException("RSA JWK oth parameter must be a non-empty array");
        }
        final List<OtherPrime> result = new ArrayList<>(array.values().size());
        for (JsonValue element : array.values()) {
            if (!(element instanceof JsonValue.ObjectValue prime)) {
                throw new ValidateException("RSA JWK oth entries must be JSON objects");
            }
            result.add(
                    new OtherPrime(requiredBase64Url(prime, "r"), requiredBase64Url(prime, "d"),
                            requiredBase64Url(prime, "t")));
        }
        return List.copyOf(result);
    }

    /**
     * Identifies common RFC 7517 member names without maintaining a parallel field-name collection.
     *
     * @param name exact case-sensitive member name
     * @return {@code true} for a common registered component
     */
    private static boolean commonName(final String name) {
        return switch (name) {
            case KEY_TYPE, PUBLIC_KEY_USE, KEY_OPERATIONS, ALGORITHM, KEY_ID, CERTIFICATE_URL, CERTIFICATE_CHAIN, CERTIFICATE_THUMBPRINT, CERTIFICATE_THUMBPRINT_SHA256 -> true;
            default -> false;
        };
    }

    /**
     * Identifies members defined by one understood JWK key type.
     *
     * @param keyType selected case-sensitive JWK key type
     * @param name    exact case-sensitive member name
     * @return {@code true} when the member belongs to the key material component
     */
    private static boolean materialName(final String keyType, final String name) {
        return switch (keyType) {
            case "RSA" -> switch (name) {
                case "n", "e", "d", "p", "q", "dp", "dq", "qi", "oth" -> true;
                default -> false;
            };
            case "EC" -> switch (name) {
                case "crv", "x", "y", "d" -> true;
                default -> false;
            };
            case "oct" -> "k".equals(name);
            case "OKP" -> switch (name) {
                case "crv", "x", "d" -> true;
                default -> false;
            };
            default -> false;
        };
    }

    /**
     * Returns a mandatory non-blank JSON string member.
     *
     * @param value source object
     * @param name  exact member name
     * @return decoded string value
     */
    private static String requiredString(final JsonValue.ObjectValue value, final String name) {
        final JsonValue member = value.values().get(name);
        if (!(member instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("Required JWK parameter must be a non-blank string");
        }
        return string.value();
    }

    /**
     * Returns an optional non-blank JSON string member.
     *
     * @param value source object
     * @param name  exact member name
     * @return decoded string when present
     */
    private static Optional<String> optionalString(final JsonValue.ObjectValue value, final String name) {
        final JsonValue member = value.values().get(name);
        if (member == null) {
            return Optional.empty();
        }
        if (!(member instanceof JsonValue.StringValue string) || string.value().isBlank()) {
            throw new ValidateException("JWK string parameter must contain a non-blank string");
        }
        return Optional.of(string.value());
    }

    /**
     * Returns and validates a mandatory Base64URL string member.
     *
     * @param value source object
     * @param name  exact key-material member name
     * @return validated encoded value
     */
    private static String requiredBase64Url(final JsonValue.ObjectValue value, final String name) {
        final String result = requiredString(value, name);
        validateBase64Url(result);
        return result;
    }

    /**
     * Returns and validates an optional Base64URL string member.
     *
     * @param value source object
     * @param name  exact key-material member name
     * @return validated encoded value when present
     */
    private static Optional<String> optionalBase64Url(final JsonValue.ObjectValue value, final String name) {
        final Optional<String> result = optionalString(value, name);
        result.ifPresent(Jwk::validateBase64Url);
        return result;
    }

    /**
     * Returns a validated string-array member while preserving element order.
     *
     * @param value    source object
     * @param name     exact member name
     * @param nonEmpty whether an explicitly present array must contain a value
     * @return immutable string values, or an empty list when absent
     */
    private static List<String> strings(final JsonValue.ObjectValue value, final String name, final boolean nonEmpty) {
        final JsonValue member = value.values().get(name);
        if (member == null) {
            return List.of();
        }
        if (!(member instanceof JsonValue.ArrayValue array) || nonEmpty && array.values().isEmpty()) {
            throw new ValidateException("JWK array parameter has an invalid JSON value");
        }
        final List<String> result = new ArrayList<>(array.values().size());
        for (JsonValue element : array.values()) {
            if (!(element instanceof JsonValue.StringValue string) || string.value().isBlank()) {
                throw new ValidateException("JWK array entries must be non-blank strings");
            }
            result.add(string.value());
        }
        return List.copyOf(result);
    }

    /**
     * Converts and validates an absolute X.509 certificate resource URI.
     *
     * @param value exact URI text
     * @return absolute immutable URI
     */
    private static URI absoluteUri(final String value) {
        try {
            final URI uri = new URI(value);
            if (!uri.isAbsolute()) {
                throw new ValidateException("JWK x5u parameter must be an absolute URI");
            }
            return uri;
        } catch (URISyntaxException cause) {
            throw new ValidateException("JWK x5u URI syntax is invalid", cause);
        }
    }

    /**
     * Validates strict unpadded Base64URL syntax for a JWK binary value.
     *
     * @param value encoded value
     */
    private static void validateBase64Url(final String value) {
        Assert.notBlank(value, "JWK Base64URL value must not be blank");
        if ((value.length() & 3) == 1) {
            throw new ValidateException("JWK Base64URL value has an invalid length");
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (!(character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z)
                    && !(character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z)
                    && !(character >= Symbol.C_ZERO && character <= Symbol.C_NINE) && character != Symbol.C_MINUS
                    && character != Symbol.C_UNDERLINE) {
                throw new ValidateException("JWK Base64URL value contains an invalid character");
            }
        }
    }

    /**
     * Validates standard padded Base64 grammar used by an X.509 certificate chain.
     *
     * @param value encoded certificate value
     * @return whether syntax and padding are structurally valid
     */
    private static boolean standardBase64(final String value) {
        if (value.isEmpty() || (value.length() & 3) != 0) {
            return false;
        }
        int padding = 0;
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (character == Symbol.C_EQUAL) {
                padding++;
                if (index < value.length() - 2 || padding > 2) {
                    return false;
                }
            } else if (padding != 0 || !(character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z)
                    && !(character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z)
                    && !(character >= Symbol.C_ZERO && character <= Symbol.C_NINE) && character != Symbol.C_PLUS
                    && character != Symbol.C_SLASH) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds an ordered string array to a JSON object when the component is not empty.
     *
     * @param target destination members
     * @param name   exact wire member name
     * @param values component values
     */
    private static void putStrings(final Map<String, JsonValue> target, final String name, final List<String> values) {
        if (values.isEmpty()) {
            return;
        }
        target.put(
                name,
                new JsonValue.ArrayValue(
                        values.stream().map(JsonValue.StringValue::new).map(JsonValue.class::cast).toList()));
    }

    /**
     * Creates one JSON string value.
     *
     * @param value component text
     * @return JSON string representation
     */
    private static JsonValue.StringValue string(final String value) {
        return new JsonValue.StringValue(value);
    }

    /**
     * Encodes this typed JWK at the RFC 7517 JSON boundary.
     *
     * @return detached immutable complete JWK object
     */
    public JsonValue.ObjectValue parameters() {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        values.put(KEY_TYPE, string(material.keyType()));
        common.publicKeyUse().ifPresent(value -> values.put(PUBLIC_KEY_USE, string(value)));
        putStrings(values, KEY_OPERATIONS, common.keyOperations());
        common.algorithm().ifPresent(value -> values.put(ALGORITHM, string(value.name())));
        common.keyId().ifPresent(value -> values.put(KEY_ID, string(value)));
        common.certificateUrl().ifPresent(value -> values.put(CERTIFICATE_URL, string(value.toASCIIString())));
        putStrings(values, CERTIFICATE_CHAIN, common.certificateChain());
        common.certificateThumbprint().ifPresent(value -> values.put(CERTIFICATE_THUMBPRINT, string(value)));
        common.certificateThumbprintSha256()
                .ifPresent(value -> values.put(CERTIFICATE_THUMBPRINT_SHA256, string(value)));
        values.putAll(material.parameters().values());
        if (!(material instanceof Extension)) {
            values.putAll(common.extensions().values());
        }
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Looks up one exact case-sensitive JWK wire member without coercing its JSON type.
     *
     * @param name exact member name
     * @return provider-neutral member value when present
     */
    public Optional<JsonValue> parameter(final String name) {
        Assert.notBlank(name, "JWK parameter name must not be blank");
        return Optional.ofNullable(parameters().values().get(name));
    }

    /**
     * Returns the required case-sensitive key type.
     *
     * @return JWK {@code kty} value
     */
    public String keyType() {
        return material.keyType();
    }

    /**
     * Returns the optional public key use registration or extension value.
     *
     * @return JWK {@code use} value when present
     */
    public Optional<String> publicKeyUse() {
        return common.publicKeyUse();
    }

    /**
     * Returns key operation values in their original wire order.
     *
     * @return immutable {@code key_ops} list, empty when absent
     */
    public List<String> keyOperations() {
        return common.keyOperations();
    }

    /**
     * Returns the optional intended JOSE algorithm without enabling it.
     *
     * @return exact JWK {@code alg} value when present
     */
    public Optional<String> algorithm() {
        return common.algorithm().map(JwaAlgorithm::name);
    }

    /**
     * Returns the optional case-sensitive key identifier.
     *
     * @return JWK {@code kid} value when present
     */
    public Optional<String> keyId() {
        return common.keyId();
    }

    /**
     * Reports whether this understood key contains private or symmetric key material.
     *
     * @return {@code true} when the typed material must not be published
     */
    public boolean hasPrivateMaterial() {
        return material.privateMaterial();
    }

    /**
     * Produces a public-only JWK for an understood asymmetric key type.
     *
     * @return new JWK with all private parameters removed
     * @throws ValidateException for symmetric or unknown key types that cannot be proven safe for publication
     */
    public Jwk publicOnly() {
        return new Jwk(common, material.publicOnly());
    }

    /**
     * Compares typed common parameters and key material.
     *
     * @param other comparison value
     * @return {@code true} when both JWK value components are equal
     */
    @Override
    public boolean equals(final Object other) {
        return this == other || other instanceof Jwk key && common.equals(key.common) && material.equals(key.material);
    }

    /**
     * Returns a hash derived from typed common parameters and key material.
     *
     * @return stable value hash
     */
    @Override
    public int hashCode() {
        return Objects.hash(common, material);
    }

    /**
     * Returns a non-sensitive summary that omits all key parameter values.
     *
     * @return safe JWK summary
     */
    @Override
    public String toString() {
        return "Jwk[keyType=" + keyType() + ",keyId=" + keyId().orElse(null) + ",privateMaterial="
                + hasPrivateMaterial() + "]";
    }

    /**
     * Defines the behavior shared by typed JWK key-material variants.
     *
     * @author Kimi Liu
     */
    public interface Material {

        /**
         * Returns the exact registered or extension key type.
         *
         * @return JWK {@code kty} value
         */
        String keyType();

        /**
         * Encodes only the key-type-specific members.
         *
         * @return immutable key-material parameter object
         */
        JsonValue.ObjectValue parameters();

        /**
         * Reports whether the material includes a private or symmetric secret.
         *
         * @return {@code true} when publication is unsafe
         */
        boolean privateMaterial();

        /**
         * Returns a public-only representation when that transformation is defined.
         *
         * @return public asymmetric material
         * @throws ValidateException when the key type cannot be safely published
         */
        Material publicOnly();

    }

    /**
     * Holds common RFC 7517 parameters that apply independently of the selected key type.
     *
     * @param publicKeyUse                optional intended public-key use
     * @param keyOperations               ordered intended key operations
     * @param algorithm                   optional intended JOSE algorithm
     * @param keyId                       optional key identifier
     * @param certificateUrl              optional certificate-chain URL
     * @param certificateChain            ordered base64 DER certificate chain
     * @param certificateThumbprint       optional SHA-1 certificate thumbprint
     * @param certificateThumbprintSha256 optional SHA-256 certificate thumbprint
     * @param extensions                  unregistered common extension parameters
     * @author Kimi Liu
     */
    public record Common(Optional<String> publicKeyUse, List<String> keyOperations, Optional<JwaAlgorithm> algorithm,
            Optional<String> keyId, Optional<URI> certificateUrl, List<String> certificateChain,
            Optional<String> certificateThumbprint, Optional<String> certificateThumbprintSha256,
            JsonValue.ObjectValue extensions) {

        /**
         * Validates and freezes common parameters and their consistency constraints.
         *
         * @throws IllegalArgumentException if a component or container is {@code null}
         * @throws ValidateException        if use, operation, certificate, or extension semantics are invalid
         */
        public Common {
            Assert.notNull(publicKeyUse, "JWK public-key-use container must not be null");
            Assert.notNull(keyOperations, "JWK key-operation list must not be null");
            Assert.notNull(algorithm, "JWK algorithm container must not be null");
            Assert.notNull(keyId, "JWK key-id container must not be null");
            Assert.notNull(certificateUrl, "JWK certificate URL container must not be null");
            Assert.notNull(certificateChain, "JWK certificate-chain list must not be null");
            Assert.notNull(certificateThumbprint, "JWK certificate-thumbprint container must not be null");
            Assert.notNull(
                    certificateThumbprintSha256,
                    "JWK SHA-256 certificate-thumbprint container must not be null");
            Assert.notNull(extensions, "JWK extension object must not be null");
            publicKeyUse.ifPresent(value -> Assert.notBlank(value, "JWK public key use must not be blank"));
            keyId.ifPresent(value -> Assert.notBlank(value, "JWK key id must not be blank"));
            certificateUrl.ifPresent(value -> {
                if (!value.isAbsolute()) {
                    throw new ValidateException("JWK certificate URL must be absolute");
                }
            });
            certificateThumbprint.ifPresent(Jwk::validateBase64Url);
            certificateThumbprintSha256.ifPresent(Jwk::validateBase64Url);
            keyOperations = validateOperations(keyOperations, publicKeyUse);
            certificateChain = validateCertificates(certificateChain);
            for (String name : extensions.values().keySet()) {
                Assert.notBlank(name, "JWK extension parameter name must not be blank");
                if (commonName(name)) {
                    throw new ValidateException("JWK extensions must not replace a common registered parameter");
                }
            }
            extensions = new JsonValue.ObjectValue(extensions.values());
        }

        /**
         * Validates operation uniqueness and consistency with a known public-key use.
         *
         * @param values operation names in wire order
         * @param use    optional public-key use
         * @return immutable validated operation list
         */
        private static List<String> validateOperations(final List<String> values, final Optional<String> use) {
            final Set<String> unique = new LinkedHashSet<>();
            for (String value : values) {
                Assert.notBlank(value, "JWK key operation must not be blank");
                if (!unique.add(value)) {
                    throw new ValidateException("JWK key_ops parameter must not contain duplicates");
                }
                if (use.filter(Builder.SIGNATURE::equals).isPresent() && encryptionOperation(value)
                        || use.filter("enc"::equals).isPresent() && signatureOperation(value)) {
                    throw new ValidateException("JWK use and key_ops parameters are inconsistent");
                }
            }
            return List.copyOf(values);
        }

        /**
         * Identifies signature-oriented registered key operations.
         *
         * @param value operation name
         * @return {@code true} for sign or verify
         */
        private static boolean signatureOperation(final String value) {
            return "sign".equals(value) || Builder.VERIFY.equals(value);
        }

        /**
         * Identifies encryption-oriented registered key operations.
         *
         * @param value operation name
         * @return {@code true} for an encryption, wrapping, or derivation operation
         */
        private static boolean encryptionOperation(final String value) {
            return switch (value) {
                case "encrypt", "decrypt", "wrapKey", "unwrapKey", "deriveKey", "deriveBits" -> true;
                default -> false;
            };
        }

        /**
         * Validates and freezes the standard Base64 certificate chain.
         *
         * @param values encoded certificates
         * @return immutable validated certificate list
         */
        private static List<String> validateCertificates(final List<String> values) {
            final List<String> copy = List.copyOf(values);
            for (String value : copy) {
                if (!standardBase64(value)) {
                    throw new ValidateException("JWK x5c entries must be standard Base64 certificate values");
                }
            }
            return copy;
        }

    }

    /**
     * Holds RFC 7518 RSA key parameters.
     *
     * @param modulus         public modulus
     * @param exponent        public exponent
     * @param privateExponent optional private exponent
     * @param firstPrime      optional first prime factor
     * @param secondPrime     optional second prime factor
     * @param firstExponent   optional first CRT exponent
     * @param secondExponent  optional second CRT exponent
     * @param coefficient     optional first CRT coefficient
     * @param otherPrimes     optional additional-prime information
     * @author Kimi Liu
     */
    public record Rsa(String modulus, String exponent, Optional<String> privateExponent, Optional<String> firstPrime,
            Optional<String> secondPrime, Optional<String> firstExponent, Optional<String> secondExponent,
            Optional<String> coefficient, List<OtherPrime> otherPrimes) implements Material {

        /**
         * Validates and freezes RSA integer parameters.
         */
        public Rsa {
            validateBase64Url(modulus);
            validateBase64Url(exponent);
            Assert.notNull(privateExponent, "RSA private-exponent container must not be null");
            Assert.notNull(firstPrime, "RSA first-prime container must not be null");
            Assert.notNull(secondPrime, "RSA second-prime container must not be null");
            Assert.notNull(firstExponent, "RSA first-exponent container must not be null");
            Assert.notNull(secondExponent, "RSA second-exponent container must not be null");
            Assert.notNull(coefficient, "RSA coefficient container must not be null");
            Assert.notNull(otherPrimes, "RSA additional-prime list must not be null");
            privateExponent.ifPresent(Jwk::validateBase64Url);
            firstPrime.ifPresent(Jwk::validateBase64Url);
            secondPrime.ifPresent(Jwk::validateBase64Url);
            firstExponent.ifPresent(Jwk::validateBase64Url);
            secondExponent.ifPresent(Jwk::validateBase64Url);
            coefficient.ifPresent(Jwk::validateBase64Url);
            otherPrimes = List.copyOf(otherPrimes);
            final int crtCount = (firstPrime.isPresent() ? 1 : 0) + (secondPrime.isPresent() ? 1 : 0)
                    + (firstExponent.isPresent() ? 1 : 0) + (secondExponent.isPresent() ? 1 : 0)
                    + (coefficient.isPresent() ? 1 : 0);
            if ((!otherPrimes.isEmpty() || crtCount != 0) && privateExponent.isEmpty()) {
                throw new ValidateException("RSA JWK CRT parameters require a private exponent");
            }
            if (crtCount != 0 && crtCount != 5) {
                throw new ValidateException("RSA JWK CRT parameters must be supplied as one complete set");
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String keyType() {
            return "RSA";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonValue.ObjectValue parameters() {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            values.put("n", string(modulus));
            values.put("e", string(exponent));
            privateExponent.ifPresent(value -> values.put("d", string(value)));
            firstPrime.ifPresent(value -> values.put("p", string(value)));
            secondPrime.ifPresent(value -> values.put("q", string(value)));
            firstExponent.ifPresent(value -> values.put("dp", string(value)));
            secondExponent.ifPresent(value -> values.put("dq", string(value)));
            coefficient.ifPresent(value -> values.put("qi", string(value)));
            if (!otherPrimes.isEmpty()) {
                values.put(
                        "oth",
                        new JsonValue.ArrayValue(
                                otherPrimes.stream().map(OtherPrime::parameters).map(JsonValue.class::cast).toList()));
            }
            return new JsonValue.ObjectValue(values);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean privateMaterial() {
            return privateExponent.isPresent() || firstPrime.isPresent() || secondPrime.isPresent()
                    || firstExponent.isPresent() || secondExponent.isPresent() || coefficient.isPresent()
                    || !otherPrimes.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Material publicOnly() {
            return new Rsa(modulus, exponent, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(),
                    Optional.empty(), Optional.empty(), List.of());
        }

    }

    /**
     * Holds one RSA multi-prime factor entry.
     *
     * @param prime       additional prime factor
     * @param exponent    CRT exponent for the factor
     * @param coefficient CRT coefficient for the factor
     * @author Kimi Liu
     */
    public record OtherPrime(String prime, String exponent, String coefficient) {

        /**
         * Validates all encoded unsigned integers.
         */
        public OtherPrime {
            validateBase64Url(prime);
            validateBase64Url(exponent);
            validateBase64Url(coefficient);
        }

        /**
         * Encodes this entry using the RFC 7518 short member names.
         *
         * @return immutable additional-prime object
         */
        private JsonValue.ObjectValue parameters() {
            return new JsonValue.ObjectValue(
                    Map.of("r", string(prime), "d", string(exponent), "t", string(coefficient)));
        }

    }

    /**
     * Holds RFC 7518 elliptic-curve key parameters.
     *
     * @param curve        registered curve name
     * @param x            x coordinate
     * @param y            y coordinate
     * @param privateValue optional private scalar
     * @author Kimi Liu
     */
    public record Ec(String curve, String x, String y, Optional<String> privateValue) implements Material {

        /**
         * Validates and freezes EC curve and coordinate parameters.
         */
        public Ec {
            Assert.notBlank(curve, "EC JWK curve must not be blank");
            validateBase64Url(x);
            validateBase64Url(y);
            Assert.notNull(privateValue, "EC JWK private-value container must not be null");
            privateValue.ifPresent(Jwk::validateBase64Url);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String keyType() {
            return "EC";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonValue.ObjectValue parameters() {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            values.put("crv", string(curve));
            values.put("x", string(x));
            values.put("y", string(y));
            privateValue.ifPresent(value -> values.put("d", string(value)));
            return new JsonValue.ObjectValue(values);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean privateMaterial() {
            return privateValue.isPresent();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Material publicOnly() {
            return new Ec(curve, x, y, Optional.empty());
        }

    }

    /**
     * Holds RFC 7518 symmetric key material.
     *
     * @param value encoded symmetric key bytes
     * @author Kimi Liu
     */
    public record Oct(String value) implements Material {

        /**
         * Validates the encoded symmetric key value.
         */
        public Oct {
            validateBase64Url(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String keyType() {
            return "oct";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonValue.ObjectValue parameters() {
            return new JsonValue.ObjectValue(Map.of("k", string(value)));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean privateMaterial() {
            return true;
        }

        /**
         * Rejects publication because symmetric key bytes have no public representation.
         *
         * @return never returns normally
         * @throws ValidateException always, because symmetric material cannot be published
         */
        @Override
        public Material publicOnly() {
            throw new ValidateException("Symmetric JWK cannot be published as a public key");
        }

    }

    /**
     * Holds RFC 8037 octet key-pair parameters.
     *
     * @param curve        registered curve name
     * @param x            encoded public key
     * @param privateValue optional encoded private key
     * @author Kimi Liu
     */
    public record Okp(String curve, String x, Optional<String> privateValue) implements Material {

        /**
         * Validates and freezes OKP parameters.
         */
        public Okp {
            Assert.notBlank(curve, "OKP JWK curve must not be blank");
            validateBase64Url(x);
            Assert.notNull(privateValue, "OKP JWK private-value container must not be null");
            privateValue.ifPresent(Jwk::validateBase64Url);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String keyType() {
            return "OKP";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonValue.ObjectValue parameters() {
            final Map<String, JsonValue> values = new LinkedHashMap<>();
            values.put("crv", string(curve));
            values.put("x", string(x));
            privateValue.ifPresent(value -> values.put("d", string(value)));
            return new JsonValue.ObjectValue(values);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean privateMaterial() {
            return privateValue.isPresent();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Material publicOnly() {
            return new Okp(curve, x, Optional.empty());
        }

    }

    /**
     * Preserves parameters for an extension key type whose private-material rules are unknown locally.
     *
     * @param keyType    registered or collision-resistant extension key type
     * @param parameters extension-defined key parameters
     * @author Kimi Liu
     */
    public record Extension(String keyType, JsonValue.ObjectValue parameters) implements Material {

        /**
         * Validates and freezes extension key material.
         */
        public Extension {
            Assert.notBlank(keyType, "Extension JWK key type must not be blank");
            Assert.notNull(parameters, "Extension JWK parameters must not be null");
            parameters = new JsonValue.ObjectValue(parameters.values());
        }

        /**
         * Returns {@code false} because unknown material cannot be classified, while publication remains prohibited.
         *
         * @return {@code false}; callers must still use {@link #publicOnly()} before publication
         */
        @Override
        public boolean privateMaterial() {
            return false;
        }

        /**
         * Rejects publication because unknown extension material cannot be proven public.
         *
         * @return never returns normally
         * @throws ValidateException always, because extension material cannot be safely filtered
         */
        @Override
        public Material publicOnly() {
            throw new ValidateException("Unknown JWK key type cannot be safely converted to a public key");
        }

    }

    /**
     * Carries the result of one JSON-to-component decode operation.
     *
     * @param common   decoded common parameters
     * @param material decoded key-type material
     * @author Kimi Liu
     */
    private record Decoded(Common common, Material material) {

        /**
         * Validates the completed decode result.
         */
        private Decoded {
            Assert.notNull(common, "Decoded JWK common parameters must not be null");
            Assert.notNull(material, "Decoded JWK material must not be null");
        }

    }

}
