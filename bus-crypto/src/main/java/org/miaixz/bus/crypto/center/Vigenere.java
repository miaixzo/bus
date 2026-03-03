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
package org.miaixz.bus.crypto.center;

/**
 * Implementation of the Vigenere cipher. The Vigenere cipher is a polyalphabetic substitution cipher that extends the
 * Caesar cipher. Algorithm implementation from: https://github.com/zhaorenjie110/SymmetricEncryptionAndDecryption
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Vigenere {

    /**
     * Encrypts the data.
     *
     * @param data      The data to encrypt.
     * @param cipherKey The key.
     * @return The ciphertext.
     */
    public static String encrypt(final CharSequence data, final CharSequence cipherKey) {
        final int dataLen = data.length();
        final int cipherKeyLen = cipherKey.length();

        final char[] cipherArray = new char[dataLen];
        for (int i = 0; i < dataLen / cipherKeyLen + 1; i++) {
            for (int t = 0; t < cipherKeyLen; t++) {
                if (t + i * cipherKeyLen < dataLen) {
                    final char dataChar = data.charAt(t + i * cipherKeyLen);
                    final char cipherKeyChar = cipherKey.charAt(t);
                    cipherArray[t + i * cipherKeyLen] = (char) ((dataChar + cipherKeyChar - 64) % 95 + 32);
                }
            }
        }

        return String.valueOf(cipherArray);
    }

    /**
     * Decrypts the data.
     *
     * @param data      The ciphertext.
     * @param cipherKey The key.
     * @return The plaintext.
     */
    public static String decrypt(final CharSequence data, final CharSequence cipherKey) {
        final int dataLen = data.length();
        final int cipherKeyLen = cipherKey.length();

        final char[] clearArray = new char[dataLen];
        for (int i = 0; i < dataLen; i++) {
            for (int t = 0; t < cipherKeyLen; t++) {
                if (t + i * cipherKeyLen < dataLen) {
                    final char dataChar = data.charAt(t + i * cipherKeyLen);
                    final char cipherKeyChar = cipherKey.charAt(t);
                    if (dataChar - cipherKeyChar >= 0) {
                        clearArray[t + i * cipherKeyLen] = (char) ((dataChar - cipherKeyChar) % 95 + 32);
                    } else {
                        clearArray[t + i * cipherKeyLen] = (char) ((dataChar - cipherKeyChar + 95) % 95 + 32);
                    }
                }
            }
        }
        return String.valueOf(clearArray);
    }

}
