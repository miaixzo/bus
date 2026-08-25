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

import java.io.ByteArrayOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECKey;
import java.security.interfaces.RSAKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PSSParameterSpec;
import java.util.*;

import javax.crypto.SecretKey;

import org.miaixz.bus.auth.guard.AlgorithmGuard;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.builtin.asymmetric.KeyType;
import org.miaixz.bus.crypto.center.HMac;
import org.miaixz.bus.crypto.center.Sign;
import org.miaixz.bus.extra.json.JsonKit;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Produces, parses, serializes, and verifies RFC 7515 JSON Web Signatures with profile-controlled algorithms.
 * <p>
 * The service preserves received protected-header octets through their Base64URL representation. It supports Compact,
 * Flattened JSON, and General JSON serialization while leaving key discovery and application processing of declared
 * critical extensions to the caller.
 * </p>
 *
 * @author Kimi Liu
 */
public class JwsService {

    /**
     * Critical extension used by RFC 7797, which this version deliberately does not implement.
     */
    private static final String UNENCODED_PAYLOAD = "b64";
    /**
     * Exact JWS JSON payload member name.
     */
    private static final String PAYLOAD = "payload";
    /**
     * Exact JWS JSON protected-header member name.
     */
    private static final String PROTECTED = "protected";
    /**
     * Exact JWS JSON unprotected-header member name.
     */
    private static final String HEADER = "header";
    /**
     * Exact JWS JSON signature member name.
     */
    private static final String SIGNATURE = "signature";
    /**
     * Exact General JWS JSON signatures-array member name.
     */
    private static final String SIGNATURES = "signatures";

    /**
     * Shared algorithm/key-direction validation primitive.
     */
    private final AlgorithmGuard algorithmGuard;
    /**
     * Immutable profile-specific JWS algorithm allow-list.
     */
    private final Set<String> allowedAlgorithms;
    /**
     * Whether operation keys must satisfy the registered JWA minimum strength.
     */
    private final boolean minimumKeyStrengthEnforced;

    /**
     * Creates a profile-scoped JWS service.
     *
     * @param algorithmGuard    shared algorithm validation primitive
     * @param allowedAlgorithms exact case-sensitive JWS algorithm allow-list
     */
    public JwsService(final AlgorithmGuard algorithmGuard, final Set<String> allowedAlgorithms) {
        this(algorithmGuard, allowedAlgorithms, true);
    }

    /**
     * Creates a profile-scoped JWS service with an explicit minimum-key-strength policy.
     * <p>
     * Disabling the minimum-strength check exists only for an explicitly selected legacy String-key compatibility
     * profile. Algorithm allow-list, key type, key direction, and signature verification checks remain mandatory.
     * </p>
     *
     * @param algorithmGuard             shared algorithm validation primitive
     * @param allowedAlgorithms          exact case-sensitive JWS algorithm allow-list
     * @param minimumKeyStrengthEnforced whether registered JWA minimum key sizes are enforced
     */
    public JwsService(
            final AlgorithmGuard algorithmGuard,
            final Set<String> allowedAlgorithms,
            final boolean minimumKeyStrengthEnforced) {
        this.algorithmGuard = Assert.notNull(algorithmGuard, "JWS algorithm guard must not be null");
        Assert.notNull(allowedAlgorithms, "JWS algorithm allowlist must not be null");
        this.allowedAlgorithms = Set.copyOf(allowedAlgorithms);
        this.minimumKeyStrengthEnforced = minimumKeyStrengthEnforced;
        if (this.allowedAlgorithms.isEmpty()) {
            throw new ValidateException("JWS algorithm allowlist must not be empty");
        }
        if (!minimumKeyStrengthEnforced && this.allowedAlgorithms.stream().anyMatch(name -> !name.startsWith("HS"))) {
            throw new ValidateException("Relaxed JWS minimum key strength is available for HMAC algorithms only");
        }
    }

    /**
     * Validates application-declared critical processing and rejects unsupported RFC 7797 semantics.
     *
     * @param header             combined JOSE Header
     * @param understoodCritical caller-processed extension names
     */
    private static void validateCritical(final JoseHeader header, final Set<String> understoodCritical) {
        Assert.notNull(understoodCritical, "Understood JWS critical parameters must not be null");
        if (header.critical().contains(UNENCODED_PAYLOAD)) {
            throw new ValidateException("Unencoded JWS payloads are not supported by this framework version");
        }
        header.validateCritical(understoodCritical);
    }

    /**
     * Computes a signature or MAC through a new bus-crypto operation object.
     *
     * @param algorithm    selected JWA algorithm
     * @param registration local execution mapping
     * @param key          signing key
     * @param input        exact signing input
     * @return raw cryptographic signature bytes
     */
    private static byte[] createSignature(
            final JwaAlgorithm algorithm,
            final JwaAlgorithm.Registration registration,
            final Key key,
            final byte[] input) {
        final Algorithm core = registration.coreAlgorithm()
                .orElseThrow(() -> new ValidateException("JWS algorithm has no exact bus-crypto mapping"));
        if (key instanceof SecretKey secretKey) {
            return Builder.hmac(core, secretKey).digest(input);
        }
        final Sign sign = new Sign(core, new KeyPair(null, (PrivateKey) key));
        configurePss(algorithm, sign);
        return sign.sign(input);
    }

    /**
     * Verifies a signature or MAC through a new bus-crypto operation object.
     *
     * @param algorithm    selected JWA algorithm
     * @param registration local execution mapping
     * @param key          verification key
     * @param input        exact signing input
     * @param signature    raw signature bytes in the bus-crypto expected form
     * @return whether the signature is valid
     */
    private static boolean verifySignature(
            final JwaAlgorithm algorithm,
            final JwaAlgorithm.Registration registration,
            final Key key,
            final byte[] input,
            final byte[] signature) {
        final Algorithm core = registration.coreAlgorithm()
                .orElseThrow(() -> new ValidateException("JWS algorithm has no exact bus-crypto mapping"));
        if (key instanceof SecretKey secretKey) {
            final HMac mac = Builder.hmac(core, secretKey);
            return mac.verify(mac.digest(input), signature);
        }
        final Sign sign = new Sign(core, new KeyPair((PublicKey) key, null));
        configurePss(algorithm, sign);
        return sign.verify(input, signature);
    }

    /**
     * Applies the RFC 7518 RSASSA-PSS salt and MGF digest parameters when selected.
     *
     * @param algorithm selected JWA algorithm
     * @param sign      new bus-crypto signature operation
     */
    private static void configurePss(final JwaAlgorithm algorithm, final Sign sign) {
        final PSSParameterSpec parameters;
        if (JwaAlgorithm.PS256.equals(algorithm)) {
            parameters = new PSSParameterSpec(Algorithm.SHA256.getValue(), "MGF1", MGF1ParameterSpec.SHA256, 32, 1);
        } else if (JwaAlgorithm.PS384.equals(algorithm)) {
            parameters = new PSSParameterSpec(Algorithm.SHA384.getValue(), "MGF1", MGF1ParameterSpec.SHA384, 48, 1);
        } else if (JwaAlgorithm.PS512.equals(algorithm)) {
            parameters = new PSSParameterSpec(Algorithm.SHA512.getValue(), "MGF1", MGF1ParameterSpec.SHA512, 64, 1);
        } else {
            parameters = null;
        }
        if (parameters != null) {
            sign.setParameter(parameters);
        }
    }

    /**
     * Builds the exact ASCII JWS signing input.
     *
     * @param encodedProtected preserved protected segment
     * @param payload          exact payload octets
     * @return newly allocated signing input
     */
    private static byte[] signingInput(final String encodedProtected, final byte[] payload) {
        return (encodedProtected + Symbol.C_DOT + Base64.encodeUrlSafe(payload)).getBytes(Charset.US_ASCII);
    }

    /**
     * Strictly decodes an unpadded Base64URL segment through bus-core.
     *
     * @param value        encoded segment
     * @param emptyAllowed whether the empty octet sequence is legal here
     * @return decoded octets
     */
    private static byte[] decodeBase64Url(final String value, final boolean emptyAllowed) {
        Assert.notNull(value, "Base64URL segment must not be null");
        if (value.isEmpty()) {
            if (emptyAllowed) {
                return Normal.EMPTY_BYTE_ARRAY;
            }
            throw new ValidateException("Required Base64URL segment must not be empty");
        }
        if ((value.length() & 3) == 1) {
            throw new ValidateException("Base64URL segment has an invalid length");
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (!(character >= Symbol.C_UPPER_A && character <= Symbol.C_UPPER_Z)
                    && !(character >= Symbol.C_LOWER_A && character <= Symbol.C_LOWER_Z)
                    && !(character >= Symbol.C_ZERO && character <= Symbol.C_NINE) && character != Symbol.C_MINUS
                    && character != Symbol.C_UNDERLINE) {
                throw new ValidateException("Base64URL segment contains an invalid character");
            }
        }
        return Base64.decode(value);
    }

    /**
     * Returns a mandatory string member from a JWS JSON object.
     *
     * @param object source object
     * @param name   exact member name
     * @return decoded string
     */
    private static String requiredString(final JsonValue.ObjectValue object, final String name) {
        final JsonValue value = object.values().get(name);
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Required JWS JSON member must be a string");
        }
        return string.value();
    }

    /**
     * Returns an optional string member from a JWS JSON object.
     *
     * @param object   source object
     * @param name     exact member name
     * @param fallback value returned when absent
     * @return decoded or fallback string
     */
    private static String optionalString(final JsonValue.ObjectValue object, final String name, final String fallback) {
        final JsonValue value = object.values().get(name);
        if (value == null) {
            return fallback;
        }
        if (!(value instanceof JsonValue.StringValue string)) {
            throw new ValidateException("JWS JSON member must be a string");
        }
        return string.value();
    }

    /**
     * Maps a JCA key to the equivalent case-sensitive JWK family.
     *
     * @param key selected execution key
     * @return JWK family name
     */
    private static String keyFamily(final Key key) {
        if (key instanceof SecretKey) {
            return "oct";
        }
        if (key instanceof RSAKey) {
            return "RSA";
        }
        if (key instanceof ECKey) {
            return "EC";
        }
        if (Algorithm.ED25519.getValue().equalsIgnoreCase(key.getAlgorithm())
                || JwaAlgorithm.EDDSA.name().equalsIgnoreCase(key.getAlgorithm())) {
            return "OKP";
        }
        throw new ValidateException("JWS key family is not supported");
    }

    /**
     * Calculates effective key strength without exporting private material.
     *
     * @param key selected execution key
     * @return key size in bits
     */
    private static int keyBits(final Key key) {
        if (key instanceof RSAKey rsa) {
            return rsa.getModulus().bitLength();
        }
        if (key instanceof ECKey ec) {
            return ec.getParams().getOrder().bitLength();
        }
        if (key instanceof SecretKey secret && secret.getEncoded() != null) {
            return secret.getEncoded().length * Byte.SIZE;
        }
        if (Algorithm.ED25519.getValue().equalsIgnoreCase(key.getAlgorithm())
                || JwaAlgorithm.EDDSA.name().equalsIgnoreCase(key.getAlgorithm())) {
            return 256;
        }
        throw new ValidateException("JWS key strength cannot be determined");
    }

    /**
     * Tests whether the selected registration uses ECDSA's JOSE R||S representation.
     *
     * @param algorithm selected JWA algorithm
     * @return whether ECDSA conversion is required
     */
    private static boolean isEc(final JwaAlgorithm algorithm) {
        return JwaAlgorithm.ES256.equals(algorithm) || JwaAlgorithm.ES384.equals(algorithm)
                || JwaAlgorithm.ES512.equals(algorithm);
    }

    /**
     * Returns the fixed unsigned integer width for an ECDSA JOSE signature.
     *
     * @param algorithm selected ECDSA registration
     * @return bytes per R or S integer
     */
    private static int ecPartLength(final JwaAlgorithm algorithm) {
        if (JwaAlgorithm.ES256.equals(algorithm)) {
            return 32;
        }
        if (JwaAlgorithm.ES384.equals(algorithm)) {
            return 48;
        }
        if (JwaAlgorithm.ES512.equals(algorithm)) {
            return 66;
        }
        throw new ValidateException("Unsupported ECDSA JWA algorithm");
    }

    /**
     * Converts a DER SEQUENCE of ECDSA integers into fixed-width JOSE R||S form.
     *
     * @param der        DER-encoded ECDSA signature
     * @param partLength required unsigned width of each integer
     * @return JOSE signature bytes
     */
    private static byte[] derToJose(final byte[] der, final int partLength) {
        final int[] offset = { 0 };
        if (readByte(der, offset) != 0x30) {
            throw new ValidateException("ECDSA signature is not a DER sequence");
        }
        final int sequenceLength = readLength(der, offset);
        if (sequenceLength != der.length - offset[0] || readByte(der, offset) != 0x02) {
            throw new ValidateException("ECDSA DER sequence length is invalid");
        }
        final byte[] r = readInteger(der, offset);
        if (readByte(der, offset) != 0x02) {
            throw new ValidateException("ECDSA DER signature is missing S");
        }
        final byte[] s = readInteger(der, offset);
        if (offset[0] != der.length) {
            throw new ValidateException("ECDSA DER signature contains trailing data");
        }
        final byte[] jose = new byte[partLength * 2];
        copyUnsigned(r, jose, 0, partLength);
        copyUnsigned(s, jose, partLength, partLength);
        return jose;
    }

    /**
     * Converts fixed-width JOSE R||S bytes into a DER ECDSA signature.
     *
     * @param jose       JOSE signature bytes
     * @param partLength required unsigned width of each integer
     * @return DER-encoded ECDSA signature
     */
    private static byte[] joseToDer(final byte[] jose, final int partLength) {
        if (jose.length != partLength * 2) {
            throw new ValidateException("ECDSA JOSE signature length is invalid");
        }
        final byte[] r = unsignedInteger(Arrays.copyOfRange(jose, 0, partLength));
        final byte[] s = unsignedInteger(Arrays.copyOfRange(jose, partLength, jose.length));
        final ByteArrayOutputStream body = new ByteArrayOutputStream();
        body.write(0x02);
        writeLength(body, r.length);
        body.writeBytes(r);
        body.write(0x02);
        writeLength(body, s.length);
        body.writeBytes(s);
        final ByteArrayOutputStream sequence = new ByteArrayOutputStream();
        sequence.write(0x30);
        writeLength(sequence, body.size());
        sequence.writeBytes(body.toByteArray());
        return sequence.toByteArray();
    }

    /**
     * Reads one unsigned byte from DER input with bounds checking.
     *
     * @param input  DER input
     * @param offset mutable single-element cursor
     * @return unsigned byte value
     */
    private static int readByte(final byte[] input, final int[] offset) {
        if (offset[0] >= input.length) {
            throw new ValidateException("ECDSA DER signature ended unexpectedly");
        }
        return input[offset[0]++] & 0xff;
    }

    /**
     * Reads a definite DER length.
     *
     * @param input  DER input
     * @param offset mutable single-element cursor
     * @return decoded length
     */
    private static int readLength(final byte[] input, final int[] offset) {
        final int first = readByte(input, offset);
        if (first < 0x80) {
            return first;
        }
        final int count = first & 0x7f;
        if (count == 0 || count > 2) {
            throw new ValidateException("ECDSA DER length form is invalid");
        }
        int length = 0;
        for (int index = 0; index < count; index++) {
            length = (length << 8) | readByte(input, offset);
        }
        return length;
    }

    /**
     * Reads and minimally validates one positive DER INTEGER body.
     *
     * @param input  DER input
     * @param offset mutable single-element cursor
     * @return integer body bytes
     */
    private static byte[] readInteger(final byte[] input, final int[] offset) {
        final int length = readLength(input, offset);
        if (length == 0 || offset[0] + length > input.length) {
            throw new ValidateException("ECDSA DER integer length is invalid");
        }
        final byte[] value = Arrays.copyOfRange(input, offset[0], offset[0] + length);
        offset[0] += length;
        if ((value[0] & 0x80) != 0 || value.length > 1 && value[0] == 0 && (value[1] & 0x80) == 0) {
            throw new ValidateException("ECDSA DER integer is not minimally encoded and positive");
        }
        return value;
    }

    /**
     * Copies a positive DER INTEGER into one fixed-width JOSE component.
     *
     * @param integer      positive DER integer bytes
     * @param output       JOSE destination
     * @param outputOffset destination component offset
     * @param width        destination component width
     */
    private static void copyUnsigned(
            final byte[] integer,
            final byte[] output,
            final int outputOffset,
            final int width) {
        final int sourceOffset = integer.length > 1 && integer[0] == 0 ? 1 : 0;
        final int length = integer.length - sourceOffset;
        if (length > width) {
            throw new ValidateException("ECDSA integer exceeds the selected curve width");
        }
        System.arraycopy(integer, sourceOffset, output, outputOffset + width - length, length);
    }

    /**
     * Converts a fixed-width unsigned integer into minimal positive DER INTEGER bytes.
     *
     * @param value fixed-width unsigned bytes
     * @return minimally encoded positive integer body
     */
    private static byte[] unsignedInteger(final byte[] value) {
        int first = 0;
        while (first < value.length - 1 && value[first] == 0) {
            first++;
        }
        final boolean prefix = (value[first] & 0x80) != 0;
        final byte[] result = new byte[value.length - first + (prefix ? 1 : 0)];
        System.arraycopy(value, first, result, prefix ? 1 : 0, value.length - first);
        return result;
    }

    /**
     * Writes a definite DER length using the shortest supported representation.
     *
     * @param output DER destination
     * @param length content length
     */
    private static void writeLength(final ByteArrayOutputStream output, final int length) {
        if (length < 0x80) {
            output.write(length);
        } else if (length <= 0xff) {
            output.write(0x81);
            output.write(length);
        } else {
            output.write(0x82);
            output.write(length >>> 8);
            output.write(length);
        }
    }

    /**
     * Computes one JWS signature over the supplied payload.
     *
     * @param header  validated JOSE Header whose {@code alg} member is protected
     * @param payload exact payload octets
     * @param key     explicit private or symmetric signing key
     * @return immutable signature retaining its encoded protected header
     */
    public Signature sign(final JoseHeader header, final byte[] payload, final Key key) {
        Assert.notNull(header, "JWS header must not be null");
        Assert.notNull(payload, "JWS payload must not be null");
        Assert.notNull(key, "JWS signing key must not be null");
        header.requireProtected(Set.of(JoseHeader.ALGORITHM));
        validateCritical(header, Set.of());
        final JwaAlgorithm algorithm = JwaAlgorithm.of(header.algorithm());
        final JwaAlgorithm.Registration registration = algorithm.require(JwaAlgorithm.Kind.SIGNATURE);
        validateKey(algorithm, registration, key, true);
        final String encodedProtected = Base64.encodeUrlSafe(JsonKit.writeValue(header.protectedParameters()));
        final byte[] input = signingInput(encodedProtected, payload);
        byte[] signature = createSignature(algorithm, registration, key, input);
        if (isEc(algorithm)) {
            signature = derToJose(signature, ecPartLength(algorithm));
        }
        return new Signature(header, encodedProtected, signature);
    }

    /**
     * Verifies one signature against exact payload octets and an explicitly selected key.
     *
     * @param signature          parsed or locally created JWS signature
     * @param payload            exact payload octets
     * @param key                explicit public or symmetric verification key
     * @param understoodCritical critical extension names already processed by the calling application
     * @throws ValidateException if structure, algorithm, key, critical processing, or signature validation fails
     */
    public void verify(
            final Signature signature,
            final byte[] payload,
            final Key key,
            final Set<String> understoodCritical) {
        Assert.notNull(signature, "JWS signature must not be null");
        Assert.notNull(payload, "JWS payload must not be null");
        Assert.notNull(key, "JWS verification key must not be null");
        assertProtectedMatches(signature);
        signature.header().requireProtected(Set.of(JoseHeader.ALGORITHM));
        validateCritical(signature.header(), understoodCritical);
        final JwaAlgorithm algorithm = JwaAlgorithm.of(signature.header().algorithm());
        final JwaAlgorithm.Registration registration = algorithm.require(JwaAlgorithm.Kind.SIGNATURE);
        validateKey(algorithm, registration, key, false);
        final byte[] input = signingInput(signature.encodedProtected(), payload);
        byte[] candidate = signature.value();
        if (isEc(algorithm)) {
            candidate = joseToDer(candidate, ecPartLength(algorithm));
        }
        if (!verifySignature(algorithm, registration, key, input, candidate)) {
            throw new ValidateException("JWS signature validation failed");
        }
    }

    /**
     * Serializes a single protected-only signature using JWS Compact Serialization.
     *
     * @param jws immutable JWS containing exactly one signature
     * @return three-segment compact representation
     */
    public String compact(final Jws jws) {
        Assert.notNull(jws, "JWS value must not be null");
        if (jws.signatures().size() != 1) {
            throw new ValidateException("JWS Compact Serialization requires exactly one signature");
        }
        final Signature signature = jws.signatures().get(0);
        if (!signature.header().unprotectedParameters().values().isEmpty()) {
            throw new ValidateException("JWS Compact Serialization does not permit an unprotected header");
        }
        assertProtectedMatches(signature);
        return signature.encodedProtected() + Symbol.C_DOT + Base64.encodeUrlSafe(jws.payload()) + Symbol.C_DOT
                + Base64.encodeUrlSafe(signature.value());
    }

    /**
     * Parses a three-segment JWS Compact Serialization without selecting or validating a key.
     *
     * @param compact            exact compact representation
     * @param understoodCritical critical extension names already processed by the calling application
     * @return immutable parsed JWS preserving the protected segment
     */
    public Jws parseCompact(final String compact, final Set<String> understoodCritical) {
        Assert.notNull(compact, "JWS Compact Serialization must not be null");
        final String[] segments = compact.split("\\.", -1);
        if (segments.length != 3 || segments[0].isEmpty() || segments[2].isEmpty()) {
            throw new ValidateException("JWS Compact Serialization must contain three valid segments");
        }
        final JoseHeader header = header(segments[0], new JsonValue.ObjectValue(Map.of()));
        validateCritical(header, understoodCritical);
        final Signature signature = new Signature(header, segments[0], decodeBase64Url(segments[2], false));
        return new Jws(decodeBase64Url(segments[1], true), List.of(signature));
    }

    /**
     * Serializes a JWS using the Flattened or General JSON representation.
     *
     * @param jws           immutable JWS payload and signatures
     * @param serialization requested RFC 7515 JSON form
     * @return implementation-neutral JSON object
     */
    public JsonValue.ObjectValue json(final Jws jws, final Serialization serialization) {
        Assert.notNull(jws, "JWS value must not be null");
        Assert.notNull(serialization, "JWS JSON serialization kind must not be null");
        final Map<String, JsonValue> result = new LinkedHashMap<>();
        result.put(PAYLOAD, new JsonValue.StringValue(Base64.encodeUrlSafe(jws.payload())));
        if (serialization == Serialization.FLATTENED) {
            if (jws.signatures().size() != 1) {
                throw new ValidateException("Flattened JWS JSON Serialization requires exactly one signature");
            }
            result.putAll(signatureJson(jws.signatures().get(0)).values());
        } else {
            final List<JsonValue> signatures = new ArrayList<>(jws.signatures().size());
            for (Signature signature : jws.signatures()) {
                signatures.add(signatureJson(signature));
            }
            result.put(SIGNATURES, new JsonValue.ArrayValue(signatures));
        }
        return new JsonValue.ObjectValue(result);
    }

    /**
     * Parses Flattened or General JWS JSON Serialization without selecting or validating keys.
     *
     * @param value              complete implementation-neutral JSON object
     * @param understoodCritical critical extension names already processed by the calling application
     * @return immutable parsed JWS preserving every protected segment
     */
    public Jws parseJson(final JsonValue.ObjectValue value, final Set<String> understoodCritical) {
        Assert.notNull(value, "JWS JSON Serialization must not be null");
        final byte[] payload = decodeBase64Url(requiredString(value, PAYLOAD), true);
        final List<Signature> signatures = new ArrayList<>();
        final JsonValue general = value.values().get(SIGNATURES);
        if (general == null) {
            signatures.add(parseSignature(value, understoodCritical));
        } else {
            if (value.values().containsKey(SIGNATURE) || value.values().containsKey(PROTECTED)
                    || value.values().containsKey(HEADER)) {
                throw new ValidateException("General JWS JSON Serialization must not contain flattened members");
            }
            if (!(general instanceof JsonValue.ArrayValue array) || array.values().isEmpty()) {
                throw new ValidateException("JWS signatures member must be a non-empty array");
            }
            for (JsonValue element : array.values()) {
                if (!(element instanceof JsonValue.ObjectValue object)) {
                    throw new ValidateException("JWS signatures entries must be JSON objects");
                }
                signatures.add(parseSignature(object, understoodCritical));
            }
        }
        return new Jws(payload, signatures);
    }

    /**
     * Encodes one signature object using exact RFC 7515 JSON member names.
     *
     * @param signature signature to encode
     * @return flattened signature members
     */
    private JsonValue.ObjectValue signatureJson(final Signature signature) {
        assertProtectedMatches(signature);
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        if (!signature.encodedProtected().isEmpty()) {
            values.put(PROTECTED, new JsonValue.StringValue(signature.encodedProtected()));
        }
        if (!signature.header().unprotectedParameters().values().isEmpty()) {
            values.put(HEADER, signature.header().unprotectedParameters());
        }
        values.put(SIGNATURE, new JsonValue.StringValue(Base64.encodeUrlSafe(signature.value())));
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Parses one flattened signature object shared by both JSON forms.
     *
     * @param value              signature JSON object
     * @param understoodCritical application-processed critical extensions
     * @return parsed signature preserving its protected segment
     */
    private Signature parseSignature(final JsonValue.ObjectValue value, final Set<String> understoodCritical) {
        final String encodedProtected = optionalString(value, PROTECTED, Normal.EMPTY);
        final JsonValue headerValue = value.values().get(HEADER);
        final JsonValue.ObjectValue unprotected;
        if (headerValue == null) {
            unprotected = new JsonValue.ObjectValue(Map.of());
        } else if (headerValue instanceof JsonValue.ObjectValue object) {
            unprotected = object;
        } else {
            throw new ValidateException("JWS header member must be a JSON object");
        }
        if (encodedProtected.isEmpty() && unprotected.values().isEmpty()) {
            throw new ValidateException("JWS JSON signature must contain a protected or unprotected header");
        }
        final JoseHeader header = header(encodedProtected, unprotected);
        validateCritical(header, understoodCritical);
        return new Signature(header, encodedProtected, decodeBase64Url(requiredString(value, SIGNATURE), false));
    }

    /**
     * Decodes one protected segment and combines it with an unprotected JSON object.
     *
     * @param encodedProtected original Base64URL protected segment
     * @param unprotected      unprotected header members
     * @return validated combined JOSE Header
     */
    private JoseHeader header(final String encodedProtected, final JsonValue.ObjectValue unprotected) {
        final JsonValue.ObjectValue protectedValues;
        if (encodedProtected.isEmpty()) {
            protectedValues = new JsonValue.ObjectValue(Map.of());
        } else {
            final JsonValue parsed = JsonKit.readValue(decodeBase64Url(encodedProtected, false));
            if (!(parsed instanceof JsonValue.ObjectValue object)) {
                throw new ValidateException("JWS protected header must decode to a JSON object");
            }
            protectedValues = object;
        }
        return new JoseHeader(protectedValues, unprotected);
    }

    /**
     * Confirms that the parsed header structurally matches the preserved protected segment.
     *
     * @param signature signature carrying both representations
     */
    private void assertProtectedMatches(final Signature signature) {
        if (signature.encodedProtected().isEmpty()) {
            if (!signature.header().protectedParameters().values().isEmpty()) {
                throw new ValidateException("JWS protected representation does not match its header");
            }
            return;
        }
        final JsonValue decoded = JsonKit.readValue(decodeBase64Url(signature.encodedProtected(), false));
        if (!signature.header().protectedParameters().equals(decoded)) {
            throw new ValidateException("JWS protected representation does not match its header");
        }
    }

    /**
     * Validates allow-list, key direction, JWK family equivalent, and minimum strength.
     *
     * @param algorithm    selected JWA algorithm
     * @param registration local registration metadata
     * @param key          execution key
     * @param signing      whether signing rather than verification is requested
     */
    private void validateKey(
            final JwaAlgorithm algorithm,
            final JwaAlgorithm.Registration registration,
            final Key key,
            final boolean signing) {
        final KeyType keyType;
        final AlgorithmGuard.Usage usage;
        if (algorithm.name().startsWith("HS")) {
            keyType = KeyType.SecretKey;
            usage = signing ? AlgorithmGuard.Usage.MAC_CREATE : AlgorithmGuard.Usage.MAC_VERIFY;
        } else {
            keyType = signing ? KeyType.PrivateKey : KeyType.PublicKey;
            usage = signing ? AlgorithmGuard.Usage.SIGN : AlgorithmGuard.Usage.VERIFY;
        }
        algorithmGuard.validate(algorithm.name(), allowedAlgorithms, key, keyType, usage);
        final String family = keyFamily(key);
        if (!registration.keyTypes().contains(family)) {
            throw new ValidateException("JWS key family does not match the selected algorithm");
        }
        if (minimumKeyStrengthEnforced && keyBits(key) < registration.minimumKeyBits()) {
            throw new ValidateException("JWS key does not meet the selected algorithm minimum strength");
        }
    }

    /**
     * Selects an RFC 7515 JSON serialization form.
     *
     * @author Kimi Liu
     */
    public enum Serialization {
        /**
         * Single-signature flattened JSON object.
         */
        FLATTENED,
        /**
         * General JSON object containing a non-empty signatures array.
         */
        GENERAL

    }

    /**
     * Carries decoded payload octets and one or more JWS signatures.
     *
     * @param payload    decoded JWS payload octets
     * @param signatures non-empty signatures in JSON wire order
     * @author Kimi Liu
     */
    public record Jws(byte[] payload, List<Signature> signatures) {

        /**
         * Defensively copies payload and signatures.
         *
         * @throws IllegalArgumentException if a component or signature is {@code null}
         * @throws ValidateException        if the signature list is empty
         */
        public Jws {
            Assert.notNull(payload, "JWS payload must not be null");
            Assert.notNull(signatures, "JWS signatures must not be null");
            payload = payload.clone();
            signatures = List.copyOf(signatures);
            if (signatures.isEmpty()) {
                throw new ValidateException("JWS must contain at least one signature");
            }
        }

        /**
         * Returns a detached payload copy.
         *
         * @return decoded payload octets
         */
        @Override
        public byte[] payload() {
            return payload.clone();
        }

    }

    /**
     * Carries a combined JOSE Header, its original protected representation, and raw signature bytes.
     *
     * @param header           validated combined JOSE Header
     * @param encodedProtected original unpadded Base64URL protected-header segment, possibly empty for JSON form
     * @param value            decoded signature or MAC bytes
     * @author Kimi Liu
     */
    public record Signature(JoseHeader header, String encodedProtected, byte[] value) {

        /**
         * Defensively copies signature material while retaining the exact protected text.
         *
         * @throws IllegalArgumentException if a component is {@code null}
         * @throws ValidateException        if the signature value is empty
         */
        public Signature {
            Assert.notNull(header, "JWS signature header must not be null");
            Assert.notNull(encodedProtected, "JWS encoded protected header must not be null");
            Assert.notNull(value, "JWS signature value must not be null");
            value = value.clone();
            if (value.length == 0) {
                throw new ValidateException("JWS signature value must not be empty");
            }
        }

        /**
         * Returns a detached signature copy.
         *
         * @return decoded signature or MAC bytes
         */
        @Override
        public byte[] value() {
            return value.clone();
        }

    }

}
