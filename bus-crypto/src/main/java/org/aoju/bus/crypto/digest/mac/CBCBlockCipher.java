/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2023 aoju.org and other contributors.                      *
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
package org.aoju.bus.crypto.digest.mac;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

import java.security.Key;

/**
 * {@link CBCBlockCipherMac}实现的MAC算法，使用CBC Block方式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CBCBlockCipher extends BCMacEngine {

    /**
     * 构造
     *
     * @param digest        摘要算法，为{@link Digest} 的接口实现
     * @param macSizeInBits mac结果的bits长度，必须为8的倍数
     * @param key           密钥
     * @param iv            加盐
     */
    public CBCBlockCipher(BlockCipher digest, int macSizeInBits, Key key, byte[] iv) {
        this(digest, macSizeInBits, key.getEncoded(), iv);
    }

    /**
     * 构造
     *
     * @param digest        摘要算法，为{@link Digest} 的接口实现
     * @param macSizeInBits mac结果的bits长度，必须为8的倍数
     * @param key           密钥
     * @param iv            加盐
     */
    public CBCBlockCipher(BlockCipher digest, int macSizeInBits, byte[] key, byte[] iv) {
        this(digest, macSizeInBits, new ParametersWithIV(new KeyParameter(key), iv));
    }

    /**
     * 构造
     *
     * @param cipher        算法，为{@link BlockCipher} 的接口实现
     * @param macSizeInBits mac结果的bits长度，必须为8的倍数
     * @param key           密钥
     */
    public CBCBlockCipher(BlockCipher cipher, int macSizeInBits, Key key) {
        this(cipher, macSizeInBits, key.getEncoded());
    }

    /**
     * 构造
     *
     * @param cipher        算法，为{@link BlockCipher} 的接口实现
     * @param macSizeInBits mac结果的bits长度，必须为8的倍数
     * @param key           密钥
     */
    public CBCBlockCipher(BlockCipher cipher, int macSizeInBits, byte[] key) {
        this(cipher, macSizeInBits, new KeyParameter(key));
    }

    /**
     * 构造
     *
     * @param cipher        算法，为{@link BlockCipher} 的接口实现
     * @param macSizeInBits mac结果的bits长度，必须为8的倍数
     * @param params        参数，例如密钥可以用{@link KeyParameter}
     */
    public CBCBlockCipher(BlockCipher cipher, int macSizeInBits, CipherParameters params) {
        this(new CBCBlockCipherMac(cipher, macSizeInBits), params);
    }

    /**
     * 构造
     *
     * @param mac    {@link CBCBlockCipherMac}
     * @param params 参数，例如密钥可以用{@link KeyParameter}
     */
    public CBCBlockCipher(CBCBlockCipherMac mac, CipherParameters params) {
        super(mac, params);
    }

    /**
     * 初始化
     *
     * @param cipher {@link BlockCipher}
     * @param params 参数，例如密钥可以用{@link KeyParameter}
     * @return this
     * @see #init(Mac, CipherParameters)
     */
    public CBCBlockCipher init(BlockCipher cipher, CipherParameters params) {
        return (CBCBlockCipher) init(new CBCBlockCipherMac(cipher), params);
    }

}
