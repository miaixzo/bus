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

import java.io.StringReader;
import java.io.StringWriter;
import java.nio.ByteBuffer;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.text.CharsBacker;
import org.miaixz.bus.core.text.TextSimilarity;

/**
 * String utility class.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class StringKit extends CharsBacker {

    /**
     * Trims whitespace from the beginning and end of all strings in the given array.
     *
     * @param args The array of strings.
     */
    public static void trim(final String[] args) {
        if (null == args) {
            return;
        }
        String text;
        for (int i = 0; i < args.length; i++) {
            text = args[i];
            if (null != text) {
                args[i] = trim(text);
            }
        }
    }

    /**
     * Converts char[] to String
     *
     * @param args The character array
     * @return The string, returns null if the given value is null
     */
    public static String string(final char[] args) {
        if (null == args) {
            return null;
        }
        return new String(args);
    }

    /**
     * Converts an object to a string.
     *
     * <pre>
     * 1. Byte arrays and ByteBuffers are converted to their corresponding string representations.
     * 2. Object arrays are converted using Arrays.toString().
     * </pre>
     *
     * @param object The object to convert.
     * @return The string representation.
     */
    public static String toString(final Object object) {
        return toString(object, Charset.UTF_8);
    }

    /**
     * Converts an object to a string.
     *
     * <pre>
     * 1. Byte arrays and ByteBuffers are converted to their corresponding string representations.
     * 2. char[] is converted directly to a new String.
     * 3. Object arrays are converted using Arrays.toString().
     * </pre>
     *
     * @param object  The object to convert.
     * @param charset The character set to use for byte-based conversions.
     * @return The string representation.
     */
    public static String toString(final Object object, final java.nio.charset.Charset charset) {
        if (null == object) {
            return null;
        }

        if (object instanceof String) {
            return (String) object;
        } else if (object instanceof char[]) {
            return new String((char[]) object);
        } else if (object instanceof byte[]) {
            return toString((byte[]) object, charset);
        } else if (object instanceof Byte[]) {
            return toString((Byte[]) object, charset);
        } else if (object instanceof ByteBuffer) {
            return toString((ByteBuffer) object, charset);
        } else if (ArrayKit.isArray(object)) {
            return ArrayKit.toString(object);
        }

        return object.toString();
    }

    /**
     * Decodes a byte array into a string.
     *
     * @param data    The byte array.
     * @param charset The character set. If null, the platform's default charset is used.
     * @return The decoded string.
     */
    public static String toString(final byte[] data, final java.nio.charset.Charset charset) {
        if (data == null) {
            return null;
        }

        if (null == charset) {
            return new String(data);
        }
        return new String(data, charset);
    }

    /**
     * Decodes an array of `Byte` objects into a string.
     *
     * @param data    The array of `Byte` objects.
     * @param charset The character set. If null, the platform's default charset is used.
     * @return The decoded string.
     */
    public static String toString(final Byte[] data, final java.nio.charset.Charset charset) {
        if (data == null) {
            return null;
        }

        final byte[] bytes = new byte[data.length];
        Byte dataByte;
        for (int i = 0; i < data.length; i++) {
            dataByte = data[i];
            bytes[i] = (null == dataByte) ? -1 : dataByte;
        }

        return toString(bytes, charset);
    }

    /**
     * Converts an encoded `ByteBuffer` to a string.
     *
     * @param data    The `ByteBuffer`.
     * @param charset The character set. If null, the system's default charset is used.
     * @return The string.
     */
    public static String toString(final ByteBuffer data, java.nio.charset.Charset charset) {
        if (null == charset) {
            charset = java.nio.charset.Charset.defaultCharset();
        }
        return charset.decode(data.duplicate()).toString();
    }

    /**
     * Creates a new `StringBuilder` object.
     *
     * @return A `StringBuilder` object.
     */
    public static StringBuilder builder() {
        return new StringBuilder();
    }

    /**
     * Creates a new `StringBuilder` object with a specified initial capacity.
     *
     * @param capacity The initial capacity.
     * @return A `StringBuilder` object.
     */
    public static StringBuilder builder(final int capacity) {
        return new StringBuilder(capacity);
    }

    /**
     * Gets a `StringReader` for a `CharSequence`.
     *
     * @param text The `CharSequence`.
     * @return A `StringReader`.
     */
    public static StringReader getReader(final CharSequence text) {
        if (null == text) {
            return null;
        }
        return new StringReader(text.toString());
    }

    /**
     * Gets a new `StringWriter`.
     *
     * @return A `StringWriter`.
     */
    public static StringWriter getWriter() {
        return new StringWriter();
    }

    /**
     * Reverses a string. Example: "abcd" becomes "dcba".
     *
     * @param text The string to reverse.
     * @return The reversed string.
     */
    public static String reverse(final String text) {
        if (isBlank(text)) {
            return text;
        }
        return new String(ArrayKit.reverse(text.toCharArray()));
    }

    /**
     * Pads a string to a specified length with a given character at the beginning. If the string is already longer than
     * the specified length, it is returned unchanged.
     *
     * @param text       The string to pad.
     * @param filledChar The character to pad with.
     * @param len        The target length.
     * @return The padded string.
     */
    public static String fillBefore(final String text, final char filledChar, final int len) {
        return fill(text, filledChar, len, true);
    }

    /**
     * Pads a string to a specified length with a given character at the end.
     *
     * @param text       The string to pad.
     * @param filledChar The character to pad with.
     * @param len        The target length.
     * @return The padded string.
     */
    public static String fillAfter(final String text, final char filledChar, final int len) {
        return fill(text, filledChar, len, false);
    }

    /**
     * Pads a string to a specified length with a given character.
     *
     * @param text       The string to pad.
     * @param filledChar The character to pad with.
     * @param len        The target length.
     * @param isPre      If true, pads at the beginning; otherwise, pads at the end.
     * @return The padded string.
     */
    public static String fill(final String text, final char filledChar, final int len, final boolean isPre) {
        final int strLen = text.length();
        if (strLen > len) {
            return text;
        }
        final String filledStr = repeat(filledChar, len - strLen);
        return isPre ? filledStr.concat(text) : text.concat(filledStr);
    }

    /**
     * Creates a string by repeating a character a specified number of times.
     *
     * @param count   The number of times to repeat.
     * @param charVal The character.
     * @return The resulting string.
     */
    public static String fill(int count, char charVal) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be greater than or equal to 0.");
        }
        char[] chs = new char[count];
        for (int i = 0; i < count; i++) {
            chs[i] = charVal;
        }
        return new String(chs);
    }

    /**
     * Creates a string by repeating a string a specified number of times.
     *
     * @param count  The number of times to repeat.
     * @param strVal The string.
     * @return The resulting string.
     */
    public static String fill(int count, String strVal) {
        if (count < 0) {
            throw new IllegalArgumentException("count must be greater than or equal to 0.");
        }
        StringBuilder sb = new StringBuilder(count * strVal.length());
        for (int i = 0; i < count; i++) {
            sb.append(strVal);
        }
        return sb.toString();
    }

    /**
     * Calculates the similarity between two strings.
     *
     * @param str1 The first string.
     * @param str2 The second string.
     * @return The similarity score.
     */
    public static double similar(final String str1, final String str2) {
        return TextSimilarity.similar(str1, str2);
    }

    /**
     * Calculates the similarity between two strings as a percentage.
     *
     * @param str1  The first string.
     * @param str2  The second string.
     * @param scale The number of decimal places for the percentage.
     * @return The similarity percentage string.
     */
    public static String similar(final String str1, final String str2, final int scale) {
        return TextSimilarity.similar(str1, str2, scale);
    }

    /**
     * Checks if a `String` contains actual text.
     *
     * @param text The `String` to check (may be `null`).
     * @return `true` if the `String` is not `null`, has a length greater than 0, and contains at least one
     *         non-whitespace character.
     */
    public static boolean hasText(String text) {
        if (null == text || text.isEmpty()) {
            return false;
        }
        int strLen = text.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if all strings in an array contain actual text.
     *
     * @param text The array of strings to check.
     * @return `true` if all strings contain text.
     */
    public static boolean hasText(String... text) {
        for (String string : text) {
            if (!hasText(string)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Builds a new string by combining parts of an original string with a middle part.
     *
     * @param original     The original object (will be converted to a string).
     * @param middle       The middle part to insert.
     * @param prefixLength The length of the prefix to take from the original string.
     * @return The newly constructed string.
     */
    public static String build(final Object original, final String middle, final int prefixLength) {
        if (ObjectKit.isNull(original)) {
            return null;
        }
        final String string = original.toString();
        final int stringLength = string.length();
        String prefix;
        if (stringLength >= prefixLength) {
            prefix = string.substring(0, prefixLength);
        } else {
            prefix = string.substring(0, stringLength);
        }
        String suffix = Normal.EMPTY;
        int suffixLength = stringLength - prefix.length() - middle.length();
        if (suffixLength > 0) {
            suffix = string.substring(stringLength - suffixLength);
        }
        return prefix + middle + suffix;
    }

    /**
     * Capitalizes the first character of a string.
     *
     * <pre>
     * StringKit.capitalize(null)  = null
     * StringKit.capitalize("")    = ""
     * StringKit.capitalize("cat") = "Cat"
     * StringKit.capitalize("cAt") = "CAt"
     * </pre>
     *
     * @param text The string to capitalize.
     * @return The capitalized string, or `null` if the input is `null`.
     */
    public static String capitalize(final String text) {
        int strLen;
        if (null == text || (strLen = text.length()) == 0) {
            return text;
        }
        final int firstCodepoint = text.codePointAt(0);
        final int newCodePoint = Character.toTitleCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            return text;
        }
        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint;
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen;) {
            final int codepoint = text.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint;
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    /**
     * Uncapitalizes the first character of a string.
     *
     * <pre>
     * StringKit.uncapitalize(null)  = null
     * StringKit.uncapitalize("")    = ""
     * StringKit.uncapitalize("Cat") = "cat"
     * StringKit.uncapitalize("CAT") = "cAT"
     * </pre>
     *
     * @param text The string to uncapitalize.
     * @return The uncapitalized string, or `null` if the input is `null`.
     */
    public static String unCapitalize(final String text) {
        int strLen;
        if (null == text || (strLen = text.length()) == 0) {
            return text;
        }
        final int firstCodepoint = text.codePointAt(0);
        final int newCodePoint = Character.toLowerCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            return text;
        }
        final int[] newCodePoints = new int[strLen];
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint;
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen;) {
            final int codepoint = text.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint;
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    /**
     * Reverses a string.
     * <p>
     * This method reverses the string by Unicode code points, correctly handling supplementary characters (such as
     * emojis, combining marks, and other multi-char Unicode sequences) to ensure they are not split incorrectly.
     * <p>
     * {@code "abcd"} → {@code "dcba"}<br>
     * {@code "abc"} → {@code "cba"}<br>
     *
     * @param text the string to be reversed
     * @return the reversed string; returns {@code null} if the input is {@code null}
     */
    public static String reverseByCodePoint(String text) {
        if (null == text) {
            return null;
        }

        // Perform reversal by Unicode code point
        final StringBuilder result = new StringBuilder();
        for (int i = text.length(); i > 0;) {
            // Get the code point before the specified position
            final int codePoint = text.codePointBefore(i);
            // Adjust the index based on the number of chars occupied by the code point
            i -= Character.charCount(codePoint);
            // Append the code point to the result
            result.appendCodePoint(codePoint);
        }

        return result.toString();
    }

}
