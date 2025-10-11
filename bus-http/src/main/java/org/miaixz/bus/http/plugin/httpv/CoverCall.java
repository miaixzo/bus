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
package org.miaixz.bus.http.plugin.httpv;

import org.miaixz.bus.core.io.ByteString;
import org.miaixz.bus.core.lang.exception.InternalException;
import org.miaixz.bus.core.net.HTTP;
import org.miaixz.bus.http.Httpv;
import org.miaixz.bus.http.Request;
import org.miaixz.bus.http.Response;
import org.miaixz.bus.http.socket.WebSocket;
import org.miaixz.bus.http.socket.WebSocketListener;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents an active or pending WebSocket call. This class provides methods to send messages, cancel the connection,
 * and close it gracefully. It also queues messages sent before the WebSocket is fully open.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class CoverCall implements Cancelable {

    /**
     * A queue for messages sent before the WebSocket connection is established.
     */
    private final List<Object> queues = new ArrayList<>();
    /**
     * The executor for handling message serialization.
     */
    private final CoverTasks.Executor executor;
    /**
     * A flag indicating if the call has been canceled or the socket has been closed.
     */
    private boolean cancelOrClosed;
    /**
     * The underlying WebSocket instance. It is null until the connection is opened.
     */
    private WebSocket webSocket;
    /**
     * The character set for message encoding and decoding.
     */
    private Charset charset;
    /**
     * The default message type for serialization (e.g., "json", "xml").
     */
    private String msgType;

    /**
     * Constructs a new CoverCall.
     *
     * @param executor The task executor.
     * @param msgType  The default message type for serialization.
     */
    public CoverCall(CoverTasks.Executor executor, String msgType) {
        this.executor = executor;
        this.msgType = msgType;
    }

    /**
     * Sets the character set to be used for message serialization.
     *
     * @param charset The character set.
     */
    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    /**
     * Cancels the WebSocket connection immediately. Any queued messages that have not been transmitted will be
     * discarded.
     *
     * @return {@code true} if the cancellation was initiated.
     */
    @Override
    public synchronized boolean cancel() {
        if (null != webSocket) {
            webSocket.cancel();
        }
        cancelOrClosed = true;
        return true;
    }

    /**
     * Attempts to close the WebSocket connection gracefully.
     *
     * @param code   The closing status code (e.g., 1000 for normal closure).
     * @param reason A descriptive reason for closing.
     * @return {@code true} if the closure was initiated.
     */
    public synchronized boolean close(int code, String reason) {
        if (null != webSocket) {
            webSocket.close(code, reason);
        }
        cancelOrClosed = true;
        return true;
    }

    /**
     * Sets the default message type for object serialization.
     *
     * @param type The message type (e.g., "json"). Cannot be "form".
     * @throws IllegalArgumentException if the type is null or "form".
     */
    public void msgType(String type) {
        if (null == type || type.equalsIgnoreCase(HTTP.FORM)) {
            throw new IllegalArgumentException("msgType cannot be null or form");
        }
        this.msgType = type;
    }

    /**
     * Returns the number of messages queued to be sent.
     *
     * @return The queue size.
     */
    public long queueSize() {
        if (null != webSocket) {
            return webSocket.queueSize();
        }
        return queues.size();
    }

    /**
     * Enqueues a message to be sent over the WebSocket. If the connection is not yet open, the message is queued
     * internally.
     *
     * @param msg The message to send (can be String, ByteString, byte[], or a serializable object).
     * @return {@code true} if the message was accepted for sending.
     */
    public boolean send(Object msg) {
        if (null == msg) {
            return false;
        }
        synchronized (queues) {
            if (null != webSocket) {
                return send(webSocket, msg);
            } else {
                queues.add(msg);
            }
        }
        return true;
    }

    /**
     * Associates this call with an opened WebSocket and flushes any queued messages. This method is intended for
     * internal use.
     *
     * @param webSocket The opened WebSocket instance.
     */
    void setWebSocket(WebSocket webSocket) {
        synchronized (queues) {
            for (Object msg : queues) {
                send(webSocket, msg);
            }
            this.webSocket = webSocket;
            queues.clear();
        }
    }

    /**
     * Sends a message over the given WebSocket, handling different message types.
     *
     * @param webSocket The WebSocket to send the message on.
     * @param msg       The message object.
     * @return {@code true} if the message was sent successfully.
     */
    boolean send(WebSocket webSocket, Object msg) {
        if (null == msg) {
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
        byte[] bytes = executor.doMsgConvert(msgType, (Convertor c) -> c.serialize(msg, charset)).data;
        return webSocket.send(new String(bytes, charset));
    }

    /**
     * A functional interface for handling WebSocket events.
     *
     * @param <T> The type of data associated with the event.
     */
    public interface Register<T> {

        /**
         * Called when a WebSocket event occurs.
         *
         * @param ws   The CoverCall instance.
         * @param data The data associated with the event.
         */
        void on(CoverCall ws, T data);

    }

    /**
     * Encapsulates information about a WebSocket closure.
     */
    public static class Close {

        /**
         * Custom status code indicating the WebSocket was canceled by the client.
         */
        public static final int CANCELED = 0;
        /**
         * Custom status code indicating the WebSocket was closed due to an unexpected exception.
         */
        public static final int EXCEPTION = -1;
        /**
         * Custom status code indicating the WebSocket was closed due to a network error.
         */
        public static final int NETWORK_ERROR = -2;
        /**
         * Custom status code indicating the WebSocket was closed due to a timeout.
         */
        public static final int TIMEOUT = -3;

        /**
         * The WebSocket closing status code.
         */
        private final int code;
        /**
         * The human-readable reason for closing.
         */
        private final String reason;

        /**
         * Constructs a new Close event object.
         *
         * @param code   The closing status code.
         * @param reason The reason for closing.
         */
        public Close(int code, String reason) {
            this.code = code;
            this.reason = reason;
        }

        /**
         * @return The closing status code.
         */
        public int getCode() {
            return code;
        }

        /**
         * @return The reason for closing.
         */
        public String getReason() {
            return reason;
        }

        /**
         * @return True if the WebSocket was closed due to cancellation.
         */
        public boolean isCanceled() {
            return code == CANCELED;
        }

        /**
         * @return True if the WebSocket was closed due to an exception.
         */
        public boolean isException() {
            return code == EXCEPTION;
        }

        /**
         * @return True if the WebSocket was closed due to a network error.
         */
        public boolean isNetworkError() {
            return code == NETWORK_ERROR;
        }

        /**
         * @return True if the WebSocket was closed due to a network timeout.
         */
        public boolean isTimeout() {
            return code == TIMEOUT;
        }

        @Override
        public String toString() {
            return "Close [code=" + code + ", reason=" + reason + "]";
        }
    }

    /**
     * A WebSocket listener that bridges Httpd's WebSocket events to the CoverCall's event system.
     */
    public static class Listener extends WebSocketListener {

        /**
         * The client that initiated the WebSocket connection.
         */
        private final Client client;
        /**
         * The CoverCall associated with this listener.
         */
        CoverCall webSocket;

        /**
         * The character set inferred from the server's response.
         */
        Charset charset;

        /**
         * Constructs a new listener.
         *
         * @param client    The initiating client.
         * @param webSocket The CoverCall wrapper.
         */
        public Listener(Client client, CoverCall webSocket) {
            this.client = client;
            this.webSocket = webSocket;
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            this.charset = client.charset(response);
            this.webSocket.setCharset(charset);
            this.webSocket.setWebSocket(webSocket);
            CoverTasks.Listener<CoverResult> listener = client.httpv.executor().getResponseListener();
            CoverResult result = new CoverResult.Real(client, response, client.httpv.executor());
            if (null != listener) {
                if (listener.listen(client, result) && null != client.onOpen) {
                    client.execute(() -> client.onOpen.on(this.webSocket, result), client.openOnIO);
                }
            } else if (null != client.onOpen) {
                client.execute(() -> client.onOpen.on(this.webSocket, result), client.openOnIO);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            if (null != client.onMessage) {
                client.execute(() -> client.onMessage.on(this.webSocket, new Message(text)), client.messageOnIO);
            }
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            if (null != client.onMessage) {
                client.execute(() -> client.onMessage.on(this.webSocket, new Message(bytes)), client.messageOnIO);
            }
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            if (null != client.onClosing) {
                client.execute(() -> client.onClosing.on(this.webSocket, new Close(code, reason)), client.closingOnIO);
            }
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            doOnClose(CoverResult.State.RESPONSED, code, reason);
        }

        /**
         * Handles the logic for triggering the 'onClosed' callback.
         *
         * @param state  The final state of the connection.
         * @param code   The closing status code.
         * @param reason The closing reason.
         */
        private void doOnClose(CoverResult.State state, int code, String reason) {
            CoverTasks.Listener<CoverResult.State> listener = client.httpv.executor().getCompleteListener();
            if (null != listener) {
                if (listener.listen(client, state) && null != client.onClosed) {
                    client.execute(
                            () -> client.onClosed.on(this.webSocket, toClose(state, code, reason)),
                            client.closedOnIO);
                }
            } else if (null != client.onClosed) {
                client.execute(
                        () -> client.onClosed.on(this.webSocket, toClose(state, code, reason)),
                        client.closedOnIO);
            }
        }

        /**
         * Converts an internal state and reason into a public {@link Close} object.
         *
         * @param state  The final state of the connection.
         * @param code   The original close code.
         * @param reason The original reason.
         * @return A {@link Close} object representing the closure.
         */
        private Close toClose(CoverResult.State state, int code, String reason) {
            if (state == CoverResult.State.CANCELED) {
                return new Close(Close.CANCELED, "Canceled");
            }
            if (state == CoverResult.State.EXCEPTION) {
                return new Close(Close.EXCEPTION, reason);
            }
            if (state == CoverResult.State.NETWORK_ERROR) {
                return new Close(Close.NETWORK_ERROR, reason);
            }
            if (state == CoverResult.State.TIMEOUT) {
                return new Close(Close.TIMEOUT, reason);
            }
            return new Close(code, reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            IOException e = t instanceof IOException ? (IOException) t : new IOException(t.getMessage(), t);
            doOnClose(client.toState(e), 0, t.getMessage());
            CoverTasks.Listener<IOException> listener = client.httpv.executor().getExceptionListener();
            if (null != listener) {
                if (listener.listen(client, e) && null != client.onException) {
                    client.execute(() -> client.onException.on(this.webSocket, t), client.exceptionOnIO);
                }
            } else if (null != client.onException) {
                client.execute(() -> client.onException.on(this.webSocket, t), client.exceptionOnIO);
            } else if (!client.nothrow) {
                throw new InternalException("WebSocket exception", t);
            }
        }

    }

    /**
     * A client for creating and configuring WebSocket connections.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public static class Client extends CoverHttp<Client> {

        /**
         * Listener for when the WebSocket connection is successfully opened.
         */
        private Register<CoverResult> onOpen;
        /**
         * Listener for when a connection failure occurs.
         */
        private Register<Throwable> onException;
        /**
         * Listener for incoming messages.
         */
        private Register<Message> onMessage;
        /**
         * Listener for when the server initiates a graceful close.
         */
        private Register<Close> onClosing;
        /**
         * Listener for when the connection is fully closed (also called on failure or cancellation).
         */
        private Register<Close> onClosed;

        /**
         * Flag to execute the onOpen callback on an I/O thread.
         */
        private boolean openOnIO;
        /**
         * Flag to execute the onException callback on an I/O thread.
         */
        private boolean exceptionOnIO;
        /**
         * Flag to execute the onMessage callback on an I/O thread.
         */
        private boolean messageOnIO;
        /**
         * Flag to execute the onClosing callback on an I/O thread.
         */
        private boolean closingOnIO;
        /**
         * Flag to execute the onClosed callback on an I/O thread.
         */
        private boolean closedOnIO;

        /**
         * Client-to-server ping interval in seconds.
         */
        private int pingSeconds = -1;
        /**
         * Expected server-to-client pong interval in seconds.
         */
        private int pongSeconds = -1;

        /**
         * Constructs a new WebSocket client.
         *
         * @param client The HTTP client instance.
         * @param url    The WebSocket URL.
         */
        public Client(Httpv client, String url) {
            super(client, url);
        }

        /**
         * Sets the heartbeat interval, overriding the default heartbeat mode.
         * <p>
         * Key differences: 1. Any message sent by the client acts as a heartbeat. 2. Any message sent by the server
         * acts as a heartbeat. 3. A timeout is only triggered if the server does not reply within 3 * pongSeconds. 4.
         * The specific content of the heartbeat can be specified (defaults to empty).
         *
         * @param pingSeconds The client's heartbeat interval in seconds (0 means no heartbeat).
         * @param pongSeconds The server's heartbeat interval in seconds (0 means no heartbeat).
         * @return this client instance for chaining.
         */
        public Client heatbeat(int pingSeconds, int pongSeconds) {
            if (pingSeconds < 0 || pongSeconds < 0) {
                throw new IllegalArgumentException("pingSeconds and pongSeconds must be greater than or equal to 0!");
            }
            this.pingSeconds = pingSeconds;
            this.pongSeconds = pongSeconds;
            return this;
        }

        /**
         * Starts the WebSocket connection and returns a {@link CoverCall} to interact with it.
         *
         * @return The CoverCall representing the connection.
         */
        public CoverCall listen() {
            String bodyType = getBodyType();
            String msgType = HTTP.FORM.equalsIgnoreCase(bodyType) ? HTTP.JSON : bodyType;
            CoverCall socket = new CoverCall(httpv.executor(), msgType);
            registeTagTask(socket);
            httpv.preprocess(this, () -> {
                synchronized (socket) {
                    if (socket.cancelOrClosed) {
                        removeTagTask();
                    } else {
                        Request request = prepareRequest("GET");
                        httpv.webSocket(request, new Listener(this, socket));
                    }
                }
            }, skipPreproc, skipSerialPreproc);
            return socket;
        }

        /**
         * Executes a command on the appropriate thread pool.
         *
         * @param command The command to run.
         * @param onIo    If true, execute on the I/O thread pool; otherwise, use the main pool.
         */
        private void execute(Runnable command, boolean onIo) {
            httpv.executor().execute(command, onIo);
        }

        /**
         * Sets the open connection listener.
         *
         * @param onOpen The listener.
         * @return this client instance for chaining.
         */
        public Client setOnOpen(Register<CoverResult> onOpen) {
            this.onOpen = onOpen;
            openOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * Sets the connection exception listener.
         *
         * @param onException The listener.
         * @return this client instance for chaining.
         */
        public Client setOnException(Register<Throwable> onException) {
            this.onException = onException;
            exceptionOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * Sets the message listener.
         *
         * @param onMessage The listener.
         * @return this client instance for chaining.
         */
        public Client setOnMessage(Register<Message> onMessage) {
            this.onMessage = onMessage;
            messageOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * Sets the closing listener.
         *
         * @param onClosing The listener.
         * @return this client instance for chaining.
         */
        public Client setOnClosing(Register<Close> onClosing) {
            this.onClosing = onClosing;
            closingOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * Sets the closed listener (also called on cancellation or exception).
         *
         * @param onClosed The listener.
         * @return this client instance for chaining.
         */
        public Client setOnClosed(Register<Close> onClosed) {
            this.onClosed = onClosed;
            closedOnIO = nextOnIO;
            nextOnIO = false;
            return this;
        }

        /**
         * @return The configured client-to-server ping interval in seconds.
         */
        public int pingSeconds() {
            return pingSeconds;
        }

        /**
         * @return The configured expected server-to-client pong interval in seconds.
         */
        public int pongSeconds() {
            return pongSeconds;
        }

    }

    /**
     * Represents an incoming WebSocket message, which can be either text or binary. Provides convenient methods for
     * data conversion.
     *
     * @author Kimi Liu
     * @since Java 17+
     */
    public static class Message {

        /**
         * The text content of the message, if it is a text message.
         */
        private String text;
        /**
         * The binary content of the message, if it is a binary message.
         */
        private ByteString bytes;

        /**
         * Constructs a message from a text string.
         *
         * @param text The text content.
         */
        public Message(String text) {
            this.text = text;
        }

        /**
         * Constructs a message from a ByteString.
         *
         * @param bytes The binary content.
         */
        public Message(ByteString bytes) {
            this.bytes = bytes;
        }

        /**
         * @return {@code true} if this is a text message, {@code false} otherwise.
         */
        public boolean isText() {
            return null != text;
        }

        /**
         * Converts the message content to a byte array. Text messages are encoded using UTF-8.
         *
         * @return The message content as a byte array, or {@code null} if the message is empty.
         */
        public byte[] toBytes() {
            if (null != text) {
                return text.getBytes(org.miaixz.bus.core.lang.Charset.UTF_8);
            }
            if (null != bytes) {
                return bytes.toByteArray();
            }
            return null;
        }

        /**
         * Returns the string representation of the message. For binary messages, this decodes the bytes as a UTF-8
         * string.
         *
         * @return The message content as a string, or {@code null} if the message is empty.
         */
        @Override
        public String toString() {
            if (null != text) {
                return text;
            }
            if (null != bytes) {
                return bytes.utf8();
            }
            return null;
        }

        /**
         * Converts the message content to a {@link ByteString}. Text messages are encoded using UTF-8.
         *
         * @return The message content as a {@link ByteString}.
         */
        public ByteString toByteString() {
            if (null != text) {
                return ByteString.encodeUtf8(text);
            }
            return bytes;
        }

        /**
         * Returns a {@link Reader} for the message content.
         *
         * @return A character stream reader.
         */
        public Reader toCharStream() {
            return new InputStreamReader(toByteStream());
        }

        /**
         * Returns an {@link InputStream} for the message content.
         *
         * @return A byte stream.
         */
        public InputStream toByteStream() {
            if (null != text) {
                return new ByteArrayInputStream(text.getBytes(org.miaixz.bus.core.lang.Charset.UTF_8));
            }
            if (null != bytes) {
                ByteBuffer buffer = bytes.asByteBuffer();
                return new InputStream() {

                    @Override
                    public int read() {
                        if (buffer.hasRemaining()) {
                            return buffer.get();
                        }
                        return -1;
                    }
                };
            }
            return null;
        }

    }

}
