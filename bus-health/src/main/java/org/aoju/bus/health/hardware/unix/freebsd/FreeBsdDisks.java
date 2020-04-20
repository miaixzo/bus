/*********************************************************************************
 *                                                                               *
 * The MIT License                                                               *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org and other contributors.                      *
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
 ********************************************************************************/
package org.aoju.bus.health.hardware.unix.freebsd;

import org.aoju.bus.core.lang.Normal;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.common.unix.freebsd.BsdSysctlUtils;
import org.aoju.bus.health.hardware.Disks;
import org.aoju.bus.health.hardware.HWDiskStore;
import org.aoju.bus.health.hardware.HWPartition;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FreeBSD hard disk implementation.
 *
 * @author Kimi Liu
 * @version 5.8.8
 * @since JDK 1.8+
 */
public class FreeBsdDisks implements Disks {

    private static final Pattern MOUNT_PATTERN = Pattern.compile("/dev/(\\S+p\\d+) on (\\S+) .*");
    // Create map indexed by device name to populate data from multiple commands
    private static final Map<String, HWDiskStore> diskMap = new HashMap<>();
    // Map of partitions to mount points
    private static final Map<String, String> mountMap = new HashMap<>();

    /**
     * <p>
     * updateDiskStats.
     * </p>
     *
     * @param diskStore a {@link HWDiskStore} object.
     * @return a boolean.
     */
    public static boolean updateDiskStats(HWDiskStore diskStore) {
        List<String> output = Command.runNative("iostat -Ix " + diskStore.getName());
        long timeStamp = System.currentTimeMillis();
        boolean diskFound = false;
        for (String line : output) {
            String[] split = Builder.whitespaces.split(line);
            if (split.length < 7 || !split[0].equals(diskStore.getName())) {
                continue;
            }
            diskFound = true;
            diskStore.setReads((long) Builder.parseDoubleOrDefault(split[1], 0d));
            diskStore.setWrites((long) Builder.parseDoubleOrDefault(split[2], 0d));
            // In KB
            diskStore.setReadBytes((long) (Builder.parseDoubleOrDefault(split[3], 0d) * 1024));
            diskStore.setWriteBytes((long) (Builder.parseDoubleOrDefault(split[4], 0d) * 1024));
            // # transactions
            diskStore.setCurrentQueueLength(Builder.parseLongOrDefault(split[5], 0L));
            // In seconds, multiply for ms
            diskStore.setTransferTime((long) (Builder.parseDoubleOrDefault(split[6], 0d) * 1000));
            diskStore.setTimeStamp(timeStamp);
        }
        return diskFound;
    }


    @Override
    public HWDiskStore[] getDisks() {
        // Parse 'mount' to map partitions to mount point
        mountMap.clear();
        for (String mnt : Command.runNative("mount")) {
            Matcher m = MOUNT_PATTERN.matcher(mnt);
            if (m.matches()) {
                mountMap.put(m.group(1), m.group(2));
            }
        }

        // Get list of valid disks
        diskMap.clear();
        List<String> devices = Arrays.asList(Builder.whitespaces.split(BsdSysctlUtils.sysctl("kern.disks", Normal.EMPTY)));

        // Temporary list to hold partitions
        List<HWPartition> partList = new ArrayList<>();

        // Run iostat -Ix to enumerate disks by name and get kb r/w
        List<String> disks = Command.runNative("iostat -Ix");
        long timeStamp = System.currentTimeMillis();
        for (String line : disks) {
            String[] split = Builder.whitespaces.split(line);
            if (split.length < 7 || !devices.contains(split[0])) {
                continue;
            }
            HWDiskStore store = new HWDiskStore();
            store.setName(split[0]);
            store.setReads((long) Builder.parseDoubleOrDefault(split[1], 0d));
            store.setWrites((long) Builder.parseDoubleOrDefault(split[2], 0d));
            // In KB
            store.setReadBytes((long) (Builder.parseDoubleOrDefault(split[3], 0d) * 1024));
            store.setWriteBytes((long) (Builder.parseDoubleOrDefault(split[4], 0d) * 1024));
            // # transactions
            store.setCurrentQueueLength(Builder.parseLongOrDefault(split[5], 0L));
            // In seconds, multiply for ms
            store.setTransferTime((long) (Builder.parseDoubleOrDefault(split[6], 0d) * 1000));
            store.setTimeStamp(timeStamp);
            diskMap.put(split[0], store);
        }

        // Now grab geom output for disks
        List<String> geom = Command.runNative("geom disk list");

        HWDiskStore store = null;
        for (String line : geom) {
            if (line.startsWith("Geom name:")) {
                // Process partition list on current store, if any
                if (store != null) {
                    setPartitions(store, partList);
                }
                String device = line.substring(line.lastIndexOf(Symbol.C_SPACE) + 1);
                // Get the device.
                if (devices.contains(device)) {
                    store = diskMap.get(device);
                    // If for some reason we didn't have one, create
                    // a new value here.
                    if (store == null) {
                        store = new HWDiskStore();
                        store.setName(device);
                    }
                }
            }
            // If we don't have a valid store, don't bother parsing anything
            // until we do.
            if (store == null) {
                continue;
            }
            line = line.trim();
            if (line.startsWith("Mediasize:")) {
                String[] split = Builder.whitespaces.split(line);
                if (split.length > 1) {
                    store.setSize(Builder.parseLongOrDefault(split[1], 0L));
                }
            }
            if (line.startsWith("descr:")) {
                store.setModel(line.replace("descr:", Normal.EMPTY).trim());
            }
            if (line.startsWith("ident:")) {
                store.setSerial(line.replace("ident:", Normal.EMPTY).replace("(null)", Normal.EMPTY).trim());
            }
        }

        // Now grab geom output for partitions
        geom = Command.runNative("geom part list");
        store = null;
        HWPartition partition = null;
        for (String line : geom) {
            line = line.trim();
            if (line.startsWith("Geom name:")) {
                String device = line.substring(line.lastIndexOf(Symbol.C_SPACE) + 1);
                // Get the device.
                if (devices.contains(device)) {
                    store = diskMap.get(device);
                    // If for some reason we didn't have one, create
                    // a new value here.
                    if (store == null) {
                        store = new HWDiskStore();
                        store.setName(device);
                    }
                }
            }
            // If we don't have a valid store, don't bother parsing anything
            // until we do.
            if (store == null) {
                continue;
            }
            if (line.contains("Name:")) {
                // Save the current partition, if any
                if (partition != null) {
                    partList.add(partition);
                    partition = null;
                }
                // Verify new entry is a partition
                // (will happen in 'providers' section)
                String part = line.substring(line.lastIndexOf(Symbol.C_SPACE) + 1);
                if (part.startsWith(store.getName())) {
                    // Create a new partition.
                    partition = new HWPartition();
                    partition.setIdentification(part);
                    partition.setName(part);
                    partition.setMountPoint(mountMap.getOrDefault(part, Normal.EMPTY));
                }
            }
            // If we don't have a valid store, don't bother parsing anything
            // until we do.
            if (partition == null) {
                continue;
            }
            String[] split = Builder.whitespaces.split(line);
            if (split.length < 2) {
                continue;
            }
            if (line.startsWith("Mediasize:")) {
                partition.setSize(Builder.parseLongOrDefault(split[1], 0L));
            } else if (line.startsWith("rawuuid:")) {
                partition.setUuid(split[1]);
            } else if (line.startsWith("type:")) {
                partition.setType(split[1]);
            }
        }
        // Process last partition list
        if (store != null) {
            setPartitions(store, partList);
        }

        // Populate result array
        List<HWDiskStore> diskList = new ArrayList<>(diskMap.keySet().size());
        diskList.addAll(diskMap.values());
        Collections.sort(diskList);

        return diskList.toArray(new HWDiskStore[0]);
    }

    private void setPartitions(HWDiskStore store, List<HWPartition> partList) {
        HWPartition[] partitions = new HWPartition[partList.size()];
        int index = 0;
        Collections.sort(partList);
        for (HWPartition partition : partList) {
            // FreeBSD Major # is 0.
            // Minor # is filesize of /dev entry.
            partition.setMinor(Builder
                    .parseIntOrDefault(Command.getFirstAnswer("stat -f %i /dev/" + partition.getName()), 0));
            partitions[index++] = partition;
        }
        partList.clear();
        store.setPartitions(partitions);
    }
}
