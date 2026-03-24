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
import java.io.Serializable;

/**
 * Abstract base class for secure keys, implementing the {@link Key} interface and {@link Serializable}. This class
 * provides common fields and methods for cryptographic keys, such as algorithm, key size, IV size, and password.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public abstract class SecureKey implements Key, Serializable {

    @Serial
    private static final long serialVersionUID = -1L;

    /**
     * The name of the cryptographic algorithm used with this key.
     */
    protected final String algorithm;
    /**
     * The size of the key in bits.
     */
    protected final int keysize;
    /**
     * The size of the initialization vector (IV) in bits.
     */
    protected final int ivsize;
    /**
     * The password associated with this key, if any.
     */
    protected final String password;

    /**
     * Constructs a new {@code SecureKey} with the specified algorithm, key size, IV size, and password.
     *
     * @param algorithm The name of the algorithm used for this key.
     * @param keysize   The size of the key in bits.
     * @param ivsize    The size of the initialization vector (IV) in bits.
     * @param password  The password associated with the key, if any.
     */
    protected SecureKey(String algorithm, int keysize, int ivsize, String password) {
        this.algorithm = algorithm;
        this.keysize = keysize;
        this.ivsize = ivsize;
        this.password = password;
    }

    /**
     * Retrieves the name of the cryptographic algorithm used with this key.
     *
     * @return The algorithm name (e.g., "AES", "RSA").
     */
    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Retrieves the size of the key in bits.
     *
     * @return The key size.
     */
    @Override
    public int getKeysize() {
        return keysize;
    }

    /**
     * Retrieves the size of the initialization vector (IV) in bits. This is primarily applicable to symmetric
     * encryption algorithms that use IVs.
     *
     * @return The IV size.
     */
    @Override
    public int getIvsize() {
        return ivsize;
    }

    /**
     * Retrieves the password associated with this key, if any. This might be used for password-based key derivation
     * functions.
     *
     * @return The password string, or {@code null} if no password is associated.
     */
    @Override
    public String getPassword() {
        return password;
    }

}
