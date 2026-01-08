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
package org.miaixz.bus.shade.safety.algorithm;

/**
 * Represents an asymmetric secure key, extending {@link SecureKey} and implementing {@link AsymmetricKey}. This class
 * holds the public and private key components for asymmetric encryption schemes.
 *
 * @author Kimi Liu
 * @since Java 17+
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
