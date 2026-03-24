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
package org.miaixz.bus.health.windows.driver.wmi;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.WmiQueryHandler;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

/**
 * Utility to query WMI classes in Storage namespace assocaited with Storage Pools
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class MSFTStorage {

    private static final String STORAGE_NAMESPACE = "ROOT\\Microsoft\\Windows\\Storage";
    private static final String MSFT_STORAGE_POOL_WHERE_IS_PRIMORDIAL_FALSE = "MSFT_StoragePool WHERE IsPrimordial=FALSE";
    private static final String MSFT_STORAGE_POOL_TO_PHYSICAL_DISK = "MSFT_StoragePoolToPhysicalDisk";
    private static final String MSFT_PHYSICAL_DISK = "MSFT_PhysicalDisk";
    private static final String MSFT_VIRTUAL_DISK = "MSFT_VirtualDisk";

    /**
     * Query the storage pools.
     *
     * @param h An instantiated {@link WmiQueryHandler}. User should have already initialized COM.
     * @return Storage pools that are not primordial (raw disks not added to a storage space).
     */
    public static WmiResult<StoragePoolProperty> queryStoragePools(WmiQueryHandler h) {
        WmiQuery<StoragePoolProperty> storagePoolQuery = new WmiQuery<>(STORAGE_NAMESPACE,
                MSFT_STORAGE_POOL_WHERE_IS_PRIMORDIAL_FALSE, StoragePoolProperty.class);
        return h.queryWMI(storagePoolQuery, false);
    }

    /**
     * Query the storage pool to physical disk connection.
     *
     * @param h An instantiated {@link WmiQueryHandler}. User should have already initialized COM.
     * @return Links between physical disks and storage pools. All raw disks will be part of the primordial pool in
     *         addition to the storage space they are a member of.
     */
    public static WmiResult<StoragePoolToPhysicalDiskProperty> queryStoragePoolPhysicalDisks(WmiQueryHandler h) {
        WmiQuery<StoragePoolToPhysicalDiskProperty> storagePoolToPhysicalDiskQuery = new WmiQuery<>(STORAGE_NAMESPACE,
                MSFT_STORAGE_POOL_TO_PHYSICAL_DISK, StoragePoolToPhysicalDiskProperty.class);
        return h.queryWMI(storagePoolToPhysicalDiskQuery, false);
    }

    /**
     * Query the physical disks.
     *
     * @param h An instantiated {@link WmiQueryHandler}. User should have already initialized COM.
     * @return The physical disks.
     */
    public static WmiResult<PhysicalDiskProperty> queryPhysicalDisks(WmiQueryHandler h) {
        WmiQuery<PhysicalDiskProperty> physicalDiskQuery = new WmiQuery<>(STORAGE_NAMESPACE, MSFT_PHYSICAL_DISK,
                PhysicalDiskProperty.class);
        return h.queryWMI(physicalDiskQuery, false);
    }

    /**
     * Query the virtual disks.
     *
     * @param h An instantiated {@link WmiQueryHandler}. User should have already initialized COM.
     * @return The virtual disks.
     */
    public static WmiResult<VirtualDiskProperty> queryVirtualDisks(WmiQueryHandler h) {
        WmiQuery<VirtualDiskProperty> virtualDiskQuery = new WmiQuery<>(STORAGE_NAMESPACE, MSFT_VIRTUAL_DISK,
                VirtualDiskProperty.class);
        return h.queryWMI(virtualDiskQuery, false);
    }

    /**
     * Properties to identify the storage pool. The Object ID uniquely defines the pool.
     */
    public enum StoragePoolProperty {
        FRIENDLYNAME, OBJECTID
    }

    /**
     * Properties to link a storage pool with a physical disk. OSHI parses these references to strings that can match
     * the object IDs.
     */
    public enum StoragePoolToPhysicalDiskProperty {
        STORAGEPOOL, PHYSICALDISK
    }

    /**
     * Properties for a physical disk. The Object ID uniquely defines the disk.
     */
    public enum PhysicalDiskProperty {
        FRIENDLYNAME, PHYSICALLOCATION, OBJECTID
    }

    /**
     * Properties for a virtual disk. The Object ID uniquely defines the disk.
     */
    public enum VirtualDiskProperty {
        FRIENDLYNAME, OBJECTID
    }

}
