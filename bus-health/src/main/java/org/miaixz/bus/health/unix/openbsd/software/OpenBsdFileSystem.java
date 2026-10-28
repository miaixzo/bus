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
package org.miaixz.bus.health.unix.openbsd.software;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.sun.jna.platform.unix.Resource;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.common.AbstractFileSystem;
import org.miaixz.bus.health.unix.openbsd.OpenBsdSysctlKit;
import org.miaixz.bus.health.unix.shared.driver.MountTable;
import org.miaixz.bus.health.unix.shared.jna.OpenBsdLibc;

/**
 * The FreeBSD File System contains {@link OSFileStore}s which are a storage pool, device, partition, volume, concrete
 * file system or other implementation specific means of file storage.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class OpenBsdFileSystem extends AbstractFileSystem {

    /**
     * The FS_PATH_EXCLUDES constant.
     */
    private static final List<PathMatcher> FS_PATH_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Builder._UNIX_OPENBSD_FS_PATH_EXCLUDES);

    /**
     * The FS_PATH_INCLUDES constant.
     */
    private static final List<PathMatcher> FS_PATH_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Builder._UNIX_OPENBSD_FS_PATH_INCLUDES);

    /**
     * The FS_VOLUME_EXCLUDES constant.
     */
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Builder._UNIX_OPENBSD_FS_VOLUME_EXCLUDES);

    /**
     * The FS_VOLUME_INCLUDES constant.
     */
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Builder._UNIX_OPENBSD_FS_VOLUME_INCLUDES);

    /**
     * Creates a new OpenBsdFileSystem instance.
     */
    public OpenBsdFileSystem() {
        // No initialization required.
    }

    /**
     * Returns the file store matching.
     *
     * @param nameToMatch the name to match
     * @param localOnly   the local only
     * @return the get file store matching result
     */
    static List<OSFileStore> getFileStoreMatching(String nameToMatch, boolean localOnly) {
        List<OSFileStore> fsList = new ArrayList<>();

        // Get inode usage data
        String command = "df -i" + (localOnly ? " -l" : Normal.EMPTY);
        Pair<Map<String, Long>, Map<String, Long>> inodes = MountTable.parseDfInodes(Executor.runNative(command));
        Map<String, Long> inodeFreeMap = inodes.getLeft();
        Map<String, Long> inodeTotalMap = inodes.getRight();

        // Get mount table
        for (MountTable.Entry mount : MountTable.parseOpenBsdVerbose(Executor.runNative("mount -v"))) {
            String volume = mount.getVolume();
            String uuid = mount.getUuid();
            String path = mount.getPath();
            String type = mount.getType();
            String options = mount.getOptions();

            boolean isLocal = !NETWORK_FS_TYPES.contains(type);
            if ((localOnly && !isLocal)
                    || !path.equals(Symbol.SLASH) && (PSEUDO_FS_TYPES.contains(type) || Parsing.isFileStoreExcluded(
                            path,
                            volume,
                            FS_PATH_INCLUDES,
                            FS_PATH_EXCLUDES,
                            FS_VOLUME_INCLUDES,
                            FS_VOLUME_EXCLUDES))) {
                continue;
            }

            String name = path.substring(path.lastIndexOf(Symbol.C_SLASH) + 1);
            if (name.isEmpty()) {
                name = volume.substring(volume.lastIndexOf(Symbol.C_SLASH) + 1);
            }

            if (nameToMatch != null && !nameToMatch.equals(name)) {
                continue;
            }
            File f = new File(path);
            long totalSpace = f.getTotalSpace();
            long usableSpace = f.getUsableSpace();
            long freeSpace = f.getFreeSpace();

            String description;
            if (volume.startsWith(Symbol.SLASH + "dev") || path.equals(Symbol.SLASH)) {
                description = "Local Disk";
            } else if (volume.equals("tmpfs")) {
                description = "Ram Disk (dynamic)";
            } else if (volume.equals("mfs")) {
                description = "Ram Disk (fixed)";
            } else if (NETWORK_FS_TYPES.contains(type)) {
                description = "Network Disk";
            } else {
                description = "Mount Point";
            }

            fsList.add(
                    new OpenBsdOSFileStore(name, volume, name, path, options, uuid, isLocal, Normal.EMPTY, description,
                            type, freeSpace, usableSpace, totalSpace, inodeFreeMap.getOrDefault(path, 0L),
                            inodeTotalMap.getOrDefault(path, 0L)));
        }
        return fsList;
    }

    /**
     * Returns the file store matching.
     *
     * @param nameToMatch the name to match
     * @return the get file store matching result
     */
    static List<OSFileStore> getFileStoreMatching(String nameToMatch) {
        return getFileStoreMatching(nameToMatch, false);
    }

    /**
     * Returns the file stores.
     *
     * @param localOnly the local only
     * @return the get file stores result
     */
    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        return getFileStoreMatching(null, localOnly);
    }

    /**
     * Returns the open file descriptors.
     *
     * @return the get open file descriptors result
     */
    @Override
    public long getOpenFileDescriptors() {
        return OpenBsdSysctlKit.sysctl("kern.nfiles", 0);
    }

    /**
     * Returns the max file descriptors.
     *
     * @return the get max file descriptors result
     */
    @Override
    public long getMaxFileDescriptors() {
        return OpenBsdSysctlKit.sysctl("kern.maxfiles", 0);
    }

    /**
     * Returns the max file descriptors per process.
     *
     * @return the get max file descriptors per process result
     */
    @Override
    public long getMaxFileDescriptorsPerProcess() {
        Resource.Rlimit limit = new Resource.Rlimit();
        long processLimit = OpenBsdLibc.INSTANCE.getrlimit(Normal._8, limit) == Normal._0 ? limit.rlim_max : Normal.__1;
        return capAtSystemLimit(processLimit, getMaxFileDescriptors());
    }

}
