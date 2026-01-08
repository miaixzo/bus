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
import java.io.Serializable;

/**
 * Abstract base class for secure keys, implementing the {@link Key} interface and {@link Serializable}. This class
 * provides common fields and methods for cryptographic keys, such as algorithm, key size, IV size, and password.
 *
 * @author Kimi Liu
 * @since Java 17+
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
