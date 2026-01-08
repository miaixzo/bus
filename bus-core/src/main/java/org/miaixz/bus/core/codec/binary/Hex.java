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

import org.miaixz.bus.core.codec.binary.provider.Base16Provider;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Provides hexadecimal (Base16) encoding and decoding.
 * <p>
 * Hexadecimal (abbreviated as hex or base 16) is a positional numeral system with a radix, or base, of 16. It uses
 * sixteen distinct symbols, most often the symbols "0"–"9" to represent values zero to nine, and "A"–"F" (or
 * alternatively "a"–"f") to represent values ten to fifteen. For example, the decimal number 57 is written as 111001 in
 * binary and 39 in hexadecimal. Languages like Java and C use the prefix "0x" to distinguish hexadecimal from decimal
 * numbers, for example, 0x20 is decimal 32, not decimal 20.
 *
 * @author Kimi Liu
 * @see Base16Provider
 * @since Java 17+
 */
public class Hex {

    /**
     * Constructs a new Hex. Utility class constructor for static access.
     */
    public Hex() {
    }

    /**
     * Encodes a byte array into a hexadecimal character array (lowercase).
     *
     * @param data The byte array to encode.
     * @return A hexadecimal character array.
     */
    public static char[] encode(final byte[] data) {
        return encode(data, true);
    }

    /**
     * Encodes a string into a hexadecimal character array (lowercase).
     *
     * @param text    The string to encode.
     * @param charset The character set to use.
     * @return A hexadecimal character array.
     */
    public static char[] encode(final String text, final java.nio.charset.Charset charset) {
        return encode(ByteKit.toBytes(text, charset), true);
    }

    /**
     * Encodes a byte array into a hexadecimal character array.
     *
     * @param data        The byte array to encode.
     * @param toLowerCase {@code true} to convert to lowercase, {@code false} to convert to uppercase.
     * @return A hexadecimal character array. Returns {@code null} if the input data is {@code null}.
     */
    public static char[] encode(final byte[] data, final boolean toLowerCase) {
        if (null == data) {
            return null;
        }
        return (toLowerCase ? Base16Provider.CODEC_LOWER : Base16Provider.CODEC_UPPER).encode(data);
    }

    /**
     * Encodes a byte array into a hexadecimal string (lowercase).
     *
     * @param data The byte array to encode.
     * @return A hexadecimal string.
     */
    public static String encodeString(final byte[] data) {
        return encodeString(data, true);
    }

    /**
     * Encodes a byte array into a hexadecimal string.
     *
     * @param data        The byte array to encode.
     * @param toLowerCase {@code true} to convert to lowercase, {@code false} to convert to uppercase.
     * @return A hexadecimal string.
     */
    public static String encodeString(final byte[] data, final boolean toLowerCase) {
        return StringKit.toString(encode(data, toLowerCase), Charset.UTF_8);
    }

    /**
     * Encodes a string into a lowercase hexadecimal string using the default UTF-8 charset.
     *
     * @param data The string to encode.
     * @return A hexadecimal string.
     */
    public static String encodeString(final CharSequence data) {
        return encodeString(data, Charset.UTF_8);
    }

    /**
     * Encodes a string into a lowercase hexadecimal string.
     *
     * @param data    The string to encode.
     * @param charset The character set to use.
     * @return A hexadecimal string.
     */
    public static String encodeString(final CharSequence data, final java.nio.charset.Charset charset) {
        return encodeString(ByteKit.toBytes(data, charset), true);
    }

    /**
     * Decodes a hexadecimal character sequence into a byte array. The decoder is case-insensitive.
     *
     * @param data The hexadecimal string to decode.
     * @return A byte array.
     * @throws InternalException if the source hexadecimal character sequence has an odd length.
     */
    public static byte[] decode(final CharSequence data) {
        return Base16Provider.CODEC_LOWER.decode(data);
    }

    /**
     * Decodes a hexadecimal character array into a byte array. The decoder is case-insensitive.
     *
     * @param data The hexadecimal character array to decode.
     * @return A byte array.
     * @throws InternalException if the source hexadecimal character array has an odd length.
     */
    public static byte[] decode(final char[] data) {
        return decode(String.valueOf(data));
    }

    /**
     * Decodes a hexadecimal string into a string using the default UTF-8 charset.
     *
     * @param data The hexadecimal string to decode.
     * @return The decoded string.
     * @throws InternalException if the source hexadecimal character sequence has an odd length.
     */
    public static String decodeString(final CharSequence data) {
        return decodeString(data, Charset.UTF_8);
    }

    /**
     * Decodes a hexadecimal string into a string.
     *
     * @param data    The hexadecimal string to decode.
     * @param charset The character set to use.
     * @return The decoded string.
     * @throws InternalException if the source hexadecimal character sequence has an odd length.
     */
    public static String decodeString(final CharSequence data, final java.nio.charset.Charset charset) {
        if (StringKit.isEmpty(data)) {
            return StringKit.toStringOrNull(data);
        }
        return StringKit.toString(decode(data), charset);
    }

    /**
     * Decodes a hexadecimal character array into a string.
     *
     * @param data    The hexadecimal character array to decode.
     * @param charset The character set to use.
     * @return The decoded string.
     * @throws InternalException if the source hexadecimal character array has an odd length.
     */
    public static String decodeString(final char[] data, final java.nio.charset.Charset charset) {
        return StringKit.toString(decode(data), charset);
    }

}
