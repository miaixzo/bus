/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.crypto.center;

import java.io.Serial;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.spec.SecretKeySpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.crypto.builtin.digest.mac.MacFactory;

/**
 * HMAC (Hash-based Message Authentication Code) digest algorithm implementation.
 * <p>
 * HMAC uses a cryptographic hash function and a secret key to generate a message digest. It is primarily used to verify
 * the authenticity and integrity of messages exchanged between two parties who share a common secret key.
 * </p>
 * <p>
 * HMAC can be used with any iterative hash function, such as MD5 and SHA-1. It also uses a key for computing and
 * verifying the message authentication value.
 * </p>
 * <p>
 * Note: Instances of this object are not thread-safe after instantiation.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HMac extends org.miaixz.bus.crypto.center.Mac {

    @Serial
    private static final long serialVersionUID = 2852290076863L;

    /**
     * Constructs an HMAC instance with the specified algorithm, generating a random key.
     *
     * @param algorithm The HMAC algorithm, see {@link Algorithm}.
     */
    public HMac(final Algorithm algorithm) {
        this(algorithm, (Key) null);
    }

    /**
     * Constructs an HMAC instance with the specified algorithm and key material.
     *
     * @param algorithm The HMAC algorithm, see {@link Algorithm}.
     * @param key       The key material as a byte array.
     */
    public HMac(final Algorithm algorithm, final byte[] key) {
        this(algorithm.getValue(), key);
    }

    /**
     * Constructs an HMAC instance with the specified algorithm and {@link Key}.
     *
     * @param algorithm The HMAC algorithm, see {@link Algorithm}.
     * @param key       The cryptographic {@link Key}.
     */
    public HMac(final Algorithm algorithm, final Key key) {
        this(algorithm.getValue(), key);
    }

    /**
     * Constructs an HMAC instance with the specified algorithm name and key material.
     *
     * @param algorithm The HMAC algorithm name.
     * @param key       The key material as a byte array.
     */
    public HMac(final String algorithm, final byte[] key) {
        this(algorithm, new SecretKeySpec(key, algorithm));
    }

    /**
     * Constructs an HMAC instance with the specified algorithm name and {@link Key}.
     *
     * @param algorithm The HMAC algorithm name.
     * @param key       The cryptographic {@link Key}.
     */
    public HMac(final String algorithm, final Key key) {
        this(algorithm, key, null);
    }

    /**
     * Constructs an HMAC instance with the specified algorithm name, {@link Key}, and {@link AlgorithmParameterSpec}.
     *
     * @param algorithm The HMAC algorithm name.
     * @param key       The cryptographic {@link Key}.
     * @param spec      The {@link AlgorithmParameterSpec} for initializing the MAC.
     */
    public HMac(final String algorithm, final Key key, final AlgorithmParameterSpec spec) {
        this(MacFactory.createEngine(algorithm, key, spec));
    }

    /**
     * Constructs an HMAC instance with an existing MAC algorithm engine.
     *
     * @param engine The MAC algorithm implementation engine.
     */
    public HMac(final org.miaixz.bus.crypto.builtin.digest.mac.Mac engine) {
        super(engine);
    }

}
