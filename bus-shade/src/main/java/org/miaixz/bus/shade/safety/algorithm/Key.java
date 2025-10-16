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
package org.miaixz.bus.shade.safety.algorithm;

/**
 * Represents a cryptographic key, providing essential information about the key, including its algorithm, size,
 * associated password, and the actual key material for encryption and decryption.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public interface Key {

    /**
     * Retrieves the name of the cryptographic algorithm used with this key.
     *
     * @return The algorithm name (e.g., "AES", "RSA").
     */
    String getAlgorithm();

    /**
     * Retrieves the size of the key in bits.
     *
     * @return The key size.
     */
    int getKeysize();

    /**
     * Retrieves the size of the initialization vector (IV) in bits. This is primarily applicable to symmetric
     * encryption algorithms that use IVs.
     *
     * @return The IV size.
     */
    int getIvsize();

    /**
     * Retrieves the password associated with this key, if any. This might be used for password-based key derivation
     * functions.
     *
     * @return The password string, or {@code null} if no password is associated.
     */
    String getPassword();

    /**
     * Retrieves the raw byte array of the key material used for encryption.
     *
     * @return The encryption key as a byte array.
     */
    byte[] getEncryptKey();

    /**
     * Retrieves the raw byte array of the key material used for decryption.
     *
     * @return The decryption key as a byte array.
     */
    byte[] getDecryptKey();

    /**
     * Retrieves the raw byte array of the initialization vector (IV) parameter. This is typically used in symmetric
     * block ciphers to ensure unique ciphertext for identical plaintexts.
     *
     * @return The IV parameter as a byte array, or {@code null} if not applicable.
     */
    byte[] getIvParameter();

}
