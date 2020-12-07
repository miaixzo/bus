/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org sandao and other contributors.               *
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
package org.aoju.bus.socket.plugins;

import org.aoju.bus.logger.Logger;
import org.aoju.bus.socket.AioSession;
import org.aoju.bus.socket.QuickTimer;
import org.aoju.bus.socket.SocketStatus;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * 服务器运行状态监控插件
 *
 * @author Kimi Liu
 * @version 6.1.3
 * @since JDK 1.8+
 */
public class MonitorPlugin<T> extends AbstractPlugin<T> implements Runnable {

    /**
     * 当前周期内消息 流量监控
     */
    private final LongAdder inFlow = new LongAdder();
    /**
     * 当前周期内消息 流量监控
     */
    private final LongAdder outFlow = new LongAdder();
    /**
     * 当前周期内处理失败消息数
     */
    private final LongAdder processFailNum = new LongAdder();
    /**
     * 当前周期内处理消息数
     */
    private final LongAdder processMsgNum = new LongAdder();
    private final LongAdder totleProcessMsgNum = new LongAdder();
    /**
     * 新建连接数
     */
    private final LongAdder newConnect = new LongAdder();
    /**
     * 断链数
     */
    private final LongAdder disConnect = new LongAdder();
    private final LongAdder totalConnect = new LongAdder();
    private final LongAdder readCount = new LongAdder();
    private final LongAdder writeCount = new LongAdder();
    /**
     * 任务执行频率
     */
    private final int seconds;
    /**
     * 在线连接数
     */
    private long onlineCount;

    public MonitorPlugin() {
        this(60);
    }

    public MonitorPlugin(int seconds) {
        this.seconds = seconds;
        long mills = TimeUnit.SECONDS.toMillis(seconds);
        QuickTimer.scheduleAtFixedRate(this, mills, mills);
    }


    @Override
    public boolean preProcess(AioSession session, T t) {
        processMsgNum.increment();
        totleProcessMsgNum.increment();
        return true;
    }

    @Override
    public void stateEvent(SocketStatus socketStatus, AioSession session, Throwable throwable) {
        switch (socketStatus) {
            case PROCESS_EXCEPTION:
                processFailNum.increment();
                break;
            case NEW_SESSION:
                newConnect.increment();
                break;
            case SESSION_CLOSED:
                disConnect.increment();
                break;
            default:
                break;
        }
    }

    @Override
    public void run() {
        long curInFlow = getAndReset(inFlow);
        long curOutFlow = getAndReset(outFlow);
        long curDiscardNum = getAndReset(processFailNum);
        long curProcessMsgNum = getAndReset(processMsgNum);
        long connectCount = getAndReset(newConnect);
        long disConnectCount = getAndReset(disConnect);
        onlineCount += connectCount - disConnectCount;
        Logger.info("\r\n-----" + seconds + "seconds ----\r\ninflow:\t\t" + curInFlow * 1.0 / (1024 * 1024) + "(MB)"
                + "\r\noutflow:\t" + curOutFlow * 1.0 / (1024 * 1024) + "(MB)"
                + "\r\nprocess fail:\t" + curDiscardNum
                + "\r\nprocess success:\t" + curProcessMsgNum
                + "\r\nprocess total:\t" + totleProcessMsgNum.longValue()
                + "\r\nread count:\t" + getAndReset(readCount) + "\twrite count:\t" + getAndReset(writeCount)
                + "\r\nconnect count:\t" + connectCount
                + "\r\ndisconnect count:\t" + disConnectCount
                + "\r\nonline count:\t" + onlineCount
                + "\r\nconnected total:\t" + getAndReset(totalConnect)
                + "\r\nRequests/sec:\t" + curProcessMsgNum * 1.0 / seconds
                + "\r\nTransfer/sec:\t" + (curInFlow * 1.0 / (1024 * 1024) / seconds) + "(MB)");
    }

    private long getAndReset(LongAdder longAdder) {
        long result = longAdder.longValue();
        longAdder.add(-result);
        return result;
    }

    @Override
    public void afterRead(AioSession session, int readSize) {
        if (readSize == 0) {
            Logger.error("readSize is 0");
        }
        inFlow.add(readSize);
    }

    @Override
    public void beforeRead(AioSession session) {
        readCount.increment();
    }

    @Override
    public void afterWrite(AioSession session, int writeSize) {
        outFlow.add(writeSize);
    }

    @Override
    public void beforeWrite(AioSession session) {
        writeCount.increment();
    }

}
