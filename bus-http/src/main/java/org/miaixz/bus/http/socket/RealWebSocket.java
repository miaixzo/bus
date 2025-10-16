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
 * An implementation of the WebSocket protocol (RFC 6455). This class manages the lifecycle of a WebSocket connection,
 * including message queuing, sending and receiving frames, handling ping/pong, and performing a graceful shutdown. It
 * uses a listener to notify of events.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public final class RealWebSocket implements WebSocket, WebSocketReader.FrameCallback {

    /**
     * WebSocket connections require HTTP/1.1 to perform the upgrade.
     */
    private static final List<Protocol> ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);
    /**
     * The maximum number of bytes to enqueue before trigger a graceful shutdown.
     */
    private static final long MAX_QUEUE_SIZE = Normal._16 * Normal._1024 * Normal._1024; // 16 MiB.
    /**
     *
     * The maximum time to wait for the server to close the connection after we send a close frame.
     */
    private static final long CANCEL_AFTER_CLOSE_MILLIS = 60 * 1000;

    /**
     * The listener for WebSocket events.
     */
    final WebSocketListener listener;
    /**
     * The original HTTP request that initiated this WebSocket.
     */
    private final Request originalRequest;
    /**
     * A source of random data for generating the client key and masking frames.
     */
    private final Random random;
    /**
     * The interval for sending pings, in milliseconds. 0 means no pings.
     */
    private final long pingIntervalMillis;
    /**
     * The client-generated key for the WebSocket handshake.
     */
    private final String key;
    /**
     * The task that writes frames to the network.
     */
    private final Runnable writerRunnable;

    /**
     * A queue of pong frames to be sent.
     */
    private final ArrayDeque<ByteString> pongQueue = new ArrayDeque<>();
    /**
     * A queue of message and close frames to be sent.
     */
    private final ArrayDeque<Object> messageAndCloseQueue = new ArrayDeque<>();
    /**
     * The underlying HTTP call.
     */
    private NewCall call;
    /**
     * The reader for incoming frames.
     */
    private WebSocketReader reader;
    /**
     * The writer for outgoing frames.
     */
    private WebSocketWriter writer;
    /**
     * The executor for sending pings and canceling the connection.
     */
    private ScheduledExecutorService executor;
    /**
     * The combined sink and source for the connection. Null until the connection is established.
     */
    private Streams streams;
    /**
     * The total size in bytes of enqueued messages.
     */
    private long queueSize;
    /**
     * True if we have enqueued a close frame. No further messages will be accepted.
     */
    private boolean enqueuedClose;
    /**
     * A task that will cancel the connection if a graceful close doesn't complete.
     */
    private ScheduledFuture<?> cancelFuture;
    /**
     * The close code received from the peer, or -1 if none.
     */
    private int receivedCloseCode = -1;
    /**
     * The close reason received from the peer, or null if none.
     */
    private String receivedCloseReason;
    /**
     * True if this WebSocket has failed.
     */
    private boolean failed;
    /**
     * The number of pings we have sent.
     */
    private int sentPingCount;
    /**
     * The number of pings we have received.
     */
    private int receivedPingCount;
    /**
     * The number of pongs we have received.
     */
    private int receivedPongCount;
    /**
     * True if we are awaiting a pong from the peer.
     */
    private boolean awaitingPong;

    /**
     * Constructs a new RealWebSocket.
     *
     * @param request            The original HTTP GET request.
     * @param listener           The listener for WebSocket events.
     * @param random             A source of randomness.
     * @param pingIntervalMillis The interval for sending pings in milliseconds, or 0 for no pings.
     * @throws IllegalArgumentException if the request method is not GET.
     */
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

    /**
     * Initiates the WebSocket connection.
     *
     * @param client The HTTP client to use for the connection.
     */
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

    /**
     * Checks if the server's response indicates a successful WebSocket upgrade.
     *
     * @param response The server's response.
     * @param exchange The exchange object.
     * @throws IOException if the upgrade was not successful.
     */
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

    /**
     * Initializes the frame reader, writer, and ping executor.
     *
     * @param name    A descriptive name for the threads.
     * @param streams The sink and source for the connection.
     */
    public void initReaderAndWriter(String name, Streams streams) {
        synchronized (this) {
            this.streams = streams;
            this.writer = new WebSocketWriter(streams.client, streams.sink, random);
            this.executor = new ScheduledThreadPoolExecutor(1, Builder.threadFactory(name, false));
            if (pingIntervalMillis != 0) {
                executor.scheduleAtFixedRate(
                        new PingRunnable(),
                        pingIntervalMillis,
                        pingIntervalMillis,
                        TimeUnit.MILLISECONDS);
            }
            if (!messageAndCloseQueue.isEmpty()) {
                runWriter(); // Send messages that were enqueued before we were connected.
            }
        }

        reader = new WebSocketReader(streams.client, streams.source, this);
    }

    /**
     * Starts the reader loop to process incoming frames.
     *
     * @throws IOException if a read error occurs.
     */
    public void loopReader() throws IOException {
        while (receivedCloseCode == -1) {
            reader.processNextFrame();
        }
    }

    /**
     * Processes a single incoming frame. (For testing).
     *
     * @return true if the connection is still open.
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
     * Awaits the termination of the executor. (For testing).
     *
     * @param timeout  The timeout duration.
     * @param timeUnit The unit of time for the timeout.
     * @throws InterruptedException if the thread is interrupted.
     */
    void awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException {
        executor.awaitTermination(timeout, timeUnit);
    }

    /**
     * Shuts down the executor and waits for termination. (For testing).
     *
     * @throws InterruptedException if the thread is interrupted.
     */
    void tearDown() throws InterruptedException {
        if (cancelFuture != null) {
            cancelFuture.cancel(false);
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * @return The number of pings sent from this client.
     */
    synchronized int sentPingCount() {
        return sentPingCount;
    }

    /**
     * @return The number of pings received by this client.
     */
    synchronized int receivedPingCount() {
        return receivedPingCount;
    }

    /**
     * @return The number of pongs received by this client.
     */
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
        if (failed || (enqueuedClose && messageAndCloseQueue.isEmpty()))
            return;

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

    /**
     * Enqueues a data frame to be sent.
     *
     * @param data         The data to send.
     * @param formatOpcode The opcode for the frame format.
     * @return true if the message was successfully enqueued.
     */
    private synchronized boolean send(ByteString data, int formatOpcode) {
        if (failed || enqueuedClose)
            return false;

        if (queueSize + data.size() > MAX_QUEUE_SIZE) {
            close(WebSocketProtocol.CLOSE_CLIENT_GOING_AWAY, null);
            return false;
        }

        queueSize += data.size();
        messageAndCloseQueue.add(new Message(formatOpcode, data));
        runWriter();
        return true;
    }

    /**
     * Enqueues a pong frame to be sent.
     *
     * @param payload The pong payload.
     * @return true if the pong was successfully enqueued.
     */
    synchronized boolean pong(ByteString payload) {
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

    /**
     * Enqueues a close frame to be sent.
     *
     * @param code                   The close code.
     * @param reason                 The close reason.
     * @param cancelAfterCloseMillis The timeout for waiting for the server to acknowledge the close.
     * @return true if the close frame was successfully enqueued.
     */
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

        enqueuedClose = true;

        messageAndCloseQueue.add(new Close(code, reasonBytes, cancelAfterCloseMillis));
        runWriter();
        return true;
    }

    /**
     * Schedules the writer task to be executed if it's not already running.
     */
    private void runWriter() {
        assert (Thread.holdsLock(this));

        if (null != executor) {
            executor.execute(writerRunnable);
        }
    }

    /**
     * Writes a single frame from the queue to the network. This method prioritizes control frames (pong, close) over
     * message frames. It is only ever called by the writer thread.
     *
     * @return true if a frame was written and more frames may be in the queue.
     * @throws IOException if a write error occurs.
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
                return false; // Fail fast.
            }

            writer = this.writer;
            pong = pongQueue.poll();
            if (null == pong) {
                messageOrClose = messageAndCloseQueue.poll();
                if (messageOrClose instanceof Close) {
                    receivedCloseCode = this.receivedCloseCode;
                    receivedCloseReason = this.receivedCloseReason;
                    if (receivedCloseCode != -1) {
                        // The peer beat us to closing. Close the connection now.
                        streamsToClose = this.streams;
                        this.streams = null;
                        this.executor.shutdown();
                    } else {
                        // We're initiating a graceful close. Schedule a cancel task just in case.
                        cancelFuture = executor.schedule(
                                new CancelRunnable(),
                                ((Close) messageOrClose).cancelAfterCloseMillis,
                                TimeUnit.MILLISECONDS);
                    }
                } else if (null == messageOrClose) {
                    return false; // The queue is empty.
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

                // If we successfully closed the writer, then the reader and writer are both closed.
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

    /**
     * Sends a ping frame to the peer. This is called by the executor.
     */
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
            failWebSocket(
                    new SocketTimeoutException("sent ping but didn't receive pong within " + pingIntervalMillis
                            + "ms (after " + (failedPing - 1) + " successful ping/pongs)"),
                    null);
            return;
        }

        try {
            writer.writePing(ByteString.EMPTY);
        } catch (IOException e) {
            failWebSocket(e, null);
        }
    }

    /**
     * Notifies the listener of a failure and closes the connection.
     *
     * @param e        The exception that caused the failure.
     * @param response The response received before the failure, or null.
     */
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

    /**
     * A data class for enqueued messages.
     */
    static final class Message {

        /**
         * The opcode of the message.
         */
        final int formatOpcode;
        /**
         * The message data.
         */
        final ByteString data;

        Message(int formatOpcode, ByteString data) {
            this.formatOpcode = formatOpcode;
            this.data = data;
        }
    }

    /**
     * A data class for enqueued close frames.
     */
    static final class Close {

        /**
         * The close code.
         */
        final int code;
        /**
         * The close reason.
         */
        final ByteString reason;
        /**
         * The timeout for canceling the connection after sending the close frame.
         */
        final long cancelAfterCloseMillis;

        Close(int code, ByteString reason, long cancelAfterCloseMillis) {
            this.code = code;
            this.reason = reason;
            this.cancelAfterCloseMillis = cancelAfterCloseMillis;
        }
    }

    /**
     * A holder for the sink and source of a WebSocket connection.
     */
    public abstract static class Streams implements Closeable {

        /**
         * True if this is a client stream.
         */
        public final boolean client;
        /**
         * The source for reading incoming data.
         */
        public final BufferSource source;
        /**
         * The sink for writing outgoing data.
         */
        public final BufferSink sink;

        public Streams(boolean client, BufferSource source, BufferSink sink) {
            this.client = client;
            this.source = source;
            this.sink = sink;
        }
    }

    /**
     * The task for sending periodic pings.
     */
    private final class PingRunnable implements Runnable {

        PingRunnable() {
        }

        @Override
        public void run() {
            writePingFrame();
        }
    }

    /**
     * The task for canceling the connection if a graceful close times out.
     */
    final class CancelRunnable implements Runnable {

        @Override
        public void run() {
            cancel();
        }
    }

}
