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
