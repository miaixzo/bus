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
package org.miaixz.bus.crypto.center;

import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.builtin.symmetric.SymmetricCrypto;

import javax.crypto.spec.IvParameterSpec;

/**
 * 祖冲之算法集（ZUC算法）实现，基于BouncyCastle实现。
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ZUC extends SymmetricCrypto {

    private static final long serialVersionUID = -1L;

    /**
     * 构造
     *
     * @param algorithm ZUC算法枚举，包括128位和256位两种
     * @param key       密钥
     * @param iv        加盐，128位加盐是16bytes，256位是25bytes，{@code null}是随机加盐
     */
    public ZUC(final ZUCAlgorithm algorithm, final byte[] key, final byte[] iv) {
        super(algorithm.value,
                Keeper.generateKey(algorithm.value, key),
                generateIvParam(algorithm, iv));
    }

    /**
     * 生成ZUC算法密钥
     *
     * @param algorithm ZUC算法
     * @return 密钥
     * @see Keeper#generateKey(String)
     */
    public static byte[] generateKey(final ZUCAlgorithm algorithm) {
        return Keeper.generateKey(algorithm.value).getEncoded();
    }

    /**
     * 生成加盐参数
     *
     * @param algorithm ZUC算法
     * @param iv        加盐，128位加盐是16bytes，256位是25bytes，{@code null}是随机加盐
     * @return {@link IvParameterSpec}
     */
    private static IvParameterSpec generateIvParam(final ZUCAlgorithm algorithm, byte[] iv) {
        if (null == iv) {
            switch (algorithm) {
                case ZUC_128:
                    iv = RandomKit.randomBytes(16);
                    break;
                case ZUC_256:
                    iv = RandomKit.randomBytes(25);
                    break;
            }
        }
        return new IvParameterSpec(iv);
    }

    /**
     * ZUC类型，包括128位和256位
     */
    public enum ZUCAlgorithm {
        /**
         * ZUC-128
         */
        ZUC_128("ZUC-128"),
        /**
         * ZUC-256
         */
        ZUC_256("ZUC-256");

        private final String value;

        /**
         * 构造
         *
         * @param value 算法的字符串表示，区分大小写
         */
        ZUCAlgorithm(final String value) {
            this.value = value;
        }

        /**
         * 获得算法的字符串表示形式
         *
         * @return 算法字符串
         */
        public String getValue() {
            return this.value;
        }
    }

}
