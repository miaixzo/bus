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
 *                                                                               *
 ********************************************************************************/
package org.aoju.bus.health.mac.hardware;

import com.sun.jna.platform.mac.IOKit.IOIterator;
import com.sun.jna.platform.mac.IOKit.IORegistryEntry;
import com.sun.jna.platform.mac.IOKitUtil;
import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Quintet;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.hardware.AbstractFirmware;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * Firmware data obtained from ioreg.
 *
 * @author Kimi Liu
 * @version 6.1.2
 * @since JDK 1.8+
 */
@Immutable
final class MacFirmware extends AbstractFirmware {

    private final Supplier<Quintet<String, String, String, String, String>> manufNameDescVersRelease = Memoize.memoize(
            MacFirmware::queryEfi);

    private static Quintet<String, String, String, String, String> queryEfi() {
        String manufacturer = null;
        String name = null;
        String description = null;
        String version = null;
        String releaseDate = null;

        IORegistryEntry platformExpert = IOKitUtil.getMatchingService("IOPlatformExpertDevice");
        if (platformExpert != null) {
            IOIterator iter = platformExpert.getChildIterator("IODeviceTree");
            if (iter != null) {
                IORegistryEntry entry = iter.next();
                while (entry != null) {
                    switch (entry.getName()) {
                        case "rom":
                            byte[] data = entry.getByteArrayProperty("vendor");
                            if (data != null) {
                                manufacturer = new String(data, StandardCharsets.UTF_8).trim();
                            }
                            data = entry.getByteArrayProperty("version");
                            if (data != null) {
                                version = new String(data, StandardCharsets.UTF_8).trim();
                            }
                            data = entry.getByteArrayProperty("release-date");
                            if (data != null) {
                                releaseDate = new String(data, StandardCharsets.UTF_8).trim();
                            }
                            break;
                        case "chosen":
                            data = entry.getByteArrayProperty("booter-name");
                            if (data != null) {
                                name = new String(data, StandardCharsets.UTF_8).trim();
                            }
                            break;
                        case "efi":
                            data = entry.getByteArrayProperty("firmware-abi");
                            if (data != null) {
                                description = new String(data, StandardCharsets.UTF_8).trim();
                            }
                            break;
                        default:
                            break;
                    }
                    entry.release();
                    entry = iter.next();
                }
                iter.release();
            }
            platformExpert.release();
        }
        return new Quintet<>(StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(name) ? Normal.UNKNOWN : name,
                StringKit.isBlank(description) ? Normal.UNKNOWN : description,
                StringKit.isBlank(version) ? Normal.UNKNOWN : version,
                StringKit.isBlank(releaseDate) ? Normal.UNKNOWN : releaseDate);
    }

    @Override
    public String getManufacturer() {
        return manufNameDescVersRelease.get().getA();
    }

    @Override
    public String getName() {
        return manufNameDescVersRelease.get().getB();
    }

    @Override
    public String getDescription() {
        return manufNameDescVersRelease.get().getC();
    }

    @Override
    public String getVersion() {
        return manufNameDescVersRelease.get().getD();
    }

    @Override
    public String getReleaseDate() {
        return manufNameDescVersRelease.get().getE();
    }

}
