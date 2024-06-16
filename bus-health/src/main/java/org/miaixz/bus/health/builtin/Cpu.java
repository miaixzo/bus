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
 *//*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org and other contributors.                    ~
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

import org.miaixz.bus.core.xyz.MathKit;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;

import java.text.DecimalFormat;

/**
 * @author Kimi Liu
 * @since Java 17+
 */
public class Cpu {

    private static final DecimalFormat LOAD_FORMAT = new DecimalFormat("#.00");

    /**
     * CPU核心数
     */
    private Integer cpuNum;

    /**
     * CPU总的使用率
     */
    private double toTal;

    /**
     * CPU系统使用率
     */
    private double sys;

    /**
     * CPU用户使用率
     */
    private double user;

    /**
     * CPU当前等待率
     */
    private double wait;

    /**
     * CPU当前空闲率
     */
    private double free;

    /**
     * CPU型号信息
     */
    private String cpuModel;

    /**
     * CPU型号信息
     */
    private Ticks ticks;

    /**
     * 空构造
     */
    public Cpu() {

    }

    /**
     * 构造，等待时间为用于计算在一定时长内的CPU负载情况，如传入1000表示最近1秒的负载情况
     *
     * @param processor   {@link CentralProcessor}
     * @param waitingTime 设置等待时间，单位毫秒
     */
    public Cpu(CentralProcessor processor, long waitingTime) {
        init(processor, waitingTime);
    }

    /**
     * 构造
     *
     * @param cpuNum   CPU核心数
     * @param toTal    CPU总的使用率
     * @param sys      CPU系统使用率
     * @param user     CPU用户使用率
     * @param wait     CPU当前等待率
     * @param free     CPU当前空闲率
     * @param cpuModel CPU型号信息
     */
    public Cpu(Integer cpuNum, double toTal, double sys, double user, double wait, double free, String cpuModel) {
        this.cpuNum = cpuNum;
        this.toTal = toTal;
        this.sys = sys;
        this.user = user;
        this.wait = wait;
        this.free = free;
        this.cpuModel = cpuModel;
    }

    /**
     * 获取每个CPU核心的tick，计算方式为 100 * tick / totalCpu
     *
     * @param tick     tick
     * @param totalCpu CPU总数
     * @return 平均每个CPU核心的tick
     */
    private static double formatDouble(long tick, long totalCpu) {
        if (0 == totalCpu) {
            return 0D;
        }
        return Double.parseDouble(LOAD_FORMAT.format(tick <= 0 ? 0 : (100d * tick / totalCpu)));
    }

    public Integer getCpuNum() {
        return cpuNum;
    }

    public void setCpuNum(Integer cpuNum) {
        this.cpuNum = cpuNum;
    }

    public double getToTal() {
        return toTal;
    }

    public void setToTal(double toTal) {
        this.toTal = toTal;
    }

    public double getSys() {
        return sys;
    }

    public void setSys(double sys) {
        this.sys = sys;
    }

    public double getUser() {
        return user;
    }

    public void setUser(double user) {
        this.user = user;
    }

    public double getWait() {
        return wait;
    }

    public void setWait(double wait) {
        this.wait = wait;
    }

    public double getFree() {
        return free;
    }

    public void setFree(double free) {
        this.free = free;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public Ticks getTicks() {
        return ticks;
    }

    public void setTicks(Ticks ticks) {
        this.ticks = ticks;
    }

    /**
     * 获取用户+系统的总的CPU使用率
     *
     * @return 总CPU使用率
     */
    public double getUsed() {
        return MathKit.sub(100, this.free).doubleValue();
    }

    @Override
    public String toString() {
        return "Cpu{" +
                "CPU核心数=" + cpuNum +
                ", CPU总的使用率=" + toTal +
                ", CPU系统使用率=" + sys +
                ", CPU用户使用率=" + user +
                ", CPU当前等待率=" + wait +
                ", CPU当前空闲率=" + free +
                ", CPU利用率=" + getUsed() +
                ", CPU型号信息='" + cpuModel + '\'' +
                '}';
    }

    /**
     * 获取指定等待时间内系统CPU 系统使用率、用户使用率、利用率等等 相关信息
     *
     * @param processor   {@link CentralProcessor}
     * @param waitingTime 设置等待时间，单位毫秒
     */
    private void init(CentralProcessor processor, long waitingTime) {
        final Ticks ticks = new Ticks(processor, waitingTime);
        this.ticks = ticks;

        this.cpuNum = processor.getLogicalProcessorCount();
        this.cpuModel = processor.toString();

        final long totalCpu = ticks.totalCpu();
        this.toTal = totalCpu;
        this.sys = formatDouble(ticks.cSys, totalCpu);
        this.user = formatDouble(ticks.user, totalCpu);
        this.wait = formatDouble(ticks.ioWait, totalCpu);
        this.free = formatDouble(ticks.idle, totalCpu);
    }

}
