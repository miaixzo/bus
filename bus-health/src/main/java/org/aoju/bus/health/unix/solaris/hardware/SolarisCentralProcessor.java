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
package org.aoju.bus.health.unix.solaris.hardware;

import com.sun.jna.platform.unix.solaris.LibKstat;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.hardware.AbstractCentralProcessor;
import org.aoju.bus.health.builtin.hardware.CentralProcessor;
import org.aoju.bus.health.unix.solaris.KstatKit;
import org.aoju.bus.health.unix.solaris.KstatKit.KstatChain;
import org.aoju.bus.health.unix.solaris.SolarisLibc;

import java.util.*;

/**
 * A CPU
 *
 * @author Kimi Liu
 * @version 6.0.3
 * @since JDK 1.8+
 */
@ThreadSafe
final class SolarisCentralProcessor extends AbstractCentralProcessor {

    private static final String CPU_INFO = "cpu_info";

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
                lgroup = Builder.getFirstIntValue(line);
            } else if (line.contains("CPUs:") || line.contains("CPU:")) {
                for (Integer cpu : Builder.parseHyphenatedIntList(line.split(Symbol.COLON)[1])) {
                    numaNodeMap.put(cpu, lgroup);
                }
            }
        }
        return numaNodeMap;
    }

    /**
     * Fetches the ProcessorID by encoding the stepping, model, family, and feature
     * flags.
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
        return createProcessorID(stepping, model, family, RegEx.SPACES.split(flags.toString().toLowerCase()));
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String cpuVendor = Normal.EMPTY;
        String cpuName = Normal.EMPTY;
        String cpuFamily = Normal.EMPTY;
        String cpuModel = Normal.EMPTY;
        String cpuStepping = Normal.EMPTY;

        // Get first result
        try (KstatChain kc = KstatKit.openChain()) {
            com.sun.jna.platform.unix.solaris.LibKstat.Kstat ksp = kc.lookup(CPU_INFO, -1, null);
            // Set values
            if (ksp != null && kc.read(ksp)) {
                cpuVendor = KstatKit.dataLookupString(ksp, "vendor_id");
                cpuName = KstatKit.dataLookupString(ksp, "brand");
                cpuFamily = KstatKit.dataLookupString(ksp, "family");
                cpuModel = KstatKit.dataLookupString(ksp, "model");
                cpuStepping = KstatKit.dataLookupString(ksp, "stepping");
            }
        }
        boolean cpu64bit = "64".equals(Executor.getFirstAnswer("isainfo -b").trim());
        String processorID = getProcessorID(cpuStepping, cpuModel, cpuFamily);

        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit);
    }

    @Override
    protected List<CentralProcessor.LogicalProcessor> initProcessorCounts() {
        Map<Integer, Integer> numaNodeMap = mapNumaNodes();
        List<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<>();
        try (KstatChain kc = KstatKit.openChain()) {
            List<LibKstat.Kstat> kstats = kc.lookupAll(CPU_INFO, -1, null);

            for (LibKstat.Kstat ksp : kstats) {
                if (ksp != null && kc.read(ksp)) {
                    int procId = logProcs.size(); // 0-indexed
                    String chipId = KstatKit.dataLookupString(ksp, "chip_id");
                    String coreId = KstatKit.dataLookupString(ksp, "core_id");
                    CentralProcessor.LogicalProcessor logProc = new CentralProcessor.LogicalProcessor(procId, Builder.parseIntOrDefault(coreId, 0),
                            Builder.parseIntOrDefault(chipId, 0), numaNodeMap.getOrDefault(procId, 0));
                    logProcs.add(logProc);
                }
            }
        }
        if (logProcs.isEmpty()) {
            logProcs.add(new CentralProcessor.LogicalProcessor(0, 0, 0));
        }
        return logProcs;
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
        long[] freqs = new long[getLogicalProcessorCount()];
        Arrays.fill(freqs, -1);
        try (KstatChain kc = KstatKit.openChain()) {
            for (int i = 0; i < freqs.length; i++) {
                for (LibKstat.Kstat ksp : kc.lookupAll(CPU_INFO, i, null)) {
                    if (kc.read(ksp)) {
                        freqs[i] = KstatKit.dataLookupLong(ksp, "current_clock_Hz");
                    }
                }
            }
        }
        return freqs;
    }

    @Override
    public long queryMaxFreq() {
        long max = -1L;
        try (KstatChain kc = KstatKit.openChain()) {
            for (com.sun.jna.platform.unix.solaris.LibKstat.Kstat ksp : kc.lookupAll(CPU_INFO, 0, null)) {
                if (kc.read(ksp)) {
                    String suppFreq = KstatKit.dataLookupString(ksp, "supported_frequencies_Hz");
                    if (!suppFreq.isEmpty()) {
                        for (String s : suppFreq.split(Symbol.COLON)) {
                            long freq = Builder.parseLongOrDefault(s, -1L);
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
    public long[][] queryProcessorCpuLoadTicks() {
        long[][] ticks = new long[getLogicalProcessorCount()][CentralProcessor.TickType.values().length];
        int cpu = -1;
        try (KstatChain kc = KstatKit.openChain()) {
            for (com.sun.jna.platform.unix.solaris.LibKstat.Kstat ksp : kc.lookupAll("cpu", -1, "sys")) {
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
        long swtch = 0;
        List<String> kstat = Executor.runNative("kstat -p cpu_stat:::/pswitch\\\\|inv_swtch/");
        for (String s : kstat) {
            swtch += Builder.parseLastLong(s, 0L);
        }
        return swtch > 0 ? swtch : -1L;
    }

    @Override
    public long queryInterrupts() {
        long intr = 0;
        List<String> kstat = Executor.runNative("kstat -p cpu_stat:::/intr/");
        for (String s : kstat) {
            intr += Builder.parseLastLong(s, 0L);
        }
        return intr > 0 ? intr : -1L;
    }

}
