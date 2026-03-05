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
package org.miaixz.bus.health.unix.platform.solaris.hardware;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;
import org.miaixz.bus.health.unix.platform.solaris.driver.kstat.SystemPages;

/**
 * Memory obtained by kstat and swap
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class SolarisVirtualMemory extends AbstractVirtualMemory {

    private static final Pattern SWAP_INFO = Pattern.compile(".+\\s(\\d+)K\\s+(\\d+)K$");

    private final SolarisGlobalMemory global;

    // Physical
    private final Supplier<Pair<Long, Long>> availTotal = Memoizer
            .memoize(SystemPages::queryAvailableTotal, Memoizer.defaultExpiration());

    // Swap
    private final Supplier<Pair<Long, Long>> usedTotal = Memoizer
            .memoize(SolarisVirtualMemory::querySwapInfo, Memoizer.defaultExpiration());

    private final Supplier<Long> pagesIn = Memoizer
            .memoize(SolarisVirtualMemory::queryPagesIn, Memoizer.defaultExpiration());

    private final Supplier<Long> pagesOut = Memoizer
            .memoize(SolarisVirtualMemory::queryPagesOut, Memoizer.defaultExpiration());

    /**
     * Constructor for SolarisVirtualMemory.
     *
     * @param solarisGlobalMemory The parent global memory class instantiating this
     */
    SolarisVirtualMemory(SolarisGlobalMemory solarisGlobalMemory) {
        this.global = solarisGlobalMemory;
    }

    private static long queryPagesIn() {
        long swapPagesIn = 0L;
        for (String s : Executor.runNative("kstat -p cpu_stat:::pgswapin")) {
            swapPagesIn += Parsing.parseLastLong(s, 0L);
        }
        return swapPagesIn;
    }

    private static long queryPagesOut() {
        long swapPagesOut = 0L;
        for (String s : Executor.runNative("kstat -p cpu_stat:::pgswapout")) {
            swapPagesOut += Parsing.parseLastLong(s, 0L);
        }
        return swapPagesOut;
    }

    private static Pair<Long, Long> querySwapInfo() {
        long swapTotal = 0L;
        long swapUsed = 0L;
        String swap = Executor.getAnswerAt("swap -lk", 1);
        Matcher m = SWAP_INFO.matcher(swap);
        if (m.matches()) {
            swapTotal = Parsing.parseLongOrDefault(m.group(1), 0L) << 10;
            swapUsed = swapTotal - (Parsing.parseLongOrDefault(m.group(2), 0L) << 10);
        }
        return Pair.of(swapUsed, swapTotal);
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
        return this.global.getPageSize() * availTotal.get().getRight() + getSwapTotal();
    }

    @Override
    public long getVirtualInUse() {
        return this.global.getPageSize() * (availTotal.get().getRight() - availTotal.get().getLeft()) + getSwapUsed();
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
