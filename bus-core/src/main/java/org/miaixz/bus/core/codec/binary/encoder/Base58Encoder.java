/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.core.codec.binary.encoder;

import java.util.Arrays;

import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.codec.binary.provider.Base58Provider;
import org.miaixz.bus.core.lang.Normal;

/**
 * Encodes a byte array into a Base58 string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base58Encoder implements Encoder<byte[], String> {

    /**
     * The default Base58 alphabet used by Bitcoin.
     */
    public static final String DEFAULT_ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";

    /**
     * The default Base58 encoder.
     */
    public static final Base58Encoder ENCODER = new Base58Encoder(DEFAULT_ALPHABET.toCharArray());

    /**
     * The alphabet used for encoding.
     */
    private final char[] alphabet;

    /**
     * The character in the alphabet that represents zero.
     */
    private final char alphabetZero;

    /**
     * Constructs a new Base58Encoder with a custom alphabet.
     *
     * @param alphabet The alphabet to use for encoding.
     */
    public Base58Encoder(final char[] alphabet) {
        this.alphabet = alphabet;
        this.alphabetZero = alphabet[0];
    }

    /**
     * Encodes a byte array into a Base58 string.
     *
     * @param data The byte array to encode.
     * @return The Base58 encoded string, or {@code null} if the input is null.
     */
    @Override
    public String encode(byte[] data) {
        if (null == data) {
            return null;
        }
        if (data.length == 0) {
            return Normal.EMPTY;
        }
        // Count leading zeros.
        int zeroCount = 0;
        while (zeroCount < data.length && data[zeroCount] == 0) {
            ++zeroCount;
        }
        // Convert base-256 digits to base-58 digits.
        data = Arrays.copyOf(data, data.length);
        final char[] encoded = new char[data.length * 2];
        int outputStart = encoded.length;
        for (int inputStart = zeroCount; inputStart < data.length;) {
            encoded[--outputStart] = alphabet[Base58Provider.divmod(data, inputStart, 256, 58)];
            if (data[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        // Preserve exactly as many leading encoded zeros in output as there were leading zeros in input.
        while (outputStart < encoded.length && encoded[outputStart] == alphabetZero) {
            ++outputStart;
        }
        while (--zeroCount >= 0) {
            encoded[--outputStart] = alphabetZero;
        }
        // Return encoded string (including encoded leading zeros).
        return new String(encoded, outputStart, encoded.length - outputStart);
    }

}
