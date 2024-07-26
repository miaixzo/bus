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

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.PerfCounterQuery;

import java.util.Collections;
import java.util.Map;

/**
 * Utility to query Memory performance counter
 *
 * @author Kimi Liu
 * @since Java 17+
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
        return PerfCounterQuery.queryValues(PageSwapProperty.class, PerfmonConsts.MEMORY,
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
