/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
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
package org.miaixz.bus.health.unix.platform.openbsd.hardware;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Quartet;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.common.AbstractCentralProcessor;
import org.miaixz.bus.health.unix.jna.OpenBsdLibc;
import org.miaixz.bus.health.unix.platform.openbsd.OpenBsdSysctlKit;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenBSD Central Processor implementation
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class OpenBsdCentralProcessor extends AbstractCentralProcessor {
    private final Supplier<Pair<Long, Long>> vmStats = Memoizer.memoize(OpenBsdCentralProcessor::queryVmStats,
            Memoizer.defaultExpiration());
    private static final Pattern DMESG_CPU = Pattern.compile("cpu(\\d+): smt (\\d+), core (\\d+), package (\\d+)");

    private static Triplet<Integer, Integer, Integer> cpuidToFamilyModelStepping(int cpuid) {
        // family is bits 27:20 | 11:8
        int family = cpuid >> 16 & 0xff0 | cpuid >> 8 & 0xf;
        // model is bits 19:16 | 7:4
        int model = cpuid >> 12 & 0xf0 | cpuid >> 4 & 0xf;
        // stepping is bits 3:0
        int stepping = cpuid & 0xf;
        return Triplet.of(family, model, stepping);
    }

    private static Pair<Long, Long> queryVmStats() {
        long contextSwitches = 0L;
        long interrupts = 0L;
        List<String> vmstat = Executor.runNative("vmstat -s");
        for (String line : vmstat) {
            if (line.endsWith("cpu context switches")) {
                contextSwitches = Parsing.getFirstIntValue(line);
            } else if (line.endsWith("interrupts")) {
                interrupts = Parsing.getFirstIntValue(line);
            }
        }
        return Pair.of(contextSwitches, interrupts);
    }

    /**
     * Parse memory buffer returned from sysctl kern.cptime or kern.cptime2 to an array of 5 or 6 longs depending on
     * version.
     * <p>
     * Versions 6.4 and later have a 6-element array while earlier versions have only 5 elements. Additionally
     * kern.cptime uses a native-sized long (32- or 64-bit) value while kern.cptime2 is always a 64-bit value.
     *
     * @param m          A buffer containing the array.
     * @param force64bit True if the buffer is filled with 64-bit longs, false if native long sized values
     * @return The array
     */
    private static long[] cpTimeToTicks(Memory m, boolean force64bit) {
        long longBytes = force64bit ? 8L : Native.LONG_SIZE;
        int arraySize = m == null ? 0 : (int) (m.size() / longBytes);
        if (force64bit && m != null) {
            return m.getLongArray(0, arraySize);
        }
        long[] ticks = new long[arraySize];
        for (int i = 0; i < arraySize; i++) {
            ticks[i] = m.getNativeLong(i * longBytes).longValue();
        }
        return ticks;
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String cpuVendor = OpenBsdSysctlKit.sysctl("machdep.cpuvendor", Normal.EMPTY);
        int[] mib = new int[2];
        mib[0] = OpenBsdLibc.CTL_HW;
        mib[1] = OpenBsdLibc.HW_MODEL;
        String cpuName = OpenBsdSysctlKit.sysctl(mib, Normal.EMPTY);
        // CPUID: first 32 bits is cpufeature, last 32 bits is cpuid
        int cpuid = Parsing.hexStringToInt(OpenBsdSysctlKit.sysctl("machdep.cpuid", Normal.EMPTY), 0);
        int cpufeature = Parsing.hexStringToInt(OpenBsdSysctlKit.sysctl("machdep.cpufeature", Normal.EMPTY), 0);
        Triplet<Integer, Integer, Integer> cpu = cpuidToFamilyModelStepping(cpuid);
        String cpuFamily = cpu.getLeft().toString();
        String cpuModel = cpu.getMiddle().toString();
        String cpuStepping = cpu.getRight().toString();
        long cpuFreq = Parsing.parseHertz(cpuName);
        if (cpuFreq < 0) {
            cpuFreq = queryMaxFreq();
        }
        mib[1] = OpenBsdLibc.HW_MACHINE;
        String machine = OpenBsdSysctlKit.sysctl(mib, Normal.EMPTY);
        boolean cpu64bit = machine != null && machine.contains("64")
                || Executor.getFirstAnswer("uname -m").trim().contains("64");
        String processorID = String.format(Locale.ROOT, "%08x%08x", cpufeature, cpuid);

        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit,
                cpuFreq);
    }

    @Override
    protected long[] queryCurrentFreq() {
        long[] freq = new long[1];
        int[] mib = new int[2];
        mib[0] = OpenBsdLibc.CTL_HW;
        mib[1] = OpenBsdLibc.HW_CPUSPEED;
        freq[0] = OpenBsdSysctlKit.sysctl(mib, 0L) * 1_000_000L;
        return freq;
    }

    /**
     * Get number of context switches
     *
     * @return The context switches
     */
    @Override
    protected long queryContextSwitches() {
        return vmStats.get().getLeft();
    }

    /**
     * Get number of interrupts
     *
     * @return The interrupts
     */
    @Override
    protected long queryInterrupts() {
        return vmStats.get().getRight();
    }

    @Override
    protected Quartet<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>, List<CentralProcessor.ProcessorCache>, List<String>> initProcessorCounts() {
        // Iterate dmesg, look for lines:
        // cpu0: smt 0, core 0, package 0
        // cpu1: smt 0, core 1, package 0
        Map<Integer, Integer> coreMap = new HashMap<>();
        Map<Integer, Integer> packageMap = new HashMap<>();
        for (String line : Executor.runNative("dmesg")) {
            Matcher m = DMESG_CPU.matcher(line);
            if (m.matches()) {
                int cpu = Parsing.parseIntOrDefault(m.group(1), 0);
                coreMap.put(cpu, Parsing.parseIntOrDefault(m.group(3), 0));
                packageMap.put(cpu, Parsing.parseIntOrDefault(m.group(4), 0));
            }
        }
        // native call seems to fail here, use fallback
        int logicalProcessorCount = OpenBsdSysctlKit.sysctl("hw.ncpuonline", 1);
        // If we found more procs in dmesg, update
        if (logicalProcessorCount < coreMap.keySet().size()) {
            logicalProcessorCount = coreMap.keySet().size();
        }
        List<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<>(logicalProcessorCount);
        for (int i = 0; i < logicalProcessorCount; i++) {
            logProcs.add(new CentralProcessor.LogicalProcessor(i, coreMap.getOrDefault(i, 0), packageMap.getOrDefault(i, 0)));
        }
        Map<Integer, String> cpuMap = new HashMap<>();
        // cpu0 at mainbus0 mpidr 0: ARM Cortex-A7 r0p5
        // but NOT: cpu0 at mainbus0: apid 0 (boot processor)
        // cpu0: AMD GX-412TC SOC, 998.28 MHz, 16-30-01
        // cpu0: AMD EPYC 7313P 16-Core Processor, 2994.74 MHz, 19-01-01
        // cpu0: Intel(R) Celeron(R) N4000 CPU @ 1.10GHz, 2491.67 MHz, 06-7a-01
        Pattern p = Pattern.compile("cpu(\\\\d+).*: ((ARM|AMD|Intel|Apple).+)");

        Set<CentralProcessor.ProcessorCache> caches = new HashSet<>();
        // cpu0: 48KB 64b/line 12-way D-cache, 32KB 64b/line 8-way I-cache,
        // ... 1MB 64b/line 10-way L2 cache, 24MB 64b/line 12-way L3 cache
        // cpu0: 32KB 64b/line 8-way D-cache, 64KB 64b/line 4-way I-cache,
        // ... 512KB 64b/line 8-way L2 cache, 4MB 64b/line 16-way L3 cache
        // cpu0: 32KB 64b/line 2-way I-cache, 32KB 64b/line 8-way D-cache,
        // ... 2MB 64b/line 16-way L2 cache
        // cpu0: 32KB 64b/line 8-way I-cache, 32KB 64b/line 8-way D-cache,
        // ... 512KB 64b/line 8-way L2 cache
        // cpu0: 256KB 64b/line disabled L2 cache
        // cpu0: 1MB 64b/line 16-way L2 cache
        // --- Mac M1 example ---
        // shows different for icestorm/firestrorm and multiple lines for L1 vs. L2
        // cpu0 at mainbus0 mpidr 0: Apple Icestorm Pro r2p0
        // cpu0: 128KB 64b/line 8-way L1 VIPT I-cache, 64KB 64b/line 8-way L1 D-cache
        // cpu0: 4096KB 128b/line 16-way L2 cache
        // cpu2 at mainbus0 mpidr 10100: Apple Firestorm Pro r2p0
        // cpu2: 192KB 64b/line 6-way L1 VIPT I-cache, 128KB 64b/line 8-way L1 D-cache
        // cpu2: 12288KB 128b/line 12-way L2 cache
        // --- BIG:little example ---
        // shows different for A53/A72 and multiple lines for L1 vs. L2
        // cpu0 at mainbus0 mpidr 0: ARM Cortex-A53 r0p4
        // cpu0: 32KB 64b/line 2-way L1 VIPT I-cache, 32KB 64b/line 4-way L1 D-cache
        // cpu0: 512KB 64b/line 16-way L2 cache
        // cpu4 at mainbus0 mpidr 100: ARM Cortex-A72 r0p2
        // cpu4: 48KB 64b/line 3-way L1 PIPT I-cache, 32KB 64b/line 2-way L1 D-cache
        // cpu4: 1024KB 64b/line 16-way L2 cache
        Pattern q = Pattern.compile("cpu(\\\\d+).*: (.+(I-|D-|L\\d+\\s)cache)");
        Set<String> featureFlags = new LinkedHashSet<>();
        for (String s : Executor.runNative("dmesg")) {
            Matcher m = p.matcher(s);
            if (m.matches()) {
                int coreId = Parsing.parseIntOrDefault(m.group(1), 0);
                cpuMap.put(coreId, m.group(2).trim());
            } else {
                Matcher n = q.matcher(s);
                if (n.matches()) {
                    for (String cacheStr : n.group(1).split(",")) {
                        CentralProcessor.ProcessorCache cache = parseCacheStr(cacheStr);
                        if (cache != null) {
                            caches.add(cache);
                        }
                    }
                }
            }
            if (s.startsWith("cpu")) {
                String[] ss = s.trim().split(": ");
                if (ss.length == 2 && ss[1].split(",").length > 3) {
                    featureFlags.add(ss[1]);
                }
            }
        }
        List<CentralProcessor.PhysicalProcessor> physProcs = cpuMap.isEmpty() ? null : createProcListFromDmesg(logProcs, cpuMap);
        return new Quartet<>(logProcs, physProcs, orderedProcCaches(caches), new ArrayList<>(featureFlags));
    }

    private CentralProcessor.ProcessorCache parseCacheStr(String cacheStr) {
        String[] split = RegEx.SPACES.split(cacheStr);
        if (split.length > 3) {
            switch (split[split.length - 1]) {
                case "I-cache":
                    return new CentralProcessor.ProcessorCache(1, Parsing.getFirstIntValue(split[2]), Parsing.getFirstIntValue(split[1]),
                            Parsing.parseDecimalMemorySizeToBinary(split[0]), CentralProcessor.ProcessorCache.Type.INSTRUCTION);
                case "D-cache":
                    return new CentralProcessor.ProcessorCache(1, Parsing.getFirstIntValue(split[2]), Parsing.getFirstIntValue(split[1]),
                            Parsing.parseDecimalMemorySizeToBinary(split[0]), CentralProcessor.ProcessorCache.Type.DATA);
                default:
                    return new CentralProcessor.ProcessorCache(Parsing.getFirstIntValue(split[3]), Parsing.getFirstIntValue(split[2]),
                            Parsing.getFirstIntValue(split[1]), Parsing.parseDecimalMemorySizeToBinary(split[0]),
                            CentralProcessor.ProcessorCache.Type.UNIFIED);
            }
        }
        return null;
    }

    /**
     * Get the system CPU load ticks
     *
     * @return The system CPU load ticks
     */
    @Override
    protected long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        int[] mib = new int[2];
        mib[0] = OpenBsdLibc.CTL_KERN;
        mib[1] = OpenBsdLibc.KERN_CPTIME;
        try (Memory m = OpenBsdSysctlKit.sysctl(mib)) {
            // array of 5 or 6 native longs
            long[] cpuTicks = cpTimeToTicks(m, false);
            if (cpuTicks.length >= 5) {
                ticks[CentralProcessor.TickType.USER.getIndex()] = cpuTicks[OpenBsdLibc.CP_USER];
                ticks[CentralProcessor.TickType.NICE.getIndex()] = cpuTicks[OpenBsdLibc.CP_NICE];
                ticks[CentralProcessor.TickType.SYSTEM.getIndex()] = cpuTicks[OpenBsdLibc.CP_SYS];
                int offset = cpuTicks.length > 5 ? 1 : 0;
                ticks[CentralProcessor.TickType.IRQ.getIndex()] = cpuTicks[OpenBsdLibc.CP_INTR + offset];
                ticks[CentralProcessor.TickType.IDLE.getIndex()] = cpuTicks[OpenBsdLibc.CP_IDLE + offset];
            }
        }
        return ticks;
    }

    /**
     * Get the processor CPU load ticks
     *
     * @return The processor CPU load ticks
     */
    @Override
    protected long[][] queryProcessorCpuLoadTicks() {
        long[][] ticks = new long[getLogicalProcessorCount()][CentralProcessor.TickType.values().length];
        int[] mib = new int[3];
        mib[0] = OpenBsdLibc.CTL_KERN;
        mib[1] = OpenBsdLibc.KERN_CPTIME2;
        for (int cpu = 0; cpu < getLogicalProcessorCount(); cpu++) {
            mib[2] = cpu;
            try (Memory m = OpenBsdSysctlKit.sysctl(mib)) {
                // array of 5 or 6 longs
                long[] cpuTicks = cpTimeToTicks(m, true);
                if (cpuTicks.length >= 5) {
                    ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = cpuTicks[OpenBsdLibc.CP_USER];
                    ticks[cpu][CentralProcessor.TickType.NICE.getIndex()] = cpuTicks[OpenBsdLibc.CP_NICE];
                    ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = cpuTicks[OpenBsdLibc.CP_SYS];
                    int offset = cpuTicks.length > 5 ? 1 : 0;
                    ticks[cpu][CentralProcessor.TickType.IRQ.getIndex()] = cpuTicks[OpenBsdLibc.CP_INTR + offset];
                    ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = cpuTicks[OpenBsdLibc.CP_IDLE + offset];
                }
            }
        }
        return ticks;
    }

    /**
     * Returns the system load average for the number of elements specified, up to 3, representing 1, 5, and 15 minutes.
     * The system load average is the sum of the number of runnable entities queued to the available processors and the
     * number of runnable entities running on the available processors averaged over a period of time. The way in which
     * the load average is calculated is operating system specific but is typically a damped time-dependent average. If
     * the load average is not available, a negative value is returned. This method is designed to provide a hint about
     * the system load and may be queried frequently.
     * <p>
     * The load average may be unavailable on some platforms (e.g., Windows) where it is expensive to implement this
     * method.
     *
     * @param nelem Number of elements to return.
     * @return an array of the system load averages for 1, 5, and 15 minutes with the size of the array specified by
     * nelem; or negative values if not available.
     */
    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        int retval = OpenBsdLibc.INSTANCE.getloadavg(average, nelem);
        if (retval < nelem) {
            Arrays.fill(average, -1d);
        }
        return average;
    }
}
