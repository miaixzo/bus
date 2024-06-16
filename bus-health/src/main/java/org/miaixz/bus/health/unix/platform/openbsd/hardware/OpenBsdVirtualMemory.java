/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
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

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;

import java.util.function.Supplier;

/**
 * Memory info on OpenBSD
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class OpenBsdVirtualMemory extends AbstractVirtualMemory {

    private final OpenBsdGlobalMemory global;

    private final Supplier<Triplet<Integer, Integer, Integer>> usedTotalPgin = Memoizer.memoize(
            OpenBsdVirtualMemory::queryVmstat, Memoizer.defaultExpiration());
    private final Supplier<Integer> pgout = Memoizer.memoize(OpenBsdVirtualMemory::queryUvm, Memoizer.defaultExpiration());

    OpenBsdVirtualMemory(OpenBsdGlobalMemory freeBsdGlobalMemory) {
        this.global = freeBsdGlobalMemory;
    }

    private static Triplet<Integer, Integer, Integer> queryVmstat() {
        int used = 0;
        int total = 0;
        int swapIn = 0;
        for (String line : Executor.runNative("vmstat -s")) {
            if (line.contains("swap pages in use")) {
                used = Parsing.getFirstIntValue(line);
            } else if (line.contains("swap pages")) {
                total = Parsing.getFirstIntValue(line);
            } else if (line.contains("pagein operations")) {
                swapIn = Parsing.getFirstIntValue(line);
            }
        }
        return Triplet.of(used, total, swapIn);
    }

    private static int queryUvm() {
        for (String line : Executor.runNative("systat -ab uvm")) {
            if (line.contains("pdpageouts")) {
                return Parsing.getFirstIntValue(line);
            }
        }
        return 0;
    }

    @Override
    public long getSwapUsed() {
        return usedTotalPgin.get().getLeft() * global.getPageSize();
    }

    @Override
    public long getSwapTotal() {
        return usedTotalPgin.get().getMiddle() * global.getPageSize();
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
        return usedTotalPgin.get().getRight() * global.getPageSize();
    }

    @Override
    public long getSwapPagesOut() {
        return pgout.get() * global.getPageSize();
    }
}
