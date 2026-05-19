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

import com.sun.jna.platform.win32.VersionHelpers;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.windows.PerfCounterQuery;
import org.miaixz.bus.health.windows.PerfCounterWildcardQuery;

/**
 * Utility to query Processor performance counter
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class ProcessorInformation {

    /**
     * The IS_WIN7_OR_GREATER constant.
     */
    private static final boolean IS_WIN7_OR_GREATER = VersionHelpers.IsWindows7OrGreater();

    /**
     * Creates a new ProcessorInformation instance.
     */
    private ProcessorInformation() {
        // No initialization required.
    }

    /**
     * Returns processor performance counters.
     *
     * @return Performance Counters for processors.
     */
    public static Pair<List<String>, Map<ProcessorTickCountProperty, List<Long>>> queryProcessorCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return IS_WIN7_OR_GREATER
                ? PerfCounterWildcardQuery.queryInstancesAndValues(
                        ProcessorTickCountProperty.class,
                        PerfmonConsts.PROCESSOR_INFORMATION,
                        PerfmonConsts.WIN32_PERF_RAW_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL)
                : PerfCounterWildcardQuery.queryInstancesAndValues(
                        ProcessorTickCountProperty.class,
                        PerfmonConsts.PROCESSOR,
                        PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_OS_PROCESSOR_WHERE_NAME_NOT_TOTAL);
    }

    /**
     * Returns system performance counters.
     *
     * @return Performance Counters for the total of all processors.
     */
    public static Map<SystemTickCountProperty, Long> querySystemCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(
                SystemTickCountProperty.class,
                PerfmonConsts.PROCESSOR,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_OS_PROCESSOR_WHERE_NAME_TOTAL);
    }

    /**
     * Returns processor capacity performance counters.
     *
     * @return Performance Counters for processor capacity.
     */
    public static Pair<List<String>, Map<ProcessorUtilityTickCountProperty, List<Long>>> queryProcessorCapacityCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                ProcessorUtilityTickCountProperty.class,
                PerfmonConsts.PROCESSOR_INFORMATION,
                PerfmonConsts.WIN32_PERF_RAW_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL);
    }

    /**
     * Returns system interrupts counters.
     *
     * @return Interrupts counter for the total of all processors.
     */
    public static Map<InterruptsProperty, Long> queryInterruptCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(
                InterruptsProperty.class,
                PerfmonConsts.PROCESSOR,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_OS_PROCESSOR_WHERE_NAME_TOTAL);
    }

    /**
     * Returns processor frequency counters.
     *
     * @return Processor frequency counter for each processor.
     */
    public static Pair<List<String>, Map<ProcessorFrequencyProperty, List<Long>>> queryFrequencyCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValues(
                ProcessorFrequencyProperty.class,
                PerfmonConsts.PROCESSOR_INFORMATION,
                PerfmonConsts.WIN32_PERF_RAW_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL);
    }

    /**
     * Returns processor performance counters from the WMI formatted table.
     *
     * @return Processor performance percentage counters for each processor.
     */
    public static Pair<List<String>, Map<ProcessorPerformanceProperty, List<Long>>> queryProcessorPerformanceCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Pair.of(Collections.emptyList(), Collections.emptyMap());
        }
        return PerfCounterWildcardQuery.queryInstancesAndValuesFromWMI(
                ProcessorPerformanceProperty.class,
                PerfmonConsts.WIN32_PERF_FORMATTED_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL);
    }

    /**
     * System interrupts counters
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum InterruptsProperty implements PerfCounterQuery.PdhCounterProperty {

        /**
         * Executes the interruptspersec operation.
         *
         * @param PerfCounterQuery the perf counter query value
         */

        INTERRUPTSPERSEC(PerfCounterQuery.TOTAL_INSTANCE, "Interrupts/sec");

        /**
         * The instance value.
         */
        private final String instance;

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new InterruptsProperty instance.
         *
         * @param instance the instance
         * @param counter  the counter
         */
        InterruptsProperty(String instance, String counter) {
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
     * Processor Frequency counters. Requires Win7 or greater
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProcessorFrequencyProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element defines WMI instance name field and PDH instance filter
        /**
         * Executes the name operation.
         *
         * @param PerfCounterQuery the perf counter query value
         */
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        // Remaining elements define counters
        /**
         * Executes the percentofmaximumfrequency operation.
         *
         * @param Frequency the frequency value
         */
        PERCENTOFMAXIMUMFREQUENCY("% of Maximum Frequency");

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new ProcessorFrequencyProperty instance.
         *
         * @param counter the counter
         */
        ProcessorFrequencyProperty(String counter) {
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
     * Processor performance counters from the WMI formatted data table.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProcessorPerformanceProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element defines WMI instance name field and PDH instance filter
        /**
         * Executes the name operation.
         *
         * @param PerfCounterQuery the perf counter query value
         */
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        // Remaining elements define counters
        /**
         * Executes the percentprocessorperformance operation.
         *
         * @param Performance the performance value
         */
        PERCENTPROCESSORPERFORMANCE("% Processor Performance");

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new ProcessorPerformanceProperty instance.
         *
         * @param counter the counter
         */
        ProcessorPerformanceProperty(String counter) {
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
     * Processor performance counters
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProcessorTickCountProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element defines WMI instance name field and PDH instance filter
        /**
         * Executes the name operation.
         *
         * @param PerfCounterQuery the perf counter query value
         */
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        // Remaining elements define counters
        /**
         * Executes the percentdpctime operation.
         *
         * @param Time the time value
         */
        PERCENTDPCTIME("% DPC Time"), //
        /**
         * Executes the percentinterrupttime operation.
         *
         * @param Time the time value
         */
        PERCENTINTERRUPTTIME("% Interrupt Time"), //
        /**
         * Executes the percentprivilegedtime operation.
         *
         * @param Time the time value
         */
        PERCENTPRIVILEGEDTIME("% Privileged Time"), //
        /**
         * Executes the percentprocessortime operation.
         *
         * @param Time the time value
         */
        PERCENTPROCESSORTIME("% Processor Time"), //
        /**
         * Executes the percentusertime operation.
         *
         * @param Time the time value
         */
        PERCENTUSERTIME("% User Time");

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new ProcessorTickCountProperty instance.
         *
         * @param counter the counter
         */
        ProcessorTickCountProperty(String counter) {
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
     * Processor performance counters including utility counters
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProcessorUtilityTickCountProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {

        // First element defines WMI instance name field and PDH instance filter
        /**
         * Executes the name operation.
         *
         * @param PerfCounterQuery the perf counter query value
         */
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        // Remaining elements define counters
        /**
         * Executes the percentdpctime operation.
         *
         * @param Time the time value
         */
        PERCENTDPCTIME("% DPC Time"), //
        /**
         * Executes the percentinterrupttime operation.
         *
         * @param Time the time value
         */
        PERCENTINTERRUPTTIME("% Interrupt Time"), //
        /**
         * Executes the percentprivilegedtime operation.
         *
         * @param Time the time value
         */
        PERCENTPRIVILEGEDTIME("% Privileged Time"), //
        /**
         * Executes the percentprocessortime operation.
         *
         * @param Time the time value
         */
        PERCENTPROCESSORTIME("% Processor Time"), //
        // The above 3 counters are 100ns base type
        // For PDH accessible as secondary counter in any of them
        /**
         * Executes the timestamp sys100 ns operation.
         *
         * @param Time_Base the time base value
         */
        TIMESTAMP_SYS100NS("% Processor Time_Base"), //
        /**
         * Executes the percentprivilegedutility operation.
         *
         * @param Utility the utility value
         */
        PERCENTPRIVILEGEDUTILITY("% Privileged Utility"), //
        /**
         * Executes the percentprocessorutility operation.
         *
         * @param Utility the utility value
         */
        PERCENTPROCESSORUTILITY("% Processor Utility"), //
        /**
         * Executes the percentprocessorutility base operation.
         *
         * @param Utility_Base the utility base value
         */
        PERCENTPROCESSORUTILITY_BASE("% Processor Utility_Base"), //
        /**
         * Executes the percentusertime operation.
         *
         * @param Time the time value
         */
        PERCENTUSERTIME("% User Time");

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new ProcessorUtilityTickCountProperty instance.
         *
         * @param counter the counter
         */
        ProcessorUtilityTickCountProperty(String counter) {
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
     * System performance counters
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum SystemTickCountProperty implements PerfCounterQuery.PdhCounterProperty {

        /**
         * Executes the percentdpctime operation.
         *
         * @param PerfCounterQuery the perf counter query value
         * @param Time             the time value
         */

        PERCENTDPCTIME(PerfCounterQuery.TOTAL_INSTANCE, "% DPC Time"), //
        /**
         * Executes the percentinterrupttime operation.
         *
         * @param PerfCounterQuery the perf counter query value
         * @param Time             the time value
         */
        PERCENTINTERRUPTTIME(PerfCounterQuery.TOTAL_INSTANCE, "% Interrupt Time");

        /**
         * The instance value.
         */
        private final String instance;

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new SystemTickCountProperty instance.
         *
         * @param instance the instance
         * @param counter  the counter
         */
        SystemTickCountProperty(String instance, String counter) {
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

}
