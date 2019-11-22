/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.http.socket;

import org.aoju.bus.core.io.segment.BufferSink;
import org.aoju.bus.core.io.segment.BufferSource;
import org.aoju.bus.core.io.segment.ByteString;
import org.aoju.bus.core.utils.IoUtils;
import org.aoju.bus.http.*;
import org.aoju.bus.http.accord.StreamAllocation;
import org.aoju.bus.http.offers.EventListener;

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
 * @version 5.2.3
 * @since JDK 1.8+
 */
public final class RealWebSocket implements WebSocket, WebSocketReader.FrameCallback {

    private static final List<Protocol> ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);

    /**
     * The maximum number of bytes to enqueue. Rather than enqueueing beyond this limit we tear down
     * the web socket! It's possible that we're writing faster than the peer can read.
     */
    private static final long MAX_QUEUE_SIZE = 16 * 1024 * 1024; // 16 MiB.

    /**
     * The maximum amount of time after the client calls {@link #close} to wait for a graceful
     * shutdown. If the server doesn't respond the websocket will be canceled.
     */
    private static final long CANCEL_AFTER_CLOSE_MILLIS = 60 * 1000;
    final SocketListener listener;
    /**
     * The application's original request unadulterated by web socket headers.
     */
    private final Request originalRequest;
    private final Random random;
    private final long pingIntervalMillis;
    private final String key;
    /**
     * This runnable processes the outgoing queues. Call {@link #runWriter()} to after enqueueing.
     */
    private final Runnable writerRunnable;
    /**
     * Outgoing pongs in the order they should be written.
     */
    private final ArrayDeque<ByteString> pongQueue = new ArrayDeque<>();
    /**
     * Outgoing messages and close frames in the order they should be written.
     */
    private final ArrayDeque<Object> messageAndCloseQueue = new ArrayDeque<>();

    // All mutable web socket state is guarded by this.
    /**
     * Non-null for client web sockets. These can be canceled.
     */
    private Call call;
    /**
     * Null until this web socket is connected. Only accessed by the reader thread.
     */
    private WebSocketReader reader;
    /**
     * Null until this web socket is connected. Note that messages may be enqueued before that.
     */
    private WebSocketWriter writer;
    /**
     * Null until this web socket is connected. Used for writes, pings, and close timeouts.
     */
    private ScheduledExecutorService executor;
    /**
     * The streams held by this web socket. This is non-null until all incoming messages have been
     * read and all outgoing messages have been written. It is closed when both reader and writer are
     * exhausted, or if there is any failure.
     */
    private Streams streams;
    /**
     * The total size in bytes of enqueued but not yet transmitted messages.
     */
    private long queueSize;

    /**
     * True if we've enqueued a close frame. No further message frames will be enqueued.
     */
    private boolean enqueuedClose;

    /**
     * When executed this will cancel this websocket. This future itself should be canceled if that is
     * unnecessary because the web socket is already closed or canceled.
     */
    private ScheduledFuture<?> cancelFuture;

    /**
     * The close code from the peer, or -1 if this web socket has not yet read a close frame.
     */
    private int receivedCloseCode = -1;

    /**
     * The close reason from the peer, or null if this web socket has not yet read a close frame.
     */
    private String receivedCloseReason;

    /**
     * True if this web socket failed and the listener has been notified.
     */
    private boolean failed;

    /**
     * Total number of pings sent by this web socket.
     */
    private int sentPingCount;

    /**
     * Total number of pings received by this web socket.
     */
    private int receivedPingCount;

    /**
     * Total number of pongs received by this web socket.
     */
    private int receivedPongCount;

    /**
     * True if we have sent a ping that is still awaiting a reply.
     */
    private boolean awaitingPong;

    public RealWebSocket(Request request, SocketListener listener, Random random,
                         long pingIntervalMillis) {
        if (!"GET".equals(request.method())) {
            throw new IllegalArgumentException("Request must be GET: " + request.method());
        }
        this.originalRequest = request;
        this.listener = listener;
        this.random = random;
        this.pingIntervalMillis = pingIntervalMillis;

        byte[] nonce = new byte[16];
        random.nextBytes(nonce);
        this.key = ByteString.of(nonce).base64();

        this.writerRunnable = new Runnable() {
            @Override
            public void run() {
                try {
                    while (writeOneFrame()) {
                    }
                } catch (IOException e) {
                    failWebSocket(e, null);
                }
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

    public void connect(Client client) {
        client = client.newBuilder()
                .eventListener(EventListener.NONE)
                .protocols(ONLY_HTTP1)
                .build();
        final Request request = originalRequest.newBuilder()
                .header("Upgrade", "websocket")
                .header("Connection", "Upgrade")
                .header("Sec-WebSocket-Key", key)
                .header("Sec-WebSocket-Version", "13")
                .build();
        call = Internal.instance.newWebSocketCall(client, request);
        call.timeout().clearTimeout();
        call.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                try {
                    checkResponse(response);
                } catch (ProtocolException e) {
                    failWebSocket(e, response);
                    Internal.closeQuietly(response);
                    return;
                }

                // Promote the HTTP streams into web socket streams.
                StreamAllocation streamAllocation = Internal.instance.streamAllocation(call);
                streamAllocation.noNewStreams(); // Prevent connection pooling!
                Streams streams = streamAllocation.connection().newWebSocketStreams(streamAllocation);

                // Process all web socket messages.
                try {
                    listener.onOpen(RealWebSocket.this, response);
                    String name = "httpClient WebSocket " + request.url().redact();
                    initReaderAndWriter(name, streams);
                    streamAllocation.connection().socket().setSoTimeout(0);
                    loopReader();
                } catch (Exception e) {
                    failWebSocket(e, null);
                }
            }

            @Override
            public void onFailure(Call call, IOException e) {
                failWebSocket(e, null);
            }
        });
    }

    void checkResponse(Response response) throws ProtocolException {
        if (response.code() != 101) {
            throw new ProtocolException("Expected HTTP 101 response but was '"
                    + response.code() + " " + response.message() + "'");
        }

        String headerConnection = response.header("Connection");
        if (!"Upgrade".equalsIgnoreCase(headerConnection)) {
            throw new ProtocolException("Expected 'Connection' header value 'Upgrade' but was '"
                    + headerConnection + "'");
        }

        String headerUpgrade = response.header("Upgrade");
        if (!"websocket".equalsIgnoreCase(headerUpgrade)) {
            throw new ProtocolException(
                    "Expected 'Upgrade' header value 'websocket' but was '" + headerUpgrade + "'");
        }

        String headerAccept = response.header("Sec-WebSocket-Accept");
        String acceptExpected = ByteString.encodeUtf8(key + WebSocketProtocol.ACCEPT_MAGIC)
                .sha1().base64();
        if (!acceptExpected.equals(headerAccept)) {
            throw new ProtocolException("Expected 'Sec-WebSocket-Accept' header value '"
                    + acceptExpected + "' but was '" + headerAccept + "'");
        }
    }

    public void initReaderAndWriter(String name, Streams streams) throws IOException {
        synchronized (this) {
            this.streams = streams;
            this.writer = new WebSocketWriter(streams.client, streams.sink, random);
            this.executor = new ScheduledThreadPoolExecutor(1, Internal.threadFactory(name, false));
            if (pingIntervalMillis != 0) {
                executor.scheduleAtFixedRate(
                        new PingRunnable(), pingIntervalMillis, pingIntervalMillis, TimeUnit.MILLISECONDS);
            }
            if (!messageAndCloseQueue.isEmpty()) {
                runWriter(); // Send messages that were enqueued before we were connected.
            }
        }

        reader = new WebSocketReader(streams.client, streams.source, this);
    }

    public void loopReader() throws IOException {
        while (receivedCloseCode == -1) {
            // This method call results in first or more onRead* methods being called on this thread.
            reader.processNextFrame();
        }
    }

    boolean processNextFrame() throws IOException {
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
    public void onReadMessage(String text) throws IOException {
        listener.onMessage(this, text);
    }

    @Override
    public void onReadMessage(ByteString bytes) throws IOException {
        listener.onMessage(this, bytes);
    }

    @Override
    public synchronized void onReadPing(ByteString payload) {
        // Don't respond to pings after we've failed or sent the close frame.
        if (failed || (enqueuedClose && messageAndCloseQueue.isEmpty())) return;

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
            Internal.closeQuietly(toClose);
        }
    }

    // Writer methods to enqueue frames. They'll be sent asynchronously by the writer thread.

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
        // Don't send new frames after we've failed or enqueued a close frame.
        if (failed || enqueuedClose) return false;

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

        // Immediately prevent further frames from being enqueued.
        enqueuedClose = true;

        // Enqueue the close frame.
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

    boolean writeOneFrame() throws IOException {
        WebSocketWriter writer;
        ByteString pong;
        Object messageOrClose = null;
        int receivedCloseCode = -1;
        String receivedCloseReason = null;
        Streams streamsToClose = null;

        synchronized (RealWebSocket.this) {
            if (failed) {
                return false; // Failed web socket.
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
                        // When we request a graceful close also schedule a cancel of the websocket.
                        cancelFuture = executor.schedule(new CancelRunnable(),
                                ((Close) messageOrClose).cancelAfterCloseMillis, TimeUnit.MILLISECONDS);
                    }
                } else if (messageOrClose == null) {
                    return false; // The queue is exhausted.
                }
            }
        }

        try {
            if (pong != null) {
                writer.writePong(pong);

            } else if (messageOrClose instanceof Message) {
                ByteString data = ((Message) messageOrClose).data;
                BufferSink sink = IoUtils.buffer(writer.newMessageSink(
                        ((Message) messageOrClose).formatOpcode, data.size()));
                sink.write(data);
                sink.close();
                synchronized (this) {
                    queueSize -= data.size();
                }

            } else if (messageOrClose instanceof Close) {
                Close close = (Close) messageOrClose;
                writer.writeClose(close.code, close.reason);

                // We closed the writer: now both reader and writer are closed.
                if (streamsToClose != null) {
                    listener.onClosed(this, receivedCloseCode, receivedCloseReason);
                }

            } else {
                throw new AssertionError();
            }

            return true;
        } finally {
            Internal.closeQuietly(streamsToClose);
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
            Internal.closeQuietly(streamsToClose);
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
