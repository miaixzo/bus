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
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.common.AbstractCentralProcessor;
import org.miaixz.bus.health.unix.jna.SolarisLibc;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit.KstatChain;
import org.miaixz.bus.health.unix.platform.solaris.software.SolarisOperatingSystem;

import java.util.*;
import java.util.regex.Matcher;

/**
 * A CPU
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class SolarisCentralProcessor extends AbstractCentralProcessor {

    private static final String KSTAT_SYSTEM_CPU = "kstat:/system/cpu/";
    private static final String INFO = "/info";
    private static final String SYS = "/sys";

    private static final String KSTAT_PM_CPU = "kstat:/pm/cpu/";
    private static final String PSTATE = "/pstate";

    private static final String CPU_INFO = "cpu_info";

    private static CentralProcessor.ProcessorIdentifier queryProcessorId2(boolean cpu64bit) {
        Object[] results = KstatKit.queryKstat2(KSTAT_SYSTEM_CPU + "0" + INFO, "vendor_id", "brand", "family", "model",
                "stepping", "clock_MHz");

        String cpuVendor = results[0] == null ? Normal.EMPTY : (String) results[0];
        String cpuName = results[1] == null ? Normal.EMPTY : (String) results[1];
        String cpuFamily = results[2] == null ? Normal.EMPTY : results[2].toString();
        String cpuModel = results[3] == null ? Normal.EMPTY : results[3].toString();
        String cpuStepping = results[4] == null ? Normal.EMPTY : results[4].toString();
        long cpuFreq = results[5] == null ? 0L : (long) results[5];

        String processorID = getProcessorID(cpuStepping, cpuModel, cpuFamily);
        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit,
                cpuFreq);
    }

    private static List<CentralProcessor.LogicalProcessor> initProcessorCounts2(Map<Integer, Integer> numaNodeMap) {
        List<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<>();

        List<Object[]> results = KstatKit.queryKstat2List(KSTAT_SYSTEM_CPU, INFO, "chip_id", "core_id");
        for (Object[] result : results) {
            int procId = logProcs.size();
            long chipId = result[0] == null ? 0L : (long) result[0];
            long coreId = result[1] == null ? 0L : (long) result[1];
            CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(procId, (int) coreId, (int) chipId,
                    numaNodeMap.getOrDefault(procId, 0));
            logProcs.add(logProc);
        }
        if (logProcs.isEmpty()) {
            logProcs.add(new CentralProcessor.LogicalProcessor(0, 0, 0));
        }
        return logProcs;
    }

    private static Map<Integer, Integer> mapNumaNodes() {
        // Get numa node info from lgrpinfo
        Map<Integer, Integer> numaNodeMap = new HashMap<>();
        int lgroup = 0;
        for (String line : Executor.runNative("lgrpinfo -c leaves")) {
            // Format:
            // lgroup 0 (root):
            // CPUs: 0 1
            // CPUs: 0-7
            // CPUs: 0-3 6 7 12 13
            // CPU: 0
            // CPU: 1
            if (line.startsWith("lgroup")) {
                lgroup = Parsing.getFirstIntValue(line);
            } else if (line.contains("CPUs:") || line.contains("CPU:")) {
                for (Integer cpu : Parsing.parseHyphenatedIntList(line.split(Symbol.COLON)[1])) {
                    numaNodeMap.put(cpu, lgroup);
                }
            }
        }
        return numaNodeMap;
    }

    private static long[] queryCurrentFreq2(int processorCount) {
        long[] freqs = new long[processorCount];
        Arrays.fill(freqs, -1);

        List<Object[]> results = KstatKit.queryKstat2List(KSTAT_SYSTEM_CPU, INFO, "current_clock_Hz");
        int cpu = -1;
        for (Object[] result : results) {
            if (++cpu >= freqs.length) {
                break;
            }
            freqs[cpu] = result[0] == null ? -1L : (long) result[0];
        }
        return freqs;
    }

    private static long queryMaxFreq2() {
        long max = -1L;
        List<Object[]> results = KstatKit.queryKstat2List(KSTAT_PM_CPU, PSTATE, "supported_frequencies");
        for (Object[] result : results) {
            for (long freq : result[0] == null ? new long[0] : (long[]) result[0]) {
                if (freq > max) {
                    max = freq;
                }
            }
        }
        return max;
    }

    private static long[][] queryProcessorCpuLoadTicks2(int processorCount) {
        long[][] ticks = new long[processorCount][CentralProcessor.TickType.values().length];
        List<Object[]> results = KstatKit.queryKstat2List(KSTAT_SYSTEM_CPU, SYS, "cpu_ticks_idle", "cpu_ticks_kernel",
                "cpu_ticks_user");
        int cpu = -1;
        for (Object[] result : results) {
            if (++cpu >= ticks.length) {
                break;
            }
            ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = result[0] == null ? 0L : (long) result[0];
            ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = result[1] == null ? 0L : (long) result[1];
            ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = result[2] == null ? 0L : (long) result[2];
        }
        return ticks;
    }

    /**
     * Fetches the ProcessorID by encoding the stepping, model, family, and feature flags.
     *
     * @param stepping The stepping
     * @param model    The model
     * @param family   The family
     * @return The Processor ID string
     */
    private static String getProcessorID(String stepping, String model, String family) {
        List<String> isainfo = Executor.runNative("isainfo -v");
        StringBuilder flags = new StringBuilder();
        for (String line : isainfo) {
            if (line.startsWith("32-bit")) {
                break;
            } else if (!line.startsWith("64-bit")) {
                flags.append(Symbol.C_SPACE).append(line.trim());
            }
        }
        return createProcessorID(stepping, model, family,
                Pattern.SPACES_PATTERN.split(flags.toString().toLowerCase(Locale.ROOT)));
    }

    private static long queryContextSwitches2() {
        long swtch = 0;
        List<Object[]> results = KstatKit.queryKstat2List(KSTAT_SYSTEM_CPU, SYS, "pswitch", "inv_swtch");
        for (Object[] result : results) {
            swtch += result[0] == null ? 0L : (long) result[0];
            swtch += result[1] == null ? 0L : (long) result[1];
        }
        return swtch;
    }

    private static long queryInterrupts2() {
        long intr = 0;
        List<Object[]> results = KstatKit.queryKstat2List(KSTAT_SYSTEM_CPU, SYS, "intr");
        for (Object[] result : results) {
            intr += result[0] == null ? 0L : (long) result[0];
        }
        return intr;
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        boolean cpu64bit = "64".equals(Executor.getFirstAnswer("isainfo -b").trim());
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return queryProcessorId2(cpu64bit);
        }
        String cpuVendor = Normal.EMPTY;
        String cpuName = Normal.EMPTY;
        String cpuFamily = Normal.EMPTY;
        String cpuModel = Normal.EMPTY;
        String cpuStepping = Normal.EMPTY;
        long cpuFreq = 0L;

        // Get first result
        try (KstatChain kc = KstatKit.openChain()) {
            Kstat ksp = kc.lookup(CPU_INFO, -1, null);
            // Set values
            if (ksp != null && kc.read(ksp)) {
                cpuVendor = KstatKit.dataLookupString(ksp, "vendor_id");
                cpuName = KstatKit.dataLookupString(ksp, "brand");
                cpuFamily = KstatKit.dataLookupString(ksp, "family");
                cpuModel = KstatKit.dataLookupString(ksp, "model");
                cpuStepping = KstatKit.dataLookupString(ksp, "stepping");
                cpuFreq = KstatKit.dataLookupLong(ksp, "clock_MHz") * 1_000_000L;
            }
        }
        String processorID = getProcessorID(cpuStepping, cpuModel, cpuFamily);

        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit,
                cpuFreq);
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        int retval = SolarisLibc.INSTANCE.getloadavg(average, nelem);
        if (retval < nelem) {
            for (int i = Math.max(retval, 0); i < average.length; i++) {
                average[i] = -1d;
            }
        }
        return average;
    }

    @Override
    protected Tuple initProcessorCounts() {
        List<CentralProcessor.LogicalProcessor> logProcs;
        Map<Integer, Integer> numaNodeMap = mapNumaNodes();
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            logProcs = initProcessorCounts2(numaNodeMap);
        } else {
            logProcs = new ArrayList<>();
            try (KstatChain kc = KstatKit.openChain()) {
                List<Kstat> kstats = kc.lookupAll(CPU_INFO, -1, null);

                for (Kstat ksp : kstats) {
                    if (ksp != null && kc.read(ksp)) {
                        int procId = logProcs.size(); // 0-indexed
                        String chipId = KstatKit.dataLookupString(ksp, "chip_id");
                        String coreId = KstatKit.dataLookupString(ksp, "core_id");
                        CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(procId, Parsing.parseIntOrDefault(coreId, 0),
                                Parsing.parseIntOrDefault(chipId, 0), numaNodeMap.getOrDefault(procId, 0));
                        logProcs.add(logProc);
                    }
                }
            }
        }
        if (logProcs.isEmpty()) {
            logProcs.add(new CentralProcessor.LogicalProcessor(0, 0, 0));
        }
        Map<Integer, String> dmesg = new HashMap<>();
        // Jan 9 14:04:28 solaris unix: [ID 950921 kern.info] cpu0: Intel(r) Celeron(r)
        // CPU J3455 @ 1.50GHz
        // but NOT: Jan 9 14:04:28 solaris unix: [ID 950921 kern.info] cpu0: x86 (chipid
        // 0x0 GenuineIntel 506C9 family 6 model 92 step 9 clock 1500 MHz)
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(".* cpu(\\\\d+): ((ARM|AMD|Intel).+)");
        for (String s : Executor.runNative("dmesg")) {
            Matcher m = p.matcher(s);
            if (m.matches()) {
                int coreId = Parsing.parseIntOrDefault(m.group(1), 0);
                dmesg.put(coreId, m.group(2).trim());
            }
        }
        if (dmesg.isEmpty()) {
            return new Tuple(logProcs, null, null, Collections.emptyList());
        }
        List<String> featureFlags = Executor.runNative("isainfo -x");
        return new Tuple(logProcs, createProcListFromDmesg(logProcs, dmesg), null, featureFlags);
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        // Average processor ticks
        long[][] procTicks = getProcessorCpuLoadTicks();
        for (int i = 0; i < ticks.length; i++) {
            for (long[] procTick : procTicks) {
                ticks[i] += procTick[i];
            }
            ticks[i] /= procTicks.length;
        }
        return ticks;
    }

    @Override
    public long[] queryCurrentFreq() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return queryCurrentFreq2(getLogicalProcessorCount());
        }
        long[] freqs = new long[getLogicalProcessorCount()];
        Arrays.fill(freqs, -1);
        try (KstatChain kc = KstatKit.openChain()) {
            for (int i = 0; i < freqs.length; i++) {
                for (Kstat ksp : kc.lookupAll(CPU_INFO, i, null)) {
                    if (ksp != null && kc.read(ksp)) {
                        freqs[i] = KstatKit.dataLookupLong(ksp, "current_clock_Hz");
                    }
                }
            }
        }
        return freqs;
    }

    @Override
    public long queryMaxFreq() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return queryMaxFreq2();
        }
        long max = -1L;
        try (KstatChain kc = KstatKit.openChain()) {
            for (Kstat ksp : kc.lookupAll(CPU_INFO, 0, null)) {
                if (kc.read(ksp)) {
                    String suppFreq = KstatKit.dataLookupString(ksp, "supported_frequencies_Hz");
                    if (!suppFreq.isEmpty()) {
                        for (String s : suppFreq.split(Symbol.COLON)) {
                            long freq = Parsing.parseLongOrDefault(s, -1L);
                            if (max < freq) {
                                max = freq;
                            }
                        }
                    }
                }
            }
        }
        return max;
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return queryProcessorCpuLoadTicks2(getLogicalProcessorCount());
        }
        long[][] ticks = new long[getLogicalProcessorCount()][CentralProcessor.TickType.values().length];
        int cpu = -1;
        try (KstatChain kc = KstatKit.openChain()) {
            for (Kstat ksp : kc.lookupAll("cpu", -1, "sys")) {
                // This is a new CPU
                if (++cpu >= ticks.length) {
                    // Shouldn't happen
                    break;
                }
                if (kc.read(ksp)) {
                    ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = KstatKit.dataLookupLong(ksp, "cpu_ticks_idle");
                    ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = KstatKit.dataLookupLong(ksp, "cpu_ticks_kernel");
                    ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = KstatKit.dataLookupLong(ksp, "cpu_ticks_user");
                }
            }
        }
        return ticks;
    }

    @Override
    public long queryContextSwitches() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return queryContextSwitches2();
        }
        long swtch = 0;
        List<String> kstat = Executor.runNative("kstat -p cpu_stat:::/pswitch\\\\|inv_swtch/");
        for (String s : kstat) {
            swtch += Parsing.parseLastLong(s, 0L);
        }
        return swtch;
    }

    @Override
    public long queryInterrupts() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return queryInterrupts2();
        }
        long intr = 0;
        List<String> kstat = Executor.runNative("kstat -p cpu_stat:::/intr/");
        for (String s : kstat) {
            intr += Parsing.parseLastLong(s, 0L);
        }
        return intr;
    }

}
