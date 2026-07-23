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

import java.io.IOException;
import java.net.Socket;
import java.security.Provider;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

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
     * Shared empty protocol array.
     */
    private static final String[] EMPTY_PROTOCOLS = new String[0];

    /**
     * SSL context.
     */
    private final SSLContext context;

    /** Stable client socket factory owned by the immutable SSL context. */
    private final SSLSocketFactory socketFactory;

    /** Default enabled protocols captured from the context's client socket factory. */
    private final String[] defaultProtocols;

    /** Default enabled cipher suites captured from the context's client socket factory. */
    private final String[] defaultCiphers;

    /**
     * Most recently resolved immutable engine configuration.
     */
    private volatile EngineConfiguration engineConfiguration;

    /** Most recent immutable client-socket parameter template, including host-specific SNI. */
    private volatile SocketConfiguration socketConfiguration;

    /**
     * Creates a TLS context.
     *
     * @param context SSL context
     */
    private TlsContext(final SSLContext context) {
        this.context = Assert.notNull(context, () -> new ValidateException("SSL context must not be null"));
        this.socketFactory = this.context.getSocketFactory();
        final SSLParameters defaults = this.context.getDefaultSSLParameters();
        this.defaultProtocols = defaults.getProtocols();
        this.defaultCiphers = defaults.getCipherSuites();
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
        final EngineConfiguration configuration = configuration(checkedSettings);
        final SSLEngine engine = createEngine(checkedAddress.host(), checkedAddress.port());
        try {
            engine.setUseClientMode(true);
            TlsParameters.applyClient(
                    engine,
                    checkedAddress.host(),
                    configuration.versions,
                    configuration.ciphers,
                    configuration.applicationProtocols,
                    checkedSettings.supportsTlsExtensions(),
                    checkedSettings.verifyHostname());
            checkedSettings.clientAuthMode().apply(engine);
            return engine;
        } catch (final IllegalArgumentException e) {
            throw new ProtocolException("Unsupported TLS engine configuration", e);
        }
    }

    /**
     * Layers a configured blocking client TLS socket over an already connected transport socket.
     *
     * @param raw      connected transport socket
     * @param address  logical peer address used for SNI and verification
     * @param settings immutable TLS settings
     * @return configured socket before handshake
     */
    public SSLSocket socket(final Socket raw, final Address address, final TlsSettings settings) {
        final Socket checkedRaw = Assert.notNull(raw, () -> new ValidateException("TLS socket must not be null"));
        final Address checkedAddress = Assert
                .notNull(address, () -> new ValidateException("TLS address must not be null"));
        final TlsSettings checkedSettings = Assert
                .notNull(settings, () -> new ValidateException("TLS settings must not be null"));
        final EngineConfiguration configuration = configuration(checkedSettings);
        try {
            final SSLSocket socket = (SSLSocket) socketFactory
                    .createSocket(checkedRaw, checkedAddress.host(), checkedAddress.port(), false);
            socket.setUseClientMode(true);
            // Apply the complete immutable policy once. Calling both enabled-array setters and then
            // get/setSSLParameters clones the same arrays twice on every cold connection.
            socket.setSSLParameters(socketParameters(checkedAddress.host(), checkedSettings, configuration));
            return socket;
        } catch (final IOException | IllegalArgumentException e) {
            throw new ProtocolException("Unable to create TLS socket", e);
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
        final EngineConfiguration configuration = configuration(checkedSettings);
        try {
            final SSLEngine engine = createEngine(checkedAddress.host(), checkedAddress.port());
            engine.setUseClientMode(false);
            engine.setEnabledProtocols(configuration.versions);
            engine.setEnabledCipherSuites(configuration.ciphers);

            final SSLParameters parameters = engine.getSSLParameters();
            parameters.setServerNames(List.of());
            parameters.setEndpointIdentificationAlgorithm(null);
            parameters.setApplicationProtocols(
                    checkedSettings.supportsTlsExtensions() ? configuration.applicationProtocols : EMPTY_PROTOCOLS);
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
     * Returns the stable identity of the wrapped SSL context.
     *
     * <p>
     * The identity is the caller-provided context itself; no certificate, key, session, or derived text is
     * materialized.
     * </p>
     *
     * @return stable context identity
     */
    public Object identity() {
        return context;
    }

    /**
     * Returns the provider of the wrapped SSL context.
     *
     * @return SSL provider
     */
    public Provider provider() {
        return context.getProvider();
    }

    /**
     * Returns the existing client session context without modifying its global configuration.
     *
     * @return client session context
     */
    public SSLSessionContext clientSessionContext() {
        return context.getClientSessionContext();
    }

    /**
     * Returns the current client session cache capacity.
     *
     * @return session cache capacity
     */
    public int clientSessionCacheSize() {
        return clientSessionContext().getSessionCacheSize();
    }

    /**
     * Returns the current client session timeout in seconds.
     *
     * @return session timeout seconds
     */
    public int clientSessionTimeout() {
        return clientSessionContext().getSessionTimeout();
    }

    /**
     * Creates an unconfigured engine directly from the existing SSL context.
     *
     * @param peerHost peer host metadata
     * @param peerPort peer port metadata
     * @return new SSL engine
     */
    public SSLEngine createEngine(final String peerHost, final int peerPort) {
        return context.createSSLEngine(peerHost, peerPort);
    }

    /**
     * Resolves reusable array views for one immutable TLS setting value.
     *
     * @param settings TLS settings
     * @return reusable engine configuration
     */
    private EngineConfiguration configuration(final TlsSettings settings) {
        EngineConfiguration current = engineConfiguration;
        if (current == null || current.settings != settings && !current.settings.equals(settings)) {
            current = new EngineConfiguration(settings, settings.versions().toArray(String[]::new),
                    settings.ciphers().toArray(String[]::new), settings.applicationProtocols().toArray(String[]::new));
            engineConfiguration = current;
        }
        return current;
    }

    /** Builds and reuses the host-specific parameter template copied by each newly created SSLSocket. */
    private SSLParameters socketParameters(
            final String host,
            final TlsSettings settings,
            final EngineConfiguration engine) {
        SocketConfiguration current = socketConfiguration;
        if (current != null && current.settings.equals(settings) && current.host.equals(host)) {
            return current.parameters;
        }
        final SSLParameters parameters = new SSLParameters();
        if (!Arrays.equals(engine.ciphers, defaultCiphers)) {
            parameters.setCipherSuites(engine.ciphers);
        }
        if (!Arrays.equals(engine.versions, defaultProtocols)) {
            parameters.setProtocols(engine.versions);
        }
        if (settings.supportsTlsExtensions()) {
            parameters.setApplicationProtocols(engine.applicationProtocols);
            TlsParameters.serverNames(host).ifPresent(parameters::setServerNames);
        }
        if (settings.verifyHostname()) {
            parameters.setEndpointIdentificationAlgorithm(TlsParameters.HTTPS_ENDPOINT_IDENTIFICATION);
        }
        switch (settings.clientAuthMode()) {
            case REQUIRE -> parameters.setNeedClientAuth(true);
            case OPTIONAL -> parameters.setWantClientAuth(true);
            case NONE -> {
                // A newly created client socket already has both flags disabled.
            }
        }
        current = new SocketConfiguration(settings, host, parameters);
        socketConfiguration = current;
        return parameters;
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

    /**
     * Immutable TLS engine array snapshot.
     *
     * @param settings             settings identity
     * @param versions             enabled protocol versions
     * @param ciphers              enabled cipher suites
     * @param applicationProtocols ALPN protocol names
     */
    private record EngineConfiguration(TlsSettings settings, String[] versions, String[] ciphers,
            String[] applicationProtocols) {
    }

    /**
     * Cached socket parameters for one settings and host pair.
     *
     * @param settings   settings identity
     * @param host       peer host used for SNI and endpoint verification
     * @param parameters immutable socket parameter template
     */
    private record SocketConfiguration(TlsSettings settings, String host, SSLParameters parameters) {
    }

}
