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

import io.netty.channel.ChannelHandlerContext;

/**
 * @author Kimi Liu
 * @version 5.5.3
 * @since JDK 1.8+
 */
public class ClientService {

    private static ClientMap activeClients = new ClientMap();
    private static ClientGroup group = new ClientGroup();

    public static void active(ChannelHandlerContext context) {
        activeClients.put(context.channel().id(), new SocketClient(context.channel()));
    }

    public static void inactive(ChannelHandlerContext context) {
        activeClients.remove(context.channel().id());
    }

    public static SocketClient getClient(ChannelHandlerContext context) {
        return activeClients.get(context.channel().id());
    }

    public static ClientMap getClients() {
        return activeClients;
    }

    public static SocketClient subscribe(ChannelHandlerContext context, String topic) {
        if (group.containsKey(topic)) {
            ClientMap map = group.get(topic);
            return subscribe(context, topic, map);
        } else {
            ClientMap map = new ClientMap();
            group.put(topic, map);
            return subscribe(context, topic, map);
        }
    }

    private static SocketClient subscribe(ChannelHandlerContext context, String topic, ClientMap map) {
        if (map.containsKey(context.channel().id())) {
            SocketClient client = map.get(context.channel().id());
            client.subscribe(topic);
            return client;
        } else {
            SocketClient client = getClient(context);
            client.subscribe(topic);
            map.put(context.channel().id(), client);
            return client;
        }
    }

    public static void remove(ChannelHandlerContext context) {
        inactive(context);
        for (ClientMap map : group.values()) {
            map.remove(context.channel().id());
        }
    }

    public static void publish(String topic, String message) {
        CommandExecutor.execute(new PublishCommand(topic, message));
    }

    public static ClientGroup getClientGroup() {
        return group;
    }

}
