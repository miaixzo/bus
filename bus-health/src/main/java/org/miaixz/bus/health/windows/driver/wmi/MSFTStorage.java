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
package org.miaixz.bus.health.windows.driver.wmi;

import java.util.regex.Pattern;

import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiQuery;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.windows.WmiQueryHandler;

/**
 * Queries WMI classes in Storage namespace assocaited with Storage Pools
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class MSFTStorage {

    /**
     * The WMI namespace for storage.
     */
    public static final String STORAGE_NAMESPACE = "ROOT\\Microsoft\\Windows\\Storage";

    /** Matches a storage pool object ID and captures its GUID. */
    public static final Pattern SP_OBJECT_ID = Pattern.compile("SP:(\\{[^}]*\\})");

    /** Matches a physical disk object ID and captures its GUID. */
    public static final Pattern PD_OBJECT_ID = Pattern.compile("PD:(\\{[^}]*\\})");

    /** Matches a virtual disk object ID and captures its pool and disk GUIDs. */
    public static final Pattern VD_OBJECT_ID = Pattern.compile("VD:(\\{[^}]*\\})(\\{[^}]*\\})");

    /**
     * The WMI class name for non-primordial storage pools.
     */
    public static final String MSFT_STORAGE_POOL_WHERE_IS_PRIMORDIAL_FALSE = "MSFT_StoragePool WHERE IsPrimordial=FALSE";

    /**
     * The WMI class name for storage pool to physical disk associations.
     */
    public static final String MSFT_STORAGE_POOL_TO_PHYSICAL_DISK = "MSFT_StoragePoolToPhysicalDisk";

    /**
     * The WMI class name for physical disks.
     */
    public static final String MSFT_PHYSICAL_DISK = "MSFT_PhysicalDisk";

    /**
     * The WMI class name for virtual disks.
     */
    public static final String MSFT_VIRTUAL_DISK = "MSFT_VirtualDisk";

    /**
     * The WMI class linking virtual disks to physical disks.
     */
    public static final String MSFT_VIRTUAL_DISK_TO_PHYSICAL_DISK = "MSFT_VirtualDiskToPhysicalDisk";

    /**
     * The WMI class describing disks exposed by Windows.
     */
    public static final String MSFT_DISK = "MSFT_Disk";

    /**
     * Constructs a new {@code MSFTStorage} instance.
     */
    public MSFTStorage() {
        // No initialization required.
    }

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
     * Queries physical disk media types.
     *
     * @param h An initialized WMI query handler.
     * @return The physical disk media type data.
     */
    public static WmiResult<PhysicalDiskMediaTypeProperty> queryPhysicalDiskMediaTypes(WmiQueryHandler h) {
        WmiQuery<PhysicalDiskMediaTypeProperty> query = new WmiQuery<>(STORAGE_NAMESPACE, MSFT_PHYSICAL_DISK,
                PhysicalDiskMediaTypeProperty.class);
        return h.queryWMI(query, false);
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
     * Queries disks in the Storage namespace.
     *
     * @param h An initialized WMI query handler.
     * @return The disk data.
     */
    public static WmiResult<DiskProperty> queryDisks(WmiQueryHandler h) {
        return h.queryWMI(new WmiQuery<>(STORAGE_NAMESPACE, MSFT_DISK, DiskProperty.class), false);
    }

    /**
     * Queries virtual disk identities.
     *
     * @param h An initialized WMI query handler.
     * @return The virtual disk identities.
     */
    public static WmiResult<VirtualDiskIdProperty> queryVirtualDiskIds(WmiQueryHandler h) {
        return h.queryWMI(new WmiQuery<>(STORAGE_NAMESPACE, MSFT_VIRTUAL_DISK, VirtualDiskIdProperty.class), false);
    }

    /**
     * Queries virtual disk to physical disk associations.
     *
     * @param h An initialized WMI query handler.
     * @return The disk associations.
     */
    public static WmiResult<VirtualDiskToPhysicalDiskProperty> queryVirtualDiskToPhysicalDisks(WmiQueryHandler h) {
        return h.queryWMI(
                new WmiQuery<>(STORAGE_NAMESPACE, MSFT_VIRTUAL_DISK_TO_PHYSICAL_DISK,
                        VirtualDiskToPhysicalDiskProperty.class),
                false);
    }

    /**
     * Queries physical disk identities.
     *
     * @param h An initialized WMI query handler.
     * @return The physical disk identities.
     */
    public static WmiResult<PhysicalDiskIdProperty> queryPhysicalDiskIds(WmiQueryHandler h) {
        return h.queryWMI(new WmiQuery<>(STORAGE_NAMESPACE, MSFT_PHYSICAL_DISK, PhysicalDiskIdProperty.class), false);
    }

    /**
     * Properties to identify the storage pool. The Object ID uniquely defines the pool.
     *
     * @author Kimi Liu
     */
    public enum StoragePoolProperty {
        /**
         * The friendlyname storage pool property.
         */
        FRIENDLYNAME,
        /**
         * The objectid storage pool property.
         */
        OBJECTID;
    }

    /**
     * Properties to link a storage pool with a physical disk. OSHI parses these references to strings that can match
     * the object IDs.
     *
     * @author Kimi Liu
     */
    public enum StoragePoolToPhysicalDiskProperty {
        /**
         * The storagepool storage pool to physical disk property.
         */
        STORAGEPOOL,
        /**
         * The physicaldisk storage pool to physical disk property.
         */
        PHYSICALDISK;
    }

    /**
     * Properties for a physical disk. The Object ID uniquely defines the disk.
     *
     * @author Kimi Liu
     */
    public enum PhysicalDiskProperty {
        /**
         * The friendlyname physical disk property.
         */
        FRIENDLYNAME,
        /**
         * The physicallocation physical disk property.
         */
        PHYSICALLOCATION,
        /**
         * The objectid physical disk property.
         */
        OBJECTID;
    }

    /**
     * Properties describing a physical disk's media technology.
     *
     * @author Kimi Liu
     */
    public enum PhysicalDiskMediaTypeProperty {
        /** The physical drive number. */
        DEVICEID,
        /** The media type. */
        MEDIATYPE,
        /** The spindle speed. */
        SPINDLESPEED;
    }

    /**
     * Properties identifying a physical disk.
     *
     * @author Kimi Liu
     */
    public enum PhysicalDiskIdProperty {
        /** The physical drive number. */
        DEVICEID,
        /** The physical disk object ID. */
        OBJECTID;
    }

    /**
     * Properties linking a virtual disk to a physical disk.
     *
     * @author Kimi Liu
     */
    public enum VirtualDiskToPhysicalDiskProperty {
        /** The virtual disk reference. */
        VIRTUALDISK,
        /** The physical disk reference. */
        PHYSICALDISK;
    }

    /**
     * Properties describing a disk and its backing storage.
     *
     * @author Kimi Liu
     */
    public enum DiskProperty {
        /** The disk number. */
        NUMBER,
        /** The storage bus type. */
        BUSTYPE,
        /** The location of a backing file. */
        LOCATION,
        /** The disk's unique ID. */
        UNIQUEID;
    }

    /**
     * Properties identifying a virtual disk.
     *
     * @author Kimi Liu
     */
    public enum VirtualDiskIdProperty {
        /** The virtual disk unique ID. */
        UNIQUEID,
        /** The virtual disk object ID. */
        OBJECTID;
    }

    /**
     * Properties for a virtual disk. The Object ID uniquely defines the disk.
     *
     * @author Kimi Liu
     */
    public enum VirtualDiskProperty {
        /**
         * The friendlyname virtual disk property.
         */
        FRIENDLYNAME,
        /**
         * The objectid virtual disk property.
         */
        OBJECTID;
    }

}
