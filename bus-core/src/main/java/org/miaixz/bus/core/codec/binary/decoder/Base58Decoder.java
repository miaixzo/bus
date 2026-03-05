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
package org.miaixz.bus.core.codec.binary.decoder;

import java.util.Arrays;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base58Encoder;
import org.miaixz.bus.core.codec.binary.provider.Base58Provider;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Decodes a Base58 encoded string into a byte array.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base58Decoder implements Decoder<CharSequence, byte[]> {

    /**
     * The default Base58 decoder.
     */
    public static Base58Decoder DECODER = new Base58Decoder(Base58Encoder.DEFAULT_ALPHABET);

    /**
     * A lookup table for decoding Base58 characters.
     */
    private final byte[] lookupTable;

    /**
     * Constructs a new Base58Decoder with a custom alphabet.
     *
     * @param alphabet The alphabet to use for decoding.
     */
    public Base58Decoder(final String alphabet) {
        final byte[] lookupTable = new byte['z' + 1];
        Arrays.fill(lookupTable, (byte) -1);

        final int length = alphabet.length();
        for (int i = 0; i < length; i++) {
            lookupTable[alphabet.charAt(i)] = (byte) i;
        }
        this.lookupTable = lookupTable;
    }

    /**
     * Decodes a Base58 encoded {@link CharSequence} into a byte array.
     *
     * @param encoded The Base58 encoded data.
     * @return The decoded byte array.
     * @throws IllegalArgumentException if the input contains an invalid character.
     */
    @Override
    public byte[] decode(final CharSequence encoded) {
        if (encoded.length() == 0) {
            return new byte[0];
        }
        // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
        final byte[] input58 = new byte[encoded.length()];
        for (int i = 0; i < encoded.length(); ++i) {
            final char c = encoded.charAt(i);
            final int digit = c < 128 ? lookupTable[c] : -1;
            if (digit < 0) {
                throw new IllegalArgumentException(StringKit.format("Invalid char '{}' at [{}]", c, i));
            }
            input58[i] = (byte) digit;
        }
        // Count leading zeros.
        int zeros = 0;
        while (zeros < input58.length && input58[zeros] == 0) {
            ++zeros;
        }
        // Convert base-58 digits to base-256 digits.
        final byte[] decoded = new byte[encoded.length()];
        int outputStart = decoded.length;
        for (int inputStart = zeros; inputStart < input58.length;) {
            decoded[--outputStart] = Base58Provider.divmod(input58, inputStart, 58, 256);
            if (input58[inputStart] == 0) {
                ++inputStart; // optimization - skip leading zeros
            }
        }
        // Ignore extra leading zeroes that were added during the calculation.
        while (outputStart < decoded.length && decoded[outputStart] == 0) {
            ++outputStart;
        }
        // Return decoded data (including original number of leading zeros).
        return Arrays.copyOfRange(decoded, outputStart - zeros, decoded.length);
    }

}
