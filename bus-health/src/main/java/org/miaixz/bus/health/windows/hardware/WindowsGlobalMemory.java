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
package org.miaixz.bus.health.windows.hardware;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Psapi;
import com.sun.jna.platform.win32.VersionHelpers;
import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.builtin.hardware.PhysicalMemory;
import org.miaixz.bus.health.builtin.hardware.VirtualMemory;
import org.miaixz.bus.health.builtin.hardware.common.AbstractGlobalMemory;
import org.miaixz.bus.health.builtin.jna.Struct;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.driver.wmi.Win32PhysicalMemory;
import org.miaixz.bus.logger.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Memory obtained by Performance Info.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
final class WindowsGlobalMemory extends AbstractGlobalMemory {

    private static final boolean IS_WINDOWS10_OR_GREATER = VersionHelpers.IsWindows10OrGreater();

    private final Supplier<Triplet<Long, Long, Long>> availTotalSize = Memoizer.memoize(WindowsGlobalMemory::readPerfInfo,
            Memoizer.defaultExpiration());

    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    /**
     * Convert memory type number to a human readable string
     *
     * @param type The memory type
     * @return A string describing the type
     */
    private static String memoryType(int type) {
        switch (type) {
            case 0:
                return "Unknown";
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
                return "BRAM";
            case 23:
                return "DDR FB-DIMM";
            default:
                // values 24 and higher match SMBIOS types
                return smBiosMemoryType(type);
        }
    }

    /**
     * Convert SMBIOS type number to a human readable string
     *
     * @param type The SMBIOS type
     * @return A string describing the type
     */
    private static String smBiosMemoryType(int type) {
        // https://www.dmtf.org/sites/default/files/standards/documents/DSP0134_3.7.0.pdf
        // table 77
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
            case 0x20:
                return "HBM";
            case 0x21:
                return "HBM2";
            case 0x22:
                return "DDR5";
            case 0x23:
                return "LPDDR5";
            case 0x24:
                return "HBM3";
            case 0x02:
            default:
                return "Unknown";
        }
    }

    private static Triplet<Long, Long, Long> readPerfInfo() {
        try (Struct.CloseablePerformanceInformation performanceInfo = new Struct.CloseablePerformanceInformation()) {
            if (!Psapi.INSTANCE.GetPerformanceInfo(performanceInfo, performanceInfo.size())) {
                Logger.error("Failed to get Performance Info. Error code: {}", Kernel32.INSTANCE.GetLastError());
                return Triplet.of(0L, 0L, 4098L);
            }
            long pageSize = performanceInfo.PageSize.longValue();
            long memAvailable = pageSize * performanceInfo.PhysicalAvailable.longValue();
            long memTotal = pageSize * performanceInfo.PhysicalTotal.longValue();
            return Triplet.of(memAvailable, memTotal, pageSize);
        }
    }

    @Override
    public long getAvailable() {
        return availTotalSize.get().getLeft();
    }

    @Override
    public long getTotal() {
        return availTotalSize.get().getMiddle();
    }

    @Override
    public long getPageSize() {
        return availTotalSize.get().getRight();
    }

    @Override
    public VirtualMemory getVirtualMemory() {
        return vm.get();
    }

    private VirtualMemory createVirtualMemory() {
        return new WindowsVirtualMemory(this);
    }

    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        List<PhysicalMemory> physicalMemoryList = new ArrayList<>();
        if (IS_WINDOWS10_OR_GREATER) {
            WmiResult<Win32PhysicalMemory.PhysicalMemoryProperty> bankMap = Win32PhysicalMemory.queryphysicalMemory();
            for (int index = 0; index < bankMap.getResultCount(); index++) {
                String bankLabel = WmiKit.getString(bankMap, Win32PhysicalMemory.PhysicalMemoryProperty.BANKLABEL, index);
                long capacity = WmiKit.getUint64(bankMap, Win32PhysicalMemory.PhysicalMemoryProperty.CAPACITY, index);
                long speed = WmiKit.getUint32(bankMap, Win32PhysicalMemory.PhysicalMemoryProperty.SPEED, index) * 1_000_000L;
                String manufacturer = WmiKit.getString(bankMap, Win32PhysicalMemory.PhysicalMemoryProperty.MANUFACTURER, index);
                String memoryType = smBiosMemoryType(
                        WmiKit.getUint32(bankMap, Win32PhysicalMemory.PhysicalMemoryProperty.SMBIOSMEMORYTYPE, index));
                String partNumber = WmiKit.getString(bankMap, Win32PhysicalMemory.PhysicalMemoryProperty.PARTNUMBER, index);
                physicalMemoryList
                        .add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType, partNumber));
            }
        } else {
            WmiResult<Win32PhysicalMemory.PhysicalMemoryPropertyWin8> bankMap = Win32PhysicalMemory.queryphysicalMemoryWin8();
            for (int index = 0; index < bankMap.getResultCount(); index++) {
                String bankLabel = WmiKit.getString(bankMap, Win32PhysicalMemory.PhysicalMemoryPropertyWin8.BANKLABEL, index);
                long capacity = WmiKit.getUint64(bankMap, Win32PhysicalMemory.PhysicalMemoryPropertyWin8.CAPACITY, index);
                long speed = WmiKit.getUint32(bankMap, Win32PhysicalMemory.PhysicalMemoryPropertyWin8.SPEED, index) * 1_000_000L;
                String manufacturer = WmiKit.getString(bankMap, Win32PhysicalMemory.PhysicalMemoryPropertyWin8.MANUFACTURER, index);
                String memoryType = memoryType(
                        WmiKit.getUint16(bankMap, Win32PhysicalMemory.PhysicalMemoryPropertyWin8.MEMORYTYPE, index));
                String partNumber = WmiKit.getString(bankMap, Win32PhysicalMemory.PhysicalMemoryPropertyWin8.PARTNUMBER, index);
                physicalMemoryList
                        .add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType, partNumber));
            }
        }
        return physicalMemoryList;
    }

}
