/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.core.codec.binary.provider;

import java.io.Serial;
import java.io.Serializable;

import org.miaixz.bus.core.codec.Decoder;
import org.miaixz.bus.core.codec.Encoder;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Base16 (Hex) encoder and decoder. Hexadecimal is a base-16 numeral system, using 16 symbols: 0-9 and A-F. For
 * example, the decimal number 57 is represented as 111001 in binary and 39 in hexadecimal.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base16Provider implements Encoder<byte[], char[]>, Decoder<CharSequence, byte[]>, Serializable {

    @Serial
    private static final long serialVersionUID = 2852258525806L;

    /**
     * The lowercase Base16 codec.
     */
    public static final Base16Provider CODEC_LOWER = new Base16Provider(true);
    /**
     * The uppercase Base16 codec.
     */
    public static final Base16Provider CODEC_UPPER = new Base16Provider(false);
    /**
     * The character alphabet used for encoding.
     */
    private final char[] alphabets;

    /**
     * Constructs a new Base16Provider.
     *
     * @param lowerCase {@code true} for lowercase output, {@code false} for uppercase.
     */
    public Base16Provider(final boolean lowerCase) {
        this.alphabets = (lowerCase ? "0123456789abcdef" : "0123456789ABCDEF").toCharArray();
    }

    /**
     * Converts a hexadecimal character to an integer.
     *
     * @param ch    The hexadecimal character.
     * @param index The index of the character in the input sequence.
     * @return The integer value.
     * @throws InternalException if the character is not a valid hexadecimal digit.
     */
    private static int toDigit(final char ch, final int index) {
        final int digit = Character.digit(ch, 16);
        if (digit < 0) {
            throw new InternalException("Illegal hexadecimal character {} at index {}", ch, index);
        }
        return digit;
    }

    /**
     * Encodes a byte array into a hexadecimal character array.
     *
     * @param data The byte array to encode.
     * @return The resulting hexadecimal character array.
     */
    @Override
    public char[] encode(final byte[] data) {
        final int len = data.length;
        final char[] out = new char[len << 1]; // len * 2
        // Two characters for each byte
        for (int i = 0, j = 0; i < len; i++) {
            // High nibble
            out[j++] = hexDigit(data[i] >> 4);
            // Low nibble
            out[j++] = hexDigit(data[i]);
        }
        return out;
    }

    /**
     * Decodes a hexadecimal character sequence into a byte array.
     *
     * @param encoded The hexadecimal character sequence to decode.
     * @return The decoded byte array, or {@code null} if the input is empty.
     */
    @Override
    public byte[] decode(CharSequence encoded) {
        if (StringKit.isEmpty(encoded)) {
            return null;
        }

        encoded = StringKit.cleanBlank(encoded);
        int len = encoded.length();

        if ((len & 0x01) != 0) {
            // If the input has an odd length, pad with a leading zero.
            encoded = "0" + encoded;
            len = encoded.length();
        }

        final byte[] out = new byte[len >> 1];

        // Two characters for each byte
        for (int i = 0, j = 0; j < len; i++) {
            int f = toDigit(encoded.charAt(j), j) << 4;
            j++;
            f = f | toDigit(encoded.charAt(j), j);
            j++;
            out[i] = (byte) (f & 0xFF);
        }

        return out;
    }

    /**
     * Converts a character to its Unicode string representation (e.g., {@code \u4f60}). The resulting string is padded
     * with leading zeros if necessary to ensure a 4-digit hex value.
     * 
     * <pre>
     * toUnicodeHex('你') = "\\u4f60"
     * </pre>
     *
     * @param ch The character to convert.
     * @return The Unicode string representation.
     */
    public String toUnicodeHex(final char ch) {
        return "\\u" + hexDigit(ch >> 12) + hexDigit(ch >> 8) + hexDigit(ch >> 4) + hexDigit(ch);
    }

    /**
     * Appends the hexadecimal representation of a byte to a {@link StringBuilder}.
     *
     * @param builder The {@link StringBuilder} to append to.
     * @param b       The byte to convert.
     */
    public void appendHex(final StringBuilder builder, final byte b) {
        // High nibble
        builder.append(hexDigit(b >> 4));
        // Low nibble
        builder.append(hexDigit(b));
    }

    /**
     * Converts a 4-bit value (nibble) to its hexadecimal character representation.
     *
     * @param b The integer value (only the lower 4 bits are used).
     * @return The corresponding hexadecimal character.
     */
    public char hexDigit(final int b) {
        return alphabets[b & 0x0f];
    }

}
