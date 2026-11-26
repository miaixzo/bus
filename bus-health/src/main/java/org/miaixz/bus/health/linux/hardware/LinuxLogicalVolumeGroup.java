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
package org.miaixz.bus.health.linux.hardware;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import com.sun.jna.platform.linux.Udev;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.LogicalVolumeGroup;
import org.miaixz.bus.health.builtin.hardware.common.AbstractLogicalVolumeGroup;
import org.miaixz.bus.health.linux.DevPath;
import org.miaixz.bus.health.linux.SysPath;
import org.miaixz.bus.health.linux.software.LinuxOperatingSystem;

/**
 * The linux logical volume group class.
 *
 * @author Kimi Liu
 */
public class LinuxLogicalVolumeGroup extends AbstractLogicalVolumeGroup {

    /**
     * The BLOCK constant.
     */
    private static final String BLOCK = "block";

    /**
     * The DM_UUID constant.
     */
    private static final String DM_UUID = "DM_UUID";

    /**
     * The DM_VG_NAME constant.
     */
    private static final String DM_VG_NAME = "DM_VG_NAME";

    /**
     * The DM_LV_NAME constant.
     */
    private static final String DM_LV_NAME = "DM_LV_NAME";

    /**
     * Creates a new LinuxLogicalVolumeGroup instance.
     *
     * @param name  the name
     * @param lvMap the lv map
     * @param pvSet the pv set
     */
    public LinuxLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        super(name, lvMap, pvSet);
    }

    /**
     * Returns the logical volume groups.
     *
     * @return the get logical volume groups result
     */
    static List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        if (!LinuxOperatingSystem.HAS_UDEV) {
            return querySysfsLogicalVolumeGroups();
        }
        Map<String, Map<String, Set<String>>> logicalVolumesMap = new HashMap<>();
        Map<String, Set<String>> physicalVolumesMap = new HashMap<>();

        // Populate pv map from pvs command
        // This requires elevated permissions and may fail
        for (String s : Executor.runPrivilegedNative("pvs -o vg_name,pv_name")) {
            String[] split = Pattern.SPACES_PATTERN.split(s.trim());
            if (split.length == 2 && split[1].startsWith(DevPath.NODE_ROOT)) {
                physicalVolumesMap.computeIfAbsent(split[0], k -> new HashSet<>()).add(split[1]);
            }
        }

        // Populate lv map from udev
        Udev.UdevContext udev = Udev.INSTANCE.udev_new();
        if (udev == null) {
            return Collections.emptyList();
        }
        try {
            Udev.UdevEnumerate enumerate = udev.enumerateNew();
            try {
                enumerate.addMatchSubsystem(BLOCK);
                enumerate.scanDevices();
                for (Udev.UdevListEntry entry = enumerate.getListEntry(); entry != null; entry = entry.getNext()) {
                    String syspath = entry.getName();
                    if (syspath == null) {
                        continue;
                    }
                    Udev.UdevDevice device = udev.deviceNewFromSyspath(syspath);
                    if (device != null) {
                        try {
                            String devnode = device.getDevnode();
                            if (devnode != null && devnode.startsWith(DevPath.DM)) {
                                String uuid = device.getPropertyValue(DM_UUID);
                                if (uuid != null && uuid.startsWith("LVM-")) {
                                    String vgName = device.getPropertyValue(DM_VG_NAME);
                                    String lvName = device.getPropertyValue(DM_LV_NAME);
                                    if (!StringKit.isBlank(vgName) && !StringKit.isBlank(lvName)) {
                                        String vg = Parsing.getStringValueOrEmpty(vgName);
                                        String lv = Parsing.getStringValueOrEmpty(lvName);
                                        logicalVolumesMap.computeIfAbsent(vg, k -> new HashMap<>());
                                        Map<String, Set<String>> lvMapForGroup = logicalVolumesMap.get(vg);
                                        // Backup to add to pv set if pvs command failed
                                        physicalVolumesMap.computeIfAbsent(vg, k -> new HashSet<>());
                                        Set<String> pvSetForGroup = physicalVolumesMap.get(vg);

                                        File slavesDir = new File(syspath + "/slaves");
                                        File[] slaves = slavesDir.listFiles();
                                        if (slaves != null) {
                                            for (File f : slaves) {
                                                String pvName = f.getName().replace('!', '/');
                                                lvMapForGroup.computeIfAbsent(lv, k -> new HashSet<>())
                                                        .add(DevPath.NODE_ROOT + pvName);
                                                // Backup to add to pv set if pvs command failed
                                                // Added /dev/ to remove duplicates like /dev/sda1 and sda1
                                                pvSetForGroup.add(DevPath.NODE_ROOT + pvName);
                                            }
                                        }
                                    }
                                }
                            }
                        } finally {
                            device.unref();
                        }
                    }
                }
            } finally {
                enumerate.unref();
            }
        } finally {
            udev.unref();
        }
        return logicalVolumesMap.entrySet().stream()
                .map(
                        e -> new LinuxLogicalVolumeGroup(e.getKey(), e.getValue(),
                                physicalVolumesMap.getOrDefault(e.getKey(), Collections.emptySet())))
                .collect(Collectors.toList());
    }

    /**
     * Reads logical volume groups directly from the device-mapper sysfs hierarchy.
     *
     * @return logical volume groups available through sysfs
     */
    private static List<LogicalVolumeGroup> querySysfsLogicalVolumeGroups() {
        Map<String, Map<String, Set<String>>> logicalVolumesMap = new HashMap<>();
        Map<String, Set<String>> physicalVolumesMap = queryPhysicalVolumes();
        File[] devices = new File(SysPath.BLOCK).listFiles();
        if (devices == null) {
            return Collections.emptyList();
        }
        for (File device : devices) {
            String name = device.getName();
            if (!name.startsWith("dm-")) {
                continue;
            }
            String uuid = Builder.getStringFromFile(device.getPath() + "/dm/uuid").trim();
            if (!uuid.startsWith("LVM-")) {
                continue;
            }
            String[] vgLv = LinuxHWDiskStore
                    .splitLvmDmName(Builder.getStringFromFile(device.getPath() + "/dm/name").trim());
            addLogicalVolume(logicalVolumesMap, physicalVolumesMap, device.getPath(), vgLv[Normal._0], vgLv[Normal._1]);
        }
        return toLogicalVolumeGroups(logicalVolumesMap, physicalVolumesMap);
    }

    /**
     * Reads physical-volume membership from the LVM command.
     *
     * @return volume-group names mapped to their physical volumes
     */
    private static Map<String, Set<String>> queryPhysicalVolumes() {
        Map<String, Set<String>> physicalVolumesMap = new HashMap<>();
        for (String text : Executor.runPrivilegedNative("pvs -o vg_name,pv_name")) {
            String[] split = Pattern.SPACES_PATTERN.split(text.trim());
            if (split.length == Normal._2 && split[Normal._1].startsWith(DevPath.NODE_ROOT)) {
                physicalVolumesMap.computeIfAbsent(split[Normal._0], key -> new HashSet<>()).add(split[Normal._1]);
            }
        }
        return physicalVolumesMap;
    }

    /**
     * Adds one device-mapper logical volume and its sysfs slave devices.
     *
     * @param logicalVolumesMap  the logical-volume map being assembled
     * @param physicalVolumesMap the physical-volume map being assembled
     * @param syspath            the device sysfs path
     * @param vgName             the volume-group name
     * @param lvName             the logical-volume name
     */
    private static void addLogicalVolume(
            Map<String, Map<String, Set<String>>> logicalVolumesMap,
            Map<String, Set<String>> physicalVolumesMap,
            String syspath,
            String vgName,
            String lvName) {
        if (StringKit.isBlank(vgName) || StringKit.isBlank(lvName)) {
            return;
        }
        Map<String, Set<String>> lvMap = logicalVolumesMap.computeIfAbsent(vgName, key -> new HashMap<>());
        Set<String> pvSet = physicalVolumesMap.computeIfAbsent(vgName, key -> new HashSet<>());
        File[] slaves = new File(syspath + "/slaves").listFiles();
        if (slaves != null) {
            for (File slave : slaves) {
                String pvName = DevPath.NODE_ROOT + slave.getName().replace('!', '/');
                lvMap.computeIfAbsent(lvName, key -> new HashSet<>()).add(pvName);
                pvSet.add(pvName);
            }
        }
    }

    /**
     * Converts assembled maps to logical-volume group records.
     *
     * @param logicalVolumesMap  the logical-volume map
     * @param physicalVolumesMap the physical-volume map
     * @return logical-volume group records
     */
    private static List<LogicalVolumeGroup> toLogicalVolumeGroups(
            Map<String, Map<String, Set<String>>> logicalVolumesMap,
            Map<String, Set<String>> physicalVolumesMap) {
        return logicalVolumesMap.entrySet().stream()
                .map(
                        entry -> new LinuxLogicalVolumeGroup(entry.getKey(), entry.getValue(),
                                physicalVolumesMap.getOrDefault(entry.getKey(), Collections.emptySet())))
                .collect(Collectors.toList());
    }

}
