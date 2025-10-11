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
package org.miaixz.bus.crypto.builtin.asymmetric;

import javax.crypto.Cipher;

/**
 * Enumeration representing different types of cryptographic keys. These key types are typically used in asymmetric
 * encryption operations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum KeyType {

    /**
     * Public key type, corresponding to {@link Cipher#PUBLIC_KEY}.
     */
    PublicKey(Cipher.PUBLIC_KEY),
    /**
     * Private key type, corresponding to {@link Cipher#PRIVATE_KEY}.
     */
    PrivateKey(Cipher.PRIVATE_KEY),
    /**
     * Secret key type, corresponding to {@link Cipher#SECRET_KEY}.
     */
    SecretKey(Cipher.SECRET_KEY);

    private final int value;

    /**
     * Constructs a {@code KeyType} enum with the specified integer value.
     *
     * @param value The integer value representing the key type, typically from {@link Cipher}.
     */
    KeyType(final int value) {
        this.value = value;
    }

    /**
     * Retrieves the integer representation of the key type.
     *
     * @return The integer value corresponding to this key type.
     */
    public int getValue() {
        return this.value;
    }

}
