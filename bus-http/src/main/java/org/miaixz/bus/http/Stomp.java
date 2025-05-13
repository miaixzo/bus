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
package org.miaixz.bus.http;

import java.util.*;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.plugin.httpv.CoverCall;
import org.miaixz.bus.logger.Logger;

/**
 * WebSocket 的 STOMP 协议客户端
 * <p>
 * 提供与 STOMP 服务器的连接、消息发送、订阅和消息确认功能。 支持主题（topic）和队列（queue）订阅，自动或手动消息确认。
 * </p>
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Stomp {

    /**
     * 支持的 STOMP 协议版本
     */
    public static final String SUPPORTED_VERSIONS = "1.1,1.2";
    /**
     * 自动确认模式
     */
    public static final String AUTO_ACK = "auto";
    /**
     * 客户端确认模式
     */
    public static final String CLIENT_ACK = "client";
    /**
     * 主题订阅前缀
     */
    private static final String TOPIC = "/topic";
    /**
     * 队列订阅前缀
     */
    private static final String QUEUE = "/queue";
    /**
     * 是否自动确认消息
     */
    private final boolean autoAck;
    /**
     * 底层 WebSocket 客户端
     */
    private final CoverCall.Client cover;
    /**
     * 订阅者映射（目标地址到订阅者）
     */
    private final Map<String, Subscriber> subscribers;
    /**
     * 是否已连接到服务器
     */
    private boolean connected;
    /**
     * WebSocket 连接实例
     */
    private CoverCall websocket;
    /**
     * 是否使用传统空白字符格式
     */
    private boolean legacyWhitespace = false;
    /**
     * 连接成功回调
     */
    private Callback<Stomp> onConnected;
    /**
     * 连接断开回调
     */
    private Callback<CoverCall.Close> onDisconnected;
    /**
     * 错误消息回调
     */
    private Callback<Message> onError;

    /**
     * 构造函数，初始化 STOMP 客户端
     *
     * @param cover   底层 WebSocket 客户端
     * @param autoAck 是否自动确认消息
     */
    private Stomp(CoverCall.Client cover, boolean autoAck) {
        this.cover = cover;
        this.autoAck = autoAck;
        this.subscribers = new HashMap<>();
    }

    /**
     * 构建 STOMP 客户端（默认自动确认消息）
     *
     * @param task 底层 WebSocket 客户端
     * @return STOMP 客户端实例
     */
    public static Stomp over(CoverCall.Client task) {
        return over(task, true);
    }

    /**
     * 构建 STOMP 客户端
     *
     * @param task    底层 WebSocket 客户端
     * @param autoAck 是否自动确认消息
     * @return STOMP 客户端实例
     */
    public static Stomp over(CoverCall.Client task, boolean autoAck) {
        return new Stomp(task, autoAck);
    }

    /**
     * 连接到 STOMP 服务器
     *
     * @return 当前 STOMP 客户端实例
     */
    public Stomp connect() {
        return connect(null);
    }

    /**
     * 使用指定头部连接到 STOMP 服务器
     *
     * @param headers STOMP 头部信息
     * @return 当前 STOMP 客户端实例
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

    /**
     * 断开与 STOMP 服务器的连接
     */
    public void disconnect() {
        if (null != websocket) {
            websocket.close(1000, "disconnect by user");
        }
    }

    /**
     * 设置连接成功回调
     *
     * @param onConnected 连接成功回调
     * @return 当前 STOMP 客户端实例
     */
    public Stomp setOnConnected(Callback<Stomp> onConnected) {
        this.onConnected = onConnected;
        return this;
    }

    /**
     * 设置连接断开回调
     *
     * @param onDisconnected 连接断开回调
     * @return 当前 STOMP 客户端实例
     */
    public Stomp setOnDisconnected(Callback<CoverCall.Close> onDisconnected) {
        this.onDisconnected = onDisconnected;
        return this;
    }

    /**
     * 设置错误消息回调
     *
     * @param onError 错误消息回调
     * @return 当前 STOMP 客户端实例
     */
    public Stomp setOnError(Callback<Message> onError) {
        this.onError = onError;
        return this;
    }

    /**
     * 发送消息到指定目标地址
     *
     * @param destination 目标地址
     * @param data        消息内容
     */
    public void sendTo(String destination, String data) {
        send(new Message(Builder.SEND, Collections.singletonList(new Header(Header.DESTINATION, destination)), data));
    }

    /**
     * 发送 STOMP 消息到服务器
     *
     * @param message STOMP 消息
     * @throws IllegalArgumentException 如果未调用 connect 方法
     */
    public void send(Message message) {
        if (null == websocket) {
            throw new IllegalArgumentException("You must call connect before send");
        }
        websocket.send(message.compile(legacyWhitespace));
    }

    /**
     * 订阅主题消息
     *
     * @param destination 主题地址
     * @param callback    消息回调
     * @return 当前 STOMP 客户端实例
     */
    public Stomp topic(String destination, Callback<Message> callback) {
        return topic(destination, null, callback);
    }

    /**
     * 订阅主题消息（带附加头部）
     *
     * @param destination 主题地址
     * @param headers     附加头部信息
     * @param callback    消息回调
     * @return 当前 STOMP 客户端实例
     */
    public Stomp topic(String destination, List<Header> headers, Callback<Message> callback) {
        return subscribe(TOPIC + destination, headers, callback);
    }

    /**
     * 订阅队列消息
     *
     * @param destination 队列地址
     * @param callback    消息回调
     * @return 当前 STOMP 客户端实例
     */
    public Stomp queue(String destination, Callback<Message> callback) {
        return queue(destination, null, callback);
    }

    /**
     * 订阅队列消息（带附加头部）
     *
     * @param destination 队列地址
     * @param headers     附加头部信息
     * @param callback    消息回调
     * @return 当前 STOMP 客户端实例
     */
    public Stomp queue(String destination, List<Header> headers, Callback<Message> callback) {
        return subscribe(QUEUE + destination, headers, callback);
    }

    /**
     * 订阅指定地址的消息
     *
     * @param destination 订阅地址
     * @param headers     附加头部信息
     * @param callback    消息回调
     * @return 当前 STOMP 客户端实例
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
     * 确认收到消息
     *
     * @param message 服务器发送的消息
     */
    public void ack(Message message) {
        Header subscription = message.header(Header.SUBSCRIPTION);
        Header msgId = message.header(Header.MESSAGE_ID);
        if (null != subscription && null != msgId) {
            List<Header> headers = new ArrayList<>();
            headers.add(subscription);
            headers.add(msgId);
            send(new Message(Builder.ACK, headers, null));
        } else {
            Logger.error("subscription and message-id not found in " + message.toString() + ", so it can not be ack!");
        }
    }

    /**
     * 取消主题订阅
     *
     * @param destination 主题地址
     */
    public void untopic(String destination) {
        unsubscribe(TOPIC + destination);
    }

    /**
     * 取消队列订阅
     *
     * @param destination 队列地址
     */
    public void unqueue(String destination) {
        unsubscribe(QUEUE + destination);
    }

    /**
     * 取消指定地址的订阅
     *
     * @param destination 订阅地址
     */
    public synchronized void unsubscribe(String destination) {
        Subscriber subscriber = subscribers.remove(destination);
        if (null != subscriber) {
            subscriber.unsubscribe();
        }
    }

    /**
     * 处理接收到的 STOMP 消息
     *
     * @param msg STOMP 消息
     */
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

    /**
     * 设置是否使用传统空白字符格式
     *
     * @param legacyWhitespace 是否启用传统格式
     */
    public void setLegacyWhitespace(boolean legacyWhitespace) {
        this.legacyWhitespace = legacyWhitespace;
    }

    /**
     * STOMP 头部类
     */
    public static class Header {

        /**
         * STOMP 协议版本头部
         */
        public static final String VERSION = "accept-version";
        /**
         * 心跳间隔头部
         */
        public static final String HEART_BEAT = "heart-beat";
        /**
         * 目标地址头部
         */
        public static final String DESTINATION = "destination";
        /**
         * 消息 ID 头部
         */
        public static final String MESSAGE_ID = "message-id";
        /**
         * 订阅 ID 头部
         */
        public static final String ID = "id";
        /**
         * 订阅标识头部
         */
        public static final String SUBSCRIPTION = "subscription";
        /**
         * 确认模式头部
         */
        public static final String ACK = "ack";

        /**
         * 头部键
         */
        private final String key;
        /**
         * 头部值
         */
        private final String value;

        /**
         * 构造函数，初始化头部
         *
         * @param key   头部键
         * @param value 头部值
         */
        public Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * 获取头部键
         *
         * @return 头部键
         */
        public String getKey() {
            return key;
        }

        /**
         * 获取头部值
         *
         * @return 头部值
         */
        public String getValue() {
            return value;
        }

        /**
         * 返回头部的字符串表示
         *
         * @return 键值对字符串（格式：key:value）
         */
        @Override
        public String toString() {
            return key + Symbol.C_COLON + value;
        }
    }

    /**
     * STOMP 消息类
     */
    public static class Message {

        /**
         * 消息命令
         */
        private final String command;
        /**
         * 消息头部列表
         */
        private final List<Header> headers;
        /**
         * 消息内容
         */
        private final String payload;

        /**
         * 构造函数，初始化 STOMP 消息
         *
         * @param command 消息命令
         * @param headers 头部列表
         * @param payload 消息内容
         */
        public Message(String command, List<Header> headers, String payload) {
            this.command = command;
            this.headers = headers;
            this.payload = payload;
        }

        /**
         * 从字符串解析 STOMP 消息
         *
         * @param data 消息字符串
         * @return 解析后的 Message 对象（无效时为 null）
         */
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

        /**
         * 获取消息头部列表
         *
         * @return 头部列表
         */
        public List<Header> getHeaders() {
            return headers;
        }

        /**
         * 获取消息内容
         *
         * @return 消息内容
         */
        public String getPayload() {
            return payload;
        }

        /**
         * 获取消息命令
         *
         * @return 命令
         */
        public String getCommand() {
            return command;
        }

        /**
         * 获取指定键的头部值
         *
         * @param key 头部键
         * @return 头部值（不存在时为 null）
         */
        public String headerValue(String key) {
            Header header = header(key);
            if (null != header) {
                return header.getValue();
            }
            return null;
        }

        /**
         * 获取指定键的头部
         *
         * @param key 头部键
         * @return 头部对象（不存在时为 null）
         */
        public Header header(String key) {
            if (null != headers) {
                for (Header header : headers) {
                    if (header.getKey().equals(key))
                        return header;
                }
            }
            return null;
        }

        /**
         * 编译消息为字符串
         *
         * @param legacyWhitespace 是否使用传统空白字符格式
         * @return 编译后的消息字符串
         */
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

        /**
         * 返回消息的字符串表示
         *
         * @return 包含命令、头部和内容的字符串
         */
        @Override
        public String toString() {
            return "Message {command='" + command + "', headers=" + headers + ", payload='" + payload + "'}";
        }
    }

    /**
     * 订阅者类，管理消息订阅
     */
    class Subscriber {

        /**
         * 订阅者唯一标识
         */
        private final String id;
        /**
         * 订阅目标地址
         */
        private final String destination;
        /**
         * 消息回调
         */
        private final Callback<Message> callback;
        /**
         * 附加头部信息
         */
        private final List<Header> headers;
        /**
         * 是否已订阅
         */
        private boolean subscribed;

        /**
         * 构造函数，初始化订阅者
         *
         * @param id          订阅者标识
         * @param destination 订阅地址
         * @param callback    消息回调
         * @param headers     附加头部
         */
        Subscriber(String id, String destination, Callback<Message> callback, List<Header> headers) {
            this.id = id;
            this.destination = destination;
            this.callback = callback;
            this.headers = headers;
        }

        /**
         * 执行订阅操作
         */
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

        /**
         * 取消订阅
         */
        void unsubscribe() {
            List<Header> headers = Collections.singletonList(new Header(Header.ID, id));
            send(new Message(Builder.UNSUBSCRIBE, headers, null));
            subscribed = false;
        }
    }

}