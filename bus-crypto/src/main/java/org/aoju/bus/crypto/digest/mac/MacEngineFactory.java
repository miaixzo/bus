/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.crypto.digest.mac;

import org.aoju.bus.core.consts.Algorithm;
import org.aoju.bus.crypto.Builder;

import javax.crypto.SecretKey;

/**
 * {@link MacEngine} 实现工厂类
 *
 * @author Kimi Liu
 * @version 5.0.3
 * @since JDK 1.8+
 */
public class MacEngineFactory {

    /**
     * 根据给定算法和密钥生成对应的{@link MacEngine}
     *
     * @param algorithm 算法
     * @param key       密钥
     * @return {@link MacEngine}
     */
    public static MacEngine createEngine(String algorithm, SecretKey key) {
        if (algorithm.equalsIgnoreCase(Algorithm.HmacSM3)) {
            // HmacSM3算法是BC库实现的
            return Builder.createHmacSm3Engine(key.getEncoded());
        }
        return new DefaultHMacEngine(algorithm, key);
    }

}
