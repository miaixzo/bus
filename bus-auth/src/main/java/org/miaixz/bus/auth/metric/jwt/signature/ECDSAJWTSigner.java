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
package org.miaixz.bus.auth.metric.jwt.signature;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECKey;

import org.miaixz.bus.core.basic.normal.ErrorCode;
import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.exception.JWTException;

/**
 * ES256 JWT signer with strict conversion between ASN.1 DER and the fixed JWS {@code R || S} representation.
 * <p>
 * This compatibility class reuses {@link RSAJWTSigner} only for its Bus {@code Sign} execution path. It accepts the
 * P-256 key size and the {@link Algorithm#SHA256WITHECDSA} algorithm exclusively.
 * </p>
 *
 * @author Kimi Liu
 * @see RSAJWTSigner
 */
public class ECDSAJWTSigner extends RSAJWTSigner {

    /**
     * DER sequence tag.
     */
    private static final int DER_SEQUENCE = 0x30;

    /**
     * DER integer tag.
     */
    private static final int DER_INTEGER = 0x02;

    /**
     * Byte length of one P-256 coordinate.
     */
    private static final int COORDINATE_LENGTH = 32;

    /**
     * Byte length of an ES256 JWS signature.
     */
    private static final int SIGNATURE_LENGTH = COORDINATE_LENGTH * 2;

    /**
     * Bit length required for a P-256 key.
     */
    private static final int CURVE_BITS = 256;

    /**
     * Creates an ES256 signer with one EC key.
     *
     * @param algorithm exact SHA256withECDSA JCA algorithm name
     * @param key       EC public verification key or EC private signing key
     */
    public ECDSAJWTSigner(final String algorithm, final Key key) {
        super(requireAlgorithm(algorithm), requireKey(key), true);
    }

    /**
     * Creates an ES256 signer with an EC key pair.
     *
     * @param algorithm exact SHA256withECDSA JCA algorithm name
     * @param keyPair   EC key pair containing at least one usable key
     */
    public ECDSAJWTSigner(final String algorithm, final KeyPair keyPair) {
        super(requireAlgorithm(algorithm), requireKeyPair(keyPair), true);
    }

    /**
     * Converts a strictly encoded DER ECDSA signature to the fixed ES256 representation.
     *
     * @param derSignature DER signature
     * @return 64-byte JWS signature
     */
    private static byte[] derToConcat(final byte[] derSignature) {
        if (derSignature == null || derSignature.length < 8 || derSignature.length > 72
                || unsigned(derSignature[0]) != DER_SEQUENCE || unsigned(derSignature[1]) != derSignature.length - 2) {
            reject();
        }
        final byte[] result = new byte[SIGNATURE_LENGTH];
        int offset = readInteger(derSignature, 2, result, 0);
        offset = readInteger(derSignature, offset, result, COORDINATE_LENGTH);
        if (offset != derSignature.length) {
            reject();
        }
        return result;
    }

    /**
     * Reads one minimally encoded positive DER integer into a fixed-width coordinate.
     *
     * @param source       complete DER signature
     * @param offset       integer tag offset
     * @param target       fixed JWS signature target
     * @param targetOffset coordinate target offset
     * @return offset immediately after the integer
     */
    private static int readInteger(final byte[] source, final int offset, final byte[] target, final int targetOffset) {
        if (offset < 0 || offset + 2 > source.length || unsigned(source[offset]) != DER_INTEGER) {
            reject();
        }
        final int encodedLength = unsigned(source[offset + 1]);
        final int valueOffset = offset + 2;
        final int end = valueOffset + encodedLength;
        if (encodedLength < 1 || encodedLength > COORDINATE_LENGTH + 1 || end > source.length
                || (source[valueOffset] & 0x80) != 0) {
            reject();
        }
        int significantOffset = valueOffset;
        int significantLength = encodedLength;
        if (encodedLength > 1 && source[valueOffset] == 0) {
            if ((source[valueOffset + 1] & 0x80) == 0) {
                reject();
            }
            significantOffset++;
            significantLength--;
        }
        if (significantLength > COORDINATE_LENGTH) {
            reject();
        }
        System.arraycopy(
                source,
                significantOffset,
                target,
                targetOffset + COORDINATE_LENGTH - significantLength,
                significantLength);
        return end;
    }

    /**
     * Converts a fixed ES256 JWS signature to minimal DER encoding.
     *
     * @param jwsSignature 64-byte JWS signature
     * @return DER signature
     */
    private static byte[] concatToDer(final byte[] jwsSignature) {
        if (jwsSignature == null || jwsSignature.length != SIGNATURE_LENGTH) {
            reject();
        }
        final int rOffset = significantOffset(jwsSignature, 0);
        final int sOffset = significantOffset(jwsSignature, COORDINATE_LENGTH);
        final int rLength = encodedIntegerLength(jwsSignature, rOffset, COORDINATE_LENGTH);
        final int sLength = encodedIntegerLength(jwsSignature, sOffset, SIGNATURE_LENGTH);
        final int contentLength = 2 + rLength + 2 + sLength;
        final byte[] result = new byte[2 + contentLength];
        result[0] = (byte) DER_SEQUENCE;
        result[1] = (byte) contentLength;
        int offset = writeInteger(jwsSignature, rOffset, COORDINATE_LENGTH, result, 2, rLength);
        writeInteger(jwsSignature, sOffset, SIGNATURE_LENGTH, result, offset, sLength);
        return result;
    }

    /**
     * Locates the first significant byte while retaining one byte for zero.
     *
     * @param source fixed JWS signature
     * @param start  coordinate start offset
     * @return first significant byte offset
     */
    private static int significantOffset(final byte[] source, final int start) {
        final int end = start + COORDINATE_LENGTH;
        int offset = start;
        while (offset < end - 1 && source[offset] == 0) {
            offset++;
        }
        return offset;
    }

    /**
     * Calculates the minimal positive DER integer length.
     *
     * @param source fixed JWS signature
     * @param offset first significant byte offset
     * @param end    coordinate end offset
     * @return DER integer value length
     */
    private static int encodedIntegerLength(final byte[] source, final int offset, final int end) {
        return end - offset + ((source[offset] & 0x80) == 0 ? 0 : 1);
    }

    /**
     * Writes one fixed-width coordinate as a minimal positive DER integer.
     *
     * @param source        fixed JWS signature
     * @param sourceOffset  first significant byte offset
     * @param sourceEnd     coordinate end offset
     * @param target        DER target
     * @param targetOffset  integer tag offset
     * @param encodedLength DER integer value length
     * @return offset immediately after the integer
     */
    private static int writeInteger(
            final byte[] source,
            final int sourceOffset,
            final int sourceEnd,
            final byte[] target,
            final int targetOffset,
            final int encodedLength) {
        target[targetOffset] = (byte) DER_INTEGER;
        target[targetOffset + 1] = (byte) encodedLength;
        final int sourceLength = sourceEnd - sourceOffset;
        final int valueOffset = targetOffset + 2;
        System.arraycopy(source, sourceOffset, target, valueOffset + encodedLength - sourceLength, sourceLength);
        return valueOffset + encodedLength;
    }

    /**
     * Returns one byte as an unsigned integer.
     *
     * @param value byte value
     * @return unsigned value
     */
    private static int unsigned(final byte value) {
        return value & 0xff;
    }

    /**
     * Validates the exact ES256 JCA algorithm name.
     *
     * @param algorithm supplied algorithm name
     * @return validated algorithm name
     */
    private static String requireAlgorithm(final String algorithm) {
        if (!Algorithm.SHA256WITHECDSA.getValue().equals(algorithm)) {
            throw new IllegalArgumentException("Only ES256 is supported");
        }
        return algorithm;
    }

    /**
     * Validates one P-256 public or private key.
     *
     * @param key supplied key
     * @return validated key
     */
    private static Key requireKey(final Key key) {
        if (!(key instanceof ECKey) || !(key instanceof PublicKey || key instanceof PrivateKey)) {
            throw new IllegalArgumentException("A P-256 public or private key is required");
        }
        requireCurve((ECKey) key);
        return key;
    }

    /**
     * Validates a P-256 key pair containing at least one key.
     *
     * @param keyPair supplied key pair
     * @return validated key pair
     */
    private static KeyPair requireKeyPair(final KeyPair keyPair) {
        if (keyPair == null || keyPair.getPublic() == null && keyPair.getPrivate() == null) {
            throw new IllegalArgumentException("Signer key pair must contain a key");
        }
        if (keyPair.getPublic() != null) {
            requireKey(keyPair.getPublic());
        }
        if (keyPair.getPrivate() != null) {
            requireKey(keyPair.getPrivate());
        }
        return keyPair;
    }

    /**
     * Validates the finite-field size required by P-256.
     *
     * @param key EC key
     */
    private static void requireCurve(final ECKey key) {
        if (key.getParams() == null || key.getParams().getCurve().getField().getFieldSize() != CURVE_BITS
                || key.getParams().getOrder().bitLength() != CURVE_BITS) {
            throw new IllegalArgumentException("A P-256 key is required");
        }
    }

    /**
     * Rejects malformed signature encoding with the shared token-signature error.
     */
    private static void reject() {
        throw new JWTException(ErrorCode._100532);
    }

    /**
     * Signs bytes and converts the provider DER signature to the fixed JWS representation.
     *
     * @param data signing input
     * @return 64-byte JWS signature
     */
    @Override
    protected byte[] sign(final byte[] data) {
        return derToConcat(super.sign(data));
    }

    /**
     * Converts a fixed JWS representation to DER and verifies it.
     *
     * @param data   verification input
     * @param signed 64-byte JWS signature
     * @return {@code true} when the signature is valid
     */
    @Override
    protected boolean verify(final byte[] data, final byte[] signed) {
        return super.verify(data, concatToDer(signed));
    }

}
