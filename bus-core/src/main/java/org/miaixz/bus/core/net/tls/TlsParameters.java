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
package org.miaixz.bus.core.net.tls;

import java.util.List;
import java.util.Optional;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.xyz.ArrayKit;
import org.miaixz.bus.core.xyz.StringKit;

/**
 * Utilities for applying common TLS parameters to JSSE engines.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public class TlsParameters {

    /**
     * HTTPS endpoint identification algorithm.
     */
    public static final String HTTPS_ENDPOINT_IDENTIFICATION = "HTTPS";

    /**
     * Keeps TLS parameter construction on the static API.
     */
    private TlsParameters() {
        // No initialization required.
    }

    /**
     * Applies client-side TLS parameters to an engine.
     *
     * @param engine               SSL engine
     * @param host                 peer host
     * @param protocols            enabled protocols
     * @param cipherSuites         enabled cipher suites
     * @param applicationProtocols ALPN application protocols
     * @param tlsExtensions        whether TLS extensions should be applied
     * @param verifyHostname       whether HTTPS endpoint identification should be enabled
     */
    public static void applyClient(
            final SSLEngine engine,
            final String host,
            final String[] protocols,
            final String[] cipherSuites,
            final String[] applicationProtocols,
            final boolean tlsExtensions,
            final boolean verifyHostname) {
        final SSLEngine checkedEngine = Assert.notNull(engine, "SSL engine must not be null");
        if (ArrayKit.isNotEmpty(protocols)) {
            checkedEngine.setEnabledProtocols(protocols);
        }
        if (ArrayKit.isNotEmpty(cipherSuites)) {
            checkedEngine.setEnabledCipherSuites(cipherSuites);
        }
        final SSLParameters parameters = checkedEngine.getSSLParameters();
        if (tlsExtensions) {
            parameters.setApplicationProtocols(applicationProtocols == null ? new String[0] : applicationProtocols);
            serverNames(host).ifPresent(parameters::setServerNames);
        }
        if (verifyHostname) {
            parameters.setEndpointIdentificationAlgorithm(HTTPS_ENDPOINT_IDENTIFICATION);
        }
        checkedEngine.setSSLParameters(parameters);
    }

    /**
     * Creates SNI server names when the host is a valid DNS name.
     *
     * @param host host
     * @return server names
     */
    public static Optional<List<SNIServerName>> serverNames(final String host) {
        if (StringKit.isBlank(host)) {
            return Optional.empty();
        }
        try {
            final List<SNIServerName> names = List.of(new SNIHostName(host));
            return Optional.of(names);
        } catch (final IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}
