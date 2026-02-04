/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.builtin.hardware;

import org.miaixz.bus.core.lang.annotation.Immutable;
import org.miaixz.bus.health.Formats;

/**
 * The PhysicalMemory class represents a physical memory device located on a computer system and available to the
 * operating system.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Immutable
public class PhysicalMemory {

    private final String bankLabel;
    private final long capacity;
    private final long clockSpeed;
    private final String manufacturer;
    private final String memoryType;
    private final String partNumber;
    private final String serialNumber;

    public PhysicalMemory(String bankLabel, long capacity, long clockSpeed, String manufacturer, String memoryType,
            String partNumber, String serialNumber) {
        this.bankLabel = bankLabel;
        this.capacity = capacity;
        this.clockSpeed = clockSpeed;
        this.manufacturer = manufacturer;
        this.memoryType = memoryType;
        this.partNumber = partNumber;
        this.serialNumber = serialNumber;
    }

    /**
     * The bank and/or slot label.
     *
     * @return the bank label
     */
    public String getBankLabel() {
        return bankLabel;
    }

    /**
     * The capacity of memory bank in bytes.
     *
     * @return the capacity
     */
    public long getCapacity() {
        return capacity;
    }

    /**
     * The configured memory clock speed in hertz.
     * <p>
     * For DDR memory, this is the data transfer rate, which is a multiple of the actual bus clock speed.
     *
     * @return the clock speed, if avaialable. If unknown, returns -1.
     */
    public long getClockSpeed() {
        return clockSpeed;
    }

    /**
     * The manufacturer of the physical memory.
     *
     * @return the manufacturer
     */
    public String getManufacturer() {
        return manufacturer;
    }

    /**
     * The type of physical memory
     *
     * @return the memory type
     */
    public String getMemoryType() {
        return memoryType;
    }

    /**
     * The part number of the physical memory
     *
     * @return the part number
     */
    public String getPartNumber() {
        return partNumber;
    }

    /**
     * The serial number of the physical memory
     *
     * @return the serial number
     */
    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Bank label: " + getBankLabel());
        sb.append(", Capacity: " + Formats.formatBytes(getCapacity()));
        sb.append(", Clock speed: " + Formats.formatHertz(getClockSpeed()));
        sb.append(", Manufacturer: " + getManufacturer());
        sb.append(", Memory type: " + getMemoryType());
        sb.append(", Part number: " + getPartNumber());
        sb.append(", Serial number: " + getSerialNumber());
        return sb.toString();
    }

}
