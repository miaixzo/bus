/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
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
package org.aoju.bus.socket.origin.plugins;

import org.aoju.bus.socket.origin.AioSession;
import org.aoju.bus.socket.origin.Message;
import org.aoju.bus.socket.origin.NetMonitor;
import org.aoju.bus.socket.origin.StateMachine;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public abstract class AbstractMessage<T> implements Message<T>, NetMonitor<T> {

    private List<Plugin<T>> plugins = new ArrayList<>();

    @Override
    public final void readMonitor(AioSession<T> session, int readSize) {
        for (Plugin<T> plugin : plugins) {
            plugin.readMonitor(session, readSize);
        }
    }

    @Override
    public final void writeMonitor(AioSession<T> session, int writeSize) {
        for (Plugin<T> plugin : plugins) {
            plugin.writeMonitor(session, writeSize);
        }
    }

    @Override
    public final boolean acceptMonitor(AsynchronousSocketChannel channel) {
        boolean accept;
        for (Plugin<T> plugin : plugins) {
            accept = plugin.acceptMonitor(channel);
            if (!accept) {
                return accept;
            }
        }
        return true;
    }

    @Override
    public final void process(AioSession<T> session, T msg) {
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

    public abstract void process0(AioSession<T> session, T msg);

    @Override
    public final void stateEvent(AioSession<T> session, StateMachine stateMachineEnum, Throwable throwable) {
        for (Plugin<T> plugin : plugins) {
            plugin.stateEvent(stateMachineEnum, session, throwable);
        }
        stateEvent0(session, stateMachineEnum, throwable);
    }

    public abstract void stateEvent0(AioSession<T> session, StateMachine stateMachineEnum, Throwable throwable);

    public final void addPlugin(Plugin plugin) {
        this.plugins.add(plugin);
    }
}
