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
package org.miaixz.bus.health.windows.hardware;

import static org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.PD_OBJECT_ID;
import static org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.VD_OBJECT_ID;

import java.util.*;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.COM.COMException;
import com.sun.jna.platform.win32.COM.WbemcliUtil.WmiResult;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLE;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHWDiskStore;
import org.miaixz.bus.health.builtin.jna.ByRef;
import org.miaixz.bus.health.windows.WmiKit;
import org.miaixz.bus.health.windows.WmiQueryHandler;
import org.miaixz.bus.health.windows.driver.perfmon.PhysicalDisk;
import org.miaixz.bus.health.windows.driver.perfmon.PhysicalDisk.PhysicalDiskProperty;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.DiskProperty;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.PhysicalDiskIdProperty;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.PhysicalDiskMediaTypeProperty;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.VirtualDiskIdProperty;
import org.miaixz.bus.health.windows.driver.wmi.MSFTStorage.VirtualDiskToPhysicalDiskProperty;
import org.miaixz.bus.health.windows.driver.wmi.Win32DiskDrive;
import org.miaixz.bus.health.windows.driver.wmi.Win32DiskDrive.DiskDriveProperty;
import org.miaixz.bus.health.windows.driver.wmi.Win32DiskDriveToDiskPartition;
import org.miaixz.bus.health.windows.driver.wmi.Win32DiskDriveToDiskPartition.DriveToPartitionProperty;
import org.miaixz.bus.health.windows.driver.wmi.Win32DiskPartition;
import org.miaixz.bus.health.windows.driver.wmi.Win32DiskPartition.DiskPartitionProperty;
import org.miaixz.bus.health.windows.driver.wmi.Win32LogicalDiskToPartition;
import org.miaixz.bus.health.windows.driver.wmi.Win32LogicalDiskToPartition.DiskToPartitionProperty;
import org.miaixz.bus.logger.Logger;

/**
 * Windows hard disk implementation.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class WindowsHWDiskStore extends AbstractHWDiskStore {

    /**
     * The PHYSICALDRIVE_PREFIX constant.
     */
    private static final String PHYSICALDRIVE_PREFIX = "\\\\.\\PHYSICALDRIVE";

    /**
     * The DEVICE_ID constant.
     */
    private static final Pattern DEVICE_ID = Pattern.compile(".*\\.DeviceID=\"(.*)\"");

    // A reasonable size for the buffer to accommodate the largest possible volume
    // GUID path is 50 characters.
    /**
     * The GUID_BUFSIZE constant.
     */
    private static final int GUID_BUFSIZE = 100;

    // Windows allows up to 32 characters for NTFS volume labels; +1 for null terminator
    /**
     * The LABEL_BUFSIZE constant.
     */
    private static final int LABEL_BUFSIZE = 33;

    /** Disk type reported when the media technology is unknown. */
    private static final String DISK_TYPE_UNKNOWN = "Unknown";

    /** Solid-state disk type. */
    private static final String DISK_TYPE_SSD = "SSD";

    /** Rotating hard disk type. */
    private static final String DISK_TYPE_HDD = "HDD";

    /** Removable disk type. */
    private static final String DISK_TYPE_REMOVABLE = "Removable";

    /** Virtual disk type. */
    private static final String DISK_TYPE_VIRTUAL = "Virtual";

    /** IOCTL code for storage property queries. */
    private static final int IOCTL_STORAGE_QUERY_PROPERTY = 0x002D1400;

    /** Timeout for Storage namespace fallback queries. */
    private static final int MSFT_PHYSICAL_DISK_TIMEOUT_MS = 5_000;

    /**
     * The reads value.
     */
    private long reads = 0L;

    /**
     * The readBytes value.
     */
    private long readBytes = 0L;

    /**
     * The writes value.
     */
    private long writes = 0L;

    /**
     * The writeBytes value.
     */
    private long writeBytes = 0L;

    /**
     * The currentQueueLength value.
     */
    private long currentQueueLength = 0L;

    /**
     * The transferTime value.
     */
    private long transferTime = 0L;

    /**
     * The timeStamp value.
     */
    private long timeStamp = 0L;

    /**
     * The partitionList value.
     */
    private List<HWPartition> partitionList = Collections.emptyList();

    /**
     * Supplies backing stores for all disks in this enumeration.
     */
    private final SupplierX<Map<String, List<String>>> backingStores;

    /**
     * Creates a new WindowsHWDiskStore instance.
     *
     * @param name     the name
     * @param model    the model
     * @param serial   the serial
     * @param size     the size
     * @param diskType the disk type
     */
    public WindowsHWDiskStore(String name, String model, String serial, long size, String diskType) {
        this(name, model, serial, size, diskType, Collections::emptyMap);
    }

    /**
     * Creates a disk store with lazily queried backing stores.
     *
     * @param name          The disk name.
     * @param model         The disk model.
     * @param serial        The serial number.
     * @param size          The size in bytes.
     * @param diskType      The media type.
     * @param backingStores The backing store supplier.
     */
    private WindowsHWDiskStore(String name, String model, String serial, long size, String diskType,
            SupplierX<Map<String, List<String>>> backingStores) {
        super(name, model, serial, size, diskType);
        this.backingStores = backingStores;
    }

    /**
     * Gets the disks on this machine
     *
     * @return a list of {@link HWDiskStore} objects representing the disks
     */
    public static List<HWDiskStore> getDisks() {
        WmiQueryHandler h = Objects.requireNonNull(WmiQueryHandler.createInstance());
        boolean comInit = false;
        try {
            comInit = h.initCOM();
            DiskStats stats = queryReadWriteStats(null);
            PartitionMaps maps = queryPartitionMaps(h);

            WmiResult<DiskDriveProperty> vals = Win32DiskDrive.queryDiskDrive(h);
            int count = vals.getResultCount();
            String[] diskTypes = classifyDiskTypes(vals, WindowsHWDiskStore::queryStorageProperties);
            if (anyDiskTypeUnknown(diskTypes)) {
                int savedTimeout = h.getWmiTimeout();
                if (savedTimeout < Normal._0) {
                    h.setWmiTimeout(MSFT_PHYSICAL_DISK_TIMEOUT_MS);
                }
                try {
                    fillUnknownDiskTypes(diskTypes, vals, MSFTStorage.queryPhysicalDiskMediaTypes(h));
                } finally {
                    h.setWmiTimeout(savedTimeout);
                }
            }
            SupplierX<Map<String, List<String>>> backingStores = Memoizer
                    .memoize(WindowsHWDiskStore::queryBackingStores);
            List<HWDiskStore> result = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                WindowsHWDiskStore ds = new WindowsHWDiskStore(WmiKit.getString(vals, DiskDriveProperty.NAME, i),
                        (WmiKit.getString(vals, DiskDriveProperty.MODEL, i) + Symbol.SPACE
                                + WmiKit.getString(vals, DiskDriveProperty.MANUFACTURER, i)).trim(),
                        // Most vendors store serial # as a hex string; convert
                        Parsing.hexStringToString(WmiKit.getString(vals, DiskDriveProperty.SERIALNUMBER, i)),
                        WmiKit.getUint64(vals, DiskDriveProperty.SIZE, i), diskTypes[i], backingStores);

                String index = Integer.toString(WmiKit.getUint32(vals, DiskDriveProperty.INDEX, i));
                ds.reads = stats.readMap.getOrDefault(index, 0L);
                ds.readBytes = stats.readByteMap.getOrDefault(index, 0L);
                ds.writes = stats.writeMap.getOrDefault(index, 0L);
                ds.writeBytes = stats.writeByteMap.getOrDefault(index, 0L);
                ds.currentQueueLength = stats.queueLengthMap.getOrDefault(index, 0L);
                // DiskTime (sum of readTime+writeTime) slightly overestimates actual transfer
                // time because it includes waiting time in the queue and can exceed 100%.
                // However, alternative calculations require use of a timestamp with 1/64-second
                // resolution producing unacceptable variation in what should be a monotonically
                // increasing counter. See extended discussion and experiments here:
                // https://github.com/oshi/oshi/issues/1504
                ds.transferTime = stats.diskTimeMap.getOrDefault(index, 0L);
                ds.timeStamp = stats.timeStamp;
                // Get partitions
                List<HWPartition> partitions = new ArrayList<>();
                List<String> partList = maps.driveToPartitionMap.get(ds.getName());
                if (partList != null && !partList.isEmpty()) {
                    for (String part : partList) {
                        if (maps.partitionMap.containsKey(part)) {
                            partitions.addAll(maps.partitionMap.get(part));
                        }
                    }
                }
                ds.partitionList = Collections.unmodifiableList(
                        partitions.stream().sorted(Comparator.comparing(HWPartition::getName))
                                .collect(Collectors.toList()));
                // Add to list
                result.add(ds);
            }
            return result;
        } catch (COMException e) {
            Logger.warn(false, "Health", "COM exception: {}", e.getClass().getSimpleName());
            return Collections.emptyList();
        } finally {
            if (comInit) {
                h.unInitCOM();
            }
        }
    }

    /**
     * Queries backing stores in an independent COM session because the memoized supplier may run on any thread.
     *
     * @return Backing stores keyed by physical drive name.
     */
    private static Map<String, List<String>> queryBackingStores() {
        WmiQueryHandler h = Objects.requireNonNull(WmiQueryHandler.createInstance());
        boolean comInit = false;
        int savedTimeout = h.getWmiTimeout();
        try {
            comInit = h.initCOM();
            if (savedTimeout < Normal._0) {
                h.setWmiTimeout(MSFT_PHYSICAL_DISK_TIMEOUT_MS);
            }
            return queryBackingStores(h, WindowsHWDiskStore::queryVolumePathName);
        } catch (COMException e) {
            Logger.debug(false, "Health", "Unable to query Storage backing stores: {}", e.getClass().getSimpleName());
            return Collections.emptyMap();
        } finally {
            h.setWmiTimeout(savedTimeout);
            if (comInit) {
                h.unInitCOM();
            }
        }
    }

    /**
     * Queries the Storage namespace and correlates each disk with its backing stores.
     *
     * @param h       The initialized WMI handler.
     * @param mountOf Resolves a path to its volume mount point.
     * @return Backing stores keyed by physical drive name.
     */
    private static Map<String, List<String>> queryBackingStores(WmiQueryHandler h, UnaryOperator<String> mountOf) {
        return buildBackingStores(
                MSFTStorage.queryDisks(h),
                MSFTStorage.queryVirtualDiskIds(h),
                MSFTStorage.queryVirtualDiskToPhysicalDisks(h),
                MSFTStorage.queryPhysicalDiskIds(h),
                mountOf);
    }

    /**
     * Correlates Storage namespace query results into each disk's backing stores.
     *
     * @param disks   The disk query result.
     * @param vds     The virtual disk identities.
     * @param vdpd    The virtual-to-physical disk associations.
     * @param pds     The physical disk identities.
     * @param mountOf Resolves a file path to its volume mount point.
     * @return Backing stores keyed by physical drive name.
     */
    private static Map<String, List<String>> buildBackingStores(
            WmiResult<DiskProperty> disks,
            WmiResult<VirtualDiskIdProperty> vds,
            WmiResult<VirtualDiskToPhysicalDiskProperty> vdpd,
            WmiResult<PhysicalDiskIdProperty> pds,
            UnaryOperator<String> mountOf) {
        try {
            Map<String, String> deviceIdByPd = new HashMap<>();
            for (int i = Normal._0; i < pds.getResultCount(); i++) {
                String pd = objectGuid(PD_OBJECT_ID, WmiKit.getString(pds, PhysicalDiskIdProperty.OBJECTID, i));
                deviceIdByPd.put(pd, WmiKit.getString(pds, PhysicalDiskIdProperty.DEVICEID, i));
            }
            Map<String, String> vdByUniqueId = new HashMap<>();
            for (int i = Normal._0; i < vds.getResultCount(); i++) {
                vdByUniqueId.put(
                        WmiKit.getString(vds, VirtualDiskIdProperty.UNIQUEID, i),
                        objectGuid(VD_OBJECT_ID, WmiKit.getString(vds, VirtualDiskIdProperty.OBJECTID, i)));
            }
            Map<String, List<String>> pdsByVd = new HashMap<>();
            for (int i = Normal._0; i < vdpd.getResultCount(); i++) {
                String vd = objectGuid(
                        VD_OBJECT_ID,
                        WmiKit.getRefString(vdpd, VirtualDiskToPhysicalDiskProperty.VIRTUALDISK, i));
                String pd = objectGuid(
                        PD_OBJECT_ID,
                        WmiKit.getRefString(vdpd, VirtualDiskToPhysicalDiskProperty.PHYSICALDISK, i));
                pdsByVd.computeIfAbsent(vd, key -> new ArrayList<>()).add(pd);
            }
            Map<String, List<String>> result = new HashMap<>();
            for (int i = Normal._0; i < disks.getResultCount(); i++) {
                String name = PHYSICALDRIVE_PREFIX + WmiKit.getUint32(disks, DiskProperty.NUMBER, i);
                int busType = WmiKit.getUint16(disks, DiskProperty.BUSTYPE, i);
                if (busType == Normal._15) {
                    String location = WmiKit.getString(disks, DiskProperty.LOCATION, i);
                    String mount = location.isEmpty() ? Normal.EMPTY : mountOf.apply(location);
                    if (!mount.isEmpty()) {
                        result.put(name, Collections.singletonList(StoreKind.FILE.entry(mount)));
                    }
                } else if (busType == Normal._16) {
                    String vd = vdByUniqueId.get(WmiKit.getString(disks, DiskProperty.UNIQUEID, i));
                    TreeSet<String> stores = new TreeSet<>();
                    for (String pd : pdsByVd.getOrDefault(vd == null ? Normal.EMPTY : vd, Collections.emptyList())) {
                        String deviceId = deviceIdByPd.get(pd);
                        if (deviceId != null && !deviceId.isEmpty()) {
                            stores.add(StoreKind.DISK.entry(PHYSICALDRIVE_PREFIX + deviceId));
                        }
                    }
                    if (!stores.isEmpty()) {
                        result.put(name, Collections.unmodifiableList(new ArrayList<>(stores)));
                    }
                } else {
                    result.put(name, Collections.emptyList());
                }
            }
            return Collections.unmodifiableMap(result);
        } catch (ClassCastException e) {
            Logger.debug(
                    false,
                    "Health",
                    "Unexpected Storage namespace property type: {}",
                    e.getClass().getSimpleName());
            return Collections.emptyMap();
        }
    }

    /**
     * Extracts the GUIDs from a Storage namespace object ID or reference.
     *
     * @param pattern  The object ID pattern.
     * @param objectId The object ID or reference.
     * @return The captured GUIDs, or the original string when it does not match.
     */
    private static String objectGuid(Pattern pattern, String objectId) {
        Matcher matcher = pattern.matcher(objectId);
        if (!matcher.find()) {
            return objectId;
        }
        StringBuilder result = new StringBuilder();
        for (int group = Normal._1; group <= matcher.groupCount(); group++) {
            result.append(matcher.group(group));
        }
        return result.toString();
    }

    /**
     * Resolves the volume mount point holding a path.
     *
     * @param path The path.
     * @return The mount point, or an empty string when unavailable.
     */
    private static String queryVolumePathName(String path) {
        char[] buffer = new char[WinDef.MAX_PATH + Normal._1];
        return Kernel32.INSTANCE.GetVolumePathName(path, buffer, buffer.length) ? Native.toString(buffer)
                : Normal.EMPTY;
    }

    /**
     * Reads storage properties directly from a physical drive.
     *
     * @param name The physical drive path.
     * @return The storage properties, or {@code null} when unavailable.
     */
    private static StorageProperties queryStorageProperties(String name) {
        if (!name.startsWith(PHYSICALDRIVE_PREFIX)) {
            return null;
        }
        HANDLE handle = Kernel32.INSTANCE.CreateFile(
                name,
                Normal._0,
                WinNT.FILE_SHARE_READ | WinNT.FILE_SHARE_WRITE | WinNT.FILE_SHARE_DELETE,
                null,
                WinNT.OPEN_EXISTING,
                Normal._0,
                null);
        if (handle == null || WinBase.INVALID_HANDLE_VALUE.equals(handle)) {
            Logger.debug(false, "Health", "Unable to open {} for storage properties.", name);
            return null;
        }
        try (Memory query = new Memory(Normal._12);
                Memory deviceDescriptor = new Memory(Normal._1024);
                Memory seekPenaltyDescriptor = new Memory(Normal._12);
                ByRef.CloseableIntByReference bytesReturned = new ByRef.CloseableIntByReference()) {
            query.clear();
            query.setInt(Normal._0, Normal._0);
            query.setInt(Integer.BYTES, Normal._0);
            deviceDescriptor.clear();
            if (!Kernel32.INSTANCE.DeviceIoControl(
                    handle,
                    IOCTL_STORAGE_QUERY_PROPERTY,
                    query,
                    Normal._12,
                    deviceDescriptor,
                    Normal._1024,
                    bytesReturned,
                    null)) {
                return null;
            }
            boolean removableMedia = deviceDescriptor.getByte(Normal._10) != Normal._0;
            int busType = deviceDescriptor.getInt(Normal._28);
            Boolean incursSeekPenalty = null;
            query.setInt(Normal._0, Normal._7);
            seekPenaltyDescriptor.clear();
            if (Kernel32.INSTANCE.DeviceIoControl(
                    handle,
                    IOCTL_STORAGE_QUERY_PROPERTY,
                    query,
                    Normal._12,
                    seekPenaltyDescriptor,
                    Normal._12,
                    bytesReturned,
                    null)) {
                incursSeekPenalty = seekPenaltyDescriptor.getByte(Normal._8) != Normal._0;
            }
            return new StorageProperties(busType, removableMedia, incursSeekPenalty);
        } finally {
            Kernel32.INSTANCE.CloseHandle(handle);
        }
    }

    /**
     * Gets disk stats for the specified index. If the index is null, populates all the maps
     *
     * @param index The index to populate/update maps for
     * @return An object encapsulating maps with the stats
     */
    private static DiskStats queryReadWriteStats(String index) {
        // Create object to hold and return results
        DiskStats stats = new DiskStats();
        Pair<List<String>, Map<PhysicalDiskProperty, List<Long>>> instanceValuePair = PhysicalDisk.queryDiskCounters();
        List<String> instances = instanceValuePair.getLeft();
        Map<PhysicalDiskProperty, List<Long>> valueMap = instanceValuePair.getRight();
        stats.timeStamp = System.currentTimeMillis();
        List<Long> readList = valueMap.get(PhysicalDiskProperty.DISKREADSPERSEC);
        List<Long> readByteList = valueMap.get(PhysicalDiskProperty.DISKREADBYTESPERSEC);
        List<Long> writeList = valueMap.get(PhysicalDiskProperty.DISKWRITESPERSEC);
        List<Long> writeByteList = valueMap.get(PhysicalDiskProperty.DISKWRITEBYTESPERSEC);
        List<Long> queueLengthList = valueMap.get(PhysicalDiskProperty.CURRENTDISKQUEUELENGTH);
        List<Long> diskTimeList = valueMap.get(PhysicalDiskProperty.PERCENTDISKTIME);

        if (instances.isEmpty() || readList == null || readByteList == null || writeList == null
                || writeByteList == null || queueLengthList == null || diskTimeList == null) {
            return stats;
        }
        for (int i = 0; i < instances.size(); i++) {
            String name = getIndexFromName(instances.get(i));
            // If index arg passed, only update passed arg
            if (index != null && !index.equals(name)) {
                continue;
            }
            stats.readMap.put(name, readList.get(i));
            stats.readByteMap.put(name, readByteList.get(i));
            stats.writeMap.put(name, writeList.get(i));
            stats.writeByteMap.put(name, writeByteList.get(i));
            stats.queueLengthMap.put(name, queueLengthList.get(i));
            stats.diskTimeMap.put(name, diskTimeList.get(i) / 10_000L);
        }
        return stats;
    }

    /**
     * Queries the partition maps.
     *
     * @param h the h
     * @return the query partition maps result
     */
    private static PartitionMaps queryPartitionMaps(WmiQueryHandler h) {
        // Create object to hold and return results
        PartitionMaps maps = new PartitionMaps();

        // For Regexp matching DeviceIDs
        Matcher mAnt;
        Matcher mDep;

        // Map drives to partitions
        WmiResult<DriveToPartitionProperty> drivePartitionMap = Win32DiskDriveToDiskPartition.queryDriveToPartition(h);
        for (int i = 0; i < drivePartitionMap.getResultCount(); i++) {
            mAnt = DEVICE_ID.matcher(WmiKit.getRefString(drivePartitionMap, DriveToPartitionProperty.ANTECEDENT, i));
            mDep = DEVICE_ID.matcher(WmiKit.getRefString(drivePartitionMap, DriveToPartitionProperty.DEPENDENT, i));
            if (mAnt.matches() && mDep.matches()) {
                maps.driveToPartitionMap
                        .computeIfAbsent(mAnt.group(1).replace("\\\\", Symbol.BACKSLASH), x -> new ArrayList<>())
                        .add(mDep.group(1));
            }
        }

        // Map partitions to logical disks
        WmiResult<DiskToPartitionProperty> diskPartitionMap = Win32LogicalDiskToPartition.queryDiskToPartition(h);
        for (int i = 0; i < diskPartitionMap.getResultCount(); i++) {
            mAnt = DEVICE_ID.matcher(WmiKit.getRefString(diskPartitionMap, DiskToPartitionProperty.ANTECEDENT, i));
            mDep = DEVICE_ID.matcher(WmiKit.getRefString(diskPartitionMap, DiskToPartitionProperty.DEPENDENT, i));
            long size = WmiKit.getUint64(diskPartitionMap, DiskToPartitionProperty.ENDINGADDRESS, i)
                    - WmiKit.getUint64(diskPartitionMap, DiskToPartitionProperty.STARTINGADDRESS, i) + 1L;
            if (mAnt.matches() && mDep.matches()) {
                if (maps.partitionToLogicalDriveMap.containsKey(mAnt.group(1))) {
                    maps.partitionToLogicalDriveMap.get(mAnt.group(1))
                            .add(Pair.of(mDep.group(1) + Symbol.BACKSLASH, size));
                } else {
                    List<Pair<String, Long>> list = new ArrayList<>();
                    list.add(Pair.of(mDep.group(1) + Symbol.BACKSLASH, size));
                    maps.partitionToLogicalDriveMap.put(mAnt.group(1), list);
                }
            }
        }

        // Next, get all partitions and create objects
        WmiResult<DiskPartitionProperty> hwPartitionQueryMap = Win32DiskPartition.queryPartition(h);
        for (int i = 0; i < hwPartitionQueryMap.getResultCount(); i++) {
            String deviceID = WmiKit.getString(hwPartitionQueryMap, DiskPartitionProperty.DEVICEID, i);
            List<Pair<String, Long>> logicalDrives = maps.partitionToLogicalDriveMap.get(deviceID);
            if (logicalDrives == null) {
                continue;
            }
            for (int j = 0; j < logicalDrives.size(); j++) {
                Pair<String, Long> logicalDrive = logicalDrives.get(j);
                if (logicalDrive != null && !logicalDrive.getLeft().isEmpty()) {
                    char[] volumeChr = new char[GUID_BUFSIZE];
                    Kernel32.INSTANCE.GetVolumeNameForVolumeMountPoint(logicalDrive.getLeft(), volumeChr, GUID_BUFSIZE);
                    String uuid = Parsing.parseUuidOrDefault(new String(volumeChr).trim(), Normal.EMPTY);
                    char[] labelChr = new char[LABEL_BUFSIZE];
                    String label = Normal.EMPTY;
                    if (Kernel32.INSTANCE.GetVolumeInformation(
                            logicalDrive.getLeft(),
                            labelChr,
                            LABEL_BUFSIZE,
                            null,
                            null,
                            null,
                            null,
                            0)) {
                        label = new String(labelChr).trim();
                    } else {
                        int error = Kernel32.INSTANCE.GetLastError();
                        Logger.debug(
                                false,
                                "Health",
                                "Failed to get volume label for {}: error code {}",
                                logicalDrive.getLeft(),
                                error);
                    }
                    HWPartition pt = new HWPartition(
                            WmiKit.getString(hwPartitionQueryMap, DiskPartitionProperty.NAME, i),
                            WmiKit.getString(hwPartitionQueryMap, DiskPartitionProperty.TYPE, i),
                            WmiKit.getString(hwPartitionQueryMap, DiskPartitionProperty.DESCRIPTION, i), uuid, label,
                            logicalDrive.getRight(),
                            WmiKit.getUint32(hwPartitionQueryMap, DiskPartitionProperty.DISKINDEX, i),
                            WmiKit.getUint32(hwPartitionQueryMap, DiskPartitionProperty.INDEX, i),
                            logicalDrive.getLeft());
                    if (maps.partitionMap.containsKey(deviceID)) {
                        maps.partitionMap.get(deviceID).add(pt);
                    } else {
                        List<HWPartition> ptlist = new ArrayList<>();
                        ptlist.add(pt);
                        maps.partitionMap.put(deviceID, ptlist);
                    }
                }
            }
        }
        return maps;
    }

    /**
     * Parse a drive name like "0 C:" to just the index "0"
     *
     * @param s A drive name to parse
     * @return The first space-delimited value
     */
    private static String getIndexFromName(String s) {
        return s.split("\\s", 2)[0];
    }

    /**
     * Parses the Win32 disk media type, which can identify removability but not SSD versus HDD.
     *
     * @param mediaType The WMI media type.
     * @return {@code Removable} for removable media, otherwise {@code Unknown}.
     */
    private static String parseWindowsMediaType(String mediaType) {
        if (mediaType != null && StringKit.toLowerCase(mediaType).contains("removable")) {
            return DISK_TYPE_REMOVABLE;
        }
        return DISK_TYPE_UNKNOWN;
    }

    /**
     * Classifies a disk from storage driver properties with a WMI removability fallback.
     *
     * @param properties The storage properties, or {@code null} when unavailable.
     * @param mediaType  The Win32 media type.
     * @return The disk type.
     */
    private static String classifyDiskType(StorageProperties properties, String mediaType) {
        if (properties == null) {
            return parseWindowsMediaType(mediaType);
        }
        if (properties.isRemovableMedia()) {
            return DISK_TYPE_REMOVABLE;
        }
        if (properties.getBusType() == Normal._15 || properties.getBusType() == Normal._16) {
            return DISK_TYPE_VIRTUAL;
        }
        Boolean seekPenalty = properties.getIncursSeekPenalty();
        if (seekPenalty != null) {
            return seekPenalty ? DISK_TYPE_HDD : DISK_TYPE_SSD;
        }
        return DISK_TYPE_UNKNOWN;
    }

    /**
     * Converts Storage namespace media values to a disk type.
     *
     * @param mediaType    The Storage namespace media type.
     * @param spindleSpeed The spindle speed as an unsigned value.
     * @return The disk type.
     */
    private static String diskTypeFromMsftMediaType(int mediaType, long spindleSpeed) {
        switch (mediaType) {
            case Normal._3:
                return DISK_TYPE_HDD;

            case Normal._4:
            case Normal._5:
                return DISK_TYPE_SSD;

            default:
                return spindleSpeed > 0L ? DISK_TYPE_HDD : DISK_TYPE_UNKNOWN;
        }
    }

    /**
     * Builds a physical drive number to disk type map.
     *
     * @param disks The Storage namespace physical disk results.
     * @return The known disk types by drive number.
     */
    private static Map<String, String> buildMsftDiskTypeMap(WmiResult<PhysicalDiskMediaTypeProperty> disks) {
        Map<String, String> diskTypes = new HashMap<>();
        for (int i = Normal._0; i < disks.getResultCount(); i++) {
            String deviceId = WmiKit.getString(disks, PhysicalDiskMediaTypeProperty.DEVICEID, i);
            String diskType = diskTypeFromMsftMediaType(
                    WmiKit.getUint16(disks, PhysicalDiskMediaTypeProperty.MEDIATYPE, i),
                    WmiKit.getUint32asLong(disks, PhysicalDiskMediaTypeProperty.SPINDLESPEED, i));
            if (!deviceId.isEmpty() && !DISK_TYPE_UNKNOWN.equals(diskType)) {
                diskTypes.put(deviceId, diskType);
            }
        }
        return diskTypes;
    }

    /**
     * Classifies every Win32 disk through the storage driver.
     *
     * @param disks    The Win32 disk query result.
     * @param resolver The native storage property resolver.
     * @return A disk type for each query result.
     */
    private static String[] classifyDiskTypes(WmiResult<DiskDriveProperty> disks, StoragePropertiesResolver resolver) {
        String[] diskTypes = new String[disks.getResultCount()];
        for (int i = Normal._0; i < diskTypes.length; i++) {
            diskTypes[i] = classifyDiskType(
                    resolver.resolve(WmiKit.getString(disks, DiskDriveProperty.NAME, i)),
                    WmiKit.getString(disks, DiskDriveProperty.MEDIATYPE, i));
        }
        return diskTypes;
    }

    /**
     * Tests whether any disk type is still unknown.
     *
     * @param diskTypes The disk types.
     * @return {@code true} if at least one type is unknown.
     */
    private static boolean anyDiskTypeUnknown(String[] diskTypes) {
        for (String diskType : diskTypes) {
            if (DISK_TYPE_UNKNOWN.equals(diskType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fills disk types that the storage driver could not determine.
     *
     * @param diskTypes The disk types to update.
     * @param disks     The Win32 disk query result.
     * @param media     The Storage namespace media query result.
     */
    private static void fillUnknownDiskTypes(
            String[] diskTypes,
            WmiResult<DiskDriveProperty> disks,
            WmiResult<PhysicalDiskMediaTypeProperty> media) {
        Map<String, String> diskTypeMap = buildMsftDiskTypeMap(media);
        for (int i = Normal._0; i < diskTypes.length; i++) {
            if (DISK_TYPE_UNKNOWN.equals(diskTypes[i])) {
                String deviceId = Integer.toString(WmiKit.getUint32(disks, DiskDriveProperty.INDEX, i));
                diskTypes[i] = diskTypeMap.getOrDefault(deviceId, DISK_TYPE_UNKNOWN);
            }
        }
    }

    /**
     * Resolves native storage properties for a physical drive.
     */
    @FunctionalInterface
    private interface StoragePropertiesResolver {

        /**
         * Reads a drive's storage properties.
         *
         * @param name The physical drive path.
         * @return The properties, or {@code null} when unavailable.
         */
        StorageProperties resolve(String name);
    }

    /**
     * Storage properties used to classify a disk.
     *
     * @author Kimi Liu
     */
    private static final class StorageProperties {

        /** The storage bus type. */
        private final int busType;

        /** Whether the device holds removable media. */
        private final boolean removableMedia;

        /** Whether the device incurs a seek penalty, or {@code null} if unsupported. */
        private final Boolean incursSeekPenalty;

        /**
         * Creates storage properties.
         *
         * @param busType           The storage bus type.
         * @param removableMedia    Whether the media is removable.
         * @param incursSeekPenalty Whether the device incurs a seek penalty.
         */
        private StorageProperties(int busType, boolean removableMedia, Boolean incursSeekPenalty) {
            this.busType = busType;
            this.removableMedia = removableMedia;
            this.incursSeekPenalty = incursSeekPenalty;
        }

        /**
         * Returns the bus type.
         *
         * @return The bus type.
         */
        private int getBusType() {
            return busType;
        }

        /**
         * Returns whether the media is removable.
         *
         * @return {@code true} for removable media.
         */
        private boolean isRemovableMedia() {
            return removableMedia;
        }

        /**
         * Returns whether the device incurs a seek penalty.
         *
         * @return {@code true} for rotating media, {@code false} for solid state, or {@code null} if unsupported.
         */
        private Boolean getIncursSeekPenalty() {
            return incursSeekPenalty;
        }
    }

    /**
     * Returns the reads.
     *
     * @return the get reads result
     */
    @Override
    public long getReads() {
        return reads;
    }

    /**
     * Returns the read bytes.
     *
     * @return the get read bytes result
     */
    @Override
    public long getReadBytes() {
        return readBytes;
    }

    /**
     * Returns the writes.
     *
     * @return the get writes result
     */
    @Override
    public long getWrites() {
        return writes;
    }

    /**
     * Returns the write bytes.
     *
     * @return the get write bytes result
     */
    @Override
    public long getWriteBytes() {
        return writeBytes;
    }

    /**
     * Returns the current queue length.
     *
     * @return the get current queue length result
     */
    @Override
    public long getCurrentQueueLength() {
        return currentQueueLength;
    }

    /**
     * Returns the transfer time.
     *
     * @return the get transfer time result
     */
    @Override
    public long getTransferTime() {
        return transferTime;
    }

    /**
     * Returns the time stamp.
     *
     * @return the get time stamp result
     */
    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    /**
     * Returns the partitions.
     *
     * @return the get partitions result
     */
    @Override
    public List<HWPartition> getPartitions() {
        return this.partitionList;
    }

    /**
     * Returns the stores this disk is built on.
     *
     * @return The backing stores, or an empty optional when unavailable.
     */
    @Override
    public Optional<List<String>> getBackingStores() {
        return Optional.ofNullable(backingStores.get().get(getName()));
    }

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public synchronized boolean updateAttributes() {
        String index;
        List<HWPartition> partitions = getPartitions();
        if (!partitions.isEmpty()) {
            // If a partition exists on this drive, the major property
            // corresponds to the disk index, so use it.
            index = Integer.toString(partitions.get(0).getMajor());
        } else if (getName().startsWith(PHYSICALDRIVE_PREFIX)) {
            // If no partition exists, Windows reliably uses a name to match the
            // disk index. That said, the skeptical person might wonder why a
            // disk has read/write statistics without a partition, and wonder
            // why this branch is even relevant as an option. The author of this
            // comment does not have an answer for this valid question.
            index = getName().substring(PHYSICALDRIVE_PREFIX.length());
        } else {
            // The author of this comment cannot fathom a circumstance in which
            // the code reaches this point, but just in case it does, here's the
            // correct response. If you get this log warning, the circumstances
            // would be of great interest to the project's maintainers.
            Logger.warn(false, "Health", "Couldn't match index for {}", getName());
            return false;
        }
        DiskStats stats = queryReadWriteStats(index);
        if (stats.readMap.containsKey(index)) {
            this.reads = stats.readMap.getOrDefault(index, 0L);
            this.readBytes = stats.readByteMap.getOrDefault(index, 0L);
            this.writes = stats.writeMap.getOrDefault(index, 0L);
            this.writeBytes = stats.writeByteMap.getOrDefault(index, 0L);
            this.currentQueueLength = stats.queueLengthMap.getOrDefault(index, 0L);
            this.transferTime = stats.diskTimeMap.getOrDefault(index, 0L);
            this.timeStamp = stats.timeStamp;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Maps to store read/write bytes per drive index
     *
     * @author Kimi Liu
     */
    private static final class DiskStats {

        /**
         * The readMap value.
         */
        private final Map<String, Long> readMap = new HashMap<>();

        /**
         * The readByteMap value.
         */
        private final Map<String, Long> readByteMap = new HashMap<>();

        /**
         * The writeMap value.
         */
        private final Map<String, Long> writeMap = new HashMap<>();

        /**
         * The writeByteMap value.
         */
        private final Map<String, Long> writeByteMap = new HashMap<>();

        /**
         * The queueLengthMap value.
         */
        private final Map<String, Long> queueLengthMap = new HashMap<>();

        /**
         * The diskTimeMap value.
         */
        private final Map<String, Long> diskTimeMap = new HashMap<>();

        /**
         * The timeStamp value.
         */
        private long timeStamp;

    }

    /**
     * Maps for the partition structure
     *
     * @author Kimi Liu
     */
    private static final class PartitionMaps {

        /**
         * The driveToPartitionMap value.
         */
        private final Map<String, List<String>> driveToPartitionMap = new HashMap<>();

        /**
         * The partitionToLogicalDriveMap value.
         */
        private final Map<String, List<Pair<String, Long>>> partitionToLogicalDriveMap = new HashMap<>();

        /**
         * The partitionMap value.
         */
        private final Map<String, List<HWPartition>> partitionMap = new HashMap<>();

    }

}
