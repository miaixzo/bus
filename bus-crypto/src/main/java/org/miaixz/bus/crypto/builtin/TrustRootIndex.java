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

import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.security.auth.x500.X500Principal;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.Keeper;

/**
 * In-memory trusted root lookup keyed by subject principal.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TrustRootIndex {

    /**
     * CA certificates by subject.
     */
    private final Map<X500Principal, Set<X509Certificate>> subjectToCaCerts;

    /**
     * Creates a trust root index.
     *
     * @param caCerts CA certificates
     */
    private TrustRootIndex(final Collection<X509Certificate> caCerts) {
        if (caCerts == null || caCerts.stream().anyMatch(certificate -> certificate == null)) {
            throw new ValidateException("CA certificates must be non-null and contain no null elements");
        }
        final LinkedHashMap<X500Principal, Set<X509Certificate>> indexed = new LinkedHashMap<>();
        for (final X509Certificate caCert : caCerts) {
            indexed.computeIfAbsent(caCert.getSubjectX500Principal(), ignored -> new LinkedHashSet<>()).add(caCert);
        }
        final LinkedHashMap<X500Principal, Set<X509Certificate>> copy = new LinkedHashMap<>();
        indexed.forEach((principal, certificates) -> copy.put(principal, Set.copyOf(certificates)));
        this.subjectToCaCerts = Map.copyOf(copy);
    }

    /**
     * Creates a trust root index.
     *
     * @param caCerts CA certificates
     * @return index
     */
    public static TrustRootIndex of(final X509Certificate... caCerts) {
        return new TrustRootIndex(Arrays
                .asList(Assert.notNull(caCerts, () -> new ValidateException("CA certificates must not be null"))));
    }

    /**
     * Creates a trust root index.
     *
     * @param caCerts CA certificates
     * @return index
     */
    public static TrustRootIndex of(final Collection<X509Certificate> caCerts) {
        return new TrustRootIndex(caCerts);
    }

    /**
     * Finds a trusted CA certificate that signed the supplied certificate.
     *
     * @param certificate certificate to verify
     * @return trusted signing certificate or null
     */
    public X509Certificate findByIssuerAndSignature(final X509Certificate certificate) {
        final X509Certificate checkedCertificate = Assert
                .notNull(certificate, () -> new ValidateException("Certificate must not be null"));
        final Set<X509Certificate> candidates = subjectToCaCerts.get(checkedCertificate.getIssuerX500Principal());
        if (candidates == null) {
            return null;
        }
        for (final X509Certificate candidate : candidates) {
            if (Keeper.isSignedBy(checkedCertificate, candidate)) {
                return candidate;
            }
        }
        return null;
    }

}
