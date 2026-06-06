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
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;

/**
 * Memory obtained by swapinfo
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class FreeBsdVirtualMemory extends AbstractVirtualMemory {

    /**
     * The global value.
     */
    private final FreeBsdGlobalMemory global;

    /**
     * The used value.
     */
    private final Supplier<Long> used = Memoizer
            .memoize(FreeBsdVirtualMemory::querySwapUsed, Memoizer.defaultExpiration());

    /**
     * The total value.
     */
    private final Supplier<Long> total = Memoizer
            .memoize(FreeBsdVirtualMemory::querySwapTotal, Memoizer.defaultExpiration());

    /**
     * The pagesIn value.
     */
    private final Supplier<Long> pagesIn = Memoizer
            .memoize(FreeBsdVirtualMemory::queryPagesIn, Memoizer.defaultExpiration());

    /**
     * The pagesOut value.
     */
    private final Supplier<Long> pagesOut = Memoizer
            .memoize(FreeBsdVirtualMemory::queryPagesOut, Memoizer.defaultExpiration());

    /**
     * Creates a new FreeBsdVirtualMemory instance.
     *
     * @param freeBsdGlobalMemory the free bsd global memory
     */
    FreeBsdVirtualMemory(FreeBsdGlobalMemory freeBsdGlobalMemory) {
        this.global = freeBsdGlobalMemory;
    }

    /**
     * Queries the swap used.
     *
     * @return the query swap used result
     */
    private static long querySwapUsed() {
        return sumSwapUsed(Executor.runNative("swapinfo -k"));
    }

    /**
     * Aggregates the used swap column from {@code swapinfo -k} output.
     *
     * @param swapInfoLines the swapinfo output lines
     * @return the total used swap in bytes
     */
    static long sumSwapUsed(List<String> swapInfoLines) {
        long sum = 0L;
        long totalRow = -1L;
        for (String line : swapInfoLines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("Device")) {
                continue;
            }
            if (trimmed.startsWith("Total")) {
                totalRow = parseSwapUsed(trimmed);
                continue;
            }
            sum += parseSwapUsed(trimmed);
        }
        return totalRow >= 0L ? totalRow : sum;
    }

    /**
     * Parses the used swap column from a single swapinfo data row.
     *
     * @param swapInfoRow the swapinfo row
     * @return the used swap in bytes
     */
    static long parseSwapUsed(String swapInfoRow) {
        String[] split = Pattern.SPACES_PATTERN.split(swapInfoRow);
        if (split.length < 5) {
            return 0L;
        }
        return Parsing.parseLongOrDefault(split[2], 0L) << 10;
    }

    /**
     * Queries the swap total.
     *
     * @return the query swap total result
     */
    private static long querySwapTotal() {
        return BsdSysctlKit.sysctl("vm.swap_total", 0L);
    }

    /**
     * Queries the pages in.
     *
     * @return the query pages in result
     */
    private static long queryPagesIn() {
        return BsdSysctlKit.sysctl("vm.stats.vm.v_swappgsin", 0L);
    }

    /**
     * Queries the pages out.
     *
     * @return the query pages out result
     */
    private static long queryPagesOut() {
        return BsdSysctlKit.sysctl("vm.stats.vm.v_swappgsout", 0L);
    }

    /**
     * Returns the swap used.
     *
     * @return the get swap used result
     */
    @Override
    public long getSwapUsed() {
        return used.get();
    }

    /**
     * Returns the swap total.
     *
     * @return the get swap total result
     */
    @Override
    public long getSwapTotal() {
        return total.get();
    }

    /**
     * Returns the virtual max.
     *
     * @return the get virtual max result
     */
    @Override
    public long getVirtualMax() {
        return this.global.getTotal() + getSwapTotal();
    }

    /**
     * Returns the virtual in use.
     *
     * @return the get virtual in use result
     */
    @Override
    public long getVirtualInUse() {
        return this.global.getTotal() - this.global.getAvailable() + getSwapUsed();
    }

    /**
     * Returns the swap pages in.
     *
     * @return the get swap pages in result
     */
    @Override
    public long getSwapPagesIn() {
        return pagesIn.get();
    }

    /**
     * Returns the swap pages out.
     *
     * @return the get swap pages out result
     */
    @Override
    public long getSwapPagesOut() {
        return pagesOut.get();
    }

}
