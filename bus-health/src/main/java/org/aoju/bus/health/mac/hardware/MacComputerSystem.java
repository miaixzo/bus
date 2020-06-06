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
package org.aoju.bus.health.mac.hardware;

import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Triple;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.builtin.hardware.AbstractComputerSystem;
import org.aoju.bus.health.builtin.hardware.Baseboard;
import org.aoju.bus.health.builtin.hardware.Firmware;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import static org.aoju.bus.health.Memoize.memoize;

/**
 * Hardware data obtained from ioreg.
 *
 * @author Kimi Liu
 * @version 5.9.8
 * @since JDK 1.8+
 */
@Immutable
final class MacComputerSystem extends AbstractComputerSystem {

    private final Supplier<Triple<String, String, String>> manufacturerModelSerial = memoize(
            MacComputerSystem::platformExpert);

    private static Triple<String, String, String> platformExpert() {
        String manufacturer = null;
        String model = null;
        String serialNumber = null;
        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = new String(data, StandardCharsets.UTF_8).trim();
            }
            data = platformExpert.getByteArrayProperty("model");
            if (data != null) {
                model = new String(data, StandardCharsets.UTF_8).trim();
            }
            serialNumber = platformExpert.getStringProperty("IOPlatformSerialNumber");
            platformExpert.release();
        }
        return Triple.of(StringKit.isBlank(manufacturer) ? "Apple Inc." : manufacturer,
                StringKit.isBlank(model) ? Normal.UNKNOWN : model,
                StringKit.isBlank(serialNumber) ? Normal.UNKNOWN : serialNumber);
    }

    @Override
    public String getManufacturer() {
        return manufacturerModelSerial.get().getLeft();
    }

    @Override
    public String getModel() {
        return manufacturerModelSerial.get().getMiddle();
    }

    @Override
    public String getSerialNumber() {
        return manufacturerModelSerial.get().getRight();
    }

    @Override
    public Firmware createFirmware() {
        return new MacFirmware();
    }

    @Override
    public Baseboard createBaseboard() {
        return new MacBaseboard();
    }

}
