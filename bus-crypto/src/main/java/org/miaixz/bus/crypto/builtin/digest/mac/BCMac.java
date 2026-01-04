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

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.miaixz.bus.core.lang.wrapper.SimpleWrapper;

/**
 * BouncyCastle MAC algorithm implementation engine. This class wraps a BouncyCastle {@link org.bouncycastle.crypto.Mac}
 * instance to provide MAC functionality. When the BouncyCastle library is included, it is automatically used as the
 * provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BCMac extends SimpleWrapper<org.bouncycastle.crypto.Mac> implements Mac {

    /**
     * Constructs a {@code BCMac} instance with the specified BouncyCastle MAC and cipher parameters.
     *
     * @param mac    The BouncyCastle {@link org.bouncycastle.crypto.Mac} instance.
     * @param params The {@link CipherParameters} for initializing the MAC, e.g., a {@link KeyParameter} for the key.
     */
    public BCMac(final org.bouncycastle.crypto.Mac mac, final CipherParameters params) {
        super(initMac(mac, params));
    }

    /**
     * Initializes the BouncyCastle MAC instance with the given parameters.
     *
     * @param mac    The BouncyCastle {@link org.bouncycastle.crypto.Mac} instance.
     * @param params The {@link CipherParameters} for initialization.
     * @return The initialized BouncyCastle {@link org.bouncycastle.crypto.Mac} instance.
     */
    private static org.bouncycastle.crypto.Mac initMac(
            final org.bouncycastle.crypto.Mac mac,
            final CipherParameters params) {
        mac.init(params);
        return mac;
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
        final byte[] result = new byte[getMacLength()];
        this.raw.doFinal(result, 0);
        return result;
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
        return this.raw.getMacSize();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getAlgorithm() {
        return this.raw.getAlgorithmName();
    }

}
