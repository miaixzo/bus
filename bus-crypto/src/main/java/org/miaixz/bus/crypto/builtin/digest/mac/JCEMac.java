/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
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
     * Description inherited from parent class or interface.
     *
     * @param in Description inherited from parent class or interface.
     */
    @Override
    public void update(final byte[] in) {
        this.raw.update(in);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @param in    Description inherited from parent class or interface.
     * @param inOff Description inherited from parent class or interface.
     * @param len   Description inherited from parent class or interface.
     */
    @Override
    public void update(final byte[] in, final int inOff, final int len) {
        this.raw.update(in, inOff, len);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return Description inherited from parent class or interface.
     */
    @Override
    public byte[] doFinal() {
        return this.raw.doFinal();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public void reset() {
        this.raw.reset();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return Description inherited from parent class or interface.
     */
    @Override
    public int getMacLength() {
        return this.raw.getMacLength();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return Description inherited from parent class or interface.
     */
    @Override
    public String getAlgorithm() {
        return this.raw.getAlgorithm();
    }

}
