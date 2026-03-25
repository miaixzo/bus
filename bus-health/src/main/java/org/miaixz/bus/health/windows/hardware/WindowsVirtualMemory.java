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
package org.miaixz.bus.health.windows.hardware;

import java.util.Map;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.windows.driver.perfmon.MemoryInformation;
import org.miaixz.bus.health.windows.driver.perfmon.MemoryInformation.PageSwapProperty;
import org.miaixz.bus.health.windows.driver.perfmon.PagingFile;
import org.miaixz.bus.health.windows.driver.perfmon.PagingFile.PagingPercentProperty;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;

/**
 * Memory obtained from WMI
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class WindowsVirtualMemory extends AbstractVirtualMemory {

    private final WindowsGlobalMemory global;

    private final Supplier<Long> used = Memoizer
            .memoize(WindowsVirtualMemory::querySwapUsed, Memoizer.defaultExpiration());

    private final Supplier<Triplet<Long, Long, Long>> totalVmaxVused = Memoizer
            .memoize(WindowsVirtualMemory::querySwapTotalVirtMaxVirtUsed, Memoizer.defaultExpiration());

    private final Supplier<Pair<Long, Long>> swapInOut = Memoizer
            .memoize(WindowsVirtualMemory::queryPageSwaps, Memoizer.defaultExpiration());

    /**
     * Constructor for WindowsVirtualMemory.
     *
     * @param windowsGlobalMemory The parent global memory class instantiating this
     */
    WindowsVirtualMemory(WindowsGlobalMemory windowsGlobalMemory) {
        this.global = windowsGlobalMemory;
    }

    private static long querySwapUsed() {
        return PagingFile.querySwapUsed().getOrDefault(PagingPercentProperty.PERCENTUSAGE, 0L);
    }

    private static Triplet<Long, Long, Long> querySwapTotalVirtMaxVirtUsed() {
        try (Struct.CloseablePerformanceInformation perfInfo = new Struct.CloseablePerformanceInformation()) {
            if (!Psapi.INSTANCE.GetPerformanceInfo(perfInfo, perfInfo.size())) {
                Logger.error("Failed to get Performance Info. Error code: {}", Kernel32.INSTANCE.GetLastError());
                return Triplet.of(0L, 0L, 0L);
            }
            return Triplet.of(
                    perfInfo.CommitLimit.longValue() - perfInfo.PhysicalTotal.longValue(),
                    perfInfo.CommitLimit.longValue(),
                    perfInfo.CommitTotal.longValue());
        }
    }

    private static Pair<Long, Long> queryPageSwaps() {
        Map<PageSwapProperty, Long> valueMap = MemoryInformation.queryPageSwaps();
        return Pair.of(
                valueMap.getOrDefault(PageSwapProperty.PAGESINPUTPERSEC, 0L),
                valueMap.getOrDefault(PageSwapProperty.PAGESOUTPUTPERSEC, 0L));
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
