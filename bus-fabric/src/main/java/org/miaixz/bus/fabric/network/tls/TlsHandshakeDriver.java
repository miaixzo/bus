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

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.network.Conduit;

/**
 * Explicit TLS handshake and post-handshake state driver.
 *
 * <p>
 * After FINISHED the negotiated metadata and buffer sizes are cached and application data avoids this state loop. A
 * later non-NOT_HANDSHAKING result re-enters the driver for TLS 1.3 KeyUpdate or delegated tasks.
 * </p>
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class TlsHandshakeDriver {

    /**
     * Shared empty application input duplicated for handshake-only wrap operations.
     */
    private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);

    /**
     * TLS engine adapter used for wrap, unwrap, task, and negotiated-metadata access.
     */
    private final TlsEngine adapter;

    /**
     * Wrapped JDK engine whose handshake state drives this loop.
     */
    private final SSLEngine engine;

    /**
     * Connection conduit carrying encrypted handshake records.
     */
    private final Conduit transport;

    /**
     * Reusable direct encrypted input maintained in read mode between unwrap operations.
     */
    private ByteBuffer encryptedInput;

    /**
     * Reusable direct staging buffer for records produced by handshake wraps.
     */
    private ByteBuffer encryptedOutput;

    /**
     * Reusable direct scratch buffer for plaintext produced during handshake unwraps.
     */
    private ByteBuffer plaintext;

    /**
     * Bridge buffer transferring bytes between direct TLS buffers and the conduit.
     */
    private final Buffer bridge = new Buffer();

    /**
     * Whether negotiated metadata has been frozen for the application-data path.
     */
    private volatile boolean stable;

    /**
     * Negotiated ALPN value cached on stable-session entry, empty when unavailable.
     */
    private volatile String applicationProtocol = "";

    /**
     * Negotiated cipher-suite name cached on stable-session entry.
     */
    private volatile String cipherSuite = "";

    /**
     * Negotiated SSL session, or null before stable-session entry.
     */
    private volatile SSLSession session;

    /**
     * Bounded provider packet-buffer hint cached on stable-session entry.
     */
    private volatile int packetBufferSize;

    /**
     * Bounded provider application-buffer hint cached on stable-session entry.
     */
    private volatile int applicationBufferSize;

    /**
     * Creates a handshake driver with provider-sized direct buffers.
     *
     * @param adapter   non-null TLS engine adapter
     * @param transport non-null conduit carrying encrypted records
     */
    TlsHandshakeDriver(final TlsEngine adapter, final Conduit transport) {
        if (adapter == null || transport == null) {
            throw new ValidateException("TLS handshake collaborators must not be null");
        }
        this.adapter = adapter;
        this.engine = adapter.engine();
        this.transport = transport;
        this.encryptedInput = ByteBuffer.allocateDirect(adapter.packetBufferSize());
        this.encryptedInput.flip();
        this.encryptedOutput = ByteBuffer.allocateDirect(adapter.packetBufferSize());
        this.plaintext = ByteBuffer.allocateDirect(adapter.applicationBufferSize());
    }

    /**
     * Begins and synchronously drives the initial handshake to a stable state.
     */
    void handshake() {
        try {
            engine.beginHandshake();
        } catch (final IOException e) {
            throw new SocketException("Unable to start TLS handshake", e);
        }
        drive(engine.getHandshakeStatus());
    }

    /**
     * Re-enters the state machine when an application-data operation reports post-handshake work.
     *
     * @param status handshake status returned by the application-data operation
     */
    void driveIfNeeded(final SSLEngineResult.HandshakeStatus status) {
        if (status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING
                && status != SSLEngineResult.HandshakeStatus.FINISHED) {
            stable = false;
            drive(status);
        }
    }

    /**
     * Returns whether negotiated metadata is ready for the stable application-data path.
     *
     * @return true after the most recent handshake transition completed
     */
    boolean stable() {
        return stable;
    }

    /**
     * Returns the cached negotiated application protocol.
     *
     * @return ALPN value, empty when none was negotiated or stability has not been reached
     */
    String applicationProtocol() {
        return applicationProtocol;
    }

    /**
     * Returns the cached negotiated cipher suite.
     *
     * @return cipher-suite name, empty before stability is reached
     */
    String cipherSuite() {
        return cipherSuite;
    }

    /**
     * Returns the cached negotiated session.
     *
     * @return SSL session, or null before stability is reached
     */
    SSLSession session() {
        return session;
    }

    /**
     * Returns the cached packet-buffer allocation size.
     *
     * @return positive packet-buffer size after stability is reached, otherwise zero
     */
    int packetBufferSize() {
        return packetBufferSize;
    }

    /**
     * Returns the cached application-buffer allocation size.
     *
     * @return positive application-buffer size after stability is reached, otherwise zero
     */
    int applicationBufferSize() {
        return applicationBufferSize;
    }

    /**
     * Drives explicit NEED_WRAP, NEED_UNWRAP, NEED_UNWRAP_AGAIN, and NEED_TASK transitions.
     *
     * @param status initial handshake status to process
     */
    private void drive(SSLEngineResult.HandshakeStatus status) {
        int noProgress = 0;
        while (status != SSLEngineResult.HandshakeStatus.FINISHED
                && status != SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING) {
            switch (status) {
                case NEED_TASK -> {
                    adapter.task().run();
                    status = engine.getHandshakeStatus();
                }
                case NEED_WRAP -> status = wrapHandshake();
                case NEED_UNWRAP, NEED_UNWRAP_AGAIN -> status = unwrapHandshake(
                        status == SSLEngineResult.HandshakeStatus.NEED_UNWRAP);
                default -> throw new ProtocolException("Unsupported TLS handshake transition");
            }
            if (++noProgress > 10_000) {
                throw new SocketException("TLS handshake exceeded progress bound");
            }
        }
        cacheStableSession();
    }

    /**
     * Produces and writes handshake records, growing encrypted output on overflow.
     *
     * @return handshake status reported by the successful wrap
     */
    private SSLEngineResult.HandshakeStatus wrapHandshake() {
        for (;;) {
            encryptedOutput.clear();
            final SSLEngineResult result = adapter.wrap(EMPTY.duplicate(), encryptedOutput);
            if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
                encryptedOutput = ByteBuffer
                        .allocateDirect(Math.max(adapter.packetBufferSize(), encryptedOutput.capacity() << 1));
                continue;
            }
            if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
                throw new SocketException("TLS handshake closed during wrap");
            }
            encryptedOutput.flip();
            writeAll(encryptedOutput);
            return result.getHandshakeStatus();
        }
    }

    /**
     * Consumes handshake records and obtains more encrypted input when permitted or required by underflow.
     *
     * @param allowRead whether an empty encrypted-input buffer may be filled before the first unwrap attempt
     * @return next handshake status, or NEED_UNWRAP after refilling on underflow
     */
    private SSLEngineResult.HandshakeStatus unwrapHandshake(final boolean allowRead) {
        if (!encryptedInput.hasRemaining() && allowRead) {
            readMore();
        }
        plaintext.clear();
        final SSLEngineResult result = adapter.unwrap(encryptedInput, plaintext);
        if (result.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW) {
            readMore();
            return SSLEngineResult.HandshakeStatus.NEED_UNWRAP;
        }
        if (result.getStatus() == SSLEngineResult.Status.BUFFER_OVERFLOW) {
            plaintext = ByteBuffer.allocateDirect(Math.max(adapter.applicationBufferSize(), plaintext.capacity() << 1));
            return result.getHandshakeStatus();
        }
        if (result.getStatus() == SSLEngineResult.Status.CLOSED) {
            throw new SocketException("TLS handshake closed during unwrap");
        }
        return result.getHandshakeStatus();
    }

    /**
     * Compacts unconsumed encrypted input, grows a full buffer, and reads additional bytes from the conduit.
     */
    private void readMore() {
        encryptedInput.compact();
        if (!encryptedInput.hasRemaining()) {
            encryptedInput.flip();
            final ByteBuffer grown = ByteBuffer.allocateDirect(encryptedInput.capacity() << 1);
            grown.put(encryptedInput);
            encryptedInput = grown;
        }
        final long read;
        try {
            read = transport.readSynchronously(bridge, encryptedInput.remaining());
        } catch (final IOException e) {
            throw new SocketException("TLS handshake read failed", e);
        }
        if (read <= 0L) {
            throw new SocketException("TLS handshake transport ended");
        }
        bridge.readTo(encryptedInput, (int) read);
        encryptedInput.flip();
    }

    /**
     * Transfers every produced encrypted handshake byte through synchronous conduit writes.
     *
     * @param source encrypted source buffer consumed into the bridge buffer
     */
    private void writeAll(final ByteBuffer source) {
        try {
            bridge.write(source);
            while (bridge.size() != 0L) {
                final long written = transport.writeSynchronously(bridge, bridge.size());
                if (written <= 0L) {
                    throw new SocketException("TLS handshake write made no progress");
                }
            }
        } catch (final IOException e) {
            throw new SocketException("TLS handshake write failed", e);
        }
    }

    /**
     * Freezes the negotiated session, ALPN, cipher suite, and buffer sizes for the stable application-data path.
     */
    private void cacheStableSession() {
        session = engine.getSession();
        applicationProtocol = adapter.applicationProtocol();
        cipherSuite = session.getCipherSuite();
        packetBufferSize = adapter.packetBufferSize();
        applicationBufferSize = adapter.applicationBufferSize();
        stable = true;
    }

}
