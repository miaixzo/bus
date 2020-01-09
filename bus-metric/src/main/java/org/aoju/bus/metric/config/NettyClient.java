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
package org.aoju.bus.metric.config;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Data;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.builtin.ManagerInitializer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * netty客户端
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8++
 */
@Data
public class NettyClient {

    private EventLoopGroup loop = new NioEventLoopGroup();

    private Map<String, NettyProcessor> processorMap;
    private List<ManagerInitializer> initializers;

    private String host;
    private int port;
    private String docUrl;

    public NettyClient(ConfigClient configClient, String host, int port) {
        this.processorMap = configClient.getProcessorMap();
        this.initializers = configClient.getInitializers();
        this.host = host;
        this.port = port;
        this.docUrl = configClient.getLocalServerPort();
    }

    public Bootstrap createBootstrap(Bootstrap bootstrap, EventLoopGroup eventLoop, boolean wait) {
        if (bootstrap != null) {
            Logger.debug("连接Netty服务器，serverIp:{}, serverPort:{}", this.host, this.port);
            final NettyClientHandler handler = new NettyClientHandler(this);
            bootstrap.group(eventLoop);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) {
                    ChannelPipeline pipeline = socketChannel.pipeline();
                    pipeline.addLast("ping", new IdleStateHandler(0, 4, 0, TimeUnit.SECONDS));
                    // 添加Jboss的序列化，编解码工具
                    pipeline.addLast("decoder", MarshallingCodeCFactory.buildMarshallingDecoder());
                    pipeline.addLast("encoder", MarshallingCodeCFactory.buildMarshallingEncoder());
                    pipeline.addLast("handler", handler);
                    pipeline.addLast(new HeartBeatClientHandler());
                }
            });
            bootstrap.remoteAddress(this.host, this.port);
            bootstrap.connect().addListener(new NettyListener(this));
        }
        return bootstrap;
    }

    public void run() {
        createBootstrap(new Bootstrap(), loop, true);
    }

    public void reconnect(EventLoopGroup loop) {
        this.createBootstrap(new Bootstrap(), loop, false);
    }

}
