/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.unix.platform.aix.driver.perfstat;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

import com.sun.jna.platform.unix.aix.Perfstat;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_cpu_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_cpu_total_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_id_t;

/**
 * Utility to query performance stats for cpu
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class PerfstatCpu {

    private static final Perfstat PERF = Perfstat.INSTANCE;

    private PerfstatCpu() {
    }

    /**
     * Queries perfstat_cpu_total for total CPU usage statistics
     *
     * @return usage statistics
     */
    public static perfstat_cpu_total_t queryCpuTotal() {
        perfstat_cpu_total_t cpu = new perfstat_cpu_total_t();
        int ret = PERF.perfstat_cpu_total(null, cpu, cpu.size(), 1);
        if (ret > 0) {
            return cpu;
        }
        return new perfstat_cpu_total_t();
    }

    /**
     * Queries perfstat_cpu for per-CPU usage statistics
     *
     * @return an array of usage statistics
     */
    public static perfstat_cpu_t[] queryCpu() {
        perfstat_cpu_t cpu = new perfstat_cpu_t();
        // With null, null, ..., 0, returns total # of elements
        int cputotal = PERF.perfstat_cpu(null, null, cpu.size(), 0);
        if (cputotal > 0) {
            perfstat_cpu_t[] statp = (perfstat_cpu_t[]) cpu.toArray(cputotal);
            perfstat_id_t firstcpu = new perfstat_id_t(); // name is ""
            int ret = PERF.perfstat_cpu(firstcpu, statp, cpu.size(), cputotal);
            if (ret > 0) {
                return statp;
            }
        }
        return new perfstat_cpu_t[0];
    }

    /**
     * Returns affinity mask from the number of CPU in the OS.
     *
     * @return affinity mask
     */
    public static long queryCpuAffinityMask() {
        int cpus = queryCpuTotal().ncpus;
        if (cpus < 63) {
            return (1L << cpus) - 1;
        }
        return cpus == 63 ? Long.MAX_VALUE : -1L;
    }

}
