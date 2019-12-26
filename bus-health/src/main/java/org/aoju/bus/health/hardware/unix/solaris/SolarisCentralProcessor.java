/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.unix.solaris;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.common.unix.solaris.KstatUtils;
import org.aoju.bus.health.common.unix.solaris.KstatUtils.KstatChain;
import org.aoju.bus.health.common.unix.solaris.SolarisLibc;
import org.aoju.bus.health.hardware.AbstractCentralProcessor;

import java.util.*;

/**
 * A CPU
 *
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public class SolarisCentralProcessor extends AbstractCentralProcessor {

    private static final String CPU_INFO = "cpu_info";

    @Override
    protected ProcessorIdentifier queryProcessorId() {
        String cpuVendor = "";
        String cpuName = "";
        String cpuFamily = "";
        String cpuModel = "";
        String cpuStepping = "";

        // Get first result
        try (KstatChain kc = KstatUtils.openChain()) {
            Kstat ksp = kc.lookup(CPU_INFO, -1, null);
            // Set values
            if (ksp != null && kc.read(ksp)) {
                cpuVendor = KstatUtils.dataLookupString(ksp, "vendor_id");
                cpuName = KstatUtils.dataLookupString(ksp, "brand");
                cpuFamily = KstatUtils.dataLookupString(ksp, "family");
                cpuModel = KstatUtils.dataLookupString(ksp, "model");
                cpuStepping = KstatUtils.dataLookupString(ksp, "stepping");
            }
        }
        boolean cpu64bit = "64".equals(Command.getFirstAnswer("isainfo -b").trim());
        String processorID = getProcessorID(cpuStepping, cpuModel, cpuFamily);

        return new ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit);
    }

    @Override
    protected LogicalProcessor[] initProcessorCounts() {
        Map<Integer, Integer> numaNodeMap = mapNumaNodes();
        List<LogicalProcessor> logProcs = new ArrayList<>();
        try (KstatChain kc = KstatUtils.openChain()) {
            List<Kstat> kstats = kc.lookupAll(CPU_INFO, -1, null);

            for (Kstat ksp : kstats) {
                if (ksp != null && kc.read(ksp)) {
                    int procId = logProcs.size(); // 0-indexed
                    String chipId = KstatUtils.dataLookupString(ksp, "chip_id");
                    String coreId = KstatUtils.dataLookupString(ksp, "core_id");
                    LogicalProcessor logProc = new LogicalProcessor(procId, Builder.parseIntOrDefault(coreId, 0),
                            Builder.parseIntOrDefault(chipId, 0), numaNodeMap.getOrDefault(procId, 0));
                    logProcs.add(logProc);
                }
            }
        }
        if (logProcs.isEmpty()) {
            logProcs.add(new LogicalProcessor(0, 0, 0));
        }
        return logProcs.toArray(new LogicalProcessor[0]);
    }

    private Map<Integer, Integer> mapNumaNodes() {
        Map<Integer, Integer> numaNodeMap = new HashMap<>();
        // Get numa node info from lgrpinfo
        List<String> lgrpinfo = Command.runNative("lgrpinfo -c leaves");
        // Format:
        // lgroup 0 (root):
        // CPUs 0 1
        // CPUs 0-7
        // CPUs 0-3 6 7 12 13
        int lgroup = 0;
        for (String line : lgrpinfo) {
            if (line.startsWith("lgroup")) {
                lgroup = Builder.getFirstIntValue(line);
            } else if (line.contains("CPUs:")) {
                String[] cpuList = Builder.whitespaces.split(line.split(Symbol.COLON)[1]);
                for (String cpu : cpuList) {
                    // Will either be individual CPU or hyphen-delimited range
                    if (cpu.contains(Symbol.HYPHEN)) {
                        int first = Builder.getFirstIntValue(cpu);
                        int last = Builder.getNthIntValue(line, 2);
                        for (int i = first; i <= last; i++) {
                            numaNodeMap.put(i, lgroup);
                        }
                    } else {
                        numaNodeMap.put(Builder.parseIntOrDefault(cpu, 0), lgroup);
                    }
                }
            }
        }
        return numaNodeMap;
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[TickType.values().length];
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
        try (KstatChain kc = KstatUtils.openChain()) {
            for (int i = 0; i < freqs.length; i++) {
                for (Kstat ksp : kc.lookupAll(CPU_INFO, i, null)) {
                    if (kc.read(ksp)) {
                        freqs[i] = KstatUtils.dataLookupLong(ksp, "current_clock_Hz");
                    }
                }
            }
        }
        return freqs;
    }

    @Override
    public long queryMaxFreq() {
        long max = -1L;
        try (KstatChain kc = KstatUtils.openChain()) {
            for (Kstat ksp : kc.lookupAll(CPU_INFO, 0, null)) {
                if (kc.read(ksp)) {
                    String suppFreq = KstatUtils.dataLookupString(ksp, "supported_frequencies_Hz");
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
        long[][] ticks = new long[getLogicalProcessorCount()][TickType.values().length];
        int cpu = -1;
        try (KstatChain kc = KstatUtils.openChain()) {
            for (Kstat ksp : kc.lookupAll("cpu", -1, "sys")) {
                // This is a new CPU
                if (++cpu >= ticks.length) {
                    // Shouldn't happen
                    break;
                }
                if (kc.read(ksp)) {
                    ticks[cpu][TickType.IDLE.getIndex()] = KstatUtils.dataLookupLong(ksp, "cpu_ticks_idle");
                    ticks[cpu][TickType.SYSTEM.getIndex()] = KstatUtils.dataLookupLong(ksp, "cpu_ticks_kernel");
                    ticks[cpu][TickType.USER.getIndex()] = KstatUtils.dataLookupLong(ksp, "cpu_ticks_user");
                }
            }
        }
        return ticks;
    }

    private String getProcessorID(String stepping, String model, String family) {
        List<String> isainfo = Command.runNative("isainfo -v");
        StringBuilder flags = new StringBuilder();
        for (String line : isainfo) {
            if (line.startsWith("32-bit")) {
                break;
            } else if (!line.startsWith("64-bit")) {
                flags.append(Symbol.C_SPACE).append(line.trim());
            }
        }
        return createProcessorID(stepping, model, family, Builder.whitespaces.split(flags.toString().toLowerCase()));
    }

    @Override
    public long queryContextSwitches() {
        long swtch = 0;
        List<String> kstat = Command.runNative("kstat -p cpu_stat:::/pswitch\\\\|inv_swtch/");
        for (String s : kstat) {
            swtch += Builder.parseLastLong(s, 0L);
        }
        return swtch > 0 ? swtch : -1L;
    }

    @Override
    public long queryInterrupts() {
        long intr = 0;
        List<String> kstat = Command.runNative("kstat -p cpu_stat:::/intr/");
        for (String s : kstat) {
            intr += Builder.parseLastLong(s, 0L);
        }
        return intr > 0 ? intr : -1L;
    }

}
