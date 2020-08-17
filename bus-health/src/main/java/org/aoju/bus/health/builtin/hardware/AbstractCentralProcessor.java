/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.builtin.hardware;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.logger.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A CPU.
 *
 * @author Kimi Liu
 * @version 6.0.6
 * @since JDK 1.8+
 */
@ThreadSafe
public abstract class AbstractCentralProcessor implements CentralProcessor {

    private final Supplier<ProcessorIdentifier> cpuid = Memoize.memoize(this::queryProcessorId);
    private final Supplier<Long> maxFreq = Memoize.memoize(this::queryMaxFreq, Memoize.defaultExpiration());
    private final Supplier<long[]> currentFreq = Memoize.memoize(this::queryCurrentFreq, Memoize.defaultExpiration());
    private final Supplier<Long> contextSwitches = Memoize.memoize(this::queryContextSwitches, Memoize.defaultExpiration());
    private final Supplier<Long> interrupts = Memoize.memoize(this::queryInterrupts, Memoize.defaultExpiration());

    private final Supplier<long[]> systemCpuLoadTicks = Memoize.memoize(this::querySystemCpuLoadTicks, Memoize.defaultExpiration());
    private final Supplier<long[][]> processorCpuLoadTicks = Memoize.memoize(this::queryProcessorCpuLoadTicks,
            Memoize.defaultExpiration());

    // 逻辑和物理处理器计数
    private final int physicalPackageCount;
    private final int physicalProcessorCount;
    private final int logicalProcessorCount;

    // 处理器信息，在构造函数中初始化
    private final List<LogicalProcessor> logicalProcessors;

    /**
     * 创建一个处理器
     */
    public AbstractCentralProcessor() {
        // 填充逻辑处理器阵列
        this.logicalProcessors = Collections.unmodifiableList(initProcessorCounts());
        // I初始化处理器数
        Set<String> physProcPkgs = new HashSet<>();
        Set<Integer> physPkgs = new HashSet<>();
        for (LogicalProcessor logProc : this.logicalProcessors) {
            int pkg = logProc.getPhysicalPackageNumber();
            physProcPkgs.add(logProc.getPhysicalProcessorNumber() + Symbol.COLON + pkg);
            physPkgs.add(pkg);
        }
        this.logicalProcessorCount = this.logicalProcessors.size();
        this.physicalProcessorCount = physProcPkgs.size();
        this.physicalPackageCount = physPkgs.size();
    }

    /**
     * 通过编码步进，型号，系列和功能*标志来创建处理器ID
     *
     * @param stepping CPU步进
     * @param model    CPU型号
     * @param family   CPU系列
     * @param flags    以空格分隔的CPU功能标志列表
     * @return 处理器ID字符串
     */
    protected static String createProcessorID(String stepping, String model, String family, String[] flags) {
        long processorIdBytes = 0L;
        long steppingL = Builder.parseLongOrDefault(stepping, 0L);
        long modelL = Builder.parseLongOrDefault(model, 0L);
        long familyL = Builder.parseLongOrDefault(family, 0L);
        // 3:0 – Stepping
        processorIdBytes |= steppingL & 0xf;
        // 19:16,7:4 – Model
        processorIdBytes |= (modelL & 0x0f) << 4;
        processorIdBytes |= (modelL & 0xf0) << 16;
        // 27:20,11:8 – Family
        processorIdBytes |= (familyL & 0x0f) << 8;
        processorIdBytes |= (familyL & 0xf0) << 20;
        // 13:12 – Processor Type, assume 0
        for (String flag : flags) {
            switch (flag) { // NOSONAR squid:S1479
                case "fpu":
                    processorIdBytes |= 1L << 32;
                    break;
                case "vme":
                    processorIdBytes |= 1L << 33;
                    break;
                case "de":
                    processorIdBytes |= 1L << 34;
                    break;
                case "pse":
                    processorIdBytes |= 1L << 35;
                    break;
                case "tsc":
                    processorIdBytes |= 1L << 36;
                    break;
                case "msr":
                    processorIdBytes |= 1L << 37;
                    break;
                case "pae":
                    processorIdBytes |= 1L << 38;
                    break;
                case "mce":
                    processorIdBytes |= 1L << 39;
                    break;
                case "cx8":
                    processorIdBytes |= 1L << 40;
                    break;
                case "apic":
                    processorIdBytes |= 1L << 41;
                    break;
                case "sep":
                    processorIdBytes |= 1L << 43;
                    break;
                case "mtrr":
                    processorIdBytes |= 1L << 44;
                    break;
                case "pge":
                    processorIdBytes |= 1L << 45;
                    break;
                case "mca":
                    processorIdBytes |= 1L << 46;
                    break;
                case "cmov":
                    processorIdBytes |= 1L << 47;
                    break;
                case "pat":
                    processorIdBytes |= 1L << 48;
                    break;
                case "pse-36":
                    processorIdBytes |= 1L << 49;
                    break;
                case "psn":
                    processorIdBytes |= 1L << 50;
                    break;
                case "clfsh":
                    processorIdBytes |= 1L << 51;
                    break;
                case "ds":
                    processorIdBytes |= 1L << 53;
                    break;
                case "acpi":
                    processorIdBytes |= 1L << 54;
                    break;
                case "mmx":
                    processorIdBytes |= 1L << 55;
                    break;
                case "fxsr":
                    processorIdBytes |= 1L << 56;
                    break;
                case "sse":
                    processorIdBytes |= 1L << 57;
                    break;
                case "sse2":
                    processorIdBytes |= 1L << 58;
                    break;
                case "ss":
                    processorIdBytes |= 1L << 59;
                    break;
                case "htt":
                    processorIdBytes |= 1L << 60;
                    break;
                case "tm":
                    processorIdBytes |= 1L << 61;
                    break;
                case "ia64":
                    processorIdBytes |= 1L << 62;
                    break;
                case "pbe":
                    processorIdBytes |= 1L << 63;
                    break;
                default:
                    break;
            }
        }
        return String.format("%016X", processorIdBytes);
    }

    /**
     * 更新逻辑和物理处理器的数量和阵列
     *
     * @return 初始化的逻辑处理器数组
     */
    protected abstract List<LogicalProcessor> initProcessorCounts();

    /**
     * 更新逻辑和物理处理器的数量和阵列
     *
     * @return 初始化的逻辑处理器数组
     */
    protected abstract ProcessorIdentifier queryProcessorId();

    @Override
    public ProcessorIdentifier getProcessorIdentifier() {
        return cpuid.get();
    }

    @Override
    public long getMaxFreq() {
        return maxFreq.get();
    }

    /**
     * 获得处理器最大频率
     *
     * @return 最大频率.
     */
    protected abstract long queryMaxFreq();

    @Override
    public long[] getCurrentFreq() {
        return currentFreq.get();
    }

    /**
     * 获取处理器当前频率
     *
     * @return 当前的频率
     */
    protected abstract long[] queryCurrentFreq();

    @Override
    public long getContextSwitches() {
        return contextSwitches.get();
    }

    /**
     * 获取上下文切换的次数
     *
     * @return 上下文切换次数
     */
    protected abstract long queryContextSwitches();

    @Override
    public long getInterrupts() {
        return interrupts.get();
    }

    /**
     * 获取中断的数量
     *
     * @return 中断数量
     */
    protected abstract long queryInterrupts();

    @Override
    public List<LogicalProcessor> getLogicalProcessors() {
        return this.logicalProcessors;
    }

    @Override
    public long[] getSystemCpuLoadTicks() {
        return systemCpuLoadTicks.get();
    }

    /**
     * 获取系统CPU负载嘀嗒声
     *
     * @return 系统CPU负载滴答作响
     */
    protected abstract long[] querySystemCpuLoadTicks();

    @Override
    public long[][] getProcessorCpuLoadTicks() {
        return processorCpuLoadTicks.get();
    }

    /**
     * 获取处理器CPU负载嘀嗒声
     *
     * @return 处理器CPU负载滴答作响
     */
    protected abstract long[][] queryProcessorCpuLoadTicks();

    @Override
    public double getSystemCpuLoadBetweenTicks(long[] oldTicks) {
        if (oldTicks.length != TickType.values().length) {
            throw new IllegalArgumentException(
                    "Tick array " + oldTicks.length + " should have " + TickType.values().length + " elements");
        }
        long[] ticks = getSystemCpuLoadTicks();
        long total = 0;
        for (int i = 0; i < ticks.length; i++) {
            total += ticks[i] - oldTicks[i];
        }
        // 根据idle和IOwait的差异计算idle
        long idle = ticks[TickType.IDLE.getIndex()] + ticks[TickType.IOWAIT.getIndex()]
                - oldTicks[TickType.IDLE.getIndex()] - oldTicks[TickType.IOWAIT.getIndex()];
        Logger.trace("Total ticks: {}  Idle ticks: {}", total, idle);

        return total > 0 && idle >= 0 ? (double) (total - idle) / total : 0d;
    }

    @Override
    public double[] getProcessorCpuLoadBetweenTicks(long[][] oldTicks) {
        if (oldTicks.length != this.logicalProcessorCount || oldTicks[0].length != TickType.values().length) {
            throw new IllegalArgumentException(
                    "Tick array " + oldTicks.length + " should have " + this.logicalProcessorCount
                            + " arrays, each of which has " + TickType.values().length + " elements");
        }
        long[][] ticks = getProcessorCpuLoadTicks();
        double[] load = new double[this.logicalProcessorCount];
        for (int cpu = 0; cpu < this.logicalProcessorCount; cpu++) {
            long total = 0;
            for (int i = 0; i < ticks[cpu].length; i++) {
                total += ticks[cpu][i] - oldTicks[cpu][i];
            }
            // 根据idle和IOwait的差异计算idle
            long idle = ticks[cpu][TickType.IDLE.getIndex()] + ticks[cpu][TickType.IOWAIT.getIndex()]
                    - oldTicks[cpu][TickType.IDLE.getIndex()] - oldTicks[cpu][TickType.IOWAIT.getIndex()];
            Logger.trace("CPU: {}  Total ticks: {}  Idle ticks: {}", cpu, total, idle);
            load[cpu] = total > 0 && idle >= 0 ? (double) (total - idle) / total : 0d;
        }
        return load;
    }

    @Override
    public int getLogicalProcessorCount() {
        return this.logicalProcessorCount;
    }

    @Override
    public int getPhysicalProcessorCount() {
        return this.physicalProcessorCount;
    }

    @Override
    public int getPhysicalPackageCount() {
        return this.physicalPackageCount;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(getProcessorIdentifier().getName());
        sb.append("\n ").append(getPhysicalPackageCount()).append(" physical CPU package(s)");
        sb.append("\n ").append(getPhysicalProcessorCount()).append(" physical CPU core(s)");
        sb.append("\n ").append(getLogicalProcessorCount()).append(" logical CPU(s)");
        sb.append('\n').append("Identifier: ").append(getProcessorIdentifier().getIdentifier());
        sb.append('\n').append("ProcessorID: ").append(getProcessorIdentifier().getProcessorID());
        sb.append('\n').append("Microarchitecture: ").append(getProcessorIdentifier().getMicroarchitecture());
        return sb.toString();
    }

}