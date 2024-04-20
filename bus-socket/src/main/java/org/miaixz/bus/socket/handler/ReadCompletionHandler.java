/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.socket.handler;

import org.miaixz.bus.socket.NetMonitor;
import org.miaixz.bus.socket.SocketStatus;
import org.miaixz.bus.socket.TcpAioSession;
import org.miaixz.bus.socket.channel.EnhanceAsynchronousChannelProvider;

import java.nio.channels.CompletionHandler;

/**
 * 读写事件回调处理类
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class ReadCompletionHandler implements CompletionHandler<Integer, TcpAioSession> {

    /**
     * 处理消息读回调事件
     *
     * @param result     已读消息字节数
     * @param aioSession 当前触发读回调的会话
     */
    @Override
    public void completed(final Integer result, final TcpAioSession aioSession) {
        try {
            // 释放缓冲区
            if (result == EnhanceAsynchronousChannelProvider.READ_MONITOR_SIGNAL) {
                aioSession.suspendRead();
                return;
            }
            if (result == EnhanceAsynchronousChannelProvider.READABLE_SIGNAL) {
                aioSession.doRead();
                return;
            }
            // 接收到的消息进行预处理
            NetMonitor monitor = aioSession.getServerConfig().getMonitor();
            if (monitor != null) {
                monitor.afterRead(aioSession, result);
            }
            // 触发读回调
            aioSession.flipRead(result == -1);
            aioSession.signalRead();
        } catch (Exception e) {
            failed(e, aioSession);
        }
    }

    @Override
    public final void failed(Throwable exc, TcpAioSession aioSession) {
        try {
            aioSession.getServerConfig().getProcessor().stateEvent(aioSession, SocketStatus.INPUT_EXCEPTION, exc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            // 兼容性处理，windows要强制关闭,其他系统优雅关闭
            // aioSession.close(IoKit.OS_WINDOWS);
            aioSession.close(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}