/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI Team and other contributors.          *
 *                                                                               *
 * Permission is hereby granted, free of charge, to any person obtaining a copy  *
 * of this software and associated documentation files (the "Software"), to deal *
 * in the Software without restriction, including without limitation the rights  *
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell     *
 * copies of the Software, and to permit persons to whom the Software is         *
 * furnished to do so, subject to the following conditions:                      *
 *                                                                               *
 * The above copyright notice and this permission notice shall be included in    *
 * all copies or substantial portions of the Software.                           *
 *                                                                               *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR    *
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,      *
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE   *
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER        *
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, *
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN     *
 * THE SOFTWARE.                                                                 *
 *                                                                               *
 ********************************************************************************/
package org.miaixz.bus.health.windows.hardware;

import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.VersionHelpers;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.builtin.hardware.LogicalVolumeGroup;
import org.miaixz.bus.health.builtin.hardware.common.AbstractLogicalVolumeGroup;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.WmiQueryHandler;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.PhysicalDiskProperty;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.StoragePoolProperty;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.StoragePoolToPhysicalDiskProperty;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.VirtualDiskProperty;
import org.miaixz.bus.logger.Logger;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;

/**
 * Windows Logical Volume Group
 *
 * @author Kimi Liu
 * @since Java 17+
 */
final class WindowsLogicalVolumeGroup extends AbstractLogicalVolumeGroup {

    private static final java.util.regex.Pattern SP_OBJECT_ID = java.util.regex.Pattern.compile(".*ObjectId=.*SP:(\\{.*\\}).*");
    private static final java.util.regex.Pattern PD_OBJECT_ID = java.util.regex.Pattern.compile(".*ObjectId=.*PD:(\\{.*\\}).*");
    private static final java.util.regex.Pattern VD_OBJECT_ID = java.util.regex.Pattern.compile(".*ObjectId=.*VD:(\\{.*\\})(\\{.*\\}).*");

    private static final boolean IS_WINDOWS8_OR_GREATER = VersionHelpers.IsWindows8OrGreater();

    WindowsLogicalVolumeGroup(String name, Map<String, Set<String>> lvMap, Set<String> pvSet) {
        super(name, lvMap, pvSet);
    }

    static List<LogicalVolumeGroup> getLogicalVolumeGroups() {
        // Storage Spaces requires Windows 8 or Server 2012
        if (!IS_WINDOWS8_OR_GREATER) {
            return Collections.emptyList();
        }
        WmiQueryHandler h = Objects.requireNonNull(WmiQueryHandler.createInstance());
        boolean comInit = false;
        try {
            comInit = h.initCOM();
            // Query Storage Pools first, so we can skip other queries if we have no pools
            WmiResult<StoragePoolProperty> sp = MSFTStorage.queryStoragePools(h);
            int count = sp.getResultCount();
            if (count == 0) {
                return Collections.emptyList();
            }
            // We have storage pool(s) but now need to gather other info

            // Get all the Virtual Disks
            Map<String, String> vdMap = new HashMap<>();
            WmiResult<VirtualDiskProperty> vds = MSFTStorage.queryVirtualDisks(h);
            count = vds.getResultCount();
            for (int i = 0; i < count; i++) {
                String vdObjectId = WmiKit.getString(vds, VirtualDiskProperty.OBJECTID, i);
                Matcher m = VD_OBJECT_ID.matcher(vdObjectId);
                if (m.matches()) {
                    vdObjectId = m.group(2) + Symbol.SPACE + m.group(1);
                }
                // Store key with SP|VD
                vdMap.put(vdObjectId, WmiKit.getString(vds, VirtualDiskProperty.FRIENDLYNAME, i));
            }

            // Get all the Physical Disks
            Map<String, Pair<String, String>> pdMap = new HashMap<>();
            WmiResult<PhysicalDiskProperty> pds = MSFTStorage.queryPhysicalDisks(h);
            count = pds.getResultCount();
            for (int i = 0; i < count; i++) {
                String pdObjectId = WmiKit.getString(pds, PhysicalDiskProperty.OBJECTID, i);
                Matcher m = PD_OBJECT_ID.matcher(pdObjectId);
                if (m.matches()) {
                    pdObjectId = m.group(1);
                }
                // Store key with PD
                pdMap.put(pdObjectId, Pair.of(WmiKit.getString(pds, PhysicalDiskProperty.FRIENDLYNAME, i),
                        WmiKit.getString(pds, PhysicalDiskProperty.PHYSICALLOCATION, i)));
            }

            // Get the Storage Pool to Physical Disk mappping
            Map<String, String> sppdMap = new HashMap<>();
            WmiResult<StoragePoolToPhysicalDiskProperty> sppd = MSFTStorage.queryStoragePoolPhysicalDisks(h);
            count = sppd.getResultCount();
            for (int i = 0; i < count; i++) {
                // Ref string contains object id, will do partial match later
                String spObjectId = WmiKit.getRefString(sppd, StoragePoolToPhysicalDiskProperty.STORAGEPOOL, i);
                Matcher m = SP_OBJECT_ID.matcher(spObjectId);
                if (m.matches()) {
                    spObjectId = m.group(1);
                }
                String pdObjectId = WmiKit.getRefString(sppd, StoragePoolToPhysicalDiskProperty.PHYSICALDISK, i);
                m = PD_OBJECT_ID.matcher(pdObjectId);
                if (m.matches()) {
                    pdObjectId = m.group(1);
                }
                sppdMap.put(spObjectId + Symbol.SPACE + pdObjectId, pdObjectId);
            }

            // Finally process the storage pools
            List<LogicalVolumeGroup> lvgList = new ArrayList<>();
            count = sp.getResultCount();
            for (int i = 0; i < count; i++) {
                // Name
                String name = WmiKit.getString(sp, StoragePoolProperty.FRIENDLYNAME, i);
                // Parse object ID to match
                String spObjectId = WmiKit.getString(sp, StoragePoolProperty.OBJECTID, i);
                Matcher m = SP_OBJECT_ID.matcher(spObjectId);
                if (m.matches()) {
                    spObjectId = m.group(1);
                }
                // find matching physical and logical volumes
                Set<String> physicalVolumeSet = new HashSet<>();
                for (Entry<String, String> entry : sppdMap.entrySet()) {
                    if (entry.getKey().contains(spObjectId)) {
                        String pdObjectId = entry.getValue();
                        Pair<String, String> nameLoc = pdMap.get(pdObjectId);
                        if (nameLoc != null) {
                            physicalVolumeSet.add(nameLoc.getLeft() + " @ " + nameLoc.getRight());
                        }
                    }
                }
                // find matching logical volume
                Map<String, Set<String>> logicalVolumeMap = new HashMap<>();
                for (Entry<String, String> entry : vdMap.entrySet()) {
                    if (entry.getKey().contains(spObjectId)) {
                        String vdObjectId = Pattern.SPACES_PATTERN.split(entry.getKey())[0];
                        logicalVolumeMap.put(entry.getValue() + Symbol.SPACE + vdObjectId, physicalVolumeSet);
                    }
                }
                // Add to list
                lvgList.add(new WindowsLogicalVolumeGroup(name, logicalVolumeMap, physicalVolumeSet));
            }

            return lvgList;
        } catch (COMException e) {
            Logger.warn("COM exception: {}", e.getMessage());
            return Collections.emptyList();
        } finally {
            if (comInit) {
                h.unInitCOM();
            }
        }
    }

}
