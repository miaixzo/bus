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
