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
package org.miaixz.bus.crypto.builtin.digest.mac;

import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;
import org.miaixz.bus.crypto.Builder;
import org.miaixz.bus.crypto.Keeper;

/**
 * JCE (Java Cryptography Extension) MAC algorithm implementation engine. This class wraps a {@link javax.crypto.Mac}
 * instance to provide MAC functionality. It uses the JDK's default provider for MAC algorithms.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class JCEMac extends SimpleWrapper<javax.crypto.Mac> implements Mac {

    /**
     * Constructs a {@code JCEMac} instance with the specified algorithm and key material.
     *
     * @param algorithm The MAC algorithm name (e.g., "HmacSHA256").
     * @param key       The key material as a byte array. If {@code null}, a random key is generated.
     */
    public JCEMac(final String algorithm, final byte[] key) {
        this(algorithm, (null == key) ? null : new SecretKeySpec(key, algorithm));
    }

    /**
     * Constructs a {@code JCEMac} instance with the specified algorithm and {@link Key}.
     *
     * @param algorithm The MAC algorithm name.
     * @param key       The {@link Key} to use. If {@code null}, a random key is generated.
     */
    public JCEMac(final String algorithm, final Key key) {
        this(algorithm, key, null);
    }

    /**
     * Constructs a {@code JCEMac} instance with the specified algorithm, {@link Key}, and
     * {@link AlgorithmParameterSpec}.
     *
     * @param algorithm The MAC algorithm name.
     * @param key       The {@link Key} to use. If {@code null}, a random key is generated.
     * @param spec      The {@link AlgorithmParameterSpec} for initializing the MAC.
     */
    public JCEMac(final String algorithm, final Key key, final AlgorithmParameterSpec spec) {
        super(initMac(algorithm, key, spec));
    }

    /**
     * Initializes the {@link javax.crypto.Mac} instance.
     *
     * @param algorithm The MAC algorithm name.
     * @param key       The {@link Key} to use. If {@code null}, a random key is generated using
     *                  {@link Keeper#generateKey(String)}.
     * @param spec      The {@link AlgorithmParameterSpec} for initialization. Can be {@code null}.
     * @return The initialized {@link javax.crypto.Mac} instance.
     * @throws CryptoException if initialization fails.
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

    /**
     * {@inheritDoc}
     *
     * @param in {@inheritDoc}
     */
    @Override
    public void update(final byte[] in) {
        this.raw.update(in);
    }

    /**
     * {@inheritDoc}
     *
     * @param in    {@inheritDoc}
     * @param inOff {@inheritDoc}
     * @param len   {@inheritDoc}
     */
    @Override
    public void update(final byte[] in, final int inOff, final int len) {
        this.raw.update(in, inOff, len);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public byte[] doFinal() {
        return this.raw.doFinal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        this.raw.reset();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int getMacLength() {
        return this.raw.getMacLength();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getAlgorithm() {
        return this.raw.getAlgorithm();
    }

}
