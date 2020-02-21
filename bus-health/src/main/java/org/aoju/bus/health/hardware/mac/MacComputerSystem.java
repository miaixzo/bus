/*
 * The MIT License
 *
 * Copyright (c) 2015-2020 aoju.org All rights reserved.
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
package org.aoju.bus.health.hardware.mac;

import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.hardware.AbstractComputerSystem;
import org.aoju.bus.health.hardware.Baseboard;
import org.aoju.bus.health.hardware.Firmware;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Hardware data obtained from ioreg.
 *
 * @author Kimi Liu
 * @version 5.6.3
 * @since JDK 1.8+
 */
final class MacComputerSystem extends AbstractComputerSystem {

    private final Supplier<ManufacturerModelSerial> profileSystem = Memoizer.memoize(this::platformExpert);

    @Override
    public String getManufacturer() {
        return profileSystem.get().manufacturer;
    }

    @Override
    public String getModel() {
        return profileSystem.get().model;
    }

    @Override
    public String getSerialNumber() {
        return profileSystem.get().serialNumber;
    }

    @Override
    public Firmware createFirmware() {
        return new MacFirmware();
    }

    @Override
    public Baseboard createBaseboard() {
        return new MacBaseboard();
    }

    private ManufacturerModelSerial platformExpert() {
        String manufacturer = null;
        String model = null;
        String serialNumber = null;
        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = new String(data, StandardCharsets.UTF_8);
            }
            data = platformExpert.getByteArrayProperty("model");
            if (data != null) {
                model = new String(data, StandardCharsets.UTF_8);
            }
            serialNumber = platformExpert.getStringProperty("IOPlatformSerialNumber");
            platformExpert.release();
        }
        return new ManufacturerModelSerial(manufacturer, model, serialNumber);
    }

    private static final class ManufacturerModelSerial {
        private final String manufacturer;
        private final String model;
        private final String serialNumber;

        private ManufacturerModelSerial(String manufacturer, String model, String serialNumber) {
            this.manufacturer = StringUtils.isBlank(manufacturer) ? "Apple Inc." : manufacturer;
            this.model = StringUtils.isBlank(model) ? Builder.UNKNOWN : model;
            this.serialNumber = StringUtils.isBlank(serialNumber) ? Builder.UNKNOWN : serialNumber;
        }
    }
}
