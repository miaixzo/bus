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

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.software.OSFileStore;
import org.miaixz.bus.health.builtin.software.common.AbstractOSFileStore;

/**
 * OSFileStore implementation
 *
 * @author Kimi Liu
 * @since Java 21+
 */
@ThreadSafe
public class LinuxOSFileStore extends AbstractOSFileStore {

    /**
     * The logicalVolume value.
     */
    private String logicalVolume;

    /**
     * The description value.
     */
    private String description;

    /**
     * The fsType value.
     */
    private String fsType;

    /**
     * The freeSpace value.
     */
    private long freeSpace;

    /**
     * The usableSpace value.
     */
    private long usableSpace;

    /**
     * The totalSpace value.
     */
    private long totalSpace;

    /**
     * The freeInodes value.
     */
    private long freeInodes;

    /**
     * The totalInodes value.
     */
    private long totalInodes;

    /**
     * Whether this file store represents an NFS mount whose server was unreachable during enumeration.
     */
    private final boolean unreachable;

    /**
     * Creates a new LinuxOSFileStore instance.
     *
     * @param name          the name
     * @param volume        the volume
     * @param label         the label
     * @param mount         the mount
     * @param options       the options
     * @param uuid          the uuid
     * @param local         the local
     * @param logicalVolume the logical volume
     * @param description   the description
     * @param fsType        the fs type
     * @param freeSpace     the free space
     * @param usableSpace   the usable space
     * @param totalSpace    the total space
     * @param freeInodes    the free inodes
     * @param totalInodes   the total inodes
     */
    public LinuxOSFileStore(String name, String volume, String label, String mount, String options, String uuid,
            boolean local, String logicalVolume, String description, String fsType, long freeSpace, long usableSpace,
            long totalSpace, long freeInodes, long totalInodes) {
        this(name, volume, label, mount, options, uuid, local, logicalVolume, description, fsType, freeSpace,
                usableSpace, totalSpace, freeInodes, totalInodes, false);
    }

    /**
     * Creates a new LinuxOSFileStore instance.
     *
     * @param name          the name
     * @param volume        the volume
     * @param label         the label
     * @param mount         the mount
     * @param options       the options
     * @param uuid          the uuid
     * @param local         the local
     * @param logicalVolume the logical volume
     * @param description   the description
     * @param fsType        the fs type
     * @param freeSpace     the free space
     * @param usableSpace   the usable space
     * @param totalSpace    the total space
     * @param freeInodes    the free inodes
     * @param totalInodes   the total inodes
     * @param unreachable   whether the backing NFS server was unreachable
     */
    LinuxOSFileStore(String name, String volume, String label, String mount, String options, String uuid, boolean local,
            String logicalVolume, String description, String fsType, long freeSpace, long usableSpace, long totalSpace,
            long freeInodes, long totalInodes, boolean unreachable) {
        super(name, volume, label, mount, options, uuid, local);
        this.logicalVolume = logicalVolume;
        this.description = description;
        this.fsType = fsType;
        this.freeSpace = freeSpace;
        this.usableSpace = usableSpace;
        this.totalSpace = totalSpace;
        this.freeInodes = freeInodes;
        this.totalInodes = totalInodes;
        this.unreachable = unreachable;
    }

    /**
     * Returns the logical volume.
     *
     * @return the get logical volume result
     */
    @Override
    public String getLogicalVolume() {
        return this.logicalVolume;
    }

    /**
     * Returns the description.
     *
     * @return the get description result
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Returns the type.
     *
     * @return the get type result
     */
    @Override
    public String getType() {
        return this.fsType;
    }

    /**
     * Returns the free space.
     *
     * @return the get free space result
     */
    @Override
    public long getFreeSpace() {
        return this.freeSpace;
    }

    /**
     * Returns the usable space.
     *
     * @return the get usable space result
     */
    @Override
    public long getUsableSpace() {
        return this.usableSpace;
    }

    /**
     * Returns the total space.
     *
     * @return the get total space result
     */
    @Override
    public long getTotalSpace() {
        return this.totalSpace;
    }

    /**
     * Returns the free inodes.
     *
     * @return the get free inodes result
     */
    @Override
    public long getFreeInodes() {
        return this.freeInodes;
    }

    /**
     * Returns the total inodes.
     *
     * @return the get total inodes result
     */
    @Override
    public long getTotalInodes() {
        return this.totalInodes;
    }

    /**
     * Updates the attributes.
     *
     * @return the update attributes result
     */
    @Override
    public boolean updateAttributes() {
        if (this.unreachable) {
            for (OSFileStore fileStore : LinuxFileSystem
                    .getFileStoreMatching(getName(), LinuxFileSystem.buildUuidMap(), isLocal())) {
                if (getVolume().equals(fileStore.getVolume()) && getMount().equals(fileStore.getMount())) {
                    updateFrom(fileStore);
                    return true;
                }
            }
            return false;
        }
        long[] vfs = LinuxFileSystem.queryStatvfs(getMount());
        if (vfs != null) {
            long total = vfs[2];
            long usable = vfs[3];
            long free = vfs[4];
            if (total == 0L) {
                File f = new File(getMount());
                total = f.getTotalSpace();
                usable = f.getUsableSpace();
                free = f.getFreeSpace();
            }
            this.freeSpace = free;
            this.usableSpace = usable;
            this.totalSpace = total;
            this.freeInodes = vfs[1];
            this.totalInodes = vfs[0];
            return true;
        }
        for (OSFileStore fileStore : LinuxFileSystem
                .getFileStoreMatching(getName(), LinuxFileSystem.buildUuidMap(), isLocal())) {
            if (getVolume().equals(fileStore.getVolume()) && getMount().equals(fileStore.getMount())) {
                updateFrom(fileStore);
                return true;
            }
        }
        return false;
    }

    /**
     * Updates mutable fields from another file store.
     *
     * @param fileStore the source file store
     */
    private void updateFrom(OSFileStore fileStore) {
        this.logicalVolume = fileStore.getLogicalVolume();
        this.description = fileStore.getDescription();
        this.fsType = fileStore.getType();
        this.freeSpace = fileStore.getFreeSpace();
        this.usableSpace = fileStore.getUsableSpace();
        this.totalSpace = fileStore.getTotalSpace();
        this.freeInodes = fileStore.getFreeInodes();
        this.totalInodes = fileStore.getTotalInodes();
    }

}
