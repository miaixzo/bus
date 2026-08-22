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
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.common.AbstractCentralProcessor;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.mac.SysctlKit;
import org.miaixz.bus.health.mac.driver.CpuFrequencyResidency;
import org.miaixz.bus.health.mac.driver.IOReportClient;
import org.miaixz.bus.health.mac.jna.SystemB;
import org.miaixz.bus.logger.Logger;

/**
 * <p>
 * MacCentralProcessor class.
 * </p>
 * A CPU.
 *
 * @author Kimi Liu
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
     * The Apple power-manager cluster table property.
     */
    private static final String ACC_CLUSTERS = "acc-clusters";

    /**
     * The legacy efficiency-cluster voltage state table.
     */
    private static final String LEGACY_EFFICIENCY_TABLE = "voltage-states1-sram";

    /**
     * The legacy performance-cluster voltage state table.
     */
    private static final String LEGACY_PERFORMANCE_TABLE = "voltage-states5-sram";

    /**
     * The lower bound of a plausible CPU frequency in hertz.
     */
    private static final long MIN_PLAUSIBLE_HZ = 100_000_000L;

    /**
     * The vendor value.
     */
    private final SupplierX<String> vendor = Memoizer.memoize(MacCentralProcessor::platformExpert);

    /**
     * The isArmCpu value.
     */
    private final boolean isArmCpu = isArmCpu();

    /**
     * The nominal frequency table for each efficiency class.
     */
    private final SupplierX<long[][]> nominalFrequencyTables = Memoizer.memoize(this::queryNominalFrequencyTables);

    /**
     * The nominal maximum frequency for each efficiency class.
     */
    private final SupplierX<long[]> nominalFrequencies = Memoizer.memoize(this::queryNominalFrequencies);

    /**
     * The IOReport CPU frequency sampler.
     */
    private final SupplierX<IOReportCpuSampler> cpuSampler = Memoizer.memoize(this::cpuFrequencySampler);

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
     * @param coreKeys       the physical core keys
     * @param coreProperties the per-core compatible and cluster-type properties
     * @param perfLevelCores the physical core count for each performance level
     * @return a map from core key to efficiency class
     */
    static Map<Integer, Integer> deriveEfficiencyClasses(
            List<Integer> coreKeys,
            Map<Integer, Pair<String, String>> coreProperties,
            int[] perfLevelCores) {
        Map<Integer, Integer> efficiencyMap = perfLevelClasses(coreKeys, perfLevelCores);
        if (!efficiencyMap.isEmpty()) {
            return efficiencyMap;
        }
        Map<Integer, Integer> rankByKey = new HashMap<>();
        Map<String, Integer> rankByCodename = new HashMap<>();
        boolean unrankedClusterType = false;
        for (Integer key : coreKeys) {
            String clusterType = clusterType(coreProperties.get(key));
            Integer rank = clusterType == null ? null : clusterTypeRank(clusterType);
            if (rank != null) {
                rankByKey.put(key, rank);
                String codename = codename(coreProperties.get(key));
                if (codename != null) {
                    rankByCodename.put(codename, rank);
                }
            } else if (clusterType != null) {
                unrankedClusterType = true;
            }
        }
        if (unrankedClusterType) {
            rankByKey.clear();
            rankByCodename.clear();
        }
        if (!rankByKey.isEmpty() && rankByKey.size() < coreKeys.size()) {
            for (Integer key : coreKeys) {
                if (rankByKey.containsKey(key)) {
                    continue;
                }
                String codename = codename(coreProperties.get(key));
                Integer rank = codename == null ? null : rankByCodename.get(codename);
                if (rank != null) {
                    rankByKey.put(key, rank);
                }
            }
        }
        Set<Integer> ranks = new TreeSet<>(rankByKey.values());
        if (rankByKey.size() < coreKeys.size()) {
            ranks.add(0);
        }
        List<Integer> presentRanks = new ArrayList<>(ranks);
        for (Map.Entry<Integer, Integer> rank : rankByKey.entrySet()) {
            efficiencyMap.put(rank.getKey(), presentRanks.indexOf(rank.getValue()));
        }
        if (efficiencyMap.size() == coreKeys.size()) {
            return efficiencyMap;
        }
        int topPerfLevelCores = perfLevelCores.length > 0 ? perfLevelCores[0] : 0;
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
     * Assigns efficiency classes from macOS performance-level core counts.
     *
     * @param coreKeys       the physical core keys
     * @param perfLevelCores the core count for each performance level
     * @return a map from core key to efficiency class, or empty if the counts cannot be trusted
     */
    private static Map<Integer, Integer> perfLevelClasses(List<Integer> coreKeys, int[] perfLevelCores) {
        Map<Integer, Integer> efficiencyMap = new HashMap<>();
        if (perfLevelCores.length < 2) {
            return efficiencyMap;
        }
        int total = 0;
        for (int levelCores : perfLevelCores) {
            if (levelCores < 1) {
                return efficiencyMap;
            }
            total += levelCores;
        }
        if (total != coreKeys.size()) {
            return efficiencyMap;
        }
        int index = coreKeys.size();
        for (int level = 0; level < perfLevelCores.length; level++) {
            int efficiency = perfLevelCores.length - 1 - level;
            for (int i = 0; i < perfLevelCores[level]; i++) {
                efficiencyMap.put(coreKeys.get(--index), efficiency);
            }
        }
        return efficiencyMap;
    }

    /**
     * Extracts a core's cluster-type value.
     *
     * @param properties the core properties
     * @return the cluster-type value, or {@code null} if absent
     */
    private static String clusterType(Pair<String, String> properties) {
        String clusterType = properties == null ? null : properties.getRight();
        return clusterType == null || clusterType.isEmpty() ? null : clusterType;
    }

    /**
     * Ranks a core cluster type against other cluster types.
     *
     * @param clusterType the cluster-type value
     * @return the rank, or {@code null} if the value is unknown
     */
    private static Integer clusterTypeRank(String clusterType) {
        switch (Character.toUpperCase(clusterType.charAt(0))) {
            case 'E':
                return 0;

            case 'P':
                return 1;

            case 'S':
                return 2;

            default:
                return null;
        }
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
        long cpuFreq = isArmCpu ? getPerformanceCoreFrequency() : SysctlKit.sysctl("hw.cpufrequency", 0L);
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
        int perflevels = Math.max(1, SysctlKit.sysctl("hw.nperflevels", 1, false));
        int[] perfLevelCores = new int[perflevels];
        for (int i = 0; i < perflevels; i++) {
            perfLevelCores[i] = SysctlKit.sysctl("hw.perflevel" + i + ".physicalcpu", 0, false);
        }
        List<Integer> coreKeys = pkgCoreKeys.stream().sorted().collect(Collectors.toList());
        Map<Integer, Integer> efficiencyMap = deriveEfficiencyClasses(coreKeys, coreProps, perfLevelCores);
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
            long[] byClass = nominalFrequencies.get();
            long topFreq = byClass[byClass.length - 1];
            Map<Integer, Long> physFreqMap = new HashMap<>();
            getPhysicalProcessors().forEach(
                    processor -> physFreqMap.put(
                            processor.getPhysicalProcessorNumber(),
                            byClass[Math.min(Math.max(processor.getEfficiency(), 0), byClass.length - 1)]));
            applyLiveFrequencies(physFreqMap);
            return getLogicalProcessors().stream().map(CentralProcessor.LogicalProcessor::getPhysicalProcessorNumber)
                    .map(processor -> physFreqMap.getOrDefault(processor, topFreq)).mapToLong(frequency -> frequency)
                    .toArray();
        }
        return new long[] { getProcessorIdentifier().getVendorFreq() };
    }

    /**
     * Applies live IOReport frequencies to the nominal physical-processor frequency map.
     *
     * @param physFreqMap the physical processor frequency map to update
     */
    private void applyLiveFrequencies(Map<Integer, Long> physFreqMap) {
        IOReportCpuSampler sampler = cpuSampler.get();
        if (sampler == null) {
            return;
        }
        CpuResidencySample sample = sampler.sampleResidencyDelta();
        if (sample == null) {
            return;
        }
        Map<String, Map<String, Long>> residency = sample.getCoreStates();
        if (residency.isEmpty()) {
            return;
        }
        Map<Integer, List<String>> channelGroups = groupChannelsByCoreType(residency.keySet());
        Map<Integer, List<CentralProcessor.PhysicalProcessor>> coreGroups = groupCoresByEfficiencyClass();
        if (channelGroups.containsKey(Normal._4)) {
            Logger.debug(
                    false,
                    "Health",
                    "IOReport reports a CPU core type this release does not recognize: {}",
                    channelGroups.get(Normal._4));
            return;
        }
        if (channelGroups.size() != coreGroups.size()) {
            Logger.debug(
                    false,
                    "Health",
                    "IOReport reports {} core types but this processor has {} efficiency classes.",
                    channelGroups.size(),
                    coreGroups.size());
            return;
        }
        Iterator<List<String>> channelIterator = channelGroups.values().iterator();
        for (Map.Entry<Integer, List<CentralProcessor.PhysicalProcessor>> cores : coreGroups.entrySet()) {
            List<String> channels = channelIterator.next();
            if (channels.size() != cores.getValue().size()) {
                Logger.debug(
                        false,
                        "Health",
                        "IOReport reports {} cores of one type but efficiency class {} has {}.",
                        channels.size(),
                        cores.getKey(),
                        cores.getValue().size());
                return;
            }
        }
        Map<Integer, Map<String, Long>> complexByRank = CpuFrequencyResidency
                .realizedComplexStates(sample.getComplexStates());
        boolean complexMatchesCores = complexByRank.keySet().equals(channelGroups.keySet());
        long[][] tables = nominalFrequencyTables.get();
        Iterator<Map.Entry<Integer, List<String>>> channelEntries = channelGroups.entrySet().iterator();
        for (Map.Entry<Integer, List<CentralProcessor.PhysicalProcessor>> cores : coreGroups.entrySet()) {
            Map.Entry<Integer, List<String>> channelEntry = channelEntries.next();
            List<String> channels = channelEntry.getValue();
            long[] table = tables[Math.min(Math.max(cores.getKey(), 0), tables.length - 1)];
            Map<String, Long> cluster = complexMatchesCores ? complexByRank.get(channelEntry.getKey()) : null;
            long realized = cluster == null ? 0L : CpuFrequencyResidency.activeWeightedFrequency(cluster, table);
            for (int i = 0; i < channels.size(); i++) {
                Map<String, Long> states = residency.get(channels.get(i));
                long frequency = states == null ? 0L : CpuFrequencyResidency.activeWeightedFrequency(states, table);
                if (frequency == 0L) {
                    continue;
                }
                if (realized > 0 && frequency != table[0]) {
                    frequency = realized;
                }
                physFreqMap.put(cores.getValue().get(i).getPhysicalProcessorNumber(), frequency);
            }
        }
    }

    /**
     * Groups IOReport channel names by CPU core type.
     *
     * @param channelNames the channel names sampled
     * @return the grouped channel names
     */
    private static Map<Integer, List<String>> groupChannelsByCoreType(Collection<String> channelNames) {
        Map<Integer, List<String>> groups = new TreeMap<>();
        for (String channel : CpuFrequencyResidency.orderChannels(channelNames)) {
            int rank = CpuFrequencyResidency.prefixRank(channel);
            groups.computeIfAbsent(rank, ignored -> new ArrayList<>()).add(channel);
        }
        return groups;
    }

    /**
     * Groups physical cores by efficiency class.
     *
     * @return the grouped physical processors
     */
    private Map<Integer, List<CentralProcessor.PhysicalProcessor>> groupCoresByEfficiencyClass() {
        Map<Integer, List<CentralProcessor.PhysicalProcessor>> groups = new TreeMap<>();
        for (CentralProcessor.PhysicalProcessor processor : getPhysicalProcessors()) {
            int efficiency = Math.max(processor.getEfficiency(), 0);
            groups.computeIfAbsent(efficiency, ignored -> new ArrayList<>()).add(processor);
        }
        for (List<CentralProcessor.PhysicalProcessor> group : groups.values()) {
            group.sort(
                    (left, right) -> Integer
                            .compare(left.getPhysicalProcessorNumber(), right.getPhysicalProcessorNumber()));
        }
        return groups;
    }

    /**
     * Creates the IOReport CPU frequency sampler when live frequency reporting is enabled.
     *
     * @return the sampler, or {@code null} when live frequency reporting is unavailable
     */
    private IOReportCpuSampler cpuFrequencySampler() {
        if (!isArmCpu || !Builder.get(Builder._MAC_CPU_FREQUENCY_IOREPORT, false)) {
            return null;
        }
        IOReportCpuSampler sampler = IOReportClient.createForCpu();
        if (sampler == null) {
            Logger.warn(
                    false,
                    "Health",
                    "Unable to subscribe to the IOReport CPU performance states. Reporting nominal cluster frequencies instead.");
        }
        return sampler;
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
            return getPerformanceCoreFrequency();
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
                int returnedCpuCount = cpuTicks.length / SystemB.CPU_STATE_MAX;
                if (returnedCpuCount > ticks.length) {
                    Logger.warn(
                            false,
                            "Health",
                            "processor_cpu_load_info returned {} CPUs but expected {}; capping iteration",
                            returnedCpuCount,
                            ticks.length);
                }
                int cpuLimit = Math.min(returnedCpuCount, ticks.length);
                for (int cpu = 0; cpu < cpuLimit; cpu++) {
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
     * Queries nominal maximum frequency for each efficiency class.
     *
     * @return the nominal frequencies in hertz
     */
    protected long[] queryNominalFrequencies() {
        long[][] tables = nominalFrequencyTables.get();
        long[] byClass = new long[tables.length];
        for (int i = 0; i < tables.length; i++) {
            byClass[i] = tables[i].length == 0 ? DEFAULT_FREQUENCY : tables[i][tables[i].length - 1];
        }
        return byClass;
    }

    /**
     * Queries nominal frequency tables for each efficiency class.
     *
     * @return the nominal frequency tables in hertz
     */
    protected long[][] queryNominalFrequencyTables() {
        return mapClusterTables(queryClusterFrequencyTables(), efficiencyClassCount());
    }

    /**
     * Reads the maximum frequency of each CPU cluster.
     *
     * @return the distinct cluster frequencies in hertz
     */
    protected long[] queryClusterFrequencies() {
        long[][] tables = queryClusterFrequencyTables();
        long[] maxima = new long[tables.length];
        for (int i = 0; i < tables.length; i++) {
            maxima[i] = tables[i][tables[i].length - 1];
        }
        return maxima;
    }

    /**
     * Reads the complete voltage-state table of each CPU cluster.
     *
     * @return the distinct cluster frequency tables in hertz
     */
    protected long[][] queryClusterFrequencyTables() {
        List<long[]> tables = new ArrayList<>();
        IOIterator iter = IOKitUtil.getMatchingServices("AppleARMIODevice");
        if (iter != null) {
            try {
                IORegistryEntry device = iter.next();
                try {
                    while (device != null) {
                        if ("pmgr".equalsIgnoreCase(device.getName())) {
                            for (int table : parseClusterTables(device.getByteArrayProperty(ACC_CLUSTERS))) {
                                addTable(tables, device, "voltage-states" + table + "-sram");
                            }
                            if (tables.isEmpty()) {
                                addTable(tables, device, LEGACY_EFFICIENCY_TABLE);
                                addTable(tables, device, LEGACY_PERFORMANCE_TABLE);
                            }
                            break;
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
        tables.sort((left, right) -> Long.compare(left[left.length - 1], right[right.length - 1]));
        List<long[]> distinct = new ArrayList<>(tables.size());
        for (long[] table : tables) {
            long[] previous = distinct.isEmpty() ? null : distinct.get(distinct.size() - 1);
            if (previous == null || previous[previous.length - 1] != table[table.length - 1]) {
                distinct.add(table);
            }
        }
        return distinct.toArray(new long[0][]);
    }

    /**
     * Adds one voltage-state table to a list when it contains a positive maximum frequency.
     *
     * @param tables the target table list
     * @param entry  the power-manager registry entry
     * @param key    the property key
     */
    private void addTable(List<long[]> tables, IORegistryEntry entry, String key) {
        long[] table = parseFrequencyTable(entry.getByteArrayProperty(key));
        if (table.length > 0 && table[table.length - 1] > 0) {
            tables.add(table);
        }
    }

    /**
     * Parses a voltage-state table property.
     *
     * @param data the raw property value
     * @return the frequencies in hertz, in ascending order
     */
    static long[] parseFrequencyTable(byte[] data) {
        if (data == null || data.length < Normal._8) {
            return new long[0];
        }
        long[] frequencies = new long[data.length / Normal._8];
        for (int i = 0; i < frequencies.length; i++) {
            frequencies[i] = toHz(
                    Parsing.byteArrayToLong(
                            Arrays.copyOfRange(data, i * Normal._8, i * Normal._8 + Normal._4),
                            Normal._4,
                            false));
        }
        return frequencies;
    }

    /**
     * Parses the Apple power-manager {@code acc-clusters} property.
     *
     * @param data the raw property value
     * @return voltage-state table numbers in ascending tier order
     */
    static int[] parseClusterTables(byte[] data) {
        if (data == null || data.length < Normal._8) {
            return new int[0];
        }
        int clusters = data.length / Normal._8;
        int[] tables = new int[clusters];
        Integer[] order = new Integer[clusters];
        for (int i = 0; i < clusters; i++) {
            order[i] = i;
        }
        Arrays.sort(
                order,
                (left, right) -> Integer.compare(
                        data[left * Normal._8 + Normal._1] & 0xff,
                        data[right * Normal._8 + Normal._1] & 0xff));
        for (int i = 0; i < clusters; i++) {
            tables[i] = data[order[i] * Normal._8] & 0xff;
        }
        return tables;
    }

    /**
     * Maps cluster frequencies to efficiency classes.
     *
     * @param clusterFrequencies the cluster frequencies in ascending order
     * @param classCount         the number of efficiency classes
     * @return the frequency for each efficiency class
     */
    static long[] mapClusterFrequencies(long[] clusterFrequencies, int classCount) {
        int[] indices = CpuFrequencyResidency.alignAtTop(clusterFrequencies.length, classCount);
        if (indices.length == 0) {
            long[] byClass = new long[Math.max(classCount, 1)];
            Arrays.fill(byClass, DEFAULT_FREQUENCY);
            return byClass;
        }
        long[] byClass = new long[indices.length];
        for (int i = 0; i < indices.length; i++) {
            byClass[i] = clusterFrequencies[indices[i]];
        }
        return byClass;
    }

    /**
     * Maps cluster frequency tables to efficiency classes.
     *
     * @param clusterTables the cluster tables ordered by maximum frequency
     * @param classCount    the number of efficiency classes
     * @return the table for each efficiency class
     */
    static long[][] mapClusterTables(long[][] clusterTables, int classCount) {
        int[] indices = CpuFrequencyResidency.alignAtTop(clusterTables.length, classCount);
        if (indices.length == 0) {
            long[][] byClass = new long[Math.max(classCount, 1)][];
            Arrays.fill(byClass, new long[0]);
            return byClass;
        }
        long[][] byClass = new long[indices.length][];
        for (int i = 0; i < indices.length; i++) {
            byClass[i] = clusterTables[indices[i]];
        }
        return byClass;
    }

    /**
     * Counts the efficiency classes reported by physical processors.
     *
     * @return the efficiency class count
     */
    private int efficiencyClassCount() {
        int highest = 0;
        for (CentralProcessor.PhysicalProcessor processor : getPhysicalProcessors()) {
            highest = Math.max(highest, processor.getEfficiency());
        }
        return highest + 1;
    }

    /**
     * Gets the nominal frequency of the highest-performing cores.
     *
     * @return the nominal performance-core frequency in hertz
     */
    protected long getPerformanceCoreFrequency() {
        long[] byClass = nominalFrequencies.get();
        return byClass[byClass.length - 1];
    }

    /**
     * Extracts the maximum frequency from a voltage-state table property.
     *
     * @param data the byte array from IOKit
     * @return the frequency in hertz, or {@link #DEFAULT_FREQUENCY} if unavailable
     */
    protected long getMaxFreqFromByteArray(byte[] data) {
        long[] table = parseFrequencyTable(data);
        return table.length == 0 ? DEFAULT_FREQUENCY : table[table.length - 1];
    }

    /**
     * Converts a voltage-state table frequency to hertz.
     *
     * @param frequency the raw table value
     * @return the frequency in hertz
     */
    static long toHz(long frequency) {
        return frequency > 0 && frequency < MIN_PLAUSIBLE_HZ ? frequency * 1000L : frequency;
    }

}
