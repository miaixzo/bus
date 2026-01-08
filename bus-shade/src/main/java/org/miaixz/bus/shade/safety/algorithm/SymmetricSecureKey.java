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

import java.io.Serial;

/**
 * Represents a symmetric secure key, extending {@link SecureKey} and implementing {@link SymmetricKey}. This class
 * holds the secret key and initialization vector (IV) for symmetric encryption schemes.
 *
 * @author Kimi Liu
 * @since Java 17+
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
