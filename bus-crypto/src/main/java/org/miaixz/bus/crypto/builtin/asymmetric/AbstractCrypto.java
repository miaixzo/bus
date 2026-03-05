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
package org.miaixz.bus.crypto.builtin.asymmetric;

import java.io.Serial;
import java.security.KeyPair;

/**
 * Abstract base class for asymmetric cryptographic objects. This class extends {@link Asymmetric} and provides common
 * implementations for encryption and decryption operations, including conversions to Hex and Base64 formats.
 *
 * @param <T> The type of the concrete subclass extending {@code AbstractCrypto}.
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractCrypto<T extends AbstractCrypto<T>> extends Asymmetric<T>
        implements Encryptor, Decryptor {

    @Serial
    private static final long serialVersionUID = 2852335251112L;

    /**
     * Constructs an {@code AbstractCrypto} instance with the specified algorithm and key pair. If both private and
     * public keys within the {@code keyPair} are {@code null}, a new key pair will be generated. If only one key is
     * provided, the crypto object can only be used for operations corresponding to that key.
     *
     * @param algorithm The asymmetric algorithm name.
     * @param keyPair   The {@link KeyPair} containing the private and public keys. If {@code null}, a new random key
     *                  pair is generated.
     */
    public AbstractCrypto(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

}
