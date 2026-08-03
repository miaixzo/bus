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
package org.miaixz.bus.fabric.network.dns.server;

import java.util.Arrays;
import java.util.Base64;

import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.dns.message.DnsName;

/**
 * Immutable TSIG shared-secret key used to authenticate DNS messages.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class DnsTsigKey {

    /**
     * DNS algorithm name for HMAC-MD5 TSIG.
     */
    public static final String HMAC_MD5 = "hmac-md5.sig-alg.reg.int.";

    /**
     * DNS algorithm name for HMAC-SHA1 TSIG.
     */
    public static final String HMAC_SHA1 = "hmac-sha1.";

    /**
     * DNS algorithm name for HMAC-SHA224 TSIG.
     */
    public static final String HMAC_SHA224 = "hmac-sha224.";

    /**
     * DNS algorithm name for HMAC-SHA256 TSIG.
     */
    public static final String HMAC_SHA256 = "hmac-sha256.";

    /**
     * DNS algorithm name for HMAC-SHA384 TSIG.
     */
    public static final String HMAC_SHA384 = "hmac-sha384.";

    /**
     * DNS algorithm name for HMAC-SHA512 TSIG.
     */
    public static final String HMAC_SHA512 = "hmac-sha512.";

    /**
     * DNS algorithm name for truncated HMAC-SHA256 TSIG.
     */
    public static final String HMAC_SHA256_128 = "hmac-sha256-128.";

    /**
     * DNS algorithm name for truncated HMAC-SHA384 TSIG.
     */
    public static final String HMAC_SHA384_192 = "hmac-sha384-192.";

    /**
     * DNS algorithm name for truncated HMAC-SHA512 TSIG.
     */
    public static final String HMAC_SHA512_256 = "hmac-sha512-256.";

    /**
     * Canonical key owner name.
     */
    private final String name;

    /**
     * Canonical TSIG algorithm DNS name.
     */
    private final String algorithmName;

    /**
     * Java Cryptography Architecture MAC algorithm name.
     */
    private final String macAlgorithm;

    /**
     * Maximum MAC bytes carried on the wire for this TSIG algorithm.
     */
    private final int macLengthBytes;

    /**
     * Raw shared secret bytes.
     */
    private final byte[] secret;

    /**
     * Creates a TSIG key.
     *
     * @param name           key owner name
     * @param algorithmName  TSIG algorithm DNS name
     * @param macAlgorithm   Java Cryptography Architecture MAC algorithm name
     * @param macLengthBytes maximum MAC bytes carried on the wire
     * @param secret         raw shared secret bytes
     */
    private DnsTsigKey(final String name, final String algorithmName, final String macAlgorithm,
            final int macLengthBytes, final byte[] secret) {
        this.name = DnsName.normalize(name);
        this.algorithmName = DnsName.normalize(algorithmName);
        this.macAlgorithm = validateMacAlgorithm(macAlgorithm);
        this.macLengthBytes = validateMacLength(macLengthBytes);
        this.secret = copySecret(secret);
    }

    /**
     * Creates a TSIG key from a DNS algorithm name and raw shared secret bytes.
     *
     * @param name          key owner name
     * @param algorithmName TSIG algorithm DNS name
     * @param secret        raw shared secret bytes
     * @return immutable TSIG key
     */
    public static DnsTsigKey of(final String name, final String algorithmName, final byte[] secret) {
        final Algorithm algorithm = algorithm(algorithmName);
        return new DnsTsigKey(name, algorithm.name, algorithm.macAlgorithm, algorithm.macLengthBytes, secret);
    }

    /**
     * Creates a TSIG key from a DNS algorithm name and base64 shared secret.
     *
     * @param name          key owner name
     * @param algorithmName TSIG algorithm DNS name
     * @param base64Secret  base64-encoded shared secret
     * @return immutable TSIG key
     */
    public static DnsTsigKey ofBase64(final String name, final String algorithmName, final String base64Secret) {
        if (base64Secret == null || base64Secret.isBlank()) {
            throw new ValidateException("DNS TSIG key secret must not be blank");
        }
        return of(name, algorithmName, Base64.getDecoder().decode(base64Secret));
    }

    /**
     * Creates an HMAC-SHA256 TSIG key from raw shared secret bytes.
     *
     * @param name   key owner name
     * @param secret raw shared secret bytes
     * @return immutable TSIG key
     */
    public static DnsTsigKey hmacSha256(final String name, final byte[] secret) {
        return of(name, HMAC_SHA256, secret);
    }

    /**
     * Creates an HMAC-SHA256 TSIG key from a base64 shared secret.
     *
     * @param name         key owner name
     * @param base64Secret base64-encoded shared secret
     * @return immutable TSIG key
     */
    public static DnsTsigKey hmacSha256Base64(final String name, final String base64Secret) {
        return ofBase64(name, HMAC_SHA256, base64Secret);
    }

    /**
     * Returns the canonical key owner name.
     *
     * @return key owner name ending with a dot
     */
    public String name() {
        return name;
    }

    /**
     * Returns the canonical TSIG algorithm DNS name.
     *
     * @return TSIG algorithm name ending with a dot
     */
    public String algorithmName() {
        return algorithmName;
    }

    /**
     * Returns the Java Cryptography Architecture MAC algorithm name.
     *
     * @return JCA MAC algorithm name
     */
    public String macAlgorithm() {
        return macAlgorithm;
    }

    /**
     * Returns the maximum MAC length carried by this TSIG algorithm.
     *
     * @return maximum MAC bytes
     */
    public int macLengthBytes() {
        return macLengthBytes;
    }

    /**
     * Returns a copy of the raw shared secret bytes.
     *
     * @return shared secret copy
     */
    public byte[] secret() {
        return Arrays.copyOf(secret, secret.length);
    }

    /**
     * Returns whether this key matches a parsed TSIG owner and algorithm.
     *
     * @param candidateName      parsed TSIG owner name
     * @param candidateAlgorithm parsed TSIG algorithm name
     * @return true when both names match canonically
     */
    public boolean matches(final String candidateName, final String candidateAlgorithm) {
        return name.equals(DnsName.normalize(candidateName))
                && algorithmName.equals(DnsName.normalize(candidateAlgorithm));
    }

    /**
     * Resolves metadata for a TSIG algorithm DNS name.
     *
     * @param algorithmName TSIG algorithm DNS name
     * @return algorithm metadata
     */
    private static Algorithm algorithm(final String algorithmName) {
        final String normalized = DnsName.normalize(algorithmName);
        return switch (normalized) {
            case HMAC_MD5 -> new Algorithm(HMAC_MD5, "HmacMD5", 16);
            case HMAC_SHA1 -> new Algorithm(HMAC_SHA1, "HmacSHA1", 20);
            case HMAC_SHA224 -> new Algorithm(HMAC_SHA224, "HmacSHA224", 28);
            case HMAC_SHA256 -> new Algorithm(HMAC_SHA256, "HmacSHA256", 32);
            case HMAC_SHA384 -> new Algorithm(HMAC_SHA384, "HmacSHA384", 48);
            case HMAC_SHA512 -> new Algorithm(HMAC_SHA512, "HmacSHA512", 64);
            case HMAC_SHA256_128 -> new Algorithm(HMAC_SHA256_128, "HmacSHA256", 16);
            case HMAC_SHA384_192 -> new Algorithm(HMAC_SHA384_192, "HmacSHA384", 24);
            case HMAC_SHA512_256 -> new Algorithm(HMAC_SHA512_256, "HmacSHA512", 32);
            default -> throw new ValidateException("DNS TSIG algorithm is unsupported: " + normalized);
        };
    }

    /**
     * Validates a JCA MAC algorithm name.
     *
     * @param value candidate MAC algorithm name
     * @return validated MAC algorithm name
     */
    private static String validateMacAlgorithm(final String value) {
        if (value == null || value.isBlank()) {
            throw new ValidateException("DNS TSIG MAC algorithm must not be blank");
        }
        return value;
    }

    /**
     * Validates a MAC length.
     *
     * @param value candidate MAC length
     * @return validated MAC length
     */
    private static int validateMacLength(final int value) {
        if (value <= 0) {
            throw new ValidateException("DNS TSIG MAC length must be positive");
        }
        return value;
    }

    /**
     * Copies and validates shared secret bytes.
     *
     * @param value candidate shared secret bytes
     * @return shared secret copy
     */
    private static byte[] copySecret(final byte[] value) {
        if (value == null || value.length == 0) {
            throw new ValidateException("DNS TSIG key secret must not be empty");
        }
        return Arrays.copyOf(value, value.length);
    }

    /**
     * Algorithm metadata used while creating keys.
     *
     * @param name           canonical TSIG algorithm DNS name
     * @param macAlgorithm   Java Cryptography Architecture MAC algorithm name
     * @param macLengthBytes maximum MAC bytes carried on the wire
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private record Algorithm(String name, String macAlgorithm, int macLengthBytes) {
    }

}
