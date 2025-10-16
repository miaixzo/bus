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
package org.miaixz.bus.crypto;

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
     * Retrieves the {@link java.security.Provider} instance. If {@link #useCustomProvider} is {@code false}, this
     * method returns {@code null}, indicating that the JDK's default providers should be used.
     *
     * @return The {@link java.security.Provider} instance, or {@code null} if not using a custom provider.
     */
    public static java.security.Provider getProvider() {
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
