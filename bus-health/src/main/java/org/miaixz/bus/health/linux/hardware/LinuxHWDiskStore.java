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
import com.sun.jna.platform.linux.Udev.UdevContext;
import com.sun.jna.platform.linux.Udev.UdevDevice;
import com.sun.jna.platform.linux.Udev.UdevEnumerate;
import com.sun.jna.platform.linux.Udev.UdevListEntry;

import org.miaixz.bus.core.center.function.SupplierX;
import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.xyz.StringKit;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Memoizer;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHWDiskStore;
import org.miaixz.bus.health.linux.DevPath;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.SysPath;
import org.miaixz.bus.health.linux.UdevData;
import org.miaixz.bus.health.linux.software.LinuxOperatingSystem;
import org.miaixz.bus.logger.Logger;

/**
 * Linux hard disk implementation.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class LinuxHWDiskStore extends AbstractHWDiskStore {

    /**
     * The BLOCK constant.
     */
    private static final String BLOCK = "block";

    /**
     * The DISK constant.
     */
    private static final String DISK = "disk";

    /**
     * The PARTITION constant.
     */
    private static final String PARTITION = "partition";

    /**
     * The STAT constant.
     */
    private static final String STAT = "stat";

    /**
     * The SIZE constant.
     */
    private static final String SIZE = "size";

    /**
     * The MINOR constant.
     */
    private static final String MINOR = "MINOR";

    /**
     * The MAJOR constant.
     */
    private static final String MAJOR = "MAJOR";

    /**
     * The ID_FS_TYPE constant.
     */
    private static final String ID_FS_TYPE = "ID_FS_TYPE";

    /**
     * The ID_FS_UUID constant.
     */
    private static final String ID_FS_UUID = "ID_FS_UUID";

    /**
     * The ID_FS_LABEL constant.
     */
    private static final String ID_FS_LABEL = "ID_FS_LABEL";

    /**
     * The ID_MODEL constant.
     */
    private static final String ID_MODEL = "ID_MODEL";

    /**
     * The ID_SERIAL_SHORT constant.
     */
    private static final String ID_SERIAL_SHORT = "ID_SERIAL_SHORT";

    /**
     * The DM_UUID constant.
     */
    private static final String DM_UUID = "DM_UUID";

    /**
     * The DM_NAME constant.
     */
    private static final String DM_NAME = "DM_NAME";

    /**
     * The DM_VG_NAME constant.
     */
    private static final String DM_VG_NAME = "DM_VG_NAME";

    /**
     * The DM_LV_NAME constant.
     */
    private static final String DM_LV_NAME = "DM_LV_NAME";

    /**
     * The LOGICAL_VOLUME_GROUP constant.
     */
    private static final String LOGICAL_VOLUME_GROUP = "Logical Volume Group";

    /**
     * The ENCRYPTED_VOLUME constant.
     */
    private static final String ENCRYPTED_VOLUME = "Encrypted Volume";

    /**
     * The DEVICE_MAPPER constant.
     */
    private static final String DEVICE_MAPPER = "Device Mapper";

    /**
     * The SECTORSIZE constant.
     */
    private static final int SECTORSIZE = 512;

    /**
     * Get a list of orders to pass to Parsing.
     */
    private static final int[] UDEV_STAT_ORDERS = new int[UdevStat.values().length];

    /**
     * There are at least 11 elements in udev stat output or sometimes 15. We want the rightmost 11 or 15 if there is
     * leading text.
     */
    private static final int UDEV_STAT_LENGTH;

    static {
        for (UdevStat stat : UdevStat.values()) {
            UDEV_STAT_ORDERS[stat.ordinal()] = stat.getOrder();
        }
    }

    static {
        String stat = Builder.getStringFromFile(ProcPath.DISKSTATS);
        int statLength = 11;
        if (!stat.isEmpty()) {
            statLength = Parsing.countStringToLongArray(stat, Symbol.C_SPACE);
        }
        UDEV_STAT_LENGTH = statLength;
    }

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
    private List<HWPartition> partitionList = new ArrayList<>();

    /**
     * The memoized backing stores.
     */
    private final SupplierX<Optional<List<String>>> backingStores = Memoizer
            .memoize(() -> readBackingStores(SysPath.BLOCK, getName()));

    /**
     * Creates a new LinuxHWDiskStore instance.
     *
     * @param name   the name
     * @param model  the model
     * @param serial the serial
     * @param size   the size
     */
    public LinuxHWDiskStore(String name, String model, String serial, long size) {
        super(name, model, serial, size);
    }

    /**
     * Creates a new LinuxHWDiskStore instance.
     *
     * @param name     the name
     * @param model    the model
     * @param serial   the serial
     * @param size     the size
     * @param diskType the disk type
     */
    private LinuxHWDiskStore(String name, String model, String serial, long size, String diskType) {
        super(name, model, serial, size, diskType);
    }

    /**
     * Returns the disks.
     *
     * @param storeToUpdate the store to update
     * @return the get disks result
     */
    private static List<HWDiskStore> getDisks(LinuxHWDiskStore storeToUpdate) {
        if (!LinuxOperatingSystem.HAS_UDEV) {
            Logger.debug(false, "Health", "libudev is not present; reading disk stores from sysfs.");
            return getDisksFromSysfs(storeToUpdate);
        }
        LinuxHWDiskStore store = null;
        List<HWDiskStore> result = new ArrayList<>();

        Map<String, String> mountsMap = readMountsMap();

        UdevContext udev = Udev.INSTANCE.udev_new();
        if (udev == null) {
            return Collections.emptyList();
        }
        try {
            UdevEnumerate enumerate = udev.enumerateNew();
            try {
                enumerate.addMatchSubsystem(BLOCK);
                enumerate.scanDevices();
                for (UdevListEntry entry = enumerate.getListEntry(); entry != null; entry = entry.getNext()) {
                    String syspath = entry.getName();
                    UdevDevice device = udev.deviceNewFromSyspath(syspath);
                    if (device != null) {
                        try {
                            // devnode is what we use as name, like /dev/sda
                            String devnode = device.getDevnode();
                            // Ignore loopback and ram disks; do nothing
                            if (devnode != null && !devnode.startsWith(DevPath.LOOP)
                                    && !devnode.startsWith(DevPath.RAM)) {
                                if (DISK.equals(device.getDevtype())) {
                                    // Null model and serial in virtual environments
                                    String devModel = device.getPropertyValue(ID_MODEL);
                                    String devSerial = device.getPropertyValue(ID_SERIAL_SHORT);
                                    long devSize = Parsing.parseLongOrDefault(device.getSysattrValue(SIZE), 0L)
                                            * SECTORSIZE;
                                    if (devnode.startsWith(DevPath.DM)) {
                                        devSerial = device.getPropertyValue(DM_UUID);
                                        devModel = getModelForDmDevice(devSerial);
                                        store = new LinuxHWDiskStore(devnode, devModel,
                                                devSerial == null ? Normal.UNKNOWN : devSerial, devSize, "Virtual");
                                        String vgName = device.getPropertyValue(DM_VG_NAME);
                                        String lvName = device.getPropertyValue(DM_LV_NAME);
                                        addDeviceMapperPartition(
                                                store,
                                                mountsMap,
                                                devSerial,
                                                vgName,
                                                lvName,
                                                device.getPropertyValue(DM_NAME),
                                                devnode,
                                                device.getSysname(),
                                                device.getSyspath(),
                                                device.getPropertyValue(ID_FS_TYPE),
                                                device.getPropertyValue(ID_FS_UUID),
                                                device.getPropertyValue(ID_FS_LABEL),
                                                Parsing.parseLongOrDefault(device.getSysattrValue(SIZE), 0L)
                                                        * SECTORSIZE,
                                                Parsing.parseIntOrDefault(device.getPropertyValue(MAJOR), 0),
                                                Parsing.parseIntOrDefault(device.getPropertyValue(MINOR), 0));
                                    } else {
                                        store = new LinuxHWDiskStore(devnode,
                                                devModel == null ? Normal.UNKNOWN : devModel,
                                                devSerial == null ? Normal.UNKNOWN : devSerial, devSize,
                                                detectDiskType(device));
                                    }
                                    if (storeToUpdate == null) {
                                        // If getting all stores, add to the list with stats
                                        computeDiskStats(store, device.getSysattrValue(STAT));
                                        result.add(store);
                                    } else if (store.getName().equals(storeToUpdate.getName())
                                            && store.getModel().equals(storeToUpdate.getModel())
                                            && store.getSerial().equals(storeToUpdate.getSerial())
                                            && store.getSize() == storeToUpdate.getSize()) {
                                        // If we are only updating a single disk, the name, model, serial, and size are
                                        // sufficient to test if this is a match. Add the (old) object, release handle
                                        // and return.
                                        computeDiskStats(storeToUpdate, device.getSysattrValue(STAT));
                                        result.add(storeToUpdate);
                                        break;
                                    }
                                } else if (storeToUpdate == null && store != null // only add if getting new list
                                        && PARTITION.equals(device.getDevtype())) {
                                    // udev_device_get_parent_*() does not take a reference on the returned device,
                                    // it is automatically unref'd with the parent
                                    UdevDevice parent = device.getParentWithSubsystemDevtype(BLOCK, DISK);
                                    if (parent != null && store.getName().equals(parent.getDevnode())) {
                                        // `store` should still point to the parent HWDiskStore this partition is
                                        // attached to. If not, it's an error, so skip.
                                        String name = device.getDevnode();
                                        store.partitionList.add(
                                                new HWPartition(name, device.getSysname(),
                                                        device.getPropertyValue(ID_FS_TYPE) == null ? PARTITION
                                                                : device.getPropertyValue(ID_FS_TYPE),
                                                        device.getPropertyValue(ID_FS_UUID) == null ? Normal.EMPTY
                                                                : device.getPropertyValue(ID_FS_UUID),
                                                        device.getPropertyValue(ID_FS_LABEL) == null ? Normal.EMPTY
                                                                : device.getPropertyValue(ID_FS_LABEL),
                                                        Parsing.parseLongOrDefault(device.getSysattrValue(SIZE), 0L)
                                                                * SECTORSIZE,
                                                        Parsing.parseIntOrDefault(device.getPropertyValue(MAJOR), 0),
                                                        Parsing.parseIntOrDefault(device.getPropertyValue(MINOR), 0),
                                                        mountsMap.getOrDefault(
                                                                name,
                                                                getDependentNamesFromHoldersDirectory(
                                                                        device.getSyspath()))));
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
        // Iterate the list and make the partitions unmodifiable
        for (HWDiskStore hwds : result) {
            ((LinuxHWDiskStore) hwds).partitionList = Collections.unmodifiableList(
                    hwds.getPartitions().stream().sorted(Comparator.comparing(HWPartition::getName))
                            .collect(Collectors.toList()));
        }
        return result;
    }

    /**
     * Reads disk stores directly from sysfs and supplements them with the udev property database when available.
     *
     * @param storeToUpdate an existing store to update, or {@code null} to enumerate all stores
     * @return matching disk stores
     */
    private static List<HWDiskStore> getDisksFromSysfs(LinuxHWDiskStore storeToUpdate) {
        List<HWDiskStore> result = new ArrayList<>();
        Map<String, String> mountsMap = readMountsMap();
        File[] blockDevices = new File(SysPath.BLOCK).listFiles();
        if (blockDevices == null) {
            return result;
        }
        for (File blockDevice : blockDevices) {
            String sysname = blockDevice.getName();
            String devnode = DevPath.NODE_ROOT + toDevName(sysname);
            if (devnode.startsWith(DevPath.LOOP) || devnode.startsWith(DevPath.RAM)) {
                continue;
            }
            String syspath = blockDevice.getPath() + Symbol.SLASH;
            long size = Parsing.parseLongOrDefault(Builder.getStringFromFile(syspath + SIZE).trim(), 0L) * SECTORSIZE;
            int[] majorMinor = readMajorMinor(syspath);
            Map<String, String> udev = UdevData.queryBlockDevice(majorMinor[Normal._0], majorMinor[Normal._1]);
            LinuxHWDiskStore store;
            if (new File(syspath + "dm").isDirectory()) {
                String dmUuid = Builder.getStringFromFile(syspath + "dm/uuid").trim();
                store = new LinuxHWDiskStore(devnode, getModelForDmDevice(dmUuid),
                        Parsing.getStringValueOrUnknown(dmUuid), size, "Virtual");
                String dmName = Builder.getStringFromFile(syspath + "dm/name").trim();
                String[] vgLv = isLogicalVolume(dmUuid) ? splitLvmDmName(dmName)
                        : new String[] { Normal.EMPTY, Normal.EMPTY };
                addDeviceMapperPartition(
                        store,
                        mountsMap,
                        dmUuid,
                        vgLv[Normal._0],
                        vgLv[Normal._1],
                        dmName,
                        devnode,
                        sysname,
                        syspath,
                        udev.get(ID_FS_TYPE),
                        udev.get(ID_FS_UUID),
                        udev.get(ID_FS_LABEL),
                        size,
                        majorMinor[Normal._0],
                        majorMinor[Normal._1]);
            } else {
                String model = Parsing.getStringValueOrDefault(
                        udev.get(ID_MODEL),
                        Builder.getStringFromFile(syspath + "device/model").trim());
                String serial = Parsing.getStringValueOrDefault(
                        udev.get(ID_SERIAL_SHORT),
                        Builder.getStringFromFile(syspath + "device/serial").trim());
                store = new LinuxHWDiskStore(devnode, Parsing.getStringValueOrUnknown(model),
                        Parsing.getStringValueOrUnknown(serial), size, detectDiskType(syspath));
            }
            if (storeToUpdate != null) {
                if (store.getName().equals(storeToUpdate.getName()) && store.getModel().equals(storeToUpdate.getModel())
                        && store.getSerial().equals(storeToUpdate.getSerial())
                        && store.getSize() == storeToUpdate.getSize()) {
                    computeDiskStats(storeToUpdate, Builder.getStringFromFile(syspath + STAT).trim());
                    result.add(storeToUpdate);
                    break;
                }
                continue;
            }
            computeDiskStats(store, Builder.getStringFromFile(syspath + STAT).trim());
            File[] partitions = blockDevice.listFiles(file -> new File(file, PARTITION).isFile());
            if (partitions != null) {
                for (File partition : partitions) {
                    String partName = partition.getName();
                    String partPath = partition.getPath() + Symbol.SLASH;
                    int[] partMajorMinor = readMajorMinor(partPath);
                    Map<String, String> partUdev = UdevData
                            .queryBlockDevice(partMajorMinor[Normal._0], partMajorMinor[Normal._1]);
                    String partDevnode = DevPath.NODE_ROOT + toDevName(partName);
                    long partSize = Parsing.parseLongOrDefault(Builder.getStringFromFile(partPath + SIZE).trim(), 0L)
                            * SECTORSIZE;
                    store.partitionList.add(
                            new HWPartition(partDevnode, partName,
                                    Parsing.getStringValueOrDefault(partUdev.get(ID_FS_TYPE), PARTITION),
                                    Parsing.getStringValueOrEmpty(partUdev.get(ID_FS_UUID)),
                                    Parsing.getStringValueOrEmpty(partUdev.get(ID_FS_LABEL)), partSize,
                                    partMajorMinor[Normal._0], partMajorMinor[Normal._1],
                                    mountsMap.getOrDefault(
                                            partDevnode,
                                            getDependentNamesFromHoldersDirectory(partPath))));
                }
            }
            store.partitionList = Collections.unmodifiableList(store.partitionList);
            result.add(store);
        }
        return result;
    }

    /**
     * Reads a block device's major and minor numbers from its sysfs {@code dev} file.
     *
     * @param sysPath the device sysfs path, ending with a slash
     * @return the major and minor numbers
     */
    private static int[] readMajorMinor(String sysPath) {
        String[] values = Builder.getStringFromFile(sysPath + "dev").trim().split(Symbol.COLON, Normal._2);
        if (values.length < Normal._2) {
            return new int[] { Normal._0, Normal._0 };
        }
        return new int[] { Parsing.parseIntOrDefault(values[Normal._0], Normal._0),
                Parsing.parseIntOrDefault(values[Normal._1], Normal._0) };
    }

    /**
     * Reads the mounts map.
     *
     * @return the read mounts map result
     */
    private static Map<String, String> readMountsMap() {
        return parseMountsMap(Builder.readFile(ProcPath.MOUNTS));
    }

    /**
     * Parses mount records into device names and their first mount points.
     *
     * @param mounts the mount records
     * @return the device-to-mount map with kernel octal escapes decoded
     */
    static Map<String, String> parseMountsMap(List<String> mounts) {
        Map<String, String> mountsMap = new HashMap<>();
        for (String mount : mounts) {
            String[] split = Pattern.SPACES_PATTERN.split(mount);
            if (split.length < Normal._2 || !split[Normal._0].startsWith(DevPath.NODE_ROOT)) {
                continue;
            }
            mountsMap.putIfAbsent(Parsing.unescapeOctal(split[Normal._0]), Parsing.unescapeOctal(split[Normal._1]));
        }
        return mountsMap;
    }

    /**
     * Reads the stores a disk is built on from its sysfs {@code slaves} directory.
     *
     * @param sysBlock The sysfs block directory.
     * @param name     The disk device path.
     * @return The sorted backing stores, or an empty optional if the directory could not be read.
     */
    static Optional<List<String>> readBackingStores(String sysBlock, String name) {
        if (!name.startsWith(DevPath.NODE_ROOT)) {
            return Optional.empty();
        }
        File[] slaves = new File(sysBlock + toSysfsName(name.substring(DevPath.NODE_ROOT.length())) + "/slaves")
                .listFiles();
        if (slaves == null) {
            return Optional.empty();
        }
        List<String> stores = new ArrayList<>();
        for (File slave : slaves) {
            String slaveName = slave.getName();
            StoreKind kind = new File(sysBlock + slaveName).exists() ? StoreKind.DISK : StoreKind.PARTITION;
            stores.add(kind.entry(DevPath.NODE_ROOT + toDevName(slaveName)));
        }
        stores.sort(null);
        return Optional.of(Collections.unmodifiableList(stores));
    }

    /**
     * Converts a sysfs block device name to its path below the device root.
     *
     * @param sysfsName The sysfs device name.
     * @return The relative device path.
     */
    private static String toDevName(String sysfsName) {
        return sysfsName.replace('!', Symbol.C_SLASH);
    }

    /**
     * Converts a relative device path to its sysfs block device name.
     *
     * @param devName The relative device path.
     * @return The sysfs device name.
     */
    private static String toSysfsName(String devName) {
        return devName.replace(Symbol.C_SLASH, '!');
    }

    /**
     * Handles the compute disk stats operation.
     *
     * @param store   the store
     * @param devstat the devstat
     */
    private static void computeDiskStats(LinuxHWDiskStore store, String devstat) {
        long[] devstatArray = Parsing
                .parseStringToLongArray(devstat, UDEV_STAT_ORDERS, UDEV_STAT_LENGTH, Symbol.C_SPACE);
        store.timeStamp = System.currentTimeMillis();

        // Reads and writes are converted in bytes
        store.reads = devstatArray[UdevStat.READS.ordinal()];
        store.readBytes = devstatArray[UdevStat.READ_BYTES.ordinal()] * SECTORSIZE;
        store.writes = devstatArray[UdevStat.WRITES.ordinal()];
        store.writeBytes = devstatArray[UdevStat.WRITE_BYTES.ordinal()] * SECTORSIZE;
        store.currentQueueLength = devstatArray[UdevStat.QUEUE_LENGTH.ordinal()];
        store.transferTime = devstatArray[UdevStat.ACTIVE_MS.ordinal()];
    }

    /**
     * Returns the partition name for dm device.
     *
     * @param vgName the vg name
     * @param lvName the lv name
     * @return the get partition name for dm device result
     */
    private static String getPartitionNameForDmDevice(String vgName, String lvName) {
        return DevPath.NODE_ROOT + vgName + Symbol.C_SLASH + lvName;
    }

    /**
     * Splits an LVM device-mapper name into volume-group and logical-volume names.
     *
     * @param dmName the device-mapper name, with literal hyphens escaped as doubled hyphens
     * @return the volume-group and logical-volume names
     */
    protected static String[] splitLvmDmName(String dmName) {
        String[] names = { Normal.EMPTY, Normal.EMPTY };
        StringBuilder value = new StringBuilder();
        int component = Normal._0;
        int index = Normal._0;
        while (index < dmName.length() && component < names.length) {
            char character = dmName.charAt(index++);
            if (character != Symbol.C_MINUS) {
                value.append(character);
            } else if (index < dmName.length() && dmName.charAt(index) == Symbol.C_MINUS) {
                value.append(Symbol.C_MINUS);
                index++;
            } else {
                names[component++] = value.toString();
                value.setLength(Normal._0);
            }
        }
        if (component < names.length) {
            names[component] = value.toString();
        }
        return names;
    }

    /**
     * Returns the mount point of dm device.
     *
     * @param vgName the vg name
     * @param lvName the lv name
     * @return the get mount point of dm device result
     */
    private static String getMountPointOfDmDevice(String vgName, String lvName) {
        return DevPath.MAPPER + vgName + Symbol.C_MINUS + lvName;
    }

    /**
     * Returns the model description for a device-mapper device.
     *
     * @param dmUuid the device-mapper UUID
     * @return the model description
     */
    private static String getModelForDmDevice(String dmUuid) {
        if (isLogicalVolume(dmUuid)) {
            return LOGICAL_VOLUME_GROUP;
        } else if (isEncryptedVolume(dmUuid)) {
            return ENCRYPTED_VOLUME;
        }
        return DEVICE_MAPPER;
    }

    /**
     * Returns whether a device-mapper UUID identifies an LVM logical volume.
     *
     * @param dmUuid the device-mapper UUID
     * @return whether the UUID identifies an LVM logical volume
     */
    private static boolean isLogicalVolume(String dmUuid) {
        return dmUuid != null && dmUuid.startsWith("LVM-");
    }

    /**
     * Returns whether a device-mapper UUID identifies an encrypted volume.
     *
     * @param dmUuid the device-mapper UUID
     * @return whether the UUID identifies an encrypted volume
     */
    private static boolean isEncryptedVolume(String dmUuid) {
        return dmUuid != null && dmUuid.startsWith("CRYPT-");
    }

    /**
     * Returns the preferred path for a device-mapper device.
     *
     * @param dmName  the device-mapper name
     * @param devnode the device node path
     * @return the device path
     */
    private static String getDmDevicePath(String dmName, String devnode) {
        return StringKit.isBlank(dmName) ? devnode : DevPath.MAPPER + dmName;
    }

    /**
     * Returns the mount point for a device-mapper device.
     *
     * @param mountsMap the map of device paths to mount points
     * @param dmName    the device-mapper name
     * @param devnode   the device node path
     * @param sysPath   the sysfs path for the device
     * @return the mount point or dependent device names
     */
    private static String getMountPointForDmDevice(
            Map<String, String> mountsMap,
            String dmName,
            String devnode,
            String sysPath) {
        String devicePath = getDmDevicePath(dmName, devnode);
        String mountPoint = mountsMap.get(devicePath);
        if (mountPoint != null) {
            return mountPoint;
        }
        if (!devicePath.equals(devnode)) {
            mountPoint = mountsMap.get(devnode);
            if (mountPoint != null) {
                return mountPoint;
            }
        }
        return getDependentNamesFromHoldersDirectory(sysPath);
    }

    /**
     * Adds a partition entry for a supported device-mapper device.
     *
     * @param store     the disk store to update
     * @param mountsMap the map of device paths to mount points
     * @param dmUuid    the device-mapper UUID
     * @param vgName    the LVM volume group name
     * @param lvName    the LVM logical volume name
     * @param dmName    the device-mapper name
     * @param devnode   the device node path
     * @param sysname   the sysfs device name
     * @param sysPath   the sysfs path for the device
     * @param fsType    the filesystem type
     * @param fsUuid    the filesystem UUID
     * @param fsLabel   the filesystem label
     * @param size      the partition size in bytes
     * @param major     the major device ID
     * @param minor     the minor device ID
     */
    private static void addDeviceMapperPartition(
            LinuxHWDiskStore store,
            Map<String, String> mountsMap,
            String dmUuid,
            String vgName,
            String lvName,
            String dmName,
            String devnode,
            String sysname,
            String sysPath,
            String fsType,
            String fsUuid,
            String fsLabel,
            long size,
            int major,
            int minor) {
        if (isLogicalVolume(dmUuid) && !StringKit.isBlank(vgName) && !StringKit.isBlank(lvName)) {
            String vg = Parsing.getStringValueOrEmpty(vgName);
            String lv = Parsing.getStringValueOrEmpty(lvName);
            store.partitionList.add(
                    new HWPartition(getPartitionNameForDmDevice(vg, lv), sysname, fsType == null ? PARTITION : fsType,
                            fsUuid == null ? Normal.EMPTY : fsUuid, fsLabel == null ? Normal.EMPTY : fsLabel, size,
                            major, minor, getMountPointOfDmDevice(vg, lv)));
        } else if (isEncryptedVolume(dmUuid)) {
            String name = getDmDevicePath(dmName, devnode);
            store.partitionList.add(
                    new HWPartition(name, sysname, fsType == null ? PARTITION : fsType,
                            fsUuid == null ? Normal.EMPTY : fsUuid, fsLabel == null ? Normal.EMPTY : fsLabel, size,
                            major, minor, getMountPointForDmDevice(mountsMap, dmName, devnode, sysPath)));
        }
    }

    /**
     * Gets the disks on this machine
     *
     * @return a list of {@link HWDiskStore} objects representing the disks
     */
    public static List<HWDiskStore> getDisks() {
        return getDisks(null);
    }

    /**
     * Detects the disk type using Linux sysfs attributes exposed by udev.
     *
     * @param device the udev device
     * @return the detected disk type
     */
    private static String detectDiskType(UdevDevice device) {
        String removable = device.getSysattrValue("removable");
        if (Symbol.ONE.equals(removable)) {
            return "Removable";
        }
        String rotational = device.getSysattrValue("queue/rotational");
        if (Symbol.ZERO.equals(rotational)) {
            return "SSD";
        } else if (Symbol.ONE.equals(rotational)) {
            return "HDD";
        }
        return "Unknown";
    }

    /**
     * Detects a disk type from Linux sysfs attributes.
     *
     * @param sysPath the disk sysfs path, ending with a slash
     * @return the detected disk type
     */
    private static String detectDiskType(String sysPath) {
        if (Builder.getIntFromFile(sysPath + "removable") == Normal._1) {
            return "Removable";
        }
        int rotational = Builder.getIntFromFile(sysPath + "queue/rotational");
        if (rotational == Normal._0) {
            return "SSD";
        } else if (rotational == Normal._1) {
            return "HDD";
        }
        return Normal.UNKNOWN;
    }

    /**
     * Returns the dependent names from holders directory.
     *
     * @param sysPath the sys path
     * @return the get dependent names from holders directory result
     */
    private static String getDependentNamesFromHoldersDirectory(String sysPath) {
        File holdersDir = new File(sysPath + "/holders");
        File[] holders = holdersDir.listFiles();
        if (holders != null) {
            return Arrays.stream(holders).map(File::getName).collect(Collectors.joining(Symbol.SPACE));
        }
        return Normal.EMPTY;
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
     * Returns the backing stores.
     *
     * @return The backing stores, or an empty optional if unavailable.
     */
    @Override
    public Optional<List<String>> getBackingStores() {
        return backingStores.get();
    }

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public synchronized boolean updateAttributes() {
        // If this returns non-empty (the same store, but updated) then we were
        // successful in the update
        return !getDisks(this).isEmpty();
    }

    /**
     * The UdevStat enum.
     *
     * @author Kimi Liu
     */
    enum UdevStat {

        /**
         * Completed read operations.
         * <p>
         * The parsing implementation in Parsing requires these to be declared in increasing order. Use 0-ordered index
         * here.
         */
        READS(0),
        /**
         * Bytes read from the device.
         */
        READ_BYTES(2),
        /**
         * Completed write operations.
         */
        WRITES(4),
        /**
         * Bytes written to the device.
         */
        WRITE_BYTES(6),
        /**
         * Current I/O queue length.
         */
        QUEUE_LENGTH(8),
        /**
         * Milliseconds spent servicing I/O.
         */
        ACTIVE_MS(9);

        /**
         * The order value.
         */
        private final int order;

        /**
         * Creates a new UdevStat instance.
         *
         * @param order the order
         */
        UdevStat(int order) {
            this.order = order;
        }

        /**
         * Returns the order.
         *
         * @return the get order result
         */
        public int getOrder() {
            return this.order;
        }

    }

}
