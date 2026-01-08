/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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
