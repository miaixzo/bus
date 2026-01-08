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
package org.miaixz.bus.http.secure;

import org.miaixz.bus.http.accord.platform.Platform;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.X509TrustManager;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

/**
 * Computes a clean certificate chain from the raw array provided by a TLS implementation. A cleaned chain is a list of
 * certificates where the first element is the server's certificate, each certificate is signed by the one that follows,
 * and the final certificate is a trusted CA. This class can be used to omit unexpected certificates and to find the
 * trust anchor for certificate pinning.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class CertificateChainCleaner {

    /**
     * Creates a {@code CertificateChainCleaner} that uses the platform's default trust manager.
     *
     * @param trustManager The trust manager to use.
     * @return A new platform-specific {@code CertificateChainCleaner}.
     */
    public static CertificateChainCleaner get(X509TrustManager trustManager) {
        return Platform.get().buildCertificateChainCleaner(trustManager);
    }

    /**
     * Creates a {@code CertificateChainCleaner} that uses a custom set of CA certificates.
     *
     * @param caCerts The custom set of trusted CA certificates.
     * @return A new {@code CertificateChainCleaner}.
     */
    public static CertificateChainCleaner get(X509Certificate... caCerts) {
        return new BasicCertificateChainCleaner(new BasicTrustRootIndex(caCerts));
    }

    /**
     * Takes a raw certificate chain and returns a cleaned, validated chain up to a trusted root.
     *
     * @param chain    The raw, possibly unordered, certificate chain from the peer.
     * @param hostname The hostname of the peer, used for verification.
     * @return A list of certificates representing the validated chain.
     * @throws SSLPeerUnverifiedException if the chain cannot be validated against a trusted root.
     */
    public abstract List<Certificate> clean(List<Certificate> chain, String hostname) throws SSLPeerUnverifiedException;

}
