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
package org.miaixz.bus.health.unix.platform.freebsd.software;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.common.AbstractFileSystem;
import org.miaixz.bus.health.linux.software.LinuxOSFileStore;
import org.miaixz.bus.health.unix.platform.freebsd.BsdSysctlKit;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The FreeBSD File System contains {@link OSFileStore}s which are a storage pool, device, partition,
 * volume, concrete file system or other implementation specific means of file storage.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public final class FreeBsdFileSystem extends AbstractFileSystem {

    private static final List<PathMatcher> FS_PATH_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_FREEBSD_FS_PATH_EXCLUDES);
    private static final List<PathMatcher> FS_PATH_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_FREEBSD_FS_PATH_INCLUDES);
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_FREEBSD_FS_VOLUME_EXCLUDES);
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_FREEBSD_FS_VOLUME_INCLUDES);

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        // TODO map mount point to UUID?
        // is /etc/fstab useful for this?
        Map<String, String> uuidMap = new HashMap<>();
        // Now grab dmssg output
        String device = Normal.EMPTY;
        for (String line : Executor.runNative("geom part list")) {
            if (line.contains("Name: ")) {
                device = line.substring(line.lastIndexOf(' ') + 1);
            }
            // If we aren't working with a current partition, continue
            if (device.isEmpty()) {
                continue;
            }
            line = line.trim();
            if (line.startsWith("rawuuid:")) {
                uuidMap.put(device, line.substring(line.lastIndexOf(' ') + 1));
                device = Normal.EMPTY;
            }
        }

        List<OSFileStore> fsList = new ArrayList<>();

        // Get inode usage data
        Map<String, Long> inodeFreeMap = new HashMap<>();
        Map<String, Long> inodeTotalMap = new HashMap<>();
        for (String line : Executor.runNative("df -i")) {
            /*- Sample Output:
            Filesystem    1K-blocks   Used   Avail Capacity iused  ifree %iused  Mounted on
            /dev/twed0s1a   2026030 584112 1279836    31%    2751 279871    1%   /
            */
            if (line.startsWith("/")) {
                String[] split = RegEx.SPACES.split(line);
                if (split.length > 7) {
                    inodeFreeMap.put(split[0], Parsing.parseLongOrDefault(split[6], 0L));
                    // total is used + free
                    inodeTotalMap.put(split[0],
                            inodeFreeMap.get(split[0]) + Parsing.parseLongOrDefault(split[5], 0L));
                }
            }
        }

        // Get mount table
        for (String fs : Executor.runNative("mount -p")) {
            String[] split = RegEx.SPACES.split(fs);
            if (split.length < 5) {
                continue;
            }
            // 1st field is volume name
            // 2nd field is mount point
            // 3rd field is fs type
            // 4th field is options
            // other fields ignored
            String volume = split[0];
            String path = split[1];
            String type = split[2];
            String options = split[3];

            // Skip non-local drives if requested, and exclude pseudo file systems
            if ((localOnly && NETWORK_FS_TYPES.contains(type))
                    || !path.equals("/") && (PSEUDO_FS_TYPES.contains(type) || Builder.isFileStoreExcluded(path,
                    volume, FS_PATH_INCLUDES, FS_PATH_EXCLUDES, FS_VOLUME_INCLUDES, FS_VOLUME_EXCLUDES))) {
                continue;
            }

            String name = path.substring(path.lastIndexOf('/') + 1);
            // Special case for /, pull last element of volume instead
            if (name.isEmpty()) {
                name = volume.substring(volume.lastIndexOf('/') + 1);
            }
            File f = new File(path);
            long totalSpace = f.getTotalSpace();
            long usableSpace = f.getUsableSpace();
            long freeSpace = f.getFreeSpace();

            String description;
            if (volume.startsWith("/dev") || path.equals("/")) {
                description = "Local Disk";
            } else if (volume.equals("tmpfs")) {
                description = "Ram Disk";
            } else if (NETWORK_FS_TYPES.contains(type)) {
                description = "Network Disk";
            } else {
                description = "Mount Point";
            }
            // Match UUID
            String uuid = uuidMap.getOrDefault(name, Normal.EMPTY);

            fsList.add(new LinuxOSFileStore(name, volume, name, path, options, uuid, Normal.EMPTY, description, type, freeSpace,
                    usableSpace, totalSpace, inodeFreeMap.containsKey(path) ? inodeFreeMap.get(path) : 0L,
                    inodeTotalMap.containsKey(path) ? inodeTotalMap.get(path) : 0L));
        }
        return fsList;
    }

    @Override
    public long getOpenFileDescriptors() {
        return BsdSysctlKit.sysctl("kern.openfiles", 0);
    }

    @Override
    public long getMaxFileDescriptors() {
        return BsdSysctlKit.sysctl("kern.maxfiles", 0);
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        // On FreeBsd there is no process specific system-wide limit, so the general limit is returned
        return getMaxFileDescriptors();
    }
}
