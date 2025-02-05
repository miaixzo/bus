/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2025 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.platform.openbsd.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.VirtualMemory;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGlobalMemory;
import org.miaixz.bus.health.unix.jna.OpenBsdLibc;
import org.miaixz.bus.health.unix.platform.openbsd.OpenBsdSysctlKit;

import com.sun.jna.Memory;

/**
 * Memory obtained by sysctl vm.stats
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class OpenBsdGlobalMemory extends AbstractGlobalMemory {

    private final Supplier<Long> available = Memoizer.memoize(OpenBsdGlobalMemory::queryAvailable,
            Memoizer.defaultExpiration());

    private final Supplier<Long> total = Memoizer.memoize(OpenBsdGlobalMemory::queryPhysMem);

    private final Supplier<Long> pageSize = Memoizer.memoize(OpenBsdGlobalMemory::queryPageSize);

    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

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

    private static long queryPhysMem() {
        return OpenBsdSysctlKit.sysctl("hw.physmem", 0L);
    }

    private static long queryPageSize() {
        return OpenBsdSysctlKit.sysctl("hw.pagesize", 4096L);
    }

    @Override
    public long getAvailable() {
        return available.get() * getPageSize();
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

    private VirtualMemory createVirtualMemory() {
        return new OpenBsdVirtualMemory(this);
    }
}
