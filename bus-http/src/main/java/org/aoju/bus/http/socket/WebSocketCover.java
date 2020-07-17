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

import org.aoju.bus.core.io.ByteString;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.http.*;
import org.aoju.bus.http.magic.RealResult;
import org.aoju.bus.http.metric.Cancelable;
import org.aoju.bus.http.metric.Convertor;
import org.aoju.bus.http.metric.TaskExecutor;
import org.aoju.bus.http.metric.TaskListener;
import org.aoju.bus.http.metric.http.CoverHttp;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 6.0.3
 * @since JDK 1.8+
 */
public class WebSocketCover extends CoverHttp<WebSocketCover> {

    private Sockets.Listener<Results> onOpen;
    private Sockets.Listener<Throwable> onException;
    private Sockets.Listener<Sockets.Message> onMessage;
    private Sockets.Listener<Sockets.Close> onClosing;
    private Sockets.Listener<Sockets.Close> onClosed;

    private boolean openOnIO;
    private boolean exceptionOnIO;
    private boolean messageOnIO;
    private boolean closingOnIO;
    private boolean closedOnIO;

    public WebSocketCover(Httpv httpClient, String url) {
        super(httpClient, url);
    }

    /**
     * 启动 WebSocket 监听
     *
     * @return WebSocket
     */
    public Sockets listen() {
        String bodyType = getBodyType();
        String msgType = Builder.FORM.equalsIgnoreCase(bodyType) ? Builder.JSON : bodyType;
        WebSocketImpl socket = new WebSocketImpl(httpClient.executor(), msgType);
        registeTagTask(socket);
        httpClient.preprocess(this, () -> {
            synchronized (socket) {
                if (socket.cancelOrClosed) {
                    removeTagTask();
                } else {
                    Request request = prepareRequest("GET");
                    httpClient.webSocket(request, new MessageListener(socket));
                }
            }
        }, skipPreproc, skipSerialPreproc);
        return socket;
    }

    private void execute(Runnable command, boolean onIo) {
        httpClient.executor().execute(command, onIo);
    }

    /**
     * 连接打开监听
     *
     * @param onOpen 监听器
     * @return WebSocketCover
     */
    public WebSocketCover setOnOpen(Sockets.Listener<Results> onOpen) {
        this.onOpen = onOpen;
        openOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * 连接异常监听
     *
     * @param onException 监听器
     * @return WebSocketCover
     */
    public WebSocketCover setOnException(Sockets.Listener<Throwable> onException) {
        this.onException = onException;
        exceptionOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * 消息监听
     *
     * @param onMessage 监听器
     * @return WebSocketCover
     */
    public WebSocketCover setOnMessage(Sockets.Listener<Sockets.Message> onMessage) {
        this.onMessage = onMessage;
        messageOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * 正在关闭监听
     *
     * @param onClosing 监听器
     * @return WebSocketCover
     */
    public WebSocketCover setOnClosing(Sockets.Listener<Sockets.Close> onClosing) {
        this.onClosing = onClosing;
        closingOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    /**
     * 已关闭监听（当连接被取消或发生异常时，也会走该回调）
     *
     * @param onClosed 监听器
     * @return WebSocketCover
     */
    public WebSocketCover setOnClosed(Sockets.Listener<Sockets.Close> onClosed) {
        this.onClosed = onClosed;
        closedOnIO = nextOnIO;
        nextOnIO = false;
        return this;
    }

    public interface Sockets extends Cancelable {

        /**
         * 若连接已打开，则：
         * 同 {@link Sockets#queueSize()}，返回排序消息的字节数
         * 否则：
         *
         * @return 排队消息的数量
         */
        long queueSize();

        /**
         * @param object 待发送的对象，可以是 String | ByteString | byte[] | Java Bean
         * @return 如果连接已断开 返回 false
         */
        boolean send(Object object);

        /**
         * 同 {@link Sockets#close(int, String)}
         *
         * @param code   编码
         * @param reason 原因
         * @return 是否关闭
         */
        boolean close(int code, String reason);

        /**
         * 设置消息类型
         *
         * @param type 消息类型，如 json、xml、protobuf 等
         */
        void msgType(String type);

        /**
         * WebSocket 消息
         */
        interface Message extends Toable {

            /**
             * 判断是文本消息还是二进制消息
             *
             * @return 是否是文本消息
             */
            boolean isText();

        }

        interface Listener<T> {

            void on(Sockets ws, T data);

        }

        class Close {

            public static int CANCELED = 0;
            public static int EXCEPTION = -1;
            public static int NETWORK_ERROR = -2;
            public static int TIMEOUT = -3;

            private int code;
            private String reason;

            public Close(int code, String reason) {
                this.code = code;
                this.reason = reason;
            }

            /**
             * @return 关闭状态码
             */
            public int getCode() {
                return code;
            }

            /**
             * @return 关闭原因
             */
            public String getReason() {
                return reason;
            }

            /**
             * @return 是否因 WebSocket 连接被取消而关闭
             */
            public boolean isCanceled() {
                return code == CANCELED;
            }

            /**
             * @return 是否因 WebSocket 连接发生异常而关闭
             */
            public boolean isException() {
                return code == EXCEPTION;
            }

            /**
             * @return 是否因 网络错误 而关闭
             */
            public boolean isNetworkError() {
                return code == NETWORK_ERROR;
            }

            /**
             * @return 是否因 网络超时 而关闭
             */
            public boolean isTimeout() {
                return code == TIMEOUT;
            }

            @Override
            public String toString() {
                return "Close [code=" + code + ", reason=" + reason + "]";
            }
        }

    }

    static class WebSocketImpl implements Sockets {

        private final List<Object> queues = new ArrayList<>();
        private boolean cancelOrClosed;
        private WebSocket webSocket;
        private TaskExecutor taskExecutor;

        private Charset charset;

        private String msgType;

        public WebSocketImpl(TaskExecutor taskExecutor, String msgType) {
            this.taskExecutor = taskExecutor;
            this.msgType = msgType;
        }

        public void setCharset(Charset charset) {
            this.charset = charset;
        }

        @Override
        public synchronized boolean cancel() {
            if (webSocket != null) {
                webSocket.cancel();
            }
            cancelOrClosed = true;
            return true;
        }

        @Override
        public synchronized boolean close(int code, String reason) {
            if (webSocket != null) {
                webSocket.close(code, reason);
            }
            cancelOrClosed = true;
            return true;
        }

        @Override
        public void msgType(String type) {
            if (type == null || type.equalsIgnoreCase(Builder.FORM)) {
                throw new IllegalArgumentException("msgType 不可为空 或 form");
            }
            this.msgType = type;
        }

        @Override
        public long queueSize() {
            if (webSocket != null) {
                return webSocket.queueSize();
            }
            return queues.size();
        }

        @Override
        public boolean send(Object msg) {
            if (msg == null) {
                return false;
            }
            synchronized (queues) {
                if (webSocket != null) {
                    return send(webSocket, msg);
                } else {
                    queues.add(msg);
                }
            }
            return true;
        }

        void setWebSocket(WebSocket webSocket) {
            synchronized (queues) {
                for (Object msg : queues) {
                    send(webSocket, msg);
                }
                this.webSocket = webSocket;
                queues.clear();
            }
        }

        boolean send(WebSocket webSocket, Object msg) {
            if (msg == null) {
                return false;
            }
            if (msg instanceof String) {
                return webSocket.send((String) msg);
            }
            if (msg instanceof ByteString) {
                return webSocket.send((ByteString) msg);
            }
            if (msg instanceof byte[]) {
                return webSocket.send(ByteString.of((byte[]) msg));
            }
            byte[] bytes = taskExecutor.doMsgConvert(msgType, (Convertor c) -> c.serialize(msg, charset)).data;
            return webSocket.send(new String(bytes, charset));
        }

    }

    class MessageListener extends WebSocketListener {

        WebSocketImpl webSocket;

        Charset charset;

        public MessageListener(WebSocketImpl webSocket) {
            this.webSocket = webSocket;
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            this.charset = charset(response);
            this.webSocket.setCharset(charset);
            this.webSocket.setWebSocket(webSocket);
            TaskListener<Results> listener = httpClient.executor().getResponseListener();
            Results result = new RealResult(WebSocketCover.this, response, httpClient.executor());
            if (listener != null) {
                if (listener.listen(WebSocketCover.this, result) && onOpen != null) {
                    execute(() -> onOpen.on(this.webSocket, result), openOnIO);
                }
            } else if (onOpen != null) {
                execute(() -> onOpen.on(this.webSocket, result), openOnIO);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if (onMessage != null) {
                execute(() -> onMessage.on(this.webSocket, new WebSocketMessage(text, httpClient.executor(), charset)), messageOnIO);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if (onMessage != null) {
                execute(() -> onMessage.on(this.webSocket, new WebSocketMessage(bytes, httpClient.executor(), charset)), messageOnIO);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            if (onClosing != null) {
                execute(() -> onClosing.on(this.webSocket, new Sockets.Close(code, reason)), closingOnIO);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            doOnClose(Results.State.RESPONSED, code, reason);
        }

        private void doOnClose(Results.State state, int code, String reason) {
            TaskListener<Results.State> listener = httpClient.executor().getCompleteListener();
            if (listener != null) {
                if (listener.listen(WebSocketCover.this, state) && onClosed != null) {
                    execute(() -> onClosed.on(this.webSocket, toClose(state, code, reason)), closedOnIO);
                }
            } else if (onClosed != null) {
                execute(() -> onClosed.on(this.webSocket, toClose(state, code, reason)), closedOnIO);
            }
        }

        private Sockets.Close toClose(Results.State state, int code, String reason) {
            if (state == Results.State.CANCELED) {
                return new Sockets.Close(Sockets.Close.CANCELED, "Canceled");
            }
            if (state == Results.State.EXCEPTION) {
                return new Sockets.Close(Sockets.Close.CANCELED, reason);
            }
            if (state == Results.State.NETWORK_ERROR) {
                return new Sockets.Close(Sockets.Close.NETWORK_ERROR, reason);
            }
            if (state == Results.State.TIMEOUT) {
                return new Sockets.Close(Sockets.Close.TIMEOUT, reason);
            }
            return new Sockets.Close(code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            IOException e = t instanceof IOException ? (IOException) t : new IOException(t.getMessage(), t);
            doOnClose(toState(e), 0, t.getMessage());
            TaskListener<IOException> listener = httpClient.executor().getExceptionListener();
            if (listener != null) {
                if (listener.listen(WebSocketCover.this, e) && onException != null) {
                    execute(() -> onException.on(this.webSocket, t), exceptionOnIO);
                }
            } else if (onException != null) {
                execute(() -> onException.on(this.webSocket, t), exceptionOnIO);
            } else if (!nothrow) {
                throw new InstrumentException("WebSocket exception", t);
            }
        }

    }

}
