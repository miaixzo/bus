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
package org.miaixz.bus.health.mac.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import com.sun.jna.platform.mac.SystemB;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.common.AbstractCentralProcessor;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.mac.SysctlKit;
import org.miaixz.bus.logger.Logger;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A CPU.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class MacCentralProcessor extends AbstractCentralProcessor {

    private static final Set<String> ARM_P_CORES = Stream
            .of("apple,firestorm arm,v8", "apple,avalanche arm,v8", "apple,everest arm,v8").collect(Collectors.toSet());

    private static final int ARM_CPUTYPE = 0x0100000C;
    private static final int M1_CPUFAMILY = 0x1b588bb3;
    private static final int M2_CPUFAMILY = 0xda33d83d;
    private static final int M3_CPUFAMILY = 0x8765edea;
    private static final long DEFAULT_FREQUENCY = 2_400_000_000L;
    private static final Pattern CPU_N = Pattern.compile("^cpu(\\d+)");

    private final Supplier<String> vendor = Memoizer.memoize(MacCentralProcessor::platformExpert);
    private final boolean isArmCpu = isArmCpu();

    // Equivalents of hw.cpufrequency on Apple Silicon, defaulting to Rosetta value
    // Will update during initialization
    private long performanceCoreFrequency = DEFAULT_FREQUENCY;
    private long efficiencyCoreFrequency = DEFAULT_FREQUENCY;

    private static String platformExpert() {
        String manufacturer = null;
        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            // Get manufacturer from IOPlatformExpertDevice
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = Native.toString(data, Charset.UTF_8);
            }
            platformExpert.release();
        }
        return StringKit.isBlank(manufacturer) ? "Apple Inc." : manufacturer;
    }

    // Called by initProcessorCount in the constructor
    // These populate the physical processor id strings
    private static Map<Integer, String> queryCompatibleStrings() {
        Map<Integer, String> compatibleStrMap = new HashMap<>();
        // All CPUs are an IOPlatformDevice
        // Iterate each CPU and save frequency and "compatible" strings
        IOIterator iter = IOKitUtil.getMatchingServices("IOPlatformDevice");
        if (iter != null) {
            IORegistryEntry cpu = iter.next();
            while (cpu != null) {
                Matcher m = CPU_N.matcher(cpu.getName().toLowerCase(Locale.ROOT));
                if (m.matches()) {
                    int procId = Parsing.parseIntOrDefault(m.group(1), 0);
                    // Compatible key is null-delimited C string array in byte array
                    byte[] data = cpu.getByteArrayProperty("compatible");
                    if (data != null) {
                        // Byte array is null delimited
                        // Example value for M2: "apple,blizzard", "ARM,v8"
                        compatibleStrMap.put(procId,
                                new String(data, Charset.UTF_8).replace('\0', Symbol.C_SPACE).trim());
                    }
                }
                cpu.release();
                cpu = iter.next();
            }
            iter.release();
        }
        return compatibleStrMap;
    }

    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String cpuName = SysctlKit.sysctl("machdep.cpu.brand_string", Normal.EMPTY);
        String cpuVendor;
        String cpuStepping;
        String cpuModel;
        String cpuFamily;
        String processorID;
        // Initial M1 chips said "Apple Processor". Later branding includes M1, M1 Pro,
        // M1 Max, M2, etc. So if it starts with Apple it's M-something.
        if (cpuName.startsWith("Apple")) {
            // Processing an M1 chip
            cpuVendor = vendor.get();
            cpuStepping = "0"; // No correlation yet
            cpuModel = "0"; // No correlation yet
            int type;
            int family;
            if (isArmCpu) {
                type = ARM_CPUTYPE;
                int mSeries = Parsing.getFirstIntValue(cpuName);
                switch (mSeries) {
                    case 2:
                        family = M2_CPUFAMILY;
                        break;
                    case 3:
                        family = M3_CPUFAMILY;
                        break;
                    default:
                        // Some M1 did not brand as such
                        family = M1_CPUFAMILY;
                }
            } else {
                type = SysctlKit.sysctl("hw.cputype", 0);
                family = SysctlKit.sysctl("hw.cpufamily", 0);
            }
            // Translate to output
            cpuFamily = String.format(Locale.ROOT, "0x%08x", family);
            // Processor ID is an intel concept but CPU type + family conveys same info
            processorID = String.format(Locale.ROOT, "%08x%08x", type, family);
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
            processorID = String.format(Locale.ROOT, "%016x", processorIdBits);
        }
        if (isArmCpu) {
            calculateNominalFrequencies();
        }
        long cpuFreq = isArmCpu ? performanceCoreFrequency : SysctlKit.sysctl("hw.cpufrequency", 0L);
        boolean cpu64bit = SysctlKit.sysctl("hw.cpu64bit_capable", 0) != 0;

        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping, processorID, cpu64bit,
                cpuFreq);
    }

    @Override
    protected Tuple initProcessorCounts() {
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
        Map<Integer, String> compatMap = queryCompatibleStrings();
        int perflevels = SysctlKit.sysctl("hw.nperflevels", 1);
        List<CentralProcessor.PhysicalProcessor> physProcs = pkgCoreKeys.stream().sorted().map(k -> {
            String compat = compatMap.getOrDefault(k, Normal.EMPTY).toLowerCase(Locale.ROOT);
            // This is brittle. A better long term solution is to use sysctls
            // hw.perflevel1.physicalcpu: 2
            // hw.perflevel0.physicalcpu: 8
            // Note the 1 and 0 values are reversed from API definition
            int efficiency = ARM_P_CORES.contains(compat) ? 1 : 0;
            return new CentralProcessor.PhysicalProcessor(k >> 16, k & 0xffff, efficiency, compat);
        }).collect(Collectors.toList());
        List<CentralProcessor.ProcessorCache> caches = orderedProcCaches(getCacheValues(perflevels));
        List<String> featureFlags = getFeatureFlagsFromSysctl();
        return new Tuple(logProcs, physProcs, caches, featureFlags);
    }

    private Set<CentralProcessor.ProcessorCache> getCacheValues(int perflevels) {
        int linesize = (int) SysctlKit.sysctl("hw.cachelinesize", 0L);
        int l1associativity = SysctlKit.sysctl("machdep.cpu.cache.L1_associativity", 0, false);
        int l2associativity = SysctlKit.sysctl("machdep.cpu.cache.L2_associativity", 0, false);
        Set<CentralProcessor.ProcessorCache> caches = new HashSet<>();
        for (int i = 0; i < perflevels; i++) {
            int size = SysctlKit.sysctl("hw.perflevel" + i + ".l1icachesize", 0, false);
            if (size > 0) {
                caches.add(new CentralProcessor.ProcessorCache(1, l1associativity, linesize, size, CentralProcessor.ProcessorCache.Type.INSTRUCTION));
            }
            size = SysctlKit.sysctl("hw.perflevel" + i + ".l1dcachesize", 0, false);
            if (size > 0) {
                caches.add(new CentralProcessor.ProcessorCache(1, l1associativity, linesize, size, CentralProcessor.ProcessorCache.Type.DATA));
            }
            size = SysctlKit.sysctl("hw.perflevel" + i + ".l2cachesize", 0, false);
            if (size > 0) {
                caches.add(new CentralProcessor.ProcessorCache(2, l2associativity, linesize, size, CentralProcessor.ProcessorCache.Type.UNIFIED));
            }
            size = SysctlKit.sysctl("hw.perflevel" + i + ".l3cachesize", 0, false);
            if (size > 0) {
                caches.add(new CentralProcessor.ProcessorCache(3, 0, linesize, size, CentralProcessor.ProcessorCache.Type.UNIFIED));
            }
        }
        return caches;
    }

    private List<String> getFeatureFlagsFromSysctl() {
        List<String> x86Features = Arrays.asList("features", "extfeatures", "leaf7_features").stream().map(f -> {
            String key = "machdep.cpu." + f;
            String features = SysctlKit.sysctl(key, Normal.EMPTY, false);
            return StringKit.isBlank(features) ? null : (key + ": " + features);
        }).filter(Objects::nonNull).collect(Collectors.toList());
        return x86Features.isEmpty() ? Executor.runNative("sysctl -a hw.optional") : x86Features;
    }

    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        int machPort = SystemB.INSTANCE.mach_host_self();
        try (Struct.CloseableHostCpuLoadInfo cpuLoadInfo = new Struct.CloseableHostCpuLoadInfo();
             ByRef.CloseableIntByReference size = new ByRef.CloseableIntByReference(cpuLoadInfo.size())) {
            if (0 != SystemB.INSTANCE.host_statistics(machPort, SystemB.HOST_CPU_LOAD_INFO, cpuLoadInfo, size)) {
                Logger.error("Failed to get System CPU ticks. Error code: {} ", Native.getLastError());
                return ticks;
            }

            ticks[CentralProcessor.TickType.USER.getIndex()] = cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_USER];
            ticks[CentralProcessor.TickType.NICE.getIndex()] = cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_NICE];
            ticks[CentralProcessor.TickType.SYSTEM.getIndex()] = cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_SYSTEM];
            ticks[CentralProcessor.TickType.IDLE.getIndex()] = cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_IDLE];
        }
        // Leave IOWait and IRQ values as 0
        return ticks;
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
    public long[] queryCurrentFreq() {
        if (isArmCpu) {
            Map<Integer, Long> physFreqMap = new HashMap<>();
            getPhysicalProcessors().stream().forEach(p -> physFreqMap.put(p.getPhysicalProcessorNumber(),
                    p.getEfficiency() > 0 ? performanceCoreFrequency : efficiencyCoreFrequency));
            return getLogicalProcessors().stream().map(CentralProcessor.LogicalProcessor::getPhysicalProcessorNumber)
                    .map(p -> physFreqMap.getOrDefault(p, performanceCoreFrequency)).mapToLong(f -> f).toArray();
        }
        return new long[]{getProcessorIdentifier().getVendorFreq()};
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

    @Override
    public long queryMaxFreq() {
        if (isArmCpu) {
            return performanceCoreFrequency;
        }
        return SysctlKit.sysctl("hw.cpufrequency_max", getProcessorIdentifier().getVendorFreq());
    }

    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        long[][] ticks = new long[getLogicalProcessorCount()][CentralProcessor.TickType.values().length];

        int machPort = SystemB.INSTANCE.mach_host_self();
        try (ByRef.CloseableIntByReference procCount = new ByRef.CloseableIntByReference();
             ByRef.CloseablePointerByReference procCpuLoadInfo = new ByRef.CloseablePointerByReference();
             ByRef.CloseableIntByReference procInfoCount = new ByRef.CloseableIntByReference()) {
            if (0 != SystemB.INSTANCE.host_processor_info(machPort, SystemB.PROCESSOR_CPU_LOAD_INFO, procCount,
                    procCpuLoadInfo, procInfoCount)) {
                Logger.error("Failed to update CPU Load. Error code: {}", Native.getLastError());
                return ticks;
            }

            int[] cpuTicks = procCpuLoadInfo.getValue().getIntArray(0, procInfoCount.getValue());
            for (int cpu = 0; cpu < procCount.getValue(); cpu++) {
                int offset = cpu * SystemB.CPU_STATE_MAX;
                ticks[cpu][CentralProcessor.TickType.USER.getIndex()] = Formats
                        .getUnsignedInt(cpuTicks[offset + SystemB.CPU_STATE_USER]);
                ticks[cpu][CentralProcessor.TickType.NICE.getIndex()] = Formats
                        .getUnsignedInt(cpuTicks[offset + SystemB.CPU_STATE_NICE]);
                ticks[cpu][CentralProcessor.TickType.SYSTEM.getIndex()] = Formats
                        .getUnsignedInt(cpuTicks[offset + SystemB.CPU_STATE_SYSTEM]);
                ticks[cpu][CentralProcessor.TickType.IDLE.getIndex()] = Formats
                        .getUnsignedInt(cpuTicks[offset + SystemB.CPU_STATE_IDLE]);
            }
        }
        return ticks;
    }

    // Called when initiating instance variables which occurs after constructor has
    // populated physical processors
    private boolean isArmCpu() {
        return getPhysicalProcessors().stream().map(CentralProcessor.PhysicalProcessor::getIdString).anyMatch(id -> id.contains("arm"));
    }

    private void calculateNominalFrequencies() {
        IOIterator iter = IOKitUtil.getMatchingServices("AppleARMIODevice");
        if (iter != null) {
            try {
                IORegistryEntry device = iter.next();
                try {
                    while (device != null) {
                        if (device.getName().equalsIgnoreCase("pmgr")) {
                            performanceCoreFrequency = getMaxFreqFromByteArray(
                                    device.getByteArrayProperty("voltage-states5-sram"));
                            efficiencyCoreFrequency = getMaxFreqFromByteArray(
                                    device.getByteArrayProperty("voltage-states1-sram"));
                            return;
                        }
                        device.release();
                        device = iter.next();
                    }
                } finally {
                    if (device != null) {
                        device.release();
                    }
                }
            } finally {
                iter.release();
            }
        }
    }

    private long getMaxFreqFromByteArray(byte[] data) {
        // Max freq is 8 bytes from the end of the array
        if (data != null && data.length >= 8) {
            byte[] freqData = Arrays.copyOfRange(data, data.length - 8, data.length - 4);
            return Parsing.byteArrayToLong(freqData, 4, false);
        }
        return DEFAULT_FREQUENCY;
    }
}
