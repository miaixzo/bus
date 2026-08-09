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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.cert.Certificate;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.Source;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.crypto.builtin.CertificateChain;
import org.miaixz.bus.crypto.builtin.TlsHandshake;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Timeout;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.tls.context.TlsContext;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;
import org.miaixz.bus.fabric.runtime.resource.Cancellation;

/**
 * Blocking {@link SSLSocket} conduit used by the built-in HTTP socket connector.
 *
 * @author Kimi Liu
 */
public final class TlsSocketChannel implements Conduit {

    /**
     * Bounded staging pool shared by blocking reads and writes. A borrowed array belongs exclusively to one operation
     * and is always returned in {@code finally}; excess concurrency allocates transient arrays without growing the
     * retained pool.
     */
    private static final ArrayBlockingQueue<byte[]> SCRATCH = new ArrayBlockingQueue<>(Normal._256);

    /**
     * Connected TLS socket owned by this channel.
     */
    private final SSLSocket socket;

    /**
     * Immutable TLS policy applied before the handshake.
     */
    private final TlsSettings settings;

    /**
     * Logical peer address used for endpoint verification.
     */
    private final Address address;

    /**
     * Read and write deadlines applied to socket operations.
     */
    private final Timeout timeout;

    /**
     * Dispatcher owning asynchronous blocking handshakes.
     */
    private final Dispatcher dispatcher;

    /**
     * Connection cancellation scope.
     */
    private final Cancellation cancellation;

    /**
     * Input stream borrowed from the connected socket.
     */
    private final InputStream input;

    /**
     * Output stream borrowed from the connected socket.
     */
    private final OutputStream output;

    /**
     * One-way channel close guard.
     */
    private final AtomicBoolean closed = new AtomicBoolean();

    /**
     * Source view backed by this conduit.
     */
    private final Source source;

    /**
     * Sink view backed by this conduit.
     */
    private final Sink sink;

    /**
     * Negotiated session published after the physical handshake completes.
     */
    private volatile SSLSession session;

    /**
     * Lazily materialized public handshake metadata.
     */
    private volatile TlsHandshake handshake;

    /**
     * Shared in-flight asynchronous handshake.
     */
    private volatile CompletableFuture<TlsHandshake> handshakeFuture;

    private TlsSocketChannel(final SSLSocket socket, final Address address, final TlsSettings settings,
            final Timeout timeout, final Dispatcher dispatcher, final Cancellation cancellation) {
        this.socket = socket;
        this.address = address;
        this.settings = settings;
        this.timeout = timeout;
        this.dispatcher = dispatcher;
        this.cancellation = cancellation;
        try {
            this.input = socket.getInputStream();
            this.output = socket.getOutputStream();
        } catch (final IOException e) {
            throw new SocketException("Unable to open TLS socket streams", e);
        }
        this.source = Conduit.super.source();
        this.sink = Conduit.super.sink();
    }

    /**
     * Creates a configured channel over a connected socket.
     *
     * @param context      TLS context used to create the layered socket
     * @param raw          connected raw socket
     * @param address      target network address
     * @param settings     TLS configuration
     * @param timeout      socket timeout policy
     * @param dispatcher   runtime dispatcher for asynchronous handshakes
     * @param cancellation connection cancellation scope
     * @return configured TLS socket channel
     */
    public static TlsSocketChannel wrap(
            final TlsContext context,
            final Socket raw,
            final Address address,
            final TlsSettings settings,
            final Timeout timeout,
            final Dispatcher dispatcher,
            final Cancellation cancellation) {
        return new TlsSocketChannel(context.socket(raw, address, settings), address, settings, timeout, dispatcher,
                cancellation);
    }

    /**
     * Performs the blocking handshake on the calling thread and exposes its outcome as a completed future.
     *
     * @return future completed with negotiated handshake metadata
     */
    public CompletableFuture<TlsHandshake> handshake() {
        CompletableFuture<TlsHandshake> current = handshakeFuture;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            current = handshakeFuture;
            if (current != null) {
                return current;
            }
            cancellation.throwIfCancelled();
            current = dispatcher.backgroundSupply("tls-socket:handshake", this, this::handshakeSynchronously);
            handshakeFuture = current;
            final CompletableFuture<TlsHandshake> published = current;
            final Runnable unregister = cancellation.onCancel(() -> published.cancel(true));
            current.whenComplete((result, cause) -> {
                unregister.run();
                synchronized (TlsSocketChannel.this) {
                    if (handshakeFuture == published) {
                        handshakeFuture = null;
                    }
                }
            });
            return current;
        }
    }

    /**
     * Performs the handshake on the calling carrier, avoiding an unnecessary scheduler round trip.
     *
     * @return negotiated handshake metadata
     */
    public TlsHandshake handshakeSynchronously() {
        handshakeSessionSynchronously();
        return handshakeMetadata();
    }

    /**
     * Completes the physical handshake without materializing certificate wrapper objects when JSSE already owns all
     * required trust and endpoint verification.
     *
     * @return negotiated JSSE session
     */
    public SSLSession handshakeSessionSynchronously() {
        SSLSession current = session;
        if (current != null) {
            return current;
        }
        try {
            final int handshakeTimeout = timeoutMillis(timeout.connect());
            final int readTimeout = timeoutMillis(timeout.read());
            socket.setSoTimeout(handshakeTimeout);
            socket.startHandshake();
            if (readTimeout != handshakeTimeout) {
                socket.setSoTimeout(readTimeout);
            }
            current = socket.getSession();
            session = current;
            if (requiresPolicyValidation()) {
                handshakeMetadata();
            }
            return current;
        } catch (final IOException e) {
            throw new SocketException("TLS socket handshake failed", e);
        }
    }

    /**
     * Materializes and caches certificate-bearing handshake metadata on first observation.
     *
     * @return negotiated handshake metadata
     */
    public TlsHandshake handshakeMetadata() {
        TlsHandshake current = handshake;
        if (current != null) {
            return current;
        }
        synchronized (this) {
            current = handshake;
            if (current != null) {
                return current;
            }
            final SSLSession negotiated = session;
            if (negotiated == null) {
                throw new IllegalStateException("TLS handshake has not completed");
            }
            try {
                final Certificate[] certificates = negotiated.getPeerCertificates();
                final CertificateChain chain = CertificateChain.of(List.of(certificates));
                if (!chain.empty() && requiresPolicyValidation()) {
                    settings.certificate().checkPeer(address.host(), chain);
                }
                current = TlsHandshake.of(negotiated.getProtocol(), negotiated.getCipherSuite(), chain);
                handshake = current;
                return current;
            } catch (final SSLPeerUnverifiedException e) {
                throw new SocketException("TLS peer certificate is not available", e);
            }
        }
    }

    /**
     * Returns negotiated ALPN.
     *
     * @return negotiated protocol, or an empty string when none was selected
     */
    public String applicationProtocol() {
        final String protocol = socket.getApplicationProtocol();
        return protocol == null ? Normal.EMPTY : protocol;
    }

    @Override
    public CompletableFuture<Long> read(final Buffer target, final long byteCount) {
        try {
            return CompletableFuture.completedFuture(readSynchronously(target, byteCount));
        } catch (final Throwable failure) {
            return CompletableFuture.failedFuture(failure);
        }
    }

    @Override
    public long readSynchronously(final Buffer target, final long byteCount) throws IOException {
        if (byteCount == 0L)
            return 0L;
        final byte[] scratch = borrowScratch();
        try {
            final int count = input.read(scratch, 0, (int) Math.min(byteCount, scratch.length));
            if (count > 0)
                target.write(scratch, 0, count);
            return count;
        } finally {
            releaseScratch(scratch);
        }
    }

    @Override
    public int readSynchronously(final ByteBuffer target) throws IOException {
        if (!target.hasRemaining())
            return 0;
        final byte[] scratch = borrowScratch();
        try {
            final int requested = Math.min(target.remaining(), scratch.length);
            final int count = input.read(scratch, 0, requested);
            if (count > 0)
                target.put(scratch, 0, count);
            return count;
        } finally {
            releaseScratch(scratch);
        }
    }

    @Override
    public CompletableFuture<Long> write(final Buffer source, final long byteCount) {
        try {
            return CompletableFuture.completedFuture(writeSynchronously(source, byteCount));
        } catch (final Throwable failure) {
            return CompletableFuture.failedFuture(failure);
        }
    }

    @Override
    public long writeSynchronously(final Buffer source, final long byteCount) throws IOException {
        final byte[] scratch = borrowScratch();
        try {
            long remaining = byteCount;
            while (remaining > 0L) {
                final int count = (int) Math.min(remaining, scratch.length);
                source.read(scratch, 0, count);
                output.write(scratch, 0, count);
                remaining -= count;
            }
            return byteCount;
        } finally {
            releaseScratch(scratch);
        }
    }

    @Override
    public int writeSynchronously(final ByteBuffer source) throws IOException {
        final byte[] scratch = borrowScratch();
        try {
            final int total = source.remaining();
            while (source.hasRemaining()) {
                final int count = Math.min(source.remaining(), scratch.length);
                source.get(scratch, 0, count);
                output.write(scratch, 0, count);
            }
            return total;
        } finally {
            releaseScratch(scratch);
        }
    }

    /**
     * Borrows an exclusive staging array without blocking the I/O operation.
     *
     * @return reusable or transient staging array
     */
    private static byte[] borrowScratch() {
        final byte[] scratch = SCRATCH.poll();
        return scratch == null ? new byte[Normal._8192] : scratch;
    }

    /**
     * Returns a staging array to the bounded pool.
     *
     * @param scratch staging array no longer used by the caller
     */
    private static void releaseScratch(final byte[] scratch) {
        SCRATCH.offer(scratch);
    }

    @Override
    public Source source() {
        return source;
    }

    @Override
    public Sink sink() {
        return sink;
    }

    @Override
    public boolean opened() {
        return !closed.get() && !socket.isClosed();
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true))
            return;
        final CompletableFuture<TlsHandshake> current = handshakeFuture;
        if (current != null) {
            current.cancel(true);
        }
        try {
            socket.close();
        } catch (final IOException e) {
            throw new SocketException("Unable to close TLS socket", e);
        }
    }

    /**
     * Marks the wrapper closed; the owning raw connection closes the transport without close_notify.
     */
    public void abort() {
        closed.set(true);
        final CompletableFuture<TlsHandshake> current = handshakeFuture;
        if (current != null) {
            current.cancel(true);
        }
    }

    /**
     * JSSE already performs trust and endpoint verification; only custom cleaning or pins need a second pass.
     */
    private boolean requiresPolicyValidation() {
        return settings.certificate().chainCleaner() != null || !settings.certificate().pins().isEmpty();
    }

    private static int timeoutMillis(final Duration value) {
        if (value == null || value.isZero())
            return 0;
        return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, value.toMillis()));
    }

}
