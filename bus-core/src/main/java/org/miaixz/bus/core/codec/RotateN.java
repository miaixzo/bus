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
package org.miaixz.bus.core.codec;

import org.miaixz.bus.core.lang.Assert;

/**
 * RotN (rotate by N places) cipher, a simple substitution cipher and a variant of the Caesar cipher developed in
 * ancient Rome. This cipher shifts each letter in the plaintext by a certain number of places down or up the alphabet.
 * Code adapted from: <a href="https://github.com/orclight/jencrypt">https://github.com/orclight/jencrypt</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RotateN {

    /**
     * Represents the character 'a', used as a reference for lowercase letters.
     */
    private static final char aCHAR = 'a';
    /**
     * Represents the character 'z', used as a reference for lowercase letters.
     */
    private static final char zCHAR = 'z';
    /**
     * Represents the character 'A', used as a reference for uppercase letters.
     */
    private static final char ACHAR = 'A';
    /**
     * Represents the character 'Z', used as a reference for uppercase letters.
     */
    private static final char ZCHAR = 'Z';
    /**
     * Represents the character '0', used as a reference for digits.
     */
    private static final char CHAR0 = '0';
    /**
     * Represents the character '9', used as a reference for digits.
     */
    private static final char CHAR9 = '9';

    /**
     * Encodes the given message using the RotateN-13 cipher, including digits. This is a specific case of the
     * {@link #encode(String, int, boolean)} method with an offset of 13 and {@code isEncodeNumber} set to {@code true}.
     *
     * @param message The message to be encoded.
     * @return The encoded string.
     */
    public static String encode13(final String message) {
        return encode13(message, true);
    }

    /**
     * Encodes the given message using the RotateN-13 cipher. This is a specific case of the
     * {@link #encode(String, int, boolean)} method with an offset of 13.
     *
     * @param message        The message to be encoded.
     * @param isEncodeNumber A boolean indicating whether digits should also be encoded.
     * @return The encoded string.
     */
    public static String encode13(final String message, final boolean isEncodeNumber) {
        return encode(message, 13, isEncodeNumber);
    }

    /**
     * Encodes the given message using the RotN cipher with a specified offset. Letters (both uppercase and lowercase)
     * and optionally digits are shifted by the given offset.
     *
     * @param message        The message to be encoded.
     * @param offset         The number of positions to rotate (shift) each character. A common value is 13.
     * @param isEncodeNumber A boolean indicating whether digits should also be encoded.
     * @return The encoded string.
     */
    public static String encode(final String message, final int offset, final boolean isEncodeNumber) {
        final int len = message.length();
        final char[] chars = new char[len];

        for (int i = 0; i < len; i++) {
            chars[i] = encodeChar(message.charAt(i), offset, isEncodeNumber);
        }
        return new String(chars);
    }

    /**
     * Decodes the given ciphertext using the RotateN-13 cipher, including digits. This is a specific case of the
     * {@link #decode(String, int, boolean)} method with an offset of 13 and {@code isDecodeNumber} set to {@code true}.
     *
     * @param rot The ciphertext to be decoded.
     * @return The decoded string (plaintext).
     */
    public static String decode13(final String rot) {
        return decode13(rot, true);
    }

    /**
     * Decodes the given ciphertext using the RotateN-13 cipher. This is a specific case of the
     * {@link #decode(String, int, boolean)} method with an offset of 13.
     *
     * @param rot            The ciphertext to be decoded.
     * @param isDecodeNumber A boolean indicating whether digits should also be decoded.
     * @return The decoded string (plaintext).
     */
    public static String decode13(final String rot, final boolean isDecodeNumber) {
        return decode(rot, 13, isDecodeNumber);
    }

    /**
     * Decodes the given ciphertext using the RotN cipher with a specified offset. Letters (both uppercase and
     * lowercase) and optionally digits are shifted back by the given offset.
     *
     * @param rot            The ciphertext to be decoded. Must not be null.
     * @param offset         The number of positions used for encoding (to shift back). A common value is 13.
     * @param isDecodeNumber A boolean indicating whether digits should also be decoded.
     * @return The decoded string (plaintext).
     * @throws NullPointerException if the input {@code rot} is null.
     */
    public static String decode(final String rot, final int offset, final boolean isDecodeNumber) {
        Assert.notNull(rot, "rot must not be null");
        final int len = rot.length();
        final char[] chars = new char[len];

        for (int i = 0; i < len; i++) {
            chars[i] = decodeChar(rot.charAt(i), offset, isDecodeNumber);
        }
        return new String(chars);
    }

    /**
     * Encodes a single character using the RotN cipher. If {@code isEncodeNumber} is true, digits are rotated within
     * '0'-'9'. Uppercase letters are rotated within 'A'-'Z'. Lowercase letters are rotated within 'a'-'z'. Other
     * characters remain unchanged.
     *
     * @param c              The character to be encoded.
     * @param offset         The number of positions to rotate the character.
     * @param isEncodeNumber A boolean indicating whether digits should be encoded.
     * @return The encoded character.
     */
    private static char encodeChar(char c, final int offset, final boolean isEncodeNumber) {
        if (isEncodeNumber) {
            if (c >= CHAR0 && c <= CHAR9) {
                c -= CHAR0;
                c = (char) ((c + offset) % 10);
                c += CHAR0;
            }
        }

        // A == 65, Z == 90
        if (c >= ACHAR && c <= ZCHAR) {
            c -= ACHAR;
            c = (char) ((c + offset) % 26);
            c += ACHAR;
        }
        // a == 97, z == 122.
        else if (c >= aCHAR && c <= zCHAR) {
            c -= aCHAR;
            c = (char) ((c + offset) % 26);
            c += aCHAR;
        }
        return c;
    }

    /**
     * Decodes a single character using the RotN cipher. If {@code isDecodeNumber} is true, digits are rotated back
     * within '0'-'9'. Uppercase letters are rotated back within 'A'-'Z'. Lowercase letters are rotated back within
     * 'a'-'z'. Handles negative results from the modulo operation to ensure correct character wrapping. Other
     * characters remain unchanged.
     *
     * @param c              The character to be decoded.
     * @param offset         The number of positions used for encoding (to shift back).
     * @param isDecodeNumber A boolean indicating whether digits should be decoded.
     * @return The decoded character.
     */
    private static char decodeChar(final char c, final int offset, final boolean isDecodeNumber) {
        int temp = c;
        // if converting numbers is enabled
        if (isDecodeNumber) {
            if (temp >= CHAR0 && temp <= CHAR9) {
                temp -= CHAR0;
                temp = temp - offset;
                while (temp < 0) {
                    temp += 10;
                }
                temp += CHAR0;
            }
        }

        // A == 65, Z == 90
        if (temp >= ACHAR && temp <= ZCHAR) {
            temp -= ACHAR;

            temp = temp - offset;
            while (temp < 0) {
                temp = 26 + temp;
            }
            temp += ACHAR;
        } else if (temp >= aCHAR && temp <= zCHAR) {
            temp -= aCHAR;

            temp = temp - offset;
            if (temp < 0)
                temp = 26 + temp;

            temp += aCHAR;
        }
        return (char) temp;
    }

}
