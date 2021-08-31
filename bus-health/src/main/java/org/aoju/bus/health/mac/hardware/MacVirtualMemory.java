/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2021 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.mac.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.VMStatistics;
import com.sun.jna.platform.mac.SystemB.XswUsage;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.tuple.Pair;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.hardware.AbstractVirtualMemory;
import org.aoju.bus.health.mac.SysctlKit;
import org.aoju.bus.logger.Logger;

import java.util.function.Supplier;

/**
 * Memory obtained by host_statistics (vm_stat) and sysctl.
 *
 * @author Kimi Liu
 * @version 6.2.8
 * @since JDK 1.8+
 */
@ThreadSafe
final class MacVirtualMemory extends AbstractVirtualMemory {

    private final MacGlobalMemory global;

    private final Supplier<Pair<Long, Long>> usedTotal = Memoize.memoize(MacVirtualMemory::querySwapUsage, Memoize.defaultExpiration());

    private final Supplier<Pair<Long, Long>> inOut = Memoize.memoize(MacVirtualMemory::queryVmStat, Memoize.defaultExpiration());

    /**
     * Constructor for MacVirtualMemory.
     *
     * @param macGlobalMemory The parent global memory class instantiating this
     */
    MacVirtualMemory(MacGlobalMemory macGlobalMemory) {
        this.global = macGlobalMemory;
    }

    private static Pair<Long, Long> querySwapUsage() {
        long swapUsed = 0L;
        long swapTotal = 0L;
        XswUsage xswUsage = new XswUsage();
        if (SysctlKit.sysctl("vm.swapusage", xswUsage)) {
            swapUsed = xswUsage.xsu_used;
            swapTotal = xswUsage.xsu_total;
        }
        return Pair.of(swapUsed, swapTotal);
    }

    private static Pair<Long, Long> queryVmStat() {
        long swapPagesIn = 0L;
        long swapPagesOut = 0L;
        VMStatistics vmStats = new VMStatistics();
        if (0 == SystemB.INSTANCE.host_statistics(SystemB.INSTANCE.mach_host_self(), SystemB.HOST_VM_INFO, vmStats,
                new IntByReference(vmStats.size() / SystemB.INT_SIZE))) {
            swapPagesIn = Builder.unsignedIntToLong(vmStats.pageins);
            swapPagesOut = Builder.unsignedIntToLong(vmStats.pageouts);
        } else {
            Logger.error("Failed to get host VM info. Error code: {}", Native.getLastError());
        }
        return Pair.of(swapPagesIn, swapPagesOut);
    }

    @Override
    public long getSwapUsed() {
        return usedTotal.get().getLeft();
    }

    @Override
    public long getSwapTotal() {
        return usedTotal.get().getRight();
    }

    @Override
    public long getVirtualMax() {
        return this.global.getTotal() + getSwapTotal();
    }

    @Override
    public long getVirtualInUse() {
        return this.global.getTotal() - this.global.getAvailable() + getSwapUsed();
    }

    @Override
    public long getSwapPagesIn() {
        return inOut.get().getLeft();
    }

    @Override
    public long getSwapPagesOut() {
        return inOut.get().getRight();
    }

}
