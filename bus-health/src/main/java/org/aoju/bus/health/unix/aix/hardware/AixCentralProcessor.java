/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2022 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.unix.aix.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_cpu_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_cpu_total_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_partition_config_t;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.hardware.AbstractCentralProcessor;
import org.aoju.bus.health.unix.aix.drivers.Lssrad;
import org.aoju.bus.health.unix.aix.drivers.perfstat.PerfstatConfig;
import org.aoju.bus.health.unix.aix.drivers.perfstat.PerfstatCpu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.defaultExpiration;
import static org.aoju.bus.health.Memoize.memoize;

/**
 * A CPU
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class AixCentralProcessor extends AbstractCentralProcessor {

    private static final int SBITS = querySbits();
    /**
     * Jiffies per second, used for process time counters.
     */
    private static final long USER_HZ = Builder.parseLongOrDefault(Executor.getFirstAnswer("getconf CLK_TCK"),
            100L);
    private final Supplier<perfstat_cpu_total_t> cpuTotal = memoize(PerfstatCpu::queryCpuTotal, defaultExpiration());
    private final Supplier<perfstat_cpu_t[]> cpuProc = memoize(PerfstatCpu::queryCpu, defaultExpiration());
    private perfstat_partition_config_t config;

    private static int querySbits() {
        // read from /usr/include/sys/proc.h
        for (String s : Builder.readFile("/usr/include/sys/proc.h")) {
            if (s.contains("SBITS") && s.contains("#define")) {
                return Builder.parseLastInt(s, 16);
            }
        }
        return 16;
    }

    @Override
    protected ProcessorIdentifier queryProcessorId() {
        String cpuVendor = Normal.UNKNOWN;
        String cpuName = Normal.EMPTY;
        String cpuFamily = Normal.EMPTY;
        boolean cpu64bit = false;

        final String nameMarker = "Processor Type:";
        final String familyMarker = "Processor Version:";
        final String bitnessMarker = "CPU Type:";
        for (final String checkLine : Executor.runNative("prtconf")) {
            if (checkLine.startsWith(nameMarker)) {
                cpuName = checkLine.split(nameMarker)[1].trim();
                if (cpuName.startsWith("P")) {
                    cpuVendor = "IBM";
                } else if (cpuName.startsWith("I")) {
                    cpuVendor = "Intel";
                }
            } else if (checkLine.startsWith(familyMarker)) {
                cpuFamily = checkLine.split(familyMarker)[1].trim();
            } else if (checkLine.startsWith(bitnessMarker)) {
                cpu64bit = checkLine.split(bitnessMarker)[1].contains("64");
            }
        }

        String cpuModel = Normal.EMPTY;
        String cpuStepping = Normal.EMPTY;
        String machineId = Native.toString(config.machineID);
        if (machineId.isEmpty()) {
            machineId = Executor.getFirstAnswer("uname -m");
        }
        // last 4 characters are model ID (often 4C) and submodel (always 00)
        if (machineId.length() > 10) {
            int m = machineId.length() - 4;
            int s = machineId.length() - 2;
            cpuModel = machineId.substring(m, s);
            cpuStepping = machineId.substring(s);
        }

        return new ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, machineId, cpu64bit,
                (long) (config.processorMHz * 1_000_000L));
    }

    @Override
    protected Pair<List<LogicalProcessor>, List<PhysicalProcessor>> initProcessorCounts() {
        this.config = PerfstatConfig.queryConfig();

        int physProcs = (int) config.numProcessors.max;
        if (physProcs < 1) {
            physProcs = 1;
        }
        int lcpus = config.lcpus;
        if (lcpus < 1) {
            lcpus = 1;
        }
        // Get node and package mapping
        Map<Integer, Pair<Integer, Integer>> nodePkgMap = Lssrad.queryNodesPackages();
        List<LogicalProcessor> logProcs = new ArrayList<>();
        for (int proc = 0; proc < lcpus; proc++) {
            Pair<Integer, Integer> nodePkg = nodePkgMap.get(proc);
            logProcs.add(new LogicalProcessor(proc, proc / physProcs, nodePkg == null ? 0 : nodePkg.getRight(),
                    nodePkg == null ? 0 : nodePkg.getLeft()));
        }
        return Pair.of(logProcs, null);
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        perfstat_cpu_total_t perfstat = cpuTotal.get();
        long[] ticks = new long[TickType.values().length];
        ticks[TickType.USER.ordinal()] = perfstat.user * 1000L / USER_HZ;
        // Skip NICE
        ticks[TickType.SYSTEM.ordinal()] = perfstat.sys * 1000L / USER_HZ;
        ticks[TickType.IDLE.ordinal()] = perfstat.idle * 1000L / USER_HZ;
        ticks[TickType.IOWAIT.ordinal()] = perfstat.wait * 1000L / USER_HZ;
        ticks[TickType.IRQ.ordinal()] = perfstat.devintrs * 1000L / USER_HZ;
        ticks[TickType.SOFTIRQ.ordinal()] = perfstat.softintrs * 1000L / USER_HZ;
        ticks[TickType.STEAL.ordinal()] = (perfstat.idle_stolen_purr + perfstat.busy_stolen_purr) * 1000L / USER_HZ;
        return ticks;
    }

    @Override
    public long[] queryCurrentFreq() {
        // $ pmcycles -m
        // CPU 0 runs at 4204 MHz
        // CPU 1 runs at 4204 MHz
        //
        // ~/git/health$ pmcycles -m
        // This machine runs at 1000 MHz

        long[] freqs = new long[getLogicalProcessorCount()];
        Arrays.fill(freqs, -1);
        String freqMarker = "runs at";
        int idx = 0;
        for (final String checkLine : Executor.runNative("pmcycles -m")) {
            if (checkLine.contains(freqMarker)) {
                freqs[idx++] = Builder.parseHertz(checkLine.split(freqMarker)[1].trim());
                if (idx >= freqs.length) {
                    break;
                }
            }
        }
        return freqs;
    }

    @Override
    protected long queryMaxFreq() {
        perfstat_cpu_total_t perfstat = cpuTotal.get();
        return perfstat.processorHZ;
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        long[] loadavg = cpuTotal.get().loadavg;
        for (int i = 0; i < nelem; i++) {
            average[i] = loadavg[i] / (double) (1L << SBITS);
        }
        return average;
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        perfstat_cpu_t[] cpu = cpuProc.get();
        long[][] ticks = new long[cpu.length][TickType.values().length];
        for (int i = 0; i < cpu.length; i++) {
            ticks[i] = new long[TickType.values().length];
            ticks[i][TickType.USER.ordinal()] = cpu[i].user * 1000L / USER_HZ;
            // Skip NICE
            ticks[i][TickType.SYSTEM.ordinal()] = cpu[i].sys * 1000L / USER_HZ;
            ticks[i][TickType.IDLE.ordinal()] = cpu[i].idle * 1000L / USER_HZ;
            ticks[i][TickType.IOWAIT.ordinal()] = cpu[i].wait * 1000L / USER_HZ;
            ticks[i][TickType.IRQ.ordinal()] = cpu[i].devintrs * 1000L / USER_HZ;
            ticks[i][TickType.SOFTIRQ.ordinal()] = cpu[i].softintrs * 1000L / USER_HZ;
            ticks[i][TickType.STEAL.ordinal()] = (cpu[i].idle_stolen_purr + cpu[i].busy_stolen_purr) * 1000L / USER_HZ;
        }
        return ticks;
    }

    @Override
    public long queryContextSwitches() {
        return cpuTotal.get().pswitch;
    }

    @Override
    public long queryInterrupts() {
        perfstat_cpu_total_t cpu = cpuTotal.get();
        return cpu.devintrs + cpu.softintrs;
    }

}
