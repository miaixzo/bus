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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.miaixz.bus.core.codec.binary.provider.Base62Provider;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A utility class for Base62 encoding and decoding.
 * <p>
 * Base62 is a binary-to-text encoding scheme that represents binary data in an ASCII string format by using a
 * 62-character set (typically {@code 0-9}, {@code a-z}, {@code A-Z}). It is commonly used in applications like URL
 * shorteners where a compact, human-readable representation of data is desired.
 *
 * <p>
 * This class provides methods for both standard and inverted alphabet encoding. The standard alphabet is
 * {@code 0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz}. The inverted alphabet is
 * {@code 0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ}.
 *
 * @author Kimi Liu
 * @see Base62Provider
 * @since Java 17+
 */
public class Base62 {

    /**
     * Encodes a string into a Base62 string using the standard alphabet and default UTF-8 charset.
     *
     * @param source The string to be encoded.
     * @return The Base62-encoded string.
     */
    public static String encode(final CharSequence source) {
        return encode(source, Charset.UTF_8);
    }

    /**
     * Encodes a string into a Base62 string using the standard alphabet and the specified charset.
     *
     * @param source  The string to be encoded.
     * @param charset The character set to use.
     * @return The Base62-encoded string.
     */
    public static String encode(final CharSequence source, final java.nio.charset.Charset charset) {
        return encode(ByteKit.toBytes(source, charset));
    }

    /**
     * Encodes a byte array into a Base62 string using the standard alphabet.
     *
     * @param source The byte array to be encoded.
     * @return The Base62-encoded string.
     */
    public static String encode(final byte[] source) {
        return new String(Base62Provider.INSTANCE.encode(source));
    }

    /**
     * Encodes the content of an {@link InputStream} into a Base62 string using the standard alphabet.
     *
     * @param in The input stream to be encoded (e.g., an image or file stream).
     * @return The Base62-encoded string.
     */
    public static String encode(final InputStream in) {
        return encode(IoKit.readBytes(in));
    }

    /**
     * Encodes the content of a file into a Base62 string using the standard alphabet.
     *
     * @param file The file to be encoded.
     * @return The Base62-encoded string.
     */
    public static String encode(final File file) {
        return encode(FileKit.readBytes(file));
    }

    /**
     * Encodes a string into a Base62 string using the inverted alphabet and the default UTF-8 charset.
     *
     * @param source The string to be encoded.
     * @return The Base62-encoded string.
     */
    public static String encodeInverted(final CharSequence source) {
        return encodeInverted(source, Charset.UTF_8);
    }

    /**
     * Encodes a string into a Base62 string using the inverted alphabet and the specified charset.
     *
     * @param source  The string to be encoded.
     * @param charset The character set to use.
     * @return The Base62-encoded string.
     */
    public static String encodeInverted(final CharSequence source, final java.nio.charset.Charset charset) {
        return encodeInverted(ByteKit.toBytes(source, charset));
    }

    /**
     * Encodes a byte array into a Base62 string using the inverted alphabet.
     *
     * @param source The byte array to be encoded.
     * @return The Base62-encoded string.
     */
    public static String encodeInverted(final byte[] source) {
        return new String(Base62Provider.INSTANCE.encode(source, true));
    }

    /**
     * Encodes the content of an {@link InputStream} into a Base62 string using the inverted alphabet.
     *
     * @param in The input stream to be encoded.
     * @return The Base62-encoded string.
     */
    public static String encodeInverted(final InputStream in) {
        return encodeInverted(IoKit.readBytes(in));
    }

    /**
     * Encodes the content of a file into a Base62 string using the inverted alphabet.
     *
     * @param file The file to be encoded.
     * @return The Base62-encoded string.
     */
    public static String encodeInverted(final File file) {
        return encodeInverted(FileKit.readBytes(file));
    }

    /**
     * Decodes a Base62 string into a string using the default UTF-8 charset. The decoder is case-sensitive and
     * automatically detects the alphabet (standard or inverted).
     *
     * @param source The Base62 string to be decoded.
     * @return The decoded string.
     */
    public static String decodeString(final CharSequence source) {
        return decodeString(source, Charset.UTF_8);
    }

    /**
     * Decodes a Base62 string into a string using the specified charset. The decoder is case-sensitive and
     * automatically detects the alphabet (standard or inverted).
     *
     * @param source  The Base62 string to be decoded.
     * @param charset The character set to use.
     * @return The decoded string.
     */
    public static String decodeString(final CharSequence source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decode(source), charset);
    }

    /**
     * Decodes a Base62 string and writes the result to a file. The decoder is case-sensitive and automatically detects
     * the alphabet.
     *
     * @param base62   The Base62 string to be decoded.
     * @param destFile The destination file.
     * @return The destination file.
     */
    public static File decodeToFile(final CharSequence base62, final File destFile) {
        return FileKit.writeBytes(decode(base62), destFile);
    }

    /**
     * Decodes a Base62 string and writes the result to an {@link OutputStream}. The decoder is case-sensitive and
     * automatically detects the alphabet.
     *
     * @param base62Str  The Base62 string to be decoded.
     * @param out        The output stream to write to.
     * @param isCloseOut Whether to close the output stream after writing.
     */
    public static void decodeToStream(final CharSequence base62Str, final OutputStream out, final boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decode(base62Str));
    }

    /**
     * Decodes a Base62 string into a byte array. The decoder is case-sensitive and automatically detects the alphabet.
     *
     * @param base62Str The Base62 string to be decoded.
     * @return The decoded byte array.
     */
    public static byte[] decode(final CharSequence base62Str) {
        return decode(ByteKit.toBytes(base62Str, Charset.UTF_8));
    }

    /**
     * Decodes a Base62 byte array into a byte array. The decoder is case-sensitive and automatically detects the
     * alphabet.
     *
     * @param base62bytes The Base62 input as a byte array.
     * @return The decoded byte array.
     */
    public static byte[] decode(final byte[] base62bytes) {
        return Base62Provider.INSTANCE.decode(base62bytes);
    }

    /**
     * Decodes a Base62 string (using the inverted alphabet) into a string using the default UTF-8 charset.
     *
     * @param source The Base62 string to be decoded.
     * @return The decoded string.
     */
    public static String decodeStrInverted(final CharSequence source) {
        return decodeStrInverted(source, Charset.UTF_8);
    }

    /**
     * Decodes a Base62 string (using the inverted alphabet) into a string using the specified charset.
     *
     * @param source  The Base62 string to be decoded.
     * @param charset The character set to use.
     * @return The decoded string.
     */
    public static String decodeStrInverted(final CharSequence source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decodeInverted(source), charset);
    }

    /**
     * Decodes a Base62 string (using the inverted alphabet) and writes the result to a file.
     *
     * @param base62   The Base62 string to be decoded.
     * @param destFile The destination file.
     * @return The destination file.
     */
    public static File decodeToFileInverted(final CharSequence base62, final File destFile) {
        return FileKit.writeBytes(decodeInverted(base62), destFile);
    }

    /**
     * Decodes a Base62 string (using the inverted alphabet) and writes the result to an {@link OutputStream}.
     *
     * @param base62     The Base62 string to be decoded.
     * @param out        The output stream to write to.
     * @param isCloseOut Whether to close the output stream after writing.
     */
    public static void decodeToStreamInverted(
            final CharSequence base62,
            final OutputStream out,
            final boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decodeInverted(base62));
    }

    /**
     * Decodes a Base62 string (using the inverted alphabet) into a byte array.
     *
     * @param base62 The Base62 string to be decoded.
     * @return The decoded byte array.
     */
    public static byte[] decodeInverted(final CharSequence base62) {
        return decodeInverted(ByteKit.toBytes(base62, Charset.UTF_8));
    }

    /**
     * Decodes a Base62 byte array (using the inverted alphabet) into a byte array.
     *
     * @param base62bytes The Base62 input as a byte array.
     * @return The decoded byte array.
     */
    public static byte[] decodeInverted(final byte[] base62bytes) {
        return Base62Provider.INSTANCE.decode(base62bytes, true);
    }

}
