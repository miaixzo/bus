/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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

import com.alibaba.fastjson.JSON;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Kimi Liu
 * @version 5.5.9
 * @since JDK 1.8+
 */
public class SocketService {

    private static Map<String, List<EventHandler>> handlers = new HashMap<>();
    private static List<CustomizeEventHandler> customizeHandlers = new ArrayList<>();

    public static void start(int port) {
        start(port, NettyConsts.END_POINT);
    }

    public static void start(int port, String endPoint) {
        start(port, NettyConsts.BOSS_GROUP_THREADS, NettyConsts.WORKER_GROUP_THREADS, endPoint);
    }

    public static void start(int port, int bossGroupThreads, int workerGroupThreads) {
        start(port, bossGroupThreads, workerGroupThreads, NettyConsts.END_POINT);
    }

    public static void start(int port, int bossGroupThreads, int workerGroupThreads, String endPoint) {
        ChannelExecutor.start();
        new SocketServer(port, bossGroupThreads, workerGroupThreads, endPoint).run();
    }

    public static void addHandler(String topic, EventHandler handler) {
        handlers.computeIfAbsent(topic, k -> new ArrayList<>()).add(handler);
    }

    public static void addCustomizeHandler(CustomizeEventHandler customizeHandler) {
        customizeHandlers.add(customizeHandler);
    }

    public static void onSubscribe(SocketClient client, String topic, String data) {
        List<EventHandler> eventHandlers = handlers.get(topic);
        if (eventHandlers != null && !eventHandlers.isEmpty()) {
            for (EventHandler handler : eventHandlers) {
                String message = handler.onSubscribe(topic, data);
                if (message != null) {
                    client.send(JSON.toJSONString(message));
                }
            }
        }
        for (CustomizeEventHandler customizeHandler : customizeHandlers) {
            if (customizeHandler.equalsTopic(topic)) {
                String message = customizeHandler.onSubscribe(topic, data);
                if (message != null) {
                    client.send(JSON.toJSONString(message));
                }
            }
        }
    }

    public static void onMessage(SocketClient client, String topic, String data) {
        List<EventHandler> eventHandlers = handlers.get(topic);
        if (eventHandlers != null && !eventHandlers.isEmpty()) {
            for (EventHandler handler : eventHandlers) {
                String message = handler.onMessage(topic, data);
                if (message != null) {
                    client.send(JSON.toJSONString(message));
                }
            }
        }
        for (CustomizeEventHandler customizeHandler : customizeHandlers) {
            if (customizeHandler.equalsTopic(topic)) {
                String message = customizeHandler.onMessage(topic, data);
                if (message != null) {
                    client.send(JSON.toJSONString(message));
                }
            }
        }
    }

    public static void onCancel(SocketClient client, String topic, String data) {
        List<EventHandler> eventHandlers = handlers.get(topic);
        if (eventHandlers != null && !eventHandlers.isEmpty()) {
            for (EventHandler handler : eventHandlers) {
                String message = handler.onCancel(topic, data);
                if (message != null) {
                    client.send(JSON.toJSONString(message));
                }
            }
        }
        for (CustomizeEventHandler customizeHandler : customizeHandlers) {
            if (customizeHandler.equalsTopic(topic)) {
                String message = customizeHandler.onCancel(topic, data);
                if (message != null) {
                    client.send(JSON.toJSONString(message));
                }
            }
        }
    }

}
