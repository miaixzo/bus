/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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
package org.miaixz.bus.crypto.builtin;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.crypto.Keeper;

/**
 * Certificate chain cleaner backed by a trusted root index.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class CertificateChainCleaner {

    /**
     * Maximum signer depth accepted while building a chain.
     */
    private static final int MAX_SIGNERS = Normal._9;

    /**
     * Trust root index.
     */
    private final TrustRootIndex trustRootIndex;

    /**
     * Creates a chain cleaner.
     *
     * @param trustRootIndex trust root index
     */
    private CertificateChainCleaner(final TrustRootIndex trustRootIndex) {
        this.trustRootIndex = Assert
                .notNull(trustRootIndex, () -> new ValidateException("Trust root index must not be null"));
    }

    /**
     * Creates a chain cleaner.
     *
     * @param trustRootIndex trust root index
     * @return cleaner
     */
    public static CertificateChainCleaner of(final TrustRootIndex trustRootIndex) {
        return new CertificateChainCleaner(trustRootIndex);
    }

    /**
     * Creates a chain cleaner from CA certificates.
     *
     * @param caCerts CA certificates
     * @return cleaner
     */
    public static CertificateChainCleaner of(final X509Certificate... caCerts) {
        return new CertificateChainCleaner(TrustRootIndex.of(caCerts));
    }

    /**
     * Cleans a raw chain by following issuer signatures up to a trusted root.
     *
     * @param chain raw peer chain
     * @param host  host name
     * @return cleaned chain
     */
    public CertificateChain clean(final List<Certificate> chain, final String host) {
        validateHost(host);
        if (chain == null || chain.isEmpty() || chain.stream().anyMatch(certificate -> certificate == null)) {
            throw new ValidateException("Certificate chain must be non-null, non-empty, and contain no null elements");
        }
        final Deque<Certificate> queue = new ArrayDeque<>(chain);
        final ArrayList<Certificate> result = new ArrayList<>();
        result.add(x509(queue.removeFirst()));
        boolean foundTrustedCertificate = false;
        followIssuerChain: for (int c = Normal._0; c < MAX_SIGNERS; c++) {
            final X509Certificate toVerify = x509(result.getLast());
            final X509Certificate trustedCert = trustRootIndex.findByIssuerAndSignature(toVerify);
            if (trustedCert != null) {
                if (result.size() > Normal._1 || !toVerify.equals(trustedCert)) {
                    result.add(trustedCert);
                }
                if (Keeper.isSignedBy(trustedCert, trustedCert)) {
                    return CertificateChain.of(result);
                }
                foundTrustedCertificate = true;
                continue;
            }
            for (final Iterator<Certificate> iterator = queue.iterator(); iterator.hasNext();) {
                final X509Certificate signingCert = x509(iterator.next());
                if (Keeper.isSignedBy(toVerify, signingCert)) {
                    iterator.remove();
                    result.add(signingCert);
                    continue followIssuerChain;
                }
            }
            if (foundTrustedCertificate) {
                return CertificateChain.of(result);
            }
            throw new ProtocolException("Failed to find a trusted cert that signed " + toVerify);
        }
        throw new ProtocolException("Certificate chain too long: " + result);
    }

    /**
     * Casts to X509 certificate.
     *
     * @param certificate certificate
     * @return X509 certificate
     */
    private static X509Certificate x509(final Certificate certificate) {
        if (certificate instanceof X509Certificate x509) {
            return x509;
        }
        throw new ProtocolException("Certificate chain must contain X509 certificates");
    }

    /**
     * Validates host.
     *
     * @param host host
     */
    private static void validateHost(final String host) {
        if (StringKit.isBlank(host) || StringKit.containsAny(host, Symbol.C_CR, Symbol.C_LF)) {
            throw new ValidateException("Certificate host must be non-blank and single-line");
        }
    }

}
