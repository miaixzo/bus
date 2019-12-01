/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.mac;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.VMStatistics;
import com.sun.jna.platform.mac.SystemB.XswUsage;
import com.sun.jna.ptr.IntByReference;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.mac.SysctlUtils;
import org.aoju.bus.health.hardware.AbstractVirtualMemory;
import org.aoju.bus.logger.Logger;

import java.util.function.Supplier;

/**
 * Memory obtained by host_statistics (vm_stat) and sysctl.
 *
 * @author Kimi Liu
 * @version 5.2.8
 * @since JDK 1.8+
 */
public class MacVirtualMemory extends AbstractVirtualMemory {

    private final Supplier<SwapUsage> swapUsage = Memoizer.memoize(this::querySwapUsage, Memoizer.defaultExpiration());

    private final Supplier<VmStat> vmStat = Memoizer.memoize(this::queryVmStat, Memoizer.defaultExpiration());

    @Override
    public long getSwapUsed() {
        return swapUsage.get().used;
    }

    @Override
    public long getSwapTotal() {
        return swapUsage.get().total;
    }

    @Override
    public long getSwapPagesIn() {
        return vmStat.get().pagesIn;
    }

    @Override
    public long getSwapPagesOut() {
        return vmStat.get().pagesOut;
    }

    private SwapUsage querySwapUsage() {
        long swapUsed = 0L;
        long swapTotal = 0L;
        XswUsage xswUsage = new XswUsage();
        if (SysctlUtils.sysctl("vm.swapusage", xswUsage)) {
            swapUsed = xswUsage.xsu_used;
            swapTotal = xswUsage.xsu_total;
        }
        return new SwapUsage(swapTotal, swapUsed);
    }

    private VmStat queryVmStat() {
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
        return new VmStat(swapPagesIn, swapPagesOut);
    }

    private static final class SwapUsage {
        private final long total;
        private final long used;

        private SwapUsage(long total, long used) {
            this.total = total;
            this.used = used;
        }
    }

    private static final class VmStat {
        private final long pagesIn;
        private final long pagesOut;

        private VmStat(long pagesIn, long pagesOut) {
            this.pagesIn = pagesIn;
            this.pagesOut = pagesOut;
        }
    }
}
