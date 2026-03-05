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
