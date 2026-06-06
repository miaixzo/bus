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
package org.miaixz.bus.health.unix.openbsd.hardware;

import java.util.function.Supplier;

import com.sun.jna.Memory;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.VirtualMemory;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGlobalMemory;
import org.miaixz.bus.health.unix.shared.jna.OpenBsdLibc;
import org.miaixz.bus.health.unix.openbsd.OpenBsdSysctlKit;

/**
 * Memory obtained by sysctl vm.stats
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class OpenBsdGlobalMemory extends AbstractGlobalMemory {

    /**
     * The available value.
     */
    private final Supplier<Long> available = Memoizer
            .memoize(OpenBsdGlobalMemory::queryAvailable, Memoizer.defaultExpiration());

    /**
     * The total value.
     */
    private final Supplier<Long> total = Memoizer.memoize(OpenBsdGlobalMemory::queryPhysMem);

    /**
     * The pageSize value.
     */
    private final Supplier<Long> pageSize = Memoizer.memoize(OpenBsdGlobalMemory::queryPageSize);

    /**
     * The vm value.
     */
    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    /**
     * Queries the available.
     *
     * @return the query available result
     */
    private static long queryAvailable() {
        long free = 0L;
        long inactive = 0L;
        for (String line : Executor.runNative("vmstat -s")) {
            if (line.endsWith("pages free")) {
                free = Parsing.getFirstIntValue(line);
            } else if (line.endsWith("pages inactive")) {
                inactive = Parsing.getFirstIntValue(line);
            }
        }
        int[] mib = new int[3];
        mib[0] = OpenBsdLibc.CTL_VFS;
        mib[1] = OpenBsdLibc.VFS_GENERIC;
        mib[2] = OpenBsdLibc.VFS_BCACHESTAT;
        try (Memory m = OpenBsdSysctlKit.sysctl(mib)) {
            OpenBsdLibc.Bcachestats cache = new OpenBsdLibc.Bcachestats(m);
            return (cache.numbufpages + free + inactive);
        }
    }

    /**
     * Queries the phys mem.
     *
     * @return the query phys mem result
     */
    private static long queryPhysMem() {
        return OpenBsdSysctlKit.sysctl("hw.physmem", 0L);
    }

    /**
     * Queries the page size.
     *
     * @return the query page size result
     */
    private static long queryPageSize() {
        return OpenBsdSysctlKit.sysctl("hw.pagesize", 4096L);
    }

    /**
     * Returns the available.
     *
     * @return the get available result
     */
    @Override
    public long getAvailable() {
        return available.get() * getPageSize();
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
     * Creates the virtual memory.
     *
     * @return the create virtual memory result
     */
    private VirtualMemory createVirtualMemory() {
        return new OpenBsdVirtualMemory(this);
    }

}
