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
 * @since Java 21+
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
