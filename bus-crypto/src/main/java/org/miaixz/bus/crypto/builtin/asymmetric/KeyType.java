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

    /**
     * The integer value representing the key type.
     */
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
