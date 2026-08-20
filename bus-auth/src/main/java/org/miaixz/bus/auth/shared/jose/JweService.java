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

import java.security.Key;
import java.security.interfaces.RSAKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.util.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

import org.miaixz.bus.auth.guard.AlgorithmGuard;
import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.*;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.builtin.asymmetric.KeyType;
import org.miaixz.bus.crypto.cipher.JceCipher;
import org.miaixz.bus.extra.json.JsonProvider;
import org.miaixz.bus.extra.json.JsonValue;

/**
 * Produces, parses, serializes, and decrypts RFC 7516 JSON Web Encryption values using explicit profile allow-lists.
 * <p>
 * The service retains protected, shared unprotected, and per-recipient Header sections independently so Flattened and
 * General JSON serializations can be reproduced without losing their security boundaries. Cryptographic execution is
 * intentionally limited to the modern algorithm combinations documented by this framework version.
 * </p>
 *
 * @author Kimi Liu
 */
public final class JweService {

    /**
     * Exact JWE JSON protected Header member name.
     */
    private static final String PROTECTED = "protected";
    /**
     * Exact JWE JSON shared unprotected Header member name.
     */
    private static final String UNPROTECTED = "unprotected";
    /**
     * Exact JWE JSON per-recipient Header member name.
     */
    private static final String HEADER = "header";
    /**
     * Exact JWE JSON external additional authenticated data member name.
     */
    private static final String AAD = "aad";
    /**
     * Exact JWE JSON encrypted content-encryption key member name.
     */
    private static final String ENCRYPTED_KEY = "encrypted_key";
    /**
     * Exact JWE JSON initialization vector member name.
     */
    private static final String INITIALIZATION_VECTOR = "iv";
    /**
     * Exact JWE JSON ciphertext member name.
     */
    private static final String CIPHERTEXT = "ciphertext";
    /**
     * Exact JWE JSON authentication tag member name.
     */
    private static final String TAG = "tag";
    /**
     * Exact General JWE JSON recipients member name.
     */
    private static final String RECIPIENTS = "recipients";
    /**
     * AES-GCM initialization vector size required by the JWA profile.
     */
    private static final int GCM_IV_BYTES = 12;
    /**
     * AES-GCM authentication tag size required by the JWA profile.
     */
    private static final int GCM_TAG_BYTES = 16;

    /**
     * Runtime-supplied provider-neutral JSON codec.
     */
    private final JsonProvider jsonProvider;
    /**
     * Shared algorithm and key-direction guard.
     */
    private final AlgorithmGuard algorithmGuard;
    /**
     * Immutable key-management algorithm allow-list.
     */
    private final Set<String> allowedKeyAlgorithms;
    /**
     * Immutable content-encryption algorithm allow-list.
     */
    private final Set<String> allowedContentAlgorithms;

    /**
     * Creates a profile-scoped JWE service.
     *
     * @param jsonProvider             provider-neutral JSON codec
     * @param algorithmGuard           shared algorithm validation primitive
     * @param allowedKeyAlgorithms     exact case-sensitive key-management allow-list
     * @param allowedContentAlgorithms exact case-sensitive content-encryption allow-list
     */
    public JweService(final JsonProvider jsonProvider, final AlgorithmGuard algorithmGuard,
            final Set<String> allowedKeyAlgorithms, final Set<String> allowedContentAlgorithms) {
        this.jsonProvider = Assert.notNull(jsonProvider, "JWE JSON provider must not be null");
        this.algorithmGuard = Assert.notNull(algorithmGuard, "JWE algorithm guard must not be null");
        Assert.notNull(allowedKeyAlgorithms, "JWE key algorithm allowlist must not be null");
        Assert.notNull(allowedContentAlgorithms, "JWE content algorithm allowlist must not be null");
        this.allowedKeyAlgorithms = Set.copyOf(allowedKeyAlgorithms);
        this.allowedContentAlgorithms = Set.copyOf(allowedContentAlgorithms);
        if (this.allowedKeyAlgorithms.isEmpty() || this.allowedContentAlgorithms.isEmpty()) {
            throw new ValidateException("JWE algorithm allowlists must not be empty");
        }
    }

    /**
     * Builds and validates complete JOSE Headers for all recipients.
     *
     * @param protectedHeader   protected members
     * @param unprotectedHeader shared unprotected members
     * @param recipientHeaders  per-recipient unprotected members
     * @return recipient-complete headers in wire order
     */
    private static List<JoseHeader> headers(
            final JsonValue.ObjectValue protectedHeader,
            final JsonValue.ObjectValue unprotectedHeader,
            final List<JsonValue.ObjectValue> recipientHeaders) {
        final List<JoseHeader> values = new ArrayList<>(recipientHeaders.size());
        for (JsonValue.ObjectValue recipient : recipientHeaders) {
            values.add(header(protectedHeader, unprotectedHeader, recipient));
        }
        return values;
    }

    /**
     * Creates one complete recipient JOSE Header while rejecting shared/recipient duplicate members.
     *
     * @param protectedHeader   protected members
     * @param unprotectedHeader shared unprotected members
     * @param recipientHeader   per-recipient unprotected members
     * @return combined validated Header
     */
    private static JoseHeader header(
            final JsonValue.ObjectValue protectedHeader,
            final JsonValue.ObjectValue unprotectedHeader,
            final JsonValue.ObjectValue recipientHeader) {
        final Map<String, JsonValue> unprotected = new LinkedHashMap<>(unprotectedHeader.values());
        for (Map.Entry<String, JsonValue> entry : recipientHeader.values().entrySet()) {
            if (unprotected.put(entry.getKey(), entry.getValue()) != null) {
                throw new ValidateException("JWE Header parameter appears in shared and per-recipient sections");
            }
        }
        return new JoseHeader(protectedHeader, new JsonValue.ObjectValue(unprotected));
    }

    /**
     * Validates critical processing, protected content algorithm, compression exclusion, and execution registration.
     *
     * @param header             recipient-complete JOSE Header
     * @param understoodCritical caller-processed critical extensions
     */
    private static void validateHeader(final JoseHeader header, final Set<String> understoodCritical) {
        Assert.notNull(understoodCritical, "Understood JWE critical parameters must not be null");
        header.validateCritical(understoodCritical);
        header.requireProtected(Set.of(JoseHeader.ENCRYPTION));
        if (header.parameter(JoseHeader.COMPRESSION).isPresent()) {
            throw new ValidateException("JWE compression is not supported by this framework version");
        }
        JwaAlgorithm.of(header.algorithm()).require(JwaAlgorithm.Kind.KEY_MANAGEMENT);
        JwaAlgorithm.of(header.encryption().orElseThrow(() -> new ValidateException("JWE Header must contain enc")))
                .require(JwaAlgorithm.Kind.CONTENT_ENCRYPTION);
    }

    /**
     * Returns the exact AES key length for an executable content-encryption algorithm.
     *
     * @param algorithm selected content-encryption algorithm
     * @return AES key length in bits
     */
    private static int contentKeyBits(final JwaAlgorithm algorithm) {
        return switch (algorithm.name()) {
            case "A128GCM" -> 128;
            case "A192GCM" -> 192;
            case "A256GCM" -> 256;
            default -> throw new ValidateException("Unsupported JWE content-encryption algorithm");
        };
    }

    /**
     * Creates a bus-crypto JCE wrapper for one executable key-management algorithm.
     *
     * @param algorithm exact JWA key-management name
     * @return uninitialized cipher wrapper
     */
    private static JceCipher managementCipher(final String algorithm) {
        return switch (algorithm) {
            case "RSA-OAEP", "RSA-OAEP-256" -> new JceCipher("RSA/ECB/OAEPPadding");
            case "A128KW", "A192KW", "A256KW" -> new JceCipher("AESWrap");
            default -> throw new ValidateException("Unsupported JWE key-management algorithm");
        };
    }

    /**
     * Returns exact OAEP parameters or no parameters for AES Key Wrap.
     *
     * @param algorithm exact JWA key-management name
     * @return algorithm parameters, or {@code null} for AES Key Wrap
     */
    private static AlgorithmParameterSpec managementParameters(final String algorithm) {
        return switch (algorithm) {
            case "RSA-OAEP" -> OAEPParameterSpec.DEFAULT;
            case "RSA-OAEP-256" -> new OAEPParameterSpec(Algorithm.SHA256.getValue(), "MGF1", MGF1ParameterSpec.SHA256,
                    PSource.PSpecified.DEFAULT);
            case "A128KW", "A192KW", "A256KW" -> null;
            default -> throw new ValidateException("Unsupported JWE key-management parameters");
        };
    }

    /**
     * Runs AES-GCM content encryption or authenticated decryption through bus-crypto.
     *
     * @param mode              encrypt or decrypt mode
     * @param algorithm         selected content algorithm
     * @param contentKey        exact-length AES content key
     * @param iv                96-bit initialization vector
     * @param authenticatedData exact JWE AAD input
     * @param input             plaintext for encryption or ciphertext for decryption
     * @param tag               empty for encryption or 128-bit authentication tag for decryption
     * @return ciphertext||tag for encryption or plaintext for decryption
     */
    private static byte[] cryptContent(
            final Algorithm.Type mode,
            final JwaAlgorithm algorithm,
            final SecretKey contentKey,
            final byte[] iv,
            final byte[] authenticatedData,
            final byte[] input,
            final byte[] tag) {
        if (iv.length != GCM_IV_BYTES || mode == Algorithm.Type.DECRYPT && tag.length != GCM_TAG_BYTES) {
            throw new ValidateException("JWE AES-GCM IV or authentication tag length is invalid");
        }
        if (keyBits(contentKey) != contentKeyBits(algorithm)) {
            throw new ValidateException("JWE content key length does not match enc");
        }
        final JceCipher cipher = new JceCipher("AES/GCM/NoPadding");
        cipher.init(
                mode,
                new JceCipher.JceParameters(contentKey, new GCMParameterSpec(GCM_TAG_BYTES * Byte.SIZE, iv),
                        mode == Algorithm.Type.ENCRYPT ? RandomKit.getSecureRandom() : null));
        cipher.updateAad(authenticatedData);
        return cipher.processFinal(mode == Algorithm.Type.ENCRYPT ? input : concatenate(input, tag));
    }

    /**
     * Constructs exact JWE Additional Authenticated Data bytes.
     *
     * @param encodedProtected original protected Base64URL text
     * @param aad              decoded external AAD bytes
     * @return ASCII authentication input
     */
    private static byte[] authenticatedData(final String encodedProtected, final byte[] aad) {
        final String value = aad.length == 0 ? encodedProtected
                : encodedProtected + Symbol.C_DOT + Base64.encodeUrlSafe(aad);
        return value.getBytes(Charset.US_ASCII);
    }

    /**
     * Concatenates ciphertext and authentication tag for JCE AES-GCM decryption.
     *
     * @param ciphertext ciphertext bytes
     * @param tag        authentication tag bytes
     * @return combined input
     */
    private static byte[] concatenate(final byte[] ciphertext, final byte[] tag) {
        final byte[] result = Arrays.copyOf(ciphertext, ciphertext.length + tag.length);
        System.arraycopy(tag, 0, result, ciphertext.length, tag.length);
        return result;
    }

    /**
     * Generates cryptographically strong bytes through the bus-core random facade.
     *
     * @param length requested byte length
     * @return newly allocated unpredictable bytes
     */
    private static byte[] secureBytes(final int length) {
        return RandomKit.randomBytes(length, RandomKit.getSecureRandom());
    }

    /**
     * Calculates key strength without exposing its contents beyond the execution boundary.
     *
     * @param key selected execution key
     * @return key size in bits
     */
    private static int keyBits(final Key key) {
        if (key instanceof RSAKey rsa) {
            return rsa.getModulus().bitLength();
        }
        if (key instanceof SecretKey secret && secret.getEncoded() != null) {
            return secret.getEncoded().length * Byte.SIZE;
        }
        throw new ValidateException("JWE key strength cannot be determined");
    }

    /**
     * Encodes one recipient using exact RFC 7516 JSON member names.
     *
     * @param recipient recipient to encode
     * @return recipient JSON object
     */
    private static JsonValue.ObjectValue recipientJson(final Recipient recipient) {
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        if (!recipient.header().values().isEmpty()) {
            values.put(HEADER, recipient.header());
        }
        values.put(ENCRYPTED_KEY, new JsonValue.StringValue(Base64.encodeUrlSafe(recipient.encryptedKey())));
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Parses one Flattened or General recipient object.
     *
     * @param value recipient JSON object
     * @return immutable parsed recipient
     */
    private static Recipient parseRecipient(final JsonValue.ObjectValue value) {
        return new Recipient(optionalObject(value, HEADER),
                decodeBase64Url(optionalString(value, ENCRYPTED_KEY, Normal.EMPTY), true));
    }

    /**
     * Strictly decodes an unpadded Base64URL value through bus-core.
     *
     * @param value        encoded text
     * @param emptyAllowed whether the empty octet sequence is legal
     * @return decoded octets
     */
    private static byte[] decodeBase64Url(final String value, final boolean emptyAllowed) {
        Assert.notNull(value, "Base64URL value must not be null");
        if (value.isEmpty()) {
            if (emptyAllowed) {
                return Normal.EMPTY_BYTE_ARRAY;
            }
            throw new ValidateException("Required Base64URL value must not be empty");
        }
        if ((value.length() & 3) == 1) {
            throw new ValidateException("Base64URL value has an invalid length");
        }
        for (int index = 0; index < value.length(); index++) {
            final char character = value.charAt(index);
            if (!(character >= 'A' && character <= 'Z') && !(character >= 'a' && character <= 'z')
                    && !(character >= Symbol.C_ZERO && character <= Symbol.C_NINE) && character != Symbol.C_MINUS
                    && character != Symbol.C_UNDERLINE) {
                throw new ValidateException("Base64URL value contains an invalid character");
            }
        }
        return Base64.decode(value);
    }

    /**
     * Returns a mandatory JSON string member.
     *
     * @param value source object
     * @param name  exact member name
     * @return decoded string
     */
    private static String requiredString(final JsonValue.ObjectValue value, final String name) {
        final JsonValue member = value.values().get(name);
        if (!(member instanceof JsonValue.StringValue string)) {
            throw new ValidateException("Required JWE JSON member must be a string");
        }
        return string.value();
    }

    /**
     * Returns an optional JSON string member.
     *
     * @param value    source object
     * @param name     exact member name
     * @param fallback value returned when absent
     * @return decoded or fallback string
     */
    private static String optionalString(final JsonValue.ObjectValue value, final String name, final String fallback) {
        final JsonValue member = value.values().get(name);
        if (member == null) {
            return fallback;
        }
        if (!(member instanceof JsonValue.StringValue string)) {
            throw new ValidateException("JWE JSON member must be a string");
        }
        return string.value();
    }

    /**
     * Returns an optional JSON object member or an empty object.
     *
     * @param value source object
     * @param name  exact member name
     * @return object member or immutable empty object
     */
    private static JsonValue.ObjectValue optionalObject(final JsonValue.ObjectValue value, final String name) {
        final JsonValue member = value.values().get(name);
        if (member == null) {
            return new JsonValue.ObjectValue(Map.of());
        }
        return object(member, name);
    }

    /**
     * Requires one provider-neutral JSON value to be an object.
     *
     * @param value candidate value
     * @param name  semantic member name used for diagnostics
     * @return object value
     */
    private static JsonValue.ObjectValue object(final JsonValue value, final String name) {
        if (!(value instanceof JsonValue.ObjectValue object)) {
            throw new ValidateException("JWE " + name + " member must be a JSON object");
        }
        return object;
    }

    /**
     * Encrypts one plaintext for one or more explicitly keyed recipients.
     *
     * @param encryption complete encryption request with independently sourced Header sections
     * @return immutable JWE value retaining all serialization boundaries
     */
    public Jwe encrypt(final Encryption encryption) {
        Assert.notNull(encryption, "JWE encryption request must not be null");
        final String encodedProtected = Base64.encodeUrlSafe(jsonProvider.writeValue(encryption.protectedHeader()));
        final List<JoseHeader> headers = headers(
                encryption.protectedHeader(),
                encryption.unprotectedHeader(),
                encryption.recipients().stream().map(EncryptionRecipient::header).toList());
        final JwaAlgorithm contentAlgorithm = contentAlgorithm(headers);
        final int contentKeyBits = contentKeyBits(contentAlgorithm);
        final boolean direct = headers.stream().anyMatch(header -> "dir".equals(header.algorithm()));
        if (direct && (headers.size() != 1 || !headers.stream().allMatch(header -> "dir".equals(header.algorithm())))) {
            throw new ValidateException("JWE direct key management requires exactly one recipient");
        }
        final SecretKey contentKey = direct
                ? requireDirectKey(encryption.recipients().get(0).key(), contentKeyBits, headers.get(0))
                : Keeper.generateKey(Algorithm.AES.getValue(), secureBytes(contentKeyBits / Byte.SIZE));
        validateContentKey(contentAlgorithm, contentKey, AlgorithmGuard.Usage.ENCRYPT);
        final List<Recipient> recipients = new ArrayList<>(headers.size());
        for (int index = 0; index < headers.size(); index++) {
            final JoseHeader header = headers.get(index);
            validateHeader(header, Set.of());
            final byte[] encryptedKey = wrap(contentKey, encryption.recipients().get(index).key(), header);
            recipients.add(new Recipient(encryption.recipients().get(index).header(), encryptedKey));
        }
        final byte[] iv = secureBytes(GCM_IV_BYTES);
        final byte[] authenticatedData = authenticatedData(encodedProtected, encryption.aad());
        final byte[] combined = cryptContent(
                Algorithm.Type.ENCRYPT,
                contentAlgorithm,
                contentKey,
                iv,
                authenticatedData,
                encryption.plaintext(),
                Normal.EMPTY_BYTE_ARRAY);
        final byte[] ciphertext = Arrays.copyOf(combined, combined.length - GCM_TAG_BYTES);
        final byte[] tag = Arrays.copyOfRange(combined, combined.length - GCM_TAG_BYTES, combined.length);
        return new Jwe(encryption.protectedHeader(), encodedProtected, encryption.unprotectedHeader(), encryption.aad(),
                iv, ciphertext, tag, recipients);
    }

    /**
     * Decrypts one selected recipient after validating all Header, algorithm, key, and authentication constraints.
     *
     * @param jwe                parsed or locally produced JWE value
     * @param recipientIndex     zero-based recipient position in wire order
     * @param key                explicit private or symmetric recipient key
     * @param understoodCritical critical extension names already processed by the caller
     * @return newly allocated plaintext octets
     */
    public byte[] decrypt(
            final Jwe jwe,
            final int recipientIndex,
            final Key key,
            final Set<String> understoodCritical) {
        Assert.notNull(jwe, "JWE value must not be null");
        Assert.notNull(key, "JWE decryption key must not be null");
        if (recipientIndex < 0 || recipientIndex >= jwe.recipients().size()) {
            throw new ValidateException("JWE recipient index is outside the recipient array");
        }
        assertProtectedMatches(jwe);
        final Recipient recipient = jwe.recipients().get(recipientIndex);
        final JoseHeader header = header(jwe.protectedHeader(), jwe.unprotectedHeader(), recipient.header());
        validateHeader(header, understoodCritical);
        final JwaAlgorithm contentAlgorithm = contentAlgorithm(List.of(header));
        final int contentKeyBits = contentKeyBits(contentAlgorithm);
        final SecretKey contentKey = unwrap(recipient.encryptedKey(), key, header, contentKeyBits);
        validateContentKey(contentAlgorithm, contentKey, AlgorithmGuard.Usage.DECRYPT);
        final byte[] authenticatedData = authenticatedData(jwe.encodedProtected(), jwe.aad());
        try {
            return cryptContent(
                    Algorithm.Type.DECRYPT,
                    contentAlgorithm,
                    contentKey,
                    jwe.initializationVector(),
                    authenticatedData,
                    jwe.ciphertext(),
                    jwe.tag());
        } catch (CryptoException | IllegalArgumentException cause) {
            throw new ValidateException("JWE authentication or decryption failed", cause);
        }
    }

    /**
     * Serializes a single-recipient protected-only JWE using Compact Serialization.
     *
     * @param jwe immutable JWE value
     * @return five-segment compact representation
     */
    public String compact(final Jwe jwe) {
        Assert.notNull(jwe, "JWE value must not be null");
        if (jwe.recipients().size() != 1 || !jwe.unprotectedHeader().values().isEmpty()
                || !jwe.recipients().get(0).header().values().isEmpty() || jwe.aad().length != 0) {
            throw new ValidateException(
                    "JWE Compact Serialization requires one protected-only recipient and no external AAD");
        }
        assertProtectedMatches(jwe);
        return jwe.encodedProtected() + Symbol.C_DOT + Base64.encodeUrlSafe(jwe.recipients().get(0).encryptedKey())
                + Symbol.C_DOT + Base64.encodeUrlSafe(jwe.initializationVector()) + Symbol.C_DOT
                + Base64.encodeUrlSafe(jwe.ciphertext()) + Symbol.C_DOT + Base64.encodeUrlSafe(jwe.tag());
    }

    /**
     * Parses five-segment JWE Compact Serialization without selecting or using a recipient key.
     *
     * @param compact            exact compact representation
     * @param understoodCritical critical extension names already processed by the caller
     * @return immutable parsed JWE preserving its protected segment
     */
    public Jwe parseCompact(final String compact, final Set<String> understoodCritical) {
        Assert.notNull(compact, "JWE Compact Serialization must not be null");
        final String[] segments = compact.split("\\.", -1);
        if (segments.length != 5 || segments[0].isEmpty() || segments[2].isEmpty() || segments[4].isEmpty()) {
            throw new ValidateException("JWE Compact Serialization must contain five structurally valid segments");
        }
        final JsonValue protectedValue = jsonProvider.readValue(decodeBase64Url(segments[0], false));
        if (!(protectedValue instanceof JsonValue.ObjectValue protectedHeader)) {
            throw new ValidateException("JWE protected Header must decode to a JSON object");
        }
        final JsonValue.ObjectValue empty = new JsonValue.ObjectValue(Map.of());
        final JoseHeader header = header(protectedHeader, empty, empty);
        validateHeader(header, understoodCritical);
        return new Jwe(protectedHeader, segments[0], empty, Normal.EMPTY_BYTE_ARRAY,
                decodeBase64Url(segments[2], false), decodeBase64Url(segments[3], true),
                decodeBase64Url(segments[4], false), List.of(new Recipient(empty, decodeBase64Url(segments[1], true))));
    }

    /**
     * Serializes a JWE using Flattened or General JSON Serialization.
     *
     * @param jwe           immutable JWE value
     * @param serialization requested RFC 7516 JSON form
     * @return provider-neutral JWE JSON object
     */
    public JsonValue.ObjectValue json(final Jwe jwe, final Serialization serialization) {
        Assert.notNull(jwe, "JWE value must not be null");
        Assert.notNull(serialization, "JWE JSON serialization kind must not be null");
        assertProtectedMatches(jwe);
        final Map<String, JsonValue> values = new LinkedHashMap<>();
        if (!jwe.encodedProtected().isEmpty()) {
            values.put(PROTECTED, new JsonValue.StringValue(jwe.encodedProtected()));
        }
        if (!jwe.unprotectedHeader().values().isEmpty()) {
            values.put(UNPROTECTED, jwe.unprotectedHeader());
        }
        if (jwe.aad().length != 0) {
            values.put(AAD, new JsonValue.StringValue(Base64.encodeUrlSafe(jwe.aad())));
        }
        values.put(INITIALIZATION_VECTOR, new JsonValue.StringValue(Base64.encodeUrlSafe(jwe.initializationVector())));
        values.put(CIPHERTEXT, new JsonValue.StringValue(Base64.encodeUrlSafe(jwe.ciphertext())));
        values.put(TAG, new JsonValue.StringValue(Base64.encodeUrlSafe(jwe.tag())));
        if (serialization == Serialization.FLATTENED) {
            if (jwe.recipients().size() != 1) {
                throw new ValidateException("Flattened JWE JSON Serialization requires exactly one recipient");
            }
            values.putAll(recipientJson(jwe.recipients().get(0)).values());
        } else {
            final List<JsonValue> recipients = new ArrayList<>(jwe.recipients().size());
            for (Recipient recipient : jwe.recipients()) {
                recipients.add(recipientJson(recipient));
            }
            values.put(RECIPIENTS, new JsonValue.ArrayValue(recipients));
        }
        return new JsonValue.ObjectValue(values);
    }

    /**
     * Parses Flattened or General JWE JSON Serialization without selecting or using a recipient key.
     *
     * @param value              complete provider-neutral JWE JSON object
     * @param understoodCritical critical extension names already processed by the caller
     * @return immutable parsed JWE preserving protected, shared, and recipient sections
     */
    public Jwe parseJson(final JsonValue.ObjectValue value, final Set<String> understoodCritical) {
        Assert.notNull(value, "JWE JSON Serialization must not be null");
        final String encodedProtected = optionalString(value, PROTECTED, Normal.EMPTY);
        final JsonValue.ObjectValue protectedHeader = encodedProtected.isEmpty() ? new JsonValue.ObjectValue(Map.of())
                : object(jsonProvider.readValue(decodeBase64Url(encodedProtected, false)), PROTECTED);
        final JsonValue.ObjectValue unprotected = optionalObject(value, UNPROTECTED);
        final byte[] aad = decodeBase64Url(optionalString(value, AAD, Normal.EMPTY), true);
        final List<Recipient> recipients = new ArrayList<>();
        final JsonValue general = value.values().get(RECIPIENTS);
        if (general == null) {
            recipients.add(parseRecipient(value));
        } else {
            if (value.values().containsKey(HEADER) || value.values().containsKey(ENCRYPTED_KEY)) {
                throw new ValidateException(
                        "General JWE JSON Serialization must not contain flattened recipient members");
            }
            if (!(general instanceof JsonValue.ArrayValue array) || array.values().isEmpty()) {
                throw new ValidateException("JWE recipients member must be a non-empty array");
            }
            for (JsonValue element : array.values()) {
                recipients.add(parseRecipient(object(element, RECIPIENTS)));
            }
        }
        final Jwe jwe = new Jwe(protectedHeader, encodedProtected, unprotected, aad,
                decodeBase64Url(requiredString(value, INITIALIZATION_VECTOR), false),
                decodeBase64Url(requiredString(value, CIPHERTEXT), true),
                decodeBase64Url(requiredString(value, TAG), false), recipients);
        for (Recipient recipient : recipients) {
            validateHeader(header(protectedHeader, unprotected, recipient.header()), understoodCritical);
        }
        return jwe;
    }

    /**
     * Requires one common protected content-encryption algorithm across all recipients.
     *
     * @param headers complete recipient Headers
     * @return selected content-encryption algorithm
     */
    private JwaAlgorithm contentAlgorithm(final List<JoseHeader> headers) {
        if (headers.isEmpty()) {
            throw new ValidateException("JWE must contain at least one recipient");
        }
        JwaAlgorithm selected = null;
        for (JoseHeader header : headers) {
            final JwaAlgorithm candidate = JwaAlgorithm.of(header.encryption().orElseThrow());
            if (selected != null && !selected.equals(candidate)) {
                throw new ValidateException("JWE recipients must use one shared content-encryption algorithm");
            }
            selected = candidate;
        }
        if (!allowedContentAlgorithms.contains(selected.name())) {
            throw new ValidateException("JWE content-encryption algorithm is not allowed by the selected profile");
        }
        if (!Set.of("A128GCM", "A192GCM", "A256GCM").contains(selected.name())) {
            throw new ValidateException("JWE content-encryption algorithm is not executable by this framework version");
        }
        return selected;
    }

    /**
     * Applies the shared algorithm guard to an exact-length symmetric content-encryption key.
     *
     * @param algorithm selected JWA content-encryption algorithm
     * @param key       AES content-encryption key
     * @param usage     encryption or decryption direction
     */
    private void validateContentKey(
            final JwaAlgorithm algorithm,
            final SecretKey key,
            final AlgorithmGuard.Usage usage) {
        algorithmGuard.validate(algorithm.name(), allowedContentAlgorithms, key, KeyType.SecretKey, usage);
    }

    /**
     * Validates and returns a direct content-encryption key.
     *
     * @param key            supplied direct key
     * @param contentKeyBits exact required AES key length
     * @param header         recipient-complete Header
     * @return validated symmetric content key
     */
    private SecretKey requireDirectKey(final Key key, final int contentKeyBits, final JoseHeader header) {
        validateManagementKey(header, key, false);
        if (!(key instanceof SecretKey secret) || keyBits(secret) != contentKeyBits) {
            throw new ValidateException("JWE direct key length must exactly match the content-encryption algorithm");
        }
        return secret;
    }

    /**
     * Wraps a shared content key for one recipient using an executable key-management algorithm.
     *
     * @param contentKey   generated or direct content key
     * @param recipientKey explicit recipient key
     * @param header       recipient-complete Header
     * @return encrypted_key bytes, empty only for {@code dir}
     */
    private byte[] wrap(final SecretKey contentKey, final Key recipientKey, final JoseHeader header) {
        validateManagementKey(header, recipientKey, false);
        final String algorithm = header.algorithm();
        if ("dir".equals(algorithm)) {
            return Normal.EMPTY_BYTE_ARRAY;
        }
        try {
            final JceCipher cipher = managementCipher(algorithm);
            cipher.init(
                    Algorithm.Type.ENCRYPT,
                    new JceCipher.JceParameters(recipientKey, managementParameters(algorithm),
                            RandomKit.getSecureRandom()));
            return cipher.processFinal(contentKey.getEncoded());
        } catch (CryptoException | IllegalArgumentException cause) {
            throw new ValidateException("JWE content key wrapping failed", cause);
        }
    }

    /**
     * Resolves the content key by direct use or key unwrapping.
     *
     * @param encryptedKey   encrypted_key bytes
     * @param recipientKey   explicit recipient decryption key
     * @param header         recipient-complete Header
     * @param contentKeyBits exact required content key length
     * @return AES content-encryption key
     */
    private SecretKey unwrap(
            final byte[] encryptedKey,
            final Key recipientKey,
            final JoseHeader header,
            final int contentKeyBits) {
        validateManagementKey(header, recipientKey, true);
        final String algorithm = header.algorithm();
        if ("dir".equals(algorithm)) {
            if (encryptedKey.length != 0) {
                throw new ValidateException("JWE direct key management requires an empty encrypted_key");
            }
            return requireDirectKey(recipientKey, contentKeyBits, header);
        }
        if (encryptedKey.length == 0) {
            throw new ValidateException("JWE wrapped key management requires encrypted_key");
        }
        try {
            final JceCipher cipher = managementCipher(algorithm);
            cipher.init(
                    Algorithm.Type.DECRYPT,
                    new JceCipher.JceParameters(recipientKey, managementParameters(algorithm), null));
            final byte[] key = cipher.processFinal(encryptedKey);
            if (key.length * Byte.SIZE != contentKeyBits) {
                throw new ValidateException("JWE unwrapped content key has an invalid length");
            }
            return Keeper.generateKey(Algorithm.AES.getValue(), key);
        } catch (CryptoException | IllegalArgumentException cause) {
            throw new ValidateException("JWE content key unwrapping failed", cause);
        }
    }

    /**
     * Validates one key-management algorithm, allow-list, key family, direction, and strength.
     *
     * @param header     recipient-complete Header
     * @param key        explicit recipient key
     * @param decrypting whether unwrapping/decryption is requested
     */
    private void validateManagementKey(final JoseHeader header, final Key key, final boolean decrypting) {
        Assert.notNull(key, "JWE key-management key must not be null");
        final JwaAlgorithm algorithm = JwaAlgorithm.of(header.algorithm());
        final JwaAlgorithm.Registration registration = algorithm.require(JwaAlgorithm.Kind.KEY_MANAGEMENT);
        if (!Set.of("dir", "RSA-OAEP", "RSA-OAEP-256", "A128KW", "A192KW", "A256KW").contains(algorithm.name())) {
            throw new ValidateException("JWE key-management algorithm is not executable by this framework version");
        }
        final boolean symmetric = algorithm.name().equals("dir") || algorithm.name().endsWith("KW");
        final KeyType keyType = symmetric ? KeyType.SecretKey : decrypting ? KeyType.PrivateKey : KeyType.PublicKey;
        final AlgorithmGuard.Usage usage = decrypting ? AlgorithmGuard.Usage.UNWRAP : AlgorithmGuard.Usage.WRAP;
        algorithmGuard.validate(algorithm.name(), allowedKeyAlgorithms, key, keyType, usage);
        final String family = symmetric ? "oct" : key instanceof RSAKey ? "RSA" : Normal.EMPTY;
        if (!registration.keyTypes().contains(family) || keyBits(key) < registration.minimumKeyBits()) {
            throw new ValidateException("JWE key does not match key-management algorithm requirements");
        }
        if (algorithm.name().matches("A(128|192|256)KW")) {
            final int required = Integer.parseInt(algorithm.name().substring(1, 4));
            if (keyBits(key) != required) {
                throw new ValidateException("JWE AES Key Wrap key length does not match its algorithm");
            }
        }
    }

    /**
     * Verifies that a JWE's parsed protected object matches its preserved Base64URL representation.
     *
     * @param jwe JWE carrying both representations
     */
    private void assertProtectedMatches(final Jwe jwe) {
        if (jwe.encodedProtected().isEmpty()) {
            if (!jwe.protectedHeader().values().isEmpty()) {
                throw new ValidateException("JWE protected representation does not match its Header");
            }
            return;
        }
        final JsonValue decoded = jsonProvider.readValue(decodeBase64Url(jwe.encodedProtected(), false));
        if (!jwe.protectedHeader().equals(decoded)) {
            throw new ValidateException("JWE protected representation does not match its Header");
        }
    }

    /**
     * Selects an RFC 7516 JSON serialization form.
     *
     * @author Kimi Liu
     */
    public enum Serialization {
        /**
         * Single-recipient flattened JSON object.
         */
        FLATTENED,
        /**
         * General JSON object containing a non-empty recipients array.
         */
        GENERAL

    }

    /**
     * Describes all inputs required to encrypt a JWE without hiding Header source boundaries.
     *
     * @param plaintext         exact plaintext octets
     * @param protectedHeader   integrity-protected Header members
     * @param unprotectedHeader shared unprotected Header members
     * @param aad               decoded external additional authenticated data
     * @param recipients        non-empty recipient inputs in requested wire order
     * @author Kimi Liu
     */
    public record Encryption(byte[] plaintext, JsonValue.ObjectValue protectedHeader,
            JsonValue.ObjectValue unprotectedHeader, byte[] aad, List<EncryptionRecipient> recipients) {

        /**
         * Defensively copies the encryption request.
         *
         * @throws IllegalArgumentException if a component or recipient is {@code null}
         * @throws ValidateException        if no recipient is supplied
         */
        public Encryption {
            Assert.notNull(plaintext, "JWE plaintext must not be null");
            Assert.notNull(protectedHeader, "JWE protected Header must not be null");
            Assert.notNull(unprotectedHeader, "JWE shared unprotected Header must not be null");
            Assert.notNull(aad, "JWE external AAD must not be null");
            Assert.notNull(recipients, "JWE encryption recipients must not be null");
            plaintext = plaintext.clone();
            protectedHeader = new JsonValue.ObjectValue(protectedHeader.values());
            unprotectedHeader = new JsonValue.ObjectValue(unprotectedHeader.values());
            aad = aad.clone();
            recipients = List.copyOf(recipients);
            if (recipients.isEmpty()) {
                throw new ValidateException("JWE encryption requires at least one recipient");
            }
        }

        /**
         * Returns a detached copy of the plaintext so callers cannot mutate the retained encryption request.
         *
         * @return plaintext octets owned by the caller
         */
        @Override
        public byte[] plaintext() {
            return plaintext.clone();
        }

        /**
         * Returns a detached copy of the external authenticated data retained by the encryption request.
         *
         * @return external AAD octets owned by the caller
         */
        @Override
        public byte[] aad() {
            return aad.clone();
        }

    }

    /**
     * Couples one per-recipient Header with the explicit key used to encrypt or supply the content key.
     *
     * @param header per-recipient unprotected Header members
     * @param key    explicit recipient public or symmetric key
     * @author Kimi Liu
     */
    public record EncryptionRecipient(JsonValue.ObjectValue header, Key key) {

        /**
         * Validates and detaches the per-recipient Header.
         */
        public EncryptionRecipient {
            Assert.notNull(header, "JWE recipient Header must not be null");
            Assert.notNull(key, "JWE recipient key must not be null");
            header = new JsonValue.ObjectValue(header.values());
        }

    }

    /**
     * Represents one serialized JWE recipient without retaining an execution key.
     *
     * @param header       per-recipient unprotected Header members
     * @param encryptedKey decoded encrypted_key bytes, empty for direct key management
     * @author Kimi Liu
     */
    public record Recipient(JsonValue.ObjectValue header, byte[] encryptedKey) {

        /**
         * Validates and detaches recipient wire values.
         */
        public Recipient {
            Assert.notNull(header, "JWE recipient Header must not be null");
            Assert.notNull(encryptedKey, "JWE encrypted key must not be null");
            header = new JsonValue.ObjectValue(header.values());
            encryptedKey = encryptedKey.clone();
        }

        /**
         * Returns a detached copy of the recipient's decoded {@code encrypted_key} value.
         *
         * @return encrypted key octets owned by the caller
         */
        @Override
        public byte[] encryptedKey() {
            return encryptedKey.clone();
        }

    }

    /**
     * Retains every structural component of one Compact, Flattened JSON, or General JSON JWE.
     *
     * @param protectedHeader      parsed protected Header members
     * @param encodedProtected     original protected Header Base64URL representation
     * @param unprotectedHeader    shared unprotected Header members
     * @param aad                  decoded external AAD bytes
     * @param initializationVector decoded initialization vector
     * @param ciphertext           decoded ciphertext
     * @param tag                  decoded authentication tag
     * @param recipients           non-empty recipients in wire order
     * @author Kimi Liu
     */
    public record Jwe(JsonValue.ObjectValue protectedHeader, String encodedProtected,
            JsonValue.ObjectValue unprotectedHeader, byte[] aad, byte[] initializationVector, byte[] ciphertext,
            byte[] tag, List<Recipient> recipients) {

        /**
         * Validates and defensively copies all JWE wire components.
         *
         * @throws IllegalArgumentException if a component or recipient is {@code null}
         * @throws ValidateException        if recipients, IV, or tag are empty
         */
        public Jwe {
            Assert.notNull(protectedHeader, "JWE protected Header must not be null");
            Assert.notNull(encodedProtected, "JWE encoded protected Header must not be null");
            Assert.notNull(unprotectedHeader, "JWE shared unprotected Header must not be null");
            Assert.notNull(aad, "JWE external AAD must not be null");
            Assert.notNull(initializationVector, "JWE initialization vector must not be null");
            Assert.notNull(ciphertext, "JWE ciphertext must not be null");
            Assert.notNull(tag, "JWE authentication tag must not be null");
            Assert.notNull(recipients, "JWE recipients must not be null");
            protectedHeader = new JsonValue.ObjectValue(protectedHeader.values());
            unprotectedHeader = new JsonValue.ObjectValue(unprotectedHeader.values());
            aad = aad.clone();
            initializationVector = initializationVector.clone();
            ciphertext = ciphertext.clone();
            tag = tag.clone();
            recipients = List.copyOf(recipients);
            if (recipients.isEmpty() || initializationVector.length == 0 || tag.length == 0) {
                throw new ValidateException("JWE recipients, initialization vector, and tag must not be empty");
            }
        }

        /**
         * Returns a detached copy of the decoded external authenticated data.
         *
         * @return external AAD octets owned by the caller
         */
        @Override
        public byte[] aad() {
            return aad.clone();
        }

        /**
         * Returns a detached copy of the decoded content-encryption initialization vector.
         *
         * @return initialization vector octets owned by the caller
         */
        @Override
        public byte[] initializationVector() {
            return initializationVector.clone();
        }

        /**
         * Returns a detached copy of the decoded encrypted content.
         *
         * @return ciphertext octets owned by the caller
         */
        @Override
        public byte[] ciphertext() {
            return ciphertext.clone();
        }

        /**
         * Returns a detached copy of the decoded content-authentication tag.
         *
         * @return authentication tag octets owned by the caller
         */
        @Override
        public byte[] tag() {
            return tag.clone();
        }

    }

}
