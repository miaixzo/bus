/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.http.plugin.httpv.CoverCall;
import org.miaixz.bus.logger.Logger;

import java.util.*;

/**
 * A STOMP protocol client over WebSocket.
 * <p>
 * This class provides functionality for connecting to a STOMP server, sending messages, subscribing to topics and
 * queues, and acknowledging messages. It supports both automatic and client-side message acknowledgment.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Stomp {

    /**
     * The supported STOMP protocol versions.
     */
    public static final String SUPPORTED_VERSIONS = "1.1,1.2";
    /**
     * The automatic acknowledgment mode.
     */
    public static final String AUTO_ACK = "auto";
    /**
     * The client-side acknowledgment mode.
     */
    public static final String CLIENT_ACK = "client";
    /**
     * The prefix for topic subscriptions.
     */
    private static final String TOPIC = "/topic";
    /**
     * The prefix for queue subscriptions.
     */
    private static final String QUEUE = "/queue";
    /**
     * Whether to automatically acknowledge messages.
     */
    private final boolean autoAck;
    /**
     * The underlying WebSocket client.
     */
    private final CoverCall.Client cover;
    /**
     * A map of subscribers, keyed by destination.
     */
    private final Map<String, Subscriber> subscribers;
    /**
     * Whether the client is connected to the server.
     */
    private boolean connected;
    /**
     * The WebSocket connection instance.
     */
    private CoverCall websocket;
    /**
     * Whether to use legacy whitespace formatting.
     */
    private boolean legacyWhitespace = false;
    /**
     * The callback for when the connection is successfully established.
     */
    private Callback<Stomp> onConnected;
    /**
     * The callback for when the connection is disconnected.
     */
    private Callback<CoverCall.Close> onDisconnected;
    /**
     * The callback for error messages.
     */
    private Callback<Message> onError;

    /**
     * Constructs a new STOMP client.
     *
     * @param cover   The underlying WebSocket client.
     * @param autoAck Whether to automatically acknowledge messages.
     */
    private Stomp(CoverCall.Client cover, boolean autoAck) {
        this.cover = cover;
        this.autoAck = autoAck;
        this.subscribers = new HashMap<>();
    }

    /**
     * Creates a STOMP client with automatic message acknowledgment.
     *
     * @param task The underlying WebSocket client.
     * @return a new {@link Stomp} instance.
     */
    public static Stomp over(CoverCall.Client task) {
        return over(task, true);
    }

    /**
     * Creates a STOMP client with a specified acknowledgment mode.
     *
     * @param task    The underlying WebSocket client.
     * @param autoAck Whether to automatically acknowledge messages.
     * @return a new {@link Stomp} instance.
     */
    public static Stomp over(CoverCall.Client task, boolean autoAck) {
        return new Stomp(task, autoAck);
    }

    /**
     * Connects to the STOMP server.
     *
     * @return this {@link Stomp} instance.
     */
    public Stomp connect() {
        return connect(null);
    }

    /**
     * Connects to the STOMP server with the specified headers.
     *
     * @param headers The STOMP headers for the connection.
     * @return this {@link Stomp} instance.
     */
    public Stomp connect(List<Header> headers) {
        if (connected) {
            return this;
        }
        cover.setOnOpen((ws, res) -> {
            List<Header> cHeaders = new ArrayList<>();
            cHeaders.add(new Header(Header.VERSION, SUPPORTED_VERSIONS));
            cHeaders.add(
                    new Header(Header.HEART_BEAT,
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
     * Disconnects from the STOMP server.
     */
    public void disconnect() {
        if (null != websocket) {
            websocket.close(1000, "disconnect by user");
        }
    }

    /**
     * Sets the callback for when the connection is successfully established.
     *
     * @param onConnected The connection callback.
     * @return this {@link Stomp} instance.
     */
    public Stomp setOnConnected(Callback<Stomp> onConnected) {
        this.onConnected = onConnected;
        return this;
    }

    /**
     * Sets the callback for when the connection is disconnected.
     *
     * @param onDisconnected The disconnection callback.
     * @return this {@link Stomp} instance.
     */
    public Stomp setOnDisconnected(Callback<CoverCall.Close> onDisconnected) {
        this.onDisconnected = onDisconnected;
        return this;
    }

    /**
     * Sets the callback for error messages.
     *
     * @param onError The error callback.
     * @return this {@link Stomp} instance.
     */
    public Stomp setOnError(Callback<Message> onError) {
        this.onError = onError;
        return this;
    }

    /**
     * Sends a message to the specified destination.
     *
     * @param destination The destination to send the message to.
     * @param data        The message payload.
     */
    public void sendTo(String destination, String data) {
        send(new Message(Builder.SEND, Collections.singletonList(new Header(Header.DESTINATION, destination)), data));
    }

    /**
     * Sends a STOMP message to the server.
     *
     * @param message The STOMP message to send.
     * @throws IllegalArgumentException if the {@code connect} method has not been called.
     */
    public void send(Message message) {
        if (null == websocket) {
            throw new IllegalArgumentException("You must call connect before send");
        }
        websocket.send(message.compile(legacyWhitespace));
    }

    /**
     * Subscribes to a topic.
     *
     * @param destination The topic destination.
     * @param callback    The callback for received messages.
     * @return this {@link Stomp} instance.
     */
    public Stomp topic(String destination, Callback<Message> callback) {
        return topic(destination, null, callback);
    }

    /**
     * Subscribes to a topic with additional headers.
     *
     * @param destination The topic destination.
     * @param headers     Additional headers for the subscription.
     * @param callback    The callback for received messages.
     * @return this {@link Stomp} instance.
     */
    public Stomp topic(String destination, List<Header> headers, Callback<Message> callback) {
        return subscribe(TOPIC + destination, headers, callback);
    }

    /**
     * Subscribes to a queue.
     *
     * @param destination The queue destination.
     * @param callback    The callback for received messages.
     * @return this {@link Stomp} instance.
     */
    public Stomp queue(String destination, Callback<Message> callback) {
        return queue(destination, null, callback);
    }

    /**
     * Subscribes to a queue with additional headers.
     *
     * @param destination The queue destination.
     * @param headers     Additional headers for the subscription.
     * @param callback    The callback for received messages.
     * @return this {@link Stomp} instance.
     */
    public Stomp queue(String destination, List<Header> headers, Callback<Message> callback) {
        return subscribe(QUEUE + destination, headers, callback);
    }

    /**
     * Subscribes to a destination.
     *
     * @param destination The destination to subscribe to.
     * @param headers     Additional headers for the subscription.
     * @param callback    The callback for received messages.
     * @return this {@link Stomp} instance.
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
     * Acknowledges the receipt of a message.
     *
     * @param message The message received from the server.
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
     * Unsubscribes from a topic.
     *
     * @param destination The topic destination.
     */
    public void untopic(String destination) {
        unsubscribe(TOPIC + destination);
    }

    /**
     * Unsubscribes from a queue.
     *
     * @param destination The queue destination.
     */
    public void unqueue(String destination) {
        unsubscribe(QUEUE + destination);
    }

    /**
     * Unsubscribes from a destination.
     *
     * @param destination The destination to unsubscribe from.
     */
    public synchronized void unsubscribe(String destination) {
        Subscriber subscriber = subscribers.remove(destination);
        if (null != subscriber) {
            subscriber.unsubscribe();
        }
    }

    /**
     * Handles received STOMP messages.
     *
     * @param msg The received STOMP message.
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
     * Sets whether to use legacy whitespace formatting.
     *
     * @param legacyWhitespace {@code true} to enable legacy formatting.
     */
    public void setLegacyWhitespace(boolean legacyWhitespace) {
        this.legacyWhitespace = legacyWhitespace;
    }

    /**
     * Represents a STOMP header.
     */
    public static class Header {

        /**
         * The STOMP protocol version header.
         */
        public static final String VERSION = "accept-version";
        /**
         * The heart-beat interval header.
         */
        public static final String HEART_BEAT = "heart-beat";
        /**
         * The destination header.
         */
        public static final String DESTINATION = "destination";
        /**
         * The message ID header.
         */
        public static final String MESSAGE_ID = "message-id";
        /**
         * The subscription ID header.
         */
        public static final String ID = "id";
        /**
         * The subscription identifier header.
         */
        public static final String SUBSCRIPTION = "subscription";
        /**
         * The acknowledgment mode header.
         */
        public static final String ACK = "ack";

        /**
         * The header key.
         */
        private final String key;
        /**
         * The header value.
         */
        private final String value;

        /**
         * Constructs a new header.
         *
         * @param key   The header key.
         * @param value The header value.
         */
        public Header(String key, String value) {
            this.key = key;
            this.value = value;
        }

        /**
         * Gets the header key.
         *
         * @return The header key.
         */
        public String getKey() {
            return key;
        }

        /**
         * Gets the header value.
         *
         * @return The header value.
         */
        public String getValue() {
            return value;
        }

        /**
         * Returns the string representation of the header.
         *
         * @return a key-value string in the format "key:value".
         */
        @Override
        public String toString() {
            return key + Symbol.C_COLON + value;
        }
    }

    /**
     * Represents a STOMP message.
     */
    public static class Message {

        /**
         * The message command.
         */
        private final String command;
        /**
         * The list of message headers.
         */
        private final List<Header> headers;
        /**
         * The message payload.
         */
        private final String payload;

        /**
         * Constructs a new STOMP message.
         *
         * @param command The message command.
         * @param headers The list of headers.
         * @param payload The message payload.
         */
        public Message(String command, List<Header> headers, String payload) {
            this.command = command;
            this.headers = headers;
            this.payload = payload;
        }

        /**
         * Parses a STOMP message from a string.
         *
         * @param data The message string.
         * @return The parsed {@link Message} object, or null if invalid.
         */
        public static Message from(String data) {
            if (null == data || data.trim().isEmpty()) {
                return new Message(Normal.UNKNOWN, null, data);
            }

            int cmdIndex = data.indexOf("\n");
            int mhIndex = data.indexOf("\n\n");

            if (cmdIndex >= mhIndex) {
                Logger.error("Invalid STOMP message: " + data);
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
         * Gets the list of message headers.
         *
         * @return The list of headers.
         */
        public List<Header> getHeaders() {
            return headers;
        }

        /**
         * Gets the message payload.
         *
         * @return The message payload.
         */
        public String getPayload() {
            return payload;
        }

        /**
         * Gets the message command.
         *
         * @return The command.
         */
        public String getCommand() {
            return command;
        }

        /**
         * Gets the value of a header by its key.
         *
         * @param key The header key.
         * @return The header value, or null if not found.
         */
        public String headerValue(String key) {
            Header header = header(key);
            if (null != header) {
                return header.getValue();
            }
            return null;
        }

        /**
         * Gets a header by its key.
         *
         * @param key The header key.
         * @return The {@link Header} object, or null if not found.
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
         * Compiles the message into a string.
         *
         * @param legacyWhitespace Whether to use legacy whitespace formatting.
         * @return The compiled message string.
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
         * Returns the string representation of the message.
         *
         * @return a string containing the command, headers, and payload.
         */
        @Override
        public String toString() {
            return "Message {command='" + command + "', headers=" + headers + ", payload='" + payload + "'}";
        }
    }

    /**
     * Represents a subscriber that manages a message subscription.
     */
    class Subscriber {

        /**
         * The unique ID of the subscriber.
         */
        private final String id;
        /**
         * The destination to subscribe to.
         */
        private final String destination;
        /**
         * The callback for received messages.
         */
        private final Callback<Message> callback;
        /**
         * Additional headers for the subscription.
         */
        private final List<Header> headers;
        /**
         * Whether the subscriber is currently subscribed.
         */
        private boolean subscribed;

        /**
         * Constructs a new subscriber.
         *
         * @param id          The subscriber ID.
         * @param destination The destination to subscribe to.
         * @param callback    The message callback.
         * @param headers     Additional headers.
         */
        Subscriber(String id, String destination, Callback<Message> callback, List<Header> headers) {
            this.id = id;
            this.destination = destination;
            this.callback = callback;
            this.headers = headers;
        }

        /**
         * Subscribes to the destination.
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
         * Unsubscribes from the destination.
         */
        void unsubscribe() {
            List<Header> headers = Collections.singletonList(new Header(Header.ID, id));
            send(new Message(Builder.UNSUBSCRIBE, headers, null));
            subscribed = false;
        }
    }

}
