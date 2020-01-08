/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
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
package org.aoju.bus.health.hardware.linux;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.linux.ProcUtils;
import org.aoju.bus.health.hardware.AbstractBaseboard;

import java.util.List;
import java.util.function.Supplier;

/**
 * Baseboard data obtained by sysfs
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
final class LinuxBaseboard extends AbstractBaseboard {

    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);

    private final Supplier<String> model = Memoizer.memoize(this::queryModel);

    private final Supplier<String> version = Memoizer.memoize(this::queryVersion);

    private final Supplier<String> serialNumber = Memoizer.memoize(this::querySerialNumber);

    @Override
    public String getManufacturer() {
        return manufacturer.get();
    }

    @Override
    public String getModel() {
        return model.get();
    }

    @Override
    public String getVersion() {
        return version.get();
    }

    @Override
    public String getSerialNumber() {
        return serialNumber.get();
    }

    private String queryManufacturer() {
        String result;
        if ((result = queryManufacturerFromSysfs()) == null && (result = queryProcCpu().manufacturer) == null) {
            return Builder.UNKNOWN;
        }
        return result;
    }

    private String queryModel() {
        String result;
        if ((result = queryModelFromSysfs()) == null && (result = queryProcCpu().model) == null) {
            return Builder.UNKNOWN;
        }
        return result;
    }

    private String queryVersion() {
        String result;
        if ((result = queryVersionFromSysfs()) == null && (result = queryProcCpu().version) == null) {
            return Builder.UNKNOWN;
        }
        return result;
    }

    private String querySerialNumber() {
        String result;
        if ((result = querySerialFromSysfs()) == null && (result = queryProcCpu().serialNumber) == null) {
            return Builder.UNKNOWN;
        }
        return result;
    }

    // Note: /sys/class/dmi/id symlinks here, but /sys/devices/* is the
    // official/approved path for sysfs information

    // $ ls /sys/devices/virtual/dmi/id/
    // bios_date board_vendor chassis_version product_version
    // bios_vendor board_version modalias subsystem
    // bios_version chassis_asset_tag power sys_vendor
    // board_asset_tag chassis_serial product_name uevent
    // board_name chassis_type product_serial
    // board_serial chassis_vendor product_uuid

    private String queryManufacturerFromSysfs() {
        final String boardVendor = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "board_vendor").trim();
        if (!boardVendor.isEmpty()) {
            return boardVendor;
        }
        return null;
    }

    private String queryModelFromSysfs() {
        final String boardName = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "board_name").trim();
        if (!boardName.isEmpty()) {
            return boardName;
        }
        return null;
    }

    private String queryVersionFromSysfs() {
        final String boardVersion = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "board_version").trim();
        if (!boardVersion.isEmpty()) {
            return boardVersion;
        }
        return null;
    }

    private String querySerialFromSysfs() {
        final String boardSerial = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "board_serial").trim();
        if (!boardSerial.isEmpty()) {
            return boardSerial;
        }
        return null;
    }

    private ProcCpuStrings queryProcCpu() {
        String pcManufacturer = null;
        String pcModel = null;
        String pcVersion = null;
        String pcSerialNumber = null;

        List<String> cpuInfo = Builder.readFile(ProcUtils.getProcPath() + ProcUtils.CPUINFO);
        for (String line : cpuInfo) {
            String[] splitLine = Builder.whitespacesColonWhitespace.split(line);
            if (splitLine.length < 2) {
                continue;
            }
            switch (splitLine[0]) {
                case "Hardware":
                    pcModel = splitLine[1];
                    break;
                case "Revision":
                    pcVersion = splitLine[1];
                    if (pcVersion.length() > 1) {
                        pcManufacturer = queryBoardManufacturer(pcVersion.charAt(1));
                    }
                    break;
                case "Serial":
                    pcSerialNumber = splitLine[1];
                    break;
                default:
                    // Do nothing
            }
        }
        return new ProcCpuStrings(pcManufacturer, pcModel, pcVersion, pcSerialNumber);
    }

    private String queryBoardManufacturer(char digit) {
        switch (digit) {
            case Symbol.C_ZERO:
                return "Sony UK";
            case Symbol.C_ONE:
                return "Egoman";
            case Symbol.C_TWO:
                return "Embest";
            case Symbol.C_THREE:
                return "Sony Japan";
            case Symbol.C_FOUR:
                return "Embest";
            case Symbol.C_FIVE:
                return "Stadium";
            default:
                return Builder.UNKNOWN;
        }
    }

    private static final class ProcCpuStrings {
        private final String manufacturer;
        private final String model;
        private final String version;
        private final String serialNumber;

        private ProcCpuStrings(String manufacturer, String model, String version, String serialNumber) {
            this.manufacturer = StringUtils.isBlank(manufacturer) ? Builder.UNKNOWN : manufacturer;
            this.model = StringUtils.isBlank(model) ? Builder.UNKNOWN : model;
            this.version = StringUtils.isBlank(version) ? Builder.UNKNOWN : version;
            this.serialNumber = StringUtils.isBlank(serialNumber) ? Builder.UNKNOWN : serialNumber;
        }
    }

}
