/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.secure;

import javax.net.ssl.SSLPeerUnverifiedException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * A certificate chain cleaner that uses a set of trusted root certificates to build the trusted
 * chain. This class duplicates the clean chain building performed during the TLS handshake. We
 * prefer other mechanisms where they exist, such as with
 * {@code org.aoju.bus.http.internal.platform.AndroidPlatform.AndroidCertificateChainCleaner}.
 *
 * <p>This class includes code from <a href="https://conscrypt.org/">Conscrypt's</a> {@code
 * TrustManagerImpl} and {@code TrustedCertificateIndex}.
 *
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
public final class BasicCertificateChainCleaner extends CertificateChainCleaner {
    /**
     * The maximum number of signers in a chain. We use 9 for consistency with OpenSSL.
     */
    private static final int MAX_SIGNERS = 9;

    private final TrustRootIndex trustRootIndex;

    public BasicCertificateChainCleaner(TrustRootIndex trustRootIndex) {
        this.trustRootIndex = trustRootIndex;
    }

    @Override
    public List<Certificate> clean(List<Certificate> chain, String hostname)
            throws SSLPeerUnverifiedException {
        Deque<Certificate> queue = new ArrayDeque<>(chain);
        List<Certificate> result = new ArrayList<>();
        result.add(queue.removeFirst());
        boolean foundTrustedCertificate = false;

        followIssuerChain:
        for (int c = 0; c < MAX_SIGNERS; c++) {
            X509Certificate toVerify = (X509Certificate) result.get(result.size() - 1);

            // If this cert has been signed by a trusted cert, use that. Add the trusted certificate to
            // the end of the chain unless it's already present. (That would happen if the first
            // certificate in the chain is itself a self-signed and trusted CA certificate.)
            X509Certificate trustedCert = trustRootIndex.findByIssuerAndSignature(toVerify);
            if (trustedCert != null) {
                if (result.size() > 1 || !toVerify.equals(trustedCert)) {
                    result.add(trustedCert);
                }
                if (verifySignature(trustedCert, trustedCert)) {
                    return result; // The self-signed cert is a root CA. We're done.
                }
                foundTrustedCertificate = true;
                continue;
            }

            // Search for the certificate in the chain that signed this certificate. This is typically
            // the next element in the chain, but it could be any element.
            for (Iterator<Certificate> i = queue.iterator(); i.hasNext(); ) {
                X509Certificate signingCert = (X509Certificate) i.next();
                if (verifySignature(toVerify, signingCert)) {
                    i.remove();
                    result.add(signingCert);
                    continue followIssuerChain;
                }
            }

            // We've reached the end of the chain. If any cert in the chain is trusted, we're done.
            if (foundTrustedCertificate) {
                return result;
            }

            // The last link isn't trusted. Fail.
            throw new SSLPeerUnverifiedException(
                    "Failed to find a trusted cert that signed " + toVerify);
        }

        throw new SSLPeerUnverifiedException("Certificate chain too long: " + result);
    }

    private boolean verifySignature(X509Certificate toVerify, X509Certificate signingCert) {
        if (!toVerify.getIssuerDN().equals(signingCert.getSubjectDN())) return false;
        try {
            toVerify.verify(signingCert.getPublicKey());
            return true;
        } catch (GeneralSecurityException verifyFailed) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return trustRootIndex.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        return other instanceof BasicCertificateChainCleaner
                && ((BasicCertificateChainCleaner) other).trustRootIndex.equals(trustRootIndex);
    }

}
