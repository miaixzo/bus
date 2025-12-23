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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

import org.miaixz.bus.core.lang.exception.CryptoException;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * A {@link TrustManager} that trusts all client and server certificates.
 * <p>
 * <strong>Warning:</strong> This class should be used with caution, as it effectively disables certificate validation,
 * which can expose your application to man-in-the-middle attacks.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class AnyTrustManager extends X509ExtendedTrustManager {

    /**
     * Constructs a new AnyTrustManager. Utility class constructor for static access.
     */
    private AnyTrustManager() {
    }

    /**
     * A global singleton trust manager that trusts all client and server certificates.
     */
    public static final AnyTrustManager INSTANCE = new AnyTrustManager();
    /**
     * An array containing the singleton {@link #INSTANCE}, which trusts all certificates.
     */
    public static final X509TrustManager[] TRUST_ANYS = { INSTANCE };
    /**
     * An empty array of {@link X509Certificate}s.
     */
    private static final X509Certificate[] EMPTY_X509_CERTIFICATE_ARRAY = {};

    /**
     * Gets the default {@link X509TrustManager} (SunX509). This method is mainly used to get the
     * {@link X509TrustManager} for self-signed certificates.
     *
     * @return The default {@link X509TrustManager}, or {@code null} if not found.
     */
    public static X509TrustManager getDefaultTrustManager() {
        return getTrustManager(null, null);
    }

    /**
     * Gets the specified {@link X509TrustManager}. This method is mainly used to get the {@link X509TrustManager} for
     * self-signed certificates.
     *
     * @param keyStore The {@link KeyStore}.
     * @param provider The algorithm provider (e.g., Bouncy Castle), or {@code null} for the default.
     * @return The specified {@link X509TrustManager}, or {@code null} if not found.
     */
    public static X509TrustManager getTrustManager(final KeyStore keyStore, final Provider provider) {
        return getTrustManager(keyStore, null, provider);
    }

    /**
     * Gets the specified {@link X509TrustManager}. This method is mainly used to get the {@link X509TrustManager} for
     * self-signed certificates.
     *
     * @param keyStore  The {@link KeyStore}.
     * @param algorithm The algorithm name (e.g., "SunX509"), or {@code null} for the default.
     * @param provider  The algorithm provider (e.g., Bouncy Castle), or {@code null} for the default (SunJSSE).
     * @return The specified {@link X509TrustManager}, or {@code null} if not found.
     */
    public static X509TrustManager getTrustManager(
            final KeyStore keyStore,
            final String algorithm,
            final Provider provider) {
        final TrustManager[] tms = getTrustManagers(keyStore, algorithm, provider);
        for (final TrustManager tm : tms) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }

        return null;
    }

    /**
     * Gets the default {@link TrustManager}s (SunX509). This method is mainly used to get the {@link TrustManager}s for
     * self-signed certificates.
     *
     * @return An array of default {@link TrustManager}s.
     */
    public static TrustManager[] getDefaultTrustManagers() {
        return getTrustManagers(null);
    }

    /**
     * Gets the specified {@link TrustManager}s. This method is mainly used to get the {@link TrustManager}s for
     * self-signed certificates.
     *
     * @param keyStore The {@link KeyStore}.
     * @return An array of {@link TrustManager}s.
     */
    public static TrustManager[] getTrustManagers(final KeyStore keyStore) {
        return getTrustManagers(keyStore, null, null);
    }

    /**
     * Gets the specified {@link TrustManager}s. This method is mainly used to get the {@link TrustManager}s for
     * self-signed certificates.
     *
     * @param keyStore  The {@link KeyStore}.
     * @param algorithm The algorithm name (e.g., "SunX509"), or {@code null} for the default.
     * @param provider  The algorithm provider (e.g., Bouncy Castle), or {@code null} for the default (SunJSSE).
     * @return An array of {@link TrustManager}s.
     */
    public static TrustManager[] getTrustManagers(
            final KeyStore keyStore,
            final String algorithm,
            final Provider provider) {
        return getTrustManagerFactory(keyStore, algorithm, provider).getTrustManagers();
    }

    /**
     * Gets the specified {@link TrustManagerFactory}.
     *
     * @param keyStore  The {@link KeyStore}.
     * @param algorithm The algorithm name (e.g., "SunX509"), or {@code null} for the default.
     * @param provider  The algorithm provider (e.g., Bouncy Castle), or {@code null} for the default (SunJSSE).
     * @return The specified {@link TrustManagerFactory}.
     * @throws CryptoException if the algorithm is not found or the factory cannot be initialized.
     */
    public static TrustManagerFactory getTrustManagerFactory(
            final KeyStore keyStore,
            String algorithm,
            final Provider provider) {
        final TrustManagerFactory tmf;

        if (StringKit.isEmpty(algorithm)) {
            algorithm = TrustManagerFactory.getDefaultAlgorithm();
        }
        try {
            if (null == provider) {
                tmf = TrustManagerFactory.getInstance(algorithm);
            } else {
                tmf = TrustManagerFactory.getInstance(algorithm, provider);
            }
        } catch (final NoSuchAlgorithmException e) {
            throw new CryptoException(e);
        }
        try {
            tmf.init(keyStore);
        } catch (final KeyStoreException e) {
            throw new CryptoException(e);
        }

        return tmf;
    }

    /**
     * Creates a {@link KeyStore} from the given certificates.
     *
     * @param certs The certificates.
     * @return The created {@link KeyStore}.
     * @throws KeyStoreException if the KeyStore cannot be created.
     */
    public static KeyStore createKeyStore(X509Certificate... certs) throws KeyStoreException {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try {
            ks.load(null);
        } catch (IOException | CertificateException | NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        for (X509Certificate cert : certs)
            ks.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);
        return ks;
    }

    /**
     * Loads a {@link KeyStore} from a URL.
     *
     * @param type     The type of the KeyStore.
     * @param url      The URL of the KeyStore.
     * @param password The password for the KeyStore.
     * @return The loaded {@link KeyStore}.
     * @throws IOException              if an I/O error occurs.
     * @throws KeyStoreException        if the KeyStore cannot be loaded.
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found.
     * @throws CertificateException     if any of the certificates in the keystore could not be loaded.
     */
    public static KeyStore loadKeyStore(String type, String url, String password)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        return loadKeyStore(type, url, password.toCharArray());
    }

    /**
     * Loads a {@link KeyStore} from a URL.
     *
     * @param type     The type of the KeyStore.
     * @param url      The URL of the KeyStore.
     * @param password The password for the KeyStore.
     * @return The loaded {@link KeyStore}.
     * @throws IOException              if an I/O error occurs.
     * @throws KeyStoreException        if the KeyStore cannot be loaded.
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found.
     * @throws CertificateException     if any of the certificates in the keystore could not be loaded.
     */
    public static KeyStore loadKeyStore(String type, String url, char[] password)
            throws IOException, KeyStoreException, NoSuchAlgorithmException, CertificateException {
        KeyStore ks = KeyStore.getInstance(type);
        try (InputStream in = IoKit.openFileOrURL(url)) {
            ks.load(in, password);
        }
        return ks;
    }

    /**
     * Creates a {@link KeyManager}.
     *
     * @param type          The type of the KeyStore.
     * @param url           The URL of the KeyStore.
     * @param storePassword The password for the KeyStore.
     * @param keyPassword   The password for the key.
     * @return The created {@link KeyManager}.
     * @throws UnrecoverableKeyException if the key cannot be recovered.
     * @throws KeyStoreException         if the KeyStore cannot be loaded.
     * @throws NoSuchAlgorithmException  if the algorithm used to check the integrity of the keystore cannot be found.
     * @throws CertificateException      if any of the certificates in the keystore could not be loaded.
     * @throws IOException               if an I/O error occurs.
     */
    public static KeyManager createKeyManager(String type, String url, char[] storePassword, char[] keyPassword)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException {
        return createKeyManager(loadKeyStore(type, url, storePassword), keyPassword);
    }

    /**
     * Creates a {@link KeyManager}.
     *
     * @param type          The type of the KeyStore.
     * @param url           The URL of the KeyStore.
     * @param storePassword The password for the KeyStore.
     * @param keyPassword   The password for the key.
     * @return The created {@link KeyManager}.
     * @throws UnrecoverableKeyException if the key cannot be recovered.
     * @throws KeyStoreException         if the KeyStore cannot be loaded.
     * @throws NoSuchAlgorithmException  if the algorithm used to check the integrity of the keystore cannot be found.
     * @throws CertificateException      if any of the certificates in the keystore could not be loaded.
     * @throws IOException               if an I/O error occurs.
     */
    public static KeyManager createKeyManager(String type, String url, String storePassword, String keyPassword)
            throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException, CertificateException,
            IOException {
        return createKeyManager(loadKeyStore(type, url, storePassword), keyPassword);
    }

    /**
     * Creates a {@link KeyManager}.
     *
     * @param ks       The {@link KeyStore}.
     * @param password The password for the key.
     * @return The created {@link KeyManager}.
     * @throws UnrecoverableKeyException if the key cannot be recovered.
     * @throws KeyStoreException         if the KeyStore cannot be loaded.
     */
    public static KeyManager createKeyManager(KeyStore ks, String password)
            throws UnrecoverableKeyException, KeyStoreException {
        return createKeyManager(ks, password.toCharArray());
    }

    /**
     * Creates a {@link KeyManager}.
     *
     * @param ks       The {@link KeyStore}.
     * @param password The password for the key.
     * @return The created {@link KeyManager}.
     * @throws UnrecoverableKeyException if the key cannot be recovered.
     * @throws KeyStoreException         if the KeyStore cannot be loaded.
     */
    public static KeyManager createKeyManager(KeyStore ks, char[] password)
            throws UnrecoverableKeyException, KeyStoreException {
        try {
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);
            KeyManager[] kms = kmf.getKeyManagers();
            return kms.length > 0 ? kms[0] : null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a {@link TrustManager}.
     *
     * @param ks The {@link KeyStore}.
     * @return The created {@link TrustManager}.
     * @throws KeyStoreException if the KeyStore cannot be loaded.
     */
    public static TrustManager createTrustManager(KeyStore ks) throws KeyStoreException {
        try {
            TrustManagerFactory kmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            kmf.init(ks);
            TrustManager[] tms = kmf.getTrustManagers();
            return tms.length > 0 ? tms[0] : null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a {@link TrustManager} from the given certificates.
     *
     * @param certs The certificates.
     * @return The created {@link TrustManager}.
     * @throws KeyStoreException if the KeyStore cannot be created.
     */
    public static TrustManager createTrustManager(X509Certificate... certs) throws KeyStoreException {
        return createTrustManager(createKeyStore(certs));
    }

    /**
     * Creates a {@link TrustManager} from a {@link KeyStore} loaded from a URL.
     *
     * @param type     The type of the KeyStore.
     * @param url      The URL of the KeyStore.
     * @param password The password for the KeyStore.
     * @return The created {@link TrustManager}.
     * @throws KeyStoreException        if the KeyStore cannot be loaded.
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found.
     * @throws CertificateException     if any of the certificates in the keystore could not be loaded.
     * @throws IOException              if an I/O error occurs.
     */
    public static TrustManager createTrustManager(String type, String url, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return createTrustManager(loadKeyStore(type, url, password));
    }

    /**
     * Creates a {@link TrustManager} from a {@link KeyStore} loaded from a URL.
     *
     * @param type     The type of the KeyStore.
     * @param url      The URL of the KeyStore.
     * @param password The password for the KeyStore.
     * @return The created {@link TrustManager}.
     * @throws KeyStoreException        if the KeyStore cannot be loaded.
     * @throws NoSuchAlgorithmException if the algorithm used to check the integrity of the keystore cannot be found.
     * @throws CertificateException     if any of the certificates in the keystore could not be loaded.
     * @throws IOException              if an I/O error occurs.
     */
    public static TrustManager createTrustManager(String type, String url, String password)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException, IOException {
        return createTrustManager(loadKeyStore(type, url, password));
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return EMPTY_X509_CERTIFICATE_ARRAY;
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] chain, final String authType) {
        // Trust all clients
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] chain, final String authType) {
        // Trust all servers
    }

    @Override
    public void checkClientTrusted(final X509Certificate[] x509Certificates, final String s, final Socket socket) {
        // Trust all clients
    }

    @Override
    public void checkServerTrusted(final X509Certificate[] x509Certificates, final String s, final Socket socket) {
        // Trust all servers
    }

    @Override
    public void checkClientTrusted(
            final X509Certificate[] x509Certificates,
            final String s,
            final SSLEngine sslEngine) {
        // Trust all clients
    }

    @Override
    public void checkServerTrusted(
            final X509Certificate[] x509Certificates,
            final String s,
            final SSLEngine sslEngine) {
        // Trust all servers
    }

}
