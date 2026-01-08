/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.codec.binary;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import org.miaixz.bus.core.codec.binary.provider.Base58Provider;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * A utility class for Base58 encoding and decoding.
 * <p>
 * Base58 is a binary-to-text encoding scheme used in Bitcoin and other cryptocurrencies. It is similar to Base64 but
 * avoids non-alphanumeric characters and letters that might look ambiguous when printed (0, O, I, l).
 *
 * <p>
 * This implementation includes support for Base58Check, which adds a checksum to the data to prevent transcription
 * errors. For the specification, see <a href="https://en.bitcoin.it/wiki/Base58Check_encoding">Base58Check
 * encoding</a>. The core encoding/decoding logic is based on the implementation from
 * <a href="https://github.com/Anujraval24/Base58Encoding">https://github.com/Anujraval24/Base58Encoding</a>.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base58 {

    /**
     * Constructs a new Base58. Utility class constructor for static access.
     */
    private Base58() {
    }

    /**
     * The size of the checksum in bytes.
     */
    private static final int CHECKSUM_SIZE = 4;

    /**
     * Encodes the given data using Base58Check, including a version byte and a checksum.
     *
     * @param version The version byte. If {@code null}, no version byte is prepended.
     * @param data    The data to be encoded.
     * @return The Base58Check-encoded string.
     */
    public static String encodeChecked(final Integer version, final byte[] data) {
        return encode(addChecksum(version, data));
    }

    /**
     * Encodes the given data into a Base58 string without a checksum.
     *
     * @param data The data to be encoded.
     * @return The Base58-encoded string.
     */
    public static String encode(final byte[] data) {
        return Base58Provider.INSTANCE.encode(data);
    }

    /**
     * Decodes a Base58Check-encoded string, verifies the checksum, and removes the version byte. It automatically
     * detects whether a version byte is present.
     *
     * @param encoded The Base58Check-encoded string to decode.
     * @return The decoded byte array (payload).
     * @throws ValidateException If the checksum is invalid.
     */
    public static byte[] decodeChecked(final CharSequence encoded) throws ValidateException {
        try {
            // Try decoding with a version byte first.
            return decodeChecked(encoded, true);
        } catch (final ValidateException ignore) {
            // If that fails, try decoding without a version byte.
            return decodeChecked(encoded, false);
        }
    }

    /**
     * Decodes a Base58Check-encoded string, verifies the checksum, and optionally removes the version byte.
     *
     * @param encoded     The Base58Check-encoded string to decode.
     * @param withVersion {@code true} if the encoded data includes a version byte.
     * @return The decoded byte array (payload).
     * @throws ValidateException If the checksum is invalid.
     */
    public static byte[] decodeChecked(final CharSequence encoded, final boolean withVersion) throws ValidateException {
        final byte[] valueWithChecksum = decode(encoded);
        return verifyAndRemoveChecksum(valueWithChecksum, withVersion);
    }

    /**
     * Decodes a standard Base58-encoded string into a byte array.
     *
     * @param encoded The Base58-encoded string.
     * @return The decoded byte array.
     */
    public static byte[] decode(final CharSequence encoded) {
        return Base58Provider.INSTANCE.decode(encoded);
    }

    /**
     * Verifies the checksum and removes both the checksum and the version byte from the data.
     *
     * @param data        The encoded data including checksum and optional version byte.
     * @param withVersion {@code true} if a version byte is present.
     * @return The original payload data.
     * @throws ValidateException if the checksum does not match.
     */
    private static byte[] verifyAndRemoveChecksum(final byte[] data, final boolean withVersion) {
        final int payloadStart = withVersion ? 1 : 0;
        final byte[] payload = Arrays.copyOfRange(data, payloadStart, data.length - CHECKSUM_SIZE);
        final byte[] checksum = Arrays.copyOfRange(data, data.length - CHECKSUM_SIZE, data.length);
        final byte[] expectedChecksum = checksum(payload);
        if (!Arrays.equals(checksum, expectedChecksum)) {
            throw new ValidateException("Base58 check is invalid");
        }
        return payload;
    }

    /**
     * Prepends a version byte (if provided) and appends a checksum to the payload.
     *
     * @param version The version byte. If {@code null}, no version byte is added.
     * @param payload The data to which the checksum will be appended.
     * @return The data with version and checksum.
     */
    private static byte[] addChecksum(final Integer version, final byte[] payload) {
        final byte[] addressBytes;
        int payloadOffset = 0;
        if (null != version) {
            addressBytes = new byte[1 + payload.length + CHECKSUM_SIZE];
            addressBytes[0] = (byte) version.intValue();
            payloadOffset = 1;
        } else {
            addressBytes = new byte[payload.length + CHECKSUM_SIZE];
        }
        System.arraycopy(payload, 0, addressBytes, payloadOffset, payload.length);
        final byte[] checksum = checksum(payload);
        System.arraycopy(checksum, 0, addressBytes, addressBytes.length - CHECKSUM_SIZE, CHECKSUM_SIZE);
        return addressBytes;
    }

    /**
     * Calculates the checksum for the given data. The checksum is the first {@link #CHECKSUM_SIZE} bytes of a double
     * SHA-256 hash of the data.
     *
     * @param data The data to checksum.
     * @return The checksum.
     */
    private static byte[] checksum(final byte[] data) {
        // The checksum is the first 4 bytes of the double-SHA-256 hash.
        final byte[] hash = hash256(hash256(data));
        return Arrays.copyOfRange(hash, 0, CHECKSUM_SIZE);
    }

    /**
     * Calculates the SHA-256 hash of the given data.
     *
     * @param data The data to hash.
     * @return The SHA-256 hash.
     * @throws InternalException if the SHA-256 algorithm is not available.
     */
    private static byte[] hash256(final byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (final NoSuchAlgorithmException e) {
            // Should not happen, SHA-256 is a standard algorithm.
            throw new InternalException(e);
        }
    }

}
