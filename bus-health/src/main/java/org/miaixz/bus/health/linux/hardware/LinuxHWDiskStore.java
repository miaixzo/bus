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
package org.miaixz.bus.health.linux.hardware;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.hardware.HWDiskStore;
import org.miaixz.bus.health.builtin.hardware.HWPartition;
import org.miaixz.bus.health.builtin.hardware.common.AbstractHWDiskStore;
import org.miaixz.bus.health.linux.DevPath;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.health.linux.software.LinuxOperatingSystem;
import org.miaixz.bus.logger.Logger;

import com.sun.jna.platform.linux.Udev;
import com.sun.jna.platform.linux.Udev.UdevContext;
import com.sun.jna.platform.linux.Udev.UdevDevice;
import com.sun.jna.platform.linux.Udev.UdevEnumerate;
import com.sun.jna.platform.linux.Udev.UdevListEntry;

/**
 * Linux hard disk implementation.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public final class LinuxHWDiskStore extends AbstractHWDiskStore {

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
     * The SECTORSIZE constant.
     */
    private static final int SECTORSIZE = 512;

    // Get a list of orders to pass to Parsing
    /**
     * The UDEV_STAT_ORDERS constant.
     */
    private static final int[] UDEV_STAT_ORDERS = new int[UdevStat.values().length];
    // There are at least 11 elements in udev stat output or sometimes 15. We want
    // the rightmost 11 or 15 if there is leading text.
    /**
     * The UDEV_STAT_LENGTH constant.
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
     * Creates a new LinuxHWDiskStore instance.
     *
     * @param name   the name
     * @param model  the model
     * @param serial the serial
     * @param size   the size
     */
    private LinuxHWDiskStore(String name, String model, String serial, long size) {
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
            Logger.warn(false, "Health", "Disk Store information requires libudev, which is not present.");
            return Collections.emptyList();
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
                                        devModel = LOGICAL_VOLUME_GROUP;
                                        devSerial = device.getPropertyValue(DM_UUID);
                                        store = new LinuxHWDiskStore(devnode, devModel,
                                                devSerial == null ? Normal.UNKNOWN : devSerial, devSize, "Virtual");
                                        String vgName = device.getPropertyValue(DM_VG_NAME);
                                        String lvName = device.getPropertyValue(DM_LV_NAME);
                                        if (vgName != null && lvName != null && devSerial != null
                                                && devSerial.startsWith("LVM-")) {
                                            store.partitionList.add(
                                                    new HWPartition(getPartitionNameForDmDevice(vgName, lvName),
                                                            device.getSysname(),
                                                            device.getPropertyValue(ID_FS_TYPE) == null ? PARTITION
                                                                    : device.getPropertyValue(ID_FS_TYPE),
                                                            device.getPropertyValue(ID_FS_UUID) == null ? Normal.EMPTY
                                                                    : device.getPropertyValue(ID_FS_UUID),
                                                            device.getPropertyValue(ID_FS_LABEL) == null ? ""
                                                                    : device.getPropertyValue(ID_FS_LABEL),
                                                            Parsing.parseLongOrDefault(device.getSysattrValue(SIZE), 0L)
                                                                    * SECTORSIZE,
                                                            Parsing.parseIntOrDefault(
                                                                    device.getPropertyValue(MAJOR),
                                                                    0),
                                                            Parsing.parseIntOrDefault(
                                                                    device.getPropertyValue(MINOR),
                                                                    0),
                                                            getMountPointOfDmDevice(vgName, lvName)));
                                        }
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
                                                        device.getPropertyValue(ID_FS_LABEL) == null ? ""
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
     * Reads the mounts map.
     *
     * @return the read mounts map result
     */
    private static Map<String, String> readMountsMap() {
        Map<String, String> mountsMap = new HashMap<>();
        List<String> mounts = Builder.readFile(ProcPath.MOUNTS);
        for (String mount : mounts) {
            String[] split = Pattern.SPACES_PATTERN.split(mount);
            if (split.length < 2 || !split[0].startsWith(DevPath.DEV)) {
                continue;
            }
            mountsMap.put(split[0], split[1]);
        }
        return mountsMap;
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
        return DevPath.DEV + vgName + '/' + lvName;
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
        if ("1".equals(removable)) {
            return "Removable";
        }
        String rotational = device.getSysattrValue("queue/rotational");
        if ("0".equals(rotational)) {
            return "SSD";
        } else if ("1".equals(rotational)) {
            return "HDD";
        }
        return "Unknown";
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
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public boolean updateAttributes() {
        // If this returns non-empty (the same store, but updated) then we were
        // successful in the update
        return !getDisks(this).isEmpty();
    }

    // Order the field is in udev stats
    /**
     * The UdevStat enum.
     */
    enum UdevStat {

        // The parsing implementation in Parsing requires these to be declared
        // in increasing order. Use 0-ordered index here
        READS(0), READ_BYTES(2), WRITES(4), WRITE_BYTES(6), QUEUE_LENGTH(8), ACTIVE_MS(9);

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
