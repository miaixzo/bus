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
package org.miaixz.bus.metrics.bridge;

import java.util.*;

import org.miaixz.bus.health.Collector;
import org.miaixz.bus.health.Platform;
import org.miaixz.bus.health.builtin.hardware.*;
import org.miaixz.bus.health.builtin.software.CgroupInfo;
import org.miaixz.bus.health.builtin.software.InternetProtocolStats.IPConnection;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.OSProcess;
import org.miaixz.bus.health.builtin.software.OperatingSystem;
import org.miaixz.bus.metrics.builtin.HostMetricsOptions;

/**
 * The only production adapter from bus-health objects to Bus-owned host snapshots.
 *
 * @author Kimi Liu
 */
final class HealthMetricSource implements HostMetricSource {

    /**
     * Supplies operating-system and hardware data through bus-health.
     */
    private final Collector collector;
    /**
     * Controls enabled dimensions and collection limits.
     */
    private final HostMetricsOptions options;
    /**
     * Declares the metric shapes supported by the current platform.
     */
    private final HostMetricCapabilities capabilities;

    /**
     * Creates a bus-health-backed source.
     *
     * @param collector bus-health collector
     * @param options   host metric options
     */
    HealthMetricSource(Collector collector, HostMetricsOptions options) {
        this.collector = java.util.Objects.requireNonNull(collector, "Health collector must not be null");
        this.options = java.util.Objects.requireNonNull(options, "Host options must not be null");
        this.capabilities = capabilities(Platform.getCurrentPlatform());
    }

    /**
     * Maps a bus-health platform to the metric shapes it can supply reliably.
     *
     * @param platform current platform
     * @return platform capability declaration
     */
    private static HostMetricCapabilities capabilities(Platform.OS platform) {
        boolean split = platform == Platform.OS.LINUX || platform == Platform.OS.MACOS
                || platform == Platform.OS.FREEBSD || platform == Platform.OS.OPENBSD
                || platform == Platform.OS.SOLARIS;
        HostMetricCapabilities.PagingFaultShape faults = split ? HostMetricCapabilities.PagingFaultShape.SPLIT
                : platform == Platform.OS.WINDOWS ? HostMetricCapabilities.PagingFaultShape.TOTAL
                        : HostMetricCapabilities.PagingFaultShape.NONE;
        boolean windows = platform == Platform.OS.WINDOWS;
        boolean unix = platform == Platform.OS.LINUX || platform == Platform.OS.MACOS || platform == Platform.OS.FREEBSD
                || platform == Platform.OS.OPENBSD || platform == Platform.OS.SOLARIS || platform == Platform.OS.AIX;
        return new HostMetricCapabilities(faults, split, unix, windows, true, platform == Platform.OS.LINUX);
    }

    /**
     * Maps a process state to the bounded telemetry vocabulary.
     *
     * @param state process state
     * @return normalized process state
     */
    private static String processState(OSProcess.State state) {
        return switch (state) {
            case ZOMBIE -> "defunct";
            case RUNNING -> "running";
            case SLEEPING, WAITING -> "sleeping";
            case STOPPED -> "stopped";
            default -> "other";
        };
    }

    /**
     * Normalizes a protocol type into a bounded transport dimension.
     *
     * @param type protocol type
     * @return normalized transport
     */
    private static String transport(String type) {
        String normalized = type == null ? "" : type.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("tcp"))
            return "tcp";
        if (normalized.startsWith("udp"))
            return "udp";
        if (normalized.startsWith("unix"))
            return "unix";
        if (normalized.startsWith("pipe"))
            return "pipe";
        if (normalized.startsWith("quic"))
            return "quic";
        return "other";
    }

    /**
     * Normalizes a connection state into the exported vocabulary.
     *
     * @param state connection state
     * @return normalized state
     */
    private static String connectionState(String state) {
        String normalized = state.toLowerCase(Locale.ROOT).replace("syn_recv", "syn_received");
        return switch (normalized) {
            case "closed", "close_wait", "closing", "established", "fin_wait_1", "fin_wait_2", "last_ack", "listen", "syn_received", "syn_sent", "time_wait" -> normalized;
            default -> "other";
        };
    }

    /**
     * Tests a comma-delimited filesystem option without accepting partial matches.
     *
     * @param optionsText normalized option text
     * @param option      option to find
     * @return whether the option is present
     */
    private static boolean containsOption(String optionsText, String option) {
        return ("," + optionsText + ",").contains("," + option + ",");
    }

    /**
     * Reads one CPU tick bucket while tolerating platform-specific short arrays.
     *
     * @param ticks CPU tick array
     * @param type  tick bucket
     * @return non-negative tick count
     */
    private static long tick(long[] ticks, CentralProcessor.TickType type) {
        int index = type.getIndex();
        return index < ticks.length ? nonNegative(ticks[index]) : 0;
    }

    /**
     * Converts a millisecond CPU counter to seconds and appends it to the snapshot.
     *
     * @param times  destination list
     * @param cpu    logical CPU number
     * @param mode   normalized CPU mode
     * @param millis cumulative CPU time in milliseconds
     */
    private static void addTime(List<CpuSnapshot.CpuTime> times, long cpu, String mode, long millis) {
        times.add(new CpuSnapshot.CpuTime(cpu, mode, millis / 1000.0));
    }

    /**
     * Converts a negative bus-health sentinel to an empty optional.
     *
     * @param value source value
     * @return optional non-negative value
     */
    private static OptionalLong optional(long value) {
        return value < 0 ? OptionalLong.empty() : OptionalLong.of(value);
    }

    /**
     * Clamps an unavailable or negative cumulative value to zero.
     *
     * @param value source value
     * @return non-negative value
     */
    private static long nonNegative(long value) {
        return Math.max(0, value);
    }

    /**
     * Adds two non-negative values without overflowing.
     *
     * @param left  left operand
     * @param right right operand
     * @return the sum, or {@link Long#MAX_VALUE} when it would overflow
     */
    private static long saturatedAdd(long left, long right) {
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    @Override
    public HostMetricCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public GeneralSnapshot general() {
        OperatingSystem os = collector.getOperatingSystem();
        Map<String, Long> counts = new HashMap<>();
        if (options.process().stateCounts()) {
            for (OSProcess process : os.getProcesses(null, null, 0)) {
                counts.merge(processState(process.getState()), 1L, Long::sum);
            }
        }
        return new GeneralSnapshot(nonNegative(os.getSystemUptime()), counts);
    }

    @Override
    public CpuSnapshot cpu() {
        CentralProcessor processor = collector.getProcessor();
        long[][] ticks = processor.getProcessorCpuLoadTicks();
        int count = Math
                .min(Math.min(ticks.length, processor.getLogicalProcessorCount()), options.cpu().maxLogicalCpus());
        List<CpuSnapshot.CpuTime> times = new ArrayList<>(count * 7);
        for (int cpu = 0; cpu < count; cpu++) {
            long[] values = ticks[cpu];
            addTime(times, cpu, "user", tick(values, CentralProcessor.TickType.USER));
            addTime(times, cpu, "nice", tick(values, CentralProcessor.TickType.NICE));
            addTime(times, cpu, "system", tick(values, CentralProcessor.TickType.SYSTEM));
            addTime(times, cpu, "idle", tick(values, CentralProcessor.TickType.IDLE));
            addTime(times, cpu, "iowait", tick(values, CentralProcessor.TickType.IOWAIT));
            addTime(
                    times,
                    cpu,
                    "interrupt",
                    saturatedAdd(
                            tick(values, CentralProcessor.TickType.IRQ),
                            tick(values, CentralProcessor.TickType.SOFTIRQ)));
            addTime(times, cpu, "steal", tick(values, CentralProcessor.TickType.STEAL));
        }
        long[] currentFreq = processor.getCurrentFreq();
        List<CpuSnapshot.CpuFrequency> frequencies = new ArrayList<>();
        for (int cpu = 0; cpu < Math.min(count, currentFreq.length); cpu++) {
            if (currentFreq[cpu] >= 0) {
                frequencies.add(new CpuSnapshot.CpuFrequency(cpu, currentFreq[cpu]));
            }
        }
        return new CpuSnapshot(nonNegative(processor.getPhysicalProcessorCount()), count, times, frequencies);
    }

    @Override
    public MemorySnapshot memory() {
        GlobalMemory memory = collector.getHardware().getMemory();
        long total = nonNegative(memory.getTotal());
        long available = Math.min(total, nonNegative(memory.getAvailable()));
        return new MemorySnapshot(total, total - available, available);
    }

    @Override
    public PagingSnapshot paging() {
        VirtualMemory paging = collector.getHardware().getMemory().getVirtualMemory();
        long total = nonNegative(paging.getSwapTotal());
        long used = Math.min(total, nonNegative(paging.getSwapUsed()));
        return new PagingSnapshot(total, used, total - used, optional(paging.getSwapPagesIn()),
                optional(paging.getSwapPagesOut()));
    }

    @Override
    public List<DiskSnapshot> disks() {
        List<DiskSnapshot> snapshots = new ArrayList<>();
        List<HWDiskStore> disks = collector.getHardware().getDiskStores().stream()
                .sorted(Comparator.comparing(HWDiskStore::getName)).toList();
        for (HWDiskStore disk : disks) {
            if (snapshots.size() >= options.disk().maxDevices()) {
                break;
            }
            if (!options.disk().includeVirtual() && disk.getDiskType().toLowerCase(Locale.ROOT).contains("virtual")) {
                continue;
            }
            if (disk.updateAttributes() && disk.getName() != null && !disk.getName().isBlank()) {
                snapshots.add(
                        new DiskSnapshot(disk.getName(), optional(disk.getSize()), optional(disk.getReads()),
                                optional(disk.getReadBytes()), optional(disk.getWrites()),
                                optional(disk.getWriteBytes()), optional(disk.getTransferTime())));
            }
        }
        return List.copyOf(snapshots);
    }

    @Override
    public List<FileSystemSnapshot> fileSystems() {
        List<FileSystemSnapshot> snapshots = new ArrayList<>();
        List<OSFileStore> stores = collector.getOperatingSystem().getFileSystem()
                .getFileStores(options.fileSystem().localOnly()).stream()
                .sorted(Comparator.comparing(OSFileStore::getMount).thenComparing(OSFileStore::getVolume)).toList();
        for (OSFileStore store : stores) {
            if (snapshots.size() >= options.fileSystem().maxFileSystems()) {
                break;
            }
            if (!store.updateAttributes()) {
                continue;
            }
            String optionsText = store.getOptions() == null ? "" : store.getOptions().toLowerCase(Locale.ROOT);
            String mode = containsOption(optionsText, "ro") ? "ro"
                    : containsOption(optionsText, "rw") ? "rw" : "unknown";
            snapshots.add(
                    new FileSystemSnapshot(store.getVolume(), store.getMount(), store.getType(), mode,
                            nonNegative(store.getTotalSpace()), nonNegative(store.getFreeSpace()),
                            nonNegative(store.getUsableSpace())));
        }
        return List.copyOf(snapshots);
    }

    @Override
    public NetworkSnapshot network() {
        List<NetworkSnapshot.InterfaceSnapshot> interfaces = new ArrayList<>();
        List<NetworkIF> networkInterfaces = collector.getHardware().getNetworkIFs(options.network().includeLoopback())
                .stream().sorted(Comparator.comparing(NetworkIF::getName)).toList();
        for (NetworkIF network : networkInterfaces) {
            if (interfaces.size() >= options.network().maxInterfaces()) {
                break;
            }
            if (!options.network().includeVirtual() && network.isKnownVmMacAddr()) {
                continue;
            }
            if (network.updateAttributes() && network.getName() != null && !network.getName().isBlank()) {
                interfaces.add(
                        new NetworkSnapshot.InterfaceSnapshot(network.getName(), nonNegative(network.getBytesRecv()),
                                nonNegative(network.getBytesSent()), nonNegative(network.getPacketsRecv()),
                                nonNegative(network.getPacketsSent()), nonNegative(network.getInDrops()),
                                nonNegative(network.getCollisions()), nonNegative(network.getInErrors()),
                                nonNegative(network.getOutErrors())));
            }
        }
        List<NetworkSnapshot.ConnectionSnapshot> connections = options.network().connections()
                && capabilities.networkConnections() ? connections() : List.of();
        return new NetworkSnapshot(interfaces, connections);
    }

    @Override
    public Optional<ProcessSnapshot> currentProcess() {
        OSProcess process = collector.getOperatingSystem().getCurrentProcess();
        if (!process.updateAttributes()) {
            return Optional.empty();
        }
        OptionalLong openFiles = capabilities.unixFileDescriptors() ? optional(process.getOpenFiles())
                : OptionalLong.empty();
        OptionalLong handles = capabilities.windowsHandles() ? optional(process.getOpenFiles()) : OptionalLong.empty();
        OptionalLong minor = capabilities.pagingFaults() == HostMetricCapabilities.PagingFaultShape.NONE
                ? OptionalLong.empty()
                : optional(process.getMinorFaults());
        OptionalLong major = capabilities.pagingFaults() == HostMetricCapabilities.PagingFaultShape.SPLIT
                ? optional(process.getMajorFaults())
                : OptionalLong.empty();
        OptionalLong voluntary = capabilities.contextSwitchTypes() ? optional(process.getVoluntaryContextSwitches())
                : OptionalLong.empty();
        OptionalLong involuntary = capabilities.contextSwitchTypes() ? optional(process.getInvoluntaryContextSwitches())
                : OptionalLong.empty();
        return Optional.of(
                new ProcessSnapshot(nonNegative(process.getUserTime()), nonNegative(process.getKernelTime()),
                        nonNegative(process.getUpTime()), nonNegative(process.getResidentMemory()),
                        nonNegative(process.getVirtualSize()), nonNegative(process.getBytesRead()),
                        nonNegative(process.getBytesWritten()), nonNegative(process.getThreadCount()), openFiles,
                        handles, minor, major, voluntary, involuntary));
    }

    @Override
    public Optional<ContainerSnapshot> container() {
        CgroupInfo cgroup = collector.getOperatingSystem().getCgroupInfo();
        if (!cgroup.isContainerized() || cgroup.getCpuUsage() < 0 || cgroup.getMemoryUsage() < 0
                || cgroup.getMemoryUsage() == CgroupInfo.UNLIMITED_MEMORY) {
            return Optional.empty();
        }
        return Optional.of(new ContainerSnapshot(cgroup.getCpuUsage() / 1_000_000_000.0, cgroup.getMemoryUsage()));
    }

    /**
     * Aggregates protocol connections by transport and state.
     *
     * @return immutable connection aggregates
     */
    private List<NetworkSnapshot.ConnectionSnapshot> connections() {
        Map<String, Long> counts = new HashMap<>();
        for (IPConnection connection : collector.getOperatingSystem().getInternetProtocolStats().getConnections()) {
            String transport = transport(connection.getType());
            String state = connectionState(connection.getState().name());
            counts.merge(transport + '\0' + state, 1L, Long::sum);
        }
        return counts.entrySet().stream().map(entry -> {
            int separator = entry.getKey().indexOf('\0');
            return new NetworkSnapshot.ConnectionSnapshot(entry.getKey().substring(0, separator),
                    entry.getKey().substring(separator + 1), entry.getValue());
        }).toList();
    }

}
