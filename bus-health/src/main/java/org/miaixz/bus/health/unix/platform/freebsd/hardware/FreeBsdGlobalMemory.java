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

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.VirtualMemory;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGlobalMemory;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;

/**
 * Memory obtained by sysctl vm.stats
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class FreeBsdGlobalMemory extends AbstractGlobalMemory {

    /**
     * The total value.
     */
    private final Supplier<Long> total = Memoizer.memoize(FreeBsdGlobalMemory::queryPhysMem);

    /**
     * The pageSize value.
     */
    private final Supplier<Long> pageSize = Memoizer.memoize(FreeBsdGlobalMemory::queryPageSize);

    /**
     * The available value.
     */
    private final Supplier<Long> available = Memoizer.memoize(this::queryVmStats, Memoizer.defaultExpiration());

    /**
     * The vm value.
     */
    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    /**
     * Queries the phys mem.
     *
     * @return the query phys mem result
     */
    private static long queryPhysMem() {
        return BsdSysctlKit.sysctl("hw.physmem", 0L);
    }

    /**
     * Queries the page size.
     *
     * @return the query page size result
     */
    private static long queryPageSize() {
        // sysctl hw.pagesize doesn't work on FreeBSD 13
        return Parsing.parseLongOrDefault(Executor.getFirstAnswer("getconf PAGESIZE"), 4096L);
    }

    /**
     * Returns the available.
     *
     * @return the get available result
     */
    @Override
    public long getAvailable() {
        return available.get();
    }

    /**
     * Returns the total.
     *
     * @return the get total result
     */
    @Override
    public long getTotal() {
        return total.get();
    }

    /**
     * Returns the page size.
     *
     * @return the get page size result
     */
    @Override
    public long getPageSize() {
        return pageSize.get();
    }

    /**
     * Returns the virtual memory.
     *
     * @return the get virtual memory result
     */
    @Override
    public VirtualMemory getVirtualMemory() {
        return vm.get();
    }

    /**
     * Queries the vm stats.
     *
     * @return the query vm stats result
     */
    private long queryVmStats() {
        // cached removed in FreeBSD 12 but was always set to 0
        long inactive = Integer.toUnsignedLong(BsdSysctlKit.sysctl("vm.stats.vm.v_inactive_count", 0));
        long free = Integer.toUnsignedLong(BsdSysctlKit.sysctl("vm.stats.vm.v_free_count", 0));
        return (inactive + free) * getPageSize();
    }

    /**
     * Creates the virtual memory.
     *
     * @return the create virtual memory result
     */
    private VirtualMemory createVirtualMemory() {
        return new FreeBsdVirtualMemory(this);
    }

}
