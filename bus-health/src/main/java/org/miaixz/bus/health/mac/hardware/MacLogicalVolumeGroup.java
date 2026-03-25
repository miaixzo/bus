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
package org.miaixz.bus.health.mac.hardware;

import java.util.*;
import java.util.stream.Collectors;

import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.hardware.LogicalVolumeGroup;
import org.miaixz.bus.health.builtin.hardware.common.AbstractLogicalVolumeGroup;

/**
 * <p>
 * MacLogicalVolumeGroup class.
 * </p>
 * Logical Volume Group data obtained from `diskutil cs list` command on macOS.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
final class MacLogicalVolumeGroup extends AbstractLogicalVolumeGroup {

    private static final String DISKUTIL_CS_LIST = "diskutil cs list";
    private static final String LOGICAL_VOLUME_GROUP = "Logical Volume Group";
    private static final String PHYSICAL_VOLUME = "Physical Volume";
    private static final String LOGICAL_VOLUME = "Logical Volume";

    /**
     * Constructs a {@code MacLogicalVolumeGroup} object.
     *
     * @param name  The name of the logical volume group.
     * @param lvMap A map of logical volumes within this group, where keys are logical volume names and values are sets
     *              of physical volumes they span.
     * @param pvSet A set of physical volumes belonging to this group.
     */
    MacLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        super(name, lvMap, pvSet);
    }

    /**
     * Retrieves a list of {@link LogicalVolumeGroup} objects representing the logical volume groups on macOS. This
     * method parses the output of the `diskutil cs list` command.
     *
     * @return A list of {@link LogicalVolumeGroup} objects.
     */
    static List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        Map<String, Map<String, Set<String>>> logicalVolumesMap = new HashMap<>();
        Map<String, Set<String>> physicalVolumesMap = new HashMap<>();

        String currentVolumeGroup = null;
        boolean lookForVGName = false;
        boolean lookForPVName = false;
        int indexOf;
        // Parse `diskutil cs list` to populate logical volume map
        for (String line : Executor.runNative(DISKUTIL_CS_LIST)) {
            if (line.contains(LOGICAL_VOLUME_GROUP)) {
                // Disks that follow should be attached to this VG
                lookForVGName = true;
            } else if (lookForVGName) {
                indexOf = line.indexOf("Name:");
                if (indexOf >= 0) {
                    currentVolumeGroup = line.substring(indexOf + 5).trim();
                    lookForVGName = false;
                }
            } else if (line.contains(PHYSICAL_VOLUME)) {
                lookForPVName = true;
            } else if (line.contains(LOGICAL_VOLUME)) {
                lookForPVName = false;
            } else {
                indexOf = line.indexOf("Disk:");
                if (indexOf >= 0) {
                    if (lookForPVName) {
                        physicalVolumesMap.computeIfAbsent(currentVolumeGroup, k -> new HashSet<>())
                                .add(line.substring(indexOf + 5).trim());
                    } else {
                        logicalVolumesMap.computeIfAbsent(currentVolumeGroup, k -> new HashMap<>())
                                .put(line.substring(indexOf + 5).trim(), Collections.emptySet());
                    }
                }
            }
        }
        return logicalVolumesMap.entrySet().stream()
                .map(e -> new MacLogicalVolumeGroup(e.getKey(), e.getValue(), physicalVolumesMap.get(e.getKey())))
                .collect(Collectors.toList());
    }

}
