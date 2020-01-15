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
package org.aoju.bus.metric.builtin;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoop;
import org.aoju.bus.core.lang.exception.InstrumentException;
import org.aoju.bus.logger.Logger;
import org.aoju.bus.metric.manual.ManagerInitializer;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * netty连接监听
 *
 * @author Kimi Liu
 * @version 5.5.5
 * @since JDK 1.8++
 */
public class NettyClientListener implements ChannelFutureListener {

    private static volatile long DELAY_SECONDS = 5L;
    private static volatile boolean firstFail = true;
    private static AtomicInteger tryTimes = new AtomicInteger();

    private NettyClient nettyClient;

    public NettyClientListener(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    /**
     * 连接回调
     *
     * @param channelFuture 频道
     */
    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        // 链接成功
        if (channelFuture.isSuccess()) {
            tryTimes.set(0);
            firstFail = false;
        } else { // 链接失败，尝试重新连接
            Logger.info("Netty客户端连接服务器失败，尝试重连({})", tryTimes.incrementAndGet());
            final EventLoop loop = channelFuture.channel().eventLoop();

            // 如果是第一次失败，还是要初始化一下
            if (firstFail) {
                try {
                    this.fireLocalConfig(this.nettyClient.getInitializers());
                    firstFail = false;
                } catch (Exception e) {
                    channelFuture.channel().pipeline().fireExceptionCaught(new InstrumentException(e));
                }
            }

            // 进行重连
            loop.schedule(() -> nettyClient.reconnect(loop), DELAY_SECONDS, TimeUnit.SECONDS);
        }
    }

    public void fireLocalConfig(List<ManagerInitializer> initializers) {
        Logger.info("Netty客户端无法连接到服务器，尝试加载本地配置文件");
        for (ManagerInitializer managerInitializer : initializers) {
            managerInitializer.loadLocal();
        }
    }

}