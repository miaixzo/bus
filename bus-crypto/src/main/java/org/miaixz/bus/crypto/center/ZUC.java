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
package org.miaixz.bus.crypto.center;

import java.io.Serial;

import javax.crypto.spec.IvParameterSpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

/**
 * ZUC (Zu Chongzhi) algorithm implementation, based on BouncyCastle.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ZUC extends Crypto {

    @Serial
    private static final long serialVersionUID = 2852291166600L;

    /**
     * Constructor.
     *
     * @param algorithm ZUC algorithm enum, including 128-bit and 256-bit versions.
     * @param key       The key.
     * @param iv        The IV (salt), 16 bytes for 128-bit and 25 bytes for 256-bit. If {@code null}, a random IV is
     *                  generated.
     */
    public ZUC(final Algorithm algorithm, final byte[] key, final byte[] iv) {
        super(algorithm.getValue(), Keeper.generateKey(algorithm.getValue(), key), generateIvParam(algorithm, iv));
    }

    /**
     * Generates a ZUC algorithm key.
     *
     * @param algorithm The ZUC algorithm.
     * @return The key.
     * @see Keeper#generateKey(String)
     */
    public static byte[] generateKey(final Algorithm algorithm) {
        return Keeper.generateKey(algorithm.getValue()).getEncoded();
    }

    /**
     * Generates the IV parameter.
     *
     * @param algorithm The ZUC algorithm.
     * @param iv        The IV (salt), 16 bytes for 128-bit and 25 bytes for 256-bit. If {@code null}, a random IV is
     *                  generated.
     * @return {@link IvParameterSpec}
     */
    private static IvParameterSpec generateIvParam(final Algorithm algorithm, byte[] iv) {
        if (null == iv) {
            switch (algorithm) {
                case ZUC_128:
                    iv = RandomKit.randomBytes(16);
                    break;

                case ZUC_256:
                    iv = RandomKit.randomBytes(25);
                    break;
            }
        }
        return new IvParameterSpec(iv);
    }

}
