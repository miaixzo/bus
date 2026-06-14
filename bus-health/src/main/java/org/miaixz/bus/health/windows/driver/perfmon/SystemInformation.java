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
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.PerfCounterQuery;

/**
 * Utility to query System performance counters
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class SystemInformation {

    /**
     * Creates a new SystemInformation instance.
     */
    private SystemInformation() {
        // No initialization required.
    }

    /**
     * Returns system context switch counters.
     *
     * @return Context switches counter for the total of all processors.
     */
    public static Map<ContextSwitchProperty, Long> queryContextSwitchCounters() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(
                ContextSwitchProperty.class,
                PerfmonConsts.SYSTEM,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_OS_SYSTEM);
    }

    /**
     * Returns processor queue length.
     *
     * @return Processor Queue Length.
     */
    public static Map<ProcessorQueueLengthProperty, Long> queryProcessorQueueLength() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(
                ProcessorQueueLengthProperty.class,
                PerfmonConsts.SYSTEM,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_OS_SYSTEM);
    }

    /**
     * Context switch property
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ContextSwitchProperty implements PerfCounterQuery.PdhCounterProperty {

        /**
         * Context switches per second counter.
         */
        CONTEXTSWITCHESPERSEC(null, "Context Switches/sec");

        /**
         * The instance value.
         */
        private final String instance;

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new ContextSwitchProperty instance.
         *
         * @param instance the instance
         * @param counter  the counter
         */
        ContextSwitchProperty(String instance, String counter) {
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
     * Processor Queue Length property
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    public enum ProcessorQueueLengthProperty implements PerfCounterQuery.PdhCounterProperty {

        /**
         * Processor queue length counter.
         */
        PROCESSORQUEUELENGTH(null, "Processor Queue Length");

        /**
         * The instance value.
         */
        private final String instance;

        /**
         * The counter value.
         */
        private final String counter;

        /**
         * Creates a new ProcessorQueueLengthProperty instance.
         *
         * @param instance the instance
         * @param counter  the counter
         */
        ProcessorQueueLengthProperty(String instance, String counter) {
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
