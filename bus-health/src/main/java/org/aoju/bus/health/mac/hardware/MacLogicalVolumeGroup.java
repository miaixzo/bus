package org.aoju.bus.health.mac.hardware;


import org.aoju.bus.health.Executor;
import org.aoju.bus.health.builtin.hardware.AbstractLogicalVolumeGroup;
import org.aoju.bus.health.builtin.hardware.LogicalVolumeGroup;

import java.util.*;
import java.util.stream.Collectors;

final class MacLogicalVolumeGroup extends AbstractLogicalVolumeGroup {

    private static final String DISKUTIL_CS_LIST = "diskutil cs list";
    private static final String LOGICAL_VOLUME_GROUP = "Logical Volume Group";
    private static final String PHYSICAL_VOLUME = "Physical Volume";
    private static final String LOGICAL_VOLUME = "Logical Volume";

    MacLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        super(name, lvMap, pvSet);
    }

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