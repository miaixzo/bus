/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.metric.builtin;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.aoju.bus.logger.Logger;

import java.util.concurrent.TimeUnit;

/**
 * Netty服务端
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8++
 */
public class NettyServer {

    /**
     * NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器，Netty 提供了许多不同的
     * EventLoopGroup 的实现用来处理不同的传输。在这个例子中我们实现了一个服务端的应用，
     * 因此会有2个 NioEventLoopGroup 会被使用。第一个经常被叫做‘boss’，用来接收进来的连接。
     * 第二个经常被叫做‘worker’，用来处理已经被接收的连接，一旦‘boss’接收到连接，就会把连接
     * 信息注册到‘worker’上。如何知道多少个线程已经被使用，如何映射到已经创建的 Channel上都
     * 需要依赖于 EventLoopGroup 的实现，并且可以通过构造函数来配置他们的关系
     */
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup workerGroup = new NioEventLoopGroup(2);

    private int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() {
        /**
         * ServerBootstrap 是一个启动 NIO 服务的辅助启动类。你可以在这个服务中直接使
         * 用 Channel， 但是这会是一个复杂的处理过程，在很多情况下你并不需要这样做
         */
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        /**
         * 这里的事件处理类经常会被用来处理一个最近的已经接收的 Channel SimpleChatServerInitializer
         * 继承自ChannelInitializer 是一个特殊的处理类，他的目的是帮助使用者配置一个新的 Channel
         * 也许你想通过增加一些处理类比如SimpleChatServerHandler 来配置一个新的 Channel 或者其对应
         * 的ChannelPipeline 来实现你的网络程序。  当你的程序变的复杂时，可能你会增加更多的处理类到
         * pipline 上，然后提取这些匿名类到最顶层的类上
         */
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("pong", new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS));
                        pipeline.addLast("decoder", MarshallingCodeCFactory.buildMarshallingDecoder());
                        pipeline.addLast("encoder", MarshallingCodeCFactory.buildMarshallingEncoder());
                        pipeline.addLast("handler", new ApiConfigServerHandler());
                        pipeline.addLast("hartbeat", new HeartBeatServerHandler());
                    }
                })
                /**
                 * 你可以设置这里指定的 Channel 实现的配置参数。我们正在写一个TCP/IP 的服务端，因此我们
                 * 被允许设置 socket 的参数选项比如tcpNoDelay 和 keepAlive。 请参考 ChannelOption
                 * 和详细的 ChannelConfig 实现的接口文档以此可以对ChannelOption 的有一个大概的认识
                 */
                .option(ChannelOption.SO_BACKLOG, 128)
                /**
                 * option() 是提供给NioServerSocketChannel 用来接收进来的连接。childOption()
                 * 是提供给由父管道 ServerChannel 接收到的连接，在这个例子中也是 NioServerSocketChannel
                 */
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        /**
         * 我们继续，剩下的就是绑定端口然后启动服务。这里我们在机器上绑定了机器所有
         * 网卡上的 8080 端口当然现在你可以多次调用 bind() 方法(基于不同绑定地址)
         */
        serverBootstrap.bind(port).addListener(new NettyServerListener(this));
        Logger.debug("Service startup successful, Port:{}", this.port);
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

}
