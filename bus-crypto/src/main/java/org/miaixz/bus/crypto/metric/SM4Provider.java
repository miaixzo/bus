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
import org.miaixz.bus.core.xyz.HexKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Provider;
import org.miaixz.bus.crypto.center.SM4;

/**
 * Provides an implementation for the SM4 symmetric encryption algorithm.
 * <p>
 * SM4 is a Chinese national standard for symmetric block encryption.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class SM4Provider implements Provider {

    /**
     * Encrypts the given content using SM4 with the provided key. The key is expected to be a hexadecimal string.
     *
     * @param key     The encryption key as a hexadecimal string.
     * @param content The content to be encrypted as a byte array.
     * @return The encrypted content as a byte array.
     * @throws InternalException if the key is null or empty.
     */
    @Override
    public byte[] encrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        SM4 sm4 = new SM4(HexKit.decode(key));
        return sm4.encrypt(content);
    }

    /**
     * Decrypts the given content using SM4 with the provided key. The key is expected to be a hexadecimal string.
     *
     * @param key     The decryption key as a hexadecimal string.
     * @param content The content to be decrypted as a byte array.
     * @return The decrypted content as a byte array.
     * @throws InternalException if the key is null or empty.
     */
    @Override
    public byte[] decrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        SM4 sm4 = new SM4(HexKit.decode(key));
        return sm4.decrypt(content);
    }

}
