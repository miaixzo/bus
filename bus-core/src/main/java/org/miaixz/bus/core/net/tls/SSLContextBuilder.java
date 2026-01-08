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
package org.miaixz.bus.core.net.tls;

import java.io.Serial;
import java.security.*;
import java.util.Arrays;

import javax.net.ssl.*;

import org.miaixz.bus.core.Builder;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A builder for {@link SSLContext} objects.
 * <p>
 * This builder allows for the customization of:
 * <ul>
 * <li>The protocol (default: TLS)</li>
 * <li>{@link KeyManager} (default: none)</li>
 * <li>{@link TrustManager} (default: none)</li>
 * <li>{@link SecureRandom} (default: none)</li>
 * </ul>
 * After configuration, an {@link SSLContext} can be built. The {@link SSLSocketFactory} can be obtained by calling
 * {@link SSLContext#getSocketFactory()}.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class SSLContextBuilder implements Builder<SSLContext> {

    @Serial
    private static final long serialVersionUID = 2852230919781L;

    /**
     * The SSL protocol to use.
     */
    private String protocol = Protocol.TLS.name;
    /**
     * The key managers.
     */
    private KeyManager[] keyManagers;
    /**
     * The trust managers.
     */
    private TrustManager[] trustManagers;
    /**
     * The secure random number generator.
     */
    private SecureRandom secureRandom;
    /**
     * The security provider.
     */
    private Provider provider;

    /**
     * Creates a new {@code SSLContextBuilder}.
     *
     * @return A new {@code SSLContextBuilder}.
     */
    public static SSLContextBuilder of() {
        return new SSLContextBuilder();
    }

    /**
     * Gets the default {@link SSLContext}.
     *
     * @return The default {@link SSLContext}.
     * @throws InternalException if the default SSLContext cannot be retrieved.
     */
    public static SSLContext getDefault() {
        try {
            return SSLContext.getDefault();
        } catch (final NoSuchAlgorithmException e) {
            throw new InternalException(e);
        }
    }

    /**
     * Creates an {@link SSLContext} that trusts all certificates, using the TLS protocol.
     *
     * @return The created {@link SSLContext}.
     * @throws InternalException if a {@link GeneralSecurityException} occurs.
     */
    public static SSLContext createTrustAnySSLContext() throws InternalException {
        return createTrustAnySSLContext(null);
    }

    /**
     * Creates an {@link SSLContext} that trusts all certificates.
     *
     * @param protocol The SSL protocol (e.g., TLS). If {@code null}, TLS will be used as the default.
     * @return The created {@link SSLContext}.
     * @throws InternalException if a {@link GeneralSecurityException} occurs.
     */
    public static SSLContext createTrustAnySSLContext(final String protocol) throws InternalException {
        return of().setProtocol(protocol)
                // Trust all servers
                .setTrustManagers(AnyTrustManager.TRUST_ANYS).build();
    }

    /**
     * Creates an {@link SSLContext}.
     *
     * @param protocol     The SSL protocol (e.g., TLS).
     * @param keyManager   The key manager, or {@code null} for the default.
     * @param trustManager The trust manager, or {@code null} for the default.
     * @return The created {@link SSLContext}.
     * @throws InternalException if a {@link GeneralSecurityException} occurs.
     */
    public static SSLContext createSSLContext(
            final String protocol,
            final KeyManager keyManager,
            final TrustManager trustManager) throws InternalException {
        return createSSLContext(
                protocol,
                keyManager == null ? null : new KeyManager[] { keyManager },
                trustManager == null ? null : new TrustManager[] { trustManager });
    }

    /**
     * Creates and initializes an {@link SSLContext}.
     *
     * @param keyStore The {@link KeyStore}.
     * @param password The password.
     * @return The created {@link SSLContext}.
     * @throws InternalException if a {@link GeneralSecurityException} occurs.
     */
    public static SSLContext createSSLContext(final KeyStore keyStore, final char[] password) throws InternalException {
        return createSSLContext(
                AnyKeyManager.getKeyManagers(keyStore, password),
                AnyTrustManager.getTrustManagers(keyStore));
    }

    /**
     * Creates and initializes an {@link SSLContext}.
     *
     * @param keyManagers   The key managers, or {@code null} for the default.
     * @param trustManagers The trust managers, or {@code null} for the default.
     * @return The created {@link SSLContext}.
     * @throws InternalException if a {@link GeneralSecurityException} occurs.
     */
    public static SSLContext createSSLContext(final KeyManager[] keyManagers, final TrustManager[] trustManagers)
            throws InternalException {
        return createSSLContext(null, keyManagers, trustManagers);
    }

    /**
     * Creates and initializes an {@link SSLContext}.
     *
     * @param protocol      The SSL protocol (e.g., TLS).
     * @param keyManagers   The key managers, or {@code null} for the default.
     * @param trustManagers The trust managers, or {@code null} for the default.
     * @return The created {@link SSLContext}.
     * @throws InternalException if a {@link GeneralSecurityException} occurs.
     */
    public static SSLContext createSSLContext(
            final String protocol,
            final KeyManager[] keyManagers,
            final TrustManager[] trustManagers) throws InternalException {
        return of().setProtocol(protocol).setKeyManagers(keyManagers).setTrustManagers(trustManagers).build();
    }

    /**
     * Creates a new {@link SSLSocketFactory} with the given trust manager.
     *
     * @param x509TrustManager The trust manager.
     * @return The new {@link SSLSocketFactory}.
     */
    public static SSLSocketFactory newSslSocketFactory(X509TrustManager x509TrustManager) {
        try {
            SSLContext sslContext = getSSLContext();
            sslContext.init(null, new TrustManager[] { x509TrustManager }, null);
            return sslContext.getSocketFactory();
        } catch (GeneralSecurityException e) {
            throw new AssertionError("No System TLS", e); // The system has no TLS. Just give up.
        }
    }

    /**
     * Creates a new default {@link X509TrustManager}.
     *
     * @return The new {@link X509TrustManager}.
     */
    public static X509TrustManager newTrustManager() {
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            return (X509TrustManager) trustManagers[0];
        } catch (GeneralSecurityException e) {
            throw new AssertionError("No System TLS", e); // The system has no TLS. Just give up.
        }
    }

    /**
     * Gets an {@link SSLContext} instance for the most recent version of TLS, falling back to older versions if
     * necessary.
     *
     * @return The {@link SSLContext} instance.
     */
    public static SSLContext getSSLContext() {
        try {
            return SSLContext.getInstance(Protocol.TLSv1_3.name);
        } catch (NoSuchAlgorithmException e) {
            try {
                return SSLContext.getInstance(Protocol.TLS.name);
            } catch (NoSuchAlgorithmException e2) {
                throw new IllegalStateException("No TLS provider", e);
            }
        }
    }

    /**
     * Sets the SSL protocol (e.g., TLS).
     *
     * @param protocol The protocol.
     * @return This builder.
     */
    public SSLContextBuilder setProtocol(final String protocol) {
        if (StringKit.isNotBlank(protocol)) {
            this.protocol = protocol;
        }
        return this;
    }

    /**
     * Sets the trust managers.
     *
     * @param trustManagers The list of trust managers.
     * @return This builder.
     */
    public SSLContextBuilder setTrustManagers(final TrustManager... trustManagers) {
        if (ArrayKit.isNotEmpty(trustManagers)) {
            this.trustManagers = trustManagers;
        }
        return this;
    }

    /**
     * Sets the JSSE key managers.
     *
     * @param keyManagers The JSSE key managers.
     * @return This builder.
     */
    public SSLContextBuilder setKeyManagers(final KeyManager... keyManagers) {
        if (ArrayKit.isNotEmpty(keyManagers)) {
            this.keyManagers = keyManagers;
        }
        return this;
    }

    /**
     * Sets the {@link SecureRandom}.
     *
     * @param secureRandom The {@link SecureRandom}.
     * @return This builder.
     */
    public SSLContextBuilder setSecureRandom(final SecureRandom secureRandom) {
        if (null != secureRandom) {
            this.secureRandom = secureRandom;
        }
        return this;
    }

    /**
     * Sets the security provider.
     *
     * @param provider The provider, or {@code null} to use the default or global provider.
     * @return This builder.
     */
    public SSLContextBuilder setProvider(final Provider provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Builds the {@link SSLContext}.
     *
     * @return The {@link SSLContext}.
     */
    @Override
    public SSLContext build() {
        return buildQuietly();
    }

    /**
     * Builds the {@link SSLContext}, throwing checked exceptions on failure.
     *
     * @return The {@link SSLContext}.
     * @throws NoSuchAlgorithmException if the specified protocol is not available.
     * @throws KeyManagementException   if initializing the context fails.
     */
    public SSLContext buildChecked() throws NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = null != this.provider ? SSLContext.getInstance(protocol, provider)
                : SSLContext.getInstance(protocol);
        sslContext.init(this.keyManagers, this.trustManagers, this.secureRandom);
        return sslContext;
    }

    /**
     * Builds the {@link SSLContext}, wrapping any {@link GeneralSecurityException} in an {@link InternalException}.
     *
     * @return The {@link SSLContext}.
     * @throws InternalException wrapping a {@link GeneralSecurityException}.
     */
    public SSLContext buildQuietly() throws InternalException {
        try {
            return buildChecked();
        } catch (final GeneralSecurityException e) {
            throw new InternalException(e);
        }
    }

}
