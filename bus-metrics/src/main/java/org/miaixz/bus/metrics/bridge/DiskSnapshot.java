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
package org.miaixz.bus.metrics.bridge;

import java.util.Objects;
import java.util.OptionalLong;

/**
 * Immutable cumulative disk-I/O snapshot without identifying hardware metadata.
 *
 * @param device             stable device name
 * @param sizeBytes          device size when available
 * @param reads              cumulative read operations when available
 * @param readBytes          cumulative bytes read when available
 * @param writes             cumulative write operations when available
 * @param writeBytes         cumulative bytes written when available
 * @param transferTimeMillis cumulative transfer time when available
 * @author Kimi Liu
 */
public record DiskSnapshot(String device, OptionalLong sizeBytes, OptionalLong reads, OptionalLong readBytes,
        OptionalLong writes, OptionalLong writeBytes, OptionalLong transferTimeMillis) {

    /**
     * Validates the device identity and optional values.
     */
    public DiskSnapshot {
        if (device == null || device.isBlank()) {
            throw new IllegalArgumentException("Disk device must not be blank");
        }
        device = device.trim();
        sizeBytes = checked(sizeBytes, "Disk size");
        reads = checked(reads, "Disk reads");
        readBytes = checked(readBytes, "Disk read bytes");
        writes = checked(writes, "Disk writes");
        writeBytes = checked(writeBytes, "Disk write bytes");
        transferTimeMillis = checked(transferTimeMillis, "Disk transfer time");
    }

    /**
     * Validates one optional cumulative disk value.
     *
     * @param value optional value
     * @param label diagnostic label
     * @return the validated value
     */
    private static OptionalLong checked(OptionalLong value, String label) {
        Objects.requireNonNull(value, label + " must not be null");
        if (value.isPresent() && value.getAsLong() < 0) {
            throw new IllegalArgumentException(label + " must not be negative");
        }
        return value;
    }

}
