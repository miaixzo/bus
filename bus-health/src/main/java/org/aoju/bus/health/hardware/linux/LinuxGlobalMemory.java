/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 ********************************************************************************/
package org.aoju.bus.health.hardware.linux;

import com.sun.jna.platform.linux.LibC;
import com.sun.jna.platform.linux.LibC.Sysinfo;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.linux.ProcUtils;
import org.aoju.bus.health.hardware.AbstractGlobalMemory;
import org.aoju.bus.health.hardware.VirtualMemory;
import org.aoju.bus.logger.Logger;

import java.util.List;
import java.util.function.Supplier;

/**
 * Memory obtained by /proc/meminfo and sysinfo.totalram
 *
 * @author Kimi Liu
 * @version 5.8.2
 * @since JDK 1.8+
 */
public class LinuxGlobalMemory extends AbstractGlobalMemory {

    private final Supplier<MemInfo> memInfo = Memoizer.memoize(this::readMemInfo, Memoizer.defaultExpiration());

    private final Supplier<Long> pageSize = Memoizer.memoize(this::queryPageSize);

    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    @Override
    public long getAvailable() {
        return memInfo.get().available;
    }

    @Override
    public long getTotal() {
        return memInfo.get().total;
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
        try {
            Sysinfo info = new Sysinfo();
            if (0 == LibC.INSTANCE.sysinfo(info)) {
                return info.mem_unit;
            }
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            Logger.debug("Failed to get sysinfo. {}", e);
        }
        return Builder.parseLongOrDefault(Command.getFirstAnswer("getconf PAGE_SIZE"), 4096L);
    }

    /**
     * Updates instance variables from reading /proc/meminfo. While most of the
     * information is available in the sysinfo structure, the most accurate
     * calculation of MemAvailable is only available from reading this pseudo-file.
     * The maintainers of the Linux Kernel have indicated this location will be kept
     * up to date if the calculation changes: see
     * https://git.kernel.org/cgit/linux/kernel/git/torvalds/linux.git/commit/?
     * id=34e431b0ae398fc54ea69ff85ec700722c9da773
     * <p>
     * Internally, reading /proc/meminfo is faster than sysinfo because it only
     * spends time populating the memory components of the sysinfo structure.
     */
    private MemInfo readMemInfo() {
        long memFree = 0L;
        long activeFile = 0L;
        long inactiveFile = 0L;
        long sReclaimable = 0L;

        long memTotal = 0L;
        long memAvailable;

        List<String> procMemInfo = Builder.readFile(ProcUtils.getProcPath() + "/meminfo");
        for (String checkLine : procMemInfo) {
            String[] memorySplit = Builder.whitespaces.split(checkLine);
            if (memorySplit.length > 1) {
                switch (memorySplit[0]) {
                    case "MemTotal:":
                        memTotal = parseMeminfo(memorySplit);
                        break;
                    case "MemAvailable:":
                        memAvailable = parseMeminfo(memorySplit);
                        // We're done!
                        return new MemInfo(memTotal, memAvailable);
                    case "MemFree:":
                        memFree = parseMeminfo(memorySplit);
                        break;
                    case "Active(file):":
                        activeFile = parseMeminfo(memorySplit);
                        break;
                    case "Inactive(file):":
                        inactiveFile = parseMeminfo(memorySplit);
                        break;
                    case "SReclaimable:":
                        sReclaimable = parseMeminfo(memorySplit);
                        break;
                    default:
                        // do nothing with other lines
                        break;
                }
            }
        }
        // We didn't find MemAvailable so we estimate from other fields
        return new MemInfo(memTotal, memFree + activeFile + inactiveFile + sReclaimable);
    }

    /**
     * Parses lines from the display of /proc/meminfo
     *
     * @param memorySplit Array of Strings representing the 3 columns of /proc/meminfo
     * @return value, multiplied by 1024 if kB is specified
     */
    private long parseMeminfo(String[] memorySplit) {
        if (memorySplit.length < 2) {
            return 0L;
        }
        long memory = Builder.parseLongOrDefault(memorySplit[1], 0L);
        if (memorySplit.length > 2 && "kB".equals(memorySplit[2])) {
            memory *= 1024;
        }
        return memory;
    }

    private VirtualMemory createVirtualMemory() {
        return new LinuxVirtualMemory();
    }

    private static final class MemInfo {
        private final long total;
        private final long available;

        private MemInfo(long total, long available) {
            this.total = total;
            this.available = available;
        }
    }
}
