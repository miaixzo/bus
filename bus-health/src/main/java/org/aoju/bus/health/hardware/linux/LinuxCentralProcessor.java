/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.health.hardware.linux;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.common.linux.LinuxLibc;
import org.aoju.bus.health.common.linux.ProcUtils;
import org.aoju.bus.health.hardware.AbstractCentralProcessor;
import org.aoju.bus.health.software.linux.LinuxOS;

import java.util.*;

/**
 * A CPU as defined in Linux /proc.
 *
 * @author Kimi Liu
 * @version 5.5.6
 * @since JDK 1.8+
 */
public class LinuxCentralProcessor extends AbstractCentralProcessor {

    private static final String CPUFREQ_PATH = "/sys/devices/system/cpu/cpu";

    @Override
    protected final ProcessorIdentifier queryProcessorId() {
        String cpuVendor = "";
        String cpuName = "";
        String cpuFamily = "";
        String cpuModel = "";
        String cpuStepping = "";
        String processorID;
        boolean cpu64bit = false;

        StringBuilder armStepping = new StringBuilder(); // For ARM equivalent
        String[] flags = Normal.EMPTY_STRING_ARRAY;
        List<String> cpuInfo = Builder.readFile(ProcUtils.getProcPath() + ProcUtils.CPUINFO);
        for (String line : cpuInfo) {
            String[] splitLine = Builder.whitespacesColonWhitespace.split(line);
            if (splitLine.length < 2) {
                continue;
            }
            switch (splitLine[0]) {
                case "vendor_id":
                case "CPU implementer":
                    cpuVendor = splitLine[1];
                    break;
                case "model name":
                    cpuName = splitLine[1];
                    break;
                case "flags":
                    flags = splitLine[1].toLowerCase().split(Symbol.SPACE);
                    for (String flag : flags) {
                        if ("lm".equals(flag)) {
                            cpu64bit = true;
                            break;
                        }
                    }
                    break;
                case "stepping":
                    cpuStepping = splitLine[1];
                    break;
                case "CPU variant":
                    armStepping.insert(0, "r" + splitLine[1]);
                    break;
                case "CPU revision":
                    armStepping.append('p').append(splitLine[1]);
                    break;
                case "model":
                case "CPU part":
                    cpuModel = splitLine[1];
                    break;
                case "cpu family":
                case "CPU architecture":
                    cpuFamily = splitLine[1];
                    break;
                default:
                    // Do nothing
            }
        }
        if (cpuStepping.isEmpty()) {
            cpuStepping = armStepping.toString();
        }
        processorID = getProcessorID(cpuVendor, cpuStepping, cpuModel, cpuFamily, flags);
        if (cpuVendor.startsWith("0x")) {
            List<String> lscpu = Command.runNative("lscpu");
            for (String line : lscpu) {
                if (line.startsWith("Architecture:")) {
                    cpuVendor = line.replace("Architecture:", "").trim();
                }
            }
        }
        return new ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit);
    }

    @Override
    protected LogicalProcessor[] initProcessorCounts() {
        Map<Integer, Integer> numaNodeMap = mapNumaNodes();
        List<String> procCpu = Builder.readFile(ProcUtils.getProcPath() + ProcUtils.CPUINFO);
        List<LogicalProcessor> logProcs = new ArrayList<>();
        int currentProcessor = 0;
        int currentCore = 0;
        int currentPackage = 0;
        boolean first = true;
        for (String cpu : procCpu) {
            // Count logical processors
            if (cpu.startsWith("processor")) {
                if (!first) {
                    logProcs.add(new LogicalProcessor(currentProcessor, currentCore, currentPackage,
                            numaNodeMap.getOrDefault(currentProcessor, 0)));
                } else {
                    first = false;
                }
                currentProcessor = Builder.parseLastInt(cpu, 0);
            } else if (cpu.startsWith("core id") || cpu.startsWith("cpu number")) {
                // Count unique combinations of core id and physical id.
                currentCore = Builder.parseLastInt(cpu, 0);
            } else if (cpu.startsWith("physical id")) {
                currentPackage = Builder.parseLastInt(cpu, 0);
            }
        }
        logProcs.add(new LogicalProcessor(currentProcessor, currentCore, currentPackage,
                numaNodeMap.getOrDefault(currentProcessor, 0)));

        return logProcs.toArray(new LogicalProcessor[0]);
    }

    private Map<Integer, Integer> mapNumaNodes() {
        Map<Integer, Integer> numaNodeMap = new HashMap<>();
        // Get numa node info from lscpu
        List<String> lscpu = Command.runNative("lscpu -p=cpu,node");
        // Format:
        // # comment lines starting with #
        // # then comma-delimited cpu,node
        // 0,0
        // 1,0
        for (String line : lscpu) {
            if (line.startsWith(Symbol.SHAPE)) {
                continue;
            }
            String[] split = line.split(Symbol.COMMA);
            if (split.length == 2) {
                numaNodeMap.put(Builder.parseIntOrDefault(split[0], 0), Builder.parseIntOrDefault(split[1], 0));
            }
        }
        return numaNodeMap;
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        // convert the Linux Jiffies to Milliseconds.
        long[] ticks = ProcUtils.readSystemCpuLoadTicks();
        long hz = LinuxOS.getHz();
        for (int i = 0; i < ticks.length; i++) {
            ticks[i] = ticks[i] * 1000L / hz;
        }
        return ticks;
    }

    @Override
    public long[] queryCurrentFreq() {
        long[] freqs = new long[getLogicalProcessorCount()];
        // Attempt to fill array from cpu-freq source
        long max = 0L;
        for (int i = 0; i < freqs.length; i++) {
            freqs[i] = Builder.getLongFromFile(CPUFREQ_PATH + i + "/cpufreq/scaling_cur_freq");
            if (freqs[i] == 0) {
                freqs[i] = Builder.getLongFromFile(CPUFREQ_PATH + i + "/cpufreq/cpuinfo_cur_freq");
            }
            if (max < freqs[i]) {
                max = freqs[i];
            }
        }
        if (max > 0L) {
            // If successful, array is filled with values in KHz.
            for (int i = 0; i < freqs.length; i++) {
                freqs[i] *= 1000L;
            }
            return freqs;
        }
        // If unsuccessful, try from /proc/cpuinfo
        Arrays.fill(freqs, -1);
        List<String> cpuInfo = Builder.readFile(ProcUtils.getProcPath() + ProcUtils.CPUINFO);
        int proc = 0;
        for (String s : cpuInfo) {
            if (s.toLowerCase().contains("cpu mhz")) {
                freqs[proc] = (long) (Builder.parseLastDouble(s, 0d) * 1_000_000);
                if (++proc >= freqs.length) {
                    break;
                }
            }
        }
        return freqs;
    }

    @Override
    public long queryMaxFreq() {
        long max = 0L;
        for (int i = 0; i < getLogicalProcessorCount(); i++) {
            long freq = Builder.getLongFromFile(CPUFREQ_PATH + i + "/cpufreq/scaling_max_freq");
            if (freq == 0) {
                freq = Builder.getLongFromFile(CPUFREQ_PATH + i + "/cpufreq/cpuinfo_max_freq");
            }
            if (max < freq) {
                max = freq;
            }
        }
        if (max > 0L) {
            // If successful, value is in KHz.
            return max * 1000L;
        }
        return -1L;
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        int retval = LinuxLibc.INSTANCE.getloadavg(average, nelem);
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
        // /proc/stat expected format
        // first line is overall user,nice,system,idle, etc.
        // cpu 3357 0 4313 1362393 ...
        // per-processor subsequent lines for cpu0, cpu1, etc.
        int cpu = 0;
        List<String> procStat = Builder.readFile(ProcUtils.getProcPath() + ProcUtils.STAT);
        for (String stat : procStat) {
            if (stat.startsWith("cpu") && !stat.startsWith("cpu ")) {
                // Split the line. Note the first (0) element is "cpu" so
                // remaining
                // elements are offset by 1 from the enum index
                String[] tickArr = Builder.whitespaces.split(stat);
                if (tickArr.length <= TickType.IDLE.getIndex()) {
                    // If ticks don't at least go user/nice/system/idle, abort
                    return ticks;
                }
                // Note tickArr is offset by 1
                for (int i = 0; i < TickType.values().length; i++) {
                    ticks[cpu][i] = Builder.parseLongOrDefault(tickArr[i + 1], 0L);
                }
                // Ignore guest or guest_nice, they are included in
                if (++cpu >= getLogicalProcessorCount()) {
                    break;
                }
            }
        }
        // convert the Linux Jiffies to Milliseconds.
        long hz = LinuxOS.getHz();
        for (int i = 0; i < ticks.length; i++) {
            for (int j = 0; j < ticks[i].length; j++) {
                ticks[i][j] = ticks[i][j] * 1000L / hz;
            }
        }
        return ticks;
    }

    /**
     * 从dmidecode(如果可能的话，使用根权限)、cpuid命令
     * (如果安装了)或者通过编码步骤、模型、类型和特性标志来获取ProcessorID
     *
     * @param vendor   提供者
     * @param stepping 采取行动
     * @param model    模型
     * @param family   类型
     * @param flags    标记
     * @return The Processor ID string
     */
    private String getProcessorID(String vendor,
                                  String stepping,
                                  String model,
                                  String family,
                                  String[] flags) {
        boolean procInfo = false;
        String marker = "Processor Information";
        for (String checkLine : Command.runNative("dmidecode -t 4")) {
            if (!procInfo && checkLine.contains(marker)) {
                marker = "ID:";
                procInfo = true;
            } else if (procInfo && checkLine.contains(marker)) {
                return checkLine.split(marker)[1].trim();
            }
        }
        // If we've gotten this far, dmidecode failed. Try cpuid.
        marker = "eax=";
        for (String checkLine : Command.runNative("cpuid -1r")) {
            if (checkLine.contains(marker) && checkLine.trim().startsWith("0x00000001")) {
                String eax = "";
                String edx = "";
                for (String register : Builder.whitespaces.split(checkLine)) {
                    if (register.startsWith("eax=")) {
                        eax = Builder.removeMatchingString(register, "eax=0x");
                    } else if (register.startsWith("edx=")) {
                        edx = Builder.removeMatchingString(register, "edx=0x");
                    }
                }
                return edx + eax;
            }
        }
        // If we've gotten this far, dmidecode failed. Encode arguments
        if (vendor.startsWith("0x")) {
            return createMIDR(vendor, stepping, model, family) + "00000000";
        }
        return createProcessorID(stepping, model, family, flags);
    }

    /**
     * Creates the MIDR, the ARM equivalent of CPUID ProcessorID
     *
     * @param vendor   the CPU implementer
     * @param stepping the "rnpn" variant and revision
     * @param model    the partnum
     * @param family   the architecture
     * @return A 32-bit hex string for the MIDR
     */
    private String createMIDR(String vendor, String stepping, String model, String family) {
        int midrBytes = 0;
        // Build 32-bit MIDR
        if (stepping.startsWith("r") && stepping.contains("p")) {
            String[] rev = stepping.substring(1).split("p");
            // 3:0 – Revision: last n in rnpn
            midrBytes |= Builder.parseLastInt(rev[1], 0);
            // 23:20 - Variant: first n in rnpn
            midrBytes |= Builder.parseLastInt(rev[0], 0) << 20;
        }
        // 15:4 - PartNum = model
        midrBytes |= Builder.parseLastInt(model, 0) << 4;
        // 19:16 - Architecture = family
        midrBytes |= Builder.parseLastInt(family, 0) << 16;
        // 31:24 - Implementer = vendor
        midrBytes |= Builder.parseLastInt(vendor, 0) << 24;

        return String.format("%08X", midrBytes);
    }

    @Override
    public long queryContextSwitches() {
        List<String> procStat = Builder.readFile(ProcUtils.getProcPath() + ProcUtils.STAT);
        for (String stat : procStat) {
            if (stat.startsWith("ctxt ")) {
                String[] ctxtArr = Builder.whitespaces.split(stat);
                if (ctxtArr.length == 2) {
                    return Builder.parseLongOrDefault(ctxtArr[1], 0);
                }
            }
        }
        return -1;
    }

    @Override
    public long queryInterrupts() {
        List<String> procStat = Builder.readFile(ProcUtils.getProcPath() + ProcUtils.STAT);
        for (String stat : procStat) {
            if (stat.startsWith("intr ")) {
                String[] intrArr = Builder.whitespaces.split(stat);
                if (intrArr.length > 2) {
                    return Builder.parseLongOrDefault(intrArr[1], 0);
                }
            }
        }
        return -1;
    }

}
