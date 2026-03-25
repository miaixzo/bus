/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ 
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.builtin.hardware;

import java.util.Map;
import java.util.Set;

import org.miaixz.bus.core.lang.annotation.Immutable;

/**
 * A logical volume group implemented as part of logical volume management, combining the space on one or more storage
 * devices such as disks or partitions (physical volumes) into a storage pool, and subsequently allocating that space to
 * virtual partitions (logical volumes) as block devices accessible to the file system.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@Immutable
public interface LogicalVolumeGroup {

    /**
     * Gets the logical volume group name.
     *
     * @return The name of the logical volume group.
     */
    String getName();

    /**
     * Gets a set of all physical volumes in this volume group.
     *
     * @return A set with the names of the physical volumes.
     */
    Set<String> getPhysicalVolumes();

    /**
     * Gets a map containing information about the logical volumes in the logical volume group, represented to the file
     * system as block devices. The keyset for the map represents a collection of the logical volumes, while the values
     * associated with these keys represent the physical volumes mapped to each logical volume (if known).
     *
     * @return A map with the logical volume names as the key, and a set of associated physical volume names as the
     *         value.
     */
    Map<String, Set<String>> getLogicalVolumes();

}
