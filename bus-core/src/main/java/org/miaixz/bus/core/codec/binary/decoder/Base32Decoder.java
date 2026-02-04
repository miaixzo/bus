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
package org.miaixz.bus.core.codec.binary.decoder;

import java.util.Arrays;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.binary.encoder.Base32Encoder;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Decodes a Base32 encoded string into a byte array.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base32Decoder implements Decoder<CharSequence, byte[]> {

    /**
     * The default Base32 decoder.
     */
    public static final Base32Decoder DECODER = new Base32Decoder(Base32Encoder.DEFAULT_ALPHABET);

    /**
     * The Base32 decoder for the hexadecimal alphabet.
     */
    public static final Base32Decoder HEX_DECODER = new Base32Decoder(Base32Encoder.HEX_ALPHABET);

    /**
     * A lookup table for decoding Base32 characters.
     */
    private final byte[] lookupTable;

    /**
     * Constructs a new Base32Decoder with a custom alphabet.
     *
     * @param alphabet The alphabet to use for decoding.
     */
    public Base32Decoder(final String alphabet) {
        lookupTable = new byte[128];
        Arrays.fill(lookupTable, (byte) -1);

        final int length = alphabet.length();

        char c;
        for (int i = 0; i < length; i++) {
            c = alphabet.charAt(i);
            lookupTable[c - Symbol.C_ZERO] = (byte) i;
            // Support lowercase decoding
            if (c >= 'A' && c <= 'Z') {
                lookupTable[Character.toLowerCase(c) - Symbol.C_ZERO] = (byte) i;
            }
        }
    }

    /**
     * Decodes a Base32 encoded {@link CharSequence} into a byte array.
     *
     * @param encoded The Base32 encoded data.
     * @return The decoded byte array.
     */
    @Override
    public byte[] decode(final CharSequence encoded) {
        int i, index, lookup, offset, digit;
        final String base32 = encoded.toString();
        final int len = base32.endsWith(Symbol.EQUAL) ? base32.indexOf(Symbol.EQUAL) * 5 / 8 : base32.length() * 5 / 8;
        final byte[] bytes = new byte[len];

        for (i = 0, index = 0, offset = 0; i < base32.length(); i++) {
            lookup = base32.charAt(i) - Symbol.C_ZERO;

            /* Skip chars outside the lookup table */
            if (lookup < 0 || lookup >= lookupTable.length) {
                continue;
            }

            digit = lookupTable[lookup];

            /* If this digit is not in the table, ignore it */
            if (digit < 0) {
                continue;
            }

            if (index <= 3) {
                index = (index + 5) % 8;
                if (index == 0) {
                    bytes[offset] |= digit;
                    offset++;
                    if (offset >= bytes.length) {
                        break;
                    }
                } else {
                    bytes[offset] |= digit << (8 - index);
                }
            } else {
                index = (index + 5) % 8;
                bytes[offset] |= (digit >>> index);
                offset++;

                if (offset >= bytes.length) {
                    break;
                }
                bytes[offset] |= digit << (8 - index);
            }
        }
        return bytes;
    }

}
