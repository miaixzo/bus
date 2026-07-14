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

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.lang.Assert;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.lang.exception.ProtocolException;
import org.miaixz.bus.core.lang.exception.SocketException;
import org.miaixz.bus.core.lang.exception.StatefulException;
import org.miaixz.bus.core.lang.exception.TimeoutException;
import org.miaixz.bus.core.lang.exception.ValidateException;
import org.miaixz.bus.fabric.Headers;
import org.miaixz.bus.fabric.Status;
import org.miaixz.bus.fabric.network.Connection;
import org.miaixz.bus.fabric.runtime.dispatch.Dispatcher;

/**
 * HTTP/2 connection with stream registry and frame IO.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
public final class Http2Connection implements AutoCloseable {

    /**
     * Frame header size.
     */
    private static final int FRAME_HEADER = Normal._9;

    /**
     * Default flow-control window size.
     */
    private static final long DEFAULT_WINDOW = Normal._65535;

    /**
     * Window update threshold.
     */
    private static final long WINDOW_UPDATE_THRESHOLD = DEFAULT_WINDOW / Normal._2;

    /**
     * Default maximum frame payload size.
     */
    private static final int MAX_FRAME_SIZE = Normal._16384;

    /**
     * Maximum accumulated HPACK header block size.
     */
    private static final int MAX_HEADER_BLOCK_SIZE = Normal._64 * Normal._1024;

    /**
     * SETTINGS ACK flag.
     */
    private static final int SETTINGS_ACK = Http2Frame.ACK;

    /**
     * HTTP/2 CANCEL error code.
     */
    private static final int CANCEL = Normal._8;

    /**
     * Maximum HTTP/2 stream identifier.
     */
    private static final int MAX_STREAM_ID = Integer.MAX_VALUE;

    /**
     * Bound network connection.
     */
    private final Connection connection;

    /**
     * Header block writer codec.
     */
    private final HpackCodec hpackWriter;

    /**
     * Header block reader codec.
     */
    private final HpackCodec hpackReader;

    /**
     * Streams.
     */
    private final ConcurrentHashMap<Integer, Http2Stream> streams;

    /**
     * Per-stream inbound frame queues populated by the connection reader task.
     */
    private final ConcurrentHashMap<Integer, BlockingQueue<StreamFrame>> streamFrames;

    /**
     * Stream flow-control windows.
     */
    private final ConcurrentHashMap<Integer, AtomicLong> streamWindows;

    /**
     * Unacknowledged inbound bytes per stream.
     */
    private final ConcurrentHashMap<Integer, AtomicLong> streamUnacknowledgedBytes;

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
    private final AtomicLong connectionWindow;

    /**
     * Connection inbound flow-control window.
     */
    private final AtomicLong receiveWindow;

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
        this.connection = require(connection, "Network connection");
        this.hpackWriter = new HpackCodec();
        this.hpackReader = new HpackCodec();
        this.streams = new ConcurrentHashMap<>();
        this.streamFrames = new ConcurrentHashMap<>();
        this.streamWindows = new ConcurrentHashMap<>();
        this.streamUnacknowledgedBytes = new ConcurrentHashMap<>();
        this.priorities = new ConcurrentHashMap<>();
        this.pushedStreams = ConcurrentHashMap.newKeySet();
        this.connectionWindow = new AtomicLong(DEFAULT_WINDOW);
        this.receiveWindow = new AtomicLong(DEFAULT_WINDOW);
        this.unacknowledgedConnectionBytes = new AtomicLong();
        this.nextLocal = new AtomicInteger(Normal._1);
        this.nextRemote = new AtomicInteger(Normal._2);
        this.state = new AtomicReference<>(Status.OPENED);
        this.reader = new Http2Reader(this);
        this.readerStarted = new AtomicBoolean();
        this.dispatcherClosed = new AtomicBoolean();
        this.connectionFailure = new AtomicReference<>();
        this.peerSettings = new AtomicReference<>(Http2Settings.defaults());
        this.maxFrameSize = new AtomicInteger(MAX_FRAME_SIZE);
        this.initialWindowSize = new AtomicInteger((int) DEFAULT_WINDOW);
        this.maxConcurrentStreams = new AtomicInteger(MAX_STREAM_ID);
        this.maxHeaderListSize = new AtomicInteger(HpackCodec.DEFAULT_MAX_HEADER_LIST_SIZE);
        this.shutdown = new AtomicBoolean();
        this.goAwayLastStreamId = new AtomicInteger(MAX_STREAM_ID);
        this.lastPingPayload = new AtomicLong();
        this.lastAlternateService = new AtomicReference<>();
        this.pushObserver = pushObserver == null ? PushObserver.canceling() : pushObserver;
        this.dispatcher = require(dispatcher, "Dispatcher");
        this.ownsDispatcher = ownsDispatcher;
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
     * Creates a stream.
     *
     * @param headers headers
     * @param out     local direction
     * @return stream
     */
    public Http2Stream newStream(final Headers headers, final boolean out) {
        require(headers, "HTTP/2 stream headers");
        if (state.get().terminal()) {
            throw new StatefulException("HTTP/2 connection is closed");
        }
        if (shutdown.get()) {
            throw new StatefulException("HTTP/2 connection is shutting down");
        }
        if (streams.size() >= maxConcurrentStreams.get()) {
            throw new StatefulException("HTTP/2 max concurrent streams exceeded");
        }
        final int id = out ? nextLocal.getAndAdd(Normal._2) : nextRemote.getAndAdd(Normal._2);
        if (id <= Normal._0 || id > MAX_STREAM_ID) {
            throw new ProtocolException("HTTP/2 stream id overflow");
        }
        final Http2Stream stream = new Http2Stream(id, headers, length -> acknowledgeInbound(id, length));
        final Http2Priority priority = priorities.get(id);
        if (priority != null) {
            stream.priority(priority);
        }
        streams.put(id, stream);
        streamFrames.put(id, new LinkedBlockingQueue<>());
        streamWindows.put(id, new AtomicLong(initialWindowSize.get()));
        streamUnacknowledgedBytes.put(id, new AtomicLong());
        return stream;
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
        final Http2Stream stream = streams.remove(id);
        if (stream != null) {
            streamFrames.remove(id);
            streamWindows.remove(id);
            streamUnacknowledgedBytes.remove(id);
            priorities.remove(id);
            pushedStreams.remove(id);
            try {
                stream.close();
            } catch (final RuntimeException e) {
                throw e instanceof InternalException internal ? internal
                        : new InternalException("Unable to remove HTTP/2 stream", e);
            }
        }
    }

    /**
     * Writes a frame.
     *
     * @param frame frame
     */
    public synchronized void writeFrame(final Http2Frame frame) {
        require(frame, "HTTP/2 frame");
        final Buffer encoded = payload(frame);
        final int frameMax = maxFrameSize.get();
        validateFrame(
                frame.type(),
                frame.streamId(),
                frame.flags(),
                frame.type() == Http2Frame.DATA ? (int) Math.min(encoded.size(), frameMax) : toIntSize(encoded.size()),
                frameMax);
        if (frame.type() == Http2Frame.DATA && encoded.size() > frameMax) {
            writeDataFrames(frame, frameMax);
            return;
        }
        if (frame.type() == Http2Frame.DATA) {
            consumeWindow(frame.streamId(), toIntSize(encoded.size()));
        }
        writeSingleFrame(frame.type(), frame.streamId(), frame.flags(), encoded);
    }

    /**
     * Writes fragmented DATA frames.
     *
     * @param frame        frame
     * @param maxFrameSize max frame size
     */
    private void writeDataFrames(final Http2Frame frame, final int maxFrameSize) {
        final Buffer source = new Buffer().write(frame.payloadBytes());
        final int length = toIntSize(source.size());
        consumeWindow(frame.streamId(), length);
        while (source.size() > Normal._0) {
            final long count = Math.min(source.size(), maxFrameSize);
            final Buffer fragment = new Buffer();
            fragment.write(source, count);
            final boolean last = source.size() == Normal._0;
            final int flags = last ? frame.flags() : frame.flags() & ~Http2Frame.END_STREAM;
            writeSingleFrame(Http2Frame.DATA, frame.streamId(), flags, fragment);
        }
    }

    /**
     * Writes one encoded frame.
     *
     * @param type     type
     * @param streamId stream id
     * @param flags    flags
     * @param payload  payload
     */
    private void writeSingleFrame(final int type, final int streamId, final int flags, final Buffer payload) {
        final int length = toIntSize(payload.size());
        final Buffer header = new Buffer();
        header.writeByte((length >>> Normal._16) & 0xff);
        header.writeByte((length >>> Normal._8) & 0xff);
        header.writeByte(length & 0xff);
        header.writeByte(type);
        header.writeByte(flags);
        header.writeInt(streamId & MAX_STREAM_ID);
        write(header);
        write(payload);
    }

    /**
     * Reads a frame.
     *
     * @return frame
     */
    Http2Frame readFrame() {
        final Buffer header = readFully(FRAME_HEADER);
        final int length = readMedium(header);
        final int type = header.readByte() & 0xff;
        final int flags = header.readByte() & 0xff;
        final int streamId = header.readInt() & MAX_STREAM_ID;
        validateFrame(type, streamId, flags, length);
        ByteString payload = readFully(length).readByteString();
        Http2Priority priority = null;
        Http2AlternateService alternateService = null;
        final List<Http2Header> headers = switch (type) {
            case Http2Frame.HEADERS -> {
                priority = decodeHeaderPriority(streamId, flags, payload);
                payload = headerBlock(streamId, flags, headerFragment(flags, payload));
                yield hpackReader.decode(new Buffer().write(payload));
            }
            case Http2Frame.PUSH_PROMISE -> {
                payload = headerBlock(streamId, flags, payload);
                yield hpackReader.decode(new Buffer().write(pushHeaderBlock(payload)));
            }
            case Http2Frame.PRIORITY -> {
                priority = Http2Priority.decode(payload, streamId);
                yield List.of();
            }
            case Http2Frame.ALTSVC -> {
                alternateService = Http2AlternateService.decode(payload, streamId);
                yield List.of();
            }
            default -> List.of();
        };
        final int decodedFlags = type == Http2Frame.HEADERS || type == Http2Frame.PUSH_PROMISE
                ? flags | Http2Frame.END_HEADERS
                : flags;
        return Http2Frame.decoded(type, streamId, decodedFlags, payload, headers, priority, alternateService);
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
        dispatcher.run("http2:reader:" + System.identityHashCode(this), this::readLoop);
    }

    /**
     * Waits for the next frame that belongs to a stream.
     *
     * @param streamId stream id
     * @param timeout  wait timeout; zero waits without an explicit deadline
     * @return next stream frame
     */
    public Http2Frame nextFrame(final int streamId, final Duration timeout) {
        positiveStream(streamId);
        final Duration current = require(timeout, "HTTP/2 stream read timeout");
        if (current.isNegative()) {
            throw new ValidateException("HTTP/2 stream read timeout must not be negative");
        }
        final BlockingQueue<StreamFrame> queue = streamFrames.get(streamId);
        if (queue == null) {
            throw streamFailure("HTTP/2 stream frame queue is missing");
        }
        final StreamFrame event = takeFrame(streamId, queue, current);
        if (event.failure != null) {
            throw event.failure;
        }
        return event.frame;
    }

    /**
     * Discards queued frames for a completed stream response.
     *
     * @param streamId stream id
     */
    public void discardFrames(final int streamId) {
        positiveStream(streamId);
        streamFrames.remove(streamId);
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
        goAwayLastStreamId.set(lastStreamId);
        failStreamsAbove(lastStreamId, new SocketException("HTTP/2 connection sent GOAWAY"));
        writeFrame(Http2Frame.goAway(lastStreamId, errorCode, debugData));
    }

    /**
     * Sends a GOAWAY frame and rejects new streams locally through a JDK byte buffer compatibility boundary.
     *
     * @param lastStreamId last processed stream id
     * @param errorCode    error code
     * @param debugData    optional debug data
     * @deprecated use {@link #goAway(int, int, ByteString)}
     */
    @Deprecated(since = "8.8.3")
    public void goAway(final int lastStreamId, final int errorCode, final ByteBuffer debugData) {
        goAway(
                lastStreamId,
                errorCode,
                debugData == null ? ByteString.EMPTY : ByteString.of(debugData.asReadOnlyBuffer()));
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
                length -> acknowledgeInbound(streamId, length));
        final Http2Priority priority = priorities.get(streamId);
        if (priority != null) {
            stream.priority(priority);
        }
        streams.put(streamId, stream);
        streamFrames.put(streamId, new LinkedBlockingQueue<>());
        streamWindows.put(streamId, new AtomicLong(initialWindowSize.get()));
        streamUnacknowledgedBytes.put(streamId, new AtomicLong());
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
                resetPushedStream(streamId, CANCEL);
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
                resetPushedStream(streamId, CANCEL);
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
     * @param data      data
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
            final boolean cancel = pushObserver.onData(streamId, snapshot, endStream);
            if (cancel) {
                resetPushedStream(streamId, CANCEL);
            } else if (endStream) {
                remove(streamId);
            }
            return cancel;
        });
    }

    /**
     * Dispatches pushed data through a JDK byte buffer compatibility boundary.
     *
     * @param streamId  pushed stream id
     * @param data      data
     * @param endStream true when ended
     * @return future whose value is true when canceled
     * @deprecated use {@link #pushDataLater(int, ByteString, boolean)}
     */
    @Deprecated(since = "8.8.3")
    public CompletableFuture<Boolean> pushDataLater(
            final int streamId,
            final ByteBuffer data,
            final boolean endStream) {
        final ByteBuffer checkedData = Assert
                .notNull(data, () -> new ValidateException("HTTP/2 push data must not be null"));
        return pushDataLater(streamId, ByteString.of(checkedData.asReadOnlyBuffer()), endStream);
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
            streamWindows.remove(streamId);
            streamUnacknowledgedBytes.remove(streamId);
            priorities.remove(streamId);
        });
    }

    /**
     * Updates a flow window.
     *
     * @param streamId stream id
     * @param delta    delta
     */
    public void updateWindow(final int streamId, final long delta) {
        if (streamId < Normal._0 || delta <= Normal._0 || delta > Integer.MAX_VALUE) {
            throw new ProtocolException("Invalid HTTP/2 window update");
        }
        if (streamId == Normal._0) {
            addWindow(connectionWindow, delta);
            return;
        }
        final AtomicLong window = streamWindows.get(streamId);
        if (window == null) {
            throw new ProtocolException("HTTP/2 stream is missing");
        }
        addWindow(window, delta);
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
    public synchronized void close() {
        final Status current = state.get();
        if (current == Status.CLOSED) {
            closeOwnedDispatcher();
            return;
        }
        if (!current.canTransit(Status.CLOSING)) {
            throw new StatefulException("HTTP/2 connection cannot close from state " + current);
        }
        state.set(Status.CLOSING);
        final RuntimeException closed = new SocketException("HTTP/2 connection closed");
        connectionFailure.compareAndSet(null, closed);
        signalStreams(closed);
        RuntimeException failure = null;
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
        streamWindows.clear();
        streamUnacknowledgedBytes.clear();
        priorities.clear();
        pushedStreams.clear();
        try {
            connection.close();
        } catch (final RuntimeException e) {
            if (failure == null) {
                failure = e;
            }
        }
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
                dispatch(reader.nextFrame());
            }
        } catch (final RuntimeException e) {
            if (!state.get().terminal()) {
                closeAfterReaderFailure(e);
            }
        }
    }

    /**
     * Dispatches a decoded frame to connection state, push callbacks, or a stream queue.
     *
     * @param frame frame
     */
    private void dispatch(final Http2Frame frame) {
        switch (frame.type()) {
            case Http2Frame.SETTINGS -> dispatchSettings(frame);
            case Http2Frame.PING -> dispatchPing(frame);
            case Http2Frame.GOAWAY -> dispatchGoAway(frame);
            case Http2Frame.WINDOW_UPDATE -> {
                updateWindow(frame.streamId(), frame.windowDelta());
                return;
            }
            case Http2Frame.PRIORITY -> {
                applyPriority(frame.streamId(), frame.priority());
                return;
            }
            case Http2Frame.ALTSVC -> {
                dispatchAlternateService(frame);
                return;
            }
            case Http2Frame.PUSH_PROMISE -> {
                pushRequestLater(frame.promisedStreamId(), frame.headers());
                return;
            }
            case Http2Frame.HEADERS -> dispatchHeaders(frame);
            case Http2Frame.DATA -> dispatchData(frame);
            case Http2Frame.RST_STREAM -> resetStream(frame.streamId(), frame.errorCode());
            default -> throw new ProtocolException("Unsupported HTTP/2 frame type");
        }
    }

    /**
     * Applies SETTINGS and acknowledges non-ACK settings frames.
     *
     * @param frame frame
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
     * @param settings settings
     */
    private synchronized void applySettings(final Http2Settings settings) {
        final Http2Settings merged = peerSettings.get().copy();
        final int previousWindow = merged.initialWindowSize();
        merged.merge(settings);
        peerSettings.set(merged);
        maxFrameSize.set(merged.maxFrameSize());
        maxConcurrentStreams.set(merged.maxConcurrentStreams());
        initialWindowSize.set(merged.initialWindowSize());
        hpackWriter.maxTableSize(merged.headerTableSize());
        if (settings.isSet(Http2Settings.MAX_HEADER_LIST_SIZE)) {
            final int headerListSize = merged.maxHeaderListSize();
            hpackWriter.maxHeaderListSize(headerListSize);
            maxHeaderListSize.set(headerListSize);
        }
        final long delta = (long) merged.initialWindowSize() - previousWindow;
        if (delta != Normal._0) {
            for (final AtomicLong window : streamWindows.values()) {
                adjustWindow(window, delta);
            }
        }
    }

    /**
     * Handles PING frames.
     *
     * @param frame frame
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
     * @param frame frame
     */
    private void dispatchGoAway(final Http2Frame frame) {
        shutdown.set(true);
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
            if (streamId > lastStreamId) {
                enqueue(streamId, StreamFrame.failure(failure));
                streams.remove(streamId);
                streamWindows.remove(streamId);
                streamUnacknowledgedBytes.remove(streamId);
                try {
                    entry.getValue().close();
                } catch (final RuntimeException ignored) {
                    // The GOAWAY failure is already queued for the stream.
                }
            }
        }
    }

    /**
     * Dispatches a HEADERS frame.
     *
     * @param frame frame
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
        stream.receiveHeaders(toHeaders(frame.headers()));
        enqueue(frame.streamId(), StreamFrame.of(frame));
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
     * @param frame frame
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
     * @param frame frame
     */
    private void dispatchData(final Http2Frame frame) {
        if (pushedStreams.contains(frame.streamId())) {
            pushDataLater(frame.streamId(), frame.payloadBytes(), frame.endStream());
            return;
        }
        final Http2Stream stream = streams.get(frame.streamId());
        if (stream == null) {
            throw new ProtocolException("HTTP/2 frame references a missing stream");
        }
        final int length = frame.payloadBytes().size();
        consumeReceiveWindow(length);
        stream.receiveData(frame.payloadBytes());
        enqueue(frame.streamId(), StreamFrame.of(frame));
    }

    /**
     * Consumes the connection-level inbound window.
     *
     * @param length byte count
     */
    private void consumeReceiveWindow(final int length) {
        if (length == Normal._0) {
            return;
        }
        if (!subtractWindow(receiveWindow, length)) {
            throw new ProtocolException("HTTP/2 connection flow-control window is exhausted");
        }
    }

    /**
     * Sends WINDOW_UPDATE frames when inbound data crosses the update threshold.
     *
     * @param streamId stream id
     * @param length   consumed length
     */
    private void acknowledgeInbound(final int streamId, final long length) {
        if (length == Normal._0) {
            return;
        }
        final long connectionDelta = accumulate(unacknowledgedConnectionBytes, length);
        if (connectionDelta >= WINDOW_UPDATE_THRESHOLD) {
            writeFrame(Http2Frame.windowUpdate(Normal._0, connectionDelta));
            addWindow(receiveWindow, connectionDelta);
        }
        final AtomicLong streamBytes = streamUnacknowledgedBytes.computeIfAbsent(streamId, id -> new AtomicLong());
        final long streamDelta = accumulate(streamBytes, length);
        if (streamDelta >= WINDOW_UPDATE_THRESHOLD) {
            writeFrame(Http2Frame.windowUpdate(streamId, streamDelta));
            final Http2Stream stream = streams.get(streamId);
            if (stream != null) {
                stream.updateReceiveWindow(streamDelta);
            }
        }
    }

    /**
     * Adds bytes to a counter and resets it when threshold is crossed.
     *
     * @param counter counter
     * @param length  length
     * @return accumulated length after the addition
     */
    private static long accumulate(final AtomicLong counter, final long length) {
        long current;
        long next;
        do {
            current = counter.get();
            next = current + length;
            if (next < current || next > Integer.MAX_VALUE) {
                throw new ProtocolException("HTTP/2 flow-control update overflow");
            }
        } while (!counter.compareAndSet(current, next >= WINDOW_UPDATE_THRESHOLD ? Normal._0 : next));
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
        final RuntimeException failure = new SocketException("HTTP/2 stream was reset");
        enqueue(streamId, StreamFrame.failure(failure));
        final Http2Stream stream = streams.remove(streamId);
        streamWindows.remove(streamId);
        streamUnacknowledgedBytes.remove(streamId);
        if (stream != null) {
            try {
                stream.close();
            } catch (final RuntimeException ignored) {
                // The reset failure above is the application-visible terminal signal.
            }
        }
    }

    /**
     * Enqueues a stream frame event when the stream queue still exists.
     *
     * @param streamId stream id
     * @param event    event
     */
    private void enqueue(final int streamId, final StreamFrame event) {
        final BlockingQueue<StreamFrame> queue = streamFrames.get(streamId);
        if (queue != null) {
            queue.offer(event);
        }
    }

    /**
     * Waits for a queued stream frame event.
     *
     * @param streamId stream id
     * @param queue    queue
     * @param timeout  timeout
     * @return event
     */
    private StreamFrame takeFrame(final int streamId, final BlockingQueue<StreamFrame> queue, final Duration timeout) {
        try {
            final StreamFrame event;
            if (timeout.isZero()) {
                event = queue.take();
            } else {
                final long millis = Math.max(Normal._1, timeout.toMillis());
                event = queue.poll(millis, TimeUnit.MILLISECONDS);
            }
            if (event == null) {
                throw new TimeoutException("Timed out waiting for HTTP/2 stream " + streamId);
            }
            return event;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for HTTP/2 stream " + streamId, e);
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
        signalStreams(failure);
        for (final Map.Entry<Integer, Http2Stream> entry : streams.entrySet()) {
            try {
                entry.getValue().close();
            } catch (final RuntimeException ignored) {
                // The reader failure is already delivered to every active stream queue.
            }
        }
        streams.clear();
        streamWindows.clear();
        streamUnacknowledgedBytes.clear();
        pushedStreams.clear();
        try {
            connection.close();
        } finally {
            state.set(Status.CLOSED);
            if (ownsDispatcher) {
                CompletableFuture.runAsync(this::closeOwnedDispatcher);
            }
        }
    }

    /**
     * Signals all active stream queues with a terminal failure.
     *
     * @param failure failure
     */
    private void signalStreams(final RuntimeException failure) {
        for (final BlockingQueue<StreamFrame> queue : streamFrames.values()) {
            queue.offer(StreamFrame.failure(failure));
        }
        streamFrames.clear();
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
     * @param frame frame
     * @return payload
     */
    private Buffer payload(final Http2Frame frame) {
        if (frame.type() == Http2Frame.HEADERS) {
            final Buffer headers = hpackWriter.encodeBuffer(frame.headers());
            if (frame.priority() == null) {
                return headers;
            }
            final Buffer payload = new Buffer().write(frame.priority().encodeBytes());
            payload.write(headers, headers.size());
            return payload;
        }
        if (frame.type() == Http2Frame.PUSH_PROMISE) {
            final Buffer headers = hpackWriter.encodeBuffer(frame.headers());
            final Buffer payload = new Buffer();
            payload.writeInt(frame.promisedStreamId());
            payload.write(headers, headers.size());
            return payload;
        }
        if (frame.type() == Http2Frame.PRIORITY) {
            return new Buffer().write(frame.priority().encodeBytes());
        }
        if (frame.type() == Http2Frame.ALTSVC) {
            return new Buffer().write(frame.alternateService().encodeBytes());
        }
        return new Buffer().write(frame.payloadBytes());
    }

    /**
     * Validates supported frame metadata.
     *
     * @param type     type
     * @param streamId stream id
     * @param flags    flags
     * @param length   payload length
     */
    private static void validateFrame(final int type, final int streamId, final int flags, final int length) {
        validateFrame(type, streamId, flags, length, MAX_FRAME_SIZE);
    }

    /**
     * Validates supported frame metadata.
     *
     * @param type         type
     * @param streamId     stream id
     * @param flags        flags
     * @param length       payload length
     * @param maxFrameSize max payload length
     */
    private static void validateFrame(
            final int type,
            final int streamId,
            final int flags,
            final int length,
            final int maxFrameSize) {
        if (streamId < Normal._0 || streamId > MAX_STREAM_ID || flags < Normal._0 || flags > 0xff || length < Normal._0
                || length > maxFrameSize) {
            throw new ProtocolException("Invalid HTTP/2 frame metadata");
        }
        if (type == Http2Frame.SETTINGS) {
            if (streamId != Normal._0 || (flags & ~SETTINGS_ACK) != Normal._0
                    || ((flags & SETTINGS_ACK) != Normal._0 && length != Normal._0)
                    || length % Normal._6 != Normal._0) {
                throw new ProtocolException("Invalid HTTP/2 SETTINGS frame");
            }
            return;
        }
        if (type == Http2Frame.PING) {
            if (streamId != Normal._0 || (flags & ~Http2Frame.ACK) != Normal._0 || length != Normal._8) {
                throw new ProtocolException("Invalid HTTP/2 PING frame");
            }
            return;
        }
        if (type == Http2Frame.GOAWAY) {
            if (streamId != Normal._0 || flags != Normal._0 || length < Normal._4 * Normal._2) {
                throw new ProtocolException("Invalid HTTP/2 GOAWAY frame");
            }
            return;
        }
        if (type == Http2Frame.WINDOW_UPDATE) {
            if (flags != Normal._0 || length != Normal._4) {
                throw new ProtocolException("Invalid HTTP/2 WINDOW_UPDATE frame");
            }
            return;
        }
        if (type == Http2Frame.ALTSVC) {
            if (flags != Normal._0 || length < Normal._2) {
                throw new ProtocolException("Invalid HTTP/2 ALTSVC frame");
            }
            return;
        }
        if (streamId <= Normal._0) {
            throw new ProtocolException("Invalid HTTP/2 stream frame id");
        }
        switch (type) {
            case Http2Frame.DATA -> validateFlags(flags, Http2Frame.END_STREAM);
            case Http2Frame.HEADERS -> {
                validateFlags(flags, Http2Frame.END_STREAM | Http2Frame.END_HEADERS | Http2Frame.PRIORITY_FLAG);
                if ((flags & Http2Frame.PRIORITY_FLAG) != Normal._0 && length < Http2Priority.LENGTH) {
                    throw new ProtocolException("Invalid HTTP/2 HEADERS priority payload");
                }
            }
            case Http2Frame.PRIORITY -> {
                validateFlags(flags, Normal._0);
                if (length != Http2Priority.LENGTH) {
                    throw new ProtocolException("Invalid HTTP/2 PRIORITY length");
                }
            }
            case Http2Frame.PUSH_PROMISE -> {
                validateFlags(flags, Http2Frame.END_HEADERS);
                if (length < Normal._4) {
                    throw new ProtocolException("Invalid HTTP/2 PUSH_PROMISE frame");
                }
            }
            case Http2Frame.RST_STREAM -> {
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
     * @param flags    flags
     * @param payload  original HEADERS payload
     * @return priority or null
     */
    private static Http2Priority decodeHeaderPriority(final int streamId, final int flags, final ByteString payload) {
        if ((flags & Http2Frame.PRIORITY_FLAG) == Normal._0) {
            return null;
        }
        return Http2Priority.decode(payload, streamId);
    }

    /**
     * Returns the HPACK fragment from a HEADERS payload.
     *
     * @param flags   flags
     * @param payload original HEADERS payload
     * @return header fragment
     */
    private static ByteString headerFragment(final int flags, final ByteString payload) {
        if ((flags & Http2Frame.PRIORITY_FLAG) != Normal._0) {
            return payload.substring(Http2Priority.LENGTH);
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
        if ((flags & Http2Frame.END_HEADERS) != Normal._0) {
            if (first.size() > MAX_HEADER_BLOCK_SIZE) {
                throw new ProtocolException("HTTP/2 header block exceeds max size");
            }
            return first;
        }
        final Buffer fragments = new Buffer();
        int total = appendHeaderFragment(fragments, first, Normal._0);
        int currentFlags = flags;
        while ((currentFlags & Http2Frame.END_HEADERS) == Normal._0) {
            final Buffer header = readFully(FRAME_HEADER);
            final int length = readMedium(header);
            final int type = header.readByte() & 0xff;
            currentFlags = header.readByte() & 0xff;
            final int continuationStreamId = header.readInt() & MAX_STREAM_ID;
            validateContinuation(streamId, type, continuationStreamId, currentFlags, length);
            total = appendHeaderFragment(fragments, readFully(length).readByteString(), total);
        }
        return fragments.readByteString();
    }

    /**
     * Adds a header block fragment and checks the accumulated size.
     *
     * @param fragments fragments
     * @param fragment  new fragment
     * @param total     current total
     * @return updated total
     */
    private static int appendHeaderFragment(final Buffer fragments, final ByteString fragment, final int total) {
        final int next = total + fragment.size();
        if (next < total || next > MAX_HEADER_BLOCK_SIZE) {
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
        if (type != Http2Frame.CONTINUATION || streamId != expectedStreamId || streamId <= Normal._0
                || length < Normal._0 || length > MAX_FRAME_SIZE) {
            throw new ProtocolException("Invalid HTTP/2 CONTINUATION frame");
        }
        validateFlags(flags, Http2Frame.END_HEADERS);
    }

    /**
     * Validates frame flags.
     *
     * @param flags   flags
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
        if (length == Normal._0) {
            return;
        }
        if (!subtractWindow(connectionWindow, length)) {
            throw new StatefulException("HTTP/2 connection window is exhausted");
        }
        final AtomicLong streamWindow = streamWindows.get(streamId);
        if (streamWindow != null && !subtractWindow(streamWindow, length)) {
            connectionWindow.addAndGet(length);
            throw new StatefulException("HTTP/2 stream window is exhausted");
        }
    }

    /**
     * Subtracts from a flow-control window.
     *
     * @param window window
     * @param length length
     * @return true when the window was available
     */
    private static boolean subtractWindow(final AtomicLong window, final long length) {
        long current;
        do {
            current = window.get();
            if (current < length) {
                return false;
            }
        } while (!window.compareAndSet(current, current - length));
        return true;
    }

    /**
     * Adds to a flow-control window.
     *
     * @param window window
     * @param delta  delta
     */
    private static void addWindow(final AtomicLong window, final long delta) {
        long current;
        long next;
        do {
            current = window.get();
            next = current + delta;
            if (next > MAX_STREAM_ID || next < current) {
                throw new ProtocolException("HTTP/2 flow-control window overflow");
            }
        } while (!window.compareAndSet(current, next));
    }

    /**
     * Adjusts a flow-control window by a signed delta.
     *
     * @param window window
     * @param delta  signed delta
     */
    private static void adjustWindow(final AtomicLong window, final long delta) {
        long current;
        long next;
        do {
            current = window.get();
            next = current + delta;
            if (next < Normal._0 || next > MAX_STREAM_ID || (delta > Normal._0 && next < current)) {
                throw new ProtocolException("HTTP/2 flow-control window overflow");
            }
        } while (!window.compareAndSet(current, next));
    }

    /**
     * Writes a reset for a pushed stream and removes local tracking.
     *
     * @param streamId  stream id
     * @param errorCode error code
     */
    private void resetPushedStream(final int streamId, final int errorCode) {
        pushedStreams.remove(streamId);
        streams.remove(streamId);
        streamWindows.remove(streamId);
        streamUnacknowledgedBytes.remove(streamId);
        priorities.remove(streamId);
        writeFrame(Http2Frame.rstStream(streamId, errorCode));
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
            builder.add(header.name(), header.value());
        }
        return builder.build();
    }

    /**
     * Validates and snapshots headers.
     *
     * @param headers headers
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
     * @param payload payload
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
     * @param failure failure
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
     * @param cause cause
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
     * @param source source
     */
    private void write(final Buffer source) {
        while (source.size() > Normal._0) {
            final ByteBuffer view = source.nioBuffer(toIntSize(Math.min(source.size(), Integer.MAX_VALUE)));
            final int written = await(connection.write(view));
            if (written < Normal._0) {
                throw new SocketException("HTTP/2 write reached EOF");
            }
            if (written == Normal._0) {
                Thread.yield();
            } else {
                try {
                    source.skip(written);
                } catch (final java.io.IOException e) {
                    throw new InternalException("Unable to consume written HTTP/2 bytes", e);
                }
            }
        }
    }

    /**
     * Converts a buffer size to int.
     *
     * @param size size
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
     * @param length length
     * @return buffer
     */
    private Buffer readFully(final int length) {
        final Buffer buffer = new Buffer();
        while (buffer.size() < length) {
            final int remaining = (int) Math.min(length - buffer.size(), MAX_FRAME_SIZE);
            final ByteBuffer chunk = ByteBuffer.allocate(remaining);
            final int position = chunk.position();
            final int read = await(connection.read(chunk));
            if (read < Normal._0) {
                throw new SocketException("HTTP/2 frame reached EOF");
            }
            if (read == Normal._0) {
                Thread.yield();
            } else {
                chunk.position(position + read);
                chunk.flip();
                try {
                    buffer.write(chunk);
                } catch (final java.io.IOException e) {
                    throw new InternalException("Unable to buffer HTTP/2 frame bytes", e);
                }
            }
        }
        return buffer;
    }

    /**
     * Reads a 24-bit unsigned integer.
     *
     * @param buffer buffer
     * @return value
     */
    private static int readMedium(final Buffer buffer) {
        return ((buffer.readByte() & 0xff) << Normal._16) | ((buffer.readByte() & 0xff) << Normal._8)
                | (buffer.readByte() & 0xff);
    }

    /**
     * Waits for IO.
     *
     * @param future future
     * @return result
     */
    private static int await(final CompletableFuture<Integer> future) {
        try {
            return future.get(Normal._5, TimeUnit.SECONDS);
        } catch (final java.util.concurrent.TimeoutException e) {
            throw new TimeoutException("HTTP/2 IO timed out", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InternalException("Interrupted while waiting for HTTP/2 IO", e);
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new SocketException("HTTP/2 IO failed", cause);
        }
    }

    /**
     * Validates required value.
     *
     * @param value value
     * @param name  name
     * @param <T>   type
     * @return value
     */
    private static <T> T require(final T value, final String name) {
        return Assert.notNull(value, () -> new ValidateException(name + " must not be null"));
    }

    /**
     * Stream queue event carrying either a frame or a terminal failure.
     *
     * @param frame   frame
     * @param failure terminal failure
     */
    private record StreamFrame(Http2Frame frame, RuntimeException failure) {

        /**
         * Creates a frame event.
         *
         * @param frame frame
         * @return event
         */
        static StreamFrame of(final Http2Frame frame) {
            return new StreamFrame(require(frame, "HTTP/2 frame"), null);
        }

        /**
         * Creates a failure event.
         *
         * @param failure failure
         * @return event
         */
        static StreamFrame failure(final RuntimeException failure) {
            return new StreamFrame(null, require(failure, "HTTP/2 stream failure"));
        }

    }

}
