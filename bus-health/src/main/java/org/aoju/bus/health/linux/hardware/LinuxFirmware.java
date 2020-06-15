/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
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
package org.aoju.bus.health.linux.hardware;

import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.RegEx;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.hardware.AbstractFirmware;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.memoize;

/**
 * Firmware data obtained by sysfs.
 *
 * @author Kimi Liu
 * @version 5.9.9
 * @since JDK 1.8+
 */
@Immutable
final class LinuxFirmware extends AbstractFirmware {

    // Jan 13 2013 16:24:29
    private static final DateTimeFormatter VCGEN_FORMATTER = DateTimeFormatter.ofPattern("MMM d uuuu HH:mm:ss",
            Locale.ENGLISH);
    private final Supplier<VcGenCmdStrings> vcGenCmd = memoize(this::queryVcGenCmd);
    private final Supplier<String> manufacturer = memoize(this::queryManufacturer);
    private final Supplier<String> description = memoize(this::queryDescription);
    private final Supplier<String> releaseDate = memoize(this::queryReleaseDate);
    private final Supplier<BiosStrings> bios = memoize(this::queryBios);
    private final Supplier<String> version = memoize(this::queryVersion);
    private final Supplier<String> name = memoize(this::queryName);

    private static String queryManufacturerFromSysfs() {
        final String biosVendor = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "bios_vendor").trim();
        if (biosVendor.isEmpty()) {
            return biosVendor;
        }
        return null;
    }

    private static String queryDescriptionFromSysfs() {
        final String modalias = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "modalias").trim();
        if (!modalias.isEmpty()) {
            return modalias;
        }
        return null;
    }

    private static String queryReleaseDateFromSysfs() {
        final String biosDate = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "bios_date").trim();
        if (!biosDate.isEmpty()) {
            return Builder.parseMmDdYyyyToYyyyMmDD(biosDate);
        }
        return null;
    }

    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    @Override
    public String getDescription() {
        return description.get();
    }

    @Override
    public String getVersion() {
        return version.get();
    }

    @Override
    public String getReleaseDate() {
        return releaseDate.get();
    }

    @Override
    public String getName() {
        return name.get();
    }

    private String queryManufacturer() {
        String result = null;
        if ((result = queryManufacturerFromSysfs()) == null && (result = vcGenCmd.get().manufacturer) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryDescription() {
        String result = null;
        if ((result = queryDescriptionFromSysfs()) == null && (result = vcGenCmd.get().description) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    // $ ls /sys/devices/virtual/dmi/id/
    // bios_date board_vendor chassis_version product_version
    // bios_vendor board_version modalias subsystem
    // bios_version chassis_asset_tag power sys_vendor
    // board_asset_tag chassis_serial product_name uevent
    // board_name chassis_type product_serial
    // board_serial chassis_vendor product_uuid

    private String queryVersion() {
        String result = null;
        if ((result = queryVersionFromSysfs()) == null && (result = vcGenCmd.get().version) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryReleaseDate() {
        String result = null;
        if ((result = queryReleaseDateFromSysfs()) == null && (result = vcGenCmd.get().releaseDate) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryName() {
        String result = null;
        if ((result = bios.get().biosName) == null && (result = vcGenCmd.get().name) == null) {
            return Normal.UNKNOWN;
        }
        return result;
    }

    private String queryVersionFromSysfs() {
        final String biosVersion = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "bios_version").trim();
        if (!biosVersion.isEmpty()) {
            String biosRevision = this.bios.get().biosRevision;
            return biosVersion + (StringKit.isBlank(biosRevision) ? Normal.EMPTY : " (revision " + biosRevision + ")");
        }
        return null;
    }

    // $ sudo dmidecode -t bios
    // # dmidecode 2.11
    // SMBIOS 2.4 present.
    //
    // Handle 0x0000, DMI type 0, 24 bytes
    // BIOS Information
    // Vendor: Phoenix Technologies LTD
    // Version: 6.00
    // Release Date: 07/02/2015
    // Address: 0xEA5E0
    // Runtime Size: 88608 bytes
    // ROM Size: 64 kB
    // Characteristics:
    // ISA is supported
    // PCI is supported
    // PC Card (PCMCIA) is supported
    // PNP is supported
    // APM is supported
    // BIOS is upgradeable
    // BIOS shadowing is allowed
    // ESCD support is available
    // Boot from CD is supported
    // Selectable boot is supported
    // EDD is supported
    // Print screen service is supported (int 5h)
    // 8042 keyboard services are supported (int 9h)
    // Serial services are supported (int 14h)
    // Printer services are supported (int 17h)
    // CGA/mono video services are supported (int 10h)
    // ACPI is supported
    // Smart battery is supported
    // BIOS boot specification is supported
    // Function key-initiated network boot is supported
    // Targeted content distribution is supported
    // BIOS Revision: 4.6
    // Firmware Revision: 0.0

    private BiosStrings queryBios() {
        String biosName = null;
        String revision = null;

        final String biosMarker = "SMBIOS";
        final String revMarker = "Bios Revision:";

        // Requires root, may not return anything
        for (final String checkLine : Executor.runNative("dmidecode -t bios")) {
            if (checkLine.contains(biosMarker)) {
                String[] biosArr = RegEx.SPACES.split(checkLine);
                if (biosArr.length >= 2) {
                    biosName = biosArr[0] + Symbol.SPACE + biosArr[1];
                }
            }
            if (checkLine.contains(revMarker)) {
                revision = checkLine.split(revMarker)[1].trim();
                // SMBIOS should be first line so if we're here we are done iterating
                break;
            }
        }
        return new BiosStrings(biosName, revision);
    }

    private VcGenCmdStrings queryVcGenCmd() {
        String vcReleaseDate = null;
        String vcManufacturer = null;
        String vcVersion = null;

        List<String> vcgencmd = Executor.runNative("vcgencmd version");
        if (vcgencmd.size() >= 3) {
            // First line is date
            try {
                vcReleaseDate = DateTimeFormatter.ISO_LOCAL_DATE.format(VCGEN_FORMATTER.parse(vcgencmd.get(0)));
            } catch (DateTimeParseException e) {
                vcReleaseDate = Normal.UNKNOWN;
            }
            // Second line is copyright
            String[] copyright = RegEx.SPACES.split(vcgencmd.get(1));
            vcManufacturer = copyright[copyright.length - 1];
            // Third line is version
            vcVersion = vcgencmd.get(2).replace("version ", Normal.EMPTY);
            return new VcGenCmdStrings(vcReleaseDate, vcManufacturer, vcVersion, "RPi", "Bootloader");
        }
        return new VcGenCmdStrings(null, null, null, null, null);
    }

    private static final class BiosStrings {
        private final String biosName;
        private final String biosRevision;

        private BiosStrings(String biosName, String biosRevision) {
            this.biosName = biosName;
            this.biosRevision = biosRevision;
        }
    }

    private static final class VcGenCmdStrings {
        private final String releaseDate;
        private final String manufacturer;
        private final String version;
        private final String name;
        private final String description;

        private VcGenCmdStrings(String releaseDate, String manufacturer, String version, String name,
                                String description) {
            this.releaseDate = releaseDate;
            this.manufacturer = manufacturer;
            this.version = version;
            this.name = name;
            this.description = description;
        }
    }

}
