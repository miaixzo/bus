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
package org.miaixz.bus.crypto.nimble;

import java.security.Provider;
import java.security.Security;

/**
 * Factory class for {@link org.bouncycastle.jce.provider.BouncyCastleProvider}. This class implements the
 * {@link BouncyCastleProvider} interface to create and provide instances of the Bouncy Castle security provider.
 *
 * @author Kimi Liu
 */
public class BouncyCastleServiceProvider implements BouncyCastleProvider {

    /**
     * Constructs a {@code BouncyCastleServiceProvider}. This constructor performs a check to ensure that the Bouncy
     * Castle library is available when loaded via SPI.
     */
    public BouncyCastleServiceProvider() {
        // Keep this optional lookup constant so Native Image can resolve it during analysis.
        try {
            Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider");
        } catch (final ClassNotFoundException e) {
            throw new IllegalStateException("Bouncy Castle is not available", e);
        }
    }

    /**
     * Creates and returns a {@link java.security.Provider} instance, typically a Bouncy Castle provider.
     *
     * @return A {@link java.security.Provider} instance.
     */
    @Override
    public Provider create() {
        Provider provider = Security.getProvider("BC");
        if (null == provider) {
            provider = ProviderFactory.create();
        }

        return provider;
    }

    /**
     * Defers linkage to the optional BC library until a provider is requested.
     */
    private static class ProviderFactory {

        /**
         * Creates the optional Bouncy Castle security provider.
         *
         * @return new Bouncy Castle provider
         */
        private static Provider create() {
            return new org.bouncycastle.jce.provider.BouncyCastleProvider();
        }

    }

}
