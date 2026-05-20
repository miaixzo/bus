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
package org.miaixz.bus.health.builtin.hardware.common;

import java.util.ArrayList;
import java.util.List;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Formats;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.GlobalMemory;
import org.miaixz.bus.health.builtin.hardware.PhysicalMemory;

/**
 * Memory info.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public abstract class AbstractGlobalMemory implements GlobalMemory {

    /**
     * Constructs a new AbstractGlobalMemory instance.
     */
    public AbstractGlobalMemory() {
        // No initialization required.
    }

    /**
     * Returns the physical memory.
     *
     * @return the get physical memory result
     */
    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        // dmidecode requires sudo permission but is the only option on Linux
        // and Unix
        return getPhysicalMemory(Executor.runPrivilegedNative("dmidecode --type 17"));
    }

    /**
     * Parse physical memory information from dmidecode output.
     *
     * @param dmi output of {@code dmidecode --type 17}
     * @return a list of physical memory modules
     */
    static List<PhysicalMemory> getPhysicalMemory(List<String> dmi) {
        List<PhysicalMemory> pmList = new ArrayList<>();
        int bank = 0;
        String bankLabel = Normal.UNKNOWN;
        String locator = Normal.EMPTY;
        long capacity = 0L;
        long speed = 0L;
        String manufacturer = Normal.UNKNOWN;
        String memoryType = Normal.UNKNOWN;
        String partNumber = Normal.UNKNOWN;
        String serialNumber = Normal.UNKNOWN;
        for (String line : dmi) {
            if (line.trim().contains("DMI type 17")) {
                // Save previous bank
                if (bank++ > 0) {
                    if (capacity > 0) {
                        pmList.add(
                                new PhysicalMemory(bankLabel + locator, capacity, speed, manufacturer, memoryType,
                                        partNumber, serialNumber));
                    }
                    bankLabel = Normal.UNKNOWN;
                    locator = Normal.EMPTY;
                    capacity = 0L;
                    speed = 0L;
                    manufacturer = Normal.UNKNOWN;
                    memoryType = Normal.UNKNOWN;
                    partNumber = Normal.UNKNOWN;
                    serialNumber = Normal.UNKNOWN;
                }
            } else if (bank > 0) {
                String[] split = line.trim().split(Symbol.COLON);
                if (split.length == 2) {
                    switch (split[0]) {
                        case "Bank Locator":
                            bankLabel = split[1].trim();
                            break;

                        case "Locator":
                            locator = "/" + split[1].trim();
                            break;

                        case "Size":
                            capacity = Parsing.parseDecimalMemorySizeToBinary(split[1].trim());
                            break;

                        case "Type":
                            memoryType = split[1].trim();
                            break;

                        case "Speed":
                            speed = Parsing.parseSpeed(split[1]);
                            break;

                        case "Manufacturer":
                            manufacturer = split[1].trim();
                            break;

                        case "PartNumber":
                        case "Part Number":
                            partNumber = split[1].trim();
                            break;

                        case "Serial Number":
                            serialNumber = split[1].trim();
                            break;

                        default:
                            break;
                    }
                }
            }
        }
        if (capacity > 0) {
            pmList.add(
                    new PhysicalMemory(bankLabel + locator, capacity, speed, manufacturer, memoryType, partNumber,
                            serialNumber));
        }
        return pmList;
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        String sb = "Available: " + Formats.formatBytes(getAvailable()) + "/" + Formats.formatBytes(getTotal());
        return sb;
    }

}
