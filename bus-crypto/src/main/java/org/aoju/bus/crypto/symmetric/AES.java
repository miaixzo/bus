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
package org.aoju.bus.crypto.symmetric;

import org.aoju.bus.core.consts.ModeType;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.crypto.CryptoUtils;
import org.aoju.bus.crypto.Padding;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * AES加密算法实现
 * 高级加密标准（英语：Advanced Encryption Standard，缩写：AES），在密码学中又称Rijndael加密法
 * 对于Java中AES的默认模式是：AES/ECB/PKCS5Padding，如果使用CryptoJS，请调整为：padding: CryptoJS.pad.Pkcs7
 * <p>
 *
 * @author Kimi Liu
 * @version 3.5.2
 * @since JDK 1.8
 */
public class AES extends Symmetric {

    /**
     * 构造，默认AES/ECB/PKCS5Padding，使用随机密钥
     */
    public AES() {
        super(ModeType.AES);
    }

    /**
     * 构造，使用默认的AES/ECB/PKCS5Padding
     *
     * @param key 密钥
     */
    public AES(byte[] key) {
        super(ModeType.AES, key);
    }

    /**
     * 构造，使用随机密钥
     *
     * @param mode    模式{@link ModeType}
     * @param padding {@link Padding}补码方式
     */
    public AES(String mode, Padding padding) {
        this(mode, padding.name());
    }

    /**
     * 构造
     *
     * @param mode    模式{@link ModeType}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，支持三种密钥长度：128、192、256位
     */
    public AES(String mode, Padding padding, byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link ModeType}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，支持三种密钥长度：128、192、256位
     * @param iv      偏移向量，加盐
     * @since 3.3.0
     */
    public AES(String mode, Padding padding, byte[] key, byte[] iv) {
        this(mode, padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link ModeType}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，支持三种密钥长度：128、192、256位
     * @since 3.3.0
     */
    public AES(String mode, Padding padding, SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link ModeType}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，支持三种密钥长度：128、192、256位
     * @param iv      偏移向量，加盐
     * @since 3.3.0
     */
    public AES(String mode, Padding padding, SecretKey key, IvParameterSpec iv) {
        this(mode, padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     */
    public AES(String mode, String padding) {
        this(mode, padding, (byte[]) null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，支持三种密钥长度：128、192、256位
     */
    public AES(String mode, String padding, byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，支持三种密钥长度：128、192、256位
     * @param iv      加盐
     */
    public AES(String mode, String padding, byte[] key, byte[] iv) {
        this(mode, padding, CryptoUtils.generateKey(ModeType.AES, key), null == iv ? null : new IvParameterSpec(iv));
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，支持三种密钥长度：128、192、256位
     */
    public AES(String mode, String padding, SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，支持三种密钥长度：128、192、256位
     * @param iv      加盐
     */
    public AES(String mode, String padding, SecretKey key, IvParameterSpec iv) {
        super(StringUtils.format("AES/{}/{}", mode, padding), key, iv);
    }

    /**
     * 设置偏移向量
     *
     * @param iv {@link IvParameterSpec}偏移向量
     * @return 自身
     */
    public AES setIv(IvParameterSpec iv) {
        super.setParams(iv);
        return this;
    }

    /**
     * 设置偏移向量
     *
     * @param iv 偏移向量，加盐
     * @return 自身
     * @since 3.3.0
     */
    public AES setIv(byte[] iv) {
        setIv(new IvParameterSpec(iv));
        return this;
    }

}
