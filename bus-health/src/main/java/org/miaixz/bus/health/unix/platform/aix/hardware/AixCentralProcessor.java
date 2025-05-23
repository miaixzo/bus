/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
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
package org.miaixz.bus.health.unix.platform.aix.hardware;

import java.util.*;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.common.AbstractCentralProcessor;
import org.miaixz.bus.health.unix.platform.aix.driver.Lssrad;
import org.miaixz.bus.health.unix.platform.aix.driver.perfstat.PerfstatConfig;
import org.miaixz.bus.health.unix.platform.aix.driver.perfstat.PerfstatCpu;

import com.sun.jna.Native;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_cpu_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_cpu_total_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_partition_config_t;

/**
 * A CPU
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class AixCentralProcessor extends AbstractCentralProcessor {

    /**
     * Jiffies per second, used for process time counters.
     */
    private static final long USER_HZ = Parsing.parseLongOrDefault(Executor.getFirstAnswer("getconf CLK_TCK"), 100L);
    private static final int SBITS = querySbits();
    private final Supplier<perfstat_cpu_total_t> cpuTotal = Memoizer.memoize(PerfstatCpu::queryCpuTotal,
            Memoizer.defaultExpiration());
    private final Supplier<perfstat_cpu_t[]> cpuProc = Memoizer.memoize(PerfstatCpu::queryCpu,
            Memoizer.defaultExpiration());
    private perfstat_partition_config_t config;

    private static int querySbits() {
        // read from /usr/include/sys/proc.h
        for (String s : Builder.readFile("/usr/include/sys/proc.h")) {
            if (s.contains("SBITS") && s.contains("#define")) {
                return Parsing.parseLastInt(s, 16);
            }
        }
        return 16;
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
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

        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, machineId,
                cpu64bit, (long) (config.processorMHz * 1_000_000L));
    }

    @Override
    protected Tuple initProcessorCounts() {
        this.config = PerfstatConfig.queryConfig();

        // Reporting "online" or "active" values can lead to nonsense so we go with max
        int physProcs = (int) config.numProcessors.max;
        if (physProcs < 1) {
            physProcs = 1;
        }
        int lcpus = (int) config.vcpus.max;
        if (lcpus < 1) {
            lcpus = 1;
        }
        // Sanity check to ensure lp/pp ratio >= 1
        if (physProcs > lcpus) {
            physProcs = lcpus;
        }
        int lpPerPp = lcpus / physProcs;
        // Get node and package mapping
        Map<Integer, Pair<Integer, Integer>> nodePkgMap = Lssrad.queryNodesPackages();
        List<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<>();
        for (int proc = 0; proc < lcpus; proc++) {
            Pair<Integer, Integer> nodePkg = nodePkgMap.get(proc);
            int physProc = proc / lpPerPp;
            logProcs.add(new CentralProcessor.LogicalProcessor(proc, physProc, nodePkg == null ? 0 : nodePkg.getRight(),
                    nodePkg == null ? 0 : nodePkg.getLeft()));
        }
        return new Tuple(logProcs, null, getCachesForModel(physProcs), Collections.emptyList());
    }

    private List<CentralProcessor.ProcessorCache> getCachesForModel(int cores) {
        // The only info available in the OS is the L2 size
        // But we can hardcode POWER7, POWER8, and POWER9 configs
        List<CentralProcessor.ProcessorCache> caches = new ArrayList<>();
        int powerVersion = Parsing.getFirstIntValue(Executor.getFirstAnswer("uname -n"));
        switch (powerVersion) {
        case 7:
            caches.add(new CentralProcessor.ProcessorCache(3, 8, 128, (2 * 32) << 20,
                    CentralProcessor.ProcessorCache.Type.UNIFIED));
            caches.add(new CentralProcessor.ProcessorCache(2, 8, 128, 256 << 10,
                    CentralProcessor.ProcessorCache.Type.UNIFIED));
            caches.add(new CentralProcessor.ProcessorCache(1, 8, 128, 32 << 10,
                    CentralProcessor.ProcessorCache.Type.DATA));
            caches.add(new CentralProcessor.ProcessorCache(1, 4, 128, 32 << 10,
                    CentralProcessor.ProcessorCache.Type.INSTRUCTION));
            break;
        case 8:
            caches.add(new CentralProcessor.ProcessorCache(4, 8, 128, (16 * 16) << 20,
                    CentralProcessor.ProcessorCache.Type.UNIFIED));
            caches.add(new CentralProcessor.ProcessorCache(3, 8, 128, 40 << 20,
                    CentralProcessor.ProcessorCache.Type.UNIFIED));
            caches.add(new CentralProcessor.ProcessorCache(2, 8, 128, 512 << 10,
                    CentralProcessor.ProcessorCache.Type.UNIFIED));
            caches.add(new CentralProcessor.ProcessorCache(1, 8, 128, 64 << 10,
                    CentralProcessor.ProcessorCache.Type.DATA));
            caches.add(new CentralProcessor.ProcessorCache(1, 8, 128, 32 << 10,
                    CentralProcessor.ProcessorCache.Type.INSTRUCTION));
            break;
        case 9:
            caches.add(new CentralProcessor.ProcessorCache(3, 20, 128, (cores * 10L) << 20,
                    CentralProcessor.ProcessorCache.Type.UNIFIED));
            caches.add(new CentralProcessor.ProcessorCache(2, 8, 128, 512 << 10,
                    CentralProcessor.ProcessorCache.Type.UNIFIED));
            caches.add(new CentralProcessor.ProcessorCache(1, 8, 128, 32 << 10,
                    CentralProcessor.ProcessorCache.Type.DATA));
            caches.add(new CentralProcessor.ProcessorCache(1, 8, 128, 32 << 10,
                    CentralProcessor.ProcessorCache.Type.INSTRUCTION));
            break;
        default:
            // Don't guess
        }
        return caches;
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        perfstat_cpu_total_t perfstat = cpuTotal.get();
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        ticks[CentralProcessor.TickType.USER.ordinal()] = perfstat.user * 1000L / USER_HZ;
        // Skip NICE
        ticks[CentralProcessor.TickType.SYSTEM.ordinal()] = perfstat.sys * 1000L / USER_HZ;
        ticks[CentralProcessor.TickType.IDLE.ordinal()] = perfstat.idle * 1000L / USER_HZ;
        ticks[CentralProcessor.TickType.IOWAIT.ordinal()] = perfstat.wait * 1000L / USER_HZ;
        ticks[CentralProcessor.TickType.IRQ.ordinal()] = perfstat.devintrs * 1000L / USER_HZ;
        ticks[CentralProcessor.TickType.SOFTIRQ.ordinal()] = perfstat.softintrs * 1000L / USER_HZ;
        ticks[CentralProcessor.TickType.STEAL.ordinal()] = (perfstat.idle_stolen_purr + perfstat.busy_stolen_purr)
                * 1000L / USER_HZ;
        return ticks;
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
    public long[] queryCurrentFreq() {
        // $ pmcycles -m
        // CPU 0 runs at 4204 MHz
        // CPU 1 runs at 4204 MHz
        //
        // ~/git/oshi$ pmcycles -m
        // This machine runs at 1000 MHz

        // FIXME change to this as pmcycles requires root
        // $ lsattr -El proc0
        // frequency 3425000000 Processor Speed False

        long[] freqs = new long[getLogicalProcessorCount()];
        Arrays.fill(freqs, -1);
        String freqMarker = "runs at";
        int idx = 0;
        for (final String checkLine : Executor.runNative("pmcycles -m")) {
            if (checkLine.contains(freqMarker)) {
                freqs[idx++] = Parsing.parseHertz(checkLine.split(freqMarker)[1].trim());
                if (idx >= freqs.length) {
                    break;
                }
            }
        }
        return freqs;
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

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        perfstat_cpu_t[] cpu = cpuProc.get();
        // oversize the array to ensure constant length; we'll only fill cpu.length of it
        long[][] ticks = new long[cpu.length][CentralProcessor.TickType.values().length];
        for (int i = 0; i < cpu.length; i++) {
            ticks[i] = new long[CentralProcessor.TickType.values().length];
            ticks[i][CentralProcessor.TickType.USER.ordinal()] = cpu[i].user * 1000L / USER_HZ;
            // Skip NICE
            ticks[i][CentralProcessor.TickType.SYSTEM.ordinal()] = cpu[i].sys * 1000L / USER_HZ;
            ticks[i][CentralProcessor.TickType.IDLE.ordinal()] = cpu[i].idle * 1000L / USER_HZ;
            ticks[i][CentralProcessor.TickType.IOWAIT.ordinal()] = cpu[i].wait * 1000L / USER_HZ;
            ticks[i][CentralProcessor.TickType.IRQ.ordinal()] = cpu[i].devintrs * 1000L / USER_HZ;
            ticks[i][CentralProcessor.TickType.SOFTIRQ.ordinal()] = cpu[i].softintrs * 1000L / USER_HZ;
            ticks[i][CentralProcessor.TickType.STEAL.ordinal()] = (cpu[i].idle_stolen_purr + cpu[i].busy_stolen_purr)
                    * 1000L / USER_HZ;
        }
        return ticks;
    }

}
