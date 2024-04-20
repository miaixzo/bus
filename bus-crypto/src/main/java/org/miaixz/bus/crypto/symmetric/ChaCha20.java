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
package org.miaixz.bus.crypto.symmetric;

import org.miaixz.bus.core.toolkit.RandomKit;
import org.miaixz.bus.crypto.Builder;

import javax.crypto.spec.IvParameterSpec;

/**
 * ChaCha20算法实现
 * ChaCha系列流密码，作为salsa密码的改良版，具有更强的抵抗密码分析攻击的特性，“20”表示该算法有20轮的加密计算
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChaCha20 extends Crypto {

    public static final String ALGORITHM_NAME = "ChaCha20";
    private static final long serialVersionUID = 1L;

    /**
     * 构造
     *
     * @param key 密钥
     * @param iv  加盐，12bytes（64bit）
     */
    public ChaCha20(byte[] key, byte[] iv) {
        super(ALGORITHM_NAME,
                Builder.generateKey(ALGORITHM_NAME, key),
                generateIvParam(iv));
    }

    /**
     * 生成加盐参数
     *
     * @param iv 加盐
     * @return {@link IvParameterSpec}
     */
    private static IvParameterSpec generateIvParam(byte[] iv) {
        if (null == iv) {
            iv = RandomKit.randomBytes(12);
        }
        return new IvParameterSpec(iv);
    }

}
