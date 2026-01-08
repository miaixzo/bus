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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.miaixz.bus.core.codec.binary.decoder.Base64Decoder;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.*;

/**
 * A utility class for Base64 encoding and decoding.
 * <p>
 * Base64 encoding uses a 64-character set to represent binary data. This means that a 3-byte binary array is encoded
 * into a 4-character ASCII string, increasing the length by a third. This class provides methods for both standard and
 * URL-safe Base64 operations, following the specifications in RFC 4648.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Base64 {

    /**
     * Constructs a new Base64. Utility class constructor for static access.
     */
    private Base64() {
    }

    /**
     * Encodes a byte array into a Base64 byte array.
     *
     * @param arr     The byte array to be encoded.
     * @param lineSep If {@code true}, a CRLF line separator is inserted after every 76 characters (MIME-style).
     * @return The Base64-encoded byte array, or {@code null} if the input is {@code null}.
     */
    public static byte[] encode(final byte[] arr, final boolean lineSep) {
        if (arr == null) {
            return null;
        }
        return lineSep ? java.util.Base64.getMimeEncoder().encode(arr) : java.util.Base64.getEncoder().encode(arr);
    }

    /**
     * Encodes a string into a Base64 string using the default UTF-8 charset.
     *
     * @param source The string to be encoded.
     * @return The Base64-encoded string.
     */
    public static String encode(final CharSequence source) {
        return encode(source, Charset.UTF_8);
    }

    /**
     * Encodes a string into a URL-safe Base64 string using the default UTF-8 charset. The resulting string does not
     * contain padding.
     *
     * @param source The string to be encoded.
     * @return The URL-safe Base64-encoded string.
     */
    public static String encodeUrlSafe(final CharSequence source) {
        return encodeUrlSafe(source, Charset.UTF_8);
    }

    /**
     * Encodes a string into a Base64 string using the specified charset.
     *
     * @param source  The string to be encoded.
     * @param charset The character set to use.
     * @return The Base64-encoded string.
     */
    public static String encode(final CharSequence source, final java.nio.charset.Charset charset) {
        return encode(ByteKit.toBytes(source, charset));
    }

    /**
     * Encodes a string into a URL-safe Base64 string using the specified charset. The resulting string does not contain
     * padding.
     *
     * @param source  The string to be encoded.
     * @param charset The character set to use.
     * @return The URL-safe Base64-encoded string.
     */
    public static String encodeUrlSafe(final CharSequence source, final java.nio.charset.Charset charset) {
        return encodeUrlSafe(ByteKit.toBytes(source, charset));
    }

    /**
     * Encodes a byte array into a Base64 string.
     *
     * @param source The byte array to be encoded.
     * @return The Base64-encoded string, or {@code null} if the input is {@code null}.
     */
    public static String encode(final byte[] source) {
        if (source == null) {
            return null;
        }
        return java.util.Base64.getEncoder().encodeToString(source);
    }

    /**
     * Encodes a byte array into a Base64 string without padding (the trailing '=' characters).
     *
     * @param source The byte array to be encoded.
     * @return The Base64-encoded string without padding, or {@code null} if the input is {@code null}.
     */
    public static String encodeWithoutPadding(final byte[] source) {
        if (source == null) {
            return null;
        }
        return java.util.Base64.getEncoder().withoutPadding().encodeToString(source);
    }

    /**
     * Encodes a byte array into a URL-safe Base64 string without padding.
     *
     * @param source The byte array to be encoded.
     * @return The URL-safe Base64-encoded string, or {@code null} if the input is {@code null}.
     */
    public static String encodeUrlSafe(final byte[] source) {
        if (source == null) {
            return null;
        }
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(source);
    }

    /**
     * Encodes the content of an {@link InputStream} into a Base64 string.
     *
     * @param in The input stream to be encoded (e.g., an image or file stream).
     * @return The Base64-encoded string, or {@code null} if the input is {@code null}.
     */
    public static String encode(final InputStream in) {
        if (in == null) {
            return null;
        }
        return encode(IoKit.readBytes(in));
    }

    /**
     * Encodes the content of an {@link InputStream} into a URL-safe Base64 string.
     *
     * @param in The input stream to be encoded.
     * @return The URL-safe Base64-encoded string, or {@code null} if the input is {@code null}.
     */
    public static String encodeUrlSafe(final InputStream in) {
        if (in == null) {
            return null;
        }
        return encodeUrlSafe(IoKit.readBytes(in));
    }

    /**
     * Encodes the content of a file into a Base64 string.
     *
     * @param file The file to be encoded.
     * @return The Base64-encoded string.
     */
    public static String encode(final File file) {
        return encode(FileKit.readBytes(file));
    }

    /**
     * Encodes the content of a file into a URL-safe Base64 string.
     *
     * @param file The file to be encoded.
     * @return The URL-safe Base64-encoded string.
     */
    public static String encodeUrlSafe(final File file) {
        return encodeUrlSafe(FileKit.readBytes(file));
    }

    /**
     * Encodes a portion of a byte array into a Base64 character array.
     *
     * @param src     The source byte array.
     * @param srcPos  The starting position in the source array.
     * @param srcLen  The number of bytes to encode.
     * @param dest    The destination character array.
     * @param destPos The starting position in the destination array.
     * @throws IndexOutOfBoundsException if the positions or lengths are invalid.
     */
    public static void encode(byte[] src, int srcPos, int srcLen, char[] dest, int destPos) {
        if (srcPos < 0 || srcLen < 0 || srcLen > src.length - srcPos)
            throw new IndexOutOfBoundsException("Invalid source bounds");
        int destLen = (srcLen * 4 / 3 + 3) & ~3;
        if (destPos < 0 || destLen > dest.length - destPos)
            throw new IndexOutOfBoundsException("Invalid destination bounds");
        byte b1, b2, b3;
        int n = srcLen / 3;
        int r = srcLen - 3 * n;
        while (n-- > 0) {
            dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 = src[srcPos++]) >>> 2) & 0x3F];
            dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 & 0x03) << 4)
                    | (((b2 = src[srcPos++]) >>> 4) & 0x0F)];
            dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b2 & 0x0F) << 2)
                    | (((b3 = src[srcPos++]) >>> 6) & 0x03)];
            dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[b3 & 0x3F];
        }
        if (r > 0) {
            if (r == 1) {
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 = src[srcPos]) >>> 2) & 0x3F];
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 & 0x03) << 4)];
                dest[destPos++] = Symbol.C_EQUAL;
                dest[destPos] = Symbol.C_EQUAL;
            } else {
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 = src[srcPos++]) >>> 2) & 0x3F];
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[((b1 & 0x03) << 4)
                        | (((b2 = src[srcPos]) >>> 4) & 0x0F)];
                dest[destPos++] = CharKit.getChars(Normal.ENCODE_64_TABLE)[(b2 & 0x0F) << 2];
                dest[destPos] = Symbol.C_EQUAL;
            }
        }
    }

    /**
     * Decodes a Base64 string into a string using the default UTF-8 charset.
     *
     * @param source The Base64 string to be decoded.
     * @return The decoded string.
     */
    public static String decodeString(final CharSequence source) {
        return decodeString(source, Charset.UTF_8);
    }

    /**
     * Decodes a Base64 string into a string using the specified charset.
     *
     * @param source  The Base64 string to be decoded.
     * @param charset The character set to use.
     * @return The decoded string.
     */
    public static String decodeString(final CharSequence source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decode(source), charset);
    }

    /**
     * Decodes a Base64 string and writes the result to a file.
     *
     * @param base64   The Base64 string to be decoded.
     * @param destFile The destination file.
     * @return The destination file.
     */
    public static File decodeToFile(final CharSequence base64, final File destFile) {
        return FileKit.writeBytes(decode(base64), destFile);
    }

    /**
     * Decodes a Base64 string and writes the result to an {@link OutputStream}.
     *
     * @param base64     The Base64 string to be decoded.
     * @param out        The output stream to write to.
     * @param isCloseOut Whether to close the output stream after writing.
     */
    public static void decodeToStream(final CharSequence base64, final OutputStream out, final boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decode(base64));
    }

    /**
     * Decodes a Base64 string into a byte array. Handles both standard and URL-safe Base64 strings.
     *
     * @param base64 The Base64 string to be decoded.
     * @return The decoded byte array.
     */
    public static byte[] decode(final CharSequence base64) {
        return decode(ByteKit.toBytes(base64, Charset.UTF_8));
    }

    /**
     * Decodes a Base64 byte array. Handles both standard and URL-safe Base64 data.
     *
     * @param in The Base64 byte array to decode.
     * @return The decoded byte array.
     */
    public static byte[] decode(final byte[] in) {
        return Base64Decoder.INSTANCE.decode(in);
    }

    /**
     * Decodes a portion of a Base64 character array and writes the result to an {@link OutputStream}.
     *
     * @param ch  The character array containing Base64 data.
     * @param off The starting offset in the character array.
     * @param len The number of characters to decode.
     * @param out The output stream to write the decoded bytes to.
     * @throws InternalException if an {@link IOException} occurs during writing.
     */
    public static void decode(char[] ch, int off, int len, OutputStream out) {
        try {
            byte b2, b3;
            while ((len -= 2) >= 0) {
                out.write(
                        (byte) ((Normal.DECODE_64_TABLE[ch[off++]] << 2)
                                | ((b2 = Normal.DECODE_64_TABLE[ch[off++]]) >>> 4)));
                if ((len-- == 0) || ch[off] == Symbol.C_EQUAL)
                    break;
                out.write((byte) ((b2 << 4) | ((b3 = Normal.DECODE_64_TABLE[ch[off++]]) >>> 2)));
                if ((len-- == 0) || ch[off] == Symbol.C_EQUAL)
                    break;
                out.write((byte) ((b3 << 6) | Normal.DECODE_64_TABLE[ch[off++]]));
            }
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Checks if a given string is a valid Base64-encoded string.
     *
     * @param base64 The string to check.
     * @return {@code true} if the string is a valid Base64 string, {@code false} otherwise.
     */
    public static boolean isTypeBase64(final CharSequence base64) {
        if (base64 == null || base64.length() < 2) {
            return false;
        }

        final byte[] bytes = ByteKit.toBytes(base64);

        if (bytes.length != base64.length()) {
            // If lengths are not equal, it means there are multi-byte characters, so it's not Base64.
            return false;
        }

        return isTypeBase64(bytes);
    }

    /**
     * Checks if a given byte array is valid Base64 data.
     *
     * @param base64Bytes The byte array to check.
     * @return {@code true} if the byte array is valid Base64 data, {@code false} otherwise.
     */
    public static boolean isTypeBase64(final byte[] base64Bytes) {
        if (base64Bytes == null || base64Bytes.length < 3) {
            return false;
        }

        boolean hasPadding = false;
        for (final byte base64Byte : base64Bytes) {
            if (hasPadding) {
                if (Symbol.C_EQUAL != base64Byte) {
                    // If the previous character was padding, all subsequent characters must also be padding.
                    return false;
                }
            } else if (Symbol.C_EQUAL == base64Byte) {
                // Found padding character.
                hasPadding = true;
            } else if (!(Base64Decoder.INSTANCE.isBase64Code(base64Byte) || isWhiteSpace(base64Byte))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if a byte is a whitespace character (space, newline, carriage return, or tab).
     *
     * @param byteToCheck The byte to check.
     * @return {@code true} if the byte is a whitespace character, {@code false} otherwise.
     */
    private static boolean isWhiteSpace(final byte byteToCheck) {
        switch (byteToCheck) {
            case Symbol.C_SPACE:
            case '\n':
            case '\r':
            case '\t':
                return true;

            default:
                return false;
        }
    }

}
