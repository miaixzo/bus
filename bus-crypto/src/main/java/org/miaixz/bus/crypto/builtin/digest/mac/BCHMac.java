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
package org.miaixz.bus.crypto.builtin.digest.mac;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;

/**
 * BouncyCastle的HMAC算法实现引擎，使用{@link Mac} 实现摘要 当引入BouncyCastle库时自动使用其作为Provider
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BCHMac extends BCMac {

    /**
     * 构造
     *
     * @param digest 摘要算法，为{@link Digest} 的接口实现
     * @param key    密钥
     * @param iv     加盐
     */
    public BCHMac(final Digest digest, final byte[] key, final byte[] iv) {
        this(digest, new ParametersWithIV(new KeyParameter(key), iv));
    }

    /**
     * 构造
     *
     * @param digest 摘要算法，为{@link Digest} 的接口实现
     * @param key    密钥
     */
    public BCHMac(final Digest digest, final byte[] key) {
        this(digest, new KeyParameter(key));
    }

    /**
     * 构造
     *
     * @param digest 摘要算法
     * @param params 参数，例如密钥可以用{@link KeyParameter}
     */
    public BCHMac(final Digest digest, final CipherParameters params) {
        this(new HMac(digest), params);
    }

    /**
     * 构造
     *
     * @param mac    {@link HMac}
     * @param params 参数，例如密钥可以用{@link KeyParameter}
     */
    public BCHMac(final HMac mac, final CipherParameters params) {
        super(mac, params);
    }

}
