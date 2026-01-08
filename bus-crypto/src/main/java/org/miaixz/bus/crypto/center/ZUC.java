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

import javax.crypto.spec.IvParameterSpec;

import org.miaixz.bus.core.lang.Algorithm;
import org.miaixz.bus.core.xyz.RandomKit;
import org.miaixz.bus.crypto.Keeper;
import org.miaixz.bus.crypto.builtin.symmetric.Crypto;

/**
 * ZUC (Zu Chongzhi) algorithm implementation, based on BouncyCastle.
 *
 * @author Kimi Liu
 * @since Java 17+
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
