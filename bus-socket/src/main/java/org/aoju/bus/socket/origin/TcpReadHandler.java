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
package org.aoju.bus.socket.origin;

import org.aoju.bus.core.io.RingBuffer;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.logger.Logger;

import java.nio.channels.CompletionHandler;
import java.util.concurrent.Semaphore;

/**
 * 读写事件回调处理类
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
class TcpReadHandler<T> implements CompletionHandler<Integer, TcpAioSession<T>> {

    /**
     * 读回调资源信号量
     */
    private Semaphore semaphore;
    /**
     * 递归线程标识
     */
    private ThreadLocal<CompletionHandler> recursionThreadLocal = null;

    private RingBuffer<TcpReadEvent> ringBuffer;

    public TcpReadHandler() {
    }

    public TcpReadHandler(final RingBuffer<TcpReadEvent> ringBuffer, final ThreadLocal<CompletionHandler> recursionThreadLocal, Semaphore semaphore) {
        this.semaphore = semaphore;
        this.recursionThreadLocal = recursionThreadLocal;
        this.ringBuffer = ringBuffer;
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    int consumerIndex = ringBuffer.nextReadIndex();
                    TcpReadEvent readEvent = ringBuffer.get(consumerIndex);
                    TcpAioSession aioSession = readEvent.getSession();
                    int size = readEvent.getReadSize();
                    ringBuffer.publishReadIndex(consumerIndex);
                    completed0(size, aioSession);
                } catch (InterruptedException e) {
                    Logger.error(Normal.EMPTY, e);
                }
            }
        }, "bus-socket:DaemonThread");
        t.setDaemon(true);
        t.setPriority(1);
        t.start();
    }

    @Override
    public void completed(final Integer result, final TcpAioSession<T> aioSession) {
        if (recursionThreadLocal == null || recursionThreadLocal.get() != null) {
            completed0(result, aioSession);
            return;
        }

        if (semaphore.tryAcquire()) {
            try {
                recursionThreadLocal.set(this);
                completed0(result, aioSession);
                runRingBufferTask();
            } finally {
                recursionThreadLocal.remove();
                semaphore.release();
            }
        } else {
            try {
                int sequence = ringBuffer.nextWriteIndex();
                TcpReadEvent readEvent = ringBuffer.get(sequence);
                readEvent.setSession(aioSession);
                readEvent.setReadSize(result);
                ringBuffer.publishWriteIndex(sequence);
            } catch (InterruptedException e) {
                Logger.error("InterruptedException", e);
            }
        }

    }

    /**
     * 执行异步队列中的任务
     */
    void runRingBufferTask() {
        if (ringBuffer == null) {
            return;
        }
        int index;
        TcpReadEvent readEvent;
        TcpAioSession<T> aioSession;
        int size;
        while ((index = ringBuffer.tryNextReadIndex()) >= 0) {
            readEvent = ringBuffer.get(index);
            aioSession = readEvent.getSession();
            size = readEvent.getReadSize();
            ringBuffer.publishReadIndex(index);
            completed0(size, aioSession);
        }
    }

    private void completed0(final Integer result, final TcpAioSession<T> aioSession) {
        try {
            // 接收到的消息进行预处理
            NetMonitor<T> monitor = aioSession.getServerConfig().getMonitor();
            if (monitor != null) {
                monitor.readMonitor(aioSession, result);
            }
            aioSession.readFromChannel(result == -1);
        } catch (Exception e) {
            failed(e, aioSession);
        }
    }

    @Override
    public void failed(Throwable exc, TcpAioSession<T> aioSession) {
        try {
            aioSession.getServerConfig().getProcessor().stateEvent(aioSession, StateMachine.INPUT_EXCEPTION, exc);
        } catch (Exception e) {
            Logger.debug(e.getMessage(), e);
        }
        try {
            aioSession.close(false);
        } catch (Exception e) {
            Logger.debug(e.getMessage(), e);
        }
    }

}
