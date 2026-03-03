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
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import java.util.function.Supplier;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;

/**
 * Memory obtained by swapinfo
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class FreeBsdVirtualMemory extends AbstractVirtualMemory {

    private final FreeBsdGlobalMemory global;

    private final Supplier<Long> used = Memoizer
            .memoize(FreeBsdVirtualMemory::querySwapUsed, Memoizer.defaultExpiration());

    private final Supplier<Long> total = Memoizer
            .memoize(FreeBsdVirtualMemory::querySwapTotal, Memoizer.defaultExpiration());

    private final Supplier<Long> pagesIn = Memoizer
            .memoize(FreeBsdVirtualMemory::queryPagesIn, Memoizer.defaultExpiration());

    private final Supplier<Long> pagesOut = Memoizer
            .memoize(FreeBsdVirtualMemory::queryPagesOut, Memoizer.defaultExpiration());

    FreeBsdVirtualMemory(FreeBsdGlobalMemory freeBsdGlobalMemory) {
        this.global = freeBsdGlobalMemory;
    }

    private static long querySwapUsed() {
        String swapInfo = Executor.getAnswerAt("swapinfo -k", 1);
        String[] split = Pattern.SPACES_PATTERN.split(swapInfo);
        if (split.length < 5) {
            return 0L;
        }
        return Parsing.parseLongOrDefault(split[2], 0L) << 10;
    }

    private static long querySwapTotal() {
        return BsdSysctlKit.sysctl("vm.swap_total", 0L);
    }

    private static long queryPagesIn() {
        return BsdSysctlKit.sysctl("vm.stats.vm.v_swappgsin", 0L);
    }

    private static long queryPagesOut() {
        return BsdSysctlKit.sysctl("vm.stats.vm.v_swappgsout", 0L);
    }

    @Override
    public long getSwapUsed() {
        return used.get();
    }

    @Override
    public long getSwapTotal() {
        return total.get();
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
        return pagesIn.get();
    }

    @Override
    public long getSwapPagesOut() {
        return pagesOut.get();
    }

}
