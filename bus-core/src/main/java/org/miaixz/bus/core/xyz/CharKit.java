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
package org.miaixz.bus.core.xyz;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;

import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.text.ASCIIStrCache;

/**
 * Character utility class. Some methods are inspired by Apache Commons Lang.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CharKit {

    /**
     * Checks if the character is an ASCII character (0-127).
     *
     * <pre>
     * CharKit.isAscii('a')  = true
     * CharKit.isAscii('A')  = true
     * CharKit.isAscii('3')  = true
     * CharKit.isAscii('-')  = true
     * CharKit.isAscii('\n') = true
     * CharKit.isAscii('&copy;') = false
     * </pre>
     *
     * @param ch The character to check.
     * @return {@code true} if the character is an ASCII character (0-127).
     */
    public static boolean isAscii(final char ch) {
        return ch < 128;
    }

    /**
     * Checks if the character is a printable ASCII character (32-126).
     *
     * <pre>
     * CharKit.isAsciiPrintable('a')  = true
     * CharKit.isAsciiPrintable('A')  = true
     * CharKit.isAsciiPrintable('3')  = true
     * CharKit.isAsciiPrintable('-')  = true
     * CharKit.isAsciiPrintable('\n') = false
     * CharKit.isAsciiPrintable('&copy;') = false
     * </pre>
     *
     * @param ch The character to check.
     * @return {@code true} if the character is a printable ASCII character (32-126).
     */
    public static boolean isAsciiPrintable(final char ch) {
        return ch >= 32 && ch < 127;
    }

    /**
     * Checks if the character is an ASCII control character (0-31 and 127).
     *
     * <pre>
     * CharKit.isAsciiControl('a')  = false
     * CharKit.isAsciiControl('A')  = false
     * CharKit.isAsciiControl('3')  = false
     * CharKit.isAsciiControl('-')  = false
     * CharKit.isAsciiControl('\n') = true
     * CharKit.isAsciiControl('&copy;') = false
     * </pre>
     *
     * @param ch The character to check.
     * @return {@code true} if the character is a control character (0-31 and 127).
     */
    public static boolean isAsciiControl(final char ch) {
        return ch < 32 || ch == 127;
    }

    /**
     * Checks if the character is a letter (a-z, A-Z).
     *
     * <pre>
     * CharKit.isLetter('a')  = true
     * CharKit.isLetter('A')  = true
     * CharKit.isLetter('3')  = false
     * CharKit.isLetter('-')  = false
     * CharKit.isLetter('\n') = false
     * CharKit.isLetter('&copy;') = false
     * </pre>
     *
     * @param ch The character to check.
     * @return {@code true} if the character is a letter (a-z, A-Z).
     */
    public static boolean isLetter(final char ch) {
        return isLetterUpper(ch) || isLetterLower(ch);
    }

    /**
     * Checks if the character is an uppercase letter (A-Z).
     *
     * <pre>
     * CharKit.isLetterUpper('a')  = false
     * CharKit.isLetterUpper('A')  = true
     * CharKit.isLetterUpper('3')  = false
     * CharKit.isLetterUpper('-')  = false
     * CharKit.isLetterUpper('\n') = false
     * CharKit.isLetterUpper('&copy;') = false
     * </pre>
     *
     * @param ch The character to check.
     * @return {@code true} if the character is an uppercase letter (A-Z).
     */
    public static boolean isLetterUpper(final char ch) {
        return ch >= 'A' && ch <= 'Z';
    }

    /**
     * Checks if the character is a lowercase letter (a-z).
     *
     * <pre>
     * CharKit.isLetterLower('a')  = true
     * CharKit.isLetterLower('A')  = false
     * CharKit.isLetterLower('3')  = false
     * CharKit.isLetterLower('-')  = false
     * CharKit.isLetterLower('\n') = false
     * CharKit.isLetterLower('&copy;') = false
     * </pre>
     *
     * @param ch The character to check.
     * @return {@code true} if the character is a lowercase letter (a-z).
     */
    public static boolean isLetterLower(final char ch) {
        return ch >= 'a' && ch <= 'z';
    }

    /**
     * Checks if the character is a digit (0-9).
     *
     * <pre>
     * CharKit.isNumber('a')  = false
     * CharKit.isNumber('A')  = false
     * CharKit.isNumber('3')  = true
     * CharKit.isNumber('-')  = false
     * CharKit.isNumber('\n') = false
     * CharKit.isNumber('&copy;') = false
     * </pre>
     *
     * @param ch The character to check.
     * @return {@code true} if the character is a digit (0-9).
     */
    public static boolean isNumber(final char ch) {
        return ch >= '0' && ch <= '9';
    }

    /**
     * Checks if the character is a valid hexadecimal character.
     * <p>
     * A valid hex character is one of:
     * 
     * <pre>
     * 1. 0-9
     * 2. a-f
     * 3. A-F
     * </pre>
     *
     * @param c The character to check.
     * @return {@code true} if the character is a valid hexadecimal character.
     */
    public static boolean isHexChar(final char c) {
        return isNumber(c) || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    /**
     * Checks if the character is a letter or a digit.
     *
     * <pre>
     * CharKit.isLetterOrNumber('a')  = true
     * CharKit.isLetterOrNumber('A')  = true
     * CharKit.isLetterOrNumber('3')  = true
     * CharKit.isLetterOrNumber('-')  = false
     * CharKit.isLetterOrNumber('\n') = false
     * CharKit.isLetterOrNumber('&copy;') = false
     * </pre>
     *
     * @param ch The character to check.
     * @return {@code true} if the character is a letter or a digit.
     */
    public static boolean isLetterOrNumber(final char ch) {
        return isLetter(ch) || isNumber(ch);
    }

    /**
     * Converts a character to a String. Uses a cache for ASCII characters.
     *
     * @param c The character.
     * @return A String containing the character.
     * @see ASCIIStrCache#toString(char)
     */
    public static String toString(final char c) {
        return ASCIIStrCache.toString(c);
    }

    /**
     * Checks if the given class is a character class, which includes:
     *
     * <pre>
     * Character.class
     * char.class
     * </pre>
     *
     * @param clazz The class to check.
     * @return {@code true} if it is a character class.
     */
    public static boolean isCharClass(final Class<?> clazz) {
        return clazz == Character.class || clazz == char.class;
    }

    /**
     * Checks if the given object is a character type, which includes:
     *
     * <pre>
     * Character.class
     * char.class
     * </pre>
     *
     * @param value The object to check.
     * @return {@code true} if the object is a character type.
     */
    public static boolean isChar(final Object value) {
        return value instanceof Character || value.getClass() == char.class;
    }

    /**
     * Checks if the character is a blank character (whitespace, tab, etc.).
     *
     * @param c The character.
     * @return {@code true} if it is a blank character.
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     */
    public static boolean isBlankChar(final char c) {
        return isBlankChar((int) c);
    }

    /**
     * Checks if the character is a blank character (whitespace, tab, etc.).
     *
     * @param c The character code point.
     * @return {@code true} if it is a blank character.
     * @see Character#isWhitespace(int)
     * @see Character#isSpaceChar(int)
     */
    public static boolean isBlankChar(final int c) {
        return Character.isWhitespace(c) || Character.isSpaceChar(c) || c == '\ufeff' || c == '\u202a' || c == '\u0000'
        // Hangul Filler
                || c == '\u3164'
                // Braille Pattern Blank
                || c == '\u2800'
                // Zero Width Non-Joiner, ZWNJ
                || c == '\u200c'
                // MONGOLIAN VOWEL SEPARATOR
                || c == '\u180e';
    }

    /**
     * Checks if the character is an emoji.
     *
     * @param c The character.
     * @return {@code true} if it is an emoji.
     */
    public static boolean isEmoji(final char c) {
        // A simple check for common emoji ranges. This is not exhaustive.
        return !((c == 0x0) || (c == 0x9) || (c == 0xA) || (c == 0xD) || ((c >= 0x20) && (c <= 0xD7FF))
                || ((c >= 0xE000) && (c <= 0xFFFD)) || ((c >= 0x100000) && (c <= 0x10FFFF)));
    }

    /**
     * Checks if the character is a file separator for Windows ('\') or Unix ('/').
     *
     * @param c The character.
     * @return {@code true} if it is a file separator.
     */
    public static boolean isFileSeparator(final char c) {
        return Symbol.C_SLASH == c || Symbol.C_BACKSLASH == c;
    }

    /**
     * Checks if the character is a zero-width character.
     *
     * @param c The character to check.
     * @return {@code true} if the character is a zero-width character.
     */
    public static boolean isZeroWidthChar(final char c) {
        switch (c) {
            case '\u200B': // Zero-width space
            case '\u200C': // Zero-width non-joiner
            case '\u200D': // Zero-width joiner
            case '\uFEFF': // Zero-width no-break space (Byte Order Mark)
            case '\u2060': // Word joiner
            case '\u2063': // Invisible separator
            case '\u2064': // Invisible plus
            case '\u2065': // Invisible separator
                return true;

            default:
                return false;
        }
    }

    /**
     * Compares two characters for equality.
     *
     * @param c1              The first character.
     * @param c2              The second character.
     * @param caseInsensitive If {@code true}, comparison is case-insensitive.
     * @return {@code true} if the characters are equal.
     */
    public static boolean equals(final char c1, final char c2, final boolean caseInsensitive) {
        if (caseInsensitive) {
            return Character.toLowerCase(c1) == Character.toLowerCase(c2);
        }
        return c1 == c2;
    }

    /**
     * Gets the general category of a character.
     *
     * @param c The character (code point).
     * @return The character type.
     * @see Character#getType(int)
     */
    public static int getType(final int c) {
        return Character.getType(c);
    }

    /**
     * Gets the hexadecimal numeric value of a character.
     *
     * @param c The character (code point).
     * @return The integer value of the hex digit.
     */
    public static int digit16(final int c) {
        return Character.digit(c, 16);
    }

    /**
     * Converts a letter or digit to its enclosed (circled) form.
     * <p>
     * Examples:
     * 
     * <pre>
     * '1' - '①'
     * 'A' - 'Ⓐ'
     * 'a' - 'ⓐ'
     * </pre>
     *
     * @param c The character to convert. If the character cannot be converted, it is returned unchanged.
     * @return The converted character.
     * @see <a href="https://en.wikipedia.org/wiki/Enclosed_Alphanumerics">Enclosed Alphanumerics</a>
     */
    public static char toCloseChar(final char c) {
        int result = c;
        if (c >= '1' && c <= '9') {
            result = '①' + c - '1';
        } else if (c >= 'A' && c <= 'Z') {
            result = 'Ⓐ' + c - 'A';
        } else if (c >= 'a' && c <= 'z') {
            result = 'ⓐ' + c - 'a';
        }
        return (char) result;
    }

    /**
     * Converts a number from 1 to 20 to its enclosed (circled) character form. Also known as Enclosed Alphanumerics.
     * <p>
     * Examples:
     * 
     * <pre>
     * 1 - '①'
     * 12 - '⑫'
     * 20 - '⑳'
     * </pre>
     *
     * @param number The number to convert (must be between 1 and 20).
     * @return The converted character.
     * @see <a href="https://en.wikipedia.org/wiki/Enclosed_Alphanumerics">Enclosed Alphanumerics</a>
     */
    public static char toCloseByNumber(final int number) {
        if (number < 1 || number > 20) {
            throw new IllegalArgumentException("Number must be between 1 and 20.");
        }
        return (char) ('①' + number - 1);
    }

    /**
     * Converts a byte array to a char array using the UTF-8 charset.
     *
     * @param bytes The byte array.
     * @return The resulting char array.
     */
    public static char[] getChars(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes).flip();
        CharBuffer cb = Charset.UTF_8.decode(bb);
        return cb.array();
    }

}
