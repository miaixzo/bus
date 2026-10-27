/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org and other contributors.                ~
 ~                                                                           ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");           ~
 ~ you may not use this file except in compliance with the License.          ~
 ~ You may obtain a copy of the License at                                   ~
 ~                                                                           ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                          ~
 ~                                                                           ~
 ~ Unless required by applicable law or agreed to in writing, software       ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,         ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  ~
 ~ See the License for the specific language governing permissions and       ~
 ~ limitations under the License.                                            ~
 ~                                                                           ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.freebsd.hardware;

import java.util.Map;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractFirmware;
import org.miaixz.bus.health.unix.freebsd.driver.Kenv;

/**
 * Firmware information from SMBIOS kernel environment values with a {@code dmidecode} fallback.
 *
 * @author Kimi Liu
 */
@Immutable
final class FreeBsdFirmware extends AbstractFirmware {

    /**
     * The manufVersRelease value.
     */
    private final SupplierX<Triplet<String, String, String>> manufVersRelease = Memoizer
            .memoize(FreeBsdFirmware::readFirmware);

    /**
     * Reads firmware values from the kernel environment and fills missing values from {@code dmidecode}.
     *
     * @return the read dmi decode result
     */
    private static Triplet<String, String, String> readFirmware() {
        Map<String, String> environment = Kenv.queryKenv();
        String manufacturer = environment.get("smbios.bios.vendor");
        String version = environment.get("smbios.bios.version");
        String releaseDate = Parsing
                .parseMmDdYyyyToYyyyMmDD(environment.getOrDefault("smbios.bios.reldate", Normal.EMPTY));

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

        if (StringKit.isBlank(manufacturer) || StringKit.isBlank(version) || StringKit.isBlank(releaseDate)) {
            for (String checkLine : Executor.runNative("dmidecode -t bios")) {
                if (checkLine.contains(manufacturerMarker) && StringKit.isBlank(manufacturer)) {
                    manufacturer = Parsing.getTextAfterString(checkLine, manufacturerMarker).trim();
                } else if (checkLine.contains(versionMarker) && StringKit.isBlank(version)) {
                    version = Parsing.getTextAfterString(checkLine, versionMarker).trim();
                } else if (checkLine.contains(releaseDateMarker) && StringKit.isBlank(releaseDate)) {
                    releaseDate = Parsing.getTextAfterString(checkLine, releaseDateMarker).trim();
                }
            }
        }
        releaseDate = Parsing.parseMmDdYyyyToYyyyMmDD(releaseDate);
        return Triplet.of(
                StringKit.isBlank(manufacturer) ? Normal.UNKNOWN : manufacturer,
                StringKit.isBlank(version) ? Normal.UNKNOWN : version,
                StringKit.isBlank(releaseDate) ? Normal.UNKNOWN : releaseDate);
    }

    /**
     * Returns the manufacturer.
     *
     * @return the get manufacturer result
     */
    @Override
    public String getManufacturer() {
        return manufVersRelease.get().getLeft();
    }

    /**
     * Returns the version.
     *
     * @return the get version result
     */
    @Override
    public String getVersion() {
        return manufVersRelease.get().getMiddle();
    }

    /**
     * Returns the release date.
     *
     * @return the get release date result
     */
    @Override
    public String getReleaseDate() {
        return manufVersRelease.get().getRight();
    }

}
