/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.crypto.algorithm.digest.mac;

import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.core.utils.IoUtils;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;

import java.io.IOException;
import java.io.InputStream;

/**
 * BouncyCastle的HMAC算法实现引擎,使用{@link Mac} 实现摘要<br>
 * 当引入BouncyCastle库时自动使用其作为Provider
 *
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8+
 */
public class BCHMacEngine implements MacEngine {

    private Mac mac;

    /**
     * 构造
     *
     * @param digest 摘要算法,为{@link Digest} 的接口实现
     * @param key    密钥
     */
    public BCHMacEngine(Digest digest, byte[] key) {
        this(digest, new KeyParameter(key));
    }

    /**
     * 构造
     *
     * @param digest 摘要算法
     * @param params 参数,例如密钥可以用{@link KeyParameter}
     */
    public BCHMacEngine(Digest digest, CipherParameters params) {
        init(digest, params);
    }

    /**
     * 初始化
     *
     * @param digest 摘要算法
     * @param params 参数,例如密钥可以用{@link KeyParameter}
     * @return this
     */
    public BCHMacEngine init(Digest digest, CipherParameters params) {
        mac = new HMac(digest);
        mac.init(params);
        return this;
    }

    @Override
    public byte[] digest(InputStream data, int bufferLength) {
        if (bufferLength < 1) {
            bufferLength = IoUtils.DEFAULT_BUFFER_SIZE;
        }
        final byte[] buffer = new byte[bufferLength];

        byte[] result;
        try {
            int read = data.read(buffer, 0, bufferLength);

            while (read > -1) {
                mac.update(buffer, 0, read);
                read = data.read(buffer, 0, bufferLength);
            }
            result = new byte[this.mac.getMacSize()];
            mac.doFinal(result, 0);
        } catch (IOException e) {
            throw new InstrumentException(e);
        } finally {
            mac.reset();
        }
        return result;
    }

    /**
     * 获得 {@link Mac}
     *
     * @return {@link Mac}
     */
    public Mac getMac() {
        return mac;
    }

}
