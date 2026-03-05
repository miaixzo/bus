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
