/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org sandao and other contributors.               *
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
package org.aoju.bus.socket;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.socket.process.MessageProcessor;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * UDP消息分发器
 *
 * @author Kimi Liu
 * @version 6.5.0
 * @since Java 17+
 */
public class UdpDispatcher implements Runnable {

    public final static RequestTask EXECUTE_TASK_OR_SHUTDOWN = new RequestTask(null, null);
    private final BlockingQueue<RequestTask> taskQueue = new LinkedBlockingQueue<>();
    private final MessageProcessor processor;

    public UdpDispatcher(MessageProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void run() {
        while (true) {
            try {
                RequestTask unit = taskQueue.take();
                if (unit == EXECUTE_TASK_OR_SHUTDOWN) {
                    Logger.info("shutdown thread:{}", Thread.currentThread());
                    break;
                }
                processor.process(unit.session, unit.request);
                if (!unit.session.isInvalid()) {
                    unit.session.writeBuffer().flush();
                }
            } catch (InterruptedException e) {
                Logger.info("InterruptedException", e);
            } catch (Exception e) {
                Logger.error(e.getClass().getName(), e);
            }
        }
    }

    /**
     * 任务分发
     *
     * @param session the session
     * @param request the request
     */
    public void dispatch(UdpAioSession session, Object request) {
        dispatch(new RequestTask(session, request));
    }

    /**
     * 任务分发
     *
     * @param requestTask the requestTask
     */
    public void dispatch(RequestTask requestTask) {
        taskQueue.offer(requestTask);
    }

    static class RequestTask {
        UdpAioSession session;
        Object request;

        public RequestTask(UdpAioSession session, Object request) {
            this.session = session;
            this.request = request;
        }
    }

}
