/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
package org.aoju.bus.health.hardware.linux;

import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.common.linux.ProcUtils;
import org.aoju.bus.health.hardware.AbstractComputerSystem;
import org.aoju.bus.health.hardware.Baseboard;
import org.aoju.bus.health.hardware.Firmware;

import java.util.List;
import java.util.function.Supplier;

/**
 * Hardware data obtained from sysfs.
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
final class LinuxComputerSystem extends AbstractComputerSystem {

    private final Supplier<String> manufacturer = Memoizer.memoize(this::queryManufacturer);

    private final Supplier<String> model = Memoizer.memoize(this::queryModel);

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
    public String getSerialNumber() {
        return serialNumber.get();
    }

    @Override
    public Firmware createFirmware() {
        return new LinuxFirmware();
    }

    @Override
    public Baseboard createBaseboard() {
        return new LinuxBaseboard();
    }

    private String queryManufacturer() {
        String result;
        if ((result = queryManufacturerFromSysfs()) == null && (result = queryManufacturerFromProcCpu()) == null) {
            return Builder.UNKNOWN;
        }
        return result;
    }

    private String queryModel() {
        String result;
        if ((result = queryModelFromSysfs()) == null && (result = queryModelFromDeviceTree()) == null
                && (result = queryModelFromLshw()) == null) {
            return Builder.UNKNOWN;
        }
        return result;
    }

    private String querySerialNumber() {
        String result;
        if ((result = querySerialFromSysfs()) == null && (result = querySerialFromDmiDecode()) == null
                && (result = querySerialFromLshal()) == null && (result = querySerialFromLshw()) == null) {
            return Builder.UNKNOWN;
        }
        return result;
    }

    private String queryManufacturerFromSysfs() {
        final String sysVendor = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "sys_vendor").trim();
        if (!sysVendor.isEmpty()) {
            return sysVendor;
        }
        return null;
    }

    private String queryManufacturerFromProcCpu() {
        List<String> cpuInfo = Builder.readFile(ProcUtils.getProcPath() + ProcUtils.CPUINFO);
        for (String line : cpuInfo) {
            if (line.startsWith("CPU implementer")) {
                int part = Builder.parseLastInt(line, 0);
                switch (part) {
                    case 0x41:
                        return "ARM";
                    case 0x42:
                        return "Broadcom";
                    case 0x43:
                        return "Cavium";
                    case 0x44:
                        return "DEC";
                    case 0x4e:
                        return "Nvidia";
                    case 0x50:
                        return "APM";
                    case 0x51:
                        return "Qualcomm";
                    case 0x53:
                        return "Samsung";
                    case 0x56:
                        return "Marvell";
                    case 0x66:
                        return "Faraday";
                    case 0x69:
                        return "Intel";
                    default:
                        return null;
                }
            }
        }
        return null;
    }

    private String queryModelFromSysfs() {
        final String productName = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "product_name").trim();
        final String productVersion = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "product_version")
                .trim();
        if (productName.isEmpty()) {
            if (!productVersion.isEmpty()) {
                return productVersion;
            }
        } else {
            if (!productVersion.isEmpty() && !"None".equals(productVersion)) {
                return productName + " (version: " + productVersion + ")";
            } else {
                return productName;
            }
        }
        return null;
    }

    private String queryModelFromDeviceTree() {
        String modelStr = Builder.getStringFromFile("/sys/firmware/devicetree/base/model");
        if (!modelStr.isEmpty()) {
            return modelStr.replace("Machine: ", "");
        }
        return null;
    }

    private String queryModelFromLshw() {
        String modelMarker = "product:";
        for (String checkLine : Command.runNative("lshw -C system")) {
            if (checkLine.contains(modelMarker)) {
                return checkLine.split(modelMarker)[1].trim();
            }
        }
        return null;
    }

    private String querySerialFromSysfs() {
        // These sysfs files accessible by root, or can be chmod'd at boot time
        // to enable access without root
        String serial = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "product_serial");
        if (serial.isEmpty() || "None".equals(serial)) {
            serial = Builder.getStringFromFile(Builder.SYSFS_SERIAL_PATH + "board_serial");
            if (serial.isEmpty() || "None".equals(serial)) {
                return null;
            }
            return serial;
        }
        return null;
    }

    private String querySerialFromDmiDecode() {
        // If root privileges this will work
        String marker = "Serial Number:";
        for (String checkLine : Command.runNative("dmidecode -t system")) {
            if (checkLine.contains(marker)) {
                return checkLine.split(marker)[1].trim();
            }
        }
        return null;
    }

    private String querySerialFromLshal() {
        String marker = "system.hardware.serial =";
        for (String checkLine : Command.runNative("lshal")) {
            if (checkLine.contains(marker)) {
                return Builder.getSingleQuoteStringValue(checkLine);
            }
        }
        return null;
    }

    private String querySerialFromLshw() {
        String serialMarker = "serial:";
        for (String checkLine : Command.runNative("lshw -C system")) {
            if (checkLine.contains(serialMarker)) {
                return checkLine.split(serialMarker)[1].trim();
            }
        }
        return null;
    }

}
