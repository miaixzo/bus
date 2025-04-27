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
package org.miaixz.bus.crypto.center;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

/**
 * DES加密算法实现 DES全称为Data Encryption Standard，即数据加密标准，是一种使用密钥加密的块算法 Java中默认实现为：DES/ECB/PKCS5Padding
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class DES extends Crypto {

    private static final long serialVersionUID = -1L;

    /**
     * 构造，默认DES/ECB/PKCS5Padding，使用随机密钥
     */
    public DES() {
        super(Algorithm.DES);
    }

    /**
     * 构造，使用默认的DES/ECB/PKCS5Padding
     *
     * @param key 密钥
     */
    public DES(final byte[] key) {
        super(Algorithm.DES, key);
    }

    /**
     * 构造，使用随机密钥
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     */
    public DES(final Algorithm.Mode mode, final Padding padding) {
        this(mode.name(), padding.name());
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度：8的倍数
     */
    public DES(final Algorithm.Mode mode, final Padding padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度：8的倍数
     * @param iv      偏移向量，加盐
     */
    public DES(final Algorithm.Mode mode, final Padding padding, final byte[] key, final byte[] iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度：8的倍数
     */
    public DES(final Algorithm.Mode mode, final Padding padding, final SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，长度：8的倍数
     * @param iv      偏移向量，加盐
     */
    public DES(final Algorithm.Mode mode, final Padding padding, final SecretKey key, final IvParameterSpec iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     */
    public DES(final String mode, final String padding) {
        this(mode, padding, (byte[]) null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度：8的倍数
     */
    public DES(final String mode, final String padding, final byte[] key) {
        this(mode, padding, Keeper.generateKey("DES", key), null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度：8的倍数
     * @param iv      加盐
     */
    public DES(final String mode, final String padding, final byte[] key, final byte[] iv) {
        this(mode, padding, Keeper.generateKey("DES", key), null == iv ? null : new IvParameterSpec(iv));
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度：8的倍数
     */
    public DES(final String mode, final String padding, final SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，长度：8的倍数
     * @param iv      加盐
     */
    public DES(final String mode, final String padding, final SecretKey key, final IvParameterSpec iv) {
        super(StringKit.format("DES/{}/{}", mode, padding), key, iv);
    }

}
