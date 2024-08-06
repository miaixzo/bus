/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.crypto.builtin.digest.mac;

import java.security.Key;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * {@link org.bouncycastle.crypto.macs.CBCBlockCipherMac}实现的MAC算法，使用CBC Block方式
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CBCBlockCipherMac extends BCMac {

    /**
     * 构造
     *
     * @param digest        摘要算法，为{@link Digest} 的接口实现
     * @param macSizeInBits mac结果的bits长度，必须为8的倍数
     * @param key           密钥
     * @param iv            加盐
     */
    public CBCBlockCipherMac(final BlockCipher digest, final int macSizeInBits, final Key key, final byte[] iv) {
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
    public CBCBlockCipherMac(final BlockCipher digest, final int macSizeInBits, final byte[] key, final byte[] iv) {
        this(digest, macSizeInBits, new ParametersWithIV(new KeyParameter(key), iv));
    }

    /**
     * 构造
     *
     * @param cipher        算法，为{@link BlockCipher} 的接口实现
     * @param macSizeInBits mac结果的bits长度，必须为8的倍数
     * @param key           密钥
     */
    public CBCBlockCipherMac(final BlockCipher cipher, final int macSizeInBits, final Key key) {
        this(cipher, macSizeInBits, key.getEncoded());
    }

    /**
     * 构造
     *
     * @param cipher        算法，为{@link BlockCipher} 的接口实现
     * @param macSizeInBits mac结果的bits长度，必须为8的倍数
     * @param key           密钥
     */
    public CBCBlockCipherMac(final BlockCipher cipher, final int macSizeInBits, final byte[] key) {
        this(cipher, macSizeInBits, new KeyParameter(key));
    }

    /**
     * 构造
     *
     * @param cipher        算法，为{@link BlockCipher} 的接口实现
     * @param macSizeInBits mac结果的bits长度，必须为8的倍数
     * @param params        参数，例如密钥可以用{@link KeyParameter}
     */
    public CBCBlockCipherMac(final BlockCipher cipher, final int macSizeInBits, final CipherParameters params) {
        this(new org.bouncycastle.crypto.macs.CBCBlockCipherMac(cipher, macSizeInBits), params);
    }

    /**
     * 构造
     *
     * @param mac    {@link org.bouncycastle.crypto.macs.CBCBlockCipherMac}
     * @param params 参数，例如密钥可以用{@link KeyParameter}
     */
    public CBCBlockCipherMac(final org.bouncycastle.crypto.macs.CBCBlockCipherMac mac, final CipherParameters params) {
        super(mac, params);
    }

}
