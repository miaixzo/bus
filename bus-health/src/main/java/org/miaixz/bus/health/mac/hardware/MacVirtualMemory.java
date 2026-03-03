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
package org.miaixz.bus.health.mac.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.mac.SysctlKit;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;

/**
 * <p>
 * MacVirtualMemory class.
 * </p>
 * Memory obtained by host_statistics (vm_stat) and sysctl.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class MacVirtualMemory extends AbstractVirtualMemory {

    private final MacGlobalMemory global;

    private final Supplier<Pair<Long, Long>> usedTotal = Memoizer
            .memoize(MacVirtualMemory::querySwapUsage, Memoizer.defaultExpiration());

    private final Supplier<Pair<Long, Long>> inOut = Memoizer
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
                Logger.error("Failed to get host VM info. Error code: {}", Native.getLastError());
            }
        }
        return Pair.of(swapPagesIn, swapPagesOut);
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getSwapUsed() {
        return usedTotal.get().getLeft();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getSwapTotal() {
        return usedTotal.get().getRight();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getVirtualMax() {
        return this.global.getTotal() + getSwapTotal();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getVirtualInUse() {
        return this.global.getTotal() - this.global.getAvailable() + getSwapUsed();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getSwapPagesIn() {
        return inOut.get().getLeft();
    }

    /**
     * Description inherited from parent class or interface.
     */
    @Override
    public long getSwapPagesOut() {
        return inOut.get().getRight();
    }

}
