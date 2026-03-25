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
 * Represents an asymmetric secure key, extending {@link SecureKey} and implementing {@link AsymmetricKey}. This class
 * holds the public and private key components for asymmetric encryption schemes.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class AsymmetricSecureKey extends SecureKey implements AsymmetricKey {

    private final byte[] publicKey;
    private final byte[] privateKey;

    /**
     * Constructs an {@code AsymmetricSecureKey} with the specified algorithm, key sizes, password, and key components.
     *
     * @param algorithm  The name of the algorithm used for this key.
     * @param keysize    The size of the key in bits.
     * @param ivsize     The size of the initialization vector (IV) in bits. (Not applicable for asymmetric keys,
     *                   usually 0 or ignored).
     * @param password   The password associated with the key, if any.
     * @param publicKey  The public key component as a byte array.
     * @param privateKey The private key component as a byte array.
     */
    public AsymmetricSecureKey(String algorithm, int keysize, int ivsize, String password, byte[] publicKey,
            byte[] privateKey) {
        super(algorithm, keysize, ivsize, password);
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * Retrieves the key used for encryption, which is the public key in asymmetric cryptography.
     *
     * @return The public key as a byte array.
     */
    public byte[] getEncryptKey() {
        return publicKey;
    }

    /**
     * Retrieves the key used for decryption, which is the private key in asymmetric cryptography.
     *
     * @return The private key as a byte array.
     */
    public byte[] getDecryptKey() {
        return privateKey;
    }

    /**
     * Retrieves the public key component of this asymmetric key.
     *
     * @return The public key as a byte array.
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * Retrieves the private key component of this asymmetric key.
     *
     * @return The private key as a byte array.
     */
    public byte[] getPrivateKey() {
        return privateKey;
    }

    /**
     * Returns the initialization vector (IV) parameter. For asymmetric keys, this is typically {@code null}.
     *
     * @return {@code null} as IVs are not typically used with asymmetric keys.
     */
    public byte[] getIvParameter() {
        return null;
    }

}
