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
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.VirtualMemory;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGlobalMemory;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.software.LinuxOperatingSystem;

/**
 * Memory obtained by /proc/meminfo and sysinfo.totalram
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class LinuxGlobalMemory extends AbstractGlobalMemory {

    private static final long PAGE_SIZE = LinuxOperatingSystem.getPageSize();

    private final Supplier<Pair<Long, Long>> availTotal = Memoizer
            .memoize(LinuxGlobalMemory::readMemInfo, Memoizer.defaultExpiration());

    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    /**
     * Updates instance variables from reading /proc/meminfo. While most of the information is available in the sysinfo
     * structure, the most accurate calculation of MemAvailable is only available from reading this pseudo-file. The
     * maintainers of the Linux Kernel have indicated this location will be kept up to date if the calculation changes:
     * see https://git.kernel.org/cgit/linux/kernel/git/torvalds/linux.git/commit/?
     * id=34e431b0ae398fc54ea69ff85ec700722c9da773
     * <p>
     * Internally, reading /proc/meminfo is faster than sysinfo because it only spends time populating the memory
     * components of the sysinfo structure.
     *
     * @return A pair containing available and total memory in bytes
     */
    private static Pair<Long, Long> readMemInfo() {
        long memFree = 0L;
        long activeFile = 0L;
        long inactiveFile = 0L;
        long sReclaimable = 0L;

        long memTotal = 0L;
        long memAvailable;

        List<String> procMemInfo = Builder.readFile(ProcPath.MEMINFO);
        for (String checkLine : procMemInfo) {
            String[] memorySplit = Pattern.SPACES_PATTERN.split(checkLine, 2);
            if (memorySplit.length > 1) {
                switch (memorySplit[0]) {
                    case "MemTotal:":
                        memTotal = Parsing.parseDecimalMemorySizeToBinary(memorySplit[1]);
                        break;

                    case "MemAvailable:":
                        memAvailable = Parsing.parseDecimalMemorySizeToBinary(memorySplit[1]);
                        // We're done!
                        return Pair.of(memAvailable, memTotal);

                    case "MemFree:":
                        memFree = Parsing.parseDecimalMemorySizeToBinary(memorySplit[1]);
                        break;

                    case "Active(file):":
                        activeFile = Parsing.parseDecimalMemorySizeToBinary(memorySplit[1]);
                        break;

                    case "Inactive(file):":
                        inactiveFile = Parsing.parseDecimalMemorySizeToBinary(memorySplit[1]);
                        break;

                    case "SReclaimable:":
                        sReclaimable = Parsing.parseDecimalMemorySizeToBinary(memorySplit[1]);
                        break;

                    default:
                        // do nothing with other lines
                        break;
                }
            }
        }
        // We didn't find MemAvailable so we estimate from other fields
        return Pair.of(memFree + activeFile + inactiveFile + sReclaimable, memTotal);
    }

    @Override
    public long getAvailable() {
        return availTotal.get().getLeft();
    }

    @Override
    public long getTotal() {
        return availTotal.get().getRight();
    }

    @Override
    public long getPageSize() {
        return PAGE_SIZE;
    }

    @Override
    public VirtualMemory getVirtualMemory() {
        return vm.get();
    }

    private VirtualMemory createVirtualMemory() {
        return new LinuxVirtualMemory(this);
    }

}
