/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
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

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.Firmware;
import org.miaixz.bus.health.builtin.hardware.common.AbstractComputerSystem;
import org.miaixz.bus.health.unix.hardware.UnixBaseboard;

/**
 * Hardware data obtained from smbios.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
final class SolarisComputerSystem extends AbstractComputerSystem {

    /**
     * The smbiosStrings value.
     */
    private final Supplier<SmbiosStrings> smbiosStrings = Memoizer.memoize(SolarisComputerSystem::readSmbios);

    /**
     * Reads the smbios.
     *
     * @return the read smbios result
     */
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
            Integer colonDelimiterIndex = checkLine.indexOf(Symbol.COLON);
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
        if (!smbTypeSystemMap.containsKey(serialNumMarker)
                || StringKit.isBlank(smbTypeSystemMap.get(serialNumMarker))) {
            smbTypeSystemMap.put(serialNumMarker, readSerialNumber());
        }
        return new SmbiosStrings(smbTypeBIOSMap, smbTypeSystemMap, smbTypeBaseboardMap);
    }

    /**
     * Reads the serial number.
     *
     * @return the read serial number result
     */
    private static String readSerialNumber() {
        // If they've installed STB (Sun Explorer) this should work
        String serialNumber = Executor.getFirstAnswer("sneep");
        // if that didn't work, try...
        if (serialNumber.isEmpty()) {
            String marker = "chassis-sn:";
            for (String checkLine : Executor.runNative("prtconf -pv")) {
                if (checkLine.contains(marker)) {
                    serialNumber = Parsing.getSingleQuoteStringValue(checkLine);
                    break;
                }
            }
        }
        return serialNumber;
    }

    /**
     * Returns the smb type.
     *
     * @param checkLine the check line
     * @return the get smb type result
     */
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

    /**
     * Returns the manufacturer.
     *
     * @return the get manufacturer result
     */
    @Override
    public String getManufacturer() {
        return smbiosStrings.get().manufacturer;
    }

    /**
     * Returns the model.
     *
     * @return the get model result
     */
    @Override
    public String getModel() {
        return smbiosStrings.get().model;
    }

    /**
     * Returns the serial number.
     *
     * @return the get serial number result
     */
    @Override
    public String getSerialNumber() {
        return smbiosStrings.get().serialNumber;
    }

    /**
     * Returns the hardware uuid.
     *
     * @return the get hardware uuid result
     */
    @Override
    public String getHardwareUUID() {
        return smbiosStrings.get().uuid;
    }

    /**
     * Creates the firmware.
     *
     * @return the create firmware result
     */
    @Override
    public Firmware createFirmware() {
        return new SolarisFirmware(smbiosStrings.get().biosVendor, smbiosStrings.get().biosVersion,
                smbiosStrings.get().biosDate);
    }

    /**
     * Creates the baseboard.
     *
     * @return the create baseboard result
     */
    @Override
    public Baseboard createBaseboard() {
        return new UnixBaseboard(smbiosStrings.get().boardManufacturer, smbiosStrings.get().boardModel,
                smbiosStrings.get().boardSerialNumber, smbiosStrings.get().boardVersion);
    }

    /**
     * The SmbType enum.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
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

    /**
     * The SmbiosStrings class.
     *
     * @author Kimi Liu
     * @since Java 21+
     */
    private static final class SmbiosStrings {

        /**
         * The biosVendor value.
         */
        private final String biosVendor;

        /**
         * The biosVersion value.
         */
        private final String biosVersion;

        /**
         * The biosDate value.
         */
        private final String biosDate;

        /**
         * The manufacturer value.
         */
        private final String manufacturer;

        /**
         * The model value.
         */
        private final String model;

        /**
         * The serialNumber value.
         */
        private final String serialNumber;

        /**
         * The uuid value.
         */
        private final String uuid;

        /**
         * The boardManufacturer value.
         */
        private final String boardManufacturer;

        /**
         * The boardModel value.
         */
        private final String boardModel;

        /**
         * The boardVersion value.
         */
        private final String boardVersion;

        /**
         * The boardSerialNumber value.
         */
        private final String boardSerialNumber;

        /**
         * Creates a new SmbiosStrings instance.
         *
         * @param smbTypeBIOSStrings      the smb type bios strings
         * @param smbTypeSystemStrings    the smb type system strings
         * @param smbTypeBaseboardStrings the smb type baseboard strings
         */
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

            this.biosVendor = Parsing.getValueOrUnknown(smbTypeBIOSStrings, vendorMarker);
            this.biosVersion = Parsing.getValueOrUnknown(smbTypeBIOSStrings, biosVersionMarker);
            this.biosDate = Parsing.getValueOrUnknown(smbTypeBIOSStrings, biosDateMarker);
            this.manufacturer = Parsing.getValueOrUnknown(smbTypeSystemStrings, manufacturerMarker);
            this.model = Parsing.getValueOrUnknown(smbTypeSystemStrings, productMarker);
            this.serialNumber = Parsing.getValueOrUnknown(smbTypeSystemStrings, serialNumMarker);
            this.uuid = Parsing.getValueOrUnknown(smbTypeSystemStrings, uuidMarker);
            this.boardManufacturer = Parsing.getValueOrUnknown(smbTypeBaseboardStrings, manufacturerMarker);
            this.boardModel = Parsing.getValueOrUnknown(smbTypeBaseboardStrings, productMarker);
            this.boardVersion = Parsing.getValueOrUnknown(smbTypeBaseboardStrings, versionMarker);
            this.boardSerialNumber = Parsing.getValueOrUnknown(smbTypeBaseboardStrings, serialNumMarker);
        }

    }

}
