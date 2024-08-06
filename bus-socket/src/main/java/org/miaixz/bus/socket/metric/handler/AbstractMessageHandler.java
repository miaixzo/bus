/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org sandao and other contributors.             ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.socket.metric.handler;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.socket.*;

/**
 * 抽象消息处理
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public abstract class AbstractMessageHandler<T> implements Handler<T>, Monitor {

    private final List<Plugin<T>> plugins = new ArrayList<>();

    @Override
    public final void afterRead(Session session, int readSize) {
        for (Plugin<T> plugin : plugins) {
            plugin.afterRead(session, readSize);
        }
    }

    @Override
    public final void afterWrite(Session session, int writeSize) {
        for (Plugin<T> plugin : plugins) {
            plugin.afterWrite(session, writeSize);
        }
    }

    @Override
    public final void beforeRead(Session session) {
        for (Plugin<T> plugin : plugins) {
            plugin.beforeRead(session);
        }
    }

    @Override
    public final void beforeWrite(Session session) {
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
    public final void process(Session session, T data) {
        boolean flag = true;
        for (Plugin<T> plugin : plugins) {
            if (!plugin.process(session, data)) {
                flag = false;
            }
        }
        if (flag) {
            process0(session, data);
        }
    }

    /**
     * 处理接收到的消息
     *
     * @param session 会话
     * @param data    消息信息
     * @see Handler#process(Session, Object)
     */
    public abstract void process0(Session session, T data);

    /**
     * @param session   本次触发状态机的Session对象
     * @param status    状态枚举
     * @param throwable 异常对象，如果存在的话
     */
    @Override
    public final void stateEvent(Session session, Status status, Throwable throwable) {
        for (Plugin<T> plugin : plugins) {
            plugin.stateEvent(status, session, throwable);
        }
        stateEvent0(session, status, throwable);
    }

    /**
     * @param session   会话
     * @param status    状态
     * @param throwable 异常
     * @see #stateEvent(Session, Status, Throwable)
     */
    public abstract void stateEvent0(Session session, Status status, Throwable throwable);

    public final void addPlugin(Plugin<T> plugin) {
        this.plugins.add(plugin);
    }

}
