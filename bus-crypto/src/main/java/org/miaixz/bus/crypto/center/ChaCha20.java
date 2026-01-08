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
