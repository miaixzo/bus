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
package org.miaixz.bus.fabric.protocol.http.http2;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.Sink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.core.net.Http;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.fabric.Builder;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.runtime.Activity;
import org.miaixz.bus.fabric.runtime.dispatch.DispatchHandle;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * HTTP/2 connection with stream registry and frame IO.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Connection implements AutoCloseable {

    /**
     * Advertised receive window that avoids closed-stream updates for supported payloads.
     */
    private static final int LOCAL_RECEIVE_WINDOW = (int) Builder.HTTP2_CONNECTION_DEFAULT_MAX_QUEUED_DATA;

    /**
     * Bound network connection.
     */
    private final Connection connection;

    /**
     * Buffered source over the bound connection.
     */
    private final BufferSource source;

    /**
     * Sink over the bound connection.
     */
    private final Sink sink;

    /**
     * Connection-owned direct frame batch writer.
     */
    private final Http2FrameWriter frameWriter;

    /**
     * Bounded command queue feeding the sole physical writer.
     */
    private final Http2WriteScheduler writeScheduler;

    /**
     * Handle of the connection-level writer activity.
     */
    private final AtomicReference<DispatchHandle> writerHandle;

    /**
     * Header block writer codec.
     */
    private final HpackCodec hpackWriter;

    /**
     * Header block reader codec.
     */
    private final HpackCodec hpackReader;

    /** Reader-owned canonical response field list for repeated HPACK blocks. */
    private List<Http2Header> cachedResponseFields;

    /** Immutable root-header snapshot paired with {@link #cachedResponseFields}. */
    private Headers cachedResponseHeaders;

    /**
     * Streams.
     */
    private final ConcurrentHashMap<Integer, Http2Stream> streams;

    /**
     * Latest priority metadata per stream.
     */
    private final ConcurrentHashMap<Integer, Http2Priority> priorities;

    /**
     * Pushed stream ids.
     */
    private final Set<Integer> pushedStreams;

    /**
     * Connection flow-control window.
     */
    private long connectionWriteWindow;

    /**
     * Connection inbound flow-control window.
     */
    private long connectionReceiveWindow;

    /**
     * Unacknowledged connection inbound bytes.
     */
    private final AtomicLong unacknowledgedConnectionBytes;

    /**
     * Next local stream id.
     */
    private final AtomicInteger nextLocal;

    /**
     * Next remote stream id.
     */
    private final AtomicInteger nextRemote;

    /**
     * Number of locally initiated streams currently registered on this connection.
     */
    private final AtomicInteger localStreamCount;

    /**
     * State.
     */
    private final AtomicReference<Status> state;

    /**
     * Connection-level streaming reader.
     */
    private final Http2Reader reader;

    /**
     * Reader task start guard.
     */
    private final AtomicBoolean readerStarted;

    /**
     * Handle of the single long-running reader activity.
     */
    private final AtomicReference<DispatchHandle> readerHandle;

    /**
     * Lock protecting every flow-control value and terminal flow state.
     */
    private final ReentrantLock flowLock;

    /**
     * Condition signaled whenever flow-control or terminal state changes.
     */
    private final Condition flowChanged;

    /**
     * Lock serializing all physical frame writes and HPACK writer access.
     */
    private final ReentrantLock frameWriteLock;

    /**
     * Monitor preserving the RFC 9113 creation order of locally initiated streams.
     */
    private final Object localHeaderOrder;

    /**
     * Next local stream id whose initial HEADERS may be emitted.
     */
    private int nextLocalHeaderStreamId;

    /**
     * Allocated local stream ids explicitly terminated before initial HEADERS.
     */
    private final Set<Integer> abandonedLocalHeaders;

    /**
     * Reusable control-frame payload scratch buffer guarded by {@link #frameWriteLock}.
     */
    private final Buffer controlPayloadScratch;

    /**
     * Owned dispatcher close guard.
     */
    private final AtomicBoolean dispatcherClosed;

    /**
     * Failure visible to active and late stream readers.
     */
    private final AtomicReference<RuntimeException> connectionFailure;

    /**
     * Peer settings snapshot.
     */
    private final AtomicReference<Http2Settings> peerSettings;

    /**
     * Current peer maximum frame size.
     */
    private final AtomicInteger maxFrameSize;

    /**
     * Current peer initial stream window.
     */
    private final AtomicInteger initialWindowSize;

    /**
     * Current peer maximum concurrent streams.
     */
    private final AtomicInteger maxConcurrentStreams;

    /**
     * Current peer maximum header list size.
     */
    private final AtomicInteger maxHeaderListSize;

    /**
     * True after a GOAWAY is received or sent.
     */
    private final AtomicBoolean shutdown;

    /**
     * Last stream id accepted by peer GOAWAY.
     */
    private final AtomicInteger goAwayLastStreamId;

    /**
     * Last observed PING ACK payload.
     */
    private final AtomicLong lastPingPayload;

    /**
     * Last safely ignored ALTSVC metadata.
     */
    private final AtomicReference<Http2AlternateService> lastAlternateService;

    /**
     * Server push observer.
     */
    private final PushObserver pushObserver;

    /**
     * Runtime dispatcher for push callbacks.
     */
    private final Dispatcher dispatcher;

    /**
     * Whether this connection owns the dispatcher lifecycle.
     */
    private final boolean ownsDispatcher;

    /**
     * Maximum locally accepted concurrent streams before peer limits are applied.
     */
    private final int localStreamLimit;

    /**
     * Connection-owned publication point for multiplex session and capacity state.
     */
    private final Connection.MultiplexAttachment multiplexAttachment;

    /**
     * Whether the local endpoint accepts peer-initiated push streams.
     */
    private final boolean pushEnabled;

    /**
     * Ensures the client preface and initial SETTINGS are written exactly once.
     */
    private final AtomicBoolean prefaceSent;

    /**
     * Aggregate bytes retained by inbound DATA queues across all active streams.
     */
    private final AtomicLong queuedInboundBytes;

    /**
     * Hard aggregate inbound DATA retention limit in bytes.
     */
    private final long maxQueuedInboundBytes;

    /**
     * Creates a connection.
     *
     * @param connection network connection
     */
    private Http2Connection(final Connection connection) {
        this(connection, PushObserver.canceling(), Dispatcher.create(), true);
    }

    /**
     * Creates a connection.
     *
     * @param connection   network connection
     * @param pushObserver push observer
     */
    private Http2Connection(final Connection connection, final PushObserver pushObserver) {
        this(connection, pushObserver, Dispatcher.create(), true);
    }

    /**
     * Creates a connection.
     *
     * @param connection     network connection
     * @param pushObserver   push observer
     * @param dispatcher     runtime dispatcher
     * @param ownsDispatcher true when close should stop dispatcher
     */
    private Http2Connection(final Connection connection, final PushObserver pushObserver, final Dispatcher dispatcher,
            final boolean ownsDispatcher) {
        this(connection, pushObserver, dispatcher, ownsDispatcher, Builder.HTTP2_CONNECTION_DEFAULT_MAX_QUEUED_DATA);
    }

    /**
     * Creates a connection with an explicit aggregate inbound DATA budget.
     *
     * @param connection            physical network connection
     * @param pushObserver          callback for server-push streams
     * @param dispatcher            dispatcher running reader and background work
     * @param ownsDispatcher        whether closing this connection closes the dispatcher
     * @param maxQueuedInboundBytes maximum aggregate queued inbound DATA bytes
     */
    private Http2Connection(final Connection connection, final PushObserver pushObserver, final Dispatcher dispatcher,
            final boolean ownsDispatcher, final long maxQueuedInboundBytes) {
        this.connection = require(connection, "Network connection");
        this.source = IoKit.buffer(this.connection.source());
        this.sink = this.connection.sink();
        this.frameWriter = new Http2FrameWriter(this.sink);
        this.writeScheduler = new Http2WriteScheduler(new Http2WriteScheduler.Handler() {

            /**
             * Converts an acquired scheduler command into one physical HTTP/2 frame.
             *
             * @param type     scheduler command type
             * @param streamId target stream identifier
             * @param flags    frame flags
             * @param value    raw frame type for generic FRAME and DATA_FRAME commands
             * @param payload  transferred payload buffer, or null for an empty payload
             */
            @Override
            public void handle(
                    final int type,
                    final int streamId,
                    final int flags,
                    final long value,
                    final Buffer payload) {
                if (type == Http2WriteScheduler.FLUSH || type == Http2WriteScheduler.CLOSE) {
                    return;
                }
                try {
                    frameWriter.maxFrameSize(maxFrameSize.get());
                    final Buffer body = payload == null ? new Buffer() : payload;
                    final int frameType = type == Http2WriteScheduler.FRAME || type == Http2WriteScheduler.DATA_FRAME
                            ? (int) value
                            : type;
                    frameWriter.writeFrame(frameType, streamId, flags, body, body.size());
                } catch (final IOException e) {
                    throw new SocketException("HTTP/2 physical frame write failed", e);
                }
            }

            /**
             * Flushes the connection-owned physical frame writer after a logical scheduler batch.
             */
            @Override
            public void flush() {
                try {
                    frameWriter.flush();
                } catch (final IOException e) {
                    throw new SocketException("HTTP/2 physical frame flush failed", e);
                }
            }

            /**
             * Publishes the first scheduler failure as a connection-level stream failure.
             *
             * @param failure writer-thread runtime failure
             */
            @Override
            public void failed(final RuntimeException failure) {
                connectionFailure.compareAndSet(null, streamFailure(failure));
            }
        });
        this.writerHandle = new AtomicReference<>();
        this.hpackWriter = new HpackCodec();
        this.hpackReader = new HpackCodec();
        this.streams = new ConcurrentHashMap<>();
        this.priorities = new ConcurrentHashMap<>();
        this.pushedStreams = ConcurrentHashMap.newKeySet();
        this.connectionWriteWindow = Http.Setting.DEFAULT_INITIAL_WINDOW_SIZE;
        this.connectionReceiveWindow = LOCAL_RECEIVE_WINDOW;
        this.unacknowledgedConnectionBytes = new AtomicLong();
        this.nextLocal = new AtomicInteger(Normal._1);
        this.nextRemote = new AtomicInteger(Normal._2);
        this.localStreamCount = new AtomicInteger();
        this.state = new AtomicReference<>(Status.OPENED);
        this.reader = new Http2Reader(this);
        this.readerStarted = new AtomicBoolean();
        this.readerHandle = new AtomicReference<>();
        this.flowLock = new ReentrantLock();
        this.flowChanged = flowLock.newCondition();
        this.frameWriteLock = new ReentrantLock();
        this.localHeaderOrder = new Object();
        this.nextLocalHeaderStreamId = Normal._1;
        this.abandonedLocalHeaders = ConcurrentHashMap.newKeySet();
        this.controlPayloadScratch = new Buffer();
        this.dispatcherClosed = new AtomicBoolean();
        this.connectionFailure = new AtomicReference<>();
        this.peerSettings = new AtomicReference<>(Http2Settings.defaults());
        this.maxFrameSize = new AtomicInteger(Builder.HTTP2_DEFAULT_MAX_FRAME_SIZE);
        this.initialWindowSize = new AtomicInteger((int) Http.Setting.DEFAULT_INITIAL_WINDOW_SIZE);
        this.maxConcurrentStreams = new AtomicInteger(Integer.MAX_VALUE);
        this.maxHeaderListSize = new AtomicInteger(Builder.BYTES_64_KIB);
        this.shutdown = new AtomicBoolean();
        this.goAwayLastStreamId = new AtomicInteger(Integer.MAX_VALUE);
        this.lastPingPayload = new AtomicLong();
        this.lastAlternateService = new AtomicReference<>();
        this.pushObserver = pushObserver == null ? PushObserver.canceling() : pushObserver;
        this.dispatcher = require(dispatcher, "Dispatcher");
        this.ownsDispatcher = ownsDispatcher;
        this.localStreamLimit = connection.destination() == null ? Normal._64
                : Math.min(Normal._64, Math.max(Normal._1, connection.destination().maxMultiplexStreams()));
        this.multiplexAttachment = connection.multiplexAttachment();
        this.pushEnabled = this.pushObserver != PushObserver.canceling();
        this.prefaceSent = new AtomicBoolean();
        this.queuedInboundBytes = new AtomicLong();
        if (maxQueuedInboundBytes <= Normal.LONG_ZERO
                || maxQueuedInboundBytes > Builder.HTTP2_CONNECTION_DEFAULT_MAX_QUEUED_DATA) {
            throw new ValidateException("HTTP/2 aggregate queued DATA limit must be between 1 and 64 MiB");
        }
        this.maxQueuedInboundBytes = maxQueuedInboundBytes;
        this.maxConcurrentStreams.set(localStreamLimit);
        publishCapacity(false);
    }

    /**
     * Creates a connection.
     *
     * @param connection network connection
     * @return HTTP/2 connection
     */
    public static Http2Connection create(final Connection connection) {
        return new Http2Connection(connection);
    }

    /**
     * Creates a connection with a push observer.
     *
     * @param connection   network connection
     * @param pushObserver push observer
     * @return HTTP/2 connection
     */
    public static Http2Connection create(final Connection connection, final PushObserver pushObserver) {
        return new Http2Connection(connection, pushObserver);
    }

    /**
     * Creates a connection with a shared dispatcher.
     *
     * @param connection network connection
     * @param dispatcher runtime dispatcher
     * @return HTTP/2 connection
     */
    public static Http2Connection create(final Connection connection, final Dispatcher dispatcher) {
        return new Http2Connection(connection, PushObserver.canceling(), require(dispatcher, "Dispatcher"), false);
    }

    /**
     * Creates a connection with a push observer and shared dispatcher.
     *
     * @param connection   network connection
     * @param pushObserver push observer
     * @param dispatcher   runtime dispatcher
     * @return HTTP/2 connection
     */
    public static Http2Connection create(
            final Connection connection,
            final PushObserver pushObserver,
            final Dispatcher dispatcher) {
        return new Http2Connection(connection, pushObserver, require(dispatcher, "Dispatcher"), false);
    }

    /**
     * Creates a connection with shared dispatch and a tightened aggregate DATA budget.
     *
     * @param connection            physical network connection
     * @param dispatcher            shared runtime dispatcher
     * @param maxQueuedInboundBytes maximum aggregate queued inbound DATA bytes
     * @return HTTP/2 connection using the supplied inbound budget
     */
    public static Http2Connection create(
            final Connection connection,
            final Dispatcher dispatcher,
            final long maxQueuedInboundBytes) {
        return new Http2Connection(connection, PushObserver.canceling(), require(dispatcher, "Dispatcher"), false,
                maxQueuedInboundBytes);
    }

    /**
     * Creates a stream.
     *
     * @param headers initial stream headers to encode
     * @param out     local direction
     * @return stream
     */
    public Http2Stream newStream(final Headers headers, final boolean out) {
        return newStream(headers, out, false);
    }

    /**
     * Creates a stream, optionally consuming a local slot already reserved by the caller.
     *
     * @param headers       initial stream headers
     * @param out           local direction
     * @param localReserved whether the local stream count was incremented before entry
     * @return registered stream
     */
    private Http2Stream newStream(final Headers headers, final boolean out, final boolean localReserved) {
        require(headers, "HTTP/2 stream headers");
        if (out && !localReserved) {
            reserveLocalStream();
        } else {
            if (!out) {
                ensureNewStreamAllowed();
            }
        }
        final int id;
        try {
            id = allocateStreamId(out ? nextLocal : nextRemote);
        } catch (final RuntimeException failure) {
            if (out) {
                localStreamCount.decrementAndGet();
                publishCapacity(false);
            }
            throw failure;
        }
        try {
            ensureNewStreamAllowed();
            final Http2Stream stream = new Http2Stream(id, headers, length -> releaseInbound(id, length),
                    () -> streamTerminated(id), () -> cancelStream(id));
            stream.initializeFlow(initialWindowSize.get(), LOCAL_RECEIVE_WINDOW);
            final Http2Priority priority = priorities.get(id);
            if (priority != null) {
                stream.priority(priority);
            }
            if (streams.putIfAbsent(id, stream) != null) {
                throw new ProtocolException("HTTP/2 stream id is already registered");
            }
            signalFlowWaiters();
            if (out) {
                publishCapacity(false);
            }
            return stream;
        } catch (final RuntimeException failure) {
            streams.remove(id);
            if (out) {
                localStreamCount.decrementAndGet();
                abandonLocalHeaders(id);
                publishCapacity(shutdown.get());
            }
            throw failure;
        }
    }

    /**
     * Atomically allocates a local stream and publishes its initial HEADERS in stream-id order. This closes the
     * scheduling gap between separate new-stream and write-headers calls under high producer counts.
     *
     * @param initialHeaders local compatibility header snapshot
     * @param requestHeaders HTTP/2 pseudo and regular request fields
     * @param endStream      whether HEADERS also closes the local side
     * @return newly registered stream
     */
    public Http2Stream openStream(
            final Headers initialHeaders,
            final List<Http2Header> requestHeaders,
            final boolean endStream) {
        final Headers checkedInitialHeaders = require(initialHeaders, "HTTP/2 stream headers");
        final List<Http2Header> checkedRequestHeaders = validateHeaders(requestHeaders);
        reserveLocalStream();
        synchronized (localHeaderOrder) {
            final Http2Stream stream = newStream(checkedInitialHeaders, true, true);
            try {
                startReader();
                final Http2Frame frame = Http2Frame.headers(stream.id(), checkedRequestHeaders, endStream);
                awaitLocalHeaderTurn(stream.id());
                writeFrame(frame);
                if (stream.id() == nextLocalHeaderStreamId) {
                    nextLocalHeaderStreamId = nextStreamId(nextLocalHeaderStreamId);
                    localHeaderOrder.notifyAll();
                }
                return stream;
            } catch (final RuntimeException failure) {
                stream.close();
                throw failure;
            }
        }
    }

    /**
     * Returns a stream.
     *
     * @param id stream id
     * @return stream or null
     */
    public Http2Stream stream(final int id) {
        return streams.get(id);
    }

    /**
     * Returns priority metadata for a stream.
     *
     * @param streamId stream id
     * @return priority or null
     */
    public Http2Priority priority(final int streamId) {
        positiveStream(streamId);
        final Http2Stream stream = streams.get(streamId);
        if (stream != null && stream.priority() != null) {
            return stream.priority();
        }
        return priorities.get(streamId);
    }

    /**
     * Returns the latest safely ignored ALTSVC event.
     *
     * @return alternate service or null
     */
    public Http2AlternateService lastAlternateService() {
        return lastAlternateService.get();
    }

    /**
     * Removes a stream.
     *
     * @param id stream id
     */
    public void remove(final int id) {
        if (id <= Normal._0) {
            throw new ValidateException("HTTP/2 stream id must be positive");
        }
        final Http2Stream stream = streams.get(id);
        if (stream != null) {
            try {
                stream.close();
            } catch (final RuntimeException e) {
                throw e instanceof InternalException internal ? internal
                        : new InternalException("Unable to remove HTTP/2 stream", e);
            } finally {
                streamTerminated(id);
            }
        }
    }

    /**
     * Removes one terminal stream and wakes every writer waiting on its flow window.
     *
     * @param streamId terminal stream id
     */
    void streamTerminated(final int streamId) {
        final Http2Stream removed = streams.remove(streamId);
        if (removed != null && localStream(streamId)) {
            localStreamCount.decrementAndGet();
        }
        flowLock.lock();
        try {
            priorities.remove(streamId);
            pushedStreams.remove(streamId);
            flowChanged.signalAll();
        } finally {
            flowLock.unlock();
        }
        if (localStream(streamId)) {
            synchronized (localHeaderOrder) {
                if (streamId >= nextLocalHeaderStreamId) {
                    abandonedLocalHeaders.add(streamId);
                }
                localHeaderOrder.notifyAll();
            }
        }
        publishCapacity(shutdown.get());
    }

    /**
     * Writes a frame.
     *
     * @param frame HTTP/2 frame to encode and write
     */
    public void writeFrame(final Http2Frame frame) {
        final Http2Frame checked = require(frame, "HTTP/2 frame");
        if (checked.type() == Normal._0) {
            writeData(
                    checked.streamId(),
                    new Buffer().write(checked.payloadBytes()),
                    checked.endStream(),
                    Duration.ZERO);
            return;
        }
        writeFrameSerialized(checked);
    }

    /**
     * Writes one header block for a stream.
     *
     * @param streamId  stream id
     * @param headers   HTTP/2 headers including pseudo headers
     * @param endStream whether the header block ends the local stream
     */
    public void writeHeaders(final int streamId, final List<Http2Header> headers, final boolean endStream) {
        positiveStream(streamId);
        final Http2Frame frame = Http2Frame.headers(streamId, validateHeaders(headers), endStream);
        if (!localStream(streamId)) {
            writeFrame(frame);
            return;
        }
        synchronized (localHeaderOrder) {
            awaitLocalHeaderTurn(streamId);
            writeFrame(frame);
            if (streamId == nextLocalHeaderStreamId) {
                nextLocalHeaderStreamId = nextStreamId(nextLocalHeaderStreamId);
                localHeaderOrder.notifyAll();
            }
        }
    }

    /**
     * Waits until every lower allocated local stream has emitted initial HEADERS or has terminated. HTTP/2 peers
     * permanently close skipped idle stream ids when they observe a higher id, so concurrent callers must not reorder
     * this first frame.
     *
     * @param streamId local stream waiting to emit its initial HEADERS
     */
    private void awaitLocalHeaderTurn(final int streamId) {
        while (streamId > nextLocalHeaderStreamId) {
            if (abandonedLocalHeaders.remove(nextLocalHeaderStreamId)) {
                nextLocalHeaderStreamId = nextStreamId(nextLocalHeaderStreamId);
                continue;
            }
            try {
                localHeaderOrder.wait();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new SocketException("Interrupted while ordering HTTP/2 request headers", e);
            }
        }
    }

    /**
     * Marks an allocated local id as safe to skip during ordered initial HEADERS emission.
     *
     * @param streamId allocated stream identifier that will not emit headers
     */
    private void abandonLocalHeaders(final int streamId) {
        synchronized (localHeaderOrder) {
            if (streamId >= nextLocalHeaderStreamId) {
                abandonedLocalHeaders.add(streamId);
            }
            localHeaderOrder.notifyAll();
        }
    }

    /**
     * Advances an odd local stream id without overflowing into a valid id.
     *
     * @param streamId current odd local stream identifier
     * @return next odd identifier or the terminal maximum sentinel
     */
    private static int nextStreamId(final int streamId) {
        return streamId > Integer.MAX_VALUE - Normal._2 ? Integer.MAX_VALUE : streamId + Normal._2;
    }

    /**
     * Writes DATA while waiting without an explicit deadline for positive flow credit.
     *
     * @param streamId  stream id
     * @param data      data buffer consumed by this method
     * @param endStream whether the last DATA frame ends the local stream
     */
    public void writeData(final int streamId, final Buffer data, final boolean endStream) {
        writeData(streamId, data, endStream, Duration.ZERO);
    }

    /**
     * Writes DATA frames whose payload is the minimum positive value allowed by remaining data, peer frame size,
     * connection credit and stream credit.
     *
     * @param streamId  stream id
     * @param data      data buffer consumed by this method
     * @param endStream whether the last DATA frame ends the local stream
     * @param timeout   maximum flow-control wait; zero waits until signaled
     */
    public void writeData(final int streamId, final Buffer data, final boolean endStream, final Duration timeout) {
        positiveStream(streamId);
        final Buffer remaining = require(data, "HTTP/2 data");
        final Duration checkedTimeout = require(timeout, "HTTP/2 write timeout");
        if (checkedTimeout.isNegative()) {
            throw new ValidateException("HTTP/2 write timeout must not be negative");
        }
        if (remaining.size() == Normal.LONG_ZERO) {
            if (endStream) {
                writeSingleFrameSerialized(Normal._0, streamId, Normal._1, new Buffer());
            }
            return;
        }
        final long deadline = checkedTimeout.isZero() ? Normal.LONG_ZERO : deadline(checkedTimeout);
        while (remaining.size() > Normal.LONG_ZERO) {
            final int frameLength = reserveWriteCredit(streamId, remaining.size(), deadline);
            final Buffer payload = new Buffer();
            payload.write(remaining, frameLength);
            final boolean last = remaining.size() == Normal.LONG_ZERO;
            writeSingleFrameSerialized(Normal._0, streamId, last && endStream ? Normal._1 : Normal._0, payload);
        }
    }

    /**
     * Writes one encoded frame.
     *
     * @param type     HTTP/2 frame type code
     * @param streamId stream id
     * @param flags    frame flags valid for the selected type
     * @param payload  encoded frame payload bytes
     */
    private void writeSingleFrameLocked(final int type, final int streamId, final int flags, final Buffer payload) {
        final int command = type == Normal._0 ? Http2WriteScheduler.DATA_FRAME : Http2WriteScheduler.FRAME;
        if (!writeScheduler.offer(command, streamId, flags, type, payload)) {
            throw new SocketException("HTTP/2 writer queue is full or closed");
        }
    }

    /**
     * Enqueues a flush command for the current logical frame group on the connection-owned writer.
     */
    private void flushFrames() {
        if (!writeScheduler.offer(Http2WriteScheduler.FLUSH, Normal._0, Normal._0, Normal.LONG_ZERO, null)) {
            throw new SocketException("HTTP/2 writer queue is full or closed");
        }
    }

    /**
     * Starts the connection-level reader loop once.
     */
    public void startReader() {
        if (state.get().terminal()) {
            throw new StatefulException("HTTP/2 connection is closed");
        }
        if (!readerStarted.compareAndSet(false, true)) {
            return;
        }
        sendClientPreface();
        final String writerKey = "http2:writer:" + System.identityHashCode(this);
        final DispatchHandle writerTask = dispatcher
                .background(writerKey, this, Activity.of(writerKey, writeScheduler::runWriter));
        if (!writerHandle.compareAndSet(null, writerTask)) {
            dispatcher.cancel(writerTask);
        }
        final String key = "http2:reader:" + System.identityHashCode(this);
        final Activity activity = Activity.of(key, this::readLoop);
        final DispatchHandle handle = dispatcher.background(key, this, activity);
        if (!readerHandle.compareAndSet(null, handle)) {
            dispatcher.cancel(handle);
        }
    }

    /**
     * Sends the client preface and initial SETTINGS exactly once before requests.
     */
    private void sendClientPreface() {
        if (!prefaceSent.compareAndSet(false, true)) {
            return;
        }
        frameWriteLock.lock();
        try {
            ensureWritable();
            write(new Buffer().writeUtf8(Builder.HTTP2_CONNECTION_PREFACE));
            final Http2Settings local = Http2Settings.defaults();
            if (!pushEnabled) {
                local.set(Http.Setting.ENABLE_PUSH_ID, Normal._0);
            }
            local.set(Http.Setting.INITIAL_WINDOW_SIZE_ID, LOCAL_RECEIVE_WINDOW);
            final Http2Frame settings = Http2Frame.settings(local);
            final Buffer payload = payload(settings);
            writeSingleFrameLocked(settings.type(), settings.streamId(), settings.flags(), payload);
            writeSingleFrameLocked(
                    Normal._8,
                    Normal._0,
                    Normal._0,
                    new Buffer().writeInt(LOCAL_RECEIVE_WINDOW - Http.Setting.DEFAULT_INITIAL_WINDOW_SIZE));
        } finally {
            frameWriteLock.unlock();
        }
    }

    /**
     * Sends a PING frame.
     *
     * @param payload opaque payload
     */
    public void ping(final long payload) {
        writeFrame(Http2Frame.ping(payload, false));
    }

    /**
     * Sends a GOAWAY frame and rejects new streams locally.
     *
     * @param lastStreamId last processed stream id
     * @param errorCode    error code
     * @param debugData    optional debug data
     */
    public void goAway(final int lastStreamId, final int errorCode, final ByteString debugData) {
        shutdown.set(true);
        publishCapacity(true);
        goAwayLastStreamId.set(lastStreamId);
        final String detail = debugData == null || debugData.size() == Normal._0 ? "" : ": " + debugData.utf8();
        failStreamsAbove(lastStreamId, new SocketException("HTTP/2 connection sent GOAWAY" + detail));
        writeFrame(Http2Frame.goAway(lastStreamId, errorCode, debugData));
    }

    /**
     * Creates a pushed stream and records it in the stream registry.
     *
     * @param streamId  pushed stream id
     * @param headers   pushed request headers
     * @param endStream true when already ended
     * @return pushed stream
     */
    public Http2Stream pushStream(final int streamId, final List<Http2Header> headers, final boolean endStream) {
        positiveStream(streamId);
        final List<Http2Header> snapshot = validateHeaders(headers);
        if (!pushedStreams.add(streamId)) {
            throw new ProtocolException("HTTP/2 pushed stream already exists");
        }
        final Http2Stream stream = new Http2Stream(streamId, toHeaders(snapshot),
                length -> releaseInbound(streamId, length), () -> streamTerminated(streamId),
                () -> cancelStream(streamId));
        stream.initializeFlow(initialWindowSize.get(), LOCAL_RECEIVE_WINDOW);
        final Http2Priority priority = priorities.get(streamId);
        if (priority != null) {
            stream.priority(priority);
        }
        streams.put(streamId, stream);
        signalFlowWaiters();
        if (endStream) {
            remove(streamId);
        }
        return stream;
    }

    /**
     * Returns whether a stream is a pushed stream.
     *
     * @param streamId stream id
     * @return true when pushed
     */
    public boolean pushedStream(final int streamId) {
        positiveStream(streamId);
        return pushedStreams.contains(streamId);
    }

    /**
     * Dispatches pushed request headers.
     *
     * @param streamId pushed stream id
     * @param headers  request headers
     * @return future whose value is true when canceled
     */
    public CompletableFuture<Boolean> pushRequestLater(final int streamId, final List<Http2Header> headers) {
        positiveStream(streamId);
        final List<Http2Header> snapshot = validateHeaders(headers);
        return dispatcher.supply("http2:push-request:" + streamId, () -> {
            final boolean cancel = pushObserver.onRequest(streamId, snapshot);
            if (cancel) {
                resetPushedStream(streamId, Normal._8);
            } else if (!pushedStreams.contains(streamId)) {
                pushStream(streamId, snapshot, false);
            }
            return cancel;
        });
    }

    /**
     * Dispatches pushed response headers.
     *
     * @param streamId  pushed stream id
     * @param headers   response headers
     * @param endStream true when ended
     * @return future whose value is true when canceled
     */
    public CompletableFuture<Boolean> pushHeadersLater(
            final int streamId,
            final List<Http2Header> headers,
            final boolean endStream) {
        positiveStream(streamId);
        final List<Http2Header> snapshot = validateHeaders(headers);
        return dispatcher.supply("http2:push-headers:" + streamId, () -> {
            final boolean cancel = pushObserver.onHeaders(streamId, snapshot, endStream);
            if (cancel) {
                resetPushedStream(streamId, Normal._8);
            } else if (endStream) {
                remove(streamId);
            }
            return cancel;
        });
    }

    /**
     * Dispatches pushed data.
     *
     * @param streamId  pushed stream id
     * @param data      pushed DATA payload bytes
     * @param endStream true when ended
     * @return future whose value is true when canceled
     */
    public CompletableFuture<Boolean> pushDataLater(
            final int streamId,
            final ByteString data,
            final boolean endStream) {
        positiveStream(streamId);
        final ByteString snapshot = Assert
                .notNull(data, () -> new ValidateException("HTTP/2 push data must not be null"));
        return dispatcher.supply("http2:push-data:" + streamId, () -> {
            try {
                final boolean cancel = pushObserver.onData(streamId, snapshot, endStream);
                if (cancel) {
                    resetPushedStream(streamId, Normal._8);
                } else if (endStream) {
                    remove(streamId);
                }
                return cancel;
            } finally {
                releaseInbound(streamId, snapshot.size());
            }
        });
    }

    /**
     * Dispatches a pushed stream reset.
     *
     * @param streamId  pushed stream id
     * @param errorCode error code
     * @return completion future
     */
    public CompletableFuture<Void> pushResetLater(final int streamId, final int errorCode) {
        positiveStream(streamId);
        if (errorCode < Normal._0) {
            throw new ValidateException("HTTP/2 push reset code must be non-negative");
        }
        return dispatcher.run("http2:push-reset:" + streamId, () -> {
            pushObserver.onReset(streamId, errorCode);
            pushedStreams.remove(streamId);
            streams.remove(streamId);
            flowLock.lock();
            try {
                flowChanged.signalAll();
            } finally {
                flowLock.unlock();
            }
            priorities.remove(streamId);
        });
    }

    /**
     * Updates a flow window.
     *
     * @param streamId stream id
     * @param delta    positive flow-control increment
     */
    public void updateWindow(final int streamId, final long delta) {
        if (streamId < Normal._0 || delta <= Normal._0 || delta > Integer.MAX_VALUE) {
            throw new ProtocolException("Invalid HTTP/2 window update");
        }
        flowLock.lock();
        try {
            if (streamId == Normal._0) {
                connectionWriteWindow = checkedWindowAdd(connectionWriteWindow, delta);
            } else {
                final Http2Stream stream = streams.get(streamId);
                if (stream == null) {
                    throw new ProtocolException("HTTP/2 stream is missing");
                }
                stream.writeWindow(checkedWindowAdd(stream.writeWindow(), delta));
            }
            flowChanged.signalAll();
        } finally {
            flowLock.unlock();
        }
    }

    /**
     * Returns whether connection is reusable.
     *
     * @return true when reusable
     */
    boolean opened() {
        return state.get() == Status.OPENED && connection.healthy();
    }

    /**
     * Returns the underlying network connection for HTTP/2 helpers in this package.
     *
     * @return network connection
     */
    Connection network() {
        return connection;
    }

    /**
     * Returns current peer maximum frame size.
     *
     * @return max frame size
     */
    int maxFrameSize() {
        return maxFrameSize.get();
    }

    /**
     * Returns current peer initial window size.
     *
     * @return initial window size
     */
    int initialWindowSize() {
        return initialWindowSize.get();
    }

    /**
     * Returns current peer maximum concurrent streams.
     *
     * @return max concurrent streams
     */
    int maxConcurrentStreams() {
        return maxConcurrentStreams.get();
    }

    /**
     * Returns current peer maximum header list size.
     *
     * @return max header list size
     */
    int maxHeaderListSize() {
        return maxHeaderListSize.get();
    }

    /**
     * Returns whether the connection is shutting down after GOAWAY.
     *
     * @return true when shutting down
     */
    boolean shutdown() {
        return shutdown.get();
    }

    /**
     * Returns last GOAWAY stream id.
     *
     * @return last stream id
     */
    int goAwayLastStreamId() {
        return goAwayLastStreamId.get();
    }

    /**
     * Returns last observed PING ACK payload.
     *
     * @return payload
     */
    long lastPingPayload() {
        return lastPingPayload.get();
    }

    /**
     * Closes this connection.
     */
    @Override
    public void close() {
        final Status current = state.getAndUpdate(value -> value == Status.CLOSED ? Status.CLOSED : Status.CLOSING);
        if (current == Status.CLOSED) {
            closeOwnedDispatcher();
            return;
        }
        if (current == Status.CLOSING) {
            try {
                connection.abort();
            } finally {
                closeOwnedDispatcher();
            }
            return;
        }
        final RuntimeException closed = new SocketException("HTTP/2 connection closed");
        shutdown.set(true);
        publishCapacity(true);
        connectionFailure.compareAndSet(null, closed);
        signalFlowWaiters();
        writeScheduler.abort();
        RuntimeException failure = null;
        // Break physical reads and writes before interrupting their owners. TLS/AIO writes are not guaranteed to
        // observe a virtual-thread interrupt until the underlying channel completes, so cancelling first can leave
        // the dispatcher retaining an already-CANCELLED writer until its shutdown deadline expires.
        try {
            connection.abort();
        } catch (final RuntimeException e) {
            failure = e;
        }
        final DispatchHandle handle = readerHandle.getAndSet(null);
        if (handle != null) {
            handle.cancel();
        }
        final DispatchHandle writer = writerHandle.getAndSet(null);
        if (writer != null) {
            writer.cancel();
        }
        try {
            reader.close();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = e;
            } else {
                failure.addSuppressed(e);
            }
        }
        for (final Map.Entry<Integer, Http2Stream> entry : streams.entrySet()) {
            try {
                entry.getValue().close();
            } catch (final RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
            }
        }
        streams.clear();
        localStreamCount.set(Normal._0);
        flowLock.lock();
        try {
            flowChanged.signalAll();
        } finally {
            flowLock.unlock();
        }
        queuedInboundBytes.set(Normal.LONG_ZERO);
        priorities.clear();
        pushedStreams.clear();
        try {
            closeOwnedDispatcher();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = e;
            }
        }
        state.set(Status.CLOSED);
        if (failure != null) {
            throw closeFailure(failure);
        }
    }

    /**
     * Runs the single connection reader and dispatches inbound frames by stream id.
     */
    private void readLoop() {
        try {
            while (opened()) {
                final Http2Frame frame = reader.nextFrame();
                try {
                    dispatch(frame);
                } finally {
                    frame.close();
                }
            }
        } catch (final RuntimeException e) {
            if (!state.get().terminal()) {
                if (e instanceof ProtocolException) {
                    try {
                        goAway(Normal._0, Normal._1, ByteString.encodeUtf8(e.getMessage()));
                    } catch (final RuntimeException ignored) {
                        // The original protocol failure remains authoritative.
                    }
                }
                closeAfterReaderFailure(e);
            }
        }
    }

    /**
     * Dispatches a decoded frame to connection state, push callbacks, or a stream queue.
     *
     * @param frame decoded frame to dispatch
     */
    private void dispatch(final Http2Frame frame) {
        switch (frame.type()) {
            case Normal._4 -> dispatchSettings(frame);
            case Normal._6 -> dispatchPing(frame);
            case Normal._7 -> dispatchGoAway(frame);
            case Normal._8 -> {
                updateWindow(frame.streamId(), frame.windowDelta());
                return;
            }
            case Normal._2 -> {
                applyPriority(frame.streamId(), frame.priority());
                return;
            }
            case Normal._10 -> {
                dispatchAlternateService(frame);
                return;
            }
            case Normal._5 -> {
                if (!pushEnabled) {
                    throw new ProtocolException("HTTP/2 PUSH_PROMISE received while server push is disabled");
                }
                pushRequestLater(frame.promisedStreamId(), frame.headers());
                return;
            }
            case Normal._1 -> dispatchHeaders(frame);
            case Normal._0 -> dispatchData(frame);
            case Normal._3 -> resetStream(frame.streamId(), frame.errorCode());
            default -> throw new ProtocolException("Unsupported HTTP/2 frame type");
        }
    }

    /**
     * Applies SETTINGS and acknowledges non-ACK settings frames.
     *
     * @param frame decoded SETTINGS frame to apply
     */
    private void dispatchSettings(final Http2Frame frame) {
        if (frame.ack()) {
            return;
        }
        final Http2Settings settings = frame.settings();
        if (settings != null) {
            applySettings(settings);
        }
        writeFrame(Http2Frame.settingsAck());
    }

    /**
     * Applies peer settings to connection state.
     *
     * @param settings peer settings to merge into connection state
     */
    private void applySettings(final Http2Settings settings) {
        if (settings.isSet(Http.Setting.ENABLE_PUSH_ID)) {
            throw new ProtocolException("Server SETTINGS_ENABLE_PUSH is invalid for a client connection");
        }
        final Http2Settings merged = peerSettings.get().copy();
        final int previousWindow = merged.initialWindowSize();
        merged.merge(settings);
        peerSettings.set(merged);
        maxFrameSize.set(merged.maxFrameSize());
        final int streamCapacity = (int) Math
                .min(Integer.MAX_VALUE, Math.min(localStreamLimit, merged.maxConcurrentStreamsUnsigned()));
        maxConcurrentStreams.set(streamCapacity);
        initialWindowSize.set(merged.initialWindowSize());
        if (settings.isSet(Http.Setting.MAX_HEADER_LIST_SIZE_ID)) {
            final int headerListSize = (int) Math.min(Builder.BYTES_64_KIB, merged.maxHeaderListSizeUnsigned());
            maxHeaderListSize.set(headerListSize);
        }
        final long delta = (long) merged.initialWindowSize() - previousWindow;
        flowLock.lock();
        try {
            if (delta != Normal.LONG_ZERO) {
                for (final Http2Stream stream : streams.values()) {
                    stream.writeWindow(checkedWindowAdjust(stream.writeWindow(), delta));
                }
            }
            flowChanged.signalAll();
        } finally {
            flowLock.unlock();
        }
        RuntimeException failure = null;
        frameWriteLock.lock();
        try {
            hpackWriter.maxTableSize(
                    (int) Math.min(Builder.BYTES_64_KIB, merged.getLong(Http.Setting.HEADER_TABLE_SIZE_ID)));
            if (settings.isSet(Http.Setting.MAX_HEADER_LIST_SIZE_ID)) {
                hpackWriter.maxHeaderListSize(maxHeaderListSize.get());
            }
        } catch (final RuntimeException e) {
            failure = e;
        } finally {
            frameWriteLock.unlock();
        }
        if (failure != null) {
            throw failure;
        }
        publishCapacity(false);
    }

    /**
     * Handles PING frames.
     *
     * @param frame decoded PING frame to process
     */
    private void dispatchPing(final Http2Frame frame) {
        if (frame.ack()) {
            lastPingPayload.set(frame.pingPayload());
            return;
        }
        writeFrame(Http2Frame.ping(frame.pingPayload(), true));
    }

    /**
     * Handles GOAWAY by preventing new streams and failing unprocessed high-id streams.
     *
     * @param frame decoded GOAWAY frame to process
     */
    private void dispatchGoAway(final Http2Frame frame) {
        shutdown.set(true);
        publishCapacity(true);
        goAwayLastStreamId.set(frame.lastStreamId());
        final RuntimeException failure = new SocketException("HTTP/2 connection received GOAWAY");
        failStreamsAbove(frame.lastStreamId(), failure);
    }

    /**
     * Fails active streams whose id is greater than a GOAWAY boundary.
     *
     * @param lastStreamId last accepted stream id
     * @param failure      stream failure
     */
    private void failStreamsAbove(final int lastStreamId, final RuntimeException failure) {
        for (final Map.Entry<Integer, Http2Stream> entry : streams.entrySet()) {
            final int streamId = entry.getKey();
            if ((streamId & Normal._1) != Normal._0 && streamId > lastStreamId) {
                try {
                    entry.getValue().fail(failure, Http2Stream.Outcome.GOAWAY_UNPROCESSED);
                } catch (final RuntimeException ignored) {
                    // The connection failure remains authoritative.
                } finally {
                    streamTerminated(streamId);
                }
            }
        }
    }

    /**
     * Dispatches a HEADERS frame.
     *
     * @param frame decoded HEADERS frame to dispatch
     */
    private void dispatchHeaders(final Http2Frame frame) {
        if (frame.priority() != null) {
            applyPriority(frame.streamId(), frame.priority());
        }
        if (pushedStreams.contains(frame.streamId())) {
            pushHeadersLater(frame.streamId(), frame.headers(), frame.endStream());
            return;
        }
        final Http2Stream stream = streams.get(frame.streamId());
        if (stream == null) {
            throw new ProtocolException("HTTP/2 frame references a missing stream");
        }
        final List<Http2Header> fields = frame.headers();
        String status = null;
        for (final Http2Header field : fields) {
            if (field.pseudo()) {
                if (!Http.Header.PSEUDO_STATUS.equals(field.name()) || status != null) {
                    throw new ProtocolException("Invalid HTTP/2 response pseudo-header");
                }
                status = field.value();
            }
        }
        stream.receiveHeaders(toResponseHeaders(fields), status, frame.endStream());
    }

    /**
     * Applies PRIORITY metadata.
     *
     * @param streamId stream id
     * @param priority priority metadata
     */
    private void applyPriority(final int streamId, final Http2Priority priority) {
        positiveStream(streamId);
        require(priority, "HTTP/2 priority");
        priorities.put(streamId, priority);
        final Http2Stream stream = streams.get(streamId);
        if (stream != null) {
            stream.priority(priority);
        }
    }

    /**
     * Reports ALTSVC metadata and leaves connection routing unchanged.
     *
     * @param frame decoded ALTSVC frame to report
     */
    private void dispatchAlternateService(final Http2Frame frame) {
        final Http2AlternateService alternateService = frame.alternateService();
        if (alternateService == null) {
            return;
        }
        lastAlternateService.set(alternateService);
        try {
            pushObserver.onAlternateService(frame.streamId(), alternateService);
        } catch (final RuntimeException ignored) {
            // ALTSVC is advisory; observer failures must not poison the connection.
        }
    }

    /**
     * Dispatches a DATA frame.
     *
     * @param frame decoded DATA frame to dispatch
     */
    private void dispatchData(final Http2Frame frame) {
        final int streamId = frame.streamId();
        final Http2Stream stream = streams.get(streamId);
        if (stream == null) {
            throw new ProtocolException("HTTP/2 frame references a missing stream");
        }
        final int length = frame.payloadSize();
        consumeReceiveWindows(streamId, length);
        if (!reserveInbound(length)) {
            acknowledgeConnectionInbound(length);
            resetStream(streamId, Normal._8);
            return;
        }
        acknowledgeConnectionInbound(length);
        if (pushedStreams.contains(streamId)) {
            final Buffer direct = frame.takeDataPayload();
            pushDataLater(streamId, direct == null ? frame.payloadBytes() : direct.readByteString(), frame.endStream());
            return;
        }
        final Buffer direct = frame.takeDataPayload();
        if (direct == null) {
            stream.receiveData(frame.payloadBytes(), frame.endStream());
        } else {
            stream.receiveData(direct, direct.size(), frame.endStream());
        }
    }

    /**
     * Consumes an unpadded DATA frame directly from the reader buffer without frame or payload objects.
     *
     * @param streamId target stream identifier
     * @param flags    DATA frame flags
     * @param source   reader buffer containing the payload
     * @param length   number of payload bytes to consume
     * @return {@code true} while the connection remains open
     */
    boolean dispatchData(final int streamId, final int flags, final Buffer source, final int length) {
        final Http2Stream stream = streams.get(streamId);
        if (stream == null) {
            throw new ProtocolException("HTTP/2 frame references a missing stream");
        }
        consumeReceiveWindows(streamId, length);
        if (!reserveInbound(length)) {
            discard(source, length);
            acknowledgeConnectionInbound(length);
            resetStream(streamId, Normal._8);
            return opened();
        }
        acknowledgeConnectionInbound(length);
        final boolean endStream = (flags & Normal._1) != Normal._0;
        if (pushedStreams.contains(streamId)) {
            final Buffer pushed = new Buffer();
            pushed.write(source, length);
            pushDataLater(streamId, pushed.readByteString(), endStream);
            return opened();
        }
        stream.receiveData(source, length, endStream);
        return opened();
    }

    /** Discards an exact direct-reader payload after a rejected aggregate reservation. */
    private static void discard(final Buffer source, final long length) {
        try {
            source.skip(length);
        } catch (final IOException e) {
            throw new SocketException("HTTP/2 DATA payload is truncated", e);
        }
    }

    /**
     * Consumes connection and stream inbound windows under one lock acquisition.
     *
     * @param streamId stream receiving DATA
     * @param length   byte count
     */
    private void consumeReceiveWindows(final int streamId, final int length) {
        if (length == Normal._0) {
            return;
        }
        flowLock.lock();
        try {
            if (connectionReceiveWindow < length) {
                throw new ProtocolException("HTTP/2 connection flow-control window is exhausted");
            }
            final Http2Stream stream = streams.get(streamId);
            if (stream == null || stream.receiveWindow() < length) {
                throw new ProtocolException("HTTP/2 stream receive window is exhausted");
            }
            connectionReceiveWindow -= length;
            stream.receiveWindow(stream.receiveWindow() - length);
        } finally {
            flowLock.unlock();
        }
    }

    /**
     * Reserves the connection-wide queued DATA budget without blocking the reader.
     *
     * @param length inbound DATA bytes to reserve
     * @return {@code true} when the aggregate budget accepted the bytes
     */
    private boolean reserveInbound(final long length) {
        if (length == Normal.LONG_ZERO) {
            return true;
        }
        long current;
        long next;
        do {
            current = queuedInboundBytes.get();
            next = current + length;
            if (next < current || next > maxQueuedInboundBytes) {
                return false;
            }
        } while (!queuedInboundBytes.compareAndSet(current, next));
        return true;
    }

    /**
     * Sends WINDOW_UPDATE frames when inbound data crosses the update threshold.
     *
     * @param length consumed length
     */
    private void acknowledgeConnectionInbound(final long length) {
        if (length == Normal._0) {
            return;
        }
        final long connectionDelta = accumulate(
                unacknowledgedConnectionBytes,
                length,
                Builder.HTTP2_CONNECTION_WINDOW_UPDATE_THRESHOLD);
        if (connectionDelta >= Builder.HTTP2_CONNECTION_WINDOW_UPDATE_THRESHOLD) {
            flowLock.lock();
            try {
                connectionReceiveWindow = checkedWindowAdd(connectionReceiveWindow, connectionDelta);
                flowChanged.signalAll();
            } finally {
                flowLock.unlock();
            }
            writeFrame(Http2Frame.windowUpdate(Normal._0, connectionDelta));
        }
    }

    /**
     * Releases aggregate queue bytes and batches the stream-level WINDOW_UPDATE.
     *
     * @param streamId stream whose queued bytes were consumed
     * @param length   number of consumed DATA bytes
     */
    private void releaseInbound(final int streamId, final long length) {
        if (length == Normal.LONG_ZERO) {
            return;
        }
        queuedInboundBytes.updateAndGet(current -> Math.max(Normal.LONG_ZERO, current - length));
        final Http2Stream stream = streams.get(streamId);
        if (stream == null || stream.remoteEnded()) {
            return;
        }
        final long streamDelta;
        flowLock.lock();
        try {
            streamDelta = stream.consumeForWindowUpdate(length, LOCAL_RECEIVE_WINDOW / Normal._2);
            if (streamDelta >= LOCAL_RECEIVE_WINDOW / Normal._2) {
                stream.receiveWindow(checkedWindowAdd(stream.receiveWindow(), streamDelta));
                flowChanged.signalAll();
            }
        } finally {
            flowLock.unlock();
        }
        if (streamDelta >= LOCAL_RECEIVE_WINDOW / Normal._2) {
            writeFrame(Http2Frame.windowUpdate(streamId, streamDelta));
        }
    }

    /**
     * Adds bytes to a counter and resets it when threshold is crossed.
     *
     * @param counter   byte counter updated atomically
     * @param length    number of bytes to add
     * @param threshold byte count that triggers a counter reset and update
     * @return accumulated length after the addition
     */
    private static long accumulate(final AtomicLong counter, final long length, final long threshold) {
        long current;
        long next;
        do {
            current = counter.get();
            next = current + length;
            if (next < current || next > Integer.MAX_VALUE) {
                throw new ProtocolException("HTTP/2 flow-control update overflow");
            }
        } while (!counter.compareAndSet(current, next >= threshold ? Normal._0 : next));
        return next;
    }

    /**
     * Fails a reset stream without affecting other active streams.
     *
     * @param streamId  stream id
     * @param errorCode HTTP/2 reset code
     */
    private void resetStream(final int streamId, final int errorCode) {
        if (pushedStreams.contains(streamId)) {
            pushResetLater(streamId, errorCode);
            return;
        }
        final RuntimeException failure = new SocketException(
                errorCode == Normal._7 ? "HTTP/2 stream was refused before processing"
                        : "HTTP/2 stream was reset: errorCode=" + errorCode);
        final Http2Stream stream = streams.get(streamId);
        if (stream != null) {
            try {
                stream.fail(
                        failure,
                        errorCode == Normal._7 ? Http2Stream.Outcome.REFUSED_STREAM : Http2Stream.Outcome.RESET);
            } catch (final RuntimeException ignored) {
                // The reset failure remains authoritative.
            } finally {
                streamTerminated(streamId);
            }
        }
    }

    /**
     * Cancels one locally closed stream and releases all of its flow-control state.
     *
     * @param streamId stream id
     */
    private void cancelStream(final int streamId) {
        final Status current = state.get();
        if (streams.containsKey(streamId) && (current == Status.OPENED || current == Status.RUNNING)) {
            writeFrame(Http2Frame.rstStream(streamId, Normal._8));
        }
        streamTerminated(streamId);
    }

    /**
     * Decodes one HPACK header block with the connection decoder state.
     *
     * @param source encoded header block
     * @return decoded headers
     */
    List<Http2Header> decodeHeaders(final Buffer source) {
        synchronized (hpackReader) {
            return hpackReader.decode(require(source, "HTTP/2 header block"));
        }
    }

    /**
     * Closes network resources after the reader loop observes a fatal connection failure.
     *
     * @param cause failure cause
     */
    private void closeAfterReaderFailure(final RuntimeException cause) {
        final RuntimeException failure = streamFailure(cause);
        if (!state.compareAndSet(Status.OPENED, Status.CLOSING)
                && !state.compareAndSet(Status.RUNNING, Status.CLOSING)) {
            return;
        }
        connectionFailure.compareAndSet(null, failure);
        shutdown.set(true);
        publishCapacity(true);
        signalFlowWaiters();
        writeScheduler.abort();
        final DispatchHandle writer = writerHandle.getAndSet(null);
        if (writer != null) {
            writer.cancel();
        }
        for (final Map.Entry<Integer, Http2Stream> entry : streams.entrySet()) {
            try {
                entry.getValue().fail(failure);
            } catch (final RuntimeException ignored) {
                // The connection failure remains authoritative.
            }
        }
        streams.clear();
        localStreamCount.set(Normal._0);
        flowLock.lock();
        try {
            flowChanged.signalAll();
        } finally {
            flowLock.unlock();
        }
        queuedInboundBytes.set(Normal.LONG_ZERO);
        priorities.clear();
        pushedStreams.clear();
        try {
            reader.close();
        } finally {
            state.set(Status.CLOSED);
            // The TLS failure callback must unwind before final abort joins the opposite engine direction. Directory
            // shutdown may perform the same idempotent abort sooner; this fallback covers standalone connections.
            CompletableFuture.runAsync(() -> {
                try {
                    connection.abort();
                } finally {
                    if (ownsDispatcher)
                        closeOwnedDispatcher();
                }
            });
        }
    }

    /**
     * Wakes every writer waiting for flow credit or a stream terminal transition.
     */
    private void signalFlowWaiters() {
        flowLock.lock();
        try {
            flowChanged.signalAll();
        } finally {
            flowLock.unlock();
        }
    }

    /**
     * Publishes the saturated protocol stream capacity to the physical connection.
     *
     * @param draining whether shutdown prevents creation of new streams
     */
    private void publishCapacity(final boolean draining) {
        if (multiplexAttachment != null) {
            final int available = Math.max(Normal._0, maxConcurrentStreams.get() - localStreamCount.get());
            multiplexAttachment.publish(draining ? Normal._0 : available, draining);
        }
    }

    /**
     * Reserves one peer-authorized local stream slot without serializing unrelated stream creation.
     */
    private void reserveLocalStream() {
        flowLock.lock();
        try {
            while (true) {
                ensureNewStreamAllowed();
                final int current = localStreamCount.get();
                if (current < maxConcurrentStreams.get()) {
                    localStreamCount.incrementAndGet();
                    return;
                }
                try {
                    flowChanged.await();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SocketException("Interrupted while waiting for HTTP/2 stream capacity", e);
                }
            }
        } finally {
            flowLock.unlock();
        }
    }

    /**
     * Ensures the connection can register another logical stream.
     */
    private void ensureNewStreamAllowed() {
        if (state.get().terminal()) {
            throw new StatefulException("HTTP/2 connection is closed");
        }
        if (shutdown.get()) {
            throw new StatefulException("HTTP/2 connection is shutting down");
        }
    }

    /**
     * Atomically allocates one positive stream id and leaves a terminal sentinel after exhaustion.
     *
     * @param sequence local or remote stream-id sequence
     * @return allocated stream id
     */
    private static int allocateStreamId(final AtomicInteger sequence) {
        while (true) {
            final int current = sequence.get();
            if (current <= Normal._0) {
                throw new ProtocolException("HTTP/2 stream id overflow");
            }
            final int next = current > Integer.MAX_VALUE - Normal._2 ? Normal._0 : current + Normal._2;
            if (sequence.compareAndSet(current, next)) {
                return current;
            }
        }
    }

    /**
     * Returns whether an id belongs to a locally initiated client stream.
     *
     * @param streamId stream identifier to classify
     * @return {@code true} for an odd locally initiated client stream id
     */
    private static boolean localStream(final int streamId) {
        return (streamId & Normal._1) != Normal._0;
    }

    /**
     * Closes an owned dispatcher exactly once.
     */
    private void closeOwnedDispatcher() {
        if (ownsDispatcher && dispatcherClosed.compareAndSet(false, true)) {
            dispatcher.close();
        }
    }

    /**
     * Creates a payload for a frame.
     *
     * @param frame frame whose DATA bytes form the payload
     * @return payload
     */
    private Buffer payload(final Http2Frame frame) {
        if (frame.type() == Normal._1) {
            final Buffer headers = hpackWriter.encodeBuffer(frame.headers());
            if (frame.priority() == null) {
                return headers;
            }
            final Buffer payload = new Buffer().write(frame.priority().encodeBytes());
            payload.write(headers, headers.size());
            return payload;
        }
        if (frame.type() == Normal._5) {
            final Buffer headers = hpackWriter.encodeBuffer(frame.headers());
            final Buffer payload = new Buffer();
            payload.writeInt(frame.promisedStreamId());
            payload.write(headers, headers.size());
            return payload;
        }
        if (frame.type() == Normal._2) {
            return new Buffer().write(frame.priority().encodeBytes());
        }
        if (frame.type() == Normal._10) {
            return new Buffer().write(frame.alternateService().encodeBytes());
        }
        return controlPayloadScratch.write(frame.payloadBytes());
    }

    /**
     * Encodes and writes one non-DATA frame under the unique physical write lock.
     *
     * @param frame non-DATA frame to encode and write
     */
    private void writeFrameSerialized(final Http2Frame frame) {
        RuntimeException failure = null;
        frameWriteLock.lock();
        try {
            ensureWritable();
            final Buffer encoded = payload(frame);
            if ((frame.type() == Normal._1 || frame.type() == Normal._5) && encoded.size() > maxFrameSize.get()) {
                writeHeaderGroupLocked(frame, encoded);
            } else {
                validateFrame(
                        frame.type(),
                        frame.streamId(),
                        frame.flags(),
                        toIntSize(encoded.size()),
                        maxFrameSize.get());
                writeSingleFrameLocked(frame.type(), frame.streamId(), frame.flags(), encoded);
            }
        } catch (final RuntimeException e) {
            failure = e;
        } finally {
            frameWriteLock.unlock();
        }
        if (failure != null) {
            failAfterPhysicalWrite(failure);
            throw failure;
        }
    }

    /**
     * Writes one HEADERS/PUSH_PROMISE plus contiguous CONTINUATION group atomically.
     *
     * @param frame   logical HEADERS or PUSH_PROMISE frame
     * @param encoded HPACK block consumed into contiguous frame fragments
     */
    private void writeHeaderGroupLocked(final Http2Frame frame, final Buffer encoded) {
        final int maximum = maxFrameSize.get();
        int type = frame.type();
        int flags = frame.flags() & ~Normal._4;
        while (encoded.size() > Normal.LONG_ZERO) {
            final int length = (int) Math.min(encoded.size(), maximum);
            final Buffer fragment = new Buffer();
            fragment.write(encoded, length);
            final boolean last = encoded.size() == Normal.LONG_ZERO;
            if (type == Normal._9) {
                flags = last ? Normal._4 : Normal._0;
            } else if (last) {
                flags |= Normal._4;
            }
            writeSingleFrameLocked(type, frame.streamId(), flags, fragment);
            type = Normal._9;
        }
    }

    /**
     * Writes one already encoded frame under the unique physical write lock.
     *
     * @param type     frame type
     * @param streamId stream id
     * @param flags    frame flags
     * @param payload  encoded payload
     */
    private void writeSingleFrameSerialized(final int type, final int streamId, final int flags, final Buffer payload) {
        RuntimeException failure = null;
        frameWriteLock.lock();
        try {
            ensureWritable();
            validateFrame(type, streamId, flags, toIntSize(payload.size()), maxFrameSize.get());
            writeSingleFrameLocked(type, streamId, flags, payload);
        } catch (final RuntimeException e) {
            failure = e;
        } finally {
            frameWriteLock.unlock();
        }
        if (failure != null) {
            failAfterPhysicalWrite(failure);
            throw failure;
        }
    }

    /**
     * Reserves the next positive DATA payload under the flow lock.
     *
     * @param streamId  stream id
     * @param remaining remaining body bytes
     * @param deadline  absolute monotonic deadline, or zero for no explicit deadline
     * @return reserved frame payload length
     */
    private int reserveWriteCredit(final int streamId, final long remaining, final long deadline) {
        flowLock.lock();
        try {
            while (true) {
                ensureWritable();
                final Http2Stream stream = streams.get(streamId);
                if (stream == null) {
                    throw streamFailure("HTTP/2 stream is terminal");
                }
                final long available = Math.min(
                        Math.min(remaining, maxFrameSize.get()),
                        Math.min(connectionWriteWindow, stream.writeWindow()));
                if (available > Normal.LONG_ZERO) {
                    connectionWriteWindow -= available;
                    stream.writeWindow(stream.writeWindow() - available);
                    return toIntSize(available);
                }
                awaitFlowChange(streamId, deadline);
            }
        } finally {
            flowLock.unlock();
        }
    }

    /**
     * Waits for a flow-control or terminal state change while holding the flow lock.
     *
     * @param streamId stream id used in timeout diagnostics
     * @param deadline absolute monotonic deadline, or zero for no explicit deadline
     */
    private void awaitFlowChange(final int streamId, final long deadline) {
        try {
            if (deadline == Normal.LONG_ZERO) {
                flowChanged.await();
                return;
            }
            final long remaining = deadline - System.nanoTime();
            if (remaining <= Normal.LONG_ZERO || flowChanged.awaitNanos(remaining) <= Normal.LONG_ZERO) {
                throw new TimeoutException("Timed out waiting for HTTP/2 write window for stream " + streamId);
            }
        } catch (final InterruptedException e) {
            ThreadKit.interrupt(Thread.currentThread(), false);
            throw new InternalException("Interrupted while waiting for HTTP/2 write window for stream " + streamId, e);
        }
    }

    /**
     * Returns a safe monotonic deadline for one duration.
     *
     * @param timeout duration measured from the current monotonic time
     * @return deadline
     */
    private static long deadline(final Duration timeout) {
        try {
            final long now = System.nanoTime();
            final long nanos = timeout.toNanos();
            return nanos >= Long.MAX_VALUE - now ? Long.MAX_VALUE : now + nanos;
        } catch (final ArithmeticException e) {
            return Long.MAX_VALUE;
        }
    }

    /**
     * Ensures physical writes are still permitted.
     */
    private void ensureWritable() {
        final RuntimeException failure = connectionFailure.get();
        if (failure != null) {
            throw failure;
        }
        if (state.get().terminal()) {
            throw new StatefulException("HTTP/2 connection is closed");
        }
    }

    /**
     * Permanently fails the connection after a physical frame write may have made partial progress.
     *
     * @param failure physical write failure
     */
    private void failAfterPhysicalWrite(final RuntimeException failure) {
        if (failure instanceof SocketException) {
            closeAfterReaderFailure(streamFailure(failure));
        }
    }

    /**
     * Adds a positive flow-control delta without exceeding the protocol maximum.
     *
     * @param current current window
     * @param delta   positive delta
     * @return adjusted window
     */
    private static long checkedWindowAdd(final long current, final long delta) {
        if (delta <= Normal.LONG_ZERO || current < Normal.LONG_ZERO || current > Integer.MAX_VALUE - delta) {
            throw new ProtocolException("HTTP/2 flow-control window overflow");
        }
        return current + delta;
    }

    /**
     * Adjusts a stream window after SETTINGS_INITIAL_WINDOW_SIZE changes.
     *
     * @param current current window
     * @param delta   signed settings delta
     * @return adjusted window
     */
    private static long checkedWindowAdjust(final long current, final long delta) {
        final long adjusted;
        try {
            adjusted = Math.addExact(current, delta);
        } catch (final ArithmeticException e) {
            throw new ProtocolException("HTTP/2 flow-control window overflow", e);
        }
        if (adjusted > Integer.MAX_VALUE || adjusted < -Integer.MAX_VALUE) {
            throw new ProtocolException("HTTP/2 flow-control window overflow");
        }
        return adjusted;
    }

    /**
     * Validates supported frame metadata.
     *
     * @param type     HTTP/2 frame type code
     * @param streamId stream id
     * @param flags    frame flags to validate
     * @param length   payload length
     */
    private static void validateFrame(final int type, final int streamId, final int flags, final int length) {
        validateFrame(type, streamId, flags, length, Builder.HTTP2_DEFAULT_MAX_FRAME_SIZE);
    }

    /**
     * Validates supported frame metadata.
     *
     * @param type         HTTP/2 frame type code
     * @param streamId     stream id
     * @param flags        frame flags to validate
     * @param length       payload length
     * @param maxFrameSize max payload length
     */
    private static void validateFrame(
            final int type,
            final int streamId,
            final int flags,
            final int length,
            final int maxFrameSize) {
        if (streamId < Normal._0 || streamId > Integer.MAX_VALUE || flags < Normal._0
                || flags > Builder.UNSIGNED_BYTE_MASK || length < Normal._0 || length > maxFrameSize) {
            throw new ProtocolException("Invalid HTTP/2 frame metadata");
        }
        if (type == Normal._4) {
            if (streamId != Normal._0 || (flags & ~Normal._1) != Normal._0
                    || ((flags & Normal._1) != Normal._0 && length != Normal._0) || length % Normal._6 != Normal._0) {
                throw new ProtocolException("Invalid HTTP/2 SETTINGS frame");
            }
            return;
        }
        if (type == Normal._6) {
            if (streamId != Normal._0 || (flags & ~Normal._1) != Normal._0 || length != Normal._8) {
                throw new ProtocolException("Invalid HTTP/2 PING frame");
            }
            return;
        }
        if (type == Normal._7) {
            if (streamId != Normal._0 || flags != Normal._0 || length < Normal._4 * Normal._2) {
                throw new ProtocolException("Invalid HTTP/2 GOAWAY frame");
            }
            return;
        }
        if (type == Normal._8) {
            if (flags != Normal._0 || length != Normal._4) {
                throw new ProtocolException("Invalid HTTP/2 WINDOW_UPDATE frame");
            }
            return;
        }
        if (type == Normal._10) {
            if (flags != Normal._0 || length < Normal._2) {
                throw new ProtocolException("Invalid HTTP/2 ALTSVC frame");
            }
            return;
        }
        if (streamId <= Normal._0) {
            throw new ProtocolException("Invalid HTTP/2 stream frame id");
        }
        switch (type) {
            case Normal._0 -> validateFlags(flags, Normal._1);
            case Normal._1 -> {
                validateFlags(flags, Normal._1 | Normal._4 | Normal._32);
                if ((flags & Normal._32) != Normal._0 && length < Normal._5) {
                    throw new ProtocolException("Invalid HTTP/2 HEADERS priority payload");
                }
            }
            case Normal._2 -> {
                validateFlags(flags, Normal._0);
                if (length != Normal._5) {
                    throw new ProtocolException("Invalid HTTP/2 PRIORITY length");
                }
            }
            case Normal._5 -> {
                validateFlags(flags, Normal._4);
                if (length < Normal._4) {
                    throw new ProtocolException("Invalid HTTP/2 PUSH_PROMISE frame");
                }
            }
            case Normal._3 -> {
                validateFlags(flags, Normal._0);
                if (length != Normal._4) {
                    throw new ProtocolException("Invalid HTTP/2 RST_STREAM length");
                }
            }
            default -> throw new ProtocolException("Unsupported HTTP/2 frame type");
        }
    }

    /**
     * Decodes optional HEADERS priority metadata.
     *
     * @param streamId stream id
     * @param flags    HEADERS frame flags controlling optional fields
     * @param payload  original HEADERS payload
     * @return priority or null
     */
    private static Http2Priority decodeHeaderPriority(final int streamId, final int flags, final ByteString payload) {
        if ((flags & Normal._32) == Normal._0) {
            return null;
        }
        return Http2Priority.decode(payload, streamId);
    }

    /**
     * Returns the HPACK fragment from a HEADERS payload.
     *
     * @param flags   HEADERS frame flags controlling optional fields
     * @param payload original HEADERS payload
     * @return header fragment
     */
    private static ByteString headerFragment(final int flags, final ByteString payload) {
        if ((flags & Normal._32) != Normal._0) {
            return payload.substring(Normal._5);
        }
        return payload;
    }

    /**
     * Reads CONTINUATION frames until the header block is complete.
     *
     * @param streamId stream id
     * @param flags    first frame flags
     * @param first    first header block fragment
     * @return complete header block payload
     */
    private ByteString headerBlock(final int streamId, final int flags, final ByteString first) {
        if ((flags & Normal._4) != Normal._0) {
            if (first.size() > Builder.BYTES_64_KIB) {
                throw new ProtocolException("HTTP/2 header block exceeds max size");
            }
            return first;
        }
        final Buffer fragments = new Buffer();
        int total = appendHeaderFragment(fragments, first, Normal._0);
        int currentFlags = flags;
        while ((currentFlags & Normal._4) == Normal._0) {
            final Buffer header = readFully(Builder.HTTP2_FRAME_HEADER_BYTES);
            final int length = readMedium(header);
            final int type = header.readByte() & Builder.UNSIGNED_BYTE_MASK;
            currentFlags = header.readByte() & Builder.UNSIGNED_BYTE_MASK;
            final int continuationStreamId = header.readInt() & Integer.MAX_VALUE;
            validateContinuation(streamId, type, continuationStreamId, currentFlags, length);
            total = appendHeaderFragment(fragments, readFully(length).readByteString(), total);
        }
        return fragments.readByteString();
    }

    /**
     * Adds a header block fragment and checks the accumulated size.
     *
     * @param fragments accumulated header block fragments
     * @param fragment  new fragment
     * @param total     current total
     * @return updated total
     */
    private static int appendHeaderFragment(final Buffer fragments, final ByteString fragment, final int total) {
        final int next = total + fragment.size();
        if (next < total || next > Builder.BYTES_64_KIB) {
            throw new ProtocolException("HTTP/2 header block exceeds max size");
        }
        fragments.write(fragment);
        return next;
    }

    /**
     * Validates a CONTINUATION frame while reading a header block.
     *
     * @param expectedStreamId expected stream id
     * @param type             frame type
     * @param streamId         frame stream id
     * @param flags            frame flags
     * @param length           payload length
     */
    private static void validateContinuation(
            final int expectedStreamId,
            final int type,
            final int streamId,
            final int flags,
            final int length) {
        if (type != Normal._9 || streamId != expectedStreamId || streamId <= Normal._0 || length < Normal._0
                || length > Builder.HTTP2_DEFAULT_MAX_FRAME_SIZE) {
            throw new ProtocolException("Invalid HTTP/2 CONTINUATION frame");
        }
        validateFlags(flags, Normal._4);
    }

    /**
     * Validates frame flags.
     *
     * @param flags   actual frame flags
     * @param allowed allowed mask
     */
    private static void validateFlags(final int flags, final int allowed) {
        if ((flags & ~allowed) != Normal._0) {
            throw new ProtocolException("Unsupported HTTP/2 frame flags");
        }
    }

    /**
     * Consumes flow-control windows for DATA.
     *
     * @param streamId stream id
     * @param length   byte count
     */
    private void consumeWindow(final int streamId, final int length) {
        throw new InternalException("Legacy HTTP/2 flow reservation must not be used");
    }

    /**
     * Subtracts from a flow-control window.
     *
     * @param window flow-control window to debit
     * @param length number of bytes to subtract
     * @return true when the window was available
     */
    private static boolean subtractWindow(final AtomicLong window, final long length) {
        return false;
    }

    /**
     * Adds to a flow-control window.
     *
     * @param window flow-control window to credit
     * @param delta  positive increment to add
     */
    private static void addWindow(final AtomicLong window, final long delta) {
        throw new InternalException("Legacy HTTP/2 flow update must not be used");
    }

    /**
     * Adjusts a flow-control window by a signed delta.
     *
     * @param window flow-control window to adjust
     * @param delta  signed delta
     */
    private static void adjustWindow(final AtomicLong window, final long delta) {
        throw new InternalException("Legacy HTTP/2 flow adjustment must not be used");
    }

    /**
     * Writes a reset for a pushed stream and removes local tracking.
     *
     * @param streamId  stream id
     * @param errorCode error code
     */
    private void resetPushedStream(final int streamId, final int errorCode) {
        writeFrame(Http2Frame.rstStream(streamId, errorCode));
        streamTerminated(streamId);
    }

    /**
     * Converts HTTP/2 headers to root headers.
     *
     * @param headers HTTP/2 headers
     * @return root headers
     */
    private static Headers toHeaders(final List<Http2Header> headers) {
        final Headers.Builder builder = Headers.builder();
        for (final Http2Header header : headers) {
            if (!header.pseudo()) {
                builder.add(header.name(), header.value());
            }
        }
        return builder.build();
    }

    /**
     * Reuses the immutable root-header snapshot when a peer repeats the same response field block. HPACK-heavy services
     * commonly return an identical content type and length for every response; canonicalizing that immutable value also
     * lets callers reuse its lazy map view.
     *
     * @param headers decoded response fields owned by the reader loop
     * @return canonical immutable headers
     */
    private Headers toResponseHeaders(final List<Http2Header> headers) {
        final List<Http2Header> cachedFields = cachedResponseFields;
        if (cachedFields != null && cachedFields.equals(headers)) {
            return cachedResponseHeaders;
        }
        final Headers converted = toHeaders(headers);
        cachedResponseFields = headers;
        cachedResponseHeaders = converted;
        return converted;
    }

    /**
     * Validates and snapshots headers.
     *
     * @param headers header collection to validate and copy
     * @return snapshot
     */
    private static List<Http2Header> validateHeaders(final List<Http2Header> headers) {
        final List<Http2Header> checkedHeaders = Assert
                .notNull(headers, () -> new ValidateException("HTTP/2 push headers must not contain null values"));
        for (final Http2Header header : checkedHeaders) {
            Assert.notNull(header, () -> new ValidateException("HTTP/2 push headers must not contain null values"));
        }
        return List.copyOf(checkedHeaders);
    }

    /**
     * Returns the HPACK header block from a PUSH_PROMISE payload.
     *
     * @param payload PUSH_PROMISE payload containing the header block
     * @return header block
     */
    private static ByteString pushHeaderBlock(final ByteString payload) {
        return payload.substring(Normal._4);
    }

    /**
     * Validates stream id.
     *
     * @param streamId stream id
     */
    private static void positiveStream(final int streamId) {
        if (streamId <= Normal._0) {
            throw new ValidateException("HTTP/2 stream id must be positive");
        }
    }

    /**
     * Classifies close failure.
     *
     * @param failure failure raised while closing the connection
     * @return runtime failure
     */
    private static RuntimeException closeFailure(final RuntimeException failure) {
        if (failure instanceof SocketException || failure instanceof InternalException
                || failure instanceof StatefulException) {
            return failure;
        }
        return new InternalException("Unable to close HTTP/2", failure);
    }

    /**
     * Returns the connection failure, falling back to a stream state failure.
     *
     * @param fallback fallback message
     * @return failure
     */
    private RuntimeException streamFailure(final String fallback) {
        final RuntimeException failure = connectionFailure.get();
        return failure == null ? new StatefulException(fallback) : failure;
    }

    /**
     * Classifies a reader failure for stream delivery.
     *
     * @param cause reader failure to expose to a stream
     * @return stream-visible failure
     */
    private static RuntimeException streamFailure(final RuntimeException cause) {
        if (cause instanceof SocketException || cause instanceof ProtocolException || cause instanceof TimeoutException
                || cause instanceof InternalException || cause instanceof StatefulException) {
            return cause;
        }
        return new SocketException("HTTP/2 reader failed", cause);
    }

    /**
     * Writes bytes.
     *
     * @param source buffer whose remaining bytes are written
     */
    private void write(final Buffer source) {
        final Buffer payload = require(source, "HTTP/2 write buffer");
        if (payload.size() == Normal._0) {
            return;
        }
        try {
            sink.write(payload, payload.size());
        } catch (final IOException e) {
            throw new SocketException("HTTP/2 write failed", e);
        }
    }

    /**
     * Converts a buffer size to int.
     *
     * @param size buffer size to range-check and convert
     * @return int size
     */
    private static int toIntSize(final long size) {
        if (size > Integer.MAX_VALUE) {
            throw new ProtocolException("HTTP/2 buffer exceeds integer range");
        }
        return (int) size;
    }

    /**
     * Reads an exact byte count into a core buffer.
     *
     * @param length exact number of network bytes to read
     * @return buffer
     */
    private Buffer readFully(final int length) {
        final Buffer buffer = new Buffer();
        while (buffer.size() < length) {
            final long remaining = Math.min(length - buffer.size(), Normal._16384);
            final long read;
            try {
                read = source.read(buffer, remaining);
            } catch (final IOException e) {
                throw new SocketException("HTTP/2 read failed", e);
            }
            if (read < Normal._0) {
                throw new SocketException("HTTP/2 frame reached EOF");
            }
        }
        return buffer;
    }

    /**
     * Reads a 24-bit unsigned integer.
     *
     * @param buffer buffer positioned at the three-byte integer
     * @return decoded unsigned 24-bit value
     */
    private static int readMedium(final Buffer buffer) {
        return ((buffer.readByte() & Builder.UNSIGNED_BYTE_MASK) << Normal._16)
                | ((buffer.readByte() & Builder.UNSIGNED_BYTE_MASK) << Normal._8)
                | (buffer.readByte() & Builder.UNSIGNED_BYTE_MASK);
    }

    /**
     * Validates required value.
     *
     * @param value reference to validate
     * @param name  diagnostic parameter name
     * @param <T>   type
     * @return the validated reference
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

}
