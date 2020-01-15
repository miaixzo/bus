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
import org.aoju.bus.crypto.Builder;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;

/**
 * 默认的HMAC算法实现引擎,使用{@link Mac} 实现摘要<br>
 * 当引入BouncyCastle库时自动使用其作为Provider
 *
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8+
 */
public class DefaultHMacEngine implements MacEngine {

    private Mac mac;

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     */
    public DefaultHMacEngine(String algorithm, byte[] key) {
        init(algorithm, key);
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     */
    public DefaultHMacEngine(String algorithm, SecretKey key) {
        init(algorithm, key);
    }

    /**
     * 初始化
     *
     * @param algorithm 算法
     * @param key       密钥
     * @return this
     */
    public DefaultHMacEngine init(String algorithm, byte[] key) {
        return init(algorithm, (null == key) ? null : new SecretKeySpec(key, algorithm));
    }

    /**
     * 初始化
     *
     * @param algorithm 算法
     * @param key       密钥 {@link SecretKey}
     * @return this
     */
    public DefaultHMacEngine init(String algorithm, SecretKey key) {
        try {
            mac = Builder.createMac(algorithm);
            if (null == key) {
                key = Builder.generateKey(algorithm);
            }
            mac.init(key);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
        return this;
    }

    @Override
    public byte[] digest(InputStream data, int bufferLength) {
        if (bufferLength < 1) {
            bufferLength = IoUtils.DEFAULT_BUFFER_SIZE;
        }
        byte[] buffer = new byte[bufferLength];

        byte[] result;
        try {
            int read = data.read(buffer, 0, bufferLength);

            while (read > -1) {
                mac.update(buffer, 0, read);
                read = data.read(buffer, 0, bufferLength);
            }
            result = mac.doFinal();
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
