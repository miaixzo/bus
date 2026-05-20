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
package org.miaixz.bus.health.linux.driver;

import java.util.List;
import java.util.Locale;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Executor;

/**
 * Utility to read info from {@code dmidecode}
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class Dmidecode {

    /**
     * Constructs a new Dmidecode instance.
     */
    public Dmidecode() {
        // No initialization required.
    }

    // $ sudo dmidecode -t bios
    // # dmidecode 2.11
    // SMBIOS 2.4 present.
    //
    // Handle 0x0000, DMI type 0, 24 bytes
    // BIOS Information
    // Vendor: Phoenix Technologies LTD
    // Version: 6.00
    // Release Date: 07/02/2015
    // Address: 0xEA5E0
    // Runtime Size: 88608 bytes
    // ROM Size: 64 kB
    // Characteristics:
    // ISA is supported
    // PCI is supported
    // PC Card (PCMCIA) is supported
    // PNP is supported
    // APM is supported
    // BIOS is upgradeable
    // BIOS shadowing is allowed
    // ESCD support is available
    // Boot from CD is supported
    // Selectable boot is supported
    // EDD is supported
    // Print screen service is supported (int 5h)
    // 8042 keyboard services are supported (int 9h)
    // Serial services are supported (int 14h)
    // Printer services are supported (int 17h)
    // CGA/mono video services are supported (int 10h)
    // ACPI is supported
    // Smart battery is supported
    // BIOS boot specification is supported
    // Function key-initiated network boot is supported
    // Targeted content distribution is supported
    // BIOS Revision: 4.6
    // Firmware Revision: 0.0

    /**
     * Query the serial number from dmidecode
     *
     * @return The serial number if available, null otherwise
     */
    public static String querySerialNumber() {
        return querySerialNumber(Executor.runPrivilegedNative("dmidecode -t system"));
    }

    /**
     * Parse the serial number from dmidecode output.
     *
     * @param lines output of {@code dmidecode -t system}
     * @return The serial number if available, null otherwise
     */
    static String querySerialNumber(List<String> lines) {
        String marker = "Serial Number:";
        for (String checkLine : lines) {
            if (checkLine.contains(marker)) {
                return checkLine.split(marker)[1].trim();
            }
        }
        return null;
    }

    /**
     * Query the UUID from dmidecode
     *
     * @return The UUID if available, null otherwise
     */
    public static String queryUUID() {
        return queryUUID(Executor.runPrivilegedNative("dmidecode -t system"));
    }

    /**
     * Parse the UUID from dmidecode output.
     *
     * @param lines output of {@code dmidecode -t system}
     * @return The UUID if available, null otherwise
     */
    static String queryUUID(List<String> lines) {
        String marker = "UUID:";
        for (String checkLine : lines) {
            if (checkLine.contains(marker)) {
                return checkLine.split(marker)[1].trim();
            }
        }
        return null;
    }

    /**
     * Query the name and revision from dmidecode
     *
     * @return The a pair containing the name and revision if available, null values in the pair otherwise
     */
    public static Pair<String, String> queryBiosNameRev() {
        return queryBiosNameRev(Executor.runPrivilegedNative("dmidecode -t bios"));
    }

    /**
     * Parse the BIOS name and revision from dmidecode output.
     *
     * @param lines output of {@code dmidecode -t bios}
     * @return The a pair containing the name and revision if available, null values in the pair otherwise
     */
    static Pair<String, String> queryBiosNameRev(List<String> lines) {
        String biosName = null;
        String revision = null;

        final String biosMarker = "SMBIOS";
        final String revMarker = "bios revision:";

        for (final String checkLine : lines) {
            if (checkLine.contains(biosMarker)) {
                String[] biosArr = Pattern.SPACES_PATTERN.split(checkLine);
                if (biosArr.length >= 2) {
                    biosName = biosArr[0] + Symbol.SPACE + biosArr[1];
                }
            }
            int revIdx = checkLine.toLowerCase(Locale.ROOT).indexOf(revMarker);
            if (revIdx >= 0) {
                revision = checkLine.substring(revIdx + revMarker.length()).trim();
                // SMBIOS should be first line so if we're here we are done iterating
                break;
            }
        }
        return Pair.of(biosName, revision);
    }

}
