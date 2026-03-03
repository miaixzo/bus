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
package org.miaixz.bus.health.builtin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * Disk Information
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@Getter
@Setter
@Builder
public class Disk {

    /**
     * Device name (e.g., /dev/sda1)
     */
    private String deviceName;

    /**
     * File system volume name
     */
    private String volumeName;

    /**
     * Volume label
     */
    private String label;

    /**
     * Logical volume name
     */
    private String logicalVolumeName;

    /**
     * Mount point (e.g., /mnt/data)
     */
    private String mountPoint;

    /**
     * File system description
     */
    private String description;

    /**
     * Mount options (e.g., rw, ro)
     */
    private String mountOptions;

    /**
     * File system type (e.g., ext4, xfs, vfat)
     */
    private String filesystemType;

    /**
     * UUID
     */
    private String uuid;

    /**
     * Total space
     */
    private Long totalSpace;

    /**
     * Used space
     */
    private Long usedSpace;

    /**
     * Free space
     */
    private Long freeSpace;

    /**
     * Usage percentage (usedSpace / totalSpace * 100)
     */
    private double usagePercent;

}
