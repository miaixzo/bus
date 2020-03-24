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

import org.aoju.bus.logger.Logger;
import org.aoju.bus.socket.origin.AioSession;
import org.aoju.bus.socket.origin.QuickTimer;
import org.aoju.bus.socket.origin.StateMachine;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 服务器运行状态监控插件
 *
 * @author Kimi Liu
 * @version 5.8.0
 * @since JDK 1.8+
 */
public final class MonitorPlugin<T> extends TimerTask implements Plugin<T> {

    /**
     * 任务执行频率
     */
    private int seconds = 0;
    /**
     * 当前周期内消息 流量监控
     */
    private AtomicLong inFlow = new AtomicLong(0);

    /**
     * 当前周期内消息 流量监控
     */
    private AtomicLong outFlow = new AtomicLong(0);

    /**
     * 当前周期内处理失败消息数
     */
    private AtomicLong processFailNum = new AtomicLong(0);

    /**
     * 当前周期内处理消息数
     */
    private AtomicLong processMsgNum = new AtomicLong(0);


    private AtomicLong totleProcessMsgNum = new AtomicLong(0);

    /**
     * 新建连接数
     */
    private AtomicInteger newConnect = new AtomicInteger(0);

    /**
     * 断链数
     */
    private AtomicInteger disConnect = new AtomicInteger(0);

    /**
     * 在线连接数
     */
    private AtomicInteger onlineCount = new AtomicInteger(0);

    private AtomicInteger totalConnect = new AtomicInteger(0);

    public MonitorPlugin() {
        this(60);
    }

    public MonitorPlugin(int seconds) {
        this.seconds = seconds;
        long mills = TimeUnit.SECONDS.toMillis(seconds);
        QuickTimer.getTimer().schedule(this, mills, mills);
    }


    @Override
    public boolean preProcess(AioSession<T> session, T t) {
        processMsgNum.incrementAndGet();
        totleProcessMsgNum.incrementAndGet();
        return true;
    }

    @Override
    public void stateEvent(StateMachine stateMachineEnum, AioSession<T> session, Throwable throwable) {
        switch (stateMachineEnum) {
            case PROCESS_EXCEPTION:
                processFailNum.incrementAndGet();
                break;
            case NEW_SESSION:
                newConnect.incrementAndGet();
                break;
            case SESSION_CLOSED:
                disConnect.incrementAndGet();
                break;
        }
    }

    @Override
    public void run() {
        long curInFlow = inFlow.getAndSet(0);
        long curOutFlow = outFlow.getAndSet(0);
        long curDiscardNum = processFailNum.getAndSet(0);
        long curProcessMsgNum = processMsgNum.getAndSet(0);
        int connectCount = newConnect.getAndSet(0);
        int disConnectCount = disConnect.getAndSet(0);
        Logger.info("\r\n-----这" + seconds + "秒发生了什么----\r\n流入流量:\t\t" + curInFlow * 1.0 / (1024 * 1024) + "(MB)"
                + "\r\n流出流量:\t" + curOutFlow * 1.0 / (1024 * 1024) + "(MB)"
                + "\r\n处理失败消息数:\t" + curDiscardNum
                + "\r\n已处理消息量:\t" + curProcessMsgNum
                + "\r\n已处理消息总量:\t" + totleProcessMsgNum.get()
                + "\r\n新建连接数:\t" + connectCount
                + "\r\n断开连接数:\t" + disConnectCount
                + "\r\n在线连接数:\t" + onlineCount.addAndGet(connectCount - disConnectCount)
                + "\r\n总连接次数:\t" + totalConnect.addAndGet(connectCount)
                + "\r\nRequests/sec:\t" + curProcessMsgNum * 1.0 / seconds
                + "\r\nTransfer/sec:\t" + (curInFlow * 1.0 / (1024 * 1024) / seconds) + "(MB)");
    }

    @Override
    public boolean acceptMonitor(AsynchronousSocketChannel channel) {
        return true;
    }

    @Override
    public void readMonitor(AioSession<T> session, int readSize) {
        //出现result为0,说明代码存在问题
        if (readSize == 0) {
            Logger.error("readSize is 0");
        }
        inFlow.addAndGet(readSize);
    }

    @Override
    public void writeMonitor(AioSession<T> session, int writeSize) {
        outFlow.addAndGet(writeSize);
    }
}
