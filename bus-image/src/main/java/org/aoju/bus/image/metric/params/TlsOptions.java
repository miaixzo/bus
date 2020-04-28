/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.image.metric.params;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class TlsOptions {

    // cipherSuites
    public static final String[] TLS =
            {"SSL_RSA_WITH_NULL_SHA", "TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA"};
    public static final String[] TLS_NULL = {"SSL_RSA_WITH_NULL_SHA"};
    public static final String[] TLS_3DES = {"SSL_RSA_WITH_3DES_EDE_CBC_SHA"};
    public static final String[] TLS_AES = {"TLS_RSA_WITH_AES_128_CBC_SHA", "SSL_RSA_WITH_3DES_EDE_CBC_SHA"};

    // tlsProtocols
    public static final String[] defaultProtocols = {"TLSv1", "SSLv3"};
    public static final String[] tls1 = {"TLSv1"};
    public static final String[] tls11 = {"TLSv1.1"};
    public static final String[] tls12 = {"TLSv1.2"};
    public static final String[] ssl3 = {"SSLv3"}; // deprecated
    public static final String[] ssl2Hello = {"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2"};

    private final String[] cipherSuites;
    private final String[] tlsProtocols;

    private final boolean tlsNeedClientAuth;

    private final String keystoreURL;
    private final String keystoreType;
    private final String keystorePass;
    private final String keyPass;
    private final String truststoreURL;
    private final String truststoreType;
    private final String truststorePass;

    public TlsOptions(boolean tlsNeedClientAuth, String keystoreURL, String keystoreType, String keystorePass,
                      String keyPass, String truststoreURL, String truststoreType, String truststorePass) {
        this(TLS, defaultProtocols, tlsNeedClientAuth, keystoreURL, keystoreType, keystorePass, keyPass, truststoreURL,
                truststoreType, truststorePass);
    }

    public TlsOptions(String[] cipherSuites, String[] tlsProtocols, boolean tlsNeedClientAuth, String keystoreURL,
                      String keystoreType, String keystorePass, String keyPass, String truststoreURL, String truststoreType,
                      String truststorePass) {
        if (cipherSuites == null) {
            throw new IllegalArgumentException("cipherSuites cannot be null");
        }
        this.cipherSuites = cipherSuites;
        this.tlsProtocols = tlsProtocols;
        this.tlsNeedClientAuth = tlsNeedClientAuth;
        this.keystoreURL = keystoreURL;
        this.keystoreType = keystoreType;
        this.keystorePass = keystorePass;
        this.keyPass = keyPass;
        this.truststoreURL = truststoreURL;
        this.truststoreType = truststoreType;
        this.truststorePass = truststorePass;
    }

    public boolean isTlsNeedClientAuth() {
        return tlsNeedClientAuth;
    }

    public String[] getCipherSuites() {
        return cipherSuites;
    }

    public String[] getTlsProtocols() {
        return tlsProtocols;
    }

    public String getKeystoreURL() {
        return keystoreURL;
    }

    public String getKeystoreType() {
        return keystoreType;
    }

    public String getKeystorePass() {
        return keystorePass;
    }

    public String getKeyPass() {
        return keyPass;
    }

    public String getTruststoreURL() {
        return truststoreURL;
    }

    public String getTruststoreType() {
        return truststoreType;
    }

    public String getTruststorePass() {
        return truststorePass;
    }

}
