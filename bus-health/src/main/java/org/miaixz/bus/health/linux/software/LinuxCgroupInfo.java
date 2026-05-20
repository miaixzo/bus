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
package org.miaixz.bus.health.linux.software;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.CgroupInfo;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.SysPath;

/**
 * Linux implementation of {@link CgroupInfo} supporting both cgroup v2 and v1.
 * <p>
 * This implementation detects the cgroup version and reads resource limits and usage from the appropriate cgroup
 * filesystem paths. Limit values are memoized while usage values are read fresh on each call.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class LinuxCgroupInfo implements CgroupInfo {

    /**
     * Nanoseconds per microsecond.
     */
    private static final long NANOSECONDS_PER_MICROSECOND = 1000L;

    /**
     * Guard band for cgroup v1 no-limit memory values.
     */
    private static final long V1_NO_LIMIT_THRESHOLD = UNLIMITED_MEMORY - 0x1_0000;

    /**
     * Known cgroup path markers for container runtimes.
     */
    private static final String[] CONTAINER_MARKERS = { "/docker/", "docker-", "/kubepods/", "kubepods.slice", "/lxc/",
            "/containerd/", "cri-containerd-", "/crio-", "/buildkit/", "/libpod-", "/podman-" };

    /**
     * Memoized cgroup version supplier.
     */
    private final Supplier<Integer> versionSupplier = Memoizer.memoize(this::detectVersion);

    /**
     * Memoized cgroup path supplier.
     */
    private final Supplier<String> cgroupPathSupplier = Memoizer.memoize(this::parseCgroupPath);

    /**
     * Memoized container detection supplier.
     */
    private final Supplier<Boolean> containerizedSupplier = Memoizer.memoize(this::detectContainerized);

    /**
     * Memoized contents of {@code /proc/self/cgroup}.
     */
    private final Supplier<List<String>> selfCgroupSupplier = Memoizer
            .memoize(() -> Builder.readFile(ProcPath.SELF_CGROUP, false));

    /**
     * Memoized CPU quota supplier.
     */
    private final Supplier<Long> cpuQuotaSupplier = Memoizer.memoize(this::readCpuQuota, Memoizer.defaultExpiration());

    /**
     * Memoized CPU period supplier.
     */
    private final Supplier<Long> cpuPeriodSupplier = Memoizer
            .memoize(this::readCpuPeriod, Memoizer.defaultExpiration());

    /**
     * Memoized memory limit supplier.
     */
    private final Supplier<Long> memoryLimitSupplier = Memoizer
            .memoize(this::readMemoryLimit, Memoizer.defaultExpiration());

    /**
     * Memoized PID limit supplier.
     */
    private final Supplier<Long> pidLimitSupplier = Memoizer.memoize(this::readPidLimit, Memoizer.defaultExpiration());

    /**
     * Cache of resolved cgroup v1 controller paths.
     */
    private final Map<String, String> v1ControllerPathCache = new ConcurrentHashMap<>();

    /**
     * Creates a new LinuxCgroupInfo instance.
     */
    public LinuxCgroupInfo() {
        // No initialization required.
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return {@code true} if the process appears containerized
     */
    @Override
    public boolean isContainerized() {
        return containerizedSupplier.get();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the detected cgroup version
     */
    @Override
    public int getVersion() {
        return versionSupplier.get();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the CPU quota in microseconds
     */
    @Override
    public long getCpuQuota() {
        return cpuQuotaSupplier.get();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the CPU period in microseconds
     */
    @Override
    public long getCpuPeriod() {
        return cpuPeriodSupplier.get();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the CPU usage in nanoseconds
     */
    @Override
    public long getCpuUsage() {
        int version = getVersion();
        if (version == 2) {
            return readCpuUsageV2();
        } else if (version == 1) {
            return readCpuUsageV1();
        }
        return 0L;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the memory limit in bytes
     */
    @Override
    public long getMemoryLimit() {
        return memoryLimitSupplier.get();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the memory usage in bytes
     */
    @Override
    public long getMemoryUsage() {
        int version = getVersion();
        if (version == 2) {
            return readMemoryUsageV2();
        } else if (version == 1) {
            return readMemoryUsageV1();
        }
        return 0L;
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the PID limit
     */
    @Override
    public long getPidLimit() {
        return pidLimitSupplier.get();
    }

    /**
     * Description inherited from parent class or interface.
     *
     * @return the current PID count
     */
    @Override
    public long getPidCurrent() {
        int version = getVersion();
        if (version == 2) {
            return readPidCurrentV2();
        } else if (version == 1) {
            return readPidCurrentV1();
        }
        return 0L;
    }

    /**
     * Detects whether the current process appears to be running inside a container.
     *
     * @return {@code true} if container markers are detected
     */
    private boolean detectContainerized() {
        if (new File("/.dockerenv").exists() || new File("/run/.containerenv").exists()) {
            return true;
        }
        String cgroupPath = cgroupPathSupplier.get();
        if (!cgroupPath.isEmpty()) {
            for (String marker : CONTAINER_MARKERS) {
                if (cgroupPath.contains(marker)) {
                    return true;
                }
            }
        }
        for (String line : selfCgroupSupplier.get()) {
            for (String marker : CONTAINER_MARKERS) {
                if (line.contains(marker)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Detects the available cgroup version.
     *
     * @return the detected cgroup version
     */
    private int detectVersion() {
        List<String> filesystems = Builder.readFile(ProcPath.FILESYSTEMS, false);
        boolean hasCgroup2 = filesystems.stream().anyMatch(line -> line.contains("cgroup2"));
        List<String> selfCgroup = selfCgroupSupplier.get();
        if (selfCgroup.isEmpty()) {
            return 0;
        }
        if (hasCgroup2 && selfCgroup.size() == 1 && selfCgroup.get(0).startsWith("0::")) {
            return 2;
        }
        return 1;
    }

    /**
     * Parses the cgroup v2 relative path from {@code /proc/self/cgroup}.
     *
     * @return the cgroup path, or an empty string when unavailable
     */
    private String parseCgroupPath() {
        List<String> selfCgroup = selfCgroupSupplier.get();
        if (selfCgroup.isEmpty()) {
            return "";
        }
        if (getVersion() == 2) {
            String line = selfCgroup.get(0);
            if (line.startsWith("0::")) {
                String path = line.substring(3);
                return path.isEmpty() ? "/" : path;
            }
        }
        return "";
    }

    /**
     * Returns the absolute base path for the cgroup v2 files.
     *
     * @return the cgroup v2 base path
     */
    private String getV2CgroupBase() {
        String cgroupPath = cgroupPathSupplier.get();
        if (cgroupPath.isEmpty() || cgroupPath.equals("/")) {
            return SysPath.CGROUP;
        }
        if (cgroupPath.startsWith("/")) {
            cgroupPath = cgroupPath.substring(1);
        }
        return SysPath.CGROUP + cgroupPath + "/";
    }

    /**
     * Returns the absolute base path for a cgroup v1 controller.
     *
     * @param controller the cgroup controller name
     * @return the cgroup v1 controller path
     */
    private String getV1ControllerPath(String controller) {
        return v1ControllerPathCache.computeIfAbsent(controller, this::resolveV1ControllerPath);
    }

    /**
     * Resolves a cgroup v1 controller path from {@code /proc/self/cgroup}.
     *
     * @param controller the cgroup controller name
     * @return the cgroup v1 controller path
     */
    private String resolveV1ControllerPath(String controller) {
        for (String line : selfCgroupSupplier.get()) {
            String[] parts = line.split(":", 3);
            if (parts.length >= 3) {
                String controllers = parts[1];
                if (controllers.isEmpty()) {
                    continue;
                }
                String path = parts[2];
                for (String c : controllers.split(",")) {
                    if (c.equals(controller)) {
                        if (path.startsWith("/")) {
                            path = path.substring(1);
                        }
                        return SysPath.CGROUP + controllers + "/" + path + "/";
                    }
                }
            }
        }
        return SysPath.CGROUP + controller + "/";
    }

    /**
     * Reads the CPU quota from the active cgroup version.
     *
     * @return the CPU quota in microseconds
     */
    private long readCpuQuota() {
        int version = getVersion();
        if (version == 2) {
            return readCpuQuotaV2();
        } else if (version == 1) {
            return readCpuQuotaV1();
        }
        return UNLIMITED;
    }

    /**
     * Reads the CPU quota from cgroup v2.
     *
     * @return the CPU quota in microseconds
     */
    private long readCpuQuotaV2() {
        return readCpuQuotaV2(getV2CgroupBase());
    }

    /**
     * Reads the CPU quota from a cgroup v2 base path.
     *
     * @param basePath the cgroup v2 base path
     * @return the CPU quota in microseconds
     */
    long readCpuQuotaV2(String basePath) {
        String cpuMax = Builder.getStringFromFile(basePath + "cpu.max");
        if (cpuMax.isEmpty()) {
            return UNLIMITED;
        }
        String[] parts = cpuMax.split("\\s+");
        if (parts.length >= 1) {
            if ("max".equalsIgnoreCase(parts[0])) {
                return UNLIMITED;
            }
            return Parsing.parseLongOrDefault(parts[0], UNLIMITED);
        }
        return UNLIMITED;
    }

    /**
     * Reads the CPU quota from cgroup v1.
     *
     * @return the CPU quota in microseconds
     */
    private long readCpuQuotaV1() {
        return readCpuQuotaV1(getV1ControllerPath("cpu"));
    }

    /**
     * Reads the CPU quota from a cgroup v1 controller path.
     *
     * @param controllerBase the controller base path
     * @return the CPU quota in microseconds
     */
    long readCpuQuotaV1(String controllerBase) {
        long quota = Builder.getLongFromFile(controllerBase + "cpu.cfs_quota_us");
        return quota == 0 ? UNLIMITED : quota;
    }

    /**
     * Reads the CPU period from the active cgroup version.
     *
     * @return the CPU period in microseconds
     */
    private long readCpuPeriod() {
        int version = getVersion();
        if (version == 2) {
            return readCpuPeriodV2();
        } else if (version == 1) {
            return readCpuPeriodV1();
        }
        return DEFAULT_CPU_PERIOD;
    }

    /**
     * Reads the CPU period from cgroup v2.
     *
     * @return the CPU period in microseconds
     */
    private long readCpuPeriodV2() {
        return readCpuPeriodV2(getV2CgroupBase());
    }

    /**
     * Reads the CPU period from a cgroup v2 base path.
     *
     * @param basePath the cgroup v2 base path
     * @return the CPU period in microseconds
     */
    long readCpuPeriodV2(String basePath) {
        String cpuMax = Builder.getStringFromFile(basePath + "cpu.max");
        if (cpuMax.isEmpty()) {
            return DEFAULT_CPU_PERIOD;
        }
        String[] parts = cpuMax.split("\\s+");
        if (parts.length >= 2) {
            return Parsing.parseLongOrDefault(parts[1], DEFAULT_CPU_PERIOD);
        }
        return DEFAULT_CPU_PERIOD;
    }

    /**
     * Reads the CPU period from cgroup v1.
     *
     * @return the CPU period in microseconds
     */
    private long readCpuPeriodV1() {
        return readCpuPeriodV1(getV1ControllerPath("cpu"));
    }

    /**
     * Reads the CPU period from a cgroup v1 controller path.
     *
     * @param controllerBase the controller base path
     * @return the CPU period in microseconds
     */
    long readCpuPeriodV1(String controllerBase) {
        long period = Builder.getLongFromFile(controllerBase + "cpu.cfs_period_us");
        return period == 0 ? DEFAULT_CPU_PERIOD : period;
    }

    /**
     * Reads the CPU usage from cgroup v2.
     *
     * @return the CPU usage in nanoseconds
     */
    long readCpuUsageV2() {
        return readCpuUsageV2(getV2CgroupBase());
    }

    /**
     * Reads the CPU usage from a cgroup v2 base path.
     *
     * @param basePath the cgroup v2 base path
     * @return the CPU usage in nanoseconds
     */
    long readCpuUsageV2(String basePath) {
        List<String> lines = Builder.readFile(basePath + "cpu.stat", false);
        for (String line : lines) {
            if (line.startsWith("usage_usec")) {
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    long usec = Parsing.parseLongOrDefault(parts[1], 0L);
                    return usec * NANOSECONDS_PER_MICROSECOND;
                }
            }
        }
        return 0L;
    }

    /**
     * Reads the CPU usage from cgroup v1.
     *
     * @return the CPU usage in nanoseconds
     */
    long readCpuUsageV1() {
        return readCpuUsageV1(getV1ControllerPath("cpuacct"));
    }

    /**
     * Reads the CPU usage from a cgroup v1 controller path.
     *
     * @param controllerBase the controller base path
     * @return the CPU usage in nanoseconds
     */
    long readCpuUsageV1(String controllerBase) {
        return Builder.getLongFromFile(controllerBase + "cpuacct.usage");
    }

    /**
     * Reads the memory limit from the active cgroup version.
     *
     * @return the memory limit in bytes
     */
    private long readMemoryLimit() {
        int version = getVersion();
        if (version == 2) {
            return readMemoryLimitV2();
        } else if (version == 1) {
            return readMemoryLimitV1();
        }
        return UNLIMITED_MEMORY;
    }

    /**
     * Reads the memory limit from cgroup v2.
     *
     * @return the memory limit in bytes
     */
    private long readMemoryLimitV2() {
        return readMemoryLimitV2(getV2CgroupBase());
    }

    /**
     * Reads the memory limit from a cgroup v2 base path.
     *
     * @param basePath the cgroup v2 base path
     * @return the memory limit in bytes
     */
    long readMemoryLimitV2(String basePath) {
        String memMax = Builder.getStringFromFile(basePath + "memory.max");
        if (memMax.isEmpty() || "max".equalsIgnoreCase(memMax.trim())) {
            return UNLIMITED_MEMORY;
        }
        return Parsing.parseLongOrDefault(memMax.trim(), UNLIMITED_MEMORY);
    }

    /**
     * Reads the memory limit from cgroup v1.
     *
     * @return the memory limit in bytes
     */
    private long readMemoryLimitV1() {
        return readMemoryLimitV1(getV1ControllerPath("memory"));
    }

    /**
     * Reads the memory limit from a cgroup v1 controller path.
     *
     * @param controllerBase the controller base path
     * @return the memory limit in bytes
     */
    long readMemoryLimitV1(String controllerBase) {
        long limit = Builder.getLongFromFile(controllerBase + "memory.limit_in_bytes");
        if (limit == 0 || limit > V1_NO_LIMIT_THRESHOLD) {
            return UNLIMITED_MEMORY;
        }
        return limit;
    }

    /**
     * Reads the memory usage from cgroup v2.
     *
     * @return the memory usage in bytes
     */
    long readMemoryUsageV2() {
        return readMemoryUsageV2(getV2CgroupBase());
    }

    /**
     * Reads the memory usage from a cgroup v2 base path.
     *
     * @param basePath the cgroup v2 base path
     * @return the memory usage in bytes
     */
    long readMemoryUsageV2(String basePath) {
        String memCurrent = Builder.getStringFromFile(basePath + "memory.current");
        return Parsing.parseLongOrDefault(memCurrent.trim(), 0L);
    }

    /**
     * Reads the memory usage from cgroup v1.
     *
     * @return the memory usage in bytes
     */
    long readMemoryUsageV1() {
        return readMemoryUsageV1(getV1ControllerPath("memory"));
    }

    /**
     * Reads the memory usage from a cgroup v1 controller path.
     *
     * @param controllerBase the controller base path
     * @return the memory usage in bytes
     */
    long readMemoryUsageV1(String controllerBase) {
        return Builder.getLongFromFile(controllerBase + "memory.usage_in_bytes");
    }

    /**
     * Reads the PID limit from the active cgroup version.
     *
     * @return the PID limit
     */
    private long readPidLimit() {
        int version = getVersion();
        if (version == 2) {
            return readPidLimitV2();
        } else if (version == 1) {
            return readPidLimitV1();
        }
        return UNLIMITED;
    }

    /**
     * Reads the PID limit from cgroup v2.
     *
     * @return the PID limit
     */
    private long readPidLimitV2() {
        return readPidLimitV2(getV2CgroupBase());
    }

    /**
     * Reads the PID limit from a cgroup v2 base path.
     *
     * @param basePath the cgroup v2 base path
     * @return the PID limit
     */
    long readPidLimitV2(String basePath) {
        String pidsMax = Builder.getStringFromFile(basePath + "pids.max");
        if (pidsMax.isEmpty() || "max".equalsIgnoreCase(pidsMax.trim())) {
            return UNLIMITED;
        }
        return Parsing.parseLongOrDefault(pidsMax.trim(), UNLIMITED);
    }

    /**
     * Reads the PID limit from cgroup v1.
     *
     * @return the PID limit
     */
    private long readPidLimitV1() {
        return readPidLimitV1(getV1ControllerPath("pids"));
    }

    /**
     * Reads the PID limit from a cgroup v1 controller path.
     *
     * @param controllerBase the controller base path
     * @return the PID limit
     */
    long readPidLimitV1(String controllerBase) {
        String pidsMax = Builder.getStringFromFile(controllerBase + "pids.max");
        if (pidsMax.isEmpty() || "max".equalsIgnoreCase(pidsMax.trim())) {
            return UNLIMITED;
        }
        long limit = Parsing.parseLongOrDefault(pidsMax.trim(), UNLIMITED);
        return limit == 0 ? UNLIMITED : limit;
    }

    /**
     * Reads the current PID count from cgroup v2.
     *
     * @return the current PID count
     */
    long readPidCurrentV2() {
        return readPidCurrentV2(getV2CgroupBase());
    }

    /**
     * Reads the current PID count from a cgroup v2 base path.
     *
     * @param basePath the cgroup v2 base path
     * @return the current PID count
     */
    long readPidCurrentV2(String basePath) {
        String pidsCurrent = Builder.getStringFromFile(basePath + "pids.current");
        return Parsing.parseLongOrDefault(pidsCurrent.trim(), 0L);
    }

    /**
     * Reads the current PID count from cgroup v1.
     *
     * @return the current PID count
     */
    long readPidCurrentV1() {
        return readPidCurrentV1(getV1ControllerPath("pids"));
    }

    /**
     * Reads the current PID count from a cgroup v1 controller path.
     *
     * @param controllerBase the controller base path
     * @return the current PID count
     */
    long readPidCurrentV1(String controllerBase) {
        return Builder.getLongFromFile(controllerBase + "pids.current");
    }

}
