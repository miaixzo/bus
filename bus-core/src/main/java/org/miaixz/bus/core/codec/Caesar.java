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
package org.miaixz.bus.core.codec;

import org.miaixz.bus.core.lang.Assert;

/**
 * Implementation of the Caesar cipher. The Caesar cipher is one of the simplest and most widely known encryption
 * techniques. It is a type of substitution cipher in which each letter in the plaintext is replaced by a letter some
 * fixed number of positions down the alphabet. Algorithm source: <a href=
 * "https://github.com/zhaorenjie110/SymmetricEncryptionAndDecryption">https://github.com/zhaorenjie110/SymmetricEncryptionAndDecryption</a>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class Caesar {

    /**
     * Constructs a new Caesar. Utility class constructor for static access.
     */
    private Caesar() {
    }

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
