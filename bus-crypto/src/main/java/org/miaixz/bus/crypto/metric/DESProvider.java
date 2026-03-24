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
package org.miaixz.bus.crypto.metric;

import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Provider;
import org.miaixz.bus.crypto.center.DES;

/**
 * Provides an implementation for the Data Encryption Standard (DES) algorithm. DES is a relatively fast encryption
 * standard, suitable for scenarios requiring the encryption of large amounts of data.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class DESProvider implements Provider {

    /**
     * Encrypts the given content using DES with the provided key.
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
        DES des = new DES(key.getBytes());
        return des.encrypt(content);
    }

    /**
     * Decrypts the given content using DES with the provided key.
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
        DES des = new DES(key.getBytes());
        return des.decrypt(content);
    }

}
