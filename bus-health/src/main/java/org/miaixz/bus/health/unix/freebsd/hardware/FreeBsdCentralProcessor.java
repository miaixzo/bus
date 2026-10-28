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
package org.miaixz.bus.health.unix.freebsd.hardware;

import java.util.*;
import java.util.regex.Matcher;

import com.sun.jna.Memory;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.common.AbstractCentralProcessor;
import org.miaixz.bus.health.unix.freebsd.BsdSysctlKit;
import org.miaixz.bus.health.unix.shared.jna.FreeBsdLibc;

/**
 * A CPU
 *
 * @author Kimi Liu
 */
@ThreadSafe
final class FreeBsdCentralProcessor extends AbstractCentralProcessor {

    // Capture the CSV of hex values as group(1), clients should split on ','
    /**
     * The CPUMASK constant.
     */
    private static final java.util.regex.Pattern CPUMASK = java.util.regex.Pattern
            .compile(".*<cpu\\s.*mask=\"(\\p{XDigit}+(,\\p{XDigit}+)*)\".*>.*</cpu>.*");

    /**
     * The CP_USER constant.
     */
    private static final int CP_USER = 0;

    /**
     * The CP_NICE constant.
     */
    private static final int CP_NICE = 1;

    /**
     * The CP_SYS constant.
     */
    private static final int CP_SYS = 2;

    /**
     * The CP_INTR constant.
     */
    private static final int CP_INTR = 3;

    /**
     * The CP_IDLE constant.
     */
    private static final int CP_IDLE = 4;

    /**
     * The CPUSTATES constant.
     */
    private static final int CPUSTATES = 5;

    /**
     * Fills the processor tick array from a FreeBSD CPU time array.
     *
     * @param ticks       the target tick array
     * @param cpTimeArray the source CPU time array
     * @param offset      the starting offset in the source array
     */
    private static void fillTicks(long[] ticks, long[] cpTimeArray, int offset) {
        ticks[CentralProcessor.TickType.USER.getIndex()] = cpTimeArray[offset + CP_USER];
        ticks[CentralProcessor.TickType.NICE.getIndex()] = cpTimeArray[offset + CP_NICE];
        ticks[CentralProcessor.TickType.SYSTEM.getIndex()] = cpTimeArray[offset + CP_SYS];
        ticks[CentralProcessor.TickType.IRQ.getIndex()] = cpTimeArray[offset + CP_INTR];
        ticks[CentralProcessor.TickType.IDLE.getIndex()] = cpTimeArray[offset + CP_IDLE];
    }

    /**
     * Queries a FreeBSD CPU time sysctl as an unsigned 64-bit array.
     *
     * @param name the sysctl name
     * @return the CPU time values, or {@code null} if the query failed
     */
    private static long[] queryCpTimes(String name) {
        try (Memory p = BsdSysctlKit.sysctl(name)) {
            if (p == null) {
                return null;
            }
            int count = (int) (p.size() / Long.BYTES);
            long[] result = new long[count];
            for (int i = 0; i < count; i++) {
                result[i] = p.getLong((long) i * Long.BYTES);
            }
            return result;
        }
    }

    /**
     * Parses the topology.
     *
     * @return the parse topology result
     */
    private static List<CentralProcessor.LogicalProcessor> parseTopology() {
        String specification = BsdSysctlKit.sysctl("kern.sched.topology_spec", Normal.EMPTY);
        List<TopologyGroup> groups = new ArrayList<>();
        Deque<TopologyGroup> openGroups = new ArrayDeque<>();
        for (String line : specification.split("[\\n\\r]")) {
            if (line.contains("<group level=")) {
                TopologyGroup group = new TopologyGroup(openGroups.size() + Normal._1);
                groups.add(group);
                openGroups.push(group);
            } else if (line.contains("</group>")) {
                openGroups.poll();
            } else if (!openGroups.isEmpty()) {
                TopologyGroup group = openGroups.element();
                if (line.contains("<cpu")) {
                    group.cpus.addAll(parseCpuList(line));
                } else if (line.contains("<flag name=\"THREAD\"") || line.contains("<flag name=\"SMT\"")
                        || line.contains("<flag name=\"HTT\"")) {
                    group.thread = true;
                }
            }
        }
        if (groups.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Integer, Integer> coreOf = new HashMap<>();
        Map<Integer, Integer> packageOf = new HashMap<>();
        int packageNumber = Normal._0;
        for (TopologyGroup group : groups) {
            if (group.thread && !group.cpus.isEmpty()) {
                int first = Collections.min(group.cpus);
                for (int cpu : group.cpus) {
                    coreOf.put(cpu, first);
                }
            } else if (group.level == Normal._2) {
                for (int cpu : group.cpus) {
                    packageOf.put(cpu, packageNumber);
                }
                packageNumber++;
            }
        }
        List<CentralProcessor.LogicalProcessor> processors = new ArrayList<>();
        Map<Integer, Integer> coreNumbers = new HashMap<>();
        for (int cpu : new TreeSet<>(groups.get(Normal._0).cpus)) {
            int core = coreNumbers.computeIfAbsent(coreOf.getOrDefault(cpu, cpu), unused -> coreNumbers.size());
            processors.add(
                    new CentralProcessor.LogicalProcessor(cpu, core, packageOf.getOrDefault(cpu, Normal._0),
                            Normal._0));
        }
        return processors;
    }

    /** Parses CPU numbers from a topology specification line. */
    private static List<Integer> parseCpuList(String line) {
        List<Integer> cpus = new ArrayList<>();
        int open = line.indexOf(Symbol.C_GT);
        int close = line.indexOf("</cpu>");
        if (open >= Normal._0 && close > open) {
            for (String value : line.substring(open + Normal._1, close).split(Symbol.COMMA)) {
                int cpu = Parsing.parseIntOrDefault(value.trim(), Normal.__1);
                if (cpu >= Normal._0) {
                    cpus.add(cpu);
                }
            }
        }
        if (cpus.isEmpty()) {
            Matcher matcher = CPUMASK.matcher(line);
            if (matcher.find()) {
                String[] words = matcher.group(Normal._1).split(Symbol.COMMA);
                for (int word = Normal._0; word < words.length; word++) {
                    long bits = Parsing.hexStringToLong(words[word], 0L);
                    for (int bit = Normal._0; bit < Long.SIZE; bit++) {
                        if ((bits & 1L << bit) != 0L) {
                            cpus.add(word * Long.SIZE + bit);
                        }
                    }
                }
            }
        }
        return cpus;
    }

    /** A CPU group from the scheduler topology specification. */
    private static final class TopologyGroup {

        /** Nesting level. */
        private final int level;
        /** CPUs in the group. */
        private final List<Integer> cpus = new ArrayList<>();
        /** Whether the group represents hardware threads of one core. */
        private boolean thread;

        /** Creates a topology group. */
        private TopologyGroup(int level) {
            this.level = level;
        }
    }

    /**
     * Fetches the ProcessorID from dmidecode (if possible with root permissions), otherwise uses the values from
     * /var/run/dmesg.boot
     *
     * @param processorID The processorID as a long
     * @return The ProcessorID string
     */
    private static String getProcessorIDfromDmiDecode(long processorID) {
        boolean procInfo = false;
        String marker = "Processor Information";
        for (String checkLine : Executor.runNative("dmidecode -t processor")) {
            if (!procInfo && checkLine.contains(marker)) {
                marker = "ID:";
                procInfo = true;
            } else if (procInfo && checkLine.contains(marker)) {
                return Parsing.getTextAfterString(checkLine, marker).trim();
            }
        }
        // If we've gotten this far, dmidecode failed. Used the passed-in values
        return String.format(Locale.ROOT, "%016X", processorID);
    }

    /**
     * Queries the processor id.
     *
     * @return the query processor id result
     */
    @Override
    protected CentralProcessor.ProcessorIdentifier queryProcessorId() {
        final java.util.regex.Pattern originPattern = java.util.regex.Pattern.compile("Origin\\s*=.*");
        final java.util.regex.Pattern fieldPattern = java.util.regex.Pattern
                .compile("(?<!\\w)(\\w++)\\s*+=\\s*+(\"[^\"]*+\"|\\S++)");
        final java.util.regex.Pattern featuresPattern = java.util.regex.Pattern.compile("Features=(\\S+)<.*");

        String cpuVendor = Normal.EMPTY;
        String cpuName = BsdSysctlKit.sysctl("hw.model", Normal.EMPTY);
        String cpuFamily = Normal.EMPTY;
        String cpuModel = Normal.EMPTY;
        String cpuStepping = Normal.EMPTY;
        String processorID;
        long cpuFreq = BsdSysctlKit.sysctl("hw.clockrate", 0L) * 1_000_000L;

        boolean cpu64bit;

        // Parsing dmesg.boot is apparently the only reliable source for processor
        // identification in FreeBSD
        long processorIdBits = 0L;
        List<String> cpuInfo = Builder.readFile("/var/run/dmesg.boot", false);
        for (String line : cpuInfo) {
            line = line.trim();
            // Prefer hw.model to this one
            if (line.startsWith("CPU:") && cpuName.isEmpty()) {
                cpuName = line.replace("CPU:", Normal.EMPTY).trim();
            } else if (originPattern.matcher(line).matches()) {
                Map<String, String> fields = new HashMap<>();
                Matcher matcher = fieldPattern.matcher(line);
                while (matcher.find()) {
                    fields.put(matcher.group(Normal._1), matcher.group(Normal._2));
                }
                cpuVendor = Parsing
                        .getStringBetween(fields.getOrDefault("Origin", Normal.EMPTY), Symbol.C_DOUBLE_QUOTES);
                int id = Parsing.decodeIntOrDefault(fields.getOrDefault("Id", Normal.EMPTY), Normal._0);
                processorIdBits |= Parsing.unsignedIntToLong(id);
                Triplet<Integer, Integer, Integer> decoded = cpuidToFamilyModelStepping(id);
                cpuFamily = Integer.toString(Parsing.decodeIntOrDefault(fields.get("Family"), decoded.getLeft()));
                cpuModel = Integer.toString(Parsing.decodeIntOrDefault(fields.get("Model"), decoded.getMiddle()));
                cpuStepping = Integer.toString(Parsing.decodeIntOrDefault(fields.get("Stepping"), decoded.getRight()));
            } else if (line.startsWith("Features=")) {
                Matcher m = featuresPattern.matcher(line);
                if (m.matches()) {
                    processorIdBits |= Parsing.decodeLongOrDefault(m.group(1), 0L) << 32;
                }
                // No further interest in this file
                break;
            }
        }
        cpu64bit = Executor.getFirstAnswer("uname -m").trim().contains("64");
        processorID = getProcessorIDfromDmiDecode(processorIdBits);

        return new CentralProcessor.ProcessorIdentifier(cpuVendor, cpuName, cpuFamily, cpuModel, cpuStepping,
                processorID, cpu64bit, cpuFreq);
    }

    /**
     * Returns the init processor counts result.
     *
     * @return the init processor counts result
     */
    @Override
    protected Tuple initProcessorCounts() {
        List<CentralProcessor.LogicalProcessor> logProcs = parseTopology();
        // Force at least one processor
        if (logProcs.isEmpty()) {
            logProcs.add(new CentralProcessor.LogicalProcessor(0, 0, 0));
        }
        Map<Integer, String> dmesg = new HashMap<>();
        // cpu0: <Open Firmware CPU> on cpulist0
        java.util.regex.Pattern normal = java.util.regex.Pattern.compile("cpu(\\d+): (.+) on .*");
        // CPU 0: ARM Cortex-A53 r0p4 affinity: 0 0
        java.util.regex.Pattern hybrid = java.util.regex.Pattern.compile("CPU\\s*(\\d+): (.+) affinity:.*");
        List<String> featureFlags = new ArrayList<>();
        boolean readingFlags = false;
        for (String s : Builder.readFile("/var/run/dmesg.boot", false)) {
            Matcher h = hybrid.matcher(s);
            if (h.matches()) {
                int coreId = Parsing.parseIntOrDefault(h.group(1), 0);
                // This always takes priority, overwrite if needed
                dmesg.put(coreId, h.group(2).trim());
            } else {
                Matcher n = normal.matcher(s);
                if (n.matches()) {
                    int coreId = Parsing.parseIntOrDefault(n.group(1), 0);
                    // Don't overwrite if h matched earlier
                    dmesg.putIfAbsent(coreId, n.group(2).trim());
                }
            }
            if (s.contains("Origin=")) {
                readingFlags = true;
            } else if (readingFlags) {
                if (s.startsWith("  ")) {
                    featureFlags.add(s.trim());
                } else {
                    readingFlags = false;
                }
            }
        }
        List<CentralProcessor.PhysicalProcessor> physProcs = dmesg.isEmpty() ? null
                : createProcListFromDmesg(logProcs, dmesg);
        List<CentralProcessor.ProcessorCache> caches = getCacheInfoFromLscpu();
        return new Tuple(logProcs, physProcs, caches, featureFlags);
    }

    /**
     * Returns the cache info from lscpu.
     *
     * @return the get cache info from lscpu result
     */
    private List<CentralProcessor.ProcessorCache> getCacheInfoFromLscpu() {
        Set<CentralProcessor.ProcessorCache> caches = new HashSet<>();
        for (String checkLine : Executor.runNative("lscpu")) {
            if (checkLine.contains("L1d cache:")) {
                caches.add(
                        new CentralProcessor.ProcessorCache(1, 0, 0,
                                Parsing.parseDecimalMemorySizeToBinary(checkLine.split(Symbol.COLON)[1].trim()),
                                CentralProcessor.ProcessorCache.Type.DATA));
            } else if (checkLine.contains("L1i cache:")) {
                caches.add(
                        new CentralProcessor.ProcessorCache(1, 0, 0,
                                Parsing.parseDecimalMemorySizeToBinary(checkLine.split(Symbol.COLON)[1].trim()),
                                CentralProcessor.ProcessorCache.Type.INSTRUCTION));
            } else if (checkLine.contains("L2 cache:")) {
                caches.add(
                        new CentralProcessor.ProcessorCache(2, 0, 0,
                                Parsing.parseDecimalMemorySizeToBinary(checkLine.split(Symbol.COLON)[1].trim()),
                                CentralProcessor.ProcessorCache.Type.UNIFIED));
            } else if (checkLine.contains("L3 cache:")) {
                caches.add(
                        new CentralProcessor.ProcessorCache(3, 0, 0,
                                Parsing.parseDecimalMemorySizeToBinary(checkLine.split(Symbol.COLON)[1].trim()),
                                CentralProcessor.ProcessorCache.Type.UNIFIED));
            }
        }
        return orderedProcCaches(caches);
    }

    /**
     * Queries the system cpu load ticks.
     *
     * @return the query system cpu load ticks result
     */
    @Override
    public long[] querySystemCpuLoadTicks() {
        long[] ticks = new long[CentralProcessor.TickType.values().length];
        long[] cpTime = queryCpTimes("kern.cp_time");
        if (cpTime == null || cpTime.length < CPUSTATES) {
            return ticks;
        }
        fillTicks(ticks, cpTime, 0);
        return ticks;
    }

    /**
     * Queries the current freq.
     *
     * @return the query current freq result
     */
    @Override
    public long[] queryCurrentFreq() {
        long[] freq = new long[1];
        freq[0] = BsdSysctlKit.sysctl("dev.cpu.0.freq", -1L);
        if (freq[0] > 0) {
            // If success, value is in MHz
            freq[0] *= 1_000_000L;
        } else {
            freq[0] = BsdSysctlKit.sysctl("machdep.tsc_freq", -1L);
        }
        return freq;
    }

    /**
     * Returns the system load average.
     *
     * @param nelem the nelem
     * @return the get system load average result
     */
    @Override
    public double[] getSystemLoadAverage(int nelem) {
        if (nelem < 1 || nelem > 3) {
            throw new IllegalArgumentException("Must include from one to three elements.");
        }
        double[] average = new double[nelem];
        int retval = FreeBsdLibc.INSTANCE.getloadavg(average, nelem);
        if (retval < nelem) {
            Arrays.fill(average, -1d);
        }
        return average;
    }

    /**
     * Queries the max freq.
     *
     * @return the query max freq result
     */
    @Override
    public long queryMaxFreq() {
        long max = -1L;
        String freqLevels = BsdSysctlKit.sysctl("dev.cpu.0.freq_levels", Normal.EMPTY);
        // MHz/Watts pairs like: 2501/32000 2187/27125 2000/24000
        for (String s : Pattern.SPACES_PATTERN.split(freqLevels)) {
            long freq = Parsing.parseLongOrDefault(s.split(Symbol.SLASH)[0], -1L);
            if (max < freq) {
                max = freq;
            }
        }
        if (max > 0) {
            // If success, value is in MHz
            max *= 1_000_000;
        } else {
            max = BsdSysctlKit.sysctl("machdep.tsc_freq", -1L);
        }
        return max;
    }

    /**
     * Queries the processor cpu load ticks.
     *
     * @return the query processor cpu load ticks result
     */
    @Override
    public long[][] queryProcessorCpuLoadTicks() {
        long[][] ticks = new long[getLogicalProcessorCount()][CentralProcessor.TickType.values().length];
        long[] cpTimes = queryCpTimes("kern.cp_times");
        if (cpTimes == null) {
            return ticks;
        }
        int cpus = Math.min(getLogicalProcessorCount(), cpTimes.length / CPUSTATES);
        for (int cpu = 0; cpu < cpus; cpu++) {
            fillTicks(ticks[cpu], cpTimes, cpu * CPUSTATES);
        }
        return ticks;
    }

    /**
     * Queries the context switches.
     *
     * @return the query context switches result
     */
    @Override
    public long queryContextSwitches() {
        return BsdSysctlKit.sysctl("vm.stats.sys.v_swtch", 0L);
    }

    /**
     * Queries the interrupts.
     *
     * @return the query interrupts result
     */
    @Override
    public long queryInterrupts() {
        return BsdSysctlKit.sysctl("vm.stats.sys.v_intr", 0L);
    }

}
