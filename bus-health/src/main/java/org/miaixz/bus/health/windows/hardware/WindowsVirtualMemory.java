/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
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
package org.miaixz.bus.health.windows.hardware;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triple;
import org.miaixz.bus.health.Memoize;
import org.miaixz.bus.health.builtin.Struct;
import org.miaixz.bus.health.builtin.hardware.AbstractVirtualMemory;
import org.miaixz.bus.health.windows.drivers.perfmon.MemoryInformation;
import org.miaixz.bus.health.windows.drivers.perfmon.PagingFile;
import org.miaixz.bus.logger.Logger;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Memory obtained from WMI
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class WindowsVirtualMemory extends AbstractVirtualMemory {

    private final WindowsGlobalMemory global;

    private final Supplier<Long> used = Memoize.memoize(WindowsVirtualMemory::querySwapUsed, Memoize.defaultExpiration());

    private final Supplier<Triple<Long, Long, Long>> totalVmaxVused = Memoize.memoize(
            WindowsVirtualMemory::querySwapTotalVirtMaxVirtUsed, Memoize.defaultExpiration());

    private final Supplier<Pair<Long, Long>> swapInOut = Memoize.memoize(WindowsVirtualMemory::queryPageSwaps,
            Memoize.defaultExpiration());

    /**
     * Constructor for WindowsVirtualMemory.
     *
     * @param windowsGlobalMemory The parent global memory class instantiating this
     */
    WindowsVirtualMemory(WindowsGlobalMemory windowsGlobalMemory) {
        this.global = windowsGlobalMemory;
    }

    private static long querySwapUsed() {
        return PagingFile.querySwapUsed().getOrDefault(PagingFile.PagingPercentProperty.PERCENTUSAGE, 0L);
    }

    private static Triple<Long, Long, Long> querySwapTotalVirtMaxVirtUsed() {
        try (Struct.CloseablePerformanceInformation perfInfo = new Struct.CloseablePerformanceInformation()) {
            if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
                Logger.error("Failed to get Performance Info. Error code: {}", Kernel32.INSTANCE.GetLastError());
                return Triple.of(0L, 0L, 0L);
            }
            return Triple.of(perfInfo.CommitLimit.longValue() - perfInfo.PhysicalTotal.longValue(),
                    perfInfo.CommitLimit.longValue(), perfInfo.CommitTotal.longValue());
        }
    }

    private static Pair<Long, Long> queryPageSwaps() {
        Map<MemoryInformation.PageSwapProperty, Long> valueMap = MemoryInformation.queryPageSwaps();
        return Pair.of(valueMap.getOrDefault(MemoryInformation.PageSwapProperty.PAGESINPUTPERSEC, 0L),
                valueMap.getOrDefault(MemoryInformation.PageSwapProperty.PAGESOUTPUTPERSEC, 0L));
    }

    @Override
    public long getSwapUsed() {
        return this.global.getPageSize() * used.get();
    }

    @Override
    public long getSwapTotal() {
        return this.global.getPageSize() * totalVmaxVused.get().getLeft();
    }

    @Override
    public long getVirtualMax() {
        return this.global.getPageSize() * totalVmaxVused.get().getMiddle();
    }

    @Override
    public long getVirtualInUse() {
        return this.global.getPageSize() * totalVmaxVused.get().getRight();
    }

    @Override
    public long getSwapPagesIn() {
        return swapInOut.get().getLeft();
    }

    @Override
    public long getSwapPagesOut() {
        return swapInOut.get().getRight();
    }

}
