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
package org.miaixz.bus.http;

import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.plugin.httpv.CoverCall;
import org.miaixz.bus.logger.Logger;

/**
 * Websockt 的 Stomp 客户端
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Stomp {

    public static final String SUPPORTED_VERSIONS = "1.1,1.2";
    public static final String AUTO_ACK = "auto";
    public static final String CLIENT_ACK = "client";
    private static final String TOPIC = "/topic";
    private static final String QUEUE = "/queue";
    private final boolean autoAck;
    private final CoverCall.Client cover;
    private final Map<String, Subscriber> subscribers;
    private boolean connected;
    private CoverCall websocket;
    private boolean legacyWhitespace = false;
    private Callback<Stomp> onConnected;
    private Callback<CoverCall.Close> onDisconnected;
    private Callback<Message> onError;

    private Stomp(CoverCall.Client cover, boolean autoAck) {
        this.cover = cover;
        this.autoAck = autoAck;
        this.subscribers = new HashMap<>();
    }

    /**
     * 构建 Stomp 客户端（自动确定消息）
     *
     * @param task 底层的 WebSocket 连接
     * @return Stomp
     */
    public static Stomp over(CoverCall.Client task) {
        return over(task, true);
    }

    /**
     * 构建 Stomp 客户端
     *
     * @param task    底层的 WebSocket 连接
     * @param autoAck 是否自动确定消息
     * @return Stomp
     */
    public static Stomp over(CoverCall.Client task, boolean autoAck) {
        return new Stomp(task, autoAck);
    }

    /**
     * 连接 Stomp 服务器
     *
     * @return Stomp
     */
    public Stomp connect() {
        return connect(null);
    }

    /**
     * 连接 Stomp 服务器
     *
     * @param headers Stomp 头信息
     * @return Stomp
     */
    public Stomp connect(List<Header> headers) {
        if (connected) {
            return this;
        }
        cover.setOnOpen((ws, res) -> {
            List<Header> cHeaders = new ArrayList<>();
            cHeaders.add(new Header(Header.VERSION, SUPPORTED_VERSIONS));
            cHeaders.add(new Header(Header.HEART_BEAT,
                    cover.pingSeconds() * 1000 + Symbol.COMMA + cover.pongSeconds() * 1000));
            if (null != headers) {
                cHeaders.addAll(headers);
            }
            send(new Message(Builder.CONNECT, cHeaders, null));
        });
        cover.setOnMessage((ws, msg) -> {
            Message message = Message.from(msg.toString());
            if (null != message) {
                receive(message);
            }
        });
        cover.setOnClosed((ws, close) -> {
            if (null != onDisconnected) {
                onDisconnected.on(close);
            }
        });
        websocket = cover.listen();
        return this;
    }

    public void disconnect() {
        if (null != websocket) {
            websocket.close(1000, "disconnect by user");
        }
    }

    /**
     * 连接成功回调
     *
     * @param onConnected 连接成功回调
     * @return Stomp
     */
    public Stomp setOnConnected(Callback<Stomp> onConnected) {
        this.onConnected = onConnected;
        return this;
    }

    /**
     * 连接断开回调
     *
     * @param onDisconnected 断开连接回调
     * @return Stomp
     */
    public Stomp setOnDisconnected(Callback<CoverCall.Close> onDisconnected) {
        this.onDisconnected = onDisconnected;
        return this;
    }

    /**
     * 错误回调（服务器返回的错误信息）
     *
     * @param onError 错误回调
     * @return Stomp
     */
    public Stomp setOnError(Callback<Message> onError) {
        this.onError = onError;
        return this;
    }

    /**
     * 发送消息到指定目的地
     *
     * @param destination 目的地
     * @param data        消息
     */
    public void sendTo(String destination, String data) {
        send(new Message(Builder.SEND, Collections.singletonList(new Header(Header.DESTINATION, destination)), data));
    }

    /**
     * 发送消息给服务器
     *
     * @param message 消息
     */
    public void send(Message message) {
        if (null == websocket) {
            throw new IllegalArgumentException("You must call connect before send");
        }
        websocket.send(message.compile(legacyWhitespace));
    }

    /**
     * 监听主题消息
     *
     * @param destination 监听地址
     * @param callback    消息回调
     * @return Stomp
     */
    public Stomp topic(String destination, Callback<Message> callback) {
        return topic(destination, null, callback);
    }

    /**
     * 监听主题消息
     *
     * @param destination 监听地址
     * @param headers     附加头信息
     * @param callback    消息回调
     * @return Stomp
     */
    public Stomp topic(String destination, List<Header> headers, Callback<Message> callback) {
        return subscribe(TOPIC + destination, headers, callback);
    }

    /**
     * 监听队列消息
     *
     * @param destination 监听地址
     * @param callback    消息回调
     * @return Stomp
     */
    public Stomp queue(String destination, Callback<Message> callback) {
        return queue(destination, null, callback);
    }

    /**
     * 监听队列消息
     *
     * @param destination 监听地址
     * @param headers     附加头信息
     * @param callback    消息回调
     * @return Stomp
     */
    public Stomp queue(String destination, List<Header> headers, Callback<Message> callback) {
        return subscribe(QUEUE + destination, headers, callback);
    }

    /**
     * 订阅消息
     *
     * @param destination 订阅地址
     * @param headers     附加头信息
     * @param callback    消息回调
     * @return Stomp
     */
    public synchronized Stomp subscribe(String destination, List<Header> headers, Callback<Message> callback) {
        if (subscribers.containsKey(destination)) {
            Logger.error("Attempted to subscribe to already-subscribed path!");
            return this;
        }
        Subscriber subscriber = new Subscriber(UUID.randomUUID().toString(), destination, callback, headers);
        subscribers.put(destination, subscriber);
        subscriber.subscribe();
        return this;
    }

    /**
     * 确认收到某条消息
     *
     * @param message 服务器发过来的消息
     */
    public void ack(Message message) {
        Header subscription = message.header(Header.SUBSCRIPTION);
        Header msgId = message.header(Header.MESSAGE_ID);
        if (null != subscription || null != msgId) {
            List<Header> headers = new ArrayList<>();
            headers.add(subscription);
            headers.add(msgId);
            send(new Message(Builder.ACK, headers, null));
        } else {
            Logger.error("subscription and message-id not found in " + message.toString() + ", so it can not be ack!");
        }
    }

    /**
     * 取消主题监听
     *
     * @param destination 监听地址
     */
    public void untopic(String destination) {
        unsubscribe(TOPIC + destination);
    }

    /**
     * 取消队列监听
     *
     * @param destination 监听地址
     */
    public void unqueue(String destination) {
        unsubscribe(QUEUE + destination);
    }

    /**
     * 取消订阅
     *
     * @param destination 订阅地址
     */
    public synchronized void unsubscribe(String destination) {
        Subscriber subscriber = subscribers.remove(destination);
        if (null != subscriber) {
            subscriber.unsubscribe();
        }
    }

    private void receive(Message msg) {
        String command = msg.getCommand();
        if (Builder.CONNECTED.equals(command)) {
            String hbHeader = msg.headerValue(Header.HEART_BEAT);
            if (null != hbHeader) {
                String[] heartbeats = hbHeader.split(Symbol.COMMA);
                int pingSeconds = Integer.parseInt(heartbeats[1]) / 1000;
                int pongSeconds = Integer.parseInt(heartbeats[0]) / 1000;
                cover.heatbeat(Math.max(pingSeconds, cover.pingSeconds()), Math.max(pongSeconds, cover.pongSeconds()));

            }
            synchronized (this) {
                connected = true;
                for (Subscriber s : subscribers.values()) {
                    s.subscribe();
                }
            }
            if (null != onConnected) {
                onConnected.on(this);
            }
        } else if (Builder.MESSAGE.equals(command)) {
            String id = msg.headerValue(Header.SUBSCRIPTION);
            String destination = msg.headerValue(Header.DESTINATION);
            if (null == id || null == destination) {
                return;
            }
            Subscriber subscriber = subscribers.get(destination);
            if (null != subscriber && id.equals(subscriber.id)) {
                subscriber.callback.on(msg);
            }
        } else if (Builder.ERROR.equals(command)) {
            if (null != onError) {
                onError.on(msg);
            }
        }
    }

    public void setLegacyWhitespace(boolean legacyWhitespace) {
        this.legacyWhitespace = legacyWhitespace;
    }

    public static class Header {

        public static final String VERSION = "accept-version";
        public static final String HEART_BEAT = "heart-beat";
        public static final String DESTINATION = "destination";
        public static final String MESSAGE_ID = "message-id";
        public static final String ID = "id";
        public static final String SUBSCRIPTION = "subscription";
        public static final String ACK = "ack";

        private final String key;
        private final String value;

        public Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return key + Symbol.C_COLON + value;
        }

    }

    public static class Message {

        private final String command;
        private final List<Header> headers;
        private final String payload;

        public Message(String command, List<Header> headers, String payload) {
            this.command = command;
            this.headers = headers;
            this.payload = payload;
        }

        public static Message from(String data) {
            if (null == data || data.trim().isEmpty()) {
                return new Message(Normal.UNKNOWN, null, data);
            }

            int cmdIndex = data.indexOf("\n");
            int mhIndex = data.indexOf("\n\n");

            if (cmdIndex >= mhIndex) {
                Logger.error("非法的 STOMP 消息：" + data);
                return null;
            }
            String command = data.substring(0, cmdIndex);
            String[] headers = data.substring(cmdIndex + 1, mhIndex).split("\n");

            List<Header> headerList = new ArrayList<>(headers.length);
            for (String header : headers) {
                String[] hv = header.split(Symbol.COLON);
                if (hv.length == 2) {
                    headerList.add(new Header(hv[0], hv[1]));
                }
            }
            String payload = null;
            if (data.length() > mhIndex + 2) {
                if (data.endsWith("\u0000\n") && data.length() > mhIndex + 4) {
                    payload = data.substring(mhIndex + 2, data.length() - 2);
                } else if (data.endsWith("\u0000") && data.length() > mhIndex + 3) {
                    payload = data.substring(mhIndex + 2, data.length() - 1);
                }
            }
            return new Message(command, headerList, payload);
        }

        public List<Header> getHeaders() {
            return headers;
        }

        public String getPayload() {
            return payload;
        }

        public String getCommand() {
            return command;
        }

        public String headerValue(String key) {
            Header header = header(key);
            if (null != header) {
                return header.getValue();
            }
            return null;
        }

        public Header header(String key) {
            if (null != headers) {
                for (Header header : headers) {
                    if (header.getKey().equals(key))
                        return header;
                }
            }
            return null;
        }

        public String compile(boolean legacyWhitespace) {
            StringBuilder builder = new StringBuilder();
            builder.append(command).append('\n');
            for (Header header : headers) {
                builder.append(header.getKey()).append(Symbol.C_COLON).append(header.getValue()).append('\n');
            }
            builder.append('\n');
            if (null != payload) {
                builder.append(payload);
                if (legacyWhitespace)
                    builder.append("\n\n");
            }
            builder.append("\u0000");
            return builder.toString();
        }

        @Override
        public String toString() {
            return "Message {command='" + command + "', headers=" + headers + ", payload='" + payload + "'}";
        }

    }

    class Subscriber {

        private final String id;
        private final String destination;
        private final Callback<Message> callback;
        private final List<Header> headers;
        private boolean subscribed;

        Subscriber(String id, String destination, Callback<Message> callback, List<Header> headers) {
            this.id = id;
            this.destination = destination;
            this.callback = callback;
            this.headers = headers;
        }

        void subscribe() {
            if (connected && !subscribed) {
                List<Header> headers = new ArrayList<>();
                headers.add(new Header(Header.ID, id));
                headers.add(new Header(Header.DESTINATION, destination));
                boolean ackNotAdded = true;
                if (null != this.headers) {
                    for (Header header : this.headers) {
                        if (Header.ACK.equals(header.getKey())) {
                            ackNotAdded = false;
                        }
                        String key = header.getKey();
                        if (!Header.ID.equals(key) && !Header.DESTINATION.equals(key)) {
                            headers.add(header);
                        }
                    }
                }
                if (ackNotAdded) {
                    headers.add(new Header(Header.ACK, autoAck ? AUTO_ACK : CLIENT_ACK));
                }
                send(new Message(Builder.SUBSCRIBE, headers, null));
                subscribed = true;
            }
        }

        void unsubscribe() {
            List<Header> headers = Collections.singletonList(new Header(Header.ID, id));
            send(new Message(Builder.UNSUBSCRIBE, headers, null));
            subscribed = false;
        }

    }

}
