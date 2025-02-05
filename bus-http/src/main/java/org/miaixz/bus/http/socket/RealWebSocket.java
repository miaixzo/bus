/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org and other contributors.                    ~
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
package org.miaixz.bus.http.socket;

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

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.io.sink.BufferSink;
import org.miaixz.bus.core.io.source.BufferSource;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.core.net.Protocol;
import org.miaixz.bus.core.xyz.IoKit;
import org.miaixz.bus.http.*;
import org.miaixz.bus.http.accord.Exchange;
import org.miaixz.bus.http.metric.EventListener;
import org.miaixz.bus.http.metric.Internal;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealWebSocket implements WebSocket, WebSocketReader.FrameCallback {

    private static final List<Protocol> ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);

    /**
     * 要加入队列的最大字节数。而不是排队超过这个限制，我们拆掉web套接字!有可能我们写得比别人读得快 16 MiB
     */
    private static final long MAX_QUEUE_SIZE = Normal._16 * Normal._1024 * Normal._1024;

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
     * 此web套接字持有的流。在读取所有传入消息和写入所有传出消息之前，这是非空的 当读者和作者都精疲力尽，或者出现任何失败时，它就关闭了
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

    public RealWebSocket(Request request, WebSocketListener listener, Random random, long pingIntervalMillis) {
        if (!HTTP.GET.equals(request.method())) {
            throw new IllegalArgumentException("Request must be GET: " + request.method());
        }
        this.originalRequest = request;
        this.listener = listener;
        this.random = random;
        this.pingIntervalMillis = pingIntervalMillis;

        byte[] nonce = new byte[Normal._16];
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
        client = client.newBuilder().eventListener(EventListener.NONE).protocols(ONLY_HTTP1).build();
        final Request request = originalRequest.newBuilder().header(HTTP.UPGRADE, "websocket")
                .header(HTTP.CONNECTION, HTTP.UPGRADE).header(HTTP.SEC_WEBSOCKET_KEY, key)
                .header(HTTP.SEC_WEBSOCKET_VERSION, "13").build();
        call = Internal.instance.newWebSocketCall(client, request);
        call.enqueue(new Callback() {
            @Override
            public void onResponse(NewCall call, Response response) {
                Exchange exchange = Internal.instance.exchange(response);
                Streams streams;
                try {
                    checkUpgradeSuccess(response, exchange);
                    streams = exchange.newWebSocketStreams();
                } catch (IOException e) {
                    if (exchange != null)
                        exchange.webSocketUpgradeFailed();
                    failWebSocket(e, response);
                    IoKit.close(response);
                    return;
                }

                // Process all web socket messages.
                try {
                    String name = "WebSocket " + request.url().redact();
                    initReaderAndWriter(name, streams);
                    listener.onOpen(RealWebSocket.this, response);
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

    void checkUpgradeSuccess(Response response, Exchange exchange) throws IOException {
        if (response.code() != 101) {
            throw new ProtocolException("Expected HTTP 101 response but was '" + response.code() + Symbol.SPACE
                    + response.message() + Symbol.SINGLE_QUOTE);
        }

        String headerConnection = response.header(HTTP.CONNECTION);
        if (!HTTP.UPGRADE.equalsIgnoreCase(headerConnection)) {
            throw new ProtocolException(
                    "Expected 'Connection' header value 'Upgrade' but was '" + headerConnection + Symbol.SINGLE_QUOTE);
        }

        String headerUpgrade = response.header(HTTP.UPGRADE);
        if (!"websocket".equalsIgnoreCase(headerUpgrade)) {
            throw new ProtocolException(
                    "Expected 'Upgrade' header value 'websocket' but was '" + headerUpgrade + Symbol.SINGLE_QUOTE);
        }

        String headerAccept = response.header(HTTP.SEC_WEBSOCKET_ACCEPT);
        String acceptExpected = ByteString.encodeUtf8(key + WebSocketProtocol.ACCEPT_MAGIC).sha1().base64();
        if (!acceptExpected.equals(headerAccept)) {
            throw new ProtocolException("Expected 'Sec-WebSocket-Accept' header value '" + acceptExpected
                    + "' but was '" + headerAccept + "'");
        }

        if (exchange == null) {
            throw new ProtocolException("Web Socket exchange missing: bad interceptor?");
        }
    }

    public void initReaderAndWriter(String name, Streams streams) {
        synchronized (this) {
            this.streams = streams;
            this.writer = new WebSocketWriter(streams.client, streams.sink, random);
            this.executor = new ScheduledThreadPoolExecutor(1, Builder.threadFactory(name, false));
            if (pingIntervalMillis != 0) {
                executor.scheduleAtFixedRate(new PingRunnable(), pingIntervalMillis, pingIntervalMillis,
                        TimeUnit.MILLISECONDS);
            }
            if (!messageAndCloseQueue.isEmpty()) {
                runWriter(); // Send messages that were enqueued before we were connected.
            }
        }

        reader = new WebSocketReader(streams.client, streams.source, this);
    }

    /**
     * Receive frames until there are no more. Invoked only by the reader thread.
     */
    public void loopReader() throws IOException {
        while (receivedCloseCode == -1) {
            // This method call results in one or more onRead* methods being called on this thread.
            reader.processNextFrame();
        }
    }

    /**
     * For testing: receive a single frame and return true if there are more frames to read. Invoked only by the reader
     * thread.
     */
    boolean processNextFrame() {
        try {
            reader.processNextFrame();
            return receivedCloseCode == -1;
        } catch (Exception e) {
            failWebSocket(e, null);
            return false;
        }
    }

    /**
     * For testing: wait until the web socket's executor has terminated.
     */
    void awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException {
        executor.awaitTermination(timeout, timeUnit);
    }

    /**
     * For testing: force this web socket to release its threads.
     */
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
        // Don't respond to pings after we've failed or sent the close frame.
        if (failed || (enqueuedClose && messageAndCloseQueue.isEmpty()))
            return;

        pongQueue.add(payload);
        runWriter();
        receivedPingCount++;
    }

    @Override
    public synchronized void onReadPong(ByteString buffer) {
        // This API doesn't expose pings.
        receivedPongCount++;
        awaitingPong = false;
    }

    @Override
    public void onReadClose(int code, String reason) {
        if (code == -1)
            throw new IllegalArgumentException();

        Streams toClose = null;
        synchronized (this) {
            if (receivedCloseCode != -1)
                throw new IllegalStateException("already closed");
            receivedCloseCode = code;
            receivedCloseReason = reason;
            if (enqueuedClose && messageAndCloseQueue.isEmpty()) {
                toClose = this.streams;
                this.streams = null;
                if (null != cancelFuture)
                    cancelFuture.cancel(false);
                this.executor.shutdown();
            }
        }

        try {
            listener.onClosing(this, code, reason);

            if (null != toClose) {
                listener.onClosed(this, code, reason);
            }
        } finally {
            IoKit.close(toClose);
        }
    }

    // Writer methods to enqueue frames. They'll be sent asynchronously by the writer thread.

    @Override
    public boolean send(String text) {
        if (text == null)
            throw new NullPointerException("text == null");
        return send(ByteString.encodeUtf8(text), WebSocketProtocol.OPCODE_TEXT);
    }

    @Override
    public boolean send(ByteString bytes) {
        if (bytes == null)
            throw new NullPointerException("bytes == null");
        return send(bytes, WebSocketProtocol.OPCODE_BINARY);
    }

    private synchronized boolean send(ByteString data, int formatOpcode) {
        // Don't send new frames after we've failed or enqueued a close frame.
        if (failed || enqueuedClose)
            return false;

        // If this frame overflows the buffer, reject it and close the web socket.
        if (queueSize + data.size() > MAX_QUEUE_SIZE) {
            close(WebSocketProtocol.CLOSE_CLIENT_GOING_AWAY, null);
            return false;
        }

        // Enqueue the message frame.
        queueSize += data.size();
        messageAndCloseQueue.add(new Message(formatOpcode, data));
        runWriter();
        return true;
    }

    synchronized boolean pong(ByteString payload) {
        // Don't send pongs after we've failed or sent the close frame.
        if (failed || (enqueuedClose && messageAndCloseQueue.isEmpty()))
            return false;

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
        if (null != reason) {
            reasonBytes = ByteString.encodeUtf8(reason);
            if (reasonBytes.size() > WebSocketProtocol.CLOSE_MESSAGE_MAX) {
                throw new IllegalArgumentException(
                        "reason.size() > " + WebSocketProtocol.CLOSE_MESSAGE_MAX + ": " + reason);
            }
        }

        if (failed || enqueuedClose)
            return false;

        // Immediately prevent further frames from being enqueued.
        enqueuedClose = true;

        // Enqueue the close frame.
        messageAndCloseQueue.add(new Close(code, reasonBytes, cancelAfterCloseMillis));
        runWriter();
        return true;
    }

    private void runWriter() {
        assert (Thread.holdsLock(this));

        if (null != executor) {
            executor.execute(writerRunnable);
        }
    }

    /**
     * 尝试从队列中删除单个帧并发送它。这种写法更倾向于在不太紧急的信息和较短的框架前 例如，可能调用者将对后面跟着ping的消息进行排队，但这会发送后面跟着消息的ping
     * 如果无法发送帧因为没有任何帧进入队列，或者因为web套接字没有连接不执行任何操作并返回false。 否则，该方法将返回true，调用者应立即再次调用该方法，直到它返回false为止.
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
            if (null == pong) {
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
                } else if (null == messageOrClose) {
                    // 队列已满
                    return false;
                }
            }
        }

        try {
            if (null != pong) {
                writer.writePong(pong);

            } else if (messageOrClose instanceof Message) {
                ByteString data = ((Message) messageOrClose).data;
                BufferSink sink = IoKit
                        .buffer(writer.newMessageSink(((Message) messageOrClose).formatOpcode, data.size()));
                sink.write(data);
                sink.close();
                synchronized (this) {
                    queueSize -= data.size();
                }

            } else if (messageOrClose instanceof Close) {
                Close close = (Close) messageOrClose;
                writer.writeClose(close.code, close.reason);

                // 我们关闭了writer:现在reader和writer都关闭了.
                if (null != streamsToClose) {
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
            if (failed)
                return;
            writer = this.writer;
            failedPing = awaitingPong ? sentPingCount : -1;
            sentPingCount++;
            awaitingPong = true;
        }

        if (failedPing != -1) {
            failWebSocket(new SocketTimeoutException("sent ping but didn't receive pong within " + pingIntervalMillis
                    + "ms (after " + (failedPing - 1) + " successful ping/pongs)"), null);
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
            if (failed)
                return; // Already failed.
            failed = true;
            streamsToClose = this.streams;
            this.streams = null;
            if (null != cancelFuture)
                cancelFuture.cancel(false);
            if (null != executor)
                executor.shutdown();
        }

        try {
            listener.onFailure(this, e, response);
        } finally {
            IoKit.close(streamsToClose);
        }
    }

    static class Message {
        final int formatOpcode;
        final ByteString data;

        Message(int formatOpcode, ByteString data) {
            this.formatOpcode = formatOpcode;
            this.data = data;
        }
    }

    static class Close {
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

    private class PingRunnable implements Runnable {
        PingRunnable() {
        }

        @Override
        public void run() {
            writePingFrame();
        }
    }

    class CancelRunnable implements Runnable {
        @Override
        public void run() {
            cancel();
        }
    }

}
