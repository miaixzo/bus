/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org sandao and other contributors.             ~
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
package org.miaixz.bus.socket.plugin;

import org.miaixz.bus.logger.Logger;
import org.miaixz.bus.socket.Session;
import org.miaixz.bus.socket.Status;
import org.miaixz.bus.socket.metric.HashedWheelTimer;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

/**
 * A plugin for monitoring the runtime status of a server.
 * <p>
 * This plugin collects and logs various statistics about network traffic, message processing, and connection status at
 * a specified interval.
 * </p>
 *
 * @param <T> the type of message object entity handled by this plugin
 * @author Kimi Liu
 * @since Java 17+
 */
public final class MonitorPlugin<T> extends AbstractPlugin<T> implements Runnable {

    /**
     * The number of bytes flowed into the server during the current monitoring cycle.
     */
    private final LongAdder inFlow = new LongAdder();
    /**
     * The number of bytes flowed out of the server during the current monitoring cycle.
     */
    private final LongAdder outFlow = new LongAdder();
    /**
     * The number of messages that failed to be processed during the current monitoring cycle.
     */
    private final LongAdder processFailNum = new LongAdder();
    /**
     * The number of messages processed during the current monitoring cycle.
     */
    private final LongAdder processMsgNum = new LongAdder();
    /**
     * The number of new connections established during the current monitoring cycle.
     */
    private final LongAdder newConnect = new LongAdder();
    /**
     * The number of connections disconnected during the current monitoring cycle.
     */
    private final LongAdder disConnect = new LongAdder();
    /**
     * The number of read operations performed during the current monitoring cycle.
     */
    private final LongAdder readCount = new LongAdder();
    /**
     * The number of write operations performed during the current monitoring cycle.
     */
    private final LongAdder writeCount = new LongAdder();
    /**
     * The frequency (in seconds) at which the monitoring task is executed.
     */
    private final int seconds;
    /**
     * A flag indicating whether the monitor is for UDP connections.
     */
    private final boolean udp;
    /**
     * The cumulative total number of connections since the plugin was enabled.
     */
    private long totalConnect;
    /**
     * The cumulative total number of messages processed since the plugin was enabled.
     */
    private long totalProcessMsgNum = 0;
    /**
     * The current number of online connections.
     */
    private long onlineCount;

    /**
     * Constructs a {@code MonitorPlugin} with a default monitoring interval of 60 seconds.
     */
    public MonitorPlugin() {
        this(60);
    }

    /**
     * Constructs a {@code MonitorPlugin} with a specified monitoring interval.
     *
     * @param seconds the monitoring interval in seconds
     */
    public MonitorPlugin(int seconds) {
        this(seconds, false);
    }

    /**
     * Constructs a {@code MonitorPlugin} with a specified monitoring interval and UDP flag.
     *
     * @param seconds the monitoring interval in seconds
     * @param udp     {@code true} if monitoring UDP connections, {@code false} for TCP
     */
    public MonitorPlugin(int seconds, boolean udp) {
        this.seconds = seconds;
        this.udp = udp;
        HashedWheelTimer.DEFAULT_TIMER.scheduleWithFixedDelay(this, seconds, TimeUnit.SECONDS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Session session, T data) {
        processMsgNum.increment();
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stateEvent(Status status, Session session, Throwable throwable) {
        switch (status) {
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        long curInFlow = getAndReset(inFlow);
        long curOutFlow = getAndReset(outFlow);
        long curDiscardNum = getAndReset(processFailNum);
        long curProcessMsgNum = getAndReset(processMsgNum);
        long connectCount = getAndReset(newConnect);
        long disConnectCount = getAndReset(disConnect);
        long curReadCount = getAndReset(readCount);
        long curWriteCount = getAndReset(writeCount);
        onlineCount += connectCount - disConnectCount;
        totalProcessMsgNum += curProcessMsgNum;
        totalConnect += connectCount;
        Logger.info(
                "\r\n-----" + seconds + " seconds ----\r\nInflow:\t\t"
                        + String.format("%.2f", curInFlow * 1.0 / (1024 * 1024)) + "(MB)" + "\r\nOutflow:\t"
                        + String.format("%.2f", curOutFlow * 1.0 / (1024 * 1024)) + "(MB)" + "\r\nProcess Fail:\t"
                        + curDiscardNum + "\r\nProcess Count:\t" + curProcessMsgNum + "\r\nProcess Total:\t"
                        + totalProcessMsgNum + "\r\nRead Count:\t" + curReadCount + "\tWrite Count:\t" + curWriteCount
                        + (udp ? ""
                                : "\r\nConnect Count:\t" + connectCount + "\r\nDisconnect Count:\t" + disConnectCount
                                        + "\r\nOnline Count:\t" + onlineCount + "\r\nConnected Total:\t" + totalConnect)
                        + "\r\nRequests/sec:\t" + String.format("%.2f", curProcessMsgNum * 1.0 / seconds)
                        + "\r\nTransfer/sec:\t" + String.format("%.2f", (curInFlow * 1.0 / (1024 * 1024) / seconds))
                        + "(MB)");
    }

    /**
     * Gets the current value of a {@link LongAdder} and resets it to zero.
     *
     * @param longAdder the {@link LongAdder} to operate on
     * @return the value of the {@link LongAdder} before it was reset
     */
    private long getAndReset(LongAdder longAdder) {
        long result = longAdder.longValue();
        longAdder.add(-result);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterRead(Session session, int readSize) {
        // If readSize is 0, it indicates a potential issue in the code
        if (readSize == 0) {
            Logger.error("Read size is 0, potential issue detected.");
        }
        inFlow.add(readSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeRead(Session session) {
        readCount.increment();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterWrite(Session session, int writeSize) {
        outFlow.add(writeSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void beforeWrite(Session session) {
        writeCount.increment();
    }

}
