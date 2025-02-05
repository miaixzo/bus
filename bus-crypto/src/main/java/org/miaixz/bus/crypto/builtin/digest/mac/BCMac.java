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
import org.bouncycastle.crypto.params.KeyParameter;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;

/**
 * BouncyCastle的MAC算法实现引擎，使用{@link org.bouncycastle.crypto.Mac} 实现摘要 当引入BouncyCastle库时自动使用其作为Provider
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BCMac extends SimpleWrapper<org.bouncycastle.crypto.Mac> implements Mac {

    /**
     * 构造
     *
     * @param mac    {@link org.bouncycastle.crypto.Mac}
     * @param params 参数，例如密钥可以用{@link KeyParameter}
     */
    public BCMac(final org.bouncycastle.crypto.Mac mac, final CipherParameters params) {
        super(initMac(mac, params));
    }

    /**
     * 初始化
     *
     * @param mac    摘要算法
     * @param params 参数，例如密钥可以用{@link KeyParameter}
     * @return this
     */
    private static org.bouncycastle.crypto.Mac initMac(final org.bouncycastle.crypto.Mac mac,
            final CipherParameters params) {
        mac.init(params);
        return mac;
    }

    @Override
    public void update(final byte[] in, final int inOff, final int len) {
        this.raw.update(in, inOff, len);
    }

    @Override
    public byte[] doFinal() {
        final byte[] result = new byte[getMacLength()];
        this.raw.doFinal(result, 0);
        return result;
    }

    @Override
    public void reset() {
        this.raw.reset();
    }

    @Override
    public int getMacLength() {
        return this.raw.getMacSize();
    }

    @Override
    public String getAlgorithm() {
        return this.raw.getAlgorithmName();
    }

}
