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

import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.network.tls.cert.CertificateChain;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.network.tls.handshake.TlsHandshake;

/**
 * Thin SSLEngine adapter with validation and bus exceptions.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class TlsEngine implements AutoCloseable {

    /**
     * TLS context.
     */
    private final TlsContext context;

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
     * @param address  target address
     * @param settings TLS settings
     */
    private TlsEngine(final TlsContext context, final Address address, final TlsSettings settings) {
        if (context == null) {
            throw new ValidateException("TLS context must not be null");
        }
        if (address == null) {
            throw new ValidateException("TLS address must not be null");
        }
        if (settings == null) {
            throw new ValidateException("TLS settings must not be null");
        }
        this.context = context;
        this.address = address;
        this.settings = settings;
        this.engine = context.engine(address, settings);
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
        return new TlsEngine(context, address, settings);
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
        if (source == null || target == null) {
            throw new ValidateException("TLS wrap buffers must not be null");
        }
        try {
            final SSLEngineResult result = engine.wrap(source, target);
            if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                runDelegatedTasks();
            }
            return result;
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
        if (source == null || target == null) {
            throw new ValidateException("TLS unwrap buffers must not be null");
        }
        try {
            final SSLEngineResult result = engine.unwrap(source, target);
            if (result.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.NEED_TASK) {
                runDelegatedTasks();
            }
            return result;
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
     * Returns current handshake metadata.
     *
     * @return handshake metadata
     */
    public TlsHandshake handshake() {
        final SSLSession session = engine.getSession();
        final CertificateChain peer = CertificateChain.of(peerCertificates(session));
        if (!peer.empty()) {
            settings.certificate().checkPeer(address.host(), peer);
        }
        return TlsHandshake.of(session.getProtocol(), session.getCipherSuite(), peer);
    }

    /**
     * Closes this engine.
     */
    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            engine.closeOutbound();
            try {
                engine.closeInbound();
            } catch (final SSLException ignored) {
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

}
