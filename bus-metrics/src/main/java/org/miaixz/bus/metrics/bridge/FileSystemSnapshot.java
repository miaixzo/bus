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

/**
 * Immutable filesystem usage snapshot.
 *
 * @param device      filesystem device
 * @param mountpoint  mounted path
 * @param type        filesystem type
 * @param mode        access mode
 * @param totalBytes  total capacity in bytes
 * @param freeBytes   free capacity in bytes
 * @param usableBytes capacity usable by the current process in bytes
 * @author Kimi Liu
 */
public record FileSystemSnapshot(String device, String mountpoint, String type, String mode, long totalBytes,
        long freeBytes, long usableBytes) {

    /**
     * Validates and normalizes filesystem identity and sizes.
     */
    public FileSystemSnapshot {
        device = normalized(device, "unknown");
        mountpoint = normalized(mountpoint, "unknown");
        type = normalized(type, "unknown").toLowerCase();
        mode = "ro".equals(mode) || "rw".equals(mode) ? mode : "unknown";
        if (totalBytes < 0 || freeBytes < 0 || usableBytes < 0) {
            throw new IllegalArgumentException("Filesystem sizes must not be negative");
        }
        freeBytes = Math.min(freeBytes, totalBytes);
        usableBytes = Math.min(usableBytes, freeBytes);
    }

    /**
     * Normalizes a filesystem dimension while preserving a stable fallback.
     *
     * @param value    source value
     * @param fallback value used when the source is blank
     * @return normalized non-blank value
     */
    private static String normalized(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

}
