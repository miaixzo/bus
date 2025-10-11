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

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.crypto.Builder;

/**
 * Simple factory class for creating {@link Mac} instances. This factory provides methods to create MAC engines based on
 * the specified algorithm and key.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class MacFactory {

    /**
     * Creates a {@link Mac} instance for the given algorithm and key.
     *
     * @param algorithm The MAC algorithm, see {@link Algorithm}.
     * @param key       The cryptographic key.
     * @return A new {@link Mac} instance.
     */
    public static Mac createEngine(final String algorithm, final Key key) {
        return createEngine(algorithm, key, null);
    }

    /**
     * Creates a {@link Mac} instance for the given algorithm, key, and algorithm parameter specification.
     *
     * @param algorithm The MAC algorithm, see {@link Algorithm}.
     * @param key       The cryptographic key.
     * @param spec      The {@link AlgorithmParameterSpec} for initializing the MAC.
     * @return A new {@link Mac} instance.
     */
    public static Mac createEngine(final String algorithm, final Key key, final AlgorithmParameterSpec spec) {
        if (algorithm.equalsIgnoreCase(Algorithm.HMACSM3.getValue())) {
            // HmacSM3 algorithm is implemented by BC library, ignore salt
            return Builder.createHmacSm3Engine(key.getEncoded());
        }
        return new JCEMac(algorithm, key, spec);
    }

}
