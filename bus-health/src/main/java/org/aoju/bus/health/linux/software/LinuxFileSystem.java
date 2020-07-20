/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2020 aoju.org OSHI and other contributors.                 *
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
package org.aoju.bus.health.linux.software;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;
import org.aoju.bus.core.annotation.ThreadSafe;
import org.aoju.bus.core.lang.Symbol;
import org.aoju.bus.core.toolkit.FileKit;
import org.aoju.bus.health.Builder;
import org.aoju.bus.health.builtin.software.AbstractFileSystem;
import org.aoju.bus.health.builtin.software.OSFileStore;
import org.aoju.bus.health.linux.ProcPath;
import org.aoju.bus.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * The Linux File System contains {@link OSFileStore}s which
 * are a storage pool, device, partition, volume, concrete file system or other
 * implementation specific means of file storage. In Linux, these are found in
 * the /proc/mount filesystem, excluding temporary and kernel mounts.
 *
 * @author Kimi Liu
 * @version 6.0.3
 * @since JDK 1.8+
 */
@ThreadSafe
public class LinuxFileSystem extends AbstractFileSystem {

    private static final String UNICODE_SPACE = "\\\\040";

    // System path mounted as tmpfs
    private static final List<String> TMP_FS_PATHS = Arrays.asList("/run", "/sys", "/proc", ProcPath.PROC);

    // called from LinuxOSFileStore
    static List<OSFileStore> getFileStoreMatching(String nameToMatch, Map<String, String> uuidMap) {
        return getFileStoreMatching(nameToMatch, uuidMap, false);
    }

    private static List<OSFileStore> getFileStoreMatching(String nameToMatch, Map<String, String> uuidMap,
                                                          boolean localOnly) {
        List<OSFileStore> fsList = new ArrayList<>();

        // Parse /proc/mounts to get fs types
        List<String> mounts = FileKit.readLines(ProcPath.MOUNTS);
        for (String mount : mounts) {
            String[] split = mount.split(Symbol.SPACE);
            // As reported in fstab(5) manpage, struct is:
            // 1st field is volume name
            // 2nd field is path with spaces escaped as \040
            // 3rd field is fs type
            // 4th field is mount options
            // 5th field is used by dump(8) (ignored)
            // 6th field is fsck order (ignored)
            if (split.length < 6) {
                continue;
            }

            // Exclude pseudo file systems
            String path = split[1].replaceAll(UNICODE_SPACE, Symbol.SPACE);
            String type = split[2];
            if ((localOnly && NETWORK_FS_TYPES.contains(type)) // Skip non-local drives if requested
                    || PSEUDO_FS_TYPES.contains(type) // exclude non-fs types
                    || path.equals("/dev") // exclude plain dev directory
                    || Builder.filePathStartsWith(TMP_FS_PATHS, path) // well known prefixes
                    || path.endsWith("/shm") // exclude shared memory
            ) {
                continue;
            }
            String options = split[3];

            String name = split[0].replaceAll(UNICODE_SPACE, Symbol.SPACE);
            if (path.equals(Symbol.SLASH)) {
                name = Symbol.SLASH;
            }

            // If only updating for one name, skip others
            if (nameToMatch != null && !nameToMatch.equals(name)) {
                continue;
            }

            String volume = split[0].replaceAll(UNICODE_SPACE, Symbol.SPACE);
            String uuid = uuidMap != null ? uuidMap.getOrDefault(split[0], "") : "";

            String description;
            if (volume.startsWith("/dev")) {
                description = "Local Disk";
            } else if (volume.equals("tmpfs")) {
                description = "Ram Disk";
            } else if (NETWORK_FS_TYPES.contains(type)) {
                description = "Network Disk";
            } else {
                description = "Mount Point";
            }

            // Add in logical volume found at /dev/mapper, useful when linking
            // file system with drive.
            String logicalVolume = "";
            String volumeMapperDirectory = "/dev/mapper/";
            Path link = Paths.get(volume);
            if (link.toFile().exists() && Files.isSymbolicLink(link)) {
                try {
                    Path slink = Files.readSymbolicLink(link);
                    Path full = Paths.get(volumeMapperDirectory + slink.toString());
                    if (full.toFile().exists()) {
                        logicalVolume = full.normalize().toString();
                    }
                } catch (IOException e) {
                    Logger.warn("Couldn't access symbolic path  {}. {}", link, e.getMessage());
                }
            }

            long totalInodes = 0L;
            long freeInodes = 0L;
            long totalSpace = 0L;
            long usableSpace = 0L;
            long freeSpace = 0L;

            try {
                LibC.Statvfs vfsStat = new LibC.Statvfs();
                if (0 == LibC.INSTANCE.statvfs(path, vfsStat)) {
                    totalInodes = vfsStat.f_files.longValue();
                    freeInodes = vfsStat.f_ffree.longValue();
                    totalSpace = vfsStat.f_blocks.longValue() * vfsStat.f_bsize.longValue();
                    usableSpace = vfsStat.f_bavail.longValue() * vfsStat.f_bsize.longValue();
                    freeSpace = vfsStat.f_bfree.longValue() * vfsStat.f_bsize.longValue();
                } else {
                    File tmpFile = new File(path);
                    totalSpace = tmpFile.getTotalSpace();
                    usableSpace = tmpFile.getUsableSpace();
                    freeSpace = tmpFile.getFreeSpace();
                    Logger.warn("Failed to get information to use statvfs. path: {}, Error code: {}", path,
                            Native.getLastError());
                }
            } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
                Logger.error("Failed to get file counts from statvfs. {}", e.getMessage());
            }

            fsList.add(new LinuxOSFileStore(name, volume, name, path, options, uuid, logicalVolume, description, type,
                    freeSpace, usableSpace, totalSpace, freeInodes, totalInodes));
        }
        return fsList;
    }

    /**
     * Returns a value from the Linux system file /proc/sys/fs/file-nr.
     *
     * @param index The index of the value to retrieve. 0 returns the total allocated
     *              file descriptors. 1 returns the number of used file descriptors
     *              for kernel 2.4, or the number of unused file descriptors for
     *              kernel 2.6. 2 returns the maximum number of file descriptors that
     *              can be allocated.
     * @return Corresponding file descriptor value from the Linux system file.
     */
    private static long getFileDescriptors(int index) {
        String filename = ProcPath.SYS_FS_FILE_NR;
        if (index < 0 || index > 2) {
            throw new IllegalArgumentException("Index must be between 0 and 2.");
        }
        List<String> osDescriptors = FileKit.readLines(filename);
        if (!osDescriptors.isEmpty()) {
            String[] splittedLine = osDescriptors.get(0).split("\\D+");
            return Builder.parseLongOrDefault(splittedLine[index], 0L);
        }
        return 0L;
    }

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        // Map uuids with device path as key
        Map<String, String> uuidMap = new HashMap<>();
        File uuidDir = new File("/dev/disk/by-uuid");
        if (uuidDir.listFiles() != null) {
            for (File uuid : uuidDir.listFiles()) {
                try {
                    // Store UUID as value with path (e.g., /dev/sda1) as key
                    uuidMap.put(uuid.getCanonicalPath(), uuid.getName().toLowerCase());
                } catch (IOException e) {
                    Logger.error("Couldn't get canonical path for {}. {}", uuid.getName(), e.getMessage());
                }
            }
        }

        // List file systems
        return getFileStoreMatching(null, uuidMap, localOnly);
    }

    @Override
    public long getOpenFileDescriptors() {
        return getFileDescriptors(0);
    }

    @Override
    public long getMaxFileDescriptors() {
        return getFileDescriptors(2);
    }

}
