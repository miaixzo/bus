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

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.Padding;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

/**
 * 国密对称堆成加密算法SM4实现
 *
 * <p>
 * 国密算法包括：
 * <ol>
 * <li>非对称加密和签名：SM2，asymmetric</li>
 * <li>摘要签名算法：SM3，digest</li>
 * <li>对称加密：SM4，symmetric</li>
 * </ol>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SM4 extends Crypto {

    private static final long serialVersionUID = -1L;

    /**
     * 构造，使用随机密钥
     */
    public SM4() {
        super(Algorithm.SM4.getValue());
    }

    /**
     * 构造
     *
     * @param key 密钥
     */
    public SM4(final byte[] key) {
        super(Algorithm.SM4.getValue(), key);
    }

    /**
     * 构造，使用随机密钥
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     */
    public SM4(final Algorithm.Mode mode, final Padding padding) {
        this(mode.name(), padding.name());
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，支持密钥长度：128位
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，支持密钥长度：128位
     * @param iv      偏移向量，加盐
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final byte[] key, final byte[] iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，支持密钥长度：128位
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final SecretKey key) {
        this(mode, padding, key, (IvParameterSpec) null);
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，支持密钥长度：128位
     * @param iv      偏移向量，加盐
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final SecretKey key, final byte[] iv) {
        this(mode, padding, key, ArrayKit.isEmpty(iv) ? null : new IvParameterSpec(iv));
    }

    /**
     * 构造
     *
     * @param mode    模式{@link Algorithm.Mode}
     * @param padding {@link Padding}补码方式
     * @param key     密钥，支持密钥长度：128位
     * @param iv      偏移向量，加盐
     */
    public SM4(final Algorithm.Mode mode, final Padding padding, final SecretKey key, final IvParameterSpec iv) {
        this(mode.name(), padding.name(), key, iv);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     */
    public SM4(final String mode, final String padding) {
        this(mode, padding, (byte[]) null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，支持密钥长度：128位
     */
    public SM4(final String mode, final String padding, final byte[] key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，支持密钥长度：128位
     * @param iv      加盐
     */
    public SM4(final String mode, final String padding, final byte[] key, final byte[] iv) {
        this(mode, padding, //
                Keeper.generateKey(Algorithm.SM4.getValue(), key),
                ArrayKit.isEmpty(iv) ? null : new IvParameterSpec(iv));
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，支持密钥长度：128位
     */
    public SM4(final String mode, final String padding, final SecretKey key) {
        this(mode, padding, key, null);
    }

    /**
     * 构造
     *
     * @param mode    模式
     * @param padding 补码方式
     * @param key     密钥，支持密钥长度：128位
     * @param iv      加盐
     */
    public SM4(final String mode, final String padding, final SecretKey key, final IvParameterSpec iv) {
        super(StringKit.format("SM4/{}/{}", mode, padding), key, iv);
    }

}
