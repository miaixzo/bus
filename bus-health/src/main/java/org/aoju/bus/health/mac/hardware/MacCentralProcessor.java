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
package org.aoju.bus.health.mac.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.HostCpuLoadInfo;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.core.lang.tuple.Quartet;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Formats;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.hardware.AbstractCentralProcessor;
import org.aoju.bus.health.builtin.hardware.CentralProcessor;
import org.aoju.bus.health.mac.SysctlKit;
import org.aoju.bus.logger.Logger;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * A CPU.
 *
 * @author Kimi Liu
 * @version 6.3.5
 * @since JDK 1.8+
 */
@ThreadSafe
final class MacCentralProcessor extends AbstractCentralProcessor {

    private static final int ROSETTA_CPUTYPE = 0x00000007;
    private static final int ROSETTA_CPUFAMILY = 0x573b5eec;
    private static final int M1_CPUTYPE = 0x0100000C;
    private static final int M1_CPUFAMILY = 0x1b588bb3;
    private final Supplier<String> vendor = Memoize.memoize(MacCentralProcessor::platformExpert);

    private static String platformExpert() {
        String manufacturer = null;
        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            // Get manufacturer from IOPlatformExpertDevice
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = Native.toString(data, StandardCharsets.UTF_8);
            }
            platformExpert.release();
        }
        return StringKit.isBlank(manufacturer) ? "Apple Inc." : manufacturer;
    }

    private static Quartet<Integer, Integer, Long, Map<Integer, String>> queryArmCpu() {
        int type = ROSETTA_CPUTYPE;
        int family = ROSETTA_CPUFAMILY;
        long freq = 0L;
        Map<Integer, String> compatibleStrMap = new HashMap<>();
        // All CPUs are an IOPlatformDevice
        // Iterate each CPU and save frequency and "compatible" strings
        IOIterator iter = IOKitUtil.getMatchingServices("IOPlatformDevice");
        if (iter != null) {
            Set<String> compatibleStrSet = new HashSet<>();
            IORegistryEntry cpu = iter.next();
            while (cpu != null) {
                if (cpu.getName().toLowerCase().startsWith("cpu")) {
                    int procId = Builder.getFirstIntValue(cpu.getName());
                    // Accurate CPU vendor frequency in kHz as little-endian byte array
                    byte[] data = cpu.getByteArrayProperty("clock-frequency");
                    if (data != null) {
                        long cpuFreq = Builder.byteArrayToLong(data, data.length, false) * 1000L;
                        if (cpuFreq > freq) {
                            freq = cpuFreq;
                        }
                    }
                    // Compatible key is null-delimited C string array in byte array
                    data = cpu.getByteArrayProperty("compatible");
                    if (data != null) {
                        for (String s : new String(data, StandardCharsets.UTF_8).split("\0")) {
                            if (!s.isEmpty()) {
                                compatibleStrSet.add(s);
                                if (compatibleStrMap.containsKey(procId)) {
                                    compatibleStrMap.put(procId, compatibleStrMap.get(procId) + " " + s);
                                } else {
                                    compatibleStrMap.put(procId, s);
                                }
                            }
                        }
                    }
                }
                cpu.release();
                cpu = iter.next();
            }
            iter.release();
            // Match strings in "compatible" field with expectation for M1 chip
            // Hard coded for M1 for now. Need to update and make more configurable for M1X,
            // M2, etc.
            List<String> m1compatible = Arrays.asList("ARM,v8", "apple,firestorm", "apple,icestorm");
            compatibleStrSet.retainAll(m1compatible);
            if (compatibleStrSet.size() == m1compatible.size()) {
                type = M1_CPUTYPE;
                family = M1_CPUFAMILY;
            }
        }
        return new Quartet<>(type, family, freq, compatibleStrMap);
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String cpuName = SysctlKit.sysctl("machdep.cpu.brand_string", Normal.EMPTY);
        String cpuVendor;
        String cpuStepping;
        String cpuModel;
        String cpuFamily;
        String processorID;
        long cpuFreq = 0L;
        if (cpuName.startsWith("Apple")) {
            // Processing an M1 chip
            cpuVendor = vendor.get();
            cpuStepping = "0"; // No correlation yet
            cpuModel = "0"; // No correlation yet
            int type = SysctlKit.sysctl("hw.cputype", 0);
            int family = SysctlKit.sysctl("hw.cpufamily", 0);
            // M1 should have hw.cputype 0x0100000C (ARM64) and hw.cpufamily 0x1b588bb3 for
            // an ARM SoC. However, under Rosetta 2, low level cpuid calls in the translated
            // environment report hw.cputype for x86 (0x00000007) and hw.cpufamily for an
            // Intel Westmere chip (0x573b5eec), family 6, model 44, stepping 0.
            // Test if under Rosetta and generate correct chip
            Quartet<Integer, Integer, Long, Map<Integer, String>> armCpu = queryArmCpu();
            if (family == ROSETTA_CPUFAMILY) {
                type = armCpu.getA();
                family = armCpu.getB();
            }
            cpuFreq = armCpu.getC();
            // Translate to output
            cpuFamily = String.format("0x%08x", family);
            // Processor ID is an intel concept but CPU type + family conveys same info
            processorID = String.format("%08x%08x", type, family);
        } else {
            // Processing an Intel chip
            cpuVendor = SysctlKit.sysctl("machdep.cpu.vendor", Normal.EMPTY);
            int i = SysctlKit.sysctl("machdep.cpu.stepping", -1);
            cpuStepping = i < 0 ? Normal.EMPTY : Integer.toString(i);
            i = SysctlKit.sysctl("machdep.cpu.model", -1);
            cpuModel = i < 0 ? Normal.EMPTY : Integer.toString(i);
            i = SysctlKit.sysctl("machdep.cpu.family", -1);
            cpuFamily = i < 0 ? Normal.EMPTY : Integer.toString(i);
            long processorIdBits = 0L;
            processorIdBits |= SysctlKit.sysctl("machdep.cpu.signature", 0);
            processorIdBits |= (SysctlKit.sysctl("machdep.cpu.feature_bits", 0L) & 0xffffffff) << 32;
            processorID = String.format("%016x", processorIdBits);
        }
        if (cpuFreq == 0) {
            cpuFreq = SysctlKit.sysctl("hw.cpufrequency", 0L);
        }
        boolean cpu64bit = SysctlKit.sysctl("hw.cpu64bit_capable", 0) != 0;

        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit,
                cpuFreq);
    }

    @Override
    protected Pair<List<CentralProcessor.LogicalProcessor>, List<CentralProcessor.PhysicalProcessor>> initProcessorCounts() {
        int logicalProcessorCount = SysctlKit.sysctl("hw.logicalcpu", 1);
        int physicalProcessorCount = SysctlKit.sysctl("hw.physicalcpu", 1);
        int physicalPackageCount = SysctlKit.sysctl("hw.packages", 1);
        List<CentralProcessor.LogicalProcessor> logProcs = new ArrayList<>(logicalProcessorCount);
        Set<Integer> pkgCoreKeys = new HashSet<>();
        for (int i = 0; i < logicalProcessorCount; i++) {
            int coreId = i * physicalProcessorCount / logicalProcessorCount;
            int pkgId = i * physicalPackageCount / logicalProcessorCount;
            logProcs.add(new CentralProcessor.LogicalProcessor(i, coreId, pkgId));
            pkgCoreKeys.add((pkgId << 16) + coreId);
        }
        Map<Integer, String> compatMap = queryArmCpu().getD();
        List<CentralProcessor.PhysicalProcessor> physProcs = pkgCoreKeys.stream().sorted().map(k -> {
            String compat = compatMap.getOrDefault(k, Normal.EMPTY);
            int efficiency = 0; // default, for E-core icestorm
            if (compat.toLowerCase().contains("firestorm")) {
                efficiency = 1; // P-core, more performance
            }
            return new CentralProcessor.PhysicalProcessor(k >> 16, k & 0xffff, efficiency, compat);
        }).collect(Collectors.toList());
        return Pair.of(logProcs, physProcs);
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        int machPort = SystemB.INSTANCE.mach_host_self();
        HostCpuLoadInfo cpuLoadInfo = new HostCpuLoadInfo();
        if (0 != SystemB.INSTANCE.host_statistics(machPort, SystemB.HOST_CPU_LOAD_INFO, cpuLoadInfo,
                new IntByReference(cpuLoadInfo.size()))) {
            Logger.error("Failed to get System CPU ticks. Error code: {} ", Native.getLastError());
            return ticks;
        }

        ticks[CentralProcessor.TickType.USER.getIndex()] = cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_USER];
        ticks[CentralProcessor.TickType.NICE.getIndex()] = cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_NICE];
        ticks[CentralProcessor.TickType.SYSTEM.getIndex()] = cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_SYSTEM];
        ticks[CentralProcessor.TickType.IDLE.getIndex()] = cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_IDLE];
        // Leave IOWait and IRQ values as 0
        return ticks;
    }

    @Override
    public long[] queryCurrentFreq() {
        long[] freq = new long[1];
        freq[0] = SysctlKit.sysctl("hw.cpufrequency", getProcessorIdentifier().getVendorFreq());
        return freq;
    }

    @Override
    public long queryMaxFreq() {
        return SysctlKit.sysctl("hw.cpufrequency_max", getProcessorIdentifier().getVendorFreq());
    }

    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        int retval = SystemB.INSTANCE.getloadavg(average, nelem);
        if (retval < nelem) {
            Arrays.fill(average, -1d);
        }
        return average;
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        long[][] ticks = new long[getLogicalProcessorCount()][CentralProcessor.TickType.values().length];

        int machPort = SystemB.INSTANCE.mach_host_self();

        IntByReference procCount = new IntByReference();
        PointerByReference procCpuLoadInfo = new PointerByReference();
        IntByReference procInfoCount = new IntByReference();
        if (0 != SystemB.INSTANCE.host_processor_info(machPort, SystemB.PROCESSOR_CPU_LOAD_INFO, procCount,
                procCpuLoadInfo, procInfoCount)) {
            Logger.error("Failed to update CPU Load. Error code: {}", Native.getLastError());
            return ticks;
        }

        int[] cpuTicks = procCpuLoadInfo.getValue().getIntArray(0, procInfoCount.getValue());
        for (int cpu = 0; cpu < procCount.getValue(); cpu++) {
            int offset = cpu * SystemB.CPU_STATE_MAX;
            ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = Formats.getUnsignedInt(cpuTicks[offset + SystemB.CPU_STATE_USER]);
            ticks[cpu][CentralProcessor.TickType.NICE.getIndex()] = Formats.getUnsignedInt(cpuTicks[offset + SystemB.CPU_STATE_NICE]);
            ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = Formats
                    .getUnsignedInt(cpuTicks[offset + SystemB.CPU_STATE_SYSTEM]);
            ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = Formats.getUnsignedInt(cpuTicks[offset + SystemB.CPU_STATE_IDLE]);
        }
        return ticks;
    }

    @Override
    public long queryContextSwitches() {
        // Not available on macOS since at least 10.3.9. Early versions may have
        // provided access to the vmmeter structure using sysctl [CTL_VM, VM_METER] but
        // it now fails (ENOENT) and there is no other reference to it in source code
        return 0L;
    }

    @Override
    public long queryInterrupts() {
        // Not available on macOS since at least 10.3.9. Early versions may have
        // provided access to the vmmeter structure using sysctl [CTL_VM, VM_METER] but
        // it now fails (ENOENT) and there is no other reference to it in source code
        return 0L;
    }

}
