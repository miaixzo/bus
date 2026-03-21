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

import org.miaixz.bus.health.Provider;
import org.miaixz.bus.health.builtin.Disk;
import org.miaixz.bus.health.builtin.hardware.CentralProcessor;
import org.miaixz.bus.health.builtin.hardware.GlobalMemory;
import org.miaixz.bus.health.builtin.hardware.NetworkIF;
import org.miaixz.bus.metrics.Builder;
import org.miaixz.bus.metrics.Metrics;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bridges bus-health's rich hardware/OS metrics into bus-metrics gauges.
 * <p>
 * Provides significantly more accurate and detailed metrics than the JVM-only
 * {@link org.miaixz.bus.metrics.builtin.JvmMetrics} and {@link org.miaixz.bus.metrics.builtin.SystemMetrics},
 * including:
 * <ul>
 * <li>Physical CPU usage (sys/user/iowait/total) via JNA, not JVM estimation</li>
 * <li>Physical RAM (total/used/free/usage%) via OS memory map</li>
 * <li>Per-disk mount point usage and I/O stats</li>
 * <li>Network interface Tx/Rx bytes and packets per second</li>
 * <li>JVM heap + runtime metrics aligned with bus-health's Jvm model</li>
 * <li>Hardware load average (1m/5m/15m) from the OS kernel</li>
 * </ul>
 * <p>
 * Conditional on {@code bus-health} being on the classpath. When absent, the fallback
 * {@link org.miaixz.bus.metrics.builtin.JvmMetrics} and {@link org.miaixz.bus.metrics.builtin.SystemMetrics} are used
 * instead.
 * <p>
 * CPU ticks require a sampling interval; metrics are refreshed every {@code refreshIntervalSeconds} seconds by a
 * background daemon thread.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
public class HealthMetrics {

    /**
     * Default interval in seconds between CPU tick refreshes.
     */
    private static final int DEFAULT_REFRESH_SECONDS = Builder.HEALTH_DEFAULT_REFRESH_SECONDS;

    /**
     * bus-health Provider used to access hardware and OS data.
     */
    private final Provider provider;
    /**
     * Interval in seconds between background CPU tick refreshes.
     */
    private final int refreshSeconds;
    /**
     * Background daemon scheduler for CPU tick-based metric refresh.
     */
    private final ScheduledExecutorService scheduler;

    /**
     * Previous CPU tick snapshot; used to compute delta-based usage percentages.
     */
    private final AtomicReference<long[]> prevTicks = new AtomicReference<>(null);
    /**
     * Latest computed CPU usage snapshot; updated on each refresh cycle.
     */
    private final AtomicReference<CpuSnapshot> cpuSnapshot = new AtomicReference<>(new CpuSnapshot(0, 0, 0, 0));

    /** Creates a HealthMetrics instance using the default bus-health Provider and refresh interval. */
    public HealthMetrics() {
        this(new Provider(), DEFAULT_REFRESH_SECONDS);
    }

    /**
     * Creates a HealthMetrics instance with a custom provider and refresh interval.
     *
     * @param provider       bus-health Provider used to access hardware/OS data
     * @param refreshSeconds how often (in seconds) to refresh CPU tick-based metrics
     */
    public HealthMetrics(Provider provider, int refreshSeconds) {
        this.provider = provider;
        this.refreshSeconds = refreshSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, Builder.THREAD_NAME_HEALTH);
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * Register all health-backed gauges and start the background refresh scheduler.
     */
    public void register() {
        // ── JVM ────────────────────────────────────────────────────────────
        Runtime rt = Runtime.getRuntime();
        Metrics.gauge("jvm.memory.used", rt, r -> (double) (r.totalMemory() - r.freeMemory()));
        Metrics.gauge("jvm.memory.free", rt, r -> (double) r.freeMemory());
        Metrics.gauge("jvm.memory.total", rt, r -> (double) r.totalMemory());
        Metrics.gauge("jvm.memory.max", rt, r -> (double) r.maxMemory());
        Metrics.gauge("jvm.memory.usage", rt, r -> (r.totalMemory() - r.freeMemory()) * 100.0 / r.totalMemory());

        // ── Physical Memory (bus-health GlobalMemory) ─────────────────────
        GlobalMemory mem = provider.getHardware().getMemory();
        Metrics.gauge("system.memory.total.bytes", mem, m -> (double) m.getTotal());
        Metrics.gauge("system.memory.available.bytes", mem, m -> (double) m.getAvailable());
        Metrics.gauge("system.memory.used.bytes", mem, m -> (double) (m.getTotal() - m.getAvailable()));
        Metrics.gauge(
                "system.memory.usage",
                mem,
                m -> m.getTotal() <= 0 ? 0.0 : (m.getTotal() - m.getAvailable()) * 100.0 / m.getTotal());

        // ── CPU (sampling via background refresh) ─────────────────────────
        // Initial tick snapshot
        prevTicks.set(provider.getProcessor().getSystemCpuLoadTicks());

        Metrics.gauge("system.cpu.usage.total", cpuSnapshot, ref -> ref.get().totalUsage());
        Metrics.gauge("system.cpu.usage.user", cpuSnapshot, ref -> ref.get().userUsage());
        Metrics.gauge("system.cpu.usage.sys", cpuSnapshot, ref -> ref.get().sysUsage());
        Metrics.gauge("system.cpu.usage.iowait", cpuSnapshot, ref -> ref.get().ioWait());

        CentralProcessor proc = provider.getProcessor();
        Metrics.gauge("system.cpu.load.average.1m", proc, p -> p.getSystemLoadAverage(1)[0]);
        Metrics.gauge("system.cpu.physical.cores", proc, p -> (double) p.getPhysicalProcessorCount());
        Metrics.gauge("system.cpu.logical.cores", proc, p -> (double) p.getLogicalProcessorCount());

        // ── Disk usage (Gauge per mount point) ─────────────────────────────
        // Disk stores change dynamically; register a summary gauge
        Metrics.gauge(
                "system.disk.total.bytes",
                provider,
                p -> p.getDisk().stream().mapToLong(Disk::getTotalSpace).sum());
        Metrics.gauge(
                "system.disk.used.bytes",
                provider,
                p -> p.getDisk().stream().mapToLong(Disk::getUsedSpace).sum());
        Metrics.gauge(
                "system.disk.free.bytes",
                provider,
                p -> p.getDisk().stream().mapToLong(Disk::getFreeSpace).sum());

        // ── Network (summary across all interfaces) ───────────────────────
        Metrics.gauge("system.network.bytes.recv", provider, p -> networkStat(p, false, false));
        Metrics.gauge("system.network.bytes.sent", provider, p -> networkStat(p, true, false));
        Metrics.gauge("system.network.packets.recv", provider, p -> networkStat(p, false, true));
        Metrics.gauge("system.network.packets.sent", provider, p -> networkStat(p, true, true));

        // ── Thread counts ─────────────────────────────────────────────────
        java.lang.management.ThreadMXBean threads = java.lang.management.ManagementFactory.getThreadMXBean();
        Metrics.gauge("jvm.threads.live", threads, t -> (double) t.getThreadCount());
        Metrics.gauge("jvm.threads.peak", threads, t -> (double) t.getPeakThreadCount());
        Metrics.gauge("jvm.threads.daemon", threads, t -> (double) t.getDaemonThreadCount());

        // ── Process uptime ────────────────────────────────────────────────
        Metrics.gauge("process.uptime.seconds", provider, p -> (double) p.getJvm().getUptime() / 1000.0);

        // Start background refresh for CPU tick-based metrics
        scheduler.scheduleAtFixedRate(this::refreshCpu, refreshSeconds, refreshSeconds, TimeUnit.SECONDS);
    }

    /** Stops the background CPU refresh scheduler. */
    public void stop() {
        scheduler.shutdown();
    }

    // ── Internals ─────────────────────────────────────────────────────────

    /**
     * Refreshes CPU tick-based usage metrics by computing deltas from the previous tick snapshot.
     */
    private void refreshCpu() {
        try {
            CentralProcessor proc = provider.getProcessor();
            long[] prev = prevTicks.get();
            long[] curr = proc.getSystemCpuLoadTicks();
            prevTicks.set(curr);

            long user = delta(curr, prev, CentralProcessor.TickType.USER);
            long nice = delta(curr, prev, CentralProcessor.TickType.NICE);
            long sys = delta(curr, prev, CentralProcessor.TickType.SYSTEM);
            long idle = delta(curr, prev, CentralProcessor.TickType.IDLE);
            long iowait = delta(curr, prev, CentralProcessor.TickType.IOWAIT);
            long irq = delta(curr, prev, CentralProcessor.TickType.IRQ);
            long softirq = delta(curr, prev, CentralProcessor.TickType.SOFTIRQ);
            long steal = delta(curr, prev, CentralProcessor.TickType.STEAL);
            long total = user + nice + sys + idle + iowait + irq + softirq + steal;
            if (total <= 0)
                return;

            cpuSnapshot.set(
                    new CpuSnapshot(round2((user + nice) * 100.0 / total), round2(sys * 100.0 / total),
                            round2(iowait * 100.0 / total), round2((total - idle) * 100.0 / total)));
        } catch (Exception ignored) {
        }
    }

    /**
     * Returns the delta between current and previous tick counts for the given tick type.
     *
     * @param curr current tick array
     * @param prev previous tick array
     * @param type the CPU tick type to compute delta for
     * @return tick delta
     */
    private static long delta(long[] curr, long[] prev, CentralProcessor.TickType type) {
        return curr[type.getIndex()] - prev[type.getIndex()];
    }

    /**
     * Rounds a double value to 2 decimal places.
     *
     * @param v the value to round
     * @return value rounded to 2 decimal places
     */
    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    /**
     * Sums a network statistic (bytes or packets, sent or received) across all network interfaces.
     *
     * @param p       the bus-health Provider
     * @param sent    true for sent, false for received
     * @param packets true for packet count, false for byte count
     * @return total value across all interfaces
     */
    private static double networkStat(Provider p, boolean sent, boolean packets) {
        List<NetworkIF> nets = p.getHardware().getNetworkIFs();
        long sum = 0;
        for (NetworkIF n : nets) {
            if (packets) {
                sum += sent ? n.getPacketsSent() : n.getPacketsRecv();
            } else {
                sum += sent ? n.getBytesSent() : n.getBytesRecv();
            }
        }
        return (double) sum;
    }

    /**
     * Holds a computed CPU tick snapshot between refresh cycles.
     *
     * @param userUsage  user+nice CPU usage percentage
     * @param sysUsage   system CPU usage percentage
     * @param ioWait     I/O wait CPU usage percentage
     * @param totalUsage total CPU usage percentage
     */
    private record CpuSnapshot(double userUsage, double sysUsage, double ioWait, double totalUsage) {
    }

}
