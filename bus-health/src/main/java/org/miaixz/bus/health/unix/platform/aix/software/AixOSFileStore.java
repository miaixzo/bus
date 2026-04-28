/*
 ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~ ~
 ~                                                                           ~
 ~ Copyright (c) 2015-2026 miaixz.org OSHI and other contributors.           ~
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
package org.miaixz.bus.health.unix.platform.aix.software;

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
public class AixOSFileStore extends AbstractOSFileStore {

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
     * Creates a new AixOSFileStore instance.
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
    public AixOSFileStore(String name, String volume, String label, String mount, String options, String uuid,
            boolean local, String logicalVolume, String description, String fsType, long freeSpace, long usableSpace,
            long totalSpace, long freeInodes, long totalInodes) {
        super(name, volume, label, mount, options, uuid, local);
        this.logicalVolume = logicalVolume;
        this.description = description;
        this.fsType = fsType;
        this.freeSpace = freeSpace;
        this.usableSpace = usableSpace;
        this.totalSpace = totalSpace;
        this.freeInodes = freeInodes;
        this.totalInodes = totalInodes;
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
        for (OSFileStore fileStore : AixFileSystem.getFileStoreMatching(getName(), isLocal())) {
            if (getVolume().equals(fileStore.getVolume()) && getMount().equals(fileStore.getMount())) {
                this.logicalVolume = fileStore.getLogicalVolume();
                this.description = fileStore.getDescription();
                this.fsType = fileStore.getType();
                this.freeSpace = fileStore.getFreeSpace();
                this.usableSpace = fileStore.getUsableSpace();
                this.totalSpace = fileStore.getTotalSpace();
                this.freeInodes = fileStore.getFreeInodes();
                this.totalInodes = fileStore.getTotalInodes();
                return true;
            }
        }
        return false;
    }

}
