/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.health.hardware.unix.solaris;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.unix.solaris.KstatUtils;
import org.aoju.bus.health.common.unix.solaris.KstatUtils.KstatChain;
import org.aoju.bus.health.hardware.AbstractGlobalMemory;
import org.aoju.bus.health.hardware.VirtualMemory;

import java.util.function.Supplier;

/**
 * Memory obtained by kstat
 *
 * @author Kimi Liu
 * @version 5.5.2
 * @since JDK 1.8+
 */
public class SolarisGlobalMemory extends AbstractGlobalMemory {

    private final Supplier<Long> pageSize = Memoizer.memoize(this::queryPageSize);
    private final Supplier<SystemPages> systemPages = Memoizer.memoize(this::readSystemPages, Memoizer.defaultExpiration());
    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    @Override
    public long getAvailable() {
        return systemPages.get().available;
    }

    @Override
    public long getTotal() {
        return systemPages.get().total;
    }

    @Override
    public long getPageSize() {
        return pageSize.get();
    }

    @Override
    public VirtualMemory getVirtualMemory() {
        return vm.get();
    }

    private long queryPageSize() {
        return Builder.parseLongOrDefault(Command.getFirstAnswer("pagesize"), 4096L);
    }

    private VirtualMemory createVirtualMemory() {
        return new SolarisVirtualMemory();
    }

    private SystemPages readSystemPages() {
        long memAvailable = 0;
        long memTotal = 0;
        // Get first result
        try (KstatChain kc = KstatUtils.openChain()) {
            Kstat ksp = kc.lookup(null, -1, "system_pages");
            // Set values
            if (ksp != null && kc.read(ksp)) {
                memAvailable = KstatUtils.dataLookupLong(ksp, "availrmem") * getPageSize();
                memTotal = KstatUtils.dataLookupLong(ksp, "physmem") * getPageSize();
            }
        }
        return new SystemPages(memTotal, memAvailable);
    }

    private static final class SystemPages {
        private final long total;
        private final long available;

        private SystemPages(long total, long available) {
            this.total = total;
            this.available = available;
        }
    }

}
