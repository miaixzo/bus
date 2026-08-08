/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.mac.hardware;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.jna.Pointer;
import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Charset;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
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
import org.miaixz.bus.health.mac.jna.SystemB;
import org.miaixz.bus.logger.Logger;

/**
 * <p>
 * MacCentralProcessor class.
 * </p>
 * A CPU.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class MacCentralProcessor extends AbstractCentralProcessor {

    /**
     * The ARM_CPUTYPE constant.
     */
    private static final int ARM_CPUTYPE = 0x0100000C;

    /**
     * The DEFAULT_FREQUENCY constant.
     */
    private static final long DEFAULT_FREQUENCY = 2_400_000_000L;

    /**
     * The CPU_N constant.
     */
    private static final Pattern CPU_N = Pattern.compile("^cpu(\\d+)");

    /**
     * The APPLE_CORE constant.
     */
    private static final Pattern APPLE_CORE = Pattern.compile("apple,([a-z0-9_.-]+)");

    /**
     * The MICROARCH_PREFIX constant.
     */
    private static final String MICROARCH_PREFIX = "ARM64 SoC: ";

    /**
     * The vendor value.
     */
    private final SupplierX<String> vendor = Memoizer.memoize(MacCentralProcessor::platformExpert);

    /**
     * The isArmCpu value.
     */
    private final boolean isArmCpu = isArmCpu();

    // Equivalents of hw.cpufrequency on Apple Silicon, defaulting to Rosetta value
    // Will update during initialization
    /**
     * The performanceCoreFrequency value.
     */
    private volatile long performanceCoreFrequency = DEFAULT_FREQUENCY;

    /**
     * The efficiencyCoreFrequency value.
     */
    private volatile long efficiencyCoreFrequency = DEFAULT_FREQUENCY;

    /**
     * Queries the platform expert device for the CPU manufacturer.
     *
     * @return The CPU manufacturer string, or "Apple Inc." if not found.
     */
    private static String platformExpert() {
        String manufacturer = null;
        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            // Get manufacturer from IOPlatformExpertDevice
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = Parsing.decodeNulTerminated(data, Charset.UTF_8);
            }
            platformExpert.release();
        }
        return StringKit.isBlank(manufacturer) ? "Apple Inc." : manufacturer;
    }

    /**
     * Queries core properties for each CPU from the I/O Registry.
     *
     * @return A map where the key is the processor ID and the value is the compatible and cluster-type properties.
     */
    private static Map<Integer, Pair<String, String>> queryCoreProperties() {
        Map<Integer, Pair<String, String>> coreProperties = new HashMap<>();
        // All CPUs are an IOPlatformDevice
        // Iterate each CPU and save "compatible" and "cluster-type" strings
        IOIterator iter = IOKitUtil.getMatchingServices("IOPlatformDevice");
        if (iter != null) {
            IORegistryEntry cpu = iter.next();
            while (cpu != null) {
                Matcher m = CPU_N.matcher(cpu.getName().toLowerCase(Locale.ROOT));
                if (m.matches()) {
                    int procId = Parsing.parseIntOrDefault(m.group(1), 0);
                    // Compatible key is null-delimited C string array in byte array
                    coreProperties.put(
                            procId,
                            Pair.of(
                                    ioRegString(cpu.getByteArrayProperty("compatible")),
                                    ioRegString(cpu.getByteArrayProperty("cluster-type"))));
                }
                cpu.release();
                cpu = iter.next();
            }
            iter.release();
        }
        return coreProperties;
    }

    /**
     * Decodes an IORegistry byte-array property. These values may hold more than one NUL-terminated string.
     *
     * @param data the raw property bytes
     * @return the decoded string, or {@code null} if the property is absent
     */
    private static String ioRegString(byte[] data) {
        return data == null ? null : new String(data, Charset.UTF_8).replace('\0', Symbol.C_SPACE).trim();
    }

    /**
     * Derives an efficiency class for each physical core.
     *
     * @param coreKeys          the physical core keys
     * @param coreProperties    the per-core compatible and cluster-type properties
     * @param topPerfLevelCores the number of cores in the highest performance level
     * @return a map from core key to efficiency class
     */
    static Map<Integer, Integer> deriveEfficiencyClasses(
            List<Integer> coreKeys,
            Map<Integer, Pair<String, String>> coreProperties,
            int topPerfLevelCores) {
        Map<Integer, Integer> efficiencyMap = new HashMap<>();
        Map<String, Integer> classByCodename = new HashMap<>();
        for (Integer key : coreKeys) {
            Integer efficiency = clusterTypeClass(coreProperties.get(key));
            if (efficiency != null) {
                efficiencyMap.put(key, efficiency);
                String codename = codename(coreProperties.get(key));
                if (codename != null) {
                    classByCodename.put(codename, efficiency);
                }
            }
        }
        if (!efficiencyMap.isEmpty() && efficiencyMap.size() < coreKeys.size()) {
            for (Integer key : coreKeys) {
                if (!efficiencyMap.containsKey(key)) {
                    String codename = codename(coreProperties.get(key));
                    Integer efficiency = codename == null ? null : classByCodename.get(codename);
                    if (efficiency != null) {
                        efficiencyMap.put(key, efficiency);
                    }
                }
            }
        }
        if (efficiencyMap.size() == coreKeys.size()) {
            return efficiencyMap;
        }
        Map<String, List<Integer>> groups = new LinkedHashMap<>();
        if (efficiencyMap.isEmpty()) {
            for (Integer key : coreKeys) {
                String codename = codename(coreProperties.get(key));
                if (codename == null) {
                    groups.clear();
                    break;
                }
                groups.computeIfAbsent(codename, ignored -> new ArrayList<>()).add(key);
            }
        }
        List<List<Integer>> ordered = new ArrayList<>(groups.values());
        if (ordered.size() == 2 && topPerfLevelCores > 0 && ordered.get(0).size() == topPerfLevelCores
                && ordered.get(1).size() != topPerfLevelCores) {
            Collections.reverse(ordered);
        }
        for (int i = 0; i < ordered.size(); i++) {
            for (Integer key : ordered.get(i)) {
                efficiencyMap.put(key, i);
            }
        }
        for (Integer key : coreKeys) {
            efficiencyMap.putIfAbsent(key, 0);
        }
        return efficiencyMap;
    }

    /**
     * Maps a core's cluster type to an efficiency class.
     *
     * @param properties the core properties
     * @return the efficiency class, or {@code null} if unknown
     */
    private static Integer clusterTypeClass(Pair<String, String> properties) {
        String clusterType = properties == null ? null : properties.getRight();
        if (clusterType == null || clusterType.isEmpty()) {
            return null;
        }
        char c = Character.toUpperCase(clusterType.charAt(0));
        if (c == 'P') {
            return 1;
        }
        return c == 'E' ? 0 : null;
    }

    /**
     * Extracts the Apple core codename from a core compatible string.
     *
     * @param properties the core properties
     * @return the codename, or {@code null} if absent
     */
    private static String codename(Pair<String, String> properties) {
        String compatible = properties == null ? null : properties.getLeft();
        if (compatible == null) {
            return null;
        }
        Matcher matcher = APPLE_CORE.matcher(compatible.toLowerCase(Locale.ROOT));
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Derives a microarchitecture description from Apple core codenames.
     *
     * @param physicalProcessors the physical processors
     * @return the microarchitecture description, or {@code null} if no codename is found
     */
    static String deriveMicroarchitecture(List<CentralProcessor.PhysicalProcessor> physicalProcessors) {
        Map<String, Pair<Integer, Integer>> codenames = new LinkedHashMap<>();
        for (CentralProcessor.PhysicalProcessor processor : physicalProcessors) {
            Matcher matcher = APPLE_CORE.matcher(processor.getIdString().toLowerCase(Locale.ROOT));
            if (matcher.find() && !codenames.containsKey(matcher.group(1))) {
                codenames.put(
                        matcher.group(1),
                        Pair.of(processor.getEfficiency(), processor.getPhysicalProcessorNumber()));
            }
        }
        if (codenames.isEmpty()) {
            return null;
        }
        List<Map.Entry<String, Pair<Integer, Integer>>> entries = new ArrayList<>(codenames.entrySet());
        entries.sort((left, right) -> {
            int byEfficiency = right.getValue().getLeft().compareTo(left.getValue().getLeft());
            return byEfficiency == 0 ? left.getValue().getRight().compareTo(right.getValue().getRight()) : byEfficiency;
        });
        StringBuilder builder = new StringBuilder(MICROARCH_PREFIX);
        for (int i = 0; i < entries.size(); i++) {
            String codename = entries.get(i).getKey();
            if (i > 0) {
                builder.append(" + ");
            }
            builder.append(codename.substring(0, 1).toUpperCase(Locale.ROOT)).append(codename.substring(1));
        }
        return builder.toString();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        String cpuName = SysctlKit.sysctl("machdep.cpu.brand_string", Normal.EMPTY);
        String cpuVendor;
        String cpuStepping;
        String cpuModel;
        String cpuFamily;
        String processorID;
        String microarchitecture = null;
        // Initial M1 chips said "Apple Processor". Later branding includes M1, M1 Pro,
        // M1 Max, M2, etc. So if it starts with Apple it's M-something.
        if (cpuName.startsWith("Apple")) {
            // Processing an M1 chip
            cpuVendor = vendor.get();
            cpuStepping = Symbol.ZERO; // No correlation yet
            cpuModel = Symbol.ZERO; // No correlation yet
            int type;
            int family;
            if (isArmCpu) {
                type = ARM_CPUTYPE;
                family = SysctlKit.sysctl("hw.cpufamily", 0);
                microarchitecture = deriveMicroarchitecture(getPhysicalProcessors());
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

        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping,
                processorID, cpu64bit, cpuFreq, microarchitecture);
    }

    /**
     * Description inherited from parent class or interface.
     */
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
        Map<Integer, Pair<String, String>> coreProps = queryCoreProperties();
        int perflevels = SysctlKit.sysctl("hw.nperflevels", 1, false);
        int topPerfLevelCores = SysctlKit.sysctl("hw.perflevel0.physicalcpu", 0, false);
        List<Integer> coreKeys = pkgCoreKeys.stream().sorted().collect(Collectors.toList());
        Map<Integer, Integer> efficiencyMap = deriveEfficiencyClasses(coreKeys, coreProps, topPerfLevelCores);
        List<CentralProcessor.PhysicalProcessor> physProcs = new ArrayList<>(coreKeys.size());
        for (Integer key : coreKeys) {
            Pair<String, String> props = coreProps.get(key);
            String compat = props == null || props.getLeft() == null ? Normal.EMPTY
                    : props.getLeft().toLowerCase(Locale.ROOT);
            physProcs.add(
                    new CentralProcessor.PhysicalProcessor(key >> 16, key & 0xffff, efficiencyMap.getOrDefault(key, 0),
                            compat));
        }
        List<CentralProcessor.ProcessorCache> caches = orderedProcCaches(getCacheValues(perflevels));
        List<String> featureFlags = getFeatureFlagsFromSysctl();
        return new Tuple(logProcs, physProcs, caches, featureFlags);
    }

    /**
     * Retrieves processor cache values from sysctl.
     *
     * @param perflevels The number of performance levels.
     * @return A set of {@link CentralProcessor.ProcessorCache} objects.
     */
    private Set<CentralProcessor.ProcessorCache> getCacheValues(int perflevels) {
        int linesize = (int) SysctlKit.sysctl("hw.cachelinesize", 0L);
        int l1associativity = SysctlKit.sysctl("machdep.cpu.cache.L1_associativity", 0, false);
        int l2associativity = SysctlKit.sysctl("machdep.cpu.cache.L2_associativity", 0, false);
        Set<CentralProcessor.ProcessorCache> caches = new HashSet<>();
        for (int i = 0; i < perflevels; i++) {
            int size = SysctlKit.sysctl("hw.perflevel" + i + ".l1icachesize", 0, false);
            if (size > 0) {
                caches.add(
                        new CentralProcessor.ProcessorCache(1, l1associativity, linesize, size,
                                CentralProcessor.ProcessorCache.Type.INSTRUCTION));
            }
            size = SysctlKit.sysctl("hw.perflevel" + i + ".l1dcachesize", 0, false);
            if (size > 0) {
                caches.add(
                        new CentralProcessor.ProcessorCache(1, l1associativity, linesize, size,
                                CentralProcessor.ProcessorCache.Type.DATA));
            }
            size = SysctlKit.sysctl("hw.perflevel" + i + ".l2cachesize", 0, false);
            if (size > 0) {
                caches.add(
                        new CentralProcessor.ProcessorCache(2, l2associativity, linesize, size,
                                CentralProcessor.ProcessorCache.Type.UNIFIED));
            }
            size = SysctlKit.sysctl("hw.perflevel" + i + ".l3cachesize", 0, false);
            if (size > 0) {
                caches.add(
                        new CentralProcessor.ProcessorCache(3, 0, linesize, size,
                                CentralProcessor.ProcessorCache.Type.UNIFIED));
            }
        }
        return caches;
    }

    /**
     * Retrieves CPU feature flags from sysctl.
     *
     * @return A list of feature flag strings.
     */
    private List<String> getFeatureFlagsFromSysctl() {
        List<String> x86Features = parseX86FeatureFlags();
        return x86Features.isEmpty() ? Executor.runNative("sysctl -a hw.optional") : x86Features;
    }

    /**
     * Parses x86 feature flag values from the machdep CPU sysctl keys.
     *
     * @return A list of populated x86 feature flag entries.
     */
    List<String> parseX86FeatureFlags() {
        return Stream.of("features", "extfeatures", "leaf7_features").map(f -> {
            String key = "machdep.cpu." + f;
            String features = SysctlKit.sysctl(key, Normal.EMPTY, false);
            return StringKit.isBlank(features) ? null : (key + ": " + features);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        int machPort = SystemB.INSTANCE.mach_host_self();
        try (Struct.CloseableHostCpuLoadInfo cpuLoadInfo = new Struct.CloseableHostCpuLoadInfo();
                ByRef.CloseableIntByReference size = new ByRef.CloseableIntByReference(
                        cpuLoadInfo.size() / SystemB.INT_SIZE)) {
            int ret = SystemB.INSTANCE.host_statistics(machPort, SystemB.HOST_CPU_LOAD_INFO, cpuLoadInfo, size);
            if (0 != ret) {
                Logger.error(false, "Health", "Failed to get System CPU ticks. Error code: {} ", ret);
                return ticks;
            }

            ticks[CentralProcessor.TickType.USER.getIndex()] = Formats
                    .getUnsignedInt(cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_USER]);
            ticks[CentralProcessor.TickType.NICE.getIndex()] = Formats
                    .getUnsignedInt(cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_NICE]);
            ticks[CentralProcessor.TickType.SYSTEM.getIndex()] = Formats
                    .getUnsignedInt(cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_SYSTEM]);
            ticks[CentralProcessor.TickType.IDLE.getIndex()] = Formats
                    .getUnsignedInt(cpuLoadInfo.cpu_ticks[SystemB.CPU_STATE_IDLE]);
        }
        // Leave IOWait and IRQ values as 0
        return ticks;
    }

    /**
     * Description inherited from parent class or interface.
     */
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

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long[] queryCurrentFreq() {
        if (isArmCpu) {
            Map<Integer, Long> physFreqMap = new HashMap<>();
            getPhysicalProcessors().stream().forEach(
                    p -> physFreqMap.put(
                            p.getPhysicalProcessorNumber(),
                            p.getEfficiency() > 0 ? performanceCoreFrequency : efficiencyCoreFrequency));
            return getLogicalProcessors().stream().map(CentralProcessor.LogicalProcessor::getPhysicalProcessorNumber)
                    .map(p -> physFreqMap.getOrDefault(p, performanceCoreFrequency)).mapToLong(f -> f).toArray();
        }
        return new long[] { getProcessorIdentifier().getVendorFreq() };
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long queryContextSwitches() {
        // Not available on macOS since at least 10.3.9. Early versions may have
        // provided access to the vmmeter structure using sysctl [CTL_VM, VM_METER] but
        // it now fails (ENOENT) and there is no other reference to it in source code
        return 0L;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long queryInterrupts() {
        // Not available on macOS since at least 10.3.9. Early versions may have
        // provided access to the vmmeter structure using sysctl [CTL_VM, VM_METER] but
        // it now fails (ENOENT) and there is no other reference to it in source code
        return 0L;
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long queryMaxFreq() {
        if (isArmCpu) {
            return performanceCoreFrequency;
        }
        return SysctlKit.sysctl("hw.cpufrequency_max", getProcessorIdentifier().getVendorFreq());
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        long[][] ticks = new long[getLogicalProcessorCount()][CentralProcessor.TickType.values().length];

        int machPort = SystemB.INSTANCE.mach_host_self();
        try (ByRef.CloseableIntByReference procCount = new ByRef.CloseableIntByReference();
                ByRef.CloseablePointerByReference procCpuLoadInfo = new ByRef.CloseablePointerByReference();
                ByRef.CloseableIntByReference procInfoCount = new ByRef.CloseableIntByReference()) {
            int ret = SystemB.INSTANCE.host_processor_info(
                    machPort,
                    SystemB.PROCESSOR_CPU_LOAD_INFO,
                    procCount,
                    procCpuLoadInfo,
                    procInfoCount);
            if (0 != ret) {
                Logger.error(false, "Health", "Failed to update CPU Load. Error code: {}", ret);
                return ticks;
            }

            try {
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
            } finally {
                try {
                    SystemB.INSTANCE.vm_deallocate(
                            SystemB.INSTANCE.mach_task_self(),
                            Pointer.nativeValue(procCpuLoadInfo.getValue()),
                            (long) procInfoCount.getValue() * SystemB.INT_SIZE);
                } catch (Exception e) {
                    Logger.warn(false, "Health", "Failed to vm_deallocate processor info buffer", e);
                }
            }
        }
        return ticks;
    }

    /**
     * Determines if the CPU is an ARM-based CPU.
     *
     * @return {@code true} if the CPU is ARM-based, {@code false} otherwise.
     */
    private boolean isArmCpu() {
        return getPhysicalProcessors().stream().map(CentralProcessor.PhysicalProcessor::getIdString)
                .anyMatch(id -> id.contains("arm"));
    }

    /**
     * Calculates the nominal frequencies for performance and efficiency cores on Apple Silicon. This method queries the
     * I/O Registry for specific properties related to CPU voltage states.
     */
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

    /**
     * Extracts the maximum frequency from a byte array property.
     *
     * @param data The byte array containing frequency data.
     * @return The maximum frequency in Hz, or {@link #DEFAULT_FREQUENCY} if extraction fails.
     */
    private long getMaxFreqFromByteArray(byte[] data) {
        // Max freq is 8 bytes from the end of the array
        if (data != null && data.length >= 8) {
            byte[] freqData = Arrays.copyOfRange(data, data.length - 8, data.length - 4);
            return Parsing.byteArrayToLong(freqData, 4, false);
        }
        return DEFAULT_FREQUENCY;
    }

}
