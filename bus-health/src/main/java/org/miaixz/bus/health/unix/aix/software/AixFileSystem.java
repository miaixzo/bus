/*********************************************************************************
 *                                                                               *
 * The MIT License (MIT)                                                         *
 *                                                                               *
 * Copyright (c) 2015-2024 miaixz.org OSHI and other contributors.               *
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
package org.miaixz.bus.health.unix.aix.software;

import org.miaixz.bus.core.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.RegEx;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.builtin.software.AbstractFileSystem;
import org.miaixz.bus.health.builtin.software.OSFileStore;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AIX File System contains {@link OSFileStore}s which are
 * a storage pool, device, partition, volume, concrete file system or other
 * implementation specific means of file storage.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class AixFileSystem extends AbstractFileSystem {

    public static final String AIX_FS_PATH_EXCLUDES = "bus.health.os.aix.filesystem.path.excludes";
    public static final String AIX_FS_PATH_INCLUDES = "bus.health.os.aix.filesystem.path.includes";
    public static final String AIX_FS_VOLUME_EXCLUDES = "bus.health.os.aix.filesystem.volume.excludes";
    public static final String AIX_FS_VOLUME_INCLUDES = "bus.health.os.aix.filesystem.volume.includes";

    private static final List<PathMatcher> FS_PATH_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(AIX_FS_PATH_EXCLUDES);
    private static final List<PathMatcher> FS_PATH_INCLUDES = Builder
            .loadAndParseFileSystemConfig(AIX_FS_PATH_INCLUDES);
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(AIX_FS_VOLUME_EXCLUDES);
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = Builder
            .loadAndParseFileSystemConfig(AIX_FS_VOLUME_INCLUDES);

    // Called by AixOSFileStore
    static List<OSFileStore> getFileStoreMatching(String nameToMatch) {
        return getFileStoreMatching(nameToMatch, false);
    }

    private static List<OSFileStore> getFileStoreMatching(String nameToMatch, boolean localOnly) {
        List<OSFileStore> fsList = new ArrayList<>();

        // Get inode usage data
        Map<String, Long> inodeFreeMap = new HashMap<>();
        Map<String, Long> inodeTotalMap = new HashMap<>();
        String command = "df -i" + (localOnly ? " -l" : Normal.EMPTY);
        for (String line : Executor.runNative(command)) {
            /*- Sample Output:
             $ df -i
            Filesystem            Inodes   IUsed   IFree IUse% Mounted on
            /dev/hd4               75081   16741   58340   23% /
            /dev/hd2              269640   43104  226536   16% /usr
            /dev/hd9var            43598    1370   42228    4% /var
            /dev/hd3               79936     386   79550    1% /tmp
            /dev/hd11admin         29138       7   29131    1% /admin
            /proc                      0       0       0    -  /proc
            /dev/hd10opt           47477    4232   43245    9% /opt
            /dev/livedump          58204       4   58200    1% /var/adm/ras/livedump
            /dev/fslv00          12419240  292668 12126572    3% /home
            */
            if (line.startsWith("/")) {
                String[] split = RegEx.SPACES.split(line);
                if (split.length > 5) {
                    inodeTotalMap.put(split[0], Builder.parseLongOrDefault(split[1], 0L));
                    inodeFreeMap.put(split[0], Builder.parseLongOrDefault(split[3], 0L));
                }
            }
        }

        // Get mount table
        for (String fs : Executor.runNative("mount")) {
            /*- Sample Output:
             *   node       mounted        mounted over    vfs       date        options
             * -------- ---------------  ---------------  ------ ------------ ---------------
             *          /dev/hd4         /                jfs2   Jun 16 09:12 rw,log=/dev/hd8
             *          /dev/hd2         /usr             jfs2   Jun 16 09:12 rw,log=/dev/hd8
             *          /dev/hd9var      /var             jfs2   Jun 16 09:12 rw,log=/dev/hd8
             *          /dev/hd3         /tmp             jfs2   Jun 16 09:12 rw,log=/dev/hd8
             *          /dev/hd11admin   /admin           jfs2   Jun 16 09:13 rw,log=/dev/hd8
             *          /proc            /proc            procfs Jun 16 09:13 rw
             *          /dev/hd10opt     /opt             jfs2   Jun 16 09:13 rw,log=/dev/hd8
             *          /dev/livedump    /var/adm/ras/livedump jfs2   Jun 16 09:13 rw,log=/dev/hd8
             * foo      /dev/fslv00      /home            jfs2   Jun 16 09:13 rw,log=/dev/loglv00
             */
            // Lines begin with optional node, which we don't use. To force sensible split
            // behavior, append any character at the beginning of the string
            String[] split = RegEx.SPACES.split("x" + fs);
            if (split.length > 7) {
                // 1st field is volume name [0-index]
                // 2nd field is mount point
                // 3rd field is fs type
                // 4th-6th fields are date, ignored
                // 7th field is options
                String volume = split[1];
                String path = split[2];
                String type = split[3];
                String options = split[4];

                // Skip non-local drives if requested, and exclude pseudo file systems
                if ((localOnly && NETWORK_FS_TYPES.contains(type)) || !path.equals("/")
                        && (PSEUDO_FS_TYPES.contains(type) || Builder.isFileStoreExcluded(path, volume,
                        FS_PATH_INCLUDES, FS_PATH_EXCLUDES, FS_VOLUME_INCLUDES, FS_VOLUME_EXCLUDES))) {
                    continue;
                }

                String name = path.substring(path.lastIndexOf('/') + 1);
                // Special case for /, pull last element of volume instead
                if (name.isEmpty()) {
                    name = volume.substring(volume.lastIndexOf('/') + 1);
                }

                if (nameToMatch != null && !nameToMatch.equals(name)) {
                    continue;
                }
                File f = new File(path);
                if (!f.exists() || f.getTotalSpace() < 0) {
                    continue;
                }
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

                fsList.add(new AixOSFileStore(name, volume, name, path, options, Normal.EMPTY, Normal.EMPTY, description, type, freeSpace,
                        usableSpace, totalSpace, inodeFreeMap.getOrDefault(volume, 0L),
                        inodeTotalMap.getOrDefault(volume, 0L)));
            }
        }
        return fsList;
    }

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        return getFileStoreMatching(null, localOnly);
    }

    @Override
    public long getOpenFileDescriptors() {
        boolean header = false;
        long openfiles = 0L;
        for (String f : Executor.runNative("lsof -nl")) {
            if (!header) {
                header = f.startsWith("COMMAND");
            } else {
                openfiles++;
            }
        }
        return openfiles;
    }

    @Override
    public long getMaxFileDescriptors() {
        return Builder.parseLongOrDefault(Executor.getFirstAnswer("ulimit -n"), 0L);
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        final List<String> lines = Builder.readFile("/etc/security/limits");
        for (final String line : lines) {
            if (line.trim().startsWith("nofiles")) {
                return Builder.parseLastLong(line, Long.MAX_VALUE);
            }
        }
        return Long.MAX_VALUE;
    }

}
