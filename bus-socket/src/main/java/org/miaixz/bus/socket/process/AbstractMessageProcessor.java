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
package org.miaixz.bus.socket.process;

import org.miaixz.bus.socket.AioSession;
import org.miaixz.bus.socket.NetMonitor;
import org.miaixz.bus.socket.SocketStatus;
import org.miaixz.bus.socket.plugins.Plugin;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractMessageProcessor<T> implements MessageProcessor<T>, NetMonitor {

    private final List<Plugin<T>> plugins = new ArrayList<>();

    @Override
    public final void afterRead(AioSession session, int readSize) {
        for (Plugin<T> plugin : plugins) {
            plugin.afterRead(session, readSize);
        }
    }

    @Override
    public final void afterWrite(AioSession session, int writeSize) {
        for (Plugin<T> plugin : plugins) {
            plugin.afterWrite(session, writeSize);
        }
    }

    @Override
    public final void beforeRead(AioSession session) {
        for (Plugin<T> plugin : plugins) {
            plugin.beforeRead(session);
        }
    }

    @Override
    public final void beforeWrite(AioSession session) {
        for (Plugin<T> plugin : plugins) {
            plugin.beforeWrite(session);
        }
    }

    @Override
    public final AsynchronousSocketChannel shouldAccept(AsynchronousSocketChannel channel) {
        AsynchronousSocketChannel acceptChannel = channel;
        for (Plugin<T> plugin : plugins) {
            acceptChannel = plugin.shouldAccept(acceptChannel);
            if (acceptChannel == null) {
                return null;
            }
        }
        return acceptChannel;
    }

    @Override
    public final void process(AioSession session, T msg) {
        boolean flag = true;
        for (Plugin<T> plugin : plugins) {
            if (!plugin.preProcess(session, msg)) {
                flag = false;
            }
        }
        if (flag) {
            process0(session, msg);
        }
    }

    /**
     * 处理接收到的消息
     *
     * @param session 会话
     * @param msg     消息信息
     * @see MessageProcessor#process(AioSession, Object)
     */
    public abstract void process0(AioSession session, T msg);

    /**
     * @param session      本次触发状态机的AioSession对象
     * @param socketStatus 状态枚举
     * @param throwable    异常对象，如果存在的话
     */
    @Override
    public final void stateEvent(AioSession session, SocketStatus socketStatus, Throwable throwable) {
        for (Plugin<T> plugin : plugins) {
            plugin.stateEvent(socketStatus, session, throwable);
        }
        stateEvent0(session, socketStatus, throwable);
    }

    /**
     * @param session
     * @param socketStatus
     * @param throwable
     * @see #stateEvent(AioSession, SocketStatus, Throwable)
     */
    public abstract void stateEvent0(AioSession session, SocketStatus socketStatus, Throwable throwable);

    public final void addPlugin(Plugin<T> plugin) {
        this.plugins.add(plugin);
    }

}
