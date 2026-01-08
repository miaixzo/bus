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
package org.miaixz.bus.core.codec.binary.encoder;

import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.Symbol;

/**
 * Encodes a byte array into a Base32 string.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base32Encoder implements Encoder<byte[], String> {

    /**
     * The default Base32 alphabet.
     */
    public static final String DEFAULT_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    /**
     * The Base32 alphabet used in hexadecimal contexts.
     */
    public static final String HEX_ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUV";

    /**
     * The default Base32 encoder.
     */
    public static final Base32Encoder ENCODER = new Base32Encoder(DEFAULT_ALPHABET, Symbol.C_EQUAL);

    /**
     * The Base32 encoder for the hexadecimal alphabet.
     */
    public static final Base32Encoder HEX_ENCODER = new Base32Encoder(HEX_ALPHABET, Symbol.C_EQUAL);

    /**
     * Padding values for Base32 encoding.
     */
    private static final int[] BASE32_FILL = { -1, 4, 1, 6, 3 };

    /**
     * The alphabet used for encoding.
     */
    private final char[] alphabet;

    /**
     * The character used for padding.
     */
    private final Character pad;

    /**
     * Constructs a new Base32Encoder with a custom alphabet and padding character.
     *
     * @param alphabet The alphabet to use for encoding (e.g., {@link #DEFAULT_ALPHABET} or {@link #HEX_ALPHABET}).
     * @param pad      The character to use for padding.
     */
    public Base32Encoder(final String alphabet, final Character pad) {
        this.alphabet = alphabet.toCharArray();
        this.pad = pad;
    }

    /**
     * Encodes a byte array into a Base32 string.
     *
     * @param data The byte array to encode.
     * @return The Base32 encoded string.
     */
    @Override
    public String encode(final byte[] data) {
        int i = 0;
        int index = 0;
        int digit;
        int currByte;
        int nextByte;

        int encodeLen = data.length * 8 / 5;
        if (encodeLen != 0) {
            encodeLen = encodeLen + 1 + BASE32_FILL[(data.length * 8) % 5];
        }

        final StringBuilder base32 = new StringBuilder(encodeLen);

        while (i < data.length) {
            // unsign
            currByte = (data[i] >= 0) ? data[i] : (data[i] + 256);

            /* Is the current digit going to span a byte boundary? */
            if (index > 3) {
                if ((i + 1) < data.length) {
                    nextByte = (data[i + 1] >= 0) ? data[i + 1] : (data[i + 1] + 256);
                } else {
                    nextByte = 0;
                }

                digit = currByte & (0xFF >> index);
                index = (index + 5) % 8;
                digit <<= index;
                digit |= nextByte >> (8 - index);
                i++;
            } else {
                digit = (currByte >> (8 - (index + 5))) & 0x1F;
                index = (index + 5) % 8;
                if (index == 0) {
                    i++;
                }
            }
            base32.append(alphabet[digit]);
        }

        if (null != pad) {
            // Pad the output to the correct length
            while (base32.length() < encodeLen) {
                base32.append(pad.charValue());
            }
        }

        return base32.toString();
    }

}
