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
package org.miaixz.bus.health.linux.hardware;

import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractVirtualMemory;
import org.miaixz.bus.health.linux.ProcPath;

/**
 * Memory obtained by /proc/meminfo and /proc/vmstat
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class LinuxVirtualMemory extends AbstractVirtualMemory {

    /**
     * The global value.
     */
    private final LinuxGlobalMemory global;

    /**
     * The usedTotalCommitLim value.
     */
    private final Supplier<Triplet<Long, Long, Long>> usedTotalCommitLim = Memoizer
            .memoize(LinuxVirtualMemory::queryMemInfo, Memoizer.defaultExpiration());

    /**
     * The inOut value.
     */
    private final Supplier<Pair<Long, Long>> inOut = Memoizer
            .memoize(LinuxVirtualMemory::queryVmStat, Memoizer.defaultExpiration());

    /**
     * Constructor for LinuxVirtualMemory.
     *
     * @param linuxGlobalMemory The parent global memory class instantiating this
     */
    LinuxVirtualMemory(LinuxGlobalMemory linuxGlobalMemory) {
        this.global = linuxGlobalMemory;
    }

    /**
     * Queries the mem info.
     *
     * @return the query mem info result
     */
    private static Triplet<Long, Long, Long> queryMemInfo() {
        long swapFree = 0L;
        long swapTotal = 0L;
        long commitLimit = 0L;

        List<String> procMemInfo = Builder.readFile(ProcPath.MEMINFO);
        for (String checkLine : procMemInfo) {
            String[] memorySplit = Pattern.SPACES_PATTERN.split(checkLine);
            if (memorySplit.length > 1) {
                switch (memorySplit[0]) {
                    case "SwapTotal:":
                        swapTotal = parseMeminfo(memorySplit);
                        break;

                    case "SwapFree:":
                        swapFree = parseMeminfo(memorySplit);
                        break;

                    case "CommitLimit:":
                        commitLimit = parseMeminfo(memorySplit);
                        break;

                    default:
                        // do nothing with other lines
                        break;
                }
            }
        }
        return Triplet.of(swapTotal - swapFree, swapTotal, commitLimit);
    }

    /**
     * Queries the vm stat.
     *
     * @return the query vm stat result
     */
    private static Pair<Long, Long> queryVmStat() {
        long swapPagesIn = 0L;
        long swapPagesOut = 0L;
        List<String> procVmStat = Builder.readFile(ProcPath.VMSTAT);
        for (String checkLine : procVmStat) {
            String[] memorySplit = Pattern.SPACES_PATTERN.split(checkLine);
            if (memorySplit.length > 1) {
                switch (memorySplit[0]) {
                    case "pswpin":
                        swapPagesIn = Parsing.parseLongOrDefault(memorySplit[1], 0L);
                        break;

                    case "pswpout":
                        swapPagesOut = Parsing.parseLongOrDefault(memorySplit[1], 0L);
                        break;

                    default:
                        // do nothing with other lines
                        break;
                }
            }
        }
        return Pair.of(swapPagesIn, swapPagesOut);
    }

    /**
     * Parses lines from the display of /proc/meminfo
     *
     * @param memorySplit Array of Strings representing the 3 columns of /proc/meminfo
     * @return value, multiplied by 1024 if kB is specified
     */
    private static long parseMeminfo(String[] memorySplit) {
        if (memorySplit.length < 2) {
            return 0L;
        }
        long memory = Parsing.parseLongOrDefault(memorySplit[1], 0L);
        if (memorySplit.length > 2 && "kB".equals(memorySplit[2])) {
            memory *= 1024;
        }
        return memory;
    }

    /**
     * Returns the swap used.
     *
     * @return the get swap used result
     */
    @Override
    public long getSwapUsed() {
        return usedTotalCommitLim.get().getLeft();
    }

    /**
     * Returns the swap total.
     *
     * @return the get swap total result
     */
    @Override
    public long getSwapTotal() {
        return usedTotalCommitLim.get().getMiddle();
    }

    /**
     * Returns the virtual max.
     *
     * @return the get virtual max result
     */
    @Override
    public long getVirtualMax() {
        return usedTotalCommitLim.get().getRight();
    }

    /**
     * Returns the virtual in use.
     *
     * @return the get virtual in use result
     */
    @Override
    public long getVirtualInUse() {
        return this.global.getTotal() - this.global.getAvailable() + getSwapUsed();
    }

    /**
     * Returns the swap pages in.
     *
     * @return the get swap pages in result
     */
    @Override
    public long getSwapPagesIn() {
        return inOut.get().getLeft();
    }

    /**
     * Returns the swap pages out.
     *
     * @return the get swap pages out result
     */
    @Override
    public long getSwapPagesOut() {
        return inOut.get().getRight();
    }

}
