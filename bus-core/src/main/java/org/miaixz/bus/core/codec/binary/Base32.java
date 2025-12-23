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
package org.miaixz.bus.core.codec.binary;

import org.miaixz.bus.core.codec.binary.provider.Base32Provider;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Provides Base32 encoding and decoding as per RFC 4648.
 * <p>
 * Base32 uses a 32-character set to represent binary data. It is more space-efficient than Base16 but less so than
 * Base64. Each 5 bits of the input data are mapped to a Base32 character.
 *
 * <p>
 * This class supports both the standard Base32 alphabet and the "Extended Hex" alphabet. See
 * <a href="https://datatracker.ietf.org/doc/html/rfc4648#section-6">RFC 4648 Section 6</a>.
 *
 * @author Kimi Liu
 * @see Base32Provider
 * @since Java 17+
 */
public class Base32 {

    /**
     * Constructs a new Base32. Utility class constructor for static access.
     */
    private Base32() {
    }

    /**
     * Encodes a byte array into a Base32 string using the standard alphabet.
     *
     * @param bytes The byte array to encode.
     * @return The Base32-encoded string.
     */
    public static String encode(final byte[] bytes) {
        return Base32Provider.INSTANCE.encode(bytes);
    }

    /**
     * Encodes a string into a Base32 string using the standard alphabet and UTF-8 charset.
     *
     * @param source The string to encode.
     * @return The Base32-encoded string.
     */
    public static String encode(final String source) {
        return encode(source, Charset.UTF_8);
    }

    /**
     * Encodes a string into a Base32 string using the standard alphabet and a specified charset.
     *
     * @param source  The string to encode.
     * @param charset The character set to use for encoding the string.
     * @return The Base32-encoded string.
     */
    public static String encode(final String source, final java.nio.charset.Charset charset) {
        return encode(ByteKit.toBytes(source, charset));
    }

    /**
     * Encodes a byte array into a Base32 string using the "Extended Hex" alphabet.
     *
     * @param bytes The byte array to encode.
     * @return The Base32-encoded string with the hex alphabet.
     */
    public static String encodeHex(final byte[] bytes) {
        return Base32Provider.INSTANCE.encode(bytes, true);
    }

    /**
     * Encodes a string into a Base32 string using the "Extended Hex" alphabet and UTF-8 charset.
     *
     * @param source The string to encode.
     * @return The Base32-encoded string with the hex alphabet.
     */
    public static String encodeHex(final String source) {
        return encodeHex(source, Charset.UTF_8);
    }

    /**
     * Encodes a string into a Base32 string using the "Extended Hex" alphabet and a specified charset.
     *
     * @param source  The string to encode.
     * @param charset The character set to use for encoding the string.
     * @return The Base32-encoded string with the hex alphabet.
     */
    public static String encodeHex(final String source, final java.nio.charset.Charset charset) {
        return encodeHex(ByteKit.toBytes(source, charset));
    }

    /**
     * Decodes a Base32 string (standard or hex alphabet) into a byte array. The decoder is case-insensitive and ignores
     * whitespace.
     *
     * @param base32 The Base32 string to decode.
     * @return The decoded byte array.
     */
    public static byte[] decode(final String base32) {
        return Base32Provider.INSTANCE.decode(base32);
    }

    /**
     * Decodes a Base32 string into a string using the UTF-8 charset.
     *
     * @param source The Base32 string to decode.
     * @return The decoded string.
     */
    public static String decodeString(final String source) {
        return decodeString(source, Charset.UTF_8);
    }

    /**
     * Decodes a Base32 string into a string using a specified charset.
     *
     * @param source  The Base32 string to decode.
     * @param charset The character set to use for the decoded string.
     * @return The decoded string.
     */
    public static String decodeString(final String source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decode(source), charset);
    }

    /**
     * Decodes a Base32 string with the "Extended Hex" alphabet into a byte array.
     *
     * @param base32 The Base32 string with the hex alphabet to decode.
     * @return The decoded byte array.
     */
    public static byte[] decodeHex(final String base32) {
        return Base32Provider.INSTANCE.decode(base32, true);
    }

    /**
     * Decodes a Base32 string with the "Extended Hex" alphabet into a string using the UTF-8 charset.
     *
     * @param source The Base32 string with the hex alphabet to decode.
     * @return The decoded string.
     */
    public static String decodeStrHex(final String source) {
        return decodeStrHex(source, Charset.UTF_8);
    }

    /**
     * Decodes a Base32 string with the "Extended Hex" alphabet into a string using a specified charset.
     *
     * @param source  The Base32 string with the hex alphabet to decode.
     * @param charset The character set to use for the decoded string.
     * @return The decoded string.
     */
    public static String decodeStrHex(final String source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decodeHex(source), charset);
    }

}
