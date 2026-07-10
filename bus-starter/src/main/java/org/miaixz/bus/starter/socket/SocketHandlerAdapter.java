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
package org.miaixz.bus.starter.socket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.CompletionHandler;
import java.nio.channels.SocketChannel;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.fabric.Address;
import org.miaixz.bus.fabric.Handler;
import org.miaixz.bus.fabric.Message;
import org.miaixz.bus.fabric.Options;
import org.miaixz.bus.fabric.Session;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.Wiring;
import org.miaixz.bus.fabric.network.Conduit;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.network.Destination;
import org.miaixz.bus.fabric.observe.EventObserver;
import org.miaixz.bus.fabric.protocol.socket.SocketOptions;
import org.miaixz.bus.fabric.protocol.socket.SocketProxyProtocol;
import org.miaixz.bus.fabric.protocol.socket.SocketSession;

/**
 * Starter-local accepted socket adapter backed by current fabric socket sessions.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class SocketHandlerAdapter implements Handler, AutoCloseable {

    /**
     * Read buffer size.
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Current fabric handler.
     */
    private final Handler delegate;

    /**
     * Current fabric decoder.
     */
    private final SocketFrameDecoder decoder;

    /**
     * Per-session reader workers.
     */
    private final ExecutorService workers;

    /**
     * Creates an adapter.
     *
     * @param delegate handler delegate
     * @param decoder  decoder
     */
    SocketHandlerAdapter(final Handler delegate, final SocketFrameDecoder decoder) {
        if (delegate == null) {
            throw new ValidateException("Socket handler must not be null");
        }
        if (decoder == null) {
            throw new ValidateException("Socket decoder must not be null");
        }
        this.delegate = delegate;
        this.decoder = decoder;
        this.workers = Executors.newCachedThreadPool(new StarterSocketThreadFactory());
    }

    @Override
    public void message(final Session session, final Message message) {
        if (!(message.tag() instanceof SocketChannel channel)) {
            delegate.message(session, message);
            return;
        }
        workers.execute(() -> readLoop(session, channel));
    }

    @Override
    public void failure(final Session session, final Throwable cause) {
        delegate.failure(session, cause);
    }

    @Override
    public void closed(final Session session) {
        delegate.closed(session);
    }

    @Override
    public void close() {
        workers.shutdownNow();
    }

    /**
     * Reads and dispatches decoded messages for one accepted session.
     *
     * @param accepted accepted TCP session
     * @param channel  accepted channel
     */
    private void readLoop(final Session accepted, final SocketChannel channel) {
        SocketSession socketSession = null;
        try {
            channel.configureBlocking(true);
            final ByteBuffer firstPacket = firstPacket(channel);
            if (firstPacket == null) {
                accepted.close();
                delegate.closed(accepted);
                return;
            }
            final SocketProxyProtocol.Result proxy = SocketProxyProtocol.parse(firstPacket);
            final ChannelConnection connection = new ChannelConnection(accepted.address(), channel, proxy.payload());
            socketSession = SocketSession.create(
                    accepted.address(),
                    connection,
                    decoder.codec(),
                    delegate,
                    attributes(proxy),
                    accepted::close,
                    Wiring.noop(),
                    Options.DEFAULT_MATERIALIZE_MAX_BYTES,
                    SocketOptions.defaults());
            while (socketSession.opened() && channel.isOpen() && !Thread.currentThread().isInterrupted()) {
                socketSession.receive().get();
            }
            if (socketSession.opened()) {
                socketSession.close();
            }
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            cancelOrFail(socketSession, accepted, e);
        } catch (final ExecutionException e) {
            closeOrFail(socketSession, accepted, cause(e));
        } catch (final IOException | RuntimeException e) {
            closeOrFail(socketSession, accepted, e);
        }
    }

    /**
     * Reads the first inbound packet for optional PROXY parsing.
     *
     * @param channel accepted channel
     * @return first packet or null on EOF
     * @throws IOException read failure
     */
    private static ByteBuffer firstPacket(final SocketChannel channel) throws IOException {
        final ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
        int read;
        do {
            read = channel.read(buffer);
        } while (read == 0 && channel.isOpen() && !Thread.currentThread().isInterrupted());
        if (read < 0) {
            return null;
        }
        buffer.flip();
        return buffer.asReadOnlyBuffer();
    }

    /**
     * Creates current socket session attributes.
     *
     * @param proxy parsed PROXY packet
     * @return attributes
     */
    private static Map<String, Object> attributes(final SocketProxyProtocol.Result proxy) {
        final LinkedHashMap<String, Object> values = new LinkedHashMap<>();
        values.put(SocketSession.ATTRIBUTE_OBSERVER, EventObserver.noop());
        values.put(SocketSession.ATTRIBUTE_SOCKET_OPTIONS, SocketOptions.defaults());
        return SocketProxyProtocol.attributes(proxy, values);
    }

    /**
     * Closes a current session, or reports a pre-session failure to the delegate.
     *
     * @param socketSession current socket session
     * @param accepted      accepted TCP session
     * @param cause         failure cause
     */
    private void closeOrFail(final SocketSession socketSession, final Session accepted, final Throwable cause) {
        if (socketSession == null) {
            delegate.failure(accepted, cause);
            accepted.close();
            return;
        }
        if (socketSession.opened()) {
            socketSession.close();
        }
    }

    /**
     * Cancels a current session, or reports a pre-session interruption to the delegate.
     *
     * @param socketSession current socket session
     * @param accepted      accepted TCP session
     * @param cause         interruption
     */
    private void cancelOrFail(final SocketSession socketSession, final Session accepted, final Throwable cause) {
        if (socketSession == null) {
            delegate.failure(accepted, cause);
            accepted.cancel();
            return;
        }
        socketSession.cancel();
    }

    /**
     * Unwraps execution causes.
     *
     * @param failure execution failure
     * @return root cause
     */
    private static Throwable cause(final ExecutionException failure) {
        return failure.getCause() == null ? failure : failure.getCause();
    }

    /**
     * SocketChannel-backed current fabric connection.
     */
    private static final class ChannelConnection implements Connection, Conduit {

        /**
         * Destination.
         */
        private final Destination destination;

        /**
         * Accepted channel.
         */
        private final SocketChannel channel;

        /**
         * Payload already read during PROXY parsing.
         */
        private ByteBuffer prefix;

        /**
         * Connection state.
         */
        private volatile Status state;

        /**
         * Creates a channel connection.
         *
         * @param address address
         * @param channel channel
         * @param prefix  prefetched payload
         */
        private ChannelConnection(final Address address, final SocketChannel channel, final ByteBuffer prefix) {
            if (address == null) {
                throw new ValidateException("Socket address must not be null");
            }
            if (channel == null) {
                throw new ValidateException("Socket channel must not be null");
            }
            if (prefix == null) {
                throw new ValidateException("Socket prefix payload must not be null");
            }
            this.destination = Destination.of(Protocol.TCP, address, Options.empty());
            this.channel = channel;
            this.prefix = prefix.asReadOnlyBuffer();
            this.state = Status.OPENED;
        }

        @Override
        public Destination destination() {
            return destination;
        }

        @Override
        public Conduit conduit() {
            return this;
        }

        @Override
        public Status state() {
            return state;
        }

        @Override
        public CompletableFuture<Integer> read(final ByteBuffer target) {
            try {
                return CompletableFuture.completedFuture(readNow(target));
            } catch (final IOException e) {
                state = Status.FAILED;
                return CompletableFuture.failedFuture(new SocketException("Unable to read accepted socket", e));
            }
        }

        @Override
        public void read(final ByteBuffer target, final CompletionHandler<Integer, ByteBuffer> handler) {
            read(target).whenComplete((read, cause) -> {
                if (cause == null) {
                    handler.completed(read, target);
                } else {
                    handler.failed(cause, target);
                }
            });
        }

        @Override
        public CompletableFuture<Integer> write(final ByteBuffer source) {
            try {
                return CompletableFuture.completedFuture(writeNow(source));
            } catch (final IOException e) {
                state = Status.FAILED;
                return CompletableFuture.failedFuture(new SocketException("Unable to write accepted socket", e));
            }
        }

        @Override
        public void write(final ByteBuffer source, final CompletionHandler<Integer, ByteBuffer> handler) {
            write(source).whenComplete((written, cause) -> {
                if (cause == null) {
                    handler.completed(written, source);
                } else {
                    handler.failed(cause, source);
                }
            });
        }

        @Override
        public boolean healthy() {
            return opened();
        }

        @Override
        public boolean idle() {
            return opened();
        }

        @Override
        public boolean opened() {
            return state == Status.OPENED && channel.isOpen();
        }

        @Override
        public void close() {
            if (state == Status.CLOSED) {
                return;
            }
            try {
                channel.close();
                state = Status.CLOSED;
            } catch (final IOException e) {
                state = Status.FAILED;
                throw new SocketException("Unable to close accepted socket", e);
            }
        }

        /**
         * Reads bytes, serving prefetched payload first.
         *
         * @param target target buffer
         * @return read byte count
         * @throws IOException read failure
         */
        private int readNow(final ByteBuffer target) throws IOException {
            if (target == null) {
                throw new ValidateException("Socket read target must not be null");
            }
            final int copied = copyPrefix(target);
            if (copied > 0) {
                return copied;
            }
            return channel.read(target);
        }

        /**
         * Writes bytes to the channel.
         *
         * @param source source bytes
         * @return written byte count
         * @throws IOException write failure
         */
        private int writeNow(final ByteBuffer source) throws IOException {
            if (source == null) {
                throw new ValidateException("Socket write source must not be null");
            }
            final ByteBuffer view = source.asReadOnlyBuffer();
            int written = 0;
            while (view.hasRemaining()) {
                final int current = channel.write(view);
                if (current < 0) {
                    throw new SocketException("Accepted socket stream closed");
                }
                if (current == 0) {
                    Thread.yield();
                    continue;
                }
                written += current;
            }
            return written;
        }

        /**
         * Copies prefetched bytes into the target buffer.
         *
         * @param target target buffer
         * @return copied byte count
         */
        private int copyPrefix(final ByteBuffer target) {
            if (prefix == null || !prefix.hasRemaining() || !target.hasRemaining()) {
                prefix = null;
                return 0;
            }
            int copied = 0;
            while (prefix.hasRemaining() && target.hasRemaining()) {
                target.put(prefix.get());
                copied++;
            }
            if (!prefix.hasRemaining()) {
                prefix = null;
            }
            return copied;
        }

    }

    /**
     * Starter socket reader thread factory.
     */
    private static final class StarterSocketThreadFactory implements ThreadFactory {

        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Thread newThread(final Runnable runnable) {
            final Thread thread = new Thread(runnable, "bus-starter-socket-" + sequence.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }

}
