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
package org.miaixz.bus.cortex.setting.secret;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Map;

import org.miaixz.bus.core.codec.binary.Base64;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.center.AES;
import org.miaixz.bus.extra.json.JsonKit;

/**
 * Seals and opens secret items during delivery of a published configuration.
 * <p>
 * Each instance binds the application, release, configuration code, and request nonce as AES-GCM authenticated data.
 * Callers own the key bytes and may clear them after use.
 *
 * @author Kimi Liu
 */
public class SealingSecretCodec implements SecretCodec {

    /**
     * Header carrying the gateway's signed forwarding credential to the registry.
     */
    public static final String FORWARDING_TOKEN = "X-SDC-Forwarding-Token";

    /**
     * Header carrying the request nonce across both hops.
     */
    public static final String NONCE = "X-SDC-Request-Nonce";

    /**
     * Purpose claim for the gateway's startup forwarding credential.
     */
    public static final String PURPOSE = "sdc-startup-config-v1";

    /**
     * Result code for a registered application whose configuration has not been published.
     */
    public static final String NOT_PUBLISHED = "SDC_CONFIG_NOT_PUBLISHED";

    /**
     * Maximum configuration payload size in bytes.
     */
    public static final int MAX_BYTES = 256 * 1024;

    /**
     * Startup response schema version.
     */
    public static final int SCHEMA = 1;

    /**
     * Version marker for encrypted configuration items.
     */
    private static final String VERSION = "sdc.secret.v1";
    /**
     * Accepted syntax for application codes, release identifiers, and configuration codes.
     */
    private static final String PART = "[A-Za-z0-9._-]{1,128}";
    /**
     * Cryptographically secure random source for request nonces.
     */
    private static final SecureRandom RANDOM = RandomKit.getSecureRandom();

    /**
     * Key identifier used to seal a value, or null for an instance that only opens values.
     */
    private final String sealingKeyId;
    /**
     * Delivery keys by identifier.
     */
    private final Map<String, byte[]> keys;
    /**
     * Authenticated data for this item and request.
     */
    private final byte[] authenticatedData;

    /**
     * Creates a codec that can open values with any of the application's current or previous delivery keys.
     *
     * @param keys     delivery keys by identifier
     * @param app      application code
     * @param revision published release identifier
     * @param code     configuration code
     * @param nonce    request nonce
     */
    public SealingSecretCodec(Map<String, byte[]> keys, String app, String revision, String code, String nonce) {
        this(null, keys, app, revision, code, nonce);
    }

    /**
     * Creates a codec that seals and opens values with the selected delivery key.
     *
     * @param keyId    identifier of the selected delivery key
     * @param key      decoded delivery key
     * @param app      application code
     * @param revision published release identifier
     * @param code     configuration code
     * @param nonce    request nonce
     */
    public SealingSecretCodec(String keyId, byte[] key, String app, String revision, String code, String nonce) {
        this(keyId, Map.of(keyId, key), app, revision, code, nonce);
    }

    private SealingSecretCodec(String keyId, Map<String, byte[]> keys, String app, String revision, String code,
            String nonce) {
        if (keyId != null) {
            requireKeyId(keyId);
            if (keys.get(keyId).length != Normal._32) {
                throw new IllegalArgumentException("Invalid SDC key length");
            }
        }
        this.sealingKeyId = keyId;
        this.keys = Map.copyOf(keys);
        this.authenticatedData = authenticatedData(app, revision, code, nonce);
    }

    /**
     * Checks the syntax of an application code, release identifier, or configuration code.
     *
     * @param value protocol coordinate to validate
     * @return {@code true} when the value matches the coordinate syntax
     */
    public static boolean validCoordinate(String value) {
        return value != null && value.matches(PART);
    }

    /**
     * Checks the format of a startup request nonce.
     *
     * @param value request nonce to validate
     * @return {@code true} when the value contains exactly 32 URL-safe Base64 characters
     */
    public static boolean validNonce(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{32}");
    }

    /**
     * Checks the syntax and length of an application reader credential before authentication.
     *
     * @param value credential to validate
     * @return {@code true} when the value matches the reader credential format
     */
    public static boolean validCredential(String value) {
        return value != null && value.matches("[A-Za-z0-9._~-]{32,256}");
    }

    /**
     * Generates a fresh nonce for one startup request.
     *
     * @return 24 random bytes encoded as unpadded URL-safe Base64
     */
    public static String nonce() {
        return Base64.encodeUrlSafe(RandomKit.randomBytes(Normal._24, RANDOM));
    }

    /**
     * Identifies a configuration value with the supported encrypted item prefix.
     *
     * @param value configuration value from the response
     * @return {@code true} when the value starts with the encrypted item marker
     */
    public static boolean sealed(String value) {
        return value != null && value.startsWith(VERSION + Symbol.DOT);
    }

    /**
     * Decodes a URL-safe Base64 key for the startup configuration protocol.
     *
     * @param encoded encoded key material
     * @return 32 decoded key bytes
     * @throws IllegalArgumentException if the encoding is invalid or the key length is incorrect
     */
    public static byte[] key(String encoded) {
        byte[] key = java.util.Base64.getUrlDecoder().decode(encoded);
        if (key.length != Normal._32) {
            throw new IllegalArgumentException("Invalid SDC key length");
        }
        return key;
    }

    /**
     * Reports whether this instance has a sealing key for encrypted protection.
     *
     * @param mode protection marker
     * @return true when a sealing key is available and the requested mode is supported
     */
    @Override
    public boolean supports(String mode) {
        return sealingKeyId != null && SecretCodec.super.supports(mode);
    }

    /**
     * Seals one value with a fresh IV and authenticates it against this delivery's coordinates.
     *
     * @param plaintext value to seal
     * @return versioned envelope containing the key identifier, IV, ciphertext, and authentication tag
     * @throws CryptoException if a sealing key is unavailable or encryption fails
     */
    @Override
    public String encrypt(String plaintext) {
        if (sealingKeyId == null) {
            throw new CryptoException("Missing SDC sealing key");
        }
        if (plaintext == null) {
            return null;
        }
        byte[] clear = plaintext.getBytes(Charset.UTF_8);
        try {
            byte[] payload = AES.sealGcm(keys.get(sealingKeyId), authenticatedData, clear);
            return VERSION + Symbol.DOT + sealingKeyId + Symbol.DOT + Base64.encodeUrlSafe(payload);
        } catch (GeneralSecurityException failure) {
            throw new CryptoException("Could not seal SDC configuration", failure);
        } finally {
            Arrays.fill(clear, (byte) 0);
        }
    }

    /**
     * Opens a protected value as a string.
     *
     * @param ciphertext versioned encrypted envelope
     * @return authenticated UTF-8 plaintext
     * @throws CryptoException if the envelope cannot be authenticated
     */
    @Override
    public String decrypt(String ciphertext) {
        if (ciphertext == null) {
            return null;
        }
        try {
            byte[] clear = open(ciphertext);
            try {
                return new String(clear, Charset.UTF_8);
            } finally {
                Arrays.fill(clear, (byte) 0);
            }
        } catch (GeneralSecurityException | IllegalArgumentException failure) {
            throw new CryptoException("Could not open SDC configuration", failure);
        }
    }

    /**
     * Opens an envelope as bytes so the caller can clear the plaintext after use.
     *
     * @param value versioned encrypted envelope
     * @return authenticated UTF-8 plaintext bytes owned by the caller
     * @throws IllegalArgumentException if the key identifier or payload encoding is invalid
     * @throws GeneralSecurityException if the envelope, key, or authentication tag is invalid
     */
    public byte[] open(String value) throws GeneralSecurityException {
        if (!sealed(value)) {
            throw new GeneralSecurityException("Invalid SDC envelope");
        }
        String[] parts = value.substring(VERSION.length() + 1).split("\\.", -1);
        if (parts.length != 2) {
            throw new GeneralSecurityException("Invalid SDC envelope");
        }
        requireKeyId(parts[0]);
        byte[] key = keys.get(parts[0]);
        byte[] payload = java.util.Base64.getUrlDecoder().decode(parts[1]);
        if (key == null || key.length != Normal._32 || payload.length < AES.GCM_OVERHEAD_BYTES
                || payload.length > MAX_BYTES) {
            throw new GeneralSecurityException("Invalid SDC envelope");
        }
        return AES.openGcm(key, authenticatedData, payload);
    }

    /**
     * Encodes the delivery coordinates in their canonical order for AES-GCM authentication.
     *
     * @param app      application code
     * @param revision release identifier
     * @param code     configuration code
     * @param nonce    request nonce
     * @return UTF-8 encoded authenticated data
     * @throws IllegalArgumentException if any delivery coordinate is invalid
     */
    private static byte[] authenticatedData(String app, String revision, String code, String nonce) {
        if (!validCoordinate(app) || !validCoordinate(revision) || !validCoordinate(code) || !validNonce(nonce)) {
            throw new IllegalArgumentException("Invalid SDC envelope coordinates");
        }
        return JsonKit.toJsonString(new String[] { VERSION, app, revision, code, nonce }).getBytes(Charset.UTF_8);
    }

    /**
     * Validates the key identifier against the envelope format.
     *
     * @param keyId delivery key identifier
     * @throws IllegalArgumentException if the identifier is absent or invalid
     */
    private static void requireKeyId(String keyId) {
        if (keyId == null || !keyId.matches("[A-Za-z0-9_-]{1,64}")) {
            throw new IllegalArgumentException("Invalid SDC key ID");
        }
    }

}
