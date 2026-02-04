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
package org.miaixz.bus.core.net.tls;

import java.security.*;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * {@link KeyManager} related utility class. This utility is used to read and use digital certificates, symmetric keys,
 * and other related information.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnyKeyManager {

    /**
     * Constructs a new AnyKeyManager. Utility class constructor for static access.
     */
    private AnyKeyManager() {
    }

    /**
     * Gets the default {@link KeyManagerFactory}.
     *
     * @return The default {@link KeyManagerFactory}.
     */
    public static KeyManagerFactory getDefaultKeyManagerFactory() {
        return getDefaultKeyManagerFactory(null);
    }

    /**
     * Gets the default {@link KeyManagerFactory}.
     *
     * @param provider The algorithm provider, or {@code null} to use the default JDK provider.
     * @return The default {@link KeyManagerFactory}.
     */
    public static KeyManagerFactory getDefaultKeyManagerFactory(final Provider provider) {
        return getKeyManagerFactory(null, provider);
    }

    /**
     * Gets a {@link KeyManagerFactory} for a given algorithm and provider.
     *
     * @param algorithm The algorithm, or {@code null} for the default algorithm (e.g., SunX509).
     * @param provider  The algorithm provider, or {@code null} to use the default JDK provider.
     * @return A {@link KeyManagerFactory}.
     * @throws CryptoException if the algorithm is not found.
     */
    public static KeyManagerFactory getKeyManagerFactory(String algorithm, final Provider provider) {
        if (StringKit.isBlank(algorithm)) {
            algorithm = KeyManagerFactory.getDefaultAlgorithm();
        }

        try {
            return null == provider ? KeyManagerFactory.getInstance(algorithm)
                    : KeyManagerFactory.getInstance(algorithm, provider);
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
    }

    /**
     * Gets a {@link KeyManagerFactory} from a {@link KeyStore}.
     *
     * @param keyStore  The {@link KeyStore}.
     * @param password  The password.
     * @param algorithm The algorithm, or {@code null} for the default algorithm (e.g., SunX509).
     * @param provider  The algorithm provider, or {@code null} to use the default JDK provider.
     * @return A {@link KeyManagerFactory}.
     * @throws CryptoException if an error occurs during initialization.
     */
    public static KeyManagerFactory getKeyManagerFactory(
            final KeyStore keyStore,
            final char[] password,
            final String algorithm,
            final Provider provider) {
        final KeyManagerFactory keyManagerFactory = getKeyManagerFactory(algorithm, provider);
        try {
            keyManagerFactory.init(keyStore, password);
        } catch (final KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            throw new CryptoException(e);
        }
        return keyManagerFactory;
    }

    /**
     * Gets a list of {@link KeyManager}s from a {@link KeyStore}.
     *
     * @param keyStore The {@link KeyStore}.
     * @param password The password.
     * @return A list of {@link KeyManager}s.
     */
    public static KeyManager[] getKeyManagers(final KeyStore keyStore, final char[] password) {
        return getKeyManagers(keyStore, password, null, null);
    }

    /**
     * Gets a list of {@link KeyManager}s from a {@link KeyStore}.
     *
     * @param keyStore  The {@link KeyStore}.
     * @param password  The password.
     * @param algorithm The algorithm, or {@code null} for the default algorithm (e.g., SunX509).
     * @param provider  The algorithm provider, or {@code null} to use the default JDK provider.
     * @return A list of {@link KeyManager}s.
     */
    public static KeyManager[] getKeyManagers(
            final KeyStore keyStore,
            final char[] password,
            final String algorithm,
            final Provider provider) {
        return getKeyManagerFactory(keyStore, password, algorithm, provider).getKeyManagers();
    }

}
