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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import org.miaixz.bus.core.instance.Instances;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.tls.SSLContextBuilder;
import org.miaixz.bus.core.net.tls.TlsParameters;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.network.tls.TlsSettings;

/**
 * Reusable TLS context for creating configured client and server engines.
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
        this.context = Assert.notNull(context, () -> new ValidateException("SSL context must not be null"));
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
        final Address checkedAddress = Assert
                .notNull(address, () -> new ValidateException("TLS address must not be null"));
        final TlsSettings checkedSettings = Assert
                .notNull(settings, () -> new ValidateException("TLS settings must not be null"));
        final SSLEngine engine = context.createSSLEngine(checkedAddress.host(), checkedAddress.port());
        try {
            engine.setUseClientMode(true);
            TlsParameters.applyClient(
                    engine,
                    checkedAddress.host(),
                    checkedSettings.versions().toArray(String[]::new),
                    checkedSettings.ciphers().toArray(String[]::new),
                    checkedSettings.applicationProtocols().toArray(String[]::new),
                    checkedSettings.supportsTlsExtensions(),
                    checkedSettings.verifyHostname());
            checkedSettings.clientAuthMode().apply(engine);
            return engine;
        } catch (final IllegalArgumentException e) {
            throw new ProtocolException("Unsupported TLS engine configuration", e);
        }
    }

    /**
     * Creates a configured server engine with peer metadata.
     *
     * @param address  peer address used only as engine metadata
     * @param settings TLS settings
     * @return configured server SSL engine
     */
    public SSLEngine serverEngine(final Address address, final TlsSettings settings) {
        final Address checkedAddress = Assert
                .notNull(address, () -> new ValidateException("TLS address must not be null"));
        final TlsSettings checkedSettings = Assert
                .notNull(settings, () -> new ValidateException("TLS settings must not be null"));
        try {
            final SSLEngine engine = context.createSSLEngine(checkedAddress.host(), checkedAddress.port());
            engine.setUseClientMode(false);
            engine.setEnabledProtocols(checkedSettings.versions().toArray(String[]::new));
            engine.setEnabledCipherSuites(checkedSettings.ciphers().toArray(String[]::new));

            final SSLParameters parameters = engine.getSSLParameters();
            parameters.setServerNames(List.of());
            parameters.setEndpointIdentificationAlgorithm(null);
            parameters.setApplicationProtocols(
                    checkedSettings.supportsTlsExtensions()
                            ? checkedSettings.applicationProtocols().toArray(String[]::new)
                            : new String[0]);
            engine.setSSLParameters(parameters);
            checkedSettings.clientAuthMode().apply(engine);
            return engine;
        } catch (final IllegalArgumentException e) {
            throw new ProtocolException("Unsupported TLS server engine configuration", e);
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
            return new TlsContext(SSLContextBuilder.getDefault());
        } catch (final InternalException e) {
            throw new ProtocolException("Default TLS context is not available", e);
        }
    }

}
