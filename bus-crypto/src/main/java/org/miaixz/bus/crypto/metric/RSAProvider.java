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
