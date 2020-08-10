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

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.aoju.bus.logger.Logger;
import sun.misc.Signal;
import sun.misc.SignalHandler;

/**
 * 启动后监听
 *
 * @author Kimi Liu
 * @version 6.0.5
 * @since JDK 1.8++
 */
public class NettyServerListener implements ChannelFutureListener {

    private NettyServer nettyServer;

    public NettyServerListener(NettyServer nettyServer) {
        this.nettyServer = nettyServer;
    }

    @Override
    public void operationComplete(ChannelFuture future) {
        this.addShutdownListner();
    }

    // 进程关闭后监听
    protected void addShutdownListner() {
        SignalHandler signalHandler = signal -> {
            Logger.info("收到进程信号量：{}", signal.getName());
            Thread t = new Thread(new ShutdownHook(), "ShutdownHookServer-Thread");
            Runtime.getRuntime().addShutdownHook(t);
            Runtime.getRuntime().exit(0);
        };
        Signal.handle(new Signal(getOSSignalKill()), signalHandler);
    }

    private String getOSSignalKill() {
        return System.getProperties().getProperty("os.name").
                toLowerCase().startsWith("win")
                ? System.getProperty("term.sig", "INT") //windows下 Ctrl + C
                : System.getProperty("term.sig", "USR2"); // Linux下（等价于kill -12 pid
    }

    class ShutdownHook implements Runnable {
        @Override
        public void run() {
            Logger.info("进程退出，关闭Netty服务端...");
            nettyServer.shutdown();
        }
    }

}
