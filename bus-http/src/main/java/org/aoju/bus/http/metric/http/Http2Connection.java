/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.http.metric.http;

import org.aoju.bus.core.io.Buffer;
import org.aoju.bus.core.io.BufferSink;
import org.aoju.bus.core.io.BufferSource;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.lang.exception.RevisedException;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.http.Headers;
import org.aoju.bus.http.Protocol;
import org.aoju.bus.http.Settings;
import org.aoju.bus.http.metric.NamedRunnable;
import org.aoju.bus.logger.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;

/**
 * 到远程对等点的套接字连接。连接主机可以发送和接收数据流.
 *
 * @author Kimi Liu
 * @version 6.3.2
 * @since JDK 1.8+
 */
public final class Http2Connection implements Closeable {

    static final int CLIENT_WINDOW_SIZE = Normal._16 * Normal._1024 * Normal._1024;

    /**
     * 共享执行程序来发送传入流的通知。这个执行器需要多个线程，因为侦听器不需要立即返回.
     */
    private static final ExecutorService listenerExecutor = new ThreadPoolExecutor(0,
            Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue<>(),
            org.aoju.bus.http.Builder.threadFactory("Httpd Http2Connection", true));

    /**
     * 如果该对等点发起连接，则为True.
     */
    final boolean client;

    /**
     * 响应传入流或设置而运行的用户代码。对它的调用总是在{@link #listenerExecutor}上调用
     */
    final Listener listener;
    final Map<Integer, Http2Stream> streams = new LinkedHashMap<>();
    final String hostname;
    /**
     * 为响应推送承诺事件而运行的用户代码
     */
    final PushObserver pushObserver;
    /**
     * 我们从对等点接收设置.
     */
    final Settings peerSettings = new Settings();
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
    boolean shutdown;
    /**
     * 应用程序消耗的总字节数，但尚未通过在此连接上发送{@code WINDOW_UPDATE}帧来确认.
     */
    long unacknowledgedBytesRead = 0;
    /**
     * 在接收窗口更新之前，可以在连接上写入的字节数.
     */
    long bytesLeftInWriteWindow;
    /**
     * 设置我们与对等点通信.
     */
    Settings settings = new Settings();
    /**
     * 如果我们发送了一个仍在等待回复的ping，则为真.
     */
    private boolean awaitingPong;

    Http2Connection(Builder builder) {
        pushObserver = builder.pushObserver;
        client = builder.client;
        listener = builder.listener;
        nextStreamId = builder.client ? 1 : 2;
        if (builder.client) {
            nextStreamId += 2;
        }

        if (builder.client) {
            settings.set(Http.INITIAL_WINDOW_SIZE, CLIENT_WINDOW_SIZE);
        }

        hostname = builder.hostname;

        writerExecutor = new ScheduledThreadPoolExecutor(1,
                org.aoju.bus.http.Builder.threadFactory(StringKit.format("Httpd %s Writer", hostname), false));
        if (builder.pingIntervalMillis != 0) {
            writerExecutor.scheduleAtFixedRate(new PingRunnable(false, 0, 0),
                    builder.pingIntervalMillis, builder.pingIntervalMillis, TimeUnit.MILLISECONDS);
        }

        pushExecutor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                org.aoju.bus.http.Builder.threadFactory(StringKit.format("Httpd %s Push Observer", hostname), true));
        peerSettings.set(Http.INITIAL_WINDOW_SIZE, Http.DEFAULT_INITIAL_WINDOW_SIZE);
        peerSettings.set(Http.MAX_FRAME_SIZE, Http2.INITIAL_MAX_FRAME_SIZE);
        bytesLeftInWriteWindow = peerSettings.getInitialWindowSize();
        socket = builder.socket;
        writer = new Http2Writer(builder.sink, client);

        readerRunnable = new ReaderRunnable(new Http2Reader(builder.source, client));
    }

    public Protocol getProtocol() {
        return Protocol.HTTP_2;
    }

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
    public Http2Stream pushStream(int associatedStreamId, List<HttpHeaders> requestHeaders, boolean out)
            throws IOException {
        if (client) throw new IllegalStateException("Client cannot push requests.");
        return newStream(associatedStreamId, requestHeaders, out);
    }

    public Http2Stream newStream(List<HttpHeaders> requestHeaders, boolean out) throws IOException {
        return newStream(0, requestHeaders, out);
    }

    private Http2Stream newStream(
            int associatedStreamId, List<HttpHeaders> requestHeaders, boolean out) throws IOException {
        boolean outFinished = !out;
        boolean inFinished = false;
        boolean flushHeaders;
        Http2Stream stream;
        int streamId;

        synchronized (writer) {
            synchronized (this) {
                if (nextStreamId > Integer.MAX_VALUE / 2) {
                    shutdown(ErrorCode.REFUSED_STREAM);
                }
                if (shutdown) {
                    throw new RevisedException();
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
                writer.synStream(outFinished, streamId, associatedStreamId, requestHeaders);
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

    void writeSynReply(int streamId, boolean outFinished, List<HttpHeaders> alternating)
            throws IOException {
        writer.synReply(outFinished, streamId, alternating);
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

    void writeSynResetLater(final int streamId, final ErrorCode errorCode) {
        try {
            writerExecutor.execute(new NamedRunnable("Httpd %s stream %d", hostname, streamId) {
                @Override
                public void execute() {
                    try {
                        writeSynReset(streamId, errorCode);
                    } catch (IOException e) {
                        failConnection();
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {
            Logger.warn(Symbol.DELIM, ignored);
        }
    }

    void writeSynReset(int streamId, ErrorCode statusCode) throws IOException {
        writer.rstStream(streamId, statusCode);
    }

    void writeWindowUpdateLater(final int streamId, final long unacknowledgedBytesRead) {
        try {
            writerExecutor.execute(
                    new NamedRunnable("Httpd Window Update %s stream %d", hostname, streamId) {
                        @Override
                        public void execute() {
                            try {
                                writer.windowUpdate(streamId, unacknowledgedBytesRead);
                            } catch (IOException e) {
                                failConnection();
                            }
                        }
                    });
        } catch (RejectedExecutionException ignored) {
            Logger.warn(Symbol.DELIM, ignored);
        }
    }

    void writePing(boolean reply, int payload1, int payload2) {
        if (!reply) {
            boolean failedDueToMissingPong;
            synchronized (this) {
                failedDueToMissingPong = awaitingPong;
                awaitingPong = true;
            }
            if (failedDueToMissingPong) {
                failConnection();
                return;
            }
        }

        try {
            writer.ping(reply, payload1, payload2);
        } catch (IOException e) {
            failConnection();
        }
    }

    void writePingAndAwaitPong() throws InterruptedException {
        writePing(false, 0x4f4b6f6b, 0xf09f8da9);
        awaitPong();
    }

    synchronized void awaitPong() throws InterruptedException {
        while (awaitingPong) {
            wait();
        }
    }

    public void flush() throws IOException {
        writer.flush();
    }

    public void shutdown(ErrorCode statusCode) throws IOException {
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

    @Override
    public void close() throws IOException {
        close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
    }

    void close(ErrorCode connectionCode, ErrorCode streamCode) throws IOException {
        assert (!Thread.holdsLock(this));
        IOException thrown = null;
        try {
            shutdown(connectionCode);
        } catch (IOException e) {
            thrown = e;
        }

        Http2Stream[] streamsToClose = null;
        synchronized (this) {
            if (!streams.isEmpty()) {
                streamsToClose = streams.values().toArray(new Http2Stream[streams.size()]);
                streams.clear();
            }
        }

        if (null != streamsToClose) {
            for (Http2Stream stream : streamsToClose) {
                try {
                    stream.close(streamCode);
                } catch (IOException e) {
                    if (null != thrown) thrown = e;
                }
            }
        }

        try {
            writer.close();
        } catch (IOException e) {
            if (null == thrown) thrown = e;
        }

        try {
            socket.close();
        } catch (IOException e) {
            thrown = e;
        }

        writerExecutor.shutdown();
        pushExecutor.shutdown();

        if (null != thrown) throw thrown;
    }

    private void failConnection() {
        try {
            close(ErrorCode.PROTOCOL_ERROR, ErrorCode.PROTOCOL_ERROR);
        } catch (IOException ignored) {
        }
    }

    public void start() throws IOException {
        start(true);
    }

    /**
     * @param sendConnectionPreface 真正发送连接序言帧。这应该总是正确的，除了在不检查连接序言的测试中.
     */
    void start(boolean sendConnectionPreface) throws IOException {
        if (sendConnectionPreface) {
            writer.connectionPreface();
            writer.settings(settings);
            int windowSize = settings.getInitialWindowSize();
            if (windowSize != Http.DEFAULT_INITIAL_WINDOW_SIZE) {
                writer.windowUpdate(0, windowSize - Http.DEFAULT_INITIAL_WINDOW_SIZE);
            }
        }
        new Thread(readerRunnable).start();
    }

    public void setSettings(Settings settings) throws IOException {
        synchronized (writer) {
            synchronized (this) {
                if (shutdown) {
                    throw new RevisedException();
                }
                settings.merge(settings);
            }
            writer.settings(settings);
        }
    }

    public synchronized boolean isShutdown() {
        return shutdown;
    }

    boolean pushedStream(int streamId) {
        return streamId != 0 && (streamId & 1) == 0;
    }

    void pushRequestLater(final int streamId, final List<HttpHeaders> requestHeaders) {
        synchronized (this) {
            if (currentPushRequests.contains(streamId)) {
                writeSynResetLater(streamId, ErrorCode.PROTOCOL_ERROR);
                return;
            }
            currentPushRequests.add(streamId);
        }
        try {
            pushExecutorExecute(new NamedRunnable("Httpd %s Push Request[%s]", hostname, streamId) {
                @Override
                public void execute() {
                    boolean cancel = pushObserver.onRequest(streamId, requestHeaders);
                    try {
                        if (cancel) {
                            writer.rstStream(streamId, ErrorCode.CANCEL);
                            synchronized (Http2Connection.this) {
                                currentPushRequests.remove(streamId);
                            }
                        }
                    } catch (IOException ignored) {
                    }
                }
            });
        } catch (RejectedExecutionException ignored) {
            Logger.warn(Symbol.DELIM, ignored);
        }
    }

    void pushHeadersLater(final int streamId, final List<HttpHeaders> requestHeaders,
                          final boolean inFinished) {
        try {
            pushExecutorExecute(new NamedRunnable("Httpd %s Push Headers[%s]", hostname, streamId) {
                @Override
                public void execute() {
                    boolean cancel = pushObserver.onHeaders(streamId, requestHeaders, inFinished);
                    try {
                        if (cancel) writer.rstStream(streamId, ErrorCode.CANCEL);
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
            Logger.warn(Symbol.DELIM, ignored);
        }
    }

    void pushDataLater(final int streamId, final BufferSource source, final int byteCount,
                       final boolean inFinished) throws IOException {
        final Buffer buffer = new Buffer();
        source.require(byteCount);
        source.read(buffer, byteCount);
        if (buffer.size() != byteCount) throw new IOException(buffer.size() + " != " + byteCount);
        pushExecutorExecute(new NamedRunnable("Httpd %s Push Data[%s]", hostname, streamId) {
            @Override
            public void execute() {
                try {
                    boolean cancel = pushObserver.onData(streamId, buffer, byteCount, inFinished);
                    if (cancel) writer.rstStream(streamId, ErrorCode.CANCEL);
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

    void pushResetLater(final int streamId, final ErrorCode errorCode) {
        pushExecutorExecute(new NamedRunnable("Httpd %s Push Reset[%s]", hostname, streamId) {
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
        if (!isShutdown()) {
            pushExecutor.execute(namedRunnable);
        }
    }

    public static class Builder {
        Socket socket;
        String hostname;
        BufferSource source;
        BufferSink sink;
        Listener listener = Listener.REFUSE_INCOMING_STREAMS;
        PushObserver pushObserver = PushObserver.CANCEL;
        boolean client;
        int pingIntervalMillis;

        /**
         * @param client 如果该对等方发起连接，则为;如果该对等点接受了连接，则为false.
         */
        public Builder(boolean client) {
            this.client = client;
        }

        public Builder socket(Socket socket) throws IOException {
            return socket(socket, ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostName(),
                    IoKit.buffer(IoKit.source(socket)), IoKit.buffer(IoKit.sink(socket)));
        }

        public Builder socket(
                Socket socket, String hostname, BufferSource source, BufferSink sink) {
            this.socket = socket;
            this.hostname = hostname;
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
     * 侦听由对等方发起的流和设置.
     */
    public abstract static class Listener {
        public static final Listener REFUSE_INCOMING_STREAMS = new Listener() {
            @Override
            public void onStream(Http2Stream stream) throws IOException {
                stream.close(ErrorCode.REFUSED_STREAM);
            }
        };

        /**
         * 处理来自此连接的对等点的新流。实现应该通过{@linkplain Http2Stream#writeHeaders 响应流}或
         * {@linkplain Http2Stream#close closing it}。这个响应不需要是同步的
         *
         * @param stream 响应流
         * @throws IOException 异常
         */
        public abstract void onStream(Http2Stream stream) throws IOException;

        /**
         * 通知连接的对等点的设置可能已更改。实现应该采取适当的操作来处理更新的设置
         * 处理对该方法的并发调用是实现的职责。发送多个设置帧的远程对等点将触发对该
         * 方法的多个调用，而这些调用不一定是序列化的
         *
         * @param connection 连接信息
         */
        public void onSettings(Http2Connection connection) {
        }
    }

    final class PingRunnable extends NamedRunnable {
        final boolean reply;
        final int payload1;
        final int payload2;

        PingRunnable(boolean reply, int payload1, int payload2) {
            super("Httpd %s ping %08x%08x", hostname, payload1, payload2);
            this.reply = reply;
            this.payload1 = payload1;
            this.payload2 = payload2;
        }

        @Override
        public void execute() {
            writePing(reply, payload1, payload2);
        }
    }

    /**
     * 该类中的方法不能锁定FrameWriter。如果一个方法需要写一个框架，创建一个异步任务来完成
     */
    class ReaderRunnable extends NamedRunnable implements Http2Reader.Handler {
        final Http2Reader reader;

        ReaderRunnable(Http2Reader reader) {
            super("Httpd %s", hostname);
            this.reader = reader;
        }

        @Override
        protected void execute() {
            ErrorCode connectionErrorCode = ErrorCode.INTERNAL_ERROR;
            ErrorCode streamErrorCode = ErrorCode.INTERNAL_ERROR;
            try {
                reader.readConnectionPreface(this);
                while (reader.nextFrame(false, this)) {
                }
                connectionErrorCode = ErrorCode.NO_ERROR;
                streamErrorCode = ErrorCode.CANCEL;
            } catch (IOException e) {
                connectionErrorCode = ErrorCode.PROTOCOL_ERROR;
                streamErrorCode = ErrorCode.PROTOCOL_ERROR;
            } finally {
                try {
                    close(connectionErrorCode, streamErrorCode);
                } catch (IOException ignored) {
                }
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
            if (null == dataStream) {
                writeSynResetLater(streamId, ErrorCode.PROTOCOL_ERROR);
                updateConnectionFlowControl(length);
                source.skip(length);
                return;
            }
            dataStream.receiveData(source, length);
            if (inFinished) {
                dataStream.receiveFin();
            }
        }

        @Override
        public void headers(boolean inFinished, int streamId, int associatedStreamId,
                            List<HttpHeaders> headersBlock) {
            if (pushedStream(streamId)) {
                pushHeadersLater(streamId, headersBlock, inFinished);
                return;
            }
            Http2Stream stream;
            synchronized (Http2Connection.this) {
                stream = getStream(streamId);

                if (null == stream) {
                    if (shutdown) return;

                    if (streamId <= lastGoodStreamId) return;

                    if (streamId % 2 == nextStreamId % 2) return;

                    Headers headers = org.aoju.bus.http.Builder.toHeaders(headersBlock);
                    final Http2Stream newStream = new Http2Stream(streamId, Http2Connection.this,
                            false, inFinished, headers);
                    lastGoodStreamId = streamId;
                    streams.put(streamId, newStream);
                    listenerExecutor.execute(new NamedRunnable("Httpd %s stream %d", hostname, streamId) {
                        @Override
                        public void execute() {
                            try {
                                listener.onStream(newStream);
                            } catch (IOException e) {
                                Logger.info("Http2Connection.Listener failure for " + hostname, e);
                                try {
                                    newStream.close(ErrorCode.PROTOCOL_ERROR);
                                } catch (IOException ignored) {
                                }
                            }
                        }
                    });
                    return;
                }
            }

            stream.receiveHeaders(headersBlock);
            if (inFinished) stream.receiveFin();
        }

        @Override
        public void rstStream(int streamId, ErrorCode errorCode) {
            if (pushedStream(streamId)) {
                pushResetLater(streamId, errorCode);
                return;
            }
            Http2Stream rstStream = removeStream(streamId);
            if (null != rstStream) {
                rstStream.receiveRstStream(errorCode);
            }
        }

        @Override
        public void settings(final boolean clearPrevious, final Settings settings) {
            try {
                writerExecutor.execute(new NamedRunnable("Httpd %s ACK Settings", hostname) {
                    @Override
                    public void execute() {
                        applyAndAckSettings(clearPrevious, settings);
                    }
                });
            } catch (RejectedExecutionException ignored) {
                // 此连接已关闭
                Logger.warn(Symbol.DELIM, ignored);
            }
        }

        void applyAndAckSettings(boolean clearPrevious, Settings settings) {
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
                    failConnection();
                }
            }
            if (null != streamsToNotify) {
                for (Http2Stream stream : streamsToNotify) {
                    synchronized (stream) {
                        stream.addBytesToWriteWindow(delta);
                    }
                }
            }
            listenerExecutor.execute(new NamedRunnable("Httpd %s settings", hostname) {
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
                    awaitingPong = false;
                    Http2Connection.this.notifyAll();
                }
            } else {
                try {
                    // 如果这是服务器，则向客户机ping发送应答
                    writerExecutor.execute(new PingRunnable(true, payload1, payload2));
                } catch (RejectedExecutionException ignored) {
                    // 此连接已关闭
                    Logger.warn(Symbol.DELIM, ignored);
                }
            }
        }

        @Override
        public void goAway(int lastGoodStreamId, ErrorCode errorCode, ByteString debugData) {
            Http2Stream[] streamsCopy;
            synchronized (Http2Connection.this) {
                streamsCopy = streams.values().toArray(new Http2Stream[streams.size()]);
                shutdown = true;
            }

            for (Http2Stream http2Stream : streamsCopy) {
                if (http2Stream.getId() > lastGoodStreamId && http2Stream.isLocallyInitiated()) {
                    http2Stream.receiveRstStream(ErrorCode.REFUSED_STREAM);
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
        public void pushPromise(int streamId, int promisedStreamId, List<HttpHeaders> requestHeaders) {
            pushRequestLater(promisedStreamId, requestHeaders);
        }

        @Override
        public void alternateService(int streamId, String origin, ByteString protocol,
                                     String host, int port, long maxAge) {
        }
    }

}
