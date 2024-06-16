/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ The MIT License (MIT)                                                         ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Permission is hereby granted, free of charge, to any person obtaining a copy  ~
 ~ of this software and associated documentation files (the "Software"), to deal ~
 ~ in the Software without restriction, including without limitation the rights  ~
 ~ to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     ~
 ~ copies of the Software, and to permit persons to whom the Software is         ~
 ~ furnished to do so, subject to the following conditions:                      ~
 ~                                                                               ~
 ~ The above copyright notice and this permission notice shall be included in    ~
 ~ all copies or substantial portions of the Software.                           ~
 ~                                                                               ~
 ~ THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    ~
 ~ IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      ~
 ~ FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   ~
 ~ AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        ~
 ~ LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, ~
 ~ OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     ~
 ~ THE SOFTWARE.                                                                 ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 */
package org.miaixz.bus.health.linux.hardware;

import com.sun.jna.platform.linux.Udev;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.hardware.LogicalVolumeGroup;
import org.miaixz.bus.health.builtin.hardware.common.AbstractLogicalVolumeGroup;
import org.miaixz.bus.health.linux.DevPath;
import org.miaixz.bus.health.linux.software.LinuxOperatingSystem;
import org.miaixz.bus.logger.Logger;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
public class LinuxLogicalVolumeGroup extends AbstractLogicalVolumeGroup {

    private static final String BLOCK = "block";
    private static final String DM_UUID = "DM_UUID";
    private static final String DM_VG_NAME = "DM_VG_NAME";
    private static final String DM_LV_NAME = "DM_LV_NAME";

    LinuxLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        super(name, lvMap, pvSet);
    }

    static List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        if (!LinuxOperatingSystem.HAS_UDEV) {
            Logger.warn("Logical Volume Group information requires libudev, which is not present.");
            return Collections.emptyList();
        }
        Map<String, Map<String, Set<String>>> logicalVolumesMap = new HashMap<>();
        Map<String, Set<String>> physicalVolumesMap = new HashMap<>();

        // Populate pv map from pvs command
        // This requires elevated permissions and may fail
        for (String s : Executor.runNative("pvs -o vg_name,pv_name")) {
            String[] split = Pattern.SPACES_PATTERN.split(s.trim());
            if (split.length == 2 && split[1].startsWith(DevPath.DEV)) {
                physicalVolumesMap.computeIfAbsent(split[0], k -> new HashSet<>()).add(split[1]);
            }
        }

        // Populate lv map from udev
        Udev.UdevContext udev = Udev.INSTANCE.udev_new();
        try {
            Udev.UdevEnumerate enumerate = udev.enumerateNew();
            try {
                enumerate.addMatchSubsystem(BLOCK);
                enumerate.scanDevices();
                for (Udev.UdevListEntry entry = enumerate.getListEntry(); entry != null; entry = entry.getNext()) {
                    String syspath = entry.getName();
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
                                        logicalVolumesMap.computeIfAbsent(vgName, k -> new HashMap<>());
                                        Map<String, Set<String>> lvMapForGroup = logicalVolumesMap.get(vgName);
                                        // Backup to add to pv set if pvs command failed
                                        physicalVolumesMap.computeIfAbsent(vgName, k -> new HashSet<>());
                                        Set<String> pvSetForGroup = physicalVolumesMap.get(vgName);

                                        File slavesDir = new File(syspath + "/slaves");
                                        File[] slaves = slavesDir.listFiles();
                                        if (slaves != null) {
                                            for (File f : slaves) {
                                                String pvName = f.getName();
                                                lvMapForGroup.computeIfAbsent(lvName, k -> new HashSet<>())
                                                        .add(DevPath.DEV + pvName);
                                                // Backup to add to pv set if pvs command failed
                                                // Added /dev/ to remove duplicates like /dev/sda1 and sda1
                                                pvSetForGroup.add(DevPath.DEV + pvName);
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
                .map(e -> new LinuxLogicalVolumeGroup(e.getKey(), e.getValue(), physicalVolumesMap.get(e.getKey())))
                .collect(Collectors.toList());
    }
}
