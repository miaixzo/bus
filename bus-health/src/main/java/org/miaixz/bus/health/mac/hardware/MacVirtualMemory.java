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
package org.miaixz.bus.health.mac.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.mac.SysctlKit;
import org.miaixz.bus.logger.Logger;

/**
 * <p>
 * MacVirtualMemory class.
 * </p>
 * Memory obtained by host_statistics (vm_stat) and sysctl.
 *
 * @author Kimi Liu
 */
@ThreadSafe
final class MacVirtualMemory extends AbstractVirtualMemory {

    /**
     * The global value.
     */
    private final MacGlobalMemory global;

    /**
     * The usedTotal value.
     */
    private final SupplierX<Pair<Long, Long>> usedTotal = Memoizer
            .memoize(MacVirtualMemory::querySwapUsage, Memoizer.defaultExpiration());

    /**
     * The inOut value.
     */
    private final SupplierX<Pair<Long, Long>> inOut = Memoizer
            .memoize(MacVirtualMemory::queryVmStat, Memoizer.defaultExpiration());

    /**
     * Constructor for MacVirtualMemory.
     *
     * @param macGlobalMemory The parent global memory class instantiating this.
     */
    MacVirtualMemory(MacGlobalMemory macGlobalMemory) {
        this.global = macGlobalMemory;
    }

    /**
     * Queries swap usage statistics.
     *
     * @return A {@link Pair} where the left element is the swap space used and the right element is the total swap
     *         space.
     */
    private static Pair<Long, Long> querySwapUsage() {
        long swapUsed = 0L;
        long swapTotal = 0L;
        try (Struct.CloseableXswUsage xswUsage = new Struct.CloseableXswUsage()) {
            if (SysctlKit.sysctl("vm.swapusage", xswUsage)) {
                swapUsed = xswUsage.xsu_used;
                swapTotal = xswUsage.xsu_total;
            }
        }
        return Pair.of(swapUsed, swapTotal);
    }

    /**
     * Queries virtual memory statistics.
     *
     * @return A {@link Pair} where the left element is the number of swap pages in and the right element is the number
     *         of swap pages out.
     */
    private static Pair<Long, Long> queryVmStat() {
        long swapPagesIn = 0L;
        long swapPagesOut = 0L;
        try (Struct.CloseableVMStatistics vmStats = new Struct.CloseableVMStatistics();
                ByRef.CloseableIntByReference size = new ByRef.CloseableIntByReference(
                        vmStats.size() / SystemB.INT_SIZE)) {
            if (0 == SystemB.INSTANCE
                    .host_statistics(SystemB.INSTANCE.mach_host_self(), SystemB.HOST_VM_INFO, vmStats, size)) {
                swapPagesIn = Parsing.unsignedIntToLong(vmStats.pageins);
                swapPagesOut = Parsing.unsignedIntToLong(vmStats.pageouts);
            } else {
                Logger.error(false, "Health", "Failed to get host VM info. Error code: {}", Native.getLastError());
            }
        }
        return Pair.of(swapPagesIn, swapPagesOut);
    }

    /**
     * The current memory committed to the paging/swap file(s), in bytes
     *
     * @return Swap used in bytes
     */
    @Override
    public long getSwapUsed() {
        return usedTotal.get().getLeft();
    }

    /**
     * The current size of the paging/swap file(s), in bytes. If the paging/swap file can be extended, this is a soft
     * limit.
     *
     * @return Total swap in bytes.
     */
    @Override
    public long getSwapTotal() {
        return usedTotal.get().getRight();
    }

    /**
     * The maximum memory that can be committed by the system without extending the paging file(s), in bytes. Also
     * called the Commit Limit. If the paging/swap file can be extended, this is a soft limit. This is generally equal
     * to the sum of the sizes of physical memory and paging/swap file(s).
     * <p>
     * On Linux, represents the total amount of memory currently available to be allocated on the system based on the
     * overcommit ratio, identified as {@code CommitLimit}. This may be higher or lower than the total size of physical
     * and swap memory depending on system configuration.
     *
     * @return Max Virtual Memory in bytes
     */
    @Override
    public long getVirtualMax() {
        return this.global.getTotal() + getSwapTotal();
    }

    /**
     * The memory currently committed by the system, in bytes. Also called the Commit Total. This is generally equal to
     * the sum of the bytes used of physical memory and paging/swap file(s).
     * <p>
     * On Windows, committing pages changes this value immediately; however, the physical memory is not charged until
     * the pages are accessed, so this value may exceed the sum of used physical and paging/swap file memory.
     *
     * @return Swap used in bytes
     */
    @Override
    public long getVirtualInUse() {
        return this.global.getTotal() - this.global.getAvailable() + getSwapUsed();
    }

    /**
     * Number of pages read from paging/swap file(s) to resolve hard page faults. (Hard page faults occur when a process
     * requires code or data that is not in its working set or elsewhere in physical memory, and must be retrieved from
     * disk.) This property was designed as a primary indicator of the kinds of faults that cause system-wide delays. It
     * includes pages retrieved to satisfy faults in the file system cache (usually requested by applications) and in
     * non-cached mapped memory files.
     *
     * @return Pages swapped in
     */
    @Override
    public long getSwapPagesIn() {
        return inOut.get().getLeft();
    }

    /**
     * Number of pages written to paging/swap file(s) to free up space in physical memory. Pages are written back to
     * disk only if they are changed in physical memory, so they are likely to hold data, not code. A high rate of pages
     * output might indicate a memory shortage. The operating system writes more pages back to disk to free up space
     * when physical memory is in short supply.
     *
     * @return Pages swapped out
     */
    @Override
    public long getSwapPagesOut() {
        return inOut.get().getRight();
    }

}
