/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
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
