/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.hardware.windows;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.Psapi.PERFORMANCE_INFORMATION;
import com.sun.jna.platform.win32.VersionHelpers;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.windows.WmiQueryHandler;
import org.aoju.bus.health.common.windows.WmiUtils;
import org.aoju.bus.health.hardware.AbstractGlobalMemory;
import org.aoju.bus.health.hardware.PhysicalMemory;
import org.aoju.bus.health.hardware.VirtualMemory;
import org.aoju.bus.logger.Logger;

import java.util.function.Supplier;

/**
 * Memory obtained by Performance Info.
 *
 * @author Kimi Liu
 * @version 5.3.8
 * @since JDK 1.8+
 */
public class WindowsGlobalMemory extends AbstractGlobalMemory {

    private static final boolean IS_WINDOWS10_OR_GREATER = VersionHelpers.IsWindows10OrGreater();

    private final Supplier<PerfInfo> perfInfo = Memoizer.memoize(this::readPerfInfo, Memoizer.defaultExpiration());

    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    /**
     * Convert memory type number to a human readable string
     *
     * @param type The memory type
     * @return A string describing the type
     */
    private static String memoryType(int type) {
        switch (type) {
            case 1:
                return "Other";
            case 2:
                return "DRAM";
            case 3:
                return "Synchronous DRAM";
            case 4:
                return "Cache DRAM";
            case 5:
                return "EDO";
            case 6:
                return "EDRAM";
            case 7:
                return "VRAM";
            case 8:
                return "SRAM";
            case 9:
                return "RAM";
            case 10:
                return "ROM";
            case 11:
                return "Flash";
            case 12:
                return "EEPROM";
            case 13:
                return "FEPROM";
            case 14:
                return "EPROM";
            case 15:
                return "CDRAM";
            case 16:
                return "3DRAM";
            case 17:
                return "SDRAM";
            case 18:
                return "SGRAM";
            case 19:
                return "RDRAM";
            case 20:
                return "DDR";
            case 21:
                return "DDR2";
            case 22:
                return "DDR2-FB-DIMM";
            case 24:
                return "DDR3";
            case 25:
                return "FBD2";
            default:
                return "Unknown";
        }
    }

    /**
     * Convert SMBIOS type number to a human readable string
     *
     * @param type The SMBIOS type
     * @return A string describing the type
     */
    private static String smBiosMemoryType(int type) {
        switch (type) {
            case 0x01:
                return "Other";
            case 0x03:
                return "DRAM";
            case 0x04:
                return "EDRAM";
            case 0x05:
                return "VRAM";
            case 0x06:
                return "SRAM";
            case 0x07:
                return "RAM";
            case 0x08:
                return "ROM";
            case 0x09:
                return "FLASH";
            case 0x0A:
                return "EEPROM";
            case 0x0B:
                return "FEPROM";
            case 0x0C:
                return "EPROM";
            case 0x0D:
                return "CDRAM";
            case 0x0E:
                return "3DRAM";
            case 0x0F:
                return "SDRAM";
            case 0x10:
                return "SGRAM";
            case 0x11:
                return "RDRAM";
            case 0x12:
                return "DDR";
            case 0x13:
                return "DDR2";
            case 0x14:
                return "DDR2 FB-DIMM";
            case 0x18:
                return "DDR3";
            case 0x19:
                return "FBD2";
            case 0x1A:
                return "DDR4";
            case 0x1B:
                return "LPDDR";
            case 0x1C:
                return "LPDDR2";
            case 0x1D:
                return "LPDDR3";
            case 0x1E:
                return "LPDDR4";
            case 0x1F:
                return "Logical non-volatile device";
            case 0x02:
            default:
                return "Unknown";
        }
    }

    @Override
    public long getAvailable() {
        return perfInfo.get().available;
    }

    @Override
    public long getTotal() {
        return perfInfo.get().total;
    }

    @Override
    public long getPageSize() {
        return perfInfo.get().pageSize;
    }

    @Override
    public VirtualMemory getVirtualMemory() {
        return vm.get();
    }

    private VirtualMemory createVirtualMemory() {
        return new WindowsVirtualMemory(getPageSize());
    }

    @Override
    public PhysicalMemory[] getPhysicalMemory() {
        PhysicalMemory[] physicalMemoryArray;
        WmiQueryHandler wmiQueryHandler = WmiQueryHandler.createInstance();
        if (IS_WINDOWS10_OR_GREATER) {
            WmiQuery<PhysicalMemoryProperty> physicalMemoryQuery = new WmiQuery<>("Win32_PhysicalMemory",
                    PhysicalMemoryProperty.class);
            WmiResult<PhysicalMemoryProperty> bankMap = wmiQueryHandler.queryWMI(physicalMemoryQuery);

            physicalMemoryArray = new PhysicalMemory[bankMap.getResultCount()];
            for (int index = 0; index < bankMap.getResultCount(); index++) {
                String bankLabel = WmiUtils.getString(bankMap, PhysicalMemoryProperty.BANKLABEL, index);
                long capacity = WmiUtils.getUint64(bankMap, PhysicalMemoryProperty.CAPACITY, index);
                long speed = WmiUtils.getUint32(bankMap, PhysicalMemoryProperty.SPEED, index) * 1_000_000L;
                String manufacturer = WmiUtils.getString(bankMap, PhysicalMemoryProperty.MANUFACTURER, index);
                String memoryType = smBiosMemoryType(
                        WmiUtils.getUint32(bankMap, PhysicalMemoryProperty.SMBIOSMEMORYTYPE, index));
                physicalMemoryArray[index] = new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType);
            }
        } else {
            WmiQuery<PhysicalMemoryPropertyWin8> physicalMemoryQuery = new WmiQuery<>("Win32_PhysicalMemory",
                    PhysicalMemoryPropertyWin8.class);
            WmiResult<PhysicalMemoryPropertyWin8> bankMap = wmiQueryHandler.queryWMI(physicalMemoryQuery);

            physicalMemoryArray = new PhysicalMemory[bankMap.getResultCount()];
            for (int index = 0; index < bankMap.getResultCount(); index++) {
                String bankLabel = WmiUtils.getString(bankMap, PhysicalMemoryPropertyWin8.BANKLABEL, index);
                long capacity = WmiUtils.getUint64(bankMap, PhysicalMemoryPropertyWin8.CAPACITY, index);
                long speed = WmiUtils.getUint32(bankMap, PhysicalMemoryPropertyWin8.SPEED, index) * 1_000_000L;
                String manufacturer = WmiUtils.getString(bankMap, PhysicalMemoryPropertyWin8.MANUFACTURER, index);
                String memoryType = memoryType(
                        WmiUtils.getUint16(bankMap, PhysicalMemoryPropertyWin8.MEMORYTYPE, index));
                physicalMemoryArray[index] = new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType);
            }
        }
        return physicalMemoryArray;
    }

    private PerfInfo readPerfInfo() {
        long pageSize;
        long memAvailable;
        long memTotal;
        PERFORMANCE_INFORMATION performanceInfo = new PERFORMANCE_INFORMATION();
        if (!Psapi.INSTANCE.GetPerformanceInfo(performanceInfo, performanceInfo.size())) {
            Logger.error("Failed to get Performance Info. Error code: {}", Kernel32.INSTANCE.GetLastError());
            return new PerfInfo(0, 0, 4098);
        }
        pageSize = performanceInfo.PageSize.longValue();
        memAvailable = pageSize * performanceInfo.PhysicalAvailable.longValue();
        memTotal = pageSize * performanceInfo.PhysicalTotal.longValue();
        return new PerfInfo(memTotal, memAvailable, pageSize);
    }

    enum PhysicalMemoryProperty {
        BANKLABEL, CAPACITY, SPEED, MANUFACTURER, SMBIOSMEMORYTYPE
    }

    // Pre-Win10
    enum PhysicalMemoryPropertyWin8 {
        BANKLABEL, CAPACITY, SPEED, MANUFACTURER, MEMORYTYPE
    }

    private static final class PerfInfo {
        private final long total;
        private final long available;
        private final long pageSize;

        private PerfInfo(long total, long available, long pageSize) {
            this.total = total;
            this.available = available;
            this.pageSize = pageSize;
        }
    }

}
