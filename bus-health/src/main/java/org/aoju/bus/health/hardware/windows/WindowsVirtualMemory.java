/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.health.hardware.windows;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.Psapi.PERFORMANCE_INFORMATION;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.windows.PerfCounterQuery;
import org.aoju.bus.health.common.windows.PerfCounterQuery.PdhCounterProperty;
import org.aoju.bus.health.hardware.AbstractVirtualMemory;
import org.aoju.bus.logger.Logger;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Memory obtained from WMI
 *
 * @author Kimi Liu
 * @version 5.8.2
 * @since JDK 1.8+
 */
public class WindowsVirtualMemory extends AbstractVirtualMemory {

    private final long pageSize;
    private final Supplier<Long> total = Memoizer.memoize(this::querySwapTotal, Memoizer.defaultExpiration());
    private PerfCounterQuery<PageSwapProperty> memoryPerfCounters = new PerfCounterQuery<>(PageSwapProperty.class,
            "Memory", "Win32_PerfRawData_PerfOS_Memory");
    private final Supplier<PagingFile> pagingFile = Memoizer.memoize(this::queryPagingFile, Memoizer.defaultExpiration());
    private PerfCounterQuery<PagingPercentProperty> pagingPerfCounters = new PerfCounterQuery<>(
            PagingPercentProperty.class, "Paging File", "Win32_PerfRawData_PerfOS_PagingFile");
    private final Supplier<Long> used = Memoizer.memoize(this::querySwapUsed, Memoizer.defaultExpiration());

    /**
     * <p>
     * Constructor for WindowsVirtualMemory.
     * </p>
     *
     * @param pageSize The size in bites of memory pages
     */
    public WindowsVirtualMemory(long pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public long getSwapUsed() {
        return used.get();
    }

    @Override
    public long getSwapTotal() {
        return total.get();
    }

    @Override
    public long getSwapPagesIn() {
        return pagingFile.get().pagesIn;
    }

    @Override
    public long getSwapPagesOut() {
        return pagingFile.get().pagesOut;
    }

    private long querySwapUsed() {
        Map<PagingPercentProperty, Long> valueMap = this.pagingPerfCounters.queryValues();
        return valueMap.getOrDefault(PagingPercentProperty.PERCENTUSAGE, 0L) * this.pageSize;
    }

    private long querySwapTotal() {
        PERFORMANCE_INFORMATION perfInfo = new PERFORMANCE_INFORMATION();
        if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
            Logger.error("Failed to get Performance Info. Error code: {}", Kernel32.INSTANCE.GetLastError());
            return 0L;
        }
        return this.pageSize * (perfInfo.CommitLimit.longValue() - perfInfo.PhysicalTotal.longValue());
    }

    private PagingFile queryPagingFile() {
        Map<PageSwapProperty, Long> valueMap = this.memoryPerfCounters.queryValues();
        return new PagingFile(valueMap.getOrDefault(PageSwapProperty.PAGESINPUTPERSEC, 0L),
                valueMap.getOrDefault(PageSwapProperty.PAGESOUTPUTPERSEC, 0L));
    }

    /*
     * For swap file usage
     */
    enum PagingPercentProperty implements PdhCounterProperty {
        PERCENTUSAGE(PerfCounterQuery.TOTAL_INSTANCE, "% Usage");

        private final String instance;
        private final String counter;

        PagingPercentProperty(String instance, String counter) {
            this.instance = instance;
            this.counter = counter;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getInstance() {
            return instance;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getCounter() {
            return counter;
        }
    }

    /*
     * For pages in/out
     */
    public enum PageSwapProperty implements PdhCounterProperty {
        PAGESINPUTPERSEC(null, "Pages Input/sec"), //
        PAGESOUTPUTPERSEC(null, "Pages Output/sec");

        private final String instance;
        private final String counter;

        PageSwapProperty(String instance, String counter) {
            this.instance = instance;
            this.counter = counter;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getInstance() {
            return instance;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getCounter() {
            return counter;
        }
    }

    private static final class PagingFile {
        private final long pagesIn;
        private final long pagesOut;

        private PagingFile(long pagesIn, long pagesOut) {
            this.pagesIn = pagesIn;
            this.pagesOut = pagesOut;
        }
    }

}
