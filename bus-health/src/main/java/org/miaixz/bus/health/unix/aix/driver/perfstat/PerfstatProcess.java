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
package org.miaixz.bus.health.unix.aix.driver.perfstat;

import java.util.Arrays;

import com.sun.jna.platform.unix.aix.Perfstat;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_id_t;
import com.sun.jna.platform.unix.aix.Perfstat.perfstat_process_t;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;

/**
 * Queries performance stats for processes
 *
 * @author Kimi Liu
 */
@ThreadSafe
public final class PerfstatProcess {

    /**
     * Keeps AIX perfstat process queries on the static API.
     */
    private PerfstatProcess() {
        // No initialization required.
    }

    /**
     * The PERF constant.
     */
    private static final Perfstat PERF = Perfstat.INSTANCE;

    /**
     * Minimum slack added to the perfstat process count between count and fill calls.
     */
    private static final int MIN_PROC_COUNT_PAD = 64;

    /**
     * Divisor for proportional process-count padding.
     */
    private static final int PROC_COUNT_PAD_DIVISOR = 10;

    /**
     * Maximum number of buffer-fill retries after an exactly full result.
     */
    private static final int MAX_BUFFER_RETRIES = 3;

    /**
     * Returns the padded allocation size for a reported process count.
     *
     * @param count the reported process count
     * @return the padded allocation size
     */
    private static int paddedSize(int count) {
        return count + Math.max(MIN_PROC_COUNT_PAD, count / PROC_COUNT_PAD_DIVISOR);
    }

    /**
     * Queries perfstat_process for per-process usage statistics
     *
     * @return an array of usage statistics
     */
    public static perfstat_process_t[] queryProcesses() {
        perfstat_process_t process = new perfstat_process_t();
        // With null, null, ..., 0, returns total # of elements
        int procCount = PERF.perfstat_process(null, null, process.size(), 0);
        for (int attempt = 0; procCount > 0; attempt++) {
            int padded = paddedSize(procCount);
            perfstat_process_t[] proct = (perfstat_process_t[]) process.toArray(padded);
            perfstat_id_t firstprocess = new perfstat_id_t(); // name is ""
            int ret = PERF.perfstat_process(firstprocess, proct, process.size(), padded);
            if (ret <= 0) {
                break;
            }
            if (ret == padded && attempt < MAX_BUFFER_RETRIES) {
                int recount = PERF.perfstat_process(null, null, process.size(), 0);
                if (recount > 0) {
                    procCount = recount;
                    continue;
                }
            }
            return Arrays.copyOf(proct, ret);
        }
        return new perfstat_process_t[0];
    }

}
