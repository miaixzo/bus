/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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

import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

/**
 * @author Kimi Liu
 * @version 3.6.5
 * @since JDK 1.8
 */
public class ChannelInitializer extends io.netty.channel.ChannelInitializer<SocketChannel> {

    private String endPoint;

    public ChannelInitializer(String endPoint) {
        this.endPoint = endPoint;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        //HttpServerCodec: 针对http协议进行编解码
        pipeline.addLast("httpServerCodec", new HttpServerCodec());
        //ChunkedWriteHandler分块写处理，文件过大会将内存撑爆
        pipeline.addLast("chunkedWriteHandler", new ChunkedWriteHandler());
        //作用是将一个Http的消息组装成一个HttpRequest或者HttpResponse, 该Handler必须放在HttpServerCodec后的后面
        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(8192));
        //用于处理websocket, /ws为访问websocket时的uri
        pipeline.addLast("webSocketServerProtocolHandler", new WebSocketServerProtocolHandler(endPoint));
        //自定义处理器
        pipeline.addLast("myWebSocketHandler", new SocketHandler());
    }

}
