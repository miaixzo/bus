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
package org.miaixz.bus.core.xyz;

/**
 * Utility for converting between Unicode-escaped strings and normal strings.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class UnicodeKit {

    /**
     * Converts a Unicode-escaped string (e.g., "\\uXXXX") to a normal string.
     *
     * @param unicode The Unicode-escaped string.
     * @return The normal string.
     */
    public static String toString(final String unicode) {
        if (StringKit.isBlank(unicode)) {
            return unicode;
        }

        final int len = unicode.length();
        final StringBuilder sb = new StringBuilder(len);
        int i;
        int pos = 0;
        while ((i = StringKit.indexOfIgnoreCase(unicode, "\\u", pos)) != -1) {
            sb.append(unicode, pos, i);
            pos = i;
            if (i + 5 < len) {
                final char c;
                try {
                    c = (char) Integer.parseInt(unicode.substring(i + 2, i + 6), 16);
                    sb.append(c);
                    pos = i + 6;
                } catch (final NumberFormatException e) {
                    // Invalid Unicode sequence, skip it.
                    sb.append(unicode, pos, i + 2);
                    pos = i + 2;
                }
            } else {
                break;
            }
        }

        if (pos < len) {
            sb.append(unicode, pos, len);
        }
        return sb.toString();
    }

    /**
     * Encodes a character into its Unicode-escaped form.
     *
     * @param c The character to encode.
     * @return The Unicode string.
     * @see HexKit#toUnicodeHex(char)
     */
    public static String toUnicode(final char c) {
        return HexKit.toUnicodeHex(c);
    }

    /**
     * Encodes a character (represented as an int) into its Unicode-escaped form.
     *
     * @param c The character code point.
     * @return The Unicode string.
     * @see HexKit#toUnicodeHex(int)
     */
    public static String toUnicode(final int c) {
        return HexKit.toUnicodeHex(c);
    }

    /**
     * Encodes a string into its Unicode-escaped form.
     *
     * @param text The string to encode.
     * @return The Unicode string.
     */
    public static String toUnicode(final CharSequence text) {
        return toUnicode(text, true);
    }

    /**
     * Encodes a string into its Unicode-escaped form.
     *
     * @param text        The string to encode.
     * @param isSkipAscii If `true`, printable ASCII characters are not encoded.
     * @return The Unicode string.
     */
    public static String toUnicode(final CharSequence text, final boolean isSkipAscii) {
        if (StringKit.isEmpty(text)) {
            return StringKit.toStringOrNull(text);
        }

        final int len = text.length();
        final StringBuilder unicode = new StringBuilder(text.length() * 6);
        char c;
        for (int i = 0; i < len; i++) {
            c = text.charAt(i);
            if (isSkipAscii && CharKit.isAsciiPrintable(c)) {
                unicode.append(c);
            } else {
                unicode.append(HexKit.toUnicodeHex(c));
            }
        }
        return unicode.toString();
    }

}
