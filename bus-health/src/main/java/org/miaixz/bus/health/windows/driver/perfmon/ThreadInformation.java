/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.windows.driver.perfmon;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.windows.PerfCounterQuery;
import org.miaixz.bus.health.windows.PerfCounterWildcardQuery;

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
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                ThreadPerformanceProperty.class,
                PerfmonConsts.THREAD,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_PROC_THREAD_WHERE_NOT_NAME_LIKE_TOTAL);
    }

    /**
     * Returns thread counters filtered to the specified process name and thread.
     *
     * @param name      The process name to filter
     * @param threadNum The thread number to match. -1 matches all threads.
     * @return Thread counters for each thread.
     */
    public static Pair<List<String>, Map<ThreadPerformanceProperty, List<Long>>> queryThreadCounters(
            String name,
            int threadNum) {
        String procName = name.toLowerCase(Locale.ROOT);
        if (threadNum >= 0) {
            Pair<List<String>, Map<ThreadPerformanceProperty, List<Long>>> threads = PerfCounterWildcardQuery
                    .queryInstancesAndValues(
                            ThreadPerformanceProperty.class,
                            PerfmonConsts.THREAD,
                            PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_PROC_THREAD + " WHERE Name LIKE \"" + procName
                                    + "/_%\" AND IDThread=" + threadNum,
                            procName + "/*");
            if (!threads.getLeft().isEmpty()) {
                return threads;
            }
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                ThreadPerformanceProperty.class,
                PerfmonConsts.THREAD,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_PROC_THREAD + " WHERE Name LIKE \"" + procName + "/_%\"",
                procName + "/*");
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
