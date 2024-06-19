/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.http.metric.http;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.buffer.Buffer;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.Headers;
import org.miaixz.bus.http.metric.NamedRunnable;
import org.miaixz.bus.logger.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.*;

/**
 * 到远程对等点的套接字连接。连接主机可以发送和接收数据流.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Http2Connection implements Closeable {

    // Internal state of this connection is guarded by 'this'. No blocking
    // operations may be performed while holding this lock!
    //
    // Socket writes are guarded by frameWriter.
    //
    // Socket reads are unguarded but are only made by the reader thread.
    //
    // Certain operations (like SYN_STREAM) need to synchronize on both the
    // frameWriter (to do blocking I/O) and this (to create streams). Such
    // operations must synchronize on 'this' last. This ensures that we never
    // wait for a blocking operation while holding 'this'.

    static final int CLIENT_WINDOW_SIZE = Normal._16 * Normal._1024 * Normal._1024;

    static final int INTERVAL_PING = 1;
    static final int DEGRADED_PING = 2;
    static final int AWAIT_PING = 3;
    static final long DEGRADED_PONG_TIMEOUT_NS = 1_000_000_000L; // 1 second.

    /**
     * 共享执行程序来发送传入流的通知。这个执行器需要多个线程，因为侦听器不需要立即返回.
     */
    private static final ExecutorService listenerExecutor = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(),
            org.miaixz.bus.http.Builder.threadFactory("Http Http2Connection", true));

    /**
     * 如果该对等点发起连接，则为True.
     */
    final boolean client;

    /**
     * 响应传入流或设置而运行的用户代码。对它的调用总是在{@link #listenerExecutor}上调用
     */
    final Listener listener;
    final Map<Integer, Http2Stream> streams = new LinkedHashMap<>();
    final String connectionName;
    /**
     * 为响应推送承诺事件而运行的用户代码
     */
    final PushObserver pushObserver;
    /**
     * 我们从对等点接收设置.
     */
    final Http2Settings peerSettings = new Http2Settings();
    final Socket socket;
    final Http2Writer writer;
    final ReaderRunnable readerRunnable;

    final Set<Integer> currentPushRequests = new LinkedHashSet<>();
    /**
     * 异步地将帧写入传出套接字
     */
    private final ScheduledExecutorService writerExecutor;
    /**
     * 确保推送承诺回调事件按顺序发送到每个流
     */
    private final ExecutorService pushExecutor;
    int lastGoodStreamId;
    int nextStreamId;
    /**
     * 应用程序消耗的总字节数，但尚未通过在此连接上发送{@code WINDOW_UPDATE}帧来确认.
     */
    long unacknowledgedBytesRead = 0;
    /**
     * 在接收窗口更新之前，可以在连接上写入的字节数.
     */
    long bytesLeftInWriteWindow;
    /**
     * 设置我们与对等点通信
     */
    Http2Settings settings = new Http2Settings();
    private boolean shutdown;
    // Total number of pings send and received of the corresponding types. All guarded by this.
    private long intervalPingsSent = 0L;
    private long intervalPongsReceived = 0L;
    private long degradedPingsSent = 0L;
    private long degradedPongsReceived = 0L;
    private long awaitPingsSent = 0L;
    private long awaitPongsReceived = 0L;
    /**
     * Consider this connection to be unhealthy if a degraded pong isn't received by this time.
     */
    private long degradedPongDeadlineNs = 0L;

    Http2Connection(Builder builder) {
        pushObserver = builder.pushObserver;
        client = builder.client;
        listener = builder.listener;
        // http://tools.ietf.org/html/draft-ietf-httpbis-http2-17#section-5.1.1
        nextStreamId = builder.client ? 1 : 2;
        if (builder.client) {
            nextStreamId += 2; // In HTTP/2, 1 on client is reserved for Upgrade.
        }

        // Flow control was designed more for servers, or proxies than edge clients.
        // If we are a client, set the flow control window to 16MiB.  This avoids
        // thrashing window updates every 64KiB, yet small enough to avoid blowing
        // up the heap.
        if (builder.client) {
            settings.set(HTTP.INITIAL_WINDOW_SIZE, CLIENT_WINDOW_SIZE);
        }

        connectionName = builder.connectionName;

        writerExecutor = new ScheduledThreadPoolExecutor(1,
                org.miaixz.bus.http.Builder.threadFactory(String.format("Http %s Writer", connectionName), false));
        if (builder.pingIntervalMillis != 0) {
            writerExecutor.scheduleAtFixedRate(new IntervalPingRunnable(),
                    builder.pingIntervalMillis, builder.pingIntervalMillis, TimeUnit.MILLISECONDS);
        }

        // Like newSingleThreadExecutor, except lazy creates the thread.
        pushExecutor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                org.miaixz.bus.http.Builder.threadFactory(String.format("Http %s Push Observer", connectionName), true));
        peerSettings.set(HTTP.INITIAL_WINDOW_SIZE, HTTP.DEFAULT_INITIAL_WINDOW_SIZE);
        peerSettings.set(HTTP.MAX_FRAME_SIZE, Http2.INITIAL_MAX_FRAME_SIZE);
        bytesLeftInWriteWindow = peerSettings.getInitialWindowSize();
        socket = builder.socket;
        writer = new Http2Writer(builder.sink, client);

        readerRunnable = new ReaderRunnable(new Http2Reader(builder.source, client));
    }

    /**
     * Returns the number of {@link Http2Stream#isOpen() open streams} on this connection.
     */
    public synchronized int openStreamCount() {
        return streams.size();
    }

    synchronized Http2Stream getStream(int id) {
        return streams.get(id);
    }

    synchronized Http2Stream removeStream(int streamId) {
        Http2Stream stream = streams.remove(streamId);
        notifyAll();
        return stream;
    }

    public synchronized int maxConcurrentStreams() {
        return peerSettings.getMaxConcurrentStreams(Integer.MAX_VALUE);
    }

    synchronized void updateConnectionFlowControl(long read) {
        unacknowledgedBytesRead += read;
        if (unacknowledgedBytesRead >= settings.getInitialWindowSize() / 2) {
            writeWindowUpdateLater(0, unacknowledgedBytesRead);
            unacknowledgedBytesRead = 0;
        }
    }

    /**
     * 返回一个新的服务器发起的流.
     *
     * @param associatedStreamId 触发发送方创建此流的流.
     * @param requestHeaders     请求头信息
     * @param out                创建一个输出流，我们可以使用它将数据发送到远程对等端。对应{@code FLAG_FIN}
     * @return http请求流
     * @throws IOException 异常
     */
    public Http2Stream pushStream(int associatedStreamId, List<Http2Header> requestHeaders, boolean out)
            throws IOException {
        if (client) throw new IllegalStateException("Client cannot push requests.");
        return newStream(associatedStreamId, requestHeaders, out);
    }

    /**
     * Returns a new locally-initiated stream.
     *
     * @param out true to create an output stream that we can use to send data to the remote peer.
     *            Corresponds to {@code FLAG_FIN}.
     */
    public Http2Stream newStream(List<Http2Header> requestHeaders, boolean out) throws IOException {
        return newStream(0, requestHeaders, out);
    }

    private Http2Stream newStream(
            int associatedStreamId, List<Http2Header> requestHeaders, boolean out) throws IOException {
        boolean outFinished = !out;
        boolean inFinished = false;
        boolean flushHeaders;
        Http2Stream stream;
        int streamId;

        synchronized (writer) {
            synchronized (this) {
                if (nextStreamId > Integer.MAX_VALUE / 2) {
                    shutdown(Http2ErrorCode.REFUSED_STREAM);
                }
                if (shutdown) {
                    throw new IOException();
                }
                streamId = nextStreamId;
                nextStreamId += 2;
                stream = new Http2Stream(streamId, this, outFinished, inFinished, null);
                flushHeaders = !out || bytesLeftInWriteWindow == 0L || stream.bytesLeftInWriteWindow == 0L;
                if (stream.isOpen()) {
                    streams.put(streamId, stream);
                }
            }
            if (associatedStreamId == 0) {
                writer.headers(outFinished, streamId, requestHeaders);
            } else if (client) {
                throw new IllegalArgumentException("client streams shouldn't have associated stream IDs");
            } else {
                writer.pushPromise(associatedStreamId, streamId, requestHeaders);
            }
        }

        if (flushHeaders) {
            writer.flush();
        }

        return stream;
    }

    void writeHeaders(int streamId, boolean outFinished, List<Http2Header> alternating)
            throws IOException {
        writer.headers(outFinished, streamId, alternating);
    }

    /**
     * 此方法的调用程序不是线程安全的，有时在应用程序线程上也是如此。
     * 通常，将调用此方法将数据的缓冲区发送给对等方.
     * 写取决于流和连接的写窗口。在有足够的窗口发送{@code byteCount}之前，
     * 调用者将阻塞。例如，{@code HttpURLConnection}的用户向输出流刷新的
     * 字节比连接的写窗口多，就会阻塞
     *
     * @param streamId    是否streamId
     * @param outFinished 是否结束
     * @param buffer      缓冲
     * @param byteCount   字节流大小
     * @throws IOException 异常
     */
    public void writeData(int streamId, boolean outFinished, Buffer buffer, long byteCount)
            throws IOException {
        if (byteCount == 0) {
            writer.data(outFinished, streamId, buffer, 0);
            return;
        }

        while (byteCount > 0) {
            int toWrite;
            synchronized (Http2Connection.this) {
                try {
                    while (bytesLeftInWriteWindow <= 0) {
                        if (!streams.containsKey(streamId)) {
                            throw new IOException("stream closed");
                        }
                        Http2Connection.this.wait();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException();
                }

                toWrite = (int) Math.min(byteCount, bytesLeftInWriteWindow);
                toWrite = Math.min(toWrite, writer.maxDataLength());
                bytesLeftInWriteWindow -= toWrite;
            }

            byteCount -= toWrite;
            writer.data(outFinished && byteCount == 0, streamId, buffer, toWrite);
        }
    }

    void writeSynResetLater(final int streamId, final Http2ErrorCode errorCode) {
        try {
            writerExecutor.execute(new NamedRunnable("Http %s stream %d", connectionName, streamId) {
                @Override
                public void execute() {
                    try {
                        writeSynReset(streamId, errorCode);
                    } catch (IOException e) {
                        failConnection(e);
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {
            // This connection has been closed.
        }
    }

    void writeSynReset(int streamId, Http2ErrorCode statusCode) throws IOException {
        writer.rstStream(streamId, statusCode);
    }

    void writeWindowUpdateLater(final int streamId, final long unacknowledgedBytesRead) {
        try {
            writerExecutor.execute(
                    new NamedRunnable("Http Window Update %s stream %d", connectionName, streamId) {
                        @Override
                        public void execute() {
                            try {
                                writer.windowUpdate(streamId, unacknowledgedBytesRead);
                            } catch (IOException e) {
                                failConnection(e);
                            }
                        }
                    });
        } catch (RejectedExecutionException ignored) {
            // This connection has been closed.
        }
    }

    void writePing(boolean reply, int payload1, int payload2) {
        try {
            writer.ping(reply, payload1, payload2);
        } catch (IOException e) {
            failConnection(e);
        }
    }

    /**
     * For testing: sends a ping and waits for a pong.
     */
    void writePingAndAwaitPong() throws InterruptedException {
        writePing();
        awaitPong();
    }

    /**
     * For testing: sends a ping to be awaited with {@link #awaitPong}.
     */
    void writePing() {
        synchronized (this) {
            awaitPingsSent++;
        }
        writePing(false, AWAIT_PING, 0x4f4b6f6b /* "OKok" */);
    }

    /**
     * For testing: awaits a pong.
     */
    synchronized void awaitPong() throws InterruptedException {
        while (awaitPongsReceived < awaitPingsSent) {
            wait();
        }
    }

    public void flush() throws IOException {
        writer.flush();
    }

    /**
     * Degrades this connection such that new streams can neither be created locally, nor accepted
     * from the remote peer. Existing streams are not impacted. This is intended to permit an endpoint
     * to gracefully stop accepting new requests without harming previously established streams.
     */
    public void shutdown(Http2ErrorCode statusCode) throws IOException {
        synchronized (writer) {
            int lastGoodStreamId;
            synchronized (this) {
                if (shutdown) {
                    return;
                }
                shutdown = true;
                lastGoodStreamId = this.lastGoodStreamId;
            }
            writer.goAway(lastGoodStreamId, statusCode, Normal.EMPTY_BYTE_ARRAY);
        }
    }

    /**
     * Closes this connection. This cancels all open streams and unanswered pings. It closes the
     * underlying input and output streams and shuts down internal executor services.
     */
    @Override
    public void close() {
        close(Http2ErrorCode.NO_ERROR, Http2ErrorCode.CANCEL, null);
    }

    void close(Http2ErrorCode connectionCode, Http2ErrorCode streamCode, IOException cause) {
        assert (!Thread.holdsLock(this));
        try {
            shutdown(connectionCode);
        } catch (IOException ignored) {
        }

        Http2Stream[] streamsToClose = null;
        synchronized (this) {
            if (!streams.isEmpty()) {
                streamsToClose = streams.values().toArray(new Http2Stream[streams.size()]);
                streams.clear();
            }
        }

        if (streamsToClose != null) {
            for (Http2Stream stream : streamsToClose) {
                try {
                    stream.close(streamCode, cause);
                } catch (IOException ignored) {
                }
            }
        }

        // Close the writer to release its resources (such as deflaters).
        try {
            writer.close();
        } catch (IOException ignored) {
        }

        // Close the socket to break out the reader thread, which will clean up after itself.
        try {
            socket.close();
        } catch (IOException ignored) {
        }

        // Release the threads.
        writerExecutor.shutdown();
        pushExecutor.shutdown();
    }

    private void failConnection(IOException e) {
        close(Http2ErrorCode.PROTOCOL_ERROR, Http2ErrorCode.PROTOCOL_ERROR, e);
    }

    /**
     * Sends any initial frames and starts reading frames from the remote peer. This should be called
     * after {@link Builder#build} for all new connections.
     */
    public void start() throws IOException {
        start(true);
    }

    /**
     * @param sendConnectionPreface true to send connection preface frames. This should always be true
     *                              except for in tests that don't check for a connection preface.
     */
    void start(boolean sendConnectionPreface) throws IOException {
        if (sendConnectionPreface) {
            writer.connectionPreface();
            writer.settings(settings);
            int windowSize = settings.getInitialWindowSize();
            if (windowSize != HTTP.DEFAULT_INITIAL_WINDOW_SIZE) {
                writer.windowUpdate(0, windowSize - HTTP.DEFAULT_INITIAL_WINDOW_SIZE);
            }
        }
        new Thread(readerRunnable).start(); // Not a daemon thread.
    }

    /**
     * Merges {@code settings} into this peer's settings and sends them to the remote peer.
     */
    public void setSettings(Http2Settings settings) throws IOException {
        synchronized (writer) {
            synchronized (this) {
                if (shutdown) {
                    throw new IOException();
                }
                settings.merge(settings);
            }
            writer.settings(settings);
        }
    }

    public synchronized boolean isHealthy(long nowNs) {
        if (shutdown) return false;

        // A degraded pong is overdue.
        if (degradedPongsReceived < degradedPingsSent && nowNs >= degradedPongDeadlineNs) return false;

        return true;
    }

    /**
     * HTTP/2 can have both stream timeouts (due to a problem with a single stream) and connection
     * timeouts (due to a problem with the transport). When a stream times out we don't know whether
     * the problem impacts just one stream or the entire connection.
     * To differentiate the two cases we ping the server when a stream times out. If the overall
     * connection is fine the ping will receive a pong; otherwise it won't.
     * The deadline to respond to this ping attempts to limit the cost of being wrong. If it is too
     * long, streams created while we await the pong will reuse broken connections and inevitably
     * fail. If it is too short, slow connections will be marked as failed and extra TCP and TLS
     * handshakes will be required.
     * The deadline is currently hardcoded. We may make this configurable in the future!
     */
    void sendDegradedPingLater() {
        synchronized (this) {
            if (degradedPongsReceived < degradedPingsSent) return; // Already awaiting a degraded pong.
            degradedPingsSent++;
            degradedPongDeadlineNs = System.nanoTime() + DEGRADED_PONG_TIMEOUT_NS;
        }
        try {
            writerExecutor.execute(new NamedRunnable("Http %s ping", connectionName) {
                @Override
                public void execute() {
                    writePing(false, DEGRADED_PING, 0);
                }
            });
        } catch (RejectedExecutionException ignored) {
            // This connection has been closed.
        }
    }

    /**
     * Even, positive numbered streams are pushed streams in HTTP/2.
     */
    boolean pushedStream(int streamId) {
        return streamId != 0 && (streamId & 1) == 0;
    }

    void pushRequestLater(final int streamId, final List<Http2Header> requestHeaders) {
        synchronized (this) {
            if (currentPushRequests.contains(streamId)) {
                writeSynResetLater(streamId, Http2ErrorCode.PROTOCOL_ERROR);
                return;
            }
            currentPushRequests.add(streamId);
        }
        try {
            pushExecutorExecute(new NamedRunnable(
                    "Http %s Push Request[%s]", connectionName, streamId) {
                @Override
                public void execute() {
                    boolean cancel = pushObserver.onRequest(streamId, requestHeaders);
                    try {
                        if (cancel) {
                            writer.rstStream(streamId, Http2ErrorCode.CANCEL);
                            synchronized (Http2Connection.this) {
                                currentPushRequests.remove(streamId);
                            }
                        }
                    } catch (IOException ignored) {
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {
            // This connection has been closed.
        }
    }

    void pushHeadersLater(final int streamId, final List<Http2Header> requestHeaders,
                          final boolean inFinished) {
        try {
            pushExecutorExecute(new NamedRunnable(
                    "Http %s Push Headers[%s]", connectionName, streamId) {
                @Override
                public void execute() {
                    boolean cancel = pushObserver.onHeaders(streamId, requestHeaders, inFinished);
                    try {
                        if (cancel) writer.rstStream(streamId, Http2ErrorCode.CANCEL);
                        if (cancel || inFinished) {
                            synchronized (Http2Connection.this) {
                                currentPushRequests.remove(streamId);
                            }
                        }
                    } catch (IOException ignored) {
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {
            // This connection has been closed.
        }
    }

    /**
     * Eagerly reads {@code byteCount} bytes from the source before launching a background task to
     * process the data.  This avoids corrupting the stream.
     */
    void pushDataLater(final int streamId, final BufferSource source, final int byteCount,
                       final boolean inFinished) throws IOException {
        final Buffer buffer = new Buffer();
        source.require(byteCount); // Eagerly read the frame before firing client thread.
        source.read(buffer, byteCount);
        if (buffer.size() != byteCount) throw new IOException(buffer.size() + " != " + byteCount);
        pushExecutorExecute(new NamedRunnable("Http %s Push Data[%s]", connectionName, streamId) {
            @Override
            public void execute() {
                try {
                    boolean cancel = pushObserver.onData(streamId, buffer, byteCount, inFinished);
                    if (cancel) writer.rstStream(streamId, Http2ErrorCode.CANCEL);
                    if (cancel || inFinished) {
                        synchronized (Http2Connection.this) {
                            currentPushRequests.remove(streamId);
                        }
                    }
                } catch (IOException ignored) {
                }
            }
        });
    }

    void pushResetLater(final int streamId, final Http2ErrorCode errorCode) {
        pushExecutorExecute(new NamedRunnable("Http %s Push Reset[%s]", connectionName, streamId) {
            @Override
            public void execute() {
                pushObserver.onReset(streamId, errorCode);
                synchronized (Http2Connection.this) {
                    currentPushRequests.remove(streamId);
                }
            }
        });
    }

    private synchronized void pushExecutorExecute(NamedRunnable namedRunnable) {
        if (!shutdown) {
            pushExecutor.execute(namedRunnable);
        }
    }

    public static class Builder {
        Socket socket;
        String connectionName;
        BufferSource source;
        BufferSink sink;
        Listener listener = Listener.REFUSE_INCOMING_STREAMS;
        PushObserver pushObserver = PushObserver.CANCEL;
        boolean client;
        int pingIntervalMillis;

        /**
         * @param client true if this peer initiated the connection; false if this peer accepted the
         *               connection.
         */
        public Builder(boolean client) {
            this.client = client;
        }

        public Builder socket(Socket socket) throws IOException {
            SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
            String connectionName = remoteSocketAddress instanceof InetSocketAddress
                    ? ((InetSocketAddress) remoteSocketAddress).getHostName()
                    : remoteSocketAddress.toString();
            return socket(socket, connectionName,
                    IoKit.buffer(IoKit.source(socket)), IoKit.buffer(IoKit.sink(socket)));
        }

        public Builder socket(
                Socket socket, String connectionName, BufferSource source, BufferSink sink) {
            this.socket = socket;
            this.connectionName = connectionName;
            this.source = source;
            this.sink = sink;
            return this;
        }

        public Builder listener(Listener listener) {
            this.listener = listener;
            return this;
        }

        public Builder pushObserver(PushObserver pushObserver) {
            this.pushObserver = pushObserver;
            return this;
        }

        public Builder pingIntervalMillis(int pingIntervalMillis) {
            this.pingIntervalMillis = pingIntervalMillis;
            return this;
        }

        public Http2Connection build() {
            return new Http2Connection(this);
        }
    }

    /**
     * Listener of streams and settings initiated by the peer.
     */
    public abstract static class Listener {

        public static final Listener REFUSE_INCOMING_STREAMS = new Listener() {
            @Override
            public void onStream(Http2Stream stream) throws IOException {
                stream.close(Http2ErrorCode.REFUSED_STREAM, null);
            }
        };

        /**
         * Handle a new stream from this connection's peer. Implementations should respond by either
         * {@linkplain Http2Stream#writeHeaders replying to the stream} or {@linkplain
         * Http2Stream#close closing it}. This response does not need to be synchronous.
         */
        public abstract void onStream(Http2Stream stream) throws IOException;

        /**
         * Notification that the connection's peer's settings may have changed. Implementations should
         * take appropriate action to handle the updated settings.
         * It is the implementation's responsibility to handle concurrent calls to this method. A
         * remote peer that sends multiple settings frames will trigger multiple calls to this method,
         * and those calls are not necessarily serialized.
         */
        public void onSettings(Http2Connection connection) {
        }
    }

    class PingRunnable extends NamedRunnable {

        final boolean reply;
        final int payload1;
        final int payload2;

        PingRunnable(boolean reply, int payload1, int payload2) {
            super("Http %s ping %08x%08x", connectionName, payload1, payload2);
            this.reply = reply;
            this.payload1 = payload1;
            this.payload2 = payload2;
        }

        @Override
        public void execute() {
            writePing(reply, payload1, payload2);
        }
    }

    class IntervalPingRunnable extends NamedRunnable {

        IntervalPingRunnable() {
            super("Http %s ping", connectionName);
        }

        @Override
        public void execute() {
            boolean failDueToMissingPong;
            synchronized (Http2Connection.this) {
                if (intervalPongsReceived < intervalPingsSent) {
                    failDueToMissingPong = true;
                } else {
                    intervalPingsSent++;
                    failDueToMissingPong = false;
                }
            }
            if (failDueToMissingPong) {
                failConnection(null);
            } else {
                writePing(false, INTERVAL_PING, 0);
            }
        }
    }

    /**
     * Methods in this class must not lock FrameWriter.  If a method needs to write a frame, create an
     * async task to do so.
     */
    class ReaderRunnable extends NamedRunnable implements Http2Reader.Handler {

        final Http2Reader reader;

        ReaderRunnable(Http2Reader reader) {
            super("Http %s", connectionName);
            this.reader = reader;
        }

        @Override
        protected void execute() {
            Http2ErrorCode connectionErrorCode = Http2ErrorCode.INTERNAL_ERROR;
            Http2ErrorCode streamErrorCode = Http2ErrorCode.INTERNAL_ERROR;
            IOException errorException = null;
            try {
                reader.readConnectionPreface(this);
                while (reader.nextFrame(false, this)) {
                }
                connectionErrorCode = Http2ErrorCode.NO_ERROR;
                streamErrorCode = Http2ErrorCode.CANCEL;
            } catch (IOException e) {
                errorException = e;
                connectionErrorCode = Http2ErrorCode.PROTOCOL_ERROR;
                streamErrorCode = Http2ErrorCode.PROTOCOL_ERROR;
            } finally {
                close(connectionErrorCode, streamErrorCode, errorException);
                IoKit.close(reader);
            }
        }

        @Override
        public void data(boolean inFinished, int streamId, BufferSource source, int length)
                throws IOException {
            if (pushedStream(streamId)) {
                pushDataLater(streamId, source, length, inFinished);
                return;
            }
            Http2Stream dataStream = getStream(streamId);
            if (dataStream == null) {
                writeSynResetLater(streamId, Http2ErrorCode.PROTOCOL_ERROR);
                updateConnectionFlowControl(length);
                source.skip(length);
                return;
            }
            dataStream.receiveData(source, length);
            if (inFinished) {
                dataStream.receiveHeaders(org.miaixz.bus.http.Builder.EMPTY_HEADERS, true);
            }
        }

        @Override
        public void headers(boolean inFinished, int streamId, int associatedStreamId,
                            List<Http2Header> headerBlock) {
            if (pushedStream(streamId)) {
                pushHeadersLater(streamId, headerBlock, inFinished);
                return;
            }
            Http2Stream stream;
            synchronized (Http2Connection.this) {
                stream = getStream(streamId);

                if (stream == null) {
                    // If we're shutdown, don't bother with this stream.
                    if (shutdown) return;

                    // If the stream ID is less than the last created ID, assume it's already closed.
                    if (streamId <= lastGoodStreamId) return;

                    // If the stream ID is in the client's namespace, assume it's already closed.
                    if (streamId % 2 == nextStreamId % 2) return;

                    // Create a stream.
                    Headers headers = org.miaixz.bus.http.Builder.toHeaders(headerBlock);
                    final Http2Stream newStream = new Http2Stream(streamId, Http2Connection.this,
                            false, inFinished, headers);
                    lastGoodStreamId = streamId;
                    streams.put(streamId, newStream);
                    listenerExecutor.execute(new NamedRunnable(
                            "Http %s stream %d", connectionName, streamId) {
                        @Override
                        public void execute() {
                            try {
                                listener.onStream(newStream);
                            } catch (IOException e) {
                                Logger.info("Http2Connection.Listener failure for " + connectionName, e);
                                try {
                                    newStream.close(Http2ErrorCode.PROTOCOL_ERROR, e);
                                } catch (IOException ignored) {
                                }
                            }
                        }
                    });
                    return;
                }
            }

            // Update an existing stream.
            stream.receiveHeaders(org.miaixz.bus.http.Builder.toHeaders(headerBlock), inFinished);
        }

        @Override
        public void rstStream(int streamId, Http2ErrorCode errorCode) {
            if (pushedStream(streamId)) {
                pushResetLater(streamId, errorCode);
                return;
            }
            Http2Stream rstStream = removeStream(streamId);
            if (rstStream != null) {
                rstStream.receiveRstStream(errorCode);
            }
        }

        @Override
        public void settings(boolean clearPrevious, Http2Settings settings) {
            try {
                writerExecutor.execute(new NamedRunnable("Http %s ACK Settings", connectionName) {
                    @Override
                    public void execute() {
                        applyAndAckSettings(clearPrevious, settings);
                    }
                });
            } catch (RejectedExecutionException ignored) {
                // This connection has been closed.
            }
        }

        void applyAndAckSettings(boolean clearPrevious, Http2Settings settings) {
            long delta = 0;
            Http2Stream[] streamsToNotify = null;
            synchronized (writer) {
                synchronized (Http2Connection.this) {
                    int priorWriteWindowSize = peerSettings.getInitialWindowSize();
                    if (clearPrevious) peerSettings.clear();
                    peerSettings.merge(settings);
                    int peerInitialWindowSize = peerSettings.getInitialWindowSize();
                    if (peerInitialWindowSize != -1 && peerInitialWindowSize != priorWriteWindowSize) {
                        delta = peerInitialWindowSize - priorWriteWindowSize;
                        streamsToNotify = !streams.isEmpty()
                                ? streams.values().toArray(new Http2Stream[streams.size()])
                                : null;
                    }
                }
                try {
                    writer.applyAndAckSettings(peerSettings);
                } catch (IOException e) {
                    failConnection(e);
                }
            }
            if (streamsToNotify != null) {
                for (Http2Stream stream : streamsToNotify) {
                    synchronized (stream) {
                        stream.addBytesToWriteWindow(delta);
                    }
                }
            }
            listenerExecutor.execute(new NamedRunnable("Http %s settings", connectionName) {
                @Override
                public void execute() {
                    listener.onSettings(Http2Connection.this);
                }
            });
        }

        @Override
        public void ackSettings() {

        }

        @Override
        public void ping(boolean reply, int payload1, int payload2) {
            if (reply) {
                synchronized (Http2Connection.this) {
                    if (payload1 == INTERVAL_PING) {
                        intervalPongsReceived++;
                    } else if (payload1 == DEGRADED_PING) {
                        degradedPongsReceived++;
                    } else if (payload1 == AWAIT_PING) {
                        awaitPongsReceived++;
                        Http2Connection.this.notifyAll();
                    }
                }
            } else {
                try {
                    // Send a reply to a client ping if this is a server and vice versa.
                    writerExecutor.execute(new PingRunnable(true, payload1, payload2));
                } catch (RejectedExecutionException ignored) {
                    // This connection has been closed.
                }
            }
        }

        @Override
        public void goAway(int lastGoodStreamId, Http2ErrorCode errorCode, ByteString debugData) {
            if (debugData.size() > 0) { // TODO: log the debugData
            }

            // Copy the streams first. We don't want to hold a lock when we call receiveRstStream().
            Http2Stream[] streamsCopy;
            synchronized (Http2Connection.this) {
                streamsCopy = streams.values().toArray(new Http2Stream[streams.size()]);
                shutdown = true;
            }

            for (Http2Stream http2Stream : streamsCopy) {
                if (http2Stream.getId() > lastGoodStreamId && http2Stream.isLocallyInitiated()) {
                    http2Stream.receiveRstStream(Http2ErrorCode.REFUSED_STREAM);
                    removeStream(http2Stream.getId());
                }
            }
        }

        @Override
        public void windowUpdate(int streamId, long windowSizeIncrement) {
            if (streamId == 0) {
                synchronized (Http2Connection.this) {
                    bytesLeftInWriteWindow += windowSizeIncrement;
                    Http2Connection.this.notifyAll();
                }
            } else {
                Http2Stream stream = getStream(streamId);
                if (null != stream) {
                    synchronized (stream) {
                        stream.addBytesToWriteWindow(windowSizeIncrement);
                    }
                }
            }
        }

        @Override
        public void priority(int streamId, int streamDependency, int weight,
                             boolean exclusive) {
        }

        @Override
        public void pushPromise(int streamId, int promisedStreamId, List<Http2Header> requestHeaders) {
            pushRequestLater(promisedStreamId, requestHeaders);
        }

        @Override
        public void alternateService(int streamId, String origin, ByteString protocol,
                                     String host, int port, long maxAge) {
        }
    }

}
