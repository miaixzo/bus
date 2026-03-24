/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.         ~
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
package org.miaixz.bus.socket.secure.factory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

/**
 * A factory for creating client-side {@link SSLContext} instances.
 * <p>
 * This factory can be configured with a trust store to validate server certificates. If no trust store is provided, it
 * will use a default {@link X509TrustManager} that trusts all certificates.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class ClientSSLContextFactory implements SSLContextFactory {

    /**
     * The input stream for the trust store file.
     */
    private InputStream trustInputStream;
    /**
     * The password for the trust store.
     */
    private String trustPassword;

    /**
     * Constructs a default {@code ClientSSLContextFactory} that trusts all server certificates.
     */
    public ClientSSLContextFactory() {
    }

    /**
     * Constructs a {@code ClientSSLContextFactory} with a specified trust store.
     *
     * @param trustInputStream the input stream to the trust store file (e.g., JKS format)
     * @param trustPassword    the password for the trust store
     */
    public ClientSSLContextFactory(InputStream trustInputStream, String trustPassword) {
        this.trustInputStream = trustInputStream;
        this.trustPassword = trustPassword;
    }

    @Override
    public SSLContext create() throws Exception {
        TrustManager[] trustManagers;
        if (trustInputStream != null) {
            KeyStore ts = KeyStore.getInstance("JKS");
            ts.load(trustInputStream, trustPassword.toCharArray());
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ts);
            trustManagers = tmf.getTrustManagers();
        } else {
            // Default to trusting all certificates if no trust store is provided
            trustManagers = new TrustManager[] { new X509TrustManager() {

                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                    // Trust all client certificates
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                    // Trust all server certificates
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            } };
        }
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new SecureRandom());
        return sslContext;
    }

}
