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
package org.aoju.bus.health.unix.freebsd.hardware;

import org.aoju.bus.core.annotation.Immutable;
import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.tuple.Triple;
import org.aoju.bus.core.toolkit.StringKit;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Executor;
import org.aoju.bus.health.Memoize;
import org.aoju.bus.health.builtin.hardware.AbstractFirmware;

import java.util.function.Supplier;

/**
 * Firmware information from dmidecode
 *
 * @author Kimi Liu
 * @version 6.0.9
 * @since JDK 1.8+
 */
@Immutable
final class FreeBsdFirmware extends AbstractFirmware {

    private final Supplier<Triple<String, String, String>> manufVersRelease = Memoize.memoize(FreeBsdFirmware::readDmiDecode);

    private static Triple<String, String, String> readDmiDecode() {
        String manufacturer = null;
        String version = null;
        String releaseDate = Normal.EMPTY;

        final String manufacturerMarker = "Vendor:";
        final String versionMarker = "Version:";
        final String releaseDateMarker = "Release Date:";

        // Only works with root permissions but it's all we've got
        for (final String checkLine : Executor.runNative("dmidecode -t bios")) {
            if (checkLine.contains(manufacturerMarker)) {
                manufacturer = checkLine.split(manufacturerMarker)[1].trim();
            } else if (checkLine.contains(versionMarker)) {
                version = checkLine.split(versionMarker)[1].trim();
            } else if (checkLine.contains(releaseDateMarker)) {
                releaseDate = checkLine.split(releaseDateMarker)[1].trim();
            }
        }
        releaseDate = Builder.parseMmDdYyyyToYyyyMmDD(releaseDate);
        return Triple.of(StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(version) ? Normal.UNKNOWN : version,
                StringKit.isBlank(releaseDate) ? Normal.UNKNOWN : releaseDate);
    }

    @Override
    public String getManufacturer() {
        return manufVersRelease.get().getLeft();
    }

    @Override
    public String getVersion() {
        return manufVersRelease.get().getMiddle();
    }

    @Override
    public String getReleaseDate() {
        return manufVersRelease.get().getRight();
    }

}
