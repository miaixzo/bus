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
package org.miaixz.bus.health.unix.freebsd.software;

import java.io.File;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.core.lang.tuple.Pair;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.common.AbstractFileSystem;
import org.miaixz.bus.health.unix.freebsd.BsdSysctlKit;
import org.miaixz.bus.health.unix.shared.driver.MountTable;

/**
 * The FreeBSD File System contains {@link OSFileStore}s which are a storage pool, device, partition, volume, concrete
 * file system or other implementation specific means of file storage.
 *
 * @author Kimi Liu
 */
@ThreadSafe
public class FreeBsdFileSystem extends AbstractFileSystem {

    /**
     * The FS_PATH_EXCLUDES constant.
     */
    private static final List<PathMatcher> FS_PATH_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Builder._UNIX_FREEBSD_FS_PATH_EXCLUDES);

    /**
     * The FS_PATH_INCLUDES constant.
     */
    private static final List<PathMatcher> FS_PATH_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Builder._UNIX_FREEBSD_FS_PATH_INCLUDES);

    /**
     * The FS_VOLUME_EXCLUDES constant.
     */
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Builder._UNIX_FREEBSD_FS_VOLUME_EXCLUDES);

    /**
     * The FS_VOLUME_INCLUDES constant.
     */
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Builder._UNIX_FREEBSD_FS_VOLUME_INCLUDES);

    /**
     * Constructs a new FreeBsdFileSystem instance.
     */
    public FreeBsdFileSystem() {
        // No initialization required.
    }

    /**
     * Returns the file stores.
     *
     * @param localOnly the local only
     * @return the get file stores result
     */
    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        Map<String, String> uuidMap = new HashMap<>();
        String device = Normal.EMPTY;
        String uuid = Normal.EMPTY;
        for (String line : Executor.runNative("geom part list")) {
            line = line.trim();
            if (line.contains("Name: ")) {
                device = line.substring(line.lastIndexOf(Symbol.C_SPACE) + 1);
                uuid = Normal.EMPTY;
            } else if (!device.isEmpty() && line.startsWith("rawuuid:")) {
                uuid = line.substring(line.lastIndexOf(Symbol.C_SPACE) + 1);
                uuidMap.put(device, uuid);
                uuidMap.put("gptid/" + uuid, uuid);
            } else if (!uuid.isEmpty() && line.startsWith("label:")) {
                String label = line.substring("label:".length()).trim();
                if (!label.isEmpty() && !"(null)".equals(label)) {
                    uuidMap.put("gpt/" + label, uuid);
                }
            }
        }

        List<OSFileStore> fsList = new ArrayList<>();

        // Get inode usage data
        Pair<Map<String, Long>, Map<String, Long>> inodes = MountTable.parseDfInodes(Executor.runNative("df -i"));
        Map<String, Long> inodeFreeMap = inodes.getLeft();
        Map<String, Long> inodeTotalMap = inodes.getRight();

        // Get mount table
        List<MountTable.Entry> mounts = MountTable.parseFreeBsdLibxo(Executor.runNative("mount -p --libxo json"));
        if (mounts.isEmpty()) {
            mounts = MountTable.parseFstab(Executor.runNative("mount -p"));
        }
        for (MountTable.Entry mount : mounts) {
            String volume = mount.getVolume();
            String path = mount.getPath();
            String type = mount.getType();
            String options = mount.getOptions();

            // Skip non-local drives if requested, and exclude pseudo file systems
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
            // Special case for /, pull last element of volume instead
            if (name.isEmpty()) {
                name = volume.substring(volume.lastIndexOf(Symbol.C_SLASH) + 1);
            }
            File f = new File(path);
            long totalSpace = f.getTotalSpace();
            long usableSpace = f.getUsableSpace();
            long freeSpace = f.getFreeSpace();

            String description;
            if (volume.startsWith(Symbol.SLASH + "dev") || path.equals(Symbol.SLASH)) {
                description = "Local Disk";
            } else if (volume.equals("tmpfs")) {
                description = "Ram Disk";
            } else if (NETWORK_FS_TYPES.contains(type)) {
                description = "Network Disk";
            } else {
                description = "Mount Point";
            }
            // Match UUID
            String volumeUuid = volume.startsWith("/dev/")
                    ? uuidMap.getOrDefault(volume.substring("/dev/".length()), Normal.EMPTY)
                    : Normal.EMPTY;

            fsList.add(
                    new FreeBsdOSFileStore(name, volume, name, path, options, volumeUuid, isLocal, Normal.EMPTY,
                            description, type, freeSpace, usableSpace, totalSpace, inodeFreeMap.getOrDefault(path, 0L),
                            inodeTotalMap.getOrDefault(path, 0L)));
        }
        return fsList;
    }

    /**
     * Returns the open file descriptors.
     *
     * @return the get open file descriptors result
     */
    @Override
    public long getOpenFileDescriptors() {
        return BsdSysctlKit.sysctl("kern.openfiles", 0);
    }

    /**
     * Returns the max file descriptors.
     *
     * @return the get max file descriptors result
     */
    @Override
    public long getMaxFileDescriptors() {
        return BsdSysctlKit.sysctl("kern.maxfiles", 0);
    }

    /**
     * Returns the max file descriptors per process.
     *
     * @return the get max file descriptors per process result
     */
    @Override
    public long getMaxFileDescriptorsPerProcess() {
        return BsdSysctlKit.sysctl("kern.maxfilesperproc", 0);
    }

}
