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
package org.miaixz.bus.crypto;

import org.miaixz.bus.core.lang.Keys;
import org.miaixz.bus.core.lang.loader.spi.NormalSpiLoader;
import org.miaixz.bus.crypto.metric.BouncyCastleProvider;

/**
 * Global singleton {@link java.security.Provider} object holder.
 * <p>
 * Upon class loading, this class uses SPI (Service Provider Interface) to locate and load available cryptographic
 * providers. It specifically searches for implementations of {@link BouncyCastleProvider} and creates a unique instance
 * globally.
 * </p>
 * <p>
 * In GraalVM native image environments, custom providers are disabled to avoid JCE verification issues. The JDK's
 * default provider will be used instead.
 * </p>
 * <p>
 * Users can still control whether to use this custom provider or the JDK's default providers by calling the
 * {@link #setUseCustomProvider(boolean)} method.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Holder implements org.miaixz.bus.core.Holder {

    /**
     * The globally held {@link java.security.Provider} instance, initialized via SPI. If no custom provider is found or
     * configured, this might be {@code null}.
     */
    private static final java.security.Provider provider = _createProvider();
    /**
     * Flag indicating whether to use the custom {@link java.security.Provider}. Defaults to {@code true}.
     */
    private static boolean useCustomProvider = true;

    /**
     * Retrieves the {@link java.security.Provider} instance. In GraalVM native image environments, this method always
     * returns {@code null} to avoid JCE verification issues.
     *
     * @return The {@link java.security.Provider} instance, or {@code null} if not using a custom provider or running in
     *         a GraalVM native image.
     */
    public static java.security.Provider getProvider() {
        // In GraalVM native image, always return null to avoid JCE verification issues
        if (Keys.IS_GRAALVM_NATIVE) {
            return null;
        }
        return useCustomProvider ? provider : null;
    }

    /**
     * Sets whether to use the custom {@link java.security.Provider}. If set to {@code false}, the JDK's default
     * providers will be used instead.
     *
     * @param isUseCustomProvider {@code true} to use the custom provider, {@code false} to use JDK's default.
     */
    public static void setUseCustomProvider(final boolean isUseCustomProvider) {
        useCustomProvider = isUseCustomProvider;
    }

    /**
     * Creates a {@link java.security.Provider} instance via SPI (Service Provider Interface). This method attempts to
     * load the first available {@link BouncyCastleProvider} implementation.
     *
     * @return A {@link java.security.Provider} instance if found and created, otherwise {@code null} (indicating that
     *         the JDK's default JCE providers will be used).
     */
    private static java.security.Provider _createProvider() {
        final BouncyCastleProvider factory = NormalSpiLoader.loadFirstAvailable(BouncyCastleProvider.class);
        if (null == factory) {
            // Default to JCE
            return null;
        }

        final java.security.Provider provider = factory.create();
        Builder.addProvider(provider);
        return provider;
    }

}
