/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.mac.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;
import com.sun.jna.platform.mac.SystemB.VMStatistics;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.hardware.AbstractGlobalMemory;
import org.aoju.bus.health.builtin.hardware.PhysicalMemory;
import org.aoju.bus.health.builtin.hardware.VirtualMemory;
import org.aoju.bus.health.mac.Sysctl;
import org.aoju.bus.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.defaultExpiration;
import static org.aoju.bus.health.Memoize.memoize;

/**
 * Memory obtained by host_statistics (vm_stat) and sysctl.
 *
 * @author Kimi Liu
 * @version 5.8.9
 * @since JDK 1.8+
 */
@ThreadSafe
final class MacGlobalMemory extends AbstractGlobalMemory {

    private final Supplier<Long> total = memoize(MacGlobalMemory::queryPhysMem);
    private final Supplier<Long> pageSize = memoize(MacGlobalMemory::queryPageSize);
    private final Supplier<Long> available = memoize(this::queryVmStats, defaultExpiration());
    private final Supplier<VirtualMemory> vm = memoize(this::createVirtualMemory);

    private static long queryPhysMem() {
        return Sysctl.sysctl("hw.memsize", 0L);
    }

    private static long queryPageSize() {
        LongByReference pPageSize = new LongByReference();
        if (0 == SystemB.INSTANCE.host_page_size(SystemB.INSTANCE.mach_host_self(), pPageSize)) {
            return pPageSize.getValue();
        }
        Logger.error("Failed to get host page size. Error code: {}", Native.getLastError());
        return 4098L;
    }

    @Override
    public long getAvailable() {
        return available.get();
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

    @Override
    public PhysicalMemory[] getPhysicalMemory() {
        List<PhysicalMemory> pmList = new ArrayList<>();
        List<String> sp = Executor.runNative("system_profiler SPMemoryDataType");
        int bank = 0;
        String bankLabel = Normal.UNKNOWN;
        long capacity = 0L;
        long speed = 0L;
        String manufacturer = Normal.UNKNOWN;
        String memoryType = Normal.UNKNOWN;
        for (String line : sp) {
            if (line.trim().startsWith("BANK")) {
                // Save previous bank
                if (bank++ > 0) {
                    pmList.add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType));
                }
                bankLabel = line.trim();
                int colon = bankLabel.lastIndexOf(Symbol.C_COLON);
                if (colon > 0) {
                    bankLabel = bankLabel.substring(0, colon - 1);
                }
            } else if (bank > 0) {
                String[] split = line.trim().split(Symbol.COLON);
                if (split.length == 2) {
                    switch (split[0]) {
                        case "Size":
                            capacity = Builder.parseDecimalMemorySizeToBinary(split[1].trim());
                            break;
                        case "Type":
                            memoryType = split[1].trim();
                            break;
                        case "Speed":
                            speed = Builder.parseHertz(split[1]);
                            break;
                        case "Manufacturer":
                            manufacturer = split[1].trim();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        pmList.add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType));

        return pmList.toArray(new PhysicalMemory[0]);
    }

    private long queryVmStats() {
        VMStatistics vmStats = new VMStatistics();
        if (0 != SystemB.INSTANCE.host_statistics(SystemB.INSTANCE.mach_host_self(), SystemB.HOST_VM_INFO, vmStats,
                new IntByReference(vmStats.size() / SystemB.INT_SIZE))) {
            Logger.error("Failed to get host VM info. Error code: {}", Native.getLastError());
            return 0L;
        }
        return (vmStats.free_count + vmStats.inactive_count) * getPageSize();
    }

    private VirtualMemory createVirtualMemory() {
        return new MacVirtualMemory(this);
    }

}
