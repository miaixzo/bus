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

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Provider;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

/**
 * Provides an implementation for the RC4 (Rivest Cipher 4) stream cipher algorithm. RC4 is known for its simplicity and
 * speed, making it suitable for scenarios requiring fast encryption/decryption.
 * <p>
 * Note: The original class comment incorrectly described AES. This has been corrected to reflect RC4.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RC4Provider implements Provider {

    /**
     * Encrypts the given content using RC4 with the provided key.
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
        Crypto rc4 = new Crypto(Algorithm.RC4, key.getBytes());
        return rc4.encrypt(StringKit.toString(content, Charset.UTF_8));
    }

    /**
     * Decrypts the given content using RC4 with the provided key.
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
        Crypto rc4 = new Crypto(Algorithm.RC4, key.getBytes());
        return rc4.decrypt(content);
    }

}
