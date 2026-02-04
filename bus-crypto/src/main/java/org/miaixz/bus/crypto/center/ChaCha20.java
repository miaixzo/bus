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

import javax.crypto.spec.IvParameterSpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

/**
 * ChaCha20 algorithm implementation. The ChaCha series of stream ciphers, as an improved version of the Salsa cipher,
 * has stronger resistance to cryptanalytic attacks. "20" indicates that the algorithm has 20 rounds of encryption
 * calculations.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ChaCha20 extends Crypto {

    @Serial
    private static final long serialVersionUID = 2852289670832L;

    /**
     * Constructor.
     *
     * @param key The secret key.
     * @param iv  The initialization vector (IV), 12 bytes (96 bits).
     */
    public ChaCha20(final byte[] key, final byte[] iv) {
        super(Algorithm.CHACHA20.getValue(), Keeper.generateKey(Algorithm.CHACHA20.getValue(), key),
                generateIvParam(iv));
    }

    /**
     * Generates the IvParameterSpec.
     *
     * @param iv The initialization vector.
     * @return {@link IvParameterSpec}
     */
    private static IvParameterSpec generateIvParam(byte[] iv) {
        if (null == iv) {
            iv = RandomKit.randomBytes(12);
        }
        return new IvParameterSpec(iv);
    }

}
