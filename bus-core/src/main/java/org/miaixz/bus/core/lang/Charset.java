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
package org.miaixz.bus.core.lang;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;

import org.miaixz.bus.core.io.CharsetDetector;
import org.miaixz.bus.core.xyz.FileKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Utility class for character set operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Charset {

    /**
     * Constructs a new Charset. Utility class constructor for static access.
     */
    private Charset() {
    }

    /**
     * The default character set of the Java virtual machine.
     */
    public static final java.nio.charset.Charset DEFAULT = java.nio.charset.Charset.defaultCharset();
    /**
     * The display name of the default character set.
     */
    public static final String DEFAULT_CHARSET = DEFAULT.displayName();
    /**
     * ISO Latin Alphabet No. 1, also known as ISO-8859-1.
     */
    public static final String DEFAULT_ISO_8859_1 = "ISO-8859-1";
    /**
     * {@link java.nio.charset.Charset} instance for ISO-8859-1.
     */
    public static final java.nio.charset.Charset ISO_8859_1 = java.nio.charset.Charset.forName(DEFAULT_ISO_8859_1);
    /**
     * 7-bit ASCII, also known as ISO646-US, which is the basic Latin character block of the Unicode character set.
     */
    public static final String DEFAULT_US_ASCII = "US-ASCII";
    /**
     * {@link java.nio.charset.Charset} instance for US-ASCII.
     */
    public static final java.nio.charset.Charset US_ASCII = java.nio.charset.Charset.forName(DEFAULT_US_ASCII);
    /**
     * GBK character set, used for Chinese characters.
     */
    public static final String DEFAULT_GBK = "GBK";
    /**
     * {@link java.nio.charset.Charset} instance for GBK.
     */
    public static final java.nio.charset.Charset GBK = java.nio.charset.Charset.forName(DEFAULT_GBK);
    /**
     * GB2312 character set, used for simplified Chinese characters.
     */
    public static final String DEFAULT_GB_2312 = "GB2312";
    /**
     * {@link java.nio.charset.Charset} instance for GB2312.
     */
    public static final java.nio.charset.Charset GB_2312 = java.nio.charset.Charset.forName(DEFAULT_GB_2312);
    /**
     * GB18030 character set, a Chinese national standard for character encoding.
     */
    public static final String DEFAULT_GB_18030 = "GB18030";
    /**
     * {@link java.nio.charset.Charset} instance for GB18030.
     */
    public static final java.nio.charset.Charset GB_18030 = java.nio.charset.Charset.forName(DEFAULT_GB_18030);
    /**
     * 8-bit UCS Transformation Format, commonly used for Unicode.
     */
    public static final String DEFAULT_UTF_8 = "UTF-8";
    /**
     * {@link java.nio.charset.Charset} instance for UTF-8.
     */
    public static final java.nio.charset.Charset UTF_8 = java.nio.charset.Charset.forName(DEFAULT_UTF_8);
    /**
     * 16-bit UCS Transformation Format, byte order identified by an optional byte order mark.
     */
    public static final String DEFAULT_UTF_16 = "UTF-16";
    /**
     * {@link java.nio.charset.Charset} instance for UTF-16.
     */
    public static final java.nio.charset.Charset UTF_16 = java.nio.charset.Charset.forName(DEFAULT_UTF_16);
    /**
     * 16-bit UCS Transformation Format, big-endian byte order.
     */
    public static final String DEFAULT_UTF_16_BE = "UTF-16BE";
    /**
     * {@link java.nio.charset.Charset} instance for UTF-16BE.
     */
    public static final java.nio.charset.Charset UTF_16_BE = java.nio.charset.Charset.forName(DEFAULT_UTF_16_BE);
    /**
     * 16-bit UCS Transformation Format, little-endian byte order.
     */
    public static final String DEFAULT_UTF_16_LE = "UTF-16LE";
    /**
     * {@link java.nio.charset.Charset} instance for UTF-16LE.
     */
    public static final java.nio.charset.Charset UTF_16_LE = java.nio.charset.Charset.forName(DEFAULT_UTF_16_LE);
    /**
     * 32-bit UCS Transformation Format, big-endian byte order.
     */
    public static final String DEFAULT_UTF_32_BE = "UTF-32BE";
    /**
     * {@link java.nio.charset.Charset} instance for UTF-32BE.
     */
    public static final java.nio.charset.Charset UTF_32_BE = java.nio.charset.Charset.forName(DEFAULT_UTF_32_BE);
    /**
     * 32-bit UCS Transformation Format, little-endian byte order.
     */
    public static final String DEFAULT_UTF_32_LE = "UTF-32LE";
    /**
     * {@link java.nio.charset.Charset} instance for UTF-32LE.
     */
    public static final java.nio.charset.Charset UTF_32_LE = java.nio.charset.Charset.forName(DEFAULT_UTF_32_LE);

    /**
     * {@link java.nio.charset.Charset} instance for Windows-1252.
     */
    public static final String DEFAULT_WINDOWS_1252 = "Windows-1252";
    /**
     * {@link java.nio.charset.Charset} instance for Windows-1252.
     */
    public static final java.nio.charset.Charset WINDOWS_1252 = java.nio.charset.Charset.forName(DEFAULT_WINDOWS_1252);

    /**
     * Converts a character set name to a {@link java.nio.charset.Charset} object. If the character set name is blank,
     * the default character set is returned.
     *
     * @param charsetName The name of the character set. If {@code null} or empty, the default character set is used.
     * @return A {@link java.nio.charset.Charset} object.
     * @throws UnsupportedCharsetException If the named character set is not supported.
     */
    public static java.nio.charset.Charset charset(final String charsetName) throws UnsupportedCharsetException {
        return StringKit.isBlank(charsetName) ? java.nio.charset.Charset.defaultCharset()
                : java.nio.charset.Charset.forName(charsetName);
    }

    /**
     * Parses a character set name into a {@link java.nio.charset.Charset} object. If parsing fails or the name is
     * blank, the system's default character set is returned.
     *
     * @param charsetName The name of the character set. If {@code null} or empty, the default character set is used.
     * @return A {@link java.nio.charset.Charset} object.
     */
    public static java.nio.charset.Charset parse(final String charsetName) {
        return parse(charsetName, java.nio.charset.Charset.defaultCharset());
    }

    /**
     * Parses a character set name into a {@link java.nio.charset.Charset} object. If parsing fails or the name is
     * blank, the specified default character set is returned.
     *
     * @param charsetName    The name of the character set. If {@code null} or empty, the default character set is used.
     * @param defaultCharset The default character set to use if parsing fails.
     * @return A {@link java.nio.charset.Charset} object.
     */
    public static java.nio.charset.Charset parse(
            final String charsetName,
            final java.nio.charset.Charset defaultCharset) {
        if (StringKit.isBlank(charsetName)) {
            return defaultCharset;
        }

        java.nio.charset.Charset result;
        try {
            result = java.nio.charset.Charset.forName(charsetName);
        } catch (final UnsupportedCharsetException e) {
            result = defaultCharset;
        }

        return result;
    }

    /**
     * Converts the character set encoding of a string.
     *
     * @param source      The input string.
     * @param srcCharset  The source character set. Defaults to ISO-8859-1 if {@code null}.
     * @param destCharset The destination character set. Defaults to UTF-8 if {@code null}.
     * @return The string with the converted character set.
     */
    public static String convert(final String source, final String srcCharset, final String destCharset) {
        return convert(
                source,
                java.nio.charset.Charset.forName(srcCharset),
                java.nio.charset.Charset.forName(destCharset));
    }

    /**
     * Converts the character set encoding of a string. This method is used to correct garbled characters caused by
     * incorrect encoding when reading a string. For example, if a client encodes request parameters with GBK in a
     * Servlet request, and we read it with UTF-8, it will be garbled. This method can restore the original content.
     * 
     * <pre>
     * Client - GBK encoding - Servlet container - UTF-8 decoding - garbled characters
     * Garbled characters - UTF-8 encoding - GBK decoding - correct content
     * </pre>
     *
     * @param source      The input string.
     * @param srcCharset  The source character set. Defaults to ISO-8859-1 if {@code null}.
     * @param destCharset The destination character set. Defaults to UTF-8 if {@code null}.
     * @return The string with the converted character set.
     */
    public static String convert(
            final String source,
            java.nio.charset.Charset srcCharset,
            java.nio.charset.Charset destCharset) {
        if (null == srcCharset) {
            srcCharset = ISO_8859_1;
        }

        if (null == destCharset) {
            destCharset = UTF_8;
        }

        if (StringKit.isBlank(source) || srcCharset.equals(destCharset)) {
            return source;
        }
        return new String(source.getBytes(srcCharset), destCharset);
    }

    /**
     * Converts the character set encoding of a file. This method assumes that the actual encoding of the file to be
     * read matches the specified {@code srcCharset}, otherwise it may lead to garbled characters.
     *
     * @param file        The file to convert.
     * @param srcCharset  The source character set of the file. Must be consistent with the file content's encoding.
     * @param destCharset The target character set for conversion.
     * @return The file with the converted encoding.
     */
    public static File convert(
            final File file,
            final java.nio.charset.Charset srcCharset,
            final java.nio.charset.Charset destCharset) {
        return FileKit.writeString(FileKit.readString(file, srcCharset), file, destCharset);
    }

    /**
     * Returns the name of the system's default character set. If the operating system is Windows, it defaults to GBK;
     * otherwise, it uses the value from {@link java.nio.charset.Charset#defaultCharset()}.
     *
     * @return The name of the system's default character set.
     * @see java.nio.charset.Charset#defaultCharset()
     */
    public static String systemCharsetName() {
        return systemCharset().name();
    }

    /**
     * Returns the system's default character set. If the operating system is Windows, it defaults to GBK; otherwise, it
     * uses the value from {@link java.nio.charset.Charset#defaultCharset()}.
     *
     * @return The system's default character set.
     * @see java.nio.charset.Charset#defaultCharset()
     */
    public static java.nio.charset.Charset systemCharset() {
        return FileKit.isWindows() ? GBK : defaultCharset();
    }

    /**
     * Returns the name of the system's default character set.
     *
     * @return The name of the system's default character set.
     */
    public static String defaultCharsetName() {
        return defaultCharset().name();
    }

    /**
     * Returns the system's default character set.
     *
     * @return The system's default character set.
     */
    public static java.nio.charset.Charset defaultCharset() {
        return java.nio.charset.Charset.defaultCharset();
    }

    /**
     * Detects the character set of an input stream. Note: This method reads a portion of the stream and then closes it.
     * If the stream needs to be reused, ensure it supports the {@code reset} method.
     *
     * @param in       The input stream to detect the character set from. This stream will be closed after use.
     * @param charsets Optional character sets to test. If {@code null} or empty, a default array of encodings is used.
     * @return The detected character set.
     * @see CharsetDetector#detect(InputStream, java.nio.charset.Charset...)
     */
    public static java.nio.charset.Charset detect(final InputStream in, final java.nio.charset.Charset... charsets) {
        return CharsetDetector.detect(in, charsets);
    }

    /**
     * Detects the character set of an input stream with a specified buffer size. Note: This method reads a portion of
     * the stream and then closes it. If the stream needs to be reused, ensure it supports the {@code reset} method.
     *
     * @param bufferSize The custom buffer size, i.e., the length to check each time.
     * @param in         The input stream to detect the character set from. This stream will be closed after use.
     * @param charsets   Optional character sets to test. If {@code null} or empty, a default array of encodings is
     *                   used.
     * @return The detected character set.
     * @see CharsetDetector#detect(int, InputStream, java.nio.charset.Charset...)
     */
    public static java.nio.charset.Charset detect(
            final int bufferSize,
            final InputStream in,
            final java.nio.charset.Charset... charsets) {
        return CharsetDetector.detect(bufferSize, in, charsets);
    }

    /**
     * Creates a new {@link CharsetEncoder} instance configured with the specified character set and error handling
     * action.
     *
     * @param charset The character set to use, must not be {@code null}.
     * @param action  The action to take for malformed input and unmappable characters, must not be {@code null}.
     * @return A configured {@link CharsetEncoder} instance.
     */
    public static CharsetEncoder newEncoder(final java.nio.charset.Charset charset, final CodingErrorAction action) {
        return Assert.notNull(charset).newEncoder().onMalformedInput(action).onUnmappableCharacter(action);
    }

    /**
     * Creates a new {@link CharsetDecoder} instance configured with the specified character set and error handling
     * action.
     *
     * @param charset The character set to use, must not be {@code null}.
     * @param action  The action to take when encountering malformed input or unmappable characters (e.g., ignore,
     *                replace).
     * @return A configured {@link CharsetDecoder} instance for decoding characters.
     */
    public static CharsetDecoder newDecoder(final java.nio.charset.Charset charset, final CodingErrorAction action) {
        return Assert.notNull(charset).newDecoder().onMalformedInput(action).onUnmappableCharacter(action)
                // Sets the replacement string when an undecodable character is encountered
                .replaceWith("?");
    }

}
