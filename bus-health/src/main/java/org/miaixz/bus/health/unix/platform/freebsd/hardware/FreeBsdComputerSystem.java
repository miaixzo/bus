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
package org.miaixz.bus.health.unix.platform.freebsd.hardware;

import org.miaixz.bus.core.annotation.Immutable;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.tuple.Tuple;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.Baseboard;
import org.miaixz.bus.health.builtin.hardware.Firmware;
import org.miaixz.bus.health.builtin.hardware.common.AbstractComputerSystem;
import org.miaixz.bus.health.unix.hardware.UnixBaseboard;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;

import java.util.function.Supplier;

/**
 * Hardware data obtained from dmidecode.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
final class FreeBsdComputerSystem extends AbstractComputerSystem {

    private final Supplier<Tuple> manufModelSerialUuidVers = Memoizer.memoize(
            FreeBsdComputerSystem::readDmiDecode);

    private static Tuple readDmiDecode() {
        String manufacturer = null;
        String model = null;
        String serialNumber = null;
        String uuid = null;
        String version = null;

        // $ sudo dmidecode -t system
        // # dmidecode 3.0
        // Scanning /dev/mem for entry point.
        // SMBIOS 2.7 present.
        //
        // Handle 0x0001, DMI type 1, 27 bytes
        // System Information
        // Manufacturer: Parallels Software International Inc.
        // Product Name: Parallels Virtual Platform
        // Version: None
        // Serial Number: Parallels-47 EC 38 2A 33 1B 4C 75 94 0F F7 AF 86 63 C0
        // C4
        // UUID: 2A38EC47-1B33-854C-940F-F7AF8663C0C4
        // Wake-up Type: Power Switch
        // SKU Number: Undefined
        // Family: Parallels VM
        //
        // Handle 0x0016, DMI type 32, 20 bytes
        // System Boot Information
        // Status: No errors detected

        final String manufacturerMarker = "Manufacturer:";
        final String productNameMarker = "Product Name:";
        final String serialNumMarker = "Serial Number:";
        final String uuidMarker = "UUID:";
        final String versionMarker = "Version:";

        // Only works with root permissions but it's all we've got
        for (final String checkLine : Executor.runNative("dmidecode -t system")) {
            if (checkLine.contains(manufacturerMarker)) {
                manufacturer = checkLine.split(manufacturerMarker)[1].trim();
            } else if (checkLine.contains(productNameMarker)) {
                model = checkLine.split(productNameMarker)[1].trim();
            } else if (checkLine.contains(serialNumMarker)) {
                serialNumber = checkLine.split(serialNumMarker)[1].trim();
            } else if (checkLine.contains(uuidMarker)) {
                uuid = checkLine.split(uuidMarker)[1].trim();
            } else if (checkLine.contains(versionMarker)) {
                version = checkLine.split(versionMarker)[1].trim();
            }
        }
        // If we get to end and haven't assigned, use fallback
        if (StringKit.isBlank(serialNumber)) {
            serialNumber = querySystemSerialNumber();
        }
        if (StringKit.isBlank(uuid)) {
            uuid = BsdSysctlKit.sysctl("kern.hostuuid", Normal.UNKNOWN);
        }
        return new Tuple(StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(model) ? Normal.UNKNOWN : model,
                StringKit.isBlank(serialNumber) ? Normal.UNKNOWN : serialNumber,
                StringKit.isBlank(uuid) ? Normal.UNKNOWN : uuid, StringKit.isBlank(version) ? Normal.UNKNOWN : version);
    }

    private static String querySystemSerialNumber() {
        String marker = "system.hardware.serial =";
        for (String checkLine : Executor.runNative("lshal")) {
            if (checkLine.contains(marker)) {
                return Parsing.getSingleQuoteStringValue(checkLine);
            }
        }
        return Normal.UNKNOWN;
    }

    @Override
    public String getManufacturer() {
        return manufModelSerialUuidVers.get().get(0);
    }

    @Override
    public String getModel() {
        return manufModelSerialUuidVers.get().get(1);
    }

    @Override
    public String getSerialNumber() {
        return manufModelSerialUuidVers.get().get(2);
    }

    @Override
    public String getHardwareUUID() {
        return manufModelSerialUuidVers.get().get(3);
    }

    @Override
    public Firmware createFirmware() {
        return new FreeBsdFirmware();
    }

    @Override
    public Baseboard createBaseboard() {
        return new UnixBaseboard(manufModelSerialUuidVers.get().get(0), manufModelSerialUuidVers.get().get(1),
                manufModelSerialUuidVers.get().get(2), manufModelSerialUuidVers.get().get(4));
    }

}
