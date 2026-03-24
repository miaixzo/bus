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

import java.io.Serial;

/**
 * Represents a symmetric secure key, extending {@link SecureKey} and implementing {@link SymmetricKey}. This class
 * holds the secret key and initialization vector (IV) for symmetric encryption schemes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class SymmetricSecureKey extends SecureKey implements SymmetricKey {

    @Serial
    private static final long serialVersionUID = -1L;

    private final byte[] secretKey;
    private final byte[] iv;

    /**
     * Constructs a {@code SymmetricSecureKey} with the specified algorithm, key sizes, password, secret key, and IV.
     *
     * @param algorithm The name of the algorithm used for this key.
     * @param keysize   The size of the key in bits.
     * @param ivsize    The size of the initialization vector (IV) in bits.
     * @param password  The password associated with the key, if any.
     * @param key       The secret key component as a byte array.
     * @param iv        The initialization vector as a byte array.
     */
    public SymmetricSecureKey(String algorithm, int keysize, int ivsize, String password, byte[] key, byte[] iv) {
        super(algorithm, keysize, ivsize, password);
        this.secretKey = key;
        this.iv = iv;
    }

    /**
     * Retrieves the key used for encryption, which is the secret key in symmetric cryptography.
     *
     * @return The secret key as a byte array.
     */
    public byte[] getEncryptKey() {
        return secretKey;
    }

    /**
     * Retrieves the key used for decryption, which is the secret key in symmetric cryptography.
     *
     * @return The secret key as a byte array.
     */
    public byte[] getDecryptKey() {
        return secretKey;
    }

    /**
     * Retrieves the secret key component of this symmetric key.
     *
     * @return The secret key as a byte array.
     */
    public byte[] getSecretKey() {
        return secretKey;
    }

    /**
     * Retrieves the initialization vector (IV) parameter.
     *
     * @return The IV as a byte array.
     */
    public byte[] getIvParameter() {
        return iv;
    }

}
