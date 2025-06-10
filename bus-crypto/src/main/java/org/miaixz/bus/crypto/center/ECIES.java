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
package org.miaixz.bus.crypto.center;

import java.io.Serial;
import java.security.KeyPair;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.crypto.builtin.asymmetric.Crypto;

/**
 * ECIES（集成加密方案，elliptic curve integrate encrypt scheme）
 * 详细介绍见：https://blog.csdn.net/baidu_26954729/article/details/90437344 此算法必须引入Bouncy Castle库
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ECIES extends Crypto {

    @Serial
    private static final long serialVersionUID = 2852289857959L;

    /**
     * 构造，生成新的私钥公钥对
     */
    public ECIES() {
        super(Algorithm.ECIES.getValue());
    }

    /**
     * 构造，生成新的私钥公钥对
     *
     * @param algorithm 自定义ECIES算法，例如ECIESwithDESede/NONE/PKCS7Padding
     */
    public ECIES(final String algorithm) {
        super(algorithm);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     */
    public ECIES(final String privateKey, final String publicKey) {
        super(Algorithm.ECIES.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param privateKey 私钥
     * @param publicKey  公钥
     */
    public ECIES(final byte[] privateKey, final byte[] publicKey) {
        super(Algorithm.ECIES.getValue(), privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm  自定义ECIES算法，例如ECIESwithDESede/NONE/PKCS7Padding
     * @param privateKey 私钥Hex或Base64表示
     * @param publicKey  公钥Hex或Base64表示
     */
    public ECIES(final String algorithm, final String privateKey, final String publicKey) {
        super(algorithm, privateKey, publicKey);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     * 
     * @param keyPair 密钥对，{@code null}表示随机生成
     */
    public ECIES(final KeyPair keyPair) {
        super(Algorithm.ECIES.getValue(), keyPair);
    }

    /**
     * 构造 私钥和公钥同时为空时生成一对新的私钥和公钥 私钥和公钥可以单独传入一个，如此则只能使用此钥匙来做加密或者解密
     *
     * @param algorithm 自定义ECIES算法，例如ECIESwithDESede/NONE/PKCS7Padding
     * @param keyPair   密钥对，{@code null}表示随机生成
     */
    public ECIES(final String algorithm, final KeyPair keyPair) {
        super(algorithm, keyPair);
    }

}
