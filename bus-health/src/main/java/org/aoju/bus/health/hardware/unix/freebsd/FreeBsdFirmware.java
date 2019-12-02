/*
 * The MIT License
 *
 * Copyright (c) 2017 aoju.org All rights reserved.
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
package org.aoju.bus.health.hardware.unix.freebsd;

import org.aoju.bus.core.utils.StringUtils;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.Memoizer;
import org.aoju.bus.health.hardware.AbstractFirmware;

import java.util.function.Supplier;

/**
 * @author Kimi Liu
 * @version 5.3.0
 * @since JDK 1.8+
 */
final class FreeBsdFirmware extends AbstractFirmware {

    private final Supplier<DmidecodeStrings> readDmiDecode = Memoizer.memoize(this::readDmiDecode);

    @Override
    public String getManufacturer() {
        return readDmiDecode.get().manufacturer;
    }

    @Override
    public String getVersion() {
        return readDmiDecode.get().version;
    }

    @Override
    public String getReleaseDate() {
        return readDmiDecode.get().releaseDate;
    }

    /*
     * Name and Description not set
     */

    private DmidecodeStrings readDmiDecode() {
        String manufacturer = null;
        String version = null;
        String releaseDate = "";

        // $ sudo dmidecode -t bios
        // # dmidecode 3.0
        // Scanning /dev/mem for entry point.
        // SMBIOS 2.7 present.
        //
        // Handle 0x0000, DMI type 0, 24 bytes
        // BIOS Information
        // Vendor: Parallels Software International Inc.
        // Version: 11.2.1 (32626)
        // Release Date: 07/15/2016
        // ... <snip> ...
        // BIOS Revision: 11.2
        // Firmware Revision: 11.2

        final String manufacturerMarker = "Vendor:";
        final String versionMarker = "Version:";
        final String releaseDateMarker = "Release Date:";

        // Only works with root permissions but it's all we've got
        for (final String checkLine : Command.runNative("dmidecode -t bios")) {
            if (checkLine.contains(manufacturerMarker)) {
                manufacturer = checkLine.split(manufacturerMarker)[1].trim();
            } else if (checkLine.contains(versionMarker)) {
                version = checkLine.split(versionMarker)[1].trim();
            } else if (checkLine.contains(releaseDateMarker)) {
                releaseDate = checkLine.split(releaseDateMarker)[1].trim();
            }
        }
        releaseDate = Builder.parseMmDdYyyyToYyyyMmDD(releaseDate);
        return new DmidecodeStrings(manufacturer, version, releaseDate);
    }

    private static final class DmidecodeStrings {
        private final String manufacturer;
        private final String version;
        private final String releaseDate;

        private DmidecodeStrings(String manufacturer, String version, String releaseDate) {
            this.manufacturer = StringUtils.isBlank(manufacturer) ? Builder.UNKNOWN : manufacturer;
            this.version = StringUtils.isBlank(version) ? Builder.UNKNOWN : version;
            this.releaseDate = StringUtils.isBlank(releaseDate) ? Builder.UNKNOWN : releaseDate;
        }
    }

}
