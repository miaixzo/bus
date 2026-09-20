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
package org.miaixz.bus.core.codec.binary;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.xyz.ByteKit;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Encodes and decodes Base85 values.
 * <p>
 * Base85 converts binary data into printable ASCII characters by encoding each four-byte block as five characters. It
 * is denser than Base64 and avoids the large-number arithmetic commonly used by Base62 encoders. This facade supports
 * the continuous Ascii85 alphabet and the ZeroMQ Z85 alphabet.
 *
 * @author Kimi Liu
 */
public class Base85 {

    /**
     * Keeps Base85 encoding and decoding on the static API.
     */
    public Base85() {
        // No initialization required.
    }

    /**
     * Encodes a string into a Base85 string using UTF-8.
     *
     * @param source The string to encode.
     * @return The Base85-encoded string.
     */
    public static String encode(final CharSequence source) {
        return encode(source, Charset.UTF_8);
    }

    /**
     * Encodes a string into a Base85 string using the specified charset.
     *
     * @param source  The string to encode.
     * @param charset The charset used to convert the string to bytes.
     * @return The Base85-encoded string.
     */
    public static String encode(final CharSequence source, final java.nio.charset.Charset charset) {
        return encode(ByteKit.toBytes(source, charset));
    }

    /**
     * Encodes a byte array into a Base85 string using the Ascii85 alphabet.
     *
     * @param source The byte array to encode.
     * @return The Base85-encoded string.
     */
    public static String encode(final byte[] source) {
        return new String(Base85Codec.INSTANCE.encode(source));
    }

    /**
     * Encodes all bytes read from an input stream into a Base85 string.
     *
     * @param in The input stream to encode.
     * @return The Base85-encoded string.
     */
    public static String encode(final InputStream in) {
        return encode(IoKit.readBytes(in));
    }

    /**
     * Encodes the content of a file into a Base85 string.
     *
     * @param file The file to encode.
     * @return The Base85-encoded string.
     */
    public static String encode(final File file) {
        return encode(FileKit.readBytes(file));
    }

    /**
     * Encodes a string into a Z85 string using UTF-8.
     *
     * @param source The string to encode.
     * @return The Z85-encoded string.
     */
    public static String encodeZ85(final CharSequence source) {
        return encodeZ85(source, Charset.UTF_8);
    }

    /**
     * Encodes a string into a Z85 string using the specified charset.
     *
     * @param source  The string to encode.
     * @param charset The charset used to convert the string to bytes.
     * @return The Z85-encoded string.
     */
    public static String encodeZ85(final CharSequence source, final java.nio.charset.Charset charset) {
        return encodeZ85(ByteKit.toBytes(source, charset));
    }

    /**
     * Encodes a byte array into a Z85 string using the ZeroMQ alphabet.
     *
     * @param source The byte array to encode.
     * @return The Z85-encoded string.
     */
    public static String encodeZ85(final byte[] source) {
        return new String(Base85Codec.INSTANCE.encode(source, true));
    }

    /**
     * Encodes all bytes read from an input stream into a Z85 string.
     *
     * @param in The input stream to encode.
     * @return The Z85-encoded string.
     */
    public static String encodeZ85(final InputStream in) {
        return encodeZ85(IoKit.readBytes(in));
    }

    /**
     * Encodes the content of a file into a Z85 string.
     *
     * @param file The file to encode.
     * @return The Z85-encoded string.
     */
    public static String encodeZ85(final File file) {
        return encodeZ85(FileKit.readBytes(file));
    }

    /**
     * Decodes a Base85 string into a UTF-8 string.
     *
     * @param source The Base85 string to decode.
     * @return The decoded string.
     */
    public static String decodeString(final CharSequence source) {
        return decodeString(source, Charset.UTF_8);
    }

    /**
     * Decodes a Base85 string into a string using the specified charset.
     *
     * @param source  The Base85 string to decode.
     * @param charset The charset used to convert decoded bytes to text.
     * @return The decoded string.
     */
    public static String decodeString(final CharSequence source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decode(source), charset);
    }

    /**
     * Decodes a Base85 string into a UTF-8 string.
     *
     * @param source The Base85 string to decode.
     * @return The decoded string.
     */
    public static String decodeStr(final CharSequence source) {
        return decodeString(source);
    }

    /**
     * Decodes a Base85 string into a string using the specified charset.
     *
     * @param source  The Base85 string to decode.
     * @param charset The charset used to convert decoded bytes to text.
     * @return The decoded string.
     */
    public static String decodeStr(final CharSequence source, final java.nio.charset.Charset charset) {
        return decodeString(source, charset);
    }

    /**
     * Decodes a Base85 string into a GBK string.
     *
     * @param source The Base85 string to decode.
     * @return The decoded string.
     */
    public static String decodeStrGbk(final CharSequence source) {
        return decodeString(source, Charset.GBK);
    }

    /**
     * Decodes a Base85 string and writes the result to a file.
     *
     * @param base85   The Base85 string to decode.
     * @param destFile The destination file.
     * @return The destination file.
     */
    public static File decodeToFile(final CharSequence base85, final File destFile) {
        return FileKit.writeBytes(decode(base85), destFile);
    }

    /**
     * Decodes a Base85 string and writes the result to an output stream.
     *
     * @param base85Str  The Base85 string to decode.
     * @param out        The output stream to write to.
     * @param isCloseOut Whether to close the output stream after writing.
     */
    public static void decodeToStream(final CharSequence base85Str, final OutputStream out, final boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decode(base85Str));
    }

    /**
     * Decodes a Base85 string into bytes.
     *
     * @param base85Str The Base85 string to decode.
     * @return The decoded bytes.
     */
    public static byte[] decode(final CharSequence base85Str) {
        return decode(ByteKit.toBytes(base85Str, Charset.UTF_8));
    }

    /**
     * Decodes a Base85 byte array into bytes.
     *
     * @param base85Bytes The Base85 input bytes.
     * @return The decoded bytes.
     */
    public static byte[] decode(final byte[] base85Bytes) {
        return Base85Codec.INSTANCE.decode(base85Bytes);
    }

    /**
     * Decodes a Z85 string into a UTF-8 string.
     *
     * @param source The Z85 string to decode.
     * @return The decoded string.
     */
    public static String decodeStringZ85(final CharSequence source) {
        return decodeStringZ85(source, Charset.UTF_8);
    }

    /**
     * Decodes a Z85 string into a string using the specified charset.
     *
     * @param source  The Z85 string to decode.
     * @param charset The charset used to convert decoded bytes to text.
     * @return The decoded string.
     */
    public static String decodeStringZ85(final CharSequence source, final java.nio.charset.Charset charset) {
        return StringKit.toString(decodeZ85(source), charset);
    }

    /**
     * Decodes a Z85 string into a UTF-8 string.
     *
     * @param source The Z85 string to decode.
     * @return The decoded string.
     */
    public static String decodeStrZ85(final CharSequence source) {
        return decodeStringZ85(source);
    }

    /**
     * Decodes a Z85 string into a string using the specified charset.
     *
     * @param source  The Z85 string to decode.
     * @param charset The charset used to convert decoded bytes to text.
     * @return The decoded string.
     */
    public static String decodeStrZ85(final CharSequence source, final java.nio.charset.Charset charset) {
        return decodeStringZ85(source, charset);
    }

    /**
     * Decodes a Z85 string and writes the result to a file.
     *
     * @param base85   The Z85 string to decode.
     * @param destFile The destination file.
     * @return The destination file.
     */
    public static File decodeToFileZ85(final CharSequence base85, final File destFile) {
        return FileKit.writeBytes(decodeZ85(base85), destFile);
    }

    /**
     * Decodes a Z85 string and writes the result to an output stream.
     *
     * @param base85Str  The Z85 string to decode.
     * @param out        The output stream to write to.
     * @param isCloseOut Whether to close the output stream after writing.
     */
    public static void decodeToStreamZ85(
            final CharSequence base85Str,
            final OutputStream out,
            final boolean isCloseOut) {
        IoKit.write(out, isCloseOut, decodeZ85(base85Str));
    }

    /**
     * Decodes a Z85 string into bytes.
     *
     * @param base85Str The Z85 string to decode.
     * @return The decoded bytes.
     */
    public static byte[] decodeZ85(final CharSequence base85Str) {
        return decodeZ85(ByteKit.toBytes(base85Str, Charset.UTF_8));
    }

    /**
     * Decodes a Z85 byte array into bytes.
     *
     * @param base85Bytes The Z85 input bytes.
     * @return The decoded bytes.
     */
    public static byte[] decodeZ85(final byte[] base85Bytes) {
        return Base85Codec.INSTANCE.decode(base85Bytes, true);
    }

}
