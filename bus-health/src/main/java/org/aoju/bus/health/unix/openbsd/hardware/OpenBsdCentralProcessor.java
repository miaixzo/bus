/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.unix.openbsd.hardware;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.lang.tuple.Triple;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.hardware.AbstractCentralProcessor;
import org.aoju.bus.health.unix.openbsd.OpenBsdLibc;
import org.aoju.bus.health.unix.openbsd.OpenBsdSysctlKit;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OpenBSD Central Processor implementation
 *
 * @author Kimi Liu
 * @version 6.2.9
 * @since JDK 1.8+
 */
public class OpenBsdCentralProcessor extends AbstractCentralProcessor {

    private static final Pattern DMESG_CPU = Pattern.compile("cpu(\\d+): smt (\\d+), core (\\d+), package (\\d+)");
    private final Supplier<Pair<Long, Long>> vmStats = Memoize.memoize(OpenBsdCentralProcessor::queryVmStats,
            Memoize.defaultExpiration());

    private static Triple<Integer, Integer, Integer> cpuidToFamilyModelStepping(int cpuid) {
        // family is bits 27:20 | 11:8
        int family = cpuid >> 16 & 0xff0 | cpuid >> 8 & 0xf;
        // model is bits 19:16 | 7:4
        int model = cpuid >> 12 & 0xf0 | cpuid >> 4 & 0xf;
        // stepping is bits 3:0
        int stepping = cpuid & 0xf;
        return Triple.of(family, model, stepping);
    }

    private static Pair<Long, Long> queryVmStats() {
        long contextSwitches = 0L;
        long interrupts = 0L;
        List<String> vmstat = Executor.runNative("vmstat -s");
        for (String line : vmstat) {
            if (line.endsWith("cpu context switches")) {
                contextSwitches = Builder.getFirstIntValue(line);
            } else if (line.endsWith("interrupts")) {
                interrupts = Builder.getFirstIntValue(line);
            }
        }
        return Pair.of(contextSwitches, interrupts);
    }

    /**
     * Parse memory buffer returned from sysctl kern.cptime or kern.cptime2 to an
     * array of 5 or 6 longs depending on version.
     * <p>
     * Versions 6.4 and later have a 6-element array while earlier versions have
     * only 5 elements. Additionally kern.cptime uses a native-sized long (32- or
     * 64-bit) value while kern.cptime2 is always a 64-bit value.
     *
     * @param m          A buffer containing the array.
     * @param force64bit True if the buffer is filled with 64-bit longs, false if native
     *                   long sized values
     * @return The array
     */
    private static long[] cpTimeToTicks(Memory m, boolean force64bit) {
        long longBytes = force64bit ? 8L : Native.LONG_SIZE;
        int arraySize = null == m ? 0 : (int) (m.size() / longBytes);
        if (force64bit && null != m) {
            return m.getLongArray(0, arraySize);
        }
        long[] ticks = new long[arraySize];
        for (int i = 0; i < arraySize; i++) {
            ticks[i] = m.getNativeLong(i * longBytes).longValue();
        }
        return ticks;
    }

    @Override
    protected ProcessorIdentifier queryProcessorId() {
        String cpuVendor = OpenBsdSysctlKit.sysctl("machdep.cpuvendor", Normal.EMPTY);
        int[] mib = new int[2];
        mib[0] = OpenBsdLibc.CTL_HW;
        mib[1] = OpenBsdLibc.HW_MODEL;
        String cpuName = OpenBsdSysctlKit.sysctl(mib, Normal.EMPTY);
        // CPUID: first 32 bits is cpufeature, last 32 bits is cpuid
        int cpuid = Builder.hexStringToInt(OpenBsdSysctlKit.sysctl("machdep.cpuid", Normal.EMPTY), 0);
        int cpufeature = Builder.hexStringToInt(OpenBsdSysctlKit.sysctl("machdep.cpufeature", Normal.EMPTY), 0);
        Triple<Integer, Integer, Integer> cpu = cpuidToFamilyModelStepping(cpuid);
        String cpuFamily = cpu.getLeft().toString();
        String cpuModel = cpu.getMiddle().toString();
        String cpuStepping = cpu.getRight().toString();
        long cpuFreq = Builder.parseHertz(cpuName);
        if (cpuFreq < 0) {
            cpuFreq = queryMaxFreq();
        }
        mib[1] = OpenBsdLibc.HW_MACHINE;
        String machine = OpenBsdSysctlKit.sysctl(mib, Normal.EMPTY);
        boolean cpu64bit = null != machine && machine.contains("64")
                || Executor.getFirstAnswer("uname -m").trim().contains("64");
        String processorID = String.format("%08x%08x", cpufeature, cpuid);

        return new ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit,
                cpuFreq);
    }

    @Override
    protected long queryMaxFreq() {
        return queryCurrentFreq()[0];
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

    @Override
    protected List<LogicalProcessor> initProcessorCounts() {
        // Iterate dmesg, look for lines:
        // cpu0: smt 0, core 0, package 0
        // cpu1: smt 0, core 1, package 0
        Map<Integer, Integer> coreMap = new HashMap<>();
        Map<Integer, Integer> packageMap = new HashMap<>();
        for (String line : Executor.runNative("dmesg")) {
            Matcher m = DMESG_CPU.matcher(line);
            if (m.matches()) {
                int cpu = Builder.parseIntOrDefault(m.group(1), 0);
                coreMap.put(cpu, Builder.parseIntOrDefault(m.group(3), 0));
                packageMap.put(cpu, Builder.parseIntOrDefault(m.group(4), 0));
            }
        }
        // native call seems to fail here, use fallback
        int logicalProcessorCount = OpenBsdSysctlKit.sysctl("hw.ncpuonline", 1);
        // If we found more procs in dmesg, update
        if (logicalProcessorCount < coreMap.keySet().size()) {
            logicalProcessorCount = coreMap.keySet().size();
        }
        List<LogicalProcessor> logProcs = new ArrayList<>(logicalProcessorCount);
        for (int i = 0; i < logicalProcessorCount; i++) {
            logProcs.add(new LogicalProcessor(i, coreMap.getOrDefault(i, 0), packageMap.getOrDefault(i, 0)));
        }
        return logProcs;
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

    /**
     * Get the system CPU load ticks
     *
     * @return The system CPU load ticks
     */
    @Override
    protected long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[TickType.values().length];
        int[] mib = new int[2];
        mib[0] = OpenBsdLibc.CTL_KERN;
        mib[1] = OpenBsdLibc.KERN_CPTIME;
        Memory m = OpenBsdSysctlKit.sysctl(mib);
        // array of 5 or 6 longs
        long[] cpuTicks = cpTimeToTicks(m, false);
        if (cpuTicks.length >= 5) {
            ticks[TickType.USER.getIndex()] = cpuTicks[OpenBsdLibc.CP_USER];
            ticks[TickType.NICE.getIndex()] = cpuTicks[OpenBsdLibc.CP_NICE];
            ticks[TickType.SYSTEM.getIndex()] = cpuTicks[OpenBsdLibc.CP_SYS];
            int offset = cpuTicks.length > 5 ? 1 : 0;
            ticks[TickType.IRQ.getIndex()] = cpuTicks[OpenBsdLibc.CP_INTR + offset];
            ticks[TickType.IDLE.getIndex()] = cpuTicks[OpenBsdLibc.CP_IDLE + offset];
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
        long[][] ticks = new long[getLogicalProcessorCount()][TickType.values().length];
        int[] mib = new int[3];
        mib[0] = OpenBsdLibc.CTL_KERN;
        mib[1] = OpenBsdLibc.KERN_CPTIME2;
        for (int cpu = 0; cpu < getLogicalProcessorCount(); cpu++) {
            mib[2] = cpu;
            Memory m = OpenBsdSysctlKit.sysctl(mib);
            // array of 5 or 6 longs
            long[] cpuTicks = cpTimeToTicks(m, true);
            if (cpuTicks.length >= 5) {
                ticks[cpu][TickType.USER.getIndex()] = cpuTicks[OpenBsdLibc.CP_USER];
                ticks[cpu][TickType.NICE.getIndex()] = cpuTicks[OpenBsdLibc.CP_NICE];
                ticks[cpu][TickType.SYSTEM.getIndex()] = cpuTicks[OpenBsdLibc.CP_SYS];
                int offset = cpuTicks.length > 5 ? 1 : 0;
                ticks[cpu][TickType.IRQ.getIndex()] = cpuTicks[OpenBsdLibc.CP_INTR + offset];
                ticks[cpu][TickType.IDLE.getIndex()] = cpuTicks[OpenBsdLibc.CP_IDLE + offset];
            }
        }
        return ticks;
    }

    /**
     * Returns the system load average for the number of elements specified, up to
     * 3, representing 1, 5, and 15 minutes. The system load average is the sum of
     * the number of runnable entities queued to the available processors and the
     * number of runnable entities running on the available processors averaged over
     * a period of time. The way in which the load average is calculated is
     * operating system specific but is typically a damped time-dependent average.
     * If the load average is not available, a negative value is returned. This
     * method is designed to provide a hint about the system load and may be queried
     * frequently.
     * <p>
     * The load average may be unavailable on some platforms (e.g., Windows) where
     * it is expensive to implement this method.
     *
     * @param nelem Number of elements to return.
     * @return an array of the system load averages for 1, 5, and 15 minutes with
     * the size of the array specified by nelem; or negative values if not
     * available.
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
