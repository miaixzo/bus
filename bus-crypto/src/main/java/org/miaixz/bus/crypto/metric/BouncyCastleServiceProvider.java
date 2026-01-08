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
package org.miaixz.bus.crypto.metric;

import java.security.Security;

import org.miaixz.bus.core.lang.Assert;

/**
 * Factory class for {@link org.bouncycastle.jce.provider.BouncyCastleProvider}. This class implements the
 * {@link BouncyCastleProvider} interface to create and provide instances of the Bouncy Castle security provider.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BouncyCastleServiceProvider implements BouncyCastleProvider {

    /**
     * Constructs a {@code BouncyCastleServiceProvider}. This constructor performs a check to ensure that the Bouncy
     * Castle library is available when loaded via SPI.
     */
    public BouncyCastleServiceProvider() {
        // Check if the BC library is introduced when loaded via SPI
        Assert.notNull(org.bouncycastle.jce.provider.BouncyCastleProvider.class);
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return Description inherited from parent class or interface.
     */
    @Override
    public java.security.Provider create() {
        java.security.Provider provider = Security
                .getProvider(org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME);
        if (null == provider) {
            provider = new org.bouncycastle.jce.provider.BouncyCastleProvider();
        }

        return provider;
    }

}
