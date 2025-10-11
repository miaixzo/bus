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
