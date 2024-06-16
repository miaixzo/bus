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
package org.miaixz.bus.health.mac.hardware;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.PhysicalMemory;
import org.miaixz.bus.health.builtin.hardware.VirtualMemory;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGlobalMemory;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.mac.SysctlKit;
import org.miaixz.bus.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Memory obtained by host_statistics (vm_stat) and sysctl.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class MacGlobalMemory extends AbstractGlobalMemory {

    private final Supplier<Long> total = Memoizer.memoize(MacGlobalMemory::queryPhysMem);
    private final Supplier<Long> pageSize = Memoizer.memoize(MacGlobalMemory::queryPageSize);
    private final Supplier<Long> available = Memoizer.memoize(this::queryVmStats, Memoizer.defaultExpiration());
    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    private static long queryPhysMem() {
        return SysctlKit.sysctl("hw.memsize", 0L);
    }

    private static long queryPageSize() {
        try (ByRef.CloseableLongByReference pPageSize = new ByRef.CloseableLongByReference()) {
            if (0 == SystemB.INSTANCE.host_page_size(SystemB.INSTANCE.mach_host_self(), pPageSize)) {
                return pPageSize.getValue();
            }
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

    private long queryVmStats() {
        try (Struct.CloseableVMStatistics vmStats = new Struct.CloseableVMStatistics();
             ByRef.CloseableIntByReference size = new ByRef.CloseableIntByReference(vmStats.size() / SystemB.INT_SIZE)) {
            if (0 != SystemB.INSTANCE.host_statistics(SystemB.INSTANCE.mach_host_self(), SystemB.HOST_VM_INFO, vmStats,
                    size)) {
                Logger.error("Failed to get host VM info. Error code: {}", Native.getLastError());
                return 0L;
            }
            return (vmStats.free_count + vmStats.inactive_count) * getPageSize();
        }
    }

    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        List<PhysicalMemory> pmList = new ArrayList<>();
        List<String> sp = Executor.runNative("system_profiler SPMemoryDataType");
        int bank = 0;
        String bankLabel = Normal.UNKNOWN;
        long capacity = 0L;
        long speed = 0L;
        String manufacturer = Normal.UNKNOWN;
        String memoryType = Normal.UNKNOWN;
        String partNumber = Normal.UNKNOWN;
        String serialNumber = Normal.UNKNOWN;
        for (String line : sp) {
            if (line.trim().startsWith("BANK")) {
                // Save previous bank
                if (bank++ > 0) {
                    pmList.add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType,
                            Normal.UNKNOWN,serialNumber));
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
                            capacity = Parsing.parseDecimalMemorySizeToBinary(split[1].trim());
                            break;
                        case "Type":
                            memoryType = split[1].trim();
                            break;
                        case "Speed":
                            speed = Parsing.parseHertz(split[1]);
                            break;
                        case "Manufacturer":
                            manufacturer = split[1].trim();
                            break;
                        case "Part Number":
                            partNumber = split[1].trim();
                            break;
                        case "Serial Number":
                            serialNumber = split[1].trim();
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        pmList.add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType, partNumber, serialNumber));

        return pmList;
    }

    private VirtualMemory createVirtualMemory() {
        return new MacVirtualMemory(this);
    }
}
