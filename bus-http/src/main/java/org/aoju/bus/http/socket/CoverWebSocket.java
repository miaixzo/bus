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

public interface CoverWebSocket extends Cancelable {

    /**
     * 若连接已打开，则：
     * 同 {@link CoverWebSocket#queueSize()}，返回排序消息的字节数
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
     * 同 {@link CoverWebSocket#close(int, String)}
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

        void on(CoverWebSocket ws, T data);

    }

    class Close {

        public static int CANCELED = 0;
        public static int EXCEPTION = -1;
        public static int NETWORK_ERROR = -2;
        public static int TIMEOUT = -3;

        private final int code;
        private final String reason;

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

    class MessageListener extends WebSocketListener {

        private final Client client;
        WebSocketImpl webSocket;

        Charset charset;

        public MessageListener(Client client, WebSocketImpl webSocket) {
            this.client = client;
            this.webSocket = webSocket;
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            this.charset = client.charset(response);
            this.webSocket.setCharset(charset);
            this.webSocket.setWebSocket(webSocket);
            TaskListener<Results> listener = client.httpv.executor().getResponseListener();
            Results result = new RealResult(client, response, client.httpv.executor());
            if (listener != null) {
                if (listener.listen(client, result) && client.onOpen != null) {
                    client.execute(() -> client.onOpen.on(this.webSocket, result), client.openOnIO);
                }
            } else if (client.onOpen != null) {
                client.execute(() -> client.onOpen.on(this.webSocket, result), client.openOnIO);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if (client.onMessage != null) {
                client.execute(() -> client.onMessage.on(this.webSocket, new WebSocketMessage(text, client.httpv.executor(), charset)), client.messageOnIO);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if (client.onMessage != null) {
                client.execute(() -> client.onMessage.on(this.webSocket, new WebSocketMessage(bytes, client.httpv.executor(), charset)), client.messageOnIO);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            if (client.onClosing != null) {
                client.execute(() -> client.onClosing.on(this.webSocket, new Close(code, reason)), client.closingOnIO);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            doOnClose(Results.State.RESPONSED, code, reason);
        }

        private void doOnClose(Results.State state, int code, String reason) {
            TaskListener<Results.State> listener = client.httpv.executor().getCompleteListener();
            if (listener != null) {
                if (listener.listen(client, state) && client.onClosed != null) {
                    client.execute(() -> client.onClosed.on(this.webSocket, toClose(state, code, reason)), client.closedOnIO);
                }
            } else if (client.onClosed != null) {
                client.execute(() -> client.onClosed.on(this.webSocket, toClose(state, code, reason)), client.closedOnIO);
            }
        }

        private Close toClose(Results.State state, int code, String reason) {
            if (state == Results.State.CANCELED) {
                return new Close(Close.CANCELED, "Canceled");
            }
            if (state == Results.State.EXCEPTION) {
                return new Close(Close.CANCELED, reason);
            }
            if (state == Results.State.NETWORK_ERROR) {
                return new Close(Close.NETWORK_ERROR, reason);
            }
            if (state == Results.State.TIMEOUT) {
                return new Close(Close.TIMEOUT, reason);
            }
            return new Close(code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            IOException e = t instanceof IOException ? (IOException) t : new IOException(t.getMessage(), t);
            doOnClose(client.toState(e), 0, t.getMessage());
            TaskListener<IOException> listener = client.httpv.executor().getExceptionListener();
            if (listener != null) {
                if (listener.listen(client, e) && client.onException != null) {
                    client.execute(() -> client.onException.on(this.webSocket, t), client.exceptionOnIO);
                }
            } else if (client.onException != null) {
                client.execute(() -> client.onException.on(this.webSocket, t), client.exceptionOnIO);
            } else if (!client.nothrow) {
                throw new InstrumentException("WebSocket exception", t);
            }
        }

    }

    class WebSocketImpl implements CoverWebSocket {

        private final List<Object> queues = new ArrayList<>();
        private final TaskExecutor taskExecutor;
        private boolean cancelOrClosed;
        private WebSocket webSocket;
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

    /**
     * @author Kimi Liu
     * @version 6.1.5
     * @since JDK 1.8+
     */
    class Client extends CoverHttp<Client> {

        private Listener<Results> onOpen;
        private Listener<Throwable> onException;
        private Listener<Message> onMessage;
        private Listener<Close> onClosing;
        private Listener<Close> onClosed;

        private boolean openOnIO;
        private boolean exceptionOnIO;
        private boolean messageOnIO;
        private boolean closingOnIO;
        private boolean closedOnIO;

        private int pingSeconds = -1;
        private int pongSeconds = -1;

        public Client(Httpv httpClient, String url) {
            super(httpClient, url);
        }

        /**
         * 设置心跳间隔
         * 覆盖原有的心跳模式，主要区别如下：
         * <p>
         * 1、客户端发送的任何消息都具有一次心跳作用
         * 2、服务器发送的任何消息都具有一次心跳作用
         * 3、若服务器超过 3 * pongSeconds 秒没有回复心跳，才判断心跳超时
         * 4、可指定心跳的具体内容（默认为空）
         *
         * @param pingSeconds 客户端心跳间隔秒数（0 表示不需要心跳）
         * @param pongSeconds 服务器心跳间隔秒数（0 表示不需要心跳）
         * @return this
         */
        public Client heatbeat(int pingSeconds, int pongSeconds) {
            if (pingSeconds < 0 || pongSeconds < 0) {
                throw new IllegalArgumentException("pingSeconds and pongSeconds must greater equal than 0!");
            }
            this.pingSeconds = pingSeconds;
            this.pongSeconds = pongSeconds;
            return this;
        }

        /**
         * 启动 WebSocket 监听
         *
         * @return WebSocket
         */
        public CoverWebSocket listen() {
            String bodyType = getBodyType();
            String msgType = Builder.FORM.equalsIgnoreCase(bodyType) ? Builder.JSON : bodyType;
            WebSocketImpl socket = new WebSocketImpl(httpv.executor(), msgType);
            registeTagTask(socket);
            httpv.preprocess(this, () -> {
                synchronized (socket) {
                    if (socket.cancelOrClosed) {
                        removeTagTask();
                    } else {
                        Request request = prepareRequest("GET");
                        httpv.webSocket(request, new MessageListener(this, socket));
                    }
                }
            }, skipPreproc, skipSerialPreproc);
            return socket;
        }

        private void execute(Runnable command, boolean onIo) {
            httpv.executor().execute(command, onIo);
        }

        /**
         * 连接打开监听
         *
         * @param onOpen 监听器
         * @return WebSocketCover
         */
        public Client setOnOpen(Listener<Results> onOpen) {
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
        public Client setOnException(Listener<Throwable> onException) {
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
        public Client setOnMessage(Listener<Message> onMessage) {
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
        public Client setOnClosing(Listener<Close> onClosing) {
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
        public Client setOnClosed(Listener<Close> onClosed) {
            this.onClosed = onClosed;
            closedOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        public int pingSeconds() {
            return pingSeconds;
        }

        public int pongSeconds() {
            return pongSeconds;
        }

    }

}
