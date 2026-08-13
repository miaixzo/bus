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
package org.miaixz.bus.auth.bridge;

import org.miaixz.bus.auth.Builder;
import org.miaixz.bus.auth.Policy;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.tls.TlsClientAuth;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.guard.route.AddressPolicy;
import org.miaixz.bus.fabric.network.tls.TlsSettings;

/**
 * Immutable authentication transport constraints composed over Fabric policies.
 *
 * @param addressPolicy        Fabric destination and peer address policy
 * @param tlsSettings          Fabric TLS handshake settings
 * @param timeout              Fabric connect, read, write, call, and close timeouts
 * @param requireLocalIdentity whether a local TLS identity is mandatory
 * @param requireStartTls      whether a clear-text session must upgrade with STARTTLS
 * @param redirectLimit        maximum followed redirects, from 0 through {@link Integer#MAX_VALUE}
 * @param maxResponseBytes     maximum buffered response size in bytes, from 1 through {@link Integer#MAX_VALUE}
 * @author Kimi Liu
 */
public record TransportPolicy(AddressPolicy addressPolicy, TlsSettings tlsSettings, Timeout timeout,
        boolean requireLocalIdentity, boolean requireStartTls, int redirectLimit, long maxResponseBytes)
        implements Policy {

    /**
     * Validates all Fabric policies and authentication-specific numeric bounds.
     *
     * @throws ValidateException if a policy is null or a numeric constraint is outside its documented range
     */
    public TransportPolicy {
        if (addressPolicy == null || tlsSettings == null || timeout == null || redirectLimit < 0 || maxResponseBytes < 1
                || maxResponseBytes > Integer.MAX_VALUE) {
            throw new ValidateException("Authentication transport policy is incomplete or invalid");
        }
    }

    /**
     * Adds the Fabric timeout and numeric authentication limits to typed options.
     *
     * @param options non-null source options
     * @return immutable updated options
     */
    @Override
    public Options from(final Options options) {
        return options.with(org.miaixz.bus.fabric.Builder.OPTION_TIMEOUT, timeout)
                .with(Builder.OPTION_REDIRECT_LIMIT, redirectLimit)
                .with(Builder.OPTION_MAX_RESPONSE_BYTES, Math.toIntExact(maxResponseBytes));
    }

    /**
     * Returns a policy that requires a local TLS identity.
     *
     * @return immutable policy copy
     */
    public TransportPolicy mutualTls() {
        return new TransportPolicy(addressPolicy, tlsSettings, timeout, true, requireStartTls, redirectLimit,
                maxResponseBytes);
    }

    /**
     * Returns a policy whose TLS settings require peer client certificates.
     *
     * @return immutable policy copy with {@link TlsClientAuth#REQUIRE}
     */
    public TransportPolicy requireClientCertificates() {
        final TlsSettings source = tlsSettings;
        final TlsSettings required = TlsSettings.builder().versions(source.versions()).ciphers(source.ciphers())
                .clientAuth(TlsClientAuth.REQUIRE).verifyHostname(source.verifyHostname())
                .certificate(source.certificate()).applicationProtocols(source.applicationProtocols())
                .supportsTlsExtensions(source.supportsTlsExtensions()).build();
        return new TransportPolicy(addressPolicy, required, timeout, false, requireStartTls, redirectLimit,
                maxResponseBytes);
    }

}
