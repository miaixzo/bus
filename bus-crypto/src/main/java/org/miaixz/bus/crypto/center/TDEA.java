/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.crypto.center;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * 三重数据加密算法（英语：Triple Data Encryption Algorithm，缩写为TDEA，Triple DEA），或称3DES（Triple DES）
 * 使用 168 位的密钥对资料进行三次加密的一种机制；它通常（但非始终）提供极其强大的安全性。
 * 如果三个 56 位的子元素都相同，则三重 DES 向后兼容 DES。
 * Java中默认实现为：DESede/ECB/PKCS5Padding
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class TDEA extends Crypto {

    private static final long serialVersionUID = -1L;

    /**
     * 构造，默认DESede/ECB/PKCS5Padding，使用随机密钥
     */
    public TDEA() {
        super(Algorithm.DESEDE);
    }

    /**
     * 构造，使用默认的DESede/ECB/PKCS5Padding
     *
     * @param key 密钥
     */
    public TDEA(final byte[] key) {
        super(Algorithm.DESEDE, key);
    }

    /**
     * 构造，使用随机密钥
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding) {
        this(mode.name(), padding.name());
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度24位
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度24位
     * @param iv      偏移向量，加盐
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding, final byte[] key, final byte[] iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度24位
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding, final SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度24位
     * @param iv      偏移向量，加盐
     */
    public TDEA(final Algorithm.Mode mode, final Padding padding, final SecretKey key, final IvParameterSpec iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     */
    public TDEA(final String mode, final String padding) {
        this(mode, padding, (byte[]) null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度24位
     */
    public TDEA(final String mode, final String padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度24位
     * @param iv      加盐
     */
    public TDEA(final String mode, final String padding, final byte[] key, final byte[] iv) {
        this(mode, padding, Keeper.generateKey(Algorithm.DESEDE.getValue(), key), null == iv ? null : new IvParameterSpec(iv));
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥
     */
    public TDEA(final String mode, final String padding, final SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥
     * @param iv      加盐
     */
    public TDEA(final String mode, final String padding, final SecretKey key, final IvParameterSpec iv) {
        super(StringKit.format("{}/{}/{}", Algorithm.DESEDE.getValue(), mode, padding), key, iv);
    }

}
