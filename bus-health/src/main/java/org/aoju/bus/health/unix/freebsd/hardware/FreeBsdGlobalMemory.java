/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.unix.freebsd.hardware;

import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.health.builtin.hardware.AbstractGlobalMemory;
import org.aoju.bus.health.builtin.hardware.VirtualMemory;
import org.aoju.bus.health.unix.freebsd.BsdSysctl;

import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.defaultExpiration;
import static org.aoju.bus.health.Memoize.memoize;

/**
 * Memory obtained by sysctl vm.stats
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
@ThreadSafe
final class FreeBsdGlobalMemory extends AbstractGlobalMemory {

    private final Supplier<Long> total = memoize(FreeBsdGlobalMemory::queryPhysMem);
    private final Supplier<Long> pageSize = memoize(FreeBsdGlobalMemory::queryPageSize);
    private final Supplier<Long> available = memoize(this::queryVmStats, defaultExpiration());
    private final Supplier<VirtualMemory> vm = memoize(this::createVirtualMemory);

    private static long queryPhysMem() {
        return BsdSysctl.sysctl("hw.physmem", 0L);
    }

    private static long queryPageSize() {
        return BsdSysctl.sysctl("hw.pagesize", 4096L);
    }

    @Override
    public long getAvailable() {
        return available.get();
    }

    @Override
    public long getTotal() {
        return total.get();
    }

    @Override
    public long getPageSize() {
        return pageSize.get();
    }

    @Override
    public VirtualMemory getVirtualMemory() {
        return vm.get();
    }

    private long queryVmStats() {
        long inactive = BsdSysctl.sysctl("vm.stats.vm.v_inactive_count", 0L);
        long cache = BsdSysctl.sysctl("vm.stats.vm.v_cache_count", 0L);
        long free = BsdSysctl.sysctl("vm.stats.vm.v_free_count", 0L);
        return (inactive + cache + free) * getPageSize();
    }

    private VirtualMemory createVirtualMemory() {
        return new FreeBsdVirtualMemory(this);
    }

}
