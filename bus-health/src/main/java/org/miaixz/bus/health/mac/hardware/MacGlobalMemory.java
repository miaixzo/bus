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
package org.miaixz.bus.health.mac.hardware;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
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

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;

/**
 * Memory obtained by host_statistics (vm_stat) and sysctl.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
final class MacGlobalMemory extends AbstractGlobalMemory {

    /**
     * The total value.
     */
    private final Supplier<Long> total = Memoizer.memoize(MacGlobalMemory::queryPhysMem);
    /**
     * The pageSize value.
     */
    private final Supplier<Long> pageSize = Memoizer.memoize(MacGlobalMemory::queryPageSize);
    /**
     * The available value.
     */
    private final Supplier<Long> available = Memoizer.memoize(this::queryVmStats, Memoizer.defaultExpiration());
    /**
     * The vm value.
     */
    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    /**
     * Queries the phys mem.
     *
     * @return the query phys mem result
     */
    private static long queryPhysMem() {
        return SysctlKit.sysctl("hw.memsize", 0L);
    }

    /**
     * Queries the page size.
     *
     * @return the query page size result
     */
    private static long queryPageSize() {
        try (ByRef.CloseableLongByReference pPageSize = new ByRef.CloseableLongByReference()) {
            if (0 == SystemB.INSTANCE.host_page_size(SystemB.INSTANCE.mach_host_self(), pPageSize)) {
                return pPageSize.getValue();
            }
        }
        Logger.error(false, "Health", "Failed to get host page size. Error code: {}", Native.getLastError());
        return 4098L;
    }

    /**
     * Returns the available.
     *
     * @return the get available result
     */
    @Override
    public long getAvailable() {
        return available.get();
    }

    /**
     * Returns the total.
     *
     * @return the get total result
     */
    @Override
    public long getTotal() {
        return total.get();
    }

    /**
     * Returns the page size.
     *
     * @return the get page size result
     */
    @Override
    public long getPageSize() {
        return pageSize.get();
    }

    /**
     * Returns the virtual memory.
     *
     * @return the get virtual memory result
     */
    @Override
    public VirtualMemory getVirtualMemory() {
        return vm.get();
    }

    /**
     * Queries the vm stats.
     *
     * @return the query vm stats result
     */
    private long queryVmStats() {
        try (Struct.CloseableVMStatistics vmStats = new Struct.CloseableVMStatistics();
                ByRef.CloseableIntByReference size = new ByRef.CloseableIntByReference(
                        vmStats.size() / SystemB.INT_SIZE)) {
            if (0 != SystemB.INSTANCE
                    .host_statistics(SystemB.INSTANCE.mach_host_self(), SystemB.HOST_VM_INFO, vmStats, size)) {
                Logger.error(false, "Health", "Failed to get host VM info. Error code: {}", Native.getLastError());
                return 0L;
            }
            return (vmStats.free_count + vmStats.inactive_count) * getPageSize();
        }
    }

    /**
     * Returns the physical memory.
     *
     * @return the get physical memory result
     */
    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        return parseSystemProfilerMemory(Executor.runNative("system_profiler SPMemoryDataType"));
    }

    /**
     * Parses the output of {@code system_profiler SPMemoryDataType} into physical memory objects.
     *
     * @param lines the output lines from system profiler
     * @return the parsed physical memory modules
     */
    static List<PhysicalMemory> parseSystemProfilerMemory(List<String> lines) {
        List<PhysicalMemory> pmList = new ArrayList<>();
        int bank = 0;
        String bankLabel = Normal.UNKNOWN;
        long capacity = 0L;
        long speed = 0L;
        String manufacturer = Normal.UNKNOWN;
        String memoryType = Normal.UNKNOWN;
        String partNumber = Normal.UNKNOWN;
        String serialNumber = Normal.UNKNOWN;
        for (String line : lines) {
            if (line.trim().startsWith("BANK")) {
                // Save previous bank
                if (bank++ > 0) {
                    pmList.add(
                            new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType, partNumber,
                                    serialNumber));
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
        if (bank > 0 && capacity > 0) {
            // Intel/socketed format: save the last bank
            pmList.add(
                    new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType, partNumber, serialNumber));
        } else {
            // Apple Silicon format: no BANK lines, parse top-level keys
            for (String line : lines) {
                String[] split = line.trim().split(Symbol.COLON);
                if (split.length == 2) {
                    String key = split[0].trim();
                    String value = split[1].trim();
                    switch (key) {
                        case "Memory":
                            capacity = Parsing.parseDecimalMemorySizeToBinary(value);
                            break;

                        case "Type":
                            memoryType = value;
                            break;

                        case "Speed":
                            speed = Parsing.parseHertz(split[1]);
                            break;

                        case "Manufacturer":
                            manufacturer = value;
                            break;

                        case "Part Number":
                            partNumber = value;
                            break;

                        case "Serial Number":
                            serialNumber = value;
                            break;

                        default:
                            break;
                    }
                }
            }
            if (capacity > 0) {
                pmList.add(
                        new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType, partNumber,
                                serialNumber));
            }
        }

        return pmList;
    }

    /**
     * Creates the virtual memory.
     *
     * @return the create virtual memory result
     */
    private VirtualMemory createVirtualMemory() {
        return new MacVirtualMemory(this);
    }

}
