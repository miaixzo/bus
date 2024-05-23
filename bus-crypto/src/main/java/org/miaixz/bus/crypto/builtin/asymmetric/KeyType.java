/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.crypto.builtin.asymmetric;

import javax.crypto.Cipher;

/**
 * 密钥类型
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public enum KeyType {
    /**
     * 公钥
     */
    PublicKey(Cipher.PUBLIC_KEY),
    /**
     * 私钥
     */
    PrivateKey(Cipher.PRIVATE_KEY),
    /**
     * 密钥
     */
    SecretKey(Cipher.SECRET_KEY);

    private final int value;

    /**
     * 构造
     *
     * @param value 见{@link Cipher}
     */
    KeyType(final int value) {
        this.value = value;
    }

    /**
     * 获取枚举值对应的int表示
     *
     * @return 枚举值对应的int表示
     */
    public int getValue() {
        return this.value;
    }

}
