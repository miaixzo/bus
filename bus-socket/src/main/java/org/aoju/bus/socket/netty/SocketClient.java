/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.socket.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.5.0
 * @since JDK 1.8+
 */
public class SocketClient {

    private Channel channel;
    private List<String> topics = new ArrayList<>();
    private Long lastUpdateTime = System.currentTimeMillis();
    private Long inactiveTime = 60000L;

    public SocketClient(Channel channel) {
        this.channel = channel;
    }

    public void send(String topic, String message) {
        if (this.topics.contains(topic)) {
            channel.writeAndFlush(new TextWebSocketFrame(message));
            lastUpdateTime = System.currentTimeMillis();
        }
    }

    public void send(String message) {
        channel.writeAndFlush(new TextWebSocketFrame(message));
        lastUpdateTime = System.currentTimeMillis();
    }

    public void sendHeartbeat() {
        channel.writeAndFlush(new TextWebSocketFrame(NettyConsts.HEARTBEAT_TEXT));
    }

    public void receiveHeartbeat() {
        lastUpdateTime = System.currentTimeMillis();
    }

    public void subscribe(String topic) {
        if (!this.topics.contains(topic)) {
            this.topics.add(topic);
        }
        lastUpdateTime = System.currentTimeMillis();
    }

    public void cancel(String data) {
        for (String topic : topics) {
            SocketService.onCancel(this, topic, data);
        }
        topics.clear();
        lastUpdateTime = System.currentTimeMillis();
    }

    public void cancel(String topic, String data) {
        if (this.topics.contains(topic)) {
            this.topics.remove(topic);
            SocketService.onCancel(this, topic, data);
        }
        lastUpdateTime = System.currentTimeMillis();
    }

    public boolean isActive() {
        return System.currentTimeMillis() - lastUpdateTime <= inactiveTime;
    }

    public boolean needClose() {
        return System.currentTimeMillis() - lastUpdateTime > inactiveTime * 3;
    }

    public void close() {
        channel.close();
    }

}
