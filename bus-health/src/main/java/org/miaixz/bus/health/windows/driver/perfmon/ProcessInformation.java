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
package org.miaixz.bus.health.windows.driver.perfmon;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.windows.PerfCounterQuery;
import org.miaixz.bus.health.windows.PerfCounterWildcardQuery;

/**
 * Utility to query Process Information performance counter
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class ProcessInformation {

    /**
     * Prevents instantiation of utility class.
     */
    private ProcessInformation() {
        // No initialization required.
    }

    /**
     * Returns process counters.
     *
     * @return Process counters for each process.
     */
    public static Pair<List<String>, Map<ProcessPerformanceProperty, List<Long>>> queryProcessCounters() {
        if (PerfmonDisabled.PERF_PROC_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                ProcessPerformanceProperty.class,
                PerfmonConsts.PROCESS,
                PerfmonConsts.WIN32_PERFPROC_PROCESS_WHERE_NOT_NAME_LIKE_TOTAL);
    }

    /**
     * Returns handle counters.
     *
     * @return Handle count for the _Total instance.
     */
    public static Map<HandleCountProperty, Long> queryHandles() {
        if (PerfmonDisabled.PERF_PROC_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(
                HandleCountProperty.class,
                PerfmonConsts.PROCESS,
                PerfmonConsts.WIN32_PERFPROC_PROCESS_WHERE_NAME_TOTAL);
    }

    /**
     * Returns cooked idle process performance counters.
     *
     * @return Cooked performance counters for idle process.
     */
    public static Pair<List<String>, Map<IdleProcessorTimeProperty, List<Long>>> queryIdleProcessCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                IdleProcessorTimeProperty.class,
                PerfmonConsts.PROCESS,
                PerfmonConsts.WIN32_PERFPROC_PROCESS_WHERE_IDPROCESS_0);
    }

    /**
     * Process performance counters
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProcessPerformanceProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        /**
         * The name process performance property.
         */
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        /**
         * Priority Base.
         */
        PRIORITYBASE("Priority Base"),
        /**
         * Elapsed Time.
         */
        ELAPSEDTIME("Elapsed Time"),
        /**
         * ID Process.
         */
        IDPROCESS("ID Process"),
        /**
         * Creating Process ID.
         */
        CREATINGPROCESSID("Creating Process ID"),
        /**
         * IO Read Bytes/sec.
         */
        IOREADBYTESPERSEC("IO Read Bytes/sec"),
        /**
         * IO Write Bytes/sec.
         */
        IOWRITEBYTESPERSEC("IO Write Bytes/sec"),
        /**
         * Working Set - Private.
         */
        WORKINGSETPRIVATE("Working Set - Private"),
        /**
         * Working Set.
         */
        WORKINGSET("Working Set"),
        /**
         * Page Faults/sec.
         */
        PAGEFAULTSPERSEC("Page Faults/sec");

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new ProcessPerformanceProperty instance.
         *
         * @param counter the counter
         */
        ProcessPerformanceProperty(String counter) {
            this.counter = counter;
        }

        /**
         * Returns the counter.
         *
         * @return the get counter result
         */
        @Override
        public String getCounter() {
            return counter;
        }

    }

    /**
     * Handle performance counters
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum HandleCountProperty implements PerfCounterQuery.PdhCounterProperty {

        /**
         * Handle Count.
         */
        HANDLECOUNT(PerfmonConsts.TOTAL_INSTANCE, "Handle Count");

        /**
         * The instance value.
         */
        private final String instance;

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new HandleCountProperty instance.
         *
         * @param instance the instance
         * @param counter  the counter
         */
        HandleCountProperty(String instance, String counter) {
            this.instance = instance;
            this.counter = counter;
        }

        /**
         * Returns the instance.
         *
         * @return the get instance result
         */
        @Override
        public String getInstance() {
            return instance;
        }

        /**
         * Returns the counter.
         *
         * @return the get counter result
         */
        @Override
        public String getCounter() {
            return counter;
        }

    }

    /**
     * Processor performance counters
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum IdleProcessorTimeProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        /**
         * The name idle processor time property.
         */
        NAME(PerfCounterQuery.TOTAL_OR_IDLE_INSTANCES),
        /**
         * % Processor Time.
         */
        PERCENTPROCESSORTIME("% Processor Time"),
        /**
         * Elapsed Time.
         */
        ELAPSEDTIME("Elapsed Time");

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new IdleProcessorTimeProperty instance.
         *
         * @param counter the counter
         */
        IdleProcessorTimeProperty(String counter) {
            this.counter = counter;
        }

        /**
         * Returns the counter.
         *
         * @return the get counter result
         */
        @Override
        public String getCounter() {
            return counter;
        }

    }

}
