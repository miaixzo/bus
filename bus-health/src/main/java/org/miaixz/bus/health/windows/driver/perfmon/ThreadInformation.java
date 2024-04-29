/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.windows.driver.perfmon;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.windows.PerfCounterQuery;
import org.miaixz.bus.health.windows.PerfCounterWildcardQuery;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility to query Thread Information performance counter
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class ThreadInformation {

    /**
     * Returns thread counters.
     *
     * @return Thread counters for each thread.
     */
    public static Pair<List<String>, Map<ThreadPerformanceProperty, List<Long>>> queryThreadCounters() {
        return PerfCounterWildcardQuery.queryInstancesAndValues(ThreadPerformanceProperty.class, PerfmonConsts.THREAD,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_PROC_THREAD_WHERE_NOT_NAME_LIKE_TOTAL);
    }

    /**
     * Returns thread counters filtered to the specified process name and thread.
     *
     * @param name      The process name to filter
     * @param threadNum The thread number to match. -1 matches all threads.
     * @return Thread counters for each thread.
     */
    public static Pair<List<String>, Map<ThreadPerformanceProperty, List<Long>>> queryThreadCounters(String name,
                                                                                                     int threadNum) {
        String procName = name.toLowerCase(Locale.ROOT);
        if (threadNum >= 0) {
            return PerfCounterWildcardQuery.queryInstancesAndValues(
                    ThreadPerformanceProperty.class, PerfmonConsts.THREAD, PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_PROC_THREAD
                            + " WHERE Name LIKE \\\"" + procName + "\\\" AND IDThread=" + threadNum,
                    procName + "/" + threadNum);
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(ThreadPerformanceProperty.class, PerfmonConsts.THREAD,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_PROC_THREAD + " WHERE Name LIKE \\\"" + procName + "\\\"", procName + "/*");
    }

    /**
     * Thread performance counters
     */
    public enum ThreadPerformanceProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {
        // First element defines WMI instance name field and PDH instance filter
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        // Remaining elements define counters
        PERCENTUSERTIME("% User Time"), //
        PERCENTPRIVILEGEDTIME("% Privileged Time"), //
        ELAPSEDTIME("Elapsed Time"), //
        PRIORITYCURRENT("Priority Current"), //
        STARTADDRESS("Start Address"), //
        THREADSTATE("Thread State"), //
        THREADWAITREASON("Thread Wait Reason"), // 5 is SUSPENDED
        IDPROCESS("ID Process"), //
        IDTHREAD("ID Thread"), //
        CONTEXTSWITCHESPERSEC("Context Switches/sec");

        private final String counter;

        ThreadPerformanceProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return counter;
        }
    }

}
