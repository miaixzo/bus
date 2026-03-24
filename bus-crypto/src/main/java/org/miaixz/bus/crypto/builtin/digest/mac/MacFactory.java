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

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.crypto.Builder;

/**
 * Simple factory class for creating {@link Mac} instances. This factory provides methods to create MAC engines based on
 * the specified algorithm and key.
 *
 * @author Kimi Liu
 * @since Java 21+
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
        Assert.notBlank(algorithm, "Algorithm must be not blank!");
        if (algorithm.equalsIgnoreCase(Algorithm.HMACSM3.getValue())) {
            // HmacSM3 algorithm is implemented by BC library, ignore salt
            return Builder.createHmacSm3Engine(key.getEncoded());
        }
        return new JCEMac(algorithm, key, spec);
    }

}
