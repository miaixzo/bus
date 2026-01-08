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
