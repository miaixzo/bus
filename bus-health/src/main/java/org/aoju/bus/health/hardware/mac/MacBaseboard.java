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
package org.aoju.bus.health.hardware.mac;

import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.hardware.AbstractBaseboard;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Baseboard data obtained from ioreg
 *
 * @author Kimi Liu
 * @version 5.8.1
 * @since JDK 1.8+
 */
final class MacBaseboard extends AbstractBaseboard {

    private final Supplier<PlatformStrings> platform = Memoizer.memoize(this::queryPlatform);

    @Override
    public String getManufacturer() {
        return platform.get().manufacturer;
    }

    @Override
    public String getModel() {
        return platform.get().model;
    }

    @Override
    public String getVersion() {
        return platform.get().version;
    }

    @Override
    public String getSerialNumber() {
        return platform.get().serialNumber;
    }

    private PlatformStrings queryPlatform() {
        String manufacturer = null;
        String model = null;
        String version = null;
        String serialNumber = null;

        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            byte[] data = platformExpert.getByteArrayProperty("manufacturer");
            if (data != null) {
                manufacturer = new String(data, StandardCharsets.UTF_8);
            }
            data = platformExpert.getByteArrayProperty("board-id");
            if (data != null) {
                model = new String(data, StandardCharsets.UTF_8);
            }
            data = platformExpert.getByteArrayProperty("version");
            if (data != null) {
                version = new String(data, StandardCharsets.UTF_8);
            }
            serialNumber = platformExpert.getStringProperty("IOPlatformSerialNumber");
            platformExpert.release();
        }
        return new PlatformStrings(manufacturer, model, version, serialNumber);
    }

    private static final class PlatformStrings {
        private final String manufacturer;
        private final String model;
        private final String version;
        private final String serialNumber;

        private PlatformStrings(String manufacturer, String model, String version, String serialNumber) {
            this.manufacturer = StringUtils.isBlank(manufacturer) ? "Apple Inc." : manufacturer;
            this.model = StringUtils.isBlank(model) ? Builder.UNKNOWN : model;
            this.version = StringUtils.isBlank(version) ? Builder.UNKNOWN : version;
            this.serialNumber = StringUtils.isBlank(serialNumber) ? Builder.UNKNOWN : serialNumber;
        }
    }
}
