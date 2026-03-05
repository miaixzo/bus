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

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Provider;
import org.miaixz.bus.crypto.builtin.asymmetric.KeyType;
import org.miaixz.bus.crypto.center.RSA;

/**
 * Provides an implementation for RSA (Rivest–Shamir–Adleman) encryption and decryption algorithms. This provider
 * handles the encryption and decryption operations using RSA keys.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RSAProvider implements Provider {

    /**
     * Encrypts the given content using RSA with the provided key. The key string is expected to be a comma-separated
     * string containing the private key, public key, and key type. Example:
     * "privateKeyBase64,publicKeyBase64,PrivateKey" or "privateKeyBase64,publicKeyBase64,PublicKey".
     *
     * @param key     The encryption key string.
     * @param content The content to be encrypted as a byte array.
     * @return The encrypted content as a byte array.
     * @throws InternalException        if the key string is null or empty.
     * @throws IllegalArgumentException if the key string format is invalid or KeyType is not recognized.
     */
    @Override
    public byte[] encrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        String[] array = StringKit.splitToArray(key, Symbol.COMMA);
        RSA rsa = new RSA(array[0], array[1]);
        return rsa.encrypt(content, KeyType.valueOf(array[2]));
    }

    /**
     * Decrypts the given content using RSA with the provided key. The key string is expected to be a comma-separated
     * string containing the private key, public key, and key type. Example:
     * "privateKeyBase64,publicKeyBase64,PrivateKey" or "privateKeyBase64,publicKeyBase64,PublicKey".
     *
     * @param key     The decryption key string.
     * @param content The content to be decrypted as a byte array.
     * @return The decrypted content as a byte array.
     * @throws InternalException        if the key string is null or empty.
     * @throws IllegalArgumentException if the key string format is invalid or KeyType is not recognized.
     */
    @Override
    public byte[] decrypt(String key, byte[] content) {
        if (StringKit.isEmpty(key)) {
            throw new InternalException("key is null!");
        }
        String[] array = StringKit.splitToArray(key, Symbol.COMMA);

        RSA rsa = new RSA(array[0], array[1]);
        return rsa.decrypt(content, KeyType.valueOf(array[2]));
    }

}
