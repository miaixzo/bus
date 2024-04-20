/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org and other contributors.                    *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.image.metric;

import org.miaixz.bus.core.toolkit.IoKit;
import org.miaixz.bus.core.toolkit.StreamKit;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class SSLManagerFactory {

    public static KeyStore createKeyStore(X509Certificate... certs)
            throws KeyStoreException {
        KeyStore ks = KeyStore.getInstance("JKS");
        try {
            ks.load(null);
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        } catch (CertificateException e) {
            throw new AssertionError(e);
        }
        for (X509Certificate cert : certs)
            ks.setCertificateEntry(cert.getSubjectX500Principal().getName(), cert);
        return ks;
    }

    public static KeyStore loadKeyStore(String type, String url, String password)
            throws IOException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException {
        return loadKeyStore(type, url, password.toCharArray());
    }

    public static KeyStore loadKeyStore(String type, String url, char[] password)
            throws IOException, KeyStoreException, NoSuchAlgorithmException,
            CertificateException {
        KeyStore ks = KeyStore.getInstance(type);
        InputStream in = StreamKit.openFileOrURL(url);
        try {
            ks.load(in, password);
        } finally {
            IoKit.close(in);
        }
        return ks;
    }

    public static KeyManager createKeyManager(String type, String url,
                                              char[] storePassword, char[] keyPassword)
            throws UnrecoverableKeyException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {
        return createKeyManager(loadKeyStore(type, url, storePassword), keyPassword);
    }

    public static KeyManager createKeyManager(String type, String url,
                                              String storePassword, String keyPassword)
            throws UnrecoverableKeyException, KeyStoreException,
            NoSuchAlgorithmException, CertificateException, IOException {
        return createKeyManager(loadKeyStore(type, url, storePassword), keyPassword);
    }

    public static KeyManager createKeyManager(KeyStore ks, String password)
            throws UnrecoverableKeyException, KeyStoreException {
        return createKeyManager(ks, password.toCharArray());
    }

    public static KeyManager createKeyManager(KeyStore ks, char[] password)
            throws UnrecoverableKeyException, KeyStoreException {
        try {
            KeyManagerFactory kmf = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(ks, password);
            KeyManager[] kms = kmf.getKeyManagers();
            return kms.length > 0 ? kms[0] : null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static TrustManager createTrustManager(KeyStore ks)
            throws KeyStoreException {
        try {
            TrustManagerFactory kmf = TrustManagerFactory
                    .getInstance(TrustManagerFactory.getDefaultAlgorithm());
            kmf.init(ks);
            TrustManager[] tms = kmf.getTrustManagers();
            return tms.length > 0 ? tms[0] : null;
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    public static TrustManager createTrustManager(X509Certificate... certs)
            throws KeyStoreException {
        return createTrustManager(createKeyStore(certs));
    }

    public static TrustManager createTrustManager(String type, String url, char[] password)
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {
        return createTrustManager(loadKeyStore(type, url, password));
    }

    public static TrustManager createTrustManager(String type, String url, String password)
            throws KeyStoreException, NoSuchAlgorithmException,
            CertificateException, IOException {
        return createTrustManager(loadKeyStore(type, url, password));
    }

}
