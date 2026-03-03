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
package org.miaixz.bus.http.secure;

import javax.security.auth.x500.X500Principal;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * An in-memory index of trusted root certificates. This class holds a cache of CA certificates, indexed by their
 * subject distinguished name, to allow for quick lookups when building a certificate chain.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class BasicTrustRootIndex implements TrustRootIndex {

    /** A map from a certificate's subject X.500 principal to the certificate itself. */
    private final Map<X500Principal, Set<X509Certificate>> subjectToCaCerts;

    /**
     * Creates a new index from a set of CA certificates.
     *
     * @param caCerts The trusted CA certificates.
     */
    public BasicTrustRootIndex(X509Certificate... caCerts) {
        subjectToCaCerts = new LinkedHashMap<>();
        for (X509Certificate caCert : caCerts) {
            X500Principal subject = caCert.getSubjectX500Principal();
            Set<X509Certificate> subjectCaCerts = subjectToCaCerts.get(subject);
            if (null == subjectCaCerts) {
                subjectCaCerts = new LinkedHashSet<>(1);
                subjectToCaCerts.put(subject, subjectCaCerts);
            }
            subjectCaCerts.add(caCert);
        }
    }

    /**
     * Finds a trusted CA certificate that signed the given certificate. This method works by first finding candidate
     * certificates based on the issuer's distinguished name and then verifying the signature.
     *
     * @param cert The certificate for which to find the issuer.
     * @return The signing CA certificate, or {@code null} if no trusted issuer is found.
     */
    @Override
    public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
        X500Principal issuer = cert.getIssuerX500Principal();
        Set<X509Certificate> subjectCaCerts = subjectToCaCerts.get(issuer);
        if (null == subjectCaCerts)
            return null;

        for (X509Certificate caCert : subjectCaCerts) {
            PublicKey publicKey = caCert.getPublicKey();
            try {
                cert.verify(publicKey);
                return caCert;
            } catch (Exception ignored) {
                // Signature verification failed, try the next certificate.
            }
        }

        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this)
            return true;
        return other instanceof BasicTrustRootIndex
                && ((BasicTrustRootIndex) other).subjectToCaCerts.equals(subjectToCaCerts);
    }

    @Override
    public int hashCode() {
        return subjectToCaCerts.hashCode();
    }

}
