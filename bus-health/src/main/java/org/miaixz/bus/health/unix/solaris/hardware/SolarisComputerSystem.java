/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
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
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.unix.solaris.hardware;

import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.toolkit.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoize;
import org.miaixz.bus.health.builtin.hardware.AbstractComputerSystem;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.Firmware;
import org.miaixz.bus.health.unix.UnixBaseboard;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Hardware data obtained from smbios.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class SolarisComputerSystem extends AbstractComputerSystem {

    private final Supplier<SmbiosStrings> smbiosStrings = Memoize.memoize(SolarisComputerSystem::readSmbios);

    private static SmbiosStrings readSmbios() {
        // $ smbios
        // ID SIZE TYPE
        // 0 87 SMB_TYPE_BIOS (BIOS Information)
        //
        // Vendor: Parallels Software International Inc.
        // Version String: 11.2.1 (32686)
        // Release Date: 07/15/2016
        // Address Segment: 0xf000
        // ... <snip> ...
        //
        // ID SIZE TYPE
        // 1 177 SMB_TYPE_SYSTEM (system information)
        //
        // Manufacturer: Parallels Software International Inc.
        // Product: Parallels Virtual Platforom
        // Version: None
        // Serial Number: Parallels-45 2E 7E 2D 57 5C 4B 59 B1 30 28 81 B7 81 89
        // 34
        //
        // UUID: 452e7e2d-575c04b59-b130-2881b7818934
        // Wake-up Event: 0x6 (Power Switch)
        // SKU Number: Undefined
        // Family: Parallels VM
        //
        // ID SIZE TYPE
        // 2 90 SMB_TYPE_BASEBOARD (base board)
        //
        // Manufacturer: Parallels Software International Inc.
        // Product: Parallels Virtual Platform
        // Version: None
        // Serial Number: None
        // ... <snip> ...
        //
        // ID SIZE TYPE
        // 3 .... <snip> ...

        final String serialNumMarker = "Serial Number";

        SmbType smbTypeId = null;

        EnumMap<SmbType, Map<String, String>> smbTypesMap = new EnumMap<>(SmbType.class);
        smbTypesMap.put(SmbType.SMB_TYPE_BIOS, new HashMap<>());
        smbTypesMap.put(SmbType.SMB_TYPE_SYSTEM, new HashMap<>());
        smbTypesMap.put(SmbType.SMB_TYPE_BASEBOARD, new HashMap<>());

        // Only works with root permissions but it's all we've got
        for (final String checkLine : Executor.runNative("smbios")) {
            // Change the smbTypeId when hitting a new header
            if (checkLine.contains("SMB_TYPE_") && (smbTypeId = getSmbType(checkLine)) == null) {
                // If we get past what we need, stop iterating
                break;
            }
            // Based on the smbTypeID we are processing for
            Integer colonDelimiterIndex = checkLine.indexOf(":");
            if (smbTypeId != null && colonDelimiterIndex >= 0) {
                String key = checkLine.substring(0, colonDelimiterIndex).trim();
                String val = checkLine.substring(colonDelimiterIndex + 1).trim();
                smbTypesMap.get(smbTypeId).put(key, val);
            }
        }

        Map<String, String> smbTypeBIOSMap = smbTypesMap.get(SmbType.SMB_TYPE_BIOS);
        Map<String, String> smbTypeSystemMap = smbTypesMap.get(SmbType.SMB_TYPE_SYSTEM);
        Map<String, String> smbTypeBaseboardMap = smbTypesMap.get(SmbType.SMB_TYPE_BASEBOARD);

        // If we get to end and haven't assigned, use fallback
        if (!smbTypeSystemMap.containsKey(serialNumMarker) || StringKit.isBlank(smbTypeSystemMap.get(serialNumMarker))) {
            smbTypeSystemMap.put(serialNumMarker, readSerialNumber());
        }
        return new SmbiosStrings(smbTypeBIOSMap, smbTypeSystemMap, smbTypeBaseboardMap);
    }

    private static SmbType getSmbType(String checkLine) {
        for (SmbType smbType : SmbType.values()) {
            if (checkLine.contains(smbType.name())) {
                return smbType;
            }
        }
        // First 3 SMB_TYPEs are what we need. After that no need to
        // continue processing the output
        return null;
    }

    private static String readSerialNumber() {
        // If they've installed STB (Sun Explorer) this should work
        String serialNumber = Executor.getFirstAnswer("sneep");
        // if that didn't work, try...
        if (serialNumber.isEmpty()) {
            String marker = "chassis-sn:";
            for (String checkLine : Executor.runNative("prtconf -pv")) {
                if (checkLine.contains(marker)) {
                    serialNumber = Builder.getSingleQuoteStringValue(checkLine);
                    break;
                }
            }
        }
        return serialNumber;
    }

    @Override
    public String getManufacturer() {
        return smbiosStrings.get().manufacturer;
    }

    @Override
    public String getModel() {
        return smbiosStrings.get().model;
    }

    @Override
    public String getSerialNumber() {
        return smbiosStrings.get().serialNumber;
    }

    @Override
    public String getHardwareUUID() {
        return smbiosStrings.get().uuid;
    }

    @Override
    public Firmware createFirmware() {
        return new SolarisFirmware(smbiosStrings.get().biosVendor, smbiosStrings.get().biosVersion,
                smbiosStrings.get().biosDate);
    }

    @Override
    public Baseboard createBaseboard() {
        return new UnixBaseboard(smbiosStrings.get().boardManufacturer, smbiosStrings.get().boardModel,
                smbiosStrings.get().boardSerialNumber, smbiosStrings.get().boardVersion);
    }

    public enum SmbType {
        /**
         * BIOS
         */
        SMB_TYPE_BIOS,
        /**
         * System
         */
        SMB_TYPE_SYSTEM,
        /**
         * Baseboard
         */
        SMB_TYPE_BASEBOARD
    }

    private static final class SmbiosStrings {
        private final String biosVendor;
        private final String biosVersion;
        private final String biosDate;

        private final String manufacturer;
        private final String model;
        private final String serialNumber;
        private final String uuid;

        private final String boardManufacturer;
        private final String boardModel;
        private final String boardVersion;
        private final String boardSerialNumber;

        private SmbiosStrings(Map<String, String> smbTypeBIOSStrings, Map<String, String> smbTypeSystemStrings,
                              Map<String, String> smbTypeBaseboardStrings) {
            final String vendorMarker = "Vendor";
            final String biosDateMarker = "Release Date";
            final String biosVersionMarker = "Version String";

            final String manufacturerMarker = "Manufacturer";
            final String productMarker = "Product";
            final String serialNumMarker = "Serial Number";
            final String uuidMarker = "UUID";
            final String versionMarker = "Version";

            this.biosVendor = Builder.getValueOrUnknown(smbTypeBIOSStrings, vendorMarker);
            this.biosVersion = Builder.getValueOrUnknown(smbTypeBIOSStrings, biosVersionMarker);
            this.biosDate = Builder.getValueOrUnknown(smbTypeBIOSStrings, biosDateMarker);
            this.manufacturer = Builder.getValueOrUnknown(smbTypeSystemStrings, manufacturerMarker);
            this.model = Builder.getValueOrUnknown(smbTypeSystemStrings, productMarker);
            this.serialNumber = Builder.getValueOrUnknown(smbTypeSystemStrings, serialNumMarker);
            this.uuid = Builder.getValueOrUnknown(smbTypeSystemStrings, uuidMarker);
            this.boardManufacturer = Builder.getValueOrUnknown(smbTypeBaseboardStrings, manufacturerMarker);
            this.boardModel = Builder.getValueOrUnknown(smbTypeBaseboardStrings, productMarker);
            this.boardVersion = Builder.getValueOrUnknown(smbTypeBaseboardStrings, versionMarker);
            this.boardSerialNumber = Builder.getValueOrUnknown(smbTypeBaseboardStrings, serialNumMarker);
        }
    }

}
