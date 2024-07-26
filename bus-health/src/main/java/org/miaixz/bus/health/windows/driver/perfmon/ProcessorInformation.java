/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.windows.driver.perfmon;

import com.sun.jna.platform.win32.VersionHelpers;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.windows.PerfCounterQuery;
import org.miaixz.bus.health.windows.PerfCounterWildcardQuery;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utility to query Processor performance counter
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class ProcessorInformation {

    private static final boolean IS_WIN7_OR_GREATER = VersionHelpers.IsWindows7OrGreater();

    private ProcessorInformation() {
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
                ? PerfCounterWildcardQuery.queryInstancesAndValues(ProcessorTickCountProperty.class,
                        PerfmonConsts.PROCESSOR_INFORMATION,
                        PerfmonConsts.WIN32_PERF_RAW_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL)
                : PerfCounterWildcardQuery.queryInstancesAndValues(ProcessorTickCountProperty.class,
                        PerfmonConsts.PROCESSOR,
                        PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_OS_PROCESSOR_WHERE_NAME_NOT_TOTAL);
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
        return PerfCounterWildcardQuery.queryInstancesAndValues(ProcessorUtilityTickCountProperty.class,
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
        return PerfCounterQuery.queryValues(InterruptsProperty.class, PerfmonConsts.PROCESSOR,
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
        return PerfCounterWildcardQuery.queryInstancesAndValues(ProcessorFrequencyProperty.class,
                PerfmonConsts.PROCESSOR_INFORMATION,
                PerfmonConsts.WIN32_PERF_RAW_DATA_COUNTERS_PROCESSOR_INFORMATION_WHERE_NOT_NAME_LIKE_TOTAL);
    }

    /**
     * System interrupts counters
     */
    public enum InterruptsProperty implements PerfCounterQuery.PdhCounterProperty {

        INTERRUPTSPERSEC(PerfCounterQuery.TOTAL_INSTANCE, "Interrupts/sec");

        private final String instance;
        private final String counter;

        InterruptsProperty(String instance, String counter) {
            this.instance = instance;
            this.counter = counter;
        }

        @Override
        public String getInstance() {
            return instance;
        }

        @Override
        public String getCounter() {
            return counter;
        }
    }

    /**
     * Processor Frequency counters. Requires Win7 or greater
     */
    public enum ProcessorFrequencyProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {
        // First element defines WMI instance name field and PDH instance filter
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        // Remaining elements define counters
        PERCENTOFMAXIMUMFREQUENCY("% of Maximum Frequency");

        private final String counter;

        ProcessorFrequencyProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return counter;
        }
    }

    /**
     * Processor performance counters
     */
    public enum ProcessorTickCountProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {
        // First element defines WMI instance name field and PDH instance filter
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        // Remaining elements define counters
        PERCENTDPCTIME("% DPC Time"), //
        PERCENTINTERRUPTTIME("% Interrupt Time"), //
        PERCENTPRIVILEGEDTIME("% Privileged Time"), //
        PERCENTPROCESSORTIME("% Processor Time"), //
        PERCENTUSERTIME("% User Time");

        private final String counter;

        ProcessorTickCountProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return counter;
        }
    }

    /**
     * Processor performance counters including utility counters
     */
    public enum ProcessorUtilityTickCountProperty implements PerfCounterWildcardQuery.PdhCounterWildcardProperty {
        // First element defines WMI instance name field and PDH instance filter
        NAME(PerfCounterQuery.NOT_TOTAL_INSTANCES),
        // Remaining elements define counters
        PERCENTDPCTIME("% DPC Time"), //
        PERCENTINTERRUPTTIME("% Interrupt Time"), //
        PERCENTPRIVILEGEDTIME("% Privileged Time"), //
        PERCENTPROCESSORTIME("% Processor Time"), //
        // The above 3 counters are 100ns base type
        // For PDH accessible as secondary counter in any of them
        TIMESTAMP_SYS100NS("% Processor Time_Base"), //
        PERCENTPRIVILEGEDUTILITY("% Privileged Utility"), //
        PERCENTPROCESSORUTILITY("% Processor Utility"), //
        PERCENTPROCESSORUTILITY_BASE("% Processor Utility_Base"), //
        PERCENTUSERTIME("% User Time");

        private final String counter;

        ProcessorUtilityTickCountProperty(String counter) {
            this.counter = counter;
        }

        @Override
        public String getCounter() {
            return counter;
        }
    }

}
