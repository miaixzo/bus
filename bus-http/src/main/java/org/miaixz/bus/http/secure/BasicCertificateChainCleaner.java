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

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.net.ssl.SSLPeerUnverifiedException;

/**
 * A certificate chain cleaner that builds a trust chain starting from a server certificate, following the chain of
 * issuers until a trusted root certificate is found. This class duplicates the clean chain building that is performed
 * by the TLS implementation. It is used when other mechanisms, such as those provided by the platform (e.g., on
 * Android), are not available.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicCertificateChainCleaner extends CertificateChainCleaner {

    /**
     * The maximum number of signers in a certificate chain. We use 9 for consistency with OpenSSL.
     */
    private static final int MAX_SIGNERS = 9;

    /**
     * An index of trusted root certificates.
     */
    private final TrustRootIndex trustRootIndex;

    /**
     * Constructs a new BasicCertificateChainCleaner.
     *
     * @param trustRootIndex An index of trusted root certificates to use for building the chain.
     */
    public BasicCertificateChainCleaner(TrustRootIndex trustRootIndex) {
        this.trustRootIndex = trustRootIndex;
    }

    /**
     * Returns a cleaned chain for {@code chain}. This method throws if the complete chain to a trusted CA certificate
     * cannot be constructed. This is unexpected unless the trust root index in this class has a different trust manager
     * than what was used to establish {@code chain}.
     *
     * @param chain    The raw certificate chain from the peer.
     * @param hostname The hostname of the peer.
     * @return A validated and cleaned certificate chain.
     * @throws SSLPeerUnverifiedException if the chain cannot be validated.
     */
    @Override
    public List<Certificate> clean(List<Certificate> chain, String hostname) throws SSLPeerUnverifiedException {
        Deque<Certificate> queue = new ArrayDeque<>(chain);
        List<Certificate> result = new ArrayList<>();
        result.add(queue.removeFirst());
        boolean foundTrustedCertificate = false;

        followIssuerChain: for (int c = 0; c < MAX_SIGNERS; c++) {
            X509Certificate toVerify = (X509Certificate) result.get(result.size() - 1);

            // If this certificate is signed by a trusted certificate, use it. Add the trusted
            // certificate to the end of the chain unless it's already present (which happens if
            // the first certificate in the chain is a self-signed, trusted CA).
            X509Certificate trustedCert = trustRootIndex.findByIssuerAndSignature(toVerify);
            if (trustedCert != null) {
                if (result.size() > 1 || !toVerify.equals(trustedCert)) {
                    result.add(trustedCert);
                }
                if (verifySignature(trustedCert, trustedCert)) {
                    // A self-signed certificate is a root CA.
                    return result;
                }
                foundTrustedCertificate = true;
                continue;
            }

            // Search for a certificate in the chain that signed this certificate. This is typically
            // the next element in the chain, but it could be any element.
            for (Iterator<Certificate> i = queue.iterator(); i.hasNext();) {
                X509Certificate signingCert = (X509Certificate) i.next();
                if (verifySignature(toVerify, signingCert)) {
                    i.remove();
                    result.add(signingCert);
                    continue followIssuerChain;
                }
            }

            // We've reached the end of the chain. If any certificate in the chain is trusted, we're done.
            if (foundTrustedCertificate) {
                return result;
            }

            // The last link isn't trusted. Fail.
            throw new SSLPeerUnverifiedException("Failed to find a trusted cert that signed " + toVerify);
        }

        throw new SSLPeerUnverifiedException("Certificate chain too long: " + result);
    }

    /**
     * Returns true if {@code toVerify} was signed by {@code signingCert}'s public key.
     *
     * @param toVerify    The certificate to be verified.
     * @param signingCert The potential signing certificate.
     * @return true if the signature is valid, false otherwise.
     */
    private boolean verifySignature(X509Certificate toVerify, X509Certificate signingCert) {
        if (!toVerify.getIssuerDN().equals(signingCert.getSubjectDN()))
            return false;
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
        if (other == this)
            return true;
        return other instanceof BasicCertificateChainCleaner
                && ((BasicCertificateChainCleaner) other).trustRootIndex.equals(trustRootIndex);
    }

}
