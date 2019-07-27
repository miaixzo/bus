package org.aoju.bus.http.internal.tls;

import java.security.cert.X509Certificate;

/**
 * @author aoju.org
 * @version 3.0.1
 * @group 839128
 * @since JDK 1.8
 */
public interface TrustRootIndex {
    /**
     * Returns the trusted CA certificate that signed {@code cert}.
     */
    X509Certificate findByIssuerAndSignature(X509Certificate cert);
}
