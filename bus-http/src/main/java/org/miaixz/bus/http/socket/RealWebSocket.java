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
 * WebSocket 客户端实现
 * <p>
 * 实现 WebSocket 协议（RFC 6455），支持消息发送、接收、ping/pong 和优雅关闭。 管理消息队列和连接生命周期，使用回调通知监听器处理事件。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class RealWebSocket implements WebSocket, WebSocketReader.FrameCallback {

    /**
     * 仅支持 HTTP/1.1 协议
     */
    private static final List<Protocol> ONLY_HTTP1 = Collections.singletonList(Protocol.HTTP_1_1);

    /**
     * 队列最大字节数（16 MiB）
     */
    private static final long MAX_QUEUE_SIZE = Normal._16 * Normal._1024 * Normal._1024;

    /**
     * 关闭后等待服务器响应的最大时间（60秒）
     */
    private static final long CANCEL_AFTER_CLOSE_MILLIS = 60 * 1000;

    /**
     * WebSocket 监听器
     */
    final WebSocketListener listener;
    /**
     * 原始请求
     */
    private final Request originalRequest;
    /**
     * 随机数生成器
     */
    private final Random random;
    /**
     * ping 间隔（毫秒）
     */
    private final long pingIntervalMillis;
    /**
     * WebSocket 密钥
     */
    private final String key;
    /**
     * 写入器运行任务
     */
    private final Runnable writerRunnable;
    /**
     * pong 队列
     */
    private final ArrayDeque<ByteString> pongQueue = new ArrayDeque<>();
    /**
     * 消息和关闭帧队列
     */
    private final ArrayDeque<Object> messageAndCloseQueue = new ArrayDeque<>();
    /**
     * WebSocket 调用
     */
    private NewCall call;
    /**
     * 帧读取器
     */
    private WebSocketReader reader;
    /**
     * 帧写入器
     */
    private WebSocketWriter writer;
    /**
     * 线程池
     */
    private ScheduledExecutorService executor;
    /**
     * 数据流
     */
    private Streams streams;
    /**
     * 队列大小
     */
    private long queueSize;
    /**
     * 是否已排队关闭
     */
    private boolean enqueuedClose;
    /**
     * 取消任务
     */
    private ScheduledFuture<?> cancelFuture;
    /**
     * 接收的关闭代码
     */
    private int receivedCloseCode = -1;
    /**
     * 接收的关闭原因
     */
    private String receivedCloseReason;
    /**
     * 是否已失败
     */
    private boolean failed;
    /**
     * 发送的 ping 计数
     */
    private int sentPingCount;
    /**
     * 接收的 ping 计数
     */
    private int receivedPingCount;
    /**
     * 接收的 pong 计数
     */
    private int receivedPongCount;
    /**
     * 是否等待 pong
     */
    private boolean awaitingPong;

    /**
     * 构造函数，初始化 WebSocket 实例
     *
     * @param request            原始请求（必须为 GET）
     * @param listener           WebSocket 监听器
     * @param random             随机数生成器
     * @param pingIntervalMillis ping 间隔（毫秒）
     * @throws IllegalArgumentException 如果请求方法不是 GET
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

    /**
     * 获取原始请求
     *
     * @return 原始请求
     */
    @Override
    public Request request() {
        return originalRequest;
    }

    /**
     * 获取队列大小
     *
     * @return 队列中的字节数
     */
    @Override
    public synchronized long queueSize() {
        return queueSize;
    }

    /**
     * 取消 WebSocket 连接
     */
    @Override
    public void cancel() {
        call.cancel();
    }

    /**
     * 建立 WebSocket 连接
     *
     * @param client HTTP 客户端
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
     * 检查 WebSocket 升级是否成功
     *
     * @param response 响应
     * @param exchange 交换对象
     * @throws IOException 如果升级失败
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
     * 初始化帧读取器和写入器
     *
     * @param name    线程名称
     * @param streams 数据流
     */
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
     * 循环读取帧
     *
     * @throws IOException 如果读取失败
     */
    public void loopReader() throws IOException {
        while (receivedCloseCode == -1) {
            // This method call results in one or more onRead* methods being called on this thread.
            reader.processNextFrame();
        }
    }

    /**
     * 处理单个帧（用于测试）
     *
     * @return true 如果还有更多帧可读
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
     * 等待线程池终止（用于测试）
     *
     * @param timeout  超时时间
     * @param timeUnit 时间单位
     * @throws InterruptedException 如果等待被中断
     */
    void awaitTermination(int timeout, TimeUnit timeUnit) throws InterruptedException {
        executor.awaitTermination(timeout, timeUnit);
    }

    /**
     * 释放线程（用于测试）
     *
     * @throws InterruptedException 如果等待被中断
     */
    void tearDown() throws InterruptedException {
        if (cancelFuture != null) {
            cancelFuture.cancel(false);
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);
    }

    /**
     * 获取发送的 ping 计数
     *
     * @return ping 计数
     */
    synchronized int sentPingCount() {
        return sentPingCount;
    }

    /**
     * 获取接收的 ping 计数
     *
     * @return ping 计数
     */
    synchronized int receivedPingCount() {
        return receivedPingCount;
    }

    /**
     * 获取接收的 pong 计数
     *
     * @return pong 计数
     */
    synchronized int receivedPongCount() {
        return receivedPongCount;
    }

    /**
     * 接收文本消息
     *
     * @param text 文本内容
     */
    @Override
    public void onReadMessage(String text) {
        listener.onMessage(this, text);
    }

    /**
     * 接收二进制消息
     *
     * @param bytes 二进制内容
     */
    @Override
    public void onReadMessage(ByteString bytes) {
        listener.onMessage(this, bytes);
    }

    /**
     * 接收 ping 帧
     *
     * @param payload ping 数据
     */
    @Override
    public synchronized void onReadPing(ByteString payload) {
        // Don't respond to pings after we've failed or sent the close frame.
        if (failed || (enqueuedClose && messageAndCloseQueue.isEmpty()))
            return;

        pongQueue.add(payload);
        runWriter();
        receivedPingCount++;
    }

    /**
     * 接收 pong 帧
     *
     * @param buffer pong 数据
     */
    @Override
    public synchronized void onReadPong(ByteString buffer) {
        // This API doesn't expose pings.
        receivedPongCount++;
        awaitingPong = false;
    }

    /**
     * 接收 close 帧
     *
     * @param code   关闭代码
     * @param reason 关闭原因
     * @throws IllegalArgumentException 如果 code 无效
     */
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

    /**
     * 发送文本消息
     *
     * @param text 文本内容
     * @return true 如果发送成功
     * @throws NullPointerException 如果 text 为 null
     */
    @Override
    public boolean send(String text) {
        if (text == null)
            throw new NullPointerException("text == null");
        return send(ByteString.encodeUtf8(text), WebSocketProtocol.OPCODE_TEXT);
    }

    /**
     * 发送二进制消息
     *
     * @param bytes 二进制内容
     * @return true 如果发送成功
     * @throws NullPointerException 如果 bytes 为 null
     */
    @Override
    public boolean send(ByteString bytes) {
        if (bytes == null)
            throw new NullPointerException("bytes == null");
        return send(bytes, WebSocketProtocol.OPCODE_BINARY);
    }

    /**
     * 发送消息
     *
     * @param data         数据
     * @param formatOpcode 操作码
     * @return true 如果发送成功
     */
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

    /**
     * 发送 pong 帧
     *
     * @param payload pong 数据
     * @return true 如果发送成功
     */
    synchronized boolean pong(ByteString payload) {
        // Don't send pongs after we've failed or sent the close frame.
        if (failed || (enqueuedClose && messageAndCloseQueue.isEmpty()))
            return false;

        pongQueue.add(payload);
        runWriter();
        return true;
    }

    /**
     * 关闭 WebSocket 连接
     *
     * @param code   关闭代码
     * @param reason 关闭原因
     * @return true 如果关闭成功
     */
    @Override
    public boolean close(int code, String reason) {
        return close(code, reason, CANCEL_AFTER_CLOSE_MILLIS);
    }

    /**
     * 关闭 WebSocket 连接（指定超时）
     *
     * @param code                   关闭代码
     * @param reason                 关闭原因
     * @param cancelAfterCloseMillis 关闭后取消超时（毫秒）
     * @return true 如果关闭成功
     * @throws IllegalArgumentException 如果原因长度超限
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

        // Immediately prevent further frames from being enqueued.
        enqueuedClose = true;

        // Enqueue the close frame.
        messageAndCloseQueue.add(new Close(code, reasonBytes, cancelAfterCloseMillis));
        runWriter();
        return true;
    }

    /**
     * 运行写入任务
     */
    private void runWriter() {
        assert (Thread.holdsLock(this));

        if (null != executor) {
            executor.execute(writerRunnable);
        }
    }

    /**
     * 写入单个帧
     * <p>
     * 从队列中取出一个帧（pong、消息或关闭）并写入，优先处理控制帧。 如果队列为空或未连接，返回 false；否则返回 true 表示需继续写入。 仅由写入线程调用。
     * </p>
     *
     * @return true 如果成功写入且需继续处理
     * @throws IOException 如果写入失败
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

    /**
     * 写入 ping 帧
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

    /**
     * 处理 WebSocket 失败
     *
     * @param e        异常
     * @param response 响应（可能为 null）
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
     * 消息结构
     */
    static class Message {

        /**
         * 操作码
         */
        final int formatOpcode;
        /**
         * 数据
         */
        final ByteString data;

        /**
         * 构造函数
         *
         * @param formatOpcode 操作码
         * @param data         数据
         */
        Message(int formatOpcode, ByteString data) {
            this.formatOpcode = formatOpcode;
            this.data = data;
        }
    }

    /**
     * 关闭结构
     */
    static class Close {
        /** 关闭代码 */
        final int code;
        /** 关闭原因 */
        final ByteString reason;
        /** 取消超时 */
        final long cancelAfterCloseMillis;

        /**
         * 构造函数
         *
         * @param code                   关闭代码
         * @param reason                 关闭原因
         * @param cancelAfterCloseMillis 取消超时
         */
        Close(int code, ByteString reason, long cancelAfterCloseMillis) {
            this.code = code;
            this.reason = reason;
            this.cancelAfterCloseMillis = cancelAfterCloseMillis;
        }
    }

    /**
     * WebSocket 数据流
     */
    public abstract static class Streams implements Closeable {

        /** 是否为客户端 */
        public final boolean client;
        /** 数据源 */
        public final BufferSource source;
        /** 输出流 */
        public final BufferSink sink;

        /**
         * 构造函数
         *
         * @param client 是否为客户端
         * @param source 数据源
         * @param sink   输出流
         */
        public Streams(boolean client, BufferSource source, BufferSink sink) {
            this.client = client;
            this.source = source;
            this.sink = sink;
        }
    }

    /**
     * ping 任务
     */
    private class PingRunnable implements Runnable {
        PingRunnable() {
        }

        @Override
        public void run() {
            writePingFrame();
        }
    }

    /**
     * 取消任务
     */
    class CancelRunnable implements Runnable {
        @Override
        public void run() {
            cancel();
        }
    }

}