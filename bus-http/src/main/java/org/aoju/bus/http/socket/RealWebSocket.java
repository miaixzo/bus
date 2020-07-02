/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.http.socket;

import org.aoju.bus.core.io.BufferSink;
import org.aoju.bus.core.io.BufferSource;
import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.Header;
import org.aoju.bus.core.lang.Http;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.IoKit;
import org.aoju.bus.http.*;
import org.aoju.bus.http.accord.StreamAllocation;
import org.aoju.bus.http.metric.EventListener;

import java.io.Closeable;
import java.io.IOException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Kimi Liu
 * @version 6.0.1
 * @since JDK 1.8+
 */
public final class RealWebSocket implements WebSocket, WebSocketReader.FrameCallback {

    private static final List<Protocol> ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);

    /**
     * 要加入队列的最大字节数。而不是排队超过这个限制，我们拆掉web套接字!有可能我们写得比别人读得快
     * 16 MiB
     */
    private static final long MAX_QUEUE_SIZE = 16 * 1024 * 1024;

    /**
     * 客户端调用{@link #close}以等待适当关闭的最大时间量。如果服务器没有响应，websocket将被取消
     */
    private static final long CANCEL_AFTER_CLOSE_MILLIS = 60 * 1000;
    final WebSocketListener listener;
    /**
     * 应用程序的原始请求未受web套接字头的影响
     */
    private final Request originalRequest;
    private final Random random;
    private final long pingIntervalMillis;
    private final String key;
    /**
     * 这个runnable处理传出队列。在进入队列后调用{@link #runWriter()}.
     */
    private final Runnable writerRunnable;
    /**
     * 发出的ping信号的顺序应该是写出来的
     */
    private final ArrayDeque<ByteString> pongQueue = new ArrayDeque<>();
    /**
     * 发送消息和关闭帧的顺序应该是它们被写入的顺序
     */
    private final ArrayDeque<Object> messageAndCloseQueue = new ArrayDeque<>();
    /**
     * 客户端web套接字是非空的。这些可以被取消.
     */
    private NewCall call;
    /**
     * 在连接此web套接字之前为空。仅由读线程访问
     */
    private WebSocketReader reader;
    /**
     * 在连接此web套接字之前为空。注意，消息可能在此之前排队
     */
    private WebSocketWriter writer;
    /**
     * 在连接此web套接字之前为空。用于写、ping和关闭超时
     */
    private ScheduledExecutorService executor;
    /**
     * 此web套接字持有的流。在读取所有传入消息和写入所有传出消息之前，这是非空的
     * 当读者和作者都精疲力尽，或者出现任何失败时，它就关闭了
     */
    private Streams streams;
    /**
     * 排队但尚未传输的消息的总大小(以字节为单位)
     */
    private long queueSize;

    /**
     * 如果我们加入了一个闭帧，则为真。不再有消息帧进入队列
     */
    private boolean enqueuedClose;

    /**
     * 执行时将取消此websocket。如果不必要的话，应该取消这个future本身，因为web套接字已经关闭或取消了
     */
    private ScheduledFuture<?> cancelFuture;

    /**
     * 来自对等端的关闭代码，如果此web套接字尚未读取关闭帧，则为-1
     */
    private int receivedCloseCode = -1;

    /**
     * 来自对等方的关闭原因，如果此web套接字尚未读取关闭帧，则为null
     */
    private String receivedCloseReason;

    /**
     * 如果此web套接字失败且侦听器已被通知，则为
     */
    private boolean failed;

    /**
     * 此web套接字发送的ping的总数
     */
    private int sentPingCount;

    /**
     * 此web套接字接收的ping的总数
     */
    private int receivedPingCount;

    /**
     * 此web套接字接收的ping总数
     */
    private int receivedPongCount;

    /**
     * 如果我们发送了一个仍在等待回复的ping，则为真
     */
    private boolean awaitingPong;

    public RealWebSocket(Request request, WebSocketListener listener, Random random,
                         long pingIntervalMillis) {
        if (!Http.GET.equals(request.method())) {
            throw new IllegalArgumentException("Request must be GET: " + request.method());
        }
        this.originalRequest = request;
        this.listener = listener;
        this.random = random;
        this.pingIntervalMillis = pingIntervalMillis;

        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        this.key = ByteString.of(nonce).base64();

        this.writerRunnable = () -> {
            try {
                while (writeOneFrame()) {
                }
            } catch (IOException e) {
                failWebSocket(e, null);
            }
        };
    }

    @Override
    public Request request() {
        return originalRequest;
    }

    @Override
    public synchronized long queueSize() {
        return queueSize;
    }

    @Override
    public void cancel() {
        call.cancel();
    }

    public void connect(Httpd client) {
        client = client.newBuilder()
                .eventListener(EventListener.NONE)
                .protocols(ONLY_HTTP1)
                .build();
        final Request request = originalRequest.newBuilder()
                .header(Header.UPGRADE, "websocket")
                .header(Header.CONNECTION, Header.UPGRADE)
                .header(Header.SEC_WEBSOCKET_KEY, key)
                .header(Header.SEC_WEBSOCKET_VERSION, "13")
                .build();
        call = Builder.instance.newWebSocketCall(client, request);
        call.timeout().clearTimeout();
        call.enqueue(new Callback() {
            @Override
            public void onResponse(NewCall call, Response response) {
                try {
                    checkResponse(response);
                } catch (ProtocolException e) {
                    failWebSocket(e, response);
                    IoKit.close(response);
                    return;
                }

                // 将HTTP流提升为web套接字流.
                StreamAllocation streamAllocation = Builder.instance.streamAllocation(call);
                streamAllocation.noNewStreams(); // Prevent connection pooling!
                Streams streams = streamAllocation.connection().newWebSocketStreams(streamAllocation);

                // 处理所有web套接字消息.
                try {
                    listener.onOpen(RealWebSocket.this, response);
                    String name = "Httpd WebSocket " + request.url().redact();
                    initReaderAndWriter(name, streams);
                    streamAllocation.connection().socket().setSoTimeout(0);
                    loopReader();
                } catch (Exception e) {
                    failWebSocket(e, null);
                }
            }

            @Override
            public void onFailure(NewCall call, IOException e) {
                failWebSocket(e, null);
            }
        });
    }

    void checkResponse(Response response) throws ProtocolException {
        if (response.code() != 101) {
            throw new ProtocolException("Expected HTTP 101 response but was '"
                    + response.code() + Symbol.SPACE + response.message() + Symbol.SINGLE_QUOTE);
        }

        String headerConnection = response.header("Connection");
        if (!"Upgrade".equalsIgnoreCase(headerConnection)) {
            throw new ProtocolException("Expected 'Connection' header value 'Upgrade' but was '"
                    + headerConnection + Symbol.SINGLE_QUOTE);
        }

        String headerUpgrade = response.header("Upgrade");
        if (!"websocket".equalsIgnoreCase(headerUpgrade)) {
            throw new ProtocolException(
                    "Expected 'Upgrade' header value 'websocket' but was '" + headerUpgrade + Symbol.SINGLE_QUOTE);
        }

        String headerAccept = response.header("Sec-WebSocket-Accept");
        String acceptExpected = ByteString.encodeUtf8(key + WebSocketProtocol.ACCEPT_MAGIC)
                .sha1().base64();
        if (!acceptExpected.equals(headerAccept)) {
            throw new ProtocolException("Expected 'Sec-WebSocket-Accept' header value '"
                    + acceptExpected + "' but was '" + headerAccept + Symbol.SINGLE_QUOTE);
        }
    }

    public void initReaderAndWriter(String name, Streams streams) {
        synchronized (this) {
            this.streams = streams;
            this.writer = new WebSocketWriter(streams.client, streams.sink, random);
            this.executor = new ScheduledThreadPoolExecutor(1, Builder.threadFactory(name, false));
            if (pingIntervalMillis != 0) {
                executor.scheduleAtFixedRate(
                        new PingRunnable(), pingIntervalMillis, pingIntervalMillis, TimeUnit.MILLISECONDS);
            }
            if (!messageAndCloseQueue.isEmpty()) {
                //发送在我们连接之前排队的消息
                runWriter();
            }
        }

        reader = new WebSocketReader(streams.client, streams.source, this);
    }

    public void loopReader() throws IOException {
        while (receivedCloseCode == -1) {
            reader.processNextFrame();
        }
    }

    boolean processNextFrame() {
        try {
            reader.processNextFrame();
            return receivedCloseCode == -1;
        } catch (Exception e) {
            failWebSocket(e, null);
            return false;
        }
    }

    void awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException {
        executor.awaitTermination(timeout, timeUnit);
    }

    void tearDown() throws InterruptedException {
        if (cancelFuture != null) {
            cancelFuture.cancel(false);
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    synchronized int sentPingCount() {
        return sentPingCount;
    }

    synchronized int receivedPingCount() {
        return receivedPingCount;
    }

    synchronized int receivedPongCount() {
        return receivedPongCount;
    }

    @Override
    public void onReadMessage(String text) {
        listener.onMessage(this, text);
    }

    @Override
    public void onReadMessage(ByteString bytes) {
        listener.onMessage(this, bytes);
    }

    @Override
    public synchronized void onReadPing(ByteString payload) {
        if (failed || (enqueuedClose && messageAndCloseQueue.isEmpty())) return;

        pongQueue.add(payload);
        runWriter();
        receivedPingCount++;
    }

    @Override
    public synchronized void onReadPong(ByteString buffer) {
        receivedPongCount++;
        awaitingPong = false;
    }

    @Override
    public void onReadClose(int code, String reason) {
        if (code == -1) throw new IllegalArgumentException();

        Streams toClose = null;
        synchronized (this) {
            if (receivedCloseCode != -1) throw new IllegalStateException("already closed");
            receivedCloseCode = code;
            receivedCloseReason = reason;
            if (enqueuedClose && messageAndCloseQueue.isEmpty()) {
                toClose = this.streams;
                this.streams = null;
                if (cancelFuture != null) cancelFuture.cancel(false);
                this.executor.shutdown();
            }
        }

        try {
            listener.onClosing(this, code, reason);

            if (toClose != null) {
                listener.onClosed(this, code, reason);
            }
        } finally {
            IoKit.close(toClose);
        }
    }

    @Override
    public boolean send(String text) {
        if (text == null) throw new NullPointerException("text == null");
        return send(ByteString.encodeUtf8(text), WebSocketProtocol.OPCODE_TEXT);
    }

    @Override
    public boolean send(ByteString bytes) {
        if (bytes == null) throw new NullPointerException("bytes == null");
        return send(bytes, WebSocketProtocol.OPCODE_BINARY);
    }

    private synchronized boolean send(ByteString data, int formatOpcode) {
        // 不要发送新的帧后，我们已经失败或排队关闭的帧.
        if (failed || enqueuedClose) return false;

        // 如果此帧溢出缓冲区，则拒绝它并关闭web套接字.
        if (queueSize + data.size() > MAX_QUEUE_SIZE) {
            close(WebSocketProtocol.CLOSE_CLIENT_GOING_AWAY, null);
            return false;
        }

        // 对消息帧进行排队.
        queueSize += data.size();
        messageAndCloseQueue.add(new Message(formatOpcode, data));
        runWriter();
        return true;
    }

    synchronized boolean pong(ByteString payload) {
        if (failed || (enqueuedClose && messageAndCloseQueue.isEmpty())) return false;

        pongQueue.add(payload);
        runWriter();
        return true;
    }

    @Override
    public boolean close(int code, String reason) {
        return close(code, reason, CANCEL_AFTER_CLOSE_MILLIS);
    }

    synchronized boolean close(int code, String reason, long cancelAfterCloseMillis) {
        WebSocketProtocol.validateCloseCode(code);

        ByteString reasonBytes = null;
        if (reason != null) {
            reasonBytes = ByteString.encodeUtf8(reason);
            if (reasonBytes.size() > WebSocketProtocol.CLOSE_MESSAGE_MAX) {
                throw new IllegalArgumentException("reason.size() > " + WebSocketProtocol.CLOSE_MESSAGE_MAX + ": " + reason);
            }
        }

        if (failed || enqueuedClose) return false;

        enqueuedClose = true;

        messageAndCloseQueue.add(new Close(code, reasonBytes, cancelAfterCloseMillis));
        runWriter();
        return true;
    }

    private void runWriter() {
        assert (Thread.holdsLock(this));

        if (executor != null) {
            executor.execute(writerRunnable);
        }
    }

    /**
     * 尝试从队列中删除单个帧并发送它。这种写法更倾向于在不太紧急的信息和较短的框架前
     * 例如，可能调用者将对后面跟着ping的消息进行排队，但这会发送后面跟着消息的ping
     * 如果无法发送帧因为没有任何帧进入队列，或者因为web套接字没有连接不执行任何操作并返回false。
     * 否则，该方法将返回true，调用者应立即再次调用该方法，直到它返回false为止.
     * 此方法只能由写线程调用。一次可能只有一个线程调用这个方法
     *
     * @return the true/false
     * @throws IOException 异常信息
     */
    boolean writeOneFrame() throws IOException {
        WebSocketWriter writer;
        ByteString pong;
        Object messageOrClose = null;
        int receivedCloseCode = -1;
        String receivedCloseReason = null;
        Streams streamsToClose = null;

        synchronized (RealWebSocket.this) {
            if (failed) {
                return false;
            }

            writer = this.writer;
            pong = pongQueue.poll();
            if (pong == null) {
                messageOrClose = messageAndCloseQueue.poll();
                if (messageOrClose instanceof Close) {
                    receivedCloseCode = this.receivedCloseCode;
                    receivedCloseReason = this.receivedCloseReason;
                    if (receivedCloseCode != -1) {
                        streamsToClose = this.streams;
                        this.streams = null;
                        this.executor.shutdown();
                    } else {
                        // 当我们请求一个优雅的关闭，也计划取消websocket.
                        cancelFuture = executor.schedule(new CancelRunnable(),
                                ((Close) messageOrClose).cancelAfterCloseMillis, TimeUnit.MILLISECONDS);
                    }
                } else if (messageOrClose == null) {
                    // 队列已满
                    return false;
                }
            }
        }

        try {
            if (pong != null) {
                writer.writePong(pong);

            } else if (messageOrClose instanceof Message) {
                ByteString data = ((Message) messageOrClose).data;
                BufferSink sink = IoKit.buffer(writer.newMessageSink(
                        ((Message) messageOrClose).formatOpcode, data.size()));
                sink.write(data);
                sink.close();
                synchronized (this) {
                    queueSize -= data.size();
                }

            } else if (messageOrClose instanceof Close) {
                Close close = (Close) messageOrClose;
                writer.writeClose(close.code, close.reason);

                // 我们关闭了writer:现在reader和writer都关闭了.
                if (streamsToClose != null) {
                    listener.onClosed(this, receivedCloseCode, receivedCloseReason);
                }

            } else {
                throw new AssertionError();
            }

            return true;
        } finally {
            IoKit.close(streamsToClose);
        }
    }

    void writePingFrame() {
        WebSocketWriter writer;
        int failedPing;
        synchronized (this) {
            if (failed) return;
            writer = this.writer;
            failedPing = awaitingPong ? sentPingCount : -1;
            sentPingCount++;
            awaitingPong = true;
        }

        if (failedPing != -1) {
            failWebSocket(new SocketTimeoutException("sent ping but didn't receive pong within "
                            + pingIntervalMillis + "ms (after " + (failedPing - 1) + " successful ping/pongs)"),
                    null);
            return;
        }

        try {
            writer.writePing(ByteString.EMPTY);
        } catch (IOException e) {
            failWebSocket(e, null);
        }
    }

    public void failWebSocket(Exception e, Response response) {
        Streams streamsToClose;
        synchronized (this) {
            if (failed) return; // Already failed.
            failed = true;
            streamsToClose = this.streams;
            this.streams = null;
            if (cancelFuture != null) cancelFuture.cancel(false);
            if (executor != null) executor.shutdown();
        }

        try {
            listener.onFailure(this, e, response);
        } finally {
            IoKit.close(streamsToClose);
        }
    }

    static final class Message {
        final int formatOpcode;
        final ByteString data;

        Message(int formatOpcode, ByteString data) {
            this.formatOpcode = formatOpcode;
            this.data = data;
        }
    }

    static final class Close {
        final int code;
        final ByteString reason;
        final long cancelAfterCloseMillis;

        Close(int code, ByteString reason, long cancelAfterCloseMillis) {
            this.code = code;
            this.reason = reason;
            this.cancelAfterCloseMillis = cancelAfterCloseMillis;
        }
    }

    public abstract static class Streams implements Closeable {
        public final boolean client;
        public final BufferSource source;
        public final BufferSink sink;

        public Streams(boolean client, BufferSource source, BufferSink sink) {
            this.client = client;
            this.source = source;
            this.sink = sink;
        }
    }

    private final class PingRunnable implements Runnable {
        PingRunnable() {
        }

        @Override
        public void run() {
            writePingFrame();
        }
    }

    final class CancelRunnable implements Runnable {
        @Override
        public void run() {
            cancel();
        }
    }

}
