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
package org.miaixz.bus.fabric.network.tls.cert;

import java.security.cert.Certificate;
import java.util.List;

import org.miaixz.bus.core.lang.exception.ValidateException;

/**
 * Immutable TLS certificate chain snapshot.
 *
 * @param certificates certificate snapshot
 * @author Kimi Liu
 * @since Java 21+
 */
public record CertificateChain(List<Certificate> certificates) {

    /**
     * Creates a certificate chain.
     */
    public CertificateChain {
        if (certificates == null || certificates.stream().anyMatch(certificate -> certificate == null)) {
            throw new ValidateException("Certificates must be non-null and contain no null elements");
        }
        certificates = List.copyOf(certificates);
    }

    /**
     * Creates a certificate chain.
     *
     * @param certificates certificates
     * @return certificate chain
     */
    public static CertificateChain of(final List<Certificate> certificates) {
        return new CertificateChain(certificates);
    }

    /**
     * Returns certificate snapshot.
     *
     * @return certificates
     */
    @Override
    public List<Certificate> certificates() {
        return List.copyOf(certificates);
    }

    /**
     * Returns the leaf certificate.
     *
     * @return leaf certificate
     */
    public Certificate leaf() {
        if (certificates.isEmpty()) {
            throw new ValidateException("Certificate chain must contain a leaf certificate");
        }
        return certificates.getFirst();
    }

    /**
     * Returns whether this chain has no certificates.
     *
     * @return true when empty
     */
    public boolean empty() {
        return certificates.isEmpty();
    }

}
