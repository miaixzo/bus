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
package org.miaixz.bus.crypto.builtin.digest.mac;

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Keeper;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

/**
 * JDK提供的的MAC算法实现引擎，使用{@link javax.crypto.Mac} 实现摘要 当引入BouncyCastle库时自动使用其作为Provider
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JCEMac extends SimpleWrapper<javax.crypto.Mac> implements Mac {

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     */
    public JCEMac(final String algorithm, final byte[] key) {
        this(algorithm, (null == key) ? null : new SecretKeySpec(key, algorithm));
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     */
    public JCEMac(final String algorithm, final Key key) {
        this(algorithm, key, null);
    }

    /**
     * 构造
     *
     * @param algorithm 算法
     * @param key       密钥
     * @param spec      {@link AlgorithmParameterSpec}
     */
    public JCEMac(final String algorithm, final Key key, final AlgorithmParameterSpec spec) {
        super(initMac(algorithm, key, spec));
    }

    /**
     * 初始化
     *
     * @param algorithm 算法
     * @param key       密钥 {@link SecretKey}
     * @param spec      {@link AlgorithmParameterSpec}
     * @return this
     * @throws CryptoException Cause by IOException
     */
    private static javax.crypto.Mac initMac(final String algorithm, Key key, final AlgorithmParameterSpec spec) {
        final javax.crypto.Mac mac;
        try {
            mac = Builder.createMac(algorithm);
            if (null == key) {
                key = Keeper.generateKey(algorithm);
            }
            if (null != spec) {
                mac.init(key, spec);
            } else {
                mac.init(key);
            }
        } catch (final Exception e) {
            throw new CryptoException(e);
        }
        return mac;
    }

    @Override
    public void update(final byte[] in) {
        this.raw.update(in);
    }

    @Override
    public void update(final byte[] in, final int inOff, final int len) {
        this.raw.update(in, inOff, len);
    }

    @Override
    public byte[] doFinal() {
        return this.raw.doFinal();
    }

    @Override
    public void reset() {
        this.raw.reset();
    }

    @Override
    public int getMacLength() {
        return this.raw.getMacLength();
    }

    @Override
    public String getAlgorithm() {
        return this.raw.getAlgorithm();
    }

}
