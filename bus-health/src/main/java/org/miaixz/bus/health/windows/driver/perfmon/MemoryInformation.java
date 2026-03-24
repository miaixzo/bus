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
package org.miaixz.bus.health.windows.driver.perfmon;

import java.util.Collections;
import java.util.Map;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.PerfCounterQuery;

/**
 * Utility to query Memory performance counter
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class MemoryInformation {

    /**
     * Returns page swap counters
     *
     * @return Page swap counters for memory.
     */
    public static Map<PageSwapProperty, Long> queryPageSwaps() {
        if (PerfmonDisabled.PERF_OS_DISABLED) {
            return Collections.emptyMap();
        }
        return PerfCounterQuery.queryValues(
                PageSwapProperty.class,
                PerfmonConsts.MEMORY,
                PerfmonConsts.WIN32_PERF_RAW_DATA_PERF_OS_MEMORY);
    }

    /**
     * For pages in/out
     */
    public enum PageSwapProperty implements PerfCounterQuery.PdhCounterProperty {

        PAGESINPUTPERSEC(null, "Pages Input/sec"), //
        PAGESOUTPUTPERSEC(null, "Pages Output/sec");

        private final String instance;
        private final String counter;

        PageSwapProperty(String instance, String counter) {
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

}
