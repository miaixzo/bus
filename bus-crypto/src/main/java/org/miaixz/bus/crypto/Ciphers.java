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
package org.miaixz.bus.crypto;

import org.miaixz.bus.core.lang.Wrapper;

import javax.crypto.Cipher;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

/**
 * {@link Cipher}包装类，提供初始化模式等额外方法
 * 包装之后可提供自定义或默认的：
 * <ul>
 *     <li>{@link AlgorithmParameterSpec}</li>
 *     <li>{@link SecureRandom}</li>
 * </ul>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ciphers implements Wrapper<Cipher> {

    /**
     * Cipher负责完成加密或解密工作
     */
    private final Cipher cipher;
    /**
     * 算法参数
     */
    private AlgorithmParameterSpec params;
    /**
     * 随机数生成器，可自定义随机数种子
     */
    private SecureRandom random;

    /**
     * 构造
     *
     * @param algorithm 算法名称
     */
    public Ciphers(String algorithm) {
        this(Builder.createCipher(algorithm));
    }

    /**
     * 构造
     *
     * @param cipher {@link Cipher}
     */
    public Ciphers(Cipher cipher) {
        this.cipher = cipher;
    }

    /**
     * 获取{@link AlgorithmParameterSpec}
     * 在某些算法中，需要特别的参数，例如在ECIES中，此处为IESParameterSpec
     *
     * @return {@link AlgorithmParameterSpec}
     */
    public AlgorithmParameterSpec getParams() {
        return this.params;
    }

    /**
     * 设置 {@link AlgorithmParameterSpec}，通常用于加盐或偏移向量
     *
     * @param params {@link AlgorithmParameterSpec}
     * @return this
     */
    public Ciphers setParams(AlgorithmParameterSpec params) {
        this.params = params;
        return this;
    }

    /**
     * 设置随机数生成器，可自定义随机数种子
     *
     * @param random 随机数生成器，可自定义随机数种子
     * @return this
     */
    public Ciphers setRandom(SecureRandom random) {
        this.random = random;
        return this;
    }

    /**
     * 获取被包装的{@link Cipher}
     *
     * @return {@link Cipher}
     */
    @Override
    public Cipher getRaw() {
        return this.cipher;
    }

    /**
     * 初始化{@link Cipher}为加密或者解密模式
     *
     * @param mode 模式，见{@link Cipher#ENCRYPT_MODE} 或 {@link Cipher#DECRYPT_MODE}
     * @param key  密钥
     * @return this
     * @throws InvalidKeyException                无效key
     * @throws InvalidAlgorithmParameterException 无效算法
     */
    public Ciphers initMode(int mode, Key key)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        final Cipher cipher = this.cipher;
        final AlgorithmParameterSpec params = this.params;
        final SecureRandom random = this.random;
        if (null != params) {
            if (null != random) {
                cipher.init(mode, key, params, random);
            } else {
                cipher.init(mode, key, params);
            }
        } else {
            if (null != random) {
                cipher.init(mode, key, random);
            } else {
                cipher.init(mode, key);
            }
        }
        return this;
    }

}
