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
package org.miaixz.bus.health.linux.software;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;

import com.sun.jna.Native;
import com.sun.jna.platform.linux.LibC;

import org.miaixz.bus.core.center.regex.Pattern;
import org.miaixz.bus.core.lang.Normal;
import org.miaixz.bus.core.lang.Symbol;
import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.Builder;
import org.miaixz.bus.health.Config;
import org.miaixz.bus.health.Executor;
import org.miaixz.bus.health.Parsing;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.common.AbstractFileSystem;
import org.miaixz.bus.health.linux.DevPath;
import org.miaixz.bus.health.linux.ProcPath;
import org.miaixz.bus.logger.Logger;

/**
 * The Linux File System contains {@link OSFileStore}s which are a storage pool, device, partition, volume, concrete
 * file system or other implementation specific means of file storage. In Linux, these are found in the /proc/mount
 * filesystem, excluding temporary and kernel mounts.
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class LinuxFileSystem extends AbstractFileSystem {

    /**
     * Constructs a new {@code LinuxFileSystem} instance.
     */
    public LinuxFileSystem() {
        // No initialization required.
    }

    /**
     * The FS_PATH_EXCLUDES constant.
     */
    private static final List<PathMatcher> FS_PATH_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._LINUX_FS_PATH_EXCLUDES);

    /**
     * The FS_PATH_INCLUDES constant.
     */
    private static final List<PathMatcher> FS_PATH_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._LINUX_FS_PATH_INCLUDES);

    /**
     * The FS_VOLUME_EXCLUDES constant.
     */
    private static final List<PathMatcher> FS_VOLUME_EXCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._LINUX_FS_VOLUME_EXCLUDES);

    /**
     * The FS_VOLUME_INCLUDES constant.
     */
    private static final List<PathMatcher> FS_VOLUME_INCLUDES = Builder
            .loadAndParseFileSystemConfig(Config._LINUX_FS_VOLUME_INCLUDES);

    /**
     * The UNICODE_SPACE constant.
     */
    private static final String UNICODE_SPACE = "\\040";

    /**
     * Whether NFS mounts should be checked for reachability before querying filesystem statistics.
     */
    private static final boolean CHECK_NFS = Config.get(Config._LINUX_FILESYSTEM_CHECKNFS, true);

    /**
     * Pattern matching {@code addr=} or {@code mountaddr=} in NFS mount options.
     */
    private static final java.util.regex.Pattern NFS_ADDR_PATTERN = java.util.regex.Pattern.compile(
            "(?:^|,)(?:mount)?addr=([^,]+)");

    /**
     * Maximum number of concurrent NFS reachability probe threads.
     */
    private static final int NFS_PROBE_MAX_THREADS = 64;

    /**
     * Queries the statvfs.
     *
     * @param path the path
     * @return the query statvfs result
     */
    static long[] queryStatvfs(String path) {
        try {
            LibC.Statvfs vfsStat = new LibC.Statvfs();
            if (0 == LibC.INSTANCE.statvfs(path, vfsStat)) {
                long frsize = vfsStat.f_frsize.longValue();
                return new long[] { vfsStat.f_files.longValue(), vfsStat.f_ffree.longValue(),
                        vfsStat.f_blocks.longValue() * frsize, vfsStat.f_bavail.longValue() * frsize,
                        vfsStat.f_bfree.longValue() * frsize };
            }
            Logger.warn(
                    false,
                    "Health",
                    "Failed to get information to use statvfs. path: {}, Error code: {}",
                    path,
                    Native.getLastError());
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            Logger.error(false, "Health", "Failed to get file counts from statvfs. {}", e.getClass().getSimpleName());
        }
        return null;
    }

    /**
     * Builds a map of filesystem UUIDs to device paths.
     *
     * @return the UUID map
     */
    static Map<String, String> buildUuidMap() {
        // Map of volume with device path as key
        Map<String, String> volumeDeviceMap = new HashMap<>();
        File devMapper = new File(DevPath.MAPPER);
        File[] volumes = devMapper.listFiles();
        if (volumes != null) {
            for (File volume : volumes) {
                try {
                    volumeDeviceMap.put(volume.getCanonicalPath(), volume.getAbsolutePath());
                } catch (IOException e) {
                    Logger.debug(
                            false,
                            "Health",
                            "Couldn't get canonical path for {}. {}",
                            volume.getName(),
                            e.getClass().getSimpleName());
                }
            }
        }
        // Map uuids with device path as key
        Map<String, String> uuidMap = new HashMap<>();
        File uuidDir = new File(DevPath.DISK_BY_UUID);
        File[] uuids = uuidDir.listFiles();
        if (uuids != null) {
            for (File uuid : uuids) {
                try {
                    // Store UUID as value with path (e.g., /dev/sda1) and volumes as key
                    String canonicalPath = uuid.getCanonicalPath();
                    uuidMap.put(canonicalPath, uuid.getName().toLowerCase(Locale.ROOT));
                    if (volumeDeviceMap.containsKey(canonicalPath)) {
                        uuidMap.put(volumeDeviceMap.get(canonicalPath), uuid.getName().toLowerCase(Locale.ROOT));
                    }
                } catch (IOException e) {
                    Logger.debug(
                            false,
                            "Health",
                            "Couldn't get canonical path for {}. {}",
                            uuid.getName(),
                            e.getClass().getSimpleName());
                }
            }
        }
        return uuidMap;
    }

    /**
     * Returns the file store matching.
     *
     * @param nameToMatch the name to match
     * @param uuidMap     the uuid map
     * @param localOnly   the local only
     * @return the get file store matching result
     */
    static List<OSFileStore> getFileStoreMatching(String nameToMatch, Map<String, String> uuidMap, boolean localOnly) {
        List<OSFileStore> fsList = new ArrayList<>();

        Map<String, String> labelMap = queryLabelMap();

        // Parse /proc/mounts to get fs types
        List<String> mounts = Builder.readFile(ProcPath.MOUNTS);
        Map<String, Boolean> nfsHostReachable = CHECK_NFS && !localOnly ? probeNfsHosts(mounts)
                : Collections.emptyMap();
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
            String volume = split[0].replace(UNICODE_SPACE, Symbol.SPACE);
            String name = volume;
            String path = split[1].replace(UNICODE_SPACE, Symbol.SPACE);
            if (path.equals("/")) {
                name = "/";
            }
            String type = split[2];

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

            String options = split[3];

            // If only updating for one name, skip others
            if (nameToMatch != null && !nameToMatch.equals(name)) {
                continue;
            }

            String uuid = uuidMap != null ? uuidMap.getOrDefault(split[0], Normal.EMPTY) : Normal.EMPTY;

            String description;
            if (volume.startsWith(DevPath.DEV)) {
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
            String logicalVolume = Normal.EMPTY;
            Path link = Paths.get(volume);
            if (link.toFile().exists() && Files.isSymbolicLink(link)) {
                try {
                    Path slink = Files.readSymbolicLink(link);
                    Path full = Paths.get(DevPath.MAPPER + slink.toString());
                    if (full.toFile().exists()) {
                        logicalVolume = full.normalize().toString();
                    }
                } catch (IOException e) {
                    Logger.warn(
                            false,
                            "Health",
                            "Couldn't access symbolic path  {}. {}",
                            link,
                            e.getClass().getSimpleName());
                }
            }

            long totalInodes = 0L;
            long freeInodes = 0L;
            long totalSpace = 0L;
            long usableSpace = 0L;
            long freeSpace = 0L;

            if (CHECK_NFS && isNfsType(type)) {
                String host = parseNfsAddr(options);
                if (host != null && Boolean.FALSE.equals(nfsHostReachable.get(host))) {
                    description = "Network Disk [unreachable]";
                    fsList.add(
                            new LinuxOSFileStore(
                                    name,
                                    volume,
                                    labelMap.getOrDefault(path, name),
                                    path,
                                    options,
                                    uuid,
                                    isLocal,
                                    logicalVolume,
                                    description,
                                    type,
                                    0L,
                                    0L,
                                    0L,
                                    0L,
                                    0L,
                                    true));
                    continue;
                }
            }

            long[] vfs = queryStatvfs(path);
            if (vfs != null) {
                totalInodes = vfs[0];
                freeInodes = vfs[1];
                totalSpace = vfs[2];
                usableSpace = vfs[3];
                freeSpace = vfs[4];
            }
            // If native methods failed use JVM methods
            if (totalSpace == 0L) {
                File tmpFile = new File(path);
                totalSpace = tmpFile.getTotalSpace();
                usableSpace = tmpFile.getUsableSpace();
                freeSpace = tmpFile.getFreeSpace();
            }

            fsList.add(
                    new LinuxOSFileStore(name, volume, labelMap.getOrDefault(path, name), path, options, uuid, isLocal,
                            logicalVolume, description, type, freeSpace, usableSpace, totalSpace, freeInodes,
                            totalInodes));
        }
        return fsList;
    }

    /**
     * Returns whether the filesystem type uses the NFS protocol.
     *
     * @param type the filesystem type
     * @return {@code true} for {@code nfs} or {@code nfs4}
     */
    static boolean isNfsType(String type) {
        return "nfs".equals(type) || "nfs4".equals(type);
    }

    /**
     * Extracts the NFS server address from mount options.
     *
     * @param options the mount options field
     * @return the server address, or {@code null} if unavailable
     */
    static String parseNfsAddr(String options) {
        Matcher matcher = NFS_ADDR_PATTERN.matcher(options);
        return matcher.find() ? matcher.group(1) : null;
    }

    /**
     * Attempts a TCP connection to the target host and port.
     *
     * @param host      the target host
     * @param port      the target port
     * @param timeoutMs the connection timeout in milliseconds
     * @return {@code true} if the connection succeeds, otherwise {@code false}
     */
    private static boolean tcpReachable(String host, int port, int timeoutMs) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), timeoutMs);
            return true;
        } catch (IOException e) {
            Logger.debug(false, "Health", "NFS host {} not reachable on port {}: {}", host, port, e.getMessage());
            return false;
        }
    }

    /**
     * Probes unique NFS hosts from mount entries in parallel.
     *
     * @param mounts the lines read from {@code /proc/mounts}
     * @return a map of host address to reachability
     */
    private static Map<String, Boolean> probeNfsHosts(List<String> mounts) {
        Set<String> hosts = new HashSet<>();
        for (String mount : mounts) {
            String[] split = mount.split(Symbol.SPACE);
            if (split.length >= 6 && isNfsType(split[2])) {
                String host = parseNfsAddr(split[3]);
                if (host != null) {
                    hosts.add(host);
                }
            }
        }
        Map<String, Boolean> reachable = new ConcurrentHashMap<>();
        if (hosts.isEmpty()) {
            return reachable;
        }
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(hosts.size(), NFS_PROBE_MAX_THREADS));
        try {
            CompletableFuture<?>[] futures = hosts.stream()
                    .map(host -> CompletableFuture.runAsync(
                            () -> reachable.put(host, tcpReachable(host, 2049, 2_000)),
                            pool))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
        } finally {
            pool.shutdownNow();
        }
        return reachable;
    }

    /**
     * Queries the label map.
     *
     * @return the query label map result
     */
    private static Map<String, String> queryLabelMap() {
        Map<String, String> labelMap = new HashMap<>();
        for (String line : Executor.runNative("lsblk -o mountpoint,label")) {
            String[] split = Pattern.SPACES_PATTERN.split(line, 2);
            if (split.length == 2) {
                labelMap.put(split[0], split[1]);
            }
        }
        return labelMap;
    }

    /**
     * Returns a value from the Linux system file /proc/sys/fs/file-nr.
     *
     * @param index The index of the value to retrieve. 0 returns the total allocated file descriptors. 1 returns the
     *              number of used file descriptors for kernel 2.4, or the number of unused file descriptors for kernel
     *              2.6. 2 returns the maximum number of file descriptors that can be allocated.
     * @return Corresponding file descriptor value from the Linux system file.
     */
    private static long getFileDescriptors(int index) {
        String filename = ProcPath.SYS_FS_FILE_NR;
        if (index < 0 || index > 2) {
            throw new IllegalArgumentException("Index must be between 0 and 2.");
        }
        List<String> osDescriptors = Builder.readFile(filename);
        if (!osDescriptors.isEmpty()) {
            String[] splittedLine = osDescriptors.get(0).split("\\D+");
            return Parsing.parseLongOrDefault(splittedLine[index], 0L);
        }
        return 0L;
    }

    /**
     * Returns the file descriptors per process.
     *
     * @return the get file descriptors per process result
     */
    private static long getFileDescriptorsPerProcess() {
        return Builder.getLongFromFile(ProcPath.SYS_FS_FILE_MAX);
    }

    /**
     * Returns the file stores.
     *
     * @param localOnly the local only
     * @return the get file stores result
     */
    @Override
    public List<OSFileStore> getFileStores(boolean localOnly) {
        return getFileStoreMatching(null, buildUuidMap(), localOnly);
    }

    /**
     * Returns the open file descriptors.
     *
     * @return the get open file descriptors result
     */
    @Override
    public long getOpenFileDescriptors() {
        return getFileDescriptors(0);
    }

    /**
     * Returns the max file descriptors.
     *
     * @return the get max file descriptors result
     */
    @Override
    public long getMaxFileDescriptors() {
        return getFileDescriptors(2);
    }

    /**
     * Returns the max file descriptors per process.
     *
     * @return the get max file descriptors per process result
     */
    @Override
    public long getMaxFileDescriptorsPerProcess() {
        return getFileDescriptorsPerProcess();
    }

}
