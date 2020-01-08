/*
 * The MIT License
 *
 * Copyright (c) 2020 aoju.org All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.aoju.bus.health.software.unix.freebsd;

import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.Command;
import org.aoju.bus.health.common.unix.freebsd.BsdSysctlUtils;
import org.aoju.bus.health.software.FileSystem;
import org.aoju.bus.health.software.OSFileStore;

import java.io.File;
import java.util.*;

/**
 * The Solaris File System contains {@link OSFileStore}s which
 * are a storage pool, device, partition, volume, concrete file system or other
 * implementation specific means of file storage. In Linux, these are found in
 * the /proc/mount filesystem, excluding temporary and kernel mounts.
 *
 * @author Kimi Liu
 * @version 5.5.1
 * @since JDK 1.8+
 */
public class FreeBsdFileSystem implements FileSystem {

    // Linux defines a set of virtual file systems
    private final List<String> pseudofs = Arrays.asList( //
            "procfs", // Proc file system
            "devfs", // Dev temporary file system
            "ctfs", // Contract file system
            "fdescfs", // fd
            "objfs", // Object file system
            "mntfs", // Mount file system
            "sharefs", // Share file system
            "lofs", // Library file system
            "autofs" // Auto mounting fs
            // "tmpfs", // Temporary file system
            // NOTE: tmpfs is evaluated apart, because Solaris uses it for
            // RAMdisks
    );

    // System path mounted as tmpfs
    private final List<String> tmpfsPaths = Arrays.asList("/system", "/tmp", "/dev/fd");

    /**
     * <p>
     * updateFileStoreStats.
     * </p>
     *
     * @param osFileStore a {@link OSFileStore} object.
     * @return a boolean.
     */
    public static boolean updateFileStoreStats(OSFileStore osFileStore) {
        // Just as fast to query all of them
        for (OSFileStore fileStore : new FreeBsdFileSystem().getFileStores()) {
            if (osFileStore.getName().equals(fileStore.getName())
                    && osFileStore.getVolume().equals(fileStore.getVolume())
                    && osFileStore.getMount().equals(fileStore.getMount())) {
                osFileStore.setLogicalVolume(fileStore.getLogicalVolume());
                osFileStore.setDescription(fileStore.getDescription());
                osFileStore.setType(fileStore.getType());
                osFileStore.setUUID(fileStore.getUUID());
                osFileStore.setFreeSpace(fileStore.getFreeSpace());
                osFileStore.setUsableSpace(fileStore.getUsableSpace());
                osFileStore.setTotalSpace(fileStore.getTotalSpace());
                osFileStore.setFreeInodes(fileStore.getFreeInodes());
                osFileStore.setTotalInodes(fileStore.getTotalInodes());
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if file path equals or starts with an element in the given list
     *
     * @param aList   A list of path prefixes
     * @param charSeq a path to check
     * @return true if the charSeq exactly equals, or starts with the directory in
     * aList
     */
    private boolean listElementStartsWith(List<String> aList, String charSeq) {
        for (String match : aList) {
            if (charSeq.equals(match) || charSeq.startsWith(match + Symbol.SLASH)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Gets File System Information.
     */
    @Override
    public OSFileStore[] getFileStores() {
        // Find any partition UUIDs and map them
        Map<String, String> uuidMap = new HashMap<>();
        // Now grab dmssg output
        String device = "";
        for (String line : Command.runNative("geom part list")) {
            if (line.contains("Name: ")) {
                device = line.substring(line.lastIndexOf(Symbol.C_SPACE) + 1);
            }
            // If we aren't working with a current partition, continue
            if (device.isEmpty()) {
                continue;
            }
            line = line.trim();
            if (line.startsWith("rawuuid:")) {
                uuidMap.put(device, line.substring(line.lastIndexOf(Symbol.C_SPACE) + 1));
                device = "";
            }
        }

        List<OSFileStore> fsList = new ArrayList<>();

        // Get inode usage data
        Map<String, Long> inodeFreeMap = new HashMap<>();
        Map<String, Long> inodeTotalMap = new HashMap<>();
        for (String line : Command.runNative("df -i")) {
            /*- Sample Output:
            Filesystem    1K-blocks   Used   Avail Capacity iused  ifree %iused  Mounted on
            /dev/twed0s1a   2026030 584112 1279836    31%    2751 279871    1%   /
            */
            if (line.startsWith(Symbol.SLASH)) {
                String[] split = Builder.whitespaces.split(line);
                if (split.length > 7) {
                    inodeFreeMap.put(split[0], Builder.parseLongOrDefault(split[6], 0L));
                    // total is used + free
                    inodeTotalMap.put(split[0],
                            inodeFreeMap.get(split[0]) + Builder.parseLongOrDefault(split[5], 0L));
                }
            }
        }

        // Get mount table
        for (String fs : Command.runNative("mount -p")) {
            String[] split = Builder.whitespaces.split(fs);
            if (split.length < 5) {
                continue;
            }
            // 1st field is volume name
            // 2nd field is mount point
            // 3rd field is fs type
            // other fields ignored
            String volume = split[0];
            String path = split[1];
            String type = split[2];

            // Exclude pseudo file systems
            if (this.pseudofs.contains(type) || path.equals("/dev") || listElementStartsWith(this.tmpfsPaths, path)
                    || volume.startsWith("rpool") && !path.equals(Symbol.SLASH)) {
                continue;
            }

            String name = path.substring(path.lastIndexOf(Symbol.C_SLASH) + 1);
            // Special case for /, pull last element of volume instead
            if (name.isEmpty()) {
                name = volume.substring(volume.lastIndexOf(Symbol.C_SLASH) + 1);
            }
            File f = new File(path);
            long totalSpace = f.getTotalSpace();
            long usableSpace = f.getUsableSpace();
            long freeSpace = f.getFreeSpace();

            String description;
            if (volume.startsWith("/dev") || path.equals(Symbol.SLASH)) {
                description = "Local Disk";
            } else if (volume.equals("tmpfs")) {
                description = "Ram Disk";
            } else if (type.startsWith("nfs") || type.equals("cifs")) {
                description = "Network Disk";
            } else {
                description = "Mount Point";
            }
            // Match UUID
            String uuid = uuidMap.getOrDefault(name, "");

            // Add to the list
            OSFileStore osStore = new OSFileStore();
            osStore.setName(name);
            osStore.setVolume(volume);
            osStore.setMount(path);
            osStore.setDescription(description);
            osStore.setType(type);
            osStore.setUUID(uuid);
            osStore.setFreeSpace(freeSpace);
            osStore.setUsableSpace(usableSpace);
            osStore.setTotalSpace(totalSpace);
            osStore.setFreeInodes(inodeFreeMap.containsKey(path) ? inodeFreeMap.get(path) : 0L);
            osStore.setTotalInodes(inodeTotalMap.containsKey(path) ? inodeTotalMap.get(path) : 0L);
            fsList.add(osStore);
        }
        return fsList.toArray(new OSFileStore[0]);
    }

    @Override
    public long getOpenFileDescriptors() {
        return BsdSysctlUtils.sysctl("kern.openfiles", 0);
    }

    @Override
    public long getMaxFileDescriptors() {
        return BsdSysctlUtils.sysctl("kern.maxfiles", 0);
    }
}
