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
package org.miaixz.bus.health.unix.openbsd.hardware;

import java.util.List;
import java.util.function.Supplier;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.core.lang.tuple.Triplet;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.common.AbstractFirmware;

/**
 * OpenBSD Firmware implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public class OpenBsdFirmware extends AbstractFirmware {

    /**
     * Creates a new OpenBsdFirmware instance.
     */
    public OpenBsdFirmware() {
    }

    /**
     * The manufVersRelease value.
     */
    private final Supplier<Triplet<String, String, String>> manufVersRelease = Memoizer
            .memoize(OpenBsdFirmware::readDmesg);

    /**
     * Reads the dmesg.
     *
     * @return the read dmesg result
     */
    private static Triplet<String, String, String> readDmesg() {
        String version = null;
        String vendor = null;
        String releaseDate = Normal.EMPTY;

        List<String> dmesg = Executor.runNative("dmesg");
        for (String line : dmesg) {
            // bios0 at mainbus0: SMBIOS rev. 2.7 @ 0xdcc0e000 (67 entries)
            // bios0: vendor LENOVO version "GLET90WW (2.44 )" date 09/13/2017
            // bios0: LENOVO 20AWA08J00
            if (line.startsWith("bios0: vendor")) {
                version = Parsing.getStringBetween(line, '"');
                releaseDate = Parsing.parseMmDdYyyyToYyyyMmDD(Parsing.parseLastString(line));
                vendor = line.split("vendor")[1].trim();
            }
        }
        return Triplet.of(
                StringKit.isBlank(vendor) ? Normal.UNKNOWN : vendor,
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
