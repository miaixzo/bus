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
package org.miaixz.bus.fabric.network.tls;

import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;

import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.crypto.builtin.CertificateChain;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;

/**
 * Thin SSLEngine adapter with validation and bus exceptions.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TlsEngine implements AutoCloseable {

    /**
     * Target address.
     */
    private final Address address;

    /**
     * TLS settings.
     */
    private final TlsSettings settings;

    /**
     * JDK engine.
     */
    private final SSLEngine engine;

    /**
     * Whether this adapter represents a client engine.
     */
    private final boolean client;

    /**
     * Delegated task runner.
     */
    private final Runnable task;

    /**
     * Close flag.
     */
    private final AtomicBoolean closed;

    /**
     * Creates a TLS engine adapter.
     *
     * @param context  TLS context
     * @param address  peer address
     * @param settings TLS settings
     * @param client   true for a client engine, false for a server engine
     */
    private TlsEngine(final TlsContext context, final Address address, final TlsSettings settings,
            final boolean client) {
        final TlsContext checkedContext = Assert
                .notNull(context, () -> new ValidateException("TLS context must not be null"));
        this.address = Assert.notNull(address, () -> new ValidateException("TLS address must not be null"));
        this.settings = Assert.notNull(settings, () -> new ValidateException("TLS settings must not be null"));
        this.engine = client ? checkedContext.engine(this.address, this.settings)
                : checkedContext.serverEngine(this.address, this.settings);
        this.client = client;
        this.task = this::runDelegatedTasks;
        this.closed = new AtomicBoolean();
    }

    /**
     * Creates a TLS engine adapter.
     *
     * @param context  TLS context
     * @param address  target address
     * @param settings TLS settings
     * @return TLS engine
     */
    public static TlsEngine create(final TlsContext context, final Address address, final TlsSettings settings) {
        return new TlsEngine(context, address, settings, true);
    }

    /**
     * Creates a server TLS engine adapter.
     *
     * @param context  TLS context
     * @param address  peer address used as engine metadata
     * @param settings TLS settings
     * @return server TLS engine
     */
    public static TlsEngine createServer(final TlsContext context, final Address address, final TlsSettings settings) {
        return new TlsEngine(context, address, settings, false);
    }

    /**
     * Returns the wrapped engine.
     *
     * @return SSL engine
     */
    public SSLEngine engine() {
        return engine;
    }

    /**
     * Wraps plain bytes into TLS records.
     *
     * @param source source buffer
     * @param target target buffer
     * @return engine result
     */
    public SSLEngineResult wrap(final ByteBuffer source, final ByteBuffer target) {
        Assert.notNull(source, () -> new ValidateException("TLS wrap buffers must not be null"));
        Assert.notNull(target, () -> new ValidateException("TLS wrap buffers must not be null"));
        try {
            return engine.wrap(source, target);
        } catch (final SSLException e) {
            throw new SocketException("TLS wrap failed", e);
        }
    }

    /**
     * Unwraps TLS records into plain bytes.
     *
     * @param source source buffer
     * @param target target buffer
     * @return engine result
     */
    public SSLEngineResult unwrap(final ByteBuffer source, final ByteBuffer target) {
        Assert.notNull(source, () -> new ValidateException("TLS unwrap buffers must not be null"));
        Assert.notNull(target, () -> new ValidateException("TLS unwrap buffers must not be null"));
        try {
            return engine.unwrap(source, target);
        } catch (final SSLException e) {
            throw new SocketException("TLS unwrap failed", e);
        }
    }

    /**
     * Returns the delegated task runner.
     *
     * @return task runner
     */
    public Runnable task() {
        return task;
    }

    /**
     * Returns the engine's current handshake status without materializing a result object.
     *
     * @return handshake status
     */
    public SSLEngineResult.HandshakeStatus handshakeStatus() {
        return engine.getHandshakeStatus();
    }

    /**
     * Returns the protocol actually negotiated by the engine.
     *
     * @return negotiated ALPN value, or an empty string when none was negotiated
     */
    public String applicationProtocol() {
        final String protocol = engine.getApplicationProtocol();
        return protocol == null ? "" : protocol;
    }

    /**
     * Classifies portable TLS 1.2 session reuse without treating a non-empty session as proof.
     *
     * @return reuse classification
     */
    public SessionReuse sessionReuse() {
        final SSLSession session = engine.getSession();
        if (!client || !"TLSv1.2".equals(session.getProtocol())) {
            return SessionReuse.UNKNOWN;
        }
        // Portable JSSE exposes no pre-handshake session provenance. Do not turn a non-empty ID into a false hit.
        return SessionReuse.UNKNOWN;
    }

    /**
     * Returns current handshake metadata.
     *
     * @return handshake metadata
     */
    public TlsHandshake handshake() {
        final SSLSession session = engine.getSession();
        final CertificateChain peer = CertificateChain.of(peerCertificates(session));
        if (client && !peer.empty()) {
            settings.certificate().checkPeer(address.host(), peer);
        }
        return TlsHandshake.of(session.getProtocol(), session.getCipherSuite(), peer);
    }

    /**
     * Starts outbound TLS closure.
     */
    public void closeOutbound() {
        engine.closeOutbound();
    }

    /**
     * Acknowledges inbound TLS closure.
     */
    public void closeInbound() {
        try {
            engine.closeInbound();
        } catch (final SSLException e) {
            throw new SocketException("TLS inbound close failed", e);
        }
    }

    /**
     * Returns the current TLS packet buffer size.
     *
     * @return packet buffer size
     */
    public int packetBufferSize() {
        return bufferHint(engine.getSession().getPacketBufferSize());
    }

    /**
     * Returns the current TLS application buffer size.
     *
     * @return application buffer size
     */
    public int applicationBufferSize() {
        return bufferHint(engine.getSession().getApplicationBufferSize());
    }

    /**
     * Closes this engine.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            closeOutbound();
            try {
                closeInbound();
            } catch (final SocketException ignored) {
                // closeInbound may fail when no close_notify was received.
            }
        }
    }

    /**
     * Runs pending delegated tasks.
     */
    private void runDelegatedTasks() {
        Runnable delegated = engine.getDelegatedTask();
        while (delegated != null) {
            delegated.run();
            delegated = engine.getDelegatedTask();
        }
    }

    /**
     * Bounds provider size hints before they reach buffer allocators.
     */
    private static int bufferHint(final int providerHint) {
        if (providerHint <= 0) {
            throw new SocketException("TLS provider returned an invalid buffer size");
        }
        return Math.min(providerHint, Builder.TLS_ENGINE_MAX_BUFFER_HINT);
    }

    /**
     * Returns peer certificates or an empty chain.
     *
     * @param session SSL session
     * @return peer certificates
     */
    private static List<Certificate> peerCertificates(final SSLSession session) {
        try {
            return List.copyOf(Arrays.asList(session.getPeerCertificates()));
        } catch (final SSLPeerUnverifiedException e) {
            return List.of();
        }
    }

    /**
     * Portable session reuse classification.
     */
    public enum SessionReuse {
        /**
         * A full handshake proven by a provider-specific classifier.
         */
        FULL,
        /**
         * The negotiated TLS 1.2 session ID existed before this engine was created.
         */
        RESUMED,
        /**
         * Reuse cannot be established without guessing.
         */
        UNKNOWN
    }

}
