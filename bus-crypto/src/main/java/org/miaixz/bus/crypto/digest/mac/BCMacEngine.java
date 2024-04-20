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
package org.miaixz.bus.crypto.digest.mac;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.params.KeyParameter;

/**
 * BouncyCastle的MAC算法实现引擎，使用{@link Mac} 实现摘要
 * 当引入BouncyCastle库时自动使用其作为Provider
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BCMacEngine implements MacEngine {

    private Mac mac;

    /**
     * 构造
     *
     * @param mac    {@link Mac}
     * @param params 参数，例如密钥可以用{@link KeyParameter}
     */
    public BCMacEngine(Mac mac, CipherParameters params) {
        init(mac, params);
    }

    /**
     * 初始化
     *
     * @param mac    摘要算法
     * @param params 参数，例如密钥可以用{@link KeyParameter}
     * @return this
     */
    public BCMacEngine init(Mac mac, CipherParameters params) {
        mac.init(params);
        this.mac = mac;
        return this;
    }

    /**
     * 获得 {@link Mac}
     *
     * @return {@link Mac}
     */
    public Mac getMac() {
        return mac;
    }

    @Override
    public void update(byte[] in, int inOff, int len) {
        this.mac.update(in, inOff, len);
    }

    @Override
    public byte[] doFinal() {
        final byte[] result = new byte[getMacLength()];
        this.mac.doFinal(result, 0);
        return result;
    }

    @Override
    public void reset() {
        this.mac.reset();
    }

    @Override
    public int getMacLength() {
        return mac.getMacSize();
    }

    @Override
    public String getAlgorithm() {
        return this.mac.getAlgorithmName();
    }

}
