/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                               ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.               ~
 ~                                                                               ~
 ~ Licensed under the Apache License, Version 2.0 (the "License");               ~
 ~ you may not use this file except in compliance with the License.              ~
 ~ You may obtain a copy of the License at                                       ~
 ~                                                                               ~
 ~      https://www.apache.org/licenses/LICENSE-2.0                              ~
 ~                                                                               ~
 ~ Unless required by applicable law or agreed to in writing, software           ~
 ~ distributed under the License is distributed on an "AS IS" BASIS,             ~
 ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.      ~
 ~ See the License for the specific language governing permissions and           ~
 ~ limitations under the License.                                                ~
 ~                                                                               ~
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
*/
package org.miaixz.bus.health.unix.platform.solaris.software;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.*;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.common.AbstractFileSystem;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit;
import org.miaixz.bus.health.unix.platform.solaris.KstatKit.KstatChain;

import com.sun.jna.platform.unix.solaris.LibKstat.Kstat;

/**
 * The Solaris File System contains {@link OSFileStore}s which are a storage pool, device, partition, volume, concrete
 * file system or other implementation specific means of file storage. In Solaris, these are found in the /proc/mount
 * filesystem, excluding temporary and kernel mounts.
 *
 * @author Kimi Liu
 * @since Java 17+
 */
@ThreadSafe
public class SolarisFileSystem extends AbstractFileSystem {

    private static final Supplier<Pair<Long, Long>> FILE_DESC = Memoizer
            .memoize(SolarisFileSystem::queryFileDescriptors, Memoizer.defaultExpiration());
    private static final List<PathMatcher> FS_PATH_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_SOLARIS_FS_PATH_EXCLUDES);
    private static final List<PathMatcher> FS_PATH_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_SOLARIS_FS_PATH_INCLUDES);
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_SOLARIS_FS_VOLUME_EXCLUDES);
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._UNIX_SOLARIS_FS_VOLUME_INCLUDES);

    static List<OSFileStore> getFileStoreMatching(String nameToMatch, boolean localOnly) {
        List<OSFileStore> fsList = new ArrayList<>();

        // Get inode usage data
        Map<String, Long> inodeFreeMap = new HashMap<>();
        Map<String, Long> inodeTotalMap = new HashMap<>();
        String key = null;
        String total = null;
        String free = null;
        String command = "df -g" + (localOnly ? " -l" : Normal.EMPTY);
        for (String line : Executor.runNative(command)) {
            /*- Sample Output:
            /                  (/dev/md/dsk/d0    ):         8192 block size          1024 frag size
            41310292 total blocks   18193814 free blocks 17780712 available        2486848 total files
             2293351 free files     22282240 filesys id
                 ufs fstype       0x00000004 flag             255 filename length
            */
            if (line.startsWith("/")) {
                key = Pattern.SPACES_PATTERN.split(line)[0];
                total = null;
            } else if (line.contains("available") && line.contains("total files")) {
                total = Parsing.getTextBetweenStrings(line, "available", "total files").trim();
            } else if (line.contains("free files")) {
                free = Parsing.getTextBetweenStrings(line, Normal.EMPTY, "free files").trim();
                if (key != null && total != null) {
                    inodeFreeMap.put(key, Parsing.parseLongOrDefault(free, 0L));
                    inodeTotalMap.put(key, Parsing.parseLongOrDefault(total, 0L));
                    key = null;
                }
            }
        }

        // Get mount table
        for (String fs : Executor.runNative("cat /etc/mnttab")) { // NOSONAR squid:S135
            String[] split = Pattern.SPACES_PATTERN.split(fs);
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
            boolean isLocal = !NETWORK_FS_TYPES.contains(type);
            if ((localOnly && !isLocal)
                    || !path.equals("/") && (PSEUDO_FS_TYPES.contains(type) || Builder.isFileStoreExcluded(
                            path,
                            volume,
                            FS_PATH_INCLUDES,
                            FS_PATH_EXCLUDES,
                            FS_VOLUME_INCLUDES,
                            FS_VOLUME_EXCLUDES))) {
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

            fsList.add(
                    new SolarisOSFileStore(name, volume, name, path, options, "", isLocal, "", description, type,
                            freeSpace, usableSpace, totalSpace, inodeFreeMap.getOrDefault(path, 0L),
                            inodeTotalMap.getOrDefault(path, 0L)));
        }
        return fsList;
    }

    private static Pair<Long, Long> queryFileDescriptors() {
        Object[] results = KstatKit.queryKstat2("kstat:/kmem_cache/kmem_default/file_cache", "buf_inuse", "buf_max");
        long inuse = results[0] == null ? 0L : (long) results[0];
        long max = results[1] == null ? 0L : (long) results[1];
        return Pair.of(inuse, max);
    }

    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        return getFileStoreMatching(null, localOnly);
    }

    @Override
    public long getOpenFileDescriptors() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return FILE_DESC.get().getLeft();
        }
        try (KstatChain kc = KstatKit.openChain()) {
            Kstat ksp = kc.lookup(null, -1, "file_cache");
            // Set values
            if (ksp != null && kc.read(ksp)) {
                return KstatKit.dataLookupLong(ksp, "buf_inuse");
            }
        }
        return 0L;
    }

    @Override
    public long getMaxFileDescriptors() {
        if (SolarisOperatingSystem.HAS_KSTAT2) {
            // Use Kstat2 implementation
            return FILE_DESC.get().getRight();
        }
        try (KstatChain kc = KstatKit.openChain()) {
            Kstat ksp = kc.lookup(null, -1, "file_cache");
            // Set values
            if (ksp != null && kc.read(ksp)) {
                return KstatKit.dataLookupLong(ksp, "buf_max");
            }
        }
        return 0L;
    }

    @Override
    public long getMaxFileDescriptorsPerProcess() {
        final List<String> lines = Builder.readFile("/etc/system");
        for (final String line : lines) {
            if (line.startsWith("set rlim_fd_max")) {
                return Parsing.parseLastLong(line, 65536L);
            }
        }
        return 65536L; // 65536 is the default value for the process open file limit in Solaris
    }

}
