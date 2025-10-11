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
package org.miaixz.bus.crypto.center;

import java.io.Serial;
import java.security.KeyPair;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.crypto.builtin.asymmetric.Crypto;

/**
 * ECIES (Elliptic Curve Integrated Encryption Scheme) implementation. ECIES is a hybrid encryption scheme that provides
 * confidentiality, data integrity, and authentication.
 * <p>
 * Detailed introduction: <a href=
 * "https://blog.csdn.net/baidu_26954729/article/details/90437344">https://blog.csdn.net/baidu_26954729/article/details/90437344</a>
 * </p>
 * <p>
 * Note: This algorithm requires the Bouncy Castle library to be included.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ECIES extends Crypto {

    @Serial
    private static final long serialVersionUID = 2852289857959L;

    /**
     * Constructs an ECIES encryptor/decryptor, generating a new private-public key pair. Uses the default ECIES
     * algorithm.
     */
    public ECIES() {
        super(Algorithm.ECIES.getValue());
    }

    /**
     * Constructs an ECIES encryptor/decryptor with a custom ECIES algorithm.
     *
     * @param algorithm The custom ECIES algorithm, e.g., "ECIESwithDESede/NONE/PKCS7Padding".
     */
    public ECIES(final String algorithm) {
        super(algorithm);
    }

    /**
     * Constructs an ECIES encryptor/decryptor with private and public keys provided as Hex or Base64 encoded strings.
     * If both private and public keys are {@code null}, a new key pair will be generated. If only one key is provided,
     * the ECIES object can only be used for operations corresponding to that key. Uses the default ECIES algorithm.
     *
     * @param privateKey The private key as a Hex or Base64 encoded string.
     * @param publicKey  The public key as a Hex or Base64 encoded string.
     */
    public ECIES(final String privateKey, final String publicKey) {
        super(Algorithm.ECIES.getValue(), privateKey, publicKey);
    }

    /**
     * Constructs an ECIES encryptor/decryptor with private and public keys provided as byte arrays. If both private and
     * public keys are {@code null}, a new key pair will be generated. If only one key is provided, the ECIES object can
     * only be used for operations corresponding to that key. Uses the default ECIES algorithm.
     *
     * @param privateKey The private key as a byte array.
     * @param publicKey  The public key as a byte array.
     */
    public ECIES(final byte[] privateKey, final byte[] publicKey) {
        super(Algorithm.ECIES.getValue(), privateKey, publicKey);
    }

    /**
     * Constructs an ECIES encryptor/decryptor with a custom ECIES algorithm and private/public keys provided as Hex or
     * Base64 encoded strings. If both private and public keys are {@code null}, a new key pair will be generated. If
     * only one key is provided, the ECIES object can only be used for operations corresponding to that key.
     *
     * @param algorithm  The custom ECIES algorithm, e.g., "ECIESwithDESede/NONE/PKCS7Padding".
     * @param privateKey The private key as a Hex or Base64 encoded string.
     * @param publicKey  The public key as a Hex or Base64 encoded string.
     */
    public ECIES(final String algorithm, final String privateKey, final String publicKey) {
        super(algorithm, privateKey, publicKey);
    }

    /**
     * Constructs an ECIES encryptor/decryptor with an existing {@link KeyPair}. If the {@code keyPair} is {@code null},
     * a new key pair will be randomly generated. If only one key (private or public) is provided within the
     * {@code keyPair}, the ECIES object can only be used for operations corresponding to that key. Uses the default
     * ECIES algorithm.
     *
     * @param keyPair The {@link KeyPair} containing the private and public keys. If {@code null}, a new random key pair
     *                is generated.
     */
    public ECIES(final KeyPair keyPair) {
        super(Algorithm.ECIES.getValue(), keyPair);
    }

    /**
     * Constructs an ECIES encryptor/decryptor with a custom ECIES algorithm and an existing {@link KeyPair}. If the
     * {@code keyPair} is {@code null}, a new key pair will be randomly generated. If only one key (private or public)
     * is provided within the {@code keyPair}, the ECIES object can only be used for operations corresponding to that
     * key.
     *
     * @param algorithm The custom ECIES algorithm, e.g., "ECIESwithDESede/NONE/PKCS7Padding".
     * @param keyPair   The {@link KeyPair} containing the private and public keys. If {@code null}, a new random key
     *                  pair is generated.
     */
    public ECIES(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

}
