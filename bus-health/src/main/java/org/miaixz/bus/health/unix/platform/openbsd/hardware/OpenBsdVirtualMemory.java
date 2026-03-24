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
package org.miaixz.bus.health.unix.platform.openbsd.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;

/**
 * Memory info on OpenBSD
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class OpenBsdVirtualMemory extends AbstractVirtualMemory {

    private final OpenBsdGlobalMemory global;

    private final Supplier<Triplet<Integer, Integer, Integer>> usedTotalPgin = Memoizer
            .memoize(OpenBsdVirtualMemory::queryVmstat, Memoizer.defaultExpiration());
    private final Supplier<Integer> pgout = Memoizer
            .memoize(OpenBsdVirtualMemory::queryUvm, Memoizer.defaultExpiration());

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
