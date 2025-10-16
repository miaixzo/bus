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
package org.miaixz.bus.core.codec;

import org.miaixz.bus.core.lang.Assert;

/**
 * Implementation of the Caesar cipher. The Caesar cipher is one of the simplest and most widely known encryption
 * techniques. It is a type of substitution cipher in which each letter in the plaintext is replaced by a letter some
 * fixed number of positions down the alphabet. Algorithm source: <a href=
 * "https://github.com/zhaorenjie110/SymmetricEncryptionAndDecryption">https://github.com/zhaorenjie110/SymmetricEncryptionAndDecryption</a>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Caesar {

    /**
     * The alphabet table used for Caesar cipher, containing both lowercase and uppercase English letters. The order is
     * important for shifting operations.
     */
    public static final String TABLE = "AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz";

    /**
     * Encodes the given message using the Caesar cipher with a specified offset. Only letter characters are shifted;
     * non-letter characters remain unchanged.
     *
     * @param message The plaintext message to be encrypted. Must not be null.
     * @param offset  The number of positions to shift each letter.
     * @return The encrypted ciphertext.
     * @throws NullPointerException if the message is null.
     */
    public static String encode(final String message, final int offset) {
        Assert.notNull(message, "message must be not null!");
        final int len = message.length();
        final char[] plain = message.toCharArray();
        char c;
        for (int i = 0; i < len; i++) {
            c = message.charAt(i);
            if (!Character.isLetter(c)) {
                continue;
            }
            plain[i] = encodeChar(c, offset);
        }
        return new String(plain);
    }

    /**
     * Decodes the given ciphertext using the Caesar cipher with a specified offset. Only letter characters are shifted
     * back; non-letter characters remain unchanged.
     *
     * @param cipherText The ciphertext message to be decrypted. Must not be null.
     * @param offset     The number of positions used for encryption (to shift back).
     * @return The decrypted plaintext message.
     * @throws NullPointerException if the cipherText is null.
     */
    public static String decode(final String cipherText, final int offset) {
        Assert.notNull(cipherText, "cipherText must be not null!");
        final int len = cipherText.length();
        final char[] plain = cipherText.toCharArray();
        char c;
        for (int i = 0; i < len; i++) {
            c = cipherText.charAt(i);
            if (!Character.isLetter(c)) {
                continue;
            }
            plain[i] = decodeChar(c, offset);
        }
        return new String(plain);
    }

    /**
     * Encrypts a single character using the Caesar cipher. The character's position in the {@link #TABLE} is shifted by
     * the given offset.
     *
     * @param c      The character to be encrypted.
     * @param offset The shift offset.
     * @return The encrypted character.
     */
    private static char encodeChar(final char c, final int offset) {
        final int position = (TABLE.indexOf(c) + offset) % 52;
        return TABLE.charAt(position);

    }

    /**
     * Decrypts a single character using the Caesar cipher. The character's position in the {@link #TABLE} is shifted
     * back by the given offset. Handles negative results from the modulo operation to ensure a valid index.
     *
     * @param c      The character to be decrypted.
     * @param offset The shift offset used during encryption.
     * @return The decrypted character.
     */
    private static char decodeChar(final char c, final int offset) {
        int position = (TABLE.indexOf(c) - offset) % 52;
        if (position < 0) {
            position += 52;
        }
        return TABLE.charAt(position);
    }

}
