/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.secure.factory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * A factory for creating server-side {@link SSLContext} instances from PEM-encoded certificate and private key files.
 * <p>
 * This factory supports loading certificates and private keys in PEM format, typically used for configuring SSL/TLS
 * servers.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class PemServerSSLContextFactory implements SSLContextFactory {

    /**
     * A list of PEM-encoded certificates in byte array format.
     */
    private List<byte[]> certificates = new ArrayList<>();
    /**
     * The PEM-encoded private key in byte array format.
     */
    private byte[] keyBytes;

    /**
     * Constructs a {@code PemServerSSLContextFactory} from a single input stream containing both certificate and
     * private key.
     *
     * @param fullPem an {@link InputStream} containing the full PEM-encoded certificate chain and private key
     * @throws IOException if an I/O error occurs while reading the stream
     */
    public PemServerSSLContextFactory(InputStream fullPem) throws IOException {
        readPem(fullPem);
    }

    /**
     * Constructs a {@code PemServerSSLContextFactory} from separate input streams for the certificate and private key.
     *
     * @param certPem an {@link InputStream} containing the PEM-encoded certificate chain
     * @param keyPem  an {@link InputStream} containing the PEM-encoded private key
     * @throws IOException if an I/O error occurs while reading the streams
     */
    public PemServerSSLContextFactory(InputStream certPem, InputStream keyPem) throws IOException {
        readPem(certPem);
        readPem(keyPem);
    }

    @Override
    public SSLContext create() throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        // Load certificates
        Certificate[] chain = new Certificate[certificates.size()];
        for (int i = 0; i < certificates.size(); i++) {
            chain[i] = cf.generateCertificate(new ByteArrayInputStream(certificates.get(i)));
        }
        // Load private key
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        // Generate KeyStore
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        ks.setKeyEntry("keyAlias", privateKey, new char[0], chain);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
        keyManagerFactory.init(ks, "".toCharArray());

        // Generate SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        certificates = null;
        keyBytes = null;
        return sslContext;
    }

    /**
     * Reads PEM-encoded data from an {@link InputStream} and populates the internal certificate and key byte arrays.
     *
     * @param inputStream the input stream containing PEM data
     * @throws IOException if an I/O error occurs during reading
     */
    private void readPem(InputStream inputStream) throws IOException {
        InputStreamReader reader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            switch (line) {
                case "-----BEGIN CERTIFICATE-----":
                case "-----BEGIN PRIVATE KEY-----":
                    sb.setLength(0);
                    break;

                case "-----END CERTIFICATE-----": {
                    certificates.add(Base64.getDecoder().decode(sb.toString()));
                    break;
                }

                case "-----END PRIVATE KEY-----": {
                    keyBytes = Base64.getDecoder().decode(sb.toString());
                    break;
                }

                default:
                    sb.append(line);
                    break;
            }
        }
    }

}
