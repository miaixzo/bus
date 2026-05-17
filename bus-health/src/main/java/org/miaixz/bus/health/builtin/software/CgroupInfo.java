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
package org.miaixz.bus.health.builtin.software;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * Represents cgroup information for containerized environments.
 * <p>
 * This interface provides access to resource limits and usage metrics for processes running in cgroups, supporting both
 * cgroup v1 and v2. Default implementations return sentinel values indicating that the process is not running in a
 * containerized environment.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public interface CgroupInfo {

    /**
     * Sentinel value for unlimited CPU quota or PIDs.
     */
    long UNLIMITED = -1L;

    /**
     * Standard default CPU period in microseconds.
     */
    long DEFAULT_CPU_PERIOD = 100_000L;

    /**
     * Sentinel value for unlimited memory.
     */
    long UNLIMITED_MEMORY = Long.MAX_VALUE;

    /**
     * Sentinel value for unlimited effective CPUs.
     */
    double UNLIMITED_CPUS = -1.0d;

    /**
     * Returns whether the current process is running in a containerized environment.
     *
     * @return {@code true} if running in a containerized environment, {@code false} otherwise
     */
    default boolean isContainerized() {
        return false;
    }

    /**
     * Returns the cgroup version being used.
     *
     * @return {@code 1} for cgroup v1, {@code 2} for cgroup v2, or {@code 0} if not in a cgroup
     */
    default int getVersion() {
        return 0;
    }

    /**
     * Returns the CPU quota for the cgroup in microseconds.
     *
     * @return the CPU quota in microseconds, or {@link #UNLIMITED} if unlimited
     */
    default long getCpuQuota() {
        return UNLIMITED;
    }

    /**
     * Returns the CPU period for the cgroup in microseconds.
     *
     * @return the CPU period in microseconds, or {@link #DEFAULT_CPU_PERIOD} when not explicitly set
     */
    default long getCpuPeriod() {
        return DEFAULT_CPU_PERIOD;
    }

    /**
     * Returns the total CPU usage for the cgroup in nanoseconds.
     *
     * @return the CPU usage in nanoseconds
     */
    default long getCpuUsage() {
        return 0L;
    }

    /**
     * Returns the effective number of CPUs available to the cgroup.
     *
     * @return the effective number of CPUs, or {@link #UNLIMITED_CPUS} if unlimited
     */
    default double getEffectiveCpus() {
        long quota = getCpuQuota();
        long period = getCpuPeriod();
        if (quota <= 0 || period <= 0) {
            return UNLIMITED_CPUS;
        }
        return (double) quota / period;
    }

    /**
     * Returns the memory limit for the cgroup in bytes.
     *
     * @return the memory limit in bytes, or {@link #UNLIMITED_MEMORY} if unlimited
     */
    default long getMemoryLimit() {
        return UNLIMITED_MEMORY;
    }

    /**
     * Returns the current memory usage for the cgroup in bytes.
     *
     * @return the memory usage in bytes
     */
    default long getMemoryUsage() {
        return 0L;
    }

    /**
     * Returns the maximum number of PIDs allowed in the cgroup.
     *
     * @return the PID limit, or {@link #UNLIMITED} if unlimited
     */
    default long getPidLimit() {
        return UNLIMITED;
    }

    /**
     * Returns the current number of PIDs in the cgroup.
     *
     * @return the current PID count
     */
    default long getPidCurrent() {
        return 0L;
    }

}
