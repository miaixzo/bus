/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.crypto.metric;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Provider;
import org.miaixz.bus.crypto.center.AES;

/**
 * Provides an implementation for the Advanced Encryption Standard (AES) algorithm. AES is a fast, highly secure,
 * next-generation encryption standard. It is a block cipher that operates on 128-bit blocks, using keys of 128, 192, or
 * 256 bits. AES is designed to be efficient, especially in 8-bit architectures, due to its byte-oriented design. It is
 * suitable for small 8-bit microcontrollers or common 32-bit microprocessors, and can achieve gigabit-level throughput
 * when implemented in dedicated hardware.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AESProvider implements Provider {

    /**
     * Encrypts the given content using AES with the provided key.
     *
     * @param key     The encryption key as a string. It will be converted to bytes using the platform's default
     *                charset.
     * @param content The content to be encrypted as a byte array.
     * @return The encrypted content as a byte array.
     * @throws InternalException if the key is null or empty.
     */
    @Override
    public byte[] encrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        AES aes = new AES(key.getBytes());
        return aes.encrypt(content);
    }

    /**
     * Decrypts the given content using AES with the provided key.
     *
     * @param key     The decryption key as a string. It will be converted to bytes using the platform's default
     *                charset.
     * @param content The content to be decrypted as a byte array.
     * @return The decrypted content as a byte array.
     * @throws InternalException if the key is null or empty.
     */
    @Override
    public byte[] decrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        AES aes = new AES(key.getBytes());
        return aes.decrypt(content);
    }

}
