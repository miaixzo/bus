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

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;

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
     * Close-state CAS without a per-engine atomic wrapper.
     */
    private static final VarHandle CLOSED;

    static {
        try {
            CLOSED = MethodHandles.lookup().findVarHandle(TlsEngine.class, "closed", int.class);
        } catch (final ReflectiveOperationException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

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

    /** Reused one-element plaintext source array required by the SSLEngine bulk API. */
    private final ByteBuffer[] wrapSources;

    /** Reused one-element plaintext target array required by the SSLEngine bulk API. */
    private final ByteBuffer[] unwrapTargets;

    /**
     * True after a FINISHED result freezes metadata for the stable application-data path.
     */
    private volatile boolean stable;

    /**
     * Session captured when the most recent handshake reached FINISHED.
     */
    private volatile SSLSession stableSession;

    /**
     * Non-null ALPN value captured with the stable session, empty when none was negotiated.
     */
    private volatile String stableApplicationProtocol;

    /**
     * Bounded packet-buffer size captured with the stable session.
     */
    private volatile int stablePacketBufferSize;

    /**
     * Bounded application-buffer size captured with the stable session.
     */
    private volatile int stableApplicationBufferSize;

    /**
     * Close flag.
     */
    private volatile int closed;

    /**
     * Creates a TLS engine adapter.
     *
     * @param context  non-null context that creates the JDK engine
     * @param address  non-null peer address used for engine metadata and certificate checks
     * @param settings non-null TLS configuration
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
        this.wrapSources = new ByteBuffer[1];
        this.unwrapTargets = new ByteBuffer[1];
    }

    /**
     * Creates a TLS engine adapter.
     *
     * @param context  non-null context that creates the client engine
     * @param address  non-null target address used for SNI and certificate checks
     * @param settings non-null client TLS configuration
     * @return new client-side engine adapter
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
     * @return wrapped JDK engine
     */
    public SSLEngine engine() {
        return engine;
    }

    /**
     * Wraps plain bytes into TLS records.
     *
     * @param source non-null plaintext or handshake source buffer
     * @param target non-null destination for produced TLS records
     * @return JDK engine result after updating stable-handshake metadata
     */
    public SSLEngineResult wrap(final ByteBuffer source, final ByteBuffer target) {
        Assert.notNull(source, () -> new ValidateException("TLS wrap buffers must not be null"));
        Assert.notNull(target, () -> new ValidateException("TLS wrap buffers must not be null"));
        wrapSources[0] = source;
        try {
            final SSLEngineResult result = engine.wrap(wrapSources, 0, 1, target);
            observe(result);
            return result;
        } catch (final SSLException e) {
            throw new SocketException("TLS wrap failed", e);
        } finally {
            wrapSources[0] = null;
        }
    }

    /**
     * Unwraps TLS records into plain bytes.
     *
     * @param source non-null source containing TLS records
     * @param target non-null destination for produced plaintext or handshake bytes
     * @return JDK engine result after updating stable-handshake metadata
     */
    public SSLEngineResult unwrap(final ByteBuffer source, final ByteBuffer target) {
        Assert.notNull(source, () -> new ValidateException("TLS unwrap buffers must not be null"));
        Assert.notNull(target, () -> new ValidateException("TLS unwrap buffers must not be null"));
        unwrapTargets[0] = target;
        try {
            final SSLEngineResult result = engine.unwrap(source, unwrapTargets, 0, 1);
            observe(result);
            return result;
        } catch (final SSLException e) {
            throw new SocketException("TLS unwrap failed", e);
        } finally {
            unwrapTargets[0] = null;
        }
    }

    /**
     * Returns the delegated task runner.
     *
     * @return reusable action that drains all currently delegated JDK engine tasks
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
        if (stable) {
            return stableApplicationProtocol;
        }
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
        final SSLSession session = stable ? stableSession : engine.getSession();
        final CertificateChain peer = CertificateChain.of(peerCertificates(session));
        if (client && !peer.empty()
                && (settings.certificate().chainCleaner() != null || !settings.certificate().pins().isEmpty())) {
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
     * @return positive provider packet-buffer hint capped by the fabric allocation limit
     */
    public int packetBufferSize() {
        return stable ? stablePacketBufferSize : bufferHint(engine.getSession().getPacketBufferSize());
    }

    /**
     * Returns the current TLS application buffer size.
     *
     * @return positive provider application-buffer hint capped by the fabric allocation limit
     */
    public int applicationBufferSize() {
        return stable ? stableApplicationBufferSize : bufferHint(engine.getSession().getApplicationBufferSize());
    }

    /**
     * Returns whether a completed handshake currently activates cached application-data metadata.
     *
     * @return true while the engine remains in its stable post-handshake state
     */
    public boolean stable() {
        return stable;
    }

    /**
     * Closes this engine.
     */
    @Override
    public void close() {
        if (CLOSED.compareAndSet(this, 0, 1)) {
            closeOutbound();
            try {
                closeInbound();
            } catch (final SocketException ignored) {
                // closeInbound may fail when no close_notify was received.
            }
        }
    }

    /**
     * Aborts a transport that is already known to be non-reusable without asking JSSE to validate a peer
     * {@code close_notify}. The underlying conduit is closed by the channel immediately afterwards.
     */
    public void abort() {
        if (CLOSED.compareAndSet(this, 0, 1)) {
            closeOutbound();
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
     * Caches session metadata on handshake completion and leaves stable mode on a later handshake transition.
     *
     * @param result wrap or unwrap result whose handshake status is observed
     */
    private void observe(final SSLEngineResult result) {
        if (!stable && result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED) {
            final SSLSession session = engine.getSession();
            stableSession = session;
            final String protocol = engine.getApplicationProtocol();
            stableApplicationProtocol = protocol == null ? "" : protocol;
            stablePacketBufferSize = bufferHint(session.getPacketBufferSize());
            stableApplicationBufferSize = bufferHint(session.getApplicationBufferSize());
            stable = true;
        } else if (stable && result.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING
                && result.getHandshakeStatus() != SSLEngineResult.HandshakeStatus.FINISHED) {
            stable = false;
        }
    }

    /**
     * Bounds provider size hints before they reach buffer allocators.
     *
     * @param providerHint buffer size reported by the TLS provider
     * @return validated allocation size
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
            return Arrays.asList(session.getPeerCertificates());
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
