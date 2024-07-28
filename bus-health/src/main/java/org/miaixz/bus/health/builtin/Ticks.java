/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.builtin;

import org.miaixz.bus.core.xyz.ThreadKit;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;

/**
 * CPU负载时间信息
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class Ticks {

    long idle;
    long nice;
    long irq;
    long softIrq;
    long steal;
    long cSys;
    long user;
    long ioWait;

    /**
     * 构造，等待时间为用于计算在一定时长内的CPU负载情况，如传入1000表示最近1秒的负载情况
     *
     * @param processor   {@link CentralProcessor}
     * @param waitingTime 设置等待时间，单位毫秒
     */
    public Ticks(CentralProcessor processor, long waitingTime) {
        // CPU信息
        final long[] prevTicks = processor.getSystemCpuLoadTicks();
        // 这里必须要设置延迟
        ThreadKit.sleep(waitingTime);
        final long[] ticks = processor.getSystemCpuLoadTicks();

        this.idle = tick(prevTicks, ticks, CentralProcessor.TickType.IDLE);
        this.nice = tick(prevTicks, ticks, CentralProcessor.TickType.NICE);
        this.irq = tick(prevTicks, ticks, CentralProcessor.TickType.IRQ);
        this.softIrq = tick(prevTicks, ticks, CentralProcessor.TickType.SOFTIRQ);
        this.steal = tick(prevTicks, ticks, CentralProcessor.TickType.STEAL);
        this.cSys = tick(prevTicks, ticks, CentralProcessor.TickType.SYSTEM);
        this.user = tick(prevTicks, ticks, CentralProcessor.TickType.USER);
        this.ioWait = tick(prevTicks, ticks, CentralProcessor.TickType.IOWAIT);
    }

    /**
     * 获取一段时间内的CPU负载标记差
     *
     * @param prevTicks 开始的ticks
     * @param ticks     结束的ticks
     * @param tickType  tick类型
     * @return 标记差
     */
    private static long tick(long[] prevTicks, long[] ticks, CentralProcessor.TickType tickType) {
        return ticks[tickType.getIndex()] - prevTicks[tickType.getIndex()];
    }

    public long getIdle() {
        return idle;
    }

    public void setIdle(long idle) {
        this.idle = idle;
    }

    public long getNice() {
        return nice;
    }

    public void setNice(long nice) {
        this.nice = nice;
    }

    public long getIrq() {
        return irq;
    }

    public void setIrq(long irq) {
        this.irq = irq;
    }

    public long getSoftIrq() {
        return softIrq;
    }

    public void setSoftIrq(long softIrq) {
        this.softIrq = softIrq;
    }

    public long getSteal() {
        return steal;
    }

    public void setSteal(long steal) {
        this.steal = steal;
    }

    public long getcSys() {
        return cSys;
    }

    public void setcSys(long cSys) {
        this.cSys = cSys;
    }

    public long getUser() {
        return user;
    }

    public void setUser(long user) {
        this.user = user;
    }

    public long getIoWait() {
        return ioWait;
    }

    public void setIoWait(long ioWait) {
        this.ioWait = ioWait;
    }

    /**
     * 获取CPU总的使用率
     *
     * @return CPU总使用率
     */
    public long totalCpu() {
        return Math.max(user + nice + cSys + idle + ioWait + irq + softIrq + steal, 0);
    }

    @Override
    public String toString() {
        return "Ticks{" + "idle=" + idle + ", nice=" + nice + ", irq=" + irq + ", softIrq=" + softIrq + ", steal="
                + steal + ", cSys=" + cSys + ", user=" + user + ", ioWait=" + ioWait + '}';
    }

}
