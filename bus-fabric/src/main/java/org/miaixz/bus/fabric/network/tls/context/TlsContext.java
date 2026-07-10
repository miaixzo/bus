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
package org.miaixz.bus.fabric.network.tls.context;

import java.util.List;

import javax.net.ssl.SNIHostName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.network.tls.TlsClientAuth;
import org.miaixz.bus.fabric.network.tls.TlsSettings;

/**
 * Reusable TLS context for creating configured client engines.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TlsContext {

    /**
     * SSL context.
     */
    private final SSLContext context;

    /**
     * Creates a TLS context.
     *
     * @param context SSL context
     */
    private TlsContext(final SSLContext context) {
        if (context == null) {
            throw new ValidateException("SSL context must not be null");
        }
        this.context = context;
    }

    /**
     * Returns the default context.
     *
     * @return context
     */
    public static TlsContext defaults() {
        return Instances.get(TlsContext.class.getName() + ".defaults", TlsContext::createDefault);
    }

    /**
     * Wraps an SSL context.
     *
     * @param context SSL context
     * @return TLS context
     */
    public static TlsContext of(final SSLContext context) {
        return new TlsContext(context);
    }

    /**
     * Creates a configured client engine.
     *
     * @param address  target address
     * @param settings TLS settings
     * @return SSL engine
     */
    public SSLEngine engine(final Address address, final TlsSettings settings) {
        if (address == null) {
            throw new ValidateException("TLS address must not be null");
        }
        if (settings == null) {
            throw new ValidateException("TLS settings must not be null");
        }
        final SSLEngine engine = context.createSSLEngine(address.host(), address.port());
        try {
            engine.setUseClientMode(true);
            engine.setEnabledProtocols(settings.versions().toArray(String[]::new));
            engine.setEnabledCipherSuites(settings.ciphers().toArray(String[]::new));
            final SSLParameters parameters = engine.getSSLParameters();
            if (settings.supportsTlsExtensions()) {
                parameters.setApplicationProtocols(settings.applicationProtocols().toArray(String[]::new));
                serverNames(address.host()).ifPresent(names -> parameters.setServerNames(List.copyOf(names)));
            }
            if (settings.verifyHostname()) {
                parameters.setEndpointIdentificationAlgorithm("HTTPS");
            }
            engine.setSSLParameters(parameters);
            applyClientAuth(engine, settings.clientAuthMode());
            return engine;
        } catch (final IllegalArgumentException e) {
            throw new ProtocolException("Unsupported TLS engine configuration", e);
        }
    }

    /**
     * Returns the wrapped SSL context.
     *
     * @return SSL context
     */
    public SSLContext context() {
        return context;
    }

    /**
     * Creates the default SSL context wrapper.
     *
     * @return TLS context
     */
    private static TlsContext createDefault() {
        try {
            return new TlsContext(SSLContext.getDefault());
        } catch (final java.security.NoSuchAlgorithmException e) {
            throw new ProtocolException("Default TLS context is not available", e);
        }
    }

    /**
     * Applies client auth mode to an engine.
     *
     * @param engine engine
     * @param mode   client auth mode
     */
    private static void applyClientAuth(final SSLEngine engine, final TlsClientAuth mode) {
        if (mode == TlsClientAuth.REQUIRE) {
            engine.setNeedClientAuth(true);
        } else if (mode == TlsClientAuth.OPTIONAL) {
            engine.setWantClientAuth(true);
        } else {
            engine.setNeedClientAuth(false);
            engine.setWantClientAuth(false);
        }
    }

    /**
     * Creates SNI server names when the host is a valid DNS name.
     *
     * @param host host
     * @return server names
     */
    private static java.util.Optional<List<SNIHostName>> serverNames(final String host) {
        try {
            return java.util.Optional.of(List.of(new SNIHostName(host)));
        } catch (final IllegalArgumentException e) {
            return java.util.Optional.empty();
        }
    }

}
