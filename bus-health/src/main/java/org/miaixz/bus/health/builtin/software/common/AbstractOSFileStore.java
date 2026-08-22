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
package org.miaixz.bus.health.builtin.software.common;

import org.miaixz.bus.core.lang.annotation.ThreadSafe;
import org.miaixz.bus.health.builtin.software.OSFileStore;

/**
 * Common implementations for OSFileStore
 *
 * @author Kimi Liu
 */
@ThreadSafe
public abstract class AbstractOSFileStore implements OSFileStore {

    /**
     * The name value.
     */
    private String name;

    /**
     * The volume value.
     */
    private String volume;

    /**
     * The label value.
     */
    private String label;

    /**
     * The mount value.
     */
    private String mount;

    /**
     * The options value.
     */
    private String options;

    /**
     * The uuid value.
     */
    private String uuid;

    /**
     * The local value.
     */
    private boolean local;

    /**
     * Creates a new AbstractOSFileStore instance.
     */
    public AbstractOSFileStore() {
        // No initialization required.
    }

    /**
     * Creates a new AbstractOSFileStore instance.
     *
     * @param name    the name
     * @param volume  the volume
     * @param label   the label
     * @param mount   the mount
     * @param options the options
     * @param uuid    the uuid
     * @param local   the local
     */
    protected AbstractOSFileStore(String name, String volume, String label, String mount, String options, String uuid,
            boolean local) {
        this.name = name;
        this.volume = volume;
        this.label = label;
        this.mount = mount;
        this.options = options;
        this.uuid = uuid;
        this.local = local;
    }

    /**
     * Returns the name.
     *
     * @return the get name result
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the volume.
     *
     * @return the get volume result
     */
    @Override
    public String getVolume() {
        return this.volume;
    }

    /**
     * Returns the label.
     *
     * @return the get label result
     */
    @Override
    public String getLabel() {
        return this.label;
    }

    /**
     * Returns the mount.
     *
     * @return the get mount result
     */
    @Override
    public String getMount() {
        return this.mount;
    }

    /**
     * Returns the options.
     *
     * @return the get options result
     */
    @Override
    public String getOptions() {
        return options;
    }

    /**
     * Returns the uuid.
     *
     * @return the get uuid result
     */
    @Override
    public String getUUID() {
        return this.uuid;
    }

    /**
     * Returns whether the local condition is true.
     *
     * @return the is local result
     */
    @Override
    public boolean isLocal() {
        return this.local;
    }

    /**
     * Clamps file-store space values to satisfy {@code 0 <= usable <= free <= total}.
     *
     * @param freeSpace   the free space in bytes
     * @param usableSpace the usable space in bytes
     * @param totalSpace  the total space in bytes
     * @return an array containing free, usable, and total space in that order
     */
    protected long[] clampSpace(long freeSpace, long usableSpace, long totalSpace) {
        long total = Math.max(0L, totalSpace);
        long free = Math.min(Math.max(0L, freeSpace), total);
        long usable = Math.min(Math.max(0L, usableSpace), free);
        return new long[] { free, usable, total };
    }

    /**
     * Returns the to string result.
     *
     * @return the to string result
     */
    @Override
    public String toString() {
        return "OSFileStore [name=" + getName() + ", volume=" + getVolume() + ", label=" + getLabel()
                + ", logicalVolume=" + getLogicalVolume() + ", mount=" + getMount() + ", description="
                + getDescription() + ", fsType=" + getType() + ", options=\"" + getOptions() + "\", uuid=" + getUUID()
                + ", isLocal=" + isLocal() + ", freeSpace=" + getFreeSpace() + ", usableSpace=" + getUsableSpace()
                + ", totalSpace=" + getTotalSpace() + ", freeInodes=" + getFreeInodes() + ", totalInodes="
                + getTotalInodes() + "]";
    }

}
