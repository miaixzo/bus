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
 * @since Java 17+
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
